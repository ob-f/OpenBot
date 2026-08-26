package org.openbot.app.robot.utils;

public class MovingAverage {
  private long[] window;
  private int n, insert;
  private long sum;

  public MovingAverage(int size) {
    window = new long[size];
    insert = 0;
    sum = 0;
  }

  public double next(long val) {
    if (n < window.length) n++;
    sum -= window[insert];
    sum += val;
    window[insert] = val;
    insert = (insert + 1) % window.length;
    return (double) sum / n;
  }
}
