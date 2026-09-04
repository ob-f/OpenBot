package org.openbot.cartfollow;

import java.util.ArrayDeque;

/** Shared bounded search driven by observations; hardware adds a separate motion gate. */
public final class DirectedReacquireController {
  private static final long HISTORY_MS = 800L;
  private static final long STRONG_IDENTITY_MS = 1000L;
  private static final float EPSILON = 0.000001f;
  private int speed = 5;
  private float targetDegrees = 180f;
  private long timeoutMs = 10000L;
  private SteeringEvidence.Direction exitDirection = SteeringEvidence.Direction.NONE;
  private long exitEvidenceMs = -1L;
  private TargetObservationEvidence previous;
  private final ArrayDeque<TargetObservationEvidence> history = new ArrayDeque<>();
  private String exitReason = "exit_history_empty";
  private int outwardObservations;
  private long firstOutwardMs = -1L;
  private long trustedAtMs = -1L;
  private int trustedTrackId = -1;
  private long missingSinceMs = -1L;
  private long lastFrameSequence = -1L;
  private long lastUpdateMs = -1L;
  private long generation = -1L;
  private int missingFrames;
  private boolean active;
  private boolean hasFollowed;
  private boolean enterRequested;
  private boolean waitForSubmittedRotation;
  private boolean rotationSubmitted;
  private long searchStartedMs = -1L;
  private long settleStartedMs = -1L;
  private int recoveryTrackId = -1;
  private int freshReidCount;
  private long lastReidId;
  private long lastFreshReidMs = -1L;
  private int continuityRecoveryTrackId = -1;
  private int continuityRecoveryFrames;
  private long continuityRecoveryFirstMs = -1L;
  private float continuityRecoveryLastCenter = Float.NaN;
  private long candidatePauseStartedMs = -1L;
  private DirectedReacquireEvidence terminal;
  private DirectedReacquireEvidence parked;
  private long parkedAtMs = -1L;

  public synchronized void configure(int speed, float targetDegrees, long timeoutMs) {
    this.speed = Math.max(5, Math.min(21, speed));
    this.targetDegrees =
        Float.isFinite(targetDegrees) ? Math.max(30f, Math.min(180f, targetDegrees)) : 180f;
    this.timeoutMs = Math.max(1000L, Math.min(10000L, timeoutMs));
  }

  public synchronized void requireSubmittedRotation() {
    waitForSubmittedRotation = true;
  }

  public synchronized void onRotationSubmitted(YawTurnTracker yaw) {
    if (!active || rotationSubmitted) return;
    yaw.reset(exitDirection);
    rotationSubmitted = true;
  }

