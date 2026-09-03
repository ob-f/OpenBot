package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MultiPersonContinuityTest {
  private SimulatorIdentityGuard started() {
    SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
    guard.begin(1);
    for (int i = 1; i <= 3; i++) SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .99f);
    return guard;
  }

  private ReIDMatchResult score(int track, long frame, long now, long observation, float value) {
    return new ReIDMatchResult(value, 0, 0, 8, true, 0, "fresh", value, 0, observation)
        .withBinding(track, now, frame, true, 0, false);
  }

  private SimulatorIdentityGuard.Decision step(
      SimulatorIdentityGuard guard, long frame, long now, int people) {
    return guard.update(
        1,
        frame,
        now,
        now,
        1,
        1,
        true,
        true,
        null,
        people,
        false,
        false,
        null,
        new SimulatorContinuityTracker.Evidence(
            true, "continuous_observations", 3, null, new RectF(100, 100, 180, 280)),
        true);
  }

  @Test
  public void unambiguousPasserByMovesAndOnlyLearningFreezes() {
    SimulatorIdentityGuard guard = started();
    for (int i = 4; i < 20; i++) {
      long now = i * 300;
      guard.inspectCandidates(
          Arrays.asList(score(1, i, now, i, .99f), score(2, i, now, i, .1f)), 2, 1, i, now);
      SimulatorIdentityGuard.Decision result = step(guard, i, now, 2);
      assertTrue(result.reason, result.motionAllowed);
      assertFalse(result.samplingAllowed);
    }
  }

  @Test
  public void twoIndependentChecksStopButCachedCheckCannotCountTwice() {
    SimulatorIdentityGuard guard = started();
    guard.inspectCandidates(
        Arrays.asList(score(1, 4, 1200, 4, .5f), score(2, 4, 1200, 4, .95f)), 2, 1, 4, 1200);
    assertTrue(step(guard, 4, 1200, 2).motionAllowed);
    assertTrue(step(guard, 5, 1500, 2).motionAllowed);
    guard.inspectCandidates(
        Arrays.asList(score(1, 6, 1800, 4, .5f), score(2, 6, 1800, 4, .95f)), 2, 1, 6, 1800);
    assertTrue(step(guard, 6, 1800, 2).motionAllowed);
    guard.inspectCandidates(
        Arrays.asList(score(1, 7, 2000, 7, .5f), score(2, 7, 2000, 7, .95f)), 2, 1, 7, 2000);
    assertEquals("identity_conflict", step(guard, 7, 2000, 2).reason);
  }

  @Test
  public void incompleteChecksTimeoutAndOverBudgetStopsImmediately() {
    SimulatorIdentityGuard guard = started();
    for (int i = 4; i <= 7; i++) assertTrue(step(guard, i, i * 300, 2).motionAllowed);
    assertEquals("multi_check_timeout", step(guard, 8, 2400, 2).reason);
    assertEquals("candidate_budget_exceeded", step(started(), 4, 1200, 6).reason);
  }

  @Test
  public void featureExtractionFailureCannotStopUniqueMeasuredSingleTarget() {
    SimulatorIdentityGuard guard = started();
    for (int i = 4; i < 100; i++) {
      SimulatorIdentityGuard.Decision result = step(guard, i, 900 + (i - 3) * 33, 1);
      assertTrue(result.motionAllowed);
      assertEquals(900, result.identityEvidenceTimeMs);
    }
  }
}
