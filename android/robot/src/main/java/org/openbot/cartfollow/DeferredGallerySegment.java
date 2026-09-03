package org.openbot.cartfollow;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Retrospective learning only. Neither pending nor approved samples authorize the current frame.
 */
public final class DeferredGallerySegment {
  public static final int CAPACITY = 16;
  public static final long TTL_MS = 5000L;
  public static final long SAMPLE_INTERVAL_MS = 200L;
  public static final long MAX_GAP_MS = 500L;
  public static final int MIN_SAMPLES = 3;
  public static final long MIN_DWELL_MS = 600L;
  public static final int REQUIRED_ENDPOINTS = 0;
  private static final float MIN_RAW_SCORE = .60f;
  private static final float STRONG_SCORE = .85f;
  private static final float ANCHOR_FLOOR = .70f;
  private static final float MIN_MARGIN = .08f;
  private static final float MIN_CONTINUITY = .75f;
  private static final float EPSILON = .000001f;

  public static final class Sample {
    public final long observationId;
    public final long frameSequence;
    public final long timestampMs;
    public final int trackId;
    public final float[] feature;
    public final RectF bbox;
    public final long sessionId;
    public final long galleryRevision;
    public final float rawScore;
    public final float anchorScore;
    public final float independentScore;
    public final float margin;

    public Sample(
        long observationId,
        long frameSequence,
        long timestampMs,
        int trackId,
        float[] feature,
        RectF bbox) {
      this(
          observationId,
          frameSequence,
          timestampMs,
          trackId,
          feature,
          bbox,
          -1L,
          -1L,
          Float.NaN,
          Float.NaN,
          Float.NaN,
          Float.NaN);
    }

    private Sample(
        long observationId,
        long frameSequence,
        long timestampMs,
        int trackId,
        float[] feature,
        RectF bbox,
        long sessionId,
        long galleryRevision,
        float rawScore,
        float anchorScore,
        float independentScore,
        float margin) {
      this.observationId = observationId;
      this.frameSequence = frameSequence;
      this.timestampMs = timestampMs;
      this.trackId = trackId;
      this.feature = feature == null ? null : feature.clone();
      this.bbox = bbox == null ? null : new RectF(bbox);
      this.sessionId = sessionId;
      this.galleryRevision = galleryRevision;
      this.rawScore = rawScore;
      this.anchorScore = anchorScore;
      this.independentScore = independentScore;
      this.margin = margin;
    }
  }

  public static final class Result {
    public final List<Sample> approvedSamples;
    public final String reason;
    public final int sampleCount;
    public final int strongEndpoints;
    public final long sessionId;
    public final long galleryRevision;
    /** Approval frame, exclusive: approved samples may only affect later frames. */
    public final long usableAfterFrameSequence;

    private Result(
        List<Sample> approvedSamples,
        String reason,
        int sampleCount,
        int strongEndpoints,
        long sessionId,
        long galleryRevision,
        long usableAfterFrameSequence) {
      this.approvedSamples = Collections.unmodifiableList(new ArrayList<>(approvedSamples));
      this.reason = reason;
      this.sampleCount = sampleCount;
      this.strongEndpoints = strongEndpoints;
      this.sessionId = sessionId;
      this.galleryRevision = galleryRevision;
      this.usableAfterFrameSequence = usableAfterFrameSequence;
    }
  }

  private final List<Sample> samples = new ArrayList<>();
  private List<float[]> anchors = Collections.emptyList();
  private List<float[]> globalAdaptive = Collections.emptyList();
  private long sessionId = -1L;
  private long galleryRevision = -1L;
  private long startedAt = -1L;
  private long lastObservationId = -1L;
  private long lastFrameSequence = -1L;
  private long lastSourceAt = -1L;
  private float[] lastFeature;
  private int trackId = -1;
  private int strongEndpoints;

  public void clear() {
    samples.clear();
    anchors = globalAdaptive = Collections.emptyList();
    sessionId = galleryRevision = startedAt = -1L;
    lastObservationId = lastFrameSequence = lastSourceAt = -1L;
    lastFeature = null;
    trackId = -1;
    strongEndpoints = 0;
  }

  /** Call explicitly on multi-person, ambiguous association, lost continuity or session end. */
  public void invalidateContext() {
    clear();
  }

  public int size(long nowMs) {
    if (expired(nowMs)) clear();
    return samples.size();
  }

