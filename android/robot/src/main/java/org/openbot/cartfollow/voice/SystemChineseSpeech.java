package org.openbot.cartfollow.voice;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import timber.log.Timber;

/** Owns one system TTS session and degrades silently when Chinese speech is unavailable. */
final class SystemChineseSpeech {
  private TextToSpeech tts;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private Context context;
  private boolean ready;
  private int generation;
  private VoiceGuidancePlanner.Prompt pendingPrompt;

  void start(Context context) {
    stop();
    this.context = context.getApplicationContext();
    final int session = generation;
    try {
      tts =
          new TextToSpeech(
              this.context, status -> mainHandler.post(() -> onInitialized(session, status)));
    } catch (RuntimeException error) {
      Timber.w(error, "Voice Cart Simulator: system TTS could not be created");
      stop();
    }
  }

  private void onInitialized(int session, int status) {
    if (session != generation || tts == null) return;
    try {
      if (status != TextToSpeech.SUCCESS) {
        Timber.w("Voice Cart Simulator: system TTS initialization failed");
        return;
      }
      int language = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
      if (language < TextToSpeech.LANG_AVAILABLE) {
        Timber.w("Voice Cart Simulator: simplified Chinese TTS is unavailable: %s", language);
        return;
      }
      tts.setAudioAttributes(
          new AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .build());
      tts.setSpeechRate(1.0f);
      tts.setPitch(1.0f);
      ready = true;
      speakPending();
    } catch (RuntimeException error) {
      Timber.w(error, "Voice Cart Simulator: system TTS could not be initialized");
      stop();
    }
  }

  void speak(VoiceGuidancePlanner.Prompt prompt) {
    if (prompt == null) return;
    pendingPrompt = prompt;
    if (ready) speakPending();
  }

  void stop() {
    generation++;
    ready = false;
    pendingPrompt = null;
    context = null;
    mainHandler.removeCallbacksAndMessages(null);
    if (tts != null) {
      tts.stop();
      tts.shutdown();
      tts = null;
    }
  }

  private void speakPending() {
    if (!ready || tts == null || context == null || pendingPrompt == null) return;
    VoiceGuidancePlanner.Prompt prompt = pendingPrompt;
    pendingPrompt = null;
    int result =
        tts.speak(
            context.getString(prompt.textRes),
            prompt.urgent ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
            null,
            "voice-cart-" + System.nanoTime());
    if (result == TextToSpeech.ERROR) Timber.w("Voice Cart Simulator: TTS rejected a prompt");
  }
}
