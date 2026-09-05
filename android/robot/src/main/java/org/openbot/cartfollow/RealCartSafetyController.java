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
  public static final int AUTO_MAX = 21;
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
  private long generation;
  private long lastSequence = -1L;
  private FollowStateMachine.FrameResult lastFrame;
  private Output currentAuto = stop("idle");
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
    lastFrame = null;
    lastSequence = -1;
    currentAuto = stop(enabled ? "start_arming" : "start_off");
  }

  public synchronized void setSessionGeneration(long generation) {
    this.generation = generation;
    lastSequence = -1L;
    lastFrame = null;
    autoMotionActive = false;
    currentAuto = stop("session_changed");
    autoDriveController.reset("session_changed");
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

  /** Kept for callers that provide a monotonic timestamp; range telemetry is observation-only. */
  public synchronized Output manual(int left, int right, long nowMs) {
    return manual(left, right);
  }

  public synchronized Output auto(FollowStateMachine.FrameResult frame, long nowMs) {
    return auto(frame, nowMs, null);
  }

  public synchronized Output auto(
      FollowStateMachine.FrameResult frame, long nowMs, RealCartSearchController.Result search) {
    if (!canMove() || mode != Mode.AUTO || !autoUnlocked || !autoRunEnabled || frame == null) {
      autoMotionActive = false;
      return stop("auto_blocked");
    }
    if (frame.sessionGeneration != generation || frame.frameSequence <= lastSequence)
      return refresh(nowMs, search);
    lastSequence = frame.frameSequence;
    if (frame.frameTiming == null
        || nowMs < frame.frameTiming.receivedAtMs
        || nowMs - frame.frameTiming.receivedAtMs > INFERENCE_TIMEOUT_MS) {
      if (autoMotionActive) return fault("inference_timeout");
      autoDriveController.reset("frame_stale");
      return currentAuto = stop("frame_stale");
    }
    lastInferenceMs = frame.frameTiming.receivedAtMs;
    lastFrame = frame;
    if (frame.state == FollowState.STOP || frame.state == FollowState.IDLE) {
      autoMotionActive = false;
      autoDriveController.reset("follow_inactive");
      return currentAuto = stop("follow_inactive");
    }
    BehaviorDecisionResult decision = frame.behaviorDecision;
    if (decision == null) {
      autoMotionActive = false;
      autoDriveController.reset("decision_missing");
      return currentAuto = stop("decision_missing");
    }
    if (decision.selectedAction == BehaviorAction.HARD_STOP
        || decision.selectedAction == BehaviorAction.EMERGENCY_STOP)
      return fault(decision.actionReason);
    if (decision.selectedAction == BehaviorAction.BLOCKED_WAIT) {
      autoMotionActive = false;
      autoDriveController.reset("blocked_wait");
      return currentAuto = stop("blocked_wait");
    }
    if (search != null && search.lockout) return fault(search.reason);
    RealCartAutoDriveController.Result result =
        search != null
                && search.overridesFollow()
                && search.generation == generation
                && search.frameSequence == lastSequence
            ? autoDriveController.search(search)
            : autoDriveController.update(frame, nowMs);
    if (result.lockout) autoUnlocked = false;
    if (result.lockout) autoRunEnabled = false;
    autoMotionActive = result.left != 0 || result.right != 0;
    return currentAuto = new Output(result.left, result.right, result.reason);
  }

  /** Called immediately before every scheduled write, not just after inference. */
  public synchronized Output refresh(long nowMs, RealCartSearchController.Result search) {
    if (!canMove() || mode != Mode.AUTO || !autoUnlocked || !autoRunEnabled)
      return currentAuto = stop("auto_blocked");
    if (search != null && search.lockout) return fault(search.reason);
    Output timeout = watchdog(nowMs);
    if (timeout != null) return timeout;
    if (lastFrame == null || lastFrame.sessionGeneration != generation)
      return currentAuto = stop("awaiting_current_session");
    if (lastFrame.state == FollowState.STOP || lastFrame.state == FollowState.IDLE)
      return currentAuto = stop("follow_inactive");
    if (lastFrame.behaviorDecision == null
        || lastFrame.behaviorDecision.selectedAction == BehaviorAction.BLOCKED_WAIT)
      return currentAuto = stop("blocked_wait");
    if (nowMs < lastInferenceMs || nowMs - lastInferenceMs > INFERENCE_TIMEOUT_MS)
      return currentAuto = stop("frame_stale");
    if (search != null
        && search.overridesFollow()
        && (search.generation != generation || search.frameSequence != lastSequence)) {
      autoDriveController.reset("awaiting_current_decision");
      return currentAuto = stop("awaiting_current_decision");
    }
    if (search != null && search.overridesFollow()) {
      RealCartAutoDriveController.Result result = autoDriveController.search(search);
      autoMotionActive = !result.isStop();
      return currentAuto = new Output(result.left, result.right, result.reason);
    }
    if (!currentAuto.isStop()
        && (lastFrame.simulatorIdentity == null
            || !lastFrame.simulatorIdentity.allowsForward(nowMs))) {
      autoMotionActive = false;
      autoDriveController.reset("identity_unverified");
      return currentAuto = stop("identity_unverified");
    }
    RealCartAutoDriveController.Result timed = autoDriveController.pollAim(nowMs);
    if (timed.isStop() && !currentAuto.isStop()) {
      autoMotionActive = false;
      return currentAuto = stop(timed.reason);
    }
    return currentAuto;
  }

  private Output fault(String reason) {
    autoUnlocked = autoRunEnabled = autoMotionActive = false;
    autoDriveController.reset(reason);
    return currentAuto = stop(reason);
  }

  public synchronized Output watchdog(long nowMs) {
    if (mode == Mode.AUTO
        && autoUnlocked
        && autoRunEnabled
        && autoMotionActive
        && nowMs - lastInferenceMs > INFERENCE_TIMEOUT_MS) {
      return fault("inference_timeout");
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
    lastFrame = null;
    lastSequence = -1L;
    return currentAuto = stop(reason);
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

  public synchronized void setMaximumGear(int value) {
    autoDriveController.setMaximumGear(value);
  }

  public synchronized int getMaximumGear() {
    return autoDriveController.getMaximumGear();
  }

  private boolean canMove() {
    return foreground && connected && firmwareReady && !emergencyLatched;
  }
}
