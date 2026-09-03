package org.openbot.cartfollow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Bounded short-term features. Query before append; never an identity authorization source. */
public final class RecentGallery {
  public static final class Status {
    public final boolean enabled;
    public final int size;
    public final float score;
    public final String reason;

    Status(boolean enabled, int size, float score, String reason) {
      this.enabled = enabled;
      this.size = size;
      this.score = score;
      this.reason = reason;
    }
  }

  public static final int CAPACITY = 16;
  public static final long TTL_MS = 5000L;
  public static final long SAMPLE_INTERVAL_MS = 300L;
  private final List<Sample> samples = new ArrayList<>();
  // Keep IDs until capture-time expiry, including samples evicted by capacity. The sampling
  // interval and TTL bound this metadata to at most 17 entries, without retaining extra features.
  private final Map<Long, Long> admittedObservations = new HashMap<>();
  private int track = -1;
  private long observation = -1;
  private long lastSampleAt = -1;

  public void clear() {
    samples.clear();
    admittedObservations.clear();
    track = -1;
    observation = lastSampleAt = -1;
  }

  private void expire(long now) {
    samples.removeIf(s -> now < s.time || now - s.time >= TTL_MS);
    admittedObservations.values().removeIf(time -> now < time || now - time >= TTL_MS);
  }

  public int size(long now) {
    expire(now);
    return samples.size();
  }

  public boolean append(int id, long observationId, long receivedAt, long now, float[] feature) {
    if (id < 0 || feature == null || receivedAt > now || now - receivedAt > 500L) return false;
    if (track != id) clear();
    track = id;
    expire(now);
    if (observationId <= observation
        || lastSampleAt >= 0 && receivedAt - lastSampleAt < SAMPLE_INTERVAL_MS) return false;
    if (!insert(observationId, receivedAt, feature)) return false;
    observation = observationId;
    lastSampleAt = receivedAt;
    return true;
  }

  /**
   * Admit an already-approved delayed sample using its original capture time (same clock as now).
   * Unlike append, observation IDs and capture times need not arrive in increasing order. Existing
   * entries win interval conflicts; admission never refreshes a sample's capture-time TTL. This
   * method does not authorize identity or alter the regular append ordering watermarks.
   */
  public boolean appendRetrospective(
      int id, long observationId, long capturedAt, long now, float[] feature) {
    if (id < 0
        || observationId < 0
        || feature == null
        || capturedAt > now
        || now - capturedAt >= TTL_MS) return false;
    if (track != id) clear();
    track = id;
    expire(now);
    return insert(observationId, capturedAt, feature);
  }

  private boolean insert(long observationId, long capturedAt, float[] feature) {
    if (admittedObservations.containsKey(observationId)) return false;
    for (long time : admittedObservations.values()) {
      long gap = capturedAt >= time ? capturedAt - time : time - capturedAt;
      if (gap < SAMPLE_INTERVAL_MS) return false;
    }
    for (float value : feature) if (!Float.isFinite(value)) return false;
    int index = 0;
    while (index < samples.size() && samples.get(index).time < capturedAt) index++;
    if (samples.size() == CAPACITY && index == 0) return false;
    samples.add(index, new Sample(feature.clone(), capturedAt));
    admittedObservations.put(observationId, capturedAt);
    if (samples.size() > CAPACITY) samples.remove(0);
    return true;
  }

  public float score(int id, float[] feature, long now) {
    expire(now);
    if (id != track || feature == null || samples.size() < 3) return 0f;
    float[] best = {-1f, -1f, -1f};
    for (Sample sample : samples) {
      if (sample.feature.length != feature.length) continue;
      float dot = 0;
      for (int i = 0; i < feature.length; i++) dot += feature[i] * sample.feature[i];
      for (int i = 0; i < 3; i++) {
        if (dot > best[i]) {
          for (int j = 2; j > i; j--) best[j] = best[j - 1];
          best[i] = dot;
          break;
        }
      }
    }
    return Math.max(0f, Math.min(1f, (best[0] + best[1] + best[2]) / 3f));
  }

  private static final class Sample {
    final float[] feature;
    final long time;

    Sample(float[] feature, long time) {
      this.feature = feature;
      this.time = time;
    }
  }
}
