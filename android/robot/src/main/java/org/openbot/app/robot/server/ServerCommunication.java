package org.openbot.app.robot.server;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Environment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openbot.app.robot.R;
import timber.log.Timber;

public class ServerCommunication {

  private final AsyncHttpClient client;
  private final Context context;
  private final NsdService nsdService;
  private final Map<String, NsdServiceInfo> servers = new HashMap<>();
  private final NsdManager.ResolveListener resolveListener =
      new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
          // Called when the resolve fails.  Use the error code to debug.
          Timber.e("Resolve failed %s", errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
          Timber.e("serviceInfo %s", serviceInfo.getServiceName());
          servers.put(serviceInfo.getServiceName(), serviceInfo);
          try {
            serverListener.onServerListChange(servers.keySet());
          } catch (Exception e) {
            Timber.w(e);
          }
        }
      };
  private final JsonHttpResponseHandler testResponseHandler =
      new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
          Timber.d("Server found: %s", response.toString() + headers + statusCode);
          Timber.d("" + headers);
          try {
            uploadAll();
          } catch (Exception e) {
            Timber.w(e);
          }
        }

        @Override
        public void onFailure(
            int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
          Timber.d("Server error: %s", throwable.toString());
        }
      };
  private final JsonHttpResponseHandler modelListHandler =
      new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
          File dir = context.getFilesDir();

          if (!dir.exists()) {
            if (!dir.mkdirs()) {
              Timber.i("Make dir failed");
            }
          }

          HashSet<String> valid = new HashSet<>();
          for (int i = 0; i < response.length(); i++) {
            try {
              String name = response.optJSONObject(i).getString("name");
              valid.add(name);
              long serverFileTime = response.optJSONObject(i).getLong("mtime") * 1000;
              File toFile = new File(dir + File.separator + name);
              if (toFile.exists()) {
                long localFileTime = toFile.lastModified();
                if (localFileTime >= serverFileTime) {
                  continue;
                }
                Timber.d("Update model: %s", name);
                Timber.d("File times: %d < %d", localFileTime, serverFileTime);
              } else {
                Timber.d("Download new model: %s", name);
              }

              client.get(
                  context,
                  serverUrl + "/models/" + name,
                  new FileAsyncHttpResponseHandler(toFile) {
                    @Override
                    public void onFailure(
                        int statusCode, Header[] headers, Throwable throwable, File file) {
                      Timber.e(throwable, "Download error: %s", name);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, File file) {
                      serverListener.onAddModel(name);
                      if (file.setLastModified(serverFileTime)) {
                        Timber.i("Successful download: %s", name);
                      } else {
                        Timber.e("Set file time error: %s", name);
                      }
                    }
                  });
            } catch (JSONException e) {
              Timber.e(e, "JSON error");
            }
          }

          // TODO: Fix the commented code.
          //  Currently all models that were not added by server (e.g. object detection) are
          //  removed. The file delete should probably be handled in onRemoveModel in
          //  ControlsFragment. MasterList/ModelManager needs to be updated and it needs be checked
          //  that only autopilot models that were added from the server are removed.

          //          String[] list = dir.list((dir1, name) -> name.endsWith(".tflite"));
          //          if (list != null) {
          //            for (String name : list) {
          //              if (!valid.contains(name)) {
          //                File file = new File(dir + File.separator + name);
          //                if (file.delete()) {
          //                  serverListener.onRemoveModel(name);
          //                  Timber.d("deleted: %s", name);
          //                } else {
          //                  Timber.e("delete error: %s", name);
          //                }
          //              }
          //            }
          //          }

        }
      };
  private final ServerListener serverListener;
  private Timer timer;

  private String serverUrl;

  public ServerCommunication(Context context, ServerListener serverListener) {
    this.client = new AsyncHttpClient();
    this.context = context;
    this.nsdService = new NsdService();
    this.serverListener = serverListener;
  }

  public void start() {
    Timber.d("service started");
    nsdService.start(context, resolveListener);
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            if (serverUrl != null) {
              Timber.d("Check for new models");
              client.get(context, serverUrl + "/models", modelListHandler);
            }
          }
        },
        0,
        10000);
  }

  public void connect(String server) {
    NsdServiceInfo serviceInfo = servers.get(server);
    if (serviceInfo == null) {
      Timber.e("Server not found: %s", server);
      return;
    }
    String ipAddress = serviceInfo.getHost().getHostAddress();
    serverUrl = "http://" + ipAddress + ":" + serviceInfo.getPort();
    Timber.d("Resolved address: %s", serverUrl);

    client.get(context, serverUrl + "/test", testResponseHandler);
    serverListener.onConnectionEstablished(ipAddress);
  }

  public void disconnect() {
    client.cancelRequests(context, true);
    serverUrl = null;
    serverListener.onConnectionEstablished(context.getString(R.string.ip_placeholder));
  }

  public void upload(File file) {
    if (serverUrl == null) {
      return;
    }
    if (serverUrl.isEmpty()) {
      return;
    }
    long size = file.length() / 1024 / 1024;
    Timber.d("Start upload %s (%d MB)", file.getName(), size);

    RequestParams params = new RequestParams();
    try {
      params.put("file", file);
    } catch (FileNotFoundException e) {
      Timber.e("File not found: %s", file.getAbsolutePath());
      return;
    }
    Timber.e("Url is" + serverUrl + "/upload" );
    Timber.e("paramerter is ->" + params );
    client.post(context, serverUrl + "/upload", params, new UploadResponseHandler(file));
  }

  public void uploadAll() {
    String logDir =
        Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + context.getString(R.string.app_name);
    File directory = new File(logDir);
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith(".zip")) {
          upload(file);
        }
      }
    }
  }

  public void stop() {
    client.cancelRequests(context, true);
    nsdService.stop();
    timer.cancel();
  }

  public Set<String> getServers() {
    return servers.keySet();
  }

  static class UploadResponseHandler extends JsonHttpResponseHandler {
    private final File file;

    public UploadResponseHandler(File file) {
      super();
      this.file = file;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
      if (file.delete()) {
        Timber.d("uploaded: %s", file.getName());
      } else {
        Timber.e("delete error: %s", file.getName());
      }
    }
  }
}

