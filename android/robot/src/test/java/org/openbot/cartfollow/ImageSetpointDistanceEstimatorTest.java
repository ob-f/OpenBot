package org.openbot.cartfollow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.tflite.Detector;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ImageSetpointDistanceEstimatorTest {
  private static final ImageSetpointDistanceEstimator.Setpoint SETPOINT =
      new ImageSetpointDistanceEstimator.Setpoint(.50f, .20f, .95f);

  @Test
  public void configurableRelativeDistanceUsesHysteresis() {
    ImageSetpointDistanceEstimator estimator = new ImageSetpointDistanceEstimator();
    estimator.setMaximumDistanceMultiplier(1.10f);
    assertEquals(DistanceState.TOO_FAR, estimate(estimator, .45f, .20f).state);
    assertEquals(DistanceState.TOO_FAR, estimate(estimator, .48f, .20f).state);
    assertEquals(DistanceState.OK, estimate(estimator, .495f, .20f).state);
  }

  @Test
  public void poseAreaDisagreementIsDiagnosticAndDoesNotStopHeightDecision() {
    ImageSetpointDistanceEstimator estimator = new ImageSetpointDistanceEstimator();
    ImageSetpointDistanceEstimator.DistanceEstimate result = estimate(estimator, .45f, .04f);
    assertEquals(DistanceState.TOO_FAR, result.state);
    assertNotNull(result.failureReason);
  }

  private static ImageSetpointDistanceEstimator.DistanceEstimate estimate(
      ImageSetpointDistanceEstimator estimator, float heightRatio, float widthRatio) {
    Detector.Recognition target =
        new Detector.Recognition(
            "1", "person", .9f, new RectF(0, 0, widthRatio * 1000f, heightRatio * 1000f), 0);
    return estimator.estimate(target, 1000, 1000, 0, SETPOINT);
  }
}
