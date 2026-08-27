// Created by Matthias Mueller - Intel Intelligent Systems Lab - 2020

package org.openbot.app.robot.logging;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import androidx.annotation.RequiresApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.openbot.app.robot.R;
import org.openbot.app.robot.env.Logger;
import org.openbot.app.robot.env.SharedPreferencesManager;
import org.openbot.app.robot.utils.Enums;

public class SensorService extends Service implements SensorEventListener {
  private SensorManager sensorManager;
  private Sensor accelerometerSensor;
  private Sensor gyroscopeSensor;
  private Sensor gravitySensor;
  private Sensor magneticSensor;
  private Sensor lightSensor;
  private Sensor proximitySensor;
  private Sensor pressureSensor;
  private Sensor temperatureSensor;
  private Sensor poseSensor;
  private Sensor motionSensor;

  private BufferedWriter accelerometerLog;
  private BufferedWriter gyroscopeLog;
  private BufferedWriter gravityLog;
  private BufferedWriter magneticLog;
  private BufferedWriter lightLog;
  private BufferedWriter proximityLog;
  private BufferedWriter pressureLog;
  private BufferedWriter temperatureLog;
  private BufferedWriter poseLog;
  private BufferedWriter motionLog;
  private BufferedWriter gpsLog;
  private BufferedWriter frameLog;
  private BufferedWriter inferenceLog;
  private BufferedWriter ctrlLog;
  private BufferedWriter indicatorLog;
  private BufferedWriter voltageLog;
  private BufferedWriter sonarLog;
  private BufferedWriter wheelsLog;
  private BufferedWriter bumperLog;

  private boolean trackingLocation = false;
  private boolean hasStarted = false;
  private FusedLocationProviderClient fusedLocationClient;
  private LocationCallback locationCallback;

  // Message Types
  public static final int MSG_FRAME = 0;
  public static final int MSG_INFERENCE = 1;
  public static final int MSG_CONTROL = 2;
  public static final int MSG_INDICATOR = 3;
  public static final int MSG_VOLTAGE = 4;
  public static final int MSG_SONAR = 5;
  public static final int MSG_WHEELS = 6;
  public static final int MSG_BUMPER = 7;

  private static final Logger LOGGER = new Logger();
  Messenger messenger = new Messenger(new SensorMessageHandler());

