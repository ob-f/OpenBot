package org.openbot.cartfollow;

import java.util.HashSet;
import java.util.Set;

/** Session-local authorization, separate from accumulated tracking belief and hardware policy. */
public final class SimulatorIdentityGuard {
  public enum State {
    VERIFIED,
    TRACK_STABLE,
    APPEARANCE_TRANSITION,
    TRACK_MAINTAINED,
    CONTINUITY_HOLD,
    ADAPTING,
    AUTO_VERIFY
  }

  public enum RecoveryType {
    NONE,
    LOCAL,
    GLOBAL
  }

  public static final class Decision {
    public final boolean authorized;
    public final boolean needsConfirmation;
    public final int trackId;
    public final int freshMatches;
    public final String reason;
    public final State state;
    public final boolean retainTarget;
    public final boolean motionAllowed;
    public final boolean samplingAllowed;
    public final long holdRemainingMs;
    public final String continuityReason;
    public final long holdDeadlineMs;
    public final RecoveryType recoveryType;
    public final int requiredFreshMatches;
    public final long recoverySpanMs;
    public final long requiredSpanMs;
    public final float recoveryProgress;
    public final long identityEvidenceTimeMs;
    public final long identityObservationId;
    public TrackingDecision tracking;

    public boolean isMaintained() {
      return state == State.TRACK_MAINTAINED;
    }

    /** A locally continuous original target. This is deliberately distinct from strong ReID. */
    public boolean isContinuous() {
      return state == State.TRACK_STABLE
          || state == State.APPEARANCE_TRANSITION
          || state == State.TRACK_MAINTAINED
          || state == State.CONTINUITY_HOLD;
    }

    public boolean isAppearanceTransition() {
      return state == State.APPEARANCE_TRANSITION || state == State.TRACK_MAINTAINED;
    }

    public boolean allowsForward(long now) {
      if (tracking != null) return tracking.allowsMotion(now, 500L);
      return (authorized && state == State.VERIFIED)
          || (isContinuous()
              && motionAllowed
              && identityEvidenceTimeMs >= 0
              && now >= identityEvidenceTimeMs
              && now - identityEvidenceTimeMs <= 500L);
    }

    public Decision withoutContinuityMotion() {
      if (state != State.CONTINUITY_HOLD) return this;
      return new Decision(
          authorized,
          needsConfirmation,
          trackId,
          freshMatches,
          reason,
          state,
          retainTarget,
          false,
          samplingAllowed,
          holdRemainingMs,
          continuityReason,
          holdDeadlineMs,
          recoveryType,
          recoverySpanMs);
    }

    Decision(
        boolean authorized, boolean needsConfirmation, int trackId, int matches, String reason) {
      this(
          authorized,
          needsConfirmation,
          trackId,
          matches,
          reason,
          authorized ? State.VERIFIED : needsConfirmation ? State.AUTO_VERIFY : State.ADAPTING,
          authorized,
          authorized,
          authorized,
          0L,
          "not_evaluated",
          0L,
          RecoveryType.NONE,
          0L);
    }

    private Decision(
        boolean authorized,
        boolean needsConfirmation,
        int trackId,
        int matches,
        String reason,
        State state,
        boolean retain,
        boolean motion,
        boolean sampling,
        long remaining,
        String continuityReason,
        long deadline,
        RecoveryType recoveryType,
        long recoverySpanMs) {
      this(
          authorized,
          needsConfirmation,
          trackId,
          matches,
          reason,
          state,
          retain,
          motion,
          sampling,
          remaining,
          continuityReason,
          deadline,
          recoveryType,
          recoverySpanMs,
          -1L,
          -1L);
    }

