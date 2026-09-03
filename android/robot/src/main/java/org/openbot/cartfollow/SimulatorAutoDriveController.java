package org.openbot.cartfollow;

/** Produces real-cart-shaped commands for display only; it never writes to a vehicle. */
public final class SimulatorAutoDriveController {
  public enum Phase {
    IDLE,
    CAPTURE,
    WAIT_CONFIRM,
    REACQUIRE,
    COUNTDOWN,
    FOLLOW,
    RECOVERY_STOP,
    PARKED_WAIT,
    ENDED
  }

  public static final int GEAR_LOW = 14;
  public static final int GEAR_MID = 18;
  public static final int GEAR_HIGH = 21;

  public static final class Result {
    public final Phase phase;
    public final int gear;
    public final int left;
    public final int right;
    public final String reason;
    public final boolean lockout;
    public final long recoveryElapsedMs;
    public final long recoveryLimitMs;

    Result(
        Phase phase,
        int gear,
        int left,
        int right,
        String reason,
        boolean lockout,
        long recoveryElapsedMs,
        long recoveryLimitMs) {
      this.phase = phase;
      this.gear = gear;
      this.left = left;
      this.right = right;
      this.reason = reason;
      this.lockout = lockout;
      this.recoveryElapsedMs = recoveryElapsedMs;
      this.recoveryLimitMs = recoveryLimitMs;
    }

    public String gearLabel() {
      if (gear == GEAR_HIGH) return "高档";
      if (gear == GEAR_MID) return "中档";
      if (gear == GEAR_LOW) return "低档";
      return "停车";
    }
  }

  private long recoveryLimitMs = 2000L;
  private long missingSinceMs = -1L;
  private int currentGear = GEAR_LOW;
  private final AutoGearSelector gears = new AutoGearSelector();
  private final MaintainedStartGate maintainedStart = new MaintainedStartGate();
  private boolean hasFollowed;
  private boolean lastMoving;
  private boolean holdWasMoving;
  private boolean inHold;

  public void setRecoveryLimitMs(long recoveryLimitMs) {
    this.recoveryLimitMs = Math.max(1000L, recoveryLimitMs);
  }

  public Result reset(String reason) {
    maintainedStart.reset();
    missingSinceMs = -1L;
    currentGear = GEAR_LOW;
    gears.reset();
    hasFollowed = false;
    lastMoving = holdWasMoving = inHold = false;
    return stopped(Phase.IDLE, reason, false, 0L);
  }

  public Result update(FollowStateMachine.FrameResult frame, long nowMs) {
    boolean hold =
        frame != null
            && frame.simulatorIdentity != null
            && frame.simulatorIdentity.state == SimulatorIdentityGuard.State.CONTINUITY_HOLD;
    if (hold && !inHold) holdWasMoving = lastMoving;
    if (!hold) holdWasMoving = false;
    inHold = hold;
    Result result = updateInternal(frame, nowMs);
    lastMoving = result.left > 0 && result.right > 0;
    if (hold && !lastMoving) holdWasMoving = false;
    return result;
  }

