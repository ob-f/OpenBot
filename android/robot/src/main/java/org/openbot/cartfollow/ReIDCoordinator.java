package org.openbot.cartfollow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.SystemClock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.openbot.tflite.Detector.Recognition;

public class ReIDCoordinator {
  private static final int MAX_PENDING_GALLERY = 12;
  private static final int CONFIRMED_GALLERY_K = 8;
  private static final int MAX_CANDIDATES = 3;
  private static final int MAX_RECOVERY_CANDIDATES = 5;
  private static final int MAX_ADAPTIVE_GALLERY = 8;
  private static final int MAX_QUARANTINE_GALLERY = 4;
  private static final float ADAPTIVE_ANCHOR_FLOOR = 0.70f;
  private static final float ADMISSION_SCORE = 0.85f;
  private static final float ADMISSION_MARGIN = 0.08f;
  private static final float NOVELTY_MAX_SIMILARITY = 0.95f;
  private static final float PENDING_MATCH_SIMILARITY = 0.90f;
  private static final long ADAPTIVE_COOLDOWN_MS = 1000L;
  private static final float QUARANTINE_SCORE_MIN = 0.60f;
  private static final float QUARANTINE_SCORE_MAX = 0.85f;
  private static final int QUARANTINE_ADD_CONFIRMATIONS = 3;
  private static final int QUARANTINE_PROMOTE_CONFIRMATIONS = 5;
  private static final long QUARANTINE_PROMOTE_DWELL_MS = 1500L;
  private static final long FOLLOW_INTERVAL_MS = 200;
  private static final long HIGH_RISK_INTERVAL_MS = 300;

  private final ReIDFeatureExtractor extractor;
  private final String disabledReason;
  private final List<float[]> pendingGallery = new ArrayList<>();
  private final List<float[]> confirmedGallery = new ArrayList<>();
  private final List<float[]> adaptiveGallery = new ArrayList<>();
  private final List<float[]> quarantineGallery = new ArrayList<>();
  private final RecentGallery recentGallery = new RecentGallery();
  private final DeferredGallerySegment deferredGallery = new DeferredGallerySegment();
  private final Map<Long, Bitmap> deferredCrops = new HashMap<>();
  private final Map<Integer, ReIDMatchResult> globalScoredTracks = new HashMap<>();
  private DeferredGallerySegment.Result deferredResult;
  private long gallerySession;

  public synchronized long getSessionEpoch() {
    return gallerySession;
  }

  private int learningTrackId = -1;
  private boolean recentMatchingSupport;
  private boolean automaticVerification;
  private boolean learningFrameInterrupted;

  public void setAutomaticVerification(boolean verifying) {
    automaticVerification = verifying;
  }

  private GalleryCropGeometry previousLearningGeometry;
  private RectF previousLearningBox;
  private int previousLearningTrack = -1;
  private long previousLearningTime = -1L;
  private final Map<String, Bitmap> acceptedDeferredCrops = new HashMap<>();

  /** Owned crops transfer to the diagnostic caller; never allocated when logging is off. */
  public Map<String, Bitmap> consumeDeferredCrops() {
    Map<String, Bitmap> result = new HashMap<>(acceptedDeferredCrops);
    acceptedDeferredCrops.clear();
    return result;
  }

  public String getDeferredGalleryStatus() {
    return deferredResult == null
        ? "deferred_idle"
        : deferredResult.reason
            + " | endpoints="
            + deferredResult.strongEndpoints
            + "/3"
            + " | source_revision="
            + deferredResult.galleryRevision;
  }

  public boolean hasRecentMatchingSupport() {
    return recentMatchingSupport;
  }

  public ReIDMatchResult getGlobalScoredTrack(int id) {
    ReIDMatchResult result = globalScoredTracks.get(id);
    if (result == null
        || frameTimeMs < result.observationTimeMs
        || frameTimeMs - result.observationTimeMs > 500L) return null;
    return result.withBinding(
        id,
        result.observationTimeMs,
        result.frameSequence,
        result.frameSequence == frameSequence,
        result.bestCandidateIndex,
        false);
  }

  public synchronized java.util.List<ReIDMatchResult> getGlobalScores() {
    return new ArrayList<>(globalScoredTracks.values());
  }

  public synchronized java.util.List<String> provenanceManifest() {
    java.util.List<String> rows = new ArrayList<>();
    for (int i = 0; i < adaptiveGallery.size(); i++) {
      GallerySampleProvenance source = provenance.get(adaptiveGallery.get(i));
      try {
        org.json.JSONObject row =
            new org.json.JSONObject()
                .put("session", gallerySession)
                .put("revision", adaptiveRevision)
                .put("index", i)
                .put("kind", "adaptive")
                .put("globalEligible", source == null || source.globalEligible);
        if (source != null)
          row.put("track_id", source.trackId)
              .put("observation_id", source.observationId)
              .put("source_frame", source.frameSequence)
              .put("source_ms", source.timestampMs)
              .put("approved_after_frame", source.approvedAfterFrame)
              .put("approval", source.approvalReason)
              .put("quarantine_source", source.quarantinePromotion)
              .put("min_samples", 3)
              .put("min_span_ms", 600)
              .put("min_adjacent_similarity", .75);
        rows.add(row.toString());
      } catch (org.json.JSONException ignored) {
        /* Primitive finite metadata only. */
      }
    }
    return rows;
  }

  private List<float[]> globalAdaptiveSamples() {
    List<float[]> samples = new ArrayList<>();
    for (float[] sample : adaptiveGallery) {
      GallerySampleProvenance source = provenance.get(sample);
      if (source == null || (source.globalEligible && frameSequence > source.approvedAfterFrame))
        samples.add(sample);
    }
    return samples;
  }

  private final Map<Integer, ReIDMatchResult> scoredTracks = new HashMap<>();
  private boolean recentEnabled = true;
  private float recentScore;
  private String recentReason = "not_started";
  private int recentLockedTrack = -1;
  private boolean galleryImageLogging;
  private Bitmap recentCrop;

  public void setGalleryImageLogging(boolean enabled) {
    galleryImageLogging = enabled;
    if (!enabled) {
      clearDeferredCrops();
      for (Bitmap crop : acceptedDeferredCrops.values()) crop.recycle();
      acceptedDeferredCrops.clear();
    }
  }

  public Bitmap consumeRecentCrop() {
    Bitmap crop = recentCrop;
    recentCrop = null;
    return crop;
  }

  public void setRecentEnabled(boolean enabled) {
    recentEnabled = enabled;
    recentGallery.clear();
    recentScore = 0f;
  }

  public RecentGallery.Status getRecentStatus(long now) {
    int size = recentGallery.size(now);
    return new RecentGallery.Status(
        enhancedRecovery && recentEnabled && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE,
        size,
        size < 3 ? 0f : recentScore,
        recentReason);
  }

  public ReIDMatchResult getScoredTrack(int id) {
    ReIDMatchResult result = scoredTracks.get(id);
    if (result == null
        || frameTimeMs < result.observationTimeMs
        || frameTimeMs - result.observationTimeMs > 500L) return null;
    return result.frameSequence == frameSequence
        ? result
        : result.withBinding(
            id,
            result.observationTimeMs,
            result.frameSequence,
            false,
            result.bestCandidateIndex,
            result.localPoseSupport);
  }

