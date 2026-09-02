package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ManualSpeedProfileTest {
  @Test
  public void stagedForwardLevelsMatchVelocityFirmware() {
    assertEquals(103, ManualSpeedProfile.estimatedMmps(9));
    assertEquals(160, ManualSpeedProfile.estimatedMmps(14));
    assertEquals(206, ManualSpeedProfile.estimatedMmps(18));
    assertEquals(240, ManualSpeedProfile.estimatedMmps(21));
  }

  @Test
  public void forwardLogicalIsClampedToFirmwareRange() {
    assertEquals(9, ManualSpeedProfile.clampForward(1));
    assertEquals(14, ManualSpeedProfile.clampForward(14));
    assertEquals(21, ManualSpeedProfile.clampForward(255));
  }

  @Test
  public void reverseUsesExistingSafetyRatio() {
    assertEquals(8, ManualSpeedProfile.reverseForForward(9));
    assertEquals(12, ManualSpeedProfile.reverseForForward(14));
    assertEquals(15, ManualSpeedProfile.reverseForForward(18));
    assertEquals(18, ManualSpeedProfile.reverseForForward(21));
  }
}
