package org.openbot.cartfollow.diagnostics;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.openbot.vehicle.RangeTelemetrySnapshot;

public class CartFollowDiagnosticSession {
  private static final String TAG = "CartFollowDiagnostic";

  private static final String FRAME_LOG_HEADER =
      "session_id,frame_id,timestamp_ms,elapsed_ms,fps,num_persons,follow_state,"
          + "selected_action,action_reason,safety_block_reason,command_text,"
          + "steering_valid,steering_reason,steering_raw_error,steering_filtered_error,"
          + "steering_lateral_rate_per_s,steering_predicted_error,steering_edge_urgency,"
          + "steering_demand_percent,steering_direction,steering_level,steering_prediction_horizon_ms,"
          + "sim_phase,sim_gear,sim_left,sim_right,sim_reason,high_confidence_threshold,"
          + "low_confidence_threshold,low_confidence_count,continued_low_confidence_count,"
          + "selected_low_confidence,directed_phase,directed_direction,directed_speed,"
          + "directed_left,directed_right,directed_turned_degrees,directed_target_degrees,"
          + "directed_elapsed_ms,directed_timeout_ms,directed_gyro_available,"
          + "directed_wrong_direction,directed_reason,frame_received_ms,sensor_timestamp_ns,"
          + "detector_ms,reid_ms,pipeline_ms,result_age_ms,dropped_frames,observation_source,"
          + "observation_track_id,screen_left,screen_top,screen_right,screen_bottom,distance_diagnostic,"
          + "copy_ms,legacy_match_ms,initialization_ms,decision_ms,"
          + "identity_state,retain_target,motion_permit,sampling_permit,hold_remaining_ms,"
          + "continuity_reason,identity_reason,recent_enabled,recent_size,recent_score,recent_reason,"
          + "anchor_count,adaptive_count,quarantine_count,deferred_review,recent_matching_support,"
          + "recovery_type,recovery_matches,recovery_required,real_intent,real_phase,real_gear,real_left,real_right,real_reason,"
          + "maintenance_evidence_ms,maintenance_observation_id";
  private static final String IDENTITY_LOG_HEADER =
      "session_id,frame_id,timestamp_ms,track_id,locked_track_id,suspected_track_id,"
          + "active_track_count,track_age,missed_frames,best_score,second_score,margin,"
          + "gallery_size,weak_ok,mid_ok,strong_ok,bbox_loose_admission_ok,bbox_default_ok,bbox_strict_ok,"
          + "prediction_ok,target_belief,belief_stable_frames,belief_uncertain_frames,"
          + "candidate_switch_count,belief_reason,reid_reason,locked_crop_path,"
          + "suspected_crop_path,best_reid_crop_path,gallery_mode,anchor_gallery_size,"
          + "adaptive_gallery_size,quarantine_gallery_size,gallery_pending_confirmations,"
          + "quarantine_confirmations,gallery_revision,anchor_score,adaptive_score,"
          + "gallery_novelty,gallery_event,gallery_reason,reid_observation_id,reid_observation_ms,"
          + "reid_source_frame,reid_scored_track,reid_fresh,crop_visible_width_px,"
          + "crop_visible_height_px,crop_height_ratio,crop_normal_reason,crop_quarantine_reason";
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

  public volatile int frameRows,
      identityRows,
      candidateRows,
      eventRows,
      rangeRows,
      cropCount,
      galleryCount;
  public final File controlLogCsv, candidateLogCsv, rangeLogCsv, provenanceFile;
  public final long startedMonotonicMs = android.os.SystemClock.elapsedRealtime();
  public final DiagnosticIo io;
  public volatile long latestFrame, latestSourceMs, latestGeneration;
  public volatile String mode = "HumanCartSimulator";
  private volatile String controlMode = "unknown";
  private volatile boolean sawManualMode;
  private volatile boolean sawAutoMode;
  private long lastRangeSequence = Long.MIN_VALUE;
  private String lastRangeState = "";
  private volatile boolean finished;
  private long lastGalleryImageMs = -1, lastSceneMs = -1;
  private String lastSceneKey = "";
  private static final java.util.Map<String, CartFollowDiagnosticSession> ACTIVE =
      new java.util.concurrent.ConcurrentHashMap<>();

  public static CartFollowDiagnosticSession active(File dir) {
    return ACTIVE.get(dir.getAbsolutePath());
  }

  public static File baseDirectory(Context context) {
    File root = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (root == null) root = new File(context.getFilesDir(), "Pictures");
    return new File(root, "cartfollow_diagnostics");
  }

  public CartFollowDiagnosticSession(Context context) {
    this(baseDirectory(context));
  }

