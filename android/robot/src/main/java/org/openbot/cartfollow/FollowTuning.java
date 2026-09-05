package org.openbot.cartfollow;

/** Candidate tuning for old-firmware field validation; not a calibrated chassis model. */
public final class FollowTuning {
  public static final String VERSION = "follow-curve-damped-v3";
  public static final long SAME_DIRECTION_SETTLE_MS = 150L;
  public static final long REVERSAL_SETTLE_MS = 650L;
  public static final long RECOVERY_CONTEXT_MS = 1000L;
  public static final long SEARCH_CONTINUITY_MS = 500L;

  public static final float CURVE_ENTER = .06f, CURVE_EXIT = .03f;
  public static final float FAR_PIVOT_ENTER = .85f, FAR_PIVOT_EXIT = .55f;
  public static final float NEAR_PIVOT_ENTER = .10f, NEAR_PIVOT_EXIT = .04f;
  public static final long VISIBLE_PIVOT_MS = 600L;
  public static int pivotSpeed(float error) {
    return Math.abs(error) >= .65f ? 10 : Math.abs(error) >= .35f ? 8 : 5;
  }
  public static final float CURVE_BRAKE_SECONDS = .25f;
  public static int effectiveStrength(int savedPercent) {
    return Math.max(20, Math.min(100, savedPercent));
  }

  /** Prediction can remove steering, but cannot amplify it or choose its sign. */
  public static float dampedError(float error, float lateralRate) {
    float magnitude = Math.abs(error);
    return Math.min(magnitude,
        Math.max(0f, magnitude + Math.signum(error) * lateralRate * CURVE_BRAKE_SECONDS));
  }

  public static float curveDemand(float error) {
    return Math.max(0f, Math.min(1f, (Math.abs(error) - CURVE_EXIT) / .82f));
  }
  private FollowTuning() {}

  public static int curveGear(int desired, float rawError) {
    float error = Math.abs(rawError);
    return error > .55f ? Math.min(14, desired)
        : error > .35f ? Math.min(18, desired) : desired;
  }

  public static int maximumReduction(int gear) {
    return 4;
  }
}
