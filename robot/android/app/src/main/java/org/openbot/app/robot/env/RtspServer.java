package org.openbot.app.robot.env;

import static org.openbot.app.robot.utils.ConnectionUtils.getIPAddress;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.ToneGenerator;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import com.pedro.rtspserver.RtspServerCamera1;
import java.util.concurrent.TimeUnit;
import org.openbot.app.robot.customview.AutoFitSurfaceView;
import org.openbot.app.robot.customview.AutoFitTextureView;
import org.openbot.app.robot.utils.AndGate;
import org.openbot.app.robot.utils.ConnectionUtils;
import org.openbot.app.robot.utils.DelayedRunner;
import org.webrtc.SurfaceViewRenderer;
import timber.log.Timber;

public class RtspServer
    implements ConnectCheckerRtsp,
        SurfaceHolder.Callback,
        TextureView.SurfaceTextureListener,
        IVideoServer {
  private final String TAG = "RtspServerPedroOpenGL";
  private RtspServerCamera1 rtspServerCamera1;
  private View view;
  private AndGate andGate;
  private Context context;
  private Size resolution = new Size(640, 360);
  private final int PORT = 1935;

  public RtspServer() {}

  // IVideoServer Interface
  @Override
  public void init(Context context) {
    this.context = context;

    /*
    AndGate will run 'startServer()' if all its input conditions are met.
    This is useful if we do not know the order of the updates to the conditions.
    */
    AndGate.Action startAction = () -> startServer();
    AndGate.Action stopAction = () -> stopServer();
    andGate = new AndGate(startAction, stopAction);
    andGate.addCondition("connected");
    andGate.addCondition("surfaceCreated");
    andGate.addCondition("view set");
    andGate.addCondition("camera permission");
    andGate.addCondition("resolution set");
    andGate.addCondition("can start");

    int camera = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
    andGate.set("camera permission", camera == PackageManager.PERMISSION_GRANTED);
  }

  @Override
  public boolean isRunning() {
    return rtspServerCamera1 != null && rtspServerCamera1.isStreaming();
  }

  @Override
  public void startClient() {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_PROTOCOL", "RTSP"));
    sendServerUrl();
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_COMMAND", "START"));

    rtspServerCamera1.enableAudio();
  }

  @Override
  public void sendServerUrl() {
    BotToControllerEventBus.emitEvent(
        ConnectionUtils.createStatus(
            "VIDEO_SERVER_URL", "rtsp://" + getIPAddress(true) + ":" + PORT
            // "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"
            ));
  }

  @Override
  public void sendVideoStoppedStatus() {
    BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("VIDEO_COMMAND", "STOP"));
  }

  @Override
  public void setView(SurfaceView view) {
    this.view = view;
    ((AutoFitSurfaceView) this.view).getHolder().addCallback(this);
    andGate.set("view set", true);
  }

  @Override
  public void setView(TextureView view) {
    this.view = view;
    ((AutoFitTextureView) this.view).setSurfaceTextureListener(this);
    andGate.set("view set", true);
  }

  @Override
  public void setView(SurfaceViewRenderer view) {
    this.view = view;
    andGate.set("view set", true);
  }

  @Override
  public void setView(OpenGlView view) {
    this.view = view;
    ((OpenGlView) this.view).getHolder().addCallback(this);
    andGate.set("view set", true);
  }

  @Override
  public void setCanStart(boolean canStart) {
    andGate.set("can start", canStart);
  }

  @Override
  public void setConnected(boolean connected) {
    int camera = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
    andGate.set("camera permission", camera == PackageManager.PERMISSION_GRANTED);

    andGate.set("connected", connected);
  }
  // end Interface

  // Local methods
  private void stopServer() {
    try {
      if (rtspServerCamera1 != null) {
        if (rtspServerCamera1.isRecording()) {
          rtspServerCamera1.stopRecord();
        }

        if (rtspServerCamera1.isStreaming()) {
          rtspServerCamera1.stopStream();
        }

        rtspServerCamera1.stopPreview();
        rtspServerCamera1 = null;
      }
    } catch (Exception e) {
      Log.d(TAG, "Got error stopping server: " + e);
    }
  }

  private void startServer() {
    startServer(resolution, PORT);
  }

  private void startServer(Size resolution, int port) {
    if (rtspServerCamera1 == null) {
      Timber.d("Resolution %dx%d", resolution.getWidth(), resolution.getHeight());

      String viewType = this.view.getClass().getName();

      if (viewType.contains("AutoFitTextureView")) {
        rtspServerCamera1 = new RtspServerCamera1((AutoFitTextureView) view, this, port);
      }
      if (viewType.contains("AutoFitSurfaceView")) {
        rtspServerCamera1 = new RtspServerCamera1((SurfaceView) view, this, port);
      }
      if (viewType.contains("AutoFitSurfaceGlView")) {
        rtspServerCamera1 = new RtspServerCamera1((OpenGlView) view, this, port);
      }
    }

    if (!rtspServerCamera1.isStreaming()) {
      if (rtspServerCamera1.prepareAudio(64 * 1024, 32000, false, true, true)
          && rtspServerCamera1.prepareVideo(
              resolution.getWidth(), resolution.getHeight(), 20, 1200 * 1024, 2, 0)) {

        rtspServerCamera1.startStream("");

        // Delay starting the client for a second to make sure the server is started.
        Runnable action = () -> startClient();
        new DelayedRunner().runAfter(action, 1000L, TimeUnit.MILLISECONDS);
      }
    }
  }

  @Override
  public void setResolution(int w, int h) {
    resolution = new Size(w, h);
    andGate.set("resolution set", true);
  }

  // ConnectCheckerRtsp callbacks
  @Override
  public void onConnectionSuccessRtsp() {
    Log.i(TAG, "onConnectionSuccessRtsp");
  }

  @Override
  public void onConnectionFailedRtsp(final String reason) {
    rtspServerCamera1.stopStream();
    beep();
  }

  @Override
  public void onNewBitrateRtsp(long bitrate) {
    Log.i(TAG, "Bitrate set to " + bitrate);
  }

  @Override
  public void onDisconnectRtsp() {
    Log.i(TAG, "onDisconnectRtsp");
  }

  @Override
  public void onAuthErrorRtsp() {
    beep();
  }

  @Override
  public void onAuthSuccessRtsp() {}

  // SurfaceHolder.Callback callbacks
  @Override
  public void surfaceCreated(@NonNull SurfaceHolder holder) {
    Log.d(TAG, "Surface created...");
    andGate.set("surfaceCreated", true);
  }

  @Override
  public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    if (rtspServerCamera1 != null) {
      rtspServerCamera1.startPreview();
    }
  }

  @Override
  public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    andGate.set("surfaceCreated", false);
    sendVideoStoppedStatus();
    andGate.set("surfaceCreated", false);
  }

  // SurfaceTextureListener callbacks
  @Override
  public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
    andGate.set("surfaceCreated", true);
  }

  @Override
  public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

  @Override
  public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
    sendVideoStoppedStatus();
    andGate.set("surfaceCreated", false);
    return false;
  }

  @Override
  public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
    Log.i(TAG, "onSurfaceTextureUpdated called");
  }

  // Utils
  private void beep() {
    final ToneGenerator tg = new ToneGenerator(6, 100);
    tg.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE);
  }
}
