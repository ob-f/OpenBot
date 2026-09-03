package org.openbot.cartfollow.diagnostics;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class OfflineDiagnosticTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void closeDrainsAcceptedEventsRejectsLateWritesAndExportsCompleteZip() throws Exception {
    CartFollowDiagnosticSession session = new CartFollowDiagnosticSession(temp.getRoot());
    session.mode = "真实小车";
    session.writeSessionInfo(new CartFollowDiagnosticConfig(), "test", .5f, true, 8, true, 90);
    session.latestFrame = 99;
    session.latestSourceMs = 100;
    session.latestGeneration = 7;
    session.control("control_submit", "requested=c18,18");
    session.control("gatt_success", "write_success_only");
    session.provenance("{\"globalEligible\":true,\"source_frame\":91}");
    session.finish("test_end");
    session.control("late", "must_not_appear");
    assertTrue(session.io.awaitClosed(5000));
    assertEquals("", session.io.error);
    String controls =
        new String(Files.readAllBytes(session.controlLogCsv.toPath()), StandardCharsets.UTF_8);
    assertTrue(controls.contains("control_submit"));
    assertTrue(controls.contains("gatt_success"));
    assertTrue(controls.contains(",99,100,7,"));
    assertFalse(controls.contains("must_not_appear"));
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DiagnosticExport.writeZip(session.sessionDir, bytes);
    Set<String> entries = new HashSet<>();
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) entries.add(entry.getName());
    }
    assertTrue(
        entries.containsAll(
            Arrays.asList(
                "session_info.json",
                "summary.json",
                "status.json",
                "frame_log.csv",
                "identity_log.csv",
                "control_log.csv",
                "gallery_provenance.jsonl")));
    assertEquals(
        "complete",
        DiagnosticRecordsActivity.readJson(new File(session.sessionDir, "summary.json"))
            .getString("status"));
    assertEquals(
        "真实小车",
        DiagnosticRecordsActivity.readJson(new File(session.sessionDir, "session_info.json"))
            .getString("app_mode"));
  }

  @Test
  public void delayedGattCallbackKeepsSubmittedFrameAfterNewDetections() throws Exception {
    CartFollowDiagnosticSession session = new CartFollowDiagnosticSession(temp.getRoot());
    String queue = "type=MOTION,generation=7,pending=1,payload=c18;18";
    session.latestFrame = 41;
    session.latestSourceMs = 1400;
    session.latestGeneration = 7;
    session.control("queue_enqueue", queue);
    session.latestFrame = 42;
    session.latestSourceMs = 1433;
    session.control("queue_dispatch", queue);
    session.latestFrame = 43;
    session.latestSourceMs = 1466;
    session.control("gatt_success", "payload=c18;18");
    session.finish("callback_test");
    assertTrue(session.io.awaitClosed(5000));
    String controls =
        new String(Files.readAllBytes(session.controlLogCsv.toPath()), StandardCharsets.UTF_8);
    assertTrue(controls.contains(",41,1400,7,\"queue_dispatch\""));
    assertTrue(controls.contains(",41,1400,7,\"gatt_success\""));
    assertFalse(controls.contains(",43,1466,7,\"gatt_success\""));
  }

  @Test
  @org.robolectric.annotation.LooperMode(org.robolectric.annotation.LooperMode.Mode.PAUSED)
  public void phoneRecordPickerCancellationPreservesOriginalRecord() throws Exception {
    android.content.Context context =
        androidx.test.core.app.ApplicationProvider.getApplicationContext();
    CartFollowDiagnosticSession session = new CartFollowDiagnosticSession(context);
    session.writeSessionInfo(new CartFollowDiagnosticConfig(), "test", .5f, true, 8, true, 0);
    session.control("stop", "c0,0");
    session.finish("export_cancel_test");
    assertTrue(session.io.awaitClosed(5000));
    byte[] original = Files.readAllBytes(session.controlLogCsv.toPath());
    org.robolectric.android.controller.ActivityController<DiagnosticRecordsActivity> controller =
        org.robolectric.Robolectric.buildActivity(DiagnosticRecordsActivity.class).setup();
    DiagnosticRecordsActivity activity = controller.get();
    android.view.ViewGroup content = activity.findViewById(android.R.id.content);
    android.widget.LinearLayout layout = (android.widget.LinearLayout) content.getChildAt(0);
    android.widget.ListView records = (android.widget.ListView) layout.getChildAt(2);
    for (int n = 0; n < 200 && records.getAdapter() == null; n++) {
      org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
      Thread.sleep(10);
    }
    assertNotNull(records.getAdapter());
    assertEquals(1, records.getAdapter().getCount());
    records.performItemClick(null, 0, 0);
    android.widget.ListView options =
        org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getListView();
    options.performItemClick(null, 0, 0);
    org.robolectric.shadows.ShadowActivity.IntentForResult launched =
        org.robolectric.Shadows.shadowOf(activity).getNextStartedActivityForResult();
    assertNotNull(launched);
    assertEquals(android.content.Intent.ACTION_CREATE_DOCUMENT, launched.intent.getAction());
    assertEquals("application/zip", launched.intent.getType());
    activity.onActivityResult(launched.requestCode, android.app.Activity.RESULT_CANCELED, null);
    assertTrue(
        ((android.widget.TextView) layout.getChildAt(0)).getText().toString().contains("已取消导出"));
    assertArrayEquals(original, Files.readAllBytes(session.controlLogCsv.toPath()));
    assertEquals(
        "complete",
        DiagnosticRecordsActivity.readJson(new File(session.sessionDir, "summary.json"))
            .getString("status"));
    controller.pause().stop().destroy();
  }

  @Test
  public void boundedImageQueueDropsAndFinalizationWaitsForAcceptedImageWork() throws Exception {
    DiagnosticIo io = new DiagnosticIo(() -> {});
    io.start();
    CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
    AtomicInteger discarded = new AtomicInteger(), completed = new AtomicInteger();
    io.image(
        () -> {
          entered.countDown();
          try {
            release.await();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        },
        () -> {});
    assertTrue(entered.await(2, TimeUnit.SECONDS));
    for (int n = 0; n < 20; n++) io.image(completed::incrementAndGet, discarded::incrementAndGet);
    io.finish(() -> {});
    assertFalse(io.isClosed());
    release.countDown();
    assertTrue(io.awaitClosed(5000));
    assertEquals(8, completed.get());
    assertEquals(12, discarded.get());
    assertEquals(12, io.droppedImages.get());
  }

  @Test
  public void textQueueOverflowIsVisibleAndNeverBlocksProducer() throws Exception {
    DiagnosticIo io = new DiagnosticIo(() -> {}); // Hold worker until the queue reaches its bound.
    for (int n = 0; n < 2200; n++) io.submit(() -> {});
    assertTrue(io.droppedText.get() > 0);
    assertFalse(io.error.isEmpty());
    io.start();
    io.finish(() -> {});
    assertTrue(io.awaitClosed(5000));
  }

  @Test
  public void storageFailureIsReportedAndDoesNotPreventShutdown() throws Exception {
    File notDirectory = temp.newFile("storage_failure");
    CartFollowDiagnosticSession session = new CartFollowDiagnosticSession(notDirectory);
    session.control("stop", "c0,0");
    session.finish("storage_test");
    assertTrue(session.io.awaitClosed(5000));
    assertFalse(session.io.error.isEmpty());
    assertTrue(session.health().contains("异常"));
  }

  @Test
  public void interruptedLegacyRecordCanBeExportedWithoutSummary() throws Exception {
    File dir = temp.newFolder("interrupted");
    Files.write(
        new File(dir, "events.csv").toPath(), "event\nstop\n".getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    DiagnosticExport.writeZip(dir, bytes);
    assertTrue(bytes.size() > 0);
    assertTrue(new File(dir, "events.csv").isFile());
    assertEquals(
        "incomplete",
        DiagnosticRecordsActivity.readJson(new File(dir, "summary.json"))
            .optString("status", "incomplete"));
  }

  @Test
  public void activeSessionCannotBeExportedAndImageSnapshotsDrainOnClose() throws Exception {
    CartFollowDiagnosticSession session = new CartFollowDiagnosticSession(temp.getRoot());
    Bitmap bitmap = Bitmap.createBitmap(64, 128, Bitmap.Config.ARGB_8888);
    CartFollowDiagnosticSaver saver = new CartFollowDiagnosticSaver();
    saver.saveGallerySnapshotAsync(bitmap, session, "target");
    try {
      DiagnosticExport.writeZip(session.sessionDir, new ByteArrayOutputStream());
      fail("active export");
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("停止"));
    }
    session.finish("close");
    assertTrue(session.io.awaitClosed(5000));
    bitmap.recycle();
    assertEquals("", session.io.error);
    assertTrue(new File(session.galleryDir, "target.jpg").isFile());
  }
}
