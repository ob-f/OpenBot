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

package org.openbot.app.robot.tflite;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Size;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import org.openbot.app.robot.env.Logger;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

public abstract class Network {

  protected static final Logger LOGGER = new Logger();

  /** The runtime device type used for execution. */
  public enum Device {
    CPU,
    GPU,
    NNAPI
  }

  /** Dimensions of inputs. */
  protected static final int DIM_BATCH_SIZE = 1;

  protected static final int DIM_PIXEL_SIZE = 3;

  private final Size imageSize;

  /** Preallocated buffers for storing image data in. */
  protected final int[] intValues;

  /** Options for configuring the Interpreter. */
  protected final Interpreter.Options tfliteOptions = new Interpreter.Options();

  /** Optional GPU delegate for accleration. */
  protected GpuDelegate gpuDelegate = null;

  /** An instance of the driver class to run model inference with Tensorflow Lite. */
  protected Interpreter tflite;

  /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
  protected ByteBuffer imgData = null;
  /** A HashMap to hold output data, to be feed into Tensorflow Lite as outputs. */
  protected Map<Integer, Object> outputMap = new HashMap<>();

  /** Initializes a {@code Network}. */
  protected Network(Activity activity, Model model, Device device, int numThreads)
      throws IOException {

    imageSize = model.getInputSize();
    intValues = new int[getImageSizeX() * getImageSizeY()];

    switch (device) {
      case NNAPI:
        tfliteOptions.setUseNNAPI(true);
        break;
      case GPU:
        gpuDelegate = new GpuDelegate();
        tfliteOptions.addDelegate(gpuDelegate);
        break;
      case CPU:
        break;
    }
    tfliteOptions.setNumThreads(numThreads);

    if (model.pathType == Model.PATH_TYPE.FILE) {
      File modelFile = new File(model.path);
      tflite = new Interpreter(modelFile, tfliteOptions);
    } else if (model.pathType == Model.PATH_TYPE.ASSET) {
      MappedByteBuffer tfliteModel = loadModelFile(activity, model);
      tflite = new Interpreter(tfliteModel, tfliteOptions);
    } else {
      throw (new IOException("No model file specified!"));
    }

    imgData =
        ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE
                * getImageSizeX()
                * getImageSizeY()
                * DIM_PIXEL_SIZE
                * getNumBytesPerChannel());
    imgData.order(ByteOrder.nativeOrder());
    LOGGER.d("Created a Tensorflow Lite Network.");
  }

  /** Memory-map the model file in Assets. */
  protected MappedByteBuffer loadModelFile(Activity activity, Model model) throws IOException {
    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(model.path);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /** Writes Image data into a {@code ByteBuffer}. */
  protected void convertBitmapToByteBuffer(Bitmap bitmap) {
    if (imgData == null) {
      return;
    }
    imgData.rewind();
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Convert the image to floating point.
    int pixel = 0;
    long startTime = SystemClock.elapsedRealtime();
    for (int i = 0; i < getImageSizeX(); ++i) {
      for (int j = 0; j < getImageSizeY(); ++j) {
        final int val = intValues[pixel++];
        addPixelValue(val);
      }
    }
    long endTime = SystemClock.elapsedRealtime();
    LOGGER.v("Timecost to put values into ByteBuffer: " + (endTime - startTime));
  }

  /** Closes the interpreter and model to release resources. */
  public void close() {
    if (tflite != null) {
      tflite.close();
      tflite = null;
    }
    if (gpuDelegate != null) {
      gpuDelegate.close();
      gpuDelegate = null;
    }
  }

  /**
   * Get the image size along the x axis.
   *
   * @return
   */
  public int getImageSizeX() {
    return imageSize.getWidth();
  }

  /**
   * Get the image size along the y axis.
   *
   * @return
   */
  public int getImageSizeY() {
    return imageSize.getHeight();
  }

  /**
   * Get the number of bytes that is used to store a single color channel value.
   *
   * @return
   */
  protected abstract int getNumBytesPerChannel();

  /**
   * Add pixelValue to byteBuffer.
   *
   * @param pixelValue
   */
  protected abstract void addPixelValue(int pixelValue);

  /**
   * Get boolean that determines if aspect ratio should be preserved when rescaling.
   *
   * @return
   */
  public abstract boolean getMaintainAspect();

  /**
   * Get a rect that determines a percentage to be cropped from left, top, right, bottom.
   *
   * @return
   */
  public abstract RectF getCropRect();
}
