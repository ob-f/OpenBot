package org.openbot.cartfollow.voice;

import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Locale;
import org.openbot.R;

/** Standalone system TTS smoke test; does not initialize the cart-follow pipeline. */
public class VoiceCartSimulatorFragment extends Fragment {
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private TextToSpeech tts;
  private TextView statusView;
  private TextView detailsView;
  private boolean ready;
  private int generation;
  private long utteranceSequence;
  private String currentUtterance;

  public VoiceCartSimulatorFragment() {
    super(R.layout.fragment_voice_cart_simulator);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    statusView = view.findViewById(R.id.voice_status);
    detailsView = view.findViewById(R.id.voice_details);
    view.findViewById(R.id.voice_replay)
        .setOnClickListener(
            v -> {
              if (ready) speak();
              else initializeSpeech();
            });
  }

  @Override
  public void onStart() {
    super.onStart();
    initializeSpeech();
  }

  private void initializeSpeech() {
    releaseSpeech();
    final int session = generation;
    statusView.setText(R.string.voice_cart_initializing);
    detailsView.setText("");
    // Always post: some engines can report failure before the constructor returns.
    try {
      tts =
          new TextToSpeech(
              requireContext().getApplicationContext(),
              status -> mainHandler.post(() -> onSpeechInitialized(session, status)));
      mainHandler.postDelayed(
          () -> {
            if (generation == session && !ready && statusView != null) {
              releaseSpeech();
              statusView.setText(R.string.voice_cart_init_timeout);
            }
          },
          10000L);
    } catch (RuntimeException error) {
      releaseSpeech();
      statusView.setText(R.string.voice_cart_init_failed);
    }
  }

  private void onSpeechInitialized(int session, int status) {
    if (generation != session || statusView == null || tts == null) return;
    mainHandler.removeCallbacksAndMessages(null);
    if (status != TextToSpeech.SUCCESS) {
      statusView.setText(R.string.voice_cart_init_failed);
      return;
    }
    try {
      int language = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
      if (language < TextToSpeech.LANG_AVAILABLE) {
        statusView.setText(
            language == TextToSpeech.LANG_MISSING_DATA
                ? R.string.voice_cart_missing_data
                : R.string.voice_cart_unsupported);
        return;
      }
      Voice voice = tts.getVoice();
      String unknown = getString(R.string.voice_cart_unknown);
      detailsView.setText(
          getString(
              R.string.voice_cart_details,
              tts.getDefaultEngine() == null ? unknown : tts.getDefaultEngine(),
              voice == null ? unknown : voice.getName(),
              voice == null
                  ? unknown
                  : getString(
                      voice.isNetworkConnectionRequired()
                          ? R.string.voice_cart_network_required
                          : R.string.voice_cart_local_voice)));
      tts.setAudioAttributes(
          new AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .build());
      tts.setSpeechRate(1.0f);
      tts.setPitch(1.0f);
      tts.setOnUtteranceProgressListener(
          new UtteranceProgressListener() {
            @Override
            public void onStart(String id) {
              report(session, id, R.string.voice_cart_speaking, null);
            }

            @Override
            public void onDone(String id) {
              report(session, id, R.string.voice_cart_done, null);
            }

            @Override
            public void onError(String id) {
              onError(id, TextToSpeech.ERROR);
            }

            @Override
            public void onError(String id, int errorCode) {
              report(session, id, R.string.voice_cart_error, errorCode);
            }
          });
      ready = true;
      speak();
    } catch (RuntimeException error) {
      releaseSpeech();
      statusView.setText(R.string.voice_cart_init_failed);
    }
  }

  private void speak() {
    if (!ready || tts == null) return;
    currentUtterance = "voice-cart-" + (++utteranceSequence);
    statusView.setText(R.string.voice_cart_queued);
    try {
      if (tts.speak(
              getString(R.string.voice_cart_phrase),
              TextToSpeech.QUEUE_FLUSH,
              null,
              currentUtterance)
          == TextToSpeech.ERROR) {
        ready = false;
        statusView.setText(getString(R.string.voice_cart_error, TextToSpeech.ERROR));
      }
    } catch (RuntimeException error) {
      ready = false;
      statusView.setText(getString(R.string.voice_cart_error, TextToSpeech.ERROR));
    }
  }

  private void report(int session, String id, int text, Integer errorCode) {
    mainHandler.post(
        () -> {
          if (generation != session || statusView == null || !id.equals(currentUtterance)) return;
          if (errorCode != null) ready = false;
          statusView.setText(errorCode == null ? getString(text) : getString(text, errorCode));
        });
  }

  private void releaseSpeech() {
    generation++;
    ready = false;
    currentUtterance = null;
    mainHandler.removeCallbacksAndMessages(null);
    if (tts != null) {
      tts.stop();
      tts.shutdown();
      tts = null;
    }
  }

  @Override
  public void onStop() {
    releaseSpeech();
    super.onStop();
  }

  @Override
  public void onDestroyView() {
    releaseSpeech();
    statusView = null;
    detailsView = null;
    super.onDestroyView();
  }
}
