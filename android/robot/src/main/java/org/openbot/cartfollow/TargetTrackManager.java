package org.openbot.cartfollow;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import org.openbot.tflite.Detector.Recognition;

public class TargetTrackManager {
  public static final class TwoStageUpdateResult {
    public final List<Recognition> continuedLowConfidence;

    TwoStageUpdateResult(List<Recognition> continuedLowConfidence) {
      this.continuedLowConfidence = new ArrayList<>(continuedLowConfidence);
    }
  }

  private static final float CENTER_GATE_RATIO = 0.35f;
  private static final float AREA_RATIO_MIN = 0.25f;
  private static final float AREA_RATIO_MAX = 4.00f;
  private static final float IOU_SOFT_GATE = 0.10f;
  private static final int MAX_MISSED_FRAMES = 16;
  private static final long LOCKED_GHOST_TTL_MS = 3000L;
  private static final int SUSPECTED_MIN_DWELL_FRAMES = 3;
  private static final int SUSPECTED_REPLACEMENT_STABLE_FRAMES = 2;
  private static final float SWITCH_BELIEF_DELTA = 0.15f;

  private final List<TargetTrack> tracks = new ArrayList<>();
  private int nextTrackId = 1;
  private int lockedTrackId = -1;
  private int suspectedTrackId = -1;
  private int suspectedDwellFrames = 0;
  private float suspectedBelief = 0f;
  private String suspectedUpdateReason = "none";
  private RectF lockedGhostBbox = null;
  private float lockedGhostCenterX = 0f;
  private float lockedGhostCenterY = 0f;
  private float lockedGhostVelocityX = 0f;
  private float lockedGhostVelocityY = 0f;
  private long lockedGhostLastSeenMs = 0L;
  private String lockedGhostLostSide = "unknown";
  private boolean globalAssociationEnabled;
  private boolean lockedAssociationCompeting;
  private float lockedAssociationMargin = Float.POSITIVE_INFINITY;
  private String associationScores = "";
  private final IdentityHashMap<Recognition, Float> candidateAssociationScores =
      new IdentityHashMap<>();

  public float getLockedAssociationMargin() {
    return lockedAssociationMargin;
  }

  public String getAssociationScores() {
    return associationScores;
  }

  public float getAssociationScore(Recognition recognition) {
    Float score = candidateAssociationScores.get(recognition);
    return score == null ? Float.NaN : score;
  }

  private void auditAssociations(List<Recognition> detections, int width, int height, long now) {
    List<Assignment> all = new ArrayList<>();
    candidateAssociationScores.clear();
    StringBuilder log = new StringBuilder();
    for (int i = 0; i < detections.size(); i++) {
      Recognition detection = detections.get(i);
      if (detection == null || detection.getLocation() == null) continue;
      for (TargetTrack track : tracks) {
        if (track.lastBbox == null) continue;
        Match match = score(track, detection, width, height, now);
        Float previous = candidateAssociationScores.get(detection);
        if (previous == null || match.score > previous)
          candidateAssociationScores.put(detection, match.score);
        log.append("T")
            .append(track.trackId)
            .append(":D")
            .append(i)
            .append(":")
            .append(match.score)
            .append(":")
            .append(match.accepted)
            .append(";");
        if (match.accepted) all.add(new Assignment(match, detection));
      }
    }
    associationScores = log.toString();
    all.sort(Comparator.comparingDouble((Assignment item) -> -item.match.score));
    Assignment locked = null;
    for (Assignment item : all)
      if (item.match.track.trackId == lockedTrackId) {
        locked = item;
        break;
      }
    lockedAssociationMargin = Float.POSITIVE_INFINITY;
    if (locked != null)
      for (Assignment item : all) {
        if (item == locked) continue;
        if (item.match.track.trackId == lockedTrackId || item.detection == locked.detection)
          lockedAssociationMargin =
              Math.min(lockedAssociationMargin, locked.match.score - item.match.score);
      }
    lockedAssociationCompeting = lockedAssociationMargin < .15f;
  }

