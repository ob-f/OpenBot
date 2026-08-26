package org.openbot.app.robot.pointGoalNavigation;

import android.content.Context;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Size;
import android.view.ViewGroup;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.Config.LightEstimationMode;
import com.google.ar.core.Config.PlaneFindingMode;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.SessionPausedException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import java.io.IOException;
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
  private Pose currentPose;
  private float gpuTextureAspectRatio = 16.0f / 9.0f;
  private ArCoreListener arCoreListener = null;
  private final float[] anchorMatrix = new float[16];
  private Anchor startAnchor, targetAnchor;
  private Handler handlerMain;
  private GLSurfaceView surfaceView;
  private boolean renderFrame = true;
  private Pose startPose = null, targetPose = null;

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
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);

    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
    lp.height = height;
    lp.width = (int) (lp.height * gpuTextureAspectRatio);
    runOnMainThread(() -> surfaceView.setLayoutParams(lp));
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
    return targetAnchor.getPose();
  }

  public void setTargetAnchor(Pose pose) {
    targetAnchor = session.createAnchor(pose);
  }

  public void setTargetAnchorAtCurrentPose() {
    targetAnchor = session.createAnchor(currentPose);
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
      if (targetAnchor != null) {
        targetPose = targetAnchor.getPose();
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
      if (targetAnchor != null && trackingState == TrackingState.TRACKING) {
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

        float translation[] = new float[3];
        float rotation[] = new float[4];
        targetAnchor.getPose().getTranslation(translation, 0);
        currentPose.getRotationQuaternion(rotation, 0);

        Pose rotatedPose = new Pose(translation, rotation);
        rotatedPose.toMatrix(anchorMatrix, 0);

        float scaleFactor = 1.0f;
        // Update and draw the model and its shadow.
        twoDRenderer.updateModelMatrix(anchorMatrix, scaleFactor);
        twoDRenderer.draw(viewmtx, projmtx);
      }
    }
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

  private void setSurfaceViewAspectRatio(float aspectRatio) {
    gpuTextureAspectRatio = aspectRatio;

    ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
    lp.width = (int) (lp.height * gpuTextureAspectRatio);
    runOnMainThread(() -> surfaceView.setLayoutParams(lp));
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
    setSurfaceViewAspectRatio(
        1.0f
            * minCameraConfig.getTextureSize().getWidth()
            / minCameraConfig.getTextureSize().getHeight());
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
