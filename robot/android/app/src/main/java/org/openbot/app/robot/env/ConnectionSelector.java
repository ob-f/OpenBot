package org.openbot.app.robot.env;

import android.content.Context;

/**
 * Provides the connection used to talk to the smartphone controller app.
 *
 * <p>The controller is discovered over the local network (NSD/mDNS), so both phones must be on the
 * same Wi-Fi network, or one phone must be connected to the other's hotspot.
 */
public class ConnectionSelector {
  private static ConnectionSelector _connectionSelector;

  private final ILocalConnection networkConnection = new NetworkServiceConnection();

  private ConnectionSelector() {
    if (_connectionSelector != null) {
      throw new RuntimeException(
          "Use getInstance() method to get the single instance of this class.");
    }
  }

  public static ConnectionSelector getInstance(Context context) {
    if (_connectionSelector == null) {
      synchronized (ConnectionSelector.class) {
        if (_connectionSelector == null) _connectionSelector = new ConnectionSelector();
      }
    }

    return _connectionSelector;
  }

  ILocalConnection getConnection() {
    return networkConnection;
  }
}
