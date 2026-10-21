package org.openbot.app.robot.env;

import android.content.Context;
import android.view.SurfaceView;
import android.view.TextureView;
import org.webrtc.SurfaceViewRenderer;

public interface IVideoServer {
  void setResolution(int w, int h);

  void setConnected(boolean connected);

  void init(Context context);

  boolean isRunning();

  void startClient();

  void sendServerUrl();

  void sendVideoStoppedStatus();

  void setView(SurfaceView view);

  void setView(TextureView view);

  void setView(SurfaceViewRenderer view);

  void setView(com.pedro.rtplibrary.view.OpenGlView view);

  void setCanStart(boolean canStart);
}
