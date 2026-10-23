package org.openbot.app.robot.pointGoalNavigation;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import java.io.IOException;
import org.openbot.app.robot.R;
import org.openbot.app.robot.common.ControlsFragment;
import org.openbot.app.robot.databinding.FragmentPointGoalNavigationBinding;
import org.openbot.app.robot.main.MainViewModel;
import org.openbot.app.robot.tflite.Model;
import org.openbot.app.robot.tflite.Model.CLASS;
import org.openbot.app.robot.tflite.Model.PATH_TYPE;
import org.openbot.app.robot.tflite.Model.TYPE;
import org.openbot.app.robot.tflite.Navigation;
import org.openbot.app.robot.tflite.Network.Device;
import org.openbot.app.robot.utils.Constants;
import org.openbot.app.robot.utils.PermissionUtils;
import org.openbot.app.robot.vehicle.Control;
import org.openbot.app.robot.vehicle.Vehicle;
import timber.log.Timber;

public class PointGoalNavigationFragment extends ControlsFragment implements ArCoreListener {

  private MainViewModel mainViewModel;
  private Handler handlerMain;
  private ArCore arCore;
  private FragmentPointGoalNavigationBinding binding;
  // Read by the watchdog thread.
  private volatile boolean isRunning = false;
  private volatile long lastNavUpdateMs = 0;
  private final ArrivalCheck arrivalCheck = new ArrivalCheck();
  private HandlerThread watchdogThread;
  private Handler watchdogHandler;

  // ARCore calls back every frame (~33 ms); this long without one means the app is stuck.
  private static final long WATCHDOG_TIMEOUT_MS = 700;
  private static final long WATCHDOG_CHECK_MS = 200;
  // Frames that waited this long in the main-thread queue are dropped; driving on them is late
  // anyway, and a backlog would delay the Stop button.
  private static final long MAX_FRAME_AGE_MS = 100;
  // Resend the stop once in case the first command got lost over Bluetooth.
  private static final long STOP_RESEND_MS = 100;
  private boolean isPermissionRequested = false;
  private Navigation navigationPolicy;
  static final int kMaxChannelValue = 262143;

  public PointGoalNavigationFragment() {
    // Required empty public constructor
  }

  public static PointGoalNavigationFragment newInstance() {
    PointGoalNavigationFragment fragment = new PointGoalNavigationFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    binding = FragmentPointGoalNavigationBinding.inflate(inflater, container, false);

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    handlerMain = new Handler(Looper.getMainLooper());
    arCore = new ArCore(requireContext(), binding.surfaceView, handlerMain);
    arCore.setGoalPlacementEnabled(true);

    if (vehicle == null) {
      mainViewModel
          .getVehicle()
          .observe(
              getViewLifecycleOwner(),
              v -> {
                if (v != null && vehicle == null) {
                  vehicle = v;
                  if (isAdded()) {
                    setupTapToSetGoal();
                  }
                }
              });
      return;
    }
    setupTapToSetGoal();
  }

