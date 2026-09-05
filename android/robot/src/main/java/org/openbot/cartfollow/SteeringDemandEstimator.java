package org.openbot.cartfollow;

import android.graphics.RectF;

/**
 * Estimates a smooth, short-horizon steering demand in displayed-image coordinates. A positive
 * error always means that the target is on the right side of the phone screen.
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

    DisplayHorizontalBounds displayed =
        displayedHorizontalBounds(bbox, frameW, frameH, sensorOrientation);
    if (displayed.width <= 0f
        || Float.isNaN(displayed.center)
        || Float.isInfinite(displayed.center)) {
      reset();
      return SteeringEvidence.unavailable("frame_invalid", horizonMs);
    }
    float rawError = clamp(2f * (displayed.center / displayed.width - 0.5f), -1f, 1f);
    boolean resetRequired =
        !initialized
            || activeTrackId != trackId
            || lastTimestampMs < 0L
            || nowMs <= lastTimestampMs
            || nowMs - lastTimestampMs > RESET_GAP_MS;
    if (resetRequired) {
      direction = SteeringEvidence.Direction.NONE;
      initialized = true;
      activeTrackId = trackId;
      filteredError = rawError;
      lateralRatePerSec = 0f;
    } else {
      float dtSec = Math.max(0.02f, Math.min(0.25f, (nowMs - lastTimestampMs) / 1000f));
      float predictedAtNow = filteredError + lateralRatePerSec * dtSec;
      float residual = rawError - predictedAtNow;
      filteredError = clamp(predictedAtNow + ALPHA * residual, -1f, 1f);
      lateralRatePerSec = clamp(lateralRatePerSec + BETA * residual / dtSec, -4f, 4f);
    }
    lastTimestampMs = nowMs;

    float predictedError =
        horizonMs == 0
            ? rawError
            : clamp(filteredError + lateralRatePerSec * horizonMs / 1000f, -1f, 1f);
    float edgeUrgency = edgeUrgency(displayed, predictedError);
    SteeringEvidence.Direction previousDirection = direction;
    updateDirection(rawError);
    float dampedError = FollowTuning.dampedError(rawError, lateralRatePerSec);
    int demandPercent = direction == SteeringEvidence.Direction.NONE
        || dampedError <= FollowTuning.CURVE_EXIT ? 0
        : Math.max(1, Math.round(100f * FollowTuning.curveDemand(dampedError)));
    updateLevel(demandPercent);

    return new SteeringEvidence(
        true,
        direction == SteeringEvidence.Direction.NONE ? "curve_centered"
            : dampedError <= FollowTuning.CURVE_EXIT ? "curve_return_brake"
            : previousDirection == direction ? "curve_active" : "curve_enter_or_change",
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

  private static float edgeUrgency(DisplayHorizontalBounds displayed, float predictedError) {
    if (Math.abs(predictedError) <= CENTER_DEAD_ZONE || displayed.width <= 0f) return 0f;
    float edgeDistance = predictedError < 0f ? displayed.left : displayed.width - displayed.right;
    float edgeZone = displayed.width * EDGE_ZONE_RATIO;
    return clamp((edgeZone - edgeDistance) / edgeZone, 0f, 1f);
  }

  private void updateDirection(float rawError) {
    float magnitude = Math.abs(rawError);
    if (magnitude <= FollowTuning.CURVE_EXIT) {
      direction = SteeringEvidence.Direction.NONE;
      return;
    }
    SteeringEvidence.Direction candidate = rawError < 0f
        ? SteeringEvidence.Direction.LEFT : SteeringEvidence.Direction.RIGHT;
    if (direction != candidate) {
      // Never preserve the old sign after a center crossing.
      direction = magnitude > FollowTuning.CURVE_ENTER ? candidate : SteeringEvidence.Direction.NONE;
    }
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

  /** Converts a source-frame bbox into the horizontal axis visible on the phone preview. */
  private static DisplayHorizontalBounds displayedHorizontalBounds(
      RectF bbox, int frameW, int frameH, int sensorOrientation) {
    switch (((sensorOrientation % 360) + 360) % 360) {
      case 90:
        return new DisplayHorizontalBounds(frameH - bbox.bottom, frameH - bbox.top, frameH);
      case 180:
        return new DisplayHorizontalBounds(frameW - bbox.right, frameW - bbox.left, frameW);
      case 270:
        return new DisplayHorizontalBounds(bbox.top, bbox.bottom, frameH);
      case 0:
      default:
        return new DisplayHorizontalBounds(bbox.left, bbox.right, frameW);
    }
  }

  private static final class DisplayHorizontalBounds {
    final float left;
    final float right;
    final float width;
    final float center;

    DisplayHorizontalBounds(float left, float right, float width) {
      this.left = left;
      this.right = right;
      this.width = width;
      this.center = (left + right) * 0.5f;
    }
  }
}
