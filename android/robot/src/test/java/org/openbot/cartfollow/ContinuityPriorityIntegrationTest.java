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
public class ContinuityPriorityIntegrationTest {
  private Recognition target(float confidence, float width, float height) {
    return new Recognition(
        "target",
        "person",
        confidence,
        new RectF(80 - width / 2, 200 - height / 2, 80 + width / 2, 200 + height / 2),
        0);
  }

  @Test
  public void thirtyFpsMeasuredTrackingSurvivesLowAndCachedReidWithNormalGears() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow()) {
      flow.startFollowing();
      RealCartSafetyController real = RealCartMigrationTest.ready();
      real.setSessionGeneration(1);
      flow.targetSimilarity = .1f;
      long lastObservation = -1, lastSource = -1;
      int highGear = 0, cached = 0;
      for (int n = 0; n < 360; n++) {
        float height = 180 - n * .17f;
        Recognition measured = target(.95f, height / 3 * (n % 90 < 45 ? 1.3f : .7f), height);
        RectF centered = measured.getLocation();
        centered.offset(120, 0);
        measured.setLocation(centered);
        SimulatorAutomaticRecoveryIntegrationTest.Step step = flow.step(33, measured);
        assertTrue("frame " + n + ": " + step.permit.reason, step.permit.motionAllowed);
        assertTrue(
            "frame="
                + n
                + " reason="
                + step.drive.reason
                + " state="
                + step.frame.state
                + " distance="
                + step.frame.distanceEstimate.state
                + " height="
                + step.frame.distanceEstimate.heightScale,
            step.drive.left > 0 && step.drive.right > 0);
        assertNotEquals(FollowState.READY_TO_FOLLOW, step.frame.state);
        assertNotNull(step.permit.tracking);
        assertEquals(SystemClock.elapsedRealtime(), step.permit.tracking.observedAtMs);
        ReIDMatchResult match = step.identity.reidMatch;
        if (match != null && !match.fresh && lastObservation == match.observationId) {
          assertEquals(lastSource, match.observationTimeMs);
          cached++;
        }
        if (match != null) {
          lastObservation = match.observationId;
          lastSource = match.observationTimeMs;
        }
        RealCartSafetyController.Output output =
            real.auto(step.frame, SystemClock.elapsedRealtime());
        assertFalse(output.reason, output.isStop());
        if (step.drive.gear == 21 && real.getAutoDriveResult().gear == 21) highGear++;
      }
      assertTrue(cached > 200);
      assertTrue(highGear > 30);
    }
  }

  @Test
  public void lowDetectionKeepsMeasuredTargetButCapsBothControllersAndFreezesGallery() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow(true)) {
      flow.startFollowing();
      RealCartSafetyController real = RealCartMigrationTest.ready();
      real.setSessionGeneration(1);
      long revision = flow.coordinator.getGalleryStatus().revision;
      int reachedMid = 0;
      for (int n = 0; n < 30; n++) {
        SimulatorAutomaticRecoveryIntegrationTest.Step step = flow.step(33, target(.30f, 35, 110));
        assertTrue(step.permit.motionAllowed);
        assertFalse(step.permit.samplingAllowed);
        assertEquals(18, step.permit.tracking.maximumGear);
        assertFalse(real.auto(step.frame, SystemClock.elapsedRealtime()).isStop());
        assertTrue(step.drive.gear <= 18 && real.getAutoDriveResult().gear <= 18);
        if (step.drive.gear == 18 && real.getAutoDriveResult().gear == 18) reachedMid++;
      }
      assertTrue(reachedMid > 5);
      assertEquals(revision, flow.coordinator.getGalleryStatus().revision);
    }
  }

  @Test
  public void shortLossStopsImmediatelyAndRestartsOnThirdMeasuredFrameWithoutReid() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow()) {
      flow.startFollowing();
      flow.targetSimilarity = .1f;
      RealCartSafetyController real = RealCartMigrationTest.ready();
      real.setSessionGeneration(1);
      real.auto(flow.step(33, target(.95f, 50, 160)).frame, SystemClock.elapsedRealtime());
      SimulatorAutomaticRecoveryIntegrationTest.Step missing = flow.step(33);
      assertEquals(0, missing.drive.left);
      assertTrue(real.auto(missing.frame, SystemClock.elapsedRealtime()).isStop());
      for (int n = 1; n <= 3; n++) {
        SimulatorAutomaticRecoveryIntegrationTest.Step step = flow.step(33, target(.95f, 50, 160));
        assertEquals(n == 3, step.permit.motionAllowed);
        RealCartSafetyController.Output output =
            real.auto(step.frame, SystemClock.elapsedRealtime());
        assertEquals(n < 3, output.isStop());
        if (n == 3) {
          assertEquals(14, step.drive.gear);
          assertEquals(14, real.getAutoDriveResult().gear);
        }
        assertNotEquals(FollowState.READY_TO_FOLLOW, step.frame.state);
      }
    }
  }

  @Test
  public void lowAnchorNewPoseIsApprovedThenActuallyRecapturedThroughGlobalPipeline() {
    try (SimulatorAutomaticRecoveryIntegrationTest.Flow flow =
        new SimulatorAutomaticRecoveryIntegrationTest.Flow(true)) {
      flow.startFollowing();
      flow.targetSimilarity = .2f;
      for (int n = 0; n < 18; n++)
        assertTrue(
            flow.step(200, SimulatorAutomaticRecoveryIntegrationTest.movingTarget())
                .permit
                .motionAllowed);
      assertTrue(flow.coordinator.getGalleryStatus().adaptiveSize > 0);
      assertTrue(
          flow.coordinator.provenanceManifest().stream()
              .anyMatch(s -> s.contains("\"globalEligible\":true")));
      flow.parkAfterLongLoss();
      RealCartSafetyController real = RealCartMigrationTest.ready();
      real.setSessionGeneration(1);
      for (int n = 1; n <= 5; n++) {
        SimulatorAutomaticRecoveryIntegrationTest.Step step =
            flow.step(300, SimulatorAutomaticRecoveryIntegrationTest.remote("returned"));
        assertTrue(step.globalScore.anchorScore < .7f);
        assertTrue(step.globalScore.adaptiveScore > .99f);
        assertEquals(n == 5, step.permit.authorized);
        assertEquals(n < 5, real.auto(step.frame, SystemClock.elapsedRealtime()).isStop());
        if (n == 5) assertTrue(step.drive.left > 0 && step.drive.right > 0);
      }
    }
  }
}
