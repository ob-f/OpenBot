package org.openbot.cartfollow.diagnostics;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openbot.cartfollow.FrameTimingEvidence;

public class FrameTimingCsvTest {
  @Test
  public void continuityFieldsAppendAfterExistingTimingColumns() {
    String values = CartFollowDiagnosticSaver.continuityColumns(null, null);
    assertEquals(12, values.split(",", -1).length);
    assertTrue(values.contains(",0,0,0,0,"));
  }

  @Test
  public void appendedTimingColumnsKeepMillisecondAndSensorClocksSeparateAndQuoteText() {
    FrameTimingEvidence timing =
        new FrameTimingEvidence(100L, 998877L, 15L, 30L, 50L, 70L, 20f, 3L);
    String columns =
        CartFollowDiagnosticSaver.timingColumns(timing, null, "height, \"partial\"\nrow2");
    assertTrue(columns.startsWith(",100,998877,15,30,50,70,3,\"unavailable\",-1,"));
    assertTrue(columns.endsWith("\"height, \"\"partial\"\"\nrow2\",0,0,0,0"));
  }
}
