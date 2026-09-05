package org.openbot.cartfollow;

import android.graphics.RectF;

/** One measured tracking decision shared by perception, both controllers, UI and diagnostics. */
public final class TrackingDecision {
  public enum DetectionLevel {
    HIGH,
    LOW,
    MISSING
  }

  public final long sessionId, frameSequence, observedAtMs;
  public final int trackId, stableFrames, maximumGear;
  public final RectF measuredBox;
  public final DetectionLevel detectionLevel;
  public final boolean associationUnique, motionAllowed, learningAllowed;
  public final String reason;

  public TrackingDecision(
      long session,
      long frame,
      long time,
      int track,
      RectF box,
      boolean high,
      boolean unique,
      int stable,
      boolean move,
      boolean learn,
      String reason) {
    sessionId = session;
    frameSequence = frame;
    observedAtMs = time;
    trackId = track;
    measuredBox = box == null ? null : new RectF(box);
    detectionLevel =
        box == null ? DetectionLevel.MISSING : high ? DetectionLevel.HIGH : DetectionLevel.LOW;
    associationUnique = unique;
    stableFrames = stable;
    motionAllowed = move;
    learningAllowed = learn;
    maximumGear = reason.startsWith("short_recovery_") ? 14 : high ? 21 : 18;
    this.reason = reason;
  }

  public boolean allowsMotion(long now, long maxAge) {
    return motionAllowed
        && associationUnique
        && measuredBox != null
        && measuredBox.width() > 0
        && measuredBox.height() > 0
        && frameSequence > 0
        && now >= observedAtMs
        && now - observedAtMs <= maxAge;
  }

  public boolean matchesFrame(FollowStateMachine.FrameResult frame) {
    return frame != null
        && frame.frameTiming != null
        && sessionId == frame.sessionGeneration
        && frameSequence == frame.frameSequence
        && observedAtMs == frame.frameTiming.receivedAtMs;
  }

  public String label() {
    if (!motionAllowed) return "重捕验证";
    if (reason.startsWith("short_recovery_")) return "短时恢复 · 限速";
    if (detectionLevel == DetectionLevel.LOW) return "检测偏弱";
    return "multi_person_check".equals(reason) ? "多人核验" : "连续跟踪";
  }
}
