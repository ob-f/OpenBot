package org.openbot.cartfollow;

import android.graphics.RectF;

/** Current measured position of the locked track, independent of permission to move. */
public final class TargetObservationEvidence {
  public final RectF screenBox;
  public final int trackId;
  public final long observedAtMs;
  public final float belief;
  public final boolean lowConfidence;
  public final boolean current;
  public final int personCount;
  public final String source;

  public TargetObservationEvidence(
      RectF screenBox,
      int trackId,
      long observedAtMs,
      float belief,
      boolean lowConfidence,
      boolean current,
      int personCount,
      String source) {
    this.screenBox = new RectF(screenBox);
    this.trackId = trackId;
    this.observedAtMs = observedAtMs;
    this.belief = belief;
    this.lowConfidence = lowConfidence;
    this.current = current;
    this.personCount = personCount;
    this.source = source;
  }

  public static RectF toScreen(RectF box, int width, int height, int orientation) {
    switch (((orientation % 360) + 360) % 360) {
      case 90:
        return new RectF(
            1f - box.bottom / height, box.left / width, 1f - box.top / height, box.right / width);
      case 180:
        return new RectF(
            1f - box.right / width,
            1f - box.bottom / height,
            1f - box.left / width,
            1f - box.top / height);
      case 270:
        return new RectF(
            box.top / height, 1f - box.right / width, box.bottom / height, 1f - box.left / width);
      default:
        return new RectF(
            box.left / width, box.top / height, box.right / width, box.bottom / height);
    }
  }
}
