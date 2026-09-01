package org.openbot.customview;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.cartfollow.SteeringEvidence;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SteeringDemandViewTest {
  @Test
  public void acceptsEvidenceAndUsesItsMeasuredBounds() {
    Context context = RuntimeEnvironment.getApplication();
    SteeringDemandView view = new SteeringDemandView(context);
    view.setEvidence(
        new SteeringEvidence(
            true,
            "ok",
            0.4f,
            0.3f,
            0.2f,
            0.38f,
            0f,
            33,
            SteeringEvidence.Direction.RIGHT,
            SteeringEvidence.Level.MEDIUM,
            400));
    view.measure(
        android.view.View.MeasureSpec.makeMeasureSpec(600, android.view.View.MeasureSpec.EXACTLY),
        android.view.View.MeasureSpec.makeMeasureSpec(100, android.view.View.MeasureSpec.EXACTLY));
    view.layout(0, 0, 600, 100);

    assertEquals(600, view.getWidth());
    assertEquals(100, view.getHeight());
  }
}
