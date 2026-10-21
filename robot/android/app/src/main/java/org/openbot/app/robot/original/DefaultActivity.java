/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

// Modified by Matthias Mueller - Intel Intelligent Systems Lab - 2020

package org.openbot.app.robot.original;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.openbot.app.robot.R;
import org.openbot.app.robot.customview.OverlayView;
import org.openbot.app.robot.customview.OverlayView.DrawCallback;
import org.openbot.app.robot.env.BorderedText;
import org.openbot.app.robot.env.BotToControllerEventBus;
import org.openbot.app.robot.env.ImageUtils;
import org.openbot.app.robot.env.Logger;
import org.openbot.app.robot.tflite.Autopilot;
import org.openbot.app.robot.tflite.Detector;
import org.openbot.app.robot.tflite.Model;
import org.openbot.app.robot.tflite.Network.Device;
import org.openbot.app.robot.tracking.MultiBoxTracker;
import org.openbot.app.robot.utils.ConnectionUtils;
import org.openbot.app.robot.utils.Enums.ControlMode;
import org.openbot.app.robot.utils.Enums.LogMode;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DefaultActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 720); // 16:9

  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Detector detector;
  private Autopilot autopilot;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingNetwork = false;
  private long frameNum = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;
  private BorderedText borderedText;

  public DefaultActivity() {}

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);
    tracker.setDynamicSpeed(preferencesManager.getDynamicSpeed());

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - ImageUtils.getScreenOrientation(this);
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

    recreateNetwork(getModel(), getDevice(), getNumThreads());
    if (detector == null && autopilot == null) {
      LOGGER.e("No network on preview!");
      return;
    }

    trackingOverlay = findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });
    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  @Override
  protected void processImage() {
    ++frameNum;
    final long currFrameNum = frameNum;
    // trackingOverlay.postInvalidate();

    // If network is busy and we don't need to log any image, return.
    if (computingNetwork && !loggingEnabled) {
      readyForNextImage();
      return;
    }

    final boolean SAVE_PREVIEW_BITMAP =
        logMode.equals(LogMode.ALL_IMGS) || logMode.equals(LogMode.PREVIEW_IMG);
    final boolean SAVE_CROP_BITMAP =
        logMode.equals(LogMode.ALL_IMGS) || logMode.equals(LogMode.CROP_IMG);

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    if (loggingEnabled && SAVE_PREVIEW_BITMAP) {
      runInBackground(
          () ->
              ImageUtils.saveBitmap(
                  rgbFrameBitmap,
                  logFolder + File.separator + "images",
                  currFrameNum + "_preview.jpeg"));
      if (!SAVE_CROP_BITMAP) sendFrameNumberToSensorService(currFrameNum);
    }

    readyForNextImage();
    // If network is busy and we don't need to log the crop, return.
    if (computingNetwork && !SAVE_CROP_BITMAP) {
      return;
    }

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (loggingEnabled && SAVE_CROP_BITMAP) {
      runInBackground(
          () ->
              ImageUtils.saveBitmap(
                  croppedBitmap,
                  logFolder + File.separator + "images",
                  currFrameNum + "_crop.jpeg"));
      sendFrameNumberToSensorService(currFrameNum);
    }

    // Network is control of the vehicle
    if (networkEnabled) {
      // If network is busy, return.
      if (computingNetwork) {
        return;
      }

      computingNetwork = true;
      LOGGER.i("Putting image " + currFrameNum + " for detection in bg thread.");

      runInBackground(
          () -> {
            if (detector != null) {
              LOGGER.i("Running detection on image " + currFrameNum);
              final long startTime = SystemClock.elapsedRealtime();
              final List<Detector.Recognition> results =
                  detector.recognizeImage(croppedBitmap, "person");
              lastProcessingTimeMs = SystemClock.elapsedRealtime() - startTime;

              if (!results.isEmpty())
                LOGGER.i(
                    "Object: "
                        + results.get(0).getLocation().centerX()
                        + ", "
                        + results.get(0).getLocation().centerY()
                        + ", "
                        + results.get(0).getLocation().height()
                        + ", "
                        + results.get(0).getLocation().width());

              cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
              final Canvas canvas1 = new Canvas(cropCopyBitmap);
              final Paint paint = new Paint();
              paint.setColor(Color.RED);
              paint.setStyle(Style.STROKE);
              paint.setStrokeWidth(2.0f);

              float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;

              final List<Detector.Recognition> mappedRecognitions =
                  new LinkedList<Detector.Recognition>();

              for (final Detector.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= minimumConfidence) {
                  canvas1.drawRect(location, paint);
                  cropToFrameTransform.mapRect(location);
                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
              }

              tracker.trackResults(mappedRecognitions, currFrameNum);
              controllerHandler.handleDriveCommand(tracker.updateTarget());
              trackingOverlay.postInvalidate();
            } else if (autopilot != null) {
              LOGGER.i("Running autopilot on image " + currFrameNum);
              final long startTime = SystemClock.elapsedRealtime();
              controllerHandler.handleDriveCommand(
                  autopilot.recognizeImage(croppedBitmap, vehicle.getIndicator()));
              lastProcessingTimeMs = SystemClock.elapsedRealtime() - startTime;
            }

            computingNetwork = false;

            if (loggingEnabled) {
              sendInferenceTimeToSensorService(currFrameNum, lastProcessingTimeMs);
            }
          });
    }

    runOnUiThread(
        () -> {
          frameValueTextView.setText(
              String.format(Locale.US, "%d x %d", previewWidth, previewHeight));
          cropValueTextView.setText(
              String.format(
                  Locale.US, "%d x %d", croppedBitmap.getWidth(), croppedBitmap.getHeight()));
          if (networkEnabled)
            inferenceTimeTextView.setText(String.format(Locale.US, "%d ms", lastProcessingTimeMs));
        });
  }

  protected void updateVehicleControl() {

    // Log controls
    if (loggingEnabled) {
      runInBackground(this::sendControlToSensorService);
    }

    // Update GUI
    runOnUiThread(
        () -> {
          Log.i("display_ctrl", "runnable");
          if (controlValueTextView != null)
            controlValueTextView.setText(
                String.format(
                    Locale.US, "%.0f,%.0f", vehicle.getLeftSpeed(), vehicle.getRightSpeed()));
        });
  }

  protected void toggleNoise() {
    noiseEnabled = !noiseEnabled;
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("NOISE", noiseEnabled));
    if (noiseEnabled) {
      vehicle.startNoise();
    } else vehicle.stopNoise();
    updateVehicleControl();
  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  protected void setNetworkEnabled(final boolean isChecked) {
    networkEnabled = isChecked;
    if (networkEnabled) {
      networkSwitchCompat.setText(R.string.on);
    } else {
      runInBackground(
          () -> {
            try {
              TimeUnit.MILLISECONDS.sleep(lastProcessingTimeMs);
              controllerHandler.handleDriveCommand(0.f, 0.f);
              runOnUiThread(() -> inferenceTimeTextView.setText(R.string.time_ms));
            } catch (InterruptedException e) {
              LOGGER.e(e, "Got interrupted.");
            }
          });
      networkSwitchCompat.setText(R.string.off);
    }

    networkSwitchCompat.setChecked(networkEnabled);
    if (networkEnabled) {
      controlModeSpinner.setAlpha(0.5f);
      driveModeSpinner.setAlpha(0.5f);
      speedModeSpinner.setAlpha(0.5f);
    } else {
      controlModeSpinner.setAlpha(1.0f);
      driveModeSpinner.setAlpha(1.0f);
      speedModeSpinner.setAlpha(1.0f);
    }
    controlModeSpinner.setEnabled(!networkEnabled);
    driveModeSpinner.setEnabled(!networkEnabled);
    speedModeSpinner.setEnabled(!networkEnabled);
  }

  @Override
  protected void onInferenceConfigurationChanged() {
    computingNetwork = false;
    if (croppedBitmap == null) {
      // Defer creation until we're getting camera frames.
      return;
    }
    final Device device = getDevice();
    final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateNetwork(model, device, numThreads));
  }

  private void recreateNetwork(Model model, Device device, int numThreads) {
    if (model == null) return;
    tracker.clearTrackedObjects();
    if (detector != null) {
      LOGGER.d("Closing detector.");
      detector.close();
      detector = null;
    }
    if (autopilot != null) {
      LOGGER.d("Closing autoPilot.");
      autopilot.close();
      autopilot = null;
    }

    try {
      if (model.type == Model.TYPE.DETECTOR) {
        LOGGER.d(
            "Creating detector (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
        detector = Detector.create(this, model, device, numThreads);
        croppedBitmap =
            Bitmap.createBitmap(
                detector.getImageSizeX(), detector.getImageSizeY(), Config.ARGB_8888);
        frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                previewWidth,
                previewHeight,
                croppedBitmap.getWidth(),
                croppedBitmap.getHeight(),
                sensorOrientation,
                detector.getCropRect(),
                detector.getMaintainAspect());
      } else {
        LOGGER.d(
            "Creating autopilot (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
        autopilot = new Autopilot(this, model, device, numThreads);
        croppedBitmap =
            Bitmap.createBitmap(
                autopilot.getImageSizeX(), autopilot.getImageSizeY(), Config.ARGB_8888);
        frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                previewWidth,
                previewHeight,
                croppedBitmap.getWidth(),
                croppedBitmap.getHeight(),
                sensorOrientation,
                autopilot.getCropRect(),
                autopilot.getMaintainAspect());
      }

      cropToFrameTransform = new Matrix();
      frameToCropTransform.invert(cropToFrameTransform);

    } catch (IllegalArgumentException | IOException e) {
      String msg = "Failed to create network: ";
      LOGGER.e(e, msg);
      Toast.makeText(this, msg + e.toString(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public boolean dispatchGenericMotionEvent(MotionEvent event) {
    // Make sure vehicle is not controlled by network
    if (!networkEnabled) {
      // Check that the event came from a game controller
      if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
          && event.getAction() == MotionEvent.ACTION_MOVE
          && controlMode == ControlMode.GAMEPAD) {
        // Process the current movement sample in the batch (position -1)
        controllerHandler.handleDriveCommand(gameController.processJoystickInput(event, -1));
        return true;
      }
    }
    return super.dispatchGenericMotionEvent(event);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Check that the event came from a game controller
    if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        && controlMode == ControlMode.GAMEPAD) {
      // Only handle key once (when released)
      if (event.getAction() == KeyEvent.ACTION_UP) {
        switch (event.getKeyCode()) {
          case KeyEvent.KEYCODE_BUTTON_A: // x
            controllerHandler.handleLogging();
            break;
          case KeyEvent.KEYCODE_BUTTON_X: // square
            controllerHandler.handleIndicatorLeft();
            break;
          case KeyEvent.KEYCODE_BUTTON_Y: // triangle
            controllerHandler.handleIndicatorStop();
            break;
          case KeyEvent.KEYCODE_BUTTON_B: // circle
            controllerHandler.handleIndicatorRight();
            break;
          case KeyEvent.KEYCODE_BUTTON_START: // options
            controllerHandler.handleNoise();
            break;
          case KeyEvent.KEYCODE_BUTTON_L1:
            controllerHandler.handleDriveMode();
            break;
          case KeyEvent.KEYCODE_BUTTON_R1:
            controllerHandler.handleNetwork();
            break;
          case KeyEvent.KEYCODE_BUTTON_THUMBL:
            controllerHandler.handleSpeedDown();
            break;
          case KeyEvent.KEYCODE_BUTTON_THUMBR:
            controllerHandler.handleSpeedUp();
            break;
          default:
            //               makeText(this,"Key " + event.getKeyCode() + " not recognized",
            //                       LENGTH_SHORT).show();
            break;
        }
      }
      return true;
    }
    return super.dispatchKeyEvent(event);
  }
}
