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

package org.openbot.app.robot.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import org.openbot.app.robot.env.BorderedText;
import org.openbot.app.robot.env.ImageUtils;
import org.openbot.app.robot.env.Logger;
import org.openbot.app.robot.tflite.Detector.Recognition;
import org.openbot.app.robot.vehicle.Control;

/** A tracker that handles non-max suppression and matches existing objects to new detections. */
public class MultiBoxTracker {
  private static final float TEXT_SIZE_DIP = 18;
  private static final float MIN_SIZE = 16.0f;
  private static final int[] COLORS = {
    Color.BLUE,
    Color.RED,
    Color.GREEN,
    Color.YELLOW,
    Color.CYAN,
    Color.MAGENTA,
    Color.WHITE,
    Color.parseColor("#55FF55"),
    Color.parseColor("#FFA500"),
    Color.parseColor("#FF8888"),
    Color.parseColor("#AAAAFF"),
    Color.parseColor("#FFFFAA"),
    Color.parseColor("#55AAAA"),
    Color.parseColor("#AA33AA"),
    Color.parseColor("#0D0068")
  };
  final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
  private final Logger logger = new Logger();
  private final Queue<Integer> availableColors = new LinkedList<Integer>();
  private final List<TrackedRecognition> trackedObjects = new LinkedList<TrackedRecognition>();
  private final Paint boxPaint = new Paint();
  private final float textSizePx;
  private final BorderedText borderedText;
  private Matrix frameToCanvasMatrix;
  private int frameWidth;
  private int frameHeight;
  private int sensorOrientation;
  private float leftControl;
  private float rightControl;
  private boolean useDynamicSpeed = false;

  public MultiBoxTracker(final Context context) {
    for (final int color : COLORS) {
      availableColors.add(color);
    }

    boxPaint.setColor(Color.RED);
    boxPaint.setStyle(Style.STROKE);
    boxPaint.setStrokeWidth(10.0f);
    boxPaint.setStrokeCap(Cap.ROUND);
    boxPaint.setStrokeJoin(Join.ROUND);
    boxPaint.setStrokeMiter(100);

    textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
  }

  public synchronized void setFrameConfiguration(
      final int width, final int height, final int sensorOrientation) {
    frameWidth = width;
    frameHeight = height;
    this.sensorOrientation = sensorOrientation;
  }

  public synchronized void drawDebug(final Canvas canvas) {
    final Paint textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setTextSize(60.0f);

    final Paint boxPaint = new Paint();
    boxPaint.setColor(Color.RED);
    boxPaint.setAlpha(200);
    boxPaint.setStyle(Style.STROKE);

    for (final Pair<Float, RectF> detection : screenRects) {
      final RectF rect = detection.second;
      canvas.drawRect(rect, boxPaint);
      canvas.drawText("" + detection.first, rect.left, rect.top, textPaint);
      borderedText.drawText(canvas, rect.centerX(), rect.centerY(), "" + detection.first);
    }
  }

  public synchronized void trackResults(final List<Recognition> results, final long timestamp) {
    logger.i("Processing %d results from %d", results.size(), timestamp);
    processResults(results);
  }

  private Matrix getFrameToCanvasMatrix() {
    return frameToCanvasMatrix;
  }

  private void updateFrameToCanvasMatrix(int canvasHeight, int canvasWidth) {
    final boolean rotated = sensorOrientation % 180 == 90;
    final float multiplier =
        Math.min(
            canvasHeight / (float) (rotated ? frameWidth : frameHeight),
            canvasWidth / (float) (rotated ? frameHeight : frameWidth));
    frameToCanvasMatrix =
        ImageUtils.getTransformationMatrix(
            frameWidth,
            frameHeight,
            (int) (multiplier * (rotated ? frameHeight : frameWidth)),
            (int) (multiplier * (rotated ? frameWidth : frameHeight)),
            sensorOrientation,
            new RectF(0, 0, 0, 0),
            false);
  }

