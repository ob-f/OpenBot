package org.openbot.cartfollow;

import org.openbot.vehicle.Control;

public class HumanCommandInterpreter {

  public static final String CMD_FORWARD = "请向前慢走";
  public static final String CMD_FORWARD_LEFT = "请向前并左转";
  public static final String CMD_FORWARD_RIGHT = "请向前并右转";
  public static final String CMD_TURN_LEFT = "请原地左转";
  public static final String CMD_TURN_RIGHT = "请原地右转";
  public static final String CMD_STOP = "请停止";
  public static final String CMD_KEEP = "保持距离";
  public static final String CMD_LOST = "目标丢失，请停止";
  public static final String CMD_TOO_CLOSE = "目标太近，请停止";
  public static final String CMD_DIST_UNKNOWN = "距离不明，请停止";

  public float FORWARD_THRESH = 0.2f;
  public float TURN_THRESH = 0.2f;

  public String interpret(Control control, FollowState state, boolean tooClose) {
    if (state == FollowState.LOST) return CMD_LOST;
    if (tooClose) return CMD_TOO_CLOSE;

    float forward = (control.getLeft() + control.getRight()) / 2f;
    float turn = (control.getRight() - control.getLeft()) / 2f;

    boolean movingForward = forward > FORWARD_THRESH;
    boolean turningLeft = turn < -TURN_THRESH;
    boolean turningRight = turn > TURN_THRESH;

    if (movingForward && !turningLeft && !turningRight) return CMD_FORWARD;
    if (movingForward && turningLeft) return CMD_FORWARD_LEFT;
    if (movingForward && turningRight) return CMD_FORWARD_RIGHT;
    if (turningLeft) return CMD_TURN_LEFT;
    if (turningRight) return CMD_TURN_RIGHT;
    return CMD_STOP;
  }

  public String interpret(Control control, FollowState state, DistanceState distState) {
    if (state == FollowState.LOST) return CMD_LOST;
    if (distState == DistanceState.UNKNOWN) return CMD_DIST_UNKNOWN;
    if (distState == DistanceState.TOO_CLOSE) return CMD_TOO_CLOSE;

    float forward = (control.getLeft() + control.getRight()) / 2f;
    float turn = (control.getRight() - control.getLeft()) / 2f;

    boolean movingForward = forward > FORWARD_THRESH;
    boolean turningLeft = turn < -TURN_THRESH;
    boolean turningRight = turn > TURN_THRESH;

    if (distState == DistanceState.OK) {
      if (turningLeft) return CMD_TURN_LEFT;
      if (turningRight) return CMD_TURN_RIGHT;
      return CMD_KEEP;
    }

    if (movingForward && !turningLeft && !turningRight) return CMD_FORWARD;
    if (movingForward && turningLeft) return CMD_FORWARD_LEFT;
    if (movingForward && turningRight) return CMD_FORWARD_RIGHT;
    if (turningLeft) return CMD_TURN_LEFT;
    if (turningRight) return CMD_TURN_RIGHT;
    return CMD_FORWARD;
  }

  public String interpret(SteeringEvidence evidence, DistanceState distState) {
    if (distState == DistanceState.UNKNOWN) return CMD_DIST_UNKNOWN;
    if (distState == DistanceState.TOO_CLOSE) return CMD_TOO_CLOSE;
    if (evidence == null || !evidence.valid || evidence.direction == SteeringEvidence.Direction.NONE) {
      return distState == DistanceState.TOO_FAR ? CMD_FORWARD : CMD_KEEP;
    }
    String turn =
        "向" + evidence.directionLabel() + evidence.levelLabel() + "转弯 · " + evidence.demandPercent + "%";
    return distState == DistanceState.TOO_FAR ? "请向前并" + turn : "保持距离，" + turn;
  }
}