    private Decision(
        boolean authorized,
        boolean needsConfirmation,
        int trackId,
        int matches,
        String reason,
        State state,
        boolean retain,
        boolean motion,
        boolean sampling,
        long remaining,
        String continuityReason,
        long deadline,
        RecoveryType recoveryType,
        long recoverySpanMs,
        long evidenceTime,
        long observationId) {
      identityEvidenceTimeMs = evidenceTime;
      identityObservationId = observationId;
      this.authorized = authorized;
      this.needsConfirmation = needsConfirmation;
      this.trackId = trackId;
      this.freshMatches = matches;
      this.reason = reason;
      this.state = state;
      this.retainTarget = retain;
      this.motionAllowed = motion;
      this.samplingAllowed = sampling;
      this.holdRemainingMs = remaining;
      this.continuityReason = continuityReason;
      this.holdDeadlineMs = deadline;
      this.recoveryType = recoveryType;
      this.requiredFreshMatches =
          recoveryType == RecoveryType.NONE ? 0 : recoveryType == RecoveryType.GLOBAL ? 5 : 3;
      this.recoverySpanMs = recoverySpanMs;
      this.requiredSpanMs = recoveryType == RecoveryType.GLOBAL ? 1200L : 0L;
      this.recoveryProgress =
          recoveryType == RecoveryType.NONE
              ? (authorized ? 1f : 0f)
              : Math.min(
                  Math.min(1f, (float) matches / requiredFreshMatches),
                  requiredSpanMs == 0L ? 1f : (float) recoverySpanMs / requiredSpanMs);
    }
  }

  private final Set<Integer> distractors = new HashSet<>();
  private long generation = -1L;
  private int verifiedTrack = -1;
  private int candidate = -1;
  private int matches;
  private long lastObservation = -1L;
  private long lastFreshAt = -1L;
  private long lastTrustedAt = -1L;
  private long lastFrame = -1L;
  private RecoveryType recoveryType = RecoveryType.NONE;
  private long firstFreshAt = -1L;
  private int continuityTrack = -1;
  private long weakSince = -1;
  private long lastObservedAt = -1L;
  private long recoveryDeadline = -1L;
  private int recoveryObservations;
  private boolean restrictedRecovery;
  private long maintainedAt = -1L;
  private long maintainedObservation = -1L;
  private int maintainedMatches;
  private boolean maintaining;
  private long lastIdentityObservation = -1L;
  private int appearanceSupportMisses;
  private long multiSince = -1, multiCheckAt = -1, lastMultiObservation = -1;
  private int alternateTrack = -1, alternateChecks;
  private boolean multiConflict;

  /**
   * A completed check needs fresh scores for every visible candidate from the same source frame.
   */
  public synchronized void inspectCandidates(
      java.util.List<ReIDMatchResult> scores,
      int candidateCount,
      int lockedId,
      long frame,
      long now) {
    if (candidateCount <= 1) {
      multiSince = multiCheckAt = -1;
      alternateChecks = 0;
      return;
    }
    if (multiSince < 0) multiSince = now;
    if (candidateCount > 5 || (multiCheckAt >= 0 && now - multiCheckAt < 500)) return;
    ReIDMatchResult original = null, alternate = null;
    int fresh = 0;
    long newest = -1;
    java.util.Set<Integer> seen = new java.util.HashSet<>();
    for (ReIDMatchResult score : scores) {
      if (score == null
          || !score.fresh
          || score.frameSequence != frame
          || score.observationTimeMs != now
          || !Float.isFinite(score.bestScore)
          || score.observationId <= lastMultiObservation
          || !seen.add(score.candidateTrackId)) continue;
      fresh++;
      newest = Math.max(newest, score.observationId);
      if (score.candidateTrackId == lockedId) original = score;
      else if (alternate == null || score.bestScore > alternate.bestScore) alternate = score;
    }
    if (fresh < candidateCount || original == null) return;
    multiCheckAt = now;
    lastMultiObservation = newest;
    if (alternate != null
        && alternate.bestScore >= .85f
        && alternate.bestScore - original.bestScore >= .10f) {
      alternateChecks = alternateTrack == alternate.candidateTrackId ? alternateChecks + 1 : 1;
      alternateTrack = alternate.candidateTrackId;
      if (alternateChecks >= 2) multiConflict = true;
    } else {
      alternateTrack = -1;
      alternateChecks = 0;
    }
  }

