package org.openbot.app.robot.env;

import android.content.Context;

public interface ILocalConnection {

  void init(Context context);

  void setDataCallback(IDataReceived dataCallback);

  void connect(Context context);

  void disconnect(Context context);

  boolean isConnected();

  void sendMessage(String message);

  void stop();

  void start();

  boolean isVideoCapable();
}
