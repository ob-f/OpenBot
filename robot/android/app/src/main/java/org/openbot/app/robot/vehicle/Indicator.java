package org.openbot.app.robot.vehicle;

public class Indicator {

  private final int left;
  private final int right;

  public Indicator(int left, int right) {
    this.left = left;
    this.right = right;
  }

  public int getLeft() {
    return left;
  }

  public int getRight() {
    return right;
  }
}