  public synchronized DirectedReacquireEvidence update(
      FollowStateMachine.FrameResult frame, long nowMs, YawTurnTracker yaw) {
    if (frame != null && generation != frame.sessionGeneration) {
      reset();
      generation = frame.sessionGeneration;
    }
    if (terminal != null) return terminal;
    if (active) {
      DirectedReacquireEvidence deadline = checkDeadline(nowMs, yaw);
      if (deadline != null) return deadline;
    }
    if (frame == null)
      return active
          ? fail(yaw, nowMs, "frame_missing")
          : parked != null ? parked : DirectedReacquireEvidence.idle("frame_missing");
    if (frame.frameTiming != null && frame.frameTiming.sourceAgeMs > 500L) {
      if (active) return fail(yaw, nowMs, "frame_stale");
      clearExit();
      missingSinceMs = -1L;
      missingFrames = 0;
      return parked != null ? parked : DirectedReacquireEvidence.idle("frame_stale");
    }
    if (frame.state == FollowState.STOP || frame.state == FollowState.IDLE) {
      if (active) return fail(yaw, nowMs, "state_stop");
      parked = null;
      hasFollowed = false;
      clearExit();
      return DirectedReacquireEvidence.idle("inactive_session");
    }
    boolean newFrame =
        frame.frameSequence > 0L ? frame.frameSequence > lastFrameSequence : nowMs > lastUpdateMs;
    lastFrameSequence = Math.max(lastFrameSequence, frame.frameSequence);
    lastUpdateMs = Math.max(lastUpdateMs, nowMs);
    TargetObservationEvidence o = frame.targetObservation;
    int candidates = candidateCount(frame);
    boolean current = validObservation(o, nowMs);
    if (candidates == 0) candidatePauseStartedMs = -1L;
    else if (candidatePauseStartedMs < 0L) candidatePauseStartedMs = nowMs;
    boolean verifiedFollow =
        current
            && candidates == 1
            && isFollowing(frame)
            && !o.lowConfidence
            && o.belief >= .75f
            && recoveryIdentityValid(frame, o)
            && recentBoundReid(frame, o, nowMs);
    if (parked != null) {
      // A frame captured before parking cannot re-arm a consumed exit episode.
      if (!newFrame || !verifiedFollow || o.observedAtMs <= parkedAtMs) return parked;
      parked = null;
      clearExit();
      missingSinceMs = -1L;
      missingFrames = 0;
    }
    if (newFrame && verifiedFollow) hasFollowed = true;
    if (!hasFollowed) {
      clearExit();
      missingSinceMs = -1L;
      missingFrames = 0;
      return DirectedReacquireEvidence.idle("awaiting_first_follow");
    }

    if (!active) {
      lastReidId = Math.max(lastReidId, reidId(frame));
      if (newFrame && current) {
        missingSinceMs = -1L;
        missingFrames = 0;
        if (candidates == 1 && o.personCount == 1) {
          if (o.trackId != trustedTrackId) clearExit();
          long strongAt = strongIdentityTime(frame, o, nowMs);
          if (strongAt >= 0L) {
            trustedAtMs = Math.max(trustedAtMs, strongAt);
            trustedTrackId = o.trackId;
          }
          if (trustedTrackId == o.trackId
              && trustedAtMs >= 0L
              && nowMs - trustedAtMs <= STRONG_IDENTITY_MS
              && continuityPermits(frame, o)) rememberExit(o);
          else {
            clearExit();
            exitReason =
                strongAt < 0L ? "strong_identity_expired_or_unreliable" : "identity_ambiguous";
          }
        } else {
          clearExit();
          exitReason = "exit_ambiguous_people";
        }
      }
      if (current) return DirectedReacquireEvidence.idle(exitReason);
      if (newFrame) {
        if (missingSinceMs < 0L) missingSinceMs = nowMs;
        missingFrames++;
      }
      if (candidates > 0) {
        clearExit();
        exitReason = "target_missing_other_candidates";
        return DirectedReacquireEvidence.idle(exitReason);
      }
      if (!history.isEmpty()) evaluateTrajectory(nowMs);
      if ((exitEvidenceMs >= 0L && nowMs - firstOutwardMs > HISTORY_MS)
          || (trustedAtMs >= 0L && nowMs - trustedAtMs > STRONG_IDENTITY_MS)) {
        clearExit();
        exitReason = "exit_evidence_expired";
      }
      if (missingFrames < 2
          || nowMs - missingSinceMs < 100L
          || outwardObservations < 2
          || exitEvidenceMs < 0L
          || nowMs - firstOutwardMs > HISTORY_MS
          || trustedAtMs < 0L
          || nowMs - trustedAtMs > STRONG_IDENTITY_MS
          || nowMs - exitEvidenceMs > HISTORY_MS) {
        return DirectedReacquireEvidence.idle(
            outwardObservations >= 2 ? "target_missing_debounce" : exitReason);
      }
      active = true;
      enterRequested = true;
      searchStartedMs = nowMs;
      clearRecovery();
      lastReidId = Math.max(lastReidId, reidId(frame));
      rotationSubmitted = false;
      if (!waitForSubmittedRotation) yaw.reset(exitDirection);
    }

    // Verification and settling share the same absolute deadline as turning.
    DirectedReacquireEvidence deadline = checkDeadline(nowMs, yaw);
    if (deadline != null) return deadline;
    if (newFrame) {
      if (candidates == 1
          && current
          && o.belief >= 0.75f
          && !o.lowConfidence
          && recoveryIdentityValid(frame, o)) {
        if (recoveryTrackId != o.trackId) {
          clearStrongRecovery();
          recoveryTrackId = o.trackId;
        }
        if (lastFreshReidMs >= 0L && nowMs - lastFreshReidMs > 500L) {
          clearStrongRecovery();
          recoveryTrackId = o.trackId;
        }
        long id = reidId(frame);
        if (id > lastReidId) {
          lastReidId = id;
          freshReidCount++;
          lastFreshReidMs = nowMs;
        }
      } else {
        clearStrongRecovery();
      }
      updateContinuityRecovery(frame, o, candidates, current, nowMs);
    }
    boolean strongRecovered =
        current
            && candidates == 1
            && o.belief >= 0.75f
            && !o.lowConfidence
            && freshReidCount >= 3
            && recoveryIdentityValid(frame, o)
            && lastFreshReidMs >= 0L
            && nowMs - lastFreshReidMs <= 500L
            && (frame.state == FollowState.FOLLOW || frame.state == FollowState.FOLLOW_CAUTION);
    boolean continuityRecovered =
        current
            && candidates == 1
            && continuityRecoveryFrames >= 3
            && continuityRecoveryFirstMs >= 0L
            && nowMs - continuityRecoveryFirstMs >= 100L
            && associationUnique(frame, o);
    boolean recovered = strongRecovered || continuityRecovered;
    if (recovered) {
      if (continuityRecovered) {
        DirectedReacquireEvidence done =
            evidence(
                DirectedReacquireEvidence.Phase.COMPLETE,
                yaw,
                nowMs,
                false,
                "recovered_by_continuity");
        active = false;
        enterRequested = false;
        clearExit();
        clearRecovery();
        missingFrames = 0;
        missingSinceMs = -1L;
        return done;
      }
      if (settleStartedMs < 0L) settleStartedMs = nowMs;
      if (nowMs - settleStartedMs >= 300L) {
        DirectedReacquireEvidence done =
            evidence(
                DirectedReacquireEvidence.Phase.COMPLETE,
                yaw,
                nowMs,
                false,
                "recovered_without_countdown");
        active = false;
        enterRequested = false;
        clearExit();
        missingFrames = 0;
        missingSinceMs = -1L;
        return done;
      }
      return evidence(
          DirectedReacquireEvidence.Phase.SETTLING, yaw, nowMs, false, "recovery_settle");
    }
    settleStartedMs = -1L;
    // Persistent rejected candidates remain stopped; they cannot cause spin/stop chatter.
    if (candidates > 0 && (candidatePauseStartedMs < 0L || nowMs - candidatePauseStartedMs < 500L))
      return evidence(
          DirectedReacquireEvidence.Phase.VERIFYING,
          yaw,
          nowMs,
          false,
          current ? "candidate_stationary_verification" : "target_missing_other_candidates");
    return evidence(
        DirectedReacquireEvidence.Phase.TURNING,
        yaw,
        nowMs,
        false,
        yaw.isAvailable() ? "directed_search" : "gyro_unavailable_time_only");
  }

