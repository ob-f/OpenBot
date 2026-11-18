package org.openbot.app.robot.pointGoalNavigation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ArrivalCheckTest {

  private ArrivalCheck check;

  @Before
  public void setUp() {
    check = new ArrivalCheck();
  }

  @Test
  public void farAway_notArrived() {
    assertFalse(check.hasArrived(2f, 0f));
  }

  @Test
  public void withinTenCentimeters_arrived() {
    assertTrue(check.hasArrived(0.09f, 0f));
  }

  @Test
  public void closeAndStillAhead_keepsDriving() {
    assertFalse(check.hasArrived(0.25f, 0.2f));
    assertFalse(check.hasArrived(0.18f, 0.3f));
  }

  @Test
  public void passedTheGoalSideways_arrived() {
    // 20 cm away, goal now beside the robot: it went past, don't turn back.
    assertTrue(check.hasArrived(0.20f, (float) Math.toRadians(100)));
  }

  @Test
  public void startingWithGoalBehindFarAway_keepsDriving() {
    // A goal behind the robot at the start is a turn, not an arrival.
    assertFalse(check.hasArrived(1.5f, (float) Math.PI));
  }

  @Test
  public void gettingFurtherAwayAfterClosestPoint_arrived() {
    assertFalse(check.hasArrived(0.22f, 0.4f));
    assertFalse(check.hasArrived(0.15f, 0.8f));
    assertTrue(check.hasArrived(0.19f, 0.9f));
  }

  @Test
  public void smallNoiseNearClosestPoint_isNotMovingAway() {
    assertFalse(check.hasArrived(0.20f, 0.2f));
    assertFalse(check.hasArrived(0.21f, 0.2f));
  }

  @Test
  public void reset_forgetsPreviousRun() {
    check.hasArrived(0.12f, 0f);
    check.reset();
    assertFalse(check.hasArrived(0.2f, 0f));
  }
}
