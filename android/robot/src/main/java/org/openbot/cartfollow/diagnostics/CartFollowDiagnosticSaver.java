package org.openbot.cartfollow.diagnostics;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import org.openbot.cartfollow.BboxContinuityEvidence;
import org.openbot.cartfollow.BehaviorDecisionResult;
import org.openbot.cartfollow.DetectionTierEvidence;
import org.openbot.cartfollow.DirectedReacquireEvidence;
import org.openbot.cartfollow.FrameTimingEvidence;
import org.openbot.cartfollow.GalleryCropGeometry;
import org.openbot.cartfollow.GalleryUpdateStatus;
import org.openbot.cartfollow.IdentityEvidence;
import org.openbot.cartfollow.ReIDMatchResult;
import org.openbot.cartfollow.RealCartAutoDriveController;
import org.openbot.cartfollow.RecentGallery;
import org.openbot.cartfollow.SimulatorAutoDriveController;
import org.openbot.cartfollow.SimulatorIdentityGuard;
import org.openbot.cartfollow.SteeringEvidence;
import org.openbot.cartfollow.TargetObservationEvidence;
import org.openbot.tflite.Detector.Recognition;
import timber.log.Timber;

public class CartFollowDiagnosticSaver {

  public void saveFrameAsync(
      Bitmap frame,
      CartFollowDiagnosticSession session,
      CartFollowDiagnosticConfig config,
      long frameNum,
      int frameW,
      int frameH,
      int sensorOrientation,
      float fps,
      int numPersons,
      String followState,
      BehaviorDecisionResult decision,
      String commandText,
      IdentityEvidence identity,
      SteeringEvidence steering,
      GalleryUpdateStatus gallery,
      GalleryCropGeometry geometry,
      SimulatorAutoDriveController.Result simulatorDrive,
      DetectionTierEvidence detectionTier,
      DirectedReacquireEvidence directed,
      FrameTimingEvidence timing,
      TargetObservationEvidence observation,
      String distanceDiagnostic,
      SimulatorIdentityGuard.Decision identityPermit,
      RecentGallery.Status recent,
      String deferredReview,
      boolean recentMatchingSupport,
      RealCartAutoDriveController.Result realDrive,
      Recognition locked,
      Recognition suspected,
      Recognition bestReid,
      boolean saveCropsForThisFrame) {
    if (session == null) return;
    final long timestampMs = System.currentTimeMillis();
    final long elapsedMs = timestampMs - session.startedAtMs;
    final String action = decision == null ? "" : decision.selectedAction.name();
    final String actionReason = decision == null ? "" : safe(decision.actionReason);
    final String safetyBlock =
        decision == null || decision.safetyBlockReason == null
            ? ""
            : safe(decision.safetyBlockReason);
    final String safeCommand = safe(commandText);
    final IdentitySnapshot identitySnapshot = new IdentitySnapshot(identity);
    final RecognitionSnapshot lockedSnapshot = RecognitionSnapshot.from(locked);
    final RecognitionSnapshot suspectedSnapshot = RecognitionSnapshot.from(suspected);
    final RecognitionSnapshot bestSnapshot = RecognitionSnapshot.from(bestReid);
    Bitmap.Config bitmapConfig =
        frame == null || frame.getConfig() == null ? Bitmap.Config.ARGB_8888 : frame.getConfig();
    boolean wantsImage = frame != null && config.saveCrops && saveCropsForThisFrame;
    if (wantsImage && !session.io.imageCapacity()) session.io.droppedImages.incrementAndGet();
    final Bitmap frameCopy =
        wantsImage && session.io.imageCapacity() ? frame.copy(bitmapConfig, false) : null;
    String lockedPath =
        frameCopy == null || lockedSnapshot == null || lockedSnapshot.bbox == null
            ? ""
            : String.format(Locale.US, "crops/%06d_locked.jpg", frameNum);
    String suspectedPath =
        frameCopy == null || suspectedSnapshot == null || suspectedSnapshot.bbox == null
            ? ""
            : String.format(Locale.US, "crops/%06d_suspected.jpg", frameNum);
    String bestPath =
        frameCopy == null || bestSnapshot == null || bestSnapshot.bbox == null
            ? ""
            : String.format(Locale.US, "crops/%06d_best_reid.jpg", frameNum);
    if (frameCopy != null)
      session.io.image(
          () -> {
            try {
              saveCrop(
                  frameCopy,
                  lockedSnapshot,
                  session,
                  config,
                  frameNum,
                  "locked",
                  sensorOrientation);
              saveCrop(
                  frameCopy,
                  suspectedSnapshot,
                  session,
                  config,
                  frameNum,
                  "suspected",
                  sensorOrientation);
              saveCrop(
                  frameCopy,
                  bestSnapshot,
                  session,
                  config,
                  frameNum,
                  "best_reid",
                  sensorOrientation);
            } finally {
              frameCopy.recycle();
            }
          },
          frameCopy::recycle);
    session.io.submit(
        () -> {
          appendFrameLog(
              session,
              frameNum,
              timestampMs,
              elapsedMs,
              fps,
              numPersons,
              followState,
              action,
              actionReason,
              safetyBlock,
              safeCommand,
              steering,
              simulatorDrive,
              detectionTier,
              directed,
              timing,
              observation,
              distanceDiagnostic,
              identityPermit,
              recent,
              gallery,
              deferredReview,
              recentMatchingSupport,
              realDrive);
          appendIdentityLog(
              session,
              frameNum,
              timestampMs,
              identitySnapshot,
              gallery,
              geometry,
              lockedPath,
              suspectedPath,
              bestPath);
        });
  }

