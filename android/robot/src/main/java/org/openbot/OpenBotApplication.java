package org.openbot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.openbot.vehicle.Vehicle;

import java.util.Locale;

import timber.log.Timber;

public class OpenBotApplication extends Application {

  private static Context context;
  public static Vehicle vehicle;

  public static Context getContext() {
    return context;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();

    // Retrieve selected language from SharedPreferences
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    String languageCode = sharedPreferences.getString("language", "en"); // Default to English
    setLocale(languageCode);  // Apply the language setting

    int baudRate = Integer.parseInt(sharedPreferences.getString("baud_rate", "115200"));
    vehicle = new Vehicle(this, baudRate);
    vehicle.initBle();
    vehicle.connectUsb();

    // Setup Timber for logging in debug mode
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree() {
        @NonNull
        @Override
        protected String createStackElementTag(@NotNull StackTraceElement element) {
          return super.createStackElementTag(element) + ":" + element.getLineNumber();
        }
      });
    }
  }

  // Set the locale for the application
  public void setLocale(String languageCode) {
    Locale locale = new Locale(languageCode);
    Locale.setDefault(locale);
    Resources resources = getResources();
    Configuration config = resources.getConfiguration();

    // Update the configuration based on the version
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      config.setLocale(locale);
      config.setLayoutDirection(locale);
      getApplicationContext().createConfigurationContext(config);
    } else {
      config.locale = locale;
      config.setLayoutDirection(locale);
    }

    resources.updateConfiguration(config, resources.getDisplayMetrics());
  }

  // Method to dynamically refresh the app's language
  public void refreshAppLanguage(String languageCode) {
    // Save the new language preference
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("language", languageCode); // Store the new language code
    editor.apply();

    // Set the locale and refresh the UI
    setLocale(languageCode);
    if (context instanceof Activity) {
      Activity activity = (Activity) context;
      activity.recreate(); // Restart the activity to apply the new locale settings
    }
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }
}
