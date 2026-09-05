package org.openbot.cartfollow;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import org.openbot.R;

/** Human-in-the-loop view of the shared cart-follow perception and behavior pipeline. */
public class HumanCartSimulatorFragment extends BaseCartFollowFragment
    implements SensorEventListener {
  private static final String SEARCH_PREFS = "simulator_directed_reacquire";
  private int predictionHorizonMs = 400;
  private final SimulatorAutoDriveController driveController = new SimulatorAutoDriveController();
  private final DirectedReacquireController directedController = new DirectedReacquireController();
  private final YawTurnTracker yawTurnTracker = new YawTurnTracker();
  private GalleryUpdateStatus.Mode galleryMode = GalleryUpdateStatus.Mode.ADAPTIVE;
  private boolean recentEnabled = true;
  private long recoveryLimitMs = 2000L;
  private SimulatorAutoDriveController.Result driveResult = driveController.reset("idle");
  private DirectedReacquireEvidence directedEvidence = DirectedReacquireEvidence.idle("idle");
  private GalleryUpdateStatus galleryStatus;
  private SensorManager sensorManager;
  private Sensor gyroscope;
  private Sensor gravity;
  private int searchSpeed = 5;
  private float searchAngle = 180f;
  private long searchTimeoutMs = 10000L;
  private float maximumDistanceMultiplier =
      ImageSetpointDistanceEstimator.DEFAULT_MAX_DISTANCE_MULTIPLIER;
  private boolean endingSession;
  private final YawTurnTracker testYawTracker = new YawTurnTracker();
  private final Handler sensorUiHandler = new Handler(Looper.getMainLooper());
  private boolean gyroRegistered;
  private boolean gravityRegistered;
  private long gyroEventNs = -1L;
  private long gravityEventNs = -1L;
  private boolean gyroTestRunning;
  private String gyroTestState = "待命";
  private long lastUiFrameReceivedAtMs = -1L;
  private boolean frameUiExpired;
  private FollowState presentedState = FollowState.IDLE;
  private FollowStateMachine.FrameResult presentedFrame;
  private DirectedReacquireEvidence polledParkedEvidence;
  private long parkedDeadlineAtMs = -1L;
  private final Runnable sensorStatusTick =
      new Runnable() {
        @Override
        public void run() {
          if (binding == null) return;
          renderSensorStatus();
          DirectedReacquireEvidence deadline =
              directedController.pollDeadline(SystemClock.elapsedRealtime(), yawTurnTracker);
          applyDirectedDeadline(deadline, SystemClock.elapsedRealtime());
          if (binding.startSwitch.isChecked()
              && !frameUiExpired
              && lastUiFrameReceivedAtMs >= 0L
              && SystemClock.elapsedRealtime() - lastUiFrameReceivedAtMs > 500L) {
            showStaleFrame();
          }
          sensorUiHandler.postDelayed(this, 100L);
        }
      };

  @Override
  protected void onCartFollowViewCreated() {
    binding.steeringPanel.setVisibility(View.VISIBLE);
    binding.simulatorExperimentPanel.setVisibility(View.VISIBLE);
    binding.simulatorExperimentScroll.setVisibility(View.VISIBLE);
    binding.simulatorReconfirm.setVisibility(View.GONE);
    binding.galleryModeGroup.check(R.id.gallery_adaptive);
    binding.recoveryTimeoutGroup.check(R.id.recovery_2s);
    installDirectedSearchControls();
    binding.gyroTestLeft.setOnClickListener(v -> startGyroTest(SteeringEvidence.Direction.LEFT));
    binding.gyroTestRight.setOnClickListener(v -> startGyroTest(SteeringEvidence.Direction.RIGHT));
    binding.gyroTestStop.setOnClickListener(v -> stopGyroTest("已停止"));
    binding.gyroTestReset.setOnClickListener(
        v -> {
          stopGyroTest("已复位");
          resetTestYaw();
          renderSensorStatus();
        });
    configureSimulatorExperiments(galleryMode, true);
    binding.recentGallerySwitch.setChecked(recentEnabled);
    configureRecentGallery(recentEnabled);
    binding.recentGallerySwitch.setOnCheckedChangeListener(
        (button, checked) -> {
          if (binding.startSwitch.isChecked()) return;
          recentEnabled = checked;
          configureRecentGallery(checked && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE);
          refreshSimulatorStatus(null);
        });
    driveController.setRecoveryLimitMs(recoveryLimitMs);
    sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
    if (sensorManager != null) {
      gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
      gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }
    binding.galleryModeGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked || binding.startSwitch.isChecked()) return;
          galleryMode =
              checkedId == R.id.gallery_static
                  ? GalleryUpdateStatus.Mode.STATIC
                  : GalleryUpdateStatus.Mode.ADAPTIVE;
          configureSimulatorExperiments(galleryMode, true);
          configureRecentGallery(recentEnabled && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE);
          binding.recentGallerySwitch.setEnabled(galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE);
          refreshSimulatorStatus(null);
        });
    binding.recoveryTimeoutGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked || binding.startSwitch.isChecked()) return;
          recoveryLimitMs = checkedId == R.id.recovery_5s ? 5000L : 2000L;
          driveController.setRecoveryLimitMs(recoveryLimitMs);
          refreshSimulatorStatus(null);
        });
    binding.predictionHorizonGroup.check(R.id.prediction_400);
    binding.predictionHorizonGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) return;
          if (checkedId == R.id.prediction_0) predictionHorizonMs = 0;
          else if (checkedId == R.id.prediction_800) predictionHorizonMs = 800;
          else predictionHorizonMs = 400;
          steeringDemandEstimator.reset();
          updateSteeringUi(SteeringEvidence.unavailable("horizon_changed", predictionHorizonMs));
        });
    updateSteeringUi(SteeringEvidence.unavailable("idle", predictionHorizonMs));
    refreshSimulatorStatus(null);
  }

  @Override
  public synchronized void onResume() {
    super.onResume();
    if (sensorManager != null) {
      gravityRegistered =
          gravity != null
              && sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
      gyroRegistered =
          gyroscope != null
              && sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }
    yawTurnTracker.setSensorStatus(gyroscope != null, gyroRegistered);
    testYawTracker.setSensorStatus(gyroscope != null, gyroRegistered);
    sensorUiHandler.removeCallbacks(sensorStatusTick);
    sensorUiHandler.post(sensorStatusTick);
  }

  @Override
  protected void onCartFollowPause() {
    if (sensorManager != null) sensorManager.unregisterListener(this);
    sensorUiHandler.removeCallbacks(sensorStatusTick);
    gyroRegistered = false;
    gravityRegistered = false;
    gyroEventNs = gravityEventNs = -1L;
    stopGyroTest("已暂停");
    testYawTracker.clear();
    directedController.reset();
    yawTurnTracker.clear();
    renderSensorStatus();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event == null || event.values == null || event.values.length < 3) return;
    if (!Float.isFinite(event.values[0])
        || !Float.isFinite(event.values[1])
        || !Float.isFinite(event.values[2])) return;
    if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
      gravityEventNs = event.timestamp;
      yawTurnTracker.onGravity(event.timestamp, event.values[0], event.values[1], event.values[2]);
      testYawTracker.onGravity(event.timestamp, event.values[0], event.values[1], event.values[2]);
    } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
      gyroEventNs = event.timestamp;
      yawTurnTracker.onGyroscope(
          event.timestamp, event.values[0], event.values[1], event.values[2]);
      if (gyroTestRunning) {
        testYawTracker.onGyroscope(
            event.timestamp, event.values[0], event.values[1], event.values[2]);
        if (testYawTracker.getTurnedDegrees() >= searchAngle) stopGyroTest("角度已达到");
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  protected int steeringPredictionHorizonMs() {
    return predictionHorizonMs;
  }

  @Override
  protected boolean simulatorExitLearningRisk(long now) {
    return directedController.isActive() || directedController.hasRecentExitEvidence(now);
  }

  @Override
  protected void onFollowFrame(FollowStateMachine.FrameResult frameResult) {
    long now = SystemClock.elapsedRealtime();
    if (frameResult == null
        || frameResult.directedReacquireEvidence == null
        || frameResult.frameTiming != null && frameResult.frameTiming.sourceAgeMs > 500L) {
      prepareSimulatorLearningFrame(frameResult, now);
    }
    DirectedReacquireEvidence deadline = directedController.pollDeadline(now, yawTurnTracker);
    if (deadline != null) directedEvidence = deadline;
    if (frameResult != null) frameResult.directedReacquireEvidence = directedEvidence;
    driveResult = driveController.update(frameResult, now);
    if (frameResult != null) frameResult.simulatorDriveResult = driveResult;
  }

  @Override
  protected void prepareSimulatorLearningFrame(FollowStateMachine.FrameResult frame, long now) {
    directedEvidence = directedController.update(frame, now, yawTurnTracker);
    if (directedController.consumeEnterRequest()) stateMachine.enterDirectedReacquire();
    if (frame != null) {
      frame.directedReacquireEvidence = directedEvidence;
      if (directedEvidence.phase == DirectedReacquireEvidence.Phase.COMPLETE) {
        org.openbot.tflite.Detector.Recognition target =
            frame.target != null ? frame.target : frame.candidate;
        stateMachine.acceptDirectedContinuityRecovery(target);
      }
    }
  }

  @Override
  protected void onFrameUiApplied(FollowStateMachine.FrameResult frame) {
    if (binding != null) binding.simulatorReconfirm.setVisibility(View.GONE);
    presentedFrame = frame;
    lastUiFrameReceivedAtMs =
        frame == null || frame.frameTiming == null ? -1L : frame.frameTiming.receivedAtMs;
    frameUiExpired = false;
    presentedState = frame == null ? FollowState.IDLE : frame.state;
    updateSteeringUi(
        frame == null || frame.targetObservation == null || !frame.targetObservation.current
            ? SteeringEvidence.unavailable("current_observation_missing", predictionHorizonMs)
            : frame.steeringEvidence);
    refreshSimulatorStatus(frame);
    if (frame == null || binding == null || endingSession) return;
    binding.simulatorReconfirm.setVisibility(View.GONE);
    binding.simulatorFrameHealth.setText(
        isFrameFresh(frame.frameTiming)
            ? "画面有效 · 完整处理 " + frame.frameTiming.pipelineMs + " ms"
            : "等待新鲜画面 · 旧观测已过期");
    boolean lockout =
        frame.simulatorDriveResult != null && frame.simulatorDriveResult.lockout
            || frame.directedReacquireEvidence != null && frame.directedReacquireEvidence.lockout;
    if (lockout) {
      endingSession = true;
      String endText = directedEndText(frame.directedReacquireEvidence);
      binding.startSwitch.setChecked(false);
      resetFollowSession();
      updateCommandText(endText);
      endingSession = false;
      return;
    }
    if (!isFrameFresh(frame.frameTiming)) showStaleFrame();
  }

  static boolean isFrameFresh(FrameTimingEvidence timing) {
    return timing != null && timing.sourceAgeMs >= 0L && timing.sourceAgeMs <= 500L;
  }

  private void showStaleFrame() {
    if (binding == null) return;
    frameUiExpired = true;
    updateSteeringUi(SteeringEvidence.unavailable("frame_stale", predictionHorizonMs));
    binding.steeringSummary.setText("等待新鲜画面 · 无实时转向证据");
    binding.simulatorFrameHealth.setText("等待新鲜画面 · 旧观测已过期（>500 ms）");
    SimulatorAutoDriveController.Result output = displayDrive(presentedFrame);
    if (output.phase == SimulatorAutoDriveController.Phase.PARKED_WAIT) {
      refreshSimulatorStatus(presentedFrame);
      binding.commandText.setText(parkedWaitText() + " · 等待新鲜画面");
    } else {
      binding.commandText.setText(staleCommand(presentedState));
      binding.simulatorStatus.setText("流程=" + presentedState.name() + "\n等待新鲜画面 · 模拟停车 c0,0");
    }
  }

  void applyDirectedDeadline(DirectedReacquireEvidence deadline, long nowMs) {
    if (binding == null || !binding.startSwitch.isChecked() || deadline == null) return;
    if (deadline.lockout) {
      String reason = directedEndText(deadline);
      binding.startSwitch.setChecked(false);
      resetFollowSession();
      updateCommandText(reason);
    } else if (deadline.phase == DirectedReacquireEvidence.Phase.PARKED_WAIT) {
      if (polledParkedEvidence == null) parkedDeadlineAtMs = nowMs;
      polledParkedEvidence = deadline;
      directedEvidence = deadline;
      driveResult =
          stoppedDisplay(
              SimulatorAutoDriveController.Phase.PARKED_WAIT,
              deadline.reason,
              deadline.elapsedMs,
              deadline.timeoutMs);
      binding.simulatorReconfirm.setVisibility(View.GONE);
      updateSteeringUi(SteeringEvidence.unavailable("parked_wait", predictionHorizonMs));
      binding.steeringSummary.setText("搜索已结束 · 停车等待目标返回");
      refreshSimulatorStatus(presentedFrame);
      binding.commandText.setText(parkedWaitText());
    }
  }

  private DirectedReacquireEvidence displaySearch(FollowStateMachine.FrameResult frame) {
    // A queued pre-deadline frame must not restore a turning command after the timer has parked.
    if (polledParkedEvidence != null) {
      if (frame == null
          || frame.frameTiming == null
          || frame.frameTiming.receivedAtMs <= parkedDeadlineAtMs) return polledParkedEvidence;
      polledParkedEvidence = null;
      parkedDeadlineAtMs = -1L;
    }
    return frame == null || frame.directedReacquireEvidence == null
        ? DirectedReacquireEvidence.idle("idle")
        : frame.directedReacquireEvidence;
  }

  private SimulatorAutoDriveController.Result displayDrive(FollowStateMachine.FrameResult frame) {
    return displayDrive(frame, displaySearch(frame));
  }

  private static SimulatorAutoDriveController.Result displayDrive(
      FollowStateMachine.FrameResult frame, DirectedReacquireEvidence search) {
    SimulatorAutoDriveController.Result drive = frame == null ? null : frame.simulatorDriveResult;
    if (search.lockout
        || drive != null
            && (drive.lockout || drive.phase == SimulatorAutoDriveController.Phase.ENDED)
        || frame != null && frame.state == FollowState.STOP) {
      return stoppedDisplay(
          SimulatorAutoDriveController.Phase.ENDED,
          search.lockout ? search.reason : drive == null ? "state_stop" : drive.reason,
          0,
          0);
    }
    if (search.phase == DirectedReacquireEvidence.Phase.PARKED_WAIT) {
      return stoppedDisplay(
          SimulatorAutoDriveController.Phase.PARKED_WAIT,
          search.reason,
          search.elapsedMs,
          search.timeoutMs);
    }
    if (drive != null && drive.phase == SimulatorAutoDriveController.Phase.PARKED_WAIT) {
      return stoppedDisplay(
          drive.phase, drive.reason, drive.recoveryElapsedMs, drive.recoveryLimitMs);
    }
    if (frame != null && !isFrameFresh(frame.frameTiming)) {
      return stoppedDisplay(SimulatorAutoDriveController.Phase.RECOVERY_STOP, "frame_stale", 0, 0);
    }
    return drive != null
        ? drive
        : stoppedDisplay(SimulatorAutoDriveController.Phase.IDLE, "drive_unavailable", 0, 0);
  }

  private static SimulatorAutoDriveController.Result stoppedDisplay(
      SimulatorAutoDriveController.Phase phase, String reason, long elapsedMs, long limitMs) {
    return new SimulatorAutoDriveController.Result(
        phase,
        0,
        0,
        0,
        reason,
        phase == SimulatorAutoDriveController.Phase.ENDED,
        elapsedMs,
        limitMs);
  }

  static String parkedWaitText() {
    return "搜索已结束 · 目标暂不可见或身份待核验 · 停车等待 · 目标身份已保留 · c0,0";
  }

  static String identityVerificationText(SimulatorIdentityGuard.Decision identity) {
    if (identity.tracking != null) return identity.tracking.label() + " · " + identity.reason;
    if (identity.state == SimulatorIdentityGuard.State.TRACK_STABLE) return "连续跟踪 · 局部外观与空间连续";
    if (identity.state == SimulatorIdentityGuard.State.APPEARANCE_TRANSITION)
      return "外观变化中 · 低档连续跟踪";
    if (identity.isMaintained()) return "连续跟踪 · 仅允许低档，尚非强身份授权";
    if (identity.authorized) return "身份已验证";
    String type =
        identity.recoveryType == SimulatorIdentityGuard.RecoveryType.GLOBAL
            ? "全局"
            : identity.recoveryType == SimulatorIdentityGuard.RecoveryType.LOCAL ? "局部" : "自动";
    String progress =
        identity.requiredFreshMatches > 0
            ? " " + identity.freshMatches + "/" + identity.requiredFreshMatches
            : " 等待新鲜匹配";
    return type
        + "身份复核"
        + progress
        + (identity.requiredSpanMs > 0
            ? String.format(
                java.util.Locale.US,
                " · 证据跨度 %.1f/%.1fs",
                identity.recoverySpanMs / 1000f,
                identity.requiredSpanMs / 1000f)
            : "");
  }

  static String staleCommand(FollowState state) {
    switch (state) {
      case CAPTURE_TARGET:
        return "采集中 · 等待新鲜画面 · c0,0";
      case LOCKED_PENDING_CONFIRM:
        return "请确认目标 · 等待新鲜画面 · c0,0";
      case DISTANCE_CALIBRATION:
        return "距离标定中 · 等待新鲜画面 · c0,0";
      case READY_TO_FOLLOW:
        return "启动倒计时 · 等待新鲜画面 · c0,0";
      case CONFIRMED_ARMED:
      case REACQUIRE_TARGET:
        return "重识别中 · 等待新鲜画面 · c0,0";
      default:
        return "等待新鲜画面 · 模拟停车 c0,0";
    }
  }

  @Override
  protected String commandForFrame(FollowStateMachine.FrameResult frameResult, String defaultText) {
    DirectedReacquireEvidence search = displaySearch(frameResult);
    SimulatorAutoDriveController.Result result = displayDrive(frameResult, search);
    if (result.phase == SimulatorAutoDriveController.Phase.ENDED) return "安全停止 · 本轮模拟已结束 · c0,0";
    if (result.phase == SimulatorAutoDriveController.Phase.PARKED_WAIT)
      return parkedWaitText()
          + (frameResult != null
                  && frameResult.simulatorIdentity != null
                  && frameResult.simulatorIdentity.state == SimulatorIdentityGuard.State.AUTO_VERIFY
              ? " · " + identityVerificationText(frameResult.simulatorIdentity)
              : "");
    if (frameResult != null
        && frameResult.frameTiming != null
        && !isFrameFresh(frameResult.frameTiming)) return staleCommand(frameResult.state);
    if (frameResult != null && result.left == 0 && result.right == 0) {
      switch (frameResult.state) {
        case CAPTURE_TARGET:
          return (frameResult.distanceDiagnosticText == null
                  ? "正在采集目标"
                  : frameResult.distanceDiagnosticText)
              + " · 模拟输出 c0,0";
        case LOCKED_PENDING_CONFIRM:
          return "请确认目标 · 模拟输出 c0,0";
        case DISTANCE_CALIBRATION:
          return (frameResult.distanceDiagnosticText == null
                  ? "已确认，正在距离标定"
                  : frameResult.distanceDiagnosticText)
              + " · 模拟输出 c0,0";
        case CONFIRMED_ARMED:
        case REACQUIRE_TARGET:
          return "已确认，正在重识别 · 模拟输出 c0,0";
        case READY_TO_FOLLOW:
          return frameResult.countdownSec + " 秒后低档启动 · 模拟输出 c0,0";
        default:
          break;
      }
    }
    if (result.left != 0 || result.right != 0) {
      if (search.phase == DirectedReacquireEvidence.Phase.TURNING
          && result.phase == SimulatorAutoDriveController.Phase.RECOVERY_STOP)
        return directedTurningText(search, result);
      SteeringEvidence steering = frameResult == null ? null : frameResult.steeringEvidence;
      String direction =
          steering == null || steering.direction == SteeringEvidence.Direction.NONE
              ? "直行"
              : "向" + steering.directionLabel() + steering.levelLabel() + "缓弯";
      String prefix =
          frameResult != null
                  && frameResult.simulatorIdentity != null
                  && frameResult.simulatorIdentity.state
                      == SimulatorIdentityGuard.State.CONTINUITY_HOLD
              ? "连续保持 · "
              : search.phase == DirectedReacquireEvidence.Phase.COMPLETE ? "恢复完成 · " : "";
      return prefix
          + (frameResult.simulatorIdentity != null && frameResult.simulatorIdentity.isContinuous()
              ? frameResult.simulatorIdentity.tracking != null
                  ? frameResult.simulatorIdentity.tracking.label() + " · "
                  : frameResult.simulatorIdentity.isAppearanceTransition() ? "外观变化中 · " : "连续跟踪 · "
              : "")
          + result.gearLabel()
          + "跟随 · "
          + direction
          + " · c"
          + result.left
          + ","
          + result.right;
    }
    if (frameResult != null && frameResult.simulatorIdentity != null) {
      SimulatorIdentityGuard.Decision identity = frameResult.simulatorIdentity;
      if (identity.isContinuous()) return "连续跟踪 · 模拟停车 c0,0 · " + driveReasonLabel(result.reason);
      if (identity.state == SimulatorIdentityGuard.State.AUTO_VERIFY)
        return identityVerificationText(identity) + " · 模拟停车 c0,0";
      if (identity.state == SimulatorIdentityGuard.State.CONTINUITY_HOLD) {
        return String.format(
            java.util.Locale.US, "连续保持 · 模拟停车 · c0,0 · 剩余 %.1fs", identity.holdRemainingMs / 1000f);
      }
      if (identity.retainTarget && !identity.authorized)
        return "姿态适应中 · 停车学习 · c0,0 · " + driveReasonLabel(identity.reason);
      if (!identity.authorized && frameResult.candidate != null)
        return identityVerificationText(identity) + " · 模拟停车 c0,0";
    }
    if (search != null && search.phase != DirectedReacquireEvidence.Phase.IDLE) {
      switch (search.phase) {
        case TURNING:
          return "定向搜索暂停 · 模拟停车 c0,0 · " + driveReasonLabel(result.reason);
        case VERIFYING:
          return "发现候选人物 · 静止 ReID 验证 · c0,0";
        case SETTLING:
          return "目标已恢复 · 停车稳定 0.3 秒 · c0,0";
        case COMPLETE:
          return "恢复完成 · 模拟停车 c0,0 · " + driveReasonLabel(result.reason);
        default:
          break;
      }
    }
    if (result.phase == SimulatorAutoDriveController.Phase.RECOVERY_STOP) {
      if ("person_visible_reacquire".equals(result.reason)) return "身份确认中 · 静止重捕 · c0,0";
      return String.format(
          java.util.Locale.US,
          "目标暂不可见 · 静止重捕 %.1f/%.1f 秒 · c0,0",
          result.recoveryElapsedMs / 1000f,
          result.recoveryLimitMs / 1000f);
    }
    if ("distance_ok".equals(result.reason)) return "身份可信 · 距离合适 · c0,0";
    if ("distance_too_close".equals(result.reason)) return "距离过近 · 模拟停车 · c0,0";
    if ("distance_unknown".equals(result.reason)) return "距离证据不足 · 模拟停车 · c0,0";
    return defaultText + " · 模拟停车 c0,0";
  }

  @Override
  protected void onFollowEnabledChanged(boolean enabled) {
    binding.simulatorReconfirm.setVisibility(View.GONE);
    binding.recentGallerySwitch.setEnabled(
        !enabled && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE);
    setGroupEnabled(binding.galleryModeGroup, !enabled);
    setGroupEnabled(binding.recoveryTimeoutGroup, !enabled);
    setDirectedControlsEnabled(!enabled);
    if (enabled) {
      stopGyroTest("跟随运行中");
      resetTestYaw();
      configureSimulatorExperiments(galleryMode, true);
      configureRecentGallery(recentEnabled && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE);
      driveController.setRecoveryLimitMs(recoveryLimitMs);
      directedController.configure(searchSpeed, searchAngle, searchTimeoutMs);
    }
  }

  @Override
  protected void onGalleryStatusUpdated(GalleryUpdateStatus status) {
    galleryStatus = status;
  }

  @Override
  protected void onFollowSessionReset() {
    presentedState = FollowState.IDLE;
    presentedFrame = null;
    polledParkedEvidence = null;
    parkedDeadlineAtMs = -1L;
    lastUiFrameReceivedAtMs = -1L;
    frameUiExpired = false;
    steeringDemandEstimator.reset();
    driveResult = driveController.reset("session_reset");
    directedController.reset();
    yawTurnTracker.clear();
    yawTurnTracker.setSensorStatus(gyroscope != null, gyroRegistered);
    stopGyroTest("已复位");
    resetTestYaw();
    endingSession = false;
    directedEvidence = DirectedReacquireEvidence.idle("session_reset");
    galleryStatus = getGalleryUpdateStatus();
    updateSteeringUi(SteeringEvidence.unavailable("session_reset", predictionHorizonMs));
    if (binding != null) {
      binding.recentGallerySwitch.setEnabled(galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE);
      binding.simulatorReconfirm.setVisibility(View.GONE);
      binding.simulatorFrameHealth.setText("等待相机帧");
      setGroupEnabled(binding.galleryModeGroup, true);
      setGroupEnabled(binding.recoveryTimeoutGroup, true);
      setDirectedControlsEnabled(true);
    }
    refreshSimulatorStatus(null);
  }

  private void refreshSimulatorStatus(FollowStateMachine.FrameResult frame) {
    if (binding == null) return;
    final GalleryUpdateStatus gallery =
        frame == null ? getGalleryUpdateStatus() : frame.galleryUpdateStatus;
    final DirectedReacquireEvidence search = displaySearch(frame);
    final SimulatorAutoDriveController.Result drive = displayDrive(frame, search);
    final String state = frame == null ? "IDLE" : frame.state.name();
    final float belief =
        frame == null || frame.identityEvidence == null ? 0f : frame.identityEvidence.targetBelief;
    final String distance =
        frame == null || frame.distanceDiagnosticText == null ? "-" : frame.distanceDiagnosticText;
    String galleryReason = gallery == null ? "-" : galleryReasonLabel(gallery.reason);
    String gear =
        drive.phase == SimulatorAutoDriveController.Phase.PARKED_WAIT
            ? "PARKED_WAIT 停车"
            : drive.left == 0 && drive.right == 0
                ? "停车"
                : drive.phase == SimulatorAutoDriveController.Phase.RECOVERY_STOP
                    ? "定向搜索"
                    : drive.gearLabel();
    int left = drive == null ? 0 : drive.left;
    int right = drive == null ? 0 : drive.right;
    String driveReason = drive == null ? "-" : driveReasonLabel(drive.reason);
    binding.simulatorStatus.setText(
        String.format(
            java.util.Locale.US,
            "流程=%s | 身份 belief=%.2f\n%s | %s\n"
                + "距离（相对图像）=%s\n模拟输出来源=%s c%d,%d\n"
                + "定向搜索=%s/%s %.0f°/%.0f° %.1f/%.1fs | %s\n"
                + "普通静止重捕=%.1f/%.1fs | 动作原因=%s%s",
            state,
            belief,
            galleryTierText(gallery, frame == null ? null : frame.recentGallery),
            galleryReason,
            distance,
            gear,
            left,
            right,
            search.phase.name(),
            search.directionLabel(),
            search.turnedDegrees,
            search.targetDegrees,
            search.elapsedMs / 1000f,
            search.timeoutMs / 1000f,
            search.reason,
            drive == null ? 0f : drive.recoveryElapsedMs / 1000f,
            drive.recoveryLimitMs / 1000f,
            driveReason,
            gallery == null
                ? ""
                : String.format(
                    java.util.Locale.US,
                    "\nGallery raw anchor=%.3f adaptive=%.3f novelty=%.3f | %s",
                    gallery.anchorScore,
                    gallery.adaptiveScore,
                    gallery.novelty,
                    gallery.reason)));
    binding.simulatorStatus.append(
        "\n" + galleryGeometryText(frame == null ? null : frame.galleryGeometry));
    if (drive.phase == SimulatorAutoDriveController.Phase.PARKED_WAIT)
      binding.simulatorStatus.append("\n" + parkedWaitText());
    if (frame != null) binding.simulatorStatus.append("\n" + deferredGalleryText(frame));
    if (frame != null && frame.recentGallery != null) {
      RecentGallery.Status recent = frame.recentGallery;
      binding.simulatorStatus.append(
          String.format(
              java.util.Locale.US,
              "\nRecent=%s %d/16 score=%.3f | %s",
              recent.enabled ? "开启" : "关闭",
              recent.size,
              recent.score,
              recent.reason));
    }
    if (frame != null && frame.simulatorIdentity != null) {
      SimulatorIdentityGuard.Decision permit = frame.simulatorIdentity;
      binding.simulatorStatus.append(
          String.format(
              java.util.Locale.US,
              "\n身份=%s 保持=%s 采样=%s 宽限=%.1fs\n连续性=%s | %s\n%s",
              permit.state,
              permit.retainTarget,
              permit.samplingAllowed,
              permit.holdRemainingMs / 1000f,
              permit.continuityReason,
              permit.reason,
              identityVerificationText(permit)));
    }
    ReIDMatchResult match =
        frame == null || frame.identityEvidence == null ? null : frame.identityEvidence.reidMatch;
    if (match != null && match.reidAvailable) {
      binding.simulatorStatus.append(
          String.format(
              java.util.Locale.US,
              "\n身份 raw anchor=%.3f adaptive=%.3f best=%.3f margin=%.3f\n"
                  + "Gallery 入库要求 best>=0.85 / margin>=0.08%s",
              match.anchorScore,
              match.adaptiveScore,
              match.bestScore,
              match.margin,
              match.bestScore < 0.85f || match.margin < 0.08f ? "（未达到）" : ""));
    }
    if (frame != null && frame.frameTiming != null) {
      FrameTimingEvidence timing = frame.frameTiming;
      binding.simulatorStatus.append(
          String.format(
              java.util.Locale.US,
              "\n检测=%dms ReID=%dms 管线=%dms\n完成帧率=%.1fFPS 源年龄=%dms 丢帧=%d",
              timing.detectorMs,
              timing.reidMs,
              timing.pipelineMs,
              timing.completedFps,
              timing.sourceAgeMs,
              timing.droppedFrames));
    }
    if (frame != null && frame.targetObservation != null && frame.targetObservation.current) {
      TargetObservationEvidence observation = frame.targetObservation;
      binding.simulatorStatus.append(
          String.format(
              java.util.Locale.US,
              "\n目标 track=%d source=%s current=%s\n屏幕归一化 bbox=[%.3f,%.3f,%.3f,%.3f]",
              observation.trackId,
              observation.source,
              observation.current,
              observation.screenBox.left,
              observation.screenBox.top,
              observation.screenBox.right,
              observation.screenBox.bottom));
    } else {
      binding.simulatorStatus.append("\n当前目标观测缺失（无本帧实测 bbox）");
    }
  }

  static String galleryTierText(GalleryUpdateStatus gallery, RecentGallery.Status recent) {
    return String.format(
        java.util.Locale.US,
        "Anchor（初始身份）=%d | Adaptive（已批准外观）=%d\n"
            + "Recent（短期记忆）=%d/16 %s | Quarantine（待验证隔离）=%d\n"
            + "Adaptive 待确认=%d | Quarantine 确认=%d",
        gallery == null ? 0 : gallery.anchorSize,
        gallery == null ? 0 : gallery.adaptiveSize,
        recent == null ? 0 : recent.size,
        recent == null ? "无本帧指标" : recent.enabled ? "开启" : "关闭",
        gallery == null ? 0 : gallery.quarantineSize,
        gallery == null ? 0 : gallery.pendingConfirmations,
        gallery == null ? 0 : gallery.quarantineConfirmations);
  }

  static String deferredGalleryText(FollowStateMachine.FrameResult frame) {
    return "延迟入库="
        + (frame.deferredGalleryStatus == null ? "-" : frame.deferredGalleryStatus)
        + "\nRecent 匹配支持="
        + (frame.recentMatchingSupport ? "有" : "无")
        + "（辅助证据，不等于身份授权）";
  }

  static String galleryGeometryText(GalleryCropGeometry geometry) {
    if (geometry == null) return "Gallery 几何=无本帧指标";
    return String.format(
        java.util.Locale.US,
        "Gallery 几何 %.1f×%.1fpx / 要求 32×64px\n"
            + "高度 %.1f%% / 普通>=18%% / Q>=12%%\n可见宽高比>=%.2f\n普通=%s | Q=%s\n"
            + "边界标记（仅诊断） 左=%s 右=%s 上=%s 下=%s 侧边=%s",
        geometry.visibleWidthPx,
        geometry.visibleHeightPx,
        geometry.heightRatio * 100f,
        GalleryCropGeometry.MIN_VISIBLE_ASPECT_RATIO,
        galleryReasonLabel(geometry.normalReason),
        galleryReasonLabel(geometry.quarantineReason),
        geometry.leftClipped,
        geometry.rightClipped,
        geometry.topClipped,
        geometry.bottomClipped,
        geometry.lateralClipped);
  }

  private void startGyroTest(SteeringEvidence.Direction direction) {
    if (binding == null || binding.startSwitch.isChecked()) return;
    testYawTracker.reset(direction);
    gyroTestRunning = true;
    gyroTestState = direction == SteeringEvidence.Direction.LEFT ? "向左测试" : "向右测试";
    renderSensorStatus();
  }

  private void resetTestYaw() {
    testYawTracker.clear();
    testYawTracker.setSensorStatus(gyroscope != null, gyroRegistered);
  }

  private void stopGyroTest(String reason) {
    gyroTestRunning = false;
    gyroTestState = reason;
    renderSensorStatus();
  }

  static String sensorState(boolean present, boolean registered, long timestampNs, long nowNs) {
    if (!present) return "不存在";
    if (!registered) return "存在 / 未注册";
    if (timestampNs < 0L) return "已注册 / 等待事件";
    long ageNs = nowNs - timestampNs;
    if (ageNs < 0L) return "已注册 / 时间戳异常";
    long ageMs = ageNs / 1_000_000L;
    return "已注册 / " + (ageNs <= 500_000_000L ? "新鲜 " : "过期 ") + ageMs + "ms";
  }

  static String directedTurningText(DirectedReacquireEvidence search) {
    return directedTurningText(
        search,
        new SimulatorAutoDriveController.Result(
            SimulatorAutoDriveController.Phase.RECOVERY_STOP,
            0,
            search.left(),
            search.right(),
            search.reason,
            false,
            search.elapsedMs,
            search.timeoutMs));
  }

  private static String directedTurningText(
      DirectedReacquireEvidence search, SimulatorAutoDriveController.Result drive) {
    return String.format(
        java.util.Locale.US,
        "%s · 模拟%s转 c%d,%d · %.0f°/%.0f°",
        search.wrongDirection ? "旋转方向错误" : "目标侧边离屏",
        search.directionLabel(),
        drive.left,
        drive.right,
        search.turnedDegrees,
        search.targetDegrees);
  }

  private void renderSensorStatus() {
    if (binding == null) return;
    long nowNs = SystemClock.elapsedRealtimeNanos();
    YawTurnTracker.Status health = yawTurnTracker.getStatus(nowNs);
    binding.gyroTestStatus.setText(
        String.format(
            java.util.Locale.US,
            "陀螺仪=%s | yaw=%s\n重力=%s\n独立转角测试=%s %.1f°/%.0f°%s",
            sensorState(gyroscope != null, gyroRegistered, gyroEventNs, nowNs),
            health.available ? "可用" : "不可用",
            sensorState(gravity != null, gravityRegistered, gravityEventNs, nowNs),
            gyroTestState,
            testYawTracker.getTurnedDegrees(),
            searchAngle,
            testYawTracker.isWrongDirection() ? " | 旋转方向错误" : ""));
  }

  private void installDirectedSearchControls() {
    SharedPreferences prefs =
        requireContext().getSharedPreferences(SEARCH_PREFS, Context.MODE_PRIVATE);
    searchSpeed = clamp(prefs.getInt("speed", 5), 5, 21);
    searchAngle = clamp(prefs.getFloat("angle", 180f), 30f, 180f);
    float timeoutSeconds = clamp(prefs.getFloat("timeout_seconds", 10f), 1f, 10f);
    searchTimeoutMs = Math.round(timeoutSeconds * 1000f);
    maximumDistanceMultiplier =
        clamp(
            prefs.getFloat(
                "maximum_distance_multiplier",
                ImageSetpointDistanceEstimator.DEFAULT_MAX_DISTANCE_MULTIPLIER),
            ImageSetpointDistanceEstimator.MIN_MAX_DISTANCE_MULTIPLIER,
            ImageSetpointDistanceEstimator.MAX_MAX_DISTANCE_MULTIPLIER);
    binding.searchSpeedSlider.setValue(searchSpeed);
    binding.searchAngleSlider.setValue(searchAngle);
    binding.searchTimeoutSlider.setValue(timeoutSeconds);
    binding.maximumDistanceSlider.setValue(maximumDistanceMultiplier);
    updateDirectedControlLabels();
    binding.searchSpeedSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          searchSpeed = Math.round(value);
          prefs.edit().putInt("speed", searchSpeed).apply();
          applyDirectedConfig();
        });
    binding.searchAngleSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          searchAngle = value;
          prefs.edit().putFloat("angle", searchAngle).apply();
          applyDirectedConfig();
        });
    binding.searchTimeoutSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          searchTimeoutMs = Math.round(value * 1000f);
          prefs.edit().putFloat("timeout_seconds", value).apply();
          applyDirectedConfig();
        });
    binding.maximumDistanceSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          maximumDistanceMultiplier = Math.round(value * 20f) / 20f;
          prefs.edit().putFloat("maximum_distance_multiplier", maximumDistanceMultiplier).apply();
          stateMachine.setMaximumDistanceMultiplier(maximumDistanceMultiplier);
          updateDirectedControlLabels();
        });
    stateMachine.setMaximumDistanceMultiplier(maximumDistanceMultiplier);
    applyDirectedConfig();
  }

  private void applyDirectedConfig() {
    updateDirectedControlLabels();
    directedController.configure(searchSpeed, searchAngle, searchTimeoutMs);
  }

  private void updateDirectedControlLabels() {
    if (binding == null) return;
    binding.searchSpeedValue.setText("模拟旋转档位 c" + searchSpeed);
    binding.searchAngleValue.setText(
        String.format(java.util.Locale.US, "最大搜索角度 %.0f°", searchAngle));
    binding.searchTimeoutValue.setText(
        String.format(java.util.Locale.US, "定向搜索上限 %.1f 秒（独立于普通重捕）", searchTimeoutMs / 1000f));
    binding.maximumDistanceValue.setText(
        getString(
            R.string.cart_follow_maximum_distance_format,
            maximumDistanceMultiplier,
            Math.max(1f, maximumDistanceMultiplier - .08f)));
  }

  private void setDirectedControlsEnabled(boolean enabled) {
    binding.searchSpeedSlider.setEnabled(enabled);
    binding.searchAngleSlider.setEnabled(enabled);
    binding.searchTimeoutSlider.setEnabled(enabled);
    binding.maximumDistanceSlider.setEnabled(enabled);
    binding.gyroTestLeft.setEnabled(enabled);
    binding.gyroTestRight.setEnabled(enabled);
    binding.gyroTestStop.setEnabled(enabled);
    binding.gyroTestReset.setEnabled(enabled);
  }

  private static String directedEndText(DirectedReacquireEvidence evidence) {
    if (evidence != null && "search_angle_limit".equals(evidence.reason)) {
      return "达到搜索角度仍未找到目标，本轮模拟已结束";
    }
    if (evidence != null && "search_timeout".equals(evidence.reason)) {
      return "定向搜索超时，本轮模拟已结束";
    }
    return "目标不可见超时，本轮模拟已结束";
  }

  private static void setGroupEnabled(android.view.ViewGroup group, boolean enabled) {
    group.setEnabled(enabled);
    for (int i = 0; i < group.getChildCount(); i++) group.getChildAt(i).setEnabled(enabled);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  private static String galleryReasonLabel(String reason) {
    if (reason == null) return "-";
    switch (reason) {
      case "aim_settling":
      case "aim_brake_observe":
        return "停止旋转，等待新画面";
      case "aim_brake_before_pivot":
        return "先停车，再短时对准目标";
      case "aim_pulse":
        return "短时对准目标";
      case "anchor_confirmed":
        return "初始身份已确认";
      case "static_mode":
        return "固定模式，不更新";
      case "pending_1_of_3":
        return "新外观复核 1/3";
      case "pending_2_of_3":
        return "新外观复核 2/3";
      case "adaptive_added":
        return "已学习新外观";
      case "quarantine_1_of_5":
        return "姿态候选复核 1/5";
      case "quarantine_2_of_5":
        return "姿态候选复核 2/5";
      case "quarantine_3_of_5":
        return "姿态候选已隔离 3/5";
      case "quarantine_4_of_5":
        return "姿态候选复核 4/5";
      case "quarantine_promoted":
        return "姿态候选已晋升";
      case "multi_person_frozen":
        return "多人出现，暂停学习";
      case "recovery_frozen":
        return "重捕期间暂停学习";
      case "sample_redundant":
        return "外观重复，不加入";
      case "sample_too_different":
        return "外观差异过大，拒绝";
      case "reid_not_strong":
        return "身份分数不足";
      case "belief_not_stable":
        return "身份尚未稳定";
      case "crop_quality_low":
        return "几何门控未通过（等待具体指标）";
      case "ok":
        return "几何门控通过";
      case "invalid_bbox":
        return "目标框无效";
      case "unsupported_orientation":
        return "屏幕方向不支持";
      case "bbox_exited_frame":
        return "目标框已离屏";
      case "lateral_left_clipped":
        return "左侧贴边或截断";
      case "lateral_right_clipped":
        return "右侧贴边或截断";
      case "upright_width_below_32px":
        return "可见宽度不足 32px";
      case "upright_height_below_64px":
        return "可见高度不足 64px";
      case "height_below_18_percent":
        return "高度占比不足 18%";
      case "height_below_12_percent":
        return "高度占比不足 12%";
      case "visible_aspect_ratio_below_0.15":
        return "可见宽高比不足 0.15";
      case "motion_gate_failed":
        return "轨迹连续性不足";
      case "cooldown":
        return "等待下一次采样";
      case "no_diversity_gain":
        return "未增加外观多样性";
      default:
        return reason;
    }
  }

  static String driveReasonLabel(String reason) {
    if (reason == null) return "-";
    switch (reason) {
      case "maintenance_score_below_0.80":
        return "独立身份分数不足 0.80";
      case "maintenance_anchor_below_0.70":
        return "初始样本相似度不足 0.70";
      case "maintenance_margin_below_0.08":
        return "候选区分度不足 0.08";
      case "continuous_track_maintained":
        return "连续目标低档维持复核";
      case "maintained_start_verification":
        return "等待三次新鲜维持证据再起步";
      case "strong_identity_revalidation":
        return "等待三次强身份匹配";
      case "identity_evidence_expired":
        return "身份依据已过期，停车等待新画面";
      case "local_association_ambiguous":
        return "人物关联有歧义，停车复核";
      case "follow_straight":
        return "身份、距离和方向允许直行";
      case "follow_curve":
        return "身份、距离和方向允许缓弯";
      case "distance_ok":
        return "已达到跟随距离";
      case "distance_too_close":
        return "目标距离过近";
      case "distance_unknown":
        return "距离证据不足";
      case "person_visible_reacquire":
        return "画面有人，正在静止确认身份";
      case "target_not_visible":
        return "目标暂不可见，等待目标返回";
      case "target_missing_wait":
      case "search_timeout":
      case "search_angle_limit":
        return "搜索已结束，停车等待，目标身份已保留";
      case "frame_stale":
        return "等待新鲜画面";
      case "drive_unavailable":
        return "等待模拟输出";
      case "stationary_reacquire":
        return "重新识别期间保持停车";
      case "directed_reacquire":
        return "可信侧边离屏，定向搜索";
      case "countdown":
        return "首次启动倒计时期间保持停车";
      case "waiting_confirmation":
        return "等待用户确认目标";
      case "collecting_target":
        return "正在自动采集目标";
      default:
        return reason;
    }
  }

  private void updateSteeringUi(SteeringEvidence evidence) {
    if (binding == null) return;
    final SteeringEvidence safeEvidence =
        evidence == null ? SteeringEvidence.unavailable("idle", predictionHorizonMs) : evidence;
    binding.steeringGauge.setEvidence(safeEvidence);
    if (!safeEvidence.valid) {
      binding.steeringSummary.setText("转向需求等待可信跟随目标");
      return;
    }
    if (safeEvidence.direction == SteeringEvidence.Direction.NONE) {
      binding.steeringSummary.setText(
          String.format(
              java.util.Locale.US,
              "转向需求 %d%% · 居中\n当前 %+.2f · 预测 %+.2f（提前 %d ms）",
              safeEvidence.demandPercent,
              safeEvidence.filteredError,
              safeEvidence.predictedError,
              safeEvidence.predictionHorizonMs));
      return;
    }
    binding.steeringSummary.setText(
        String.format(
            java.util.Locale.US,
            "转向需求 %d%% · 向%s%s转弯\n当前 %+.2f · 预测 %+.2f（提前 %d ms）",
            safeEvidence.demandPercent,
            safeEvidence.directionLabel(),
            safeEvidence.levelLabel(),
            safeEvidence.filteredError,
            safeEvidence.predictedError,
            safeEvidence.predictionHorizonMs));
  }
}
