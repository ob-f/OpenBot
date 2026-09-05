package org.openbot.cartfollow;

import static org.junit.Assert.*;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.openbot.tflite.Detector.Recognition;
import org.openbot.vehicle.Control;

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk = 28)
public class VisualTraversabilityEstimatorTest {
  private static Recognition person(String id, RectF box) {
    return new Recognition(id, "person", .9f, box, 0);
  }
  private static FollowStateMachine.FrameResult frame(Recognition target, Recognition... people) {
    return new FollowStateMachine.FrameResult(FollowState.FOLLOW, new Control(0, 0),
        target, null, Arrays.asList(people), true, false, null, -1);
  }
  @Test public void loggedSidePersonDoesNotBlockRotatedPhone() {
    Recognition other = person("2", new RectF(547.948f, 513.8986f, 878.5023f, 621.0786f));
    assertFalse(VisualTraversabilityEstimator.estimate(frame(null, other), 1280, 720, 90).centerBlocked);
    assertTrue(VisualTraversabilityEstimator.estimate(frame(null, other), 1280, 720, 0).centerBlocked);
  }
  @Test public void uprightCenterBlocksAndSidesStayClearAtEveryOrientation() {
    for (int orientation : new int[] {0, 90, 180, 270}) {
      for (float x : new float[] {.1f, .4f, .75f}) {
        RectF normalized = new RectF(x, .3f, x + .15f, .95f);
        RectF raw = TargetObservationEvidence.toScreen(normalized, 1, 1, 360 - orientation);
        raw.set(raw.left * 1280, raw.top * 720, raw.right * 1280, raw.bottom * 720);
        assertEquals("rotation=" + orientation + " x=" + x, x == .4f,
            VisualTraversabilityEstimator.estimate(frame(null, person("x", raw)), 1280, 720, orientation).centerBlocked);
      }
    }
  }
  @Test public void copiedTargetDetectionIsExcludedButDifferentCentralPersonIsNot() {
    Recognition target = person("target", new RectF(400, 100, 700, 700));
    Recognition copy = person("target", new RectF(target.getLocation()));
    assertFalse(VisualTraversabilityEstimator.estimate(frame(target, copy), 1000, 800, 0).centerBlocked);
    assertTrue(VisualTraversabilityEstimator.estimate(
        frame(target, copy, person("other", new RectF(450, 200, 650, 750))), 1000, 800, 0).centerBlocked);
  }
  @Test public void boundTargetIsExcludedWithoutTargetObject() {
    Recognition target = person("target", new RectF(400, 100, 700, 700));
    TargetTrackManager tracks = new TargetTrackManager();
    tracks.update(Collections.singletonList(target), 1000, 800, 100);
    FollowStateMachine.FrameResult f = frame(null, target);
    f.identityCandidates = IdentityCandidateSet.from(Collections.singletonList(target), Collections.emptyList(), tracks);
    f.targetObservation = new TargetObservationEvidence(new RectF(.4f, .125f, .7f, .875f),
        f.identityCandidates.trackId(target), 100, .9f, false, true, 1, "test");
    assertFalse(VisualTraversabilityEstimator.estimate(f, 1000, 800, 0).centerBlocked);
  }
  @Test public void offscreenAreaCannotCreateFalseObstacle() {
    assertFalse(VisualTraversabilityEstimator.estimate(
        frame(null, person("outside", new RectF(400, 790, 700, 5000))), 1000, 800, 0).centerBlocked);
  }
}
