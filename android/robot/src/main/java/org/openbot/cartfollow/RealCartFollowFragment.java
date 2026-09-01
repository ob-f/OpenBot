package org.openbot.cartfollow;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.navigation.Navigation;
import org.openbot.BuildConfig;
import org.openbot.R;
import org.openbot.vehicle.Control;

/** Camera-based cart following with BLE manual control and guarded experimental autonomy. */
public class RealCartFollowFragment extends BaseCartFollowFragment {
  private static final String CONTROL_LOG_TAG = "CartControl";
  private static final String SESSION_LOG_TAG = "CartFollow_Session";
  private static final long COMMAND_REPEAT_MS = 100L;
  private static final long HANDSHAKE_RETRY_MS = 500L;
  private static final long AUTO_UNLOCK_HOLD_MS = 2000L;
  private static final long AUTO_LOG_INTERVAL_MS = 250L;
  private static final int REAL_CART_PREDICTION_HORIZON_MS = 400;

  private final RealCartSafetyController safetyController = new RealCartSafetyController();
  private final ManualTouchRouter manualTouchRouter =
      new ManualTouchRouter(new ManualControlArbiter());
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private volatile RealCartSafetyController.Output latestOutput =
      RealCartSafetyController.stop("idle");
  private boolean schedulerRunning;
  private long lastHandshakeRequestMs;
  private long lastAutoLogMs;
  private RealCartAutoDriveController.Phase lastLoggedAutoPhase;
  private View activeManualButton;
  private String lastSessionEndReason = "none";

  private final Runnable commandScheduler =
      new Runnable() {
        @Override
        public void run() {
          if (!schedulerRunning || binding == null) return;
          updateConnectionState();
          long now = SystemClock.elapsedRealtime();
          if (vehicle.isBleSerialReady()
              && !vehicle.isCartFirmwareReady()
              && now - lastHandshakeRequestMs >= HANDSHAKE_RETRY_MS) {
            vehicle.requestVehicleConfig();
            lastHandshakeRequestMs = now;
          }
          RealCartSafetyController.Output watchdog = safetyController.watchdog(now);
          if (watchdog != null) {
            latestOutput = watchdog;
            finishAutoSession("inference_timeout", false);
          }
          if (safetyController.getMode() == RealCartSafetyController.Mode.MANUAL
              && manualTouchRouter.getActiveControl() == null) {
            latestOutput = RealCartSafetyController.stop("manual_idle");
          }
          sendOutput(latestOutput);
          refreshRealUi();
          mainHandler.postDelayed(this, COMMAND_REPEAT_MS);
        }
      };

