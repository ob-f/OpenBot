package org.openbot.cartfollow;

/** Diagnostic target conversion for CART_AT8236 V1; never a measured wheel speed. */
public final class LegacyWheelSpeedMapping {
  private LegacyWheelSpeedMapping() {}

  public static int targetMmps(int logical, int otherLogical) {
    if (logical == 0) return 0;
    int input = Math.min(255, Math.abs(logical));
    int speed = input <= 14 ? (input * 240 + 7) / 14
        : 240 + ((input - 14) * 360 + 3) / 7;
    speed = Math.max(40, Math.min(600, speed));
    if (logical == -otherLogical) speed = Math.max(80, speed);
    if ((long) logical * otherLogical < 0) speed = Math.min(240, speed);
    return logical < 0 ? -speed : speed;
  }
}
