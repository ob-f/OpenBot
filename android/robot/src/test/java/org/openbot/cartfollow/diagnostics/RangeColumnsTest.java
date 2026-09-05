package org.openbot.cartfollow.diagnostics;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openbot.vehicle.RangeTelemetrySnapshot;
public class RangeColumnsTest {
  @Test public void missingAndPresentRangesHaveSameColumnsAndRetainErrorTime() {
    String missing = CartFollowDiagnosticSaver.rangeColumns(null, false, "");
    RangeTelemetrySnapshot snapshot = RangeTelemetrySnapshot.unavailable().withFirmwareError("settling", 123);
    String actual = CartFollowDiagnosticSaver.rangeColumns(snapshot, false, "observation_only");
    assertEquals(8, missing.split(",", -1).length);
    assertEquals(8, actual.split(",", -1).length);
    assertTrue(actual.endsWith(",123"));
    assertEquals(123, snapshot.withReading(800, 999).firmwareErrorAtMs);
  }
}
