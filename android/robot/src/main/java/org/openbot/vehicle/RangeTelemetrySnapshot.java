package org.openbot.vehicle;

/** Immutable view of the sensor-enabled CART_AT8236 V1 {@code s<cm>} telemetry. */
public final class RangeTelemetrySnapshot {
  public final boolean capabilityAdvertised;
  public final boolean hasReading;
  public final int minimumDistanceMm;
  public final long receivedAtMs;
  public final long sequence;
  public final String lastFirmwareError;
  public final long firmwareErrorAtMs;

  private RangeTelemetrySnapshot(
      boolean capabilityAdvertised,
      boolean hasReading,
      int minimumDistanceMm,
      long receivedAtMs,
      long sequence,
      String lastFirmwareError,
      long firmwareErrorAtMs) {
    this.capabilityAdvertised = capabilityAdvertised;
    this.hasReading = hasReading;
    this.minimumDistanceMm = minimumDistanceMm;
    this.receivedAtMs = receivedAtMs;
    this.sequence = sequence;
    this.lastFirmwareError = lastFirmwareError == null ? "" : lastFirmwareError;
    this.firmwareErrorAtMs = firmwareErrorAtMs;
  }

  public static RangeTelemetrySnapshot unavailable() {
    return new RangeTelemetrySnapshot(false, false, -1, -1L, 0L, "", -1L);
  }

  public RangeTelemetrySnapshot withCapability(boolean advertised) {
    return new RangeTelemetrySnapshot(advertised, false, -1, -1L, sequence, "", -1L);
  }

  public RangeTelemetrySnapshot withReading(int distanceMm, long nowMs) {
    return new RangeTelemetrySnapshot(
        capabilityAdvertised,
        true,
        distanceMm,
        nowMs,
        sequence + 1L,
        lastFirmwareError,
        firmwareErrorAtMs);
  }

  public RangeTelemetrySnapshot withFirmwareError(String error, long nowMs) {
    return new RangeTelemetrySnapshot(
        capabilityAdvertised, hasReading, minimumDistanceMm, receivedAtMs, sequence, error, nowMs);
  }

  public long ageMs(long nowMs) {
    if (!hasReading || receivedAtMs < 0L || nowMs < receivedAtMs) return Long.MAX_VALUE;
    return nowMs - receivedAtMs;
  }

  public boolean isFresh(long nowMs, long maximumAgeMs) {
    return capabilityAdvertised && hasReading && ageMs(nowMs) <= maximumAgeMs;
  }
}