  public CartFollowDiagnosticSession(File baseDir) {
    startedAtMs = System.currentTimeMillis();
    sessionId =
        "cart_diag_"
            + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date())
            + "_"
            + java.util.UUID.randomUUID().toString().substring(0, 8);
    sessionDir = new File(baseDir, sessionId);
    cropsDir = new File(sessionDir, "crops");
    galleryDir = new File(sessionDir, "gallery");
    overlaysDir = new File(sessionDir, "overlays");
    frameLogCsv = new File(sessionDir, "frame_log.csv");
    identityLogCsv = new File(sessionDir, "identity_log.csv");
    eventsCsv = new File(sessionDir, "events.csv");
    controlLogCsv = new File(sessionDir, "control_log.csv");
    candidateLogCsv = new File(sessionDir, "candidate_log.csv");
    rangeLogCsv = new File(sessionDir, "range_log.csv");
    provenanceFile = new File(sessionDir, "gallery_provenance.jsonl");
    io = new DiagnosticIo(this::initialize);
    ACTIVE.put(sessionDir.getAbsolutePath(), this);
    io.start();
  }

  private void initialize() {
    try {
      for (File dir : new File[] {sessionDir, cropsDir, galleryDir, overlaysDir})
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Cannot create " + dir);
      writeHeader(
          frameLogCsv,
          FRAME_LOG_HEADER
              + ",tracking_session,tracking_frame,tracking_ms,tracking_tier,tracking_stable,tracking_max_gear,tracking_reason"
              + ",raw_low_candidate_count,tracked_low_candidate_count,identity_candidate_count,multi_check_state,primary_limit_reason"
              + ",aim_allowed,aim_mode,aim_error,aim_reason,translation_allowed,translation_max_gear,translation_reason"
              + ",initialization_samples,initialization_track_id,initialization_discard_reason,distance_calibration_samples,distance_calibration_completed_ms"
              + ",range_capability,range_minimum_mm,range_received_ms,range_fresh,range_gate_reason,range_firmware_error");
      writeHeader(identityLogCsv, IDENTITY_LOG_HEADER);
      writeHeader(eventsCsv, EVENTS_HEADER);
      writeHeader(
          controlLogCsv,
          "session_id,monotonic_ms,source_frame,source_ms,generation,event,details,control_mode");
      writeHeader(
          candidateLogCsv,
          "session_id,frame_id,timestamp_ms,candidate_index,tier,confidence,left,top,right,bottom,track_id,identity_eligible,association_score,association_competing,locked_association_margin,match_reason");
      writeHeader(
          rangeLogCsv,
          "session_id,monotonic_ms,received_ms,sequence,minimum_mm,age_ms,fresh,capability,has_reading,control_mode,requested_left,requested_right,observation_state,firmware_error");
      writeHeader(provenanceFile, null);
      writeJson(
          new File(sessionDir, "status.json"),
          new JSONObject().put("status", "incomplete").put("mode", mode));
    } catch (IOException | JSONException e) {
      io.fail(e);
    }
  }

  /** Retained for callers of the original logger; initialization is scheduled exactly once. */
  public void initCsvFiles() {}

  public void writeSessionInfo(
      CartFollowDiagnosticConfig config,
      String detector,
      float threshold,
      boolean reid,
      int gallerySize,
      boolean upright,
      int orientation) {
    final String initialControlMode = controlMode;
    io.submit(
        () -> {
          try {
            JSONObject json = new JSONObject();
            json.put("session_id", sessionId)
                .put("created_at_ms", startedAtMs)
                .put("started_monotonic_ms", startedMonotonicMs)
                .put("app_mode", mode)
                .put("initial_control_mode", initialControlMode)
                .put("log_version", 7)
                .put("build", org.openbot.BuildConfig.VERSION_NAME)
                .put("build_stamp", org.openbot.BuildConfig.CART_BUILD_STAMP)
                .put("strategy", "observed-aim-pulse-v5")
                .put("device_model", Build.MODEL)
                .put("sdk_int", Build.VERSION.SDK_INT)
                .put("detector", detector)
                .put("min_confidence", threshold)
                .put("low_confidence", Math.min(.25f, threshold))
                .put("reid_available", reid)
                .put("gallery_size", gallerySize)
                .put("reid_crop_upright", upright)
                .put("sensor_orientation", orientation)
                .put("frame_log_interval_ms", config.frameLogIntervalMs)
                .put("crop_interval_ms", config.cropIntervalMs)
                .put("overlay_interval_ms", config.overlayIntervalMs)
                .put("save_crops", config.saveCrops)
                .put("save_overlays", config.saveOverlays)
                .put("center_gate", .35)
                .put("prediction_gate", .18)
                .put("association_margin", .15)
                .put("short_return_ms", 500)
                .put("stable_frames", 3)
                .put("aim_pivot_enter_error", .18)
                .put("aim_pivot_exit_error", .08)
                .put(
                    "aim_edge_pivot_error",
                    org.openbot.cartfollow.TargetAimController.FAR_ENTER_ERROR)
                .put("aim_pulse_ms", org.openbot.cartfollow.TargetAimController.PULSE_MS)
                .put("aim_settle_ms", org.openbot.cartfollow.TargetAimController.SETTLE_MS)
                .put(
                    "aim_far_exit_error", org.openbot.cartfollow.TargetAimController.FAR_EXIT_ERROR)
                .put("real_prediction_horizon_ms", 0)
                .put("aim_pivot_speed", 5)
                .put("distance_calibration_samples", 15)
                .put("distance_calibration_span_ms", 500)
                .put("default_maximum_distance_multiplier", 1.10)
                .put("reid_interval_ms", 200)
                .put("multi_check_ms", 500)
                .put("multi_timeout_ms", 1000)
                .put("candidate_budget", 5)
                .put("recovery_matches", 5)
                .put("recovery_span_ms", 1200)
                .put("recovery_score", .85)
                .put("recovery_margin", .08)
                .put("learning_similarity", .75)
                .put("adaptive_capacity", 8)
                .put("recent_capacity", 16)
                .put("recent_ttl_ms", 5000)
                .put("weak_max_gear", 18)
                .put("max_gear", 21)
                .put("real_frame_max_age_ms", 400);
            json.put("range_protocol", "CART_AT8236_V1_s")
                .put("range_telemetry_period_ms", 100)
                .put("range_stale_ms", 250)
                .put("range_source", "minimum_of_three_source_unknown")
                .put("range_android_behavior", "observation_only")
                .put("range_firmware_may_reject_motion", true)
                .put("firmware_c14_mmps", 240)
                .put("firmware_c21_mmps", 600);
            writeJson(new File(sessionDir, "session_info.json"), json);
          } catch (IOException | JSONException e) {
            io.fail(e);
          }
        });
  }

  private final java.util.LinkedHashMap<String, java.util.ArrayDeque<long[]>> queuedSources =
      new java.util.LinkedHashMap<>();
  private long[] inFlightSource = new long[] {-1, -1, -1};

  private static String queueKey(String details) {
    String type = "", generation = "", payload = "";
    for (String field : details.split(",")) {
      if (field.startsWith("type=")) type = field;
      if (field.startsWith("generation=")) generation = field;
      if (field.startsWith("payload=")) payload = field;
    }
    return type + "|" + generation + "|" + payload;
  }

  public synchronized void control(String event, String details) {
    final long now = android.os.SystemClock.elapsedRealtime();
    long[] context = new long[] {latestFrame, latestSourceMs, latestGeneration};
    if (event.equals("queue_enqueue") || event.equals("queue_transition")) {
      String key = queueKey(details);
      java.util.ArrayDeque<long[]> sources = queuedSources.get(key);
      if (sources == null) {
        sources = new java.util.ArrayDeque<>();
        queuedSources.put(key, sources);
      }
      sources.addLast(context);
      if (queuedSources.size() > 128)
        queuedSources.remove(queuedSources.keySet().iterator().next());
    } else if (event.equals("queue_dispatch") || event.equals("queue_replaced")) {
      java.util.ArrayDeque<long[]> sources = queuedSources.get(queueKey(details));
      context =
          sources == null || sources.isEmpty() ? new long[] {-1, -1, -1} : sources.removeFirst();
      if (sources != null && sources.isEmpty()) queuedSources.remove(queueKey(details));
      if (event.equals("queue_dispatch")) inFlightSource = context;
    } else if (event.startsWith("gatt_")
        || event.equals("queue_success")
        || event.equals("queue_failure")
        || event.equals("queue_retry")) context = inFlightSource;
    else if (event.equals("queue_clear")) {
      queuedSources.clear();
      inFlightSource = new long[] {-1, -1, -1};
    }
    final long frame = context[0], source = context[1], generation = context[2];
    final String rowControlMode = controlMode;
    io.submit(
        () ->
            io.append(
                controlLogCsv,
                quote(sessionId)
                    + ","
                    + now
                    + ","
                    + frame
                    + ","
                    + source
                    + ","
                    + generation
                    + ","
                    + quote(event)
                    + ","
                    + quote(details)
                    + ","
                    + quote(rowControlMode)
                    + "\n"));
  }

  public synchronized void setControlMode(String nextMode) {
    String normalized = nextMode == null ? "unknown" : nextMode.trim().toLowerCase(Locale.US);
    if (!normalized.equals("manual") && !normalized.equals("auto")) normalized = "unknown";
    if (normalized.equals(controlMode)) return;
    String previous = controlMode;
    controlMode = normalized;
    if (normalized.equals("manual")) sawManualMode = true;
    if (normalized.equals("auto")) sawAutoMode = true;
    control("mode_changed", "from=" + previous + ",to=" + normalized);
  }

  /** Records each new V1 range sample and capability/freshness/error state transition. */
  public synchronized void range(
      RangeTelemetrySnapshot telemetry,
      long nowMs,
      boolean fresh,
      int requestedLeft,
      int requestedRight) {
    RangeTelemetrySnapshot safe =
        telemetry == null ? RangeTelemetrySnapshot.unavailable() : telemetry;
    String state =
        (safe.capabilityAdvertised ? "1" : "0")
            + ":"
            + (safe.hasReading ? "1" : "0")
            + ":"
            + (fresh ? "1" : "0")
            + ":"
            + safe.firmwareErrorAtMs;
    if (safe.sequence == lastRangeSequence && state.equals(lastRangeState)) return;
    lastRangeSequence = safe.sequence;
    lastRangeState = state;
    String rowControlMode = controlMode;
    io.submit(
        () -> {
          rangeRows++;
          io.append(
              rangeLogCsv,
              quote(sessionId)
                  + ","
                  + nowMs
                  + ","
                  + safe.receivedAtMs
                  + ","
                  + safe.sequence
                  + ","
                  + safe.minimumDistanceMm
                  + ","
                  + (safe.ageMs(nowMs) == Long.MAX_VALUE ? -1L : safe.ageMs(nowMs))
                  + ","
                  + (fresh ? 1 : 0)
                  + ","
                  + (safe.capabilityAdvertised ? 1 : 0)
                  + ","
                  + (safe.hasReading ? 1 : 0)
                  + ","
                  + quote(rowControlMode)
                  + ","
                  + requestedLeft
                  + ","
                  + requestedRight
                  + ","
                  + quote("observation_only")
                  + ","
                  + quote(safe.lastFirmwareError)
                  + "\n");
        });
  }

  public void provenance(String json) {
    io.submit(() -> io.append(provenanceFile, json + "\n"));
  }

  public synchronized boolean galleryImageDue(long now) {
    if (lastGalleryImageMs >= 0 && now - lastGalleryImageMs < 1000) return false;
    lastGalleryImageMs = now;
    return true;
  }

  public synchronized boolean sceneDue(long now, String key) {
    if (key.equals(lastSceneKey)) return false;
    if (lastSceneMs >= 0 && now - lastSceneMs < 1000) return false;
    lastSceneKey = key;
    lastSceneMs = now;
    return true;
  }

  public synchronized void finish(String reason) {
    if (finished) return;
    control("recording_end", reason);
    finished = true;
    final long ended = android.os.SystemClock.elapsedRealtime();
    io.finish(
        () -> {
          try {
            String recordedControlModes =
                sawManualMode && sawAutoMode
                    ? "mixed"
                    : sawManualMode ? "manual" : sawAutoMode ? "auto" : "unknown";
            JSONObject summary =
                new JSONObject()
                    .put("status", io.error.isEmpty() ? "complete" : "error")
                    .put("mode", mode)
                    .put("control_modes", recordedControlModes)
                    .put("duration_ms", Math.max(0, ended - startedMonotonicMs))
                    .put("ended_at_ms", System.currentTimeMillis())
                    .put("reason", reason)
                    .put("error", io.error)
                    .put("frames", frameRows)
                    .put("identities", identityRows)
                    .put("candidates", candidateRows)
                    .put("events", eventRows)
                    .put("ranges", rangeRows)
                    .put("crops", cropCount)
                    .put("gallery_images", galleryCount)
                    .put("dropped_images", io.droppedImages.get())
                    .put("dropped_text", io.droppedText.get());
            writeJson(new File(sessionDir, "summary.json"), summary);
            writeJson(new File(sessionDir, "status.json"), summary);
          } catch (IOException | JSONException e) {
            io.fail(e);
          } finally {
            ACTIVE.remove(sessionDir.getAbsolutePath());
          }
        });
  }

  public String health() {
    return (!io.error.isEmpty() ? "日志写入异常：" + io.error : finished ? "日志正在收尾" : "日志记录中")
        + " · 丢图 "
        + io.droppedImages.get()
        + " / 丢文字 "
        + io.droppedText.get();
  }

  public static String quote(String value) {
    return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
  }

  private static void writeHeader(File file, String header) throws IOException {
    try (java.io.Writer writer =
        new java.io.OutputStreamWriter(
            new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
      if (header != null) writer.write(header + "\n");
    }
  }

  private static void writeJson(File file, JSONObject value) throws IOException, JSONException {
    android.util.AtomicFile atomic = new android.util.AtomicFile(file);
    java.io.FileOutputStream out = null;
    try {
      out = atomic.startWrite();
      out.write(value.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      atomic.finishWrite(out);
    } catch (IOException | JSONException e) {
      if (out != null) atomic.failWrite(out);
      throw e;
    }
  }
}
