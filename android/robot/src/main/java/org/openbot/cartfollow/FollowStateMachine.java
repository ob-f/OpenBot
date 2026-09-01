package org.openbot.cartfollow;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.openbot.tflite.Detector.Recognition;
import org.openbot.vehicle.Control;

public class FollowStateMachine {

  public static class FrameResult {
    public final FollowState state;
    public final Control control;
    public final Recognition target;
    public final Recognition candidate;
    public final List<Recognition> persons;
    public final boolean matched;
    public final boolean tooClose;
    public final Bitmap snapshot;
    public final int countdownSec;
    public float matchScore;
    public ImageSetpointDistanceEstimator.DistanceEstimate distanceEstimate;
    public BehaviorDecisionResult behaviorDecision;
    public IdentityEvidence identityEvidence;
    public SteeringEvidence steeringEvidence;

    public FrameResult(
        FollowState state,
        Control control,
        Recognition target,
        Recognition candidate,
        List<Recognition> persons,
        boolean matched,
        boolean tooClose,
        Bitmap snapshot,
        int countdownSec) {
      this.state = state;
      this.control = control;
      this.target = target;
      this.candidate = candidate;
      this.persons = persons;
      this.matched = matched;
      this.tooClose = tooClose;
      this.snapshot = snapshot;
      this.countdownSec = countdownSec;
      this.matchScore = 0f;
    }
  }

  public int CAPTURE_FRAMES = 15;
  public int REACQUIRE_MATCH_N = 8;
  public int FOLLOW_LOST_M = 10;
  public long LOST_TO_SEARCH_MS = 800;
  public long SEARCH_TIMEOUT_MS = 18000;
  public long IDENTITY_UNCERTAIN_TIMEOUT_MS = 18000;
  public long COUNTDOWN_MS = 3000;
  public int CAUTION_STABLE_FRAMES = 3;
  public int UNCERTAIN_FRAMES = 3;
  public int UNCERTAIN_RECOVER_FRAMES = 2;
  public int REACQUIRE_STRICT_FRAMES = 1;
  public int REACQUIRE_DEFAULT_FRAMES = 2;
  public int LOST_RECOVER_FRAMES = 3;

  private final TargetMatcher matcher;
  private final ControlGenerator controlGenerator;
  private final TargetMemory memory = new TargetMemory();

  private FollowState state = FollowState.IDLE;
  private int captureCount = 0;
  private int matchCount = 0;
  private int lostCount = 0;
  private int midDefaultStreak = 0;
  private int strongDefaultStreak = 0;
  private int strongStrictStreak = 0;
  private int unstableStreak = 0;
  private long stateEnterTime = 0L;
  private Bitmap snapshot = null;

  public FollowStateMachine(TargetMatcher matcher, ControlGenerator controlGenerator) {
    this.matcher = matcher;
    this.controlGenerator = controlGenerator;
  }

  public FollowState getState() {
    return state;
  }

  public TargetMemory getMemory() {
    return memory;
  }

  public void startCapture() {
    if (state == FollowState.IDLE || state == FollowState.STOP) {
      memory.clear();
      snapshot = null;
      captureCount = 0;
      resetEvidenceCounters();
      state = FollowState.CAPTURE_TARGET;
    }
  }

  public void confirm() {
    if (state == FollowState.LOCKED_PENDING_CONFIRM) {
      state = FollowState.CONFIRMED_ARMED;
      matchCount = 0;
    }
  }

  public void retake() {
    if (state == FollowState.LOCKED_PENDING_CONFIRM) {
      memory.clear();
      snapshot = null;
      captureCount = 0;
      state = FollowState.CAPTURE_TARGET;
    }
  }

  public void cancel() {
    memory.clear();
    snapshot = null;
    captureCount = 0;
    matchCount = 0;
    lostCount = 0;
    resetEvidenceCounters();
    state = FollowState.IDLE;
  }

