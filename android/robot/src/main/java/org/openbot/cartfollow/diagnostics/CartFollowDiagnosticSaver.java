package org.openbot.cartfollow.diagnostics;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openbot.cartfollow.BehaviorDecisionResult;
import org.openbot.cartfollow.BboxContinuityEvidence;
import org.openbot.cartfollow.IdentityEvidence;
import org.openbot.cartfollow.ReIDMatchResult;
import org.openbot.cartfollow.SteeringEvidence;
import org.openbot.tflite.Detector.Recognition;
import timber.log.Timber;

public class CartFollowDiagnosticSaver {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        decision == null || decision.safetyBlockReason == null ? "" : safe(decision.safetyBlockReason);
    final String safeCommand = safe(commandText);
    final IdentitySnapshot identitySnapshot = new IdentitySnapshot(identity);
    final RecognitionSnapshot lockedSnapshot = RecognitionSnapshot.from(locked);
    final RecognitionSnapshot suspectedSnapshot = RecognitionSnapshot.from(suspected);
    final RecognitionSnapshot bestSnapshot = RecognitionSnapshot.from(bestReid);
    Bitmap.Config bitmapConfig = frame == null || frame.getConfig() == null ? Bitmap.Config.ARGB_8888 : frame.getConfig();
    final Bitmap frameCopy =
        frame != null && config.saveCrops && saveCropsForThisFrame ? frame.copy(bitmapConfig, false) : null;

    executor.execute(
        () -> {
          String lockedPath = "";
          String suspectedPath = "";
          String bestPath = "";
          if (frameCopy != null) {
            lockedPath =
                saveCrop(frameCopy, lockedSnapshot, session, config, frameNum, "locked", sensorOrientation);
            suspectedPath =
                saveCrop(frameCopy, suspectedSnapshot, session, config, frameNum, "suspected", sensorOrientation);
            bestPath =
                saveCrop(frameCopy, bestSnapshot, session, config, frameNum, "best_reid", sensorOrientation);
            frameCopy.recycle();
          }
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
              steering);
          appendIdentityLog(
              session,
              frameNum,
              timestampMs,
              identitySnapshot,
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
    executor.execute(
        () -> {
          String row =
              String.format(
                  Locale.US,
                  "%s,%d,%d,%s,%s\n",
                  csv(session.sessionId), timestampMs, frameNum, csv(safeType), csv(safeNote));
          append(session.eventsCsv, row, "events.csv");
          session.eventRows++;
        });
  }

  public void saveGallerySnapshotAsync(
      Bitmap bitmap, CartFollowDiagnosticSession session, String label) {
    if (bitmap == null || session == null) return;
    Bitmap.Config bitmapConfig = bitmap.getConfig() == null ? Bitmap.Config.ARGB_8888 : bitmap.getConfig();
    final Bitmap copy = bitmap.copy(bitmapConfig, false);
    final String safeLabel = sanitize(label == null ? "gallery" : label);
    executor.execute(
        () -> {
          String filename =
              String.format(Locale.US, "%06d_%s.jpg", session.galleryCount, safeLabel);
          File file = new File(session.galleryDir, filename);
          try (FileOutputStream fos = new FileOutputStream(file)) {
            copy.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            session.galleryCount++;
          } catch (IOException e) {
            Timber.e(e, "Failed to save diagnostic gallery snapshot");
          } finally {
            copy.recycle();
          }
        });
  }

  public void shutdown() {
    executor.shutdown();
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
      SteeringEvidence steering) {
    SteeringEvidence snapshot =
        steering == null ? SteeringEvidence.unavailable("not_collected", 0) : steering;
    String row =
        String.format(
            Locale.US,
            "%s,%d,%d,%d,%.2f,%d,%s,%s,%s,%s,%s,%s,%s,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%s,%s,%d\n",
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
            snapshot.predictionHorizonMs);
    append(session.frameLogCsv, row, "frame_log.csv");
    session.frameRows++;
  }

  private void appendIdentityLog(
      CartFollowDiagnosticSession session,
      long frameNum,
      long timestampMs,
      IdentitySnapshot id,
      String lockedPath,
      String suspectedPath,
      String bestPath) {
    String row =
        String.format(
            Locale.US,
            "%s,%d,%d,%d,%d,%d,%d,%d,%d,%.4f,%.4f,%.4f,%d,%s,%s,%s,%s,%s,%s,%s,%.4f,%d,%d,%d,%s,%s,%s,%s,%s\n",
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
            csv(bestPath));
    append(session.identityLogCsv, row, "identity_log.csv");
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
    } catch (Exception e) {
      return "";
    }

    Bitmap uprightCrop;
    if (sensorOrientation % 360 != 0) {
      Matrix matrix = new Matrix();
      matrix.postRotate(sensorOrientation);
      uprightCrop =
          Bitmap.createBitmap(rawCrop, 0, 0, rawCrop.getWidth(), rawCrop.getHeight(), matrix, true);
      rawCrop.recycle();
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
      Timber.e(e, "Failed to save diagnostic crop");
      return "";
    } finally {
      uprightCrop.recycle();
    }
  }

  private void append(File file, String row, String label) {
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.append(row);
    } catch (IOException e) {
      Timber.e(e, "Failed to append %s", label);
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
    }
  }
}