  public void saveEventAsync(
      CartFollowDiagnosticSession session, long frameNum, String eventType, String note) {
    if (session == null) return;
    final long timestampMs = System.currentTimeMillis();
    final String safeType = safe(eventType);
    final String safeNote = safe(note);
    session.io.submit(
        () -> {
          String row =
              String.format(
                  Locale.US,
                  "%s,%d,%d,%s,%s\n",
                  csv(session.sessionId),
                  timestampMs,
                  frameNum,
                  csv(safeType),
                  csv(safeNote));
          session.io.append(session.eventsCsv, row);
          session.eventRows++;
        });
  }

  public void saveGallerySnapshotAsync(
      Bitmap bitmap, CartFollowDiagnosticSession session, String label) {
    if (bitmap == null
        || session == null
        || !session.galleryImageDue(android.os.SystemClock.elapsedRealtime())) return;
    saveImage(bitmap, session, session.galleryDir, sanitize(label) + ".jpg", 0);
  }

  public void saveSceneAsync(
      Bitmap bitmap, CartFollowDiagnosticSession session, long frame, int orientation) {
    if (bitmap != null && session != null)
      saveImage(
          bitmap,
          session,
          session.overlaysDir,
          String.format(Locale.US, "%06d_scene.jpg", frame),
          orientation);
  }

  private void saveImage(
      Bitmap bitmap, CartFollowDiagnosticSession session, File dir, String name, int orientation) {
    if (!session.io.imageCapacity()) {
      session.io.droppedImages.incrementAndGet();
      return;
    }
    Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
    session.io.image(
        () -> {
          Bitmap upright = copy;
          try {
            if (orientation % 360 != 0) {
              Matrix matrix = new Matrix();
              matrix.postRotate(orientation);
              upright =
                  Bitmap.createBitmap(copy, 0, 0, copy.getWidth(), copy.getHeight(), matrix, true);
            }
            try (FileOutputStream out = new FileOutputStream(new File(dir, name))) {
              if (!upright.compress(Bitmap.CompressFormat.JPEG, 90, out))
                throw new IOException("JPEG encoding failed");
              session.galleryCount++;
            }
          } catch (IOException e) {
            session.io.fail(e);
          } finally {
            if (upright != copy) upright.recycle();
            copy.recycle();
          }
        },
        copy::recycle);
  }

  public void shutdown() {
    /* Each session owns and asynchronously closes its bounded queues. */
  }

