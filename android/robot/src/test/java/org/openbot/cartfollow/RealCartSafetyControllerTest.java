package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openbot.tflite.Detector;
import org.openbot.vehicle.Control;

public class RealCartSafetyControllerTest {
  @Test
  public void schedulerStopsAimPulseEvenWithoutNewInference() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult frame = frame(new Control(0, 0), BehaviorAction.FOLLOW_SLOW);
    frame.steeringEvidence =
        new SteeringEvidence(
            true,
            "test",
            .9f,
            .9f,
            0f,
            .9f,
            0f,
            80,
            SteeringEvidence.Direction.RIGHT,
            SteeringEvidence.Level.LARGE,
            0);
    assertFalse(observe(controller, frame, 1000L).isStop());
    assertFalse(controller.refresh(1399L, null).isStop());
    assertTrue(controller.refresh(1401L, null).isStop());

    assertFalse(controller.isAutoUnlocked());
    assertTrue(controller.refresh(1450L, null).isStop());
  }

  @Test
  public void manualRequiresForegroundConnectionAndHandshake() {
    RealCartSafetyController controller = new RealCartSafetyController();
    assertTrue(
        controller
            .manual(
                RealCartSafetyController.MANUAL_FORWARD, RealCartSafetyController.MANUAL_FORWARD)
            .isStop());

    controller.setForeground(true);
    controller.setConnection(true, true);
    RealCartSafetyController.Output output =
        controller.manual(
            RealCartSafetyController.MANUAL_FORWARD, RealCartSafetyController.MANUAL_FORWARD);
    assertEquals(14, output.left);
    assertEquals(14, output.right);
  }

  @Test
  public void realCartSpeedCapsUseLowSpeedBenchValues() {
    assertEquals(14, RealCartSafetyController.MANUAL_FORWARD);
    assertEquals(12, RealCartSafetyController.MANUAL_REVERSE);
    assertEquals(5, RealCartSafetyController.MANUAL_TURN);
    assertEquals(21, RealCartSafetyController.AUTO_MAX);
  }

  @Test
  public void modeChangeRevokesAutoUnlock() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setMode(RealCartSafetyController.Mode.MANUAL);
    assertFalse(controller.isAutoUnlocked());
  }

  @Test
  public void stationaryAutoSessionDoesNotTriggerInferenceWatchdog() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    assertNull(controller.watchdog(10_000L));

    controller.setAutoRunEnabled(true, 10_000L);
    assertNull(controller.watchdog(10_000L + RealCartSafetyController.INFERENCE_TIMEOUT_MS));
    assertNull(controller.watchdog(60_000L));
  }

  @Test
  public void autoOutputUsesBoundedRealCartCommands() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult frame =
        frame(new Control(0.6f, 0.6f), BehaviorAction.FOLLOW_SLOW);
    frame.distanceEstimate = distance(0.75f, DistanceState.TOO_FAR);

    observe(controller, frame, 1000L);
    observe(controller, frame, 1030L);
    RealCartSafetyController.Output output = observe(controller, frame, 1060L);
    assertEquals(14, output.left);
    assertEquals(14, output.right);
  }

  @Test
  public void staleInferenceStopsAndRevokesUnlock() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult movingFrame =
        frame(new Control(0.4f, 0.4f), BehaviorAction.FOLLOW_SLOW);
    observe(controller, movingFrame, 1000L);
    observe(controller, movingFrame, 1030L);
    observe(controller, movingFrame, 1060L);

    RealCartSafetyController.Output output =
        controller.watchdog(1060L + RealCartSafetyController.INFERENCE_TIMEOUT_MS + 1L);
    assertNotNull(output);
    assertTrue(output.isStop());
    assertFalse(controller.isAutoUnlocked());
  }

  @Test
  public void stoppedAutoOutputDisarmsInferenceWatchdog() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult movingFrame =
        frame(new Control(0.4f, 0.4f), BehaviorAction.FOLLOW_SLOW);
    observe(controller, movingFrame, 1000L);
    observe(controller, movingFrame, 1030L);
    observe(controller, movingFrame, 1060L);

    FollowStateMachine.FrameResult stoppedFrame =
        frame(new Control(0f, 0f), BehaviorAction.MOTION_STOP);
    observe(controller, stoppedFrame, 1090L);

    assertNull(controller.watchdog(10_000L));
    assertTrue(controller.isAutoUnlocked());
  }

  @Test
  public void unarmedSearchNeverMovesAndParksAfterTwoSeconds() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult frame =
        frame(new Control(0f, 0f), BehaviorAction.LOCAL_SEARCH_LEFT);
    assertTrue(observe(controller, frame, 1000L).isStop());

    RealCartSafetyController.Output output =
        observe(controller, frame, 1000L + RealCartAutoDriveController.RECOVERY_LIMIT_MS);
    assertTrue(output.isStop());
    assertTrue(controller.isAutoUnlocked());
  }

  @Test
  public void visiblePersonRecoveryStaysStoppedWithoutRevokingUnlock() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult frame =
        frame(new Control(0f, 0f), BehaviorAction.MOTION_STOP, true);
    frame.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.IDENTITY_UNCERTAIN,
            BehaviorAction.MOTION_STOP,
            "identity_uncertain",
            null,
            0f);

    assertTrue(observe(controller, frame, 1000L).isStop());
    assertTrue(observe(controller, frame, 11_000L).isStop());
    assertTrue(controller.isAutoUnlocked());
    assertNull(controller.watchdog(60_000L));
  }

  @Test
  public void emergencyLatchBlocksEveryMotionRequest() {
    RealCartSafetyController controller = new RealCartSafetyController();
    controller.setForeground(true);
    controller.setConnection(true, true);
    controller.latchEmergency();
    assertTrue(controller.manual(28, 28).isStop());
  }

  @Test
  public void manualMotionIsIndependentOfRangeObservationTiming() {
    RealCartSafetyController controller = new RealCartSafetyController();
    controller.setForeground(true);
    controller.setConnection(true, true);
    assertEquals(14, controller.manual(14, 14, 1000L).left);
    assertEquals(-12, controller.manual(-12, -12, 5000L).left);
    assertEquals(-5, controller.manual(-5, 5, 60_000L).left);
    assertEquals(-5, controller.manual(5, -5, 60_000L).right);
  }

  @Test
  public void automaticForwardIgnoresObservationOnlyRangeFields() {
    RealCartSafetyController controller = readyAutoController();
    assertTrue(controller.unlockAuto());
    controller.setAutoRunEnabled(true, 900L);
    FollowStateMachine.FrameResult frame =
        frame(new Control(0.6f, 0.6f), BehaviorAction.FOLLOW_SLOW);
    frame.rangeTelemetry =
        org.openbot.vehicle.RangeTelemetrySnapshot.unavailable()
            .withCapability(true)
            .withReading(150, 1000L);
    frame.rangeFresh = false;
    frame.rangeGateReason = "observation_only";
    observe(controller, frame, 1000L);
    observe(controller, frame, 1030L);
    RealCartSafetyController.Output output = observe(controller, frame, 1060L);
    assertFalse(output.isStop());
    frame.rangeTelemetry = org.openbot.vehicle.RangeTelemetrySnapshot.unavailable();
    assertFalse(observe(controller, frame, 1090L).isStop());
    frame.rangeTelemetry = frame.rangeTelemetry.withCapability(true).withReading(250, 1120L);
    assertFalse(observe(controller, frame, 1120L).isStop());
    frame.rangeTelemetry = frame.rangeTelemetry.withCapability(true).withReading(500, 1150L);
    assertFalse(observe(controller, frame, 1150L).isStop());
    assertTrue(controller.isAutoUnlocked());
  }

  private static RealCartSafetyController readyAutoController() {
    RealCartSafetyController controller = new RealCartSafetyController();
    controller.setForeground(true);
    controller.setConnection(true, true);
    controller.setMode(RealCartSafetyController.Mode.AUTO);
    return controller;
  }

  private static long sequence;

  private static RealCartSafetyController.Output observe(
      RealCartSafetyController controller, FollowStateMachine.FrameResult frame, long now) {
    frame.frameSequence = ++sequence;
    frame.frameTiming = new FrameTimingEvidence(now, 0, 0, 0, 0, 0, 30, 0);
    frame.simulatorIdentity = new SimulatorIdentityGuard.Decision(true, false, 1, 3, "verified");
    return controller.auto(frame, now);
  }

  private static FollowStateMachine.FrameResult frame(Control control, BehaviorAction action) {
    return frame(control, action, false);
  }

  private static FollowStateMachine.FrameResult frame(
      Control control, BehaviorAction action, boolean personVisible) {
    List<Detector.Recognition> persons = new ArrayList<>();
    if (personVisible) {
      persons.add(new Detector.Recognition("1", "person", 0.9f, new RectF(1, 1, 10, 20), 0));
    }
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.FOLLOW, control, null, null, persons, true, false, null, -1);
    frame.behaviorDecision =
        new BehaviorDecisionResult(FollowState.FOLLOW, action, "test", null, 1f);
    frame.distanceEstimate = distance(0.75f, DistanceState.TOO_FAR);
    frame.steeringEvidence =
        new SteeringEvidence(
            true,
            "test",
            0f,
            0f,
            0f,
            0f,
            0f,
            0,
            SteeringEvidence.Direction.NONE,
            SteeringEvidence.Level.CENTER,
            400);
    return frame;
  }

  private static ImageSetpointDistanceEstimator.DistanceEstimate distance(
      float heightScale, DistanceState state) {
    return new ImageSetpointDistanceEstimator.DistanceEstimate(
        heightScale, heightScale, 0f, state, 1f, null);
  }
}