  // performClick() is called on ACTION_UP; lint can't see that through a stock GLSurfaceView.
  @SuppressLint("ClickableViewAccessibility")
  private void setupTapToSetGoal() {
    binding.surfaceView.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_UP) {
            v.performClick();
            if (!isRunning) arCore.requestTapAt(event.getX(), event.getY());
          }
          return true;
        });
    binding.start.setOnClickListener(v -> startDriving());
    // Stop on finger down, not on release, so the robot stops the moment the button is touched.
    binding.stop.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN && isRunning) {
            Timber.d("nav: stopped by user");
            stop();
            showHint(getString(R.string.point_goal_tap_hint));
          }
          return false;
        });

    watchdogThread = new HandlerThread("PointGoalWatchdog");
    watchdogThread.start();
    watchdogHandler = new Handler(watchdogThread.getLooper());

    Snackbar.make(binding.getRoot(), R.string.point_goal_tap_hint, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void onGoalPlaced(float distanceM) {
    if (binding == null || isRunning) return;
    binding.start.setVisibility(View.VISIBLE);
    showHint(getString(R.string.point_goal_set, distanceM));
  }

  @Override
  public void onGoalRejected(GoalValidator.Result reason) {
    if (binding == null || isRunning) return;
    switch (reason) {
      case NOT_FLOOR:
        showHint(getString(R.string.point_goal_not_floor));
        break;
      case TOO_CLOSE:
        showHint(getString(R.string.point_goal_too_close, GoalValidator.MIN_GOAL_DISTANCE_M));
        break;
      default:
        break;
    }
  }

  private void showHint(String message) {
    Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void onArCoreUpdate(
      NavigationPoses navigationPoses,
      ImageFrame rgb,
      CameraIntrinsics cameraIntrinsics,
      long timestamp) {
    Pose targetPose = navigationPoses.getTargetPose();
    Pose currentPose = navigationPoses.getCurrentPose();
    lastNavUpdateMs = SystemClock.elapsedRealtime();
    long frameAgeMs = (SystemClock.elapsedRealtimeNanos() - timestamp) / 1_000_000;
    if (isRunning && frameAgeMs > MAX_FRAME_AGE_MS) {
      Timber.d("nav: dropping frame that is %d ms old", frameAgeMs);
      return;
    }
    if (isRunning && vehicle != null && targetPose != null && currentPose != null) {
      float goalDistance = computeDistance(targetPose, currentPose);

      Timber.d(
          "nav: current=(%.3f, %.3f, %.3f) target=(%.3f, %.3f, %.3f) distance=%.3f",
          currentPose.tx(),
          currentPose.ty(),
          currentPose.tz(),
          targetPose.tx(),
          targetPose.ty(),
          targetPose.tz(),
          goalDistance);

      float deltaYaw = computeDeltaYaw(currentPose, targetPose);
      if (arrivalCheck.hasArrived(goalDistance, deltaYaw)) {
        Timber.d("nav: goal reached at distance=%.3f deltaYaw=%.3f", goalDistance, deltaYaw);
        stop();
        audioPlayer.playFromStringID(R.string.goal_reached);
        showInfoDialog(getString(R.string.goal_reached));
      } else {

        Bitmap bitmap = convertRGBFrameToScaledBitmap(rgb, 160.f / 480.f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 30, 160, 90);

        Control control =
            navigationPolicy.recognizeImage(
                bitmap, goalDistance, (float) Math.sin(deltaYaw), (float) Math.cos(deltaYaw));

        Timber.d(
            "nav: deltaYaw=%.3f control=(%.3f, %.3f)",
            deltaYaw, control.getLeft(), control.getRight());
        vehicle.setControl(control);
      }
    }
  }

  public static Bitmap convertRGBFrameToScaledBitmap(ImageFrame bImg, float resizeFactor) {
    int previewHeight = bImg.getHeight();
    int previewWidth = bImg.getWidth();
    if (bImg == null || previewHeight == 0 || previewWidth == 0 || resizeFactor < 0) {
      throw new IllegalArgumentException();
    }

    int width = (int) (resizeFactor * previewWidth);
    int height = (int) (resizeFactor * previewHeight);

    Bitmap rgbFrameBitmap =
        Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
    int[] rgbBytes = new int[previewWidth * previewHeight];

    convertYUV420ToARGB8888(
        bImg.getYuvBytes()[0],
        bImg.getYuvBytes()[1],
        bImg.getYuvBytes()[2],
        previewWidth,
        previewHeight,
        bImg.getYRowStride(),
        bImg.getUvRowStride(),
        bImg.getUvPixelStride(),
        rgbBytes);

    rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
    return Bitmap.createScaledBitmap(rgbFrameBitmap, width, height, true);
  }

  public static void convertYUV420ToARGB8888(
      byte[] yData,
      byte[] uData,
      byte[] vData,
      int width,
      int height,
      int yRowStride,
      int uvRowStride,
      int uvPixelStride,
      int[] out) {
    int yp = 0;
    for (int j = 0; j < height; j++) {
      int pY = yRowStride * j;
      int pUV = uvRowStride * (j >> 1);

      for (int i = 0; i < width; i++) {
        int uv_offset = pUV + (i >> 1) * uvPixelStride;

        out[yp++] = YUV2RGB(0xff & yData[pY + i], 0xff & uData[uv_offset], 0xff & vData[uv_offset]);
      }
    }
  }

  public static float computeDeltaYaw(Pose pose, Pose goalPose) {
    // compute robot forward axis (global coordinate system)
    float[] forward = new float[] {0.f, 0.f, -1.f};
    float[] forwardRotated = pose.rotateVector(forward);

    // distance vector to goal (global coordinate system)
    float dx = goalPose.tx() - pose.tx();
    float dz = goalPose.tz() - pose.tz();

    double yaw = Math.atan2(forwardRotated[2], forwardRotated[0]) - Math.atan2(dz, dx);

    // fit to range (-pi, pi]
    if (yaw > Math.PI) {
      yaw -= 2 * Math.PI;
    } else if (yaw <= -Math.PI) {
      yaw += 2 * Math.PI;
    }

    return (float) yaw;
  }

  private static int YUV2RGB(int y, int u, int v) {
    // Adjust and check YUV values
    y = Math.max((y - 16), 0);
    u -= 128;
    v -= 128;

    // This is the floating point equivalent. We do the conversion in integer
    // because some Android devices do not have floating point in hardware.
    // nR = (int)(1.164 * nY + 2.018 * nU);
    // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
    // nB = (int)(1.164 * nY + 1.596 * nV);
    int y1192 = 1192 * y;
    int r = (y1192 + 1634 * v);
    int g = (y1192 - 833 * v - 400 * u);
    int b = (y1192 + 2066 * u);

    // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
    r = r > kMaxChannelValue ? kMaxChannelValue : (Math.max(r, 0));
    g = g > kMaxChannelValue ? kMaxChannelValue : (Math.max(g, 0));
    b = b > kMaxChannelValue ? kMaxChannelValue : (Math.max(b, 0));

    return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
  }

  @Override
  public void onArCoreTrackingFailure(long timestamp, TrackingFailureReason trackingFailureReason) {
    lastNavUpdateMs = SystemClock.elapsedRealtime();
    if (isRunning) {
      stop();
      audioPlayer.playFromStringID(R.string.tracking_lost);
      showInfoDialog(getString(R.string.tracking_lost));
    }
  }

  @Override
  public void onArCoreSessionPaused(long timestamp) {
    if (isRunning) stop();
    audioPlayer.playFromStringID(R.string.ar_core_session_paused);
    showInfoDialog(getString(R.string.ar_core_session_paused));
  }

  /**
   * Runs on its own thread, so it still works when the main thread or ARCore hangs: if the
   * navigation loop stops calling back while driving, the robot is stopped right away instead of
   * driving on with its last command.
   */
  private final Runnable watchdog =
      new Runnable() {
        @Override
        public void run() {
          if (!isRunning) return;
          long silentMs = SystemClock.elapsedRealtime() - lastNavUpdateMs;
          if (silentMs > WATCHDOG_TIMEOUT_MS) {
            Timber.w("nav: no update for %d ms, stopping robot", silentMs);
            if (vehicle != null) vehicle.stopBot();
            isRunning = false;
            handlerMain.post(
                () -> {
                  // The motors are already stopped above; skip the UI if the screen is going away.
                  if (binding == null || !isAdded() || getView() == null) return;
                  stop();
                  showInfoDialog(getString(R.string.point_goal_watchdog));
                });
            return;
          }
          watchdogHandler.postDelayed(this, WATCHDOG_CHECK_MS);
        }
      };

  private void stop() {
    // Motors first, everything else after.
    isRunning = false;
    if (vehicle != null) {
      vehicle.stopBot();
      Vehicle stoppedVehicle = vehicle;
      handlerMain.postDelayed(
          () -> {
            if (!isRunning) stoppedVehicle.stopBot();
          },
          STOP_RESEND_MS);
    }
    if (watchdogHandler != null) watchdogHandler.removeCallbacks(watchdog);
    binding.stop.setVisibility(View.GONE);
    arCore.detachAnchors();
    binding.start.setVisibility(View.GONE);
  }

  private static float computeDistance(Pose goalPose, Pose robotPose) {
    Float dx = abs(goalPose.tx() - robotPose.tx());
    Float dz = abs(goalPose.tz() - robotPose.tz());
    float distance = (float) Math.sqrt(dx * dx + dz * dz);

    return distance;
  }

  @Override
  public void onStart() {
    super.onStart();

    arCore.setArCoreListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    resume();
  }

  @Override
  public void onPause() {
    super.onPause();

    // Leaving the screen (home button, another app, closing) must never leave the robot driving.
    if (isRunning) stop();
    arCore.pause();
  }

  @Override
  public void onStop() {
    super.onStop();

    arCore.removeArCoreListener();
  }

  @Override
  protected void processControllerKeyData(String command) {}

  @Override
  protected void processUSBData(String data) {}

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (vehicle != null && isRunning) vehicle.stopBot();
    isRunning = false;
    if (watchdogThread != null) watchdogThread.quitSafely();
    if (navigationPolicy != null) {
      navigationPolicy.close();
      navigationPolicy = null;
    }
    arCore.closeSession();
  }

  private void resume() {
    if (!PermissionUtils.hasCameraPermission(requireActivity())) {
      getCameraPermission();
    } else if (PermissionUtils.shouldShowRational(requireActivity(), Constants.PERMISSION_CAMERA)) {
      PermissionUtils.showCameraPermissionsPreviewToast(requireActivity());
    } else {
      setupArCore();
    }
  }

  private void getCameraPermission() {
    if (!isPermissionRequested) {
      requestPermissionLauncherCamera.launch(Constants.PERMISSION_CAMERA);
      isPermissionRequested = true;
    } else {
      showPermissionDialog();
    }
  }

  private final ActivityResultLauncher<String> requestPermissionLauncherCamera =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          isGranted -> {
            if (isGranted) {
              setupArCore();
            } else {
              showPermissionDialog();
            }
          });

  private void setupArCore() {
    try {
      arCore.resume();
      return;
    } catch (java.lang.SecurityException e) {
      e.printStackTrace();
      showPermissionDialog();
      return;
    } catch (UnavailableSdkTooOldException e) {
      e.printStackTrace();
    } catch (UnavailableDeviceNotCompatibleException e) {
      e.printStackTrace();
    } catch (UnavailableArcoreNotInstalledException e) {
      e.printStackTrace();
    } catch (UnavailableApkTooOldException e) {
      e.printStackTrace();
    } catch (CameraNotAvailableException e) {
      e.printStackTrace();
    }

    showInfoDialog(getString(R.string.point_goal_arcore_failure));
  }

  private void showInfoDialog(String message) {
    if (getChildFragmentManager().findFragmentByTag(InfoDialogFragment.TAG) == null) {
      InfoDialogFragment dialog = InfoDialogFragment.newInstance(message);
      dialog.setCancelable(false);
      dialog.show(getChildFragmentManager(), InfoDialogFragment.TAG);
    }

    getChildFragmentManager()
        .setFragmentResultListener(
            InfoDialogFragment.TAG,
            getViewLifecycleOwner(),
            (requestKey, result) -> {
              Boolean restart = result.getBoolean("restart");

              if (restart) {
                showHint(getString(R.string.point_goal_tap_hint));
              } else {
                requireActivity().onBackPressed();
              }
            });
  }

  private void showPermissionDialog() {
    if (getChildFragmentManager().findFragmentByTag(PermissionDialogFragment.TAG) == null) {
      PermissionDialogFragment dialog = new PermissionDialogFragment();
      dialog.setCancelable(false);
      dialog.show(getChildFragmentManager(), PermissionDialogFragment.TAG);
    }

    getChildFragmentManager()
        .setFragmentResultListener(
            PermissionDialogFragment.TAG,
            getViewLifecycleOwner(),
            (requestKey, result) -> {
              String choice = result.getString("choice");

              if (choice.equals("settings")) {
                PermissionUtils.startInstalledAppDetailsActivity(requireActivity());
              } else if (choice.equals("retry")) {
                isPermissionRequested = false;
                resume();
              } else {
                requireActivity().onBackPressed();
              }
            });
  }

  private void startDriving() {
    if (arCore.getTargetPose() == null) {
      // No goal has been tapped yet; the Start button should be hidden in this state, but
      // guard against it anyway (e.g. a stale click event).
      return;
    }
    binding.start.setVisibility(View.GONE);

    // Load the model once and reuse it; loading it on every Start leaked a copy each time.
    if (navigationPolicy == null) {
      Model model =
          new Model(
              0,
              CLASS.NAVIGATION,
              TYPE.GOALNAV,
              "navigation.tflite",
              PATH_TYPE.ASSET,
              "networks/navigation.tflite",
              "160x90");
      try {
        navigationPolicy = new Navigation(requireActivity(), model, Device.CPU, 1);
      } catch (IOException e) {
        e.printStackTrace();
        showInfoDialog(getString(R.string.point_goal_model_failure));
        return;
      }
    }

    lastNavUpdateMs = SystemClock.elapsedRealtime();
    arrivalCheck.reset();
    isRunning = true;
    binding.stop.setVisibility(View.VISIBLE);
    watchdogHandler.postDelayed(watchdog, WATCHDOG_CHECK_MS);
  }
}
