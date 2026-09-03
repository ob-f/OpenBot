package org.openbot.cartfollow;

import android.graphics.RectF;

/** Geometry of successive observations, not a substitute for identity confirmation. */
public final class SimulatorContinuityTracker {
  public static boolean hasHistoricalLocalSupport(
      boolean bbox, boolean prediction, int candidateId, int lockedId, boolean nearGhost) {
    // The current locked detection may already have refreshed ghost memory in tracker.update().
    return bbox || prediction || (candidateId >= 0 && candidateId != lockedId && nearGhost);
  }

  public static final class Evidence {
    public final boolean reliable;
    public final String reason;
    public final int stableFrames;
    public final BboxContinuityEvidence observedGeometry;
    public final RectF currentBox;

    Evidence(boolean reliable, String reason, int stableFrames) {
      this(reliable, reason, stableFrames, null);
    }

    Evidence(boolean reliable, String reason, int stableFrames, BboxContinuityEvidence geometry) {
      this(reliable, reason, stableFrames, geometry, null);
    }

    Evidence(
        boolean reliable,
        String reason,
        int stableFrames,
        BboxContinuityEvidence geometry,
        RectF box) {
      this.reliable = reliable;
      this.reason = reason;
      this.stableFrames = stableFrames;
      this.observedGeometry = geometry;
      this.currentBox = box == null ? null : new RectF(box);
    }
  }

  private RectF previous;
  private float velocityX, velocityY;
  private int imageWidth = 640, imageHeight = 480;
  private int track = -1;
  private int stable;
  private long time = -1;
  private long sequence = -1;
  private long session = -1;

  /** Samples observed geometry even while identity or distance prevents motion. */
  public Evidence observe(
      long session,
      int id,
      RectF box,
      long frame,
      long receivedAt,
      long now,
      int width,
      int height,
      boolean highCurrent,
      boolean competing) {
    if (session < this.session || (session == this.session && frame <= sequence))
      return new Evidence(false, "obsolete_observation", 0);
    if (this.session != session) {
      reset();
      this.session = session;
    }
    imageWidth = width;
    imageHeight = height;
    BboxContinuityEvidence geometry =
        previous != null && track == id && receivedAt > time && receivedAt - time <= 500
            ? BboxContinuityEvidence.from(box, previous, null, width, height)
            : null;
    Evidence result = update(id, box, frame, receivedAt, now, highCurrent, true, competing);
    return new Evidence(result.reliable, result.reason, result.stableFrames, geometry, box);
  }

  public void reset() {
    previous = null;
    velocityX = velocityY = 0;
    track = -1;
    stable = 0;
    time = sequence = -1;
  }

  public Evidence update(
      int id,
      RectF box,
      long frame,
      long receivedAt,
      long now,
      boolean highCurrent,
      boolean local,
      boolean competing) {
    if (!highCurrent
        || competing
        || !local
        || box == null
        || id < 0
        || box.width() <= 0
        || box.height() <= 0
        || frame <= sequence
        || receivedAt > now
        || now - receivedAt > 500) {
      stable = 0;
      if (competing || !local || (time >= 0 && receivedAt - time > 500)) reset();
      return new Evidence(false, competing ? "association_competing" : "current_target_missing", 0);
    }
    boolean adjacent =
        previous != null && track == id && receivedAt > time && receivedAt - time <= 500;
    boolean smooth = false;
    if (adjacent) {
      float elapsed = receivedAt - time;
      float diagonal = (float) Math.hypot(Math.max(1, imageWidth), Math.max(1, imageHeight));
      float shift =
          (float) Math.hypot(box.centerX() - previous.centerX(), box.centerY() - previous.centerY())
              / diagonal;
      float prediction =
          (float)
                  Math.hypot(
                      box.centerX() - previous.centerX() - velocityX * elapsed,
                      box.centerY() - previous.centerY() - velocityY * elapsed)
              / diagonal;
      smooth = shift <= .35f || prediction <= .18f;
      if (smooth) {
        velocityX = (box.centerX() - previous.centerX()) / elapsed;
        velocityY = (box.centerY() - previous.centerY()) / elapsed;
      }
    }
    stable = smooth ? Math.min(3, stable + 1) : 1;
    previous = new RectF(box);
    track = id;
    time = receivedAt;
    sequence = frame;
    return new Evidence(
        stable >= 3,
        stable >= 3
            ? "continuous_observations"
            : adjacent && !smooth ? "bbox_jump" : "continuity_warming",
        stable);
  }
}