  public synchronized boolean isActive() {
    return active;
  }

  /** A recent verified side exit can freeze gallery learning before search starts. */
  public synchronized boolean hasRecentExitEvidence(long nowMs) {
    if (active || parked != null || terminal != null) return false;
    if (!history.isEmpty()) evaluateTrajectory(nowMs);
    return outwardObservations >= 2
        && trustedAtMs >= 0L
        && nowMs >= trustedAtMs
        && nowMs - trustedAtMs <= STRONG_IDENTITY_MS
        && exitEvidenceMs >= 0L
        && nowMs >= exitEvidenceMs
        && nowMs - exitEvidenceMs <= HISTORY_MS;
  }

  public synchronized boolean consumeEnterRequest() {
    boolean result = enterRequested;
    enterRequested = false;
    return result;
  }

  public synchronized void reset() {
    active = false;
    rotationSubmitted = false;
    hasFollowed = false;
    enterRequested = false;
    terminal = null;
    parked = null;
    parkedAtMs = -1L;
    generation = -1L;
    searchStartedMs = -1L;
    lastFrameSequence = -1L;
    lastUpdateMs = -1L;
    missingSinceMs = -1L;
    missingFrames = 0;
    lastReidId = 0L;
    clearRecovery();
    clearExit();
  }

  private static boolean validObservation(TargetObservationEvidence o, long nowMs) {
    return o != null
        && o.current
        && o.trackId >= 0
        && Float.isFinite(o.belief)
        && nowMs >= o.observedAtMs
        && nowMs - o.observedAtMs <= 500L
        && o.screenBox != null
        && Float.isFinite(o.screenBox.left)
        && Float.isFinite(o.screenBox.right)
        && o.screenBox.left >= 0f
        && o.screenBox.right <= 1f
        && o.screenBox.left < o.screenBox.right;
  }