  public boolean isLockedAssociationCompeting() {
    return lockedAssociationCompeting;
  }

  public void setGlobalAssociationEnabled(boolean enabled) {
    globalAssociationEnabled = enabled;
  }

  public void reset() {
    lockedAssociationCompeting = false;
    tracks.clear();
    nextTrackId = 1;
    lockedTrackId = -1;
    suspectedTrackId = -1;
    suspectedDwellFrames = 0;
    suspectedBelief = 0f;
    suspectedUpdateReason = "reset";
    lockedGhostBbox = null;
    lockedGhostCenterX = 0f;
    lockedGhostCenterY = 0f;
    lockedGhostVelocityX = 0f;
    lockedGhostVelocityY = 0f;
    lockedGhostLastSeenMs = 0L;
    lockedGhostLostSide = "unknown";
    associationScores = "";
    candidateAssociationScores.clear();
  }

  public void update(List<Recognition> detections, int frameW, int frameH, long timestampMs) {
    List<Recognition> safeDetections = detections == null ? new ArrayList<>() : detections;
    auditAssociations(safeDetections, frameW, frameH, timestampMs);
    Set<TargetTrack> matchedTracks = new HashSet<>();
    Set<Recognition> matchedDetections = new HashSet<>();

    if (globalAssociationEnabled) {
      List<Assignment> assignments = new ArrayList<>();
      for (Recognition detection : safeDetections) {
        if (detection == null || detection.getLocation() == null) continue;
        for (TargetTrack track : tracks) {
          if (track.lastBbox == null) continue;
          Match match = score(track, detection, frameW, frameH, timestampMs);
          if (match.accepted) assignments.add(new Assignment(match, detection));
        }
      }
      assignments.sort(Comparator.comparingDouble((Assignment item) -> -item.match.score));
      for (Assignment assignment : assignments) {
        if (matchedTracks.contains(assignment.match.track)
            || matchedDetections.contains(assignment.detection)) continue;
        assignment.match.track.update(
            assignment.detection, timestampMs, "global " + assignment.match.reason);
        matchedTracks.add(assignment.match.track);
        matchedDetections.add(assignment.detection);
      }
    } else {
      for (Recognition detection : safeDetections) {
        if (detection == null || detection.getLocation() == null) continue;
        Match best = null;
        for (TargetTrack track : tracks) {
          if (track.lastBbox == null || matchedTracks.contains(track)) continue;
          Match match = score(track, detection, frameW, frameH, timestampMs);
          if (!match.accepted) continue;
          if (best == null || match.score > best.score) best = match;
        }
        if (best != null) {
          best.track.update(detection, timestampMs, best.reason);
          matchedTracks.add(best.track);
          matchedDetections.add(detection);
        }
      }
    }

    for (TargetTrack track : new ArrayList<>(tracks)) {
      if (!matchedTracks.contains(track)) track.markMissed();
      if (track.missedFrames > MAX_MISSED_FRAMES) tracks.remove(track);
    }

    for (Recognition detection : safeDetections) {
      if (detection == null
          || detection.getLocation() == null
          || matchedDetections.contains(detection)) {
        continue;
      }
      tracks.add(new TargetTrack(nextTrackId++, detection, timestampMs));
    }

    TargetTrack lockedTrack = getLockedTrack();
    if (lockedTrack != null && lockedTrack.isVisible()) {
      updateLockedGhost(lockedTrack, frameW, timestampMs);
    }
  }

  public List<TargetTrack> getTracks() {
    return new ArrayList<>(tracks);
  }

  public int getActiveTrackCount() {
    int count = 0;
    for (TargetTrack track : tracks) {
      if (track.isVisible()) count++;
    }
    return count;
  }

  public TargetTrack getTrackForRecognition(Recognition recognition) {
    if (recognition == null) return null;
    for (TargetTrack track : tracks) {
      if (track.recognition == recognition) return track;
    }
    return null;
  }

