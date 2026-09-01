package org.openbot.cartfollow.diagnostics;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class CartFollowDiagnosticSession {
  private static final String TAG = "CartFollowDiagnostic";

  private static final String FRAME_LOG_HEADER =
      "session_id,frame_id,timestamp_ms,elapsed_ms,fps,num_persons,follow_state,"
          + "selected_action,action_reason,safety_block_reason,command_text,"
          + "steering_valid,steering_reason,steering_raw_error,steering_filtered_error,"
          + "steering_lateral_rate_per_s,steering_predicted_error,steering_edge_urgency,"
          + "steering_demand_percent,steering_direction,steering_level,steering_prediction_horizon_ms";
  private static final String IDENTITY_LOG_HEADER =
      "session_id,frame_id,timestamp_ms,track_id,locked_track_id,suspected_track_id,"
          + "active_track_count,track_age,missed_frames,best_score,second_score,margin,"
          + "gallery_size,weak_ok,mid_ok,strong_ok,bbox_loose_admission_ok,bbox_default_ok,bbox_strict_ok,"
          + "prediction_ok,target_belief,belief_stable_frames,belief_uncertain_frames,"
          + "candidate_switch_count,belief_reason,reid_reason,locked_crop_path,"
          + "suspected_crop_path,best_reid_crop_path";
  private static final String EVENTS_HEADER = "session_id,timestamp_ms,frame_id,event_type,note";

  public final String sessionId;
  public final File sessionDir;
  public final File cropsDir;
  public final File galleryDir;
  public final File overlaysDir;
  public final File frameLogCsv;
  public final File identityLogCsv;
  public final File eventsCsv;
  public final long startedAtMs;

  public int frameRows = 0;
  public int identityRows = 0;
  public int eventRows = 0;
  public int cropCount = 0;
  public int galleryCount = 0;

  public CartFollowDiagnosticSession(Context context) {
    this.startedAtMs = System.currentTimeMillis();
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    this.sessionId = "cart_diag_" + timestamp;
    File baseDir =
        new File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "cartfollow_diagnostics");
    this.sessionDir = new File(baseDir, sessionId);
    this.cropsDir = new File(sessionDir, "crops");
    this.galleryDir = new File(sessionDir, "gallery");
    this.overlaysDir = new File(sessionDir, "overlays");
    this.cropsDir.mkdirs();
    this.galleryDir.mkdirs();
    this.overlaysDir.mkdirs();
    this.frameLogCsv = new File(sessionDir, "frame_log.csv");
    this.identityLogCsv = new File(sessionDir, "identity_log.csv");
    this.eventsCsv = new File(sessionDir, "events.csv");
  }

  public void initCsvFiles() {
    writeHeader(frameLogCsv, FRAME_LOG_HEADER);
    writeHeader(identityLogCsv, IDENTITY_LOG_HEADER);
    writeHeader(eventsCsv, EVENTS_HEADER);
  }

  public void writeSessionInfo(
      CartFollowDiagnosticConfig config,
      String detectorModelName,
      float minConfidence,
      boolean reidAvailable,
      int gallerySize,
      boolean reidCropUpright,
      int sensorOrientation) {
    JSONObject json = new JSONObject();
    try {
      json.put("session_id", sessionId);
      json.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
      json.put("collector", "CartFollowDiagnostic");
      json.put("app_mode", "HumanCartSimulator");
      json.put("phase", "phase_c_diagnostic");
      json.put("frame_log_interval_ms", config.frameLogIntervalMs);
      json.put("crop_interval_ms", config.cropIntervalMs);
      json.put("overlay_interval_ms", config.overlayIntervalMs);
      json.put("save_crops", config.saveCrops);
      json.put("save_overlays", config.saveOverlays);
      json.put("bbox_padding_ratio", config.paddingRatio);
      json.put("jpeg_quality", config.jpegQuality);
      json.put("detector", detectorModelName == null ? "" : detectorModelName);
      json.put("min_confidence", minConfidence);
      json.put("reid_available", reidAvailable);
      json.put("gallery_size", gallerySize);
      json.put("reid_crop_upright", reidCropUpright);
      json.put("sensor_orientation", sensorOrientation);
      json.put("device_model", Build.MODEL == null ? "" : Build.MODEL);
      json.put("sdk_int", Build.VERSION.SDK_INT);
    } catch (JSONException e) {
      Log.e(TAG, "Failed to build session_info.json", e);
      return;
    }

    try (FileWriter writer = new FileWriter(new File(sessionDir, "session_info.json"), false)) {
      writer.write(json.toString(2));
    } catch (IOException | JSONException e) {
      Log.e(TAG, "Failed to write session_info.json", e);
    }
  }

  private void writeHeader(File file, String header) {
    try (FileWriter writer = new FileWriter(file, false)) {
      writer.append(header).append('\n');
    } catch (IOException e) {
      Log.e(TAG, "Failed to init csv: " + file.getName(), e);
    }
  }
}
