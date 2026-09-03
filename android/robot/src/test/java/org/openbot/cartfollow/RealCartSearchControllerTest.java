package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class RealCartSearchControllerTest {
  @Test
  public void disabledSearchParksRatherThanRotating() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(false, 18, 90, 5000);
    arm(c, y, false);
    sensors(y, 700);
    RealCartSearchController.Result r = c.poll(700, y);
    assertFalse(r.pivotAllowed);
    assertEquals(0, r.left());
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, r.evidence.phase);
  }

  @Test
  public void leftAndRightRequireZeroIntervalThenMirror() {
    for (boolean left : new boolean[] {false, true}) {
      RealCartSearchController c = new RealCartSearchController();
      YawTurnTracker y = new YawTurnTracker();
      c.configure(true, 5, 90, 5000);
      arm(c, y, left);
      sensors(y, 699);
      assertTrue(c.poll(699, y).braking);
      assertEquals(0, c.poll(699, y).left());
      sensors(y, 700);
      RealCartSearchController.Result r = c.poll(700, y);
      assertTrue(r.pivotAllowed);
      assertEquals(left ? -5 : 5, r.left());
      assertEquals(-r.left(), r.right());
    }
  }

  @Test
  public void missingSensorNeverFallsBackToTimeOnly() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 18, 90, 5000);
    c.update(observation(1, 100, .7f, .9f), 100, y);
    c.update(observation(2, 200, .8f, 1f), 200, y);
    c.update(RealCartMigrationTest.absent(3, 300), 300, y);
    RealCartSearchController.Result r = c.update(RealCartMigrationTest.absent(4, 400), 400, y);
    assertEquals("gyro_not_ready", r.reason);
    assertFalse(r.lockout);
    assertFalse(r.pivotAllowed);
  }

  @Test
  public void brakingRotationDoesNotConsumeSearchAngle() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 30, 5000);
    arm(c, y, false);
    y.reset(SteeringEvidence.Direction.RIGHT);
    y.onGyroscope(410_000_000L, 0, 0, -10);
    y.onGyroscope(510_000_000L, 0, 0, -10);
    assertTrue(y.getTurnedDegrees() > 30);
    assertTrue(c.poll(510, y).braking);
    sensors(y, 700);
    assertTrue(c.poll(700, y).pivotAllowed);
    c.noteCommand(5, -5, 700, y);
    assertEquals(0, y.getTurnedDegrees(), 0.001f);
    assertTrue(c.poll(700, y).pivotAllowed);
  }

  @Test
  public void sensorLossAfterRotationLatchesFault() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    sensors(y, 700);
    assertTrue(c.poll(700, y).pivotAllowed);
    c.noteCommand(5, -5, 700, y);
    y.setSensorStatus(true, false);
    assertTrue(c.poll(710, y).lockout);
    sensors(y, 720);
    assertTrue(c.poll(720, y).lockout);
  }

  @Test
  public void wrongDirectionFiveDegreesStopsImmediately() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    sensors(y, 700);
    c.poll(700, y);
    c.noteCommand(5, -5, 700, y);
    y.onGyroscope(710_000_000L, 0, 0, 1);
    y.onGyroscope(810_000_000L, 0, 0, 1);
    RealCartSearchController.Result r = c.poll(810, y);
    assertTrue(r.lockout);
    assertEquals("search_wrong_direction", r.reason);
    assertEquals(0, r.left());
  }

  @Test
  public void angleLimitAndAbsoluteTimeParkWithoutLockout() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 30, 1000);
    arm(c, y, false);
    sensors(y, 700);
    c.poll(700, y);
    c.noteCommand(5, -5, 700, y);
    y.onGyroscope(710_000_000L, 0, 0, -10);
    y.onGyroscope(810_000_000L, 0, 0, -10);
    RealCartSearchController.Result r = c.poll(810, y);
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, r.evidence.phase);
    assertFalse(r.lockout);
    c.configure(true, 5, 180, 1000);
    arm(c, y, false);
    sensors(y, 700);
    c.poll(700, y);
    sensors(y, 1400);
    r = c.poll(1400, y);
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, r.evidence.phase);
    assertEquals(1000, r.evidence.elapsedMs);
    assertEquals(0, r.left());
  }

  @Test
  public void candidateStopsAndDoesNotResetDeadlineOrYaw() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 1000);
    arm(c, y, false);
    sensors(y, 700);
    c.poll(700, y);
    FollowStateMachine.FrameResult candidate = RealCartMigrationTest.frame(5, 720, .5f);
    candidate.simulatorIdentity =
        new SimulatorIdentityGuard.Decision(false, true, 2, 0, "verifying");
    sensors(y, 720);
    RealCartSearchController.Result r = c.update(candidate, 720, y);
    assertFalse(r.pivotAllowed);
    assertEquals(DirectedReacquireEvidence.Phase.VERIFYING, r.evidence.phase);
    sensors(y, 1100);
    r = c.update(candidate, 1100, y);
    assertFalse(r.pivotAllowed);
    sensors(y, 1400);
    r = c.poll(1400, y);
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, r.evidence.phase);
  }

  @Test
  public void periodicSafetyGateUsesSearchPermitAndFault() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    RealCartSafetyController s = RealCartMigrationTest.ready();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    s.auto(RealCartMigrationTest.absent(4, 400), 400, c.poll(400, y));
    sensors(y, 699);
    assertTrue(s.refresh(699, c.poll(699, y)).isStop());
    sensors(y, 700);
    assertEquals(5, s.refresh(700, c.poll(700, y)).left);
    c.noteCommand(5, -5, 700, y);
    y.setSensorStatus(true, false);
    assertTrue(s.refresh(710, c.poll(710, y)).isStop());
    assertFalse(s.isAutoUnlocked());
  }

  @Test
  public void blockedArbitrationAndWrongSessionCannotBorrowSearchPermit() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    sensors(y, 700);
    RealCartSafetyController s = RealCartMigrationTest.ready();
    FollowStateMachine.FrameResult f = RealCartMigrationTest.absent(4, 400);
    f.behaviorDecision =
        new BehaviorDecisionResult(
            FollowState.LOST, BehaviorAction.BLOCKED_WAIT, "blocked", null, 0);
    assertTrue(s.auto(f, 700, c.poll(700, y)).isStop());
    assertTrue(s.refresh(710, c.poll(710, y)).isStop());
    s.setSessionGeneration(3);
    assertTrue(s.auto(f, 710, c.poll(710, y)).isStop());
  }

  @Test
  public void verifiedReturnSettlesThenRestartsLowThroughSafetyGate() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    RealCartSafetyController s = RealCartMigrationTest.ready();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    s.auto(RealCartMigrationTest.absent(4, 400), 400, c.poll(400, y));
    sensors(y, 700);
    assertEquals(5, s.refresh(700, c.poll(700, y)).left);
    c.noteCommand(5, -5, 700, y);
    for (int i = 0; i < 8; i++) {
      long now = 800 + i * 100;
      sensors(y, now);
      FollowStateMachine.FrameResult f = observation(5 + i, now, .4f, .6f);
      RealCartSearchController.Result search = c.update(f, now, y);
      RealCartSafetyController.Output out = s.auto(f, now, search);
      if (i < 7) assertTrue("verification/settle/restart frame " + i, out.isStop());
      else {
        assertEquals(14, out.left);
        assertEquals(14, out.right);
      }
      assertTrue(s.isAutoUnlocked());
    }
  }

  @Test
  public void parkedDoesNotRepeatSearchWithoutVerifiedFollow() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 1000);
    arm(c, y, false);
    sensors(y, 1400);
    c.poll(1400, y);
    for (int i = 0; i < 5; i++) {
      long now = 1500 + i * 100;
      sensors(y, now);
      RealCartSearchController.Result r =
          c.update(RealCartMigrationTest.absent(5 + i, now), now, y);
      assertFalse(r.pivotAllowed);
      assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, r.evidence.phase);
    }
  }

  @Test
  public void staleVisionDoesNotBeginPivotAfterBrake() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    sensors(y, 801);
    assertFalse(c.poll(801, y).pivotAllowed);
  }

  static void arm(RealCartSearchController c, YawTurnTracker y, boolean left) {
    arm(c, y, left, true);
  }

  static void arm(RealCartSearchController c, YawTurnTracker y, boolean left, boolean submitStop) {
    y.clear();
    sensors(y, 100);
    c.update(observation(1, 100, left ? .1f : .7f, left ? .3f : .9f), 100, y);
    sensors(y, 200);
    c.update(observation(2, 200, left ? 0 : .8f, left ? .2f : 1), 200, y);
    sensors(y, 300);
    c.update(RealCartMigrationTest.absent(3, 300), 300, y);
    sensors(y, 400);
    assertFalse(c.update(RealCartMigrationTest.absent(4, 400), 400, y).pivotAllowed);
    if (submitStop) c.noteCommand(0, 0, 400, y);
  }

  @Test
  public void zeroSubmissionNotObservationStartsBrakeInterval() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 1000);
    arm(c, y, false, false);
    sensors(y, 700);
    assertFalse(c.poll(700, y).pivotAllowed);
    c.noteCommand(0, 0, 700, y);
    sensors(y, 900);
    c.update(RealCartMigrationTest.absent(5, 900), 900, y);
    assertFalse(c.poll(999, y).pivotAllowed);
    sensors(y, 1000);
    assertTrue(c.poll(1000, y).pivotAllowed);
    sensors(y, 1400);
    assertEquals(DirectedReacquireEvidence.Phase.PARKED_WAIT, c.poll(1400, y).evidence.phase);
  }

  @Test
  public void searchStateEntryIsConsumedOnlyOnce() {
    RealCartSearchController c = new RealCartSearchController();
    YawTurnTracker y = new YawTurnTracker();
    c.configure(true, 5, 90, 5000);
    arm(c, y, false);
    assertTrue(c.consumeEnterRequest());
    assertFalse(c.consumeEnterRequest());
    sensors(y, 500);
    c.update(RealCartMigrationTest.absent(5, 500), 500, y);
    assertFalse(c.consumeEnterRequest());
  }

  static void sensors(YawTurnTracker y, long time) {
    y.setSensorStatus(true, true);
    y.onGravity(time * 1_000_000L, 0, 0, 9.8f);
    y.onGyroscope(time * 1_000_000L, 0, 0, 0);
  }

  static FollowStateMachine.FrameResult observation(long seq, long time, float left, float right) {
    FollowStateMachine.FrameResult f = RealCartMigrationTest.frame(seq, time, .5f);
    f.targetObservation =
        new TargetObservationEvidence(
            new RectF(left, 0, right, 1), 1, time, .95f, false, true, 1, "locked");
    ReIDMatchResult m =
        new ReIDMatchResult(.95f, .1f, 0, 8, true, 1, "fresh", .95f, 0, seq)
            .withBinding(1, time, seq, true, 0, false);
    f.identityEvidence =
        new IdentityEvidence(
            .95f, .95f, true, "fresh", m, null, 3, 0, f.target, 1, 1, -1, 1, 5, 0, .95f, 0, 0, 0, 0,
            3, 0, "locked");
    return f;
  }
}
