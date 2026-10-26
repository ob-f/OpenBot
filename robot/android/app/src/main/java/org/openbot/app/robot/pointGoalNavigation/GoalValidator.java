package org.openbot.app.robot.pointGoalNavigation;

import androidx.annotation.Nullable;
import java.util.List;

/**
 * Math for turning a tap into a goal on the floor, in ARCore world coordinates (y is up, meters).
 * Kept lenient on purpose: it only rejects taps a floor robot can never drive to.
 */
public final class GoalValidator {

  /** The floor is always at least this far below the phone on the robot. */
  public static final float MIN_CAMERA_HEIGHT_M = 0.03f;

  /** Phone camera height on an OpenBot DIY body, until it has been measured. */
  public static final float DEFAULT_CAMERA_HEIGHT_M = 0.12f;

  /** Starting camera height per robot body, until the floor is measured (about +-3 cm). */
  public static float startingCameraHeight(@Nullable String vehicleType) {
    if (vehicleType == null) return DEFAULT_CAMERA_HEIGHT_M;
    switch (vehicleType) {
      case "RTR_520":
        return 0.16f;
      case "RC_CAR":
        return 0.22f;
      case "MTV":
        return 0.23f;
      default:
        return DEFAULT_CAMERA_HEIGHT_M;
    }
  }

  /** Measured heights outside this range mean the phone isn't on the robot right now. */
  public static final float MIN_MOUNT_HEIGHT_M = 0.04f;

  public static final float MAX_MOUNT_HEIGHT_M = 0.40f;

  /** Closer goals would count as reached right away. */
  public static final float MIN_GOAL_DISTANCE_M = 0.30f;

  /**
   * Further than this the tap ray is so flat that a tiny angle error is a big distance error (at 12
   * cm height, 0.1 degree is already ~25 cm at 4 m).
   */
  public static final float MAX_RAY_DISTANCE_M = 6f;

  public enum Result {
    OK,
    NOT_FLOOR,
    TOO_CLOSE,
    NOT_TRACKING
  }

  private GoalValidator() {}

  /**
   * @param camera camera position {x, y, z}
   * @param hit tapped point on the floor {x, y, z}, or null if the tap doesn't reach the floor
   */
  public static Result validate(float[] camera, @Nullable float[] hit) {
    if (hit == null) return Result.NOT_FLOOR;
    // Windows, walls, sky: anything at or above the camera can't be on the floor.
    if (hit[1] > camera[1] - MIN_CAMERA_HEIGHT_M) return Result.NOT_FLOOR;
    if (horizontalDistance(camera, hit) < MIN_GOAL_DISTANCE_M) return Result.TOO_CLOSE;
    return Result.OK;
  }

  /**
   * Where the tap ray meets the floor. Taps at or above the horizon (window, wall, sky) never do.
   *
   * @return {x, y, z}, or null if the ray doesn't point down at the floor
   */
  @Nullable
  public static float[] intersectFloor(float[] origin, float[] direction, float floorY) {
    if (direction[1] >= -1e-4f) return null;
    float t = (floorY - origin[1]) / direction[1];
    if (t <= 0) return null;
    float[] hit = {
      origin[0] + t * direction[0], floorY, origin[2] + t * direction[2],
    };
    if (horizontalDistance(origin, hit) > MAX_RAY_DISTANCE_M) return null;
    return hit;
  }

  /**
   * Floor height from the tracked upward-facing planes: the largest one below the camera. The
   * lowest one would pick up false planes reflected in shiny floors.
   *
   * @param planes each entry is {y, area}
   */
  @Nullable
  public static Float pickFloorY(List<float[]> planes, float cameraY) {
    Float floorY = null;
    float bestArea = 0f;
    for (float[] plane : planes) {
      if (plane[0] > cameraY - MIN_CAMERA_HEIGHT_M) continue;
      if (plane[1] > bestArea) {
        bestArea = plane[1];
        floorY = plane[0];
      }
    }
    return floorY;
  }

  public static float horizontalDistance(float[] a, float[] b) {
    float dx = a[0] - b[0];
    float dz = a[2] - b[2];
    return (float) Math.sqrt(dx * dx + dz * dz);
  }
}
