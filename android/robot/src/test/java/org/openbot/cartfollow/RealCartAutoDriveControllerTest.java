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
    assertTrue(
        update(controller, followFrame(SteeringEvidence.Direction.NONE, 0, 0.79f), 0L).isStop());
    assertTrue(
        update(controller, followFrame(SteeringEvidence.Direction.NONE, 0, 0.79f), 33L).isStop());

    RealCartAutoDriveController.Result result =
        update(controller, followFrame(SteeringEvidence.Direction.NONE, 0, 0.79f), 66L);
    assertEquals(14, result.left);
    assertEquals(14, result.right);
    assertEquals(RealCartAutoDriveController.Phase.MOVING_STRAIGHT, result.phase);
  }

  @Test
  public void simulatorDirectionMapsToMatchingPhysicalCurve() {
    RealCartAutoDriveController.Result left =
        update(movingController(), followFrame(SteeringEvidence.Direction.LEFT, 75, 0.75f), 100L);
    assertEquals(11, left.left);
    assertEquals(14, left.right);
    assertEquals(RealCartAutoDriveController.Phase.CURVE_LEFT, left.phase);
    assertTrue(RealCartFollowFragment.commandForAutoResult(left).contains("向左"));

    RealCartAutoDriveController.Result right =
        update(movingController(), followFrame(SteeringEvidence.Direction.RIGHT, 75, 0.75f), 100L);
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
  public void steeringStrengthScalesCurveWithoutReversingAWheel() {
    assertEquals(13, RealCartAutoDriveController.innerSpeedForDemand(100, 20));
    assertEquals(10, RealCartAutoDriveController.innerSpeedForDemand(100, 100));
    assertEquals(6, RealCartAutoDriveController.innerSpeedForDemand(100, 200));
    assertEquals(14, RealCartAutoDriveController.innerSpeedForDemand(0, 200));
  }

  @Test
  public void missingSteeringEvidenceStopsInsteadOfGuessingControlSign() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    FollowStateMachine.FrameResult frame = rawFollowFrame(0.75f);
    assertTrue(update(controller, frame, 0L).isStop());
    assertEquals("steering_unavailable", controller.getLastResult().reason);
  }

  @Test
  public void distanceHoldStillAimsButSearchWithoutGateStops() {
    RealCartAutoDriveController controller = movingController();
    RealCartAutoDriveController.Result aimed =
        update(
            controller,
            frameWithError(
                SteeringEvidence.Direction.LEFT,
                80,
                -.25f,
                0.90f,
                FollowState.FOLLOW,
                BehaviorAction.FOLLOW_CAUTION),
            100L);
    assertEquals(RealCartAutoDriveController.Phase.PIVOT, aimed.phase);
    assertEquals(-5, aimed.left);
    assertEquals(5, aimed.right);
    assertFalse(aimed.translationDecision.allowed);
    assertTrue(
        update(
                controller,
                frame(
                    SteeringEvidence.Direction.LEFT,
                    80,
                    0.75f,
                    FollowState.SEARCH,
                    BehaviorAction.LOCAL_SEARCH_LEFT),
                200L)
            .isStop());
  }

  @Test
  public void tooCloseTargetMayPivotButCannotTranslate() {
    RealCartAutoDriveController.Result result =
        update(
            new RealCartAutoDriveController(),
            frameWithError(
                SteeringEvidence.Direction.RIGHT,
                50,
                .22f,
                1.20f,
                FollowState.FOLLOW,
                BehaviorAction.MOTION_STOP),
            100L);
    assertEquals(RealCartAutoDriveController.Phase.PIVOT, result.phase);
    assertEquals(5, result.left);
    assertEquals(-5, result.right);
    assertFalse(result.translationDecision.allowed);
  }

  @Test
  public void edgeTargetPivotsWithoutForwardEvenWhenFar() {
    RealCartAutoDriveController.Result result =
        update(
            movingController(),
            frameWithError(
                SteeringEvidence.Direction.LEFT,
                100,
                -.40f,
                .70f,
                FollowState.FOLLOW,
                BehaviorAction.FOLLOW_SLOW),
            100L);
    assertEquals(RealCartAutoDriveController.Phase.PIVOT, result.phase);
    assertTrue(result.translationDecision.allowed);
    assertEquals(-5, result.left);
    assertEquals(5, result.right);
  }

  @Test
  public void pivotStopsAfterThreeCenteredFrames() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    update(
        controller,
        frameWithError(
            SteeringEvidence.Direction.RIGHT,
            50,
            .25f,
            .95f,
            FollowState.FOLLOW,
            BehaviorAction.FOLLOW_CAUTION),
        0L);
    for (int i = 1; i <= 2; i++) {
      RealCartAutoDriveController.Result result =
          update(
              controller,
              frameWithError(
                  SteeringEvidence.Direction.NONE,
                  0,
                  .02f,
                  .95f,
                  FollowState.FOLLOW,
                  BehaviorAction.FOLLOW_CAUTION),
              i * 33L);
      assertEquals(RealCartAutoDriveController.Phase.PIVOT, result.phase);
    }
    RealCartAutoDriveController.Result centered =
        update(
            controller,
            frameWithError(
                SteeringEvidence.Direction.NONE,
                0,
                .02f,
                .95f,
                FollowState.FOLLOW,
                BehaviorAction.FOLLOW_CAUTION),
            99L);
    assertTrue(centered.isStop());
    assertEquals("distance_ok", centered.reason);
  }

  @Test
  public void recoveryParksWithoutLockoutAfterTwoSeconds() {
    RealCartAutoDriveController controller = movingController();
    RealCartAutoDriveController.Result first = update(controller, recoveryFrame(true), 1000L);
    assertTrue(first.isStop());
    assertFalse(first.lockout);

    update(controller, recoveryFrame(false), 1000L);
    RealCartAutoDriveController.Result timedOut =
        update(
            controller,
            recoveryFrame(false),
            1000L + RealCartAutoDriveController.RECOVERY_LIMIT_MS);
    assertTrue(timedOut.isStop());
    assertFalse(timedOut.lockout);
    assertEquals(RealCartAutoDriveController.Phase.PARKED_WAIT, timedOut.phase);
  }

  private static RealCartAutoDriveController movingController() {
    RealCartAutoDriveController controller = new RealCartAutoDriveController();
    update(controller, followFrame(SteeringEvidence.Direction.NONE, 0, 0.75f), 0L);
    update(controller, followFrame(SteeringEvidence.Direction.NONE, 0, 0.75f), 30L);
    update(controller, followFrame(SteeringEvidence.Direction.NONE, 0, 0.75f), 60L);
    return controller;
  }

  private static FollowStateMachine.FrameResult followFrame(
      SteeringEvidence.Direction direction, int demand, float heightScale) {
    return frame(direction, demand, heightScale, FollowState.FOLLOW, BehaviorAction.FOLLOW_SLOW);
  }

  private static long sequence;

  private static RealCartAutoDriveController.Result update(
      RealCartAutoDriveController controller, FollowStateMachine.FrameResult frame, long now) {
    frame.frameSequence = ++sequence;
    frame.frameTiming = new FrameTimingEvidence(now, 0, 0, 0, 0, 0, 30, 0);
    frame.simulatorIdentity = new SimulatorIdentityGuard.Decision(true, false, 1, 3, "verified");
    return controller.update(frame, now);
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

  private static FollowStateMachine.FrameResult frameWithError(
      SteeringEvidence.Direction direction,
      int demand,
      float predictedError,
      float heightScale,
      FollowState state,
      BehaviorAction action) {
    FollowStateMachine.FrameResult frame = rawFrame(heightScale, state, action, false);
    frame.steeringEvidence = evidence(direction, demand, predictedError);
    return frame;
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
            state, new Control(0.6f, 0.6f), null, null, persons, true, false, null, -1);
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
    return evidence(direction, demand, 0f);
  }

  private static SteeringEvidence evidence(
      SteeringEvidence.Direction direction, int demand, float predictedError) {
    return new SteeringEvidence(
        true,
        "test",
        predictedError,
        predictedError,
        0f,
        predictedError,
        0f,
        demand,
        direction,
        demand == 0 ? SteeringEvidence.Level.CENTER : SteeringEvidence.Level.LARGE,
        400);
  }
}
