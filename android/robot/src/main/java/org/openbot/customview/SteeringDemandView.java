package org.openbot.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import org.openbot.cartfollow.SteeringEvidence;

/** Compact gauge for comparing the current and latency-predicted turn demand. */
public final class SteeringDemandView extends View {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private SteeringEvidence evidence = SteeringEvidence.unavailable("idle", 400);

  public SteeringDemandView(Context context) {
    this(context, null);
  }

  public SteeringDemandView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint.setStrokeCap(Paint.Cap.ROUND);
  }

  public void setEvidence(SteeringEvidence evidence) {
    this.evidence = evidence == null ? SteeringEvidence.unavailable("idle", 400) : evidence;
    postInvalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float density = getResources().getDisplayMetrics().density;
    float left = getPaddingLeft() + 8f * density;
    float right = getWidth() - getPaddingRight() - 8f * density;
    float centerY = getHeight() * 0.5f;
    float trackHeight = 10f * density;
    if (right <= left) return;

    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.rgb(76, 175, 80));
    canvas.drawRoundRect(new RectF(left, centerY - trackHeight / 2f, right, centerY + trackHeight / 2f), trackHeight, trackHeight, paint);
    float center = (left + right) * 0.5f;
    paint.setColor(Color.rgb(255, 193, 7));
    float moderate = (right - left) * 0.20f;
    canvas.drawRoundRect(new RectF(center - moderate, centerY - trackHeight / 2f, center + moderate, centerY + trackHeight / 2f), trackHeight, trackHeight, paint);
    paint.setColor(Color.rgb(66, 66, 66));
    canvas.drawRect(center - density, centerY - 16f * density, center + density, centerY + 16f * density, paint);

    if (!evidence.valid) return;
    float currentX = map(evidence.filteredError, left, right);
    float predictedX = map(evidence.predictedError, left, right);
    paint.setColor(Color.WHITE);
    canvas.drawCircle(currentX, centerY, 7f * density, paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(3f * density);
    paint.setColor(Color.rgb(33, 150, 243));
    canvas.drawCircle(predictedX, centerY, 9f * density, paint);
    paint.setStyle(Paint.Style.FILL);
  }

  private static float map(float error, float left, float right) {
    float normalized = Math.max(-1f, Math.min(1f, error));
    return left + (normalized + 1f) * 0.5f * (right - left);
  }
}
