package org.openbot.cartfollow;

import static org.junit.Assert.*;
import android.graphics.RectF;
import org.junit.Test;

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk = 28)
public class ShortRecoveryTest {
  private final SimulatorIdentityGuard guard = new SimulatorIdentityGuard();
  private final SimulatorContinuityTracker tracker = new SimulatorContinuityTracker();
  private long sequence;
  private static final RectF BOX = new RectF(100, 100, 180, 280);
  private void start() {
    guard.begin(1);
    for (int i = 1; i <= 3; i++) {
      sequence = i;
      tracker.observe(1, 1, BOX, i, i * 300, i * 300, 640, 480, true, false);
      SimulatorIdentityGuardTest.continuous(guard, i, i * 300, .99f);
    }
  }
  private SimulatorIdentityGuard.Decision observe(long time, RectF box, int count, int track) {
    sequence++;
    SimulatorContinuityTracker.Evidence e = tracker.observe(1, track, box, sequence, time, time,
        640, 480, box != null, count > 1);
    return guard.update(1, sequence, time, time, track, 1, box != null, true, null,
        count, count > 1, box == null, null, e, true);
  }
  @Test public void missingStopsAndThirdFreshReturnMovesAtFourteenWithoutLearning() {
    start();
    assertFalse(observe(1000, null, 0, 1).motionAllowed);
    assertFalse(observe(1300, BOX, 1, 1).motionAllowed);
    assertFalse(observe(1400, BOX, 1, 1).motionAllowed);
    SimulatorIdentityGuard.Decision recovered = observe(1500, BOX, 1, 1);
    assertTrue(recovered.reason, recovered.allowsForward(1500));
    assertEquals(14, recovered.tracking.maximumGear);
    assertFalse(recovered.samplingAllowed);
    assertFalse(observe(1600, BOX, 1, 1).samplingAllowed);
  }
  @Test public void missingFramesDoNotExtendOriginalDeadline() {
    start();
    observe(1000, null, 0, 1);
    observe(1500, null, 0, 1);
    assertFalse(observe(1800, BOX, 1, 1).motionAllowed);
    assertFalse(observe(1900, BOX, 1, 1).motionAllowed);
    assertFalse(observe(2000, BOX, 1, 1).motionAllowed);
  }
  @Test public void crowdBreaksRecoveryAndCannotBeForgottenNextFrame() {
    start(); observe(1000, null, 0, 1);
    assertFalse(observe(1200, BOX, 2, 1).motionAllowed);
    for (long t = 1300; t <= 1700; t += 100) assertFalse(observe(t, BOX, 1, 1).motionAllowed);
  }
  @Test public void changedTrackCannotBorrowOriginalIdentity() {
    start(); observe(1000, null, 0, 1);
    for (long t = 1200; t <= 1600; t += 100) assertFalse(observe(t, BOX, 1, 2).motionAllowed);
  }
  @Test public void spatialJumpCannotUseShortRecovery() {
    start(); observe(1000, null, 0, 1);
    RectF jumped = new RectF(500, 100, 580, 280);
    for (long t = 1200; t <= 1600; t += 100) assertFalse(observe(t, jumped, 1, 1).motionAllowed);
  }
  @Test public void missingInferenceFramesAlsoRequireThreeReturnObservations() {
    start();
    assertFalse(observe(1500, BOX, 1, 1).motionAllowed);
    assertFalse(observe(1600, BOX, 1, 1).motionAllowed);
    assertTrue(observe(1700, BOX, 1, 1).motionAllowed);
  }
}
