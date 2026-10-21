// Created by Matthias Mueller - Intel Intelligent Systems Lab - 2020

package org.openbot.app.robot.env;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.openbot.app.robot.utils.Enums;

// Convert text to speech
// https://ttsmp3.com

public class AudioPlayer {

  private MediaPlayer mp;
  private final Context context;
  private TextToSpeech tts;

  public AudioPlayer(Context context) {
    mp = new MediaPlayer();
    this.context = context;

    tts =
        new TextToSpeech(
            context,
            new TextToSpeech.OnInitListener() {
              @Override
              public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                  tts.setLanguage(Locale.US);
                  tts.setSpeechRate(1.0f);
                  tts.setPitch(0.5f);
                }
              }
            });
  }

  // Play audio from string id.
  public void playFromStringID(int id) {
    playFromString(context.getString(id));
  }

  // Play audio from string.
  public void playFromString(String message) {
    tts.speak(
        message
            .replace("'Y'", "why")
            .replace("'B'", "bee")
            .replace("'X'", "axe")
            .replace("AR Core", "ay are core"),
        TextToSpeech.QUEUE_FLUSH,
        null,
        "ttsPlayer");
  }

  // Play from a resource file
  public void play(int id) {
    try {
      mp.reset();
      mp = MediaPlayer.create(context, id);
      mp.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Play from a file in storage
  public void play(String path) {
    try {
      mp.reset();
      mp.setDataSource(path);
      mp.prepare();
      mp.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Play from media asset
  public void play(String assetFolder, String fileName) {
    try {
      AssetFileDescriptor afd =
          context
              .getAssets()
              .openFd("media" + File.separator + assetFolder + File.separator + fileName);
      mp.reset();
      mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
      mp.prepare();
      mp.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void playDriveMode(String voice, Enums.DriveMode driveMode) {
    switch (driveMode) {
      case DUAL:
        play(voice, "dual_drive_control.mp3");
        break;
      case GAME:
        play(voice, "video_game_control.mp3");
        break;
      case JOYSTICK:
        play(voice, "joystick_control.mp3");
        break;
    }
  }

  public void playSpeedMode(String voice, Enums.SpeedMode speedMode) {
    switch (speedMode) {
      case SLOW:
        play(voice, "slow_speed.mp3");
        break;
      case NORMAL:
        play(voice, "normal_speed.mp3");
        break;
      case FAST:
        play(voice, "fast_speed.mp3");
        break;
    }
  }

  public void playNoise(String voice, boolean isEnabled) {
    if (isEnabled) {
      play(voice, "noise_enabled.mp3");
    } else {
      play(voice, "noise_disabled.mp3");
    }
  }

  public void playLogging(String voice, boolean isEnabled) {
    if (isEnabled) {
      play(voice, "logging_started.mp3");
    } else {
      play(voice, "logging_stopped.mp3");
    }
  }
}