  private Result updateInternal(FollowStateMachine.FrameResult frame, long nowMs) {
    if (!lastMoving) maintainedStart.prime(frame, nowMs, 500L);
    if (frame == null) return stopped(Phase.IDLE, "frame_missing", false, 0L);
    if (frame.simulatorIdentity != null
        && frame.simulatorIdentity.tracking != null
        && !frame.simulatorIdentity.tracking.matchesFrame(frame))
      return stopped(Phase.RECOVERY_STOP, "tracking_frame_mismatch", false, 0L);
    if (inHold && nowMs >= frame.simulatorIdentity.holdDeadlineMs)
      return stopped(Phase.RECOVERY_STOP, "continuity_deadline", false, 0L);
    DirectedReacquireEvidence search = frame.directedReacquireEvidence;
    if (search != null && search.lockout)
      return stopped(Phase.ENDED, search.reason, true, search.elapsedMs);
    if (frame.frameTiming != null && frame.frameTiming.sourceAgeMs > 500L) {
      return stopped(waitingPhase(frame.state), "frame_stale", false, 0L);
    }
    if (frame.state != FollowState.STOP && frame.state != FollowState.IDLE && search != null) {
      if (search.phase == DirectedReacquireEvidence.Phase.PARKED_WAIT) {
        missingSinceMs = -1L;
        return stopped(Phase.PARKED_WAIT, search.reason, false, search.elapsedMs);
      }
      if (search.phase == DirectedReacquireEvidence.Phase.FAILED || search.lockout) {
        return stopped(Phase.ENDED, search.reason, true, search.elapsedMs);
      }
      if (search.phase == DirectedReacquireEvidence.Phase.TURNING
          || search.phase == DirectedReacquireEvidence.Phase.VERIFYING
          || search.phase == DirectedReacquireEvidence.Phase.SETTLING) {
        missingSinceMs = -1L;
        currentGear = GEAR_LOW;
        gears.reset();
        return new Result(
            Phase.RECOVERY_STOP,
            0,
            search.left(),
            search.right(),
            search.reason,
            false,
            search.elapsedMs,
            search.timeoutMs);
      }
      if (search.phase == DirectedReacquireEvidence.Phase.COMPLETE) missingSinceMs = -1L;
    }
    if (hasFollowed
        && frame.state != FollowState.IDLE
        && frame.state != FollowState.STOP
        && !hasAnyPerson(frame)) {
      return recovery(frame, nowMs);
    }
    if (frame.simulatorIdentity != null && !frame.simulatorIdentity.motionAllowed) {
      if (hasAnyPerson(frame)) missingSinceMs = -1L;
      return stopped(waitingPhase(frame.state), frame.simulatorIdentity.reason, false, 0L);
    }
    boolean maintained = frame.simulatorIdentity != null && frame.simulatorIdentity.isContinuous();
    if (maintained && !frame.simulatorIdentity.allowsForward(nowMs))
      return stopped(Phase.RECOVERY_STOP, "identity_evidence_expired", false, 0L);
    if (inHold && !holdWasMoving)
      return stopped(Phase.RECOVERY_STOP, "continuity_no_restart", false, 0L);
    switch (frame.state) {
      case CAPTURE_TARGET:
        return stopped(Phase.CAPTURE, "collecting_target", false, 0L);
      case LOCKED_PENDING_CONFIRM:
        return stopped(Phase.WAIT_CONFIRM, "waiting_confirmation", false, 0L);
      case CONFIRMED_ARMED:
        return stopped(Phase.REACQUIRE, "stationary_reacquire", false, 0L);
      case REACQUIRE_TARGET:
        if (hasFollowed && (frame.persons == null || frame.persons.isEmpty())) {
          return recovery(frame, nowMs);
        }
        return stopped(Phase.REACQUIRE, "stationary_reacquire", false, 0L);
      case DIRECTED_REACQUIRE:
        return stopped(Phase.RECOVERY_STOP, "directed_reacquire", false, 0L);
      case READY_TO_FOLLOW:
        return stopped(Phase.COUNTDOWN, "countdown", false, 0L);
      case IDENTITY_UNCERTAIN:
      case LOST:
      case SEARCH:
        return recovery(frame, nowMs);
      case STOP:
        return stopped(Phase.ENDED, "state_stop", true, 0L);
      case FOLLOW:
      case FOLLOW_CAUTION:
        break;
      case IDLE:
      default:
        return stopped(Phase.IDLE, "idle", false, 0L);
    }

    missingSinceMs = -1L;
    hasFollowed = true;
    if (frame.behaviorDecision == null) return stopped(Phase.FOLLOW, "decision_missing", false, 0L);
    BehaviorAction action = frame.behaviorDecision.selectedAction;
    if (action != BehaviorAction.FOLLOW_SLOW && action != BehaviorAction.FOLLOW_CAUTION) {
      return stopped(Phase.FOLLOW, frame.behaviorDecision.actionReason, false, 0L);
    }
    if (frame.distanceEstimate == null || frame.distanceEstimate.state != DistanceState.TOO_FAR) {
      String reason =
          frame.distanceEstimate == null
              ? "distance_missing"
              : "distance_" + frame.distanceEstimate.state.name().toLowerCase();
      return stopped(Phase.FOLLOW, reason, false, 0L);
    }
    SteeringEvidence steering = frame.steeringEvidence;
    if (steering == null || !steering.valid) {
      return stopped(Phase.FOLLOW, "steering_unavailable", false, 0L);
    }

    int desiredGear = distanceGear(frame.distanceEstimate.heightScale);
    if (maintained && !lastMoving) {
      if (frame.distanceEstimate.heightScale > .80f)
        return stopped(Phase.FOLLOW, "target_not_far_enough", false, 0L);
      if (!maintainedStart.ready(frame.simulatorIdentity, nowMs))
        return stopped(Phase.FOLLOW, "maintained_start_verification", false, 0L);
    }
    if (inHold
        || frame.simulatorIdentity != null && frame.simulatorIdentity.isAppearanceTransition())
      desiredGear = GEAR_LOW;
    if (steering.demandPercent > 60) desiredGear = GEAR_LOW;
    else if (steering.demandPercent > 30) desiredGear = Math.min(desiredGear, GEAR_MID);
    if (frame.simulatorIdentity != null && frame.simulatorIdentity.tracking != null)
      desiredGear = Math.min(desiredGear, frame.simulatorIdentity.tracking.maximumGear);
    if (!lastMoving) {
      gears.reset();
      currentGear = GEAR_LOW;
    } else selectGear(desiredGear);

    int reductionMax = currentGear == GEAR_HIGH ? 6 : currentGear == GEAR_MID ? 5 : 4;
    int reduction =
        Math.round(reductionMax * Math.max(0, Math.min(100, steering.demandPercent)) / 100f);
    int inner = Math.max(1, currentGear - reduction);
    int left = currentGear;
    int right = currentGear;
    if (steering.direction == SteeringEvidence.Direction.LEFT) left = inner;
    else if (steering.direction == SteeringEvidence.Direction.RIGHT) right = inner;
    return new Result(
        Phase.FOLLOW,
        currentGear,
        left,
        right,
        steering.direction == SteeringEvidence.Direction.NONE ? "follow_straight" : "follow_curve",
        false,
        0L,
        recoveryLimitMs);
  }