  @Override
  protected void onCartFollowViewCreated() {
    // The real cart owns its missing-person timeout. Visible people keep a stationary ReID session
    // alive, while two seconds with no person ends it in RealCartAutoDriveController.
    stateMachine.IDENTITY_UNCERTAIN_TIMEOUT_MS = Long.MAX_VALUE;
    stateMachine.SEARCH_TIMEOUT_MS = Long.MAX_VALUE;
    vehicle.useBluetoothConnection();
    binding.realControlPanel.setVisibility(View.VISIBLE);
    binding.steeringPanel.setVisibility(View.VISIBLE);
    binding.predictionHorizonGroup.setVisibility(View.GONE);
    updateSteeringUi(SteeringEvidence.unavailable("idle", REAL_CART_PREDICTION_HORIZON_MS));
    binding.realModeGroup.check(R.id.real_mode_manual);
    binding.startSwitch.setChecked(false);
    binding.startSwitch.setEnabled(false);

    binding.realModeGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) return;
          setMode(
              checkedId == R.id.real_mode_auto
                  ? RealCartSafetyController.Mode.AUTO
                  : RealCartSafetyController.Mode.MANUAL);
        });

    installManualTouchRouter();

    binding.connectBle.setOnClickListener(
        v -> Navigation.findNavController(requireView()).navigate(R.id.open_bluetooth_fragment));
    installAutoUnlock();
    binding
        .getRoot()
        .getViewTreeObserver()
        .addOnWindowFocusChangeListener(
            hasFocus -> {
              if (!hasFocus && binding != null) {
                if (safetyController.getMode() == RealCartSafetyController.Mode.MANUAL) {
                  invalidateManualControl("window_focus_lost", true);
                } else {
                  finishAutoSession("window_focus_lost", true);
                }
              }
            });
    binding.emergencyStop.setOnClickListener(
        v -> {
          lastSessionEndReason = "emergency_stop";
          logSession("end", lastSessionEndReason);
          safetyController.latchEmergency();
          invalidateManualControl("emergency_stop", true);
          vehicle.emergencyStop();
          binding.startSwitch.setChecked(false);
          binding.startSwitch.setEnabled(false);
          resetFollowSession();
          refreshRealUi();
        });
    setMode(RealCartSafetyController.Mode.MANUAL);
  }

  @Override
  public synchronized void onResume() {
    super.onResume();
    safetyController.setForeground(true);
    if (vehicle.isBleSerialReady()) vehicle.startHeartbeat();
    startScheduler();
  }

  @Override
  protected void onCartFollowPause() {
    if (binding != null
        && safetyController.getMode() == RealCartSafetyController.Mode.AUTO
        && binding.startSwitch.isChecked()) {
      lastSessionEndReason = "paused";
      logSession("end", lastSessionEndReason);
    }
    safetyController.setForeground(false);
    invalidateManualControl("paused", true);
    vehicle.stopHeartbeat();
    schedulerRunning = false;
    mainHandler.removeCallbacks(commandScheduler);
    if (binding != null) {
      binding.startSwitch.setChecked(false);
      binding.startSwitch.setEnabled(false);
      resetFollowSession();
    }
  }

  @Override
  protected void onDiagnosticLoggingChanged(boolean enabled) {
    if (vehicle != null) vehicle.setBleControlDiagnosticsEnabled(enabled);
  }

  @Override
  protected void onFollowEnabledChanged(boolean enabled) {
    if (enabled) {
      lastSessionEndReason = "none";
      logSession("start", "enabled");
      safetyController.setAutoRunEnabled(true, SystemClock.elapsedRealtime());
      refreshRealUi();
      return;
    }
    latestOutput = safetyController.resetAutoDrive("start_off", false);
    lastSessionEndReason = "user_start_off";
    logSession("end", lastSessionEndReason);
    sendOutput(latestOutput);
    refreshRealUi();
  }

  @Override
  protected boolean isInferenceEnabled() {
    return safetyController.getMode() == RealCartSafetyController.Mode.AUTO
        && safetyController.isAutoUnlocked()
        && binding.startSwitch.isChecked();
  }

  @Override
  protected int steeringPredictionHorizonMs() {
    return REAL_CART_PREDICTION_HORIZON_MS;
  }

  @Override
  protected void onFollowFrame(FollowStateMachine.FrameResult frameResult) {
    latestOutput = safetyController.auto(frameResult, SystemClock.elapsedRealtime());
    logAutoDecision(frameResult);
    RealCartAutoDriveController.Result autoResult = safetyController.getAutoDriveResult();
    updateCommandText(commandForAutoResult(autoResult));
    updateSteeringUi(frameResult == null ? null : frameResult.steeringEvidence);
    if (autoResult.lockout) finishAutoSession(autoResult.reason, false);
  }

  @Override
  protected void onFollowSessionReset() {
    updateSteeringUi(
        SteeringEvidence.unavailable("session_reset", REAL_CART_PREDICTION_HORIZON_MS));
  }

  @Override
  protected void onInferenceFailure(RuntimeException error) {
    latestOutput = safetyController.resetAutoDrive("inference_failure", true);
    sendOutput(latestOutput);
    finishAutoSession("inference_failure", true);
  }

  @Override
  protected SystemSafetyEvidence createSystemSafetyEvidence() {
    boolean communicationReady = vehicle != null && vehicle.isCartFirmwareReady();
    return new SystemSafetyEvidence(
        safetyController.isEmergencyLatched(),
        communicationReady,
        isDetectorReady(),
        communicationReady ? (isDetectorReady() ? "ok" : "detector_not_ready") : "ble_not_ready");
  }

  @Override
  protected void processUSBData(String data) {
    updateConnectionState();
  }

  private void setMode(RealCartSafetyController.Mode mode) {
    invalidateManualControl("mode_change", true);
    safetyController.setMode(mode);
    binding.startSwitch.setChecked(false);
    resetFollowSession();
    boolean auto = mode == RealCartSafetyController.Mode.AUTO;
    binding.manualDriveControls.setVisibility(auto ? View.GONE : View.VISIBLE);
    binding.unlockAuto.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.realSafetyNotice.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.startSwitch.setEnabled(false);
    refreshRealUi();
  }

  private void installManualTouchRouter() {
    binding.driveForward.setClickable(false);
    binding.driveBackward.setClickable(false);
    binding.driveLeft.setClickable(false);
    binding.driveRight.setClickable(false);
    binding.manualDriveControls.setClickable(true);
    binding.manualDriveControls.setOnTouchListener((view, event) -> handleManualTouch(event));
  }

  private boolean handleManualTouch(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        int downIndex = event.getActionIndex();
        int downPointerId = event.getPointerId(downIndex);
        ManualControlArbiter.Control downControl =
            findManualControl(event.getX(downIndex), event.getY(downIndex));
        if (downControl != null) handleManualPress(downControl, downPointerId, "down");
        return true;
      case MotionEvent.ACTION_MOVE:
        int activePointerId = manualTouchRouter.getActivePointerId();
        int activeIndex = event.findPointerIndex(activePointerId);
        if (activeIndex >= 0) {
          ManualControlArbiter.Control moveControl =
              findManualControl(event.getX(activeIndex), event.getY(activeIndex));
          ManualControlArbiter.PressResult moveResult =
              manualTouchRouter.move(moveControl, activePointerId);
          if (moveResult != null) {
            handleManualDirection(moveControl, activePointerId, moveResult, "move");
          }
        }
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        int upPointerId = event.getPointerId(event.getActionIndex());
        logTouch("up", upPointerId, manualTouchRouter.getActiveControl());
        if (manualTouchRouter.release(upPointerId)) {
          clearManualButtonState();
          latestOutput = RealCartSafetyController.stop("manual_release");
          sendOutput(latestOutput);
        }
        return true;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_OUTSIDE:
        logTouch(
            "cancel", manualTouchRouter.getActivePointerId(), manualTouchRouter.getActiveControl());
        invalidateManualControl("manual_cancel", true);
        return true;
      default:
        return true;
    }
  }

  private void handleManualPress(
      ManualControlArbiter.Control control, int pointerId, String eventName) {
    ManualControlArbiter.PressResult result = manualTouchRouter.press(control, pointerId);
    handleManualDirection(control, pointerId, result, eventName);
  }

  private void handleManualDirection(
      ManualControlArbiter.Control control,
      int pointerId,
      ManualControlArbiter.PressResult pressResult,
      String eventName) {
    RealCartSafetyController.Output nextOutput = manualOutput(control);
    logTouch(eventName, pointerId, control);
    if (nextOutput.isStop()) {
      invalidateManualControl("manual_blocked", true);
      return;
    }

    clearManualButtonState();
    activeManualButton = buttonForControl(control);
    if (activeManualButton != null) activeManualButton.setPressed(true);
    latestOutput = nextOutput;
    if (pressResult.replacedActiveControl) {
      sendReplacementOutput(nextOutput, pressResult.generation);
    } else {
      sendOutput(nextOutput);
    }
  }

  private RealCartSafetyController.Output manualOutput(ManualControlArbiter.Control control) {
    switch (control) {
      case FORWARD:
        return safetyController.manual(
            RealCartSafetyController.MANUAL_FORWARD, RealCartSafetyController.MANUAL_FORWARD);
      case BACKWARD:
        return safetyController.manual(
            -RealCartSafetyController.MANUAL_REVERSE, -RealCartSafetyController.MANUAL_REVERSE);
      case LEFT:
        return safetyController.manual(
            -RealCartSafetyController.MANUAL_TURN, RealCartSafetyController.MANUAL_TURN);
      case RIGHT:
        return safetyController.manual(
            RealCartSafetyController.MANUAL_TURN, -RealCartSafetyController.MANUAL_TURN);
      default:
        return RealCartSafetyController.stop("manual_unknown");
    }
  }

  private ManualControlArbiter.Control findManualControl(float x, float y) {
    if (contains(binding.driveLeft, x, y)) return ManualControlArbiter.Control.LEFT;
    if (contains(binding.driveForward, x, y)) return ManualControlArbiter.Control.FORWARD;
    if (contains(binding.driveBackward, x, y)) return ManualControlArbiter.Control.BACKWARD;
    if (contains(binding.driveRight, x, y)) return ManualControlArbiter.Control.RIGHT;
    return null;
  }

  private static boolean contains(View child, float x, float y) {
    return child.getVisibility() == View.VISIBLE
        && x >= child.getLeft()
        && x < child.getRight()
        && y >= child.getTop()
        && y < child.getBottom();
  }

  private View buttonForControl(ManualControlArbiter.Control control) {
    switch (control) {
      case FORWARD:
        return binding.driveForward;
      case BACKWARD:
        return binding.driveBackward;
      case LEFT:
        return binding.driveLeft;
      case RIGHT:
        return binding.driveRight;
      default:
        return null;
    }
  }

  private void clearManualButtonState() {
    if (activeManualButton != null) activeManualButton.setPressed(false);
    activeManualButton = null;
  }

  private void installAutoUnlock() {
    final Runnable unlock =
        () -> {
          if (binding != null && binding.unlockAuto.isPressed() && safetyController.unlockAuto()) {
            binding.startSwitch.setEnabled(true);
            refreshRealUi();
          }
        };
    binding.unlockAuto.setOnTouchListener(
        (view, event) -> {
          if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            view.setPressed(true);
            mainHandler.postDelayed(unlock, AUTO_UNLOCK_HOLD_MS);
          } else if (event.getActionMasked() == MotionEvent.ACTION_UP
              || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            view.setPressed(false);
            mainHandler.removeCallbacks(unlock);
          }
          return true;
        });
  }

  private void updateConnectionState() {
    boolean serialReady = vehicle != null && vehicle.isBleSerialReady();
    boolean firmwareReady = vehicle != null && vehicle.isCartFirmwareReady();
    safetyController.setConnection(serialReady, firmwareReady);
    if (!firmwareReady) {
      invalidateManualControl("ble_not_ready", false);
      if (safetyController.getMode() == RealCartSafetyController.Mode.AUTO
          && binding.startSwitch.isChecked()) {
        finishAutoSession("ble_not_ready", false);
      }
    }
  }

  private void finishAutoSession(String reason, boolean revokeUnlock) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      mainHandler.post(() -> finishAutoSession(reason, revokeUnlock));
      return;
    }
    if (binding == null || safetyController.getMode() != RealCartSafetyController.Mode.AUTO) return;
    lastSessionEndReason = reason;
    logSession("end", reason);
    latestOutput = safetyController.resetAutoDrive(reason, revokeUnlock);
    sendOutput(latestOutput);
    if (binding.startSwitch.isChecked()) binding.startSwitch.setChecked(false);
    binding.startSwitch.setEnabled(false);
    resetFollowSession();
    refreshRealUi();
  }

  private void invalidateManualControl(String reason, boolean sendStop) {
    logControl(
        "invalidate",
        "reason="
            + reason
            + ",generation="
            + manualTouchRouter.getGeneration()
            + ",direction="
            + directionName(manualTouchRouter.getActiveControl()));
    manualTouchRouter.clear();
    clearManualButtonState();
    latestOutput = RealCartSafetyController.stop(reason);
    if (sendStop) sendOutput(latestOutput);
  }

  private void startScheduler() {
    if (schedulerRunning) return;
    schedulerRunning = true;
    mainHandler.post(commandScheduler);
  }

  private void sendOutput(RealCartSafetyController.Output output) {
    if (vehicle == null || output == null) return;
    int multiplier = Math.max(1, vehicle.getSpeedMultiplier());
    vehicle.setControl(
        new Control(output.left / (float) multiplier, output.right / (float) multiplier));
  }

  private void sendReplacementOutput(RealCartSafetyController.Output output, long generation) {
    if (vehicle == null || output == null) return;
    int multiplier = Math.max(1, vehicle.getSpeedMultiplier());
    vehicle.setControlReplacing(
        new Control(output.left / (float) multiplier, output.right / (float) multiplier),
        generation);
  }

  private void logTouch(String event, int pointerId, ManualControlArbiter.Control control) {
    logControl(
        "touch_" + event,
        "pointer="
            + pointerId
            + ",generation="
            + manualTouchRouter.getGeneration()
            + ",direction="
            + directionName(control));
  }

  private void logControl(String event, String details) {
    if (!isDiagnosticLoggingEnabled()) return;
    Log.i(
        CONTROL_LOG_TAG, "ms=" + SystemClock.elapsedRealtime() + ",event=" + event + "," + details);
  }

  private void logSession(String event, String reason) {
    Log.i(
        SESSION_LOG_TAG,
        "ms="
            + SystemClock.elapsedRealtime()
            + ",event="
            + event
            + ",reason="
            + reason);
  }

  static String commandForAutoResult(RealCartAutoDriveController.Result result) {
    if (result == null) return "自动控制未就绪";
    switch (result.phase) {
      case MOVING_STRAIGHT:
        return "小车直行";
      case CURVE_LEFT:
        return String.format(
            java.util.Locale.US,
            "小车向左%s缓弯 · %d%% · 输出 %d,%d",
            result.levelLabel(),
            result.demandPercent,
            result.left,
            result.right);
      case CURVE_RIGHT:
        return String.format(
            java.util.Locale.US,
            "小车向右%s缓弯 · %d%% · 输出 %d,%d",
            result.levelLabel(),
            result.demandPercent,
            result.left,
            result.right);
      case WAIT_CENTER:
        return "目标偏差过大，停车等待";
      case RECOVERY_STOP:
        return "身份确认中，停车重捕";
      case WAIT_TARGET:
        return "等待有效目标，保持停车";
      case LOCKED:
      default:
        return "自动控制已锁定";
    }
  }

  private void logAutoDecision(FollowStateMachine.FrameResult frame) {
    if (!isDiagnosticLoggingEnabled()) return;
    long now = SystemClock.elapsedRealtime();
    RealCartAutoDriveController.Result result = safetyController.getAutoDriveResult();
    if (result == null) return;
    boolean phaseChanged = result.phase != lastLoggedAutoPhase;
    if (!phaseChanged && now - lastAutoLogMs < AUTO_LOG_INTERVAL_MS) return;
    lastAutoLogMs = now;
    lastLoggedAutoPhase = result.phase;
    String action =
        frame == null || frame.behaviorDecision == null
            ? "NONE"
            : frame.behaviorDecision.selectedAction.name();
    logControl(
        "auto_decision",
        "phase="
            + result.phase
            + ",action="
            + action
            + ",raw_turn="
            + result.rawTurn
            + ",filtered_turn="
            + result.filteredTurn
            + ",steering_direction="
            + result.direction
            + ",steering_demand="
            + result.demandPercent
            + ",steering_level="
            + result.level
            + ",height_scale="
            + result.heightScale
            + ",output="
            + result.left
            + ","
            + result.right
            + ",reason="
            + result.reason);
  }

  private static String directionName(ManualControlArbiter.Control control) {
    return control == null ? "NONE" : control.name();
  }

  private void refreshRealUi() {
    if (binding == null) return;
    requireActivity()
        .runOnUiThread(
            () -> {
              if (binding == null) return;
              String connection =
                  vehicle.isCartFirmwareReady()
                      ? "BLE 已就绪 · CART_AT8236"
                      : vehicle.isBleSerialReady() ? "BLE 已连接 · 等待固件握手" : "BLE 未连接";
              String output =
                  latestOutput == null ? "0,0" : latestOutput.left + "," + latestOutput.right;
              RealCartAutoDriveController.Result autoResult = safetyController.getAutoDriveResult();
              ManualControlArbiter.Control active = manualTouchRouter.getActiveControl();
              binding.realConnectionStatus.setText(
                  connection
                      + " | output="
                      + output
                      + " | direction="
                      + (active == null ? "STOP" : active.name())
                      + " | ble="
                      + vehicle.getBleWriteStatus()
                      + (safetyController.getMode() == RealCartSafetyController.Mode.AUTO
                          ? " | auto="
                              + autoResult.phase
                              + " h="
                              + String.format(java.util.Locale.US, "%.2f", autoResult.heightScale)
                              + " turn="
                              + String.format(java.util.Locale.US, "%.2f", autoResult.filteredTurn)
                              + " demand="
                              + autoResult.demandPercent
                              + " dir="
                              + autoResult.direction
                              + " reason="
                              + autoResult.reason
                              + " last_end="
                              + lastSessionEndReason
                          : "")
                      + " | build="
                      + BuildConfig.VERSION_NAME);
              boolean emergency = safetyController.isEmergencyLatched();
              binding.emergencyStop.setEnabled(!emergency);
              binding.unlockAuto.setEnabled(!emergency && vehicle.isCartFirmwareReady());
              if (emergency) {
                binding.realSafetyNotice.setVisibility(View.VISIBLE);
                binding.realSafetyNotice.setText("急停已锁存，请重启 ESP32 后重新连接");
              } else {
                binding.realSafetyNotice.setText("近场传感器未接入，仅限空旷实验");
              }
            });
  }

  private void updateSteeringUi(SteeringEvidence evidence) {
    if (binding == null || getActivity() == null) return;
    SteeringEvidence safeEvidence =
        evidence == null
            ? SteeringEvidence.unavailable("idle", REAL_CART_PREDICTION_HORIZON_MS)
            : evidence;
    getActivity()
        .runOnUiThread(
            () -> {
              if (binding == null) return;
              binding.steeringGauge.setEvidence(safeEvidence);
              if (!safeEvidence.valid) {
                binding.steeringSummary.setText("转向需求等待可信跟随目标");
              } else if (safeEvidence.direction == SteeringEvidence.Direction.NONE) {
                binding.steeringSummary.setText(
                    String.format(
                        java.util.Locale.US,
                        "实际转向：直行 · 需求 %d%%\n预测提前 %d ms",
                        safeEvidence.demandPercent, safeEvidence.predictionHorizonMs));
              } else {
                binding.steeringSummary.setText(
                    String.format(
                        java.util.Locale.US,
                        "实际转向：向%s%s缓弯 · %d%%\n预测提前 %d ms",
                        safeEvidence.directionLabel(),
                        safeEvidence.levelLabel(),
                        safeEvidence.demandPercent,
                        safeEvidence.predictionHorizonMs));
              }
            });
  }
}
