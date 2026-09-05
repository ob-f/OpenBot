package org.openbot.cartfollow.voice;

import java.util.EnumMap;
import java.util.Map;
import org.openbot.cartfollow.FollowState;

/** Converts stable cart-follow state changes into sparse, actionable speech prompts. */
final class VoiceGuidancePlanner {
  static final long REPEAT_INTERVAL_MS = 8000L;

  static final class Prompt {
    final int textRes;
    final boolean urgent;

    Prompt(int textRes, boolean urgent) {
      this.textRes = textRes;
      this.urgent = urgent;
    }
  }

  private final Map<FollowState, Long> spokenAt = new EnumMap<>(FollowState.class);
  private FollowState previousState = FollowState.IDLE;
  private boolean clippedPromptActive;
  private long clippedPromptAtMs = -REPEAT_INTERVAL_MS;

  Prompt onFrame(FollowState state, String diagnostic, long nowMs) {
    if (state == null) return null;
    boolean stateChanged = state != previousState;
    previousState = state;
    if (state == FollowState.DISTANCE_CALIBRATION) {
      boolean clipped = diagnostic != null && diagnostic.contains("完整人物入镜");
      if (clipped && !clippedPromptActive && nowMs - clippedPromptAtMs >= REPEAT_INTERVAL_MS) {
        clippedPromptActive = true;
        clippedPromptAtMs = nowMs;
        return new Prompt(VoicePrompts.CALIBRATION_CLIPPED, false);
      }
      if (!clipped) clippedPromptActive = false;
    } else {
      clippedPromptActive = false;
    }
    if (!stateChanged || !canSpeak(state, nowMs)) return null;
    switch (state) {
      case CAPTURE_TARGET:
        return prompt(VoicePrompts.CAPTURE, false, nowMs);
      case LOCKED_PENDING_CONFIRM:
        return prompt(VoicePrompts.CONFIRM, false, nowMs);
      case DISTANCE_CALIBRATION:
        return prompt(VoicePrompts.CALIBRATION, false, nowMs);
      case CONFIRMED_ARMED:
      case REACQUIRE_TARGET:
        return prompt(VoicePrompts.REACQUIRE, false, nowMs);
      case READY_TO_FOLLOW:
        return prompt(VoicePrompts.COUNTDOWN, false, nowMs);
      case FOLLOW:
        return prompt(VoicePrompts.FOLLOW, false, nowMs);
      case IDENTITY_UNCERTAIN:
        return prompt(VoicePrompts.IDENTITY_UNCERTAIN, true, nowMs);
      case LOST:
        return prompt(VoicePrompts.LOST, true, nowMs);
      case SEARCH:
      case DIRECTED_REACQUIRE:
        return prompt(VoicePrompts.SEARCH, false, nowMs);
      case STOP:
        return prompt(VoicePrompts.STOPPED, true, nowMs);
      default:
        return null;
    }
  }

  void reset() {
    previousState = FollowState.IDLE;
    clippedPromptActive = false;
    clippedPromptAtMs = -REPEAT_INTERVAL_MS;
    spokenAt.clear();
  }

  private boolean canSpeak(FollowState state, long nowMs) {
    Long last = spokenAt.get(state);
    return last == null || nowMs - last >= REPEAT_INTERVAL_MS;
  }

  private Prompt prompt(int textRes, boolean urgent, long nowMs) {
    spokenAt.put(previousState, nowMs);
    return new Prompt(textRes, urgent);
  }
}