  public TargetTrack getLockedTrack() {
    return getTrackById(lockedTrackId);
  }

  public TargetTrack getTrackById(int trackId) {
    if (trackId < 0) return null;
    for (TargetTrack track : tracks) {
      if (track.trackId == trackId) return track;
    }
    return null;
  }

  public int getLockedTrackId() {
    return lockedTrackId;
  }

  public int getSuspectedTrackId() {
    return suspectedTrackId;
  }

  public void setSuspectedTrackId(int suspectedTrackId) {
    this.suspectedTrackId = suspectedTrackId;
    this.suspectedDwellFrames = suspectedTrackId < 0 ? 0 : 1;
    this.suspectedBelief = 0f;
    this.suspectedUpdateReason = "forced";
  }

  public int getSuspectedDwellFrames() {
    return suspectedDwellFrames;
  }

  public String getSuspectedUpdateReason() {
    return suspectedUpdateReason;
  }

  public boolean updateSuspectedTrack(int candidateTrackId, float candidateBelief) {
    if (candidateTrackId < 0) {
      suspectedTrackId = -1;
      suspectedDwellFrames = 0;
      suspectedBelief = 0f;
      suspectedUpdateReason = "clear";
      return true;
    }
    if (candidateTrackId == suspectedTrackId) {
      suspectedDwellFrames++;
      suspectedBelief = Math.max(suspectedBelief, candidateBelief);
      suspectedUpdateReason = "suspected_dwell_keep";
      return true;
    }
    TargetTrack candidate = getTrackById(candidateTrackId);
    TargetTrack current = getTrackById(suspectedTrackId);
    if (suspectedTrackId < 0 || current == null) {
      suspectedTrackId = candidateTrackId;
      suspectedDwellFrames = 1;
      suspectedBelief = candidateBelief;
      suspectedUpdateReason = "suspected_init";
      return true;
    }
    boolean candidateStable =
        candidate != null
            && candidate.isVisible()
            && candidate.stableFrames >= SUSPECTED_REPLACEMENT_STABLE_FRAMES;
    boolean clearlyBetter = candidateBelief >= suspectedBelief + SWITCH_BELIEF_DELTA;
    boolean currentWeak = !current.isVisible() || current.missedFrames > 0;
    boolean dwellSatisfied = suspectedDwellFrames >= SUSPECTED_MIN_DWELL_FRAMES;
    if (candidateStable && clearlyBetter && (currentWeak || dwellSatisfied)) {
      suspectedTrackId = candidateTrackId;
      suspectedDwellFrames = 1;
      suspectedBelief = candidateBelief;
      suspectedUpdateReason = "suspected_switch_clear_better";
      return true;
    }
    suspectedDwellFrames++;
    suspectedUpdateReason = "suspected_dwell_hold";
    return false;
  }

  public boolean hasLockedGhost(long nowMs) {
    return lockedGhostBbox != null && nowMs - lockedGhostLastSeenMs <= LOCKED_GHOST_TTL_MS;
  }

  public RectF getLockedGhostBbox(long nowMs) {
    return hasLockedGhost(nowMs) ? new RectF(lockedGhostBbox) : null;
  }

  public String getLockedGhostLostSide(long nowMs) {
    return hasLockedGhost(nowMs) ? lockedGhostLostSide : "unknown";
  }

  public boolean isNearLockedGhost(TargetTrack track, int frameW, int frameH, long nowMs) {
    if (track == null || track.lastBbox == null || !hasLockedGhost(nowMs)) return false;
    RectF ghost = predictedLockedGhost(nowMs);
    float diag = (float) Math.hypot(Math.max(1, frameW), Math.max(1, frameH));
    float centerJump = centerDistance(track.lastBbox, ghost) / Math.max(1f, diag);
    float areaRatio = area(track.lastBbox) / Math.max(1f, area(ghost));
    return centerJump <= BboxContinuityEvidence.LOOSE_CENTER_MAX
        && areaRatio >= BboxContinuityEvidence.LOOSE_AREA_MIN
        && areaRatio <= BboxContinuityEvidence.LOOSE_AREA_MAX;
  }

