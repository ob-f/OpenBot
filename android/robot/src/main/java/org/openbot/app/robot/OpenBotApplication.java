package org.openbot.app.robot;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import org.jetbrains.annotations.NotNull;
import org.openbot.app.robot.vehicle.Vehicle;
import timber.log.Timber;

public class OpenBotApplication extends Application {

  static Context context;
  public static Vehicle vehicle;

  public static Context getContext() {
    return context;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    int baudRate = Integer.parseInt(sharedPreferences.getString("baud_rate", "115200"));
    vehicle = new Vehicle(this, baudRate);
    vehicle.initBle();
    vehicle.connectUsb();
    vehicle.initBle();
    if (BuildConfig.DEBUG) {
      Timber.plant(
          new Timber.DebugTree() {
            @NonNull
            @Override
            protected String createStackElementTag(@NotNull StackTraceElement element) {
              return super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
          });
    }
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }
}
