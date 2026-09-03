package org.openbot.cartfollow;

import org.openbot.tflite.Detector.Recognition;

public class IdentityEvidence {
  public final float score;
  public final float confidence;
  public final boolean matched;
  public final String reason;
  public final ReIDMatchResult reidMatch;
  public final BboxContinuityEvidence bboxContinuity;
  public final int stableMatchCount;
  public final int candidateSwitchCount;
  public final Recognition bestCandidate;
  public final int trackId;
  public final int lockedTrackId;
  public final int suspectedTrackId;
  public final int activeTrackCount;
  public final int trackAge;
  public final int missedFrames;
  public final float targetBelief;
  public final float reidContribution;
  public final float bboxContribution;
  public final float predictionContribution;
  public final float switchPenalty;
  public final int beliefStableFrames;
  public final int beliefUncertainFrames;
  public final String beliefReason;

  public IdentityEvidence(float score, float confidence, boolean matched, String reason) {
    this(score, confidence, matched, reason, null, null, 0, 0, null);
  }

  public IdentityEvidence(
      float score,
      float confidence,
      boolean matched,
      String reason,
      ReIDMatchResult reidMatch,
      BboxContinuityEvidence bboxContinuity,
      int stableMatchCount,
      int candidateSwitchCount,
      Recognition bestCandidate) {
    this(
        score,
        confidence,
        matched,
        reason,
        reidMatch,
        bboxContinuity,
        stableMatchCount,
        candidateSwitchCount,
        bestCandidate,
        -1,
        -1,
        -1,
        0,
        0,
        0,
        0f,
        0f,
        0f,
        0f,
        0f,
        0,
        0,
        null);
  }

  public IdentityEvidence(
      float score,
      float confidence,
      boolean matched,
      String reason,
      ReIDMatchResult reidMatch,
      BboxContinuityEvidence bboxContinuity,
      int stableMatchCount,
      int candidateSwitchCount,
      Recognition bestCandidate,
      int trackId,
      int lockedTrackId,
      int suspectedTrackId,
      int activeTrackCount,
      int trackAge,
      int missedFrames,
      float targetBelief,
      float reidContribution,
      float bboxContribution,
      float predictionContribution,
      float switchPenalty,
      int beliefStableFrames,
      int beliefUncertainFrames,
      String beliefReason) {
    this.score = score;
    this.confidence = confidence;
    this.matched = matched;
    this.reason = reason;
    this.reidMatch = reidMatch;
    this.bboxContinuity = bboxContinuity;
    this.stableMatchCount = stableMatchCount;
    this.candidateSwitchCount = candidateSwitchCount;
    this.bestCandidate = bestCandidate;
    this.trackId = trackId;
    this.lockedTrackId = lockedTrackId;
    this.suspectedTrackId = suspectedTrackId;
    this.activeTrackCount = activeTrackCount;
    this.trackAge = trackAge;
    this.missedFrames = missedFrames;
    this.targetBelief = targetBelief;
    this.reidContribution = reidContribution;
    this.bboxContribution = bboxContribution;
    this.predictionContribution = predictionContribution;
    this.switchPenalty = switchPenalty;
    this.beliefStableFrames = beliefStableFrames;
    this.beliefUncertainFrames = beliefUncertainFrames;
    this.beliefReason = beliefReason;
  }

  public boolean hasBelief() {
    return trackId >= 0 || activeTrackCount > 0 || lockedTrackId >= 0;
  }

  /**
   * Simulator authorization observes the scored detection, never an unscored belief-selected box.
   */
  public IdentityEvidence forSimulatorCandidate(
      TargetTrack track,
      int lockedId,
      ReIDMatchResult scored,
      BboxContinuityEvidence bbox,
      float belief) {
    boolean valid = track != null && scored != null && scored.isBoundToTrack(track.trackId);
    return new IdentityEvidence(
        valid ? scored.bestScore : 0f,
        valid ? scored.bestScore : 0f,
        valid && scored.weakOk,
        reason,
        scored,
        bbox,
        stableMatchCount,
        candidateSwitchCount,
        track == null ? null : track.recognition,
        track == null ? -1 : track.trackId,
        lockedId,
        suspectedTrackId,
        activeTrackCount,
        track == null ? 0 : track.ageFrames,
        track == null ? 0 : track.missedFrames,
        belief,
        reidContribution,
        bboxContribution,
        predictionContribution,
        switchPenalty,
        beliefStableFrames,
        beliefUncertainFrames,
        beliefReason);
  }

  public boolean beliefConfirmed() {
    return hasBelief() && targetBelief >= IdentityBelief.BELIEF_CONFIRM;
  }

  public boolean beliefCaution() {
    return hasBelief() && targetBelief >= IdentityBelief.BELIEF_CAUTION;
  }

  public boolean reidAvailable() {
    return reidMatch != null && reidMatch.reidAvailable;
  }

  public boolean weakOk() {
    return reidAvailable() && reidMatch.weakOk;
  }

  public boolean midOk() {
    return reidAvailable() && reidMatch.midOk;
  }

  public boolean strongOk() {
    return reidAvailable() && reidMatch.strongOk;
  }

  public boolean bboxDefaultOk() {
    return bboxContinuity != null && bboxContinuity.bboxDefaultOk;
  }

  public boolean bboxStrictOk() {
    return bboxContinuity != null && bboxContinuity.bboxStrictOk;
  }

  public boolean looseAdmissionOk() {
    return bboxContinuity != null && bboxContinuity.looseAdmissionOk;
  }

  public boolean predictionOk() {
    return bboxContinuity != null && bboxContinuity.predictionOk;
  }

  public IdentityEvidence withoutMotionCandidate(String suppressedReason) {
    return new IdentityEvidence(
        0f,
        confidence,
        false,
        suppressedReason,
        reidMatch,
        bboxContinuity,
        stableMatchCount,
        candidateSwitchCount,
        null,
        trackId,
        lockedTrackId,
        suspectedTrackId,
        activeTrackCount,
        trackAge,
        missedFrames,
        targetBelief,
        reidContribution,
        bboxContribution,
        predictionContribution,
        switchPenalty,
        beliefStableFrames,
        beliefUncertainFrames,
        beliefReason);
  }
}
