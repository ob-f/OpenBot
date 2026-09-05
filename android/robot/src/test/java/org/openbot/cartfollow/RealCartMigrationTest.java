package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector.Recognition;
import org.openbot.vehicle.Control;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class RealCartMigrationTest {
  private static SteeringEvidence curve(float raw, int demand) {
    return new SteeringEvidence(true, "replay", raw, raw, 0, raw, 0, demand,
        raw < 0 ? SteeringEvidence.Direction.LEFT : SteeringEvidence.Direction.RIGHT,
        SteeringEvidence.Level.MEDIUM, 0);
  }

  @Test public void returnBrakeReleasesDifferentialAndNextObservationChoosesNewSide() {
    for (int sign : new int[] {-1, 1}) {
      RealCartSafetyController safety = movingHigh();
      FollowStateMachine.FrameResult f = frame(7, 350, .5f);
      f.steeringEvidence = new SteeringEvidence(true, "return", sign * .10f, sign * .10f,
          -sign * .8f, -sign * .1f, 0, 0,
          sign < 0 ? SteeringEvidence.Direction.LEFT : SteeringEvidence.Direction.RIGHT,
          SteeringEvidence.Level.CENTER, 0);
      RealCartSafetyController.Output out = safety.auto(f, 350);
      assertEquals(21, out.left);
      assertEquals(21, out.right);
      assertEquals("curve_return_brake", safety.getAutoDriveResult().reason);
      f = frame(8, 450, .5f);
      f.steeringEvidence = curve(-sign * .15f, 15);
      out = safety.auto(f, 450);
      assertEquals(sign > 0 ? 20 : 21, out.left);
      assertEquals(sign > 0 ? 21 : 20, out.right);
    }
  }

  @Test public void replayRightForwardCurveDoesNotStopBeforePointEightFive() {
    RealCartSafetyController safety = movingHigh();
    float[] errors = {.15f, .6261f, .70f, .8231f, .8411f};
    for (int i = 0; i < errors.length; i++) {
      long time = 350 + i * 100;
      FollowStateMachine.FrameResult f = frame(7 + i, time, .5f);
      f.steeringEvidence = curve(errors[i], 5); // Legacy low demand cannot override raw steering.
      RealCartSafetyController.Output out = safety.auto(f, time);
      assertTrue(out.left > out.right && out.right > 0);
    }
  }

  @Test public void loggedGentleCurveKeepsDistanceGearAndBoundedPositiveDifferential() {
    RealCartSafetyController safety = movingHigh();
    FollowStateMachine.FrameResult f = frame(7, 350, .35f);
    f.steeringEvidence = curve(.2505f, 19);
    RealCartSafetyController.Output out = safety.auto(f, 350);
    assertEquals(21, out.left);
    assertEquals(20, out.right);
    assertTrue(out.left > 14 && out.right > 0);
  }

  @Test
  public void threeDistinctFramesStartLowAndThreeMoreUpshift() {
    RealCartSafetyController s = ready();
    assertTrue(s.auto(frame(1, 100, .5f), 100).isStop());
    assertTrue(s.auto(frame(1, 100, .5f), 120).isStop());
    assertTrue(s.auto(frame(2, 140, .5f), 140).isStop());
    assertEquals(14, s.auto(frame(3, 180, .5f), 180).left);
    assertEquals(14, s.auto(frame(4, 220, .5f), 220).left);
    assertEquals(14, s.auto(frame(5, 260, .5f), 260).left);
    assertEquals(21, s.auto(frame(6, 300, .5f), 300).left);
  }

  @Test
  public void distanceAndTurnImmediatelyDownshiftWithoutZero() {
    RealCartSafetyController s = movingHigh();
    FollowStateMachine.FrameResult f = frame(7, 350, .5f);
    f.steeringEvidence = curve(-.4f, 50);
    RealCartSafetyController.Output mid = s.auto(f, 350);
    assertEquals(18, mid.right);
    assertEquals(16, mid.left);
    f = frame(8, 400, .5f);
    f.steeringEvidence = curve(.56f, 90);
    RealCartSafetyController.Output low = s.auto(f, 400);
    assertEquals(14, low.left);
    assertEquals(11, low.right);
  }

  @Test
  public void speedCapAndCautionRestrictHighGear() {
    RealCartSafetyController s = ready();
    s.setMaximumGear(18);
    for (int i = 1; i <= 8; i++) assertTrue(s.auto(frame(i, i * 40, .4f), i * 40).left <= 18);
    FollowStateMachine.FrameResult f = frame(9, 400, .4f);
    f.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.FOLLOW_CAUTION, BehaviorAction.FOLLOW_CAUTION, "trusted_recovery", null, 1);
    assertEquals(14, s.auto(f, 400).left);
  }

  @Test
  public void verifiedIdentityIsMandatoryDespiteGreenBelief() {
    RealCartSafetyController s = movingHigh();
    FollowStateMachine.FrameResult f = frame(7, 350, .5f);
    f.simulatorIdentity = new SimulatorIdentityGuard.Decision(false, false, 1, 0, "adapting");
    assertTrue(s.auto(f, 350).isStop());
    assertTrue(s.isAutoUnlocked());
    assertTrue(s.refresh(380, null).isStop());
    assertTrue(s.auto(frame(8, 390, .5f), 390).isStop());
    assertTrue(s.auto(frame(9, 420, .5f), 420).isStop());
    assertEquals(14, s.auto(frame(10, 450, .5f), 450).left);
  }

  @Test
  public void unconfirmedMissingAndTooCloseAllStop() {
    for (int kind = 0; kind < 4; kind++) {
      RealCartSafetyController s = movingHigh();
      FollowStateMachine.FrameResult f = frame(7, 350, .5f);
      if (kind == 0) f.simulatorIdentity = null;
      else if (kind == 1) f.distanceEstimate = null;
      else
        f.distanceEstimate =
            new ImageSetpointDistanceEstimator.DistanceEstimate(
                1, 1, 0, kind == 2 ? DistanceState.OK : DistanceState.TOO_CLOSE, 1, null);
      assertTrue(s.auto(f, 350).isStop());
    }
  }

  @Test
  public void staleSourceCannotStartOrRefreshMovingCommand() {
    RealCartSafetyController s = ready();
    assertTrue(s.auto(frame(1, 100, .5f), 501).isStop());
    assertTrue(s.isAutoUnlocked());
    s = movingHigh();
    assertTrue(s.auto(frame(7, 200, .5f), 601).isStop());
    assertFalse(s.isAutoUnlocked());
  }

  @Test
  public void schedulerStopsAtSourceDeadlineNotInferenceCompletion() {
    RealCartSafetyController s = ready();
    s.auto(frame(1, 100, .5f), 200);
    s.auto(frame(2, 140, .5f), 240);
    assertEquals(14, s.auto(frame(3, 180, .5f), 280).left);
    assertFalse(s.refresh(580, null).isStop());
    assertTrue(s.refresh(581, null).isStop());
    assertFalse(s.isAutoUnlocked());
  }

  @Test
  public void generationModeConnectionAndEmergencyCannotReviveMotion() {
    for (int kind = 0; kind < 5; kind++) {
      RealCartSafetyController s = movingHigh();
      if (kind == 0) s.setSessionGeneration(2);
      if (kind == 1) s.setMode(RealCartSafetyController.Mode.MANUAL);
      if (kind == 2) s.setConnection(false, false);
      if (kind == 3) s.setForeground(false);
      if (kind == 4) s.latchEmergency();
      assertTrue(s.auto(frame(7, 350, .5f), 350).isStop());
      assertTrue(s.refresh(360, null).isStop());
    }
  }

  @Test
  public void parkedWaitRetainsUnlockButDoesNotAuthorizeReturn() {
    RealCartSafetyController s = movingHigh();
    FollowStateMachine.FrameResult missing = absent(7, 400);
    assertTrue(s.auto(missing, 400).isStop());
    assertTrue(s.auto(absent(8, 2500), 2500).isStop());
    assertEquals(RealCartAutoDriveController.Phase.PARKED_WAIT, s.getAutoDriveResult().phase);
    assertTrue(s.isAutoUnlocked());
    FollowStateMachine.FrameResult candidate = frame(9, 3000, .5f);
    candidate.simulatorIdentity =
        new SimulatorIdentityGuard.Decision(false, true, 2, 4, "global_verifying");
    assertTrue(s.auto(candidate, 3000).isStop());
  }

  @Test
  public void calibratedStrengthPreservesPositiveMirrorOutputs() {
    for (int gear : new int[] {14, 18, 21})
      for (int demand = 0; demand <= 100; demand++) {
        int inner = RealCartAutoDriveController.innerSpeedForDemand(gear, demand, 200);
        assertTrue(inner >= 6);
        assertTrue(inner <= gear);
      }
    assertEquals(10, RealCartAutoDriveController.innerSpeedForDemand(14, 100, 200));
    assertEquals(14, RealCartAutoDriveController.innerSpeedForDemand(18, 100, 200));
    assertEquals(17, RealCartAutoDriveController.innerSpeedForDemand(21, 100, 200));
  }

  @Test
  public void sharedGearHysteresisKeepsSimulatorThresholds() {
    AutoGearSelector g = new AutoGearSelector();
    assertEquals(14, g.distanceGear(.73f));
    assertEquals(18, g.distanceGear(.72f));
    g.select(18);
    g.select(18);
    assertEquals(18, g.select(18));
    assertEquals(18, g.distanceGear(.78f));
    assertEquals(14, g.distanceGear(.79f));
    g.select(21);
    g.select(21);
    assertEquals(21, g.select(21));
    assertEquals(21, g.distanceGear(.63f));
    assertEquals(18, g.distanceGear(.64f));
  }

  static RealCartSafetyController ready() {
    RealCartSafetyController s = new RealCartSafetyController();
    s.setForeground(true);
    s.setConnection(true, true);
    s.setMode(RealCartSafetyController.Mode.AUTO);
    assertTrue(s.unlockAuto());
    s.setAutoRunEnabled(true, 0);
    return s;
  }

  static RealCartSafetyController movingHigh() {
    RealCartSafetyController s = ready();
    for (int i = 1; i <= 6; i++) s.auto(frame(i, i * 40, .5f), i * 40);
    assertEquals(21, s.getAutoDriveResult().gear);
    return s;
  }

  static FollowStateMachine.FrameResult frame(long seq, long time, float scale) {
    Recognition p = new Recognition("1", "person", .95f, new RectF(.35f, .1f, .65f, .9f), 0);
    FollowStateMachine.FrameResult f =
        new FollowStateMachine.FrameResult(
            FollowState.FOLLOW,
            new Control(0, 0),
            p,
            null,
            Collections.singletonList(p),
            true,
            false,
            null,
            -1);
    f.frameSequence = seq;
    f.frameTiming = new FrameTimingEvidence(time, 0, 0, 0, 0, 0, 30, 0);
    f.simulatorIdentity = new SimulatorIdentityGuard.Decision(true, false, 1, 3, "verified");
    f.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.FOLLOW, BehaviorAction.FOLLOW_SLOW, "follow", null, 1);
    f.distanceEstimate =
        new ImageSetpointDistanceEstimator.DistanceEstimate(
            scale, scale, 0, DistanceState.TOO_FAR, 1, null);
    f.steeringEvidence = steering(SteeringEvidence.Direction.NONE, 0);
    return f;
  }

  static FollowStateMachine.FrameResult absent(long seq, long time) {
    FollowStateMachine.FrameResult f =
        new FollowStateMachine.FrameResult(
            FollowState.LOST,
            new Control(0, 0),
            null,
            null,
            Collections.emptyList(),
            false,
            false,
            null,
            -1);
    f.frameSequence = seq;
    f.frameTiming = new FrameTimingEvidence(time, 0, 0, 0, 0, 0, 30, 0);
    f.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.LOST, BehaviorAction.MOTION_STOP, "target_lost", null, 0);
    return f;
  }

  static SteeringEvidence steering(SteeringEvidence.Direction d, int demand) {
    return new SteeringEvidence(
        true, "test", 0, 0, 0, 0, 0, demand, d, SteeringEvidence.Level.MEDIUM, 400);
  }
}