  private void rememberExit(TargetObservationEvidence o) {
    if (previous != null && o.observedAtMs <= previous.observedAtMs) return;
    history.addLast(o);
    previous = o;
    evaluateTrajectory(o.observedAtMs);
  }

  private void evaluateTrajectory(long nowMs) {
    boolean expired = false;
    while (!history.isEmpty() && nowMs - history.peekFirst().observedAtMs > HISTORY_MS) {
      history.removeFirst();
      expired = true;
    }
    clearTrajectoryResult();
    if (history.size() < 2) {
      if (expired) exitReason = "exit_evidence_expired";
      return;
    }
    TargetObservationEvidence o = history.peekLast();
    int sign = 0;
    int jitter = 0;
    TargetObservationEvidence last = null;
    for (TargetObservationEvidence sample : history) {
      if (last != null) {
        float leftDelta = sample.screenBox.left - last.screenBox.left;
        float rightDelta = sample.screenBox.right - last.screenBox.right;
        float delta = (leftDelta + rightDelta) / 2f;
        if (leftDelta * rightDelta < 0f
            && Math.min(Math.abs(leftDelta), Math.abs(rightDelta)) >= .01f - EPSILON) {
          restartTrajectory(o, "exit_geometry_ambiguous");
          return;
        }
        if (sign == 0 && Math.abs(delta) > EPSILON) sign = delta > 0f ? 1 : -1;
        if (sign * delta <= EPSILON) {
          if (Math.max(Math.abs(leftDelta), Math.abs(rightDelta)) >= .01f - EPSILON) {
            restartTrajectory(o, "exit_marked_reverse");
            return;
          }
          if (++jitter > 1) {
            restartTrajectory(o, "exit_repeated_jitter");
            return;
          }
        }
      }
      last = sample;
    }
    TargetObservationEvidence first = history.peekFirst();
    float displacement = sign * (o.screenBox.centerX() - first.screenBox.centerX());
    boolean edge = sign < 0 ? o.screenBox.left <= .15f : sign > 0 && o.screenBox.right >= .85f;
    if (history.size() >= 2 && displacement >= .02f - EPSILON && edge) {
      outwardObservations = history.size();
      firstOutwardMs = first.observedAtMs;
      exitDirection = sign < 0 ? SteeringEvidence.Direction.LEFT : SteeringEvidence.Direction.RIGHT;
      exitEvidenceMs = o.observedAtMs;
      exitReason = jitter == 0 ? "exit_outward_verified" : "exit_outward_jitter_tolerated";
    } else {
      exitReason = !edge ? "exit_not_at_edge" : "exit_displacement_insufficient";
    }
  }

  private void restartTrajectory(TargetObservationEvidence o, String reason) {
    history.clear();
    history.addLast(o);
    previous = o;
    clearTrajectoryResult();
    exitReason = reason;
  }

  private void clearTrajectoryResult() {
    outwardObservations = 0;
    firstOutwardMs = -1L;
    exitDirection = SteeringEvidence.Direction.NONE;
    exitEvidenceMs = -1L;
  }

