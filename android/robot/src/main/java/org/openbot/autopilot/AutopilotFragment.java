package org.openbot.autopilot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageProxy;
import androidx.navigation.Navigation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.openbot.R;
import org.openbot.common.CameraFragment;
import org.openbot.databinding.FragmentAutopilotBinding;
import org.openbot.env.BorderedText;
import org.openbot.env.ImageUtils;
import org.openbot.tflite.Autopilot;
import org.openbot.tflite.Model;
import org.openbot.tflite.Network;
import org.openbot.tracking.MultiBoxTracker;
import org.openbot.utils.Constants;
import org.openbot.utils.Enums;
import org.openbot.utils.PermissionUtils;
import org.openbot.vehicle.Control;

import timber.log.Timber;

public class AutopilotFragment extends CameraFragment {

    // options for drop down in object nav?
    private FragmentAutopilotBinding binding;
    private Handler handler;
    private HandlerThread handlerThread;

    private long lastProcessingTimeMs;
    private boolean computingNetwork = false;

    private static final float TEXT_SIZE_DIP = 10;

    private Autopilot autopilot;

    private Matrix frameToCropTransform;
    private Bitmap croppedBitmap;
    private int sensorOrientation;

    private MultiBoxTracker tracker;

    private Model model;
    private Network.Device device = Network.Device.CPU;
    private int numThreads = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAutopilotBinding.inflate(inflater, container, false);

