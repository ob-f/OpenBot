package org.openbot.common;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.openbot.R;
import org.openbot.env.AudioPlayer;
import org.openbot.env.BotToControllerEventBus;
import org.openbot.env.ControllerToBotEventBus;
import org.openbot.env.PhoneController;
import org.openbot.env.SharedPreferencesManager;
import org.openbot.main.MainViewModel;
import org.openbot.server.ServerCommunication;
import org.openbot.server.ServerListener;
import org.openbot.tflite.Model;
import org.openbot.utils.ConnectionUtils;
import org.openbot.utils.Constants;
import org.openbot.utils.Enums;
import org.openbot.utils.FileUtils;
import org.openbot.utils.FormatUtils;
import org.openbot.utils.PermissionUtils;
import org.openbot.vehicle.Control;
import org.openbot.vehicle.Vehicle;
import timber.log.Timber;

public abstract class ControlsFragment extends Fragment implements ServerListener {
  private static final String NO_SERVER = "No server";

  protected MainViewModel mViewModel;
  protected Vehicle vehicle;
  protected Animation startAnimation;
  protected SharedPreferencesManager preferencesManager;
  protected PhoneController phoneController;
  protected Enums.DriveMode currentDriveMode = Enums.DriveMode.GAME;

  protected AudioPlayer audioPlayer;

  protected final String voice = "matthew";
  protected List<Model> masterList;

  protected ServerCommunication serverCommunication;

  private ArrayAdapter<String> modelAdapter;
  private ArrayAdapter<String> serverAdapter;
  private Spinner modelSpinner;
  private Spinner serverSpinner;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // create before inflateFragment() to prevent npe when calling addCamera()
    preferencesManager = new SharedPreferencesManager(requireContext());
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    requireActivity()
        .getWindow()
        .addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    phoneController = PhoneController.getInstance(requireContext());

    audioPlayer = new AudioPlayer(requireContext());
    masterList = FileUtils.loadConfigJSONFromAsset(requireActivity());
    serverCommunication = new ServerCommunication(requireContext(), this);

    requireActivity()
        .getSupportFragmentManager()
        .setFragmentResultListener(
            Constants.GENERIC_MOTION_EVENT,
            this,
            (requestKey, result) -> {
              MotionEvent motionEvent = result.getParcelable(Constants.DATA);
              vehicle.setControl(vehicle.getGameController().processJoystickInput(motionEvent, -1));
              processControllerKeyData(Constants.CMD_DRIVE);
            });
    requireActivity()
        .getSupportFragmentManager()
        .setFragmentResultListener(
            Constants.KEY_EVENT,
            this,
            (requestKey, result) -> {
              KeyEvent event = result.getParcelable(Constants.DATA);
              if (KeyEvent.ACTION_UP == event.getAction()) {
                processKeyEvent(result.getParcelable(Constants.DATA));
              }
              Control newControl =
                  vehicle
                      .getGameController()
                      .processButtonInput(result.getParcelable(Constants.DATA));
              if (vehicle.getControl().getLeft() != newControl.getLeft()
                  && vehicle.getControl().getRight() != newControl.getRight()) {
                vehicle.setControl(newControl);
              }
            });

    mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    vehicle = mViewModel.getVehicle().getValue();
    startAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink);

    mViewModel
        .getDeviceData()
        .observe(
            getViewLifecycleOwner(),
            data -> {
              char header = data.charAt(0);
              String body = data.substring(1);

              switch (header) {
                case 'r':
                  vehicle.setReady(true);
                  break;
                case 'f':
                  vehicle.processVehicleConfig(body);
                  break;
                case 'v':
                  if (FormatUtils.isNumeric(body)) {
                    vehicle.setBatteryVoltage(Float.parseFloat(body));
                  } else {
                    String[] msgParts = body.split(":");
                    switch (msgParts[0]) {
                      case "min":
                        vehicle.setMinMotorVoltage(Float.parseFloat(msgParts[1]));
                      case "low":
                        vehicle.setLowBatteryVoltage(Float.parseFloat(msgParts[1]));
                        break;
                      case "max":
                        vehicle.setMaxBatteryVoltage(Float.parseFloat(msgParts[1]));
                        break;
                      default:
                        Toast.makeText(
                                requireContext().getApplicationContext(),
                                "Invalid voltage message received!",
                                Toast.LENGTH_SHORT)
                            .show();
                        break;
                    }
                  }
                  break;
                case 's':
                  vehicle.processSonarMessage(body);
                  break;
                case 'w':
                  String[] itemList = body.split(",");
                  if (itemList.length == 2
                      && FormatUtils.isNumeric(itemList[0])
                      && FormatUtils.isNumeric(itemList[1])) {
                    vehicle.setLeftWheelRpm(Float.parseFloat(itemList[0]));
                    vehicle.setRightWheelRpm(Float.parseFloat(itemList[1]));
                  }
                  break;
                case 'b':
                  // do nothing
                  break;
                case '!':
                  vehicle.recordFirmwareError(data);
                  break;
              }

              processUSBData(data);
            });

    handlePhoneControllerEvents();
  }

  protected void processKeyEvent(KeyEvent keyCode) {
    if (Enums.ControlMode.getByID(preferencesManager.getControlMode())
        == Enums.ControlMode.GAMEPAD) {
      switch (keyCode.getKeyCode()) {
        case KeyEvent.KEYCODE_BUTTON_X: // square
          toggleIndicatorEvent(Enums.VehicleIndicator.LEFT.getValue());
          processControllerKeyData(Constants.CMD_INDICATOR_LEFT);
          break;
        case KeyEvent.KEYCODE_BUTTON_Y: // triangle
          toggleIndicatorEvent(Enums.VehicleIndicator.STOP.getValue());
          processControllerKeyData(Constants.CMD_INDICATOR_STOP);
          break;
        case KeyEvent.KEYCODE_BUTTON_B: // circle
          toggleIndicatorEvent(Enums.VehicleIndicator.RIGHT.getValue());
          processControllerKeyData(Constants.CMD_INDICATOR_RIGHT);
          break;
        case KeyEvent.KEYCODE_BUTTON_A: // x
          processControllerKeyData(Constants.CMD_LOGS);
          break;
        case KeyEvent.KEYCODE_BUTTON_START: // options
          toggleNoise();
          processControllerKeyData(Constants.CMD_NOISE);
          break;
        case KeyEvent.KEYCODE_BUTTON_L1:
          processControllerKeyData(Constants.CMD_DRIVE_MODE);
          audioPlayer.playDriveMode(voice, vehicle.getDriveMode());
          break;
        case KeyEvent.KEYCODE_BUTTON_R1:
          processControllerKeyData(Constants.CMD_NETWORK);
          break;
        case KeyEvent.KEYCODE_BUTTON_THUMBL:
          processControllerKeyData(Constants.CMD_SPEED_DOWN);
          audioPlayer.playSpeedMode(
              voice, Enums.SpeedMode.getByID(preferencesManager.getSpeedMode()));
          break;
        case KeyEvent.KEYCODE_BUTTON_THUMBR:
          processControllerKeyData(Constants.CMD_SPEED_UP);

          audioPlayer.playSpeedMode(
              voice, Enums.SpeedMode.getByID(preferencesManager.getSpeedMode()));
          break;

        default:
          break;
      }
    }
  }

  private void handlePhoneControllerEvents() {
    ControllerToBotEventBus.subscribe(
        this.getClass().getSimpleName(),
        event -> {
          String commandType = "";
          if (event.has("command")) {
            commandType = event.getString("command");
          } else if (event.has("driveCmd")) {
            commandType = Constants.CMD_DRIVE;
          } else if (event.has("server")) {
            for (int i = 0; i < serverSpinner.getAdapter().getCount(); i++) {
              if (event.getString("server").equals("noServerFound")) {
                serverSpinner.setSelection(0);
              } else if (event.getString("server").equals(serverSpinner.getAdapter().getItem(i))) {
                serverSpinner.setSelection(i);
              }
            }
          }

          switch (commandType) {
            case Constants.CMD_DRIVE:
              JSONObject driveValue = event.getJSONObject("driveCmd");

              vehicle.setControl(
                  new Control(
                      Float.parseFloat(driveValue.getString("l")),
                      Float.parseFloat(driveValue.getString("r"))));
              break;

            case Constants.CMD_INDICATOR_LEFT:
              toggleIndicatorEvent(Enums.VehicleIndicator.LEFT.getValue());
              break;

            case Constants.CMD_INDICATOR_RIGHT:
              toggleIndicatorEvent(Enums.VehicleIndicator.RIGHT.getValue());
              break;

            case Constants.CMD_INDICATOR_STOP:
              toggleIndicatorEvent(Enums.VehicleIndicator.STOP.getValue());
              break;

              // We re connected to the controller, send back status info
            case Constants.CMD_CONNECTED:
              // PhoneController class will receive this event and resent it to the
              // controller.
              // Other controllers can subscribe to this event as well.
              // That is why we are not calling phoneController.send() here directly.
              BotToControllerEventBus.emitEvent(
                  ConnectionUtils.getStatus(
                      false, false, false, currentDriveMode.toString(), vehicle.getIndicator()));
              break;

            case Constants.CMD_DISCONNECTED:
              vehicle.setControl(0, 0);
              break;
          }

          processControllerKeyData(commandType);
        },
        error -> {
          Log.d(null, "Error occurred in ControllerToBotEventBus: " + error);
        },
        event ->
            event.has("command")
                || event.has("driveCmd")
                || event.has("server") // filter out everything else
        );
  }

  protected void toggleNoise() {
    vehicle.toggleNoise();
    BotToControllerEventBus.emitEvent(
        ConnectionUtils.createStatus("NOISE", vehicle.isNoiseEnabled()));
    audioPlayer.playNoise(voice, vehicle.isNoiseEnabled());
  }

  private void toggleIndicatorEvent(int value) {
    vehicle.setIndicator(value);
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("INDICATOR_LEFT", value == -1));
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("INDICATOR_RIGHT", value == 1));
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("INDICATOR_STOP", value == 0));
  }

  private boolean allGranted = true;
  protected final ActivityResultLauncher<String[]> requestPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(),
          result -> {
            result.forEach((permission, granted) -> allGranted = allGranted && granted);

            if (allGranted) phoneController.connect(requireContext());
            else {
              PermissionUtils.showControllerPermissionsToast(requireActivity());
            }
          });

  @NotNull
  protected List<String> getModelNames(Predicate<Model> filter) {
    return masterList.stream()
        .filter(filter)
        .map(f -> FileUtils.nameWithoutExtension(f.name))
        .collect(Collectors.toList());
  }

  @Override
  public void onResume() {
    serverCommunication.start();
    super.onResume();
  }

  @Override
  public void onDestroy() {
    Timber.d("onDestroy");
    ControllerToBotEventBus.unsubscribe(this.getClass().getSimpleName());
    vehicle.setControl(0, 0);
    super.onDestroy();
  }

  @Override
  public synchronized void onPause() {
    Timber.d("onPause");
    serverCommunication.stop();
    vehicle.setControl(0, 0);
    super.onPause();
  }

  @Override
  public void onStop() {
    Timber.d("onStop");
    super.onStop();
  }

  protected void initModelSpinner(Spinner spinner, List<String> models, String selected) {
    modelAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, models);
    modelAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    modelSpinner = spinner;
    modelSpinner.setAdapter(modelAdapter);
    if (!selected.isEmpty())
      modelSpinner.setSelection(
          Math.max(0, modelAdapter.getPosition(FileUtils.nameWithoutExtension(selected))));
    modelSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selected = parent.getItemAtPosition(position).toString();
            try {
              masterList.stream()
                  .filter(f -> f.name.contains(selected))
                  .findFirst()
                  .ifPresent(value -> setModel(value));

            } catch (IllegalArgumentException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
  }

  protected void initServerSpinner(Spinner spinner) {
    serverAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item);
    serverAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    serverSpinner = spinner;
    serverSpinner.setAdapter(serverAdapter);
    serverSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selected = parent.getItemAtPosition(position).toString();
            if (selected.equals(NO_SERVER)) {
              serverCommunication.disconnect();
              if (serverAdapter.getPosition(preferencesManager.getServer()) > -1) {
                preferencesManager.setServer(selected);
              }
            } else {
              serverCommunication.connect(selected);
              preferencesManager.setServer(selected);
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            serverCommunication.disconnect();
          }
        });
    onServerListChange(serverCommunication.getServers());
  }

  @Override
  public void onServerListChange(Set<String> servers) {
    if (serverAdapter == null) {
      return;
    }
    requireActivity()
        .runOnUiThread(
            () -> {
              serverAdapter.clear();
              serverAdapter.add(NO_SERVER);
              serverAdapter.addAll(servers);
              if (!preferencesManager.getServer().isEmpty()) {
                serverSpinner.setSelection(
                    Math.max(0, serverAdapter.getPosition(preferencesManager.getServer())));
              }
            });
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
            requireActivity().getFilesDir() + File.separator + model,
            "256x96");

    if (modelAdapter != null && modelAdapter.getPosition(model) == -1) {
      modelAdapter.add(model);
      masterList.add(item);
      FileUtils.updateModelConfig(requireActivity(), requireContext(), masterList, false);
    } else {
      if (model.equals(modelSpinner.getSelectedItem())) {
        setModel(item);
      }
    }
    Toast.makeText(
            requireContext().getApplicationContext(),
            "AutopilotModel added: " + model,
            Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void onRemoveModel(String model) {
    if (modelAdapter != null && modelAdapter.getPosition(model) != -1) {
      modelAdapter.remove(model);
    }
    Toast.makeText(
            requireContext().getApplicationContext(),
            "AutopilotModel removed: " + model,
            Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void onConnectionEstablished(String ipAddress) {}

  protected void setModel(Model model) {}

  protected abstract void processControllerKeyData(String command);

  protected abstract void processUSBData(String data);
}
