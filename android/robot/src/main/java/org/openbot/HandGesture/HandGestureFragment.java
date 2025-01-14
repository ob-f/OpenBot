package org.openbot.HandGesture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.openbot.R;
import org.openbot.common.CameraFragment;
import org.openbot.databinding.FragmentHandGestureBinding;
import timber.log.Timber;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
//see if need
import org.openbot.env.BorderedText;
import org.openbot.env.ImageUtils;
import org.openbot.tflite.Model;
import org.openbot.tflite.Network;
import org.openbot.tracking.MultiBoxTracker;
import org.openbot.utils.CameraUtils;
import org.openbot.utils.Constants;
import org.openbot.utils.Enums;
import org.openbot.utils.PermissionUtils;
import org.openbot.vehicle.Control;

//mediapipe here
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutioncore.ResultListener;
import com.google.mediapipe.solutions.hands.HandsResult;

public class HandGestureFragment extends CameraFragment {

    private FragmentHandGestureBinding binding;
    private Handler handler;
    private HandlerThread handlerThread;
    private Hands hands;
    private boolean computingNetwork = false;
    private static final float TEXT_SIZE_DIP = 10;
    private boolean mirrorControl;
    private Matrix frameToCropTransform;
    private Bitmap croppedBitmap;
    private int sensorOrientation;
    private MultiBoxTracker tracker;

