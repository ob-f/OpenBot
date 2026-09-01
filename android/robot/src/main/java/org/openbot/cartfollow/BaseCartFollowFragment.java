package org.openbot.cartfollow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.openbot.R;
import org.openbot.cartfollow.diagnostics.CartFollowDiagnosticConfig;
import org.openbot.cartfollow.diagnostics.CartFollowDiagnosticSaver;
import org.openbot.cartfollow.diagnostics.CartFollowDiagnosticSession;
import org.openbot.common.CameraFragment;
import org.openbot.databinding.FragmentHumanCartSimulatorBinding;
import org.openbot.env.ImageUtils;
import org.openbot.tflite.Detector;
import org.openbot.tflite.Model;
import org.openbot.tflite.Network;
import org.openbot.utils.CameraUtils;
import org.openbot.utils.Enums;
import org.openbot.vehicle.Control;
import timber.log.Timber;

public class BaseCartFollowFragment extends CameraFragment {

  private static final int COLOR_TARGET = 0;
  private static final int COLOR_CANDIDATE = 1;
  private static final int COLOR_NORMAL = 2;
  private static final int COLOR_FAIL = 3;
  private static final int RECOVERY_RELOCK_MIN_FRAMES = 2;

  protected FragmentHumanCartSimulatorBinding binding;
  private Handler handler;
  private HandlerThread handlerThread;

  private boolean computingNetwork = false;
  private float minConfidence = 0.5f;

  private Detector detector;
  private Matrix frameToCropTransform;
  private Bitmap croppedBitmap;
  private int sensorOrientation;
  private Matrix cropToFrameTransform;

  private Model model;
  private Network.Device device = Network.Device.CPU;
  private int numThreads = -1;
  private final String classType = "person";

  private long lastProcessingTimeMs = -1;
  private long frameNum = 0;

  private final ControlGenerator controlGenerator = new ControlGenerator();
  private final HumanCommandInterpreter interpreter = new HumanCommandInterpreter();
  private final TargetMatcher matcher = new TargetMatcher();
  protected final FollowStateMachine stateMachine =
      new FollowStateMachine(matcher, controlGenerator);
  private final ActionArbitrator actionArbitrator = new ActionArbitrator();
  private final TargetTrackManager targetTrackManager = new TargetTrackManager();
  private final IdentityBeliefAccumulator beliefAccumulator = new IdentityBeliefAccumulator();
  private ReIDCoordinator reidCoordinator;
  private final CartFollowDiagnosticConfig diagnosticConfig = new CartFollowDiagnosticConfig();
  private final CartFollowDiagnosticSaver diagnosticSaver = new CartFollowDiagnosticSaver();
  private CartFollowDiagnosticSession diagnosticSession;
  private boolean diagnosticEnabled = false;
  private boolean diagnosticActive = false;
  private boolean targetEventAwaitingReturn = false;
  private boolean showFullDebug = false;
  private long lastDiagnosticFrameLogMs = 0L;
  private long lastDiagnosticCropMs = 0L;
  private long lastDiagnosticGalleryMs = 0L;
  private int recoveryRelockTrackId = -1;
  private int recoveryRelockFrames = 0;
  private Bitmap latestConfirmSnapshot;

  private final List<DrawBox> drawBoxes = new ArrayList<>();
  private int drawFrameWidth = 0;
  private int drawFrameHeight = 0;
  private int drawSensorOrientation = 0;

