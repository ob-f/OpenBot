package org.openbot.app.robot.pointGoalNavigation;

import android.content.Context;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Size;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.Config.LightEstimationMode;
import com.google.ar.core.Config.PlaneFindingMode;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotTrackingException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.SessionPausedException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.openbot.app.robot.pointGoalNavigation.rendering.BackgroundRenderer;
import org.openbot.app.robot.pointGoalNavigation.rendering.DisplayRotationHelper;
import org.openbot.app.robot.pointGoalNavigation.rendering.TwoDRenderer;
import timber.log.Timber;

/* This class is used to integrate ARCore
Map Marker PNG Image obtained
From: http://www.pngall.com/map-marker-png/download/17543
 */

public class ArCore implements GLSurfaceView.Renderer {

  private DisplayRotationHelper displayRotationHelper;
  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private Context appContext;
  private Session session;
  private Camera camera;
  private final TwoDRenderer twoDRenderer = new TwoDRenderer();
  private volatile Pose currentPose;
  private ArCoreListener arCoreListener = null;
  private final float[] anchorMatrix = new float[16];
  private Anchor startAnchor;
  // Set on the GL thread (tap) and cleared on the UI thread (Stop), so read it once into a local.
  private volatile Anchor targetAnchor;
  private Handler handlerMain;
  private GLSurfaceView surfaceView;
  private boolean renderFrame = true;
  private Pose startPose = null, targetPose = null;
  // Written on the UI thread, consumed on the GL thread.
  private volatile float[] pendingTap = null;
  // Floor detection costs CPU, so only the tap-to-goal screen turns it on.
  private boolean goalPlacementEnabled = false;
  // The marker quad reaches this far below its center; lift it so the pin tip touches the floor.
  private static final float MARKER_TIP_OFFSET_M = 0.1f;

  private static final String PREFS = "point_goal_navigation";
  private static final String PREF_CAMERA_HEIGHT = "camera_height_m";
  private static final int CALIBRATE_EVERY_FRAMES = 15;
  // Height of the phone camera above the floor. Distances come from it, so it is re-measured
  // from ARCore's floor plane whenever one is found and remembered for next time.
  private float cameraHeightM;
  private boolean cameraHeightMeasured = false;
  private int frameCount = 0;

  public ArCore(Context context, GLSurfaceView surfaceView, Handler handlerMain) {
    this.appContext = context;
    displayRotationHelper = new DisplayRotationHelper(context);
    this.handlerMain = handlerMain;
    this.surfaceView = surfaceView;

    // set up renderer
    this.surfaceView.setPreserveEGLContextOnPause(true);
    this.surfaceView.setEGLContextClientVersion(2);
    this.surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    this.surfaceView.setRenderer(this);
    this.surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    this.surfaceView.setWillNotDraw(false);
    displayRotationHelper = new DisplayRotationHelper(appContext);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(appContext);
    } catch (IOException e) {
      Timber.e(e, "Failed to create background renderer");
    }
    twoDRenderer.createOnGlThread(appContext, "render/gmap_marker.png");
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    // The surfaceView is left at its XML match_parent size (full screen) rather than resized to
    // the camera's native capture aspect ratio - BackgroundRenderer.draw() already renders a
    // full-viewport quad and relies on Frame#transformCoordinates2d to crop/rotate the camera
    // texture to fill whatever the real viewport is, which is the standard ARCore pattern.
    // Shrinking the surface to match the camera's aspect ratio here fought that: it produced
    // letterboxing and, combined with DisplayRotationHelper/Session#setDisplayGeometry being fed
    // a viewport that didn't match the true full-screen size, incorrect marker projection too.
    Timber.d("onSurfaceChanged: gl surface (%d, %d)", width, height);
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  public Pose getStartPose() {
    return startAnchor == null ? null : startAnchor.getPose();
  }

  public void setStartAnchorAtCurrentPose() {
    if (currentPose != null) {
      startAnchor = session.createAnchor(currentPose);
    }
  }

  public Pose getTargetPose() {
    Anchor target = targetAnchor;
    return target == null ? null : target.getPose();
  }

