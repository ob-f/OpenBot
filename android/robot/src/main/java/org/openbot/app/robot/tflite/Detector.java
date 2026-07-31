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
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import org.openbot.app.robot.objectNav.ObjectNavFragment;
import timber.log.Timber;

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * github.com/tensorflow/models/tree/master/research/object_detection
 */
public abstract class Detector extends Network {

  /** Labels corresponding to the output of the vision model. */
  protected List<String> labels;

  // Number of output detections
  protected int NUM_DETECTIONS;

  protected String modelPath;
  protected int imageSizeX;
  protected int imageSizeY;

  /**
   * Creates a detector with the provided configuration.
   *
   * @param activity The current Activity.
   * @param model The model to use for classification.
   * @param device The device to use for classification.
   * @param numThreads The number of threads to use for classification.
   * @return A detector with the desired configuration.
   */
  public static Detector create(Activity activity, Model model, Device device, int numThreads)
      throws IOException {
    switch (model.classType) {
      case MOBILENET:
      case EFFICIENTDET:
        return new DetectorDefault(activity, model, device, numThreads);
      case YOLOV4:
        return new DetectorYoloV4(activity, model, device, numThreads);
      case YOLOV5:
        return new DetectorYoloV5(activity, model, device, numThreads);
      default:
        return null;
    }
  }

  /** An immutable result returned by a Classifier/Detector describing what was recognized. */
  public static class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /** Display name for the recognition. */
    private final String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;

    /** Location within the source image for the location of the recognized object. */
    private RectF location;

    /** Detected class of the recognized object. */
    private int classId;

    public Recognition(
        final String id,
        final String title,
        final Float confidence,
        final RectF location,
        int classId) {
      this.id = id;
      this.title = title;
      this.confidence = confidence;
      this.location = location;
      this.classId = classId;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public Float getConfidence() {
      return confidence;
    }

    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }

    public int getClassId() {
      return classId;
    }

    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (title != null) {
        resultString += title + " ";
      }

      if (confidence != null) {
        resultString += String.format(Locale.US, "(%.1f%%) ", confidence * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }
  }

  /** Initializes a {@code Detector}. */
  protected Detector(Activity activity, Model model, Device device, int numThreads)
      throws IOException {
    super(activity, model, device, numThreads);

    labels = loadLabelList(activity);
    parseTflite();
    Timber.d("Created a Tensorflow Lite Detector.");
  }

  /** Reads label list from Assets. */
  private List<String> loadLabelList(Activity activity) throws IOException {
    List<String> labels = new ArrayList<String>();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
    String line;
    while ((line = reader.readLine()) != null) {
      labels.add(line);
    }
    reader.close();
    return labels;
  }

  public List<Recognition> recognizeImage(final Bitmap bitmap, String className)
      throws IllegalArgumentException {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    long startTime = SystemClock.elapsedRealtime();
    convertBitmapToByteBuffer(bitmap);
    long endTime = SystemClock.elapsedRealtime();
    Timber.v("Timecost to convertBitmapToByteBuffer: %s", (endTime - startTime));

    Trace.endSection(); // preprocessBitmap

    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    feedData();
    Trace.endSection();

    // Run the inference call.
    Trace.beginSection("runInference");
    startTime = SystemClock.elapsedRealtime();
    runInference();
    endTime = SystemClock.elapsedRealtime();
    Trace.endSection();
    Timber.v("Timecost to run model inference: %s", (endTime - startTime));

    Trace.endSection(); // "recognizeImage"

    startTime = SystemClock.elapsedRealtime();
    List<Recognition> recognitions;
    if (className == null){
      recognitions = getAllRecognition();
    } else recognitions = getRecognitions(className);
    endTime = SystemClock.elapsedRealtime();
    Timber.v("Timecost for postprocessing: %s", (endTime - startTime));
    return recognitions;
  }

  /**
   * Recognizes multiple objects in the given Bitmap using the model for (detect multiple object) blockly command.
   *
   * @param bitmap The input Bitmap for object recognition.
   * @param classNameFirst The first class name for start vehicle recognition.
   * @param classNameSecond The second class name for stop vehicle recognition.
   * @return An ArrayList of ArrayLists containing Recognition results for both classes.
   * @throws IllegalArgumentException if an error occurs during recognition.
   */
  public ArrayList<ArrayList<Recognition>> recognizeMultipleImage(final Bitmap bitmap, String classNameFirst, String classNameSecond)
          throws IllegalArgumentException {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    long startTime = SystemClock.elapsedRealtime();
    convertBitmapToByteBuffer(bitmap);
    long endTime = SystemClock.elapsedRealtime();
    Timber.v("Timecost to convertBitmapToByteBuffer: %s", (endTime - startTime));

    Trace.endSection(); // preprocessBitmap

    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    feedData();
    Trace.endSection();

    // Run the inference call.
    Trace.beginSection("runInference");
    startTime = SystemClock.elapsedRealtime();
    runInference();
    endTime = SystemClock.elapsedRealtime();
    Trace.endSection();
    Timber.v("Timecost to run model inference: %s", (endTime - startTime));

    Trace.endSection(); // "recognizeImage"

    startTime = SystemClock.elapsedRealtime();
    ArrayList<ArrayList<Recognition>> recognitions = getMultipleRecognitions(classNameFirst, classNameSecond);
    endTime = SystemClock.elapsedRealtime();
    Timber.v("Timecost for postprocessing: %s", (endTime - startTime));

    return recognitions;
  }

  protected float mNmsThresh = 0.25f;

