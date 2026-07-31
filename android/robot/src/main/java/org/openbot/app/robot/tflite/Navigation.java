// Created by Matthias Mueller - Intel Intelligent Systems Lab - 2020

package org.openbot.app.robot.tflite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.openbot.app.robot.vehicle.Control;
import timber.log.Timber;

public class Navigation extends Network {

  private static final float IMAGE_MEAN = 0.0f;
  private static final float IMAGE_STD = 255.0f;

  /**
   * Creates a goal navigation policy with the provided configuration.
   *
   * @param activity The current Activity.
   * @param model The model to use for classification.
   * @param device The device to use for classification.
   * @param numThreads The number of threads to use for classification.
   * @return A detector with the desired configuration.
   */

  /** A ByteBuffer to hold data, to be feed into Tensorflow Lite as inputs. */
  protected ByteBuffer goalBuffer = null;

  private int goalIndex;
  private int imgIndex;

  /** Initializes a {@code Autopilot}. */
  public Navigation(Activity activity, Model model, Device device, int numThreads)
      throws IOException, IllegalArgumentException {
    super(activity, model, device, numThreads);

    goalIndex = tflite.getInputIndex("serving_default_goal_input:0");
    imgIndex = tflite.getInputIndex("serving_default_img_input:0");

    if (!Arrays.equals(
        tflite.getInputTensor(imgIndex).shape(),
        new int[] {1, getImageSizeY(), getImageSizeX(), 3})) {
      throw new IllegalArgumentException("Invalid tensor dimensions");
    }

    goalBuffer = ByteBuffer.allocateDirect(3 * 4);
    goalBuffer.order(ByteOrder.nativeOrder());

    Timber.d("Created a tflite navigation policy.");
  }

  private void convertGoalToByteBuffer(float goalDistance, float goalSin, float goalCos) {
    if (goalBuffer == null) {
      return;
    }
    goalBuffer.rewind();
    goalBuffer.putFloat(goalDistance);
    goalBuffer.putFloat(goalSin);
    goalBuffer.putFloat(goalCos);
  }

  public Control recognizeImage(
      final Bitmap bitmap, final float goalDistance, final float goalSin, final float goalCos) {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");
    Trace.beginSection("preprocessBitmap");
    convertBitmapToByteBuffer(bitmap);
    convertGoalToByteBuffer(goalDistance, goalSin, goalCos);
    Trace.endSection(); // preprocessBitmap

    // Run the inference call.
    Trace.beginSection("runInference");
    long startTime = SystemClock.elapsedRealtime();
    Object[] inputArray;
    if (goalIndex == 0) {
      inputArray = new Object[] {goalBuffer, imgData};
    } else {
      inputArray = new Object[] {imgData, goalBuffer};
    }

    float[][] predicted_ctrl = new float[1][2];
    outputMap.put(0, predicted_ctrl);
    tflite.runForMultipleInputsOutputs(inputArray, outputMap);
    long endTime = SystemClock.elapsedRealtime();
    Trace.endSection();
    Timber.v("Timecost to run model inference: %s", (endTime - startTime));

    Trace.endSection(); // "recognizeImage"
    return new Control(predicted_ctrl[0][0], predicted_ctrl[0][1]);
  }

  @Override
  protected int getNumBytesPerChannel() {
    return 4; // Float.SIZE / Byte.SIZE;
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
  }

  @Override
  public boolean getMaintainAspect() {
    return false;
  }

  @Override
  public RectF getCropRect() {
    return null;
  }
}