  private final Map<float[], GallerySampleProvenance> provenance = new IdentityHashMap<>();
  private TargetTrackManager frameTracks;
  private long frameTimeMs;
  private long frameSequence;
  private long previousFrameTimeMs = -1L;
  private long evaluatedFrameSequence = -1L;
  private long lastMultiScanMs = -1L;
  private long lastGalleryFrameMs = -1L;
  private int lastBestTrackId = -1;
  private Bitmap lastBestCrop;
  private Bitmap promotedCrop;
  private Bitmap quarantineCrop;
  private Recognition quarantineRecognition;
  private Recognition promotedRecognition;
  private GallerySampleProvenance promotedProvenance;
  private GalleryCropGeometry cropGeometry = GalleryCropGeometry.evaluate(null, 0, 0, 0);

  /** Call after tracker update, once per accepted camera frame, before evaluate. */
  public void setFrameContext(TargetTrackManager tracks, long timeMs, long sequence) {
    learningFrameInterrupted =
        previousFrameTimeMs >= 0L
            && (timeMs - previousFrameTimeMs > 500L || timeMs < previousFrameTimeMs);
    if (enhancedRecovery
        && (tracks.getLockedTrackId() != recentLockedTrack
            || tracks.isLockedAssociationCompeting())) {
      learningTrackId = -1;
      clearQuarantine();
      recentGallery.clear();
      recentScore = 0f;
      recentLockedTrack = tracks.getLockedTrackId();
    }
    frameTracks = tracks;
    frameTimeMs = timeMs;
    frameSequence = sequence;
    if (previousFrameTimeMs >= 0L
        && (timeMs - previousFrameTimeMs > 500L || timeMs < previousFrameTimeMs)) {
      clearQuarantine();
      learningTrackId = -1;
      clearPendingAdaptive();
      recentGallery.clear();
      updateGalleryStatus("rejected", "frame_gap_over_500ms", 0f, 0f, 0f);
      lastResult = ReIDMatchResult.unavailable("frame_gap_over_500ms", getGallerySize());
    }
    previousFrameTimeMs = timeMs;
  }

  /** Ownership of the exact upright extraction crop transfers to the caller. */
  public Bitmap consumePromotedCrop() {
    Bitmap crop = promotedCrop;
    promotedCrop = null;
    return crop;
  }

  public Recognition getPromotedRecognition() {
    return promotedRecognition;
  }

  public GallerySampleProvenance getPromotedProvenance() {
    return promotedProvenance;
  }

  public GalleryCropGeometry getCropGeometry() {
    return cropGeometry;
  }

  /** A separate owned copy of the sample at isolation (third fresh confirmation). */
  public Bitmap consumeQuarantineCrop() {
    Bitmap crop = quarantineCrop;
    quarantineCrop = null;
    return crop;
  }

  public Recognition getQuarantineRecognition() {
    return quarantineRecognition;
  }

  private GalleryUpdateStatus.Mode galleryMode = GalleryUpdateStatus.Mode.STATIC;
  private boolean enhancedRecovery;
  private float[] lastBestFeature;
  private long lastAdaptiveEvaluatedRunMs;
  private long lastAdaptivePromotionMs;
  private float[] pendingAdaptiveFeature;
  private int pendingAdaptiveTrackId = -1;
  private int pendingAdaptiveConfirmations;
  private GalleryUpdateStatus galleryStatus =
      new GalleryUpdateStatus(
          GalleryUpdateStatus.Mode.STATIC, 0, 0, 0, 0L, 0f, 0f, 0f, "idle", "not_started");
  private long adaptiveRevision;
  private long reidObservationSequence;
  private float[] quarantineCandidateFeature;
  private int quarantineCandidateTrackId = -1;
  private int quarantineConfirmations;
  private long quarantineCandidateStartMs;

  private ReIDMatchResult lastResult = ReIDMatchResult.unavailable("not_started", 0);
  private Recognition lastBestCandidate = null;
  private BboxContinuityEvidence lastBboxEvidence =
      BboxContinuityEvidence.unavailable("not_started");
  private long lastRunTimeMs = 0L;
  private int stableMatchCount = 0;
  private int candidateSwitchCount = 0;
  private int lastBestIndex = -1;

  public ReIDCoordinator(Activity activity, int numThreads) {
    ReIDFeatureExtractor created = null;
    String reason = null;
    try {
      created =
          new TfliteReIDFeatureExtractor(
              activity, TfliteReIDFeatureExtractor.DEFAULT_ASSET_PATH, numThreads);
    } catch (IOException | IllegalArgumentException e) {
      reason = "reid_model_unavailable";
    }
    extractor = created;
    disabledReason = reason;
  }

  ReIDCoordinator(ReIDFeatureExtractor extractor) {
    this.extractor = extractor;
    this.disabledReason = extractor == null ? "reid_model_unavailable" : null;
  }

  public synchronized void reset() {
    gallerySession++;
    learningTrackId = -1;
    globalScoredTracks.clear();
    recentMatchingSupport = false;
    automaticVerification = false;
    deferredResult = null;
    previousLearningGeometry = null;
    previousLearningBox = null;
    previousLearningTrack = -1;
    previousLearningTime = -1;
    for (Bitmap crop : acceptedDeferredCrops.values()) crop.recycle();
    acceptedDeferredCrops.clear();
    if (recentCrop != null) recentCrop.recycle();
    recentCrop = null;
    recentGallery.clear();
    recentScore = 0f;
    recentReason = "session_reset";
    recentLockedTrack = -1;
    scoredTracks.clear();
    recycleLastBestCrop();
    if (promotedCrop != null) promotedCrop.recycle();
    promotedCrop = null;
    promotedRecognition = null;
    promotedProvenance = null;
    provenance.clear();
    previousFrameTimeMs = -1L;
    evaluatedFrameSequence = -1L;
    lastGalleryFrameMs = -1L;
    frameTracks = null;
    lastBestTrackId = -1;
    pendingGallery.clear();
    confirmedGallery.clear();
    adaptiveGallery.clear();
    quarantineGallery.clear();
    lastResult = ReIDMatchResult.unavailable(isAvailable() ? "reset" : disabledReason, 0);
    lastBestCandidate = null;
    lastBboxEvidence = BboxContinuityEvidence.unavailable("reset");
    lastRunTimeMs = 0L;
    lastMultiScanMs = -1L;
    stableMatchCount = 0;
    candidateSwitchCount = 0;
    lastBestIndex = -1;
    lastBestFeature = null;
    lastAdaptiveEvaluatedRunMs = 0L;
    lastAdaptivePromotionMs = 0L;
    adaptiveRevision = 0L;
    reidObservationSequence = 0L;
    clearPendingAdaptive();
    clearQuarantine();
    updateGalleryStatus("reset", "session_reset", 0f, 0f, 0f);
  }

  public boolean isAvailable() {
    return extractor != null;
  }

  public int getGallerySize() {
    return confirmedGallery.size() + adaptiveGallery.size();
  }

  public synchronized void setGalleryMode(GalleryUpdateStatus.Mode mode) {
    gallerySession++;
    globalScoredTracks.clear();
    scoredTracks.clear();
    recentGallery.clear();
    recentScore = 0f;
    galleryMode = mode == null ? GalleryUpdateStatus.Mode.STATIC : mode;
    adaptiveGallery.clear();
    provenance.clear();
    quarantineGallery.clear();
    clearPendingAdaptive();
    clearQuarantine();
    lastResult = ReIDMatchResult.unavailable("gallery_mode_changed", getGallerySize());
    updateGalleryStatus("mode", "mode_changed", 0f, 0f, 0f);
  }

  public GalleryUpdateStatus.Mode getGalleryMode() {
    return galleryMode;
  }

  public void setEnhancedRecovery(boolean enabled) {
    if (enhancedRecovery != enabled) {
      lastResult = ReIDMatchResult.unavailable("recovery_mode_changed", getGallerySize());
    }
    enhancedRecovery = enabled;
  }

  public GalleryUpdateStatus getGalleryStatus() {
    return galleryStatus;
  }

