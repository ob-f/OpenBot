package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openbot.tflite.Detector;
import org.openbot.vehicle.Control;

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk = 28)
public class SimulatorAutoDriveControllerTest {
  @Test
  public void continuityOnlyContinuesExistingMovementAtLowGearAndNeverRestartsAfterStop() {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int i = 1; i <= 3; i++) SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .99f);
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    FollowStateMachine.FrameResult current = frame(.5f, 60, SteeringEvidence.Direction.RIGHT);
    assertTrue(controller.update(current, 900).left > 0);
    current.simulatorIdentity = SimulatorIdentityGuardTest.continuous(guard, 4, 1200, .82f);
    current.frameSequence = 4;
    current.sessionGeneration = 1;
    current.frameTiming = new FrameTimingEvidence(1200, 0, 0, 0, 0, 0, 0, 0);
    SimulatorAutoDriveController.Result moving = controller.update(current, 1200);
    assertEquals(14, moving.gear);
    assertTrue(moving.right >= 10);
    assertEquals(0, controller.update(current, 2200).left);
    SimulatorAutoDriveController stopped = new SimulatorAutoDriveController();
    assertTrue(
        stopped.update(current, 1200).left > 0); // Three measured stable frames permit restart.
    current.frameTiming = new FrameTimingEvidence(1200, 0, 0, 0, 501, 501, 0, 0);
    assertEquals(0, controller.update(current, 1701).left);
    current.frameTiming = null;
    assertEquals(0, controller.update(current, 1750).left);
  }

  @Test
  public void staleFrameCannotGenerateForwardOrSearchCommands() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    FollowStateMachine.FrameResult frame = frame(.5f, 0, SteeringEvidence.Direction.NONE);
    frame.frameTiming = new FrameTimingEvidence(0, 0, 0, 0, 501, 501, 0, 0);
    assertEquals("frame_stale", controller.update(frame, 1000).reason);
    frame.directedReacquireEvidence =
        new DirectedReacquireEvidence(
            DirectedReacquireEvidence.Phase.TURNING,
            SteeringEvidence.Direction.LEFT,
            18,
            10,
            90,
            1000,
            10000,
            true,
            false,
            false,
            "search");
    assertEquals(0, controller.update(frame, 1000).left);
    assertEquals(0, controller.update(frame, 1000).right);
  }

  @Test
  public void directedBudgetOverridesOrdinaryTwoAndFiveSecondTimers() {
    for (long ordinary : new long[] {2000L, 5000L}) {
      SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
      controller.setRecoveryLimitMs(ordinary);
      controller.update(recovery(false), 0);
      for (DirectedReacquireEvidence.Phase phase :
          new DirectedReacquireEvidence.Phase[] {
            DirectedReacquireEvidence.Phase.TURNING,
            DirectedReacquireEvidence.Phase.VERIFYING,
            DirectedReacquireEvidence.Phase.SETTLING
          }) {
        FollowStateMachine.FrameResult frame = recovery(false);
        frame.directedReacquireEvidence =
            new DirectedReacquireEvidence(
                phase,
                SteeringEvidence.Direction.LEFT,
                18,
                10,
                90,
                7000,
                10000,
                true,
                false,
                false,
                "search");
        SimulatorAutoDriveController.Result result = controller.update(frame, 7000);
        assertFalse(result.lockout);
        assertEquals(10000, result.recoveryLimitMs);
        assertEquals(phase == DirectedReacquireEvidence.Phase.TURNING ? -18 : 0, result.left);
        assertEquals(phase == DirectedReacquireEvidence.Phase.TURNING ? 18 : 0, result.right);
      }
      FollowStateMachine.FrameResult frame = recovery(false);
      frame.directedReacquireEvidence =
          new DirectedReacquireEvidence(
              DirectedReacquireEvidence.Phase.PARKED_WAIT,
              SteeringEvidence.Direction.LEFT,
              18,
              10,
              90,
              10000,
              10000,
              true,
              false,
              false,
              "search_timeout");
      assertParked(controller.update(frame, 10000));
      assertParked(controller.update(frame, 60000));
    }
  }

  @Test
  public void distanceSelectsLowMidAndHighAfterStableUpshift() {
    SimulatorAutoDriveController low = new SimulatorAutoDriveController();
    assertEquals(14, low.update(frame(0.80f, 0, SteeringEvidence.Direction.NONE), 0).gear);

    SimulatorAutoDriveController mid = new SimulatorAutoDriveController();
    mid.update(frame(0.70f, 0, SteeringEvidence.Direction.NONE), 0);
    mid.update(frame(0.70f, 0, SteeringEvidence.Direction.NONE), 30);
    mid.update(frame(0.70f, 0, SteeringEvidence.Direction.NONE), 60);
    assertEquals(18, mid.update(frame(0.70f, 0, SteeringEvidence.Direction.NONE), 90).gear);

    SimulatorAutoDriveController high = new SimulatorAutoDriveController();
    high.update(frame(0.50f, 0, SteeringEvidence.Direction.NONE), 0);
    high.update(frame(0.50f, 0, SteeringEvidence.Direction.NONE), 30);
    high.update(frame(0.50f, 0, SteeringEvidence.Direction.NONE), 60);
    assertEquals(21, high.update(frame(0.50f, 0, SteeringEvidence.Direction.NONE), 90).gear);
  }

  @Test
  public void steeringCapsGearAndProducesMirroredDifferential() {
    SimulatorAutoDriveController leftController = new SimulatorAutoDriveController();
    SimulatorAutoDriveController.Result left =
        leftController.update(frame(0.50f, 80, SteeringEvidence.Direction.LEFT), 0);
    assertEquals(14, left.gear);
    assertEquals(11, left.left);
    assertEquals(14, left.right);

    SimulatorAutoDriveController rightController = new SimulatorAutoDriveController();
    SimulatorAutoDriveController.Result right =
        rightController.update(frame(0.50f, 80, SteeringEvidence.Direction.RIGHT), 0);
    assertEquals(left.left, right.right);
    assertEquals(left.right, right.left);
  }

  @Test
  public void visiblePersonDoesNotConsumeMissingTimeout() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    controller.setRecoveryLimitMs(2000);
    assertFalse(controller.update(recovery(true), 0).lockout);
    assertFalse(controller.update(recovery(true), 5000).lockout);
    assertFalse(controller.update(recovery(false), 6000).lockout);
    assertParked(controller.update(recovery(false), 8000));
  }

  @Test
  public void nonFarDistanceAlwaysStops() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    FollowStateMachine.FrameResult frame = frame(0.90f, 0, SteeringEvidence.Direction.NONE);
    frame.distanceEstimate =
        new ImageSetpointDistanceEstimator.DistanceEstimate(
            0.90f, 0.90f, 0f, DistanceState.OK, 1f, null);
    SimulatorAutoDriveController.Result result = controller.update(frame, 0);
    assertEquals(0, result.left);
    assertEquals("distance_ok", result.reason);
  }

  @Test
  public void nonFarTargetStillPivotsUntilCenteredForThreeFrames() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    FollowStateMachine.FrameResult frame = frame(0.90f, 40, SteeringEvidence.Direction.LEFT);
    frame.distanceEstimate =
        new ImageSetpointDistanceEstimator.DistanceEstimate(
            0.90f, 0.90f, 0f, DistanceState.OK, 1f, null);
    frame.steeringEvidence = steering(-.22f, SteeringEvidence.Direction.LEFT);
    SimulatorAutoDriveController.Result pivot = controller.update(frame, 0);
    assertEquals(SimulatorAutoDriveController.Phase.PIVOT, pivot.phase);
    assertEquals(-5, pivot.left);
    assertEquals(5, pivot.right);
    frame.steeringEvidence = steering(0.04f, SteeringEvidence.Direction.NONE);
    assertEquals(SimulatorAutoDriveController.Phase.PIVOT, controller.update(frame, 33).phase);
    assertEquals(SimulatorAutoDriveController.Phase.PIVOT, controller.update(frame, 66).phase);
    SimulatorAutoDriveController.Result centered = controller.update(frame, 99);
    assertEquals(0, centered.left);
    assertEquals("distance_ok", centered.reason);
  }

  private static SteeringEvidence steering(
      float predictedError, SteeringEvidence.Direction direction) {
    return new SteeringEvidence(
        true,
        "test",
        predictedError,
        predictedError,
        0f,
        predictedError,
        0f,
        Math.round(Math.abs(predictedError) * 100f),
        direction,
        direction == SteeringEvidence.Direction.NONE
            ? SteeringEvidence.Level.CENTER
            : SteeringEvidence.Level.MEDIUM,
        400);
  }

  private static FollowStateMachine.FrameResult frame(
      float heightScale, int demand, SteeringEvidence.Direction direction) {
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.FOLLOW,
            new Control(0, 0),
            null,
            null,
            new ArrayList<>(),
            true,
            false,
            null,
            -1);
    frame.distanceEstimate =
        new ImageSetpointDistanceEstimator.DistanceEstimate(
            heightScale, heightScale, 0f, DistanceState.TOO_FAR, 1f, null);
    frame.persons.add(new Detector.Recognition("1", "person", .95f, new RectF(10, 10, 50, 80), 0));
    frame.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.FOLLOW, BehaviorAction.FOLLOW_SLOW, "test", null, 1f);
    frame.steeringEvidence =
        new SteeringEvidence(
            true,
            "test",
            0,
            0,
            0,
            0,
            0,
            demand,
            direction,
            demand == 0 ? SteeringEvidence.Level.CENTER : SteeringEvidence.Level.LARGE,
            400);
    return frame;
  }

  @Test
  public void lowConfidencePersonPausesOrdinaryTimerAndLingeringFollowCannotDelayIt() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    controller.update(frame(.5f, 0, SteeringEvidence.Direction.NONE), 0);
    FollowStateMachine.FrameResult missing = frame(.5f, 0, SteeringEvidence.Direction.NONE);
    missing.persons.clear();
    assertEquals("target_not_visible", controller.update(missing, 100).reason);
    assertParked(controller.update(missing, 2100));
    FollowStateMachine.FrameResult low = recovery(false);
    java.util.List<Detector.Recognition> candidates = new ArrayList<>();
    candidates.add(new Detector.Recognition("low", "person", .2f, new RectF(10, 10, 50, 80), 0));
    low.detectionTierEvidence = new DetectionTierEvidence(.5f, .25f, candidates, candidates, true);
    assertFalse(controller.update(low, 4000).lockout);
    assertFalse(controller.update(low, 9000).lockout);
    assertEquals(0, controller.update(low, 9000).left);
  }

  @Test
  public void ordinaryMissingTimeoutParksIndefinitelyWithoutAutoEnding() {
    for (long timeout : new long[] {2000, 5000}) {
      SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
      controller.setRecoveryLimitMs(timeout);
      assertFalse(controller.update(recovery(false), 100).lockout);
      SimulatorAutoDriveController.Result beforeTimeout =
          controller.update(recovery(false), 100 + timeout - 1);
      assertEquals(SimulatorAutoDriveController.Phase.RECOVERY_STOP, beforeTimeout.phase);
      assertFalse(beforeTimeout.lockout);
      for (long now : new long[] {100 + timeout, 60000, 3600000}) {
        SimulatorAutoDriveController.Result parked = controller.update(recovery(false), now);
        assertParked(parked);
        assertEquals("target_missing_wait", parked.reason);
        assertEquals(now - 100, parked.recoveryElapsedMs);
      }
    }
  }

  @Test
  public void parkedWaitReturnsThroughVisibleRecoveryToNormalFollowAndResetsTimer() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    assertTrue(controller.update(frame(.5f, 0, SteeringEvidence.Direction.NONE), 0).left > 0);
    controller.update(recovery(false), 100);
    assertParked(controller.update(recovery(false), 2100));
    SimulatorAutoDriveController.Result visible = controller.update(recovery(true), 60000);
    assertEquals(SimulatorAutoDriveController.Phase.RECOVERY_STOP, visible.phase);
    assertEquals("person_visible_reacquire", visible.reason);
    assertFalse(visible.lockout);
    assertEquals(0, visible.left);
    assertEquals(0, visible.right);
    SimulatorAutoDriveController.Result following =
        controller.update(frame(.5f, 0, SteeringEvidence.Direction.NONE), 60300);
    assertEquals(SimulatorAutoDriveController.Phase.FOLLOW, following.phase);
    assertFalse(following.lockout);
    assertTrue(following.left > 0 && following.right > 0);
    SimulatorAutoDriveController.Result missingAgain = controller.update(recovery(false), 60600);
    assertEquals(SimulatorAutoDriveController.Phase.RECOVERY_STOP, missingAgain.phase);
    assertEquals(0, missingAgain.recoveryElapsedMs);
    assertParked(controller.update(recovery(false), 62600));
  }

  @Test
  public void directedParkedWaitCanCompleteAndReturnToNormalFollow() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    FollowStateMachine.FrameResult parked = recovery(false);
    parked.directedReacquireEvidence =
        new DirectedReacquireEvidence(
            DirectedReacquireEvidence.Phase.PARKED_WAIT,
            SteeringEvidence.Direction.LEFT,
            18,
            90,
            90,
            10000,
            10000,
            true,
            false,
            false,
            "search_timeout");
    assertParked(controller.update(parked, 10000));
    assertParked(controller.update(parked, 3600000));
    FollowStateMachine.FrameResult recovered = frame(.5f, 0, SteeringEvidence.Direction.NONE);
    recovered.directedReacquireEvidence =
        new DirectedReacquireEvidence(
            DirectedReacquireEvidence.Phase.COMPLETE,
            SteeringEvidence.Direction.LEFT,
            18,
            90,
            90,
            10000,
            10000,
            true,
            false,
            false,
            "target_recovered");
    SimulatorAutoDriveController.Result result = controller.update(recovered, 3600300);
    assertEquals(SimulatorAutoDriveController.Phase.FOLLOW, result.phase);
    assertFalse(result.lockout);
    assertTrue(result.left > 0 && result.right > 0);
  }

  @Test
  public void explicitDirectedSafetyFailureStillEndsWithLockout() {
    SimulatorAutoDriveController controller = new SimulatorAutoDriveController();
    FollowStateMachine.FrameResult failed = recovery(false);
    failed.directedReacquireEvidence =
        new DirectedReacquireEvidence(
            DirectedReacquireEvidence.Phase.FAILED,
            SteeringEvidence.Direction.LEFT,
            18,
            10,
            90,
            1000,
            10000,
            true,
            true,
            true,
            "wrong_direction");
    SimulatorAutoDriveController.Result result = controller.update(failed, 1000);
    assertEquals(SimulatorAutoDriveController.Phase.ENDED, result.phase);
    assertTrue(result.lockout);
    assertEquals(0, result.left);
    assertEquals(0, result.right);
  }

  private static void assertParked(SimulatorAutoDriveController.Result result) {
    assertEquals(SimulatorAutoDriveController.Phase.PARKED_WAIT, result.phase);
    assertFalse(result.lockout);
    assertEquals(0, result.gear);
    assertEquals(0, result.left);
    assertEquals(0, result.right);
  }

  private static FollowStateMachine.FrameResult recovery(boolean visible) {
    List<Detector.Recognition> persons = new ArrayList<>();
    if (visible) {
      persons.add(new Detector.Recognition("1", "person", 0.9f, new RectF(1, 1, 10, 20), 0));
    }
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            FollowState.IDENTITY_UNCERTAIN,
            new Control(0, 0),
            null,
            null,
            persons,
            false,
            false,
            null,
            -1);
    frame.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.IDENTITY_UNCERTAIN,
            BehaviorAction.MOTION_STOP,
            "identity_uncertain",
            null,
            0f);
    return frame;
  }
}
