package org.openbot.app.robot.env;

import android.os.SystemClock;

public class SensorReading {

  public SensorReading() {
    this.age = 1000;
    this.timestamp = 0;
    this.reading = 0;
  }

  public long getAge() {
    return age;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public float getReading() {
    return reading;
  }

  public void setReading(float reading) {
    long currentTime = SystemClock.elapsedRealtime();
    if (currentTime > timestamp + 5 && timestamp > 0) this.age = currentTime - this.timestamp;
    this.timestamp = currentTime;
    this.reading = reading;
  }

  private long age;
  private long timestamp;
  private float reading;
}