  private void appendFrameLog(
      CartFollowDiagnosticSession session,
      long frameNum,
      long timestampMs,
      long elapsedMs,
      float fps,
      int numPersons,
      String followState,
      String action,
      String actionReason,
      String safetyBlock,
      String commandText,
      SteeringEvidence steering,
      SimulatorAutoDriveController.Result simulatorDrive,
      DetectionTierEvidence detectionTier,
      DirectedReacquireEvidence directed,
      FrameTimingEvidence timing,
      TargetObservationEvidence observation,
      String distanceDiagnostic,
      SimulatorIdentityGuard.Decision identityPermit,
      RecentGallery.Status recent,
      GalleryUpdateStatus gallery,
      String deferredReview,
      boolean recentMatchingSupport,
      RealCartAutoDriveController.Result realDrive) {
    SteeringEvidence snapshot =
        steering == null ? SteeringEvidence.unavailable("not_collected", 0) : steering;
    String row =
        String.format(
            Locale.US,
            "%s,%d,%d,%d,%.2f,%d,%s,%s,%s,%s,%s,%s,%s,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%s,%s,%d,%s,%d,%d,%d,%s,%.4f,%.4f,%d,%d,%s,%s,%s,%d,%d,%d,%.2f,%.2f,%d,%d,%s,%s,%s\n",
            csv(session.sessionId),
            frameNum,
            timestampMs,
            elapsedMs,
            fps,
            numPersons,
            csv(followState),
            csv(action),
            csv(actionReason),
            csv(safetyBlock),
            csv(commandText),
            snapshot.valid ? "1" : "0",
            csv(snapshot.reason),
            snapshot.rawError,
            snapshot.filteredError,
            snapshot.lateralRatePerSec,
            snapshot.predictedError,
            snapshot.edgeUrgency,
            snapshot.demandPercent,
            csv(snapshot.direction.name()),
            csv(snapshot.level.name()),
            snapshot.predictionHorizonMs,
            csv(simulatorDrive == null ? "" : simulatorDrive.phase.name()),
            simulatorDrive == null ? 0 : simulatorDrive.gear,
            simulatorDrive == null ? 0 : simulatorDrive.left,
            simulatorDrive == null ? 0 : simulatorDrive.right,
            csv(simulatorDrive == null ? "" : simulatorDrive.reason),
            detectionTier == null ? 0f : detectionTier.highThreshold,
            detectionTier == null ? 0f : detectionTier.lowThreshold,
            detectionTier == null ? 0 : detectionTier.lowConfidencePersons.size(),
            detectionTier == null ? 0 : detectionTier.continuedLowConfidencePersons.size(),
            detectionTier != null && detectionTier.selectedCandidateIsLowConfidence ? "1" : "0",
            csv(directed == null ? "" : directed.phase.name()),
            csv(directed == null ? "" : directed.direction.name()),
            directed == null ? 0 : directed.speed,
            directed == null ? 0 : directed.left(),
            directed == null ? 0 : directed.right(),
            directed == null ? 0f : directed.turnedDegrees,
            directed == null ? 0f : directed.targetDegrees,
            directed == null ? 0L : directed.elapsedMs,
            directed == null ? 0L : directed.timeoutMs,
            directed != null && directed.gyroAvailable ? "1" : "0",
            directed != null && directed.wrongDirection ? "1" : "0",
            csv(directed == null ? "" : directed.reason));
    row =
        row.substring(0, row.length() - 1)
            + timingColumns(timing, observation, distanceDiagnostic)
            + continuityColumns(identityPermit, recent)
            + String.format(
                Locale.US,
                ",%d,%d,%d,%s,%s,%s,%d,%d",
                gallery == null ? 0 : gallery.anchorSize,
                gallery == null ? 0 : gallery.adaptiveSize,
                gallery == null ? 0 : gallery.quarantineSize,
                csv(deferredReview),
                recentMatchingSupport ? "1" : "0",
                csv(identityPermit == null ? "" : identityPermit.recoveryType.name()),
                identityPermit == null ? 0 : identityPermit.freshMatches,
                identityPermit == null ? 0 : identityPermit.requiredFreshMatches)
            + String.format(
                Locale.US,
                ",%s,%s,%d,%d,%d,%s",
                csv(realDrive == null ? "" : realDrive.intent.name()),
                csv(realDrive == null ? "" : realDrive.phase.name()),
                realDrive == null ? 0 : realDrive.gear,
                realDrive == null ? 0 : realDrive.left,
                realDrive == null ? 0 : realDrive.right,
                csv(realDrive == null ? "" : realDrive.reason))
            + String.format(
                Locale.US,
                ",%d,%d",
                identityPermit == null ? -1L : identityPermit.identityEvidenceTimeMs,
                identityPermit == null ? -1L : identityPermit.identityObservationId)
            + trackingColumns(identityPermit == null ? null : identityPermit.tracking)
            + "\n";
    session.io.append(session.frameLogCsv, row);
    session.frameRows++;
  }

