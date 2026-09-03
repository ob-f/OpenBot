package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class FrameObservationConsistencyTest {
  @Test
  public void acceptedImageDoesNotChangeWhenCameraReusesBuffer() {
    Bitmap camera = Bitmap.createBitmap(80, 160, Bitmap.Config.ARGB_8888);
    camera.eraseColor(Color.RED);
    Bitmap accepted = BaseCartFollowFragment.copyInferenceFrame(camera);
    camera.eraseColor(Color.BLUE);
    assertEquals(Color.RED, accepted.getPixel(40, 80));
    assertEquals(Color.BLUE, camera.getPixel(40, 80));
    accepted.recycle();
    assertFalse(camera.isRecycled());
    camera.recycle();
  }

  @Test
  public void allRotationsDescribeSameDisplayedRightSideBox() {
    RectF[] source = {
      new RectF(70, 20, 100, 180),
      new RectF(20, 0, 180, 30),
      new RectF(0, 20, 30, 180),
      new RectF(20, 70, 180, 100)
    };
    for (int i = 0; i < source.length; i++) {
      boolean rotated = i % 2 == 1;
      RectF display =
          TargetObservationEvidence.toScreen(
              source[i], rotated ? 200 : 100, rotated ? 100 : 200, i * 90);
      assertEquals(.7f, display.left, .0001f);
      assertEquals(1f, display.right, .0001f);
      assertEquals(.1f, display.top, .0001f);
      assertEquals(.9f, display.bottom, .0001f);
    }
  }

  @Test
  public void observationCopiesMutableDetectionBox() {
    RectF detection = new RectF(.5f, 0f, 1f, 1f);
    TargetObservationEvidence observation =
        new TargetObservationEvidence(detection, 7, 100L, .8f, false, true, 1, "locked_detection");
    detection.setEmpty();
    assertEquals(.5f, observation.screenBox.left, 0f);
    assertEquals(1f, observation.screenBox.bottom, 0f);
  }

  @Test
  public void sourceGenerationCannotBeRecapturedAfterSessionRestart() {
    long admittedGeneration = 4L;
    long admittedSequence = 12L;
    assertFalse(
        BaseCartFollowFragment.shouldApplyUiSnapshot(admittedGeneration, 5L, admittedSequence, 0L));
    assertFalse(BaseCartFollowFragment.shouldApplyUiSnapshot(5L, 5L, admittedSequence, 13L));
  }

  @Test
  public void directedSearchUsesGlobalRecoveryAndExpandedReidBudget() {
    assertTrue(BaseCartFollowFragment.isRecoveryState(FollowState.DIRECTED_REACQUIRE));
    assertTrue(ReIDCoordinator.isRecoveryState(FollowState.DIRECTED_REACQUIRE));
    assertFalse(BaseCartFollowFragment.isRecoveryState(FollowState.FOLLOW));
    assertFalse(ReIDCoordinator.isRecoveryState(FollowState.FOLLOW));
  }

  @Test
  public void selectedLockedTrackCannotBorrowAnotherPersonsReid() throws Exception {
    org.openbot.tflite.Detector.Recognition locked =
        new org.openbot.tflite.Detector.Recognition(
            "locked", "person", .95f, new RectF(10, 10, 50, 100), 0);
    org.openbot.tflite.Detector.Recognition other =
        new org.openbot.tflite.Detector.Recognition(
            "other", "person", .95f, new RectF(70, 10, 100, 100), 0);
    ReIDMatchResult match = new ReIDMatchResult(.95f, .1f, 1, 8, true, 1, "fresh");
    IdentityEvidence base =
        new IdentityEvidence(.95f, .95f, true, "scored_other", match, null, 1, 0, other);
    IdentityBeliefAccumulator accumulator = new IdentityBeliefAccumulator();
    accumulator.setStrictReidProvenance(true);
    accumulator.lockTrack(7);
    java.lang.reflect.Method method =
        IdentityBeliefAccumulator.class.getDeclaredMethod(
            "withBelief",
            IdentityEvidence.class,
            TargetTrack.class,
            BboxContinuityEvidence.class,
            IdentityBelief.class,
            int.class,
            int.class,
            int.class,
            int.class,
            String.class);
    method.setAccessible(true);
    IdentityEvidence result =
        (IdentityEvidence)
            method.invoke(
                accumulator,
                base,
                new TargetTrack(7, locked, 100L),
                null,
                null,
                7,
                7,
                -1,
                2,
                "locked_selected");
    assertSame(locked, result.bestCandidate);
    assertFalse(result.reidAvailable());
  }
}