  public void setTargetAnchor(Pose pose) {
    targetAnchor = session.createAnchor(pose);
  }

  public void setTargetAnchorAtCurrentPose() {
    targetAnchor = session.createAnchor(currentPose);
  }

  /**
   * Requests that the given screen-space tap (in {@link #surfaceView} pixel coordinates) be turned
   * into a goal pose on the next rendered frame. The actual hit-test runs on the GL thread inside
   * {@link #onDrawFrame}, since it needs the current frame's {@link Frame} object.
   */
  public void requestTapAt(float screenX, float screenY) {
    pendingTap = new float[] {screenX, screenY};
  }

  /** Turns on floor detection for tap-to-goal (camera height). Call before {@link #resume()}. */
  public void setGoalPlacementEnabled(boolean enabled) {
    goalPlacementEnabled = enabled;
    cameraHeightM =
        appContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(PREF_CAMERA_HEIGHT, GoalValidator.DEFAULT_CAMERA_HEIGHT_M);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    session.setCameraTextureName(backgroundRenderer.getTextureId());

    // Obtain the current frame from ARSession. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    Frame frame = null;
    try {
      frame = session.update();
    } catch (CameraNotAvailableException e) {
      Timber.d(e, "ARCore camera not available.");

      runOnMainThread(
          () -> {
            if (arCoreListener != null) {
              arCoreListener.onArCoreTrackingFailure(
                  SystemClock.elapsedRealtimeNanos(), TrackingFailureReason.CAMERA_UNAVAILABLE);
            }
          });

      // Stop here since no camera is available and no rendering is possible.
      return;
    } catch (SessionPausedException e) {
      Timber.d(e, "ARCore session paused.");

      runOnMainThread(
          () -> {
            if (arCoreListener != null) {
              arCoreListener.onArCoreSessionPaused(SystemClock.elapsedRealtimeNanos());
            }
          });

      return;
    }

    camera = frame.getCamera();
    long timestamp = SystemClock.elapsedRealtimeNanos();
    // There is also a frame.getTimestamp method. However, the time base is not defined, so it might
    // be different than SystemClock;
    // see https://developers.google.com/ar/reference/java/com/google/ar/core/Frame#getTimestamp()

    TrackingState trackingState = camera.getTrackingState();
    if (trackingState != TrackingState.TRACKING) {
      Timber.d("ARCore is not tracking.");

      TrackingFailureReason trackingFailureReason = camera.getTrackingFailureReason();
      runOnMainThread(
          () -> {
            if (arCoreListener != null) {
              arCoreListener.onArCoreTrackingFailure(timestamp, trackingFailureReason);
            }
          });
    } else {
      Timber.d("ARCore is tracking.");

      // Get poses.
      currentPose = camera.getPose();
      if (startAnchor != null) {
        startPose = startAnchor.getPose();
      }
      Anchor target = targetAnchor;
      if (target != null) {
        targetPose = target.getPose();
      }

      if (goalPlacementEnabled && frameCount++ % CALIBRATE_EVERY_FRAMES == 0) {
        calibrateCameraHeight();
      }

      float[] tap = pendingTap;
      if (tap != null) {
        pendingTap = null;
        placeGoalFromTap(tap[0], tap[1]);
      }

      // Get image.
      // TODO: If needed, we could possibly implement some performance optimization here:
      // Do we really need to copy here? We could use several cycling textures instead of only
      // 1 texture. This would allow to close the image faster.
      Image image = null;
      try {
        image = frame.acquireCameraImage();
      } catch (NotYetAvailableException e) {
        Timber.d(e, "ARCore image not available.");
      }

      // Send arcore data
      if (image != null) {
        // TODO: We could use another data structure here to avoid additional copying later.
        // (Important: We need to be fast here such that `image` is closed as fast
        // as possible. Hence, we currently use ByteBuffer.get() and .put() to copy in a fast way.
        // Is there a faster way?)
        ImageFrame imageFrame = new ImageFrame(image);

        runOnMainThread(
            () -> {
              if (arCoreListener != null) {
                arCoreListener.onArCoreUpdate(
                    new NavigationPoses(currentPose, targetPose, startPose),
                    imageFrame,
                    new CameraIntrinsics(camera.getImageIntrinsics()),
                    timestamp);
              }
            });
      }

      // Release image
      if (image != null) {
        image.close();
      }
    }

    if (renderFrame) {
      backgroundRenderer.draw(frame);

      // visualize goal
      Anchor marker = targetAnchor;
      if (marker != null && trackingState == TrackingState.TRACKING) {
        // Get projection matrix.
        float[] projmtx = new float[16];
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

        // Get camera matrix and draw.
        float[] viewmtx = new float[16];
        camera.getViewMatrix(viewmtx, 0);

        // Compute lighting from average intensity of the image.
        // The first three components are color scaling factors.
        // The last one is the average pixel intensity in gamma space.
        final float[] colorCorrectionRgba = new float[4];
        frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

        // Stand the pin upright in the world (local +y = world up) and turn it about the
        // vertical axis to face the camera, so it looks right however the phone is mounted:
        // portrait, landscape or upside down. Its tip sits on the goal spot on the floor.
        float[] goal = marker.getPose().getTranslation();
        float yaw = (float) Math.atan2(currentPose.tx() - goal[0], currentPose.tz() - goal[2]);
        Pose markerPose =
            new Pose(
                new float[] {goal[0], goal[1] + MARKER_TIP_OFFSET_M, goal[2]},
                new float[] {0f, (float) Math.sin(yaw / 2), 0f, (float) Math.cos(yaw / 2)});
        markerPose.toMatrix(anchorMatrix, 0);
        twoDRenderer.updateModelMatrix(anchorMatrix, 1.0f);
        twoDRenderer.draw(viewmtx, projmtx);
      }
    }
  }

