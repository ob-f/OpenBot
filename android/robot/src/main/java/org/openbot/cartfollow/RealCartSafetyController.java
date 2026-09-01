package org.openbot.cartfollow;

/** Pure safety gate that converts UI or behavior decisions into bounded protocol commands. */
public final class RealCartSafetyController {
  public enum Mode {
    MANUAL,
    AUTO
  }

  public static final int MANUAL_FORWARD = 14;
  public static final int MANUAL_REVERSE = 12;
  public static final int MANUAL_TURN = 5;
  public static final int AUTO_MAX = 14;
  public static final long INFERENCE_TIMEOUT_MS = 400L;

  public static final class Output {
    public final int left;
    public final int right;
    public final String reason;

    private Output(int left, int right, String reason) {
      this.left = left;
      this.right = right;
      this.reason = reason;
    }

    public boolean isStop() {
      return left == 0 && right == 0;
    }
  }

  private Mode mode = Mode.MANUAL;
  private boolean foreground;
  private boolean connected;
  private boolean firmwareReady;
  private boolean autoUnlocked;
  private boolean autoRunEnabled;
  private boolean autoMotionActive;
  private boolean emergencyLatched;
  private long lastInferenceMs = -1L;
  private final RealCartAutoDriveController autoDriveController = new RealCartAutoDriveController();

  public synchronized void setForeground(boolean foreground) {
    this.foreground = foreground;
    if (!foreground) {
      autoUnlocked = false;
      autoRunEnabled = false;
      autoMotionActive = false;
      autoDriveController.reset("background");
    }
  }

  public synchronized void setConnection(boolean connected, boolean firmwareReady) {
    this.connected = connected;
    this.firmwareReady = firmwareReady;
    if (!connected || !firmwareReady) {
      autoUnlocked = false;
      autoRunEnabled = false;
      autoMotionActive = false;
      autoDriveController.reset("ble_not_ready");
    }
  }

  public synchronized void setMode(Mode mode) {
    this.mode = mode;
    autoUnlocked = false;
    autoRunEnabled = false;
    autoMotionActive = false;
    lastInferenceMs = -1L;
    autoDriveController.reset("mode_change");
  }

  public synchronized Mode getMode() {
    return mode;
  }

  public synchronized boolean unlockAuto() {
    autoUnlocked =
        mode == Mode.AUTO && foreground && connected && firmwareReady && !emergencyLatched;
    return autoUnlocked;
  }

  public synchronized boolean isAutoUnlocked() {
    return autoUnlocked;
  }

  public synchronized void setAutoRunEnabled(boolean enabled, long nowMs) {
    autoRunEnabled = enabled;
    autoMotionActive = false;
    lastInferenceMs = enabled ? nowMs : -1L;
    autoDriveController.reset(enabled ? "start_arming" : "start_off");
  }

  public synchronized void latchEmergency() {
    emergencyLatched = true;
    autoUnlocked = false;
    autoRunEnabled = false;
    autoMotionActive = false;
    autoDriveController.reset("emergency_stop");
  }

  public synchronized boolean isEmergencyLatched() {
    return emergencyLatched;
  }

  public synchronized Output manual(int left, int right) {
    if (!canMove() || mode != Mode.MANUAL) return stop("manual_blocked");
    return new Output(left, right, "manual");
  }

  public synchronized Output auto(FollowStateMachine.FrameResult frame, long nowMs) {
    lastInferenceMs = nowMs;
    if (!canMove() || mode != Mode.AUTO || !autoUnlocked || !autoRunEnabled || frame == null) {
      autoMotionActive = false;
      return stop("auto_blocked");
    }

    BehaviorDecisionResult decision = frame.behaviorDecision;
    if (decision == null) {
      autoMotionActive = false;
      return stop("decision_missing");
    }
    RealCartAutoDriveController.Result result = autoDriveController.update(frame, nowMs);
    if (result.lockout) autoUnlocked = false;
    if (result.lockout) autoRunEnabled = false;
    autoMotionActive = result.left != 0 || result.right != 0;
    return new Output(result.left, result.right, result.reason);
  }

  public synchronized Output watchdog(long nowMs) {
    if (mode == Mode.AUTO
        && autoUnlocked
        && autoRunEnabled
        && autoMotionActive
        && nowMs - lastInferenceMs > INFERENCE_TIMEOUT_MS) {
      autoUnlocked = false;
      autoRunEnabled = false;
      autoMotionActive = false;
      autoDriveController.reset("inference_timeout");
      return stop("inference_timeout");
    }
    return null;
  }

  public static Output stop(String reason) {
    return new Output(0, 0, reason);
  }

  public synchronized Output resetAutoDrive(String reason, boolean revokeUnlock) {
    if (revokeUnlock) autoUnlocked = false;
    autoRunEnabled = false;
    autoMotionActive = false;
    autoDriveController.reset(reason);
    lastInferenceMs = -1L;
    return stop(reason);
  }

  public synchronized RealCartAutoDriveController.Result getAutoDriveResult() {
    return autoDriveController.getLastResult();
  }

  public synchronized void setSteeringStrengthPercent(int percent) {
    autoDriveController.setSteeringStrengthPercent(percent);
  }

  public synchronized int getSteeringStrengthPercent() {
    return autoDriveController.getSteeringStrengthPercent();
  }

  private boolean canMove() {
    return foreground && connected && firmwareReady && !emergencyLatched;
  }
}
