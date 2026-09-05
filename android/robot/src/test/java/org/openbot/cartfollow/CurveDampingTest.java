package org.openbot.cartfollow;
import static org.junit.Assert.*;
import org.junit.Test;

public class CurveDampingTest {
  @Test public void smallOffsetsSteerSymmetricallyAtEveryGear() {
    for (int gear : new int[] {14, 18, 21}) {
      assertEquals(gear - 1, RealCartAutoDriveController.innerSpeedForError(gear, .15f, 0f, 100));
      assertEquals(gear - 1, RealCartAutoDriveController.innerSpeedForError(gear, -.15f, 0f, 100));
      assertEquals(gear - 4, RealCartAutoDriveController.innerSpeedForError(gear, .85f, 0f, 200));
    }
  }
  @Test public void returningTargetCanCancelSteeringBeforeCrossing() {
    assertEquals(21, RealCartAutoDriveController.innerSpeedForError(21, .108f, -.5f, 135));
    assertEquals(21, RealCartAutoDriveController.innerSpeedForError(21, -.108f, .5f, 135));
    assertEquals(0f, FollowTuning.dampedError(.108f, -.5f), .00001f);
    assertEquals(.108f, FollowTuning.dampedError(.108f, .5f), .00001f);
  }
  @Test public void recordedOscillationOutputsAreBoundedAndReturnIsDamped() {
    float[] errors = {-.0833f, .1048f, .2637f, .3366f, .3586f, .3113f, .265f, .108f, -.2876f, -.6811f};
    float[] rates = {-.0743f, .2486f, .4359f, .481f, .3193f, .1265f, .032f, -.284f, -.7935f, -1.179f};
    int[] gears = {18, 18, 18, 21, 18, 18, 21, 21, 21, 14};
    int[] expectedInner = {17, 17, 17, 20, 16, 17, 20, 20, 20, 11};
    for (int i = 0; i < errors.length; i++) {
      int inner = RealCartAutoDriveController.innerSpeedForError(gears[i], errors[i], rates[i], 135);
      assertEquals("sample " + i, expectedInner[i], inner);
      assertTrue(inner > 0 && gears[i] - inner <= 4);
      assertEquals(inner, RealCartAutoDriveController.innerSpeedForError(gears[i], -errors[i], -rates[i], 135));
    }
    assertTrue(FollowTuning.dampedError(.108f, -.284f) < .04f);
  }
  @Test public void strengthIsCappedWithoutOverwritingSetting() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    controller.setSteeringStrengthPercent(135);
    assertEquals(135, controller.getSteeringStrengthPercent());
    assertEquals(100, FollowTuning.effectiveStrength(controller.getSteeringStrengthPercent()));
    assertEquals(20, FollowTuning.effectiveStrength(20));
  }
  @Test public void legacyTargetsIncludeNonlinearRangeAndPivotLimits() {
    assertEquals(0, LegacyWheelSpeedMapping.targetMmps(0, 0));
    assertEquals(240, LegacyWheelSpeedMapping.targetMmps(14, 14));
    assertEquals(446, LegacyWheelSpeedMapping.targetMmps(18, 15));
    assertEquals(600, LegacyWheelSpeedMapping.targetMmps(21, 13));
    assertEquals(223, LegacyWheelSpeedMapping.targetMmps(13, 21));
    assertEquals(-86, LegacyWheelSpeedMapping.targetMmps(-5, 5));
    assertEquals(80, LegacyWheelSpeedMapping.targetMmps(1, -1));
    assertEquals(-240, LegacyWheelSpeedMapping.targetMmps(-21, 21));
  }
}
