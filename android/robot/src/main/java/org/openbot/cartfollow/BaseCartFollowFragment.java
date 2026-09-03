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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
  private static final int COLOR_LOW_CONFIDENCE = 4;
  private static final int RECOVERY_RELOCK_MIN_FRAMES = 2;

  protected FragmentHumanCartSimulatorBinding binding;
  private Handler handler;
  private HandlerThread handlerThread;

  private final AtomicBoolean computingNetwork = new AtomicBoolean(false);
  private final AtomicLong modelConfigGeneration = new AtomicLong(0L);
  private volatile boolean modelConfigPending;
  private float minConfidence = 0.5f;

  private volatile InferenceResources inferenceResources;
  private int sensorOrientation;

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
  private volatile CartFollowDiagnosticSession diagnosticSession;
  private CartFollowDiagnosticSession closingDiagnosticSession;
  private long loggedGalleryRevision = -1;
  private boolean diagnosticViewActive;
  private boolean diagnosticEnabled = false;
  private boolean diagnosticActive = false;
  private boolean targetEventAwaitingReturn = false;
  private boolean showFullDebug = false;
  private long lastDiagnosticFrameLogMs = 0L;
  private long lastDiagnosticCropMs = 0L;
  private long lastDiagnosticGalleryMs = 0L;
  private long lastPresentationLogMs;
  private int recoveryRelockTrackId = -1;
  private int recoveryRelockFrames = 0;
  private final GlobalReacquireGate globalReacquireGate = new GlobalReacquireGate();
  private final SimulatorIdentityGuard simulatorIdentityGuard = new SimulatorIdentityGuard();
  private final SimulatorContinuityTracker simulatorContinuity = new SimulatorContinuityTracker();

  protected final void configureRecentGallery(boolean enabled) {
    if (reidCoordinator != null) reidCoordinator.setRecentEnabled(enabled);
  }

  private List<Detector.Recognition> lastPresentedPersons = java.util.Collections.emptyList();
  private boolean enhancedRecoveryEnabled;
  private FollowPolicy followPolicy =
      new FollowPolicy(false, GalleryUpdateStatus.Mode.STATIC, false, false);
  private boolean dualConfidenceEnabled;
  private long lastSavedAdaptiveRevision;
  private Bitmap latestConfirmSnapshot;
  private volatile long uiGeneration;
  private long latestAppliedFrameSequence;
  private final ThreadLocal<long[]> admittedUiFrame = new ThreadLocal<>();
  private long droppedInferenceFrames;
  private long fpsWindowStartMs;
  private int completedFrames;
  private float completedFps;
  private long lastSteeringObservationMs = -1L;
  private volatile long drawObservedAtMs;

  private final List<DrawBox> drawBoxes = new ArrayList<>();
  private int drawFrameWidth = 0;
  private int drawFrameHeight = 0;
  private int drawSensorOrientation = 0;

  private final Paint targetBoxPaint = new Paint();
  private final Paint candidateBoxPaint = new Paint();
  private final Paint personBoxPaint = new Paint();
  private final Paint failBoxPaint = new Paint();
  private final Paint lowConfidenceBoxPaint = new Paint();
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
    lowConfidenceBoxPaint.setColor(Color.LTGRAY);
    lowConfidenceBoxPaint.setStyle(Paint.Style.STROKE);
    lowConfidenceBoxPaint.setStrokeWidth(5.0f);
    lowConfidenceBoxPaint.setPathEffect(
        new android.graphics.DashPathEffect(new float[] {18f, 12f}, 0f));
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
    diagnosticViewActive = true;
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
    binding.diagnosticHealth.post(
        new Runnable() {
          @Override
          public void run() {
            if (!diagnosticViewActive || binding == null) return;
            CartFollowDiagnosticSession session =
                diagnosticSession != null ? diagnosticSession : closingDiagnosticSession;
            binding.diagnosticHealth.setText(
                session == null
                    ? "日志关闭"
                    : session.io.isClosed()
                        ? session.io.error.isEmpty()
                            ? "记录已保存，可在测试记录中导出"
                            : "日志写入异常：" + session.io.error
                        : session.health());
            binding.diagnosticHealth.postDelayed(this, 1000);
          }
        });
    resetTargetEventButton();
    binding.btnTargetEvent.setOnClickListener(v -> recordTargetEvent());
    binding.btnTestRecords.setOnClickListener(
        v -> {
          if (binding.startSwitch.isChecked()) {
            Toast.makeText(requireContext(), "请先停止测试，再查看或导出记录", Toast.LENGTH_SHORT).show();
            return;
          }
          startActivity(
              new android.content.Intent(
                  requireContext(),
                  org.openbot.cartfollow.diagnostics.DiagnosticRecordsActivity.class));
        });
    binding.diagnosticSwitch.setChecked(false);
    onDiagnosticLoggingChanged(false);
    binding.diagnosticSwitch.setOnClickListener(
        v -> {
          diagnosticEnabled = binding.diagnosticSwitch.isChecked();
          onDiagnosticLoggingChanged(diagnosticEnabled);
          if (!diagnosticEnabled) stopDiagnosticSession();
          else startDiagnosticSession();
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
          recordControlEvent("target_confirmed", "locked_track=" + lockedTrackId);
          stateMachine.confirm();
          invalidatePendingUiSnapshots();
          if (enhancedRecoveryEnabled) rememberDistractors(lockedTrackId, lastPresentedPersons);
          binding.confirmPanel.setVisibility(View.GONE);
          binding.countdownText.setVisibility(View.GONE);
          updateCommandText("已确认，请回到车前");
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
          invalidatePendingUiSnapshots();
        });
    binding.btnCancel.setOnClickListener(
        v -> {
          if (binding.startSwitch.isChecked()) binding.startSwitch.setChecked(false);
          onFollowEnabledChanged(false);
          resetFollowSession();
        });

    binding.startSwitch.setChecked(false);
    binding.startSwitch.setOnClickListener(
        v -> {
          if (binding.startSwitch.isChecked()) {
            binding.modelSpinner.setEnabled(false);
            if (reidCoordinator != null) reidCoordinator.reset();
            targetTrackManager.reset();
            beliefAccumulator.reset();
            globalReacquireGate.reset();
            resetRecoveryRelock();
            startDiagnosticSession();
            stateMachine.startCapture();
            recordControlEvent("capture_start", "start_enabled");
            invalidatePendingUiSnapshots();
            onFollowEnabledChanged(true);
          } else {
            onFollowEnabledChanged(false);
            resetFollowSession();
          }
        });
    onCartFollowViewCreated();
  }

  /** Hook for concrete screens to install controls without duplicating the perception pipeline. */
  protected void onCartFollowViewCreated() {}

  /** Lets real hardware screens stop synchronously when the shared Start switch changes. */
  protected void onFollowEnabledChanged(boolean enabled) {}

  protected final void configureSimulatorExperiments(
      GalleryUpdateStatus.Mode galleryMode, boolean enhancedRecovery) {
    configureFollowPolicy(new FollowPolicy(enhancedRecovery, galleryMode, true, true));
  }

  protected final void configureFollowPolicy(FollowPolicy policy) {
    followPolicy = policy;
    boolean enhancedRecovery = policy.enhancedIdentity;
    GalleryUpdateStatus.Mode galleryMode = policy.galleryMode;
    enhancedRecoveryEnabled = enhancedRecovery;
    dualConfidenceEnabled = enhancedRecovery;
    stateMachine.setSimulatorFastRecoveryEnabled(enhancedRecovery);
    stateMachine.getMemory().setBoundedColorSampling(enhancedRecovery);
    targetTrackManager.setGlobalAssociationEnabled(enhancedRecovery);
    beliefAccumulator.setStrictReidProvenance(enhancedRecovery);
    if (reidCoordinator != null) {
      reidCoordinator.setEnhancedRecovery(enhancedRecovery);
      reidCoordinator.setGalleryMode(galleryMode);
    }
    globalReacquireGate.reset();
    lastSavedAdaptiveRevision = 0L;
  }

  protected final GalleryUpdateStatus getGalleryUpdateStatus() {
    return reidCoordinator == null ? null : reidCoordinator.getGalleryStatus();
  }

  protected void onGalleryStatusUpdated(GalleryUpdateStatus status) {}

  /** Shared continuous steering evidence used by both simulator and real-cart follow screens. */
  protected final SteeringDemandEstimator steeringDemandEstimator = new SteeringDemandEstimator();

  /** Concrete screens may select a fixed or user-configured prediction horizon. */
  protected int steeringPredictionHorizonMs() {
    return 400;
  }

  /** Lets a concrete screen attach non-control evidence to a completed perception frame. */
  protected void enrichFrameResult(
      FollowStateMachine.FrameResult frameResult,
      int frameW,
      int frameH,
      int sensorOrientation,
      long nowMs) {
    int horizonMs = steeringPredictionHorizonMs();
    if (enhancedRecoveryEnabled && frameResult != null) {
      TargetObservationEvidence observation = frameResult.targetObservation;
      if (observation != null && observation.current) {
        RectF box = observation.screenBox;
        frameResult.steeringEvidence =
            steeringDemandEstimator.update(
                new RectF(box.left * 1000f, box.top * 1000f, box.right * 1000f, box.bottom * 1000f),
                1000,
                1000,
                0,
                observation.trackId,
                observation.observedAtMs,
                horizonMs);
        lastSteeringObservationMs = observation.observedAtMs;
      } else {
        if (lastSteeringObservationMs < 0 || nowMs - lastSteeringObservationMs > 500L) {
          steeringDemandEstimator.reset();
        }
        frameResult.steeringEvidence =
            SteeringEvidence.unavailable("no_current_observation", horizonMs);
      }
      return;
    }
    boolean following =
        frameResult != null
            && (frameResult.state == FollowState.FOLLOW
                || frameResult.state == FollowState.FOLLOW_CAUTION)
            && frameResult.target != null
            && frameResult.target.getLocation() != null;
    if (!following) {
      steeringDemandEstimator.reset();
      if (frameResult != null) {
        frameResult.steeringEvidence = SteeringEvidence.unavailable("not_following", horizonMs);
      }
      return;
    }
    int trackId = frameResult.identityEvidence == null ? -1 : frameResult.identityEvidence.trackId;
    frameResult.steeringEvidence =
        steeringDemandEstimator.update(
            new RectF(frameResult.target.getLocation()),
            frameW,
            frameH,
            sensorOrientation,
            trackId,
            nowMs,
            horizonMs);
  }

  /** Lets a concrete screen reset its own per-session visual state. */
  protected void onFollowSessionReset() {}

  protected final void resetFollowSession() {
    if (binding == null) return;
    binding.modelSpinner.setEnabled(true);
    if (reidCoordinator != null) reidCoordinator.reset();
    targetTrackManager.reset();
    beliefAccumulator.reset();
    globalReacquireGate.reset();
    resetRecoveryRelock();
    steeringDemandEstimator.reset();
    lastSteeringObservationMs = -1L;
    onFollowSessionReset();
    stopDiagnosticSession();
    stateMachine.cancel();
    invalidatePendingUiSnapshots();
    clearDrawState();
    resetUiToIdle();
  }

  protected final void recaptureSimulatorTarget() {
    if (!enhancedRecoveryEnabled || binding == null) return;
    resetFollowSession();
    binding.startSwitch.setChecked(true);
    binding.modelSpinner.setEnabled(false);
    startDiagnosticSession();
    stateMachine.startCapture();
    onFollowEnabledChanged(true);
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
    modelConfigGeneration.incrementAndGet();
    modelConfigPending = false;
    InferenceResources stale = inferenceResources;
    inferenceResources = null;
    if (stale != null) runInBackground(() -> closeDetector(stale.detector));
  }

  private void requestNetworkConfiguration(int frameWidth, int frameHeight) {
    if (frameWidth <= 0 || frameHeight <= 0 || modelConfigPending) return;
    Model selectedModel = getModel();
    if (selectedModel == null) return;
    final long generation = modelConfigGeneration.incrementAndGet();
    final int orientation = 90 - ImageUtils.getScreenOrientation(requireActivity());
    final Network.Device selectedDevice = getDevice();
    final int selectedThreads = getNumThreads();
    modelConfigPending = true;
    runInBackground(
        () ->
            recreateNetwork(
                generation,
                selectedModel,
                selectedDevice,
                selectedThreads,
                frameWidth,
                frameHeight,
                orientation));
  }

  private void recreateNetwork(
      long generation,
      Model model,
      Network.Device device,
      int numThreads,
      int frameWidth,
      int frameHeight,
      int orientation) {
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
      postModelErrorIfCurrent(generation, msg);
      return;
    }

    try {
      Bitmap croppedBitmap =
          Bitmap.createBitmap(
              newDetector.getImageSizeX(), newDetector.getImageSizeY(), Bitmap.Config.ARGB_8888);
      Matrix frameToCropTransform =
          ImageUtils.getTransformationMatrix(
              frameWidth,
              frameHeight,
              croppedBitmap.getWidth(),
              croppedBitmap.getHeight(),
              orientation,
              newDetector.getCropRect(),
              newDetector.getMaintainAspect());
      Matrix cropToFrameTransform = new Matrix();
      frameToCropTransform.invert(cropToFrameTransform);
      if (generation != modelConfigGeneration.get() || !isAdded()) {
        closeDetector(newDetector);
        return;
      }
      InferenceResources old = inferenceResources;
      inferenceResources =
          new InferenceResources(
              newDetector,
              croppedBitmap,
              frameToCropTransform,
              cropToFrameTransform,
              frameWidth,
              frameHeight,
              orientation);
      sensorOrientation = orientation;
      if (old != null) closeDetector(old.detector);
    } catch (Exception e) {
      closeDetector(newDetector);
      Timber.e(e, "Failed to configure detector.");
      postModelErrorIfCurrent(generation, "模型配置失败: " + e.getMessage());
    } finally {
      if (generation == modelConfigGeneration.get()) modelConfigPending = false;
    }
  }

  private void postModelErrorIfCurrent(long generation, String message) {
    if (generation != modelConfigGeneration.get() || !isAdded()) return;
    requireActivity()
        .runOnUiThread(
            () -> {
              if (generation != modelConfigGeneration.get() || !isAdded()) return;
              Toast.makeText(requireContext().getApplicationContext(), message, Toast.LENGTH_LONG)
                  .show();
            });
  }

  private static void closeDetector(Detector detector) {
    if (detector != null) detector.close();
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
    modelConfigGeneration.incrementAndGet();
    modelConfigPending = false;
    if (handlerThread == null) {
      InferenceResources stale = inferenceResources;
      inferenceResources = null;
      closeDetector(stale == null ? null : stale.detector);
      computingNetwork.set(false);
      super.onPause();
      return;
    }
    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
      InferenceResources stale = inferenceResources;
      inferenceResources = null;
      closeDetector(stale == null ? null : stale.detector);
      computingNetwork.set(false);
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

  protected void onDiagnosticSessionChanged(CartFollowDiagnosticSession session) {}

  protected void recordControlEvent(String event, String details) {
    CartFollowDiagnosticSession session = diagnosticSession;
    if (session != null) session.control(event, details);
  }

  @Override
  public void onDestroyView() {
    diagnosticViewActive = false;
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    stopDiagnosticSession();
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
    processFrame(bitmap, image, SystemClock.elapsedRealtime(), 0L, getRotationDegrees());
  }

  @Override
  protected void processFrame(
      Bitmap bitmap, ImageProxy image, long receivedAtMs, long sensorTimestampNs, int rotation) {
    if (bitmap == null) return;
    InferenceResources resources = inferenceResources;
    if (resources == null
        || !resources.matches(
            bitmap.getWidth(),
            bitmap.getHeight(),
            90 - ImageUtils.getScreenOrientation(requireActivity()))) {
      requestNetworkConfiguration(bitmap.getWidth(), bitmap.getHeight());
      return;
    }

    final long acceptedSequence = ++frameNum;
    if (binding == null || !isInferenceEnabled()) return;
    if (!computingNetwork.compareAndSet(false, true)) {
      droppedInferenceFrames++;
      return;
    }

    final long acceptedGeneration = uiGeneration;
    final ReIDCoordinator acceptedReidCoordinator = reidCoordinator;
    final long acceptedReidSession =
        acceptedReidCoordinator == null ? -1L : acceptedReidCoordinator.getSessionEpoch();
    final Bitmap ownedFrame;
    final long copyStartedMs = SystemClock.elapsedRealtime();
    try {
      ownedFrame = copyInferenceFrame(bitmap);
    } catch (RuntimeException e) {
      computingNetwork.set(false);
      onInferenceFailure(e);
      return;
    }
    final boolean frontFacing = lensFacing == CameraSelector.LENS_FACING_FRONT;
    final long copyMs = SystemClock.elapsedRealtime() - copyStartedMs;

    final Detector activeDetector = resources.detector;
    final Bitmap activeCroppedBitmap = resources.croppedBitmap;
    final Matrix activeFrameToCropTransform = resources.frameToCropTransform;
    final Matrix activeCropToFrameTransform = resources.cropToFrameTransform;
    final int frameW = resources.frameWidth;
    final int frameH = resources.frameHeight;
    final int activeSensorOrientation = resources.sensorOrientation;
    final Runnable task =
        () -> {
          Bitmap workingFrame = ownedFrame;
          try {
            admittedUiFrame.set(new long[] {acceptedGeneration, acceptedSequence});
            if (acceptedGeneration != uiGeneration
                || resources != inferenceResources
                || enhancedRecoveryEnabled && acceptedReidCoordinator != reidCoordinator) return;
            final Canvas canvas = new Canvas(activeCroppedBitmap);
            if (frontFacing) {
              Bitmap flipped = CameraUtils.flipBitmapHorizontal(ownedFrame);
              canvas.drawBitmap(flipped, activeFrameToCropTransform, null);
              workingFrame = flipped;
            } else {
              canvas.drawBitmap(ownedFrame, activeFrameToCropTransform, null);
            }

            if (activeDetector != null) {
              final long startTime = SystemClock.elapsedRealtime();
              final List<Detector.Recognition> results =
                  activeDetector.recognizeImage(activeCroppedBitmap, classType);
              lastProcessingTimeMs = SystemClock.elapsedRealtime() - startTime;
              if (acceptedGeneration != uiGeneration || resources != inferenceResources) return;

              final List<Detector.Recognition> mappedRecognitions = new ArrayList<>();
              final List<Detector.Recognition> lowConfidenceRecognitions = new ArrayList<>();
              float lowConfidenceThreshold = lowConfidenceThreshold(minConfidence);
              for (final Detector.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null
                    && result.getConfidence()
                        >= (dualConfidenceEnabled ? lowConfidenceThreshold : minConfidence)) {
                  activeCropToFrameTransform.mapRect(location);
                  result.setLocation(location);
                  if (result.getConfidence() >= minConfidence) mappedRecognitions.add(result);
                  else lowConfidenceRecognitions.add(result);
                }
              }

              TargetTrackManager.TwoStageUpdateResult tierUpdate =
                  dualConfidenceEnabled
                      ? targetTrackManager.updateWithLowConfidence(
                          mappedRecognitions,
                          lowConfidenceRecognitions,
                          frameW,
                          frameH,
                          receivedAtMs)
                      : null;
              if (!dualConfidenceEnabled) {
                targetTrackManager.update(mappedRecognitions, frameW, frameH, receivedAtMs);
              }
              List<Detector.Recognition> continuedLowConfidence =
                  tierUpdate == null
                      ? java.util.Collections.emptyList()
                      : tierUpdate.continuedLowConfidence;
              List<Detector.Recognition> identityCandidates = new ArrayList<>(mappedRecognitions);
              identityCandidates.addAll(continuedLowConfidence);
              FollowState currentState = stateMachine.getState();
              Detector.Recognition largestPerson = selectLargest(mappedRecognitions);
              long initializationStartedMs = SystemClock.elapsedRealtime();
              if (currentState == FollowState.CAPTURE_TARGET) {
                if (reidCoordinator != null) {
                  reidCoordinator.collectInitializationCandidate(
                      workingFrame,
                      largestPerson,
                      activeSensorOrientation,
                      enhancedRecoveryEnabled ? receivedAtMs : -1L,
                      enhancedRecoveryEnabled
                          ? acceptedReidSession
                          : reidCoordinator.getSessionEpoch());
                }
                maybeSaveGalleryCandidate(workingFrame, largestPerson, activeSensorOrientation);
              }
              long initializationMs = SystemClock.elapsedRealtime() - initializationStartedMs;
              long matchStartedMs = SystemClock.elapsedRealtime();
              TargetMatcher.MatchResult legacyMatch =
                  matcher.match(
                      mappedRecognitions, workingFrame, stateMachine.getMemory(), frameW, frameH);
              long matchMs = SystemClock.elapsedRealtime() - matchStartedMs;
              if (reidCoordinator != null && enhancedRecoveryEnabled) {
                reidCoordinator.setFrameContext(targetTrackManager, receivedAtMs, acceptedSequence);
              }
              long reidStartMs = SystemClock.elapsedRealtime();
              IdentityEvidence identity =
                  reidCoordinator == null
                      ? null
                      : reidCoordinator.evaluate(
                          identityCandidates,
                          workingFrame,
                          stateMachine.getMemory(),
                          currentState,
                          frameW,
                          frameH,
                          activeSensorOrientation,
                          legacyMatch.score,
                          legacyMatch.matched,
                          legacyMatch.best,
                          enhancedRecoveryEnabled
                              ? acceptedReidSession
                              : reidCoordinator.getSessionEpoch());
              long reidMs = SystemClock.elapsedRealtime() - reidStartMs;
              long decisionStartedMs = SystemClock.elapsedRealtime();
              if (acceptedGeneration != uiGeneration) return;
              IdentityEvidence galleryIdentity = identity;
              Detector.Recognition galleryTarget = identity == null ? null : identity.bestCandidate;
              boolean selectedLowConfidence = false;
              if (identity != null) {
                boolean lowConfidenceBest = continuedLowConfidence.contains(identity.bestCandidate);
                TargetTrack reidCandidateTrack =
                    targetTrackManager.getTrackForRecognition(identity.bestCandidate);
                if (!lowConfidenceBest && !enhancedRecoveryEnabled) {
                  maybeGlobalReacquire(currentState, identity, reidCandidateTrack);
                }
                identity =
                    beliefAccumulator.update(
                        identity,
                        targetTrackManager,
                        reidCandidateTrack,
                        stateMachine.getMemory(),
                        frameW,
                        frameH);
                galleryIdentity = identity;
                galleryTarget = identity.bestCandidate;
                selectedLowConfidence = continuedLowConfidence.contains(identity.bestCandidate);
                if (selectedLowConfidence && !enhancedRecoveryEnabled) {
                  identity = identity.withoutMotionCandidate("low_confidence_observation_only");
                }
              }
              SimulatorIdentityGuard.Decision authorization = null;
              SimulatorContinuityTracker.Evidence continuity = null;
              boolean identityStage =
                  currentState != FollowState.IDLE
                      && currentState != FollowState.CAPTURE_TARGET
                      && currentState != FollowState.LOCKED_PENDING_CONFIRM
                      && currentState != FollowState.STOP;
              boolean stale =
                  enhancedRecoveryEnabled && SystemClock.elapsedRealtime() - receivedAtMs > 500L;
              if (enhancedRecoveryEnabled && identityStage) {
                // Preserve exact feature/detection provenance even when belief still prefers a
                // ghost.
                if (identity != null && reidCoordinator != null) {
                  TargetTrack lockedObservation = targetTrackManager.getLockedTrack();
                  TargetTrack scoredTrack =
                      stateMachine.hasFollowedInSession()
                              && lockedObservation != null
                              && lockedObservation.isVisible()
                              && simulatorIdentityGuard.prefersContinuity(
                                  lockedObservation.trackId, receivedAtMs)
                          ? lockedObservation
                          : targetTrackManager.getTrackForRecognition(
                              reidCoordinator.getLastBestCandidate());
                  if (scoredTrack != null)
                    identity =
                        identity.forSimulatorCandidate(
                            scoredTrack,
                            targetTrackManager.getLockedTrackId(),
                            reidCoordinator.getScoredTrack(scoredTrack.trackId),
                            reidCoordinator.getLastBboxEvidence(),
                            beliefAccumulator.getBeliefForTrack(scoredTrack));
                  galleryIdentity = identity;
                  galleryTarget = identity.bestCandidate;
                }
                TargetTrack candidateTrack =
                    identity == null
                        ? null
                        : targetTrackManager.getTrackForRecognition(identity.bestCandidate);
                boolean high =
                    identity != null && mappedRecognitions.contains(identity.bestCandidate);
                boolean local =
                    identity != null
                        && SimulatorContinuityTracker.hasHistoricalLocalSupport(
                            identity.bboxDefaultOk(),
                            identity.predictionOk(),
                            candidateTrack == null ? -1 : candidateTrack.trackId,
                            targetTrackManager.getLockedTrackId(),
                            targetTrackManager.isNearLockedGhost(
                                candidateTrack, frameW, frameH, receivedAtMs));
                continuity =
                    simulatorContinuity.observe(
                        acceptedGeneration,
                        identity == null ? -1 : identity.trackId,
                        identity == null || identity.bestCandidate == null
                            ? null
                            : identity.bestCandidate.getLocation(),
                        acceptedSequence,
                        receivedAtMs,
                        SystemClock.elapsedRealtime(),
                        frameW,
                        frameH,
                        candidateTrack != null
                            && candidateTrack.missedFrames == 0
                            && (high
                                || continuedLowConfidence.contains(candidateTrack.recognition)),
                        targetTrackManager.isLockedAssociationCompeting());
                if (identity != null
                    && candidateTrack != null
                    && continuity.observedGeometry != null) {
                  local = continuity.reliable || "continuity_warming".equals(continuity.reason);
                  identity =
                      identity.forSimulatorCandidate(
                          candidateTrack,
                          targetTrackManager.getLockedTrackId(),
                          identity.reidMatch,
                          continuity.observedGeometry,
                          beliefAccumulator.getBeliefForTrack(candidateTrack));
                  galleryIdentity = identity;
                  galleryTarget = identity.bestCandidate;
                }
                ReIDMatchResult verification = identity == null ? null : identity.reidMatch;
                if (identity != null
                    && reidCoordinator != null
                    && (!local || identity.trackId != targetTrackManager.getLockedTrackId()))
                  verification = reidCoordinator.getGlobalScoredTrack(identity.trackId);
                if (reidCoordinator != null)
                  simulatorIdentityGuard.inspectCandidates(
                      reidCoordinator.getGlobalScores(),
                      mappedRecognitions.size() + lowConfidenceRecognitions.size(),
                      targetTrackManager.getLockedTrackId(),
                      acceptedSequence,
                      receivedAtMs);
                authorization =
                    simulatorIdentityGuard.update(
                        acceptedGeneration,
                        acceptedSequence,
                        receivedAtMs,
                        SystemClock.elapsedRealtime(),
                        identity == null ? -1 : identity.trackId,
                        targetTrackManager.getLockedTrackId(),
                        high,
                        local,
                        identity == null ? null : identity.reidMatch,
                        mappedRecognitions.size() + lowConfidenceRecognitions.size(),
                        targetTrackManager.isLockedAssociationCompeting(),
                        candidateTrack == null || candidateTrack.missedFrames > 0,
                        identity == null || reidCoordinator == null
                            ? null
                            : reidCoordinator.getGlobalScoredTrack(identity.trackId),
                        continuity,
                        stateMachine.hasFollowedInSession());
                if (!followPolicy.continuityMotion)
                  authorization = authorization.withoutContinuityMotion();
                if (reidCoordinator != null)
                  reidCoordinator.setAutomaticVerification(
                      !authorization.authorized && !authorization.isContinuous());
                if (authorization.motionAllowed
                    && identity != null
                    && identity.reidMatch != null
                    && identity.reidMatch.fresh
                    && identity.reidMatch.bestScore >= .85f
                    && identity.reidMatch.margin >= .08f) {
                  rememberDistractors(authorization.trackId, mappedRecognitions);
                }
                if (authorization.authorized
                    && authorization.trackId != targetTrackManager.getLockedTrackId()) {
                  if (targetTrackManager.lockTrack(
                      authorization.trackId, "simulator_fresh_authorization")) {
                    beliefAccumulator.lockTrack(authorization.trackId);
                    identity =
                        identity.forSimulatorCandidate(
                            candidateTrack,
                            authorization.trackId,
                            verification,
                            identity.bboxContinuity,
                            beliefAccumulator.getBeliefForTrack(candidateTrack));
                    stateMachine.acceptSimulatorRecovery(authorization, identity.bestCandidate);
                  }
                }
                if (authorization.authorized
                    && (!local || currentState == FollowState.DIRECTED_REACQUIRE))
                  stateMachine.acceptSimulatorRecovery(authorization, identity.bestCandidate);
              }
              boolean holdIdentity = authorization != null && !authorization.authorized;
              FollowStateMachine.FrameResult fr =
                  !stale && holdIdentity && authorization.retainTarget
                      ? stateMachine.continuityFrame(
                          mappedRecognitions,
                          identity,
                          authorization,
                          frameW,
                          frameH,
                          activeSensorOrientation)
                      : stale || holdIdentity
                          ? stateMachine.observationOnly(mappedRecognitions, identity)
                          : stateMachine.onFrame(
                              mappedRecognitions,
                              workingFrame,
                              frameW,
                              frameH,
                              activeSensorOrientation,
                              identity,
                              enhancedRecoveryEnabled ? legacyMatch : null);
              fr.simulatorIdentity = authorization;
              fr.trackingDecision = authorization == null ? null : authorization.tracking;
              fr.frameSequence = acceptedSequence;
              fr.sessionGeneration = acceptedGeneration;
              fr.targetObservation =
                  targetObservation(
                      continuedLowConfidence,
                      mappedRecognitions.size() + lowConfidenceRecognitions.size(),
                      frameW,
                      frameH,
                      activeSensorOrientation,
                      receivedAtMs);
              fr.distanceDiagnosticText = distanceDiagnostic(fr.targetObservation);
              selectedLowConfidence =
                  identity != null && continuedLowConfidence.contains(identity.bestCandidate);
              fr.detectionTierEvidence =
                  dualConfidenceEnabled
                      ? new DetectionTierEvidence(
                          minConfidence,
                          lowConfidenceThreshold,
                          lowConfidenceRecognitions,
                          continuedLowConfidence,
                          selectedLowConfidence)
                      : DetectionTierEvidence.disabled(minConfidence);
              fr.behaviorDecision = decideBehavior(fr, frameW, frameH);
              if (!enhancedRecoveryEnabled) maybeRelockAfterRecovery(fr);
              if (enhancedRecoveryEnabled) {
                fr.frameTiming =
                    new FrameTimingEvidence(
                        receivedAtMs,
                        sensorTimestampNs,
                        lastProcessingTimeMs,
                        reidMs,
                        SystemClock.elapsedRealtime() - startTime,
                        SystemClock.elapsedRealtime() - receivedAtMs,
                        completedFps,
                        droppedInferenceFrames);
                prepareSimulatorLearningFrame(fr, SystemClock.elapsedRealtime());
              }
              if (enhancedRecoveryEnabled && reidCoordinator != null)
                reidCoordinator.setGalleryImageLogging(diagnosticEnabled && diagnosticActive);
              boolean exitLearningRisk =
                  enhancedRecoveryEnabled
                      && simulatorExitLearningRisk(SystemClock.elapsedRealtime());
              GalleryUpdateStatus galleryStatus =
                  reidCoordinator == null
                      ? null
                      : exitLearningRisk
                          ? reidCoordinator.freezeGallery("side_exit_learning_frozen")
                          : enhancedRecoveryEnabled
                              ? reidCoordinator.updateSimulatorGallery(
                                  galleryTarget != null ? galleryTarget : fr.target,
                                  fr.state,
                                  fr.behaviorDecision,
                                  galleryIdentity,
                                  mappedRecognitions.size() + lowConfidenceRecognitions.size(),
                                  frameW,
                                  frameH,
                                  activeSensorOrientation,
                                  SystemClock.elapsedRealtime(),
                                  authorization,
                                  stale,
                                  continuity != null
                                      && continuity.reliable
                                      && authorization != null
                                      && authorization.isContinuous())
                              : reidCoordinator.maybeUpdateAdaptiveGallery(
                                  galleryTarget != null ? galleryTarget : fr.target,
                                  fr.state,
                                  fr.behaviorDecision,
                                  galleryIdentity,
                                  mappedRecognitions.size() + lowConfidenceRecognitions.size(),
                                  frameW,
                                  frameH,
                                  activeSensorOrientation,
                                  SystemClock.elapsedRealtime());
              fr.galleryUpdateStatus = galleryStatus;
              if (enhancedRecoveryEnabled && reidCoordinator != null) {
                fr.recentGallery = reidCoordinator.getRecentStatus(SystemClock.elapsedRealtime());
                fr.deferredGalleryStatus = reidCoordinator.getDeferredGalleryStatus();
                fr.recentMatchingSupport = reidCoordinator.hasRecentMatchingSupport();
                for (java.util.Map.Entry<String, Bitmap> entry :
                    reidCoordinator.consumeDeferredCrops().entrySet()) {
                  if (diagnosticEnabled && diagnosticActive && diagnosticSession != null)
                    diagnosticSaver.saveGallerySnapshotAsync(
                        entry.getValue(), diagnosticSession, entry.getKey());
                  entry.getValue().recycle();
                }
              }
              fr.galleryGeometry =
                  reidCoordinator == null ? null : reidCoordinator.getCropGeometry();
              onGalleryStatusUpdated(galleryStatus);
              Bitmap recentCrop =
                  reidCoordinator == null ? null : reidCoordinator.consumeRecentCrop();
              if (recentCrop != null) {
                if (diagnosticEnabled && diagnosticActive)
                  diagnosticSaver.saveGallerySnapshotAsync(
                      recentCrop, diagnosticSession, "recent_frame_" + acceptedSequence);
                recentCrop.recycle();
              }
              Bitmap isolatedCrop =
                  reidCoordinator == null ? null : reidCoordinator.consumeQuarantineCrop();
              if (isolatedCrop != null) {
                if (diagnosticEnabled && diagnosticActive && diagnosticSession != null) {
                  diagnosticSaver.saveGallerySnapshotAsync(
                      isolatedCrop, diagnosticSession, "quarantine_frame_" + acceptedSequence);
                }
                isolatedCrop.recycle();
              }
              if (galleryStatus != null
                  && ("promoted".equals(galleryStatus.event)
                      || "quarantine_promoted".equals(galleryStatus.event))
                  && galleryStatus.revision > lastSavedAdaptiveRevision) {
                lastSavedAdaptiveRevision = galleryStatus.revision;
                Bitmap promotedCrop = reidCoordinator.consumePromotedCrop();
                if (promotedCrop != null) {
                  if (diagnosticEnabled && diagnosticActive && diagnosticSession != null) {
                    diagnosticSaver.saveGallerySnapshotAsync(
                        promotedCrop, diagnosticSession, "adaptive_" + galleryStatus.adaptiveSize);
                  }
                  promotedCrop.recycle();
                }
              }
              enrichFrameResult(fr, frameW, frameH, activeSensorOrientation, receivedAtMs);

              if (acceptedGeneration != uiGeneration) return;
              fr.frameTiming =
                  new FrameTimingEvidence(
                      receivedAtMs,
                      sensorTimestampNs,
                      lastProcessingTimeMs,
                      reidMs,
                      SystemClock.elapsedRealtime() - startTime,
                      SystemClock.elapsedRealtime() - receivedAtMs,
                      completedFps,
                      droppedInferenceFrames);
              CartFollowDiagnosticSession currentLog = diagnosticSession;
              if (currentLog != null) {
                currentLog.latestFrame = fr.frameSequence;
                currentLog.latestSourceMs = receivedAtMs;
                currentLog.latestGeneration = fr.sessionGeneration;
              }
              onFollowFrame(fr);
              String commandText = commandForFrame(fr, commandForState(fr));
              long decisionMs = SystemClock.elapsedRealtime() - decisionStartedMs;
              long completedAtMs = SystemClock.elapsedRealtime();
              if (fpsWindowStartMs == 0L) fpsWindowStartMs = receivedAtMs;
              completedFrames++;
              if (completedAtMs - fpsWindowStartMs >= 1000L) {
                completedFps = completedFrames * 1000f / (completedAtMs - fpsWindowStartMs);
                fpsWindowStartMs = completedAtMs;
                completedFrames = 0;
              }
              float fps = completedFps;
              fr.frameTiming =
                  new FrameTimingEvidence(
                          receivedAtMs,
                          sensorTimestampNs,
                          lastProcessingTimeMs,
                          reidMs,
                          completedAtMs - startTime,
                          completedAtMs - receivedAtMs,
                          fps,
                          droppedInferenceFrames)
                      .withStages(
                          copyMs, matchMs, initializationMs, decisionMs, -1L, completedAtMs);
              long logStartedMs = SystemClock.elapsedRealtime();
              maybeSaveDiagnostics(
                  workingFrame, fr, fps, commandText, frameW, frameH, activeSensorOrientation);
              long logSubmitMs = SystemClock.elapsedRealtime() - logStartedMs;
              fr.frameTiming =
                  fr.frameTiming.withStages(
                      copyMs,
                      matchMs,
                      initializationMs,
                      decisionMs,
                      logSubmitMs,
                      SystemClock.elapsedRealtime());
              postFrameUi(
                  fr,
                  commandText,
                  acceptedSequence,
                  acceptedGeneration,
                  frameW,
                  frameH,
                  activeSensorOrientation);
            }
          } catch (RuntimeException e) {
            Timber.e(e, "Cart follow inference failed.");
            if (acceptedGeneration == uiGeneration) onInferenceFailure(e);
          } finally {
            if (workingFrame != ownedFrame) workingFrame.recycle();
            ownedFrame.recycle();
            admittedUiFrame.remove();
            computingNetwork.set(false);
          }
        };
    synchronized (this) {
      if (handler == null || !handler.post(task)) {
        ownedFrame.recycle();
        computingNetwork.set(false);
      }
    }
  }

  static Bitmap copyInferenceFrame(Bitmap bitmap) {
    Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
    if (copy == null) throw new IllegalStateException("Cannot copy inference frame");
    return copy;
  }

  private TargetObservationEvidence targetObservation(
      List<Detector.Recognition> low,
      int personCount,
      int width,
      int height,
      int rotation,
      long time) {
    TargetTrack locked = targetTrackManager.getLockedTrack();
    if (!enhancedRecoveryEnabled
        || locked == null
        || !locked.isVisible()
        || locked.recognition == null
        || locked.recognition.getLocation() == null) return null;
    RectF screenBox =
        TargetObservationEvidence.toScreen(
            locked.recognition.getLocation(), width, height, rotation);
    if (!screenBox.intersect(0f, 0f, 1f, 1f)) return null;
    return new TargetObservationEvidence(
        screenBox,
        locked.trackId,
        time,
        beliefAccumulator.getBeliefForTrack(locked),
        low.contains(locked.recognition),
        SystemClock.elapsedRealtime() - time <= 500L,
        personCount,
        low.contains(locked.recognition) ? "low_continuation" : "locked_detection");
  }

  private String distanceDiagnostic(TargetObservationEvidence observation) {
    ImageSetpointDistanceEstimator.Setpoint setpoint =
        stateMachine.getMemory().getDistanceSetpoint();
    if (setpoint == null) return "相对图像尺度：尚未标定";
    if (observation == null)
      return String.format(
          Locale.US,
          "相对尺度 标定高=%.3f 面积=%.3f | 无当前目标",
          setpoint.desiredHeightRatio,
          setpoint.desiredAreaRatio);
    RectF box = observation.screenBox;
    return String.format(
        Locale.US,
        "相对尺度 标定高/面积=%.3f/%.3f 当前=%.3f/%.3f\n取景裁切 上=%s 下=%s（非米制距离）",
        setpoint.desiredHeightRatio,
        setpoint.desiredAreaRatio,
        box.height(),
        box.width() * box.height(),
        box.top <= 0.01f ? "是" : "否",
        box.bottom >= 0.99f ? "是" : "否");
  }

  protected boolean isInferenceEnabled() {
    return binding.startSwitch.isChecked();
  }

  /** Receives the final behavior decision after all identity and safety arbitration. */
  protected void onFollowFrame(FollowStateMachine.FrameResult frameResult) {}

  /** Runs on the UI thread together with boxes, confirmation and the main action text. */
  protected void onFrameUiApplied(FollowStateMachine.FrameResult frameResult) {}

  protected final void postCurrentFrameUi(Runnable action) {
    Activity activity = getActivity();
    if (activity == null) return;
    long[] token = admittedUiFrame.get();
    final long generation = token == null ? uiGeneration : token[0];
    final long sequence = token == null ? Long.MAX_VALUE : token[1];
    activity.runOnUiThread(
        () -> {
          if (binding == null
              || generation != uiGeneration
              || sequence < latestAppliedFrameSequence) return;
          action.run();
        });
  }

  /** Lets real-hardware pages replace a visual suggestion with the actual safe output. */
  protected String commandForFrame(FollowStateMachine.FrameResult frameResult, String defaultText) {
    return defaultText;
  }

  /** Called after an inference task fails so real vehicle screens can stop safely. */
  protected void onInferenceFailure(RuntimeException error) {}

  private void maybeRelockAfterRecovery(FollowStateMachine.FrameResult fr) {
    if (enhancedRecoveryEnabled) return;
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

  static float lowConfidenceThreshold(float highThreshold) {
    return Math.min(0.25f, highThreshold);
  }

  private void maybeGlobalReacquire(
      FollowState state, IdentityEvidence identity, TargetTrack candidateTrack) {
    if (!enhancedRecoveryEnabled || !isRecoveryState(state) || identity == null) {
      globalReacquireGate.reset();
      return;
    }
    TargetTrack locked = targetTrackManager.getLockedTrack();
    boolean lockedVisible = locked != null && locked.isVisible();
    int candidateTrackId = candidateTrack == null ? -1 : candidateTrack.trackId;
    if (globalReacquireGate.update(
        candidateTrackId,
        lockedVisible,
        identity.reidMatch,
        reidCoordinator == null ? 0L : reidCoordinator.getLastRunTimeMs())) {
      if (targetTrackManager.lockTrack(candidateTrackId, "global_reid_reacquire")) {
        beliefAccumulator.lockTrack(candidateTrackId);
      }
      globalReacquireGate.reset();
    }
  }

  static boolean isRecoveryState(FollowState state) {
    return state == FollowState.IDENTITY_UNCERTAIN
        || state == FollowState.LOST
        || state == FollowState.SEARCH
        || state == FollowState.REACQUIRE_TARGET
        || state == FollowState.DIRECTED_REACQUIRE;
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

  private void rememberDistractors(int targetId, List<Detector.Recognition> persons) {
    TargetTrack target = targetTrackManager.getTrackById(targetId);
    if (target == null || target.recognition == null) return;
    RectF targetBox = target.recognition.getLocation();
    for (Detector.Recognition person : persons) {
      TargetTrack other = targetTrackManager.getTrackForRecognition(person);
      if (other != null
          && other.trackId != targetId
          && separatePersonBoxes(targetBox, person.getLocation())) {
        simulatorIdentityGuard.rememberDistractor(other.trackId);
      }
    }
  }

  static boolean separatePersonBoxes(RectF a, RectF b) {
    if (a == null || b == null || a.isEmpty() || b.isEmpty()) return false;
    float overlap =
        Math.max(0f, Math.min(a.right, b.right) - Math.max(a.left, b.left))
            * Math.max(0f, Math.min(a.bottom, b.bottom) - Math.max(a.top, b.top));
    return overlap / (a.width() * a.height() + b.width() * b.height() - overlap) < 0.10f;
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

  private List<DrawBox> buildDrawBoxes(
      FollowStateMachine.FrameResult fr, int frameW, int frameH, int sensorOrientation) {
    List<DrawBox> boxes = new ArrayList<>();
    Detector.Recognition pendingCandidate =
        fr.state == FollowState.LOCKED_PENDING_CONFIRM ? selectLargest(fr.persons) : null;
    for (Detector.Recognition r : fr.persons) {
      if (r == null || r.getLocation() == null) continue;
      int colorType = COLOR_NORMAL;
      TargetTrack track = targetTrackManager.getTrackForRecognition(r);
      boolean pendingConfirmation = fr.state == FollowState.LOCKED_PENDING_CONFIRM;
      boolean recovering =
          fr.state == FollowState.IDENTITY_UNCERTAIN
              || fr.state == FollowState.REACQUIRE_TARGET
              || fr.state == FollowState.DIRECTED_REACQUIRE;
      if (pendingConfirmation && r == pendingCandidate) {
        colorType = COLOR_CANDIDATE;
      } else if (track != null
          && targetTrackManager.isLockedTrack(track)
          && !recovering
          && beliefAccumulator.getBeliefForTrack(track) >= IdentityBelief.BELIEF_CAUTION) {
        colorType = COLOR_TARGET;
      } else if (track != null && targetTrackManager.isLockedTrack(track)) {
        colorType = COLOR_CANDIDATE;
      } else if (track != null && track.trackId == targetTrackManager.getSuspectedTrackId()) {
        colorType = COLOR_CANDIDATE;
      } else if (r == fr.target) {
        colorType = fr.matched ? COLOR_TARGET : COLOR_FAIL;
      } else if (r == fr.candidate) {
        colorType = COLOR_CANDIDATE;
      }
      if (fr.simulatorIdentity != null
          && !fr.simulatorIdentity.authorized
          && !fr.simulatorIdentity.isContinuous()
          && (r == fr.candidate
              || (track != null
                  && (track.trackId == fr.simulatorIdentity.trackId
                      || targetTrackManager.isLockedTrack(track))))) colorType = COLOR_CANDIDATE;
      String label = null;
      if (pendingConfirmation && r == pendingCandidate) {
        label = "待确认";
      } else if (recovering && r == fr.target) {
        label = "重捕确认中";
      } else if (track != null) {
        label =
            String.format(
                Locale.US, "T%d b=%.2f", track.trackId, beliefAccumulator.getBeliefForTrack(track));
      }
      if (fr.simulatorIdentity != null
          && track != null
          && track.trackId == fr.simulatorIdentity.trackId) {
        SimulatorIdentityGuard.Decision permit = fr.simulatorIdentity;
        boolean green = permit.motionAllowed && (permit.authorized || permit.isContinuous());
        colorType = green ? COLOR_TARGET : COLOR_CANDIDATE;
        label =
            permit.tracking != null
                ? permit.tracking.label()
                : permit.authorized
                    ? "身份已验证"
                    : permit.state == SimulatorIdentityGuard.State.TRACK_STABLE
                        ? "连续跟踪"
                        : permit.state == SimulatorIdentityGuard.State.APPEARANCE_TRANSITION
                            ? "外观变化中 · 低档"
                            : green ? "连续保持" : permit.retainTarget ? "姿态适应中" : "身份存疑";
      }
      boxes.add(new DrawBox(new RectF(r.getLocation()), colorType, label));
    }
    if (fr.detectionTierEvidence != null) {
      for (Detector.Recognition r : fr.detectionTierEvidence.lowConfidencePersons) {
        if (r == null || r.getLocation() == null || fr.persons.contains(r)) continue;
        boolean continued = fr.detectionTierEvidence.continuedLowConfidencePersons.contains(r);
        String label =
            String.format(Locale.US, "%s %.2f", continued ? "低置信续接" : "低置信候选", r.getConfidence());
        boxes.add(new DrawBox(new RectF(r.getLocation()), COLOR_LOW_CONFIDENCE, label));
      }
    }
    return boxes;
  }

  private void drawOverlay(Canvas canvas) {
    if (drawFrameWidth <= 0 || drawFrameHeight <= 0) return;
    if (enhancedRecoveryEnabled && SystemClock.elapsedRealtime() - drawObservedAtMs > 500L) return;
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
        case COLOR_LOW_CONFIDENCE:
          paint = lowConfidenceBoxPaint;
          if (label == null) label = "低置信续接";
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
    postCurrentFrameUi(() -> binding.commandText.setText(text));
  }

  private synchronized void invalidatePendingUiSnapshots() {
    uiGeneration++;
    simulatorIdentityGuard.begin(uiGeneration);
    simulatorContinuity.reset();
    latestAppliedFrameSequence = 0L;
    fpsWindowStartMs = 0L;
    completedFrames = 0;
    completedFps = 0f;
    onFollowGenerationChanged(uiGeneration);
  }

  protected void onFollowGenerationChanged(long generation) {}

  synchronized void clearDrawState() {
    drawBoxes.clear();
    drawFrameWidth = 0;
    drawFrameHeight = 0;
    drawSensorOrientation = 0;
    if (binding != null) binding.trackingOverlay.postInvalidate();
  }

  private void postFrameUi(
      FollowStateMachine.FrameResult fr,
      String commandText,
      long frameSequence,
      long generation,
      int frameWidth,
      int frameHeight,
      int orientation) {
    if (binding == null || !isAdded()) return;
    final List<DrawBox> boxes = buildDrawBoxes(fr, frameWidth, frameHeight, orientation);
    requireActivity()
        .runOnUiThread(
            () -> {
              if (binding == null) return;
              synchronized (this) {
                if (!shouldApplyUiSnapshot(
                    generation, uiGeneration, frameSequence, latestAppliedFrameSequence)) return;
                latestAppliedFrameSequence = frameSequence;
                lastPresentedPersons = new ArrayList<>(fr.persons);
                drawBoxes.clear();
                drawBoxes.addAll(boxes);
                drawFrameWidth = frameWidth;
                drawFrameHeight = frameHeight;
                drawSensorOrientation = orientation;
                drawObservedAtMs = fr.frameTiming.receivedAtMs;
              }
              FrameTimingEvidence timing = fr.frameTiming;
              fr.frameTiming = timing.presentedAt(SystemClock.elapsedRealtime());
              binding.commandText.setText(commandText);
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
              binding.trackingOverlay.invalidate();
              if (enhancedRecoveryEnabled) binding.trackingOverlay.postInvalidateDelayed(501L);
              updateDebugInfo(
                  fr.state,
                  fr.control,
                  fr.persons.size(),
                  timing.completedFps,
                  fr.distanceEstimate,
                  fr.behaviorDecision,
                  fr.identityEvidence,
                  fr.steeringEvidence);
              onFrameUiApplied(fr);
              if (diagnosticEnabled && diagnosticSession != null) {
                long now = SystemClock.elapsedRealtime();
                if (now - lastPresentationLogMs >= diagnosticConfig.frameLogIntervalMs) {
                  lastPresentationLogMs = now;
                  diagnosticSaver.saveEventAsync(
                      diagnosticSession,
                      fr.frameSequence,
                      "frame_presented",
                      "source_age_ms="
                          + fr.frameTiming.sourceAgeMs
                          + ";generation="
                          + generation
                          + ";copy_ms="
                          + timing.copyMs
                          + ";match_ms="
                          + timing.matchMs
                          + ";initialization_ms="
                          + timing.initializationMs
                          + ";decision_ms="
                          + timing.decisionMs
                          + ";log_submit_ms="
                          + timing.logSubmitMs
                          + ";ui_wait_ms="
                          + fr.frameTiming.uiWaitMs
                          + ";identity_gate="
                          + (fr.simulatorIdentity == null
                              ? "not_applicable"
                              : fr.simulatorIdentity.reason)
                          + ";fresh_matches="
                          + (fr.simulatorIdentity == null ? 0 : fr.simulatorIdentity.freshMatches));
                }
              }
            });
  }

  static boolean shouldApplyUiSnapshot(
      long snapshotGeneration, long activeGeneration, long snapshotFrame, long latestFrame) {
    return snapshotGeneration == activeGeneration && snapshotFrame >= latestFrame;
  }

  private void startDiagnosticSession() {
    if (diagnosticSession != null) return;
    if (!diagnosticEnabled) {
      resetDiagnosticState();
      return;
    }
    diagnosticSession = new CartFollowDiagnosticSession(requireContext().getApplicationContext());
    diagnosticSession.mode = this instanceof RealCartFollowFragment ? "真实小车" : "HumanCartSimulator";
    diagnosticSession.initCsvFiles();
    diagnosticActive = true;
    loggedGalleryRevision = -1;
    activateDiagnosticSession();
    recordControlEvent("recording_start", "enabled");
    onDiagnosticSessionChanged(diagnosticSession);
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
    CartFollowDiagnosticSession ending = diagnosticSession;
    if (ending == null && !diagnosticEnabled && closingDiagnosticSession != null) {
      onDiagnosticSessionChanged(null);
      closingDiagnosticSession.finish("logging_disabled");
    }
    if (ending != null) {
      diagnosticSaver.saveEventAsync(ending, frameNum, "session_stop", "");
      closingDiagnosticSession = ending;
      if (diagnosticEnabled && this instanceof RealCartFollowFragment) {
        // Bound the final transport observation window; no images or new frame records enter it.
        new android.os.Handler(android.os.Looper.getMainLooper())
            .postDelayed(
                () -> {
                  if (diagnosticSession == null) onDiagnosticSessionChanged(null);
                  ending.finish("recording_stopped");
                },
                500);
      } else {
        onDiagnosticSessionChanged(null);
        ending.finish("recording_stopped");
      }
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
    CartFollowDiagnosticSession session = diagnosticSession;
    if (session == null) return;
    session.latestFrame = fr.frameSequence;
    session.latestSourceMs = fr.frameTiming == null ? -1 : fr.frameTiming.receivedAtMs;
    session.latestGeneration = fr.sessionGeneration;
    long now = SystemClock.elapsedRealtime();
    if (diagnosticConfig.saveOverlays
        && session.sceneDue(
            now,
            (fr.simulatorIdentity == null
                    ? "none"
                    : fr.simulatorIdentity.state + ":" + fr.simulatorIdentity.reason)
                + ":"
                + (fr.galleryUpdateStatus == null ? "" : fr.galleryUpdateStatus.event)))
      diagnosticSaver.saveSceneAsync(workingFrame, session, fr.frameSequence, sensorOrientation);
    if (reidCoordinator != null
        && fr.galleryUpdateStatus != null
        && loggedGalleryRevision != fr.galleryUpdateStatus.revision) {
      loggedGalleryRevision = fr.galleryUpdateStatus.revision;
      for (String record : reidCoordinator.provenanceManifest()) session.provenance(record);
    }
    boolean shouldLog =
        lastDiagnosticFrameLogMs == 0L
            || now - lastDiagnosticFrameLogMs >= diagnosticConfig.frameLogIntervalMs;
    boolean shouldSaveCrop =
        lastDiagnosticCropMs == 0L || now - lastDiagnosticCropMs >= diagnosticConfig.cropIntervalMs;
    if (!shouldLog && !shouldSaveCrop) return;
    if (shouldLog) {
      lastDiagnosticFrameLogMs = now;
      session.control(
          "association",
          "margin="
              + targetTrackManager.getLockedAssociationMargin()
              + ";pairs="
              + targetTrackManager.getAssociationScores());
    }
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
        fr.frameSequence,
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
        fr.galleryUpdateStatus,
        fr.galleryGeometry,
        fr.simulatorDriveResult,
        fr.detectionTierEvidence,
        fr.directedReacquireEvidence,
        fr.frameTiming,
        fr.targetObservation,
        fr.distanceDiagnosticText,
        fr.simulatorIdentity,
        fr.recentGallery,
        fr.deferredGalleryStatus,
        fr.recentMatchingSupport,
        fr.realDriveResult,
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

  protected boolean simulatorExitLearningRisk(long nowMs) {
    return false;
  }

  protected void prepareSimulatorLearningFrame(FollowStateMachine.FrameResult frame, long nowMs) {}

  private void saveAdaptiveGalleryCrop(
      Bitmap frame, Detector.Recognition candidate, int sensorOrientation, int adaptiveSize) {
    if (!diagnosticEnabled || diagnosticSession == null || frame == null || candidate == null)
      return;
    Bitmap crop =
        cropPerson(
            frame, candidate.getLocation(), diagnosticConfig.paddingRatio, sensorOrientation);
    if (crop == null) return;
    diagnosticSaver.saveGallerySnapshotAsync(
        crop, diagnosticSession, "adaptive_promoted_" + adaptiveSize);
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
      if (rawCrop == frame) rawCrop = frame.copy(Bitmap.Config.ARGB_8888, false);
      int rotation = ((sensorOrientation % 360) + 360) % 360;
      if (rotation == 0) return rawCrop;
      Matrix matrix = new Matrix();
      matrix.postRotate(rotation);
      Bitmap upright =
          Bitmap.createBitmap(rawCrop, 0, 0, rawCrop.getWidth(), rawCrop.getHeight(), matrix, true);
      if (upright != rawCrop) rawCrop.recycle();
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
    postCurrentFrameUi(() -> binding.debugInfo.setText(info));
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
            fr.state,
            identity,
            distance,
            traversability,
            safety,
            stateMachine.getMemory(),
            frameW,
            enhancedRecoveryEnabled ? fr.simulatorIdentity : null);
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
        false,
        true,
        inferenceResources != null,
        inferenceResources == null ? "detector_initializing" : "ok");
  }

  protected boolean isDetectorReady() {
    return inferenceResources != null;
  }

  private static final class InferenceResources {
    final Detector detector;
    final Bitmap croppedBitmap;
    final Matrix frameToCropTransform;
    final Matrix cropToFrameTransform;
    final int frameWidth;
    final int frameHeight;
    final int sensorOrientation;

    InferenceResources(
        Detector detector,
        Bitmap croppedBitmap,
        Matrix frameToCropTransform,
        Matrix cropToFrameTransform,
        int frameWidth,
        int frameHeight,
        int sensorOrientation) {
      this.detector = detector;
      this.croppedBitmap = croppedBitmap;
      this.frameToCropTransform = frameToCropTransform;
      this.cropToFrameTransform = cropToFrameTransform;
      this.frameWidth = frameWidth;
      this.frameHeight = frameHeight;
      this.sensorOrientation = sensorOrientation;
    }

    boolean matches(int width, int height, int orientation) {
      return frameWidth == width && frameHeight == height && sensorOrientation == orientation;
    }
  }
}