  static String trackingColumns(org.openbot.cartfollow.TrackingDecision tracking) {
    return tracking == null
        ? ",-1,-1,-1,,0,0,"
        : String.format(
            Locale.US,
            ",%d,%d,%d,%s,%d,%d,%s",
            tracking.sessionId,
            tracking.frameSequence,
            tracking.observedAtMs,
            tracking.detectionLevel.name(),
            tracking.stableFrames,
            tracking.maximumGear,
            csv(tracking.reason));
  }

  static String continuityColumns(
      SimulatorIdentityGuard.Decision permit, RecentGallery.Status recent) {
    return String.format(
        Locale.US,
        ",%s,%s,%s,%s,%d,%s,%s,%s,%d,%.4f,%s",
        csv(permit == null ? "" : permit.state.name()),
        permit != null && permit.retainTarget ? "1" : "0",
        permit != null && permit.motionAllowed ? "1" : "0",
        permit != null && permit.samplingAllowed ? "1" : "0",
        permit == null ? 0 : permit.holdRemainingMs,
        csv(permit == null ? "" : permit.continuityReason),
        csv(permit == null ? "" : permit.reason),
        recent != null && recent.enabled ? "1" : "0",
        recent == null ? 0 : recent.size,
        recent == null ? 0f : recent.score,
        csv(recent == null ? "" : recent.reason));
  }

  static String timingColumns(
      FrameTimingEvidence timing,
      TargetObservationEvidence observation,
      String distanceDiagnostic) {
    RectF box = observation == null ? null : observation.screenBox;
    return String.format(
        Locale.US,
        ",%d,%d,%d,%d,%d,%d,%d,%s,%d,%.4f,%.4f,%.4f,%.4f,%s,%d,%d,%d,%d",
        timing == null ? 0L : timing.receivedAtMs,
        timing == null ? 0L : timing.sensorTimestampNs,
        timing == null ? 0L : timing.detectorMs,
        timing == null ? 0L : timing.reidMs,
        timing == null ? 0L : timing.pipelineMs,
        timing == null ? 0L : timing.sourceAgeMs,
        timing == null ? 0L : timing.droppedFrames,
        csv(observation == null ? "unavailable" : observation.source),
        observation == null ? -1 : observation.trackId,
        box == null ? 0f : box.left,
        box == null ? 0f : box.top,
        box == null ? 0f : box.right,
        box == null ? 0f : box.bottom,
        csv(distanceDiagnostic),
        timing == null ? 0 : timing.copyMs,
        timing == null ? 0 : timing.matchMs,
        timing == null ? 0 : timing.initializationMs,
        timing == null ? 0 : timing.decisionMs);
  }

