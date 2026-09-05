package org.openbot.cartfollow.voice;

import android.os.SystemClock;
import org.openbot.cartfollow.FollowStateMachine;
import org.openbot.cartfollow.HumanCartSimulatorFragment;

/**
 * Independent simulator entry with event-driven Chinese voice guidance and no real vehicle control.
 */
public class VoiceCartSimulatorFragment extends HumanCartSimulatorFragment {
  private final VoiceGuidancePlanner guidancePlanner = new VoiceGuidancePlanner();
  private final SystemChineseSpeech speech = new SystemChineseSpeech();

  @Override
  public synchronized void onResume() {
    speech.start(requireContext());
    super.onResume();
    speech.speak(new VoiceGuidancePlanner.Prompt(VoicePrompts.WELCOME, false));
  }

  @Override
  protected void onFrameUiApplied(FollowStateMachine.FrameResult frame) {
    super.onFrameUiApplied(frame);
    if (frame == null || !isAdded()) return;
    speech.speak(
        guidancePlanner.onFrame(
            frame.state, frame.distanceDiagnosticText, SystemClock.elapsedRealtime()));
  }

  @Override
  protected void onFollowSessionReset() {
    super.onFollowSessionReset();
    guidancePlanner.reset();
  }

  @Override
  protected void onCartFollowPause() {
    speech.stop();
    guidancePlanner.reset();
    super.onCartFollowPause();
  }
}
