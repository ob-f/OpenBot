package org.openbot.app.robot.common;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openbot.app.robot.R;
import org.openbot.app.robot.env.ControllerToBotEventBus;
import org.openbot.app.robot.env.ImageUtils;
import org.openbot.app.robot.utils.Constants;
import org.openbot.app.robot.utils.Enums;
import org.openbot.app.robot.utils.PermissionUtils;
import org.openbot.app.robot.utils.YuvToRgbConverter;
import timber.log.Timber;

public abstract class CameraFragment extends ControlsFragment {

  private ExecutorService cameraExecutor;
  private PreviewView previewView;
  private Preview preview;
  protected int lensFacing;
  private ProcessCameraProvider cameraProvider;
  private Size analyserResolution = Enums.Preview.HD.getValue();
  private YuvToRgbConverter converter;
  private Bitmap bitmapBuffer;
  private int rotationDegrees;

  protected View inflateFragment(int resId, LayoutInflater inflater, ViewGroup container) {
    return addCamera(inflater.inflate(resId, container, false), inflater, container);
  }

  protected View inflateFragment(
      ViewBinding viewBinding, LayoutInflater inflater, ViewGroup container) {
    return addCamera(viewBinding.getRoot(), inflater, container);
  }

  private View addCamera(View view, LayoutInflater inflater, ViewGroup container) {
    View cameraView = inflater.inflate(R.layout.fragment_camera, container, false);
    ViewGroup rootView = (ViewGroup) cameraView.getRootView();
    // set lensFacing from user preferences (last used setting)
    lensFacing =
        preferencesManager.getCameraSwitch()
            ? CameraSelector.LENS_FACING_FRONT
            : CameraSelector.LENS_FACING_BACK;
    previewView = cameraView.findViewById(R.id.viewFinder);
    rootView.addView(view);

    if (!PermissionUtils.hasCameraPermission(requireActivity())) {
      requestPermissionLauncherCamera.launch(Constants.PERMISSION_CAMERA);
    } else if (PermissionUtils.shouldShowRational(requireActivity(), Constants.PERMISSION_CAMERA)) {
      PermissionUtils.showCameraPermissionsPreviewToast(requireActivity());
    } else {
      setupCamera();
    }
    return cameraView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    cameraExecutor = Executors.newSingleThreadExecutor();
    subscribeToLocalCameraLifecycleEvents();
  }

  // Allowing WebRtcServer pause/resume to handle camera preview
  private void subscribeToLocalCameraLifecycleEvents() {
    String subscriber = getClass().getSimpleName() + "_localCameraLifecycle";
    ControllerToBotEventBus.unsubscribe(subscriber);
    ControllerToBotEventBus.subscribe(
        subscriber,
        event -> {
          if (getActivity() == null) {
            return;
          }
          String command = event.getString("command");
          getActivity()
              .runOnUiThread(
                  () -> {
                    if (Constants.CMD_PAUSE_LOCAL_CAMERA.equals(command)) {
                      pauseLocalCamera();
                    } else if (Constants.CMD_RESUME_LOCAL_CAMERA.equals(command)) {
                      resumeLocalCamera();
                    }
                  });
        },
        error ->
            Timber.e(
                error, "Error in %s local camera lifecycle handling", getClass().getSimpleName()),
        event ->
            event.has("command")
                && (Constants.CMD_PAUSE_LOCAL_CAMERA.equals(event.getString("command"))
                    || Constants.CMD_RESUME_LOCAL_CAMERA.equals(event.getString("command"))));
  }

  private void pauseLocalCamera() {
    if (cameraProvider != null) {
      cameraProvider.unbindAll();
    }
  }

  private void resumeLocalCamera() {
    if (cameraProvider != null) {
      bindCameraUseCases();
    }
  }

  @SuppressLint("RestrictedApi")
  private void setupCamera() {
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
        ProcessCameraProvider.getInstance(requireContext());

    cameraProviderFuture.addListener(
        () -> {
          try {
            cameraProvider = cameraProviderFuture.get();
            bindCameraUseCases();
          } catch (ExecutionException | InterruptedException e) {
            Timber.e("Camera setup failed: %s", e.toString());
          }
        },
        ContextCompat.getMainExecutor(requireContext()));
  }

  @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
  private void bindCameraUseCases() {
    converter = new YuvToRgbConverter(requireContext());
    bitmapBuffer = null;
    preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
    final boolean rotated = ImageUtils.getScreenOrientation(requireActivity()) % 180 == 90;
    final PreviewView.ScaleType scaleType =
        rotated ? PreviewView.ScaleType.FIT_CENTER : PreviewView.ScaleType.FIT_START;
    previewView.setScaleType(scaleType);
    preview.setSurfaceProvider(previewView.getSurfaceProvider());
    CameraSelector cameraSelector =
        new CameraSelector.Builder().requireLensFacing(lensFacing).build();
    ImageAnalysis imageAnalysis;

    if (analyserResolution == null)
      imageAnalysis =
          new ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
    else
      imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(analyserResolution).build();
    // insert your code here.
    imageAnalysis.setAnalyzer(
        cameraExecutor,
        image -> {
          if (bitmapBuffer == null)
            bitmapBuffer =
                Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

          rotationDegrees = image.getImageInfo().getRotationDegrees();
          converter.yuvToRgb(image.getImage(), bitmapBuffer);
          image.close();

          processFrame(bitmapBuffer, image);
        });
    try {
      if (cameraProvider != null) {
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
      }
    } catch (Exception e) {
      Timber.e("Use case binding failed: %s", e.toString());
    }
  }

  public int getRotationDegrees() {
    return rotationDegrees;
  }

  private final ActivityResultLauncher<String> requestPermissionLauncherCamera =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          isGranted -> {
            if (isGranted) {
              setupCamera();
            } else if (PermissionUtils.shouldShowRational(
                requireActivity(), Constants.PERMISSION_CAMERA)) {
              PermissionUtils.showCameraPermissionsPreviewToast(requireActivity());
            } else {

            }
          });

  @Override
  public void onDestroy() {
    super.onDestroy();
    cameraExecutor.shutdown();
    ControllerToBotEventBus.unsubscribe(getClass().getSimpleName() + "_localCameraLifecycle");
  }

  @SuppressLint("RestrictedApi")
  public Size getPreviewSize() {
    return preview.getAttachedSurfaceResolution();
  }

  public Size getMaxAnalyseImageSize() {
    return new Size(bitmapBuffer.getWidth(), bitmapBuffer.getHeight());
  }

  public void toggleCamera() {
    lensFacing =
        CameraSelector.LENS_FACING_FRONT == lensFacing
            ? CameraSelector.LENS_FACING_BACK
            : CameraSelector.LENS_FACING_FRONT;
    preferencesManager.setCameraSwitch(!preferencesManager.getCameraSwitch());
    bindCameraUseCases();
  }

  public void setAnalyserResolution(Size resolutionSize) {
    if (resolutionSize == null) analyserResolution = null;
    else {
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        this.analyserResolution = new Size(resolutionSize.getHeight(), resolutionSize.getWidth());
      else this.analyserResolution = resolutionSize;
    }
    bindCameraUseCases();
  }

  protected abstract void processFrame(Bitmap image, ImageProxy imageProxy);
}
