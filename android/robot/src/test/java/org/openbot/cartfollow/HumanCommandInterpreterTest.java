package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openbot.vehicle.Control;

public class HumanCommandInterpreterTest {
  private final HumanCommandInterpreter interpreter = new HumanCommandInterpreter();

  @Test
  public void fasterRightWheelUsesTheProjectRightTurnConvention() {
    assertEquals(
        HumanCommandInterpreter.CMD_FORWARD_RIGHT,
        interpreter.interpret(new Control(0.2f, 0.8f), FollowState.FOLLOW, DistanceState.TOO_FAR));
  }

  @Test
  public void fasterLeftWheelUsesTheProjectLeftTurnConvention() {
    assertEquals(
        HumanCommandInterpreter.CMD_FORWARD_LEFT,
        interpreter.interpret(new Control(0.8f, 0.2f), FollowState.FOLLOW, DistanceState.TOO_FAR));
  }

  @Test
  public void distanceSafetyOverridesSteeringEvidence() {
    SteeringEvidence evidence =
        new SteeringEvidence(
            true,
            "test",
            0.5f,
            0.5f,
            0f,
            0.5f,
            0f,
            46,
            SteeringEvidence.Direction.RIGHT,
            SteeringEvidence.Level.MEDIUM,
            400);

    assertEquals(HumanCommandInterpreter.CMD_TOO_CLOSE, interpreter.interpret(evidence, DistanceState.TOO_CLOSE));
    assertEquals(HumanCommandInterpreter.CMD_DIST_UNKNOWN, interpreter.interpret(evidence, DistanceState.UNKNOWN));
    assertEquals("请向前并向右中等转弯 · 46%", interpreter.interpret(evidence, DistanceState.TOO_FAR));
  }
}
