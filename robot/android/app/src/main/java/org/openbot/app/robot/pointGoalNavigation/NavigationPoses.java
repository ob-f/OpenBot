package org.openbot.app.robot.pointGoalNavigation;

import com.google.ar.core.Pose;

public class NavigationPoses {

  private Pose currentPose;
  private Pose targetPose;
  private Pose referencePose;

  public NavigationPoses(Pose currentPose, Pose targetPose, Pose referencePose) {
    this.currentPose = currentPose;
    this.targetPose = targetPose;
    this.referencePose = referencePose;
  }

  public Pose getCurrentPose() {
    return currentPose;
  }

  public Pose getTargetPose() {
    return targetPose;
  }

  public Pose getReferencePose() {
    return referencePose;
  }
}
