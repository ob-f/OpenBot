package org.openbot.cartfollow;

import static org.junit.Assert.*;

import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SimulatorContinuityTrackerTest {
  @Test
  public void justUpdatedLockedGhostCannotProvideItsOwnHistoricalSupport() {
    assertFalse(SimulatorContinuityTracker.hasHistoricalLocalSupport(false, false, 1, 1, true));
    assertTrue(SimulatorContinuityTracker.hasHistoricalLocalSupport(true, false, 1, 1, true));
    assertTrue(SimulatorContinuityTracker.hasHistoricalLocalSupport(false, true, 1, 1, true));
    assertTrue(SimulatorContinuityTracker.hasHistoricalLocalSupport(false, false, 2, 1, true));
    assertFalse(SimulatorContinuityTracker.hasHistoricalLocalSupport(false, false, -1, 1, true));
  }

  @Test
  public void jitterAndGradualCrouchKeepContinuity() {
    SimulatorContinuityTracker tracker = new SimulatorContinuityTracker();
    for (int i = 0; i < 10; i++) {
      SimulatorContinuityTracker.Evidence evidence =
          tracker.update(
              1,
              new RectF(100 + i % 2, 50 + i * 8, 200 + i % 2, 350 - i * 8),
              i + 1,
              100 + i * 100,
              120 + i * 100,
              true,
              true,
              false);
      assertEquals(i >= 2, evidence.reliable);
    }
  }

  @Test
  public void sameIdJumpCompetitionLowConfidenceAndTimeoutCannotMaintain() {
    for (int mode = 0; mode < 4; mode++) {
      SimulatorContinuityTracker tracker = new SimulatorContinuityTracker();
      for (int i = 1; i <= 3; i++)
        tracker.update(1, new RectF(100, 50, 200, 350), i, i * 100, i * 100, true, true, false);
      RectF box = mode == 0 ? new RectF(500, 50, 600, 350) : new RectF(100, 50, 200, 350);
      assertFalse(
          tracker.update(
                  1,
                  box,
                  4,
                  mode == 3 ? 900 : 400,
                  mode == 3 ? 900 : 400,
                  mode != 2,
                  true,
                  mode == 1)
              .reliable);
    }
  }
}
