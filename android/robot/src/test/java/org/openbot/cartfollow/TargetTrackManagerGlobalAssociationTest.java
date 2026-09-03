package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.RectF;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TargetTrackManagerGlobalAssociationTest {
  @Test
  public void globalAssociationPreservesBestOneToOneAssignmentWhenDetectionOrderIsAmbiguous() {
    TargetTrackManager manager = new TargetTrackManager();
    manager.setGlobalAssociationEnabled(true);

    Detector.Recognition left = person("left", 10f);
    Detector.Recognition right = person("right", 40f);
    manager.update(Arrays.asList(left, right), 100, 100, 1000L);
    int leftTrack = manager.getTrackForRecognition(left).trackId;
    int rightTrack = manager.getTrackForRecognition(right).trackId;

    // This ambiguous detection arrives first, before the exact match for the left track.
    Detector.Recognition middle = person("middle", 25f);
    Detector.Recognition exactLeft = person("exact-left", 10f);
    manager.update(Arrays.asList(middle, exactLeft), 100, 100, 1100L);

    TargetTrack exactLeftTrack = manager.getTrackForRecognition(exactLeft);
    TargetTrack middleTrack = manager.getTrackForRecognition(middle);
    assertNotNull(exactLeftTrack);
    assertNotNull(middleTrack);
    assertEquals(leftTrack, exactLeftTrack.trackId);
    assertEquals(rightTrack, middleTrack.trackId);
  }

  private static Detector.Recognition person(String id, float left) {
    return new Detector.Recognition(id, "person", 0.95f, new RectF(left, 20f, left + 20f, 80f), 0);
  }
}
