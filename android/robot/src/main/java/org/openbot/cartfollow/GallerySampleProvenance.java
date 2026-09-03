package org.openbot.cartfollow;

/** Immutable source of a promoted adaptive sample; track IDs are session-local. */
public final class GallerySampleProvenance {
  public final int trackId;
  public final long observationId;
  public final long frameSequence;
  public final long timestampMs;
  public final boolean quarantinePromotion;
  public final boolean globalEligible;
  public final long approvedAfterFrame;
  public final String approvalReason;

  GallerySampleProvenance(
      int trackId,
      long observationId,
      long frameSequence,
      long timestampMs,
      boolean quarantinePromotion) {
    this(
        trackId,
        observationId,
        frameSequence,
        timestampMs,
        quarantinePromotion,
        !quarantinePromotion,
        frameSequence,
        quarantinePromotion ? "quarantine" : "strong_sample");
  }

  GallerySampleProvenance(
      int trackId,
      long observationId,
      long frameSequence,
      long timestampMs,
      boolean quarantinePromotion,
      boolean globalEligible,
      long approvedAfterFrame,
      String approvalReason) {
    this.globalEligible = globalEligible;
    this.approvedAfterFrame = approvedAfterFrame;
    this.approvalReason = approvalReason;
    this.trackId = trackId;
    this.observationId = observationId;
    this.frameSequence = frameSequence;
    this.timestampMs = timestampMs;
    this.quarantinePromotion = quarantinePromotion;
  }
}
