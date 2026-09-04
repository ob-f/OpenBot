package org.openbot.cartfollow;

/** Frame-local permission and command intent for keeping the camera pointed at the target. */
public final class AimDecision {
  public enum Mode {
    HOLD,
    CURVE,
    PIVOT_LEFT,
    PIVOT_RIGHT,
    BLOCKED
  }

  public final Mode mode;
  public final boolean allowed;
  public final float predictedError;
  public final String reason;

  private AimDecision(Mode mode, boolean allowed, float predictedError, String reason) {
    this.mode = mode;
    this.allowed = allowed;
    this.predictedError = predictedError;
    this.reason = reason;
  }

  public static AimDecision of(Mode mode, float predictedError, String reason) {
    return new AimDecision(mode, mode != Mode.BLOCKED, predictedError, reason);
  }

  public static AimDecision blocked(String reason) {
    return new AimDecision(Mode.BLOCKED, false, 0f, reason);
  }

  public boolean pivots() {
    return mode == Mode.PIVOT_LEFT || mode == Mode.PIVOT_RIGHT;
  }
}