  public int lockClosest(RectF reference) {
    TargetTrack best = null;
    float bestScore = Float.MAX_VALUE;
    for (TargetTrack track : tracks) {
      if (!track.isVisible()) continue;
      float score = reference == null ? -track.area() : centerDistance(track.lastBbox, reference);
      if (best == null || score < bestScore) {
        best = track;
        bestScore = score;
      }
    }
    if (best == null) {
      clearLockedTrack("lock_missing");
      return lockedTrackId;
    }
    lockTrack(best.trackId, "lock");
    return lockedTrackId;
  }

  public boolean lockTrack(int trackId, String reason) {
    TargetTrack track = getTrackById(trackId);
    if (track == null || !track.isVisible()) return false;
    lockedTrackId = trackId;
    suspectedTrackId = -1;
    suspectedDwellFrames = 0;
    suspectedBelief = 0f;
    suspectedUpdateReason = reason == null ? "lock_track" : reason;
    updateLockedGhost(track, 0, track.lastSeenTimestampMs);
    return true;
  }

  private void clearLockedTrack(String reason) {
    lockedTrackId = -1;
    suspectedTrackId = -1;
    suspectedDwellFrames = 0;
    suspectedBelief = 0f;
    suspectedUpdateReason = reason == null ? "clear_lock" : reason;
  }

  public boolean isLockedTrack(TargetTrack track) {
    return track != null && track.trackId == lockedTrackId;
  }

  private static Match score(
      TargetTrack track, Recognition detection, int frameW, int frameH, long timestampMs) {
    RectF current = detection.getLocation();
    RectF last = track.lastBbox;
    float diag = (float) Math.hypot(Math.max(1, frameW), Math.max(1, frameH));
    float centerJump = centerDistance(current, last) / Math.max(1f, diag);
    float areaRatio = area(current) / Math.max(1f, area(last));
    float iou = iou(current, last);
    RectF predicted = new RectF(last);
    long dt = track.lastSeenTimestampMs - track.previousSeenTimestampMs;
    if (track.previousBbox != null && dt > 0) {
      float horizon = Math.max(0, timestampMs - track.lastSeenTimestampMs) / (float) dt;
      predicted.offset(
          (last.centerX() - track.previousBbox.centerX()) * horizon,
          (last.centerY() - track.previousBbox.centerY()) * horizon);
    }
    float predictionError = centerDistance(current, predicted) / Math.max(1f, diag);
    boolean accepted = centerJump <= CENTER_GATE_RATIO || predictionError <= .18f;
    float sizePenalty = Math.min(1f, Math.abs((float) Math.log(Math.max(.0001f, areaRatio))));
    float score = iou * 2f - centerJump - predictionError - sizePenalty * .25f;
    String reason =
        String.format(
            java.util.Locale.US, "iou=%.2f center=%.3f area=%.2f", iou, centerJump, areaRatio);
    return new Match(track, accepted, score, reason);
  }

  private static float centerDistance(RectF a, RectF b) {
    if (a == null || b == null) return Float.MAX_VALUE;
    return (float) Math.hypot(a.centerX() - b.centerX(), a.centerY() - b.centerY());
  }

  private static float area(RectF b) {
    return b == null ? 0f : Math.max(0f, b.width()) * Math.max(0f, b.height());
  }

  private void updateLockedGhost(TargetTrack track, int frameW, long timestampMs) {
    if (track == null || track.lastBbox == null) return;
    long dt = timestampMs - lockedGhostLastSeenMs;
    if (lockedGhostBbox != null && dt > 0) {
      lockedGhostVelocityX = (track.lastBbox.centerX() - lockedGhostCenterX) / dt;
      lockedGhostVelocityY = (track.lastBbox.centerY() - lockedGhostCenterY) / dt;
    }
    lockedGhostBbox = new RectF(track.lastBbox);
    lockedGhostCenterX = track.lastBbox.centerX();
    lockedGhostCenterY = track.lastBbox.centerY();
    lockedGhostLastSeenMs = timestampMs;
    if (frameW > 0) {
      lockedGhostLostSide = lockedGhostCenterX < frameW / 2f ? "left" : "right";
    }
  }

