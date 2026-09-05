package org.openbot.cartfollow;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.navigation.Navigation;
import org.openbot.BuildConfig;
import org.openbot.R;
import org.openbot.vehicle.Control;
import org.openbot.vehicle.RangeTelemetrySnapshot;

/** Camera-based cart following with BLE manual control and guarded experimental autonomy. */
public class RealCartFollowFragment extends BaseCartFollowFragment implements SensorEventListener {
  private static final String CONTROL_LOG_TAG = "CartControl";
  private static final String SESSION_LOG_TAG = "CartFollow_Session";
  private static final long COMMAND_REPEAT_MS = 100L;
  private static final long HANDSHAKE_RETRY_MS = 500L;
  private static final long AUTO_UNLOCK_HOLD_MS = 2000L;
  private static final long AUTO_LOG_INTERVAL_MS = 250L;
  private static final long RANGE_STALE_MS = 250L;
  private static final int REAL_CART_PREDICTION_HORIZON_MS = 0;
  private static final String TUNING_PREFS = "real_cart_steering_tuning";
  private static final String TUNING_STRENGTH_KEY = "strength_percent";
  private static final String MANUAL_SPEED_KEY = "manual_forward_logical";

  private final RealCartSafetyController safetyController = new RealCartSafetyController();
  private final ManualTouchRouter manualTouchRouter =
      new ManualTouchRouter(new ManualControlArbiter());
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private volatile RealCartSafetyController.Output latestOutput =
      RealCartSafetyController.stop("idle");
  private boolean schedulerRunning;
  private long lastHandshakeRequestMs;
  private long lastAutoLogMs;
  private String lastRangeStateKey = "";
  private long lastFirmwareErrorAtMs = -1L;
  private volatile org.openbot.cartfollow.diagnostics.CartFollowDiagnosticSession
      activeDiagnosticSession;
  private RealCartAutoDriveController.Phase lastLoggedAutoPhase;
  private View activeManualButton;
  private String lastSessionEndReason = "none";
  private SteeringTuningRecorder tuningRecorder;
  private int manualForwardLogical = ManualSpeedProfile.DEFAULT_FORWARD_LOGICAL;
  private RealFollowSettings followSettings = new RealFollowSettings();
  private final RealCartSearchController searchController = new RealCartSearchController();
  private final YawTurnTracker yaw = new YawTurnTracker();
  private final YawTurnTracker testYaw = new YawTurnTracker();
  private boolean testingYaw;
  private boolean searchEnabled;
  private SensorManager sensorManager;
  private Sensor gyroscope;
  private Sensor gravity;
  private FollowStateMachine.FrameResult presentedFrame;
  private boolean compactAutoLayout;
  private View[] compactViews;
  private android.view.ViewGroup[] compactParents;
  private android.view.ViewGroup.LayoutParams[] compactParams;
  private int[] compactIndices;
  private androidx.constraintlayout.widget.ConstraintLayout.LayoutParams normalScrollParams;

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
          } else if (safetyController.getMode() == RealCartSafetyController.Mode.MANUAL) {
            latestOutput = manualOutput(manualTouchRouter.getActiveControl(), now);
          }
          if (safetyController.getMode() == RealCartSafetyController.Mode.AUTO) {
            latestOutput = safetyController.refresh(now, searchController.poll(now, yaw));
            if (binding.startSwitch.isChecked() && !safetyController.isAutoUnlocked())
              finishAutoSession(latestOutput.reason, true);
          }
          sendOutput(latestOutput);
          logRangeStatus(now);
          refreshRealUi();
          refreshFollowStatus();
          mainHandler.postDelayed(this, COMMAND_REPEAT_MS);
        }
      };

  @Override
  protected void onCartFollowViewCreated() {
    // Absence parks the cart without discarding the confirmed identity; hard faults still end it.
    stateMachine.IDENTITY_UNCERTAIN_TIMEOUT_MS = Long.MAX_VALUE;
    stateMachine.SEARCH_TIMEOUT_MS = Long.MAX_VALUE;
    vehicle.useBluetoothConnection();
    binding.realControlPanel.setVisibility(View.VISIBLE);
    binding.steeringPanel.setVisibility(View.VISIBLE);
    binding.predictionHorizonGroup.setVisibility(View.GONE);
    updateSteeringUi(SteeringEvidence.unavailable("idle", REAL_CART_PREDICTION_HORIZON_MS));
    tuningRecorder = new SteeringTuningRecorder(requireContext());
    installSteeringStrengthTuning();
    installManualSpeedSelector();
    installFollowExperiments();
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
    searchEnabled = true;
    if (binding != null) {
      binding.realSearchEnabled.setChecked(true);
      applyFollowSettings();
    }
    safetyController.setForeground(true);
    registerYawSensors();
    if (vehicle.isBleSerialReady()) vehicle.startHeartbeat();
    startScheduler();
  }

  @Override
  protected void onCartFollowPause() {
    if (sensorManager != null) sensorManager.unregisterListener(this);
    yaw.clear();
    testYaw.clear();
    testingYaw = false;
    searchEnabled = false;
    searchController.configure(
        false,
        followSettings.searchSpeed,
        followSettings.searchAngle,
        followSettings.searchTimeoutMs);
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
  protected void onDiagnosticSessionChanged(
      org.openbot.cartfollow.diagnostics.CartFollowDiagnosticSession session) {
    activeDiagnosticSession = session;
    lastRangeStateKey = "";
    lastFirmwareErrorAtMs = -1L;
    if (session != null) session.setControlMode(controlModeName());
    if (vehicle != null)
      vehicle.setControlDiagnosticObserver(session == null ? null : session::control);
  }

  @Override
  protected void onDiagnosticLoggingChanged(boolean enabled) {
    if (vehicle != null) vehicle.setBleControlDiagnosticsEnabled(enabled);
  }

  @Override
  protected void onFollowEnabledChanged(boolean enabled) {
    testingYaw = false;
    setExperimentControlsEnabled(!enabled);
    if (enabled) {
      lastSessionEndReason = "none";
      logSession("start", "enabled");
      safetyController.setAutoRunEnabled(true, SystemClock.elapsedRealtime());
      recordTuning("session_start");
      refreshRealUi();
      return;
    }
    latestOutput = safetyController.resetAutoDrive("start_off", false);
    lastSessionEndReason = "user_start_off";
    logSession("end", lastSessionEndReason);
    recordTuning("session_end");
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
    long now = SystemClock.elapsedRealtime();
    latestOutput = safetyController.auto(frameResult, now, searchController.poll(now, yaw));
    RangeTelemetrySnapshot range = vehicle.getRangeTelemetry();
    frameResult.rangeTelemetry = range;
    frameResult.rangeFresh = range.isFresh(now, RANGE_STALE_MS);
    frameResult.rangeGateReason = "observation_only";
    logAutoDecision(frameResult);
    RealCartAutoDriveController.Result autoResult = safetyController.getAutoDriveResult();
    if (frameResult != null) frameResult.realDriveResult = autoResult;
    updateSteeringUi(frameResult == null ? null : frameResult.steeringEvidence);
    if (autoResult.lockout || !safetyController.isAutoUnlocked())
      finishAutoSession(latestOutput.reason, true);
    if (latestOutput.isStop()) sendOutput(latestOutput);
  }

  @Override
  protected void prepareSimulatorLearningFrame(FollowStateMachine.FrameResult frame, long now) {
    RealCartSearchController.Result search = searchController.update(frame, now, yaw);
    frame.directedReacquireEvidence = search.evidence;
    if (searchController.consumeEnterRequest()) stateMachine.enterDirectedReacquire();
    if (search.evidence.phase == DirectedReacquireEvidence.Phase.COMPLETE) {
      org.openbot.tflite.Detector.Recognition target =
          frame.target != null ? frame.target : frame.candidate;
      stateMachine.acceptDirectedContinuityRecovery(target);
    }
  }

  @Override
  protected boolean simulatorExitLearningRisk(long now) {
    return searchController.learningRisk(now);
  }

  @Override
  protected void onFollowGenerationChanged(long generation) {
    safetyController.setSessionGeneration(generation);
    searchController.reset();
    presentedFrame = null;
  }

  @Override
  protected void onFrameUiApplied(FollowStateMachine.FrameResult frame) {
    boolean showConfirmation =
        frame.state == FollowState.LOCKED_PENDING_CONFIRM
            && (presentedFrame == null || presentedFrame.state != frame.state);
    presentedFrame = frame;
    refreshFollowStatus();
    if (compactAutoLayout && showConfirmation)
      binding.confirmPanel.post(
          () -> {
            if (binding != null && compactAutoLayout)
              binding.simulatorExperimentScroll.smoothScrollTo(0, binding.confirmPanel.getTop());
          });
  }

  @Override
  protected String commandForFrame(FollowStateMachine.FrameResult frameResult, String defaultText) {
    if (frameResult != null && latestOutput.isStop()) {
      switch (frameResult.state) {
        case CAPTURE_TARGET:
          return (frameResult.distanceDiagnosticText == null
                      || frameResult.distanceDiagnosticText.isEmpty()
                  ? "正在采集目标"
                  : frameResult.distanceDiagnosticText)
              + " · c0,0";
        case LOCKED_PENDING_CONFIRM:
          return "请确认目标 · c0,0";
        case DISTANCE_CALIBRATION:
          return (frameResult.distanceDiagnosticText == null
                      || frameResult.distanceDiagnosticText.isEmpty()
                  ? "已确认，正在距离标定"
                  : frameResult.distanceDiagnosticText)
              + " · c0,0";
        case CONFIRMED_ARMED:
        case REACQUIRE_TARGET:
          return "已确认，正在重识别 · c0,0";
        case READY_TO_FOLLOW:
          return frameResult.countdownSec + " 秒后低档启动 · c0,0";
        default:
          break;
      }
    }
    RealCartAutoDriveController.Result actual = safetyController.getAutoDriveResult();
    if (latestOutput.isStop() && !actual.isStop())
      return "安全停车 · " + latestOutput.reason + " · c0,0";
    return (frameResult != null
                && frameResult.simulatorIdentity != null
                && frameResult.simulatorIdentity.isContinuous()
            ? frameResult.simulatorIdentity.isAppearanceTransition() ? "外观变化中 · 低档 · " : "连续跟踪 · "
            : "")
        + commandForAutoResult(actual);
  }

  @Override
  protected void onFollowSessionReset() {
    searchController.reset();
    yaw.reset(SteeringEvidence.Direction.NONE);
    presentedFrame = null;
    setExperimentControlsEnabled(true);
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
    org.openbot.cartfollow.diagnostics.CartFollowDiagnosticSession session =
        activeDiagnosticSession;
    if (session != null) session.setControlMode(controlModeName());
    binding.startSwitch.setChecked(false);
    resetFollowSession();
    boolean auto = mode == RealCartSafetyController.Mode.AUTO;
    if (auto) searchEnabled = true;
    binding.manualDriveControls.setVisibility(auto ? View.GONE : View.VISIBLE);
    binding.manualSpeedPanel.setVisibility(auto ? View.GONE : View.VISIBLE);
    binding.unlockAuto.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.realSafetyNotice.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.steeringStrengthPanel.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.simulatorExperimentScroll.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.simulatorExperimentPanel.setVisibility(auto ? View.VISIBLE : View.GONE);
    binding.startSwitch.setEnabled(false);
    if (auto) {
      binding.realSearchEnabled.setChecked(true);
      applyFollowSettings();
    }
    configureResponsiveLayout(binding.getRoot().getWidth(), binding.getRoot().getHeight());
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
    return manualOutput(control, SystemClock.elapsedRealtime());
  }

  private RealCartSafetyController.Output manualOutput(
      ManualControlArbiter.Control control, long nowMs) {
    switch (control) {
      case FORWARD:
        return safetyController.manual(manualForwardLogical, manualForwardLogical, nowMs);
      case BACKWARD:
        int reverseLogical = ManualSpeedProfile.reverseForForward(manualForwardLogical);
        return safetyController.manual(-reverseLogical, -reverseLogical, nowMs);
      case LEFT:
        return safetyController.manual(
            -RealCartSafetyController.MANUAL_TURN, RealCartSafetyController.MANUAL_TURN, nowMs);
      case RIGHT:
        return safetyController.manual(
            RealCartSafetyController.MANUAL_TURN, -RealCartSafetyController.MANUAL_TURN, nowMs);
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

  private void installManualSpeedSelector() {
    int saved =
        requireContext()
            .getSharedPreferences(TUNING_PREFS, 0)
            .getInt(MANUAL_SPEED_KEY, ManualSpeedProfile.DEFAULT_FORWARD_LOGICAL);
    manualForwardLogical = ManualSpeedProfile.clampForward(saved);
    binding.manualSpeedSlider.setValue(manualForwardLogical);
    updateManualSpeedUi();
    binding.manualSpeedSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          manualForwardLogical = ManualSpeedProfile.clampForward(Math.round(value));
          updateManualSpeedUi();
          ManualControlArbiter.Control active = manualTouchRouter.getActiveControl();
          if (active == ManualControlArbiter.Control.FORWARD
              || active == ManualControlArbiter.Control.BACKWARD) {
            latestOutput = manualOutput(active);
          }
        });
    binding.manualSpeedSlider.addOnSliderTouchListener(
        new com.google.android.material.slider.Slider.OnSliderTouchListener() {
          @Override
          public void onStartTrackingTouch(com.google.android.material.slider.Slider slider) {}

          @Override
          public void onStopTrackingTouch(com.google.android.material.slider.Slider slider) {
            requireContext()
                .getSharedPreferences(TUNING_PREFS, 0)
                .edit()
                .putInt(MANUAL_SPEED_KEY, manualForwardLogical)
                .apply();
            logControl("manual_speed", "forward=" + manualForwardLogical);
          }
        });
  }

  private void updateManualSpeedUi() {
    if (binding == null) return;
    int reverseLogical = ManualSpeedProfile.reverseForForward(manualForwardLogical);
    binding.manualSpeedValue.setText(
        String.format(
            java.util.Locale.US,
            "手动直行速度：前进 c%d（约 %d mm/s） · 后退 c-%d（约 %d mm/s）",
            manualForwardLogical,
            ManualSpeedProfile.estimatedMmps(manualForwardLogical),
            reverseLogical,
            ManualSpeedProfile.estimatedMmps(reverseLogical)));
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

  private void logRangeStatus(long now) {
    org.openbot.cartfollow.diagnostics.CartFollowDiagnosticSession session =
        activeDiagnosticSession;
    if (!isDiagnosticLoggingEnabled() || session == null || vehicle == null) return;
    RangeTelemetrySnapshot telemetry = vehicle.getRangeTelemetry();
    boolean fresh = telemetry.isFresh(now, RANGE_STALE_MS);
    RealCartSafetyController.Output requested = latestOutput;
    session.range(
        telemetry,
        now,
        fresh,
        requested == null ? 0 : requested.left,
        requested == null ? 0 : requested.right);
    String stateKey =
        (telemetry.capabilityAdvertised ? "1" : "0")
            + ":"
            + (telemetry.hasReading ? "1" : "0")
            + ":"
            + (fresh ? "1" : "0");
    if (!stateKey.equals(lastRangeStateKey)) {
      lastRangeStateKey = stateKey;
      logControl(
          "range_state",
          "capability="
              + (telemetry.capabilityAdvertised ? 1 : 0)
              + ",minimum_mm="
              + telemetry.minimumDistanceMm
              + ",age_ms="
              + (telemetry.ageMs(now) == Long.MAX_VALUE ? -1L : telemetry.ageMs(now))
              + ",fresh="
              + (fresh ? 1 : 0)
              + ",android_behavior=observation_only");
    }
    if (telemetry.firmwareErrorAtMs >= 0L && telemetry.firmwareErrorAtMs != lastFirmwareErrorAtMs) {
      lastFirmwareErrorAtMs = telemetry.firmwareErrorAtMs;
      logControl("firmware_error", telemetry.lastFirmwareError);
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
    recordTuning("session_end_" + reason);
    latestOutput = safetyController.resetAutoDrive(reason, revokeUnlock);
    sendOutput(latestOutput);
    if (binding.startSwitch.isChecked()) binding.startSwitch.setChecked(false);
    searchEnabled = true;
    binding.realSearchEnabled.setChecked(true);
    searchController.configure(
        true,
        followSettings.searchSpeed,
        followSettings.searchAngle,
        followSettings.searchTimeoutMs);
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
    if (!output.isStop() && safetyController.getMode() == RealCartSafetyController.Mode.AUTO) {
      output =
          safetyController.refresh(
              SystemClock.elapsedRealtime(),
              searchController.poll(SystemClock.elapsedRealtime(), yaw));
      latestOutput = output;
    }
    recordControlEvent(
        "control_submit",
        "requested=c" + output.left + "," + output.right + ";reason=" + output.reason);
    int multiplier = Math.max(1, vehicle.getSpeedMultiplier());
    vehicle.setControl(
        new Control(output.left / (float) multiplier, output.right / (float) multiplier));
    if (safetyController.getMode() == RealCartSafetyController.Mode.AUTO)
      searchController.noteCommand(output.left, output.right, SystemClock.elapsedRealtime(), yaw);
  }

  private void sendReplacementOutput(RealCartSafetyController.Output output, long generation) {
    if (vehicle == null || output == null) return;
    recordControlEvent(
        "control_submit",
        "requested=c"
            + output.left
            + ","
            + output.right
            + ";reason="
            + output.reason
            + ";replacement_generation="
            + generation);
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
    recordControlEvent(event, details);
    Log.i(
        CONTROL_LOG_TAG, "ms=" + SystemClock.elapsedRealtime() + ",event=" + event + "," + details);
  }

  private void logSession(String event, String reason) {
    recordControlEvent("session_" + event, reason);
    Log.i(
        SESSION_LOG_TAG,
        "ms=" + SystemClock.elapsedRealtime() + ",event=" + event + ",reason=" + reason);
  }

  private String controlModeName() {
    return safetyController.getMode() == RealCartSafetyController.Mode.AUTO ? "auto" : "manual";
  }

  static String commandForAutoResult(RealCartAutoDriveController.Result result) {
    if (result == null) return "自动控制未就绪";
    switch (result.phase) {
      case MOVING_STRAIGHT:
        return "小车直行 · c" + result.left + "," + result.right;
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
        return (result.reason.startsWith("aim_") ? "对准停车观察 · c0,0 · " : "起步稳定验证 · c0,0 · ")
            + HumanCartSimulatorFragment.driveReasonLabel(result.reason);
      case RECOVERY_STOP:
        return "停车验证或学习 · c0,0 · " + HumanCartSimulatorFragment.driveReasonLabel(result.reason);
      case SEARCH_BRAKE:
        return "搜索前停车等待 · c0,0";
      case PIVOT:
        return (result.reason.startsWith("aim_") ? "目标对准 · 向" : "定向搜索 · 向")
            + (result.left < 0 ? "左" : "右")
            + "旋转 · c"
            + result.left
            + ","
            + result.right;
      case PARKED_WAIT:
        return "静止等待目标返回 · 身份已保留 · c0,0";
      case WAIT_TARGET:
        return "保持停车 · c0,0 · " + HumanCartSimulatorFragment.driveReasonLabel(result.reason);
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
            + result.reason
            + ",intent="
            + result.intent
            + ",gear="
            + result.gear
            + ",aim="
            + result.aimDecision.mode
            + ",aim_allowed="
            + result.aimDecision.allowed
            + ",translation_allowed="
            + result.translationDecision.allowed
            + ",translation_max_gear="
            + result.translationDecision.maximumGear
            + ",maximum_distance_multiplier="
            + followSettings.maximumDistanceMultiplier
            + ",identity="
            + (frame == null || frame.simulatorIdentity == null
                ? "NONE"
                : frame.simulatorIdentity.state)
            + ",source_age_ms="
            + (frame == null || frame.frameTiming == null
                ? -1
                : now - frame.frameTiming.receivedAtMs)
            + ",search="
            + searchController.poll(now, yaw).reason);
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
              ManualControlArbiter.Control active = manualTouchRouter.getActiveControl();
              long now = SystemClock.elapsedRealtime();
              RangeTelemetrySnapshot range = vehicle.getRangeTelemetry();
              long rangeAge = range.ageMs(now);
              String rangeText =
                  range.capabilityAdvertised
                      ? range.hasReading
                          ? range.minimumDistanceMm
                              + " mm / age="
                              + rangeAge
                              + " ms / "
                              + (range.isFresh(now, RANGE_STALE_MS) ? "fresh" : "stale")
                          : "等待首个 s<cm>"
                      : "固件未声明 :s:";
              binding.realConnectionStatus.setText(
                  connection
                      + "\n发送 c"
                      + output
                      + " | direction="
                      + (active == null ? "STOP" : active.name())
                      + " | ble="
                      + vehicle.getBleWriteStatus()
                      + " | build="
                      + BuildConfig.VERSION_NAME
                      + "\n测距(min/source unknown)="
                      + rangeText
                      + " | Android=observation_only"
                      + (range.lastFirmwareError.isEmpty()
                          ? ""
                          : "\nfirmware=" + range.lastFirmwareError));
              boolean emergency = safetyController.isEmergencyLatched();
              binding.emergencyStop.setEnabled(!emergency);
              binding.unlockAuto.setEnabled(!emergency && vehicle.isCartFirmwareReady());
              if (emergency) {
                binding.realSafetyNotice.setVisibility(View.VISIBLE);
                binding.realSafetyNotice.setText("急停已锁存，请重启 ESP32 后重新连接");
              } else {
                binding.realSafetyNotice.setVisibility(View.VISIBLE);
                if (!range.capabilityAdvertised) {
                  binding.realSafetyNotice.setText("Android 仅记录：固件未声明测距能力；ESP32 仍可能本地拒绝运动");
                } else if (!range.isFresh(now, RANGE_STALE_MS)) {
                  binding.realSafetyNotice.setText("Android 仅记录：测距不可用或过期；ESP32 仍可能本地拒绝运动");
                } else {
                  binding.realSafetyNotice.setText(
                      "Android 仅记录三路最小值 " + range.minimumDistanceMm + " mm；ESP32 旧固件仍可能按单路传感器拒绝运动");
                }
              }
            });
  }

  @Override
  protected String additionalDebugInfo() {
    long now = SystemClock.elapsedRealtime();
    RangeTelemetrySnapshot range = vehicle.getRangeTelemetry();
    long age = range.ageMs(now);
    return String.format(
        java.util.Locale.US,
        "rangeProtocol=V1+s capability=%s\nrangeMinMm=%d ageMs=%d fresh=%s source=three_way_min_unknown\nandroidRangeBehavior=observation_only\nfirmwareMayRejectMotion=true\nfirmwareError=%s",
        range.capabilityAdvertised,
        range.minimumDistanceMm,
        age == Long.MAX_VALUE ? -1L : age,
        range.isFresh(now, RANGE_STALE_MS),
        range.lastFirmwareError.isEmpty() ? "-" : range.lastFirmwareError);
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
                        safeEvidence.demandPercent,
                        safeEvidence.predictionHorizonMs));
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

  @Override
  public void onDestroy() {
    if (tuningRecorder != null) tuningRecorder.shutdown();
    super.onDestroy();
  }

  private void installSteeringStrengthTuning() {
    int saved =
        requireContext().getSharedPreferences(TUNING_PREFS, 0).getInt(TUNING_STRENGTH_KEY, 100);
    int strength =
        Math.max(
            RealCartAutoDriveController.MIN_STEERING_STRENGTH_PERCENT,
            Math.min(RealCartAutoDriveController.MAX_STEERING_STRENGTH_PERCENT, saved));
    safetyController.setSteeringStrengthPercent(strength);
    binding.steeringStrengthSlider.setValue(strength);
    updateSteeringStrengthUi(strength);
    binding.steeringStrengthSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int updated = Math.round(value / 5f) * 5;
          safetyController.setSteeringStrengthPercent(updated);
          updateSteeringStrengthUi(updated);
        });
    binding.steeringStrengthSlider.addOnSliderTouchListener(
        new com.google.android.material.slider.Slider.OnSliderTouchListener() {
          @Override
          public void onStartTrackingTouch(com.google.android.material.slider.Slider slider) {}

          @Override
          public void onStopTrackingTouch(com.google.android.material.slider.Slider slider) {
            int updated = Math.round(slider.getValue() / 5f) * 5;
            requireContext()
                .getSharedPreferences(TUNING_PREFS, 0)
                .edit()
                .putInt(TUNING_STRENGTH_KEY, updated)
                .apply();
            recordTuning("strength_changed");
          }
        });
    binding.recordSteeringTuning.setOnClickListener(
        v -> {
          recordTuning("manual_mark");
          binding.steeringTuningNote.setText("");
          Toast.makeText(requireContext(), "已记录转弯参数", Toast.LENGTH_SHORT).show();
        });
  }

  private void updateSteeringStrengthUi(int strength) {
    if (binding == null) return;
    int minimumInner = RealCartAutoDriveController.innerSpeedForDemand(100, strength);
    binding.steeringStrengthValue.setText(
        String.format(
            java.util.Locale.US,
            "转弯强度 %d%% · 最大差速 %d,14 / 14,%d",
            strength,
            minimumInner,
            minimumInner));
  }

  private void recordTuning(String event) {
    if (tuningRecorder == null) return;
    String note = binding == null ? "" : binding.steeringTuningNote.getText().toString().trim();
    tuningRecorder.record(
        event,
        safetyController.getSteeringStrengthPercent(),
        safetyController.getAutoDriveResult(),
        note);
  }

  void installFollowExperiments() {
    followSettings =
        RealFollowSettings.load(requireContext().getSharedPreferences(RealFollowSettings.PREFS, 0));
    searchEnabled = true;
    compactAutoLayout = false;
    compactViews = null;
    compactParents = null;
    compactParams = null;
    compactIndices = null;
    normalScrollParams = null;
    binding
        .getRoot()
        .addOnLayoutChangeListener(
            (v, l, t, r, b, ol, ot, or, ob) -> {
              if (binding != null && v == binding.getRoot())
                configureResponsiveLayout(r - l, b - t);
            });
    binding.realExperimentOptions.setVisibility(View.VISIBLE);
    binding.recoveryTimeoutGroup.setVisibility(View.GONE);
    binding.simulatorReconfirm.setVisibility(View.GONE);
    binding.galleryModeGroup.check(
        followSettings.dynamicGallery ? R.id.gallery_adaptive : R.id.gallery_static);
    binding.recentGallerySwitch.setChecked(followSettings.recent);
    binding.autoGearGroup.check(
        followSettings.maximumGear == 21
            ? R.id.auto_gear_21
            : followSettings.maximumGear == 18 ? R.id.auto_gear_18 : R.id.auto_gear_14);
    binding.realSearchEnabled.setChecked(true);
    android.view.ViewGroup strengthParent =
        (android.view.ViewGroup) binding.steeringStrengthPanel.getParent();
    if (strengthParent != binding.simulatorExperimentPanel) {
      strengthParent.removeView(binding.steeringStrengthPanel);
      binding.simulatorExperimentPanel.addView(
          binding.steeringStrengthPanel,
          binding.simulatorExperimentPanel.indexOfChild(binding.simulatorStatus));
    }
    binding.searchSpeedSlider.setValue(followSettings.searchSpeed);
    binding.searchAngleSlider.setValue(followSettings.searchAngle);
    binding.searchTimeoutSlider.setValue(followSettings.searchTimeoutMs / 1000f);
    binding.maximumDistanceSlider.setValue(followSettings.maximumDistanceMultiplier);
    updateMaximumDistanceUi();
    binding.galleryModeGroup.addOnButtonCheckedListener(
        (group, id, checked) -> {
          if (!checked || binding.startSwitch.isChecked()) return;
          followSettings.dynamicGallery = id == R.id.gallery_adaptive;
          applyFollowSettings();
        });
    binding.recentGallerySwitch.setOnCheckedChangeListener(
        (button, checked) -> {
          if (binding.startSwitch.isChecked()) return;
          followSettings.recent = checked;
          applyFollowSettings();
        });
    binding.autoGearGroup.addOnButtonCheckedListener(
        (group, id, checked) -> {
          if (!checked || binding.startSwitch.isChecked()) return;
          followSettings.maximumGear =
              id == R.id.auto_gear_21 ? 21 : id == R.id.auto_gear_18 ? 18 : 14;
          applyFollowSettings();
        });
    binding.realSearchEnabled.setOnCheckedChangeListener(
        (button, checked) -> {
          if (binding.startSwitch.isChecked()) return;
          searchEnabled = checked;
          applyFollowSettings();
        });
    binding.searchSpeedSlider.addOnChangeListener(
        (slider, value, user) -> {
          if (binding.startSwitch.isChecked()) return;
          followSettings.searchSpeed = Math.round(value);
          applyFollowSettings();
        });
    binding.searchAngleSlider.addOnChangeListener(
        (slider, value, user) -> {
          if (binding.startSwitch.isChecked()) return;
          followSettings.searchAngle = Math.round(value);
          applyFollowSettings();
        });
    binding.searchTimeoutSlider.addOnChangeListener(
        (slider, value, user) -> {
          if (binding.startSwitch.isChecked()) return;
          followSettings.searchTimeoutMs = Math.round(value * 1000);
          applyFollowSettings();
        });
    binding.maximumDistanceSlider.addOnChangeListener(
        (slider, value, user) -> {
          if (binding.startSwitch.isChecked()) return;
          followSettings.maximumDistanceMultiplier = Math.round(value * 20f) / 20f;
          applyFollowSettings();
        });
    binding.gyroTestLeft.setOnClickListener(v -> startYawTest(SteeringEvidence.Direction.LEFT));
    binding.gyroTestRight.setOnClickListener(v -> startYawTest(SteeringEvidence.Direction.RIGHT));
    binding.gyroTestStop.setOnClickListener(v -> testingYaw = false);
    binding.gyroTestReset.setOnClickListener(
        v -> {
          testingYaw = false;
          testYaw.reset(SteeringEvidence.Direction.NONE);
        });
    sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
    if (sensorManager != null) {
      gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
      gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }
    applyFollowSettings();
  }

  /** Keep emergency/Start pinned while short landscape windows scroll the auxiliary panels. */
  void configureResponsiveLayout(int width, int height) {
    if (binding == null) return;
    boolean compact =
        width > height
            && height > 0
            && binding.realControlPanel.getVisibility() == View.VISIBLE
            && binding.manualDriveControls.getVisibility() == View.GONE;
    if (compact == compactAutoLayout) return;
    compactAutoLayout = compact;
    if (compact) {
      compactViews =
          new View[] {
            binding.commandText,
            binding.steeringPanel,
            binding.countdownText,
            binding.confirmPanel,
            (View) binding.modelSpinner.getParent(),
            binding.realConnectionStatus,
            binding.realModeGroup,
            binding.realSafetyNotice
          };
      compactParents = new android.view.ViewGroup[compactViews.length];
      compactParams = new android.view.ViewGroup.LayoutParams[compactViews.length];
      compactIndices = new int[compactViews.length];
      for (int i = 0; i < compactViews.length; i++) {
        compactParents[i] = (android.view.ViewGroup) compactViews[i].getParent();
        compactParams[i] = compactViews[i].getLayoutParams();
        compactIndices[i] = compactParents[i].indexOfChild(compactViews[i]);
      }
      for (int i = 0; i < compactViews.length; i++) {
        compactParents[i].removeView(compactViews[i]);
        binding.simulatorExperimentPanel.addView(
            compactViews[i],
            i,
            new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
      }
      normalScrollParams =
          new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
              (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                  binding.simulatorExperimentScroll.getLayoutParams());
      androidx.constraintlayout.widget.ConstraintLayout.LayoutParams p =
          new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(normalScrollParams);
      p.topToBottom = -1;
      p.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
      p.topMargin = Math.round(58 * getResources().getDisplayMetrics().density);
      p.bottomToTop = R.id.real_control_panel;
      binding.simulatorExperimentScroll.setLayoutParams(p);
    } else {
      for (int i = 0; i < compactViews.length; i++) {
        binding.simulatorExperimentPanel.removeView(compactViews[i]);
        compactParents[i].addView(
            compactViews[i],
            Math.min(compactIndices[i], compactParents[i].getChildCount()),
            compactParams[i]);
      }
      binding.simulatorExperimentScroll.setLayoutParams(normalScrollParams);
    }
  }

  private void applyFollowSettings() {
    FollowPolicy policy =
        new FollowPolicy(
            true,
            followSettings.dynamicGallery
                ? GalleryUpdateStatus.Mode.ADAPTIVE
                : GalleryUpdateStatus.Mode.STATIC,
            false,
            searchEnabled);
    configureFollowPolicy(policy);
    configureRecentGallery(followSettings.dynamicGallery && followSettings.recent);
    safetyController.setMaximumGear(followSettings.maximumGear);
    stateMachine.setMaximumDistanceMultiplier(followSettings.maximumDistanceMultiplier);
    searchController.configure(
        policy.directedSearch,
        followSettings.searchSpeed,
        followSettings.searchAngle,
        followSettings.searchTimeoutMs);
    followSettings.save(requireContext().getSharedPreferences(RealFollowSettings.PREFS, 0));
    binding.searchSpeedValue.setText(
        "旋转档位 c"
            + followSettings.searchSpeed
            + " · 轮速约 "
            + Math.max(80, ManualSpeedProfile.estimatedMmps(followSettings.searchSpeed))
            + " mm/s");
    binding.searchAngleValue.setText("最大搜索角度 " + followSettings.searchAngle + "°");
    binding.searchTimeoutValue.setText(
        "搜索总时限 " + followSettings.searchTimeoutMs / 1000f + " 秒（含制动与验证）");
    updateMaximumDistanceUi();
    setExperimentControlsEnabled(!binding.startSwitch.isChecked());
  }

  void setExperimentControlsEnabled(boolean enabled) {
    if (binding == null) return;
    for (View v :
        new View[] {
          binding.galleryStatic,
          binding.galleryAdaptive,
          binding.autoGear14,
          binding.autoGear18,
          binding.autoGear21,
          binding.realSearchEnabled,
          binding.gyroTestLeft,
          binding.gyroTestRight,
          binding.gyroTestStop,
          binding.gyroTestReset
        }) v.setEnabled(enabled);
    binding.recentGallerySwitch.setEnabled(enabled && followSettings.dynamicGallery);
    binding.searchSpeedSlider.setEnabled(enabled && searchEnabled);
    binding.searchAngleSlider.setEnabled(enabled && searchEnabled);
    binding.searchTimeoutSlider.setEnabled(enabled && searchEnabled);
    binding.maximumDistanceSlider.setEnabled(enabled);
  }

  private void updateMaximumDistanceUi() {
    if (binding == null) return;
    float stop = Math.max(1f, followSettings.maximumDistanceMultiplier - .08f);
    binding.maximumDistanceValue.setText(
        getString(
            R.string.cart_follow_maximum_distance_format,
            followSettings.maximumDistanceMultiplier,
            stop));
  }

  private void registerYawSensors() {
    if (sensorManager == null) return;
    boolean g =
        gravity != null
            && sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
    boolean r =
        gyroscope != null
            && sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    yaw.setSensorStatus(gyroscope != null, g && r);
    testYaw.setSensorStatus(gyroscope != null, g && r);
  }

  private void startYawTest(SteeringEvidence.Direction direction) {
    if (binding == null || binding.startSwitch.isChecked()) return;
    testYaw.reset(direction);
    testingYaw = true;
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event == null || event.sensor == null || event.values == null || event.values.length < 3)
      return;
    if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
      yaw.onGravity(event.timestamp, event.values[0], event.values[1], event.values[2]);
      testYaw.onGravity(event.timestamp, event.values[0], event.values[1], event.values[2]);
    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      yaw.onGyroscope(event.timestamp, event.values[0], event.values[1], event.values[2]);
      if (testingYaw)
        testYaw.onGyroscope(event.timestamp, event.values[0], event.values[1], event.values[2]);
    }
  }

  private void refreshFollowStatus() {
    if (binding == null || safetyController.getMode() != RealCartSafetyController.Mode.AUTO) return;
    long now = SystemClock.elapsedRealtime();
    RealCartSearchController.Result search = searchController.poll(now, yaw);
    YawTurnTracker.Status sensor = yaw.getStatus(SystemClock.elapsedRealtimeNanos());
    binding.gyroTestStatus.setText(
        sensor.available
            ? "测角就绪 · 独立测试 "
                + Math.round(testYaw.getTurnedDegrees())
                + "°"
                + (testYaw.isWrongDirection() ? " · 方向错误" : "")
            : "转角不可用 · "
                + (!sensor.sensorExists ? "缺少陀螺仪" : !sensor.registered ? "传感器未就绪" : "数据中断"));
    RealCartAutoDriveController.Result actual = safetyController.getAutoDriveResult();
    FollowStateMachine.FrameResult frame = presentedFrame;
    String text =
        "实际输出 c"
            + latestOutput.left
            + ","
            + latestOutput.right
            + " · 档位 "
            + actual.gear
            + " / 上限 "
            + followSettings.maximumGear
            + "\n动作="
            + actual.phase
            + " · "
            + latestOutput.reason
            + "\n最近结束="
            + lastSessionEndReason;
    if (frame != null) {
      if (frame.simulatorIdentity != null)
        text +=
            "\n身份="
                + frame.simulatorIdentity.state
                + " · "
                + frame.simulatorIdentity.reason
                + "\n恢复="
                + frame.simulatorIdentity.recoveryType
                + " "
                + frame.simulatorIdentity.freshMatches
                + "/"
                + frame.simulatorIdentity.requiredFreshMatches
                + "\n连续性="
                + frame.simulatorIdentity.continuityReason;
      if (frame.identityEvidence != null && frame.identityEvidence.reidMatch != null) {
        ReIDMatchResult match = frame.identityEvidence.reidMatch;
        text +=
            String.format(
                java.util.Locale.US,
                "\n独立身份=%.3f Anchor=%.3f margin=%.3f",
                match.bestScore,
                match.anchorScore,
                match.margin);
      }
      GalleryUpdateStatus g = frame.galleryUpdateStatus;
      if (g != null)
        text +=
            "\nAnchor="
                + g.anchorSize
                + " · Adaptive="
                + g.adaptiveSize
                + " · Recent="
                + (frame.recentGallery == null ? 0 : frame.recentGallery.size)
                + " · Quarantine="
                + g.quarantineSize
                + "\n学习="
                + g.event
                + " · "
                + g.reason;
      text += "\n" + HumanCartSimulatorFragment.deferredGalleryText(frame);
      if (frame.distanceDiagnosticText != null) text += "\n" + frame.distanceDiagnosticText;
    }
    text +=
        "\n搜索="
            + (searchEnabled ? search.evidence.phase : "未启用")
            + " · "
            + search.reason
            + "\n"
            + search.evidence.directionLabel()
            + " "
            + Math.round(yaw.getTurnedDegrees())
            + "°/"
            + followSettings.searchAngle
            + "° · "
            + search.evidence.elapsedMs / 1000f
            + "/"
            + followSettings.searchTimeoutMs / 1000f
            + " 秒";
    binding.simulatorStatus.setText(text);
    boolean fresh =
        frame != null && frame.frameTiming != null && now - frame.frameTiming.receivedAtMs <= 500L;
    binding.simulatorFrameHealth.setText(
        fresh ? "画面有效 · 完整处理 " + frame.frameTiming.pipelineMs + " ms" : "等待新鲜画面");
    if (binding.startSwitch.isChecked()) binding.commandText.setText(commandForFrame(frame, ""));
  }
}
