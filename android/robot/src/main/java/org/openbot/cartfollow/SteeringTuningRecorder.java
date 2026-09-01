package org.openbot.cartfollow;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import timber.log.Timber;

/** Writes small, image-free steering calibration records outside the diagnostics image directory. */
final class SteeringTuningRecorder {
  private static final String HEADER =
      "timestamp,event,strength_percent,demand_percent,left,right,phase,note\n";

  private final Context context;
  private final ExecutorService writer = Executors.newSingleThreadExecutor();

  SteeringTuningRecorder(Context context) {
    this.context = context.getApplicationContext();
  }

  void record(
      String event,
      int strengthPercent,
      RealCartAutoDriveController.Result result,
      String note) {
    final int demand = result == null ? 0 : result.demandPercent;
    final int left = result == null ? 0 : result.left;
    final int right = result == null ? 0 : result.right;
    final String phase = result == null ? "NONE" : result.phase.name();
    writer.execute(
        () -> {
          File file = historyFile();
          if (file == null) return;
          boolean newFile = !file.exists();
          try (FileWriter out = new FileWriter(file, true)) {
            if (newFile) out.write(HEADER);
            String timestamp =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
            out.write(
                csv(timestamp)
                    + ','
                    + csv(event)
                    + ','
                    + strengthPercent
                    + ','
                    + demand
                    + ','
                    + left
                    + ','
                    + right
                    + ','
                    + csv(phase)
                    + ','
                    + csv(note)
                    + '\n');
          } catch (IOException e) {
            Timber.e(e, "Failed to write steering tuning history.");
          }
        });
  }

  void shutdown() {
    writer.shutdown();
  }

  private File historyFile() {
    File root = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    if (root == null) return null;
    File directory = new File(root, "cartfollow_tuning");
    if (!directory.exists() && !directory.mkdirs()) {
      Timber.w("Unable to create steering tuning directory: %s", directory);
      return null;
    }
    return new File(directory, "steering_strength_history.csv");
  }

  private static String csv(String value) {
    String safe = value == null ? "" : value.replace("\"", "\"\"");
    return '"' + safe + '"';
  }
}
