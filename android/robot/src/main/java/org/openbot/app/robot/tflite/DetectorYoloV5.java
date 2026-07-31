package org.openbot.app.robot.tflite;

import android.app.Activity;
import android.graphics.RectF;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.Tensor;

public class DetectorYoloV5 extends Detector {

  /** Additional normalization of the used input. */
  private static final float IMAGE_MEAN = 0.0f;

  private static final float IMAGE_STD = 255.0f;
  private int inputSize = getImageSizeX();
  private int output_box =
      (int)
          ((Math.pow((inputSize / 32), 2)
                  + Math.pow((inputSize / 16), 2)
                  + Math.pow((inputSize / 8), 2))
              * 3);
  int numClass;
  boolean isModelQuantized;
  private float inputScale;
  private int inputZeroPoint;
  private float outputScale;
  private int outputZeroPoint;
  private ByteBuffer outData;

  /**
   * Initializes a {@code DetectorYoloV5}.
   *
   * @param activity
   */
  public DetectorYoloV5(Activity activity, Model model, Device device, int numThreads)
      throws IOException {
    super(activity, model, device, numThreads);
  }

  @Override
  public boolean getMaintainAspect() {
    return false;
  }

  @Override
  public RectF getCropRect() {
    return new RectF(0.0f, 0.0f, 0.0f, 0.0f);
  }

  @Override
  protected String getLabelPath() {
    return "networks/coco.txt";
  }

  @Override
  protected int getNumBytesPerChannel() {
    parseTflite();
    return isModelQuantized ? 1 : 4;
  }

  @Override
  protected final int getNumDetections() {
    return NUM_DETECTIONS;
  }

