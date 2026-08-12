package org.openbot.app.robot.env;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;
import org.openbot.app.robot.R;
import org.openbot.app.robot.customview.AutoFitSurfaceGlView;
import org.openbot.app.robot.customview.WebRTCSurfaceView;
import org.openbot.app.robot.utils.CameraUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import timber.log.Timber;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PhoneController {
  private static final String REQUEST_ROOM_ID = "request-roomId";
  private static PhoneController _phoneController;
  private ConnectionSelector connectionSelector;
  private IVideoServer videoServer;
  private View view = null;
  private WebSocket webSocket;
  // Web mode only: CONNECTED starts WebRTC for browser. Phone/Flutter/gamepad stay off.
  private boolean webVideoEnabled = false;

  public static PhoneController getInstance(Context context) {
    if (_phoneController == null) {
      synchronized (PhoneController.class) {
        if (_phoneController == null) _phoneController = new PhoneController();
        _phoneController.init(context);
      }
    }

    return _phoneController;
  }

  class DataReceived implements IDataReceived {
    @Override
    public void dataReceived(String commandStr) {
      ControllerToBotEventBus.emitEvent(commandStr);
    }
  }

  private void init(Context context) {
    ControllerConfig.getInstance().init(context);

    videoServer =
        "RTSP".equals(ControllerConfig.getInstance().getVideoServerType())
            ? new RtspServer()
            : new WebRtcServer();

    videoServer.init(context);
    videoServer.setCanStart(true);

    this.connectionSelector = ConnectionSelector.getInstance(context);
    connectionSelector.getConnection().setDataCallback(new DataReceived());

    android.util.Size resolution =
        CameraUtils.getClosestCameraResolution(context, new android.util.Size(640, 360));
    videoServer.setResolution(resolution.getWidth(), resolution.getHeight());

    handleBotEvents();
    createAndSetView(context);
    monitorConnection();
  }

  private void createAndSetView(Context context) {
    if (videoServer instanceof WebRtcServer) {
      view = new org.openbot.app.robot.customview.WebRTCSurfaceView(context);
    } else if (videoServer instanceof RtspServer) {
      view = new org.openbot.app.robot.customview.AutoFitSurfaceGlView(context);
    }
    if (view != null) {
      addVideoView(view, context);
    }
  }

  private void addVideoView(View videoView, Context context) {
    ViewGroup viewGroup = (ViewGroup) ((Activity) context).getWindow().getDecorView();

    ViewGroup.LayoutParams layoutParams =
        new ViewGroup.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    videoView.setLayoutParams(layoutParams);
    videoView.setId(R.id.video_window);
    videoView.setAlpha(0f);
    viewGroup.addView(videoView, 0); // send to back

    if (videoView instanceof WebRTCSurfaceView) {
      videoServer.setView((WebRTCSurfaceView) videoView);
    } else if (videoView instanceof AutoFitSurfaceGlView) {
      videoServer.setView((AutoFitSurfaceGlView) videoView);
    }
  }

  public void connect(Context context) {
    setWebVideoEnabled(false);
    ILocalConnection connection = connectionSelector.getConnection();

    if (!connection.isConnected()) {
      connection.init(context);
      connection.connect(context);
    } else {
      connection.start();
    }
  }

  public void connectWebServer() {
    setWebVideoEnabled(true);
    closeWebSocketQuietly();
    nodeServerConnect();
  }

  private void closeWebSocketQuietly() {
    if (webSocket != null) {
      webSocket.close(1000, "Web socket closed");
      webSocket = null;
    }
  }

  private void nodeServerConnect() {
    String serverUrl = ControllerConfig.getInstance().getWebSignalingServerUrl();
    Timber.d("Connecting to web signaling server: %s", serverUrl);

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(serverUrl).build();

    WebSocketListener webSocketListener = new WebSocketListener() {
      @Override
      public void onOpen(WebSocket webSocket, @NonNull okhttp3.Response response) {
        PhoneController.this.webSocket = webSocket;
        ControllerToBotEventBus.emitEvent("{command: \"CONNECTED\"}");
        sendRoomId(webSocket);
      }

      @Override
      public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        if (text.contains(REQUEST_ROOM_ID)) {
          sendRoomId(webSocket);
          return;
        }
        ControllerToBotEventBus.emitEvent(text);
      }

      @Override
      public void onFailure(@NonNull WebSocket webSocket, Throwable t, okhttp3.Response response) {
        Timber.e(t, "Web signaling WebSocket failed (check web_signaling_server URL and Wi-Fi)");
      }
    };

    webSocket = client.newWebSocket(request, webSocketListener);
  }

  // Same email on robot and browser = same room (must be signed in with Google).
  private void sendRoomId(WebSocket socket) {
    if (socket == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
      Timber.w("Cannot join web room: WebSocket or Firebase user is missing");
      return;
    }
    try {
      JSONObject room = new JSONObject();
      room.put("roomId", FirebaseAuth.getInstance().getCurrentUser().getEmail());
      room.put("clientType", "robot");
      socket.send(room.toString());
      Timber.d("Sent web roomId for signaling");
    } catch (JSONException e) {
      Timber.e(e, "Failed to send web roomId");
    }
  }

  public void disconnect() {
    setWebVideoEnabled(false);
    closeWebSocketQuietly();
    connectionSelector.getConnection().stop();
  }

  private void setWebVideoEnabled(boolean enabled) {
    webVideoEnabled = enabled;
    if (!enabled) {
      videoServer.setConnected(false);
    }
  }

  public void send(JSONObject info) {
    if (webSocket != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
      try {
        info.put("roomId", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        webSocket.send(info.toString());
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    if (connectionSelector.getConnection().isConnected()) {
      connectionSelector.getConnection().sendMessage(info.toString());
    }
  }

  public boolean isConnected() {
    return connectionSelector.getConnection().isConnected();
  }

  private void handleBotEvents() {
    BotToControllerEventBus.subscribe(
        this::send, error -> Timber.d("Error occurred in BotToControllerEventBus: %s", error));
  }

  // Nearby CONNECTED also fires here; only start camera when web mode set webVideoEnabled.
  private void monitorConnection() {
    ControllerToBotEventBus.subscribe(
        "PhoneController.monitor",
        event -> {
          String command = event.getString("command");
          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if ("CONNECTED".equals(command)) {
                      if (webVideoEnabled) {
                        videoServer.setConnected(true);
                      }
                    } else if ("DISCONNECTED".equals(command)) {
                      videoServer.setConnected(false);
                    }
                  });
        },
        error -> Log.d(null, "Error occurred in monitorConnection: " + error),
        event ->
            event.has("command")
                && ("CONNECTED".equals(event.getString("command"))
                    || "DISCONNECTED".equals(event.getString("command"))));
  }

  private PhoneController() {
    if (_phoneController != null) {
      throw new RuntimeException(
          "Use getInstance() method to get the single instance of this class.");
    }
  }
}