  public synchronized void inspectCandidates(
      java.util.List<ReIDMatchResult> scores,
      IdentityCandidateSet candidates,
      int lockedId,
      long frame,
      long now) {
    inspectCandidates(scores, candidates == null ? 0 : candidates.size(), lockedId, frame, now);
  }

  public synchronized void reset() {
    generation = -1L;
    continuityTrack = -1;
    weakSince = -1;
    distractors.clear();
    clearVerification();
    lastFrame = lastObservation = -1L;
    lastTrustedAt = -1L;
    lastObservedAt = -1L;
    recoveryDeadline = -1L;
    recoveryObservations = 0;
    restrictedRecovery = false;
    lastIdentityObservation = -1L;
    appearanceSupportMisses = 0;
    multiSince = multiCheckAt = lastMultiObservation = -1;
    alternateTrack = -1;
    alternateChecks = 0;
    multiConflict = false;
    resetMaintainedEvidence();
  }

  public synchronized void begin(long generation) {
    reset();
    this.generation = generation;
  }

  public synchronized void rememberDistractor(int trackId) {
    if (trackId >= 0) distractors.add(trackId);
  }

  public synchronized boolean prefersContinuity(int trackId, long now) {
    return continuityTrack == trackId
        && lastObservedAt >= 0
        && now >= lastObservedAt
        && now - lastObservedAt <= 500;
  }

  public synchronized boolean isDistractor(int trackId) {
    return distractors.contains(trackId);
  }

  public synchronized Decision update(
      long session,
      long frame,
      long receivedAt,
      long now,
      int trackId,
      int lockedId,
      boolean highConfidence,
      boolean local,
      ReIDMatchResult reid) {
    return update(
        session, frame, receivedAt, now, trackId, lockedId, highConfidence, local, reid, false);
  }

  public synchronized Decision update(
      long session,
      long frame,
      long receivedAt,
      long now,
      int trackId,
      int lockedId,
      boolean highConfidence,
      boolean local,
      ReIDMatchResult reid,
      boolean multipleCandidates) {
    return update(
        session,
        frame,
        receivedAt,
        now,
        trackId,
        lockedId,
        highConfidence,
        local,
        reid,
        multipleCandidates,
        null,
        false);
  }

  public synchronized Decision update(
      long session,
      long frame,
      long receivedAt,
      long now,
      int trackId,
      int lockedId,
      boolean highConfidence,
      boolean local,
      ReIDMatchResult reid,
      boolean multipleCandidates,
      SimulatorContinuityTracker.Evidence continuity,
      boolean following) {
    return update(
        session,
        frame,
        receivedAt,
        now,
        trackId,
        lockedId,
        highConfidence,
        local,
        reid,
        multipleCandidates ? 2 : 1,
        false,
        false,
        null,
        continuity,
        following);
  }

  /**
   * candidateCount is the actual current person count, independent of associationAmbiguous.
   * targetLost reports the caller's loss state; globalReid must contain independently bound global
   * scores (not local pose/quarantine support). A null global result never falls back to localReid.
   * following means the session has entered FOLLOW, not necessarily that it is currently in FOLLOW.
   */
  public synchronized Decision update(
      long session,
      long frame,
      long receivedAt,
      long now,
      int trackId,
      int lockedId,
      boolean highConfidence,
      boolean local,
      ReIDMatchResult localReid,
      IdentityCandidateSet candidates,
      boolean associationAmbiguous,
      boolean targetLost,
      ReIDMatchResult globalReid,
      SimulatorContinuityTracker.Evidence continuity,
      boolean following) {
    return update(
        session,
        frame,
        receivedAt,
        now,
        trackId,
        lockedId,
        highConfidence,
        local,
        localReid,
        candidates == null ? 0 : candidates.size(),
        associationAmbiguous,
        targetLost,
        globalReid,
        continuity,
        following);
  }

