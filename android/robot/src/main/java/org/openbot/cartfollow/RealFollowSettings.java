package org.openbot.cartfollow;

import android.content.SharedPreferences;

/** Persisted follow tuning values. */
public final class RealFollowSettings {
  public static final String PREFS = "real_follow_experiments";
  private static final int VERSION = 2;
  public int maximumGear = 21;
  public int searchSpeed = 5;
  public int searchAngle = 180;
  public long searchTimeoutMs = 10000;
  public float maximumDistanceMultiplier =
      ImageSetpointDistanceEstimator.DEFAULT_MAX_DISTANCE_MULTIPLIER;
  public boolean dynamicGallery = true;
  public boolean recent = true;

  public static RealFollowSettings load(SharedPreferences prefs) {
    RealFollowSettings s = new RealFollowSettings();
    boolean current = prefs.getInt("settings_version", 0) >= VERSION;
    s.maximumGear = AutoGearSelector.cap(prefs.getInt("maximum_gear", 21));
    s.searchSpeed = Math.max(5, Math.min(21, prefs.getInt("search_speed", 5)));
    s.searchAngle =
        current
            ? Math.max(30, Math.min(180, Math.round(prefs.getInt("search_angle", 180) / 15f) * 15))
            : 180;
    s.searchTimeoutMs =
        current
            ? Math.max(
                1000,
                Math.min(10000, Math.round(prefs.getLong("search_timeout", 10000) / 500f) * 500L))
            : 10000;
    s.maximumDistanceMultiplier =
        Math.max(
            ImageSetpointDistanceEstimator.MIN_MAX_DISTANCE_MULTIPLIER,
            Math.min(
                ImageSetpointDistanceEstimator.MAX_MAX_DISTANCE_MULTIPLIER,
                Math.round(prefs.getFloat("maximum_distance_multiplier", 1.10f) * 20f) / 20f));
    s.dynamicGallery = prefs.getBoolean("dynamic_gallery", true);
    s.recent = prefs.getBoolean("recent", true);
    return s;
  }

  public void save(SharedPreferences prefs) {
    prefs
        .edit()
        .putInt("maximum_gear", maximumGear)
        .putInt("search_speed", searchSpeed)
        .putInt("search_angle", searchAngle)
        .putLong("search_timeout", searchTimeoutMs)
        .putFloat("maximum_distance_multiplier", maximumDistanceMultiplier)
        .putBoolean("dynamic_gallery", dynamicGallery)
        .putBoolean("recent", recent)
        .putInt("settings_version", VERSION)
        .apply();
  }
}
