package org.openbot.app.robot.pointGoalNavigation;

import com.google.ar.core.TrackingFailureReason;

public interface ArCoreListener {

  void onArCoreUpdate(
      NavigationPoses navigationPoses,
      ImageFrame rgb,
      CameraIntrinsics cameraIntrinsics,
      long timestamp);

  void onArCoreTrackingFailure(long timestamp, TrackingFailureReason trackingFailureReason);

  void onArCoreSessionPaused(long timestamp);

  /** A tapped goal passed all checks; distance is from the robot, in meters. */
  default void onGoalPlaced(float distanceM) {}

  /** A tapped point is not a goal the robot can drive to. */
  default void onGoalRejected(GoalValidator.Result reason) {}
}