  public synchronized Decision update(
      long session,
      long frame,
      long receivedAt,
      long now,
      int trackId,
      int lockedId,
      boolean highConfidence,
      boolean local,
      ReIDMatchResult localReid,
      int candidateCount,
      boolean associationAmbiguous,
      boolean targetLost,
      ReIDMatchResult globalReid,
      SimulatorContinuityTracker.Evidence continuity,
      boolean following) {
    if (session != generation || frame <= lastFrame) {
      return new Decision(false, false, trackId, 0, "obsolete_identity_frame");
    }
    lastFrame = frame;
    if (frame <= 0L || receivedAt < 0L || receivedAt > now || now - receivedAt > 500L)
      return reject(trackId, false, "frame_stale");
    boolean ambiguous = associationAmbiguous;
    boolean hardContinuityBreak =
        continuity != null
            && ("bbox_jump".equals(continuity.reason)
                || "association_competing".equals(continuity.reason));
    if (distractors.contains(trackId)) return reject(trackId, true, "known_distractor");
    if (lockedId < 0) return reject(trackId, true, "target_lock_missing");
    if (candidateCount > 1 && multiSince < 0) multiSince = receivedAt;
    if (candidateCount <= 1) {
      multiSince = multiCheckAt = -1;
      alternateChecks = 0;
    }
    boolean multiExpired =
        candidateCount > 1 && receivedAt - Math.max(multiSince, multiCheckAt) > 1000;
    if (candidateCount > 5
        || (multiConflict && continuityTrack >= 0)
        || multiExpired
        || ambiguous) {
      if (trackId != lockedId || !local || targetLost) recoveryType = RecoveryType.GLOBAL;
      return reject(
          trackId,
          false,
          candidateCount > 5
              ? "candidate_budget_exceeded"
              : multiConflict
                  ? "identity_conflict"
                  : multiExpired
                      ? "multi_check_timeout"
                      : ambiguous
                          ? (recoveryType == RecoveryType.GLOBAL
                              ? "global_association_ambiguous"
                              : "association_competing")
                          : "bbox_jump");
    }
    // Missing observations never permit motion; keep a bounded original-target recovery context.
    boolean current =
        trackId >= 0
            && candidateCount > 0
            && !targetLost
            && continuity != null
            && continuity.currentBox != null;
    boolean originalContext =
        following
            && continuityTrack == lockedId
            && lastObservedAt >= 0
            && receivedAt - lastObservedAt <= FollowTuning.RECOVERY_CONTEXT_MS;
    if (originalContext && !hardContinuityBreak && recoveryDeadline < 0L
        && (!current || receivedAt - lastObservedAt > 500L)) {
      recoveryDeadline = lastObservedAt + FollowTuning.RECOVERY_CONTEXT_MS;
      recoveryObservations = 0;
    }
    if (recoveryDeadline >= 0L) {
      if (receivedAt > recoveryDeadline || candidateCount > 1 || hardContinuityBreak
          || current && (trackId != lockedId || !local || !highConfidence
              || continuity.observedGeometry == null
              || !continuity.observedGeometry.bboxDefaultOk))
      {
        recoveryDeadline = -1L;
        recoveryObservations = 0;
        restrictedRecovery = false;
        originalContext = false;
        local = false;
        recoveryType = RecoveryType.GLOBAL;
      } else if (!current) {
        recoveryObservations = 0;
        return new Decision(false, false, trackId, 0, "current_target_missing");
      }
    }
    if (originalContext && current && trackId == lockedId && !hardContinuityBreak) {
      boolean recovering = recoveryDeadline >= 0L;
      if (recovering && receivedAt > lastObservedAt) recoveryObservations++;
      lastObservedAt = receivedAt;
      if (localReid != null
          && localReid.fresh
          && localReid.isBoundToTrack(trackId)
          && localReid.frameSequence == frame
          && localReid.observationTimeMs == receivedAt
          && localReid.observationId > lastIdentityObservation
          && localReid.bestScore >= .85f
          && localReid.margin >= .08f
          && localReid.anchorScore >= .70f) {
        lastTrustedAt = localReid.observationTimeMs;
        lastIdentityObservation = localReid.observationId;
        if (!recovering) restrictedRecovery = false;
      }
      boolean stable = continuity.reliable && (!recovering || recoveryObservations >= 3);
      if (recovering) restrictedRecovery = true;
      if (recovering && stable) recoveryDeadline = -1L;
      String reason =
          restrictedRecovery
              ? (stable ? "short_recovery_follow" : "short_recovery_verifying")
              : !stable
              ? "tracking_stabilizing"
              : !highConfidence
                  ? "low_detection_continuity"
                  : candidateCount > 1 ? "multi_person_check" : "continuous_tracking";
      TrackingDecision tracking =
          new TrackingDecision(
              session,
              frame,
              receivedAt,
              trackId,
              continuity.currentBox,
              highConfidence,
              true,
              continuity.stableFrames,
              stable,
              stable && highConfidence && candidateCount == 1 && !restrictedRecovery,
              reason);
      Decision result =
          new Decision(
              false,
              false,
              trackId,
              matches,
              reason,
              State.TRACK_STABLE,
              true,
              stable,
              tracking.learningAllowed,
              0,
              reason,
              0,
              RecoveryType.LOCAL,
              0,
              lastTrustedAt,
              lastIdentityObservation);
      result.tracking = tracking;
      return result;
    }
    if (originalContext && !current && !hardContinuityBreak) {
      Decision result = new Decision(false, false, trackId, 0, "current_target_missing");
      return result;
    }
    if (!highConfidence || trackId < 0 || candidateCount < 1)
      return reject(trackId, false, "current_target_missing");
    if (hardContinuityBreak
        || targetLost
        || (following && continuityTrack >= 0 && !originalContext)) local = false;
    continuityTrack = -1;
    boolean continuousContext = false;
    RecoveryType requestedType =
        (!local && !continuousContext)
                || trackId != lockedId
                || (lastObservedAt >= 0L && receivedAt - lastObservedAt > 3000L)
            ? RecoveryType.GLOBAL
            : RecoveryType.LOCAL;
    if (trackId == lockedId && !targetLost) lastObservedAt = receivedAt;
    // A candidate becoming spatially local cannot shorten an in-progress global verification.
    if (candidate == trackId && recoveryType == RecoveryType.GLOBAL && verifiedTrack != trackId)
      requestedType = RecoveryType.GLOBAL;
    if (lastFreshAt >= 0 && now - lastFreshAt > 500L) clearVerification();
    if (candidate != trackId
        || (recoveryType != requestedType && verifiedTrack != trackId)
        || (recoveryType != requestedType && requestedType == RecoveryType.GLOBAL)) {
      clearVerification();
      candidate = trackId;
    }
    recoveryType = requestedType;
    boolean global = recoveryType == RecoveryType.GLOBAL;
    if (global && (ambiguous || candidateCount > 1))
      return reject(
          trackId,
          false,
          candidateCount > 1 ? "global_multiple_candidates" : "global_association_ambiguous");
    if (associationAmbiguous) return reject(trackId, false, "local_association_ambiguous");
    ReIDMatchResult reid = global ? globalReid : localReid;
    if (reid == null
        || !reid.isBoundToTrack(trackId)
        || !Float.isFinite(reid.bestScore)
        || !Float.isFinite(reid.margin)
        || !Float.isFinite(reid.anchorScore)
        || reid.observationTimeMs < 0L
        || reid.observationTimeMs > receivedAt
        || now - reid.observationTimeMs > 500L) {
      return reject(trackId, false, "identity_evidence_insufficient");
    }
    boolean continuous = continuousContext && !global;
    boolean strong =
        reid.bestScore >= .85f
            && reid.margin >= .08f
            && (global
                || reid.anchorScore
                    >= (trackId == lockedId && reid.localPoseSupport ? .60f : .70f));
    boolean fresh =
        reid.fresh
            && reid.frameSequence == frame
            && reid.observationTimeMs == receivedAt
            && reid.observationId > lastIdentityObservation
            && (maintainedAt < 0 || receivedAt > maintainedAt);
    if (fresh) lastIdentityObservation = reid.observationId;
    if (!strong) {
      return reject(trackId, false, "identity_evidence_insufficient");
    }
    if (reid.fresh
        && reid.frameSequence == frame
        && reid.observationTimeMs == receivedAt
        && (lastFreshAt < 0L || receivedAt > lastFreshAt)
        && reid.observationId > lastObservation) {
      lastObservation = reid.observationId;
      lastFreshAt = receivedAt;
      if (firstFreshAt < 0L) firstFreshAt = receivedAt;
      matches = Math.min(global ? 5 : 3, matches + 1);
      if (matches >= (global ? 5 : 3) && (!global || lastFreshAt - firstFreshAt >= 1200L)) {
        verifiedTrack = trackId;
        lastObservedAt = receivedAt;
        lastTrustedAt = lastFreshAt;
        weakSince = -1L;
        if (following && !ambiguous) continuityTrack = trackId;
        multiConflict = false;
      }
    }
    boolean authorized = verifiedTrack == trackId && lastFreshAt >= 0 && now - lastFreshAt <= 500L;
    if (authorized && following && continuity != null && continuity.reliable && !ambiguous)
      continuityTrack = trackId;
    if (!authorized && continuous) {
      return maintainedDecision(
          trackId, maintaining, continuity.reason, "strong_identity_revalidation", true);
    }
    Decision verified =
        new Decision(
            authorized,
            false,
            trackId,
            matches,
            authorized
                ? "identity_authorized"
                : global ? "global_fresh_reid_verification" : "fresh_reid_verification",
            authorized ? State.VERIFIED : global ? State.AUTO_VERIFY : State.ADAPTING,
            authorized,
            authorized,
            authorized && !global && !ambiguous && continuity != null && continuity.reliable,
            0L,
            continuity == null ? "not_evaluated" : continuity.reason,
            0L,
            recoveryType,
            firstFreshAt < 0L ? 0L : lastFreshAt - firstFreshAt,
            lastFreshAt,
            lastObservation);
    if (authorized && following && continuity != null && continuity.currentBox != null) {
      verified.tracking =
          new TrackingDecision(
              session,
              frame,
              receivedAt,
              trackId,
              continuity.currentBox,
              highConfidence,
              !ambiguous,
              continuity.stableFrames,
              true,
              highConfidence && candidateCount == 1 && continuity.reliable,
              "verified_tracking");
    }
    return verified;
  }

