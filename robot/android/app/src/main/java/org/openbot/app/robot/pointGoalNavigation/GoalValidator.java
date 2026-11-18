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

  /** A hit more than this above the floor is furniture or a wall, not the floor. */
  public static final float FLOOR_TOLERANCE_M = 0.05f;

  /** Depth hits get less accurate with distance (~10 cm high at 3-4 m); plane hits don't. */
  public static final float DEPTH_TOLERANCE_PER_M = 0.04f;

  /** An upward-facing plane this close to the floor is the floor. */
  public static final float UPWARD_PLANE_TOLERANCE_M = 0.10f;

  /** A repeat tap this close, this soon, overrides an above-floor rejection. */
  public static final float REPEAT_TAP_RADIUS_PX = 50f;

  public static final long REPEAT_TAP_WINDOW_MS = 3000;

  public enum Result {
    OK,
    NOT_FLOOR,
    /** The tap hit something above the floor; tapping again places the goal below it. */
    ABOVE_FLOOR,
    TOO_CLOSE,
    NOT_TRACKING
  }

  /** What the tap hit. */
  public enum HitType {
    UPWARD_PLANE,
    OTHER_PLANE,
    DEPTH
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
   * Floor hit: that spot (upward planes at a detected floor's height). Hit above the floor: null.
   * No hit, a far hit, or one below a detected floor (a reflection): where the tap ray meets the
   * floor. An estimated floor can be too high (robot held up), so a hit below it is used.
   */
  @Nullable
  public static float[] goalOnFloor(
      @Nullable float[] origin,
      @Nullable float[] direction,
      @Nullable float[] surfaceHit,
      HitType hitType,
      float floorY,
      boolean floorFound) {
    // Junk far hits (often before the floor is found): use the floor ray instead.
    if (surfaceHit != null
        && origin != null
        && horizontalDistance(origin, surfaceHit) > MAX_RAY_DISTANCE_M) {
      surfaceHit = null;
    }
    if (surfaceHit != null) {
      float tolerance =
          origin == null
              ? FLOOR_TOLERANCE_M
              : floorTolerance(hitType, distance(origin, surfaceHit));
      if (surfaceHit[1] > floorY + tolerance) return null;
      if (!floorFound || surfaceHit[1] >= floorY - tolerance) {
        // Snap only to a detected floor; an estimate can be in the air (robot held up).
        return hitType == HitType.UPWARD_PLANE && floorFound
            ? onFloor(surfaceHit, floorY)
            : surfaceHit;
      }
    }
    if (origin == null || direction == null) return null;
    return intersectFloor(origin, direction, floorY);
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

  /** Smaller planes are ignored when picking the lowest plane. */
  private static final float MIN_FLOOR_AREA_M2 = 0.25f;

  /** A locked floor only moves this much. */
  private static final float FLOOR_UPDATE_M = 0.05f;

  /**
   * Floor height from the tracked upward-facing planes.
   *
   * @param planes each entry is {y, area}
   * @param lockedFloorY the locked floor, or null
   */
  @Nullable
  public static Float pickFloorY(
      List<float[]> planes, float cameraY, @Nullable Float lockedFloorY) {
    // Locked floor: only follow planes within 5 cm.
    if (lockedFloorY != null) {
      Float refined =
          largestPlaneY(planes, lockedFloorY - FLOOR_UPDATE_M, lockedFloorY + FLOOR_UPDATE_M);
      return refined != null ? refined : lockedFloorY;
    }
    // Lowest large plane first; a table can be 4-40 cm below a held-up phone.
    Float lowest = lowestPlaneY(planes, cameraY - MIN_CAMERA_HEIGHT_M, MIN_FLOOR_AREA_M2);
    if (lowest != null) return lowest;
    // Else a plane 4-40 cm below the camera: the robot is on it.
    return largestPlaneY(planes, cameraY - MAX_MOUNT_HEIGHT_M, cameraY - MIN_MOUNT_HEIGHT_M);
  }

  @Nullable
  private static Float largestPlaneY(List<float[]> planes, float minY, float maxY) {
    Float largest = null;
    float bestArea = 0f;
    for (float[] plane : planes) {
      if (plane[0] >= minY && plane[0] <= maxY && plane[1] > bestArea) {
        bestArea = plane[1];
        largest = plane[0];
      }
    }
    return largest;
  }

  @Nullable
  private static Float lowestPlaneY(List<float[]> planes, float maxY, float minArea) {
    Float lowest = null;
    for (float[] plane : planes) {
      if (plane[0] <= maxY && plane[1] >= minArea && (lowest == null || plane[0] < lowest)) {
        lowest = plane[0];
      }
    }
    return lowest;
  }

  /** How far a hit may be from the floor and still count as the floor. */
  public static float floorTolerance(HitType hitType, float distanceM) {
    switch (hitType) {
      case UPWARD_PLANE:
        return UPWARD_PLANE_TOLERANCE_M;
      case DEPTH:
        return Math.max(FLOOR_TOLERANCE_M, DEPTH_TOLERANCE_PER_M * distanceM);
      default:
        return FLOOR_TOLERANCE_M;
    }
  }

  /** The point at floor height below the given one. */
  public static float[] onFloor(float[] point, float floorY) {
    return new float[] {point[0], floorY, point[2]};
  }

  /** Whether a tap repeats an earlier one: within 50 px and 3 s. */
  public static boolean isRepeatTap(
      float x0, float y0, long timeMs0, float x, float y, long timeMs) {
    float dx = x - x0;
    float dy = y - y0;
    return timeMs - timeMs0 <= REPEAT_TAP_WINDOW_MS
        && dx * dx + dy * dy <= REPEAT_TAP_RADIUS_PX * REPEAT_TAP_RADIUS_PX;
  }

  private static float distance(float[] a, float[] b) {
    float dx = a[0] - b[0];
    float dy = a[1] - b[1];
    float dz = a[2] - b[2];
    return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public static float horizontalDistance(float[] a, float[] b) {
    float dx = a[0] - b[0];
    float dz = a[2] - b[2];
    return (float) Math.sqrt(dx * dx + dz * dz);
  }
}
