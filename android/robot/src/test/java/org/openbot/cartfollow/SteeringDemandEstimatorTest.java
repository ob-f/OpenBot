package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SteeringDemandEstimatorTest {
  @Test
  public void leftAndRightAreSymmetricInImageCoordinates() {
    SteeringEvidence left = new SteeringDemandEstimator().update(box(100f), 1000, 600, 0, 1, 0, 400);
    SteeringEvidence right = new SteeringDemandEstimator().update(box(900f), 1000, 600, 0, 1, 0, 400);

    assertEquals(SteeringEvidence.Direction.RIGHT, left.direction);
    assertEquals(SteeringEvidence.Direction.LEFT, right.direction);
    assertEquals(Math.abs(left.rawError), Math.abs(right.rawError), 0.001f);
    assertEquals(left.demandPercent, right.demandPercent);
  }

  @Test
  public void predictionLeadsFastMovementTowardAnEdge() {
    SteeringDemandEstimator estimator = new SteeringDemandEstimator();
    estimator.update(box(500f), 1000, 600, 0, 2, 0, 400);
    SteeringEvidence moving = estimator.update(box(700f), 1000, 600, 0, 2, 100, 400);

    assertTrue(moving.predictedError > moving.filteredError);
    assertTrue(moving.demandPercent > 0);
    assertEquals(SteeringEvidence.Direction.LEFT, moving.direction);
  }

  @Test
  public void stationaryMeasurementsReduceThePredictedLead() {
    SteeringDemandEstimator estimator = new SteeringDemandEstimator();
    estimator.update(box(500f), 1000, 600, 0, 2, 0, 400);
    SteeringEvidence moving = estimator.update(box(700f), 1000, 600, 0, 2, 100, 400);
    SteeringEvidence settled = moving;
    for (int i = 2; i <= 6; i++) {
      settled = estimator.update(box(700f), 1000, 600, 0, 2, i * 100L, 400);
    }

    assertTrue(Math.abs(settled.predictedError - settled.filteredError)
        < Math.abs(moving.predictedError - moving.filteredError));
  }

  @Test
  public void bboxNearRelevantEdgeRaisesUrgency() {
    SteeringDemandEstimator estimator = new SteeringDemandEstimator();
    SteeringEvidence evidence =
        estimator.update(new RectF(820f, 100f, 1005f, 500f), 1000, 600, 0, 3, 0, 0);

    assertEquals(1f, evidence.edgeUrgency, 0.001f);
    assertEquals(100, evidence.demandPercent);
    assertEquals(SteeringEvidence.Level.EDGE, evidence.level);
  }

  @Test
  public void trackChangeAndLongGapResetVelocity() {
    SteeringDemandEstimator estimator = new SteeringDemandEstimator();
    estimator.update(box(500f), 1000, 600, 0, 4, 0, 400);
    estimator.update(box(700f), 1000, 600, 0, 4, 100, 400);

    SteeringEvidence trackChanged = estimator.update(box(700f), 1000, 600, 0, 5, 200, 400);
    assertEquals("filter_reset", trackChanged.reason);
    assertEquals(0f, trackChanged.lateralRatePerSec, 0.001f);

    SteeringEvidence gapReset = estimator.update(box(700f), 1000, 600, 0, 5, 800, 400);
    assertEquals("filter_reset", gapReset.reason);
    assertEquals(0f, gapReset.lateralRatePerSec, 0.001f);
  }

  @Test
  public void configuredHorizonChangesOnlyPrediction() {
    SteeringDemandEstimator zeroEstimator = new SteeringDemandEstimator();
    zeroEstimator.update(box(500f), 1000, 600, 0, 6, 0, 0);
    SteeringEvidence zero = zeroEstimator.update(box(700f), 1000, 600, 0, 6, 100, 0);
    SteeringDemandEstimator longEstimator = new SteeringDemandEstimator();
    longEstimator.update(box(500f), 1000, 600, 0, 6, 0, 800);
    SteeringEvidence longHorizon = longEstimator.update(box(700f), 1000, 600, 0, 6, 100, 800);

    assertEquals(zero.filteredError, zero.predictedError, 0.001f);
    assertTrue(longHorizon.predictedError > longHorizon.filteredError);
    assertEquals(0, zero.predictionHorizonMs);
    assertEquals(800, longHorizon.predictionHorizonMs);
  }

  @Test
  public void levelUsesHysteresisNearTheMediumThreshold() {
    SteeringDemandEstimator estimator = new SteeringDemandEstimator();
    SteeringEvidence medium = estimator.update(box(700f), 1000, 600, 0, 8, 0, 0);
    SteeringEvidence stillMedium =
        estimator.update(box(690f), 1000, 600, 0, 8, 100, 0);

    assertEquals(SteeringEvidence.Level.MEDIUM, medium.level);
    assertTrue(stillMedium.demandPercent < 35);
    assertEquals(SteeringEvidence.Level.MEDIUM, stillMedium.level);
  }

  @Test
  public void unavailableTargetResetsEvidence() {
    SteeringDemandEstimator estimator = new SteeringDemandEstimator();
    estimator.update(box(700f), 1000, 600, 0, 7, 0, 400);
    SteeringEvidence unavailable = estimator.update(null, 1000, 600, 0, 7, 100, 400);

    assertFalse(unavailable.valid);
    assertEquals(SteeringEvidence.Direction.NONE, unavailable.direction);
  }

  private static RectF box(float centerX) {
    return new RectF(centerX - 40f, 100f, centerX + 40f, 500f);
  }
}
