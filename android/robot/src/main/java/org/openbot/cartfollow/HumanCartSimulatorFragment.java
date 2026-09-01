package org.openbot.cartfollow;

import android.graphics.RectF;
import android.view.View;
import org.openbot.R;

/** Human-in-the-loop view of the shared cart-follow perception and behavior pipeline. */
public class HumanCartSimulatorFragment extends BaseCartFollowFragment {
  private final SteeringDemandEstimator steeringDemandEstimator = new SteeringDemandEstimator();
  private int predictionHorizonMs = 400;

  @Override
  protected void onCartFollowViewCreated() {
    binding.steeringPanel.setVisibility(View.VISIBLE);
    binding.predictionHorizonGroup.check(R.id.prediction_400);
    binding.predictionHorizonGroup.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) return;
          if (checkedId == R.id.prediction_0) predictionHorizonMs = 0;
          else if (checkedId == R.id.prediction_800) predictionHorizonMs = 800;
          else predictionHorizonMs = 400;
          steeringDemandEstimator.reset();
          updateSteeringUi(SteeringEvidence.unavailable("horizon_changed", predictionHorizonMs));
        });
    updateSteeringUi(SteeringEvidence.unavailable("idle", predictionHorizonMs));
  }

  @Override
  protected void enrichFrameResult(
      FollowStateMachine.FrameResult frameResult,
      int frameW,
      int frameH,
      int sensorOrientation,
      long nowMs) {
    boolean following =
        frameResult != null
            && (frameResult.state == FollowState.FOLLOW || frameResult.state == FollowState.FOLLOW_CAUTION)
            && frameResult.target != null
            && frameResult.target.getLocation() != null;
    if (!following) {
      steeringDemandEstimator.reset();
      if (frameResult != null) {
        frameResult.steeringEvidence =
            SteeringEvidence.unavailable("not_following", predictionHorizonMs);
      }
      return;
    }
    int trackId =
        frameResult.identityEvidence == null ? -1 : frameResult.identityEvidence.trackId;
    RectF bbox = new RectF(frameResult.target.getLocation());
    frameResult.steeringEvidence =
        steeringDemandEstimator.update(
            bbox, frameW, frameH, sensorOrientation, trackId, nowMs, predictionHorizonMs);
  }

  @Override
  protected void onFollowFrame(FollowStateMachine.FrameResult frameResult) {
    updateSteeringUi(
        frameResult == null
            ? SteeringEvidence.unavailable("frame_missing", predictionHorizonMs)
            : frameResult.steeringEvidence);
  }

  @Override
  protected void onFollowSessionReset() {
    steeringDemandEstimator.reset();
    updateSteeringUi(SteeringEvidence.unavailable("session_reset", predictionHorizonMs));
  }

  private void updateSteeringUi(SteeringEvidence evidence) {
    if (binding == null || getActivity() == null) return;
    final SteeringEvidence safeEvidence =
        evidence == null ? SteeringEvidence.unavailable("idle", predictionHorizonMs) : evidence;
    getActivity()
        .runOnUiThread(
            () -> {
              if (binding == null) return;
              binding.steeringGauge.setEvidence(safeEvidence);
              if (!safeEvidence.valid) {
                binding.steeringSummary.setText("转向需求等待可信跟随目标");
                return;
              }
              if (safeEvidence.direction == SteeringEvidence.Direction.NONE) {
                binding.steeringSummary.setText(
                    String.format(
                        java.util.Locale.US,
                        "转向需求 %d%% · 居中\n当前 %+.2f · 预测 %+.2f（提前 %d ms）",
                        safeEvidence.demandPercent,
                        safeEvidence.filteredError,
                        safeEvidence.predictedError,
                        safeEvidence.predictionHorizonMs));
                return;
              }
              binding.steeringSummary.setText(
                  String.format(
                      java.util.Locale.US,
                      "转向需求 %d%% · 向%s%s转弯\n当前 %+.2f · 预测 %+.2f（提前 %d ms）",
                      safeEvidence.demandPercent,
                      safeEvidence.directionLabel(),
                      safeEvidence.levelLabel(),
                      safeEvidence.filteredError,
                      safeEvidence.predictedError,
                      safeEvidence.predictionHorizonMs));
            });
  }
}
