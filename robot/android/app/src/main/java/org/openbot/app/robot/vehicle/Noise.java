package org.openbot.app.robot.vehicle;

import android.os.SystemClock;
import java.util.Random;

public class Noise {

  public Noise(int minDuration, int maxDuration, long timeout) {
    this.minDuration = minDuration;
    this.maxDuration = maxDuration;
    this.timeout = timeout;
  }

  private final int minDuration;
  private final int maxDuration;
  private final long timeout;
  private float value = 0;
  private int direction = 0;
  private long duration = 0;
  private long startTime = 0;

  public void update() {
    long currentTime = SystemClock.elapsedRealtime();
    if (currentTime > startTime + duration + timeout) {
      startTime = currentTime;
      duration = generateRandomInt(minDuration, maxDuration);
      value = 0;
      Random r = new Random();
      direction = r.nextBoolean() ? -1 : 1;
    }
    if (currentTime < startTime + duration) {
      if (currentTime < startTime + duration / 2) {
        value += (float) generateRandomInt(1, 8) / 255;
      } else {
        value -= (float) generateRandomInt(1, 8) / 255;
      }
      value = Math.max(0, Math.min(value, 0.5f));
    } else value = 0;
  }

  public float getValue() {
    return value;
  }

  public int getDirection() {
    return direction;
  }

  private int generateRandomInt(int min, int max) {
    Random r = new Random();
    return r.nextInt((max - min) + 1) + min;
  }

  private float generateRandomFloat() {
    Random r = new Random();
    return r.nextFloat();
  }
}