  private void appendIdentityLog(
      CartFollowDiagnosticSession session,
      long frameNum,
      long timestampMs,
      IdentitySnapshot id,
      GalleryUpdateStatus gallery,
      GalleryCropGeometry geometry,
      String lockedPath,
      String suspectedPath,
      String bestPath) {
    String row =
        String.format(
            Locale.US,
            "%s,%d,%d,%d,%d,%d,%d,%d,%d,%.4f,%.4f,%.4f,%d,%s,%s,%s,%s,%s,%s,%s,%.4f,%d,%d,%d,%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%.4f,%.4f,%.4f,%s,%s\n",
            csv(session.sessionId),
            frameNum,
            timestampMs,
            id.trackId,
            id.lockedTrackId,
            id.suspectedTrackId,
            id.activeTrackCount,
            id.trackAge,
            id.missedFrames,
            id.bestScore,
            id.secondScore,
            id.margin,
            id.gallerySize,
            id.weakOk ? "1" : "0",
            id.midOk ? "1" : "0",
            id.strongOk ? "1" : "0",
            id.bboxLooseAdmissionOk ? "1" : "0",
            id.bboxDefaultOk ? "1" : "0",
            id.bboxStrictOk ? "1" : "0",
            id.predictionOk ? "1" : "0",
            id.targetBelief,
            id.beliefStableFrames,
            id.beliefUncertainFrames,
            id.candidateSwitchCount,
            csv(id.beliefReason),
            csv(id.reidReason),
            csv(lockedPath),
            csv(suspectedPath),
            csv(bestPath),
            csv(gallery == null ? "" : gallery.mode.name()),
            gallery == null ? 0 : gallery.anchorSize,
            gallery == null ? 0 : gallery.adaptiveSize,
            gallery == null ? 0 : gallery.quarantineSize,
            gallery == null ? 0 : gallery.pendingConfirmations,
            gallery == null ? 0 : gallery.quarantineConfirmations,
            gallery == null ? 0L : gallery.revision,
            gallery == null ? 0f : gallery.anchorScore,
            gallery == null ? 0f : gallery.adaptiveScore,
            gallery == null ? 0f : gallery.novelty,
            csv(gallery == null ? "" : gallery.event),
            csv(gallery == null ? "" : gallery.reason));
    row =
        row.substring(0, row.length() - 1)
            + String.format(
                Locale.US,
                ",%d,%d,%d,%d,%s,%.1f,%.1f,%.4f,%s,%s\n",
                id.observationId,
                id.observationTimeMs,
                id.observationFrame,
                id.scoredTrack,
                id.fresh ? "1" : "0",
                geometry == null ? 0f : geometry.visibleWidthPx,
                geometry == null ? 0f : geometry.visibleHeightPx,
                geometry == null ? 0f : geometry.heightRatio,
                csv(geometry == null ? "" : geometry.normalReason),
                csv(geometry == null ? "" : geometry.quarantineReason));
    session.io.append(session.identityLogCsv, row);
    session.identityRows++;
  }

