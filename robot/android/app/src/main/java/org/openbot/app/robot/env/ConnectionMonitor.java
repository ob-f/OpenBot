package org.openbot.app.robot.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openbot.app.robot.utils.ConnectionUtils;

public class ConnectionMonitor {

  private final String TAG = "ConnectionMonitor";
  private ILocalConnection connection;
  private static String myCurrentAddress = "";
  private WifiReceiver broadcastReceiver = new WifiReceiver();
  private IntentFilter intentFilter = new IntentFilter();

  {
    intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
  }

  public void init(Context context, ILocalConnection connection) {
    this.connection = connection;
    // broadcastReceiver.unregister(context);
    broadcastReceiver.register(context, intentFilter);
  }

  public void shutdown(Context context) {
    broadcastReceiver.unregister(context);
  }

  class WifiReceiver extends BroadcastReceiver {

    private boolean isRegistered = false;

    void register(Context context, IntentFilter intentFilter) {
      if (!isRegistered) {
        ContextCompat.registerReceiver(
            context, this, intentFilter, ContextCompat.RECEIVER_EXPORTED);
        isRegistered = true;
      }
    }

    void unregister(Context context) {
      if (isRegistered) {
        context.unregisterReceiver(this);
        isRegistered = false;
      }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

      String ipAddress = getIPAddress(true);
      Log.i(
          TAG,
          "WifiReceiver got network change event, myAddress: "
              + ipAddress
              + ", current address: "
              + myCurrentAddress);
      if (!myCurrentAddress.equals(ipAddress)) {
        myCurrentAddress = ipAddress;
        if (!"".equals(ipAddress) && connection.isConnected()) {
          BotToControllerEventBus.emitEvent(ConnectionUtils.createStatus("PORT", "1935"));
          BotToControllerEventBus.emitEvent(
              ConnectionUtils.createStatus("NEW_IP_ADDRESS", ipAddress));
        }
      }
    }
  }

  public String getIPAddress(boolean useIPv4) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        ArrayList<InetAddress> addrs = Collections.list(intf.getInetAddresses());
        for (InetAddress addr : addrs) {
          if (!addr.isLoopbackAddress()) {
            String sAddr = addr.getHostAddress();
            // boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
            boolean isIPv4 = sAddr.indexOf(':') < 0;

            if (useIPv4) {
              if (isIPv4) return sAddr;
            } else {
              if (!isIPv4) {
                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
              }
            }
          }
        }
      }
    } catch (Exception ignored) {
    } // for now eat exceptions
    return "";
  }
}