  public Recognition getLastBestCandidate() {
    return lastBestCandidate;
  }

  public BboxContinuityEvidence getLastBboxEvidence() {
    return lastBboxEvidence;
  }

  public ReIDMatchResult getLastResult() {
    return lastResult;
  }

  public long getLastRunTimeMs() {
    return lastRunTimeMs;
  }

  public void collectInitializationCandidate(
      Bitmap frame, Recognition candidate, int sensorOrientation) {
    collectInitializationCandidate(frame, candidate, sensorOrientation, -1L);
  }

  public void collectInitializationCandidate(
      Bitmap frame, Recognition candidate, int sensorOrientation, long receivedAtMs) {
    collectInitializationCandidate(
        frame, candidate, sensorOrientation, receivedAtMs, getSessionEpoch());
  }

  public void collectInitializationCandidate(
      Bitmap frame,
      Recognition candidate,
      int sensorOrientation,
      long receivedAtMs,
      long expectedSession) {
    if (!isAvailable() || frame == null || candidate == null || candidate.getLocation() == null) {
      return;
    }
    final long captureSession;
    synchronized (this) {
      captureSession = gallerySession;
      if (captureSession != expectedSession) return;
      if (pendingGallery.size() >= MAX_PENDING_GALLERY) return;
    }
    Bitmap crop = cropPerson(frame, candidate.getLocation(), 0.08f, sensorOrientation);
    float[] feature;
    try {
      feature = extractor.extract(crop);
    } finally {
      crop.recycle();
    }
    synchronized (this) {
      if (captureSession == gallerySession
          && feature != null
          && pendingGallery.size() < MAX_PENDING_GALLERY
          && (receivedAtMs < 0L || SystemClock.elapsedRealtime() - receivedAtMs <= 500L))
        pendingGallery.add(feature);
    }
  }

  public synchronized void confirmGallery() {
    gallerySession++;
    confirmedGallery.clear();
    if (pendingGallery.isEmpty()) return;
    for (float[] feature : selectDiverse(pendingGallery, CONFIRMED_GALLERY_K)) {
      confirmedGallery.add(feature);
    }
    adaptiveGallery.clear();
    provenance.clear();
    clearQuarantine();
    clearPendingAdaptive();
    updateGalleryStatus("confirmed", "anchor_confirmed", 0f, 0f, 0f);
    lastResult =
        ReIDMatchResult.unavailable(
            isAvailable() ? "gallery_confirmed" : disabledReason, getGallerySize());
  }

  public IdentityEvidence evaluate(
      List<Recognition> persons,
      Bitmap frame,
      TargetMemory memory,
      FollowState state,
      int frameW,
      int frameH,
      int sensorOrientation,
      float legacyScore,
      boolean legacyMatched,
      Recognition legacyBest) {
    return evaluate(
        persons,
        frame,
        memory,
        state,
        frameW,
        frameH,
        sensorOrientation,
        legacyScore,
        legacyMatched,
        legacyBest,
        getSessionEpoch());
  }

  public IdentityEvidence evaluate(
      List<Recognition> persons,
      Bitmap frame,
      TargetMemory memory,
      FollowState state,
      int frameW,
      int frameH,
      int sensorOrientation,
      float legacyScore,
      boolean legacyMatched,
      Recognition legacyBest,
      long expectedSession) {
    final long evaluationSession;
    evaluationSession = expectedSession;
    ReIDMatchResult result =
        maybeRunReID(
            persons,
            frame,
            memory,
            state,
            frameW,
            frameH,
            sensorOrientation,
            legacyBest,
            expectedSession);
    synchronized (this) {
      if (evaluationSession != gallerySession)
        return new IdentityEvidence(0f, 0f, false, "obsolete_reid_session");
      Recognition best = lastBestCandidate != null ? lastBestCandidate : legacyBest;
      boolean matched = legacyMatched;
      float confidence = legacyScore;
      if (result.reidAvailable && result.weakOk) {
        matched = true;
        confidence = result.bestScore;
      }
      boolean recentSupport =
          enhancedRecovery
              && frameTracks != null
              && persons.size() == 1
              && !frameTracks.isLockedAssociationCompeting()
              && lastBestTrackId == frameTracks.getLockedTrackId()
              && recentScore >= .75f
              && (lastBboxEvidence.bboxDefaultOk || lastBboxEvidence.predictionOk);
      recentMatchingSupport = recentSupport;
      if (recentSupport) {
        matched = true;
        confidence = Math.max(confidence, recentScore);
      }
      return new IdentityEvidence(
          matched ? confidence : 0f,
          confidence,
          matched,
          recentSupport ? "recent_local_support" : result.reason,
          result,
          lastBboxEvidence,
          stableMatchCount,
          candidateSwitchCount,
          best);
    }
  }