  private Result recovery(FollowStateMachine.FrameResult frame, long nowMs) {
    if (hasAnyPerson(frame)) {
      missingSinceMs = -1L;
      return stopped(Phase.RECOVERY_STOP, "person_visible_reacquire", false, 0L);
    }
    if (missingSinceMs < 0L) missingSinceMs = nowMs;
    long elapsed = Math.max(0L, nowMs - missingSinceMs);
    if (elapsed >= recoveryLimitMs) {
      return stopped(Phase.PARKED_WAIT, "target_missing_wait", false, elapsed);
    }
    return stopped(Phase.RECOVERY_STOP, "target_not_visible", false, elapsed);
  }

  private static Phase waitingPhase(FollowState state) {
    switch (state) {
      case CAPTURE_TARGET:
        return Phase.CAPTURE;
      case LOCKED_PENDING_CONFIRM:
        return Phase.WAIT_CONFIRM;
      case READY_TO_FOLLOW:
        return Phase.COUNTDOWN;
      case CONFIRMED_ARMED:
      case REACQUIRE_TARGET:
        return Phase.REACQUIRE;
      default:
        return Phase.RECOVERY_STOP;
    }
  }

  private static boolean hasAnyPerson(FollowStateMachine.FrameResult frame) {
    return (frame.persons != null && !frame.persons.isEmpty())
        || (frame.detectionTierEvidence != null
            && !frame.detectionTierEvidence.lowConfidencePersons.isEmpty());
  }

  private int distanceGear(float heightScale) {
    return gears.distanceGear(heightScale);
  }

  private void selectGear(int desired) {
    currentGear = gears.select(desired);
  }

  private Result stopped(Phase phase, String reason, boolean lockout, long recoveryElapsedMs) {
    if (!"maintained_start_verification".equals(reason)
        && !"continuous_track_maintained".equals(reason)
        && !"strong_identity_revalidation".equals(reason)) maintainedStart.reset();
    currentGear = GEAR_LOW;
    gears.reset();
    return new Result(
        phase,
        0,
        0,
        0,
        reason == null ? "stop" : reason,
        lockout,
        recoveryElapsedMs,
        recoveryLimitMs);
  }
}
