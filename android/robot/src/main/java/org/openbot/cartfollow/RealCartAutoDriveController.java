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
    RECOVERY_STOP
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

    private Result(
        int left,
        int right,
        Phase phase,
        String reason,
        SteeringEvidence evidence,
        float heightScale,
        boolean lockout) {
      this.left = clamp(left);
      this.right = clamp(right);
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
  private long targetMissingStartMs = -1L;
  private Result lastResult = stopped(Phase.LOCKED, "auto_locked", false, null, Float.NaN);

  public synchronized Result update(FollowStateMachine.FrameResult frame, long nowMs) {
    SteeringEvidence evidence = frame == null ? null : frame.steeringEvidence;
    if (frame == null || frame.behaviorDecision == null) {
      return stopNonRecovery(Phase.WAIT_TARGET, "decision_missing", evidence, Float.NaN);
    }

    BehaviorDecisionResult decision = frame.behaviorDecision;
    float heightScale = frame.distanceEstimate == null ? Float.NaN : frame.distanceEstimate.heightScale;
    if (isRecoveryDecision(frame, decision)) {
      return recoveryStop(frame, nowMs, evidence, heightScale, decision.actionReason);
    }

    targetMissingStartMs = -1L;
    if (decision.selectedAction != BehaviorAction.FOLLOW_SLOW
        || frame.state != FollowState.FOLLOW
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
      if (heightScale <= START_HEIGHT_SCALE) {
        centeredFrames++;
      } else {
        centeredFrames = 0;
      }
      if (centeredFrames < START_STABLE_FRAMES) {
        return stopped(
            Phase.WAIT_CENTER,
            heightScale > START_HEIGHT_SCALE ? "target_not_far_enough" : "target_stabilizing",
            false,
            evidence,
            heightScale);
      }
      moving = true;
      centeredFrames = 0;
    }
    return drive(evidence, heightScale);
  }

  public synchronized Result reset(String reason) {
    moving = false;
    centeredFrames = 0;
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

  private Result recoveryStop(
      FollowStateMachine.FrameResult frame,
      long nowMs,
      SteeringEvidence evidence,
      float heightScale,
      String reason) {
    moving = false;
    centeredFrames = 0;
    boolean personVisible = frame.persons != null && !frame.persons.isEmpty();
    if (personVisible) {
      targetMissingStartMs = -1L;
      return remember(
          stopped(
              Phase.RECOVERY_STOP,
              "person_visible_reacquire",
              false,
              evidence,
              heightScale));
    }
    if (targetMissingStartMs < 0L) targetMissingStartMs = nowMs;
    boolean lockout = nowMs - targetMissingStartMs >= RECOVERY_LIMIT_MS;
    if (lockout) {
      return remember(stopped(Phase.LOCKED, "target_missing_timeout", true, evidence, heightScale));
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
    if (evidence.direction == SteeringEvidence.Direction.NONE
        || evidence.demandPercent <= CENTER_DEMAND_PERCENT) {
      return remember(
          new Result(
              STRAIGHT_SPEED,
              STRAIGHT_SPEED,
              Phase.MOVING_STRAIGHT,
              "centered_follow",
              evidence,
              heightScale,
              false));
    }

    int innerSpeed = innerSpeedForDemand(evidence.demandPercent, steeringStrengthPercent);
    boolean turnLeft = evidence.direction == SteeringEvidence.Direction.LEFT;
    return remember(
        new Result(
            turnLeft ? innerSpeed : STRAIGHT_SPEED,
            turnLeft ? STRAIGHT_SPEED : innerSpeed,
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
    int clampedDemand = Math.max(0, Math.min(100, demandPercent));
    int clampedStrength =
        Math.max(MIN_STEERING_STRENGTH_PERCENT, Math.min(MAX_STEERING_STRENGTH_PERCENT, steeringStrengthPercent));
    int reduction =
        Math.round(BASE_MAX_INNER_REDUCTION * clampedDemand * clampedStrength / 10000f);
    return Math.max(MIN_INNER_SPEED, STRAIGHT_SPEED - reduction);
  }

  private Result stopNonRecovery(
      Phase phase, String reason, SteeringEvidence evidence, float heightScale) {
    moving = false;
    centeredFrames = 0;
    return remember(stopped(phase, reason, false, evidence, heightScale));
  }

  private Result remember(Result result) {
    lastResult = result;
    return result;
  }

  private static Result stopped(
      Phase phase,
      String reason,
      boolean lockout,
      SteeringEvidence evidence,
      float heightScale) {
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