        return inflateFragment(binding, inflater, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.controllerContainer.speedInfo.setText(getString(R.string.speedInfo, "---,---"));

        binding.deviceSpinner.setSelection(preferencesManager.getDevice());
        setNumThreads(preferencesManager.getNumThreads());
        binding.threads.setText(String.valueOf(getNumThreads()));
        binding.cameraToggle.setOnClickListener(v -> toggleCamera());

        if (vehicle.getConnectionType().equals("USB")) {
            binding.usbToggle.setVisibility(View.VISIBLE);
            binding.bleToggle.setVisibility(View.GONE);
        } else if (vehicle.getConnectionType().equals("Bluetooth")) {
            binding.bleToggle.setVisibility(View.VISIBLE);
            binding.usbToggle.setVisibility(View.GONE);
        }
        List<String> models =
                getModelNames(f -> f.type.equals(Model.TYPE.CMDNAV) && f.pathType != Model.PATH_TYPE.URL);
        initModelSpinner(binding.modelSpinner, models, preferencesManager.getAutopilotModel());
        initServerSpinner(binding.serverSpinner);

        setAnalyserResolution(Enums.Preview.HD.getValue());
        binding.deviceSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        setDevice(Network.Device.valueOf(selected.toUpperCase()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        binding.plus.setOnClickListener(
                v -> {
                    String threads = binding.threads.getText().toString().trim();
                    int numThreads = Integer.parseInt(threads);
                    if (numThreads >= 9) return;
                    setNumThreads(++numThreads);
                    binding.threads.setText(String.valueOf(numThreads));
                });
        binding.minus.setOnClickListener(
                v -> {
                    String threads = binding.threads.getText().toString().trim();
                    int numThreads = Integer.parseInt(threads);
                    if (numThreads == 1) return;
                    setNumThreads(--numThreads);
                    binding.threads.setText(String.valueOf(numThreads));
                });
        BottomSheetBehavior.from(binding.aiBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

        mViewModel
                .getUsbStatus()
                .observe(getViewLifecycleOwner(), status -> binding.usbToggle.setChecked(status));

        binding.usbToggle.setChecked(vehicle.isUsbConnected());
        binding.bleToggle.setChecked(vehicle.bleConnected());

        binding.usbToggle.setOnClickListener(
                v -> {
                    binding.usbToggle.setChecked(vehicle.isUsbConnected());
                    Navigation.findNavController(requireView()).navigate(R.id.open_usb_fragment);
                });

        binding.bleToggle.setOnClickListener(
                v -> {
                    binding.bleToggle.setChecked(vehicle.bleConnected());
                    Navigation.findNavController(requireView()).navigate(R.id.open_bluetooth_fragment);
                });

        setSpeedMode(Enums.SpeedMode.getByID(preferencesManager.getSpeedMode()));
        setControlMode(Enums.ControlMode.getByID(preferencesManager.getControlMode()));
        setDriveMode(Enums.DriveMode.getByID(preferencesManager.getDriveMode()));

        binding.controllerContainer.controlMode.setOnClickListener(
                v -> {
                    Enums.ControlMode controlMode =
                            Enums.ControlMode.getByID(preferencesManager.getControlMode());
                    if (controlMode != null) setControlMode(Enums.switchControlMode(controlMode));
                });
        binding.controllerContainer.driveMode.setOnClickListener(
                v -> setDriveMode(Enums.switchDriveMode(vehicle.getDriveMode())));

        binding.controllerContainer.speedMode.setOnClickListener(
                v ->
                        setSpeedMode(
                                Enums.toggleSpeed(
                                        Enums.Direction.CYCLIC.getValue(),
                                        Enums.SpeedMode.getByID(preferencesManager.getSpeedMode()))));

        binding.autoSwitch.setOnClickListener(v -> setNetworkEnabled(binding.autoSwitch.isChecked()));
    }

    private void updateCropImageInfo() {
        //    Timber.i("%s x %s",getPreviewSize().getWidth(), getPreviewSize().getHeight());
        //    Timber.i("%s x %s",getMaxAnalyseImageSize().getWidth(),
        // getMaxAnalyseImageSize().getHeight());
        frameToCropTransform = null;

        sensorOrientation = 90 - ImageUtils.getScreenOrientation(requireActivity());

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        BorderedText borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(requireContext());

        Timber.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        recreateNetwork(getModel(), getDevice(), getNumThreads());
        if (autopilot == null) {
            Timber.e("No network on preview!");
            return;
        }

        binding.trackingOverlay.addCallback(
                canvas -> {
                    if (tracker != null) {
                        tracker.draw(canvas);
                    }
                });
        tracker.setFrameConfiguration(
                getMaxAnalyseImageSize().getWidth(),
                getMaxAnalyseImageSize().getHeight(),
                sensorOrientation);
    }

    protected void onInferenceConfigurationChanged() {
        computingNetwork = false;
        if (croppedBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
        final Network.Device device = getDevice();
        final Model model = getModel();
        final int numThreads = getNumThreads();
        runInBackground(() -> recreateNetwork(model, device, numThreads));
    }

    private void recreateNetwork(Model model, Network.Device device, int numThreads) {
        if (model == null) return;
        tracker.clearTrackedObjects();
        if (autopilot != null) {
            Timber.d("Closing autoPilot.");
            autopilot.close();
            autopilot = null;
        }

        try {
            Timber.d(
                    "Creating autopilot (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
            autopilot = new Autopilot(requireActivity(), model, device, numThreads);
            croppedBitmap =
                    Bitmap.createBitmap(
                            autopilot.getImageSizeX(), autopilot.getImageSizeY(), Bitmap.Config.ARGB_8888);
            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            getMaxAnalyseImageSize().getWidth(),
                            getMaxAnalyseImageSize().getHeight(),
                            croppedBitmap.getWidth(),
                            croppedBitmap.getHeight(),
                            sensorOrientation,
                            autopilot.getCropRect(),
                            autopilot.getMaintainAspect());
            requireActivity()
                    .runOnUiThread(
                            () ->
                                    binding.inputResolution.setText(
                                            String.format(
                                                    Locale.getDefault(),
                                                    "%dx%d",
                                                    autopilot.getImageSizeX(),
                                                    autopilot.getImageSizeY())));

            Matrix cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);

        } catch (IllegalArgumentException | IOException e) {
            String msg = "Failed to create network.";
            Timber.e(e, msg);
            requireActivity()
                    .runOnUiThread(
                            () ->
                                    Toast.makeText(
                                                    requireContext().getApplicationContext(),
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG)
                                            .show());
        }
    }

    @Override
    public synchronized void onResume() {
        croppedBitmap = null;
        tracker = null;
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        binding.bleToggle.setChecked(vehicle.bleConnected());
        super.onResume();
    }

    @Override
    public synchronized void onPause() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    protected void processUSBData(String data) {
        binding.controllerContainer.speedInfo.setText(
                getString(
                        R.string.speedInfo,
                        String.format(
                                Locale.US, "%3.0f,%3.0f", vehicle.getLeftWheelRpm(), vehicle.getRightWheelRpm())));
    }

    @Override
    protected void processKeyEvent(KeyEvent keyCode) {
        if (binding.autoSwitch.isChecked()
                && (keyCode.getKeyCode() == KeyEvent.KEYCODE_BUTTON_THUMBL
                || keyCode.getKeyCode() == KeyEvent.KEYCODE_BUTTON_THUMBR)) {
            audioPlayer.playFromString("Autopilot active. Cannot change speed mode.");
        } else {
            super.processKeyEvent(keyCode);
        }
    }

    @Override
    protected void processControllerKeyData(String commandType) {
        switch (commandType) {
            case Constants.CMD_DRIVE:
                binding.controllerContainer.controlInfo.setText(
                        String.format(Locale.US, "%.0f,%.0f", vehicle.getLeftSpeed(), vehicle.getRightSpeed()));
                break;

            case Constants.CMD_DRIVE_MODE:
                setDriveMode(Enums.switchDriveMode(vehicle.getDriveMode()));
                break;

            case Constants.CMD_SPEED_DOWN:
                setSpeedMode(
                        Enums.toggleSpeed(
                                Enums.Direction.DOWN.getValue(),
                                Enums.SpeedMode.getByID(preferencesManager.getSpeedMode())));
                break;

            case Constants.CMD_SPEED_UP:
                setSpeedMode(
                        Enums.toggleSpeed(
                                Enums.Direction.UP.getValue(),
                                Enums.SpeedMode.getByID(preferencesManager.getSpeedMode())));
                break;

            case Constants.CMD_NETWORK:
                setNetworkEnabledWithAudio(!binding.autoSwitch.isChecked());
                break;
        }
    }

    private void setNetworkEnabledWithAudio(boolean b) {
        setNetworkEnabled(b);

        if (b) {
            audioPlayer.play(voice, "network_enabled.mp3");
            runInBackground(
                    () -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(lastProcessingTimeMs);
                            vehicle.setControl(0, 0);
                            requireActivity()
                                    .runOnUiThread(() -> binding.inferenceInfo.setText(R.string.time_fps));
                        } catch (InterruptedException e) {
                            Timber.e(e, "Got interrupted.");
                        }
                    });
        } else audioPlayer.playDriveMode(voice, vehicle.getDriveMode());
    }

    private void setNetworkEnabled(boolean b) {
        binding.autoSwitch.setChecked(b);
        binding.controllerContainer.controlMode.setEnabled(!b);
        binding.controllerContainer.driveMode.setEnabled(!b);
        binding.controllerContainer.speedMode.setEnabled(!b);
        binding.controllerContainer.controlMode.setAlpha(b ? 0.5f : 1f);
        binding.controllerContainer.driveMode.setAlpha(b ? 0.5f : 1f);
        binding.controllerContainer.speedMode.setAlpha(b ? 0.5f : 1f);

        if (Enums.DriveMode.getByID(preferencesManager.getDriveMode()) != null) {
            if (Enums.DriveMode.getByID(preferencesManager.getDriveMode()) == Enums.DriveMode.DUAL && Enums.ControlMode.getByID(preferencesManager.getControlMode()) == Enums.ControlMode.PHONE) {
                binding.controllerContainer.driveMode.setAlpha(0.5f);
                binding.controllerContainer.driveMode.setEnabled(false);
            } else if (Enums.DriveMode.getByID(preferencesManager.getDriveMode()) == Enums.DriveMode.GAME && Enums.ControlMode.getByID(preferencesManager.getControlMode()) == Enums.ControlMode.WEBSERVER) {
                binding.controllerContainer.driveMode.setAlpha(0.5f);
                binding.controllerContainer.driveMode.setEnabled(false);
            }
        }

        if (!b) {
            setSpeedMode(Enums.SpeedMode.getByID(preferencesManager.getSpeedMode()));
            handler.postDelayed(() -> vehicle.setControl(0, 0), 500);
        } else {
            binding.controllerContainer.speedMode.setImageResource(R.drawable.ic_speed_high);
            vehicle.setSpeedMultiplier(Enums.SpeedMode.FAST.getValue());
        }
    }

    private long frameNum = 0;

    @Override
    protected void processFrame(Bitmap bitmap, ImageProxy image) {
        if (tracker == null) updateCropImageInfo();

        ++frameNum;
        if (binding != null && binding.autoSwitch.isChecked()) {
            // If network is busy, return.
            if (computingNetwork) {
                return;
            }

            computingNetwork = true;
            Timber.i("Putting image " + frameNum + " for detection in bg thread.");

            runInBackground(
                    () -> {
                        final Canvas canvas = new Canvas(croppedBitmap);
                        canvas.drawBitmap(bitmap, frameToCropTransform, null);

                        if (autopilot != null) {
                            Timber.i("Running autopilot on image %s", frameNum);
                            final long startTime = SystemClock.elapsedRealtime();
                            handleDriveCommand(autopilot.recognizeImage(croppedBitmap, vehicle.getIndicator()));
                            lastProcessingTimeMs = SystemClock.elapsedRealtime() - startTime;
                        }

                        computingNetwork = false;
                    });
            if (lastProcessingTimeMs > 0)
                requireActivity()
                        .runOnUiThread(
                                () ->
                                        binding.inferenceInfo.setText(
                                                String.format(Locale.US, "%d fps", 1000 / lastProcessingTimeMs)));
        }
    }

    protected void handleDriveCommand(Control control) {
        vehicle.setControl(control);
        float left = vehicle.getLeftSpeed();
        float right = vehicle.getRightSpeed();
        requireActivity()
                .runOnUiThread(
                        () ->
                                binding.controllerContainer.controlInfo.setText(
                                        String.format(Locale.US, "%.0f,%.0f", left, right)));
    }

    @Override
    public void onConnectionEstablished(String ipAddress) {
        requireActivity().runOnUiThread(() -> binding.ipAddress.setText(ipAddress));
    }

    protected Model getModel() {
        return model;
    }

    private void connectWebController() {
        phoneController.connectWebServer();
        // Currently only dual drive mode supported
        setDriveMode(Enums.DriveMode.GAME);
        binding.controllerContainer.driveMode.setAlpha(0.5f);
        binding.controllerContainer.driveMode.setEnabled(false);
    }

    protected void setModel(Model model) {
        if (this.model != model) {
            Timber.d("Updating  model: %s", model);
            this.model = model;
            preferencesManager.setAutopilotModel(model.name);
            onInferenceConfigurationChanged();
        }
    }

    protected Network.Device getDevice() {
        return device;
    }

    private void setDevice(Network.Device device) {
        if (this.device != device) {
            Timber.d("Updating  device: %s", device);
            this.device = device;
            final boolean threadsEnabled = device == Network.Device.CPU;
            binding.plus.setEnabled(threadsEnabled);
            binding.minus.setEnabled(threadsEnabled);
            binding.threads.setText(threadsEnabled ? String.valueOf(numThreads) : "N/A");
            if (threadsEnabled) binding.threads.setTextColor(Color.BLACK);
            else binding.threads.setTextColor(Color.GRAY);
            preferencesManager.setDevice(device.ordinal());
            onInferenceConfigurationChanged();
        }
    }

    protected int getNumThreads() {
        return numThreads;
    }

    private void setNumThreads(int numThreads) {
        if (this.numThreads != numThreads) {
            Timber.d("Updating  numThreads: %s", numThreads);
            this.numThreads = numThreads;
            preferencesManager.setNumThreads(numThreads);
            onInferenceConfigurationChanged();
        }
    }

    private void setSpeedMode(Enums.SpeedMode speedMode) {
        if (speedMode != null && !binding.autoSwitch.isChecked()) {
            switch (speedMode) {
                case SLOW:
                    binding.controllerContainer.speedMode.setImageResource(R.drawable.ic_speed_low);
                    break;
                case NORMAL:
                    binding.controllerContainer.speedMode.setImageResource(R.drawable.ic_speed_medium);
                    break;
                case FAST:
                    binding.controllerContainer.speedMode.setImageResource(R.drawable.ic_speed_high);
                    break;
            }

            Timber.d("Updating  controlSpeed: %s", speedMode);
            preferencesManager.setSpeedMode(speedMode.getValue());
            vehicle.setSpeedMultiplier(speedMode.getValue());
        }
    }

    private void setControlMode(Enums.ControlMode controlMode) {
        if (controlMode != null) {
            switch (controlMode) {
                case GAMEPAD:
                    binding.controllerContainer.controlMode.setImageResource(R.drawable.ic_controller);
                    disconnectPhoneController();
                    break;
                case PHONE:
                    binding.controllerContainer.controlMode.setImageResource(R.drawable.ic_phone);
                    if (!PermissionUtils.hasControllerPermissions(requireActivity()))
                        requestPermissionLauncher.launch(Constants.PERMISSIONS_CONTROLLER);
                    else connectPhoneController();
                    break;
                case WEBSERVER:
                    binding.controllerContainer.controlMode.setImageResource(R.drawable.ic_server);
                    if (!PermissionUtils.hasControllerPermissions(requireActivity()))
                        requestPermissionLauncher.launch(Constants.PERMISSIONS_CONTROLLER);
                    else connectWebController();
                    break;
            }
            Timber.d("Updating  controlMode: %s", controlMode);
            preferencesManager.setControlMode(controlMode.getValue());
        }
    }

    protected void setDriveMode(Enums.DriveMode driveMode) {
        if (driveMode != null) {
            switch (driveMode) {
                case DUAL:
                    binding.controllerContainer.driveMode.setImageResource(R.drawable.ic_dual);
                    break;
                case GAME:
                    binding.controllerContainer.driveMode.setImageResource(R.drawable.ic_game);
                    break;
                case JOYSTICK:
                    binding.controllerContainer.driveMode.setImageResource(R.drawable.ic_joystick);
                    break;
            }

            Timber.d("Updating  driveMode: %s", driveMode);
            vehicle.setDriveMode(driveMode);
            preferencesManager.setDriveMode(driveMode.getValue());
        }
    }

    private void connectPhoneController() {
        phoneController.connect(requireContext());
        // Currently only dual drive mode supported
        setDriveMode(Enums.DriveMode.DUAL);
        binding.controllerContainer.driveMode.setAlpha(0.5f);
        binding.controllerContainer.driveMode.setEnabled(false);
    }

    private void disconnectPhoneController() {
        phoneController.disconnect();
        setDriveMode(Enums.DriveMode.getByID(preferencesManager.getDriveMode()));
        binding.controllerContainer.driveMode.setEnabled(true);
        binding.controllerContainer.driveMode.setAlpha(1.0f);
    }
}
