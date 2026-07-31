/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

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
import android.graphics.RectF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** This TensorFlow Lite detector works with the quantized MobileNet and EfficientDet model. */
public class DetectorDefault extends Detector {

  // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
  // contains the location of detected boxes
  private float[][][] outputLocations;
  // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
  // contains the classes of detected boxes
  private float[][] outputClasses;
  // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
  // contains the scores of detected boxes
  private float[][] outputScores;
  // numDetections: array of shape [Batchsize]
  // contains the number of detected boxes
  private float[] numDetections;

  // indices in tflite model
  private int outputLocationsIdx;
  private int outputClassesIdx;
  private int outputScoresIdx;
  private int numDetectionsIdx;

  /**
   * Initializes a {@code DetectorDefault}.
   *
   * @param activity
   */
  public DetectorDefault(Activity activity, Model model, Device device, int numThreads)
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
    return "networks/labelmap.txt";
  }

  @Override
  protected int getNumBytesPerChannel() {
    // the quantized model uses a single byte only
    return 1;
  }

  @Override
  protected final int getNumDetections() {
    return NUM_DETECTIONS;
  }

  @Override
  protected final void parseTflite() {
    try {
      outputLocationsIdx = tflite.getOutputIndex("TFLite_Detection_PostProcess");
      outputClassesIdx = tflite.getOutputIndex("TFLite_Detection_PostProcess:1");
      outputScoresIdx = tflite.getOutputIndex("TFLite_Detection_PostProcess:2");
      numDetectionsIdx = tflite.getOutputIndex("TFLite_Detection_PostProcess:3");
      NUM_DETECTIONS = tflite.getOutputTensor(outputLocationsIdx).shape()[1];
    } catch (IllegalArgumentException e) {
      outputLocationsIdx = tflite.getOutputIndex("StatefulPartitionedCall:3");
      outputClassesIdx = tflite.getOutputIndex("StatefulPartitionedCall:2");
      outputScoresIdx = tflite.getOutputIndex("StatefulPartitionedCall:1");
      numDetectionsIdx = tflite.getOutputIndex("StatefulPartitionedCall:0");
      NUM_DETECTIONS = tflite.getOutputTensor(outputLocationsIdx).shape()[1];
    }
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
    imgData.put((byte) (pixelValue & 0xFF));
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
    outputClasses = new float[1][getNumDetections()];
    outputScores = new float[1][getNumDetections()];
    numDetections = new float[1];

    outputMap.put(outputLocationsIdx, outputLocations);
    outputMap.put(outputClassesIdx, outputClasses);
    outputMap.put(outputScoresIdx, outputScores);
    outputMap.put(numDetectionsIdx, numDetections);
  }

  @Override
  protected List<Recognition> getRecognitions(String className) {
    // Show the best detections.
    // after scaling them back to the input size.
    final ArrayList<Recognition> recognitions = new ArrayList<>(getNumDetections());
    for (int i = 0; i < getNumDetections(); ++i) {
      final RectF detection =
          new RectF(
              outputLocations[0][i][1] * getImageSizeY(),
              outputLocations[0][i][0] * getImageSizeX(),
              outputLocations[0][i][3] * getImageSizeY(),
              outputLocations[0][i][2] * getImageSizeX());
      // SSD Mobilenet V1 Model assumes class 0 is background class
      // in label file and class labels start from 1 to number_of_classes+1,
      // while outputClasses correspond to class index from 0 to number_of_classes
      int classId = (int) outputClasses[0][i];
      int labelId = classId + 1;
      if (labels.get(labelId).contentEquals(className)) {
        recognitions.add(
            new Recognition("" + i, labels.get(labelId), outputScores[0][i], detection, classId));
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
    final ArrayList<ArrayList<Recognition>> multipleRecognitions = new ArrayList<>(getNumDetections());
    multipleRecognitions.add(new ArrayList<>());
    multipleRecognitions.add(new ArrayList<>());
    for (int i = 0; i < getNumDetections(); ++i) {
      final RectF detection =
              new RectF(
                      outputLocations[0][i][1] * getImageSizeY(),
                      outputLocations[0][i][0] * getImageSizeX(),
                      outputLocations[0][i][3] * getImageSizeY(),
                      outputLocations[0][i][2] * getImageSizeX());
      // SSD Mobilenet V1 Model assumes class 0 is background class
      // in label file and class labels start from 1 to number_of_classes+1,
      // while outputClasses correspond to class index from 0 to number_of_classes
      int classId = (int) outputClasses[0][i];
      int labelId = classId + 1;
      if (multipleRecognitions.get(0).isEmpty() && labels.get(labelId).contentEquals(classNameFirst)) {
        multipleRecognitions.get(0).add(
                new Recognition("" + i, labels.get(labelId), outputScores[0][i], detection, classId));
      }
      if (multipleRecognitions.get(1).isEmpty() && labels.get(labelId).contentEquals(classNameSecond)) {
        multipleRecognitions.get(1).add(
                new Recognition("" + i, labels.get(labelId), outputScores[0][i], detection, classId));
      }
    }
    return multipleNMS(multipleRecognitions);
  }

  @Override
  protected ArrayList<Recognition> getAllRecognition() {
    final ArrayList<Recognition> allRecognitions = new ArrayList<>(getNumDetections());
    for (int i = 0; i < getNumDetections(); ++i) {
      final RectF detection =
              new RectF(
                      outputLocations[0][i][1] * getImageSizeY(),
                      outputLocations[0][i][0] * getImageSizeX(),
                      outputLocations[0][i][3] * getImageSizeY(),
                      outputLocations[0][i][2] * getImageSizeX());
      // SSD Mobilenet V1 Model assumes class 0 is background class
      // in label file and class labels start from 1 to number_of_classes+1,
      // while outputClasses correspond to class index from 0 to number_of_classes
      int classId = (int) outputClasses[0][i];
      int labelId = classId + 1;
      allRecognitions.add(
                new Recognition("" + i, labels.get(labelId), outputScores[0][i], detection, classId));
    }
    return nms(allRecognitions);
  }

}