  private ReIDMatchResult maybeRunReID(
      List<Recognition> persons,
      Bitmap frame,
      TargetMemory memory,
      FollowState state,
      int frameW,
      int frameH,
      int sensorOrientation,
      Recognition legacyBest,
      long expectedSession) {
    final long runSession;
    final long now;
    final long start;
    final List<CandidateRef> refs;
    List<CandidateScore> scores = new ArrayList<>();
    synchronized (this) {
      runSession = gallerySession;
      if (runSession != expectedSession)
        return ReIDMatchResult.unavailable("obsolete_reid_session", 0);
      if (!isAvailable()) {
        lastResult = ReIDMatchResult.unavailable(disabledReason, 0);
        lastBestCandidate = legacyBest;
        lastBboxEvidence = bboxEvidence(legacyBest, memory, frameW, frameH);
        return lastResult;
      }
      if (confirmedGallery.isEmpty()) {
        lastResult = ReIDMatchResult.unavailable("gallery_empty", 0);
        lastBestCandidate = legacyBest;
        lastBboxEvidence = bboxEvidence(legacyBest, memory, frameW, frameH);
        return lastResult;
      }
      if (persons == null || persons.isEmpty() || frame == null) {
        clearQuarantine();
        clearPendingAdaptive();
        recycleLastBestCrop();
        updateGalleryStatus("rejected", "no_candidates", 0f, 0f, 0f);
        lastResult = ReIDMatchResult.unavailable("no_candidates", getGallerySize());
        lastBestCandidate = null;
        lastBboxEvidence = BboxContinuityEvidence.unavailable("no_candidates");
        return lastResult;
      }
      now = frameTracks != null ? frameTimeMs : SystemClock.elapsedRealtime();
      if (persons.size() != 1) {
        clearQuarantine();
        clearPendingAdaptive();
        updateGalleryStatus("rejected", "multi_person_frozen", 0f, 0f, 0f);
      }
      Recognition rebound = currentCachedCandidate(persons);
      if (rebound != null && lastResult.localPoseSupport) {
        BboxContinuityEvidence currentBbox = bboxEvidence(rebound, memory, frameW, frameH);
        if (frameTracks == null
            || lastBestTrackId != frameTracks.getLockedTrackId()
            || (!currentBbox.bboxDefaultOk && !currentBbox.predictionOk)) rebound = null;
      }
      boolean highRisk =
          state != FollowState.FOLLOW
              || persons.size() > 1
              || legacyBest == null
              || rebound != legacyBest
              || quarantineCandidateFeature != null
              || pendingAdaptiveFeature != null;
      long interval = highRisk ? HIGH_RISK_INTERVAL_MS : FOLLOW_INTERVAL_MS;
      if (enhancedRecovery) interval = Math.min(interval, FOLLOW_INTERVAL_MS);
      boolean multiScanDue =
          persons.size() > 1 && (lastMultiScanMs < 0 || now - lastMultiScanMs >= 500);
      if (!multiScanDue
          && ((now >= lastRunTimeMs && now - lastRunTimeMs < interval)
              || (frameTracks != null && evaluatedFrameSequence == frameSequence))
          && lastResult.reidAvailable
          && rebound != null) {
        lastBestCandidate = rebound;
        lastBboxEvidence = bboxEvidence(rebound, memory, frameW, frameH);
        lastResult =
            lastResult.withBinding(
                lastBestTrackId,
                lastResult.observationTimeMs,
                lastResult.frameSequence,
                false,
                persons.indexOf(rebound),
                lastResult.localPoseSupport);
        return lastResult;
      }

      recycleLastBestCrop();

      start = SystemClock.elapsedRealtime();
      scoredTracks.clear();
      globalScoredTracks.clear();
      int candidateLimit =
          enhancedRecovery
                  && (persons.size() > 1 || isRecoveryState(state) || automaticVerification)
              ? MAX_RECOVERY_CANDIDATES
              : MAX_CANDIDATES;
      if (enhancedRecovery
          && persons.size() > 1
          && !isRecoveryState(state)
          && !automaticVerification) {
        candidateLimit = multiScanDue ? MAX_RECOVERY_CANDIDATES : 1;
        if (multiScanDue) lastMultiScanMs = now;
      }
      Recognition priority = legacyBest;
      if (enhancedRecovery
          && frameTracks != null
          && frameTracks.getLockedTrack() != null
          && persons.contains(frameTracks.getLockedTrack().recognition))
        priority = frameTracks.getLockedTrack().recognition;
      refs = candidateRefs(persons, priority, candidateLimit);
    }
    for (CandidateRef ref : refs) {
      Bitmap crop = cropPerson(frame, ref.recognition.getLocation(), 0.08f, sensorOrientation);
      float[] feature = extractor.extract(crop);
      synchronized (this) {
        if (runSession != gallerySession) {
          crop.recycle();
          for (CandidateScore old : scores) old.crop.recycle();
          return ReIDMatchResult.unavailable("obsolete_reid_session", 0);
        }
        if (feature == null) {
          crop.recycle();
          continue;
        }
        float anchorScore = maxSimilarity(feature, confirmedGallery);
        float adaptiveScore =
            maxSimilarity(feature, enhancedRecovery ? globalAdaptiveSamples() : adaptiveGallery);
        BboxContinuityEvidence candidateBbox =
            bboxEvidence(ref.recognition, memory, frameW, frameH);
        int trackId = trackIdFor(ref.recognition);
        float eligibleAdaptive =
            enhancedRecovery || anchorScore >= ADAPTIVE_ANCHOR_FLOOR ? adaptiveScore : 0f;
        boolean localPose = false;
        if (enhancedRecovery
            && anchorScore >= QUARANTINE_SCORE_MIN
            && frameTracks != null
            && trackId >= 0
            && trackId == frameTracks.getLockedTrackId()
            && (candidateBbox.bboxDefaultOk || candidateBbox.predictionOk)) {
          for (float[] sample : adaptiveGallery) {
            GallerySampleProvenance source = provenance.get(sample);
            if (source != null
                && source.quarantinePromotion
                && source.trackId == trackId
                && frameSequence > source.approvedAfterFrame) {
              eligibleAdaptive = Math.max(eligibleAdaptive, dot(feature, sample));
            }
          }
          localPose = eligibleAdaptive > anchorScore;
        }
        float recentLocalScore =
            enhancedRecovery
                    && recentEnabled
                    && frameTracks != null
                    && persons.size() == 1
                    && !frameTracks.isLockedAssociationCompeting()
                    && trackId == frameTracks.getLockedTrackId()
                ? recentGallery.score(trackId, feature, now)
                : 0f;
        float score = Math.max(Math.max(anchorScore, eligibleAdaptive), recentLocalScore);
        scores.add(
            new CandidateScore(
                ref.index,
                ref.recognition,
                score,
                anchorScore,
                adaptiveScore,
                feature,
                crop,
                localPose));
      }
    }
    synchronized (this) {
      if (runSession != gallerySession) {
        for (CandidateScore old : scores) old.crop.recycle();
        return ReIDMatchResult.unavailable("obsolete_reid_session", 0);
      }
      if (scores.isEmpty()) {
        clearQuarantine();
        clearPendingAdaptive();
        updateGalleryStatus("rejected", "feature_extract_failed", 0f, 0f, 0f);
        lastResult = ReIDMatchResult.unavailable("feature_extract_failed", getGallerySize());
        lastBestCandidate = legacyBest;
        lastBboxEvidence = bboxEvidence(legacyBest, memory, frameW, frameH);
        return lastResult;
      }
      scores.sort((a, b) -> Float.compare(b.score, a.score));
      CandidateScore best = scores.get(0);
      // Track continuity owns candidate selection while the original track remains visible.
      if (enhancedRecovery && !automaticVerification && frameTracks != null) {
        for (CandidateScore candidate : scores)
          if (trackIdFor(candidate.recognition) == frameTracks.getLockedTrackId()) best = candidate;
      }
      for (CandidateScore candidate : scores) {
        if (candidate != best) candidate.crop.recycle();
      }
      lastBestCrop = best.crop;
      float second = 0f;
      for (CandidateScore candidate : scores)
        if (candidate != best) second = Math.max(second, candidate.score);
      int bestTrackId = trackIdFor(best.recognition);
      if (bestTrackId >= 0
          ? bestTrackId == lastBestTrackId
          : best.recognition == lastBestCandidate) {
        stableMatchCount++;
      } else {
        if (lastBestIndex >= 0) candidateSwitchCount++;
        stableMatchCount = 1;
        lastBestIndex = best.index;
      }
      lastBestCandidate = best.recognition;
      lastBestTrackId = bestTrackId;
      lastBestFeature = best.feature;
      lastBboxEvidence = bboxEvidence(best.recognition, memory, frameW, frameH);
      lastRunTimeMs = now;
      evaluatedFrameSequence = frameSequence;
      reidObservationSequence++;
      for (CandidateScore candidate : scores) {
        float competitor = 0f;
        for (CandidateScore other : scores)
          if (candidate != other) competitor = Math.max(competitor, other.score);
        int id = trackIdFor(candidate.recognition);
        scoredTracks.put(
            id,
            new ReIDMatchResult(
                    candidate.score,
                    competitor,
                    candidate.index,
                    getGallerySize(),
                    true,
                    SystemClock.elapsedRealtime() - start,
                    "fresh",
                    candidate.anchorScore,
                    candidate.adaptiveScore,
                    reidObservationSequence)
                .withBinding(id, now, frameSequence, true, candidate.index, candidate.localPose));
        float globalScore = Math.max(candidate.anchorScore, candidate.adaptiveScore);
        float globalCompetitor = 0f;
        for (CandidateScore other : scores)
          if (candidate != other)
            globalCompetitor =
                Math.max(globalCompetitor, Math.max(other.anchorScore, other.adaptiveScore));
        globalScoredTracks.put(
            id,
            new ReIDMatchResult(
                    globalScore,
                    globalCompetitor,
                    candidate.index,
                    getGallerySize(),
                    true,
                    SystemClock.elapsedRealtime() - start,
                    "fresh",
                    candidate.anchorScore,
                    candidate.adaptiveScore,
                    reidObservationSequence)
                .withBinding(id, now, frameSequence, true, candidate.index, false));
      }
      recentScore =
          enhancedRecovery && recentEnabled && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE
              ? recentGallery.score(bestTrackId, best.feature, now)
              : 0f;
      lastResult =
          new ReIDMatchResult(
                  best.score,
                  second,
                  best.index,
                  getGallerySize(),
                  true,
                  SystemClock.elapsedRealtime() - start,
                  "fresh",
                  best.anchorScore,
                  best.adaptiveScore,
                  reidObservationSequence)
              .withBinding(bestTrackId, now, frameSequence, true, best.index, best.localPose);
      return lastResult;
    }
  }

