package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Regression coverage for retrospective learning from a continuous, single-person pose change. */
@RunWith(RobolectricTestRunner.class)
public class DeferredGallerySegmentTest {
  private static final long START = 1000L;
  private final DeferredGallerySegment segment = new DeferredGallerySegment();
  private final List<float[]> anchors = new ArrayList<>(Collections.singletonList(vector(1f)));
  private final List<float[]> adaptive = new ArrayList<>();

  @Test
  public void continuousPoseChangeApprovesAfterThreeFreshSamplesAndSixHundredMs() {
    assertEquals("continuous_segment_pending", offer(0, .70f).reason);
    assertEquals("continuous_segment_pending", offer(300, .72f).reason);
    DeferredGallerySegment.Result approved = offer(600, .74f);
    assertEquals("approved", approved.reason);
    assertEquals(3, approved.approvedSamples.size());
    assertEquals(START, approved.approvedSamples.get(0).timestampMs);
    assertEquals(START + 600, approved.approvedSamples.get(2).timestampMs);
    assertEquals(700L, approved.usableAfterFrameSequence);
    assertEquals(0, segment.size(START + 600));
  }

  @Test
  public void strongInitialAppearanceDoesNotBlockContinuousLearning() {
    assertEquals("strong_segment_pending", offer(0, .95f).reason);
    offer(300, .90f);
    assertEquals("approved", offer(600, .86f).reason);
  }

  @Test
  public void poseSegmentNeedsLocalContinuityButNotAnchorScore() {
    assertEquals("context_invalid", offer(0, .75f, .75f, 0f, false).reason);
    assertEquals("continuous_segment_pending", offer(0, .59f, .59f, 0f, true).reason);
    assertEquals("continuous_segment_pending", offer(200, .61f).reason);
  }

  @Test
  public void discontinuityOrTrackChangeDiscardsWholePendingSegment() {
    offer(0, .75f);
    assertEquals("feature_discontinuity", offer(300, -.75f, .75f, 0f, true).reason);
    assertEquals(0, segment.size(START + 300));
    offer(0, .75f);
    DeferredGallerySegment.Result changed =
        segment.offer(
            7,
            11,
            new DeferredGallerySegment.Sample(301, 400, START + 300, 5, vector(.75f), null),
            true,
            true,
            .75f,
            0f,
            anchors,
            adaptive,
            START + 300);
    assertEquals("track_changed", changed.reason);
    assertEquals(0, segment.size(START + 300));
  }

  @Test
  public void cachedOrOutOfOrderEvidenceCannotAdvanceLearning() {
    offer(0, .75f);
    DeferredGallerySegment.Sample first = sample(0, .75f);
    assertEquals(
        "cached",
        segment.offer(7, 11, first, false, true, .75f, 0f, anchors, adaptive, START + 100).reason);
    assertEquals(
        "repeated_or_out_of_order",
        segment.offer(7, 11, first, true, true, .75f, 0f, anchors, adaptive, START + 100).reason);
    assertEquals(1, segment.size(START + 100));
  }

  @Test
  public void samplingUsesSourceTimeAndPreservesCopies() {
    float[] feature = vector(.75f);
    RectF bbox = new RectF(1, 2, 40, 90);
    DeferredGallerySegment.Sample first =
        new DeferredGallerySegment.Sample(1, 100, START, 4, feature, bbox);
    feature[0] = -1f;
    bbox.left = 999f;
    assertEquals(
        "continuous_segment_pending",
        segment.offer(7, 11, first, true, true, .75f, 0f, anchors, adaptive, START).reason);
    offer(300, .75f);
    DeferredGallerySegment.Result approved = offer(600, .75f);
    assertEquals("approved", approved.reason);
    assertEquals(.75f, approved.approvedSamples.get(0).feature[0], .00001f);
    assertEquals(1f, approved.approvedSamples.get(0).bbox.left, 0f);
  }

  @Test
  public void segmentExpiresAndInvalidInputsFailClosed() {
    offer(0, .75f);
    assertEquals("segment_timeout", offer(5000, .75f).reason);
    assertEquals(0, segment.size(START + 5000));
    assertEquals(
        "invalid_sample",
        segment.offer(
                7,
                11,
                new DeferredGallerySegment.Sample(1, 100, START, 4, new float[] {0, 0}, null),
                true,
                true,
                .75f,
                0f,
                anchors,
                adaptive,
                START)
            .reason);
  }

  private DeferredGallerySegment.Result offer(int offset, float score) {
    return offer(offset, score, score, 0f, true);
  }

  private DeferredGallerySegment.Result offer(
      int offset, float featureScore, float rawScore, float competitor, boolean context) {
    return segment.offer(
        7,
        11,
        sample(offset, featureScore),
        true,
        context,
        rawScore,
        competitor,
        anchors,
        adaptive,
        START + offset);
  }

  private static DeferredGallerySegment.Sample sample(int offset, float score) {
    return new DeferredGallerySegment.Sample(
        offset + 1, offset + 100, START + offset, 4, vector(score), null);
  }

  private static float[] vector(float score) {
    return new float[] {score, (float) Math.sqrt(1d - (double) score * score)};
  }
}
