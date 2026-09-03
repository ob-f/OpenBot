package org.openbot.cartfollow;

import static org.junit.Assert.*;

import org.junit.Test;

public class YawTurnTrackerTest {
  @Test
  public void reverseRotationReducesNetAngleIncludingBelowZero() {
    YawTurnTracker t = ready();
    t.reset(SteeringEvidence.Direction.RIGHT);
    t.onGyroscope(1_000_000_000L, 0, 0, -1);
    t.onGyroscope(1_100_000_000L, 0, 0, -1);
    assertEquals(5.73f, t.getTurnedDegrees(), .1f);
    t.onGyroscope(1_200_000_000L, 0, 0, 1);
    assertEquals(0f, t.getTurnedDegrees(), .1f);
    assertTrue(t.isWrongDirection());
    t.onGyroscope(1_300_000_000L, 0, 0, 1);
    assertEquals(-5.73f, t.getTurnedDegrees(), .1f);
  }

  @Test
  public void tiltedGravityAndMirroredLeftTurn() {
    YawTurnTracker t = ready();
    t.onGravity(0, 9.81f, 0);
    t.reset(SteeringEvidence.Direction.LEFT);
    t.onGyroscope(1_000_000_000L, 100, 1, 100);
    t.onGyroscope(1_100_000_000L, 100, 1, 100);
    assertEquals(5.73f, t.getTurnedDegrees(), .1f);
  }

  @Test
  public void idleSamplesAndResetPreserveAvailability() {
    YawTurnTracker t = ready();
    t.onGyroscope(1_000_000_000L, 0, 0, 1);
    assertTrue(t.getStatus(1_500_000_000L).available);
    assertEquals(0, t.getTurnedDegrees(), 0);
    t.reset(SteeringEvidence.Direction.RIGHT);
    assertTrue(t.getStatus(1_500_000_000L).available);
    assertFalse(t.getStatus(1_500_000_001L).sampleFresh);
  }

  @Test
  public void distinguishesExistenceRegistrationGravityAndFreshness() {
    YawTurnTracker t = new YawTurnTracker();
    assertFalse(t.getStatus(0).sensorExists);
    t.setSensorStatus(true, false);
    assertTrue(t.getStatus(0).sensorExists);
    assertFalse(t.getStatus(0).registered);
    t.setSensorStatus(true, true);
    t.onGyroscope(100, 0, 0, 1);
    assertTrue(t.getStatus(100).sampleFresh);
    assertFalse(t.getStatus(100).gravityAvailable);
    t.onGravity(0, 0, 9.81f);
    assertTrue(t.getStatus(100).available);
    t.setSensorStatus(true, false);
    assertFalse(t.isAvailable());
  }

  @Test
  public void gapsDuplicatesAndNonfiniteSamplesDoNotIntegrate() {
    YawTurnTracker t = ready();
    t.reset(SteeringEvidence.Direction.RIGHT);
    t.onGyroscope(1_000_000_000L, 0, 0, 1);
    t.onGyroscope(2_000_000_000L, 0, 0, 1);
    t.onGyroscope(2_000_000_000L, 0, 0, 1);
    t.onGyroscope(1_500_000_000L, 0, 0, 1);
    t.onGyroscope(2_100_000_000L, 0, 0, Float.NaN);
    assertEquals(0, t.getTurnedDegrees(), 0);
    assertFalse(t.getStatus(2_600_000_000L).available);
  }

  private static YawTurnTracker ready() {
    YawTurnTracker t = new YawTurnTracker();
    t.setSensorStatus(true, true);
    t.onGravity(0, 0, 9.81f);
    return t;
  }

  @Test
  public void physicalLeftAndRightKeepTheirMeaningAcrossPhoneOrientations() {
    float[][] axes = {{0, 1, 0}, {1, 0, 0}, {0, -1, 0}, {-1, 0, 0}, {0, 0, 1}};
    for (float[] axis : axes) {
      for (SteeringEvidence.Direction direction :
          new SteeringEvidence.Direction[] {
            SteeringEvidence.Direction.LEFT, SteeringEvidence.Direction.RIGHT
          }) {
        YawTurnTracker tracker = ready();
        tracker.onGravity(axis[0] * 9.81f, axis[1] * 9.81f, axis[2] * 9.81f);
        tracker.reset(direction);
        float sign = direction == SteeringEvidence.Direction.LEFT ? 1f : -1f;
        tracker.onGyroscope(1_000_000_000L, axis[0] * sign, axis[1] * sign, axis[2] * sign);
        tracker.onGyroscope(1_100_000_000L, axis[0] * sign, axis[1] * sign, axis[2] * sign);
        assertEquals(5.73f, tracker.getTurnedDegrees(), .1f);
        assertFalse(tracker.isWrongDirection());
        tracker.onGyroscope(1_200_000_000L, -axis[0] * sign, -axis[1] * sign, -axis[2] * sign);
        assertEquals(0f, tracker.getTurnedDegrees(), .1f);
        assertTrue(tracker.isWrongDirection());
      }
    }
  }

  @Test
  public void staleGravityCannotBeMaskedByFreshGyroscope() {
    YawTurnTracker t = ready();
    t.onGravity(1_000_000_000L, 0f, 0f, 9.81f);
    t.onGyroscope(1_600_000_000L, 0f, 0f, 0f);
    YawTurnTracker.Status status = t.getStatus(1_600_000_000L);
    assertTrue(status.sampleFresh);
    assertFalse(status.gravityAvailable);
    assertFalse(status.available);
  }
}
