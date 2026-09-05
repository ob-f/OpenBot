package org.openbot.cartfollow;

import static org.junit.Assert.*;

import org.junit.Test;

public class TargetAimControllerTest {
  private static SteeringEvidence evidence(float raw, float rate, float prediction) {
    return new SteeringEvidence(
        true,
        "replay",
        raw,
        raw,
        rate,
        prediction,
        1f,
        80,
        prediction < 0 ? SteeringEvidence.Direction.LEFT : SteeringEvidence.Direction.RIGHT,
        SteeringEvidence.Level.LARGE,
        400);
  }

  @Test
  public void recordedPredictionSwingsCannotTriggerFarPivot() {
    TargetAimController aim = new TargetAimController();
    float[] raw = {.3113f, .128f, -.2127f, .0273f, -.0107f, .005f, -.0826f};
    float[] predicted = {.4346f, -.0809f, -.5894f, .2443f, -.3386f, .1962f, .1184f};
    for (int i = 0; i < raw.length; i++) {
      AimDecision result =
          aim.update(evidence(raw[i], 0f, predicted[i]), true, true, i * 200L, i * 200L);
      assertTrue(result.allowed);
      assertFalse(result.pivots());
    }
  }

  @Test
  public void movingCartBrakesBeforeEdgePivotAndRequiresPostSettleObservation() {
    TargetAimController aim = new TargetAimController();
    SteeringEvidence edge = evidence(.7f, 0f, .8f);
    assertFalse(aim.update(edge, true, true, 0, 0).allowed);
    assertFalse(aim.update(edge, true, false, 600, 650).allowed);
    assertTrue(aim.update(edge, true, false, 650, 650).pivots());
  }

  @Test
  public void centerCrossingBrakesRatherThanImmediatelyReversing() {
    TargetAimController aim = new TargetAimController();
    assertTrue(aim.update(evidence(.25f, 0f, .3f), false, false, 0, 0).pivots());
    assertFalse(aim.update(evidence(-.25f, -1f, -.6f), false, false, 100, 100).allowed);
    assertFalse(aim.update(evidence(-.25f, 0f, -.3f), false, false, 700, 700).allowed);
    assertEquals(
        AimDecision.Mode.PIVOT_LEFT,
        aim.update(evidence(-.25f, 0f, -.3f), false, false, 750, 750).mode);
  }

  @Test
  public void approachingCenterBrakesEarlyWithoutCommandingOppositeTurn() {
    TargetAimController aim = new TargetAimController();
    aim.update(evidence(.25f, 0f, .3f), false, false, 0, 0);
    assertFalse(aim.update(evidence(.128f, -.6665f, -.0809f), false, false, 100, 100).allowed);
  }

  @Test
  public void schedulerExpiresPulseWithoutAnotherCameraFrame() {
    TargetAimController aim = new TargetAimController();
    aim.update(evidence(.7f, 0f, .7f), true, false, 0, 0);
    assertFalse(aim.expire(299));
    assertTrue(aim.expire(300));
    assertFalse(aim.expire(400));
    assertFalse(aim.update(evidence(.7f, 0f, .7f), true, false, 949, 949).allowed);
    assertTrue(aim.update(evidence(.7f, 0f, .7f), true, false, 950, 950).pivots());
  }

  @Test
  public void farTargetReturnsToCurveBeforeBecomingPerfectlyCentered() {
    TargetAimController aim = new TargetAimController();
    aim.update(evidence(.7f, 0f, .7f), true, false, 0, 0);
    assertFalse(aim.update(evidence(.3f, 0f, .4f), true, false, 100, 100).allowed);
    assertEquals(
        AimDecision.Mode.CURVE, aim.update(evidence(.3f, 0f, .4f), true, false, 750, 750).mode);
  }
}
