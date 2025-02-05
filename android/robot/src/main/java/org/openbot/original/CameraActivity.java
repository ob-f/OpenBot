/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified by Matthias Mueller - Intel Intelligent Systems Lab - 2020

package org.openbot.original;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.openbot.OpenBotApplication;
import org.openbot.R;
import org.openbot.env.AudioPlayer;
import org.openbot.env.BotToControllerEventBus;
import org.openbot.env.ControllerToBotEventBus;
import org.openbot.env.GameController;
import org.openbot.env.ImageUtils;
import org.openbot.env.Logger;
import org.openbot.env.PhoneController;
import org.openbot.env.SharedPreferencesManager;
import org.openbot.logging.LogDataUtils;
import org.openbot.logging.SensorService;
import org.openbot.server.ServerCommunication;
import org.openbot.server.ServerListener;
import org.openbot.tflite.Model;
import org.openbot.tflite.Network.Device;
import org.openbot.utils.ConnectionUtils;
import org.openbot.utils.Constants;
import org.openbot.utils.Enums;
import org.openbot.utils.Enums.ControlMode;
import org.openbot.utils.Enums.DriveMode;
import org.openbot.utils.Enums.LogMode;
import org.openbot.utils.Enums.SpeedMode;
import org.openbot.utils.FileUtils;
import org.openbot.utils.FormatUtils;
import org.openbot.utils.PermissionUtils;
import org.openbot.vehicle.Control;
import org.openbot.vehicle.Vehicle;
import org.zeroturnaround.zip.ZipUtil;
import timber.log.Timber;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener,
        ServerListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {
  private static final Logger LOGGER = new Logger();

  private static final String TAG = "CameraActivity";

  private static Context context;
  private int cameraSelection = CameraCharacteristics.LENS_FACING_BACK;
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private final boolean debug = false;
  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private final byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;

  private LinearLayout bottomSheetLayout;
  private LinearLayout gestureLayout;
  private BottomSheetBehavior sheetBehavior;

  protected SwitchCompat connectionSwitchCompat,
      networkSwitchCompat,
      logSwitchCompat,
      cameraSwitchCompat;
  protected TextView frameValueTextView,
      cropValueTextView,
      inferenceTimeTextView,
      controlValueTextView;
  protected ImageView bottomSheetArrowImageView;
  private ImageView plusImageView, minusImageView;
  protected Spinner baudRateSpinner,
      modelSpinner,
      deviceSpinner,
      controlModeSpinner,
      driveModeSpinner,
      logSpinner,
      speedModeSpinner;
  private TextView threadsTextView, voltageTextView, speedTextView, sonarTextView;
  private ArrayAdapter<CharSequence> modelAdapter;
  private List<Model> masterList;
  private Model model;
  private Device device = Device.CPU;
  private int numThreads = -1;

  public int[] BaudRates = {9600, 14400, 19200, 38400, 57600, 115200, 230400, 460800, 921600};
  private int baudRate = 115200;
  private LocalBroadcastManager localBroadcastManager;
  private BroadcastReceiver localBroadcastReceiver;

  protected LogMode logMode = LogMode.CROP_IMG;
  protected ControlMode controlMode = ControlMode.GAMEPAD;
  protected SpeedMode speedMode = SpeedMode.NORMAL;
  protected DriveMode driveMode = DriveMode.GAME;
  protected String logFolder;
  protected boolean loggingEnabled;
  protected boolean networkEnabled = false;
  protected boolean noiseEnabled = false;

  private Intent intentSensorService;
  private ServerCommunication serverCommunication;
  protected SharedPreferencesManager preferencesManager;
  protected final GameController gameController = new GameController(driveMode);
  private PhoneController phoneController;
  protected final ControllerHandler controllerHandler = new ControllerHandler();
  private AudioPlayer audioPlayer;
  private final String voice = "matthew";

  protected Vehicle vehicle;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    context = getApplicationContext();
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    audioPlayer = new AudioPlayer(context);
    vehicle = OpenBotApplication.vehicle;

    phoneController = PhoneController.getInstance(this);
    preferencesManager = new SharedPreferencesManager(this);

    setContentView(R.layout.activity_camera);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (PermissionUtils.hasCameraPermission(this)) {
      setFragment();
    } else {
      PermissionUtils.requestCameraPermission(this);
    }

    connectionSwitchCompat = findViewById(R.id.connection_switch);
    threadsTextView = findViewById(R.id.threads);
    plusImageView = findViewById(R.id.plus);
    minusImageView = findViewById(R.id.minus);
    networkSwitchCompat = findViewById(R.id.network_switch);
    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    gestureLayout = findViewById(R.id.gesture_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
    logSwitchCompat = findViewById(R.id.logger_switch);
    cameraSwitchCompat = findViewById(R.id.camera_toggle_switch);

    baudRateSpinner = findViewById(R.id.baud_rate_spinner);
    ArrayAdapter<CharSequence> baudRateAdapter =
        ArrayAdapter.createFromResource(this, R.array.baud_rates, R.layout.spinner_item);
    baudRateAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    baudRateSpinner.setAdapter(baudRateAdapter);

    modelSpinner = findViewById(R.id.model_spinner);
    masterList = FileUtils.loadConfigJSONFromAsset(this);
    List<String> models =
        masterList.stream()
            .filter(f -> f.pathType != Model.PATH_TYPE.URL)
            .filter(f -> f.type != Model.TYPE.GOALNAV)
            .map(f -> FileUtils.nameWithoutExtension(f.name))
            .collect(Collectors.toList());

    masterList.stream()
        .filter(f -> f.name.contains(preferencesManager.getDefaultModel()))
        .findFirst()
        .ifPresent(f -> model = f);

    modelAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>(models));
    modelAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    modelSpinner.setAdapter(modelAdapter);

    deviceSpinner = findViewById(R.id.device_spinner);
    ArrayAdapter<CharSequence> deviceAdapter =
        ArrayAdapter.createFromResource(this, R.array.devices, R.layout.spinner_item);
    deviceAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    deviceSpinner.setAdapter(deviceAdapter);

    logSpinner = findViewById(R.id.log_spinner);
    ArrayAdapter<CharSequence> logAdapter =
        ArrayAdapter.createFromResource(this, R.array.log_settings, R.layout.spinner_item);
    logAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    logSpinner.setAdapter(logAdapter);

    controlModeSpinner = findViewById(R.id.control_mode_spinner);
    ArrayAdapter<CharSequence> controlModeAdapter =
        ArrayAdapter.createFromResource(this, R.array.control_modes, R.layout.spinner_item);
    controlModeAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    controlModeSpinner.setAdapter(controlModeAdapter);

    driveModeSpinner = findViewById(R.id.drive_mode_spinner);
    ArrayAdapter<CharSequence> driveModeAdapter =
        ArrayAdapter.createFromResource(this, R.array.drive_modes, R.layout.spinner_item);
    driveModeAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    driveModeSpinner.setAdapter(driveModeAdapter);

    speedModeSpinner = findViewById(R.id.speed_mode_spinner);
    ArrayAdapter<CharSequence> controlAdapter =
        ArrayAdapter.createFromResource(this, R.array.speed_modes, R.layout.spinner_item);
    controlAdapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
    speedModeSpinner.setAdapter(controlAdapter);

    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            // int width = bottomSheetLayout.getMeasuredWidth();
            int height = gestureLayout.getMeasuredHeight();

            sheetBehavior.setPeekHeight(height);
          }
        });
    sheetBehavior.setHideable(false);

    sheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
              case BottomSheetBehavior.STATE_HIDDEN:
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                preferencesManager.setSheetExpanded(true);
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                preferencesManager.setSheetExpanded(false);
                break;
              case BottomSheetBehavior.STATE_DRAGGING:
                break;
              case BottomSheetBehavior.STATE_SETTLING:
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

    voltageTextView = findViewById(R.id.voltage_info);
    speedTextView = findViewById(R.id.speed_info);
    sonarTextView = findViewById(R.id.sonar_info);

    frameValueTextView = findViewById(R.id.frame_info);
    cropValueTextView = findViewById(R.id.crop_info);
    inferenceTimeTextView = findViewById(R.id.inference_info);
    controlValueTextView = findViewById(R.id.control_info);

    connectionSwitchCompat.setOnCheckedChangeListener(this);
    networkSwitchCompat.setOnCheckedChangeListener(this);
    logSwitchCompat.setOnCheckedChangeListener(this);
    cameraSwitchCompat.setOnCheckedChangeListener(this);

    plusImageView.setOnClickListener(this);
    minusImageView.setOnClickListener(this);

    baudRateSpinner.setOnItemSelectedListener(this);
    modelSpinner.setOnItemSelectedListener(this);
    deviceSpinner.setOnItemSelectedListener(this);
    logSpinner.setOnItemSelectedListener(this);
    controlModeSpinner.setOnItemSelectedListener(this);
    driveModeSpinner.setOnItemSelectedListener(this);
    speedModeSpinner.setOnItemSelectedListener(this);

    // Intent for sensor service
    intentSensorService = new Intent(this, SensorService.class);

    setInitialValues();

    // Try to connect to serial device
    toggleConnection(true);

    localBroadcastReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {

              switch (action) {
                case Constants.DEVICE_ACTION_DATA_RECEIVED:
                  long timestamp = SystemClock.elapsedRealtimeNanos();
                  String data = intent.getStringExtra("data");
                  char header = data.charAt(0);
                  String body = data.substring(1);
                  int type = -1;
                  switch (header) {
                    case 'f':
                      vehicle.processVehicleConfig(body);
                      break;
                    case 'v':
                      if (FormatUtils.isNumeric(body)) {
                        type = SensorService.MSG_VOLTAGE;
                        vehicle.setBatteryVoltage(Float.parseFloat(body));
                        runOnUiThread(
                            () -> {
                              voltageTextView.setText(
                                  String.format(Locale.US, "%2.1f V", vehicle.getBatteryVoltage()));
                            });
                      } else {
                        String[] msgParts = body.split(":");
                        switch (msgParts[0]) {
                          case "min":
                            vehicle.setMinMotorVoltage(Float.parseFloat(msgParts[1]));
                            break;
                          case "low":
                            vehicle.setLowBatteryVoltage(Float.parseFloat(msgParts[1]));
                            break;
                          case "max":
                            vehicle.setMaxBatteryVoltage(Float.parseFloat(msgParts[1]));
                            break;
                          default:
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Invalid voltage message received!",
                                    Toast.LENGTH_SHORT)
                                .show();
                            break;
                        }
                      }
                      break;
                    case 's':
                      if (FormatUtils.isNumeric(body)) {
                        type = SensorService.MSG_SONAR;
                        vehicle.setSonarReading(Float.parseFloat(body));
                        runOnUiThread(
                            () -> {
                              sonarTextView.setText(
                                  String.format(Locale.US, "%3.0f cm", vehicle.getSonarReading()));
                            });
                      }
                      break;
                    case 'w':
                      String[] itemList = body.split(",");
                      if (itemList.length == 2
                          && FormatUtils.isNumeric(itemList[0])
                          && FormatUtils.isNumeric(itemList[1])) {
                        type = SensorService.MSG_WHEELS;
                        vehicle.setLeftWheelRpm(Float.parseFloat(itemList[0]));
                        vehicle.setRightWheelRpm(Float.parseFloat(itemList[1]));
                        runOnUiThread(
                            () -> {
                              speedTextView.setText(
                                  String.format(
                                      Locale.US,
                                      "%3.0f,%3.0f rpm",
                                      vehicle.getLeftWheelRpm(),
                                      vehicle.getRightWheelRpm()));
                            });
                      }
                      break;
                    case 'b':
                      type = SensorService.MSG_BUMPER;
                      break;
                  }

                  if (type > 0) sendVehicleDataToSensorService(timestamp, data, type);

                  break;
              }
            }
          }
        };
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction(Constants.DEVICE_ACTION_DATA_RECEIVED);
    localBroadcastManager = LocalBroadcastManager.getInstance(this);
    localBroadcastManager.registerReceiver(localBroadcastReceiver, localIntentFilter);
    vehicle.requestVehicleConfig();
  }

  /**
   * Initalize bottom sheet views/fields with shared preference values (@see
   * SharedPreferenceManager)
   */
  @SuppressLint("SetTextI18n")
  private void setInitialValues() {
    cameraSwitchCompat.setChecked(preferencesManager.getCameraSwitch());

    baudRateSpinner.setSelection(Arrays.binarySearch(BaudRates, preferencesManager.getBaudrate()));
    if (!preferencesManager.getDefaultModel().isEmpty())
      modelSpinner.setSelection(
          Math.max(
              0,
              modelAdapter.getPosition(
                  FileUtils.nameWithoutExtension(preferencesManager.getDefaultModel()))));
    deviceSpinner.setSelection(preferencesManager.getDevice());
    logSpinner.setSelection(preferencesManager.getLogMode());
    if (ControlMode.getByID(preferencesManager.getControlMode()) != null)
      controlModeSpinner.setSelection(
          Objects.requireNonNull(ControlMode.getByID(preferencesManager.getControlMode()))
              .ordinal());
    if (DriveMode.getByID(preferencesManager.getDriveMode()) != null)
      driveModeSpinner.setSelection(
          Objects.requireNonNull(DriveMode.getByID(preferencesManager.getDriveMode())).ordinal());
    if (SpeedMode.getByID(preferencesManager.getSpeedMode()) != null)
      speedModeSpinner.setSelection(
          Objects.requireNonNull(SpeedMode.getByID(preferencesManager.getSpeedMode())).ordinal());

    setNumThreads(preferencesManager.getNumThreads());
    threadsTextView.setText(Integer.toString(numThreads));

    if (preferencesManager.getSheetExpanded()) {
      sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /** Callback for android.hardware.Camera API */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);

      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
    serverCommunication = new ServerCommunication(this, this);
    serverCommunication.start();
  }

  @Override
  public void onAddModel(String model) {
    Model item =
        new Model(
            masterList.size() + 1,
            Model.CLASS.AUTOPILOT,
            Model.TYPE.CMDNAV,
            model,
            Model.PATH_TYPE.FILE,
            getFilesDir() + File.separator + model,
            "256x96");
    if (modelAdapter != null && modelAdapter.getPosition(model) == -1) {
      modelAdapter.add(model);
      masterList.add(item);
      FileUtils.updateModelConfig(this, context,masterList, false);
    } else {
      if (model.equals(modelSpinner.getSelectedItem())) {
        setModel(item);
      }
    }
    Toast.makeText(context, "AutopilotModel added: " + model, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onRemoveModel(String model) {
    if (modelAdapter != null && modelAdapter.getPosition(model) != -1) {
      modelAdapter.remove(model);
    }
    Toast.makeText(context, "AutopilotModel removed: " + model, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onServerListChange(Set<String> servers) {}

  @Override
  public void onConnectionEstablished(String ipAddress) {}

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);
    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
      serverCommunication.stop();
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }
    phoneController.disconnect();
    vehicle.setControl(0, 0);
    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    //    toggleConnection(false);
    if (localBroadcastManager != null) {
      localBroadcastManager.unregisterReceiver(localBroadcastReceiver);
      localBroadcastManager = null;
    }
    if (localBroadcastReceiver != null) localBroadcastReceiver = null;
    LOGGER.d("onDestroy " + this);
    ControllerToBotEventBus.unsubscribe(this.getClass().getSimpleName());
    vehicle.setControl(0, 0);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case Constants.REQUEST_CAMERA_PERMISSION:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          setFragment();
        } else {
          if (ActivityCompat.shouldShowRequestPermissionRationale(
              this, Constants.PERMISSION_CAMERA)) {
            Toast.makeText(
                    this.getApplicationContext(),
                    getResources().getString(R.string.camera_permission_denied)
                        + " "
                        + getResources().getString(R.string.permission_reason_ai),
                    Toast.LENGTH_LONG)
                .show();
          }
        }
        break;

      case Constants.REQUEST_LOGGING_PERMISSIONS:
        // If the permission is granted, start logging,
        // otherwise, show a Toast
        if (PermissionUtils.checkLoggingPermissions(grantResults)) setIsLoggingActive(true);
        else PermissionUtils.showLoggingPermissionsToast(this);
        break;

      case Constants.REQUEST_CONTROLLER_PERMISSIONS:
        // If the permission is granted, start advertising to controller,
        // otherwise, show a Toast
        if (PermissionUtils.checkControllerPermissions(grantResults)) phoneController.connect(this);
        else PermissionUtils.showControllerPermissionsToast(this);
        break;
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera(int facingSelection) {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        LOGGER.i(
            "CAMERA ID: "
                + cameraId
                + " FACING: "
                + characteristics.get(CameraCharacteristics.LENS_FACING));
        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing != facingSelection) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API =
            (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)
                || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  protected void setFragment() {
    cameraSelection = getCameraUserSelection();
    String cameraId = chooseCamera(cameraSelection);
    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              (size, rotation) -> {
                previewHeight = size.getHeight();
                previewWidth = size.getWidth();
                CameraActivity.this.onPreviewSizeChosen(size, rotation);
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(
              this, getLayoutId(), getDesiredPreviewFrameSize(), cameraSelection);
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getCameraUserSelection() {
    // during initialisation there is no cameraToggle so we assume default
    if (this.cameraSwitchCompat == null) {
      // set from user preferences (last used selection)
      this.cameraSelection =
          preferencesManager.getCameraSwitch()
              ? CameraCharacteristics.LENS_FACING_FRONT
              : CameraCharacteristics.LENS_FACING_BACK;
    } else {
      this.cameraSelection =
          this.cameraSwitchCompat.isChecked()
              ? CameraCharacteristics.LENS_FACING_FRONT
              : CameraCharacteristics.LENS_FACING_BACK;
    }
    return this.cameraSelection;
  }

  protected void setCameraSwitchText() {
    if (this.cameraSelection == CameraCharacteristics.LENS_FACING_BACK) {
      cameraSwitchCompat.setText(R.string.camera_facing_back);
    } else {
      cameraSwitchCompat.setText(R.string.camera_facing_front);
    }
  }

  protected int getBaudRate() {
    return baudRate;
  }

  private void setBaudRate(int baudRate) {
    if (this.baudRate != baudRate) {
      LOGGER.d("Updating  baudRate: " + baudRate);
      this.baudRate = baudRate;
      preferencesManager.setBaudrate(baudRate);
    }
  }

  private void setLogMode(LogMode logMode) {
    if (this.logMode != logMode) {
      LOGGER.d("Updating  logMode: " + logMode);
      this.logMode = logMode;
      preferencesManager.setLogMode(logMode.ordinal());
    }
  }

  private void setSpeedMode(SpeedMode speedMode) {
    if (this.speedMode != speedMode) {
      LOGGER.d("Updating  speedMode: " + speedMode);
      this.speedMode = speedMode;
      preferencesManager.setSpeedMode(speedMode.getValue());
      speedModeSpinner.setSelection(speedMode.ordinal());
      vehicle.setSpeedMultiplier(speedMode.getValue());
    }
  }

  private void setControlMode(ControlMode controlMode) {
    if (this.controlMode != controlMode) {
      LOGGER.d("Updating  controlMode: " + controlMode);
      this.controlMode = controlMode;
      switch (controlMode) {
        case GAMEPAD:
          disconnectPhoneController();
          break;
        case PHONE:
          handleControllerEvents();
          if (!PermissionUtils.hasControllerPermissions(this)) {
            PermissionUtils.requestControllerPermissions(this);
          } else connectPhoneController();
          break;
        case WEBSERVER:
          handleControllerEvents();
          if (!PermissionUtils.hasControllerPermissions(this)) {
            PermissionUtils.requestControllerPermissions(this);
          } else connectWebController();
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + controlMode);
      }
      preferencesManager.setControlMode(controlMode.getValue());
      controlModeSpinner.setSelection(controlMode.ordinal());
    }
  }

  private void connectPhoneController() {
    phoneController.connect(this);
    DriveMode oldDriveMode = driveMode;
    // Currently only dual drive mode supported
    setDriveMode(DriveMode.DUAL);
    driveModeSpinner.setAlpha(0.5f);
  }

  private void connectWebController() {
    phoneController.connectWebServer();
    DriveMode oldDriveMode = driveMode;
    // Currently only dual drive mode supported
    setDriveMode(Enums.DriveMode.GAME);
    driveModeSpinner.setAlpha(0.5f);
    preferencesManager.setDriveMode(oldDriveMode.getValue());
  }

  private void disconnectPhoneController() {
    phoneController.disconnect();
    setDriveMode(DriveMode.values()[preferencesManager.getDriveMode()]);
    driveModeSpinner.setEnabled(true);
    driveModeSpinner.setAlpha(1.0f);
  }

  protected void setDriveMode(DriveMode driveMode) {
    if (this.driveMode != driveMode) {
      LOGGER.d("Updating  driveMode: " + driveMode);
      this.driveMode = driveMode;
      preferencesManager.setDriveMode(driveMode.getValue());
      gameController.setDriveMode(driveMode);
      driveModeSpinner.setSelection(driveMode.ordinal());
      BotToControllerEventBus.emitEvent(
          ConnectionUtils.createStatus("DRIVE_MODE", driveMode.toString()));
    }
  }

  protected Model getModel() {
    return model;
  }

  private void setModel(Model model) {
    if (this.model != model) {
      LOGGER.d("Updating  model: " + model);
      this.model = model;
      preferencesManager.setDefaultModel(model.name);
      onInferenceConfigurationChanged();
    }
  }

  protected Device getDevice() {
    return device;
  }

  private void setDevice(Device device) {
    if (this.device != device) {
      LOGGER.d("Updating  device: " + device);
      this.device = device;
      final boolean threadsEnabled = device == Device.CPU;
      plusImageView.setEnabled(threadsEnabled);
      minusImageView.setEnabled(threadsEnabled);
      threadsTextView.setText(threadsEnabled ? String.valueOf(numThreads) : "N/A");
      if (threadsEnabled) threadsTextView.setTextColor(Color.BLACK);
      else threadsTextView.setTextColor(Color.GRAY);
      preferencesManager.setDevice(device.ordinal());
      onInferenceConfigurationChanged();
    }
  }

  protected int getNumThreads() {
    return numThreads;
  }

  private void setNumThreads(int numThreads) {
    if (this.numThreads != numThreads) {
      LOGGER.d("Updating  numThreads: " + numThreads);
      this.numThreads = numThreads;
      preferencesManager.setNumThreads(numThreads);
      onInferenceConfigurationChanged();
    }
  }

  Messenger sensorMessenger;

  ServiceConnection sensorConnection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
          sensorMessenger = new Messenger(binder);
          Log.d("SensorServiceConnection", "connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
          sensorMessenger = null;
          Log.d("SensorServiceConnection", "disconnected");
        }
      };

  protected void sendFrameNumberToSensorService(long frameNumber) {
    if (sensorMessenger != null) {
      Message msg = Message.obtain();
      Bundle bundle = new Bundle();
      bundle.putLong("frameNumber", frameNumber);
      bundle.putLong("timestamp", SystemClock.elapsedRealtimeNanos());
      msg.setData(bundle);
      msg.what = SensorService.MSG_FRAME;
      try {
        sensorMessenger.send(msg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  protected void sendInferenceTimeToSensorService(long frameNumber, long inferenceTime) {
    if (sensorMessenger != null) {
      Message msg = Message.obtain();
      Bundle bundle = new Bundle();
      bundle.putLong("frameNumber", frameNumber);
      bundle.putLong("inferenceTime", inferenceTime);
      msg.setData(bundle);
      msg.what = SensorService.MSG_INFERENCE;
      try {
        sensorMessenger.send(msg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  protected void sendControlToSensorService() {
    if (sensorMessenger != null) {
      Message msg = Message.obtain();
      msg.arg1 = (int) (vehicle.getLeftSpeed());
      msg.arg2 = (int) (vehicle.getRightSpeed());
      msg.what = SensorService.MSG_CONTROL;
      try {
        sensorMessenger.send(msg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  protected void sendIndicatorToSensorService() {
    if (sensorMessenger != null) {
      Message msg = Message.obtain();
      msg.arg1 = vehicle.getIndicator();
      msg.what = SensorService.MSG_INDICATOR;
      try {
        sensorMessenger.send(msg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  protected void sendVehicleDataToSensorService(long timestamp, String data, int type) {
    if (sensorMessenger != null) {
      Message msg = LogDataUtils.generateVehicleDataMessage(timestamp, data, type);
      try {
        sensorMessenger.send(msg);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  private void startLogging() {
    logFolder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .getAbsolutePath()
            + File.separator
            + getString(R.string.app_name)
            + File.separator
            + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    intentSensorService.putExtra("logFolder", logFolder + File.separator + "sensor_data");
    startService(intentSensorService);
    bindService(intentSensorService, sensorConnection, Context.BIND_AUTO_CREATE);
    // Send current vehicle state to log
    runInBackground(
        () -> {
          try {
            TimeUnit.MILLISECONDS.sleep(500);
            sendControlToSensorService();
            sendIndicatorToSensorService();
          } catch (InterruptedException e) {
            LOGGER.e(e, "Got interrupted.");
          }
        });
  }

  private void stopLogging() {
    if (sensorConnection != null) unbindService(sensorConnection);
    stopService(intentSensorService);

    // Pack and upload the collected data
    runInBackground(
        () -> {
          String logZipFile = logFolder + ".zip";
          // Zip the log folder and then delete it
          File folder = new File(logFolder);
          File zip = new File(logZipFile);
          try {
            TimeUnit.MILLISECONDS.sleep(500);
            ZipUtil.pack(folder, zip);
            org.zeroturnaround.zip.commons.FileUtils.deleteQuietly(folder);
            serverCommunication.upload(zip);
          } catch (InterruptedException e) {
            LOGGER.e(e, "Got interrupted.");
          }
        });
  }

  protected void setIsLoggingActive(boolean loggingActive) {
    if (loggingActive && !loggingEnabled) {
      if (!PermissionUtils.hasLoggingPermissions(this)) {
        PermissionUtils.requestLoggingPermissions(this);
        loggingEnabled = false;
      } else {
        startLogging();
        loggingEnabled = true;
      }
    } else if (!loggingActive && loggingEnabled) {
      stopLogging();
      loggingEnabled = false;
    }
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("LOGS", loggingEnabled));

    logSpinner.setEnabled(!loggingEnabled);
    if (loggingEnabled) logSpinner.setAlpha(0.5f);
    else logSpinner.setAlpha(1.0f);
    logSwitchCompat.setChecked(loggingEnabled);
    if (loggingEnabled) logSwitchCompat.setText(R.string.logging);
    else logSwitchCompat.setText(R.string.not_logging);
  }

  protected abstract void processImage();

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

  protected abstract void onInferenceConfigurationChanged();

  protected abstract void toggleNoise();

  protected abstract void updateVehicleControl();

  protected abstract void setNetworkEnabled(boolean isChecked);

  protected void toggleCamera(boolean isChecked) {
    LOGGER.d("Camera Toggled to " + isChecked);
    this.cameraSelection = getCameraUserSelection();
    this.setCameraSwitchText();
    this.setFragment();
    preferencesManager.setCameraSwitch(isChecked);
  }

  protected void toggleConnection(boolean isChecked) {
    if (isChecked) {
      if (!vehicle.isUsbConnected()) {
        Timber.d("isConnecting");
        vehicle.connectUsb();
      } else Timber.d("already connected");
    } else {
      vehicle.disconnectUsb();
    }
    // Disable baudrate selection if connected
    baudRateSpinner.setEnabled(!vehicle.isUsbConnected());
    if (vehicle.isUsbConnected()) baudRateSpinner.setAlpha(0.5f);
    else baudRateSpinner.setAlpha(1.0f);
    connectionSwitchCompat.setChecked(vehicle.isUsbConnected());

    if (vehicle.isUsbConnected()) {
      connectionSwitchCompat.setText(vehicle.getUsbConnection().getProductName());
    } else {
      connectionSwitchCompat.setText(R.string.no_device);
      // Tried to connect but failed
      if (isChecked) {
        Toast.makeText(getContext(), "Please check the USB connection.", Toast.LENGTH_SHORT).show();
      }
    }
  }

  public static Context getContext() {
    return context;
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (buttonView == connectionSwitchCompat) {
      toggleConnection(isChecked);
    } else if (buttonView == networkSwitchCompat) {
      setNetworkEnabled(isChecked);
    } else if (buttonView == logSwitchCompat) {
      setIsLoggingActive(isChecked);
    } else if (buttonView == cameraSwitchCompat) {
      toggleCamera(isChecked);
    }
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.plus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads >= 9) return;
      setNumThreads(++numThreads);
      threadsTextView.setText(String.valueOf(numThreads));
    } else if (v.getId() == R.id.minus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads == 1) return;
      setNumThreads(--numThreads);
      threadsTextView.setText(String.valueOf(numThreads));
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    String selected = parent.getItemAtPosition(pos).toString();
    if (parent == baudRateSpinner) {
      setBaudRate(Integer.parseInt(selected));
    } else if (parent == modelSpinner) {
      try {
        masterList.stream()
            .filter(f -> f.name.contains(selected))
            .findFirst()
            .ifPresent(this::setModel);

      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      }
    } else if (parent == deviceSpinner) {
      setDevice(Device.valueOf(selected.toUpperCase()));
    } else if (parent == logSpinner) {
      setLogMode(LogMode.valueOf(selected.toUpperCase()));
    } else if (parent == controlModeSpinner) {
      setControlMode(ControlMode.valueOf(selected.toUpperCase()));
    } else if (parent == driveModeSpinner) {
      setDriveMode(DriveMode.valueOf(parent.getItemAtPosition(pos).toString().toUpperCase()));
    } else if (parent == speedModeSpinner) {
      setSpeedMode(SpeedMode.valueOf(selected.toUpperCase()));
    }
  }

  /*
     Classes to handle events from a Controller.
     This can be the entry point to other external controllers
     See how PhoneController emits events.

     Expected JSON format:
     {command: "LOGS"}
        or
     {driveCmd: {l:0.2, r:-0.34}}
  */

  private void handleControllerEvents() {
    ControllerToBotEventBus.subscribe(
        this.getClass().getSimpleName(),
        event -> {
          JSONObject commandJsn = event;
          String commandType = "";
          Log.d(null, "Got command from controller: " + commandJsn.toString());
          if (commandJsn.has("command")) {
            commandType = commandJsn.getString("command");
          } else if (commandJsn.has("driveCmd")) {
            commandType = "DRIVE_CMD";
          }
          switch (commandType) {
            case "DRIVE_CMD":
              JSONObject driveValue = commandJsn.getJSONObject("driveCmd");
              controllerHandler.handleDriveCommand(
                  Float.valueOf(driveValue.getString("l")),
                  Float.valueOf(driveValue.getString("r")));
              break;

            case "LOGS":
              controllerHandler.handleLogging();
              break;

            case "NOISE":
              controllerHandler.handleNoise();
              break;

            case "INDICATOR_LEFT":
              controllerHandler.handleIndicatorLeft();
              break;

            case "INDICATOR_RIGHT":
              controllerHandler.handleIndicatorRight();
              break;

            case "INDICATOR_STOP":
              controllerHandler.handleIndicatorStop();
              break;

            case "NETWORK":
              controllerHandler.handleNetwork();
              break;

            case "DRIVE_MODE":
              controllerHandler.handleDriveMode();
              break;

              // We re connected to the controller, send back status info
            case "CONNECTED":
              // PhoneController class will receive this event and resent it to the
              // controller.
              // Other controllers can subscribe to this event as well.
              // That is why we are not calling phoneController.send() here directly.
              BotToControllerEventBus.emitEvent(
                  ConnectionUtils.getStatus(
                      loggingEnabled,
                      noiseEnabled,
                      networkEnabled,
                      driveMode.toString(),
                      vehicle.getIndicator()));

              break;

            case "DISCONNECTED":
              controllerHandler.handleDriveCommand(0.f, 0.f);
              setControlMode(ControlMode.GAMEPAD);
              break;
          }
        },
        error -> {
          Log.d(null, "Error occurred in ControllerToBotEventBus: " + error);
        },
        event -> event.has("command") || event.has("driveCmd") // filter everything else
        );
  }

  private void sendIndicatorStatus(Integer status) {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("INDICATOR_LEFT", status == -1));
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("INDICATOR_RIGHT", status == 1));
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("INDICATOR_STOP", status == 0));
  }

  // Controller event handler
  protected class ControllerHandler {

    protected void handleDriveCommand(Control control) {
      vehicle.setControl(control);
      updateVehicleControl();
    }

    protected void handleDriveCommand(Float l, Float r) {
      vehicle.setControl(l, r);
      updateVehicleControl();
    }

    protected void handleLogging() {
      setIsLoggingActive(!loggingEnabled);
      audioPlayer.playLogging(voice, loggingEnabled);
    }

    protected void handleNoise() {
      toggleNoise();
      audioPlayer.playNoise(voice, noiseEnabled);
    }

    protected void handleIndicatorLeft() {
      vehicle.setIndicator(Enums.VehicleIndicator.LEFT.getValue());
      if (loggingEnabled) {
        sendIndicatorToSensorService();
      }
      sendIndicatorStatus(vehicle.getIndicator());
    }

    protected void handleIndicatorRight() {
      vehicle.setIndicator(Enums.VehicleIndicator.RIGHT.getValue());
      if (loggingEnabled) {
        sendIndicatorToSensorService();
      }
      sendIndicatorStatus(vehicle.getIndicator());
    }

    protected void handleIndicatorStop() {
      vehicle.setIndicator(Enums.VehicleIndicator.STOP.getValue());
      if (loggingEnabled) {
        sendIndicatorToSensorService();
      }
      sendIndicatorStatus(vehicle.getIndicator());
    }

    protected void handleDriveMode() {
      if (networkEnabled) return;
      // Set next drive mode
      setDriveMode(DriveMode.values()[(driveMode.getValue() + 1) % DriveMode.values().length]);
      audioPlayer.playDriveMode(voice, driveMode);
    }

    protected void handleSpeedUp() {
      if (networkEnabled) return;
      // Increase speed
      switch (speedMode) {
        case SLOW:
          setSpeedMode(SpeedMode.NORMAL);
          break;
        case NORMAL:
          setSpeedMode(SpeedMode.FAST);
          break;
        default:
          break;
      }
      audioPlayer.playSpeedMode(voice, speedMode);
    }

    protected void handleSpeedDown() {
      if (networkEnabled) return;
      // Decrease speed
      switch (speedMode) {
        case FAST:
          setSpeedMode(SpeedMode.NORMAL);
          break;
        case NORMAL:
          setSpeedMode(SpeedMode.SLOW);
          break;
        default:
          break;
      }
      audioPlayer.playSpeedMode(voice, speedMode);
    }

    protected void handleNetwork() {
      setNetworkEnabled(!networkEnabled);
      if (networkEnabled) audioPlayer.play(voice, "network_enabled.mp3");
      else {
        // when network disabled play the active drive mode used for controller
        audioPlayer.playDriveMode(voice, driveMode);
      }
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }
}