  private String saveCrop(
      Bitmap frame,
      RecognitionSnapshot snapshot,
      CartFollowDiagnosticSession session,
      CartFollowDiagnosticConfig config,
      long frameNum,
      String role,
      int sensorOrientation) {
    if (snapshot == null || snapshot.bbox == null) return "";
    RectF bbox = snapshot.bbox;
    float padX = bbox.width() * config.paddingRatio;
    float padY = bbox.height() * config.paddingRatio;
    int left = clamp((int) (bbox.left - padX), 0, frame.getWidth() - 1);
    int top = clamp((int) (bbox.top - padY), 0, frame.getHeight() - 1);
    int right = clamp((int) (bbox.right + padX), left + 1, frame.getWidth());
    int bottom = clamp((int) (bbox.bottom + padY), top + 1, frame.getHeight());
    int w = right - left;
    int h = bottom - top;
    if (w <= 0 || h <= 0) return "";

    Bitmap rawCrop;
    try {
      rawCrop = Bitmap.createBitmap(frame, left, top, w, h);
      if (rawCrop == frame) rawCrop = frame.copy(Bitmap.Config.ARGB_8888, false);
    } catch (Exception e) {
      return "";
    }

    Bitmap uprightCrop;
    if (sensorOrientation % 360 != 0) {
      Matrix matrix = new Matrix();
      matrix.postRotate(sensorOrientation);
      uprightCrop =
          Bitmap.createBitmap(rawCrop, 0, 0, rawCrop.getWidth(), rawCrop.getHeight(), matrix, true);
      if (uprightCrop != rawCrop) rawCrop.recycle();
    } else {
      uprightCrop = rawCrop;
    }

    String filename = String.format(Locale.US, "%06d_%s.jpg", frameNum, sanitize(role));
    File cropFile = new File(session.cropsDir, filename);
    try (FileOutputStream fos = new FileOutputStream(cropFile)) {
      uprightCrop.compress(Bitmap.CompressFormat.JPEG, config.jpegQuality, fos);
      session.cropCount++;
      return "crops/" + filename;
    } catch (IOException e) {
      session.io.fail(e);
      Timber.e(e, "Failed to save diagnostic crop");
      return "";
    } finally {
      uprightCrop.recycle();
    }
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private static String csv(String value) {
    if (value == null) return "";
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }

  private static String sanitize(String value) {
    return safe(value).replaceAll("[^a-zA-Z0-9_\\-]", "_");
  }

  private static class RecognitionSnapshot {
    final RectF bbox;

    RecognitionSnapshot(RectF bbox) {
      this.bbox = bbox;
    }

    static RecognitionSnapshot from(Recognition recognition) {
      if (recognition == null || recognition.getLocation() == null) return null;
      return new RecognitionSnapshot(new RectF(recognition.getLocation()));
    }
  }

  private static class IdentitySnapshot {
    final int trackId;
    final int lockedTrackId;
    final int suspectedTrackId;
    final int activeTrackCount;
    final int trackAge;
    final int missedFrames;
    final float bestScore;
    final float secondScore;
    final float margin;
    final int gallerySize;
    final boolean weakOk;
    final boolean midOk;
    final boolean strongOk;
    final boolean bboxLooseAdmissionOk;
    final boolean bboxDefaultOk;
    final boolean bboxStrictOk;
    final boolean predictionOk;
    final float targetBelief;
    final int beliefStableFrames;
    final int beliefUncertainFrames;
    final int candidateSwitchCount;
    final String beliefReason;
    final String reidReason;
    final long observationId;
    final long observationTimeMs;
    final long observationFrame;
    final int scoredTrack;
    final boolean fresh;

    IdentitySnapshot(IdentityEvidence identity) {
      ReIDMatchResult reid = identity == null ? null : identity.reidMatch;
      BboxContinuityEvidence bbox = identity == null ? null : identity.bboxContinuity;
      trackId = identity == null ? -1 : identity.trackId;
      lockedTrackId = identity == null ? -1 : identity.lockedTrackId;
      suspectedTrackId = identity == null ? -1 : identity.suspectedTrackId;
      activeTrackCount = identity == null ? 0 : identity.activeTrackCount;
      trackAge = identity == null ? 0 : identity.trackAge;
      missedFrames = identity == null ? 0 : identity.missedFrames;
      bestScore = reid == null ? 0f : reid.bestScore;
      secondScore = reid == null ? 0f : reid.secondScore;
      margin = reid == null ? 0f : reid.margin;
      gallerySize = reid == null ? 0 : reid.gallerySize;
      weakOk = identity != null && identity.weakOk();
      midOk = identity != null && identity.midOk();
      strongOk = identity != null && identity.strongOk();
      bboxLooseAdmissionOk = bbox != null && bbox.looseAdmissionOk;
      bboxDefaultOk = bbox != null && bbox.bboxDefaultOk;
      bboxStrictOk = bbox != null && bbox.bboxStrictOk;
      predictionOk = bbox != null && bbox.predictionOk;
      targetBelief = identity == null ? 0f : identity.targetBelief;
      beliefStableFrames = identity == null ? 0 : identity.beliefStableFrames;
      beliefUncertainFrames = identity == null ? 0 : identity.beliefUncertainFrames;
      candidateSwitchCount = identity == null ? 0 : identity.candidateSwitchCount;
      beliefReason = identity == null || identity.beliefReason == null ? "" : identity.beliefReason;
      reidReason = reid == null || reid.reason == null ? "" : reid.reason;
      observationId = reid == null ? 0L : reid.observationId;
      observationTimeMs = reid == null ? 0L : reid.observationTimeMs;
      observationFrame = reid == null ? 0L : reid.frameSequence;
      scoredTrack = reid == null ? -1 : reid.candidateTrackId;
      fresh = reid != null && reid.fresh;
    }
  }
}