  /**
   * The goal is where the tap ray meets the floor, with the floor at the known camera height below
   * the phone. This only needs the camera's orientation (gravity is exact from the IMU), so it's
   * correct from the first frame. Image depth is not used: a phone standing still on the robot has
   * no parallax yet, so early depth guesses are off and get corrected as it drives, which made the
   * goal jump.
   */
  private void placeGoalFromTap(float tapX, float tapY) {
    float[] cameraPos = currentPose.getTranslation();
    float[][] ray = tapRay(tapX, tapY);
    float[] goal =
        ray == null
            ? null
            : GoalValidator.intersectFloor(ray[0], ray[1], cameraPos[1] - cameraHeightM);

    GoalValidator.Result result = GoalValidator.validate(cameraPos, goal);
    Timber.d(
        "goal tap (%.0f, %.0f): camera=%s height=%.3f (%s) goal=%s -> %s",
        tapX,
        tapY,
        Arrays.toString(cameraPos),
        cameraHeightM,
        cameraHeightMeasured ? "measured" : "saved",
        Arrays.toString(goal),
        result);
    if (result != GoalValidator.Result.OK) {
      notifyRejected(result);
      return;
    }

    try {
      // A plain anchor at a world position stays fixed; it doesn't follow the screen.
      Anchor newAnchor = session.createAnchor(Pose.makeTranslation(goal[0], goal[1], goal[2]));
      Anchor previous = targetAnchor;
      targetAnchor = newAnchor;
      if (previous != null) previous.detach();
      targetPose = newAnchor.getPose();
      float distance = GoalValidator.horizontalDistance(cameraPos, goal);
      runOnMainThread(
          () -> {
            if (arCoreListener != null) arCoreListener.onGoalPlaced(distance);
          });
    } catch (NotTrackingException e) {
      Timber.d(e, "ARCore lost tracking while placing goal anchor.");
    }
  }