    private Model model;
    private Network.Device device = Network.Device.CPU;
    private int numThreads = -1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(
            @NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHandGestureBinding.inflate(inflater, container, false);
        return inflateFragment(binding, inflater, container);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Example initialization of vehicle (ensure usbManager and usbDevice are available)
        vehicle = new Vehicle(getContext(), usbManager, usbDevice);

        if (vehicle == null) {
            Timber.e("Vehicle initialization failed.");
        } else {
            Timber.i("Vehicle initialized successfully.");
        }

        binding.controllerContainer.speedInfo.setText(getString(R.string.speedInfo, "---,---"));

        if (vehicle.getConnectionType().equals("USB")) {
            binding.usbToggle.setVisibility(View.VISIBLE);
            binding.bleToggle.setVisibility(View.GONE);
        } else if (vehicle.getConnectionType().equals("Bluetooth")) {
            binding.bleToggle.setVisibility(View.VISIBLE);
            binding.usbToggle.setVisibility(View.GONE);
        }

        binding.deviceSpinner.setSelection(preferencesManager.getDevice());
        setNumThreads(preferencesManager.getNumThreads());
        binding.threads.setText(String.valueOf(getNumThreads()));

        binding.cameraToggle.setOnClickListener(v -> toggleCamera());

        binding.mirrorControl.setOnClickListener(v -> mirrorControl());

        List<String> models =
                getModelNames(f -> f.type.equals(Model.TYPE.DETECTOR) && f.pathType != Model.PATH_TYPE.URL);
        initModelSpinner(binding.modelSpinner, models, preferencesManager.getGestureModel());

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

    private void mirrorControl() {
        mirrorControl = !mirrorControl;
    }

    private void updateCropImageInfo() {
        //    Timber.i("%s x %s",getPreviewSize().getWidth(), getPreviewSize().getHeight());
        //    Timber.i("%s x %s",getMaxAnalyseImageSize().getWidth(),
        //     getMaxAnalyseImageSize().getHeight());
        frameToCropTransform = null;

        sensorOrientation = 90 - ImageUtils.getScreenOrientation(requireActivity());

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        BorderedText borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(requireContext());
        tracker.setDynamicSpeed(preferencesManager.getDynamicSpeed());

        Timber.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        recreateNetwork(getModel(), getDevice(), getNumThreads());
        if (detector == null) {
            Timber.e("No network on preview!");
            return;
        }

        binding.trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas);
                    //          tracker.drawDebug(canvas);
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
        resetFpsUi();
        if (model == null) return;
        tracker.clearTrackedObjects();
        if (detector != null) {
            Timber.d("Closing detector.");
            detector.close();
            detector = null;
        }

        try {
            Timber.d("Creating detector (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
            detector = Detector.create(requireActivity(), model, device, numThreads);

            assert detector != null;
            croppedBitmap =
                    Bitmap.createBitmap(
                            detector.getImageSizeX(), detector.getImageSizeY(), Bitmap.Config.ARGB_8888);
            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            getMaxAnalyseImageSize().getWidth(),
                            getMaxAnalyseImageSize().getHeight(),
                            croppedBitmap.getWidth(),
                            croppedBitmap.getHeight(),
                            sensorOrientation,
                            detector.getCropRect(),
                            detector.getMaintainAspect());

            cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);

            requireActivity()
                    .runOnUiThread(
                            () -> {
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(
                                                getContext(),
                                                android.R.layout.simple_dropdown_item_1line,
                                                detector.getLabels());
                                binding.classType.setAdapter(adapter);
                                binding.classType.setSelection(
                                        detector.getLabels().indexOf(preferencesManager.getObjectType()));
                                binding.inputResolution.setText(
                                        String.format(
                                                Locale.getDefault(),
                                                "%dx%d",
                                                detector.getImageSizeX(),
                                                detector.getImageSizeY()));
                            });

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
    protected void processControllerKeyData(String commandType) {
        switch (commandType) {
            case Constants.CMD_DRIVE:
                binding.controllerContainer.controlInfo.setText(
                        String.format(Locale.US, "%.0f,%.0f", vehicle.getLeftSpeed(), vehicle.getRightSpeed()));
                break;

            case Constants.CMD_NETWORK:
                setNetworkEnabledWithAudio(!binding.autoSwitch.isChecked());
                break;
        }
    }

    private void setNetworkEnabledWithAudio(boolean b) {
        setNetworkEnabled(b);

        if (b) audioPlayer.play(voice, "network_enabled.mp3");
        else audioPlayer.playDriveMode(voice, vehicle.getDriveMode());
    }

    private void setNetworkEnabledWithAudio(boolean b) {
        setNetworkEnabled(b);

        if (b) audioPlayer.play(voice, "network_enabled.mp3");
        else audioPlayer.playDriveMode(voice, vehicle.getDriveMode());
    }

    private void setNetworkEnabled(boolean b) {
        binding.autoSwitch.setChecked(b);

        binding.controllerContainer.controlMode.setEnabled(!b);
        binding.controllerContainer.driveMode.setEnabled(!b);
        binding.controllerContainer.speedMode.setEnabled(!b);

        binding.controllerContainer.controlMode.setAlpha(b ? 0.5f : 1f);
        binding.controllerContainer.driveMode.setAlpha(b ? 0.5f : 1f);
        binding.controllerContainer.speedMode.setAlpha(b ? 0.5f : 1f);

        resetFpsUi();
        if (!b) handler.postDelayed(() -> vehicle.setControl(0, 0), Math.max(lastProcessingTimeMs, 50));
    }

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
                        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            canvas.drawBitmap(
                                    CameraUtils.flipBitmapHorizontal(bitmap), frameToCropTransform, null);
                        } else {
                            canvas.drawBitmap(bitmap, frameToCropTransform, null);
                        }

                        if (detector != null) {
                            Timber.i("Running detection on image %s", frameNum);
                            final long startTime = SystemClock.elapsedRealtime();
                            final List<Detector.Recognition> results =
                                    detector.recognizeImage(croppedBitmap, classType);
                            lastProcessingTimeMs = SystemClock.elapsedRealtime() - startTime;

                            if (!results.isEmpty())
                                Timber.i(
                                        "Object: "
                                                + results.get(0).getLocation().centerX()
                                                + ", "
                                                + results.get(0).getLocation().centerY()
                                                + ", "
                                                + results.get(0).getLocation().height()
                                                + ", "
                                                + results.get(0).getLocation().width());

                            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                            final Canvas canvas1 = new Canvas(cropCopyBitmap);
                            final Paint paint = new Paint();
                            paint.setColor(Color.RED);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(2.0f);

                            final List<Detector.Recognition> mappedRecognitions = new LinkedList<>();

                            for (final Detector.Recognition result : results) {
                                final RectF location = result.getLocation();
                                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                                    canvas1.drawRect(location, paint);
                                    cropToFrameTransform.mapRect(location);
                                    result.setLocation(location);
                                    mappedRecognitions.add(result);
                                }
                            }