  private SharedPreferencesManager preferencesManager;

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  public final void onCreate() {
    super.onCreate();
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    poseSensor = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF);
    motionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);
    // Initialize the FusedLocationClient.
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    preferencesManager = new SharedPreferencesManager(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Bundle extras = intent.getExtras();
    String logFolder;
    if (extras == null) {
      logFolder =
          Environment.getExternalStorageDirectory().getAbsolutePath()
              + File.separator
              + getString(R.string.app_name);
    } else {
      logFolder = (String) extras.get("logFolder");
    }

    int delay = (int) (preferencesManager.getDelay() * 1000);
    if (preferencesManager.getSensorStatus(Enums.SensorType.ACCELEROMETER.getSensor())
        && accelerometerSensor != null) {
      accelerometerLog = openLog(logFolder, "accelerometerLog.txt");
      appendLog(accelerometerLog, "timestamp[ns],x[m/s^2],y[m/s^2],z[m/s^2]");
      sensorManager.registerListener(this, accelerometerSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.GYROSCOPE.getSensor())
        && gyroscopeSensor != null) {
      gyroscopeLog = openLog(logFolder, "gyroscopeLog.txt");
      appendLog(gyroscopeLog, "timestamp[ns],x[rad/s],y[rad/s],z[rad/s]");
      sensorManager.registerListener(this, gyroscopeSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.GRAVITY.getSensor())
        && gravitySensor != null) {
      gravityLog = openLog(logFolder, "gravityLog.txt");
      appendLog(gravityLog, "timestamp[ns],x[m/s^2],y[m/s^2],z[m/s^2]");
      sensorManager.registerListener(this, gravitySensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.MAGNETIC.getSensor())
        && magneticSensor != null) {
      magneticLog = openLog(logFolder, "magneticLog.txt");
      appendLog(magneticLog, "timestamp[ns],x[uT],y[uT],z[uT]");
      sensorManager.registerListener(this, magneticSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.LIGHT.getSensor())
        && lightSensor != null) {
      lightLog = openLog(logFolder, "lightLog.txt");
      appendLog(lightLog, "timestamp[ns],light[lux]");
      sensorManager.registerListener(this, lightSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.PROXIMITY.getSensor())
        && proximitySensor != null) {
      proximityLog = openLog(logFolder, "proximityLog.txt");
      appendLog(proximityLog, "timestamp[ns],proximity[cm]");
      sensorManager.registerListener(this, proximitySensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.PRESSURE.getSensor())
        && pressureSensor != null) {
      pressureLog = openLog(logFolder, "pressureLog.txt");
      appendLog(pressureLog, "timestamp[ns],pressure[hPa]");
      sensorManager.registerListener(this, pressureSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.TEMPERATURE.getSensor())
        && temperatureSensor != null) {
      temperatureLog = openLog(logFolder, "temperatureLog.txt");
      appendLog(temperatureLog, "timestamp[ns],temperature[degrees]");
      sensorManager.registerListener(this, temperatureSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.POSE.getSensor())
        && poseSensor != null) {
      poseLog = openLog(logFolder, "poseLog.txt");
      appendLog(poseLog, "timestamp[ns],x,y,z,w,x,y,z,dx,dy,dz,dw,dx,dy,dz,id");
      sensorManager.registerListener(this, poseSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.MOTION.getSensor())
        && motionSensor != null) {
      motionLog = openLog(logFolder, "motionLog.txt");
      appendLog(motionLog, "timestamp[ns],motion");
      sensorManager.registerListener(this, motionSensor, delay);
    }

    if (preferencesManager.getSensorStatus(Enums.SensorType.GPS.getSensor())) {
      gpsLog = openLog(logFolder, "gpsLog.txt");
      appendLog(gpsLog, "timestamp[ns],latitude,longitude,altitude[m],bearing,speed[m/s]");
    }

    frameLog = openLog(logFolder, "rgbFrames.txt");
    appendLog(frameLog, "timestamp[ns],frame");

    inferenceLog = openLog(logFolder, "inferenceTime.txt");
    appendLog(inferenceLog, "frame, inferenceTime [ns]");

    ctrlLog = openLog(logFolder, "ctrlLog.txt");
    appendLog(ctrlLog, "timestamp[ns],leftCtrl,rightCtrl");

    indicatorLog = openLog(logFolder, "indicatorLog.txt");
    appendLog(indicatorLog, "timestamp[ns],signal");

    if (preferencesManager.getSensorStatus(Enums.SensorType.VEHICLE.getSensor())) {
      voltageLog = openLog(logFolder, "voltageLog.txt");
      appendLog(voltageLog, "timestamp[ns],batteryVoltage");
      sonarLog = openLog(logFolder, "sonarLog.txt");
      appendLog(sonarLog, "timestamp[ns],distance[cm]");
      wheelsLog = openLog(logFolder, "wheelsLog.txt");
      appendLog(wheelsLog, "timestamp[ns],leftWheel,rightWheel");
      bumperLog = openLog(logFolder, "bumperLog.txt");
      appendLog(bumperLog, "timestamp[ns],bumper");
    }

    locationCallback =
        new LocationCallback() {
          @Override
          public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
              appendLog(
                  gpsLog,
                  location.getElapsedRealtimeNanos()
                      + ","
                      + location.getLatitude()
                      + ","
                      + location.getLongitude()
                      + ","
                      + location.getAltitude()
                      + ","
                      + location.getBearing()
                      + ","
                      + location.getSpeed());
            }
          }
        };

    startTrackingLocation();
    hasStarted = true;

    return START_REDELIVER_INTENT;
  }

  @Override
  public IBinder onBind(Intent intent) {
    // We don't provide binding, so return null
    // return null;
    return messenger.getBinder();
  }

  @Override
  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Do something here if sensor accuracy changes.
  }

  @Override
  public final void onSensorChanged(SensorEvent event) {
    // The light sensor returns a single value.
    // Many sensors return 3 values, one for each axis.
    String sensorName = event.sensor.getName();
    // Do something with this sensor value.
    switch (event.sensor.getType()) {
      case Sensor.TYPE_ACCELEROMETER:
        // Acceleration including gravity along the X, Y and Z axis
        // Units are m/s^2
        appendLog(
            accelerometerLog,
            event.timestamp
                + ","
                + event.values[0]
                + ","
                + event.values[1]
                + ","
                + event.values[2]);
        break;
      case Sensor.TYPE_GYROSCOPE:
        // Angular speed around the device's local X, Y and Z axis
        // Units are radians/second
        // The coordinate system is the same as is used by the acceleration sensor
        appendLog(
            gyroscopeLog,
            event.timestamp
                + ","
                + event.values[0]
                + ","
                + event.values[1]
                + ","
                + event.values[2]);
        break;
      case Sensor.TYPE_GRAVITY:
        // A three dimensional vector indicating the direction and magnitude of gravity
        // Units are m/s^2
        // The coordinate system is the same as is used by the acceleration sensor
        appendLog(
            gravityLog,
            event.timestamp
                + ","
                + event.values[0]
                + ","
                + event.values[1]
                + ","
                + event.values[2]);
        break;
      case Sensor.TYPE_MAGNETIC_FIELD:
        // Ambient magnetic field in the X, Y and Z axis in micro-Tesla (uT).
        appendLog(
            magneticLog,
            event.timestamp
                + ","
                + event.values[0]
                + ","
                + event.values[1]
                + ","
                + event.values[2]);
        break;
      case Sensor.TYPE_LIGHT:
        // Ambient light level in SI lux units
        appendLog(lightLog, event.timestamp + "," + event.values[0]);
        break;
      case Sensor.TYPE_PROXIMITY:
        // Proximity sensor distance measured in centimeters
        appendLog(proximityLog, event.timestamp + "," + event.values[0]);
        break;
      case Sensor.TYPE_PRESSURE:
        // Atmospheric pressure in mPa (millibar)
        appendLog(pressureLog, event.timestamp + "," + event.values[0]);
        break;
      case Sensor.TYPE_AMBIENT_TEMPERATURE:
        // Ambient temperature in degrees
        appendLog(temperatureLog, event.timestamp + "," + event.values[0]);
        break;
      case Sensor.TYPE_POSE_6DOF:
        // values[0]: x*sin(θ/2)
        // values[1]: y*sin(θ/2)
        // values[2]: z*sin(θ/2)
        // values[3]: cos(θ/2)
        // values[4]: Translation along x axis from an arbitrary origin.
        // values[5]: Translation along y axis from an arbitrary origin.
        // values[6]: Translation along z axis from an arbitrary origin.
        // values[7]: Delta quaternion rotation x*sin(θ/2)
        // values[8]: Delta quaternion rotation y*sin(θ/2)
        // values[9]: Delta quaternion rotation z*sin(θ/2)
        // values[10]: Delta quaternion rotation cos(θ/2)
        // values[11]: Delta translation along x axis.
        // values[12]: Delta translation along y axis.
        // values[13]: Delta translation along z axis.
        // values[14]: Sequence number
        appendLog(
            poseLog,
            event.timestamp
                + ","
                + event.values[0]
                + ","
                + event.values[1]
                + ","
                + event.values[2]
                + ","
                + event.values[3]
                + ","
                + event.values[4]
                + ","
                + event.values[5]
                + ","
                + event.values[6]
                + ","
                + event.values[7]
                + ","
                + event.values[8]
                + ","
                + event.values[9]
                + ","
                + event.values[10]
                + ","
                + event.values[11]
                + ","
                + event.values[12]
                + ","
                + event.values[13]
                + ","
                + event.values[14]);
        break;
      case Sensor.TYPE_MOTION_DETECT:
        appendLog(motionLog, event.timestamp + "," + event.values[0]);
        break;
      case Sensor.TYPE_STATIONARY_DETECT:
        appendLog(motionLog, event.timestamp + "," + (-1) * event.values[0]);
        break;
      default:
        // Unknown sensor
        break;
    }
  }

  private class SensorMessageHandler extends android.os.Handler {
    @Override
    public void handleMessage(Message msg) {
      if (hasStarted) {
        if (msg.what == MSG_FRAME) {
          long frameNumber = msg.getData().getLong("frameNumber");
          long timestamp = msg.getData().getLong("timestamp");
          if (frameLog != null) appendLog(frameLog, timestamp + "," + frameNumber);
        } else if (msg.what == MSG_INFERENCE) {
          long frameNumber = msg.getData().getLong("frameNumber");
          long inferenceTime = msg.getData().getLong("inferenceTime");
          if (inferenceLog != null) appendLog(inferenceLog, frameNumber + "," + inferenceTime);
        } else if (msg.what == MSG_CONTROL) {
          // msg.arg1 and msg.arg2 contain left and right control signals respectively
          if (ctrlLog != null)
            appendLog(
                ctrlLog, SystemClock.elapsedRealtimeNanos() + "," + msg.arg1 + "," + msg.arg2);
        } else if (msg.what == MSG_INDICATOR) {
          // msg.arg1 contains indicator signal
          if (indicatorLog != null)
            appendLog(indicatorLog, SystemClock.elapsedRealtimeNanos() + "," + msg.arg1);
        } else if (msg.what == MSG_VOLTAGE) {
          long timestamp = msg.getData().getLong("timestamp");
          String data = msg.getData().getString("data");
          if (voltageLog != null) appendLog(voltageLog, timestamp + "," + data);
        } else if (msg.what == MSG_SONAR) {
          long timestamp = msg.getData().getLong("timestamp");
          String data = msg.getData().getString("data");
          if (sonarLog != null) appendLog(sonarLog, timestamp + "," + data);
        } else if (msg.what == MSG_WHEELS) {
          long timestamp = msg.getData().getLong("timestamp");
          String data = msg.getData().getString("data");
          if (wheelsLog != null) appendLog(wheelsLog, timestamp + "," + data);
        } else if (msg.what == MSG_BUMPER) {
          long timestamp = msg.getData().getLong("timestamp");
          String data = msg.getData().getString("data");
          if (bumperLog != null) appendLog(bumperLog, timestamp + "," + data);
        } else LOGGER.d("Message skipped.");
      }
    }
  }

  @Override
  public void onDestroy() {
    hasStarted = false;
    sensorManager.unregisterListener(this);
    stopTrackingLocation();

    if (accelerometerLog != null) closeLog(accelerometerLog);
    if (gyroscopeLog != null) closeLog(gyroscopeLog);
    if (gravityLog != null) closeLog(gravityLog);
    if (magneticLog != null) closeLog(magneticLog);
    if (lightLog != null) closeLog(lightLog);
    if (proximityLog != null) closeLog(proximityLog);
    if (pressureLog != null) closeLog(pressureLog);
    if (temperatureLog != null) closeLog(temperatureLog);
    if (poseLog != null) closeLog(poseLog);
    if (motionLog != null) closeLog(motionLog);
    if (gpsLog != null) closeLog(gpsLog);
    if (frameLog != null) closeLog(frameLog);
    if (inferenceLog != null) closeLog(inferenceLog);
    if (ctrlLog != null) closeLog(ctrlLog);
    if (indicatorLog != null) closeLog(indicatorLog);
    if (voltageLog != null) closeLog(voltageLog);
    if (sonarLog != null) closeLog(sonarLog);
    if (wheelsLog != null) closeLog(wheelsLog);
    if (bumperLog != null) closeLog(bumperLog);
  }

  public BufferedWriter openLog(String path, String filename) {
    LOGGER.i("Opening log file: " + filename);
    final File myDir = new File(path);

    if (!myDir.exists()) {
      if (!myDir.mkdirs()) {
        LOGGER.i("Make dir failed");
      }
    }

    final File file = new File(myDir, filename);

    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    try {
      // BufferedWriter for performance, true to set append to file flag
      return new BufferedWriter(new FileWriter(file, true));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public void appendLog(BufferedWriter writer, String text) {
    try {
      writer.append(text);
      writer.newLine();
      writer.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void closeLog(BufferedWriter writer) {
    try {
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void startTrackingLocation() {
    try {
      fusedLocationClient.requestLocationUpdates(
          getLocationRequest(), locationCallback, null /* Looper */);
      trackingLocation = true;
    } catch (SecurityException e) {
      trackingLocation = false;
      throw new SecurityException("No permission to use location.", e);
    }
  }

  /**
   * Method that stops tracking the device. It removes the location updates, stops the animation and
   * reset the UI.
   */
  private void stopTrackingLocation() {
    if (trackingLocation) {
      trackingLocation = false;
      fusedLocationClient.removeLocationUpdates(locationCallback);
    }
  }

  private LocationRequest getLocationRequest() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(1000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    return locationRequest;
  }
}
