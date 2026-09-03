package org.openbot.cartfollow;

/** Immutable diagnostic snapshot for the session-only adaptive ReID gallery. */
public final class GalleryUpdateStatus {
  public enum Mode {
    STATIC,
    ADAPTIVE
  }

  public final Mode mode;
  public final int anchorSize;
  public final int adaptiveSize;
  public final int quarantineSize;
  public final int pendingConfirmations;
  public final int quarantineConfirmations;
  public final long revision;
  public final float anchorScore;
  public final float adaptiveScore;
  public final float novelty;
  public final String event;
  public final String reason;

  public boolean isPromotion() {
    return "promoted".equals(event) || "quarantine_promoted".equals(event);
  }

  GalleryUpdateStatus(
      Mode mode,
      int anchorSize,
      int adaptiveSize,
      int pendingConfirmations,
      long revision,
      float anchorScore,
      float adaptiveScore,
      float novelty,
      String event,
      String reason) {
    this(
        mode,
        anchorSize,
        adaptiveSize,
        0,
        pendingConfirmations,
        0,
        revision,
        anchorScore,
        adaptiveScore,
        novelty,
        event,
        reason);
  }

  GalleryUpdateStatus(
      Mode mode,
      int anchorSize,
      int adaptiveSize,
      int quarantineSize,
      int pendingConfirmations,
      int quarantineConfirmations,
      long revision,
      float anchorScore,
      float adaptiveScore,
      float novelty,
      String event,
      String reason) {
    this.mode = mode;
    this.anchorSize = anchorSize;
    this.adaptiveSize = adaptiveSize;
    this.quarantineSize = quarantineSize;
    this.pendingConfirmations = pendingConfirmations;
    this.quarantineConfirmations = quarantineConfirmations;
    this.revision = revision;
    this.anchorScore = anchorScore;
    this.adaptiveScore = adaptiveScore;
    this.novelty = novelty;
    this.event = event;
    this.reason = reason;
  }
}
