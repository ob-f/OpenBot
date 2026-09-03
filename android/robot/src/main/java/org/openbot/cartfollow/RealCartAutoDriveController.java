package org.openbot.cartfollow;

/** Converts shared follow decisions into bounded, forward-only commands for the real cart. */
public final class RealCartAutoDriveController {
  public enum Phase {
    LOCKED,
    WAIT_TARGET,
    WAIT_CENTER,
    MOVING_STRAIGHT,
    CURVE_LEFT,
    CURVE_RIGHT,
    RECOVERY_STOP,
    PARKED_WAIT,
    SEARCH_BRAKE,
    PIVOT
  }

  public enum Intent {
    STOP,
    FOLLOW,
    PIVOT
  }

  public static final int STRAIGHT_SPEED = 14;
  public static final int MIN_INNER_SPEED = 6;
  public static final int BASE_MAX_INNER_REDUCTION = 4;
  public static final int MIN_STEERING_STRENGTH_PERCENT = 20;
  public static final int MAX_STEERING_STRENGTH_PERCENT = 200;
  public static final int CENTER_DEMAND_PERCENT = 10;
  public static final float START_HEIGHT_SCALE = 0.80f;
  public static final int START_STABLE_FRAMES = 3;
  public static final long RECOVERY_LIMIT_MS = 2000L;

  public static final class Result {
    public final int left;
    public final int right;
    public final Phase phase;
    public final String reason;
    public final float rawTurn;
    public final float filteredTurn;
    public final int demandPercent;
    public final SteeringEvidence.Direction direction;
    public final SteeringEvidence.Level level;
    public final float heightScale;
    public final boolean lockout;
    public final Intent intent;
    public final int gear;

    private Result(
        int left,
        int right,
        Phase phase,
        String reason,
        SteeringEvidence evidence,
        float heightScale,
        boolean lockout) {
      this.left = phase == Phase.PIVOT ? Math.max(-21, Math.min(21, left)) : clamp(left);
      this.right = phase == Phase.PIVOT ? Math.max(-21, Math.min(21, right)) : clamp(right);
      this.intent =
          left == 0 && right == 0
              ? Intent.STOP
              : phase == Phase.PIVOT ? Intent.PIVOT : Intent.FOLLOW;
      this.gear = intent == Intent.FOLLOW ? Math.max(this.left, this.right) : 0;
      this.phase = phase;
      this.reason = reason;
      this.rawTurn = evidence == null ? 0f : evidence.rawError;
      this.filteredTurn = evidence == null ? 0f : evidence.filteredError;
      this.demandPercent = evidence == null ? 0 : evidence.demandPercent;
      this.direction = evidence == null ? SteeringEvidence.Direction.NONE : evidence.direction;
      this.level = evidence == null ? SteeringEvidence.Level.CENTER : evidence.level;
      this.heightScale = heightScale;
      this.lockout = lockout;
    }

    public boolean isStop() {
      return left == 0 && right == 0;
    }

    public String levelLabel() {
      switch (level) {
        case SLIGHT:
          return "轻微";
        case MEDIUM:
          return "中等";
        case LARGE:
          return "大幅";
        case EDGE:
          return "接近边缘";
        case CENTER:
        default:
          return "";
      }
    }

    private static int clamp(int value) {
      return Math.max(0, Math.min(RealCartSafetyController.AUTO_MAX, value));
    }
  }

  private boolean moving;
  private int steeringStrengthPercent = 100;
  private int centeredFrames;
  private final AutoGearSelector gears = new AutoGearSelector();
  private final MaintainedStartGate maintainedStart = new MaintainedStartGate();
  private int maximumGear = 21;
  private long lastFrameSequence = -1L;
  private long targetMissingStartMs = -1L;
  private Result lastResult = stopped(Phase.LOCKED, "auto_locked", false, null, Float.NaN);