  private void clearExit() {
    trustedTrackId = -1;
    trustedAtMs = -1L;
    previous = null;
    history.clear();
    clearTrajectoryResult();
    exitReason = "exit_history_empty";
  }

  private static boolean isFollowing(FollowStateMachine.FrameResult frame) {
    return frame.state == FollowState.FOLLOW || frame.state == FollowState.FOLLOW_CAUTION;
  }

  private static long strongIdentityTime(
      FollowStateMachine.FrameResult frame, TargetObservationEvidence o, long nowMs) {
    if (o.lowConfidence || o.belief < .75f || !recoveryIdentityValid(frame, o)) return -1L;
    ReIDMatchResult match = frame.identityEvidence.reidMatch;
    if (!match.fresh
        || !recentBoundReid(frame, o, nowMs)
        || (frame.frameSequence > 0L && match.frameSequence != frame.frameSequence)
        || match.observationTimeMs != o.observedAtMs) return -1L;
    return match.observationTimeMs;
  }

  private static boolean recentBoundReid(
      FollowStateMachine.FrameResult frame, TargetObservationEvidence o, long nowMs) {
    ReIDMatchResult match = frame.identityEvidence.reidMatch;
    return match.isBoundToTrack(o.trackId)
        && match.observationTimeMs >= 0L
        && match.observationTimeMs <= o.observedAtMs
        && nowMs - match.observationTimeMs <= 500L;
  }

  private static boolean continuityPermits(
      FollowStateMachine.FrameResult frame, TargetObservationEvidence o) {
    SimulatorIdentityGuard.Decision identity = frame.simulatorIdentity;
    if (identity == null) return true;
    return identity.trackId == o.trackId
        && !identity.needsConfirmation
        && (identity.authorized
            || (identity.retainTarget
                && (identity.isMaintained()
                    || identity.state == SimulatorIdentityGuard.State.CONTINUITY_HOLD
                    || identity.state == SimulatorIdentityGuard.State.ADAPTING)));
  }

  private void clearRecovery() {
    clearStrongRecovery();
    clearContinuityRecovery();
    candidatePauseStartedMs = -1L;
  }

  private void clearStrongRecovery() {
    recoveryTrackId = -1;
    freshReidCount = 0;
    lastFreshReidMs = -1L;
    settleStartedMs = -1L;
  }

  private void updateContinuityRecovery(
      FollowStateMachine.FrameResult frame,
      TargetObservationEvidence o,
      int candidates,
      boolean current,
      long nowMs) {
    if (!current || candidates != 1 || !associationUnique(frame, o)) {
      clearContinuityRecovery();
      return;
    }
    float center = o.screenBox.centerX();
    boolean entersExpectedEdge =
        exitDirection == SteeringEvidence.Direction.LEFT
            ? o.screenBox.left <= .25f
            : exitDirection == SteeringEvidence.Direction.RIGHT && o.screenBox.right >= .75f;
    if (continuityRecoveryTrackId != o.trackId) {
      clearContinuityRecovery();
      if (!entersExpectedEdge) {
        if (candidatePauseStartedMs < 0L) candidatePauseStartedMs = nowMs;
        return;
      }
      continuityRecoveryTrackId = o.trackId;
      continuityRecoveryFrames = 1;
      continuityRecoveryFirstMs = nowMs;
      continuityRecoveryLastCenter = center;
      candidatePauseStartedMs = nowMs;
      return;
    }
    boolean inward =
        exitDirection == SteeringEvidence.Direction.LEFT
            ? center >= continuityRecoveryLastCenter + .003f
            : center <= continuityRecoveryLastCenter - .003f;
    if (inward || Math.abs(center - .5f) < Math.abs(continuityRecoveryLastCenter - .5f)) {
      continuityRecoveryFrames++;
      continuityRecoveryLastCenter = center;
    }
  }

  private void clearContinuityRecovery() {
    continuityRecoveryTrackId = -1;
    continuityRecoveryFrames = 0;
    continuityRecoveryFirstMs = -1L;
    continuityRecoveryLastCenter = Float.NaN;
  }

