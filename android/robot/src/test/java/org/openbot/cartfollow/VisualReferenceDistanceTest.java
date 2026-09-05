package org.openbot.cartfollow;
import static org.junit.Assert.*;
import org.junit.Test;
public class VisualReferenceDistanceTest {
  @Test public void metricDistanceIsUnsetAndDoesNotBecomeVisualSetpoint() {
    TargetMemory memory = new TargetMemory();
    assertNull(memory.getDesiredFollowingDistanceMm());
    memory.setDesiredFollowingDistanceMm(800);
    assertNull(memory.getDistanceSetpoint());
    memory.resetDistanceCalibration();
    assertEquals(Integer.valueOf(800), memory.getDesiredFollowingDistanceMm());
    memory.clear();
    assertNull(memory.getDesiredFollowingDistanceMm());
  }
  @Test(expected = IllegalArgumentException.class)
  public void invalidMetricSetpointIsRejected() {
    new TargetMemory().setDesiredFollowingDistanceMm(0);
  }
}