  /** Measures the camera height from ARCore's floor plane, if it sees one. */
  private void calibrateCameraHeight() {
    float cameraY = currentPose.ty();
    Float floorY = GoalValidator.pickFloorY(collectUpwardPlanes(), cameraY);
    if (floorY == null) return;
    float height = cameraY - floorY;
    // Only heights a phone on the robot can have; not while someone holds it up by hand.
    if (height < GoalValidator.MIN_MOUNT_HEIGHT_M || height > GoalValidator.MAX_MOUNT_HEIGHT_M) {
      return;
    }
    if (!cameraHeightMeasured || Math.abs(height - cameraHeightM) > 0.005f) {
      Timber.d("camera height measured: %.3f m", height);
      appContext
          .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
          .edit()
          .putFloat(PREF_CAMERA_HEIGHT, height)
          .apply();
    }
    cameraHeightM = height;
    cameraHeightMeasured = true;
  }

  /** World-space ray {origin, direction} through a screen pixel. */
  private float[][] tapRay(float tapX, float tapY) {
    int width = surfaceView.getWidth();
    int height = surfaceView.getHeight();
    if (width == 0 || height == 0) return null;

    float[] projection = new float[16];
    camera.getProjectionMatrix(projection, 0, 0.1f, 100.0f);
    float[] view = new float[16];
    camera.getViewMatrix(view, 0);
    float[] viewProjection = new float[16];
    Matrix.multiplyMM(viewProjection, 0, projection, 0, view, 0);
    float[] inverse = new float[16];
    if (!Matrix.invertM(inverse, 0, viewProjection, 0)) return null;

    float ndcX = 2f * tapX / width - 1f;
    float ndcY = 1f - 2f * tapY / height;
    float[] near = unproject(inverse, ndcX, ndcY, -1f);
    float[] far = unproject(inverse, ndcX, ndcY, 1f);
    float[] direction = {far[0] - near[0], far[1] - near[1], far[2] - near[2]};
    float length =
        (float)
            Math.sqrt(
                direction[0] * direction[0]
                    + direction[1] * direction[1]
                    + direction[2] * direction[2]);
    if (length < 1e-6f) return null;
    for (int i = 0; i < 3; i++) direction[i] /= length;
    return new float[][] {near, direction};
  }

  private static float[] unproject(float[] inverse, float x, float y, float z) {
    float[] in = {x, y, z, 1f};
    float[] out = new float[4];
    Matrix.multiplyMV(out, 0, inverse, 0, in, 0);
    return new float[] {out[0] / out[3], out[1] / out[3], out[2] / out[3]};
  }

  private void notifyRejected(GoalValidator.Result result) {
    runOnMainThread(
        () -> {
          if (arCoreListener != null) arCoreListener.onGoalRejected(result);
        });
  }

  /** Returns {y, area} for every tracked upward-facing plane that hasn't been merged away. */
  private List<float[]> collectUpwardPlanes() {
    List<float[]> planes = new ArrayList<>();
    for (Plane plane : session.getAllTrackables(Plane.class)) {
      if (plane.getTrackingState() != TrackingState.TRACKING
          || plane.getSubsumedBy() != null
          || plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) {
        continue;
      }
      planes.add(new float[] {plane.getCenterPose().ty(), plane.getExtentX() * plane.getExtentZ()});
    }
    return planes;
  }

  protected synchronized void runOnMainThread(final Runnable r) {
    new Thread(
            () -> {
              if (handlerMain != null) {
                handlerMain.post(r);
              }
            })
        .start();
  }

  public void resume()
      throws UnavailableSdkTooOldException, UnavailableDeviceNotCompatibleException,
          UnavailableArcoreNotInstalledException, UnavailableApkTooOldException,
          CameraNotAvailableException {
    session = new Session(appContext);

    setConfig();
    setCameraConfig();

    // Note that order matters - see the note in onPause(), the reverse applies here.
    session.resume();
    surfaceView.onResume();
    displayRotationHelper.onResume();
  }

  // A smaller GPU texture size might increase performance; see
  // https://developers.google.cn/ar/develop/c/camera-configs?hl=en
  private boolean isSmaller(Size size1, Size size2) {
    if (size1.getHeight() * size1.getWidth() < size2.getHeight() * size2.getWidth()) {
      return true;
    } else {
      return false;
    }
  }

