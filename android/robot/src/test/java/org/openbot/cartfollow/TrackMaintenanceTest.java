package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import android.os.SystemClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector.Recognition;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class TrackMaintenanceTest {
  private SimulatorIdentityGuard started() {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int i = 1; i <= 3; i++) SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .92f);
    return guard;
  }

  private SimulatorIdentityGuard.Decision sample(
      SimulatorIdentityGuard guard,
      int frame,
      long time,
      float score,
      float anchor,
      float second,
      boolean fresh,
      long observation,
      long source,
      boolean competition) {
    ReIDMatchResult match =
        new ReIDMatchResult(score, second, 0, 8, true, 5, "test", anchor, 0, observation)
            .withBinding(1, source, fresh ? frame : observation, fresh, 0, false);
    return guard.update(
        1,
        frame,
        time,
        time,
        1,
        1,
        true,
        true,
        match,
        1,
        competition,
        false,
        match,
        new SimulatorContinuityTracker.Evidence(
            true,
            "continuous_observations",
            3,
            null,
            new android.graphics.RectF(100, 100, 180, 280)),
        true);
  }

  @Test
  public void moderateIndependentScoresRemainLocalForOverTenSeconds() {
    SimulatorIdentityGuard guard = started();
    for (int i = 4; i <= 45; i++) {
      float score = i < 8 ? .84f : i < 12 ? .82f : .81f;
      SimulatorIdentityGuard.Decision result =
          SimulatorIdentityGuardTest.continuous(guard, i, i * 300, score);
      assertEquals(SimulatorIdentityGuard.State.TRACK_STABLE, result.state);
      assertEquals(SimulatorIdentityGuard.RecoveryType.LOCAL, result.recoveryType);
      assertFalse(result.authorized);
      assertTrue(result.allowsForward(i * 300));
      assertEquals(900, result.identityEvidenceTimeMs);
      assertEquals(i * 300, result.tracking.observedAtMs);
    }
  }

  @Test
  public void thresholdsStopAndThreeFreshModerateThenStrongSamplesRestoreSeparatePermissions() {
    for (float[] values : new float[][] {{.799f, .9f, 0}, {.82f, .699f, 0}, {.82f, .8f, .75f}}) {
      SimulatorIdentityGuard guard = started();
      assertTrue(
          sample(guard, 4, 1200, values[0], values[1], values[2], true, 4, 1200, false)
              .motionAllowed);
      for (int i = 5; i <= 7; i++) {
        SimulatorIdentityGuard.Decision result =
            SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .82f);
        assertTrue(result.isContinuous());
        assertFalse(result.authorized);
      }
      for (int i = 8; i <= 10; i++)
        assertTrue(SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .92f).isContinuous());
    }
  }

  @Test
  public void cachedIdentityTimeStaysExactWhileNewMeasuredFramesCanMove() {
    SimulatorIdentityGuard guard = started();
    SimulatorIdentityGuard.Decision first =
        sample(guard, 4, 1200, .82f, .8f, 0, true, 4, 1200, false);
    MaintainedStartGate gate = new MaintainedStartGate();
    assertTrue(gate.ready(first, 1200));
    for (int i = 5; i <= 8; i++) {
      long time = 1200 + (i - 4) * 100;
      SimulatorIdentityGuard.Decision cached =
          sample(guard, i, time, .82f, .8f, 0, false, 4, 1200, false);
      assertTrue(cached.isContinuous());
      assertEquals(first.identityEvidenceTimeMs, cached.identityEvidenceTimeMs);
      assertEquals(first.identityObservationId, cached.identityObservationId);
      assertTrue(gate.ready(cached, time));
    }
    assertFalse(first.allowsForward(1701));
    assertTrue(sample(guard, 9, 1701, .82f, .8f, 0, false, 4, 1200, false).motionAllowed);
  }

  @Test
  public void competitionRevokesEligibilityRatherThanBorrowingTrackId() {
    SimulatorIdentityGuard guard = started();
    assertFalse(sample(guard, 4, 1200, .82f, .8f, 0, true, 4, 1200, true).motionAllowed);
    for (int i = 5; i < 12; i++)
      assertFalse(SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .82f).motionAllowed);
  }

  @Test
  public void observationHistoryUsesPreviousRealBoxAndRejectsOldSession() {
    SimulatorContinuityTracker tracker = new SimulatorContinuityTracker();
    RectF previous = null;
    for (int i = 1; i <= 45; i++) {
      float h = 240f * (float) Math.pow(.975, i);
      RectF box = new RectF(200 - h / 6, 200 - h / 2, 200 + h / 6, 200 + h / 2);
      SimulatorContinuityTracker.Evidence e =
          tracker.observe(2, 1, box, i, i * 300, i * 300, 400, 400, true, false);
      assertEquals(i >= 3, e.reliable);
      if (previous != null) {
        assertNotNull(e.observedGeometry);
        assertTrue(e.observedGeometry.bboxDefaultOk);
      }
      previous = box;
    }
    assertEquals(
        "obsolete_observation",
        tracker.observe(1, 1, previous, 100, 14000, 14000, 400, 400, true, false).reason);
    assertFalse(
        tracker.observe(2, 1, new RectF(0, 0, 20, 40), 46, 13800, 13800, 400, 400, true, false)
            .reliable);
  }

  @Test
  public void realDetectionReidGalleryAndBothControllersMaintainShrinkingTarget() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow(true)) {
      flow.startFollowing();
      RealCartSafetyController real = RealCartMigrationTest.ready();
      real.setSessionGeneration(1);
      for (int i = 0; i < 5; i++)
        real.auto(
            flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.movingTarget()).frame,
            SystemClock.elapsedRealtime());
      assertFalse(real.getAutoDriveResult().isStop());
      flow.targetSimilarity = .82f;
      for (int i = 0; i < 40; i++) {
        float h = 192f * (float) Math.pow(.98, i);
        Recognition r =
            new Recognition(
                "target",
                "person",
                .99f,
                new RectF(200 - h / 6, 200 - h / 2, 200 + h / 6, 200 + h / 2),
                0);
        SimulatorAutomaticRecoveryIntegrationTest.Step step = flow.step(300, r);
        assertTrue("frame " + i + ": " + step.permit.reason, step.permit.isContinuous());
        assertFalse(step.permit.authorized);
        assertEquals(SimulatorIdentityGuard.RecoveryType.LOCAL, step.permit.recoveryType);
        assertTrue(
            step.drive.left > 0
                && step.drive.left <= 21
                && step.drive.right > 0
                && step.drive.right <= 21);
        RealCartSafetyController.Output output =
            real.auto(step.frame, SystemClock.elapsedRealtime());
        assertTrue(
            "real " + i + ": " + output.reason,
            output.left > 0 && output.left <= 21 && output.right > 0 && output.right <= 21);
        flow.assertBaselineUnchanged();
      }
    }
  }

  @Test
  public void stoppedTargetRestartsOnThirdIndependentMaintenanceFrameWithoutDoubleWaiting() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow()) {
      flow.startFollowing();
      RealCartSafetyController real = RealCartMigrationTest.ready();
      real.setSessionGeneration(1);
      for (int i = 0; i < 4; i++)
        real.auto(
            flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.movingTarget()).frame,
            SystemClock.elapsedRealtime());
      flow.targetSimilarity = .79f;
      SimulatorAutomaticRecoveryIntegrationTest.Step weak =
          flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.movingTarget());
      assertTrue(weak.drive.left > 0);
      assertFalse(real.auto(weak.frame, SystemClock.elapsedRealtime()).isStop());
      flow.targetSimilarity = .82f;
      for (int i = 1; i <= 3; i++) {
        SimulatorAutomaticRecoveryIntegrationTest.Step step =
            flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.movingTarget());
        assertTrue(step.permit.motionAllowed);
        assertTrue(step.drive.left > 0);
        assertFalse(real.auto(step.frame, SystemClock.elapsedRealtime()).isStop());
        assertNotEquals(FollowState.READY_TO_FOLLOW, step.frame.state);
      }
    }
  }

  @Test
  public void resendUsesMeasuredFrameDeadlineAndCannotBorrowTrackingFromAnotherFrame() {
    SimulatorIdentityGuard guard = started();
    SimulatorIdentityGuard.Decision permit =
        SimulatorIdentityGuardTest.continuous(guard, 4, 1200, .82f);
    RealCartSafetyController real = RealCartMigrationTest.movingHigh();
    FollowStateMachine.FrameResult mismatch = RealCartMigrationTest.frame(7, 1400, .5f);
    mismatch.simulatorIdentity = permit;
    assertTrue(real.auto(mismatch, 1400).isStop());
    SimulatorIdentityGuard.Decision measured =
        SimulatorIdentityGuardTest.continuous(guard, 8, 1500, .82f);
    FollowStateMachine.FrameResult frame = RealCartMigrationTest.frame(8, 1500, .5f);
    frame.sessionGeneration = 1;
    real.setSessionGeneration(1);
    frame.simulatorIdentity = measured;
    assertFalse(real.auto(frame, 1500).isStop());
    assertFalse(real.refresh(1899, null).isStop());
    assertTrue(real.refresh(1901, null).isStop());
  }

  @Test
  public void sameIdSpatialJumpRequiresStrictGlobalRecovery() {
    SimulatorIdentityGuard guard = started();
    ReIDMatchResult match =
        new ReIDMatchResult(.92f, 0, 0, 8, true, 1, "test", .92f, 0, 4)
            .withBinding(1, 1200, 4, true, 0, false);
    SimulatorIdentityGuard.Decision result =
        guard.update(
            1,
            4,
            1200,
            1200,
            1,
            1,
            true,
            true,
            match,
            1,
            false,
            false,
            match,
            new SimulatorContinuityTracker.Evidence(false, "bbox_jump", 1),
            true);
    assertFalse(result.motionAllowed);
    assertEquals(SimulatorIdentityGuard.RecoveryType.GLOBAL, result.recoveryType);
    assertEquals(1, result.freshMatches);
  }
}