  protected float getObjThresh() {
    return ObjectNavFragment.MINIMUM_CONFIDENCE_TF_OD_API;
  }

  // non maximum suppression
  protected ArrayList<Recognition> nms(ArrayList<Recognition> list) {
    ArrayList<Recognition> nmsList = new ArrayList<Recognition>();

    for (int k = 0; k < labels.size(); k++) {
      // 1. Find max confidence per class
      PriorityQueue<Recognition> pq =
          new PriorityQueue<Recognition>(
              50,
              new Comparator<Recognition>() {
                @Override
                public int compare(final Recognition lhs, final Recognition rhs) {
                  // Intentionally reversed to put high confidence at the head of the queue.
                  return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                }
              });

      for (int i = 0; i < list.size(); ++i) {
        if (list.get(i).getClassId() == k) {
          pq.add(list.get(i));
        }
      }

      // 2. Do non maximum suppression
      while (pq.size() > 0) {
        // insert detection with max confidence
        Recognition[] a = new Recognition[pq.size()];
        Recognition[] detections = pq.toArray(a);
        Recognition max = detections[0];
        nmsList.add(max);
        pq.clear();

        for (int j = 1; j < detections.length; j++) {
          Recognition detection = detections[j];
          RectF b = detection.getLocation();
          if (box_iou(max.getLocation(), b) < mNmsThresh) {
            pq.add(detection);
          }
        }
      }
    }
    return nmsList;
  }

  // multiple non maximum suppression
  protected ArrayList<ArrayList<Recognition>> multipleNMS(ArrayList<ArrayList<Recognition>> list) {
    ArrayList<ArrayList<Recognition>> multipleNMSList = new ArrayList<>();
    multipleNMSList.add(new ArrayList<>());
    multipleNMSList.add(new ArrayList<>());
    PriorityQueue<Recognition> pq =
            new PriorityQueue<Recognition> (
                    50,
                    new Comparator<Recognition>() {
                      @Override
                      public int compare(final Recognition lhs, final Recognition rhs) {
                        // Intentionally reversed to put high confidence at the head of the queue.
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                      }
                    });
    for (int k = 0; k < labels.size(); k++) {
      // 1. Find max confidence per class
      for (int i = 0; i < list.size(); ++i) {
        ArrayList<Recognition> row = list.get(i);
        for (int j = 0; j < row.size(); j++) {
          if (row.get(j).getClassId() == k) {
            pq.add(row.get(j));
          }
        }
      }
    }

    // 2. Do non maximum suppression
    while (pq.size() > 0) {
      // insert detection with max confidence
      Recognition[] a = new Recognition[pq.size()];
      Recognition[] detections = pq.toArray(a);
      Recognition startMax = detections[0];
      multipleNMSList.get(0).add(startMax);
      if(detections.length > 1) {
        Recognition stopMax = detections[1];
        multipleNMSList.get(1).add(stopMax);
      }
      pq.clear();

      for (int j = 1; j < detections.length; j++) {
        Recognition detection = detections[j];
        RectF b = detection.getLocation();
        if (box_iou(startMax.getLocation(), b) < mNmsThresh) {
          pq.add(detection);
        }
      }
    }
    return multipleNMSList;
  }

  protected float box_iou(RectF a, RectF b) {
    return box_intersection(a, b) / box_union(a, b);
  }

  protected float box_intersection(RectF a, RectF b) {
    float w =
        overlap((a.left + a.right) / 2, a.right - a.left, (b.left + b.right) / 2, b.right - b.left);
    float h =
        overlap((a.top + a.bottom) / 2, a.bottom - a.top, (b.top + b.bottom) / 2, b.bottom - b.top);
    if (w < 0 || h < 0) return 0;
    float area = w * h;
    return area;
  }

  protected float box_union(RectF a, RectF b) {
    float i = box_intersection(a, b);
    float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
    return u;
  }

  protected float overlap(float x1, float w1, float x2, float w2) {
    float l1 = x1 - w1 / 2;
    float l2 = x2 - w2 / 2;
    float left = l1 > l2 ? l1 : l2;
    float r1 = x1 + w1 / 2;
    float r2 = x2 + w2 / 2;
    float right = r1 < r2 ? r1 : r2;
    return right - left;
  }

  /**
   * Get the name of the label file stored in Assets.
   *
   * @return
   */
  protected abstract String getLabelPath();

  /**
   * Run inference using the prepared input in {@link #imgData}. Afterwards, the result will be
   * provided by getProbability().
   *
   * <p>This additional method is necessary, because we don't have a common base for different
   * primitive data types.
   */
  protected abstract void runInference();

  /**
   * Feeds the data
   *
   * <p>This additional method is necessary, because we can have different number of detections
   */
  protected abstract void feedData();

  /**
   * Get the total number of labels.
   *
   * @return
   */
  protected int getNumLabels() {
    return labels.size();
  }

  public List<String> getLabels() {
    List<String> list = new ArrayList<>();
    for (String label : labels) if (!label.equals("???")) list.add(label);
    return list;
  }
  /**
   * Get the number of detections.
   *
   * @return
   */
  protected abstract int getNumDetections();

  /** Get specs from tflite file */
  protected abstract void parseTflite();

  /**
   * Get the recognitions.
   *
   * @return
   */
  protected abstract List<Recognition> getRecognitions(String className);
  protected abstract ArrayList<ArrayList<Recognition>> getMultipleRecognitions(String classNameFirst, String classNameSecond);
  protected abstract List<Recognition> getAllRecognition();
}