  /**
   * Determine the robot controls/steering from the position of the tracked object/person on screen.
   * The follow speed is adjusted based on the area of the bounding box of the tracked object.
   * Assumption: large object box --> close to object --> slow down
   *
   * @return the adjusted speed control for left and right wheels in the range -1.0 ... 1.0
   */
  public synchronized Control updateTarget() {
    if (!trackedObjects.isEmpty()) {
      // Pick detection with highest probability
      final RectF trackedPos = new RectF(trackedObjects.get(0).location);
      final boolean rotated = sensorOrientation % 180 == 90;
      float imgWidth = (float) (rotated ? frameHeight : frameWidth);
      // calculate track box area for distance estimate
      float boxArea = trackedPos.height() * trackedPos.width();
      float centerX = (rotated ? trackedPos.centerY() : trackedPos.centerX());
      // Make sure object center is in frame
      centerX = Math.max(0.0f, Math.min(centerX, imgWidth));
      // Scale relative position along x-axis between -1 and 1
      float x_pos_norm = 1.0f - 2.0f * centerX / imgWidth;
      // Scale for steering signal and account for rotation,
      float x_pos_scaled = rotated ? -x_pos_norm * 1.0f : x_pos_norm * 1.0f;
      //// Scale by "exponential" function: y = x / sqrt(1-x^2)
      // Math.max (Math.min(x_pos_norm / Math.sqrt(1 - x_pos_norm * x_pos_norm),2),-2);

      if (x_pos_scaled < 0) {
        leftControl = 1.0f;
        rightControl = 1.0f + x_pos_scaled;
      } else {
        leftControl = 1.0f - x_pos_scaled;
        rightControl = 1.0f;
      }

      // adjust speed depending on size of detected object bounding box
      if (useDynamicSpeed) {
        float scaleFactor = 1.0f - boxArea / (frameWidth * frameHeight);
        scaleFactor = scaleFactor > 0.75f ? 1.0f : scaleFactor; // tracked object far, full speed
        // apply scale factor if tracked object is not too near, otherwise stop
        if (scaleFactor > 0.25f) {
          leftControl *= scaleFactor;
          rightControl *= scaleFactor;
        } else {
          leftControl = 0.0f;
          rightControl = 0.0f;
        }
      }

    } else {
      leftControl = 0.0f;
      rightControl = 0.0f;
    }

    return new Control(
        (0 > sensorOrientation) ? rightControl : leftControl,
        (0 > sensorOrientation) ? leftControl : rightControl);
  }

  public synchronized void draw(final Canvas canvas) {
    updateFrameToCanvasMatrix(canvas.getHeight(), canvas.getWidth());

    for (final TrackedRecognition recognition : trackedObjects) {
      final RectF trackedPos = new RectF(recognition.location);

      getFrameToCanvasMatrix().mapRect(trackedPos);
      boxPaint.setColor(recognition.color);

      float cornerSize = Math.min(trackedPos.width(), trackedPos.height()) / 8.0f;
      canvas.drawRoundRect(trackedPos, cornerSize, cornerSize, boxPaint);

      final String labelString =
          !TextUtils.isEmpty(recognition.title)
              ? String.format(
                  Locale.US, "%s %.2f", recognition.title, (100 * recognition.detectionConfidence))
              : String.format(Locale.US, "%.2f", 100 * recognition.detectionConfidence);
      borderedText.drawText(
          canvas, trackedPos.left + cornerSize, trackedPos.top, labelString + "%", boxPaint);

      //      if (recognition == trackedObjects.get(0)) {
      //        borderedText.drawText(
      //                canvas,
      //                trackedPos.left + cornerSize,
      //                trackedPos.top + 40.0f,
      //                String.format(Locale.US, "%.2f", leftControl) + "," + String.format("%.2f",
      // rightControl),
      //                boxPaint);
      //      }
    }
  }

  public void clearTrackedObjects() {
    trackedObjects.clear();
  }

  private void processResults(final List<Recognition> results) {
    final List<Pair<Float, Recognition>> rectsToTrack = new LinkedList<Pair<Float, Recognition>>();

    screenRects.clear();
    final Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());

    for (final Recognition result : results) {
      if (result.getLocation() == null) {
        continue;
      }
      final RectF detectionFrameRect = new RectF(result.getLocation());

      final RectF detectionScreenRect = new RectF();
      rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);

      logger.v(
          "Result! Frame: " + result.getLocation() + " mapped to screen:" + detectionScreenRect);

      screenRects.add(new Pair<Float, RectF>(result.getConfidence(), detectionScreenRect));

      if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {
        logger.w("Degenerate rectangle! " + detectionFrameRect);
        continue;
      }

      rectsToTrack.add(new Pair<Float, Recognition>(result.getConfidence(), result));
    }

    // Clear so objects don't stay if nothing detected.
    trackedObjects.clear();

    if (rectsToTrack.isEmpty()) {
      logger.v("Nothing to track, aborting.");
      return;
    }

    // trackedObjects.clear();
    for (final Pair<Float, Recognition> potential : rectsToTrack) {
      final TrackedRecognition trackedRecognition = new TrackedRecognition();
      trackedRecognition.detectionConfidence = potential.first;
      trackedRecognition.location = new RectF(potential.second.getLocation());
      trackedRecognition.title = potential.second.getTitle();
      trackedRecognition.color = COLORS[trackedObjects.size()];
      trackedObjects.add(trackedRecognition);

      if (trackedObjects.size() >= COLORS.length) {
        break;
      }
    }
  }

  /**
   * Set use of dynamic speed on or off (used in updateTarget())
   *
   * @param isEnabled
   */
  public void setDynamicSpeed(boolean isEnabled) {
    useDynamicSpeed = isEnabled;
  }

  private static class TrackedRecognition {
    RectF location;
    float detectionConfidence;
    int color;
    String title;
  }
}