  /**
   * Call only for feature observations, not intervening camera frames. Context combines confirmed
   * original target, local continuity, single-person association and crop quality, NOT motion or
   * identity permission. Only the globally eligible Adaptive features belong in the snapshot.
   * Competitor score must be independent of Recent and this segment as well.
   */
  public Result offer(
      long sessionId,
      long galleryRevision,
      Sample sample,
      boolean fresh,
      boolean confirmedOriginalLocalContinuous,
      float rawScore,
      float competitorScore,
      List<float[]> anchors,
      List<float[]> globalAdaptive,
      long nowMs) {
    if (!confirmedOriginalLocalContinuous) return discard("context_invalid");
    if (startedAt >= 0L && this.sessionId != sessionId) return discard("session_changed");
    if (expired(nowMs)) return discard("segment_timeout");
    if (sample == null) return result("no_feature");
    if (startedAt >= 0L && trackId != sample.trackId) return discard("track_changed");
    if (!fresh) return result("cached");
    if (sample.timestampMs < 0L
        || sample.timestampMs > nowMs
        || nowMs - sample.timestampMs > MAX_GAP_MS) return result("stale");
    if (sample.observationId < 0L
        || sample.frameSequence < 0L
        || sample.trackId < 0
        || !validFeature(sample.feature)) return discard("invalid_sample");
    if (startedAt >= 0L
        && (sample.observationId <= lastObservationId
            || sample.frameSequence <= lastFrameSequence
            || sample.timestampMs <= lastSourceAt)) {
      return result("repeated_or_out_of_order");
    }
    if (startedAt >= 0L && sample.timestampMs - lastSourceAt > MAX_GAP_MS) {
      return discard("feature_gap");
    }
    if (!Float.isFinite(rawScore) || rawScore < -1f || rawScore > 1f) {
      return discard("raw_score_low_or_invalid");
    }
    if (!Float.isFinite(competitorScore) || competitorScore < -1f || competitorScore > 1f) {
      return discard("competitor_score_invalid");
    }
    boolean beginning = startedAt < 0L;
    if (beginning) {
      this.anchors = snapshot(anchors, sample.feature.length);
      this.globalAdaptive = snapshot(globalAdaptive, sample.feature.length);
      if (this.anchors.isEmpty()) return discard("no_anchor_snapshot");
      this.sessionId = sessionId;
      this.galleryRevision = galleryRevision;
      startedAt = sample.timestampMs;
      trackId = sample.trackId;
    } else if (cosine(lastFeature, sample.feature) + EPSILON < MIN_CONTINUITY) {
      return discard("feature_discontinuity");
    }

    float anchorScore = maxSimilarity(sample.feature, this.anchors);
    float independentScore =
        Math.max(anchorScore, maxSimilarity(sample.feature, this.globalAdaptive));
    float margin = independentScore - competitorScore;
    boolean strong = independentScore + EPSILON >= STRONG_SCORE && margin + EPSILON >= MIN_MARGIN;
    // Even an unsampled weak feature interrupts a consecutive endpoint run.
    if (!strong) strongEndpoints = 0;
    lastObservationId = sample.observationId;
    lastFrameSequence = sample.frameSequence;
    lastSourceAt = sample.timestampMs;
    lastFeature = sample.feature.clone();
    if (!samples.isEmpty()
        && sample.timestampMs - samples.get(samples.size() - 1).timestampMs < SAMPLE_INTERVAL_MS) {
      return result("sample_interval");
    }

    samples.add(
        new Sample(
            sample.observationId,
            sample.frameSequence,
            sample.timestampMs,
            sample.trackId,
            sample.feature,
            sample.bbox,
            this.sessionId,
            this.galleryRevision,
            rawScore,
            anchorScore,
            independentScore,
            margin));
    if (samples.size() > CAPACITY) samples.remove(0);
    if (strong) strongEndpoints = Math.min(REQUIRED_ENDPOINTS, strongEndpoints + 1);
    if (samples.size() >= MIN_SAMPLES && sample.timestampMs - startedAt >= MIN_DWELL_MS) {
      Result approved =
          new Result(
              samples,
              "approved",
              samples.size(),
              strongEndpoints,
              this.sessionId,
              this.galleryRevision,
              sample.frameSequence);
      clear();
      return approved;
    }
    return result(strong ? "strong_segment_pending" : "continuous_segment_pending");
  }

  private boolean expired(long nowMs) {
    return startedAt >= 0L && (nowMs < startedAt || nowMs - startedAt >= TTL_MS);
  }

  private Result discard(String reason) {
    clear();
    return result(reason);
  }

  private Result result(String reason) {
    return new Result(
        Collections.emptyList(),
        reason,
        samples.size(),
        strongEndpoints,
        sessionId,
        galleryRevision,
        -1L);
  }

  private static List<float[]> snapshot(List<float[]> features, int dimensions) {
    List<float[]> copy = new ArrayList<>();
    if (features != null) {
      for (float[] feature : features) {
        if (validFeature(feature) && feature.length == dimensions) copy.add(feature.clone());
      }
    }
    return copy;
  }

  private static boolean validFeature(float[] feature) {
    if (feature == null || feature.length == 0) return false;
    double norm = 0d;
    for (float value : feature) {
      if (!Float.isFinite(value)) return false;
      norm += (double) value * value;
    }
    return norm > 0d;
  }

  private static float maxSimilarity(float[] feature, List<float[]> gallery) {
    float score = 0f;
    for (float[] reference : gallery) score = Math.max(score, cosine(feature, reference));
    return score;
  }

  private static float cosine(float[] left, float[] right) {
    if (left.length != right.length) return -1f;
    double dot = 0d;
    double leftNorm = 0d;
    double rightNorm = 0d;
    for (int i = 0; i < left.length; i++) {
      dot += (double) left[i] * right[i];
      leftNorm += (double) left[i] * left[i];
      rightNorm += (double) right[i] * right[i];
    }
    return (float) Math.max(-1d, Math.min(1d, dot / Math.sqrt(leftNorm * rightNorm)));
  }
}