  private static boolean associationUnique(
      FollowStateMachine.FrameResult frame, TargetObservationEvidence o) {
    return o != null
        && o.current
        && (frame.trackingDecision == null
            || (frame.trackingDecision.trackId == o.trackId
                && frame.trackingDecision.associationUnique));
  }

  private static boolean recoveryIdentityValid(
      FollowStateMachine.FrameResult frame, TargetObservationEvidence o) {
    if (frame.simulatorIdentity != null
        && (!frame.simulatorIdentity.authorized
            || frame.simulatorIdentity.needsConfirmation
            || frame.simulatorIdentity.trackId != o.trackId)) return false;
    IdentityEvidence id = frame.identityEvidence;
    return id != null
        && id.trackId >= 0
        && id.trackId == o.trackId
        && id.trackId == id.lockedTrackId
        && id.missedFrames == 0
        && id.reidMatch != null
        && id.reidMatch.reidAvailable
        && Float.isFinite(id.reidMatch.bestScore)
        && Float.isFinite(id.reidMatch.margin)
        && id.reidMatch.bestScore >= 0.85f
        && id.reidMatch.margin >= 0.08f;
  }

  private static long reidId(FollowStateMachine.FrameResult frame) {
    return frame.identityEvidence == null || frame.identityEvidence.reidMatch == null
        ? 0L
        : frame.identityEvidence.reidMatch.observationId;
  }

  private static int candidateCount(FollowStateMachine.FrameResult frame) {
    if (frame.identityCandidates != null) return frame.identityCandidates.size();
    int count =
        (frame.persons == null ? 0 : frame.persons.size())
            + (frame.detectionTierEvidence == null
                ? 0
                : frame.detectionTierEvidence.lowConfidencePersons.size());
    return Math.max(
        count,
        frame.targetObservation != null && frame.targetObservation.current
            ? Math.max(1, frame.targetObservation.personCount)
            : 0);
  }

  private DirectedReacquireEvidence fail(YawTurnTracker yaw, long nowMs, String reason) {
    terminal = evidence(DirectedReacquireEvidence.Phase.FAILED, yaw, nowMs, true, reason);
    active = false;
    enterRequested = false;
    return terminal;
  }

  public synchronized DirectedReacquireEvidence pollDeadline(long nowMs, YawTurnTracker yaw) {
    if (terminal != null) return terminal;
    if (parked != null) return parked;
    if (!active) return null;
    return checkDeadline(nowMs, yaw);
  }

  private DirectedReacquireEvidence checkDeadline(long nowMs, YawTurnTracker yaw) {
    if (nowMs - searchStartedMs >= timeoutMs) return park(yaw, nowMs, "search_timeout");
    if ((!waitForSubmittedRotation || rotationSubmitted)
        && yaw.isAvailable()
        && yaw.getTurnedDegrees() >= targetDegrees) return park(yaw, nowMs, "search_angle_limit");
    return null;
  }

  private DirectedReacquireEvidence park(YawTurnTracker yaw, long nowMs, String reason) {
    parked = evidence(DirectedReacquireEvidence.Phase.PARKED_WAIT, yaw, nowMs, false, reason);
    parkedAtMs = nowMs;
    active = false;
    enterRequested = false;
    hasFollowed = false;
    clearExit();
    clearRecovery();
    missingSinceMs = -1L;
    missingFrames = 0;
    return parked;
  }

  private DirectedReacquireEvidence evidence(
      DirectedReacquireEvidence.Phase phase,
      YawTurnTracker yaw,
      long nowMs,
      boolean lockout,
      String reason) {
    return new DirectedReacquireEvidence(
        phase,
        exitDirection,
        speed,
        waitForSubmittedRotation && !rotationSubmitted ? 0f : yaw.getTurnedDegrees(),
        targetDegrees,
        Math.max(0L, nowMs - searchStartedMs),
        timeoutMs,
        yaw.isAvailable(),
        yaw.isWrongDirection(),
        lockout,
        reason);
  }
}
