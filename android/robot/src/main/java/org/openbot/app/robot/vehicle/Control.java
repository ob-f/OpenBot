package org.openbot.app.robot.vehicle;

public class Control {
  private final float left;
  private final float right;

  public Control(float left, float right) {
    this.left = Math.max(-1.f, Math.min(1.f, left));
    this.right = Math.max(-1.f, Math.min(1.f, right));
  }

  public float getLeft() {
    return left;
  }

  public float getRight() {
    return right;
  }

  public Control mirror() {
    return new Control(this.right, this.left);
  }
}