  public synchronized Result update(FollowStateMachine.FrameResult frame, long nowMs) {
    if (!moving) maintainedStart.prime(frame, nowMs, 400L);
    SteeringEvidence evidence = frame == null ? null : frame.steeringEvidence;
    if (frame == null || frame.behaviorDecision == null) {
      return stopNonRecovery(Phase.WAIT_TARGET, "decision_missing", evidence, Float.NaN);
    }

    BehaviorDecisionResult decision = frame.behaviorDecision;
    float heightScale =
        frame.distanceEstimate == null ? Float.NaN : frame.distanceEstimate.heightScale;
    if (frame.frameTiming == null
        || nowMs < frame.frameTiming.receivedAtMs
        || nowMs - frame.frameTiming.receivedAtMs > 400L) {
      return stopNonRecovery(Phase.WAIT_TARGET, "frame_stale", evidence, heightScale);
    }
    if (frame.state == FollowState.STOP || frame.state == FollowState.IDLE)
      return stopNonRecovery(Phase.LOCKED, "inactive_session", evidence, heightScale);
    if (isRecoveryDecision(frame, decision)) {
      return recoveryStop(frame, nowMs, evidence, heightScale, decision.actionReason);
    }

    SimulatorIdentityGuard.Decision identity = frame.simulatorIdentity;
    if (identity != null && identity.tracking != null && !identity.tracking.matchesFrame(frame))
      return stopNonRecovery(Phase.WAIT_TARGET, "tracking_frame_mismatch", evidence, heightScale);
    if (identity == null || !identity.allowsForward(nowMs))
      return recoveryStop(
          frame,
          nowMs,
          evidence,
          heightScale,
          identity == null ? "identity_unavailable" : identity.reason);

    targetMissingStartMs = -1L;
    if ((decision.selectedAction != BehaviorAction.FOLLOW_SLOW
            && decision.selectedAction != BehaviorAction.FOLLOW_CAUTION)
        || (frame.state != FollowState.FOLLOW && frame.state != FollowState.FOLLOW_CAUTION)
        || frame.distanceEstimate == null
        || frame.distanceEstimate.state != DistanceState.TOO_FAR
        || !isFinite(heightScale)) {
      return stopNonRecovery(
          Phase.WAIT_TARGET, decision.selectedAction.name().toLowerCase(), evidence, heightScale);
    }
    if (evidence == null || !evidence.valid) {
      return stopNonRecovery(Phase.WAIT_TARGET, "steering_unavailable", evidence, heightScale);
    }

    if (!moving) {
      if (heightScale > START_HEIGHT_SCALE) maintainedStart.reset();
      boolean identityReady =
          heightScale <= START_HEIGHT_SCALE && maintainedStart.ready(identity, nowMs);
      if (heightScale <= START_HEIGHT_SCALE && frame.frameSequence > lastFrameSequence) {
        centeredFrames++;
      } else if (heightScale > START_HEIGHT_SCALE) {
        centeredFrames = 0;
      }
      lastFrameSequence = Math.max(lastFrameSequence, frame.frameSequence);
      if (identity.isContinuous() || identity.tracking != null)
        centeredFrames = Math.max(centeredFrames, maintainedStart.observations());
      if (centeredFrames < START_STABLE_FRAMES || !identityReady) {
        return remember(
            stopped(
                Phase.WAIT_CENTER,
                heightScale > START_HEIGHT_SCALE
                    ? "target_not_far_enough"
                    : !identityReady ? "maintained_start_verification" : "target_stabilizing",
                false,
                evidence,
                heightScale));
      }
      moving = true;
      centeredFrames = 0;
      gears.reset();
    } else if (frame.frameSequence > lastFrameSequence) {
      int desired = Math.min(maximumGear, gears.distanceGear(heightScale));
      if (identity.isAppearanceTransition()
          || evidence.demandPercent > 60
          || frame.state == FollowState.FOLLOW_CAUTION
          || decision.selectedAction == BehaviorAction.FOLLOW_CAUTION) desired = 14;
      else if (evidence.demandPercent > 30) desired = Math.min(18, desired);
      if (identity.tracking != null) desired = Math.min(desired, identity.tracking.maximumGear);
      gears.select(desired);
      lastFrameSequence = frame.frameSequence;
    }
    return drive(evidence, heightScale);
  }

  public synchronized Result reset(String reason) {
    maintainedStart.reset();
    moving = false;
    centeredFrames = 0;
    gears.reset();
    lastFrameSequence = -1L;
    targetMissingStartMs = -1L;
    return remember(stopped(Phase.LOCKED, reason, false, null, Float.NaN));
  }

  public synchronized Result getLastResult() {
    return lastResult;
  }

  public synchronized void setSteeringStrengthPercent(int percent) {
    steeringStrengthPercent =
        Math.max(MIN_STEERING_STRENGTH_PERCENT, Math.min(MAX_STEERING_STRENGTH_PERCENT, percent));
  }

  public synchronized int getSteeringStrengthPercent() {
    return steeringStrengthPercent;
  }

  public synchronized void setMaximumGear(int value) {
    maximumGear = AutoGearSelector.cap(value);
  }

  public synchronized int getMaximumGear() {
    return maximumGear;
  }

  public synchronized Result search(RealCartSearchController.Result search) {
    maintainedStart.reset();
    moving = false;
    centeredFrames = 0;
    gears.reset();
    Phase phase =
        search.pivotAllowed
            ? Phase.PIVOT
            : search.evidence.phase == DirectedReacquireEvidence.Phase.PARKED_WAIT
                ? Phase.PARKED_WAIT
                : search.braking ? Phase.SEARCH_BRAKE : Phase.RECOVERY_STOP;
    return remember(
        new Result(
            search.left(), search.right(), phase, search.reason, null, Float.NaN, search.lockout));
  }

