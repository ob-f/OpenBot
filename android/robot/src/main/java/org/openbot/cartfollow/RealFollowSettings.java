package org.openbot.cartfollow;

import android.content.SharedPreferences;

/** Persisted tuning values; permission to rotate is deliberately not persisted. */
public final class RealFollowSettings {
  public static final String PREFS = "real_follow_experiments";
  public int maximumGear = 21;
  public int searchSpeed = 5;
  public int searchAngle = 90;
  public long searchTimeoutMs = 5000;
  public boolean dynamicGallery = true;
  public boolean recent = true;

  public static RealFollowSettings load(SharedPreferences prefs) {
    RealFollowSettings s = new RealFollowSettings();
    s.maximumGear = AutoGearSelector.cap(prefs.getInt("maximum_gear", 21));
    s.searchSpeed = Math.max(5, Math.min(21, prefs.getInt("search_speed", 5)));
    s.searchAngle =
        Math.max(30, Math.min(180, Math.round(prefs.getInt("search_angle", 90) / 15f) * 15));
    s.searchTimeoutMs =
        Math.max(
            1000, Math.min(10000, Math.round(prefs.getLong("search_timeout", 5000) / 500f) * 500L));
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
        .putBoolean("dynamic_gallery", dynamicGallery)
        .putBoolean("recent", recent)
        .apply();
  }
}