  private int trackIdFor(Recognition recognition) {
    TargetTrack track =
        frameTracks == null ? null : frameTracks.getTrackForRecognition(recognition);
    return track == null ? -1 : track.trackId;
  }

  private Recognition currentCachedCandidate(List<Recognition> persons) {
    if (frameTracks == null) return persons.contains(lastBestCandidate) ? lastBestCandidate : null;
    TargetTrack track = frameTracks.getTrackById(lastBestTrackId);
    return track != null && track.isVisible() && persons.contains(track.recognition)
        ? track.recognition
        : null;
  }

  private void recycleLastBestCrop() {
    if (lastBestCrop != null) lastBestCrop.recycle();
    lastBestCrop = null;
  }

  private BboxContinuityEvidence bboxEvidence(
      Recognition recognition, TargetMemory memory, int frameW, int frameH) {
    if (recognition == null || memory == null || recognition.getLocation() == null) {
      return BboxContinuityEvidence.unavailable("candidate_not_available");
    }
    TargetTrack observed =
        frameTracks == null ? null : frameTracks.getTrackForRecognition(recognition);
    if (enhancedRecovery
        && observed != null
        && observed.trackId == frameTracks.getLockedTrackId()
        && observed.previousBbox != null
        && observed.previousSeenTimestampMs >= 0
        && frameTimeMs > observed.previousSeenTimestampMs
        && frameTimeMs - observed.previousSeenTimestampMs <= 500L) {
      return BboxContinuityEvidence.from(
          recognition.getLocation(), observed.previousBbox, null, frameW, frameH);
    }
    return BboxContinuityEvidence.from(
        recognition.getLocation(), memory.getLastBbox(), memory.getPreviousBbox(), frameW, frameH);
  }

  private static List<CandidateRef> candidateRefs(
      List<Recognition> persons, Recognition legacyBest, int maxCandidates) {
    List<CandidateRef> refs = new ArrayList<>();
    if (legacyBest != null) {
      int idx = persons.indexOf(legacyBest);
      if (idx >= 0) refs.add(new CandidateRef(idx, legacyBest));
    }
    List<CandidateRef> all = new ArrayList<>();
    for (int i = 0; i < persons.size(); i++) {
      Recognition r = persons.get(i);
      if (r != null && r.getLocation() != null) all.add(new CandidateRef(i, r));
    }
    all.sort(
        Comparator.comparingDouble((CandidateRef ref) -> -area(ref.recognition.getLocation()))
            .thenComparingInt(ref -> ref.index));
    for (CandidateRef ref : all) {
      boolean exists = false;
      for (CandidateRef current : refs) {
        if (current.index == ref.index) {
          exists = true;
          break;
        }
      }
      if (!exists) refs.add(ref);
      if (refs.size() >= maxCandidates) break;
    }
    return refs;
  }

  public GalleryUpdateStatus freezeGallery(String reason) {
    learningTrackId = -1;
    recentGallery.clear();
    recentScore = 0f;
    recentReason = reason;
    clearQuarantine();
    clearPendingAdaptive();
    updateGalleryStatus(
        "rejected",
        reason,
        lastResult.anchorScore,
        lastResult.adaptiveScore,
        1f - Math.max(lastResult.anchorScore, lastResult.adaptiveScore));
    return galleryStatus;
  }

  /**
   * Shared entry for the simulator and integration tests: stopping is not always a sampling veto.
   */
  public synchronized GalleryUpdateStatus updateSimulatorGallery(
      Recognition target,
      FollowState state,
      BehaviorDecisionResult decision,
      IdentityEvidence identity,
      int personCount,
      int frameW,
      int frameH,
      int orientation,
      long now,
      SimulatorIdentityGuard.Decision permit,
      boolean stale) {
    return updateSimulatorGallery(
        target,
        state,
        decision,
        identity,
        personCount,
        frameW,
        frameH,
        orientation,
        now,
        permit,
        stale,
        permit != null && permit.samplingAllowed);
  }