                            tracker.trackResults(mappedRecognitions, frameNum);
                            Control target = tracker.updateTarget();
                            if (mirrorControl) {
                                handleDriveCommand(target.mirror());
                            } else {
                                handleDriveCommand(target);
                            }
                            binding.trackingOverlay.postInvalidate();
                        }

                        computingNetwork = false;
                    });
            if (lastProcessingTimeMs > 0) {
                if (isBenchmarkMode) {
                    double avgProcessingTimeMs = movingAvgProcessingTimeMs.next(lastProcessingTimeMs);
                    processedFrames += 1;
                    if (processedFrames >= movingAvgSize) updateFpsUi(avgProcessingTimeMs);
                } else updateFpsUi(lastProcessingTimeMs);
            }
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

    protected Model getModel() {
        return model;
    }


    protected void setModel(Model model) {
        if (this.model != model) {
            Timber.d("Updating model: %s", model);
            this.model = model;
            preferencesManager.setGestureModel(model.name);
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
        if (speedMode != null) {
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
        Enums.DriveMode oldDriveMode = currentDriveMode;
        // Currently only dual drive mode supported
        setDriveMode(Enums.DriveMode.DUAL);
        binding.controllerContainer.driveMode.setAlpha(0.5f);
        binding.controllerContainer.driveMode.setEnabled(false);
        preferencesManager.setDriveMode(oldDriveMode.getValue());
    }

    private void connectWebController() {
        phoneController.connectWebServer();
        Enums.DriveMode oldDriveMode = currentDriveMode;
        // Currently only dual drive mode supported
        setDriveMode(Enums.DriveMode.GAME);
        binding.controllerContainer.driveMode.setAlpha(0.5f);
        binding.controllerContainer.driveMode.setEnabled(false);
        preferencesManager.setDriveMode(oldDriveMode.getValue());
    }

    private void disconnectPhoneController() {
        phoneController.disconnect();
        setDriveMode(Enums.DriveMode.getByID(preferencesManager.getDriveMode()));
        binding.controllerContainer.driveMode.setEnabled(true);
        binding.controllerContainer.driveMode.setAlpha(1.0f);
    }

    private void initializeHands() {
        // Configure HandsOptions for the MediaPipe Hands instance
        hands = new Hands(
                requireContext(),
                HandsOptions.builder()
                        .setStaticImageMode(false)            // Real-time mode
                        .setMaxNumHands(1)                    // Detect a single hand
                        .setMinDetectionConfidence(0.5f)      // Detection threshold
                        .setMinTrackingConfidence(0.5f)       // Tracking threshold
                        .build()
        );

        hands.setResultListener(handsResult -> {
            if (!handsResult.multiHandLandmarks().isEmpty()) {
                // Get the confidence score of the first detected hand
                float confidenceScore = handsResult.multiHandWorldLandmarks().get(0).getScore();

                // Check if confidence is above the minimum threshold
                if (confidenceScore >= minimumGestureConfidence) {
                    handleHandGestures(handsResult);
                } else {
                    // Log when detected hands don't meet the confidence threshold
                    Timber.d("Hand detected but below confidence threshold: %.2f", confidenceScore);
                }
            } else {
                // Log when no hands are detected
                Timber.d("No hands detected.");
            }
        });
    }

    // Method to handle hand gestures
    private void handleHandGestures(HandsResult handsResult) {
        if (handsResult == null || handsResult.multiHandLandmarks().isEmpty()) return;

        List<NormalizedLandmark> handLandmarks = handsResult.multiHandLandmarks().get(0).getLandmarkList();
        if (handLandmarks == null || handLandmarks.isEmpty()) return;

        // Gesture detection logic
        if (isThumbsUpGesture(handLandmarks)) {
            Timber.d("Thumbs up gesture detected. Moving robot forward.");
            moveRobotForward();
        } else if (isOpenPalmGesture(handLandmarks)) {
            Timber.d("Open palm gesture detected. Stopping robot.");
            stopRobot();
        }
    }

    private boolean isThumbsUpGesture(List<NormalizedLandmark> landmarks) {
        float thumbTipY = landmarks.get(4).getY();  // Thumb tip
        float indexTipY = landmarks.get(8).getY(); // Index finger tip
        float middleTipY = landmarks.get(12).getY(); // Middle finger tip
        float ringTipY = landmarks.get(16).getY(); // Ring finger tip
        float pinkyTipY = landmarks.get(20).getY(); // Pinky finger tip

        // Check if thumb is above other fingers
        boolean thumbAboveIndex = thumbTipY < indexTipY;
        boolean thumbAboveMiddle = thumbTipY < middleTipY;
        boolean thumbAboveRing = thumbTipY < ringTipY;
        boolean thumbAbovePinky = thumbTipY < pinkyTipY;

        // Check if all other fingers are curled (lower than their base landmarks)
        boolean fingersCurled = indexTipY > landmarks.get(5).getY() && // Base of index
                middleTipY > landmarks.get(9).getY() && // Base of middle
                ringTipY > landmarks.get(13).getY() && // Base of ring
                pinkyTipY > landmarks.get(17).getY(); // Base of pinky

        return thumbAboveIndex && thumbAboveMiddle && thumbAboveRing && thumbAbovePinky && fingersCurled;
    }

    private boolean isOpenPalmGesture(List<NormalizedLandmark> landmarks) {
        float wristY = landmarks.get(0).getY(); // Wrist
        float thumbTipY = landmarks.get(4).getY(); // Thumb tip
        float indexTipY = landmarks.get(8).getY(); // Index finger tip
        float middleTipY = landmarks.get(12).getY(); // Middle finger tip
        float ringTipY = landmarks.get(16).getY(); // Ring finger tip
        float pinkyTipY = landmarks.get(20).getY(); // Pinky finger tip

        // Check if all finger tips are above the wrist
        boolean isThumbExtended = thumbTipY < wristY;
        boolean isIndexExtended = indexTipY < wristY;
        boolean isMiddleExtended = middleTipY < wristY;
        boolean isRingExtended = ringTipY < wristY;
        boolean isPinkyExtended = pinkyTipY < wristY;

        // Check distances between finger tips to ensure the hand is open
        float thumbIndexDistance = Math.abs(landmarks.get(4).getX() - landmarks.get(8).getX());
        float indexMiddleDistance = Math.abs(landmarks.get(8).getX() - landmarks.get(12).getX());
        float middleRingDistance = Math.abs(landmarks.get(12).getX() - landmarks.get(16).getX());
        float ringPinkyDistance = Math.abs(landmarks.get(16).getX() - landmarks.get(20).getX());

        boolean fingersSpreadApart = thumbIndexDistance > 0.1 &&
                indexMiddleDistance > 0.1 &&
                middleRingDistance > 0.1 &&
                ringPinkyDistance > 0.1;

        return isThumbExtended && isIndexExtended && isMiddleExtended && isRingExtended && isPinkyExtended && fingersSpreadApart;
    }

    private void moveRobotForward() {
        if (vehicle != null) {
            vehicle.moveForward();
        } else {
            Timber.e("Vehicle object is null. Cannot move robot.");
        }
    }

    // Helper method to stop the robot
    private void stopRobot() {
        if (vehicle != null) {
            vehicle.stop();
        } else {
            Timber.e("Vehicle object is null. Cannot stop robot.");
        }
    }
}


