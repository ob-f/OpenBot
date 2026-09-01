package org.openbot.cartfollow;

import android.graphics.RectF;

/**
 * Estimates a smooth, short-horizon image-plane steering demand without commanding the vehicle.
 * A positive error means that the target is on the right side of the displayed image.
 */
public final class SteeringDemandEstimator {
  public static final float ALPHA = 0.55f;
  public static final float BETA = 0.10f;
  public static final long RESET_GAP_MS = 500L;
  public static final float EDGE_ZONE_RATIO = 0.15f;
  public static final float CENTER_DEAD_ZONE = 0.08f;

  private boolean initialized;
  private int activeTrackId = Integer.MIN_VALUE;
  private long lastTimestampMs = -1L;
  private float filteredError;
  private float lateralRatePerSec;
  private SteeringEvidence.Direction direction = SteeringEvidence.Direction.NONE;
  private SteeringEvidence.Level level = SteeringEvidence.Level.CENTER;

  public synchronized SteeringEvidence update(
      RectF bbox,
      int frameW,
      int frameH,
      int sensorOrientation,
      int trackId,
      long nowMs,
      int predictionHorizonMs) {
    int horizonMs = Math.max(0, Math.min(800, predictionHorizonMs));
    if (bbox == null || frameW <= 0 || frameH <= 0) {
      reset();
      return SteeringEvidence.unavailable("target_unavailable", horizonMs);
    }

    boolean rotated = sensorOrientation % 180 == 90;
    float imageWidth = rotated ? frameH : frameW;
    float center = rotated ? bbox.centerY() : bbox.centerX();
    if (imageWidth <= 0f || Float.isNaN(center) || Float.isInfinite(center)) {
      reset();
      return SteeringEvidence.unavailable("frame_invalid", horizonMs);
    }
    float rawError = clamp(2f * (center / imageWidth - 0.5f), -1f, 1f);
    boolean resetRequired =
        !initialized
            || activeTrackId != trackId
            || lastTimestampMs < 0L
            || nowMs <= lastTimestampMs
            || nowMs - lastTimestampMs > RESET_GAP_MS;
    if (resetRequired) {
      initialized = true;
      activeTrackId = trackId;
      filteredError = rawError;
      lateralRatePerSec = 0f;
    } else {
      float dtSec = Math.max(0.02f, Math.min(0.25f, (nowMs - lastTimestampMs) / 1000f));
      float predictedAtNow = filteredError + lateralRatePerSec * dtSec;
      float residual = rawError - predictedAtNow;
      filteredError = clamp(predictedAtNow + ALPHA * residual, -1f, 1f);
      lateralRatePerSec =
          clamp(lateralRatePerSec + BETA * residual / dtSec, -4f, 4f);
    }
    lastTimestampMs = nowMs;

    float predictedError =
        clamp(filteredError + lateralRatePerSec * horizonMs / 1000f, -1f, 1f);
    float edgeUrgency = edgeUrgency(bbox, imageWidth, rotated, predictedError);
    float centerDemand =
        Math.max(0f, (Math.abs(predictedError) - CENTER_DEAD_ZONE) / (1f - CENTER_DEAD_ZONE));
    int demandPercent = Math.round(100f * Math.max(centerDemand, edgeUrgency));
    updateDirection(predictedError);
    updateLevel(demandPercent);

    return new SteeringEvidence(
        true,
        resetRequired ? "filter_reset" : "ok",
        rawError,
        filteredError,
        lateralRatePerSec,
        predictedError,
        edgeUrgency,
        demandPercent,
        direction,
        level,
        horizonMs);
  }

  public synchronized void reset() {
    initialized = false;
    activeTrackId = Integer.MIN_VALUE;
    lastTimestampMs = -1L;
    filteredError = 0f;
    lateralRatePerSec = 0f;
    direction = SteeringEvidence.Direction.NONE;
    level = SteeringEvidence.Level.CENTER;
  }

  private static float edgeUrgency(
      RectF bbox, float imageWidth, boolean rotated, float predictedError) {
    if (Math.abs(predictedError) <= CENTER_DEAD_ZONE || imageWidth <= 0f) return 0f;
    float edgeDistance;
    if (predictedError < 0f) {
      edgeDistance = rotated ? bbox.top : bbox.left;
    } else {
      edgeDistance = imageWidth - (rotated ? bbox.bottom : bbox.right);
    }
    float edgeZone = imageWidth * EDGE_ZONE_RATIO;
    return clamp((edgeZone - edgeDistance) / edgeZone, 0f, 1f);
  }

  private void updateDirection(float predictedError) {
    if (Math.abs(predictedError) <= 0.05f) {
      direction = SteeringEvidence.Direction.NONE;
      return;
    }
    // Keep the simulator display aligned with ControlGenerator.FLIP_TURN=true.
    SteeringEvidence.Direction candidate =
        predictedError < 0f ? SteeringEvidence.Direction.RIGHT : SteeringEvidence.Direction.LEFT;
    if (direction != SteeringEvidence.Direction.NONE
        && direction != candidate
        && Math.abs(predictedError) < 0.10f) return;
    direction = candidate;
  }

  private void updateLevel(int demandPercent) {
    if (direction == SteeringEvidence.Direction.NONE) {
      level = SteeringEvidence.Level.CENTER;
      return;
    }
    for (int i = 0; i < 5; i++) {
      SteeringEvidence.Level previous = level;
      switch (level) {
        case CENTER:
          if (demandPercent >= 15) level = SteeringEvidence.Level.SLIGHT;
          break;
        case SLIGHT:
          if (demandPercent < 5) level = SteeringEvidence.Level.CENTER;
          else if (demandPercent >= 35) level = SteeringEvidence.Level.MEDIUM;
          break;
        case MEDIUM:
          if (demandPercent < 25) level = SteeringEvidence.Level.SLIGHT;
          else if (demandPercent >= 65) level = SteeringEvidence.Level.LARGE;
          break;
        case LARGE:
          if (demandPercent < 55) level = SteeringEvidence.Level.MEDIUM;
          else if (demandPercent >= 90) level = SteeringEvidence.Level.EDGE;
          break;
        case EDGE:
          if (demandPercent < 80) level = SteeringEvidence.Level.LARGE;
          break;
        default:
          level = SteeringEvidence.Level.CENTER;
          break;
      }
      if (level == previous) return;
    }
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }
}
