package org.openbot.cartfollow;

/** Observed-position aiming: short turns separated by a stationary, fresh observation. */
public final class TargetAimController {
  public static final float FAR_ENTER_ERROR = FollowTuning.FAR_PIVOT_ENTER;
  public static final float FAR_EXIT_ERROR = FollowTuning.FAR_PIVOT_EXIT;
  public static final long PULSE_MS = FollowTuning.VISIBLE_PIVOT_MS;
  public static final long SETTLE_MS = FollowTuning.REVERSAL_SETTLE_MS;
  private long pulseUntil = -1L;
  private long settleUntil = -1L;
  private int direction;
  private int pulseSpeed;
  private boolean aiming;
  private int brakedDirection;
  private long brakedAt = -1L;

  public void reset() {
    pulseUntil = settleUntil = -1L;
    direction = 0;
    brakedDirection = 0;
    brakedAt = -1L;
    aiming = false;
  }

  /** Called by the command scheduler too, so a stalled camera cannot extend a turn pulse. */
  public boolean expire(long nowMs) {
    if (pulseUntil < 0L || nowMs < pulseUntil) return false;
    brake(nowMs);
    return true;
  }

  private void brake(long nowMs) {
    brakedDirection = direction;
    brakedAt = nowMs;
    pulseUntil = -1L;
    settleUntil = nowMs + (direction == 0 ? SETTLE_MS : FollowTuning.SAME_DIRECTION_SETTLE_MS);
    direction = 0;
  }

  public AimDecision update(
      SteeringEvidence evidence, boolean far, boolean moving, long observedAtMs, long nowMs) {
    float error = evidence.rawError;
    if (Float.isNaN(error)
        || Float.isInfinite(error)
        || Float.isNaN(evidence.lateralRatePerSec)
        || Float.isInfinite(evidence.lateralRatePerSec)) {
      brake(nowMs);
      return AimDecision.blocked("aim_invalid");
    }
    expire(nowMs);
    if (settleUntil >= 0L) {
      // Only another pulse in the same direction earns the shorter stationary observation.
      long deadline = brakedDirection != 0 && brakedDirection * error >= 0f
          ? settleUntil : brakedAt + SETTLE_MS;
      if (nowMs < deadline || observedAtMs < deadline)
        return AimDecision.blocked("aim_settling");
      settleUntil = -1L;
    }
    float magnitude = Math.abs(error);
    float exit = far ? FAR_EXIT_ERROR : RealCartAutoDriveController.AIM_PIVOT_EXIT_ERROR;
    // Rate can only terminate a pulse early, never choose or reverse its direction.
    float approaching = error + evidence.lateralRatePerSec * 0.12f;
    if (pulseUntil >= 0L) {
      if (direction * error <= exit || direction * approaching <= exit) {
        brake(nowMs);
        aiming = false;
        return AimDecision.blocked("aim_brake_observe");
      }
      return pivot(error);
    }
    float enter = far ? FAR_ENTER_ERROR : RealCartAutoDriveController.AIM_PIVOT_ENTER_ERROR;
    if (magnitude <= exit) aiming = false;
    if (magnitude >= enter || aiming) {
      if (brakedDirection * error < 0f
          && (nowMs < brakedAt + SETTLE_MS || observedAtMs < brakedAt + SETTLE_MS))
        return AimDecision.blocked("aim_reversal_settling");
      if (moving) {
        brake(nowMs);
        return AimDecision.blocked(far ? "aim_edge_brake_before_pivot" : "aim_brake_before_pivot");
      }
      aiming = true;
      direction = error < 0f ? -1 : 1;
      pulseSpeed = FollowTuning.pivotSpeed(error);
      pulseUntil = nowMs + PULSE_MS;
      return pivot(error);
    }
    return AimDecision.of(
        magnitude <= RealCartAutoDriveController.AIM_PIVOT_EXIT_ERROR
            ? AimDecision.Mode.HOLD
            : AimDecision.Mode.CURVE,
        error,
        "aim_observed_follow");
  }

  private AimDecision pivot(float error) {
    pulseSpeed = Math.min(pulseSpeed, FollowTuning.pivotSpeed(error));
    return AimDecision.pivot(direction < 0, error, pulseSpeed, "aim_visible_bounded");
  }
}