  // 640x480 is the default CPU stream. Requesting another CPU image size might affect the
  // performance; see
  // https://developers.google.cn/ar/develop/c/recording-and-playback/introduction?hl=en#primary_video_track_cpu_image_track
  private boolean is640x480(Size size) {
    if (size.getWidth() == 640 && size.getHeight() == 480) {
      return true;
    } else {
      return false;
    }
  }

  private void setCameraConfig() throws CameraNotAvailableException {
    // INFO: To require depth sensor add
    // `.setDepthSensorUsage(EnumSet.of(CameraConfig.DepthSensorUsage.REQUIRE_AND_USE))`
    CameraConfigFilter cameraConfigFilter =
        new CameraConfigFilter(session)
            .setFacingDirection(CameraConfig.FacingDirection.BACK)
            .setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30));

    List<CameraConfig> cameraConfigList = session.getSupportedCameraConfigs(cameraConfigFilter);

    // Get config with smallest image (1st criterion) and texture size (2nd criterion).
    CameraConfig minCameraConfig = null;
    for (CameraConfig cameraConfig : cameraConfigList) {
      Timber.d(
          "available camera config: ("
              + "CameraId: "
              + cameraConfig.getCameraId()
              + ", FacingDirection: "
              + cameraConfig.getFacingDirection()
              + ", FpsRange: "
              + cameraConfig.getFpsRange()
              + ", ImageSize: "
              + cameraConfig.getImageSize()
              + ", TextureSize: "
              + cameraConfig.getTextureSize()
              + ")");

      if (!is640x480(cameraConfig.getImageSize())) {
        continue;
      }

      if (minCameraConfig == null) {
        minCameraConfig = cameraConfig;
        continue;
      }

      if (isSmaller(cameraConfig.getTextureSize(), minCameraConfig.getTextureSize())) {
        minCameraConfig = cameraConfig;
      }
    }

    if (minCameraConfig == null) {
      Timber.w("No suitable camera config available.");
      throw new CameraNotAvailableException("No suitable camera config available.");
    }

    Timber.i(
        "selected camera config: ("
            + "CameraId: "
            + minCameraConfig.getCameraId()
            + ", FacingDirection: "
            + minCameraConfig.getFacingDirection()
            + ", FpsRange: "
            + minCameraConfig.getFpsRange()
            + ", ImageSize: "
            + minCameraConfig.getImageSize()
            + ", TextureSize: "
            + minCameraConfig.getTextureSize()
            + ")");

    session.setCameraConfig(minCameraConfig);
  }

  private void setConfig() {
    Config config =
        new Config(session)
            .setAugmentedFaceMode(Config.AugmentedFaceMode.DISABLED)
            .setCloudAnchorMode(Config.CloudAnchorMode.DISABLED)
            .setFocusMode(Config.FocusMode.AUTO)
            .setInstantPlacementMode(InstantPlacementMode.DISABLED)
            .setLightEstimationMode(LightEstimationMode.DISABLED)
            .setPlaneFindingMode(PlaneFindingMode.DISABLED)
            .setUpdateMode(Config.UpdateMode.BLOCKING);

    if (goalPlacementEnabled) {
      // Only used to measure the camera height above the floor.
      config.setPlaneFindingMode(PlaneFindingMode.HORIZONTAL);
    }

    session.configure(config);
  }

  public void pause() {
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  public void closeSession() {
    // It's advised to first pause the session in the main thread and then close it in a background
    // thread.
    // Reference: https://developers.google.com/ar/reference/java/com/google/ar/core/Session#close()
    if (session != null) {
      runOnMainThread(
          () -> {
            pause();
            HandlerThread handlerThread = new HandlerThread("ArCore Session Close Handler");
            handlerThread.start();
            Handler sessionCloseHandler = new Handler(handlerThread.getLooper());
            sessionCloseHandler.post(() -> session.close());
          });
    }
  }

  public void detachAnchors() {
    for (Anchor anchor : session.getAllAnchors()) {
      anchor.detach();
    }

    startAnchor = null;
    targetAnchor = null;
  }

  public void setArCoreListener(ArCoreListener arCoreListener) {
    this.arCoreListener = arCoreListener;
  }

  public void removeArCoreListener() {
    this.arCoreListener = null;
  }
}
