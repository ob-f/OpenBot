package org.openbot.app.robot.pointGoalNavigation;

/**
 * Decides when the robot has arrived. Besides being within the goal radius, a robot that is close
 * and has just passed the goal (it's now beside or behind it, or getting further away again) has
 * arrived too: turning back to hit the exact spot only makes it circle around it.
 */
public final class ArrivalCheck {

  /** Within this distance of the goal the robot has arrived. */
  public static final float GOAL_RADIUS_M = 0.10f;

  /** Inside this distance, passing the goal also counts as arriving. */
  public static final float ARRIVAL_ZONE_M = 0.30f;

  /** Moving away by more than this from the closest point means the goal was passed. */
  public static final float MOVING_AWAY_M = 0.03f;

  private float closest = Float.MAX_VALUE;

  public void reset() {
    closest = Float.MAX_VALUE;
  }

  /**
   * @param distance distance to the goal in meters
   * @param deltaYaw angle between heading and goal direction, as from computeDeltaYaw
   */
  public boolean hasArrived(float distance, float deltaYaw) {
    closest = Math.min(closest, distance);
    if (distance < GOAL_RADIUS_M) return true;
    if (distance >= ARRIVAL_ZONE_M) return false;

    boolean goalBesideOrBehind = Math.abs(deltaYaw) > Math.PI / 2;
    boolean movingAway = distance > closest + MOVING_AWAY_M;
    return goalBesideOrBehind || movingAway;
  }
}
