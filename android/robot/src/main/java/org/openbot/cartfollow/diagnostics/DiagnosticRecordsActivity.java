package org.openbot.cartfollow.diagnostics;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONObject;

/** Phone-only record browser. File picker and sharing require no computer or network connection. */
public class DiagnosticRecordsActivity extends AppCompatActivity {
  private final ExecutorService worker = Executors.newSingleThreadExecutor();
  private ListView list;
  private TextView status;
  private File selected;
  private boolean busy;
  private static final int EXPORT = 71;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    if (state != null && state.getString("selected") != null)
      selected =
          new File(CartFollowDiagnosticSession.baseDirectory(this), state.getString("selected"));
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    int pad = (int) (16 * getResources().getDisplayMetrics().density);
    layout.setPadding(pad, pad, pad, pad);
    status = new TextView(this);
    status.setText("测试记录 · 停止测试后可保存 ZIP 或分享");
    layout.addView(status);
    Button refresh = new Button(this);
    refresh.setText("刷新记录");
    refresh.setOnClickListener(v -> load());
    layout.addView(refresh);
    list = new ListView(this);
    layout.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
    setContentView(layout);
    load();
  }

  @Override
  protected void onSaveInstanceState(Bundle out) {
    if (selected != null) out.putString("selected", selected.getName());
    super.onSaveInstanceState(out);
  }

  private void load() {
    if (busy) return;
    busy = true;
    worker.execute(
        () -> {
          File[] dirs =
              CartFollowDiagnosticSession.baseDirectory(this).listFiles(File::isDirectory);
          if (dirs == null) dirs = new File[0];
          Arrays.sort(dirs, (a, b) -> b.getName().compareTo(a.getName()));
          File[] records = dirs;
          List<String> rows = new ArrayList<>();
          for (File dir : records) {
            JSONObject info = readJson(new File(dir, "session_info.json"));
            JSONObject summary = readJson(new File(dir, "summary.json"));
            CartFollowDiagnosticSession active = CartFollowDiagnosticSession.active(dir);
            String completion =
                active != null ? active.health() : summary.optString("status", "incomplete");
            completion =
                completion.equals("complete")
                    ? "已完成"
                    : completion.equals("incomplete")
                        ? "不完整"
                        : completion.equals("error") ? "写入异常" : completion;
            String mode = info.optString("app_mode", "未知模式");
            rows.add(
                dir.getName()
                    + "\n"
                    + mode
                    + " · "
                    + summary.optLong("duration_ms", 0) / 1000
                    + " 秒 · "
                    + String.format(Locale.US, "%.2f MB", size(dir) / 1048576.0)
                    + " · "
                    + completion
                    + (summary.optString("error").isEmpty()
                        ? ""
                        : "\n" + summary.optString("error")));
          }
          runOnUiThread(
              () -> {
                if (isFinishing() || isDestroyed()) return;
                busy = false;
                list.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
                list.setOnItemClickListener(
                    (parent, view, position, id) -> choose(records[position]));
                if (records.length == 0) status.setText("暂无记录。返回跟随页面，打开“记录日志”后开始测试。");
              });
        });
  }

  private void choose(File record) {
    if (busy) return;
    CartFollowDiagnosticSession active = CartFollowDiagnosticSession.active(record);
    if (active != null && !active.io.isClosed()) {
      status.setText("记录正在收尾，请稍后刷新");
      return;
    }
    selected = record;
    new AlertDialog.Builder(this)
        .setTitle("导出测试记录")
        .setItems(
            new String[] {"保存 ZIP 到手机文件夹", "系统分享 ZIP"},
            (dialog, which) -> {
              if (which == 0) {
                Intent intent =
                    new Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("application/zip")
                        .putExtra(Intent.EXTRA_TITLE, record.getName() + ".zip");
                startActivityForResult(intent, EXPORT);
              } else export(record, null);
            })
        .show();
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data) {
    super.onActivityResult(request, result, data);
    if (request == EXPORT
        && result == RESULT_OK
        && data != null
        && data.getData() != null
        && selected != null) export(selected, data.getData());
    else if (request == EXPORT) status.setText("已取消导出，原记录保留");
  }

  private void export(File record, Uri destination) {
    busy = true;
    status.setText("正在导出 ZIP…");
    worker.execute(
        () -> {
          try {
            File cache = new File(getCacheDir(), "diagnostic_exports");
            File zip = new File(cache, record.getName() + ".zip");
            if (destination == null && !cache.isDirectory() && !cache.mkdirs())
              throw new IOException("无法创建导出目录");
            OutputStream stream =
                destination == null
                    ? new FileOutputStream(zip)
                    : getContentResolver().openOutputStream(destination);
            if (stream == null) throw new IOException("无法写入所选文件夹");
            try (OutputStream owned = stream) {
              DiagnosticExport.writeZip(record, owned);
            }
            runOnUiThread(
                () -> {
                  busy = false;
                  if (isFinishing() || isDestroyed()) return;
                  status.setText("ZIP 已导出，原记录保留");
                  if (destination == null) {
                    Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", zip);
                    Intent send =
                        new Intent(Intent.ACTION_SEND)
                            .setType("application/zip")
                            .putExtra(Intent.EXTRA_STREAM, uri)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    send.setClipData(android.content.ClipData.newRawUri("测试记录", uri));
                    startActivity(Intent.createChooser(send, "分享测试记录"));
                  }
                });
          } catch (Exception error) {
            runOnUiThread(
                () -> {
                  busy = false;
                  if (!isDestroyed()) status.setText("导出失败，原记录保留：" + error.getMessage());
                });
          }
        });
  }

  static JSONObject readJson(File file) {
    try (InputStream in = new FileInputStream(file);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int n;
      while ((n = in.read(buffer)) >= 0) bytes.write(buffer, 0, n);
      return new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
    } catch (Exception e) {
      return new JSONObject();
    }
  }

  static long size(File file) {
    if (file.isFile()) return file.length();
    File[] files = file.listFiles();
    long bytes = 0;
    if (files != null) for (File child : files) bytes += size(child);
    return bytes;
  }

  @Override
  protected void onDestroy() {
    worker.shutdown();
    super.onDestroy();
  }
}
