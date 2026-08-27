package org.openbot.app.robot.pointGoalNavigation;

import static java.lang.Math.abs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotTrackingException;
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
import timber.log.Timber;

public class PointGoalNavigationFragment extends ControlsFragment implements ArCoreListener {

  private MainViewModel mainViewModel;
  private Handler handlerMain;
  private ArCore arCore;
  private FragmentPointGoalNavigationBinding binding;
  private boolean isRunning = false;
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
    if (getArguments() != null) {}
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

    if (vehicle == null) {
      mainViewModel
          .getVehicle()
          .observe(
              getViewLifecycleOwner(),
              v -> {
                if (v != null && vehicle == null) {
                  vehicle = v;
                  if (isAdded()) {
                    showStartDialog();
                  }
                }
              });
      return;
    }
    showStartDialog();
  }

  @Override
  public void onArCoreUpdate(
      NavigationPoses navigationPoses,
      ImageFrame rgb,
      CameraIntrinsics cameraIntrinsics,
      long timestamp) {
    if (isRunning && vehicle != null) {
      float goalDistance =
          computeDistance(navigationPoses.getTargetPose(), navigationPoses.getCurrentPose());

      if (goalDistance < 0.15f) {
        stop();
        audioPlayer.playFromStringID(R.string.goal_reached);
        showInfoDialog(getString(R.string.goal_reached));
      } else {
        float deltaYaw =
            computeDeltaYaw(navigationPoses.getCurrentPose(), navigationPoses.getTargetPose());

        Bitmap bitmap = convertRGBFrameToScaledBitmap(rgb, 160.f / 480.f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 30, 160, 90);

        Control control =
            navigationPolicy.recognizeImage(
                bitmap, goalDistance, (float) Math.sin(deltaYaw), (float) Math.cos(deltaYaw));

        Timber.d("control: (" + control.getLeft() + ", " + control.getRight() + ")");
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

  private void stop() {
    arCore.detachAnchors();
    if (vehicle != null) {
      vehicle.stopBot();
    }
    isRunning = false;
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

    showInfoDialog(
        "ARCore failure. Make sure that your device is compatible and the ARCore SDK is installed.");
  }

  private void showStartDialog() {
    if (getChildFragmentManager().findFragmentByTag(SetGoalDialogFragment.TAG) == null) {
      SetGoalDialogFragment dialog = SetGoalDialogFragment.newInstance();
      dialog.setCancelable(false);
      dialog.show(getChildFragmentManager(), SetGoalDialogFragment.TAG);
    }

    getChildFragmentManager()
        .setFragmentResultListener(
            SetGoalDialogFragment.TAG,
            getViewLifecycleOwner(),
            (requestKey, result) -> {
              Boolean start = result.getBoolean("start");

              if (start) {
                Float forward = result.getFloat("forward");
                Float left = result.getFloat("left");

                // x: right, z: backwards
                startDriving(-left, -forward);
              } else {
                requireActivity().onBackPressed();
              }
            });
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
                showStartDialog();
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

  private void startDriving(float goalX, float goalZ) {
    Timber.i("setting goal at (" + goalX + ", " + goalZ + ")");

    try {
      arCore.detachAnchors();
      arCore.setStartAnchorAtCurrentPose();

      Pose startPose = arCore.getStartPose();
      if (startPose == null) {
        showInfoDialog(getString(R.string.no_initial_ar_core_pose));
        return;
      }
      arCore.setTargetAnchor(startPose.compose(Pose.makeTranslation(goalX, 0.0f, goalZ)));
    } catch (NotTrackingException e) {
      e.printStackTrace();
      showInfoDialog(getString(R.string.tracking_lost));
      return;
    }
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
      showInfoDialog("Navigation policy could not be initialized.");
      return;
    }

    isRunning = true;
  }
}
