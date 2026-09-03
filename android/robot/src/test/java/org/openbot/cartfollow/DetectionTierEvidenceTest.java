package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DetectionTierEvidenceTest {
  @Test
  public void lowThresholdIsCappedForNormalHighThreshold() {
    assertEquals(0.25f, BaseCartFollowFragment.lowConfidenceThreshold(0.50f), 0f);
    assertEquals(0.20f, BaseCartFollowFragment.lowConfidenceThreshold(0.20f), 0f);
    assertEquals(0.08f, BaseCartFollowFragment.lowConfidenceThreshold(0.08f), 0f);
  }
}
