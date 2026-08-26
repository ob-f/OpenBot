package org.openbot.app.robot.tflite;

import android.app.Activity;
import android.graphics.RectF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetectorYoloV4 extends Detector {

  /** Additional normalization of the used input. */
  private static final float IMAGE_MEAN = 0.0f;

  private static final float IMAGE_STD = 255.0f;

  // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
  // contains the location of detected boxes
  private float[][][] outputLocations;
  // outputScores: array of shape [Batchsize, NUM_DETECTIONS,labels.size()]
  // contains the scores of detected boxes
  private float[][][] outputScores;

  // indices in tflite model
  private int outputLocationsIdx;
  private int outputScoresIdx;

  /**
   * Initializes a {@code DetectorYoloV4}.
   *
   * @param activity
   */
  public DetectorYoloV4(Activity activity, Model model, Device device, int numThreads)
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
    // the float model uses a four bytes
    return 4;
  }

  @Override
  protected final int getNumDetections() {
    return NUM_DETECTIONS;
  }

  @Override
  protected final void parseTflite() {
    try {
      outputLocationsIdx = tflite.getOutputIndex("Identity");
      outputScoresIdx = tflite.getOutputIndex("Identity_1");
    } catch (IllegalArgumentException e) {
      outputLocationsIdx = tflite.getOutputIndex("StatefulPartitionedCall:0");
      outputScoresIdx = tflite.getOutputIndex("StatefulPartitionedCall:1");
    }
    NUM_DETECTIONS = tflite.getOutputTensor(outputLocationsIdx).shape()[1];
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
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
    outputLocations = new float[1][getNumDetections()][4];
    outputScores = new float[1][getNumDetections()][labels.size()];

    outputMap.put(outputLocationsIdx, outputLocations);
    outputMap.put(outputScoresIdx, outputScores);
  }

  @Override
  protected List<Recognition> getRecognitions(String className) {
    // Show the best detections.
    // after scaling them back to the input size.
    final ArrayList<Recognition> recognitions = new ArrayList<>(getNumDetections());
    for (int i = 0; i < getNumDetections(); ++i) {
      float maxClass = 0;
      int classId = -1;
      final float[] classes = new float[labels.size()];
      System.arraycopy(outputScores[0][i], 0, classes, 0, labels.size());
      for (int c = 0; c < labels.size(); ++c) {
        if (classes[c] > maxClass) {
          classId = c;
          maxClass = classes[c];
        }
      }
      final float score = maxClass;
      final float xPos = outputLocations[0][i][0];
      final float yPos = outputLocations[0][i][1];
      final float w = outputLocations[0][i][2];
      final float h = outputLocations[0][i][3];
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
    final ArrayList<ArrayList<Recognition>> allRecognitions = new ArrayList<>(getNumDetections());
    allRecognitions.add(new ArrayList<>());
    allRecognitions.add(new ArrayList<>());
    for (int i = 0; i < getNumDetections(); ++i) {
      float maxClass = 0;
      int classId = -1;
      final float[] classes = new float[labels.size()];
      System.arraycopy(outputScores[0][i], 0, classes, 0, labels.size());
      for (int c = 0; c < labels.size(); ++c) {
        if (classes[c] > maxClass) {
          classId = c;
          maxClass = classes[c];
        }
      }
      final float score = maxClass;
      final float xPos = outputLocations[0][i][0];
      final float yPos = outputLocations[0][i][1];
      final float w = outputLocations[0][i][2];
      final float h = outputLocations[0][i][3];
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
    return multipleNMS(allRecognitions);
  }

  @Override
  protected List<Recognition> getAllRecognition() {
    // Show the best detections.
    // after scaling them back to the input size.
    final ArrayList<Recognition> recognitions = new ArrayList<>(getNumDetections());
    for (int i = 0; i < getNumDetections(); ++i) {
      float maxClass = 0;
      int classId = -1;
      final float[] classes = new float[labels.size()];
      System.arraycopy(outputScores[0][i], 0, classes, 0, labels.size());
      for (int c = 0; c < labels.size(); ++c) {
        if (classes[c] > maxClass) {
          classId = c;
          maxClass = classes[c];
        }
      }
      final float score = maxClass;
      final float xPos = outputLocations[0][i][0];
      final float yPos = outputLocations[0][i][1];
      final float w = outputLocations[0][i][2];
      final float h = outputLocations[0][i][3];
      final RectF detection =
              new RectF(
                      Math.max(0, xPos - w / 2),
                      Math.max(0, yPos - h / 2),
                      Math.min(getImageSizeX() - 1, xPos + w / 2),
                      Math.min(getImageSizeY() - 1, yPos + h / 2));
      recognitions.add(new Recognition("" + i, labels.get(classId), score, detection, classId));
    }
    return nms(recognitions);
  }
}
