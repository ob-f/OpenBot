package org.openbot.cartfollow;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import org.openbot.tflite.Detector.Recognition;

public class TargetMemory {
  private static final int H_BINS = 8;
  private static final int S_BINS = 4;
  private static final int HIST_SIZE = H_BINS * S_BINS;
  private boolean boundedColorSampling;

  public void setBoundedColorSampling(boolean enabled) {
    boundedColorSampling = enabled;
  }

  private float[] colorHistogram(Bitmap bitmap, RectF rect) {
    return boundedColorSampling
        ? computeSampledHsvHist(bitmap, rect)
        : computeHsvHist(bitmap, rect);
  }

  private RectF confirmedBbox;
  private float confirmedArea;
  private float confirmedAspectRatio;
  private float[] upperColorHist;
  private float[] lowerColorHist;

  private float desiredHeightRatio;
  private float desiredAreaRatio;
  private float desiredBottomRatio;

  private RectF lastBbox;
  private RectF previousBbox;
  private float lastCenterX;
  private float lastCenterY;
  private float lastArea;
  private long lastSeenTimeMs;

  public void captureFromBitmap(Bitmap bitmap, RectF bbox) {
    captureFromBitmap(bitmap, bbox, 0, 0, 0);
  }

  public void captureFromBitmap(
      Bitmap bitmap, RectF bbox, int frameW, int frameH, int sensorOrientation) {
    confirmedBbox = new RectF(bbox);
    confirmedArea = bbox.width() * bbox.height();
    confirmedAspectRatio = bbox.width() / Math.max(1f, bbox.height());
    upperColorHist = colorHistogram(bitmap, upperHalf(bbox));
    lowerColorHist = colorHistogram(bitmap, lowerHalf(bbox));
    lastBbox = new RectF(bbox);
    previousBbox = null;
    lastCenterX = bbox.centerX();
    lastCenterY = bbox.centerY();
    lastArea = confirmedArea;
    lastSeenTimeMs = System.currentTimeMillis();
    computeDistanceSetpoint(bbox, frameW, frameH, sensorOrientation);
  }

  private void computeDistanceSetpoint(RectF bbox, int frameW, int frameH, int sensorOrientation) {
    if (frameW <= 0 || frameH <= 0 || bbox == null) {
      desiredHeightRatio = 0f;
      desiredAreaRatio = 0f;
      desiredBottomRatio = 0f;
      return;
    }
    boolean rotated = sensorOrientation % 180 == 90;
    float imgWidth = rotated ? frameH : frameW;
    float imgHeight = rotated ? frameW : frameH;
    if (imgWidth <= 0 || imgHeight <= 0) {
      desiredHeightRatio = 0f;
      desiredAreaRatio = 0f;
      desiredBottomRatio = 0f;
      return;
    }
    float boxHeight = rotated ? bbox.width() : bbox.height();
    float boxWidth = rotated ? bbox.height() : bbox.width();
    float boxBottom = rotated ? bbox.right : bbox.bottom;
    desiredHeightRatio = boxHeight / imgHeight;
    desiredAreaRatio = (boxWidth * boxHeight) / (imgWidth * imgHeight);
    desiredBottomRatio = boxBottom / imgHeight;
  }

  public boolean hasDistanceSetpoint() {
    return desiredHeightRatio > 0f && desiredAreaRatio > 0f;
  }

  public ImageSetpointDistanceEstimator.Setpoint getDistanceSetpoint() {
    if (!hasDistanceSetpoint()) return null;
    return new ImageSetpointDistanceEstimator.Setpoint(
        desiredHeightRatio, desiredAreaRatio, desiredBottomRatio);
  }

  public void updateDynamic(Recognition r) {
    if (r == null || r.getLocation() == null) return;
    RectF b = r.getLocation();
    if (lastBbox != null) previousBbox = new RectF(lastBbox);
    lastBbox = new RectF(b);
    lastCenterX = b.centerX();
    lastCenterY = b.centerY();
    lastArea = b.width() * b.height();
    lastSeenTimeMs = System.currentTimeMillis();
  }

  public void clear() {
    confirmedBbox = null;
    confirmedArea = 0f;
    confirmedAspectRatio = 0f;
    upperColorHist = null;
    lowerColorHist = null;
    desiredHeightRatio = 0f;
    desiredAreaRatio = 0f;
    desiredBottomRatio = 0f;
    lastBbox = null;
    previousBbox = null;
    lastCenterX = 0f;
    lastCenterY = 0f;
    lastArea = 0f;
    lastSeenTimeMs = 0L;
  }

  public boolean isEmpty() {
    return confirmedBbox == null;
  }

