package org.openbot.app.robot.pointGoalNavigation;

import static org.junit.Assert.assertArrayEquals;
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
  public void startingCameraHeight_followsTheRobotBody() {
    assertEquals(0.16f, GoalValidator.startingCameraHeight("RTR_520"), 1e-6f);
    assertEquals(0.22f, GoalValidator.startingCameraHeight("RC_CAR"), 1e-6f);
    assertEquals(0.23f, GoalValidator.startingCameraHeight("MTV"), 1e-6f);
    assertEquals(0.12f, GoalValidator.startingCameraHeight("DIY"), 1e-6f);
    assertEquals(0.12f, GoalValidator.startingCameraHeight(""), 1e-6f);
    assertEquals(0.12f, GoalValidator.startingCameraHeight(null), 1e-6f);
  }

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

  // Robot on the floor at y = 0, phone 12 cm up, looking 2 m ahead.
  private static final float[] PHONE = {0f, 0.12f, 0f};
  private static final float[] TWO_METERS_AHEAD = rayTo(PHONE, new float[] {0f, 0f, -2f});

  private static float[] rayTo(float[] from, float[] to) {
    float dx = to[0] - from[0];
    float dy = to[1] - from[1];
    float dz = to[2] - from[2];
    float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    return new float[] {dx / length, dy / length, dz / length};
  }

  @Test
  public void tapHittingTheFloor_usesTheExactHitPoint() {
    float[] hit = {0.05f, 0.01f, -1.95f};
    assertArrayEquals(hit, GoalValidator.goalOnFloor(PHONE, TWO_METERS_AHEAD, hit, 0f), 1e-6f);
  }

  @Test
  public void tapOnFurniture_isNotFloor() {
    // Sofa seat 40 cm high, in front of where the ray would meet the floor.
    float[] sofa = {0f, 0.4f, -1f};
    assertNull(GoalValidator.goalOnFloor(PHONE, TWO_METERS_AHEAD, sofa, 0f));
  }

  @Test
  public void tapOnAWall_isNotFloor() {
    float[] wall = {0f, 0.3f, -1.5f};
    assertNull(GoalValidator.goalOnFloor(PHONE, TWO_METERS_AHEAD, wall, 0f));
  }

  @Test
  public void hitJustAboveTheFloor_isStillFloor() {
    float[] hit = {0f, GoalValidator.FLOOR_TOLERANCE_M - 0.01f, -2f};
    assertArrayEquals(hit, GoalValidator.goalOnFloor(PHONE, TWO_METERS_AHEAD, hit, 0f), 1e-6f);
  }

  @Test
  public void hitBelowTheFloor_isIgnored() {
    // A reflection in a shiny floor: use where the ray meets the floor instead.
    float[] reflection = {0f, -0.5f, -3f};
    float[] goal = GoalValidator.goalOnFloor(PHONE, TWO_METERS_AHEAD, reflection, 0f);
    assertEquals(0f, goal[1], 1e-5f);
    assertEquals(-2f, goal[2], 1e-4f);
  }

  @Test
  public void noHit_usesTheRayOnTheFloor() {
    float[] goal = GoalValidator.goalOnFloor(PHONE, TWO_METERS_AHEAD, null, 0f);
    assertEquals(0f, goal[1], 1e-5f);
    assertEquals(-2f, goal[2], 1e-4f);
  }

  @Test
  public void robotHeldUp_goalStaysOnTheDetectedFloor() {
    // Phone lifted to 1 m while tapping; the floor plane is still at y = 0.
    float[] lifted = {0f, 1f, 0f};
    float[] goal =
        GoalValidator.goalOnFloor(lifted, rayTo(lifted, new float[] {0f, 0f, -2f}), null, 0f);
    assertEquals(0f, goal[1], 1e-5f);
    assertEquals(-2f, goal[2], 1e-4f);
    assertEquals(Result.OK, GoalValidator.validate(lifted, goal));
  }

  @Test
  public void hitOnTheFloorTooFarAway_isRejected() {
    float[] far = {0f, 0f, -8f};
    assertNull(GoalValidator.goalOnFloor(PHONE, rayTo(PHONE, far), far, 0f));
  }

  @Test
  public void noRayAndNoHit_noGoal() {
    assertNull(GoalValidator.goalOnFloor(null, null, null, 0f));
  }
}
