package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector.Recognition;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReIDSessionIsolationTest {
  @Test
  public void expiredEpochBeforeInitializationCallSkipsExtractionAndPreservesNewPendingGallery() {
    try (Fixture f = new Fixture()) {
      long acceptedFrameEpoch = f.coordinator.getSessionEpoch();
      f.coordinator.reset();
      assertNotEquals(acceptedFrameEpoch, f.coordinator.getSessionEpoch());
      f.extractor.feature = new float[] {0f, 1f};
      f.collect();
      int callsBefore = f.extractor.calls;
      f.extractor.feature = new float[] {1f, 0f};
      f.coordinator.collectInitializationCandidate(
          f.source, f.person, 0, android.os.SystemClock.elapsedRealtime(), acceptedFrameEpoch);
      assertEquals(callsBefore, f.extractor.calls);
      f.coordinator.confirmGallery();
      assertEquals(1, f.coordinator.getGallerySize());
      IdentityEvidence oldAppearance = f.evaluate(1000, Collections.singletonList(f.person));
      assertEquals(0f, oldAppearance.reidMatch.anchorScore, .0001f);
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void expiredEpochBeforeEvaluationCallSkipsExtractionAndCannotAuthorizeAcceptedOldFrame() {
    try (Fixture f = new Fixture()) {
      f.initialize();
      long acceptedFrameEpoch = f.coordinator.getSessionEpoch();
      f.coordinator.reset();
      f.extractor.feature = new float[] {0f, 1f};
      f.initialize();
      long currentEpoch = f.coordinator.getSessionEpoch();
      assertNotEquals(acceptedFrameEpoch, currentEpoch);
      List<Recognition> people = Collections.singletonList(f.person);
      f.tracks.update(people, 400, 600, 1000);
      f.tracks.lockClosest(f.person.getLocation());
      f.coordinator.setFrameContext(f.tracks, 1000, ++f.sequence);
      int callsBefore = f.extractor.calls;
      IdentityEvidence stale =
          f.coordinator.evaluate(
              people,
              f.source,
              f.memory,
              FollowState.FOLLOW,
              400,
              600,
              0,
              .99f,
              true,
              f.person,
              acceptedFrameEpoch);
      assertEquals(callsBefore, f.extractor.calls);
      assertInvalidated(f, stale, f.tracks.getLockedTrackId());
      assertEquals(1, f.coordinator.getGallerySize());
      IdentityEvidence current =
          f.coordinator.evaluate(
              people,
              f.source,
              f.memory,
              FollowState.FOLLOW,
              400,
              600,
              0,
              .99f,
              true,
              f.person,
              currentEpoch);
      assertEquals(callsBefore + 1, f.extractor.calls);
      assertTrue(current.reidMatch.reidAvailable);
      assertEquals(1f, current.reidMatch.anchorScore, .0001f);
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void resetDuringInitializationCannotAppendOldFeatureOrRecycleSourceFrame() {
    try (Fixture f = new Fixture()) {
      f.extractor.afterCalls(1, f.coordinator::reset);
      Recognition fullFrame = new Recognition("full", "person", .95f, new RectF(0, 0, 400, 600), 0);
      f.coordinator.collectInitializationCandidate(f.source, fullFrame, 0);
      f.coordinator.confirmGallery();
      assertEquals(0, f.coordinator.getGallerySize());
      assertFalse(f.coordinator.getLastResult().reidAvailable);
      assertNull(f.coordinator.getLastBestCandidate());
      f.assertExtractedCropsReleased();
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void oldInitializationCannotJoinNewPendingOrAlreadyConfirmedGallery() {
    for (boolean confirmInsideCallback : new boolean[] {false, true}) {
      try (Fixture f = new Fixture()) {
        f.extractor.afterCalls(
            1,
            () -> {
              f.coordinator.reset();
              f.extractor.feature = new float[] {0f, 1f};
              f.collect();
              if (confirmInsideCallback) f.coordinator.confirmGallery();
            });
        f.collect();
        f.coordinator.confirmGallery();
        assertEquals(1, f.coordinator.getGallerySize());
        assertEquals(1, f.coordinator.getGalleryStatus().anchorSize);
        f.assertExtractedCropsReleased();
        // The old in-flight feature is orthogonal to the only valid new-session anchor.
        f.extractor.feature = new float[] {1f, 0f};
        IdentityEvidence oldAppearance = f.evaluate(1000, Collections.singletonList(f.person));
        assertTrue(oldAppearance.reidMatch.reidAvailable);
        assertEquals(0f, oldAppearance.reidMatch.anchorScore, .0001f);
        assertFalse(oldAppearance.reidMatch.strongOk);
        f.extractor.feature = new float[] {0f, 1f};
        IdentityEvidence newAppearance = f.evaluate(1300, Collections.singletonList(f.person));
        assertEquals(1f, newAppearance.reidMatch.anchorScore, .0001f);
        f.assertSourceOwnedByCaller();
      }
    }
  }

  @Test
  public void confirmationDuringInitializationInvalidatesTheInFlightAppend() {
    try (Fixture f = new Fixture()) {
      f.extractor.feature = new float[] {0f, 1f};
      f.collect();
      f.extractor.feature = new float[] {1f, 0f};
      f.extractor.afterCalls(1, f.coordinator::confirmGallery);
      f.collect();
      f.coordinator.confirmGallery();
      assertEquals(1, f.coordinator.getGallerySize());
      IdentityEvidence result = f.evaluate(1000, Collections.singletonList(f.person));
      assertEquals(0f, result.reidMatch.anchorScore, .0001f);
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void galleryModeChangeDuringInitializationInvalidatesTheInFlightAppend() {
    try (Fixture f = new Fixture()) {
      f.extractor.afterCalls(
          1, () -> f.coordinator.setGalleryMode(GalleryUpdateStatus.Mode.STATIC));
      f.collect();
      f.coordinator.confirmGallery();
      assertEquals(0, f.coordinator.getGallerySize());
      f.assertExtractedCropsReleased();
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void resetDuringEvaluationClearsPreviouslyPublishedIdentityAndGlobalScores() {
    try (Fixture f = new Fixture()) {
      f.initialize();
      IdentityEvidence first = f.evaluate(1000, Collections.singletonList(f.person));
      int track = f.tracks.getLockedTrackId();
      assertTrue(first.reidMatch.reidAvailable);
      assertSame(f.person, f.coordinator.getLastBestCandidate());
      assertNotNull(f.coordinator.getGlobalScoredTrack(track));
      f.extractor.afterCalls(1, f.coordinator::reset);
      IdentityEvidence interrupted = f.evaluate(1300, Collections.singletonList(f.person));
      assertInvalidated(f, interrupted, track);
      assertEquals(0, f.coordinator.getGallerySize());
      f.assertExtractedCropsReleased();
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void resetOnSecondCandidateDiscardsAllPartiallyComputedScoresAndCrops() {
    try (Fixture f = new Fixture()) {
      f.initialize();
      f.evaluate(1000, Collections.singletonList(f.person));
      int originalTrack = f.tracks.getLockedTrackId();
      Recognition second = person("second", 280);
      int before = f.extractor.calls;
      f.extractor.afterCalls(2, f.coordinator::reset);
      IdentityEvidence interrupted = f.evaluate(1300, Arrays.asList(f.person, second));
      assertEquals(before + 2, f.extractor.calls);
      assertInvalidated(f, interrupted, originalTrack);
      assertNull(
          f.coordinator.getGlobalScoredTrack(f.tracks.getTrackForRecognition(second).trackId));
      f.assertExtractedCropsReleased();
      f.assertSourceOwnedByCaller();
    }
  }

  @Test
  public void resetAndNewConfirmationDuringEvaluationCannotPublishOldSessionEvidence() {
    try (Fixture f = new Fixture()) {
      f.initialize();
      f.extractor.afterCalls(
          1,
          () -> {
            f.coordinator.reset();
            f.extractor.feature = new float[] {0f, 1f};
            f.collect();
            f.coordinator.confirmGallery();
          });
      IdentityEvidence interrupted = f.evaluate(1000, Collections.singletonList(f.person));
      assertInvalidated(f, interrupted, f.tracks.getLockedTrackId());
      assertEquals(1, f.coordinator.getGallerySize());
      f.assertExtractedCropsReleased();
      f.extractor.feature = new float[] {1f, 0f};
      IdentityEvidence oldAppearance = f.evaluate(1300, Collections.singletonList(f.person));
      assertEquals(0f, oldAppearance.reidMatch.anchorScore, .0001f);
      f.extractor.feature = new float[] {0f, 1f};
      IdentityEvidence newAppearance = f.evaluate(1600, Collections.singletonList(f.person));
      assertEquals(1f, newAppearance.reidMatch.anchorScore, .0001f);
      assertTrue(newAppearance.reidMatch.strongOk);
      f.assertSourceOwnedByCaller();
    }
  }

  private static void assertInvalidated(Fixture f, IdentityEvidence result, int oldTrack) {
    assertFalse(result.matched);
    assertTrue(result.reidMatch == null || !result.reidMatch.reidAvailable);
    assertNull(result.bestCandidate);
    assertFalse(f.coordinator.getLastResult().reidAvailable);
    assertNull(f.coordinator.getLastBestCandidate());
    assertNull(f.coordinator.getGlobalScoredTrack(oldTrack));
  }

  private static Recognition person(String id, float left) {
    return new Recognition(id, "person", .95f, new RectF(left, 30, left + 70, 330), 0);
  }

  private static final class Fixture implements AutoCloseable {
    final CallbackExtractor extractor = new CallbackExtractor();
    final ReIDCoordinator coordinator = new ReIDCoordinator(extractor);
    final Bitmap source = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
    final Recognition person = person("original", 30);
    final TargetTrackManager tracks = new TargetTrackManager();
    final TargetMemory memory = new TargetMemory();
    long sequence;

    Fixture() {
      source.eraseColor(0xff123456);
      coordinator.setGalleryMode(GalleryUpdateStatus.Mode.ADAPTIVE);
      coordinator.setEnhancedRecovery(true);
      memory.updateDynamic(person);
    }

    void collect() {
      coordinator.collectInitializationCandidate(source, person, 0);
    }

    void initialize() {
      collect();
      coordinator.confirmGallery();
      assertEquals(1, coordinator.getGallerySize());
    }

    IdentityEvidence evaluate(long now, List<Recognition> people) {
      tracks.update(people, 400, 600, now);
      if (tracks.getLockedTrackId() < 0) tracks.lockClosest(person.getLocation());
      coordinator.setFrameContext(tracks, now, ++sequence);
      return coordinator.evaluate(
          people, source, memory, FollowState.FOLLOW, 400, 600, 0, .9f, true, person);
    }

    void assertSourceOwnedByCaller() {
      assertFalse(source.isRecycled());
      assertEquals(0xff123456, source.getPixel(200, 300));
      for (Bitmap crop : extractor.crops) assertNotSame(source, crop);
    }

    void assertExtractedCropsReleased() {
      assertFalse(extractor.crops.isEmpty());
      for (Bitmap crop : extractor.crops) assertTrue(crop.isRecycled());
    }

    @Override
    public void close() {
      coordinator.reset();
      source.recycle();
    }
  }

  private static final class CallbackExtractor implements ReIDFeatureExtractor {
    final List<Bitmap> crops = new ArrayList<>();
    float[] feature = {1f, 0f};
    int calls;
    int callbackCall = -1;
    Runnable callback;

    void afterCalls(int count, Runnable action) {
      callbackCall = calls + count;
      callback = action;
    }

    @Override
    public float[] extract(Bitmap crop) {
      crops.add(crop);
      float[] inFlightFeature = feature.clone();
      if (++calls == callbackCall) {
        Runnable action = callback;
        callback = null;
        callbackCall = -1;
        action.run();
      }
      assertFalse("An in-flight extraction still owns its crop", crop.isRecycled());
      return inFlightFeature;
    }
  }
}