  private Decision continuityDecision(
      int trackId,
      State state,
      String reason,
      long evidenceTime,
      long observationId,
      boolean move) {
    return new Decision(
        false,
        false,
        trackId,
        matches,
        reason,
        state,
        true,
        move,
        true,
        0L,
        reason,
        0L,
        RecoveryType.LOCAL,
        0L,
        evidenceTime,
        observationId);
  }

  private void resetMaintainedEvidence() {
    maintainedAt = -1L;
    maintainedObservation = -1L;
    maintainedMatches = 0;
    maintaining = false;
  }

  private Decision maintainedDecision(
      int trackId, boolean motion, String continuity, String reason, boolean sample) {
    return new Decision(
        false,
        false,
        trackId,
        maintainedMatches,
        reason,
        motion ? State.TRACK_MAINTAINED : State.ADAPTING,
        true,
        motion,
        sample,
        0L,
        continuity,
        0L,
        RecoveryType.LOCAL,
        0L,
        maintainedAt,
        maintainedObservation);
  }

  private Decision reject(int trackId, boolean confirmation, String reason) {
    recoveryDeadline = -1L;
    recoveryObservations = 0;
    restrictedRecovery = false;
    RecoveryType rejectedType = recoveryType;
    continuityTrack = -1;
    resetMaintainedEvidence();
    weakSince = -1;
    clearVerification();
    return new Decision(
        false,
        confirmation,
        trackId,
        0,
        reason,
        confirmation || rejectedType == RecoveryType.GLOBAL ? State.AUTO_VERIFY : State.ADAPTING,
        false,
        false,
        false,
        0L,
        "not_evaluated",
        0L,
        rejectedType,
        0L);
  }

  private void clearVerification() {
    verifiedTrack = candidate = -1;
    matches = 0;
    lastFreshAt = -1L;
    firstFreshAt = -1L;
    recoveryType = RecoveryType.NONE;
  }
}
