package org.openbot.app.robot.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.text.TextUtils;
import android.util.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import timber.log.Timber;

public class CameraUtils {

  public static Size getClosestCameraResolution(Context context, Size resolution) {
    final Activity activity = (Activity) context;
    final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != CameraCharacteristics.LENS_FACING_BACK) {
          continue;
        }
        final Size[] outputSizes =
            characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null) {
          Timber.e("No output sizes compatible with SurfaceTexture.");
          continue;
        }
        resolution = chooseOptimalSize(outputSizes, resolution);
      }
    } catch (final CameraAccessException e) {
      Timber.e(e, "Unable to open the camera.");
    }
    return resolution;
  }

  /**
   * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
   * width and height are at least as large as the minimum of both, or an exact match if possible.
   *
   * @param choices The list of sizes that the camera supports for the intended output class
   * @param desiredSize The desired size
   * @param minSize The minimum size
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   */
  public static Size chooseOptimalSize(
      final Size[] choices, final Size desiredSize, final Size minSize) {
    // Collect the supported resolutions that are at least as big as the preview Surface
    boolean exactSizeFound = false;
    final List<Size> correctAspect = new ArrayList<Size>();
    final List<Size> bigEnough = new ArrayList<Size>();
    final List<Size> tooSmall = new ArrayList<Size>();
    for (final Size option : choices) {
      Timber.i("Size: " + option.getWidth() + "x" + option.getHeight());
      if (option.equals(desiredSize)) {
        // Set the size but don't return yet so that remaining sizes will still be logged.
        exactSizeFound = true;
      }

      if (option.getHeight() >= minSize.getHeight() && option.getWidth() >= minSize.getWidth()) {
        // Check for aspect ratio
        if (desiredSize.getWidth() / option.getWidth()
            == desiredSize.getHeight() / option.getHeight()) {
          correctAspect.add(option);
        }
        bigEnough.add(option);
      } else {
        tooSmall.add(option);
      }
    }

    Timber.i("Desired size: " + desiredSize + ", min size: " + minSize);
    Timber.i("Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
    Timber.i("Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

    if (exactSizeFound) {
      Timber.i("Exact size match found.");
      return desiredSize;
    }

    // Pick the smallest of those, assuming we found any
    if (correctAspect.size() > 0) {
      final Size chosenSize = Collections.min(correctAspect, new CompareSizesByArea());
      Timber.i("Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
      return chosenSize;
    } else if (bigEnough.size() > 0) {
      final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
      Timber.i("Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
      return chosenSize;
    } else {
      Timber.e("Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  public static Size chooseOptimalSize(final Size[] choices, final Size desiredSize) {
    return chooseOptimalSize(choices, desiredSize, desiredSize);
  }

  /** Compares two {@code Size}s based on their areas. */
  static class CompareSizesByArea implements Comparator<Size> {
    @Override
    public int compare(final Size lhs, final Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }

  /** Flip bitmap horizontally (left to right) */
  public static Bitmap flipBitmapHorizontal(Bitmap bitmap) {
    Matrix matrix = new Matrix();
    matrix.preScale(-1.0f, 1.0f);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }
}
