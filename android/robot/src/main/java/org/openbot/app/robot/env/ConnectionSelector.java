package org.openbot.app.robot.env;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

public class ConnectionSelector {
  private static final String TAG = "ConnectionManager";
  private static ConnectionSelector _connectionSelector;
  private static Context _context;
  private ILocalConnection connection;

  private final ILocalConnection networkConnection = new NetworkServiceConnection();
  private final ILocalConnection nearbyConnection = new NearbyConnection();

  private ConnectionSelector() {
    if (_connectionSelector != null) {
      throw new RuntimeException(
          "Use getInstance() method to get the single instance of this class.");
    }
  }

  public static ConnectionSelector getInstance(Context context) {
    _context = context;
    if (_connectionSelector == null) {

      synchronized (ConnectionSelector.class) {
        if (_connectionSelector == null) _connectionSelector = new ConnectionSelector();
      }
    }

    return _connectionSelector;
  }

  ILocalConnection getConnection() {
    if (connection != null) {
      return connection;
    }

    if (isConnectedViaWifi() || isWifiApEnabled()) {
      connection = networkConnection;
    } else {
      connection = nearbyConnection;
    }

    return connection;
  }

  private boolean isConnectedViaWifi() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    return mWifi.isConnected();
  }

  private boolean isWifiApEnabled() {
    WifiManager wifiManager =
            (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
    try {
      final Method method =
              wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
      Boolean result = (Boolean) method.invoke(wifiManager);
      return Boolean.TRUE.equals(result);
    } catch (final Throwable ignored) {}

    return false;
  }
}