  @Override
  protected final void parseTflite() {
    Tensor inputTensor = tflite.getInputTensor(0);
    Tensor outputTensor = tflite.getOutputTensor(0);
    inputScale = inputTensor.quantizationParams().getScale();
    inputZeroPoint = inputTensor.quantizationParams().getZeroPoint();
    outputScale = outputTensor.quantizationParams().getScale();
    outputZeroPoint = outputTensor.quantizationParams().getZeroPoint();
    isModelQuantized = (int) (outputScale + outputZeroPoint) == 0 ? false : true;
    int[] shape = outputTensor.shape();
    numClass = shape[shape.length - 1] - 5;
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    if (isModelQuantized) {
      imgData.put(
          (byte)
              ((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD / inputScale
                  + inputZeroPoint));
      imgData.put(
          (byte)
              ((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD / inputScale
                  + inputZeroPoint));
      imgData.put(
          (byte) (((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD / inputScale + inputZeroPoint));
    } else {
      imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
      imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
      imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }
  }

  @Override
  protected void runInference() {
    Object[] inputArray = {imgData};
    try {
      tflite.runForMultipleInputsOutputs(inputArray, outputMap);
    }
    catch(Exception e){
      System.out.println("Error Occurred: " + e);
    }
  }

  @Override
  protected void feedData() {
    outData = ByteBuffer.allocateDirect(output_box * (numClass + 5) * getNumBytesPerChannel());
    outData.order(ByteOrder.nativeOrder());
    outData.rewind();
    outputMap.put(0, outData);
  }

  @Override
  protected List<Recognition> getRecognitions(String className) {
    // Show the best detections.
    // after scaling them back to the input size.
    ByteBuffer byteBuffer = (ByteBuffer) outputMap.get(0);
    byteBuffer.rewind();

    ArrayList<Recognition> recognitions = new ArrayList<>();

    float[][][] out = new float[1][output_box][numClass + 5];
    for (int i = 0; i < output_box; ++i) {
      for (int j = 0; j < numClass + 5; ++j) {
        if (isModelQuantized) {
          out[0][i][j] = outputScale * (((int) byteBuffer.get() & 0xFF) - outputZeroPoint);
        } else {
          out[0][i][j] = byteBuffer.getFloat();
        }
      }
      // Denormalize xywh
      for (int j = 0; j < 4; ++j) {
        out[0][i][j] *= inputSize;
      }
    }

    for (int i = 0; i < output_box; ++i) {
      final int offset = 0;
      final float confidence = out[0][i][4];
      int classId = -1;
      float maxClass = 0;

      final float[] classes = new float[labels.size()];
      for (int c = 0; c < labels.size(); ++c) {
        classes[c] = out[0][i][5 + c];
      }

      for (int c = 0; c < labels.size(); ++c) {
        if (classes[c] > maxClass) {
          classId = c;
          maxClass = classes[c];
        }
      }

      final float score = maxClass * confidence;
      if (score > getObjThresh()) {
        final float xPos = out[0][i][0];
        final float yPos = out[0][i][1];

        final float w = out[0][i][2];
        final float h = out[0][i][3];

        final RectF detection =
            new RectF(
                Math.max(0, xPos - w / 2),
                Math.max(0, yPos - h / 2),
                Math.min(getImageSizeX() - 1, xPos + w / 2),
                Math.min(getImageSizeY() - 1, yPos + h / 2));
        if (classId > -1 && labels.get(classId).contentEquals(className)) {
          recognitions.add(new Recognition("" + i, labels.get(classId), score, detection, classId));
        }
      }
    }
    return nms(recognitions);
  }

  /**
   * Retrieves both recognized objects from the model's output and organizes them into ArrayLists
   * based on class names.
   *
   * @return An ArrayList of ArrayLists containing Recognition results for both classes.
   */
  @Override
  protected ArrayList<ArrayList<Recognition>> getMultipleRecognitions(String classNameFirst, String classNameSecond) {
    // Show the best detections.
    // after scaling them back to the input size.
    ByteBuffer byteBuffer = (ByteBuffer) outputMap.get(0);
    byteBuffer.rewind();

    final ArrayList<ArrayList<Recognition>> allRecognitions = new ArrayList<>(getNumDetections());
    allRecognitions.add(new ArrayList<>());
    allRecognitions.add(new ArrayList<>());

    float[][][] out = new float[1][output_box][numClass + 5];
    for (int i = 0; i < output_box; ++i) {
      for (int j = 0; j < numClass + 5; ++j) {
        if (isModelQuantized) {
          out[0][i][j] = outputScale * (((int) byteBuffer.get() & 0xFF) - outputZeroPoint);
        } else {
          out[0][i][j] = byteBuffer.getFloat();
        }
      }
      // Denormalize xywh
      for (int j = 0; j < 4; ++j) {
        out[0][i][j] *= inputSize;
      }
    }

    for (int i = 0; i < output_box; ++i) {
      final int offset = 0;
      final float confidence = out[0][i][4];
      int classId = -1;
      float maxClass = 0;

      final float[] classes = new float[labels.size()];
      for (int c = 0; c < labels.size(); ++c) {
        classes[c] = out[0][i][5 + c];
      }

      for (int c = 0; c < labels.size(); ++c) {
        if (classes[c] > maxClass) {
          classId = c;
          maxClass = classes[c];
        }
      }

      final float score = maxClass * confidence;
      if (score > getObjThresh()) {
        final float xPos = out[0][i][0];
        final float yPos = out[0][i][1];

        final float w = out[0][i][2];
        final float h = out[0][i][3];

        final RectF detection =
                new RectF(
                        Math.max(0, xPos - w / 2),
                        Math.max(0, yPos - h / 2),
                        Math.min(getImageSizeX() - 1, xPos + w / 2),
                        Math.min(getImageSizeY() - 1, yPos + h / 2));
        if (classId > -1 && labels.get(classId).contentEquals(classNameFirst)) {
          allRecognitions.get(0).add(new Recognition("" + i, labels.get(classId), score, detection, classId));
        } else if (classId > -1 && labels.get(classId).contentEquals(classNameSecond)) {
          allRecognitions.get(1).add(new Recognition("" + i, labels.get(classId), score, detection, classId));
        }
      }
    }
    return multipleNMS(allRecognitions);
  }

  @Override
  protected List<Recognition> getAllRecognition() {
    // Show the best detections.
    // after scaling them back to the input size.
    ByteBuffer byteBuffer = (ByteBuffer) outputMap.get(0);
    byteBuffer.rewind();

    ArrayList<Recognition> recognitions = new ArrayList<>();

    float[][][] out = new float[1][output_box][numClass + 5];
    for (int i = 0; i < output_box; ++i) {
      for (int j = 0; j < numClass + 5; ++j) {
        if (isModelQuantized) {
          out[0][i][j] = outputScale * (((int) byteBuffer.get() & 0xFF) - outputZeroPoint);
        } else {
          out[0][i][j] = byteBuffer.getFloat();
        }
      }
      // Denormalize xywh
      for (int j = 0; j < 4; ++j) {
        out[0][i][j] *= inputSize;
      }
    }

    for (int i = 0; i < output_box; ++i) {
      final int offset = 0;
      final float confidence = out[0][i][4];
      int classId = -1;
      float maxClass = 0;

      final float[] classes = new float[labels.size()];
      for (int c = 0; c < labels.size(); ++c) {
        classes[c] = out[0][i][5 + c];
      }

      for (int c = 0; c < labels.size(); ++c) {
        if (classes[c] > maxClass) {
          classId = c;
          maxClass = classes[c];
        }
      }

      final float score = maxClass * confidence;
      if (score > getObjThresh()) {
        final float xPos = out[0][i][0];
        final float yPos = out[0][i][1];

        final float w = out[0][i][2];
        final float h = out[0][i][3];

        final RectF detection =
                new RectF(
                        Math.max(0, xPos - w / 2),
                        Math.max(0, yPos - h / 2),
                        Math.min(getImageSizeX() - 1, xPos + w / 2),
                        Math.min(getImageSizeY() - 1, yPos + h / 2));
        recognitions.add(new Recognition("" + i, labels.get(classId), score, detection, classId));
      }
    }
    return nms(recognitions);
  }
}
