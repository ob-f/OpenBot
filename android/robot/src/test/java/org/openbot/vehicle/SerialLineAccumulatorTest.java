package org.openbot.vehicle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class SerialLineAccumulatorTest {
  @Test
  public void reassemblesFragmentedLine() {
    SerialLineAccumulator decoder = new SerialLineAccumulator();
    assertTrue(decoder.accept("fCART_AT").isEmpty());
    List<String> lines = decoder.accept("8236:s:\n");
    assertEquals(1, lines.size());
    assertEquals("fCART_AT8236:s:", lines.get(0));
  }

  @Test
  public void separatesCoalescedCrLfLines() {
    SerialLineAccumulator decoder = new SerialLineAccumulator();
    List<String> lines = decoder.accept("s80\r\ns79\n!ERR,sensor_center_unavailable\n");
    assertEquals(3, lines.size());
    assertEquals("s80", lines.get(0));
    assertEquals("s79", lines.get(1));
    assertEquals("!ERR,sensor_center_unavailable", lines.get(2));
  }

  @Test
  public void clearPreventsOldConnectionFragmentsFromLeaking() {
    SerialLineAccumulator decoder = new SerialLineAccumulator();
    decoder.accept("s8");
    decoder.clear();
    assertEquals("s90", decoder.accept("s90\n").get(0));
  }

  @Test
  public void dropsUnboundedLineWithoutBroadcastingIt() {
    SerialLineAccumulator decoder = new SerialLineAccumulator();
    StringBuilder oversized = new StringBuilder();
    for (int i = 0; i < 1100; i++) oversized.append('x');
    assertTrue(decoder.accept(oversized.toString()).isEmpty());
    assertEquals("r", decoder.accept("r\n").get(0));
  }
}
