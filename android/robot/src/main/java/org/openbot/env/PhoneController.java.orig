package org.openbot.env;

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
import org.openbot.R;
import org.openbot.customview.AutoFitSurfaceGlView;
import org.openbot.customview.WebRTCSurfaceView;
import org.openbot.utils.CameraUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import timber.log.Timber;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PhoneController {
  private static final String TAG = "PhoneController";
  private static PhoneController _phoneController;
  private ConnectionSelector connectionSelector;
  private IVideoServer videoServer;
  private View view = null;
  private WebSocket webSocket;

  public static PhoneController getInstance(Context context) {
    if (_phoneController == null) { // Check for the first time

      synchronized (PhoneController.class) { // Check for the second time.
        // if there is no instance available... create new one
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
      view = new org.openbot.customview.WebRTCSurfaceView(context);
    } else if (videoServer instanceof RtspServer) {
      view = new org.openbot.customview.AutoFitSurfaceGlView(context);
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
    ILocalConnection connection = connectionSelector.getConnection();

    if (!connection.isConnected()) {
      connection.init(context);
      connection.connect(context);
    } else {
      connection.start();
    }
  }

  public void connectWebServer(){
    nodeServerConnect();
  }

  private void nodeServerConnect() {
//     String serverUrl = "ws://verdant-imported-peanut.glitch.me";
           String serverUrl = "ws://192.168.1.6:8080";

    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(serverUrl).build();

    WebSocketListener webSocketListener = new WebSocketListener() {
      @Override
      public void onOpen(WebSocket webSocket, @NonNull okhttp3.Response response) {
        ControllerToBotEventBus.emitEvent("{command: \"CONNECTED\"}");
      }

      @Override
      public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        // Called when text message is received from the server
        ControllerToBotEventBus.emitEvent(text);
      }

      @Override
      public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        // Called when binary message is received from the server
      }

      @Override
      public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        // Called when the connection is closing
      }

      @Override
      public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        // Called when the connection is closed
      }

      @Override
      public void onFailure(@NonNull WebSocket webSocket, Throwable t, okhttp3.Response response) {
        // Called when an error occurs
      }
    };

    webSocket = client.newWebSocket(request, webSocketListener);
  }

  public void disconnect() {
    connectionSelector.getConnection().stop();
  }

  public void send(JSONObject info) {
    if (webSocket != null && FirebaseAuth.getInstance().getCurrentUser() != null)
      try {
        info.put("roomId", FirebaseAuth.getInstance().getCurrentUser().getEmail()); // Add the roomId to the JSON object
        String messageString = info.toString();
        webSocket.send(messageString);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }

     if (connectionSelector.getConnection().isConnected())
       connectionSelector.getConnection().sendMessage(info.toString());
  }

  public boolean isConnected() {
    return connectionSelector.getConnection().isConnected();
  }

  private void handleBotEvents() {
    BotToControllerEventBus.subscribe(
        this::send, error -> Timber.d("Error occurred in BotToControllerEventBus: %s", error));
  }

  private void monitorConnection() {
    ControllerToBotEventBus.subscribe(
        this.getClass().getSimpleName(),
        event -> {
          switch (event.getString("command")) {
            case "CONNECTED":
              new Handler(Looper.getMainLooper()).post(() -> videoServer.setConnected(true));
//              videoServer.setConnected(true);
              break;

            case "DISCONNECTED":
              new Handler(Looper.getMainLooper()).post(() -> videoServer.setConnected(false));
//              videoServer.setConnected(false);
              break;
          }
        },
        error -> {
          Log.d(null, "Error occurred in monitorConnection: " + error);
        },
        event ->
            event.has("command")
                && ("CONNECTED".equals(event.getString("command"))
                    || "DISCONNECTED".equals(event.getString("command"))) // filter everything else
        );
  }

  private PhoneController() {
    if (_phoneController != null) {
      throw new RuntimeException(
          "Use getInstance() method to get the single instance of this class.");
    }
  }
}
