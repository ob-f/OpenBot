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

public class RealCartAutoDriveControllerTest {
  @Test
  public void stoppedCartRequiresThreeStableFarFrames() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    assertTrue(controller.update(followFrame(SteeringEvidence.Direction.NONE, 0, 0.79f), 0L).isStop());
    assertTrue(controller.update(followFrame(SteeringEvidence.Direction.NONE, 0, 0.79f), 33L).isStop());

    RealCartAutoDriveController.Result result =
        controller.update(followFrame(SteeringEvidence.Direction.NONE, 0, 0.79f), 66L);
    assertEquals(14, result.left);
    assertEquals(14, result.right);
    assertEquals(RealCartAutoDriveController.Phase.MOVING_STRAIGHT, result.phase);
  }

  @Test
  public void simulatorDirectionMapsToMatchingPhysicalCurve() {
    RealCartAutoDriveController.Result left =
        movingController()
            .update(followFrame(SteeringEvidence.Direction.LEFT, 75, 0.75f), 100L);
    assertEquals(11, left.left);
    assertEquals(14, left.right);
    assertEquals(RealCartAutoDriveController.Phase.CURVE_LEFT, left.phase);
    assertTrue(RealCartFollowFragment.commandForAutoResult(left).contains("向左"));

    RealCartAutoDriveController.Result right =
        movingController()
            .update(followFrame(SteeringEvidence.Direction.RIGHT, 75, 0.75f), 100L);
    assertEquals(14, right.left);
    assertEquals(11, right.right);
    assertEquals(RealCartAutoDriveController.Phase.CURVE_RIGHT, right.phase);
    assertTrue(RealCartFollowFragment.commandForAutoResult(right).contains("向右"));
  }

  @Test
  public void demandProducesMonotonicForwardOnlyInnerSpeed() {
    assertEquals(14, RealCartAutoDriveController.innerSpeedForDemand(0));
    assertEquals(13, RealCartAutoDriveController.innerSpeedForDemand(25));
    assertEquals(12, RealCartAutoDriveController.innerSpeedForDemand(50));
    assertEquals(11, RealCartAutoDriveController.innerSpeedForDemand(75));
    assertEquals(10, RealCartAutoDriveController.innerSpeedForDemand(100));
  }

  @Test
  public void missingSteeringEvidenceStopsInsteadOfGuessingControlSign() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    FollowStateMachine.FrameResult frame = rawFollowFrame(0.75f);
    assertTrue(controller.update(frame, 0L).isStop());
    assertEquals("steering_unavailable", controller.getLastResult().reason);
  }

  @Test
  public void distanceAndSearchAlwaysStop() {
    RealCartAutoDriveController controller = movingController();
    assertTrue(
        controller
            .update(frame(SteeringEvidence.Direction.NONE, 0, 0.90f, FollowState.FOLLOW, BehaviorAction.FOLLOW_CAUTION), 100L)
            .isStop());
    assertTrue(
        controller
            .update(frame(SteeringEvidence.Direction.LEFT, 80, 0.75f, FollowState.SEARCH, BehaviorAction.LOCAL_SEARCH_LEFT), 200L)
            .isStop());
  }

  @Test
  public void recoveryLocksOutAfterTwoSeconds() {
    RealCartAutoDriveController controller = movingController();
    RealCartAutoDriveController.Result first =
        controller.update(recoveryFrame(true), 1000L);
    assertTrue(first.isStop());
    assertFalse(first.lockout);

    controller.update(recoveryFrame(false), 1000L);
    RealCartAutoDriveController.Result timedOut =
        controller.update(recoveryFrame(false), 1000L + RealCartAutoDriveController.RECOVERY_LIMIT_MS);
    assertTrue(timedOut.isStop());
    assertTrue(timedOut.lockout);
    assertEquals(RealCartAutoDriveController.Phase.LOCKED, timedOut.phase);
  }

  private static RealCartAutoDriveController movingController() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    controller.update(followFrame(SteeringEvidence.Direction.NONE, 0, 0.75f), 0L);
    controller.update(followFrame(SteeringEvidence.Direction.NONE, 0, 0.75f), 30L);
    controller.update(followFrame(SteeringEvidence.Direction.NONE, 0, 0.75f), 60L);
    return controller;
  }

  private static FollowStateMachine.FrameResult followFrame(
      SteeringEvidence.Direction direction, int demand, float heightScale) {
    return frame(direction, demand, heightScale, FollowState.FOLLOW, BehaviorAction.FOLLOW_SLOW);
  }

  private static FollowStateMachine.FrameResult recoveryFrame(boolean personVisible) {
    return frame(
        SteeringEvidence.Direction.NONE,
        0,
        Float.NaN,
        FollowState.IDENTITY_UNCERTAIN,
        BehaviorAction.MOTION_STOP,
        personVisible);
  }

  private static FollowStateMachine.FrameResult frame(
      SteeringEvidence.Direction direction,
      int demand,
      float heightScale,
      FollowState state,
      BehaviorAction action) {
    return frame(direction, demand, heightScale, state, action, false);
  }

  private static FollowStateMachine.FrameResult frame(
      SteeringEvidence.Direction direction,
      int demand,
      float heightScale,
      FollowState state,
      BehaviorAction action,
      boolean personVisible) {
    FollowStateMachine.FrameResult frame = rawFrame(heightScale, state, action, personVisible);
    frame.steeringEvidence = evidence(direction, demand);
    return frame;
  }

  private static FollowStateMachine.FrameResult rawFollowFrame(float heightScale) {
    return rawFrame(heightScale, FollowState.FOLLOW, BehaviorAction.FOLLOW_SLOW, false);
  }

  private static FollowStateMachine.FrameResult rawFrame(
      float heightScale, FollowState state, BehaviorAction action, boolean personVisible) {
    List<Detector.Recognition> persons = new ArrayList<>();
    if (personVisible) {
      persons.add(new Detector.Recognition("1", "person", 0.9f, new RectF(1, 1, 10, 20), 0));
    }
    FollowStateMachine.FrameResult frame =
        new FollowStateMachine.FrameResult(
            state,
            new Control(0.6f, 0.6f),
            null,
            null,
            persons,
            true,
            false,
            null,
            -1);
    DistanceState distanceState =
        Float.isNaN(heightScale)
            ? DistanceState.UNKNOWN
            : heightScale < 0.85f ? DistanceState.TOO_FAR : DistanceState.OK;
    frame.distanceEstimate =
        new ImageSetpointDistanceEstimator.DistanceEstimate(
            heightScale, heightScale, 0f, distanceState, 1f, null);
    String reason = state == FollowState.IDENTITY_UNCERTAIN ? "identity_uncertain" : "test";
    frame.behaviorDecision = new BehaviorDecisionResult(state, action, reason, null, 1f);
    return frame;
  }

  private static SteeringEvidence evidence(SteeringEvidence.Direction direction, int demand) {
    return new SteeringEvidence(
        true,
        "test",
        0f,
        0f,
        0f,
        0f,
        0f,
        demand,
        direction,
        demand == 0 ? SteeringEvidence.Level.CENTER : SteeringEvidence.Level.LARGE,
        400);
  }
}
