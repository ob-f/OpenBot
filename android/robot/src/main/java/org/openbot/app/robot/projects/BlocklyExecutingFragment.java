package org.openbot.app.robot.projects;

import static org.openbot.app.robot.pointGoalNavigation.PointGoalNavigationFragment.computeDeltaYaw;
import static org.openbot.app.robot.pointGoalNavigation.PointGoalNavigationFragment.convertRGBFrameToScaledBitmap;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;

import com.google.ar.core.Pose;
import com.google.ar.core.TrackingFailureReason;

import org.openbot.app.robot.R;
import org.openbot.app.robot.common.CameraFragment;
import org.openbot.app.robot.databinding.FragmentBlocklyExecutingBinding;
import org.openbot.app.robot.env.ImageUtils;
import org.openbot.app.robot.env.SharedPreferencesManager;
import org.openbot.app.robot.pointGoalNavigation.ArCore;
import org.openbot.app.robot.pointGoalNavigation.ArCoreListener;
import org.openbot.app.robot.pointGoalNavigation.CameraIntrinsics;
import org.openbot.app.robot.pointGoalNavigation.ImageFrame;
import org.openbot.app.robot.pointGoalNavigation.NavigationPoses;
import org.openbot.app.robot.tflite.Autopilot;
import org.openbot.app.robot.tflite.Detector;
import org.openbot.app.robot.tflite.Model;
import org.openbot.app.robot.tflite.Navigation;
import org.openbot.app.robot.tflite.Network;
import org.openbot.app.robot.tracking.MultiBoxTracker;
import org.openbot.app.robot.utils.CameraUtils;
import org.openbot.app.robot.utils.Enums;
import org.openbot.app.robot.utils.FileUtils;
import org.openbot.app.robot.vehicle.Control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class BlocklyExecutingFragment extends CameraFragment implements ArCoreListener {

  private FragmentBlocklyExecutingBinding binding;
  private WebView myWebView;
  private SharedPreferencesManager sharedPreferencesManager;
  private boolean isRunJSCommand = false;
  private boolean isTaskKeyDetected = false;
  private int frameCounter = 0;
  public static boolean isFollow = false;
  public static boolean isAutopilot = false;
  public static boolean isFollowMultipleObject = false;
  public static boolean isStartDetectorAutoPilot = false;
  public static boolean isOnDetection = false;
  private int numberOfFrames = 1;
  public static String classType = "person";
  public static String detectorModelName = "";
  public static String autoPilotModelName = "";

  public static String navigationModelName="";
  public static String startObject = "person";
  public static String stopObject = "person";
  public static String getTask = "";
  private Detector detector;
  private Autopilot autopilot;
  private Model setModel;
  private List<Model> modelList;
  private Bitmap detectorCroppedBitmap;
  private Bitmap autoPilotCroppedBitmap;
  private Matrix detectorFrameToCropTransform;
  private Matrix autoPilotFrameToCropTransform;
  private Network.Device getDevice;
  private int sensorOrientation;
  public MultiBoxTracker tracker;
  private long frameNum = 0;
  private boolean computingNetwork = false;
  private Handler handler;
  private ArCore arCore;
  public static Navigation navigationPolicy;
  public static boolean isRunning = false;
  private Matrix cropToFrameTransform;
  public static float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  public static TaskStorage taskStorage = new TaskStorage();

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    sharedPreferencesManager = new SharedPreferencesManager(requireContext());
    HandlerThread handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
    // initialise web view to execute javascript block codes.
    myWebView = new WebView(requireContext());
    // enable JavaScript in the web-view.
    myWebView.getSettings().setJavaScriptEnabled(true);
    if (savedInstanceState == null) {
      isRunJSCommand = true;
    } else {
      int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_LANDSCAPE
          || orientation == Configuration.ORIENTATION_PORTRAIT) {
           myWebView.destroy();
        showAlertDialog();
      }
    }
    // Inflate the layout for Blocks code executing Fragment.
    binding = FragmentBlocklyExecutingBinding.inflate(inflater, container, false);
    return inflateFragment(binding, inflater, container);
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Handler mainHandler = new Handler(Looper.getMainLooper());
    arCore = new ArCore(requireContext(), binding.GLSurfaceView, mainHandler);
    arCore.setArCoreListener(this);
    binding.stopCarBtn.setOnClickListener(
            v -> {
              myWebView.destroy();
              stop();
              binding.jsCommand.setText(getString(android.R.string.cancel));
              handleStopCarButtonClick();
            });
             binding.resetBtn.setOnClickListener(
            v -> {
              stop();
              binding.jsCommand.setText(getString(android.R.string.cancel));
              myWebView.destroy();
              myWebView = new WebView(requireContext());
              // enable JavaScript in the web-view.
              myWebView.getSettings().setJavaScriptEnabled(true);
              runJSCommand(BarCodeScannerFragment.finalCode);
              handelResetCarButtonClick();
            }
    );
    // Execute the JavaScript code if the js code variable is not null and isRunJSCommand is true
    if (BarCodeScannerFragment.finalCode != null && isRunJSCommand) {
      runJSCommand(BarCodeScannerFragment.finalCode);
    }

    modelList = FileUtils.loadConfigJSONFromAsset(requireActivity());

    // Get the selected device preference from the preferences manager.
    switch (preferencesManager.getDevice()){
      case 0:
        getDevice = Network.Device.CPU;
        break;
      case 1:
        getDevice = Network.Device.GPU;
        break;
      case 2:
        getDevice = Network.Device.NNAPI;
        break;
    }
  }

  /**
   * Stops the robot and performs cleanup actions.
   * - Destroys the WebView.
   * - Stops the robot's movement.
   */
  private void stop()
  {
    myWebView.destroy();
    vehicle.stopBot();
    vehicle.setIndicator(0);
    isAutopilot=false;
    isFollow=false;
    isFollowMultipleObject=false;
    isStartDetectorAutoPilot=false;
    isOnDetection=false;
  }

  /**
   * Transitions "Stop Car" button to "Back" text
   * and sets a listener to navigate back.
   */
  private void handleStopCarButtonClick(){
    binding.stopBtnName.setText("Back");
    binding.stopCarBtn.setOnClickListener(null);
    binding.stopCarBtn.setOnClickListener(v1 -> requireActivity().onBackPressed());
  }

  /**
   * Handles the click event of the "Reset Car" button.
   * Updates the "Stop Car" button text to its initial state.
   * Sets a listener to handle "Stop Car" button click, invoking
   * actions to navigate back and stop the robot.
   */
  private void handelResetCarButtonClick()
  {
    binding.stopBtnName.setText("Stop Car");
    binding.stopCarBtn.setOnClickListener(v -> {
      handleStopCarButtonClick();
      stop();
    });

  }
  @Override
  protected void processFrame(Bitmap bitmap, ImageProxy imageProxy) {
    // Check and execute modes based on blockly block code commands.
    if (isFollow) startFollowObject(bitmap);
    if (isAutopilot) startAutopilot(bitmap);
    if (isFollowMultipleObject) followMultipleObject(bitmap);
    if (isStartDetectorAutoPilot) startMultipleAi(bitmap);
    if (isOnDetection) onDetection(bitmap);
  }

  /**
   * Retrieves the model for object detection from the list.
   *
   * @return The selected model for object detection.
   */
  private Model getDetectorModel() {
    for (Model findModel : modelList){
      if (FileUtils.nameWithoutExtension(findModel.getName()).equals(detectorModelName)){
        setModel = findModel;
      }
    }
    return setModel;
  }

  /**
   * Retrieves the model for autopilot from the list.
   *
   * @return The selected model for autopilot.
   */
  private Model getAutoPilotModel() {
    for (Model findModel : modelList){
      if (FileUtils.nameWithoutExtension(findModel.getName()).equals(autoPilotModelName)){
        setModel = findModel;
      }
    }
    return setModel;
  }

  /**
   * Starts the autopilot mode using the blockly (enable autopilot) block code command.
   *
   * @param bitmap The input bitmap for autopilot.
   */
  private void startAutopilot(Bitmap bitmap){
    if (tracker == null) updateCropImageInfo();

    ++frameNum;
      // If network is busy, return.
      if (computingNetwork) {
        return;
      }

      computingNetwork = true;
      Timber.i("Putting image " + frameNum + " for detection in bg thread.");
      //System.out.println("frame number-------------"+ frameNum);


    if (handler != null) {
      handler.post(()->{
                final Canvas canvas = new Canvas(autoPilotCroppedBitmap);
        canvas.drawBitmap(bitmap, autoPilotFrameToCropTransform, null);

                if (autopilot != null) {
                  Timber.i("Running autopilot on image %s", frameNum);
                  vehicle.setControl(autopilot.recognizeImage(autoPilotCroppedBitmap, vehicle.getIndicator()));
                }
                computingNetwork = false;
              });}
  }

  /**
   * Starts the follow object mode using the (follow a object) blockly block code command.
   *
   * @param bitmap The input bitmap for object tracking.
   */
  private void startFollowObject(Bitmap bitmap) {
    if (tracker == null) updateCropImageInfo();

    ++frameNum;

    if (computingNetwork) {
      return;
    }

    computingNetwork = true;
    if (handler != null) {
      handler.post(()-> {

        final Canvas canvas = new Canvas(detectorCroppedBitmap);
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
          canvas.drawBitmap(
                  CameraUtils.flipBitmapHorizontal(bitmap), detectorFrameToCropTransform, null);
        } else {
          canvas.drawBitmap(bitmap, detectorFrameToCropTransform, null);
        }

        if (detector != null) {
          Timber.i("Running detection on image %s", frameNum);
          final List<Detector.Recognition> results =
                  detector.recognizeImage(detectorCroppedBitmap, classType);
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

          final List<Detector.Recognition> mappedRecognitions = new LinkedList<>();

          for (final Detector.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
              cropToFrameTransform.mapRect(location);
              result.setLocation(location);
              mappedRecognitions.add(result);
            //  System.out.println("confidence----->"+result.getConfidence());
            }
          }
          tracker.trackResults(mappedRecognitions, frameNum);
          if (isFollow) vehicle.setControl(tracker.updateTarget());
          else vehicle.stopBot();
        }

        computingNetwork = false;
      });
    }
  }

  /**
   * Follows multiple objects using the (follow and stop) blockly block code command.
   *
   * @param bitmap The input bitmap for object tracking.
   */
  private void followMultipleObject(Bitmap bitmap){
    if (tracker == null) updateCropImageInfo();

    ++frameNum;
    if (computingNetwork) {
      return;
    }
    computingNetwork = true;
        final Canvas canvas = new Canvas(detectorCroppedBitmap);
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
          canvas.drawBitmap(
                  CameraUtils.flipBitmapHorizontal(bitmap), detectorFrameToCropTransform, null);
        } else {
          canvas.drawBitmap(bitmap, detectorFrameToCropTransform, null);
        }

        if (detector != null) {
          Timber.i("Running detection on image %s", frameNum);
          final ArrayList<ArrayList<Detector.Recognition>> results =
                  detector.recognizeMultipleImage(detectorCroppedBitmap, startObject, stopObject);
          final List<Detector.Recognition> mappedRecognitions = new LinkedList<>();
          double startDistance = 0;
          double stopDistance = 0;
          for (final ArrayList<Detector.Recognition> result : results) {
            if (!result.isEmpty()){
              final RectF location = result.get(0).getLocation();
              if (location != null && result.get(0).getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                if (result.size() > 1){
                  if (result.get(0).getTitle().equals(startObject)) startDistance = Math.sqrt(Math.pow(location.centerX() - location.left, 2));
                  if (result.get(1).getTitle().equals(stopObject)) stopDistance = Math.sqrt(Math.pow(location.centerY() - location.right, 2));
                }
                cropToFrameTransform.mapRect(location);
                result.get(0).setLocation(location);
                mappedRecognitions.add(result.get(0));
              }
            }
          }
          if (tracker!=null) {
            tracker.trackResults(mappedRecognitions, frameNum);
            if ((startDistance >= stopDistance) && isFollowMultipleObject) vehicle.setControl(tracker.updateTarget());
            else runJSCommand(getTask);
          }
        }
    computingNetwork = false;
  }

  private void onDetection(Bitmap bitmap) {
    if (tracker == null) updateCropImageInfo();

    ++frameNum;
    if (computingNetwork) {
      return;
    }
    computingNetwork = true;
    if (handler != null) {
      handler.post(()-> {
        final Canvas canvas = new Canvas(detectorCroppedBitmap);
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
          canvas.drawBitmap(
                  CameraUtils.flipBitmapHorizontal(bitmap), detectorFrameToCropTransform, null);
        } else {
          canvas.drawBitmap(bitmap, detectorFrameToCropTransform, null);
        }

        if (detector != null) {
          Timber.i("Running detection on image %s", frameNum);
          final List<Detector.Recognition> results =
                  detector.recognizeImage(detectorCroppedBitmap, null);

          for (final Detector.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null) {
              // Retrieving tasks
              Map<String, Map<String, String>> taskData = taskStorage.getData();
              for (Map.Entry<String, Map<String, String>> entry : taskData.entrySet()) {
                if (result.getTitle().equals(entry.getKey()) && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                  frameCounter = 0;
                  isTaskKeyDetected = true;
                  Map<String, String> tasks = entry.getValue();
                  numberOfFrames = Integer.parseInt(Objects.requireNonNull(tasks.get("noOfFrames")));
                  runJSCommand(tasks.get("onDetect"));
                } else {
                  frameCounter ++;
                  if (isTaskKeyDetected && frameCounter >= numberOfFrames) {
                    Map<String, String> tasks = entry.getValue();
                    runJSCommand(tasks.get("onUnDetect"));
                  }
                }
              }
            }
          }
        }
          computingNetwork = false;
      });
    }
  }

  private void updateCropImageInfo() {
    detectorFrameToCropTransform = null;
    autoPilotFrameToCropTransform = null;

    sensorOrientation = 90 - ImageUtils.getScreenOrientation(requireActivity());

    tracker = new MultiBoxTracker(requireContext());
    tracker.setDynamicSpeed(preferencesManager.getDynamicSpeed());

    Timber.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    recreateNetwork(getDetectorModel(), getAutoPilotModel(), getDevice, preferencesManager.getNumThreads());
    if (detector == null) {
      Timber.e("No network on preview!");
      return;
    }

    tracker.setFrameConfiguration(
            getMaxAnalyseImageSize().getWidth(),
            getMaxAnalyseImageSize().getHeight(),
            sensorOrientation);
  }

  private void recreateNetwork(Model detectorModel, Model autoPilotModel, Network.Device device, int numThreads) {
    if (detectorModel == null && autoPilotModel == null) return;
    tracker.clearTrackedObjects();
    if (detector != null) {
      Timber.d("Closing detector.");
      detector.close();
      detector = null;
    }

    try {
      Timber.d("Creating detector (model=%s, device=%s, numThreads=%d)", detectorModel, device, numThreads);
      if (detectorModel != null) {
        detector = Detector.create(requireActivity(), detectorModel, device, numThreads);
        assert detector != null;
        detectorCroppedBitmap =
                Bitmap.createBitmap(
                        detector.getImageSizeX(), detector.getImageSizeY(), Bitmap.Config.ARGB_8888);

        detectorFrameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        getMaxAnalyseImageSize().getWidth(),
                        getMaxAnalyseImageSize().getHeight(),
                        detectorCroppedBitmap.getWidth(),
                        detectorCroppedBitmap.getHeight(),
                        sensorOrientation,
                        detector.getCropRect(),
                        detector.getMaintainAspect());

      }

      cropToFrameTransform = new Matrix();
      detectorFrameToCropTransform.invert(cropToFrameTransform);

      if (autoPilotModel != null) {
        autopilot = new Autopilot(requireActivity(), autoPilotModel, device, numThreads);
        autoPilotCroppedBitmap = Bitmap.createBitmap(
                autopilot.getImageSizeX(), autopilot.getImageSizeY(), Bitmap.Config.ARGB_8888);

        autoPilotFrameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        getMaxAnalyseImageSize().getWidth(),
                        getMaxAnalyseImageSize().getHeight(),
                        autoPilotCroppedBitmap.getWidth(),
                        autoPilotCroppedBitmap.getHeight(),
                        sensorOrientation,
                        autopilot.getCropRect(),
                        autopilot.getMaintainAspect());
      }

      cropToFrameTransform = new Matrix();
      autoPilotFrameToCropTransform.invert(cropToFrameTransform);

    } catch (IllegalArgumentException | IOException e) {
      Timber.e("Failed to create network.");
    }
  }

  /**
   * Starts both autopilot and object detection AI simultaneously using blockly block code command
   * to handle additional functionality upon detection.
   */
  private void startMultipleAi(Bitmap bitmap){
      if (tracker == null) updateCropImageInfo();

      ++frameNum;

      if (computingNetwork) {
        return;
      }

      computingNetwork = true;
      if (handler != null) {
        handler.post(()->{

          final Canvas detectorCanvas = new Canvas(detectorCroppedBitmap);
          if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            detectorCanvas.drawBitmap(
                    CameraUtils.flipBitmapHorizontal(bitmap), detectorFrameToCropTransform, null);
          } else {
            detectorCanvas.drawBitmap(bitmap, detectorFrameToCropTransform, null);
          }

          if (detector != null) {
            Timber.i("Running detection on image %s", frameNum);
            final List<Detector.Recognition> results =
                    detector.recognizeImage(detectorCroppedBitmap, classType);

              for (final Detector.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                  isStartDetectorAutoPilot = false;
                  vehicle.stopBot();
                  runJSCommand(getTask);
                }
              }
          }

          if (handler != null) {
            handler.post(()-> {
              final Canvas autoPilotCanvas = new Canvas(autoPilotCroppedBitmap);
              autoPilotCanvas.drawBitmap(bitmap, autoPilotFrameToCropTransform, null);

              if (autopilot != null) {
                Timber.i("Running autopilot on image %s", frameNum);
                 if (isStartDetectorAutoPilot) vehicle.setControl(autopilot.recognizeImage(autoPilotCroppedBitmap, vehicle.getIndicator()));
              }
            });}

          computingNetwork = false;
        });
      }
  }

  /**
   * Shows an alert dialog when phone rotation is detected during the execution of a Blockly project.
   */
  private void showAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    builder.setTitle("Rotation Detected").setMessage("Do you want to restart Blockly command?");
    builder.setPositiveButton("Yes", (dialog, id) -> restartJSCommand());
    builder.setNegativeButton("Cancel", (dialog, id) -> requireActivity().onBackPressed());
    AlertDialog dialog = builder.create();
    dialog.show();
    dialog.setCancelable(false);
  }

  /**
   * Restarts the Blockly project by initializing a WebView and executing JavaScript block codes.
   */
  @SuppressLint("SetJavaScriptEnabled")
  private void restartJSCommand() {
    // initialise web view to execute javascript block codes.
    myWebView = new WebView(requireContext());
    // enable JavaScript in the web-view.
    myWebView.getSettings().setJavaScriptEnabled(true);
    if (myWebView != null && BarCodeScannerFragment.finalCode != null) {
      runJSCommand(BarCodeScannerFragment.finalCode);
    }
  }

  /**
   * get javascript code in string from googleDrive file and run execute in webView.
   *
   */
  private void runJSCommand(String finalCode) {
    Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(
          () -> {
            // set the speed multiplier to maximum value (255) because openBot moving according to
            vehicle.setSpeedMultiplier(Enums.SpeedMode.FAST.getValue());
            // add a JavaScript interface to the web-view.
            myWebView.addJavascriptInterface(
                new BotFunctions(
                    vehicle,
                    audioPlayer,
                    sharedPreferencesManager,
                    requireContext(),
                    binding,
                    requireActivity(),
                    arCore),
                "Android");
            // execute the JavaScript code in the web-view.
            myWebView.evaluateJavascript(finalCode.replace("\n", "").replace("\r", ""), null);
          });
    }
  }

  @Override
  protected void processControllerKeyData(String command) {}

  @Override
  protected void processUSBData(String data) {}

  @Override
  public void onPause() {
    super.onPause();
    myWebView.destroy();
    vehicle.setIndicator(0);
    vehicle.stopBot();
    arCore.pause();
    isFollow = false;
    isAutopilot = false;
    isFollowMultipleObject = false;
  }

  /**
   * Handles AR Core updates for point goal navigation with Blockly block code commands.
   */
  @Override
  public void onArCoreUpdate(NavigationPoses navigationPoses, ImageFrame rgb, CameraIntrinsics cameraIntrinsics, long timestamp) {
    System.out.println("isRunning::::::;"+isRunning);
    if (isRunning) {
      float goalDistance =
              computeDistance(navigationPoses.getTargetPose(), navigationPoses.getCurrentPose());

      if (goalDistance < 0.15f) {
        vehicle.stopBot();
        audioPlayer.playFromStringID(R.string.goal_reached);
//        showInfoDialog(getString(R.string.goal_reached));
      } else {
        float deltaYaw =
                computeDeltaYaw(navigationPoses.getCurrentPose(), navigationPoses.getTargetPose());

        Bitmap bitmap = convertRGBFrameToScaledBitmap(rgb, 160.f / 480.f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 30, 160, 90);

        Control control =
                navigationPolicy.recognizeImage(
                        bitmap, goalDistance, (float) Math.sin(deltaYaw), (float) Math.cos(deltaYaw));

        Timber.d("control: (" + control.getLeft() + ", " + control.getRight() + ")");
        vehicle.setControl(control);
      }
    }
  }

  @Override
  public void onArCoreTrackingFailure(long timestamp, TrackingFailureReason trackingFailureReason) {}

  @Override
  public void onArCoreSessionPaused(long timestamp) {}

  private static float computeDistance(Pose goalPose, Pose robotPose) {
    Float dx = abs(goalPose.tx() - robotPose.tx());
    Float dz = abs(goalPose.tz() - robotPose.tz());
    return (float) Math.sqrt(dx * dx + dz * dz);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    arCore.closeSession();
  }

  @Override
  public void onStop() {
    super.onStop();
    arCore.removeArCoreListener();
  }
}
