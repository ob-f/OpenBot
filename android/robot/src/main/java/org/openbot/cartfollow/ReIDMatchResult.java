package org.openbot.cartfollow;

public class ReIDMatchResult {
  public static final float BEST_WEAK = 0.75f;
  public static final float MARGIN_WEAK = 0.03f;
  public static final float BEST_MID = 0.80f;
  public static final float MARGIN_MID = 0.05f;
  public static final float BEST_STRONG = 0.85f;
  public static final float MARGIN_STRONG = 0.05f;

  public final float bestScore;
  public final float secondScore;
  public final float margin;
  public final float anchorScore;
  public final float adaptiveScore;
  public final int bestCandidateIndex;
  public final int gallerySize;
  public final boolean reidAvailable;
  public final boolean weakOk;
  public final boolean midOk;
  public final boolean strongOk;
  public final long latencyMs;
  public final long observationId;
  public final String reason;
  public final int candidateTrackId;
  public final long observationTimeMs;
  public final long frameSequence;
  public final boolean fresh;
  public final boolean localPoseSupport;
  public final float rawNonQuarantineScore;

  public boolean isBoundToTrack(int trackId) {
    return reidAvailable && trackId >= 0 && candidateTrackId == trackId;
  }

  public ReIDMatchResult forTrack(int trackId) {
    return isBoundToTrack(trackId) ? this : unavailable("candidate_track_mismatch", gallerySize);
  }

  ReIDMatchResult withBinding(
      int trackId, long timeMs, long sequence, boolean fresh, int index, boolean localPoseSupport) {
    return new ReIDMatchResult(
        bestScore,
        secondScore,
        index,
        gallerySize,
        reidAvailable,
        latencyMs,
        fresh ? reason : "cached",
        anchorScore,
        adaptiveScore,
        observationId,
        trackId,
        timeMs,
        sequence,
        fresh,
        localPoseSupport);
  }

  public ReIDMatchResult(
      float bestScore,
      float secondScore,
      int bestCandidateIndex,
      int gallerySize,
      boolean reidAvailable,
      long latencyMs,
      String reason) {
    this(
        bestScore,
        secondScore,
        bestCandidateIndex,
        gallerySize,
        reidAvailable,
        latencyMs,
        reason,
        bestScore,
        0f,
        0L);
  }

  public ReIDMatchResult(
      float bestScore,
      float secondScore,
      int bestCandidateIndex,
      int gallerySize,
      boolean reidAvailable,
      long latencyMs,
      String reason,
      float anchorScore,
      float adaptiveScore) {
    this(
        bestScore,
        secondScore,
        bestCandidateIndex,
        gallerySize,
        reidAvailable,
        latencyMs,
        reason,
        anchorScore,
        adaptiveScore,
        0L);
  }

  public ReIDMatchResult(
      float bestScore,
      float secondScore,
      int bestCandidateIndex,
      int gallerySize,
      boolean reidAvailable,
      long latencyMs,
      String reason,
      float anchorScore,
      float adaptiveScore,
      long observationId) {
    this(
        bestScore,
        secondScore,
        bestCandidateIndex,
        gallerySize,
        reidAvailable,
        latencyMs,
        reason,
        anchorScore,
        adaptiveScore,
        observationId,
        -1,
        0L,
        0L,
        "fresh".equals(reason),
        false);
  }

  private ReIDMatchResult(
      float bestScore,
      float secondScore,
      int bestCandidateIndex,
      int gallerySize,
      boolean reidAvailable,
      long latencyMs,
      String reason,
      float anchorScore,
      float adaptiveScore,
      long observationId,
      int candidateTrackId,
      long observationTimeMs,
      long frameSequence,
      boolean fresh,
      boolean localPoseSupport) {
    this.bestScore = bestScore;
    this.secondScore = secondScore;
    this.margin = bestScore - secondScore;
    this.anchorScore = anchorScore;
    this.adaptiveScore = adaptiveScore;
    this.bestCandidateIndex = bestCandidateIndex;
    this.gallerySize = gallerySize;
    this.reidAvailable = reidAvailable;
    this.weakOk = reidAvailable && bestScore >= BEST_WEAK && margin >= MARGIN_WEAK;
    this.midOk = reidAvailable && bestScore >= BEST_MID && margin >= MARGIN_MID;
    this.strongOk = reidAvailable && bestScore >= BEST_STRONG && margin >= MARGIN_STRONG;
    this.latencyMs = latencyMs;
    this.observationId = observationId;
    this.reason = reason;
    this.candidateTrackId = candidateTrackId;
    this.observationTimeMs = observationTimeMs;
    this.frameSequence = frameSequence;
    this.fresh = fresh;
    this.localPoseSupport = localPoseSupport;
    this.rawNonQuarantineScore = Math.max(anchorScore, adaptiveScore);
  }

  public static ReIDMatchResult unavailable(String reason, int gallerySize) {
    return new ReIDMatchResult(0f, 0f, -1, gallerySize, false, 0L, reason);
  }
}