  public synchronized GalleryUpdateStatus updateSimulatorGallery(
      Recognition target,
      FollowState state,
      BehaviorDecisionResult decision,
      IdentityEvidence identity,
      int personCount,
      int frameW,
      int frameH,
      int orientation,
      long now,
      SimulatorIdentityGuard.Decision permit,
      boolean stale,
      boolean continuous) {
    stale |= frameTracks != null && (now < frameTimeMs || now - frameTimeMs > 500L);
    cropGeometry =
        GalleryCropGeometry.evaluate(
            target == null ? null : target.getLocation(), frameW, frameH, orientation);
    boolean exiting = false;
    RectF learningBox =
        target == null || target.getLocation() == null || frameW <= 0 || frameH <= 0
            ? null
            : TargetObservationEvidence.toScreen(target.getLocation(), frameW, frameH, orientation);
    if (identity != null
        && previousLearningGeometry != null
        && previousLearningTrack == identity.trackId
        && now - previousLearningTime <= 500L) {
      float shift =
          learningBox == null || previousLearningBox == null
              ? 0f
              : learningBox.centerX() - previousLearningBox.centerX();
      exiting =
          ((cropGeometry.leftClipped && shift <= -.02f)
                  || (cropGeometry.rightClipped && shift >= .02f))
              && cropGeometry.visibleWidthPx < previousLearningGeometry.visibleWidthPx * .97f;
    }
    previousLearningBox = learningBox;
    previousLearningGeometry = cropGeometry;
    previousLearningTrack = identity == null ? -1 : identity.trackId;
    previousLearningTime = now;
    if (stale
        || personCount != 1
        || permit == null
        || !permit.samplingAllowed
        || exiting
        || learningFrameInterrupted)
      return freezeGallery(
          stale
              ? "frame_stale"
              : personCount != 1
                  ? "multi_person_frozen"
                  : exiting
                      ? "side_exit_learning_frozen"
                      : learningFrameInterrupted ? "frame_gap_over_500ms" : "identity_unavailable");
    if ((permit.authorized || permit.isContinuous()) && continuous)
      learningTrackId = permit.trackId;
    boolean deferredContext =
        galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE
            && continuous
            && identity != null
            && identity.trackId == learningTrackId
            && identity.trackId == identity.lockedTrackId
            && identity.missedFrames == 0
            && cropGeometry.quarantineAllowed;
    if (!deferredContext && deferredGallery.size(now) > 0) clearQuarantine();
    recentReason = "awaiting_strong_sample";
    ReIDMatchResult match = identity == null ? null : identity.reidMatch;
    boolean boundFresh =
        match != null
            && match.fresh
            && target == lastBestCandidate
            && lastBestFeature != null
            && match.isBoundToTrack(identity.trackId)
            && match.observationId == lastResult.observationId
            && match.frameSequence == frameSequence;
    if (deferredContext && boundFresh && recentEnabled) {
      recentGallery.append(
          permit.trackId, match.observationId, match.observationTimeMs, now, lastBestFeature);
    }
    boolean pending = deferredGallery.size(now) > 0;
    if (deferredContext) {
      if (boundFresh) {
        DeferredGallerySegment.Sample sample =
            new DeferredGallerySegment.Sample(
                match.observationId,
                match.frameSequence,
                match.observationTimeMs,
                identity.trackId,
                lastBestFeature,
                target.getLocation());
        deferredResult =
            deferredGallery.offer(
                gallerySession,
                adaptiveRevision,
                sample,
                true,
                true,
                match.rawNonQuarantineScore,
                match.secondScore,
                confirmedGallery,
                globalAdaptiveSamples(),
                now);
        if (galleryImageLogging
            && lastBestCrop != null
            && deferredResult.sampleCount > 0
            && !deferredCrops.containsKey(sample.observationId))
          deferredCrops.put(
              sample.observationId, lastBestCrop.copy(Bitmap.Config.ARGB_8888, false));
        // Crop retention is bounded by the same source-time window and entry count as features.
        if (deferredCrops.size() > 16) {
          Long oldest = java.util.Collections.min(deferredCrops.keySet());
          deferredCrops.remove(oldest).recycle();
        }
        if (!deferredResult.approvedSamples.isEmpty()) {
          int added = 0;
          for (DeferredGallerySegment.Sample approved : deferredResult.approvedSamples) {
            if (recentEnabled)
              recentGallery.appendRetrospective(
                  approved.trackId,
                  approved.observationId,
                  approved.timestampMs,
                  now,
                  approved.feature);
            float nearest =
                Math.max(
                    maxSimilarity(approved.feature, confirmedGallery),
                    maxSimilarity(approved.feature, adaptiveGallery));
            if (nearest < NOVELTY_MAX_SIMILARITY && insertDiverseAdaptive(approved.feature)) {
              provenance.put(
                  approved.feature,
                  new GallerySampleProvenance(
                      approved.trackId,
                      approved.observationId,
                      approved.frameSequence,
                      approved.timestampMs,
                      true,
                      true,
                      frameSequence,
                      "continuous_segment"));
              added++;
            }
            Bitmap crop = deferredCrops.remove(approved.observationId);
            if (crop != null)
              acceptedDeferredCrops.put(
                  "deferred_source_" + approved.frameSequence + "_approved_" + frameSequence, crop);
          }
          provenance.keySet().retainAll(adaptiveGallery);
          if (added > 0) {
            adaptiveRevision++;
            lastAdaptivePromotionMs = now;
          }
          clearDeferredCrops();
          recentReason = recentEnabled ? "recent_deferred_approved" : "recent_disabled";
          updateGalleryStatus(
              "deferred_approved",
              added > 0 ? "deferred_adaptive_added" : "deferred_redundant_recent_only",
              match.anchorScore,
              match.adaptiveScore,
              1f - match.rawNonQuarantineScore);
          return galleryStatus;
        }
        if (deferredResult.sampleCount == 0) clearDeferredCrops();
        updateGalleryStatus(
            "deferred_pending",
            deferredResult.reason,
            match.anchorScore,
            match.adaptiveScore,
            1f - match.rawNonQuarantineScore);
      }
      return galleryStatus;
    }
    if (!permit.samplingAllowed) return freezeGallery(permit.reason);
    // Low-score isolation has exactly one promotion path: independent future verification above.
    if (match != null && match.bestScore < .85f) {
      updateGalleryStatus(
          "rejected",
          !cropGeometry.quarantineAllowed
              ? cropGeometry.quarantineReason
              : "deferred_continuity_required",
          match.anchorScore,
          match.adaptiveScore,
          1f - match.rawNonQuarantineScore);
      return galleryStatus;
    }
    boolean normalPhase = state == FollowState.FOLLOW || state == FollowState.FOLLOW_CAUTION;
    if (recentEnabled
        && galleryMode == GalleryUpdateStatus.Mode.ADAPTIVE
        && normalPhase
        && (permit.authorized || permit.isContinuous())
        && match != null
        && match.fresh
        && cropGeometry.normalAllowed
        && target == lastBestCandidate
        && match.isBoundToTrack(permit.trackId)
        && match.observationId == lastResult.observationId
        && match.frameSequence == frameSequence) {
      recentReason =
          recentGallery.append(
                  permit.trackId,
                  match.observationId,
                  match.observationTimeMs,
                  now,
                  lastBestFeature)
              ? "recent_added"
              : "recent_sampling_interval";
      if (galleryImageLogging && "recent_added".equals(recentReason) && lastBestCrop != null) {
        if (recentCrop != null) recentCrop.recycle();
        recentCrop = lastBestCrop.copy(Bitmap.Config.ARGB_8888, false);
      }
    }
    return maybeUpdateAdaptiveGallery(
        target, state, decision, identity, personCount, frameW, frameH, orientation, now);
  }

  public synchronized GalleryUpdateStatus maybeUpdateAdaptiveGallery(
      Recognition target,
      FollowState state,
      BehaviorDecisionResult decision,
      IdentityEvidence identity,
      int personCount,
      int frameW,
      int frameH,
      int sensorOrientation,
      long nowMs) {
    cropGeometry =
        GalleryCropGeometry.evaluate(
            target == null ? null : target.getLocation(), frameW, frameH, sensorOrientation);
    boolean frameGap =
        lastGalleryFrameMs >= 0L
            && (nowMs - lastGalleryFrameMs > 500L || nowMs < lastGalleryFrameMs);
    lastGalleryFrameMs = nowMs;
    ReIDMatchResult reid = identity == null ? null : identity.reidMatch;
    float anchorScore = reid == null ? 0f : reid.anchorScore;
    float adaptiveScore = reid == null ? 0f : reid.adaptiveScore;
    float novelty = 1f - Math.max(anchorScore, adaptiveScore);
    long sourceTimeMs = frameTracks != null ? frameTimeMs : lastRunTimeMs;
    String qualityRejection = galleryQualityReason(target, frameW, frameH, true);
    String freeze =
        personCount != 1
            ? "multi_person_frozen"
            : nowMs - sourceTimeMs > 500L || nowMs < sourceTimeMs
                ? "frame_age_over_500ms"
                : frameGap
                    ? "frame_gap_over_500ms"
                    : quarantineCandidateTrackId >= 0
                            && identity != null
                            && quarantineCandidateTrackId != identity.trackId
                        ? "quarantine_track_switched"
                        : shouldClearQuarantine(state, identity, personCount)
                            ? "quarantine_context_invalid"
                            : qualityRejection;
    if (freeze != null) {
      clearQuarantine();
      clearPendingAdaptive();
      updateGalleryStatus(
          "rejected",
          galleryMode == GalleryUpdateStatus.Mode.STATIC ? "static_mode" : freeze,
          anchorScore,
          adaptiveScore,
          novelty);
      return galleryStatus;
    }
    if (reid == null
        || !reid.reidAvailable
        || !reid.fresh
        || reid.observationId != lastResult.observationId
        || (frameTracks != null
            && (!reid.isBoundToTrack(identity.trackId) || reid.frameSequence != frameSequence))
        || reid.observationId == lastAdaptiveEvaluatedRunMs) {
      return galleryStatus;
    }
    lastAdaptiveEvaluatedRunMs = reid.observationId;
    if (isQuarantineCandidate(
        target, state, identity, personCount, frameW, frameH, sensorOrientation, nowMs)) {
      clearPendingAdaptive();
      if (enhancedRecovery) {
        updateGalleryStatus(
            "rejected", "deferred_entry_required", anchorScore, adaptiveScore, novelty);
        return galleryStatus;
      }
      updateQuarantine(identity, anchorScore, adaptiveScore, novelty, nowMs);
      return galleryStatus;
    }
    clearQuarantine();
    String rejection =
        adaptiveRejectionReason(
            target,
            state,
            decision,
            identity,
            personCount,
            frameW,
            frameH,
            sensorOrientation,
            nowMs);
    if (rejection != null) {
      clearPendingAdaptive();
      updateGalleryStatus("rejected", rejection, anchorScore, adaptiveScore, novelty);
      return galleryStatus;
    }

    if (pendingAdaptiveFeature == null
        || pendingAdaptiveTrackId != identity.trackId
        || dot(pendingAdaptiveFeature, lastBestFeature) < PENDING_MATCH_SIMILARITY) {
      pendingAdaptiveFeature = lastBestFeature.clone();
      pendingAdaptiveTrackId = identity.trackId;
      pendingAdaptiveConfirmations = 1;
      updateGalleryStatus("pending", "pending_1_of_3", anchorScore, adaptiveScore, novelty);
      return galleryStatus;
    }

    pendingAdaptiveConfirmations++;
    if (pendingAdaptiveConfirmations < 3) {
      updateGalleryStatus(
          "pending",
          "pending_" + pendingAdaptiveConfirmations + "_of_3",
          anchorScore,
          adaptiveScore,
          novelty);
      return galleryStatus;
    }

    float[] promoted = lastBestFeature.clone();
    clearPendingAdaptive();
    if (!insertDiverseAdaptive(promoted)) {
      updateGalleryStatus("rejected", "no_diversity_gain", anchorScore, adaptiveScore, novelty);
      return galleryStatus;
    }
    lastAdaptivePromotionMs = nowMs;
    adaptiveRevision++;
    recordPromotion(promoted, identity.trackId, false);
    updateGalleryStatus("promoted", "adaptive_added", anchorScore, adaptiveScore, novelty);
    return galleryStatus;
  }

