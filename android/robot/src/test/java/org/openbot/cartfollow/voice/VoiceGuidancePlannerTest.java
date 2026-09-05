package org.openbot.cartfollow.voice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openbot.cartfollow.FollowState;

public class VoiceGuidancePlannerTest {
  @Test
  public void speaksOnlyOnceWhenStateIsStable() {
    VoiceGuidancePlanner planner = new VoiceGuidancePlanner();
    assertEquals(VoicePrompts.CAPTURE, planner.onFrame(FollowState.CAPTURE_TARGET, "", 0).textRes);
    assertNull(planner.onFrame(FollowState.CAPTURE_TARGET, "", 100));
  }

  @Test
  public void clippedCalibrationProducesOneSpecificPrompt() {
    VoiceGuidancePlanner planner = new VoiceGuidancePlanner();
    planner.onFrame(FollowState.DISTANCE_CALIBRATION, "距离标定 0/15", 0);
    assertEquals(
        VoicePrompts.CALIBRATION_CLIPPED,
        planner.onFrame(FollowState.DISTANCE_CALIBRATION, "请后退并保持完整人物入镜", 100).textRes);
    assertNull(planner.onFrame(FollowState.DISTANCE_CALIBRATION, "请后退并保持完整人物入镜", 200));
  }

  @Test
  public void lossAndStopAreUrgent() {
    VoiceGuidancePlanner planner = new VoiceGuidancePlanner();
    VoiceGuidancePlanner.Prompt lost = planner.onFrame(FollowState.LOST, "", 0);
    assertEquals(VoicePrompts.LOST, lost.textRes);
    assertTrue(lost.urgent);
    VoiceGuidancePlanner.Prompt stopped = planner.onFrame(FollowState.STOP, "", 100);
    assertEquals(VoicePrompts.STOPPED, stopped.textRes);
    assertTrue(stopped.urgent);
  }
}
