package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.Color;
import android.os.SystemClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class RealCartIdentityIntegrationTest {
  @Test
  public void actualReidTrackAndStateMachineRecoverThroughRealOutputGate() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow(true)) {
      flow.startFollowing();
      RealCartSafetyController s = RealCartMigrationTest.ready();
      s.setSessionGeneration(1);
      for (int i = 0; i < 3; i++)
        send(s, flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.movingTarget()));
      assertFalse(s.getAutoDriveResult().isStop());
      for (int i = 0; i < 24; i++) assertTrue(send(s, flow.step(300)).isStop());
      assertTrue(s.isAutoUnlocked());
      assertEquals(8, flow.coordinator.getGalleryStatus().anchorSize);
      for (int i = 1; i <= 5; i++) {
        SimulatorAutomaticRecoveryIntegrationTest.Step step =
            flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.remote("returned"));
        assertEquals(i, step.permit.freshMatches);
        assertEquals(i < 5, send(s, step).isStop());
        assertNotEquals(FollowState.READY_TO_FOLLOW, step.frame.state);
      }
      assertFalse(
          send(s, flow.step(100, SimulatorAutomaticRecoveryIntegrationTest.remote("returned")))
              .isStop());
      assertFalse(
          send(s, flow.step(100, SimulatorAutomaticRecoveryIntegrationTest.remote("returned")))
              .isStop());
      flow.assertBaselineUnchanged();
    }
  }

  @Test
  public void repeatedReliableCropsPopulateRecentWithoutForcingAdaptiveDiversity() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow(true)) {
      flow.startFollowing();
      RealCartSafetyController s = RealCartMigrationTest.ready();
      s.setSessionGeneration(1);
      for (int i = 0; i < 8; i++)
        send(s, flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.movingTarget()));
      assertTrue(flow.coordinator.getRecentStatus(SystemClock.elapsedRealtime()).size >= 3);
      assertEquals(8, flow.coordinator.getGalleryStatus().anchorSize);
      assertEquals(0, flow.coordinator.getGalleryStatus().adaptiveSize);
    }
  }

  @Test
  public void distractorScoresCannotDriveRealCart() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow()) {
      flow.startFollowing();
      RealCartSafetyController s = RealCartMigrationTest.ready();
      s.setSessionGeneration(1);
      flow.colors.put("other", Color.BLUE);
      for (int i = 0; i < 3; i++)
        send(
            s,
            flow.step(
                300,
                SimulatorAutomaticRecoveryIntegrationTest.movingTarget(),
                SimulatorAutomaticRecoveryIntegrationTest.remote("other")));
      flow.colors.put("other", Color.RED);
      for (int i = 0; i < 20; i++) {
        SimulatorAutomaticRecoveryIntegrationTest.Step step =
            flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.remote("other"));
        assertFalse(step.permit.authorized);
        assertTrue(send(s, step).isStop());
      }
    }
  }

  @Test
  public void hardwarePolicyRemovesOnlyWeakMotionNotLearningContext() {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int i = 1; i <= 3; i++) SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .99f);
    SimulatorIdentityGuard.Decision weak =
        SimulatorIdentityGuardTest.continuous(guard, 4, 1200, .75f);
    assertTrue(weak.motionAllowed);
    SimulatorIdentityGuard.Decision real = weak.withoutContinuityMotion();
    assertTrue(real.motionAllowed);
    assertEquals(weak.retainTarget, real.retainTarget);
    assertEquals(weak.samplingAllowed, real.samplingAllowed);
    assertEquals(weak.holdDeadlineMs, real.holdDeadlineMs);
    RealCartSafetyController s = RealCartMigrationTest.movingHigh();
    FollowStateMachine.FrameResult f = RealCartMigrationTest.frame(7, 350, .5f);
    f.simulatorIdentity = real;
    assertTrue(s.auto(f, 350).isStop());
    assertTrue(s.isAutoUnlocked());
  }

  private static RealCartSafetyController.Output send(
      RealCartSafetyController s, SimulatorAutomaticRecoveryIntegrationTest.Step step) {
    step.frame.simulatorIdentity = step.permit.withoutContinuityMotion();
    return s.auto(step.frame, SystemClock.elapsedRealtime());
  }
}
