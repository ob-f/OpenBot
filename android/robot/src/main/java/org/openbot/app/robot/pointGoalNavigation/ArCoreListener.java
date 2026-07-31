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
}
