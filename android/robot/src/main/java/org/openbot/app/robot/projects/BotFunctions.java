package org.openbot.app.robot.projects;

import static org.webrtc.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;

import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotTrackingException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.openbot.app.robot.databinding.FragmentBlocklyExecutingBinding;
import org.openbot.app.robot.env.AudioPlayer;
import org.openbot.app.robot.env.SharedPreferencesManager;
import org.openbot.app.robot.pointGoalNavigation.ArCore;
import org.openbot.app.robot.tflite.Model;
import org.openbot.app.robot.tflite.Navigation;
import org.openbot.app.robot.tflite.Network;
import org.openbot.app.robot.utils.Enums;
import org.openbot.app.robot.vehicle.Vehicle;
import timber.log.Timber;

/** implement openBot functions according to block codes. */
public class BotFunctions implements SensorEventListener {
  private final Vehicle vehicle;
  private final AudioPlayer audioPlayer;
  private final SharedPreferencesManager sharedPreferencesManager;
  private final FragmentBlocklyExecutingBinding binding;
  private final Activity mActivity;
  private final Context mContext;
  private ArCore arCore;
  private final SensorManager sensorManager;
  private final Sensor accelerometerSensor;
  private final Sensor gyroscopeSensor;
  private final Sensor magneticSensor;
  private final float[] gyroscopeValues = new float[3]; // Array to store gyroscope values
  private final float[] accelerometerValues = new float[3]; // Array to store accelerometer values
  private final float[] magneticFieldValues = new float[3]; // Array to store magnetic values
  TextToSpeech tts;

  /**
   * get vehicle and audioPlayer in parameters to control openBot commands.
   *
   */
  public BotFunctions(
      Vehicle getVehicle,
      AudioPlayer getAudioPlayer,
      SharedPreferencesManager getSharedPreferencesManager,
      Context getContext,
      FragmentBlocklyExecutingBinding getBinding,
      Activity getActivity,
      ArCore arCore) {
    vehicle = getVehicle;
    audioPlayer = getAudioPlayer;
    sharedPreferencesManager = getSharedPreferencesManager;
    sensorManager = (SensorManager) getContext.getSystemService(Context.SENSOR_SERVICE);
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    binding = getBinding;
    mActivity = getActivity;
    mContext = getContext;
    this.arCore = arCore;
  }

  /** openBot Movement functions */
  @JavascriptInterface
  public void moveForward(int speed) {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Move Forward at " + speed));
    double speedResult = (double) speed / (double) vehicle.getSpeedMultiplier();
    vehicle.setControl((float) speedResult, (float) speedResult);
  }

  @JavascriptInterface
  public void moveBackward(int speed) {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Move Backward at " + speed));
    double speedResult = (double) speed / (double) vehicle.getSpeedMultiplier();
    vehicle.setControl((float) -speedResult, (float) -speedResult);
  }

  @JavascriptInterface
  public void moveLeft(int speed) {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Move Left at + " + speed));
    double speedResult = (double) speed / (double) vehicle.getSpeedMultiplier();
    vehicle.setControl(0, (float) speedResult);
  }

  @JavascriptInterface
  public void moveRight(int speed) {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Move Right at + " + speed));
    double speedResult = (double) speed / (double) vehicle.getSpeedMultiplier();
    vehicle.setControl((float) speedResult, 0);
  }

  @JavascriptInterface
  public void moveOpenBot(int leftSpeed, int rightSpeed) {
    mActivity.runOnUiThread(
        () ->
            binding.jsCommand.setText(
                "Move Left at " + leftSpeed + " Move Right at " + rightSpeed));
    double leftSpeedResult = (double) leftSpeed / (double) vehicle.getSpeedMultiplier();
    double rightSpeedResult = (double) rightSpeed / (double) vehicle.getSpeedMultiplier();
    vehicle.setControl((float) leftSpeedResult, (float) rightSpeedResult);
  }

  //    @JavascriptInterface
  //    public void openBotmoveCircular(int a) {
  //        mActivity.runOnUiThread(() -> binding.jsCommand.setText("Move Circular"));
  //        Control control = new Control(1F, 1F);
  //        vehicle.setControl(control);
  //    }