  private Result recoveryStop(
      FollowStateMachine.FrameResult frame,
      long nowMs,
      SteeringEvidence evidence,
      float heightScale,
      String reason) {
    if (!MaintainedStartGate.canObserve(frame, nowMs, 400L)) maintainedStart.reset();
    moving = false;
    centeredFrames = 0;
    gears.reset();
    boolean personVisible =
        (frame.persons != null && !frame.persons.isEmpty())
            || (frame.detectionTierEvidence != null
                && !frame.detectionTierEvidence.lowConfidencePersons.isEmpty());
    if (personVisible) {
      targetMissingStartMs = -1L;
      return remember(
          stopped(
              Phase.RECOVERY_STOP,
              reason == null ? "person_visible_reacquire" : reason,
              false,
              evidence,
              heightScale));
    }
    if (targetMissingStartMs < 0L) targetMissingStartMs = nowMs;
    boolean lockout = nowMs - targetMissingStartMs >= RECOVERY_LIMIT_MS;
    if (lockout) {
      return remember(
          stopped(Phase.PARKED_WAIT, "target_missing_wait", false, evidence, heightScale));
    }
    return remember(
        stopped(
            Phase.RECOVERY_STOP,
            reason == null ? "target_recovery" : reason,
            false,
            evidence,
            heightScale));
  }

  private Result drive(SteeringEvidence evidence, float heightScale) {
    int speed = gears.current();
    if (evidence.direction == SteeringEvidence.Direction.NONE
        || evidence.demandPercent <= CENTER_DEMAND_PERCENT) {
      return remember(
          new Result(
              speed,
              speed,
              Phase.MOVING_STRAIGHT,
              "centered_follow",
              evidence,
              heightScale,
              false));
    }

    int innerSpeed = innerSpeedForDemand(speed, evidence.demandPercent, steeringStrengthPercent);
    boolean turnLeft = evidence.direction == SteeringEvidence.Direction.LEFT;
    return remember(
        new Result(
            turnLeft ? innerSpeed : speed,
            turnLeft ? speed : innerSpeed,
            turnLeft ? Phase.CURVE_LEFT : Phase.CURVE_RIGHT,
            "continuous_curve",
            evidence,
            heightScale,
            false));
  }

  static int innerSpeedForDemand(int demandPercent) {
    return innerSpeedForDemand(demandPercent, 100);
  }

  static int innerSpeedForDemand(int demandPercent, int steeringStrengthPercent) {
    return innerSpeedForDemand(14, demandPercent, steeringStrengthPercent);
  }

  static int innerSpeedForDemand(int gear, int demandPercent, int steeringStrengthPercent) {
    int clampedDemand = Math.max(0, Math.min(100, demandPercent));
    int clampedStrength =
        Math.max(
            MIN_STEERING_STRENGTH_PERCENT,
            Math.min(MAX_STEERING_STRENGTH_PERCENT, steeringStrengthPercent));
    int reduction =
        Math.round(
            AutoGearSelector.maximumReduction(gear) * clampedDemand * clampedStrength / 10000f);
    return Math.max(MIN_INNER_SPEED, gear - reduction);
  }

  private Result stopNonRecovery(
      Phase phase, String reason, SteeringEvidence evidence, float heightScale) {
    maintainedStart.reset();
    moving = false;
    centeredFrames = 0;
    gears.reset();
    return remember(stopped(phase, reason, false, evidence, heightScale));
  }

  private Result remember(Result result) {
    lastResult = result;
    return result;
  }

  private static Result stopped(
      Phase phase, String reason, boolean lockout, SteeringEvidence evidence, float heightScale) {
    return new Result(0, 0, phase, reason, evidence, heightScale, lockout);
  }

  private static boolean isRecoveryDecision(
      FollowStateMachine.FrameResult frame, BehaviorDecisionResult decision) {
    if (frame.state == FollowState.IDENTITY_UNCERTAIN
        || frame.state == FollowState.LOST
        || frame.state == FollowState.SEARCH
        || decision.selectedAction == BehaviorAction.LOCAL_SEARCH_LEFT
        || decision.selectedAction == BehaviorAction.LOCAL_SEARCH_RIGHT) {
      return true;
    }
    return decision.selectedAction == BehaviorAction.MOTION_STOP
        && decision.actionReason != null
        && (decision.actionReason.startsWith("identity_")
            || decision.actionReason.startsWith("target_lost"));
  }

  private static boolean isFinite(float value) {
    return !Float.isNaN(value) && !Float.isInfinite(value);
  }
}
