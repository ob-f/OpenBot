package org.openbot.cartfollow;

/** Shared image-scale gearing. No vehicle I/O or identity authorization. */
public final class AutoGearSelector {
  private int gear = 14;
  private int pending = 14;
  private int frames;

  public void reset() {
    gear = pending = 14;
    frames = 0;
  }

  public int current() {
    return gear;
  }

  public static int cap(int value) {
    return value >= 21 ? 21 : value >= 18 ? 18 : 14;
  }

  public int distanceGear(float scale) {
    if (gear == 21 && scale <= .63f) return 21;
    if (gear == 18) {
      if (scale <= .57f) return 21;
      return scale <= .78f ? 18 : 14;
    }
    if (scale <= .57f) return 21;
    return scale <= .72f ? 18 : 14;
  }

  public int select(int desired) {
    desired = cap(desired);
    if (desired <= gear) {
      gear = pending = desired;
      frames = 0;
    } else if (pending != desired) {
      pending = desired;
      frames = 1;
    } else if (++frames >= 3) {
      gear = desired;
      frames = 0;
    }
    return gear;
  }

  public static int maximumReduction(int gear) {
    return gear == 21 ? 6 : gear == 18 ? 5 : 4;
  }
}