  private final Paint targetBoxPaint = new Paint();
  private final Paint candidateBoxPaint = new Paint();
  private final Paint personBoxPaint = new Paint();
  private final Paint failBoxPaint = new Paint();
  private final Paint boxTextPaint = new Paint();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    targetBoxPaint.setColor(Color.GREEN);
    targetBoxPaint.setStyle(Paint.Style.STROKE);
    targetBoxPaint.setStrokeWidth(8.0f);
    candidateBoxPaint.setColor(Color.YELLOW);
    candidateBoxPaint.setStyle(Paint.Style.STROKE);
    candidateBoxPaint.setStrokeWidth(8.0f);
    personBoxPaint.setColor(Color.WHITE);
    personBoxPaint.setStyle(Paint.Style.STROKE);
    personBoxPaint.setStrokeWidth(6.0f);
    failBoxPaint.setColor(Color.RED);
    failBoxPaint.setStyle(Paint.Style.STROKE);
    failBoxPaint.setStrokeWidth(8.0f);
    boxTextPaint.setColor(Color.WHITE);
    boxTextPaint.setTextSize(40.0f);
  }

  @Override
  public View onCreateView(
      @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentHumanCartSimulatorBinding.inflate(inflater, container, false);
    return inflateFragment(binding, inflater, container);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    reidCoordinator = new ReIDCoordinator(requireActivity(), getNumThreads());

    binding.confidenceValue.setText((int) (minConfidence * 100) + "%");
    binding.plusConfidence.setOnClickListener(
        v -> {
          int confValue = (int) (minConfidence * 100);
          if (confValue >= 95) return;
          confValue += 5;
          minConfidence = confValue / 100f;
          binding.confidenceValue.setText(confValue + "%");
          controlGenerator.MIN_CONFIDENCE = minConfidence;
        });
    binding.minusConfidence.setOnClickListener(
        v -> {
          int confValue = (int) (minConfidence * 100);
          if (confValue <= 5) return;
          confValue -= 5;
          minConfidence = confValue / 100f;
          binding.confidenceValue.setText(confValue + "%");
          controlGenerator.MIN_CONFIDENCE = minConfidence;
        });

    List<String> models = getModelNames(f -> f.type.equals(Model.TYPE.DETECTOR));
    initModelSpinner(binding.modelSpinner, models, preferencesManager.getObjectNavModel());

    setAnalyserResolution(Enums.Preview.HD.getValue());

    binding.trackingOverlay.addCallback(canvas -> drawOverlay(canvas));
    binding.btnDebugDetails.setOnClickListener(
        v -> {
          showFullDebug = !showFullDebug;
          binding.btnDebugDetails.setText(showFullDebug ? "收起详情" : "调试详情");
        });
    resetTargetEventButton();
    binding.btnTargetEvent.setOnClickListener(v -> recordTargetEvent());
    binding.diagnosticSwitch.setChecked(false);
    onDiagnosticLoggingChanged(false);
    binding.diagnosticSwitch.setOnClickListener(
        v -> {
          diagnosticEnabled = binding.diagnosticSwitch.isChecked();
          onDiagnosticLoggingChanged(diagnosticEnabled);
          if (!diagnosticEnabled) {
            stopDiagnosticSession();
          }
          resetTargetEventButton();
        });

    binding.btnConfirm.setOnClickListener(
        v -> {
          if (!isFollowConfirmationPending()) {
            resetFollowSession();
            return;
          }
          if (reidCoordinator != null) reidCoordinator.confirmGallery();
          int lockedTrackId =
              targetTrackManager.lockClosest(stateMachine.getMemory().getLastBbox());
          beliefAccumulator.lockTrack(lockedTrackId);
          activateDiagnosticSession();
          if (diagnosticActive && diagnosticSession != null && latestConfirmSnapshot != null) {
            diagnosticSaver.saveGallerySnapshotAsync(
                latestConfirmSnapshot, diagnosticSession, "confirmed_snapshot");
          }
          stateMachine.confirm();
        });
    binding.btnRetake.setOnClickListener(
        v -> {
          if (!isFollowConfirmationPending()) {
            resetFollowSession();
            return;
          }
          if (reidCoordinator != null) reidCoordinator.reset();
          targetTrackManager.reset();
          beliefAccumulator.reset();
          resetRecoveryRelock();
          stopDiagnosticSession();
          startDiagnosticSession();
          stateMachine.retake();
        });
    binding.btnCancel.setOnClickListener(
        v -> {
          resetFollowSession();
          if (binding.startSwitch.isChecked()) binding.startSwitch.setChecked(false);
          onFollowEnabledChanged(false);
        });

    binding.startSwitch.setChecked(false);
    binding.startSwitch.setOnClickListener(
        v -> {
          if (binding.startSwitch.isChecked()) {
            binding.modelSpinner.setEnabled(false);
            if (reidCoordinator != null) reidCoordinator.reset();
            targetTrackManager.reset();
            beliefAccumulator.reset();
            resetRecoveryRelock();
            startDiagnosticSession();
            stateMachine.startCapture();
            onFollowEnabledChanged(true);
          } else {
            resetFollowSession();
            onFollowEnabledChanged(false);
          }
        });
    onCartFollowViewCreated();
  }

  /** Hook for concrete screens to install controls without duplicating the perception pipeline. */
  protected void onCartFollowViewCreated() {}

  /** Lets real hardware screens stop synchronously when the shared Start switch changes. */
  protected void onFollowEnabledChanged(boolean enabled) {}

  /** Lets a concrete screen attach non-control evidence to a completed perception frame. */
  protected void enrichFrameResult(
      FollowStateMachine.FrameResult frameResult,
      int frameW,
      int frameH,
      int sensorOrientation,
      long nowMs) {}

  /** Lets a concrete screen reset its own per-session visual state. */
  protected void onFollowSessionReset() {}

  protected final void resetFollowSession() {
    if (binding == null) return;
    binding.modelSpinner.setEnabled(true);
    if (reidCoordinator != null) reidCoordinator.reset();
    targetTrackManager.reset();
    beliefAccumulator.reset();
    resetRecoveryRelock();
    onFollowSessionReset();
    stopDiagnosticSession();
    stateMachine.cancel();
    clearDrawState();
    resetUiToIdle();
  }

  protected final boolean isFollowConfirmationPending() {
    return binding != null
        && shouldShowConfirmation(binding.startSwitch.isChecked(), stateMachine.getState());
  }

  static boolean shouldShowConfirmation(boolean startChecked, FollowState state) {
    return startChecked && state == FollowState.LOCKED_PENDING_CONFIRM;
  }

  static boolean updateConfirmationVisibility(
      View confirmPanel, boolean startChecked, FollowState state) {
    boolean visible = shouldShowConfirmation(startChecked, state);
    confirmPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
    return visible;
  }

  protected final void resetUiToIdle() {
    updateCommandText(getString(R.string.cart_sim_idle));
    updateDebugInfo(FollowState.IDLE, new Control(0f, 0f), 0, 0f, null, null, null, null);
    if (binding != null) {
      binding.confirmPanel.setVisibility(View.GONE);
      binding.countdownText.setVisibility(View.GONE);
      binding.trackingOverlay.postInvalidate();
    }
  }

  protected void onInferenceConfigurationChanged() {
    computingNetwork = false;
    if (croppedBitmap == null) return;
    final Network.Device device = getDevice();
    final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateNetwork(model, device, numThreads));
  }

  private void recreateNetwork(Model model, Network.Device device, int numThreads) {
    if (model == null) return;
    Detector newDetector = null;
    try {
      newDetector = Detector.create(requireActivity(), model, device, numThreads);
    } catch (IllegalArgumentException | IOException e) {
      Timber.e(e, "Failed to create network.");
      String msg =
          model.pathType == Model.PATH_TYPE.URL
              ? "该模型未下载，请先在主菜单 Model Management 中下载: " + model.name
              : "模型加载失败: " + e.getMessage();
      requireActivity()
          .runOnUiThread(
              () ->
                  Toast.makeText(requireContext().getApplicationContext(), msg, Toast.LENGTH_LONG)
                      .show());
      return;
    }

    if (detector != null) {
      detector.close();
    }
    detector = newDetector;
    try {
      croppedBitmap =
          Bitmap.createBitmap(
              detector.getImageSizeX(), detector.getImageSizeY(), Bitmap.Config.ARGB_8888);
      frameToCropTransform =
          ImageUtils.getTransformationMatrix(
              getMaxAnalyseImageSize().getWidth(),
              getMaxAnalyseImageSize().getHeight(),
              croppedBitmap.getWidth(),
              croppedBitmap.getHeight(),
              sensorOrientation,
              detector.getCropRect(),
              detector.getMaintainAspect());
      cropToFrameTransform = new Matrix();
      frameToCropTransform.invert(cropToFrameTransform);
    } catch (Exception e) {
      Timber.e(e, "Failed to configure detector.");
      requireActivity()
          .runOnUiThread(
              () ->
                  Toast.makeText(
                          requireContext().getApplicationContext(),
                          "模型配置失败: " + e.getMessage(),
                          Toast.LENGTH_LONG)
                      .show());
    }
  }

  @Override
  public synchronized void onResume() {
    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
    super.onResume();
  }

  @Override
  public synchronized void onPause() {
    onCartFollowPause();
    stopDiagnosticSession();
    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    super.onPause();
  }

  /** Called before the inference thread and diagnostics are stopped. */
  protected void onCartFollowPause() {}

  protected boolean isDiagnosticLoggingEnabled() {
    return diagnosticEnabled;
  }

  protected void onDiagnosticLoggingChanged(boolean enabled) {}

  @Override
  public void onDestroy() {
    diagnosticSaver.shutdown();
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) handler.post(r);
  }

  @Override
  protected void processUSBData(String data) {}

  @Override
  protected void processControllerKeyData(String commandType) {}

  @Override
  protected void processFrame(Bitmap bitmap, ImageProxy image) {
    if (detector == null) {
      updateCropImageInfo();
      if (detector == null) return;
    }
    if (croppedBitmap == null || frameToCropTransform == null || cropToFrameTransform == null) {
      Timber.w("Inference buffers were missing; rebuilding the detector configuration.");
      updateCropImageInfo();
      if (croppedBitmap == null || frameToCropTransform == null || cropToFrameTransform == null)
        return;
    }

    ++frameNum;
    if (binding == null || !isInferenceEnabled()) return;
    if (computingNetwork) return;

    final Detector activeDetector = detector;
    final Bitmap activeCroppedBitmap = croppedBitmap;
    final Matrix activeFrameToCropTransform = frameToCropTransform;
    final Matrix activeCropToFrameTransform = cropToFrameTransform;
    computingNetwork = true;
    runInBackground(
        () -> {
          try {
            final Canvas canvas = new Canvas(activeCroppedBitmap);
            Bitmap workingFrame = bitmap;
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
              Bitmap flipped = CameraUtils.flipBitmapHorizontal(bitmap);
              canvas.drawBitmap(flipped, activeFrameToCropTransform, null);
              workingFrame = flipped;
            } else {
              canvas.drawBitmap(bitmap, activeFrameToCropTransform, null);
            }

            if (activeDetector != null) {
              final long startTime = SystemClock.elapsedRealtime();
              final List<Detector.Recognition> results =
                  activeDetector.recognizeImage(activeCroppedBitmap, classType);
              lastProcessingTimeMs = SystemClock.elapsedRealtime() - startTime;

              final List<Detector.Recognition> mappedRecognitions = new ArrayList<>();
              for (final Detector.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= minConfidence) {
                  activeCropToFrameTransform.mapRect(location);
                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
              }

              int frameW = getMaxAnalyseImageSize().getWidth();
              int frameH = getMaxAnalyseImageSize().getHeight();
              targetTrackManager.update(
                  mappedRecognitions, frameW, frameH, SystemClock.elapsedRealtime());
              FollowState currentState = stateMachine.getState();
              Detector.Recognition largestPerson = selectLargest(mappedRecognitions);
              if (currentState == FollowState.CAPTURE_TARGET) {
                if (reidCoordinator != null) {
                  reidCoordinator.collectInitializationCandidate(
                      workingFrame, largestPerson, sensorOrientation);
                }
                maybeSaveGalleryCandidate(workingFrame, largestPerson, sensorOrientation);
              }
              TargetMatcher.MatchResult legacyMatch =
                  matcher.match(
                      mappedRecognitions, workingFrame, stateMachine.getMemory(), frameW, frameH);
              IdentityEvidence identity =
                  reidCoordinator == null
                      ? null
                      : reidCoordinator.evaluate(
                          mappedRecognitions,
                          workingFrame,
                          stateMachine.getMemory(),
                          currentState,
                          frameW,
                          frameH,
                          sensorOrientation,
                          legacyMatch.score,
                          legacyMatch.matched,
                          legacyMatch.best);
              if (identity != null) {
                TargetTrack reidCandidateTrack =
                    targetTrackManager.getTrackForRecognition(identity.bestCandidate);
                identity =
                    beliefAccumulator.update(
                        identity,
                        targetTrackManager,
                        reidCandidateTrack,
                        stateMachine.getMemory(),
                        frameW,
                        frameH);
              }
              FollowStateMachine.FrameResult fr =
                  stateMachine.onFrame(
                      mappedRecognitions,
                      workingFrame,
                      frameW,
                      frameH,
                      sensorOrientation,
                      identity);
              fr.behaviorDecision = decideBehavior(fr, frameW, frameH);
              maybeRelockAfterRecovery(fr);
              enrichFrameResult(
                  fr, frameW, frameH, sensorOrientation, SystemClock.elapsedRealtime());

              updateDrawState(fr, frameW, frameH, sensorOrientation);
              String commandText = commandForState(fr);
              updateCommandText(commandText);
              float fps = lastProcessingTimeMs > 0 ? 1000f / lastProcessingTimeMs : 0f;
              maybeSaveDiagnostics(
                  workingFrame, fr, fps, commandText, frameW, frameH, sensorOrientation);
              updateDebugInfo(
                  fr.state,
                  fr.control,
                  fr.persons.size(),
                  fps,
                  fr.distanceEstimate,
                  fr.behaviorDecision,
                  fr.identityEvidence,
                  fr.steeringEvidence);
              updateUiForState(fr);
              onFollowFrame(fr);
              binding.trackingOverlay.postInvalidate();
            }
          } catch (RuntimeException e) {
            Timber.e(e, "Cart follow inference failed.");
            onInferenceFailure(e);
          } finally {
            computingNetwork = false;
          }
        });
  }

  private void updateCropImageInfo() {
    sensorOrientation = 90 - ImageUtils.getScreenOrientation(requireActivity());
    recreateNetwork(getModel(), getDevice(), getNumThreads());
  }

  protected boolean isInferenceEnabled() {
    return binding.startSwitch.isChecked();
  }

  /** Receives the final behavior decision after all identity and safety arbitration. */
  protected void onFollowFrame(FollowStateMachine.FrameResult frameResult) {}

  /** Called after an inference task fails so real vehicle screens can stop safely. */
  protected void onInferenceFailure(RuntimeException error) {}

  private void maybeRelockAfterRecovery(FollowStateMachine.FrameResult fr) {
    if (fr == null || fr.identityEvidence == null || fr.behaviorDecision == null) {
      resetRecoveryRelock();
      return;
    }
    IdentityEvidence identity = fr.identityEvidence;
    if (!isRelockState(fr.state)
        || !isRelockAction(fr.behaviorDecision.selectedAction)
        || identity.trackId < 0
        || identity.trackId == identity.lockedTrackId
        || !passesRelockMotionGate(identity)) {
      resetRecoveryRelock();
      return;
    }

    if (recoveryRelockTrackId != identity.trackId) {
      recoveryRelockTrackId = identity.trackId;
      recoveryRelockFrames = 1;
      return;
    }
    recoveryRelockFrames++;
    if (recoveryRelockFrames < RECOVERY_RELOCK_MIN_FRAMES) return;

    if (targetTrackManager.lockTrack(identity.trackId, "relock_after_recovery")) {
      beliefAccumulator.lockTrack(identity.trackId);
      fr.behaviorDecision =
          new BehaviorDecisionResult(
              fr.behaviorDecision.state,
              fr.behaviorDecision.selectedAction,
              appendActionReason(fr.behaviorDecision.actionReason, "relock_after_recovery"),
              fr.behaviorDecision.safetyBlockReason,
              fr.behaviorDecision.confidence,
              fr.behaviorDecision.distanceEvidence,
              fr.behaviorDecision.traversabilityEvidence);
    }
    resetRecoveryRelock();
  }

  private static boolean isRelockState(FollowState state) {
    return state == FollowState.REACQUIRE_TARGET
        || state == FollowState.READY_TO_FOLLOW
        || state == FollowState.FOLLOW_CAUTION
        || state == FollowState.FOLLOW;
  }

  private static boolean isRelockAction(BehaviorAction action) {
    return action == BehaviorAction.FOLLOW_CAUTION || action == BehaviorAction.FOLLOW_SLOW;
  }

  private static boolean passesRelockMotionGate(IdentityEvidence identity) {
    return identity.bboxDefaultOk() || identity.predictionOk();
  }

  private void resetRecoveryRelock() {
    recoveryRelockTrackId = -1;
    recoveryRelockFrames = 0;
  }

  private static String appendActionReason(String reason, String addition) {
    if (reason == null || reason.isEmpty()) return addition;
    if (reason.contains(addition)) return reason;
    return reason + "|" + addition;
  }

  private synchronized void updateDrawState(
      FollowStateMachine.FrameResult fr, int frameW, int frameH, int sensorOrientation) {
    drawBoxes.clear();
    for (Detector.Recognition r : fr.persons) {
      if (r == null || r.getLocation() == null) continue;
      int colorType = COLOR_NORMAL;
      TargetTrack track = targetTrackManager.getTrackForRecognition(r);
      if (track != null && targetTrackManager.isLockedTrack(track)) {
        colorType = COLOR_TARGET;
      } else if (track != null && track.trackId == targetTrackManager.getSuspectedTrackId()) {
        colorType = COLOR_CANDIDATE;
      } else if (r == fr.target) {
        colorType = fr.matched ? COLOR_TARGET : COLOR_FAIL;
      } else if (r == fr.candidate) {
        colorType = COLOR_CANDIDATE;
      }
      String label = null;
      if (track != null) {
        label =
            String.format(
                Locale.US, "T%d b=%.2f", track.trackId, beliefAccumulator.getBeliefForTrack(track));
      }
      drawBoxes.add(new DrawBox(new RectF(r.getLocation()), colorType, label));
    }
    drawFrameWidth = frameW;
    drawFrameHeight = frameH;
    drawSensorOrientation = sensorOrientation;
  }

  private void drawOverlay(Canvas canvas) {
    if (drawFrameWidth <= 0 || drawFrameHeight <= 0) return;
    final boolean rotated = drawSensorOrientation % 180 == 90;
    final float multiplier =
        Math.min(
            canvas.getHeight() / (float) (rotated ? drawFrameWidth : drawFrameHeight),
            canvas.getWidth() / (float) (rotated ? drawFrameHeight : drawFrameWidth));
    Matrix matrix =
        ImageUtils.getTransformationMatrix(
            drawFrameWidth,
            drawFrameHeight,
            (int) (multiplier * (rotated ? drawFrameHeight : drawFrameWidth)),
            (int) (multiplier * (rotated ? drawFrameWidth : drawFrameHeight)),
            drawSensorOrientation,
            new RectF(0, 0, 0, 0),
            false);

    List<DrawBox> snapshot;
    synchronized (this) {
      snapshot = new ArrayList<>(drawBoxes);
    }
    for (DrawBox box : snapshot) {
      RectF rect = new RectF(box.location);
      matrix.mapRect(rect);
      Paint paint;
      String label = box.label;
      switch (box.colorType) {
        case COLOR_TARGET:
          paint = targetBoxPaint;
          if (label == null) label = "目标";
          break;
        case COLOR_CANDIDATE:
          paint = candidateBoxPaint;
          if (label == null) label = "候选";
          break;
        case COLOR_FAIL:
          paint = failBoxPaint;
          if (label == null) label = "匹配失败";
          break;
        default:
          paint = personBoxPaint;
          break;
      }
      float cornerSize = Math.min(rect.width(), rect.height()) / 8.0f;
      canvas.drawRoundRect(rect, cornerSize, cornerSize, paint);
      if (label != null) {
        canvas.drawText(label, rect.left + cornerSize, rect.top, boxTextPaint);
      }
    }
  }

  private String commandForState(FollowStateMachine.FrameResult fr) {
    if (fr.behaviorDecision != null) {
      switch (fr.behaviorDecision.selectedAction) {
        case LOCAL_SEARCH_LEFT:
          return HumanCommandInterpreter.CMD_TURN_LEFT;
        case LOCAL_SEARCH_RIGHT:
          return HumanCommandInterpreter.CMD_TURN_RIGHT;
        case BLOCKED_WAIT:
          return "前方受阻，请停止等待";
        case MOTION_STOP:
        case HARD_STOP:
        case EMERGENCY_STOP:
          return HumanCommandInterpreter.CMD_STOP;
        case REACQUIRE_HOLD:
          return "疑似目标，请停止确认";
        case FOLLOW_SLOW:
        case FOLLOW_CAUTION:
        default:
          break;
      }
    }
    switch (fr.state) {
      case IDLE:
        return "待命，打开 Start 开始采集目标";
      case CAPTURE_TARGET:
        return "采集中，请保持站立";
      case LOCKED_PENDING_CONFIRM:
        return "请确认是否跟随此人";
      case CONFIRMED_ARMED:
        return "已确认，请回到车前";
      case REACQUIRE_TARGET:
        return "重识别中…";
      case READY_TO_FOLLOW:
        return fr.countdownSec >= 0 ? fr.countdownSec + " 秒后启动" : "准备启动";
      case FOLLOW:
      case FOLLOW_CAUTION:
        if (fr.distanceEstimate != null) {
          if (fr.steeringEvidence != null) {
            return interpreter.interpret(fr.steeringEvidence, fr.distanceEstimate.state);
          }
          return interpreter.interpret(fr.control, fr.state, fr.distanceEstimate.state);
        }
        return interpreter.interpret(fr.control, fr.state, fr.tooClose);
      case IDENTITY_UNCERTAIN:
        return "身份不确定，请停止";
      case LOST:
        return "目标丢失，请停止";
      case SEARCH:
        return "原地搜索中…";
      case STOP:
        return "已停止";
      default:
        return "请停止";
    }
  }

  protected final void updateCommandText(String text) {
    if (binding == null) return;
    requireActivity().runOnUiThread(() -> binding.commandText.setText(text));
  }

  synchronized void clearDrawState() {
    drawBoxes.clear();
    drawFrameWidth = 0;
    drawFrameHeight = 0;
    drawSensorOrientation = 0;
    if (binding != null) binding.trackingOverlay.postInvalidate();
  }

  private void updateUiForState(FollowStateMachine.FrameResult fr) {
    if (binding == null) return;
    requireActivity()
        .runOnUiThread(
            () -> {
              if (binding == null) return;
              boolean showConfirm =
                  updateConfirmationVisibility(
                      binding.confirmPanel, binding.startSwitch.isChecked(), fr.state);
              if (showConfirm && fr.snapshot != null) {
                latestConfirmSnapshot = fr.snapshot;
                binding.snapshotView.setImageBitmap(fr.snapshot);
              }
              boolean showCountdown = fr.state == FollowState.READY_TO_FOLLOW;
              binding.countdownText.setVisibility(showCountdown ? View.VISIBLE : View.GONE);
              if (showCountdown) {
                binding.countdownText.setText(
                    fr.countdownSec >= 0 ? String.valueOf(fr.countdownSec) : "");
              }
            });
  }

  private void startDiagnosticSession() {
    stopDiagnosticSession();
    if (!diagnosticEnabled) {
      resetDiagnosticState();
      return;
    }
    diagnosticSession = new CartFollowDiagnosticSession(requireContext().getApplicationContext());
    diagnosticSession.initCsvFiles();
    diagnosticActive = false;
    targetEventAwaitingReturn = false;
    latestConfirmSnapshot = null;
    lastDiagnosticFrameLogMs = 0L;
    lastDiagnosticCropMs = 0L;
    lastDiagnosticGalleryMs = 0L;
    resetTargetEventButton();
  }

  private void activateDiagnosticSession() {
    if (!diagnosticEnabled) {
      resetDiagnosticState();
      return;
    }
    if (diagnosticSession == null) {
      startDiagnosticSession();
    }
    if (diagnosticSession == null) return;
    diagnosticActive = true;
    targetEventAwaitingReturn = false;
    String detectorName = getModel() == null ? "" : getModel().name;
    boolean reidAvailable = reidCoordinator != null && reidCoordinator.isAvailable();
    int gallerySize = reidCoordinator == null ? 0 : reidCoordinator.getGallerySize();
    diagnosticSession.writeSessionInfo(
        diagnosticConfig,
        detectorName,
        minConfidence,
        reidAvailable,
        gallerySize,
        true,
        sensorOrientation);
    resetTargetEventButton();
    Toast.makeText(
            requireContext(),
            "Diagnostic: " + diagnosticSession.sessionDir.getAbsolutePath(),
            Toast.LENGTH_SHORT)
        .show();
  }

  private void stopDiagnosticSession() {
    if (diagnosticEnabled && diagnosticSession != null && diagnosticActive) {
      diagnosticSaver.saveEventAsync(diagnosticSession, frameNum, "session_stop", "");
    }
    resetDiagnosticState();
  }

  private void resetDiagnosticState() {
    diagnosticActive = false;
    diagnosticSession = null;
    targetEventAwaitingReturn = false;
    lastDiagnosticFrameLogMs = 0L;
    lastDiagnosticCropMs = 0L;
    lastDiagnosticGalleryMs = 0L;
    resetTargetEventButton();
  }

  private void resetTargetEventButton() {
    if (binding == null) return;
    Activity activity = getActivity();
    if (activity == null) return;
    activity.runOnUiThread(
        () -> {
          if (binding == null) return;
          binding.btnTargetEvent.setEnabled(diagnosticEnabled && diagnosticActive);
          binding.btnTargetEvent.setText(targetEventAwaitingReturn ? "目标回到画面" : "目标离开画面");
        });
  }

  private void recordTargetEvent() {
    if (!diagnosticEnabled || !diagnosticActive || diagnosticSession == null) return;
    String eventType = targetEventAwaitingReturn ? "target_return" : "target_left";
    diagnosticSaver.saveEventAsync(diagnosticSession, frameNum, eventType, "");
    targetEventAwaitingReturn = !targetEventAwaitingReturn;
    resetTargetEventButton();
  }

  private void maybeSaveDiagnostics(
      Bitmap workingFrame,
      FollowStateMachine.FrameResult fr,
      float fps,
      String commandText,
      int frameW,
      int frameH,
      int sensorOrientation) {
    if (!diagnosticEnabled || !diagnosticActive || diagnosticSession == null || fr == null) return;
    long now = SystemClock.elapsedRealtime();
    boolean shouldLog =
        lastDiagnosticFrameLogMs == 0L
            || now - lastDiagnosticFrameLogMs >= diagnosticConfig.frameLogIntervalMs;
    boolean shouldSaveCrop =
        lastDiagnosticCropMs == 0L || now - lastDiagnosticCropMs >= diagnosticConfig.cropIntervalMs;
    if (!shouldLog && !shouldSaveCrop) return;
    if (shouldLog) lastDiagnosticFrameLogMs = now;
    if (shouldSaveCrop) lastDiagnosticCropMs = now;

    Detector.Recognition locked = recognitionForTrack(targetTrackManager.getLockedTrack());
    TargetTrack suspectedTrack =
        targetTrackManager.getTrackById(targetTrackManager.getSuspectedTrackId());
    Detector.Recognition suspected = recognitionForTrack(suspectedTrack);
    Detector.Recognition bestReid =
        reidCoordinator == null ? null : reidCoordinator.getLastBestCandidate();
    diagnosticSaver.saveFrameAsync(
        workingFrame,
        diagnosticSession,
        diagnosticConfig,
        frameNum,
        frameW,
        frameH,
        sensorOrientation,
        fps,
        fr.persons == null ? 0 : fr.persons.size(),
        fr.state.name(),
        fr.behaviorDecision,
        commandText,
        fr.identityEvidence,
        fr.steeringEvidence,
        locked,
        suspected,
        bestReid,
        shouldSaveCrop);
  }

  private void maybeSaveGalleryCandidate(
      Bitmap frame, Detector.Recognition candidate, int sensorOrientation) {
    if (!diagnosticEnabled || diagnosticSession == null || frame == null || candidate == null)
      return;
    if (candidate.getLocation() == null) return;
    long now = SystemClock.elapsedRealtime();
    if (lastDiagnosticGalleryMs != 0L
        && now - lastDiagnosticGalleryMs < diagnosticConfig.cropIntervalMs) {
      return;
    }
    Bitmap crop =
        cropPerson(
            frame, candidate.getLocation(), diagnosticConfig.paddingRatio, sensorOrientation);
    if (crop == null) return;
    lastDiagnosticGalleryMs = now;
    diagnosticSaver.saveGallerySnapshotAsync(
        crop, diagnosticSession, "gallery_candidate_" + frameNum);
    crop.recycle();
  }

  private static Detector.Recognition recognitionForTrack(TargetTrack track) {
    return track == null || !track.isVisible() ? null : track.recognition;
  }

  private static Bitmap cropPerson(
      Bitmap frame, RectF bbox, float paddingRatio, int sensorOrientation) {
    if (frame == null || bbox == null) return null;
    float padX = bbox.width() * paddingRatio;
    float padY = bbox.height() * paddingRatio;
    int left = clamp((int) (bbox.left - padX), 0, frame.getWidth() - 1);
    int top = clamp((int) (bbox.top - padY), 0, frame.getHeight() - 1);
    int right = clamp((int) (bbox.right + padX), left + 1, frame.getWidth());
    int bottom = clamp((int) (bbox.bottom + padY), top + 1, frame.getHeight());
    int width = right - left;
    int height = bottom - top;
    if (width <= 0 || height <= 0) return null;
    try {
      Bitmap rawCrop = Bitmap.createBitmap(frame, left, top, width, height);
      int rotation = ((sensorOrientation % 360) + 360) % 360;
      if (rotation == 0) return rawCrop;
      Matrix matrix = new Matrix();
      matrix.postRotate(rotation);
      Bitmap upright =
          Bitmap.createBitmap(rawCrop, 0, 0, rawCrop.getWidth(), rawCrop.getHeight(), matrix, true);
      rawCrop.recycle();
      return upright;
    } catch (Exception e) {
      return null;
    }
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private void updateDebugInfo(
      FollowState state,
      Control control,
      int persons,
      float fps,
      ImageSetpointDistanceEstimator.DistanceEstimate dist,
      BehaviorDecisionResult behaviorDecision,
      IdentityEvidence identityEvidence,
      SteeringEvidence steeringEvidence) {
    if (binding == null) return;
    float forward = (control.getLeft() + control.getRight()) / 2f;
    float turn = (control.getRight() - control.getLeft()) / 2f;
    String distLine;
    if (dist != null) {
      distLine =
          String.format(
              Locale.US,
              "dist=%s\nhScale=%.2f\naScale=%.2f\nbShift=%+.3f\ndistConf=%.2f",
              dist.state.name(),
              dist.heightScale,
              dist.areaScale,
              dist.bottomShift,
              dist.confidence);
    } else {
      distLine = "dist=-";
    }
    String behaviorLine;
    if (behaviorDecision != null) {
      behaviorLine =
          String.format(
              Locale.US,
              "action=%s\nactionReason=%s\nsafetyBlock=%s\nactionConf=%.2f",
              behaviorDecision.selectedAction.name(),
              behaviorDecision.actionReason,
              behaviorDecision.safetyBlockReason == null ? "-" : behaviorDecision.safetyBlockReason,
              behaviorDecision.confidence);
      if (behaviorDecision.traversabilityEvidence != null) {
        TraversabilityEvidence trav = behaviorDecision.traversabilityEvidence;
        behaviorLine +=
            String.format(
                Locale.US,
                "\ncenterBlocked=%s\nfreeLCR=%.2f/%.2f/%.2f\ntravReason=%s",
                trav.centerBlocked,
                trav.leftFreeScore,
                trav.centerFreeScore,
                trav.rightFreeScore,
                trav.reason);
      }
    } else {
      behaviorLine = "action=-\nactionReason=-\nsafetyBlock=-\nactionConf=0.00";
    }
    String identityLine = buildIdentityDebugLine(identityEvidence);
    String steeringLine = buildSteeringDebugLine(steeringEvidence);
    String fullInfo =
        String.format(
            Locale.US,
            "state=%s\nforward=%.2f\nturn=%.2f\nleft=%.2f\nright=%.2f\npersons=%d\nfps=%.1f\n%s\n%s\n%s\n%s",
            state.name(),
            forward,
            turn,
            control.getLeft(),
            control.getRight(),
            persons,
            fps,
            distLine,
            behaviorLine,
            identityLine,
            steeringLine);
    String compactInfo =
        String.format(
            Locale.US,
            "fps=%.1f\nstate=%s\naction=%s\npersons=%d\ntrack=%d locked=%d suspected=%d\nbelief=%.2f\nbest=%.3f margin=%.3f\nreidCrop=upright",
            fps,
            state.name(),
            behaviorDecision == null ? "-" : behaviorDecision.selectedAction.name(),
            persons,
            identityEvidence == null ? -1 : identityEvidence.trackId,
            identityEvidence == null ? -1 : identityEvidence.lockedTrackId,
            identityEvidence == null ? -1 : identityEvidence.suspectedTrackId,
            identityEvidence == null ? 0f : identityEvidence.targetBelief,
            identityEvidence == null || identityEvidence.reidMatch == null
                ? 0f
                : identityEvidence.reidMatch.bestScore,
            identityEvidence == null || identityEvidence.reidMatch == null
                ? 0f
                : identityEvidence.reidMatch.margin);
    String info = showFullDebug ? fullInfo : compactInfo;
    requireActivity().runOnUiThread(() -> binding.debugInfo.setText(info));
  }

  private String buildIdentityDebugLine(IdentityEvidence identity) {
    if (identity == null) {
      return "reidAvailable=false\nreidCrop=upright\ngallerySize=0\nbestScore=0.000\nsecondScore=0.000\nmargin=0.000\nweak/mid/strong=false/false/false\nbboxLoose=false bboxDefault=false bboxStrict=false prediction=false\nstableMatchCount=0\ncandidateSwitchCount=0\nreidLatencyMs=0\nreidReason=-\nactiveTrackCount=0\ntrackId=-1 lockedTrackId=-1 suspectedTrackId=-1\ntrackAge=0 missedFrames=0\nbelief=0.00 beliefStable=0 beliefUncertain=0\nbeliefReason=-";
    }
    ReIDMatchResult reid = identity.reidMatch;
    BboxContinuityEvidence bbox = identity.bboxContinuity;
    boolean reidAvailable = reid != null && reid.reidAvailable;
    int gallerySize = reid == null ? 0 : reid.gallerySize;
    float best = reid == null ? 0f : reid.bestScore;
    float second = reid == null ? 0f : reid.secondScore;
    float margin = reid == null ? 0f : reid.margin;
    long latency = reid == null ? 0L : reid.latencyMs;
    String reason = reid == null ? identity.reason : reid.reason;
    return String.format(
        Locale.US,
        "reidAvailable=%s\nreidCrop=upright\ngallerySize=%d\nbestScore=%.3f\nsecondScore=%.3f\nmargin=%.3f\nweak/mid/strong=%s/%s/%s\nbboxLoose=%s bboxDefault=%s bboxStrict=%s prediction=%s\nstableMatchCount=%d\ncandidateSwitchCount=%d\nreidLatencyMs=%d\nreidReason=%s\nactiveTrackCount=%d\ntrackId=%d lockedTrackId=%d suspectedTrackId=%d\ntrackAge=%d missedFrames=%d\nbelief=%.2f reidC=%.2f bboxC=%.2f predC=%.2f switchP=%.2f\nbeliefStable=%d beliefUncertain=%d\nbeliefReason=%s",
        reidAvailable,
        gallerySize,
        best,
        second,
        margin,
        identity.weakOk(),
        identity.midOk(),
        identity.strongOk(),
        bbox != null && bbox.looseAdmissionOk,
        bbox != null && bbox.bboxDefaultOk,
        bbox != null && bbox.bboxStrictOk,
        bbox != null && bbox.predictionOk,
        identity.stableMatchCount,
        identity.candidateSwitchCount,
        latency,
        reason == null ? "-" : reason,
        identity.activeTrackCount,
        identity.trackId,
        identity.lockedTrackId,
        identity.suspectedTrackId,
        identity.trackAge,
        identity.missedFrames,
        identity.targetBelief,
        identity.reidContribution,
        identity.bboxContribution,
        identity.predictionContribution,
        identity.switchPenalty,
        identity.beliefStableFrames,
        identity.beliefUncertainFrames,
        identity.beliefReason == null ? "-" : identity.beliefReason);
  }

  private String buildSteeringDebugLine(SteeringEvidence evidence) {
    if (evidence == null || !evidence.valid) {
      return "steering=unavailable";
    }
    return String.format(
        Locale.US,
        "steering=%s%s demand=%d%%\nraw=%+.3f filtered=%+.3f rate=%+.3f/s predicted=%+.3f edge=%.2f horizon=%dms",
        evidence.directionLabel(),
        evidence.levelLabel(),
        evidence.demandPercent,
        evidence.rawError,
        evidence.filteredError,
        evidence.lateralRatePerSec,
        evidence.predictedError,
        evidence.edgeUrgency,
        evidence.predictionHorizonMs);
  }

  private BehaviorDecisionResult decideBehavior(
      FollowStateMachine.FrameResult fr, int frameW, int frameH) {
    IdentityEvidence identity =
        fr.identityEvidence != null
            ? fr.identityEvidence
            : new IdentityEvidence(
                fr.matched ? fr.matchScore : 0f,
                fr.matchScore,
                fr.matched,
                fr.matched ? "matched" : "not_matched");
    DistanceEvidence distance;
    if (fr.distanceEstimate != null) {
      distance =
          new DistanceEvidence(
              fr.distanceEstimate.state,
              fr.distanceEstimate.confidence,
              fr.distanceEstimate.failureReason);
    } else {
      distance = new DistanceEvidence(DistanceState.UNKNOWN, 0f, "distance_not_available");
    }
    TraversabilityEvidence traversability = estimateTraversability(fr, frameW, frameH);
    SystemSafetyEvidence safety = createSystemSafetyEvidence();
    BehaviorDecisionResult decision =
        actionArbitrator.decide(
            fr.state, identity, distance, traversability, safety, stateMachine.getMemory(), frameW);
    return new BehaviorDecisionResult(
        decision.state,
        decision.selectedAction,
        decision.actionReason,
        decision.safetyBlockReason,
        decision.confidence,
        distance,
        traversability);
  }

  private TraversabilityEvidence estimateTraversability(
      FollowStateMachine.FrameResult fr, int frameW, int frameH) {
    if (fr == null || fr.persons == null || frameW <= 0 || frameH <= 0) {
      return new TraversabilityEvidence(1f, 1f, 1f, false, "default_clear");
    }
    boolean centerBlocked = false;
    float centerFreeScore = 1f;
    for (Detector.Recognition person : fr.persons) {
      if (person == null || person == fr.target || person.getLocation() == null) continue;
      RectF b = person.getLocation();
      float cxRatio = b.centerX() / frameW;
      boolean inCenter = cxRatio >= 0.33f && cxRatio <= 0.67f;
      boolean lowerBodyRisk = b.bottom >= frameH * 0.55f;
      boolean largeEnough = b.width() * b.height() >= frameW * frameH * 0.03f;
      if (inCenter && lowerBodyRisk && largeEnough) {
        centerBlocked = true;
        centerFreeScore = Math.min(centerFreeScore, 0.2f);
      }
    }
    return new TraversabilityEvidence(
        1f,
        centerFreeScore,
        1f,
        centerBlocked,
        centerBlocked ? "non_target_in_center_corridor" : "default_clear");
  }

  protected Model getModel() {
    return model;
  }

  private static Detector.Recognition selectLargest(List<Detector.Recognition> persons) {
    Detector.Recognition target = null;
    float maxArea = -1f;
    if (persons == null) return null;
    for (Detector.Recognition r : persons) {
      if (r == null || r.getLocation() == null) continue;
      RectF loc = r.getLocation();
      float area = loc.width() * loc.height();
      if (area > maxArea) {
        maxArea = area;
        target = r;
      }
    }
    return target;
  }

  @Override
  protected void setModel(Model model) {
    if (this.model != model) {
      this.model = model;
      preferencesManager.setObjectNavModel(model.name);
      onInferenceConfigurationChanged();
    }
  }

  protected Network.Device getDevice() {
    return device;
  }

  protected int getNumThreads() {
    return numThreads;
  }

  private static class DrawBox {
    final RectF location;
    final int colorType;
    final String label;

    DrawBox(RectF location, int colorType, String label) {
      this.location = location;
      this.colorType = colorType;
      this.label = label;
    }
  }

  protected SystemSafetyEvidence createSystemSafetyEvidence() {
    return new SystemSafetyEvidence(
        false, true, detector != null, detector == null ? "detector_not_ready" : "ok");
  }

  protected boolean isDetectorReady() {
    return detector != null;
  }
}
