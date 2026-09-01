package org.openbot.cartfollow;

/** Continuous image-plane steering evidence for the Human Cart Simulator. */
public final class SteeringEvidence {
  public enum Direction {
    NONE,
    LEFT,
    RIGHT
  }

  public enum Level {
    CENTER,
    SLIGHT,
    MEDIUM,
    LARGE,
    EDGE
  }

  public final boolean valid;
  public final String reason;
  public final float rawError;
  public final float filteredError;
  public final float lateralRatePerSec;
  public final float predictedError;
  public final float edgeUrgency;
  public final int demandPercent;
  public final Direction direction;
  public final Level level;
  public final int predictionHorizonMs;

  public SteeringEvidence(
      boolean valid,
      String reason,
      float rawError,
      float filteredError,
      float lateralRatePerSec,
      float predictedError,
      float edgeUrgency,
      int demandPercent,
      Direction direction,
      Level level,
      int predictionHorizonMs) {
    this.valid = valid;
    this.reason = reason == null ? "" : reason;
    this.rawError = rawError;
    this.filteredError = filteredError;
    this.lateralRatePerSec = lateralRatePerSec;
    this.predictedError = predictedError;
    this.edgeUrgency = edgeUrgency;
    this.demandPercent = Math.max(0, Math.min(100, demandPercent));
    this.direction = direction == null ? Direction.NONE : direction;
    this.level = level == null ? Level.CENTER : level;
    this.predictionHorizonMs = Math.max(0, predictionHorizonMs);
  }

  public static SteeringEvidence unavailable(String reason, int predictionHorizonMs) {
    return new SteeringEvidence(
        false,
        reason,
        0f,
        0f,
        0f,
        0f,
        0f,
        0,
        Direction.NONE,
        Level.CENTER,
        predictionHorizonMs);
  }

  public String directionLabel() {
    switch (direction) {
      case LEFT:
        return "左";
      case RIGHT:
        return "右";
      case NONE:
      default:
        return "";
    }
  }

  public String levelLabel() {
    switch (level) {
      case SLIGHT:
        return "轻微";
      case MEDIUM:
        return "中等";
      case LARGE:
        return "大幅";
      case EDGE:
        return "接近边缘";
      case CENTER:
      default:
        return "居中";
    }
  }
}
