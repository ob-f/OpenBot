package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class IdentityCandidateSetTest {
  private static Detector.Recognition person(String id, float confidence, RectF box) {
    return new Detector.Recognition(id, "person", confidence, box, 0);
  }

  @Test
  public void persistentUnboundRawLowProposalsCannotCreateMultiPersonTimeout() {
    TargetTrackManager tracks = new TargetTrackManager();
    Detector.Recognition target = person("target-0", .92f, new RectF(100, 60, 220, 430));
    tracks.update(Collections.singletonList(target), 640, 480, 1000);
    tracks.lockClosest(target.getLocation());

    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int frame = 1; frame <= 3; frame++)
      SimulatorIdentityGuardTest.continuous(guard, frame, frame * 300L, .99f);

    boolean learned = false;
    for (int frame = 4; frame <= 320; frame++) {
      long now = 900L + (frame - 3) * 33L;
      Detector.Recognition measured = person("target-" + frame, .91f, new RectF(100, 60, 220, 430));
      List<Detector.Recognition> rawLow = new ArrayList<>();
      for (int n = 0; n < 6; n++)
        rawLow.add(
            person(
                "noise-" + frame + "-" + n,
                .26f,
                new RectF(300 + n * 8, 80 + n * 5, 330 + n * 8, 140 + n * 5)));
      TargetTrackManager.TwoStageUpdateResult update =
          tracks.updateWithLowConfidence(
              Collections.singletonList(measured), rawLow, 640, 480, now);
      IdentityCandidateSet candidates =
          IdentityCandidateSet.from(
              Collections.singletonList(measured), update.continuedLowConfidence, tracks);

      assertEquals(0, update.continuedLowConfidence.size());
      assertEquals(1, candidates.size());
      assertFalse(candidates.isMultiPerson());
      assertFalse(candidates.exceedsBudget());
      guard.inspectCandidates(
          Collections.emptyList(), candidates, tracks.getLockedTrackId(), frame, now);
      SimulatorIdentityGuard.Decision decision =
          guard.update(
              1,
              frame,
              now,
              now,
              tracks.getLockedTrackId(),
              tracks.getLockedTrackId(),
              true,
              true,
              null,
              candidates,
              false,
              false,
              null,
              new SimulatorContinuityTracker.Evidence(
                  true, "continuous_observations", 3, null, measured.getLocation()),
              true);
      assertTrue(decision.reason, decision.motionAllowed);
      assertNotEquals("multi_check_timeout", decision.reason);
      assertNotEquals("candidate_budget_exceeded", decision.reason);
      learned |= decision.samplingAllowed;
    }
    assertTrue("single measured target must remain eligible for learning", learned);
  }

  @Test
  public void successfullyContinuedLowDetectionRemainsAnIdentityCandidate() {
    TargetTrackManager tracks = new TargetTrackManager();
    Detector.Recognition high = person("high", .9f, new RectF(100, 60, 220, 430));
    tracks.update(Collections.singletonList(high), 640, 480, 1000);
    tracks.lockClosest(high.getLocation());
    Detector.Recognition low = person("low", .3f, new RectF(102, 62, 222, 432));
    TargetTrackManager.TwoStageUpdateResult update =
        tracks.updateWithLowConfidence(
            Collections.emptyList(), Collections.singletonList(low), 640, 480, 1033);
    IdentityCandidateSet candidates =
        IdentityCandidateSet.from(Collections.emptyList(), update.continuedLowConfidence, tracks);
    assertEquals(1, candidates.size());
    assertTrue(candidates.contains(low));
    assertEquals(tracks.getLockedTrackId(), candidates.trackId(low));
  }
}
