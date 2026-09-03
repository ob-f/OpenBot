package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FollowStateMachineFastRecoveryTest {
  @Test
  public void fullFrameSnapshotSurvivesSourceRecycle() {
    for (int rotation : new int[] {0, 180}) {
      FollowStateMachine machine =
          new FollowStateMachine(new TargetMatcher(), new ControlGenerator());
      machine.CAPTURE_FRAMES = 1;
      Bitmap source = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
      source.eraseColor(0xff123456);
      Detector.Recognition full =
          new Detector.Recognition("full", "person", .95f, new RectF(0, 0, 100, 200), 0);
      machine.startCapture();
      Bitmap snapshot =
          machine.onFrame(Collections.singletonList(full), source, 100, 200, rotation).snapshot;
      assertNotSame(source, snapshot);
      int retainedPixel = snapshot.getPixel(0, 0);
      source.recycle();
      assertFalse(snapshot.isRecycled());
      assertEquals(100, snapshot.getWidth());
      assertEquals(200, snapshot.getHeight());
      assertEquals(retainedPixel, snapshot.getPixel(0, 0));
      snapshot.recycle();
    }
  }

  @Test
  public void firstStartCountsDownButRecoveredTargetGoesDirectlyToCaution() {
    FollowStateMachine machine =
        new FollowStateMachine(new TargetMatcher(), new ControlGenerator());
    machine.setSimulatorFastRecoveryEnabled(true);
    machine.CAPTURE_FRAMES = 1;
    machine.REACQUIRE_MATCH_N = 1;
    machine.COUNTDOWN_MS = 0L;
    machine.FOLLOW_LOST_M = 1;
    Bitmap frame = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    Detector.Recognition person = person();

    machine.startCapture();
    assertEquals(
        FollowState.LOCKED_PENDING_CONFIRM,
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0).state);
    machine.confirm();
    machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 1));
    assertEquals(
        FollowState.READY_TO_FOLLOW,
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 2))
            .state);
    assertEquals(
        FollowState.FOLLOW,
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 3))
            .state);

    assertEquals(
        FollowState.IDENTITY_UNCERTAIN,
        machine.onFrame(Collections.emptyList(), frame, 100, 200, 0).state);
    assertEquals(
        FollowState.REACQUIRE_TARGET,
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 4))
            .state);
    for (int i = 0; i < 4; i++) {
      assertEquals(
          FollowState.REACQUIRE_TARGET,
          machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 0))
              .state);
    }
    machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 5));
    for (int i = 0; i < 4; i++) {
      assertEquals(
          FollowState.REACQUIRE_TARGET,
          machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 5))
              .state);
    }
    assertEquals(
        FollowState.REACQUIRE_TARGET,
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 4))
            .state);
    machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 6));
    assertEquals(
        FollowState.FOLLOW_CAUTION,
        machine.onFrame(Collections.singletonList(person), frame, 100, 200, 0, strong(person, 7))
            .state);
    frame.recycle();
  }

  private static IdentityEvidence strong(Detector.Recognition person, long observationId) {
    ReIDMatchResult reid =
        new ReIDMatchResult(0.92f, 0.75f, 0, 8, true, 1L, "fresh", 0.92f, 0f, observationId);
    return new IdentityEvidence(
        0.92f,
        0.92f,
        true,
        "test",
        reid,
        new BboxContinuityEvidence(0f, 0f, 1f, 0f, "test"),
        5,
        0,
        person,
        1,
        1,
        -1,
        1,
        10,
        0,
        0.92f,
        0f,
        0f,
        0f,
        0f,
        5,
        0,
        "test");
  }

  private static Detector.Recognition person() {
    return new Detector.Recognition("1", "person", 0.95f, new RectF(20, 20, 70, 180), 0);
  }
}