  private String adaptiveRejectionReason(
      Recognition target,
      FollowState state,
      BehaviorDecisionResult decision,
      IdentityEvidence identity,
      int personCount,
      int frameW,
      int frameH,
      int sensorOrientation,
      long nowMs) {
    if (galleryMode != GalleryUpdateStatus.Mode.ADAPTIVE) return "static_mode";
    if (lastBestFeature == null || target == null || target != lastBestCandidate)
      return "feature_mismatch";
    if (state != FollowState.FOLLOW && state != FollowState.FOLLOW_CAUTION)
      return "recovery_frozen";
    if (identity == null || identity.trackId < 0 || identity.trackId != identity.lockedTrackId) {
      return "not_locked_track";
    }
    if (identity.targetBelief < 0.85f || identity.beliefStableFrames < 5)
      return "belief_not_stable";
    if (identity.reidMatch == null
        || identity.reidMatch.bestScore < ADMISSION_SCORE
        || identity.reidMatch.margin < ADMISSION_MARGIN) return "reid_not_strong";
    if ((!identity.bboxDefaultOk() && !identity.predictionOk()) || identity.missedFrames != 0) {
      return "motion_gate_failed";
    }
    if (personCount != 1) return "multi_person_frozen";
    String qualityRejection = galleryQualityReason(target, frameW, frameH, false);
    if (qualityRejection != null) return qualityRejection;
    float nearest = Math.max(identity.reidMatch.anchorScore, identity.reidMatch.adaptiveScore);
    if (nearest >= NOVELTY_MAX_SIMILARITY) return "sample_redundant";
    if (nearest < ADMISSION_SCORE) return "sample_too_different";
    if (lastAdaptivePromotionMs > 0L && nowMs - lastAdaptivePromotionMs < ADAPTIVE_COOLDOWN_MS) {
      return "cooldown";
    }
    return null;
  }

  private boolean shouldClearQuarantine(
      FollowState state, IdentityEvidence identity, int personCount) {
    if (galleryMode != GalleryUpdateStatus.Mode.ADAPTIVE) return true;
    if (personCount != 1) return true;
    if (identity == null || identity.trackId < 0 || identity.trackId != identity.lockedTrackId) {
      return true;
    }
    if (identity.missedFrames > 1) return true;
    return state != FollowState.FOLLOW
        && state != FollowState.FOLLOW_CAUTION
        && state != FollowState.IDENTITY_UNCERTAIN;
  }

  private boolean isQuarantineCandidate(
      Recognition target,
      FollowState state,
      IdentityEvidence identity,
      int personCount,
      int frameW,
      int frameH,
      int sensorOrientation,
      long nowMs) {
    if (galleryMode != GalleryUpdateStatus.Mode.ADAPTIVE
        || lastBestFeature == null
        || target == null
        || target != lastBestCandidate
        || identity == null
        || identity.reidMatch == null) return false;
    if (state != FollowState.FOLLOW
        && state != FollowState.FOLLOW_CAUTION
        && state != FollowState.IDENTITY_UNCERTAIN) return false;
    if (personCount != 1
        || identity.trackId < 0
        || identity.trackId != identity.lockedTrackId
        || identity.missedFrames > 1) return false;
    if (!identity.bboxDefaultOk() && !identity.predictionOk()) return false;
    if (nowMs - lastRunTimeMs > 500L) return false;
    float score = identity.reidMatch.rawNonQuarantineScore;
    return score >= QUARANTINE_SCORE_MIN
        && score < QUARANTINE_SCORE_MAX
        && galleryQualityReason(target, frameW, frameH, true) == null;
  }

  private String galleryQualityReason(
      Recognition target, int frameW, int frameH, boolean quarantine) {
    if (!enhancedRecovery && target != null) {
      RectF box = target.getLocation();
      if (box.left <= 2f
          || box.top <= 2f
          || box.right >= frameW - 2f
          || box.bottom >= frameH - 2f) {
        return "legacy_edge_clipped";
      }
    }
    if (enhancedRecovery && quarantine) {
      return cropGeometry.quarantineAllowed ? null : cropGeometry.quarantineReason;
    }
    return cropGeometry.normalAllowed ? null : cropGeometry.normalReason;
  }

  private void updateQuarantine(
      IdentityEvidence identity,
      float anchorScore,
      float adaptiveScore,
      float novelty,
      long nowMs) {
    boolean sameCandidate =
        quarantineCandidateFeature != null
            && quarantineCandidateTrackId == identity.trackId
            && dot(quarantineCandidateFeature, lastBestFeature) >= PENDING_MATCH_SIMILARITY;
    if (!sameCandidate) {
      clearQuarantine();
      quarantineCandidateFeature = lastBestFeature.clone();
      quarantineCandidateTrackId = identity.trackId;
      quarantineConfirmations = 1;
      quarantineCandidateStartMs = nowMs;
      updateGalleryStatus(
          "quarantine_pending", "quarantine_1_of_5", anchorScore, adaptiveScore, novelty);
      return;
    }

    quarantineConfirmations++;
    quarantineCandidateFeature = lastBestFeature.clone();
    if (quarantineConfirmations == QUARANTINE_ADD_CONFIRMATIONS) {
      if (quarantineGallery.size() >= MAX_QUARANTINE_GALLERY) quarantineGallery.remove(0);
      quarantineGallery.add(quarantineCandidateFeature.clone());
      if (quarantineCrop != null) quarantineCrop.recycle();
      quarantineCrop =
          lastBestCrop == null ? null : lastBestCrop.copy(Bitmap.Config.ARGB_8888, false);
      quarantineRecognition = snapshot(lastBestCandidate);
    }
    if (quarantineConfirmations < QUARANTINE_PROMOTE_CONFIRMATIONS
        || nowMs - quarantineCandidateStartMs < QUARANTINE_PROMOTE_DWELL_MS) {
      updateGalleryStatus(
          "quarantine_pending",
          "quarantine_" + quarantineConfirmations + "_of_5",
          anchorScore,
          adaptiveScore,
          novelty);
      return;
    }

    float[] promoted = quarantineCandidateFeature.clone();
    if (insertDiverseAdaptive(promoted)) {
      adaptiveRevision++;
      lastAdaptivePromotionMs = nowMs;
      recordPromotion(promoted, identity.trackId, true);
      clearQuarantine();
      updateGalleryStatus(
          "quarantine_promoted", "quarantine_promoted", anchorScore, adaptiveScore, novelty);
    } else {
      clearQuarantine();
      updateGalleryStatus(
          "rejected", "quarantine_no_diversity_gain", anchorScore, adaptiveScore, novelty);
    }
  }

