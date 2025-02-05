package org.openbot.utils;

import android.util.Pair;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionUtils {

  public static JSONObject createStatus(String name, Boolean value) {
    return createStatus(name, value ? "true" : "false");
  }

  public static JSONObject createStatus(String name, String value) {
    try {
      return new JSONObject().put("status", new JSONObject().put(name, value));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return new JSONObject();
  }


  public static JSONObject createStatus(String name, JSONObject value) {
    try {
      return new JSONObject().put("status", new JSONObject().put(name, value.toString()));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return new JSONObject();
  }

  public static JSONObject createFragment(String value){
    try {
      return new JSONObject().put("FRAGMENT_TYPE", value);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return new JSONObject();
  }

  public static JSONObject getStatus(
      boolean loggingEnabled,
      boolean noiseEnabled,
      boolean networkEnabled,
      String driveMode,
      int indicator) {
    JSONObject status = new JSONObject();
    try {
      JSONObject statusValue = new JSONObject();

      statusValue.put("LOGS", loggingEnabled);
      statusValue.put("NOISE", noiseEnabled);
      statusValue.put("NETWORK", networkEnabled);
      statusValue.put("DRIVE_MODE", driveMode);

      // Possibly can only send the value of the indicator here, but this makes it clearer.
      // Also, the controller need not have to know implementation details.
      statusValue.put("INDICATOR_LEFT", indicator == -1);
      statusValue.put("INDICATOR_RIGHT", indicator == 1);
      statusValue.put("INDICATOR_STOP", indicator == 0);

      status.put("status", statusValue);

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return status;
  }

  public static JSONObject createStatusBulk(List<Pair<String, String>> nameValues) {
    JSONObject status = new JSONObject();
    try {
      JSONObject statusValue = new JSONObject();

      for (Pair nameValue : nameValues) {
        statusValue.put(String.valueOf(nameValue.first), nameValue.second);
      }

      status.put("status", statusValue);

    } catch (JSONException e) {
      e.printStackTrace();
    }
    return status;
  }

  public static String getIPAddress(boolean useIPv4) {
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
