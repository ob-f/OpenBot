package org.openbot.cartfollow;

/** Snapshot of the simulator's edge-exit and active-search decision. */
public final class DirectedReacquireEvidence {
  public enum Phase {
    IDLE,
    ARMED,
    TURNING,
    VERIFYING,
    SETTLING,
    COMPLETE,
    PARKED_WAIT,
    FAILED
  }

  public final Phase phase;
  public final SteeringEvidence.Direction direction;
  public final int speed;
  public final float turnedDegrees;
  public final float targetDegrees;
  public final long elapsedMs;
  public final long timeoutMs;
  public final boolean gyroAvailable;
  public final boolean wrongDirection;
  public final boolean lockout;
  public final String reason;

  public DirectedReacquireEvidence(
      Phase phase,
      SteeringEvidence.Direction direction,
      int speed,
      float turnedDegrees,
      float targetDegrees,
      long elapsedMs,
      long timeoutMs,
      boolean gyroAvailable,
      boolean wrongDirection,
      boolean lockout,
      String reason) {
    this.phase = phase;
    this.direction = direction;
    this.speed = speed;
    this.turnedDegrees = turnedDegrees;
    this.targetDegrees = Math.max(0f, targetDegrees);
    this.elapsedMs = Math.max(0L, elapsedMs);
    this.timeoutMs = Math.max(0L, timeoutMs);
    this.gyroAvailable = gyroAvailable;
    this.wrongDirection = wrongDirection;
    this.lockout = lockout;
    this.reason = reason == null ? "" : reason;
  }

  public static DirectedReacquireEvidence idle(String reason) {
    return new DirectedReacquireEvidence(
        Phase.IDLE,
        SteeringEvidence.Direction.NONE,
        0,
        0f,
        0f,
        0L,
        0L,
        false,
        false,
        false,
        reason);
  }

  public int left() {
    if (phase != Phase.TURNING || direction == SteeringEvidence.Direction.NONE) return 0;
    return direction == SteeringEvidence.Direction.LEFT ? -speed : speed;
  }

  public int right() {
    if (phase != Phase.TURNING || direction == SteeringEvidence.Direction.NONE) return 0;
    return direction == SteeringEvidence.Direction.LEFT ? speed : -speed;
  }

  public String directionLabel() {
    return direction == SteeringEvidence.Direction.LEFT
        ? "左"
        : direction == SteeringEvidence.Direction.RIGHT ? "右" : "-";
  }
}
