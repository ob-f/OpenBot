package org.openbot.cartfollow;

import android.graphics.RectF;
import org.openbot.tflite.Detector.Recognition;

public class TargetTrack {
  public final int trackId;
  public RectF lastBbox;
  public RectF previousBbox;
  public Recognition recognition;
  public int ageFrames;
  public int missedFrames;
  public int stableFrames;
  public long lastSeenTimestampMs;
  public long previousSeenTimestampMs = -1L;
  public String matchReason;

  TargetTrack(int trackId, Recognition recognition, long timestampMs) {
    this.trackId = trackId;
    this.recognition = recognition;
    this.lastBbox = recognition == null ? null : new RectF(recognition.getLocation());
    this.previousBbox = null;
    this.ageFrames = 1;
    this.missedFrames = 0;
    this.stableFrames = 1;
    this.lastSeenTimestampMs = timestampMs;
    this.matchReason = "new_track";
  }

  void update(Recognition recognition, long timestampMs, String reason) {
    if (recognition == null || recognition.getLocation() == null) return;
    if (lastBbox != null) previousBbox = new RectF(lastBbox);
    previousSeenTimestampMs = lastSeenTimestampMs;
    lastBbox = new RectF(recognition.getLocation());
    this.recognition = recognition;
    ageFrames++;
    missedFrames = 0;
    stableFrames++;
    lastSeenTimestampMs = timestampMs;
    matchReason = reason;
  }

  void markMissed() {
    ageFrames++;
    missedFrames++;
    stableFrames = 0;
    recognition = null;
    matchReason = "missed";
  }

  public boolean isVisible() {
    return recognition != null && missedFrames == 0 && lastBbox != null;
  }

  public float area() {
    return lastBbox == null ? 0f : Math.max(0f, lastBbox.width()) * Math.max(0f, lastBbox.height());
  }
}
