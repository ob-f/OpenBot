package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.graphics.RectF;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TargetTrackManagerLowConfidenceTest {
  @Test
  public void lowConfidenceDetectionOnlyContinuesExistingLockedTrack() {
    TargetTrackManager manager = new TargetTrackManager();
    Detector.Recognition high = person(0.9f, 20f);
    manager.update(Collections.singletonList(high), 100, 200, 1000L);
    manager.lockClosest(high.getLocation());
    int trackId = manager.getLockedTrackId();

    Detector.Recognition low = person(0.2f, 22f);
    TargetTrackManager.TwoStageUpdateResult result =
        manager.updateWithLowConfidence(
            Collections.emptyList(), Collections.singletonList(low), 100, 200, 1033L);

    assertEquals(1, result.continuedLowConfidence.size());
    assertSame(low, result.continuedLowConfidence.get(0));
    assertEquals(trackId, manager.getTrackForRecognition(low).trackId);
    assertEquals(1, manager.getTracks().size());
  }

  @Test
  public void lowConfidenceDetectionDoesNotCreateNewTrack() {
    TargetTrackManager manager = new TargetTrackManager();
    TargetTrackManager.TwoStageUpdateResult result =
        manager.updateWithLowConfidence(
            Collections.emptyList(), Collections.singletonList(person(0.2f, 70f)), 100, 200, 1000L);
    assertEquals(0, result.continuedLowConfidence.size());
    assertEquals(0, manager.getTracks().size());
  }

  private static Detector.Recognition person(float confidence, float left) {
    return new Detector.Recognition(
        "1", "person", confidence, new RectF(left, 10, left + 30, 170), 0);
  }
}