  public RectF getLastBbox() {
    return lastBbox != null
        ? new RectF(lastBbox)
        : (confirmedBbox != null ? new RectF(confirmedBbox) : null);
  }

  public RectF getPreviousBbox() {
    return previousBbox != null ? new RectF(previousBbox) : null;
  }

  public float getLastArea() {
    return lastArea > 0 ? lastArea : confirmedArea;
  }

  public float[] getUpperColorHist() {
    return upperColorHist;
  }

  public float[] getLowerColorHist() {
    return lowerColorHist;
  }

  public float getConfirmedArea() {
    return confirmedArea;
  }

  private static RectF upperHalf(RectF bbox) {
    return new RectF(bbox.left, bbox.top, bbox.right, bbox.top + bbox.height() / 2f);
  }

  private static RectF lowerHalf(RectF bbox) {
    return new RectF(bbox.left, bbox.top + bbox.height() / 2f, bbox.right, bbox.bottom);
  }

  public static float[] computeHsvHist(Bitmap bitmap, RectF rect) {
    float[] hist = new float[HIST_SIZE];
    if (bitmap == null || rect == null) return hist;
    int bw = bitmap.getWidth();
    int bh = bitmap.getHeight();
    int left = clamp((int) rect.left, 0, bw - 1);
    int top = clamp((int) rect.top, 0, bh - 1);
    int right = clamp((int) rect.right, 1, bw);
    int bottom = clamp((int) rect.bottom, 1, bh);
    int w = right - left;
    int h = bottom - top;
    if (w <= 0 || h <= 0) return hist;
    int[] pixels = new int[w * h];
    bitmap.getPixels(pixels, 0, w, left, top, w, h);
    float[] hsv = new float[3];
    int count = 0;
    for (int px : pixels) {
      Color.RGBToHSV(Color.red(px), Color.green(px), Color.blue(px), hsv);
      if (hsv[1] < 0.1f) continue;
      int hb = (int) (hsv[0] / 360f * H_BINS) % H_BINS;
      if (hb < 0) hb += H_BINS;
      int sb = Math.min(S_BINS - 1, (int) (hsv[1] * S_BINS));
      hist[hb * S_BINS + sb]++;
      count++;
    }
    if (count > 0) {
      for (int i = 0; i < HIST_SIZE; i++) hist[i] /= count;
    }
    return hist;
  }

  public static float histIntersection(float[] a, float[] b) {
    if (a == null || b == null || a.length == 0 || b.length == 0) return 0f;
    float sum = 0f;
    int n = Math.min(a.length, b.length);
    for (int i = 0; i < n; i++) sum += Math.min(a[i], b[i]);
    return sum;
  }

  public float colorScore(Bitmap bitmap, RectF bbox) {
    float[] upper = colorHistogram(bitmap, upperHalf(bbox));
    float[] lower = colorHistogram(bitmap, lowerHalf(bbox));
    float upperScore = histIntersection(upper, upperColorHist);
    float lowerScore = histIntersection(lower, lowerColorHist);
    return (upperScore + lowerScore) / 2f;
  }

  private static int clamp(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }

  static int colorSampleCount(int width, int height) {
    return Math.max(0, Math.min(64, width)) * Math.max(0, Math.min(64, height));
  }

  static float[] computeSampledHsvHist(Bitmap bitmap, RectF rect) {
    float[] hist = new float[HIST_SIZE];
    if (bitmap == null || rect == null) return hist;
    int left = clamp((int) rect.left, 0, bitmap.getWidth());
    int top = clamp((int) rect.top, 0, bitmap.getHeight());
    int width = clamp((int) rect.right, 0, bitmap.getWidth()) - left;
    int height = clamp((int) rect.bottom, 0, bitmap.getHeight()) - top;
    int nx = Math.min(64, width), ny = Math.min(64, height);
    if (nx <= 0 || ny <= 0) return hist;
    float[] hsv = new float[3];
    int count = 0;
    // Sample cell centers without allocating a full-resolution crop or pixel array.
    for (int y = 0; y < ny; y++) {
      for (int x = 0; x < nx; x++) {
        int px =
            bitmap.getPixel(
                left + (int) ((x + 0.5f) * width / nx), top + (int) ((y + 0.5f) * height / ny));
        Color.RGBToHSV(Color.red(px), Color.green(px), Color.blue(px), hsv);
        if (hsv[1] < 0.1f) continue;
        int hb = Math.min(H_BINS - 1, (int) (hsv[0] / 360f * H_BINS));
        int sb = Math.min(S_BINS - 1, (int) (hsv[1] * S_BINS));
        hist[hb * S_BINS + sb]++;
        count++;
      }
    }
    if (count > 0) for (int i = 0; i < hist.length; i++) hist[i] /= count;
    return hist;
  }
}
