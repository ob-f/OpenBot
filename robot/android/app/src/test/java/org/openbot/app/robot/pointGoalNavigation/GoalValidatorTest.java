package org.openbot.app.robot.pointGoalNavigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.openbot.app.robot.pointGoalNavigation.GoalValidator.Result;

public class GoalValidatorTest {

  // Phone mounted on the robot ~15 cm above the floor, world y is up.
  private static final float[] CAMERA = {0f, 0f, 0f};

  @Test
  public void floorTwoMetersAhead_isAccepted() {
    assertEquals(Result.OK, GoalValidator.validate(CAMERA, new float[] {0f, -0.15f, -2f}));
  }

  @Test
  public void farFloorAcrossTheRoom_isAccepted() {
    assertEquals(Result.OK, GoalValidator.validate(CAMERA, new float[] {3f, -0.15f, -4f}));
  }

  @Test
  public void tapNotReachingTheFloor_isNotFloor() {
    assertEquals(Result.NOT_FLOOR, GoalValidator.validate(CAMERA, null));
  }

  @Test
  public void spotThirtyCentimetersAway_measuresThirtyCentimeters() {
    // Camera 12 cm up; a floor spot 0.3 m ahead is atan(0.12 / 0.3) = 21.8 deg below horizon.
    double angle = Math.atan2(0.12, 0.3);
    float[] direction = {0f, (float) -Math.sin(angle), (float) -Math.cos(angle)};
    float[] hit = GoalValidator.intersectFloor(new float[] {0f, 0.12f, 0f}, direction, 0f);
    assertEquals(0.3f, GoalValidator.horizontalDistance(new float[] {0, 0.12f, 0}, hit), 1e-4f);
    assertEquals(Result.OK, GoalValidator.validate(new float[] {0, 0.12f, 0}, hit));
  }

  @Test
  public void spotTwoMetersToTheSide_measuresTwoMeters() {
    double angle = Math.atan2(0.12, 2.0);
    float[] direction = {(float) Math.cos(angle), (float) -Math.sin(angle), 0f};
    float[] hit = GoalValidator.intersectFloor(new float[] {0f, 0.12f, 0f}, direction, 0f);
    assertEquals(2f, hit[0], 1e-3f);
    assertEquals(0f, hit[2], 1e-5f);
  }

  @Test
  public void farSpotBeyondTrustedRange_isRejected() {
    double angle = Math.atan2(0.12, 8.0);
    float[] direction = {0f, (float) -Math.sin(angle), (float) -Math.cos(angle)};
    assertNull(GoalValidator.intersectFloor(new float[] {0f, 0.12f, 0f}, direction, 0f));
  }

  @Test
  public void windowOrBuildingAboveCamera_isNotFloor() {
    assertEquals(Result.NOT_FLOOR, GoalValidator.validate(CAMERA, new float[] {0f, 3f, -40f}));
  }

  @Test
  public void pointAtCameraHeight_isNotFloor() {
    assertEquals(Result.NOT_FLOOR, GoalValidator.validate(CAMERA, new float[] {0f, 0f, -2f}));
  }

  @Test
  public void goalUnderTheRobot_isTooClose() {
    assertEquals(Result.TOO_CLOSE, GoalValidator.validate(CAMERA, new float[] {0f, -0.15f, -0.2f}));
  }

  @Test
  public void rayDownToTheFloor_hitsAtTheRightDistance() {
    // Camera 0.15 m up, looking 45 degrees down: hits the floor 0.15 m ahead.
    float[] hit =
        GoalValidator.intersectFloor(
            new float[] {0f, 0.15f, 0f}, new float[] {0f, -0.7071f, -0.7071f}, 0f);
    assertEquals(0f, hit[1], 1e-5f);
    assertEquals(-0.15f, hit[2], 1e-4f);
  }

  @Test
  public void rayLevelOrUp_neverHitsTheFloor() {
    assertNull(GoalValidator.intersectFloor(new float[] {0, 0.15f, 0}, new float[] {0, 0, -1}, 0f));
    assertNull(
        GoalValidator.intersectFloor(new float[] {0, 0.15f, 0}, new float[] {0, 0.5f, -1}, 0f));
  }

  @Test
  public void rayAlmostLevel_isTooFarToTrust() {
    assertNull(
        GoalValidator.intersectFloor(new float[] {0, 0.15f, 0}, new float[] {0, -0.001f, -1}, 0f));
  }

  @Test
  public void distanceIgnoresHeight() {
    float d = GoalValidator.horizontalDistance(new float[] {0, 0, 0}, new float[] {3, -5, 4});
    assertEquals(5f, d, 1e-5f);
  }

  @Test
  public void floorIsLargestPlaneBelowCamera() {
    Float floorY =
        GoalValidator.pickFloorY(
            Arrays.asList(
                new float[] {-0.15f, 6f}, // floor
                new float[] {0.45f, 1f}, // table above the camera
                new float[] {-0.5f, 0.5f}), // small reflection under a shiny floor
            0f);
    assertEquals(-0.15f, floorY, 1e-5f);
  }

  @Test
  public void noPlanes_noFloor() {
    assertNull(GoalValidator.pickFloorY(Collections.emptyList(), 0f));
  }
}