  private void recordPromotion(float[] feature, int trackId, boolean quarantine) {
    GallerySampleProvenance source =
        new GallerySampleProvenance(
            trackId,
            lastResult.observationId,
            lastResult.frameSequence,
            lastResult.observationTimeMs,
            quarantine);
    provenance.put(feature, source);
    provenance.keySet().retainAll(adaptiveGallery);
    if (promotedCrop != null) promotedCrop.recycle();
    promotedCrop = lastBestCrop;
    lastBestCrop = null;
    promotedRecognition = snapshot(lastBestCandidate);
    promotedProvenance = source;
  }

  private static Recognition snapshot(Recognition r) {
    return r == null
        ? null
        : new Recognition(
            r.getId(), r.getTitle(), r.getConfidence(), new RectF(r.getLocation()), r.getClassId());
  }

  private boolean insertDiverseAdaptive(float[] candidate) {
    List<float[]> pool = new ArrayList<>(adaptiveGallery);
    pool.add(candidate);
    if (pool.size() <= MAX_ADAPTIVE_GALLERY) {
      adaptiveGallery.add(candidate);
      return true;
    }
    List<float[]> selected = selectDiverseAgainstAnchors(pool, MAX_ADAPTIVE_GALLERY);
    if (!selected.contains(candidate)) return false;
    adaptiveGallery.clear();
    adaptiveGallery.addAll(selected);
    return true;
  }

  private List<float[]> selectDiverseAgainstAnchors(List<float[]> pool, int k) {
    List<float[]> selected = new ArrayList<>();
    while (selected.size() < k && selected.size() < pool.size()) {
      float bestDistance = -1f;
      float[] best = null;
      for (float[] candidate : pool) {
        if (selected.contains(candidate)) continue;
        float nearest = maxSimilarity(candidate, confirmedGallery);
        if (!selected.isEmpty()) nearest = Math.max(nearest, maxSimilarity(candidate, selected));
        float distance = 1f - nearest;
        if (distance > bestDistance) {
          bestDistance = distance;
          best = candidate;
        }
      }
      if (best == null) break;
      selected.add(best);
    }
    return selected;
  }

  private void clearPendingAdaptive() {
    pendingAdaptiveFeature = null;
    pendingAdaptiveTrackId = -1;
    pendingAdaptiveConfirmations = 0;
  }

  private void clearQuarantineCandidate() {
    quarantineCandidateFeature = null;
    quarantineCandidateTrackId = -1;
    quarantineConfirmations = 0;
    quarantineCandidateStartMs = 0L;
  }

  private void clearQuarantine() {
    deferredGallery.clear();
    deferredResult = null;
    clearDeferredCrops();
    if (quarantineCrop != null) quarantineCrop.recycle();
    quarantineCrop = null;
    quarantineRecognition = null;
    quarantineGallery.clear();
    clearQuarantineCandidate();
  }

  private void clearDeferredCrops() {
    for (Bitmap crop : deferredCrops.values()) crop.recycle();
    deferredCrops.clear();
  }

  private void updateGalleryStatus(
      String event, String reason, float anchorScore, float adaptiveScore, float novelty) {
    galleryStatus =
        new GalleryUpdateStatus(
            galleryMode,
            confirmedGallery.size(),
            adaptiveGallery.size(),
            enhancedRecovery ? deferredGallery.size(frameTimeMs) : quarantineGallery.size(),
            pendingAdaptiveConfirmations,
            enhancedRecovery && deferredResult != null
                ? deferredResult.strongEndpoints
                : quarantineConfirmations,
            adaptiveRevision,
            anchorScore,
            adaptiveScore,
            novelty,
            event,
            reason);
  }

  static boolean isRecoveryState(FollowState state) {
    return state == FollowState.IDENTITY_UNCERTAIN
        || state == FollowState.LOST
        || state == FollowState.SEARCH
        || state == FollowState.REACQUIRE_TARGET
        || state == FollowState.DIRECTED_REACQUIRE;
  }

  private static Bitmap cropPerson(
      Bitmap frame, RectF bbox, float paddingRatio, int sensorOrientation) {
    int fw = frame.getWidth();
    int fh = frame.getHeight();
    float padX = bbox.width() * paddingRatio;
    float padY = bbox.height() * paddingRatio;
    int left = clamp((int) (bbox.left - padX), 0, fw - 1);
    int top = clamp((int) (bbox.top - padY), 0, fh - 1);
    int right = clamp((int) (bbox.right + padX), 1, fw);
    int bottom = clamp((int) (bbox.bottom + padY), 1, fh);
    int width = Math.max(1, right - left);
    int height = Math.max(1, bottom - top);
    Bitmap rawCrop = Bitmap.createBitmap(frame, left, top, width, height);
    if (rawCrop == frame) rawCrop = frame.copy(Bitmap.Config.ARGB_8888, false);
    int rotation = ((sensorOrientation % 360) + 360) % 360;
    if (rotation == 0) return rawCrop;
    Matrix matrix = new Matrix();
    matrix.postRotate(rotation);
    Bitmap upright =
        Bitmap.createBitmap(rawCrop, 0, 0, rawCrop.getWidth(), rawCrop.getHeight(), matrix, true);
    if (upright != rawCrop) rawCrop.recycle();
    return upright;
  }

  private static List<float[]> selectDiverse(List<float[]> features, int k) {
    List<float[]> selected = new ArrayList<>();
    if (features.isEmpty()) return selected;
    selected.add(features.get(0));
    while (selected.size() < k && selected.size() < features.size()) {
      float bestDistance = -1f;
      float[] best = null;
      for (float[] candidate : features) {
        if (selected.contains(candidate)) continue;
        float nearestSimilarity = -1f;
        for (float[] existing : selected) {
          nearestSimilarity = Math.max(nearestSimilarity, dot(candidate, existing));
        }
        float distance = 1f - nearestSimilarity;
        if (distance > bestDistance) {
          bestDistance = distance;
          best = candidate;
        }
      }
      if (best == null) break;
      selected.add(best);
    }
    return selected;
  }

  private static float maxSimilarity(float[] feature, List<float[]> gallery) {
    float best = -1f;
    for (float[] g : gallery) best = Math.max(best, dot(feature, g));
    return Math.max(0f, best);
  }

  private static float dot(float[] a, float[] b) {
    int n = Math.min(a.length, b.length);
    float sum = 0f;
    for (int i = 0; i < n; i++) sum += a[i] * b[i];
    return sum;
  }

  private static float area(RectF b) {
    return Math.max(0f, b.width()) * Math.max(0f, b.height());
  }

  private static int clamp(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }

  private static class CandidateRef {
    final int index;
    final Recognition recognition;

    CandidateRef(int index, Recognition recognition) {
      this.index = index;
      this.recognition = recognition;
    }
  }

  private static class CandidateScore {
    final int index;
    final Recognition recognition;
    final float score;
    final float anchorScore;
    final float adaptiveScore;
    final float[] feature;
    final Bitmap crop;
    final boolean localPose;

    CandidateScore(
        int index,
        Recognition recognition,
        float score,
        float anchorScore,
        float adaptiveScore,
        float[] feature,
        Bitmap crop,
        boolean localPose) {
      this.index = index;
      this.recognition = recognition;
      this.score = score;
      this.anchorScore = anchorScore;
      this.adaptiveScore = adaptiveScore;
      this.feature = feature;
      this.crop = crop;
      this.localPose = localPose;
    }
  }
}
