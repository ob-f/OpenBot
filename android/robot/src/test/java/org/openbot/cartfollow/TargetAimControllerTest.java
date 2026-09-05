package org.openbot.cartfollow;
import static org.junit.Assert.*;
import org.junit.Test;
public class TargetAimControllerTest {
  private static SteeringEvidence e(float raw, float rate) {
    return new SteeringEvidence(true, "test", raw, raw, rate, -raw, 0, 80,
        raw < 0 ? SteeringEvidence.Direction.LEFT : SteeringEvidence.Direction.RIGHT,
        SteeringEvidence.Level.LARGE, 400);
  }
  @Test public void recordedRightForwardExitRemainsArcUntilEdge() {
    TargetAimController c = new TargetAimController();
    for (float raw : new float[] {.15f, .6261f, .70f, .8231f, .8411f}) {
      AimDecision d = c.update(e(raw, 0), true, true, 100, 100);
      assertTrue(d.allowed); assertFalse(d.pivots());
    }
    assertFalse(c.update(e(.85f, 0), true, true, 200, 200).allowed);
  }
  @Test public void movingRequiresFullBrakeAndPostDeadlineFrame() {
    TargetAimController c = new TargetAimController();
    assertFalse(c.update(e(.9f, 0), true, true, 0, 0).allowed);
    assertFalse(c.update(e(.9f, 0), true, false, 649, 650).allowed);
    assertEquals(10, c.update(e(.9f, 0), true, false, 650, 650).speed);
  }
  @Test public void nearTargetStartsAtPointOneAndStopsAtPointZeroFour() {
    TargetAimController c = new TargetAimController();
    assertFalse(c.update(e(.09f, 0), false, false, 0, 0).pivots());
    assertEquals(5, c.update(e(.10f, 0), false, false, 100, 100).speed);
    assertFalse(c.update(e(.04f, 0), false, false, 200, 200).allowed);
  }
  @Test public void speedDropsButNeverRisesWithinOneTurn() {
    TargetAimController c = new TargetAimController();
    assertEquals(10, c.update(e(.7f, 0), false, false, 0, 0).speed);
    assertEquals(8, c.update(e(.5f, 0), false, false, 100, 100).speed);
    assertEquals(8, c.update(e(.7f, 0), false, false, 200, 200).speed);
    assertEquals(5, c.update(e(.3f, 0), false, false, 300, 300).speed);
  }
  @Test public void schedulerBoundsTurnAndRequiresFreshPostPauseObservation() {
    TargetAimController c = new TargetAimController();
    c.update(e(.9f, 0), true, false, 0, 0);
    assertFalse(c.expire(599)); assertTrue(c.expire(600)); assertFalse(c.expire(650));
    assertFalse(c.update(e(.9f, 0), true, false, 749, 800).allowed);
    assertTrue(c.update(e(.9f, 0), true, false, 800, 800).pivots());
  }
  @Test public void centerCrossingCannotReverseBeforeSixHundredFiftyMs() {
    TargetAimController c = new TargetAimController();
    c.update(e(.3f, 0), false, false, 0, 0);
    assertFalse(c.update(e(-.3f, 0), false, false, 100, 100).allowed);
    assertFalse(c.update(e(-.3f, 0), false, false, 749, 749).allowed);
    assertEquals(AimDecision.Mode.PIVOT_LEFT, c.update(e(-.3f, 0), false, false, 750, 750).mode);
  }
  @Test public void pauseRetainsFarExitHysteresis() {
    TargetAimController c = new TargetAimController();
    c.update(e(.9f, 0), true, false, 0, 0); c.expire(600);
    assertTrue(c.update(e(.7f, 0), true, false, 750, 750).pivots());
    assertFalse(c.update(e(.55f, 0), true, false, 850, 850).allowed);
    assertEquals(AimDecision.Mode.CURVE,c.update(e(.5f, 0),true,false,1000,1000).mode);
  }
  @Test public void predictionOnlyBrakesNeverChoosesDirection() {
    TargetAimController c = new TargetAimController();
    assertEquals(AimDecision.Mode.PIVOT_RIGHT,c.update(e(.25f, 0),false,false,0,0).mode);
    assertFalse(c.update(e(.1f, -1f),false,false,100,100).allowed);
  }
  @Test public void shortPauseExitCannotBypassLaterReversalDeadline() {
    TargetAimController c = new TargetAimController();
    c.update(e(.25f, 0),false,false,0,0);
    c.update(e(.03f, 0),false,false,100,100);
    assertTrue(c.update(e(.03f, 0),false,false,250,250).allowed);
    assertFalse(c.update(e(-.25f, 0),false,false,300,300).allowed);
    assertTrue(c.update(e(-.25f, 0),false,false,750,750).pivots());
  }
}