  @JavascriptInterface
  public void pause(int ms) {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Wait for " + ms + "ms"));
    try {
      synchronized (this) {
        wait(ms); // Pauses the thread for 5 seconds
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @JavascriptInterface
  public void stopRobot() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Stop Car Immediately"));
    BlocklyExecutingFragment.isFollow = false;
    vehicle.stopBot();
  }

  @JavascriptInterface
  public void disableAI() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Stop AI"));
//    BlocklyExecutingFragment.tracker = null;
    BlocklyExecutingFragment.isFollow = false;
    BlocklyExecutingFragment.isAutopilot = false;
    BlocklyExecutingFragment.isFollowMultipleObject = false;
    BlocklyExecutingFragment.isStartDetectorAutoPilot = false;
  }

  /**
   * openBot Sensor functions
   *
   */
  @JavascriptInterface
  public float sonarReading() {
    return vehicle.getSonarReading();
  }

  @JavascriptInterface
  public float speedReading() {
    return (vehicle.getLeftWheelRpm() + vehicle.getRightWheelRpm()) / 2;
  }

  @JavascriptInterface
  public float voltageDividerReading() {
    return vehicle.getBatteryVoltage();
  }

  @JavascriptInterface
  public boolean frontWheelReading() {
    return vehicle.isHasWheelOdometryFront();
  }

  @JavascriptInterface
  public boolean backWheelReading() {
    return vehicle.isHasWheelOdometryBack();
  }

  @JavascriptInterface
  public float gyroscopeReadingX() {
    sensorManager.registerListener(
        this, gyroscopeSensor, sharedPreferencesManager.getDelay() * 1000);
    return gyroscopeValues[0];
  }

  @JavascriptInterface
  public float gyroscopeReadingY() {
    sensorManager.registerListener(
        this, gyroscopeSensor, sharedPreferencesManager.getDelay() * 1000);
    return gyroscopeValues[1];
  }

  @JavascriptInterface
  public float gyroscopeReadingZ() {
    sensorManager.registerListener(
        this, gyroscopeSensor, sharedPreferencesManager.getDelay() * 1000);
    return gyroscopeValues[2];
  }

  @JavascriptInterface
  public float accelerationReadingX() {
    sensorManager.registerListener(
        this, accelerometerSensor, sharedPreferencesManager.getDelay() * 1000);
    return accelerometerValues[0];
  }

  @JavascriptInterface
  public float accelerationReadingY() {
    sensorManager.registerListener(
        this, accelerometerSensor, sharedPreferencesManager.getDelay() * 1000);
    return accelerometerValues[1];
  }

  @JavascriptInterface
  public float accelerationReadingZ() {
    sensorManager.registerListener(
        this, accelerometerSensor, sharedPreferencesManager.getDelay() * 1000);
    return accelerometerValues[2];
  }

  @JavascriptInterface
  public float magneticReadingX() {
    sensorManager.registerListener(
        this, magneticSensor, sharedPreferencesManager.getDelay() * 1000);
    return magneticFieldValues[0];
  }

  @JavascriptInterface
  public float magneticReadingY() {
    sensorManager.registerListener(
        this, magneticSensor, sharedPreferencesManager.getDelay() * 1000);
    return magneticFieldValues[1];
  }

  @JavascriptInterface
  public float magneticReadingZ() {
    sensorManager.registerListener(
        this, magneticSensor, sharedPreferencesManager.getDelay() * 1000);
    return magneticFieldValues[2];
  }

  @JavascriptInterface
  public void indicatorReading() {
    //    Timber.tag(TAG).d("Indicator - %s", vehicle.isHasIndicators());
  }

  /** service command to robot */
  @JavascriptInterface
  public void noiseEnable(boolean value) {
    audioPlayer.playNoise("matthew", value);
  }

  @JavascriptInterface
  public void playSoundSpeed(String speedMode) {
    if (Objects.equals(speedMode, "slow")) {
      mActivity.runOnUiThread(
          () -> binding.jsCommand.setText("Play Sound " + speedMode + " Speed"));
      audioPlayer.playSpeedMode("matthew", Enums.SpeedMode.SLOW);
    } else if (Objects.equals(speedMode, "medium")) {
      mActivity.runOnUiThread(
          () -> binding.jsCommand.setText("Play Sound " + speedMode + " Speed"));
      audioPlayer.playSpeedMode("matthew", Enums.SpeedMode.NORMAL);
    } else if (Objects.equals(speedMode, "fast")) {
      mActivity.runOnUiThread(
          () -> binding.jsCommand.setText("Play Sound " + speedMode + " Speed"));
      audioPlayer.playSpeedMode("matthew", Enums.SpeedMode.FAST);
    }
  }

  @JavascriptInterface
  public void playSoundMode(String driveMode) {
    if (Objects.equals(driveMode, "dual drive")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Play Sound " + driveMode + " Mode"));
      audioPlayer.playDriveMode("matthew", Enums.DriveMode.DUAL);
    } else if (Objects.equals(driveMode, "joystick control")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Play Sound " + driveMode + " Mode"));
      audioPlayer.playDriveMode("matthew", Enums.DriveMode.JOYSTICK);
    } else if (Objects.equals(driveMode, "gamepad")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Play Sound " + driveMode + " Mode"));
      audioPlayer.playDriveMode("matthew", Enums.DriveMode.GAME);
    }
  }

  @JavascriptInterface
  public void rightIndicatorOn() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Right Indicator On"));
    vehicle.setIndicator(1);
  }

