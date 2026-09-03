package org.openbot.cartfollow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GlobalReacquireGateTest {
  @Test
  public void requiresThreeFreshStrongMatchesForSameTrack() {
    GlobalReacquireGate gate = new GlobalReacquireGate();
    ReIDMatchResult strong = new ReIDMatchResult(0.90f, 0.80f, 0, 8, true, 1, "fresh");
    assertFalse(gate.update(4, false, strong, 100));
    assertFalse(gate.update(4, false, strong, 200));
    assertTrue(gate.update(4, false, strong, 300));
  }

  @Test
  public void cachedResultAndCandidateSwitchDoNotAdvanceGate() {
    GlobalReacquireGate gate = new GlobalReacquireGate();
    ReIDMatchResult strong = new ReIDMatchResult(0.90f, 0.80f, 0, 8, true, 1, "fresh");
    assertFalse(gate.update(4, false, strong, 100));
    assertFalse(gate.update(4, false, strong, 100));
    assertFalse(gate.update(5, false, strong, 200));
    assertFalse(gate.update(5, false, strong, 300));
    assertTrue(gate.update(5, false, strong, 400));
  }

  @Test
  public void visibleLockOrWeakEvidenceResetsGate() {
    GlobalReacquireGate gate = new GlobalReacquireGate();
    ReIDMatchResult strong = new ReIDMatchResult(0.90f, 0.80f, 0, 8, true, 1, "fresh");
    ReIDMatchResult weak = new ReIDMatchResult(0.82f, 0.80f, 0, 8, true, 1, "fresh");
    assertFalse(gate.update(4, false, strong, 100));
    assertFalse(gate.update(4, true, strong, 200));
    assertFalse(gate.update(4, false, weak, 300));
    assertFalse(gate.update(4, false, strong, 400));
  }
}
