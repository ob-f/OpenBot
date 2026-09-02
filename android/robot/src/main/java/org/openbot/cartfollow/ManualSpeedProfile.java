package org.openbot.cartfollow;

/** Maps the manual straight-line speed selector onto the staged ESP32 logical speed range. */
public final class ManualSpeedProfile {
  public static final int MIN_FORWARD_LOGICAL = 9;
  public static final int MAX_FORWARD_LOGICAL = 21;
  public static final int DEFAULT_FORWARD_LOGICAL = 14;
  private static final int FIRMWARE_FULL_LOGICAL = 21;
  private static final int FIRMWARE_MAX_MMPS = 240;

  private ManualSpeedProfile() {}

  public static int clampForward(int logical) {
    return Math.max(MIN_FORWARD_LOGICAL, Math.min(MAX_FORWARD_LOGICAL, logical));
  }

  /** Keeps reverse below forward speed using the existing 12/14 safety ratio. */
  public static int reverseForForward(int forwardLogical) {
    int clamped = clampForward(forwardLogical);
    return Math.max(1, Math.round(clamped * RealCartSafetyController.MANUAL_REVERSE / 14f));
  }

  public static int estimatedMmps(int logical) {
    if (logical == 0) return 0;
    int magnitude = Math.abs(logical);
    int scaled = Math.round(magnitude * FIRMWARE_MAX_MMPS / (float) FIRMWARE_FULL_LOGICAL);
    return Math.max(40, Math.min(FIRMWARE_MAX_MMPS, scaled));
  }
}