  public FrameResult onFrame(
      List<Recognition> persons, Bitmap frame, int frameW, int frameH, int sensorOrientation) {
    return onFrame(persons, frame, frameW, frameH, sensorOrientation, null);
  }

  public FrameResult onFrame(
      List<Recognition> persons,
      Bitmap frame,
      int frameW,
      int frameH,
      int sensorOrientation,
      IdentityEvidence externalIdentity) {
    List<Recognition> safePersons = persons == null ? new ArrayList<>() : persons;
    long now = System.currentTimeMillis();

    switch (state) {
      case IDLE:
        return new FrameResult(
            FollowState.IDLE, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);

      case CAPTURE_TARGET: {
        Recognition cand = selectLargest(safePersons);
        if (cand == null || cand.getLocation() == null) {
          captureCount = 0;
          return new FrameResult(
              FollowState.CAPTURE_TARGET, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
        }
        captureCount++;
        if (captureCount >= CAPTURE_FRAMES) {
          memory.captureFromBitmap(frame, cand.getLocation(), frameW, frameH, sensorOrientation);
          snapshot = cropSnapshot(frame, cand.getLocation());
          captureCount = 0;
          state = FollowState.LOCKED_PENDING_CONFIRM;
          return new FrameResult(
              FollowState.LOCKED_PENDING_CONFIRM, new Control(0f, 0f), null, null, safePersons, false, false, snapshot, -1);
        }
        return new FrameResult(
            FollowState.CAPTURE_TARGET, new Control(0f, 0f), null, cand, safePersons, false, false, null, -1);
      }

      case LOCKED_PENDING_CONFIRM:
        return new FrameResult(
            FollowState.LOCKED_PENDING_CONFIRM, new Control(0f, 0f), null, null, safePersons, false, false, snapshot, -1);

      case CONFIRMED_ARMED:
        if (!safePersons.isEmpty()) {
          state = FollowState.REACQUIRE_TARGET;
          matchCount = 0;
        }
        return new FrameResult(
            state, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);

      case REACQUIRE_TARGET: {
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        updateEvidenceCounters(id, m, safePersons);
        if (reacquireReady(id, m, safePersons)) matchCount++;
        else matchCount = 0;
        Recognition selected = selectedCandidate(id, m);
        if (selected != null
            && (strongStrictStreak >= REACQUIRE_STRICT_FRAMES
                || strongDefaultStreak >= REACQUIRE_DEFAULT_FRAMES
                || matchCount >= REACQUIRE_MATCH_N)) {
          state = FollowState.READY_TO_FOLLOW;
          stateEnterTime = now;
          memory.updateDynamic(selected);
          resetEvidenceCounters();
        }
        FrameResult fr =
            new FrameResult(
                state, new Control(0f, 0f), selected, null, safePersons, id.matched, false, null, -1);
        fillIdentity(fr, id);
        return fr;
      }

      case READY_TO_FOLLOW: {
        int cd = (int) Math.ceil((COUNTDOWN_MS - (now - stateEnterTime)) / 1000.0);
        if (now - stateEnterTime >= COUNTDOWN_MS) {
          state = FollowState.FOLLOW;
          lostCount = 0;
          return new FrameResult(
              FollowState.FOLLOW, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
        }
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        Recognition selected = selectedCandidate(id, m);
        FrameResult fr =
            new FrameResult(
                FollowState.READY_TO_FOLLOW,
                new Control(0f, 0f),
                selected,
                null,
                safePersons,
                id.matched,
                false,
                null,
                Math.max(0, cd));
        fillIdentity(fr, id);
        return fr;
      }

      case FOLLOW: {
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        updateEvidenceCounters(id, m, safePersons);
        Recognition selected = selectedCandidate(id, m);
        if (selected != null && followConfident(id, m, safePersons)) {
          memory.updateDynamic(selected);
          lostCount = 0;
          ControlGenerator.Result res =
              controlGenerator.generateFromTarget(
                  selected, safePersons, frameW, frameH, sensorOrientation, memory);
          FrameResult fr =
              new FrameResult(
                  FollowState.FOLLOW, res.control, selected, null, safePersons, true, res.tooClose, null, -1);
          fillIdentity(fr, id);
          fr.distanceEstimate = res.distanceEstimate;
          return fr;
        }
        if (selected != null && followCaution(id, m, safePersons)) {
          state = FollowState.FOLLOW_CAUTION;
          memory.updateDynamic(selected);
          ControlGenerator.Result res =
              controlGenerator.generateFromTarget(
                  selected, safePersons, frameW, frameH, sensorOrientation, memory);
          FrameResult fr =
              new FrameResult(
                  FollowState.FOLLOW_CAUTION,
                  res.control,
                  selected,
                  null,
                  safePersons,
                  true,
                  res.tooClose,
                  null,
                  -1);
          fillIdentity(fr, id);
          fr.distanceEstimate = res.distanceEstimate;
          return fr;
        }
        lostCount++;
        if (lostCount >= FOLLOW_LOST_M) {
          state = FollowState.IDENTITY_UNCERTAIN;
          stateEnterTime = now;
          resetEvidenceCounters();
        }
        FrameResult fr =
            new FrameResult(
                state, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
        fillIdentity(fr, id);
        return fr;
      }

      case FOLLOW_CAUTION: {
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        updateEvidenceCounters(id, m, safePersons);
        Recognition selected = selectedCandidate(id, m);
        if (selected != null && midDefaultStreak >= CAUTION_STABLE_FRAMES) {
          state = FollowState.FOLLOW;
          memory.updateDynamic(selected);
          ControlGenerator.Result res =
              controlGenerator.generateFromTarget(
                  selected, safePersons, frameW, frameH, sensorOrientation, memory);
          FrameResult fr =
              new FrameResult(
                  FollowState.FOLLOW, res.control, selected, null, safePersons, true, res.tooClose, null, -1);
          fillIdentity(fr, id);
          fr.distanceEstimate = res.distanceEstimate;
          return fr;
        }
        if (selected != null && followCaution(id, m, safePersons)) {
          memory.updateDynamic(selected);
          ControlGenerator.Result res =
              controlGenerator.generateFromTarget(
                  selected, safePersons, frameW, frameH, sensorOrientation, memory);
          FrameResult fr =
              new FrameResult(
                  FollowState.FOLLOW_CAUTION,
                  res.control,
                  selected,
                  null,
                  safePersons,
                  true,
                  res.tooClose,
                  null,
                  -1);
          fillIdentity(fr, id);
          fr.distanceEstimate = res.distanceEstimate;
          return fr;
        }
        unstableStreak++;
        if (unstableStreak >= UNCERTAIN_FRAMES) {
          state = FollowState.IDENTITY_UNCERTAIN;
          stateEnterTime = now;
          resetEvidenceCounters();
        }
        FrameResult fr =
            new FrameResult(
                state, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
        fillIdentity(fr, id);
        return fr;
      }

      case IDENTITY_UNCERTAIN: {
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        updateEvidenceCounters(id, m, safePersons);
        Recognition selected = selectedCandidate(id, m);
        if (selected != null
            && (identityAdmissionReady(id)
                || strongStrictStreak >= UNCERTAIN_RECOVER_FRAMES
                || midDefaultStreak >= UNCERTAIN_RECOVER_FRAMES)) {
          state = FollowState.REACQUIRE_TARGET;
          matchCount = 0;
          stateEnterTime = now;
        } else if (now - stateEnterTime >= IDENTITY_UNCERTAIN_TIMEOUT_MS) {
          state = FollowState.STOP;
        }
        FrameResult fr =
            new FrameResult(
                state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
        fillIdentity(fr, id);
        return fr;
      }

      case LOST: {
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        updateEvidenceCounters(id, m, safePersons);
        Recognition selected = selectedCandidate(id, m);
        if (selected != null && lostRecoverReady(id, m, safePersons)) {
          state = FollowState.REACQUIRE_TARGET;
          matchCount = 0;
          stateEnterTime = now;
        }
        if (now - stateEnterTime >= LOST_TO_SEARCH_MS) {
          state = FollowState.SEARCH;
          stateEnterTime = now;
        }
        FrameResult fr =
            new FrameResult(
                state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
        fillIdentity(fr, id);
        return fr;
      }

      case SEARCH: {
        TargetMatcher.MatchResult m = matcher.match(safePersons, frame, memory, frameW, frameH);
        IdentityEvidence id = identityFrom(m, externalIdentity);
        updateEvidenceCounters(id, m, safePersons);
        Recognition selected = selectedCandidate(id, m);
        if (selected != null && lostRecoverReady(id, m, safePersons)) {
          state = FollowState.REACQUIRE_TARGET;
          matchCount = 0;
          stateEnterTime = now;
        }
        if (now - stateEnterTime >= SEARCH_TIMEOUT_MS) {
          state = FollowState.STOP;
        }
        FrameResult fr =
            new FrameResult(
                state, new Control(0f, 0f), selected, null, safePersons, false, false, null, -1);
        fillIdentity(fr, id);
        return fr;
      }

      case STOP:
      default:
        return new FrameResult(
            FollowState.STOP, new Control(0f, 0f), null, null, safePersons, false, false, null, -1);
    }
  }

  private void fillIdentity(FrameResult fr, IdentityEvidence id) {
    if (fr == null || id == null) return;
    fr.identityEvidence = id;
    fr.matchScore = id.confidence;
  }

  private IdentityEvidence identityFrom(TargetMatcher.MatchResult m, IdentityEvidence external) {
    if (external != null) return external;
    return new IdentityEvidence(
        m.matched ? m.score : 0f,
        m.score,
        m.matched,
        m.matched ? "legacy_matched" : "legacy_not_matched",
        ReIDMatchResult.unavailable("reid_not_connected", 0),
        BboxContinuityEvidence.from(
            m.best == null ? null : m.best.getLocation(),
            memory.getLastBbox(),
            memory.getPreviousBbox(),
            1,
            1),
        0,
        0,
        m.best);
  }

  private Recognition selectedCandidate(IdentityEvidence id, TargetMatcher.MatchResult m) {
    if (id != null && id.bestCandidate != null) return id.bestCandidate;
    return m == null ? null : m.best;
  }

  private boolean followConfident(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return id.beliefConfirmed()
          && id.beliefStableFrames >= 2
          && motionGateOk(id);
    }
    if (id != null && id.reidAvailable()) {
      return id.weakOk() && id.bboxDefaultOk();
    }
    return m != null && m.matched && (persons == null || persons.size() <= 1 || id.bboxDefaultOk());
  }

  private boolean followCaution(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return id.beliefCaution() && motionGateOk(id);
    }
    if (id != null && id.reidAvailable()) {
      return id.bboxDefaultOk() && (id.weakOk() || id.midOk());
    }
    return m != null && m.matched && id != null && id.bboxDefaultOk() && persons != null && persons.size() <= 1;
  }

  private boolean reacquireReady(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return id.beliefConfirmed()
          && id.beliefStableFrames >= 3
          && motionGateOk(id);
    }
    if (id != null && id.reidAvailable()) {
      return (id.strongOk() && id.bboxDefaultOk()) || (id.midOk() && id.bboxStrictOk());
    }
    return m != null && m.matched && id != null && id.bboxStrictOk() && persons != null && persons.size() <= 1;
  }

  private boolean lostRecoverReady(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      return identityAdmissionReady(id);
    }
    if (id != null && id.reidAvailable()) {
      return strongStrictStreak >= LOST_RECOVER_FRAMES
          || strongDefaultStreak >= LOST_RECOVER_FRAMES
          || midDefaultStreak >= LOST_RECOVER_FRAMES;
    }
    return m != null
        && m.matched
        && id != null
        && id.bboxStrictOk()
        && persons != null
        && persons.size() <= 1
        && strongStrictStreak >= LOST_RECOVER_FRAMES;
  }

  private void updateEvidenceCounters(
      IdentityEvidence id, TargetMatcher.MatchResult m, List<Recognition> persons) {
    if (id != null && id.hasBelief()) {
      boolean safeBbox = motionGateOk(id);
      boolean midDefault = id.targetBelief >= IdentityBelief.BELIEF_CAUTION && safeBbox;
      boolean strongDefault = id.targetBelief >= IdentityBelief.BELIEF_CONFIRM && safeBbox;
      boolean strongStrict =
          id.targetBelief >= IdentityBelief.BELIEF_CONFIRM
              && (id.bboxStrictOk() || id.predictionOk() || id.trackId == id.lockedTrackId);
      midDefaultStreak = midDefault ? midDefaultStreak + 1 : 0;
      strongDefaultStreak = strongDefault ? strongDefaultStreak + 1 : 0;
      strongStrictStreak = strongStrict ? strongStrictStreak + 1 : 0;
      if (midDefault || strongDefault || strongStrict) unstableStreak = 0;
      return;
    }
    boolean reidAvailable = id != null && id.reidAvailable();
    boolean midDefault;
    boolean strongDefault;
    boolean strongStrict;
    if (reidAvailable) {
      midDefault = id.midOk() && id.bboxDefaultOk();
      strongDefault = id.strongOk() && id.bboxDefaultOk();
      strongStrict = id.strongOk() && id.bboxStrictOk();
    } else {
      boolean legacySafe =
          m != null && m.matched && id != null && persons != null && persons.size() <= 1;
      midDefault = legacySafe && id.bboxDefaultOk();
      strongDefault = legacySafe && id.bboxDefaultOk();
      strongStrict = legacySafe && id.bboxStrictOk();
    }
    midDefaultStreak = midDefault ? midDefaultStreak + 1 : 0;
    strongDefaultStreak = strongDefault ? strongDefaultStreak + 1 : 0;
    strongStrictStreak = strongStrict ? strongStrictStreak + 1 : 0;
    if (midDefault || strongDefault || strongStrict) unstableStreak = 0;
  }

  private boolean identityAdmissionReady(IdentityEvidence id) {
    if (id == null || !id.hasBelief()) return false;
    boolean admission =
        id.looseAdmissionOk() || id.bboxDefaultOk() || id.predictionOk() || id.trackId == id.lockedTrackId;
    return id.beliefCaution() && id.beliefStableFrames >= UNCERTAIN_RECOVER_FRAMES && admission;
  }

  private boolean motionGateOk(IdentityEvidence id) {
    if (id == null) return false;
    return id.bboxDefaultOk() || id.predictionOk() || id.trackId == id.lockedTrackId;
  }

  private void resetEvidenceCounters() {
    midDefaultStreak = 0;
    strongDefaultStreak = 0;
    strongStrictStreak = 0;
    unstableStreak = 0;
  }

  private static Recognition selectLargest(List<Recognition> persons) {
    Recognition target = null;
    float maxArea = -1f;
    for (Recognition r : persons) {
      if (r == null || r.getLocation() == null) continue;
      RectF loc = r.getLocation();
      float area = loc.width() * loc.height();
      if (area > maxArea) {
        maxArea = area;
        target = r;
      }
    }
    return target;
  }

  private static Bitmap cropSnapshot(Bitmap frame, RectF bbox) {
    if (frame == null || bbox == null) return null;
    int fw = frame.getWidth();
    int fh = frame.getHeight();
    int l = clamp((int) bbox.left, 0, fw - 1);
    int t = clamp((int) bbox.top, 0, fh - 1);
    int r = clamp((int) bbox.right, 1, fw);
    int b = clamp((int) bbox.bottom, 1, fh);
    int w = r - l;
    int h = b - t;
    if (w <= 0 || h <= 0) return null;
    try {
      return Bitmap.createBitmap(frame, l, t, w, h);
    } catch (Exception e) {
      return null;
    }
  }

  private static int clamp(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }
}
