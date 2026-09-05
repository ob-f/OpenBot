package org.openbot.cartfollow;

/** Observed-position aiming: short turns separated by a stationary, fresh observation. */
public final class TargetAimController {
  public static final float FAR_ENTER_ERROR = 0.60f;
  public static final float FAR_EXIT_ERROR = 0.35f;
  public static final long PULSE_MS = 300L;
  public static final long SETTLE_MS = 650L;
  private long pulseUntil = -1L;
  private long settleUntil = -1L;
  private int direction;

  public void reset() {
    pulseUntil = settleUntil = -1L;
    direction = 0;
  }

  /** Called by the command scheduler too, so a stalled camera cannot extend a turn pulse. */
  public boolean expire(long nowMs) {
    if (pulseUntil < 0L || nowMs < pulseUntil) return false;
    brake(nowMs);
    return true;
  }

  private void brake(long nowMs) {
    pulseUntil = -1L;
    settleUntil = nowMs + SETTLE_MS;
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
      if (nowMs < settleUntil || observedAtMs < settleUntil)
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
        return AimDecision.blocked("aim_brake_observe");
      }
      return pivot(error);
    }
    float enter = far ? FAR_ENTER_ERROR : RealCartAutoDriveController.AIM_PIVOT_ENTER_ERROR;
    if (magnitude >= enter) {
      if (moving) {
        brake(nowMs);
        return AimDecision.blocked("aim_brake_before_pivot");
      }
      direction = error < 0f ? -1 : 1;
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
    return AimDecision.of(
        direction < 0 ? AimDecision.Mode.PIVOT_LEFT : AimDecision.Mode.PIVOT_RIGHT,
        error,
        "aim_pulse");
  }
}