  private RectF predictedLockedGhost(long nowMs) {
    RectF ghost = new RectF(lockedGhostBbox);
    float steps = Math.min(500f, Math.max(0f, nowMs - lockedGhostLastSeenMs));
    ghost.offset(lockedGhostVelocityX * steps, lockedGhostVelocityY * steps);
    return ghost;
  }

  private static float iou(RectF a, RectF b) {
    if (a == null || b == null) return 0f;
    float left = Math.max(a.left, b.left);
    float top = Math.max(a.top, b.top);
    float right = Math.min(a.right, b.right);
    float bottom = Math.min(a.bottom, b.bottom);
    float inter = Math.max(0f, right - left) * Math.max(0f, bottom - top);
    float union = area(a) + area(b) - inter;
    return union <= 0f ? 0f : inter / union;
  }

  private static class Match {
    final TargetTrack track;
    final boolean accepted;
    final float score;
    final String reason;

    Match(TargetTrack track, boolean accepted, float score, String reason) {
      this.track = track;
      this.accepted = accepted;
      this.score = score;
      this.reason = reason;
    }
  }

  /**
   * Associates high-confidence detections normally, then lets low-confidence detections revive only
   * an existing locked or suspected track. Low-confidence detections never create tracks.
   */
  public TwoStageUpdateResult updateWithLowConfidence(
      List<Recognition> highConfidence,
      List<Recognition> lowConfidence,
      int frameW,
      int frameH,
      long timestampMs) {
    List<Recognition> allDetections =
        new ArrayList<>(
            highConfidence == null ? java.util.Collections.emptyList() : highConfidence);
    if (lowConfidence != null) allDetections.addAll(lowConfidence);
    auditAssociations(allDetections, frameW, frameH, timestampMs);
    boolean competing = lockedAssociationCompeting;
    float margin = lockedAssociationMargin;
    String scores = associationScores;
    IdentityHashMap<Recognition, Float> perCandidateScores =
        new IdentityHashMap<>(candidateAssociationScores);
    update(highConfidence, frameW, frameH, timestampMs);
    lockedAssociationCompeting = competing;
    lockedAssociationMargin = margin;
    associationScores = scores;
    candidateAssociationScores.clear();
    candidateAssociationScores.putAll(perCandidateScores);
    List<Recognition> continued = new ArrayList<>();
    Set<TargetTrack> revivedTracks = new HashSet<>();
    if (lowConfidence != null) {
      for (Recognition detection : lowConfidence) {
        if (detection == null || detection.getLocation() == null) continue;
        Match best = null;
        for (TargetTrack track : tracks) {
          boolean eligible = track.trackId == lockedTrackId || track.trackId == suspectedTrackId;
          if (!eligible
              || track.isVisible()
              || revivedTracks.contains(track)
              || track.lastBbox == null) {
            continue;
          }
          Match candidate = score(track, detection, frameW, frameH, timestampMs);
          if (!candidate.accepted) continue;
          if (best == null || candidate.score > best.score) best = candidate;
        }
        if (best == null) continue;
        best.track.update(detection, timestampMs, "low_confidence " + best.reason);
        revivedTracks.add(best.track);
        continued.add(detection);
      }
    }
    TargetTrack lockedTrack = getLockedTrack();
    if (lockedTrack != null && lockedTrack.isVisible()) {
      updateLockedGhost(lockedTrack, frameW, timestampMs);
    }
    return new TwoStageUpdateResult(continued);
  }

  private static class Assignment {
    final Match match;
    final Recognition detection;

    Assignment(Match match, Recognition detection) {
      this.match = match;
      this.detection = detection;
    }
  }
}