  @JavascriptInterface
  public void leftIndicatorOn() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Left Indicator On"));
    vehicle.setIndicator(-1);
  }

  @JavascriptInterface
  public void IndicatorOff() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Indicator Off"));
    vehicle.setIndicator(0);
  }

  @JavascriptInterface
  public void rightIndicatorOff() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Right Indicator Off"));
    vehicle.setIndicator(0);
  }

  @JavascriptInterface
  public void leftIndicatorOff() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Left Indicator Off"));
    vehicle.setIndicator(0);
  }

  @JavascriptInterface
  public void IndicatorOn() {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Indicator On"));
    vehicle.setIndicator(1);
    vehicle.setIndicator(-1);
  }

@JavascriptInterface
public void ledBrightness(float value) {
  mActivity.runOnUiThread(() -> binding.jsCommand.setText("Led Brightness " + value));
  vehicle.sendLightIntensity(value / 100, value / 100);
}

  @JavascriptInterface
  public void toggleLed(String value) {
    mActivity.runOnUiThread(() -> binding.jsCommand.setText("Toggle Led " + value));
    if (Objects.equals(value, "ON")) {
      vehicle.sendLightIntensity(100, 100);
    } else if (Objects.equals(value, "OFF")) {
      vehicle.sendLightIntensity(0, 0);
    }
  }

  @JavascriptInterface
  public void switchController(String controllerMode) {
    if (Objects.equals(controllerMode, "gamepad")) {
      mActivity.runOnUiThread(
          () -> binding.jsCommand.setText("Switch Controller to " + controllerMode));
      sharedPreferencesManager.setControlMode(0);
    } else if (Objects.equals(controllerMode, "phone")) {
      mActivity.runOnUiThread(
          () -> binding.jsCommand.setText("Switch Controller to " + controllerMode));
      sharedPreferencesManager.setControlMode(1);
    }
  }

  @JavascriptInterface
  public void switchDriveMode(String driveMode) {
    if (Objects.equals(driveMode, "dual")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Switch Drive Mode to " + driveMode));
      sharedPreferencesManager.setDriveMode(0);
    } else if (Objects.equals(driveMode, "game")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Switch Drive Mode to " + driveMode));
      sharedPreferencesManager.setDriveMode(1);
    } else if (Objects.equals(driveMode, "joystick")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Switch Drive Mode to " + driveMode));
      sharedPreferencesManager.setDriveMode(2);
    }
  }
  @JavascriptInterface
  public void setSpeed(String speed) {
    if (Objects.equals(speed, "slow")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Set speed to " + speed));
      sharedPreferencesManager.setSpeedMode(128);
      vehicle.setSpeedMultiplier(Enums.SpeedMode.SLOW.getValue());
    } else if (Objects.equals(speed, "medium")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Set speed to " + speed));
      sharedPreferencesManager.setDriveMode(192);
      vehicle.setSpeedMultiplier(Enums.SpeedMode.NORMAL.getValue());
    } else if (Objects.equals(speed, "fast")) {
      mActivity.runOnUiThread(() -> binding.jsCommand.setText("Set speed to " + speed));
      sharedPreferencesManager.setDriveMode(255);
      vehicle.setSpeedMultiplier(Enums.SpeedMode.FAST.getValue());
    }
  }

  @JavascriptInterface
  public void navigationModel(String AIModel) {
    Timber.tag("Ai Blocks").i(AIModel);
  }

  @JavascriptInterface
  public void reachGoal(float forward, float left, String model) {
    Timber.tag("Ai Blocks").i(forward + ", " + left);
    mActivity.runOnUiThread(() -> binding.blocklyLayout.setBackgroundColor(Color.TRANSPARENT));
    startPointGoal(-forward, -left);
    BlocklyExecutingFragment.navigationModelName = model;
  }

  @JavascriptInterface
  public void follow(String object, String modelName) {
    mActivity.runOnUiThread(() -> {
      binding.blocklyLayout.setBackgroundColor(Color.TRANSPARENT);
      binding.jsCommand.setText("Follow " + object + " using " + modelName);
    });
    BlocklyExecutingFragment.detectorModelName = modelName;
    BlocklyExecutingFragment.classType = object;
    BlocklyExecutingFragment.isFollow = true;
  }

  @JavascriptInterface
  public void enableMultipleDetection(String startObject, String modelName, String stopObject, String task) {
    mActivity.runOnUiThread(() -> {
      binding.blocklyLayout.setBackgroundColor(Color.TRANSPARENT);
      binding.jsCommand.setText("Follow " + startObject + " using " + modelName + "stop when see " + stopObject);
    });
    BlocklyExecutingFragment.detectorModelName = modelName;
    BlocklyExecutingFragment.startObject = startObject;
    BlocklyExecutingFragment.stopObject = stopObject;
    BlocklyExecutingFragment.getTask = task;
    BlocklyExecutingFragment.isFollowMultipleObject = true;
  }

  @JavascriptInterface
  public void enableAutopilot(String modelName) {
    mActivity.runOnUiThread(() -> {
      binding.blocklyLayout.setBackgroundColor(Color.TRANSPARENT);
      binding.jsCommand.setText("Start Autopilot using " + modelName);

    });
    BlocklyExecutingFragment.autoPilotModelName = modelName;
    BlocklyExecutingFragment.isAutopilot = true;
  }

  @JavascriptInterface
  public void enableMultipleAI(String autoPilotModel, String task, String classType, String detectorModel){
    mActivity.runOnUiThread(() -> {
      binding.blocklyLayout.setBackgroundColor(Color.TRANSPARENT);
      binding.jsCommand.setText("Start Autopilot with Object Tracking.");
    });
    BlocklyExecutingFragment.autoPilotModelName = autoPilotModel;
    BlocklyExecutingFragment.detectorModelName = detectorModel;
    BlocklyExecutingFragment.classType = classType;
    BlocklyExecutingFragment.getTask = task;
    BlocklyExecutingFragment.isStartDetectorAutoPilot = true;
  }

  @JavascriptInterface
  public void onDetect(String classType, String model, String task) {
    Map<String, String> detectTask = new HashMap<>();
    detectTask.put("onDetect", task);
    BlocklyExecutingFragment.detectorModelName = model;
    BlocklyExecutingFragment.taskStorage.addTask(classType, detectTask);
  }

  @JavascriptInterface
  public void onLostFrames(String classType, int numberOfFrames, String task) {
    Map<String, Map<String, String>> taskData = BlocklyExecutingFragment.taskStorage.getData();
    for (Map.Entry<String, Map<String, String>> entry : taskData.entrySet()) {
      if (classType.equals(entry.getKey())) {
        Map<String, String> existingMap = entry.getValue();
        existingMap.put("onUnDetect", task);
        existingMap.put("noOfFrames", String.valueOf(numberOfFrames));
        BlocklyExecutingFragment.taskStorage.addTask(classType, existingMap);
        taskData.put(classType, existingMap);
        break;
      }
    }
    BlocklyExecutingFragment.isOnDetection = true;
  }

  @JavascriptInterface
  public void playSound(String classType) {
    mActivity.runOnUiThread(() -> {
      binding.jsCommand.setText("Playing input sound.");
    });
    tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int i) {
        if(i == TextToSpeech.SUCCESS){
          tts.setLanguage(Locale.US);
          tts.setSpeechRate(1.0f);
          tts.speak(classType,TextToSpeech.QUEUE_ADD,null);
        }
      }
    });
  }

  @JavascriptInterface
  public void displayString(String text) {
    mActivity.runOnUiThread(() -> {
      binding.jsCommand.setText(text);
    });
  }

  @JavascriptInterface
  public void displaySensorData(String sensorType) {
      String type = "";
      String finalText = "";
      String prefix = "Android.";
      if (sensorType.startsWith(prefix)) {
        finalText = sensorType.substring(prefix.length());
      }
    switch (finalText.trim()){
        case "sonarReading()" :
            System.out.println("in sonar");
            type = "sonar : " + vehicle.getSonarReading();
            break;
        case "speedReading()" :
            type = "speed : " + (vehicle.getLeftWheelRpm() + vehicle.getRightWheelRpm()) / 2;
            break;
        case "voltageDividerReading()" :
            type = "voltage : " + vehicle.getBatteryVoltage();
            break;
        case "frontWheelReading()" :
            type = "frontWheel : " + vehicle.isHasWheelOdometryFront();
            break;
        case "backWheelReading()" :
            type = "backWheel : " + vehicle.isHasWheelOdometryBack();
            break;
        case "gyroscopeReadingX()" :
            sensorManager.registerListener(
                    this, gyroscopeSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "gyroscopeX : " + gyroscopeValues[0];
            break;
        case "gyroscopeReadingY()" :
            sensorManager.registerListener(
                    this, gyroscopeSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "gyroscopeY : " + gyroscopeValues[1];
            break;
        case "gyroscopeReadingZ()" :
            sensorManager.registerListener(
                    this, gyroscopeSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "gyroscopeZ : " + gyroscopeValues[2];
            break;
        case "accelerationReadingX" :
            sensorManager.registerListener(
                    this, accelerometerSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "accelerationX : " + accelerometerValues[0];
            break;
        case "accelerationReadingY" :
            sensorManager.registerListener(
                    this, accelerometerSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "accelerationY : " + accelerometerValues[1];
            break;
        case "accelerationReadingZ" :
            sensorManager.registerListener(
                    this, accelerometerSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "accelerationZ : " + accelerometerValues[2];
            break;
        case "magneticReadingX" :
            sensorManager.registerListener(
                    this, magneticSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "magneticX : " + magneticFieldValues[0];
            break;
        case "magneticReadingY" :
            sensorManager.registerListener(
                    this, magneticSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "magneticY : " + magneticFieldValues[1];
            break;
        case "magneticReadingZ" :
            sensorManager.registerListener(
                    this, magneticSensor, sharedPreferencesManager.getDelay() * 1000);
            type = "magneticZ : " + magneticFieldValues[2];
            break;
        default:
            break;
      }
    String finalType = type;
    mActivity.runOnUiThread(() -> {
      binding.jsCommand.setText(finalType);
    });
  }


    @JavascriptInterface
  public void reachPosition(int x, int y) {
    Timber.tag("Ai Blocks").i(x + ", " + y);
  }

  /**
   * Get phone sensors value.
   * @param event the {@link android.hardware.SensorEvent SensorEvent}.
   */
  @Override
  public void onSensorChanged(SensorEvent event) {
    switch (event.sensor.getType()) {
      case Sensor.TYPE_ACCELEROMETER:
        // Acceleration including gravity along the X, Y and Z axis
        // Units are m/s^2
        accelerometerValues[0] = event.values[0]; // Accelerometer X-axis value
        accelerometerValues[1] = event.values[1]; // Accelerometer Y-axis value
        accelerometerValues[2] = event.values[2]; // Accelerometer Z-axis value
        break;
      case Sensor.TYPE_GYROSCOPE:
        // Angular speed around the device's local X, Y and Z axis
        // Units are radians/second
        // The coordinate system is the same as is used by the acceleration sensor
        gyroscopeValues[0] = event.values[0]; // Gyroscope X-axis value
        gyroscopeValues[1] = event.values[1]; // Gyroscope Y-axis value
        gyroscopeValues[2] = event.values[2]; // Gyroscope Z-axis value
        break;
      case Sensor.TYPE_MAGNETIC_FIELD:
        // Ambient magnetic field in the X, Y and Z axis in micro-Tesla (uT).
        magneticFieldValues[0] = event.values[0]; // magneticField X-axis value
        magneticFieldValues[1] = event.values[1]; // magneticField Y-axis value
        magneticFieldValues[2] = event.values[2]; // magneticField Z-axis value
        break;
      default:
        break;
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  /**
   * Starts the process of driving towards a goal point using ARCore and navigation
   * for the Blockly block code(start point goal navigation) command.
   * @param goalX The x-coordinate of the goal point.
   * @param goalZ The z-coordinate of the goal point.
   */
  private void startPointGoal(float goalX, float goalZ) {
    setupArCore();

    while (arCore.getStartPose() == null) startDriving(goalX, goalZ);
  }

  /**
   * Sets up ARCore for usage.
   */
  private void setupArCore() {
    try {
      arCore.resume();
    } catch (java.lang.SecurityException e) {
      e.printStackTrace();
    } catch (UnavailableSdkTooOldException e) {
      e.printStackTrace();
    } catch (UnavailableDeviceNotCompatibleException e) {
      e.printStackTrace();
    } catch (UnavailableArcoreNotInstalledException e) {
      e.printStackTrace();
    } catch (UnavailableApkTooOldException e) {
      e.printStackTrace();
    } catch (CameraNotAvailableException e) {
      e.printStackTrace();
    }

//    showInfoDialog(
//            "ARCore failure. Make sure that your device is compatible and the ARCore SDK is installed.");
  }

  /**
   * Initiates the driving process towards the specified goal point using ARCore.
   *
   * @param goalX The x-coordinate of the goal point.
   * @param goalZ The z-coordinate of the goal point.
   */
  private void startDriving(float goalX, float goalZ) {
    Timber.i("setting goal at (" + goalX + ", " + goalZ + ")");
    try {
      arCore.detachAnchors();
      arCore.setStartAnchorAtCurrentPose();
      Pose startPose = arCore.getStartPose();
      if (startPose == null) {
//        showInfoDialog(getString(R.string.no_initial_ar_core_pose));
        return;
      }
      arCore.setTargetAnchor(startPose.compose(Pose.makeTranslation(goalX, 0.0f, goalZ)));
    } catch (NotTrackingException e) {
      e.printStackTrace();
//      showInfoDialog(getString(R.string.tracking_lost));
      return;
    }
    Model model =
            new Model(
                    0,
                    Model.CLASS.NAVIGATION,
                    Model.TYPE.GOALNAV,
                    "navigation.tflite",
                    Model.PATH_TYPE.ASSET,
                    "networks/navigation.tflite",
                    "160x90");

    try {
      BlocklyExecutingFragment.navigationPolicy = new Navigation(mActivity, model, Network.Device.CPU, 1);
    } catch (IOException e) {
      e.printStackTrace();
//      showInfoDialog("Navigation policy could not be initialized.");
      return;
    }
    BlocklyExecutingFragment.isRunning = true;
  }
}
