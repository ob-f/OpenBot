package org.openbot.vehicle;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.ficat.easyble.scan.BleScanCallback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openbot.main.ScanDeviceAdapter;
import org.openbot.utils.Constants;

public class BluetoothManager {
  public enum ConnectionState {
    DISCONNECTED,
    CONNECTING,
    ENABLING_NOTIFY,
    SERIAL_READY,
    RETRYING,
    RESCANNING,
    DISCONNECTING,
    FAILED
  }

  public interface ConnectionListener {
    void onBleSerialReady();

    void onBleDisconnected();

    void onBleCriticalWriteFailure();
  }

  private static final String SERVICE_UUID = "61653dc3-4021-4d1e-ba83-8b4eec61d613";
  private static final String RX_UUID = "06386c14-86ea-4d71-811c-48f97c58f8c9";
  private static final String TX_UUID = "9bf1103b-834c-47cf-b149-c9e4bcf778a7";
  private static final String CONTROL_LOG_TAG = "CartControl";
  private static final long SERIAL_SETUP_TIMEOUT_MS = 3000L;
  private static final long RETRY_DELAY_MS = 750L;
  private static final long DISCONNECT_FALLBACK_MS = 1500L;
  private static final int MAX_AUTO_RETRIES = 1;
  private BleManager manager;
  private CharacteristicInfo notifyCharacteristic;
  private CharacteristicInfo writeCharacteristic;
  private ServiceInfo writeServiceInfo;
  private ServiceInfo notifyServiceInfo;
  public List<BleDevice> deviceList = new ArrayList<>();
  public List<String> notifySuccessUuids = new ArrayList<>();
  public BleDevice bleDevice;
  private Context context;
  public ScanDeviceAdapter adapter;
  private int indexValue;
  public String readValue;
  private final SerialLineAccumulator serialLineAccumulator = new SerialLineAccumulator();
  private final LocalBroadcastManager localBroadcastManager;
  private final ConnectionListener connectionListener;
  private final BleSerialWriteQueue writeQueue;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private long motionGeneration;
  private long connectionGeneration;
  private int autoRetryCount;
  private ConnectionState connectionState = ConnectionState.DISCONNECTED;
  private String activeAddress;
  private String lastConnectionError = "";
  private BleDevice retryDevice;
  private int retryIndex;
  private boolean retryAfterDisconnect;
  private BleDevice recoveryScanMatch;
  private volatile boolean controlDiagnosticsEnabled;
  private boolean notifyEnabled;
  UUID[] uuidArray = new UUID[] {UUID.fromString(SERVICE_UUID)};

  public BluetoothManager(Context context, ConnectionListener connectionListener) {
    this.context = context;
    this.connectionListener = connectionListener;
    writeQueue =
        new BleSerialWriteQueue(
            this::writeGatt,
            payload -> {
              Logger.e("critical BLE write failed twice: " + payload.trim());
              if (this.connectionListener != null) {
                this.connectionListener.onBleCriticalWriteFailure();
              }
              handleCriticalWriteFailure(payload);
            },
            this::logQueueEvent);
    initBleManager();
    localBroadcastManager = LocalBroadcastManager.getInstance(this.context);
  }

  public void initBleManager() {
    // check if this android device supports ble
    if (!BleManager.supportBle(this.context)) {
      return;
    }

    BleManager.ScanOptions scanOptions =
        BleManager.ScanOptions.newInstance()
            .scanPeriod(4000)
            .scanDeviceName(null)
            .scanServiceUuids(uuidArray);

    BleManager.ConnectOptions connectOptions =
        BleManager.ConnectOptions.newInstance().connectTimeout(12000);

    manager =
        BleManager.getInstance()
            .setScanOptions(scanOptions)
            .setConnectionOptions(connectOptions)
            .setLog(true, "Bluetooth_Connection")
            .init(this.context);
  }

  public synchronized void startScan() {
    if (connectionState == ConnectionState.CONNECTING
        || connectionState == ConnectionState.ENABLING_NOTIFY
        || connectionState == ConnectionState.RETRYING
        || connectionState == ConnectionState.RESCANNING
        || connectionState == ConnectionState.DISCONNECTING) return;
    final long scanGeneration = connectionGeneration;
    manager.startScan(
        new BleScanCallback() {
          @Override
          public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
            if (scanGeneration != connectionGeneration) return;
            for (BleDevice d : deviceList) {
              if (device.address.equals(d.address)) {
                return;
              }
            }
            deviceList.add(device);
            notifyAdapter();
          }

          @Override
          public void onStart(boolean startScanSuccess, String info) {
            if (scanGeneration != connectionGeneration) return;
            if (bleDevice != null && bleDevice.connecting) {
            } else {
              deviceList.clear();
            }
            if (isBleConnected() && !deviceList.contains(bleDevice)) {
              deviceList.add(bleDevice);
            }
          }

          @Override
          public void onFinish() {
            notifyAdapter();
          }
        });
  }

  public synchronized void stopScan() {
    // UI clicks/lifecycle may stop a list scan, but must not truncate automatic recovery.
    if (connectionState != ConnectionState.RESCANNING) manager.stopScan();
  }

  public synchronized void toggleConnection(int position, BleDevice device) {
    if (device == null) return;
    if (connectionState == ConnectionState.CONNECTING
        || connectionState == ConnectionState.ENABLING_NOTIFY
        || connectionState == ConnectionState.RETRYING
        || connectionState == ConnectionState.RESCANNING
        || connectionState == ConnectionState.DISCONNECTING) return;

    if (isBleConnected() && device.address.equals(activeAddress)) {
      beginUserDisconnect();
      return;
    }
    if (isBleConnected()) {
      Toast.makeText(context, "请先断开当前 BLE 设备", Toast.LENGTH_SHORT).show();
      return;
    }
    beginConnection(position, device, 0);
  }

  private synchronized void beginConnection(int position, BleDevice device, int retryCount) {
    cancelConnectionTimers();
    clearTransportState();
    if (connectionListener != null) connectionListener.onBleDisconnected();
    indexValue = safeDeviceIndex(position, device);
    bleDevice = device;
    activeAddress = device.address;
    autoRetryCount = retryCount;
    retryAfterDisconnect = false;
    retryDevice = null;
    connectionGeneration++;
    long generation = connectionGeneration;
    manager.stopScan();
    recoveryScanMatch = null;
    connectionState = retryCount > 0 ? ConnectionState.RETRYING : ConnectionState.CONNECTING;
    lastConnectionError = "";
    device.connecting = true;
    device.connected = false;
    replaceDeviceAtIndex(indexValue, device);
    logConnection("connect_start", generation, device, "retry=" + retryCount);
    manager.connect(device, createConnectCallback(generation, device.address));
  }

  private BleConnectCallback createConnectCallback(long generation, String address) {
    return new BleConnectCallback() {
      @Override
      public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        if (!startConnectSuccess) {
          handleConnectionFailure(generation, device, "connect_start_failed: " + info);
        }
      }

      @Override
      public void onConnected(BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        synchronized (BluetoothManager.this) {
          bleDevice = device;
          device.connecting = false;
          device.connected = true;
          connectionState = ConnectionState.ENABLING_NOTIFY;
          replaceDeviceAtIndex(indexValue, device);
          logConnection("gatt_connected", generation, device, "services_ready");
          configureSerialCharacteristics(generation, device);
        }
      }

      @Override
      public void onDisconnected(String info, int status, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        handleDisconnected(generation, device, info + ",status=" + status);
      }

      @Override
      public void onFailure(int failCode, String info, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        handleConnectionFailure(
            generation, device, "connect_failed code=" + failCode + ": " + info);
      }
    };
  }

  private void configureSerialCharacteristics(long generation, BleDevice device) {
    if (!isActiveAttempt(generation, device.address)) return;
    Map<ServiceInfo, List<CharacteristicInfo>> deviceInfo =
        manager.getDeviceServices(device.address);
    if (deviceInfo == null) {
      handleConnectionFailure(generation, device, "service_list_missing");
      return;
    }
    for (Map.Entry<ServiceInfo, List<CharacteristicInfo>> e : deviceInfo.entrySet()) {
      if (!SERVICE_UUID.equalsIgnoreCase(e.getKey().uuid)) continue;
      for (CharacteristicInfo characteristicInfo : e.getValue()) {
        if (TX_UUID.equalsIgnoreCase(characteristicInfo.uuid) && characteristicInfo.notify) {
          notifyCharacteristic = characteristicInfo;
          notifyServiceInfo = e.getKey();
        }
        if (RX_UUID.equalsIgnoreCase(characteristicInfo.uuid) && characteristicInfo.writable) {
          writeServiceInfo = e.getKey();
          writeCharacteristic = characteristicInfo;
        }
      }
    }
    if (writeServiceInfo == null
        || writeCharacteristic == null
        || notifyServiceInfo == null
        || notifyCharacteristic == null) {
      handleConnectionFailure(generation, device, "required_characteristic_missing");
      return;
    }

    // All control and handshake messages fit the default MTU. Enabling notifications directly
    // avoids EasyBLE 2.0.2 getting stuck when an MTU request fails without a failure callback.
    manager.notify(
        device,
        notifyServiceInfo.uuid,
        notifyCharacteristic.uuid,
        createNotifyCallback(generation, device.address));
    mainHandler.postDelayed(
        () -> handleSerialSetupTimeout(generation, device), SERIAL_SETUP_TIMEOUT_MS);
  }

  public synchronized void write(String msg) {
    if (!isSerialReady() || msg == null) return;
    BleSerialWriteQueue.Type type = classifyWrite(msg);
    long generation = type == BleSerialWriteQueue.Type.MOTION ? ++motionGeneration : 0L;
    writeQueue.enqueue(type, msg, generation);
  }

  public synchronized void writeControlTransition(String stop, String motion, long generation) {
    if (!isSerialReady()) return;
    motionGeneration = Math.max(motionGeneration, generation);
    writeQueue.enqueueTransition(stop, motion, generation);
  }

  public String getWriteStatus() {
    return writeQueue.getStatus();
  }

  public void setControlDiagnosticsEnabled(boolean enabled) {
    controlDiagnosticsEnabled = enabled;
    logControl("diagnostics", "enabled=" + (enabled ? 1 : 0));
  }

  private void writeGatt(String msg) {
    if (!isSerialReady()) {
      writeQueue.onWriteComplete(false);
      return;
    }
    logControl("gatt_write", "payload=" + sanitize(msg));
    long generation = connectionGeneration;
    String address = activeAddress;
    manager.write(
        bleDevice,
        writeServiceInfo.uuid,
        writeCharacteristic.uuid,
        msg.getBytes(UTF_8),
        createWriteCallback(generation, address));
  }

  private void logQueueEvent(
      String event,
      BleSerialWriteQueue.Type type,
      String payload,
      long generation,
      int pendingCount) {
    logControl(
        "queue_" + event,
        "type="
            + (type == null ? "NONE" : type.name())
            + ",generation="
            + generation
            + ",pending="
            + pendingCount
            + ",payload="
            + sanitize(payload));
  }

  private volatile ControlDiagnosticObserver diagnosticObserver;

  public void setDiagnosticObserver(ControlDiagnosticObserver observer) {
    diagnosticObserver = observer;
  }

  private void observe(String event, String details) {
    ControlDiagnosticObserver observer = diagnosticObserver;
    if (observer != null)
      try {
        observer.onEvent(event, details);
      } catch (RuntimeException ignored) {
        /* Observation must not affect transport. */
      }
  }

  private void logControl(String event, String details) {
    observe(event, details);
    if (!controlDiagnosticsEnabled) return;
    Log.i(
        CONTROL_LOG_TAG, "ms=" + SystemClock.elapsedRealtime() + ",event=" + event + "," + details);
  }

  private static String sanitize(String payload) {
    return payload == null ? "" : payload.trim().replace(',', ';');
  }

  private static BleSerialWriteQueue.Type classifyWrite(String msg) {
    String line = msg.trim();
    if (line.startsWith("!S,")) return BleSerialWriteQueue.Type.EMERGENCY;
    if (line.equals("c0,0")) return BleSerialWriteQueue.Type.STOP;
    if (line.startsWith("c")) return BleSerialWriteQueue.Type.MOTION;
    if (line.startsWith("h")) return BleSerialWriteQueue.Type.HEARTBEAT;
    return BleSerialWriteQueue.Type.QUERY;
  }

  private BleWriteCallback createWriteCallback(long generation, String address) {
    return new BleWriteCallback() {
      @Override
      public void onWriteSuccess(byte[] data, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        String value = new String(data, StandardCharsets.UTF_8);
        Logger.i("write success:" + value);
        logControl("gatt_success", "payload=" + sanitize(value));
        writeQueue.onWriteComplete(true);
      }

      @Override
      public void onFailure(int failCode, String info, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        Logger.e("write fail:" + info + " " + failCode);
        logControl("gatt_failure", "code=" + failCode + ",info=" + info);
        writeQueue.onWriteComplete(false);
      }
    };
  }

  private BleNotifyCallback createNotifyCallback(long generation, String address) {
    return new BleNotifyCallback() {
      @Override
      public void onCharacteristicChanged(byte[] data, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        readValue = new String(data);
        onSerialDataReceived(readValue);
      }

      @Override
      public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        if (!notifySuccessUuids.contains(notifySuccessUuid)) {
          notifySuccessUuids.add(notifySuccessUuid);
        }
        if (TX_UUID.equalsIgnoreCase(notifySuccessUuid)) {
          notifyEnabled = true;
          connectionState = ConnectionState.SERIAL_READY;
          lastConnectionError = "";
          cancelConnectionTimers();
          logConnection("serial_ready", generation, device, "notify_enabled");
          notifyAdapter();
          if (connectionListener != null) connectionListener.onBleSerialReady();
        }
      }

      @Override
      public void onFailure(int failCode, String info, BleDevice device) {
        if (!isActiveAttempt(generation, address)) return;
        handleConnectionFailure(generation, device, "notify_failed code=" + failCode + ": " + info);
      }
    };
  }

  private synchronized void handleSerialSetupTimeout(long generation, BleDevice device) {
    if (!isActiveAttempt(generation, device.address)
        || connectionState != ConnectionState.ENABLING_NOTIFY) return;
    handleConnectionFailure(generation, device, "notify_setup_timeout");
  }

  private synchronized void handleCriticalWriteFailure(String payload) {
    if (bleDevice == null || activeAddress == null) return;
    handleConnectionFailure(
        connectionGeneration, bleDevice, "critical_write_failure: " + sanitize(payload));
  }

  private synchronized void handleConnectionFailure(
      long generation, BleDevice device, String reason) {
    if (device == null || !isActiveAttempt(generation, device.address)) return;
    cancelConnectionTimers();
    lastConnectionError = reason;
    clearTransportState();
    if (connectionListener != null) connectionListener.onBleDisconnected();
    logConnection("connection_failure", generation, device, reason);

    if (shouldAutoRetry(autoRetryCount)) {
      autoRetryCount++;
      retryDevice = device;
      retryIndex = safeDeviceIndex(indexValue, device);
      connectionState = ConnectionState.RETRYING;
      retryAfterDisconnect = device.connected;
      device.connecting = false;
      replaceDeviceAtIndex(retryIndex, device);
      notifyAdapter();
      if (device.connected) {
        manager.disconnect(device.address);
        mainHandler.postDelayed(
            () -> forceRetryAfterDisconnectTimeout(generation, device), DISCONNECT_FALLBACK_MS);
      } else {
        scheduleRetry(generation);
      }
      return;
    }

    failAfterRetries(device, reason);
  }

  private synchronized void handleDisconnected(long generation, BleDevice device, String details) {
    if (device == null || !isActiveAttempt(generation, device.address)) return;
    cancelConnectionTimers();
    device.connecting = false;
    device.connected = false;
    clearTransportState();
    if (connectionListener != null) connectionListener.onBleDisconnected();
    logConnection("disconnected", generation, device, details);

    if (connectionState == ConnectionState.RETRYING && retryAfterDisconnect) {
      retryAfterDisconnect = false;
      replaceDeviceAtIndex(retryIndex, device);
      notifyAdapter();
      scheduleRetry(generation);
      return;
    }

    connectionState = ConnectionState.DISCONNECTED;
    bleDevice = null;
    activeAddress = null;
    autoRetryCount = 0;
    replaceDeviceAtIndex(safeDeviceIndex(indexValue, device), device);
    notifyAdapter();
  }

  private synchronized void beginUserDisconnect() {
    if (bleDevice == null || activeAddress == null) return;
    cancelConnectionTimers();
    connectionState = ConnectionState.DISCONNECTING;
    retryAfterDisconnect = false;
    long generation = connectionGeneration;
    BleDevice device = bleDevice;
    logConnection("user_disconnect", generation, device, "requested");
    notifyAdapter();
    manager.disconnect(activeAddress);
    mainHandler.postDelayed(
        () -> forceUserDisconnectTimeout(generation, device), DISCONNECT_FALLBACK_MS);
  }

  private synchronized void forceRetryAfterDisconnectTimeout(long generation, BleDevice device) {
    if (!isActiveAttempt(generation, device.address) || connectionState != ConnectionState.RETRYING)
      return;
    logConnection("disconnect_timeout", generation, device, "hard_reset_before_retry");
    scheduleRetry(generation);
  }

  private synchronized void forceUserDisconnectTimeout(long generation, BleDevice device) {
    if (!isActiveAttempt(generation, device.address)
        || connectionState != ConnectionState.DISCONNECTING) return;
    logConnection("disconnect_timeout", generation, device, "hard_reset_after_user_disconnect");
    hardResetBleStack();
    device.connecting = false;
    device.connected = false;
    connectionState = ConnectionState.DISCONNECTED;
    bleDevice = null;
    activeAddress = null;
    replaceDeviceAtIndex(safeDeviceIndex(indexValue, device), device);
    if (connectionListener != null) connectionListener.onBleDisconnected();
    notifyAdapter();
  }

  private synchronized void scheduleRetry(long generation) {
    if (connectionGeneration != generation || retryDevice == null) return;
    // Invalidate callbacks and dispose the failed GATT before obtaining fresh scan metadata.
    hardResetBleStack();
    connectionState = ConnectionState.RESCANNING;
    recoveryScanMatch = null;
    final long scanGeneration = connectionGeneration;
    notifyAdapter();
    mainHandler.postDelayed(() -> startRecoveryScan(scanGeneration), RETRY_DELAY_MS);
  }

  private synchronized boolean isRecoveryScan(long generation) {
    return connectionGeneration == generation && connectionState == ConnectionState.RESCANNING;
  }

  private synchronized void startRecoveryScan(long generation) {
    if (!isRecoveryScan(generation)) return;
    logConnection("retry_scan_start", generation, retryDevice, "fresh_advertisement_required");
    // Independent watchdog also covers missing EasyBLE scan completion callbacks.
    mainHandler.postDelayed(() -> finishRecoveryScan(generation, "scan_timeout"), 5000L);
    manager.startScan(
        new BleScanCallback() {
          @Override
          public void onStart(boolean success, String info) {
            if (!success)
              mainHandler.post(() -> finishRecoveryScan(generation, "scan_start_failed: " + info));
          }

          @Override
          public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
            mainHandler.post(
                () -> {
                  synchronized (BluetoothManager.this) {
                    if (!isRecoveryScan(generation)
                        || device == null
                        || !device.address.equals(activeAddress)) return;
                    recoveryScanMatch = device;
                    logConnection("retry_scan_found", generation, device, "rssi=" + rssi);
                  }
                });
          }

          @Override
          public void onFinish() {
            mainHandler.post(() -> finishRecoveryScan(generation, "device_not_rediscovered"));
          }
        });
  }

  private synchronized void finishRecoveryScan(long generation, String failure) {
    if (!isRecoveryScan(generation)) return;
    BleDevice freshDevice = recoveryScanMatch;
    recoveryScanMatch = null;
    // Change state before stopScan: some implementations synchronously call onFinish.
    connectionState = ConnectionState.RETRYING;
    cancelConnectionTimers();
    manager.stopScan();
    if (freshDevice == null) {
      lastConnectionError = failure;
      failAfterRetries(retryDevice, failure);
      return;
    }
    beginConnection(retryIndex, freshDevice, autoRetryCount);
  }

  private synchronized void failAfterRetries(BleDevice device, String reason) {
    connectionGeneration++;
    hardResetBleStack();
    device.connecting = false;
    device.connected = false;
    connectionState = ConnectionState.FAILED;
    activeAddress = device.address;
    bleDevice = null;
    retryDevice = null;
    retryAfterDisconnect = false;
    replaceDeviceAtIndex(safeDeviceIndex(indexValue, device), device);
    notifyAdapter();
    Toast.makeText(context, "BLE 连接失败，请刷新扫描后重试", Toast.LENGTH_LONG).show();
    Logger.e("BLE connection failed after retry: " + reason);
  }

  private synchronized void hardResetBleStack() {
    cancelConnectionTimers();
    clearTransportState();
    connectionGeneration++;
    if (manager != null) manager.destroy();
    initBleManager();
  }

  private synchronized boolean isActiveAttempt(long generation, String address) {
    return isMatchingAttempt(generation, connectionGeneration, address, activeAddress);
  }

  static boolean isMatchingAttempt(
      long callbackGeneration,
      long currentGeneration,
      String callbackAddress,
      String activeAddress) {
    return callbackGeneration == currentGeneration
        && callbackAddress != null
        && callbackAddress.equals(activeAddress);
  }

  static boolean shouldAutoRetry(int completedRetries) {
    return completedRetries < MAX_AUTO_RETRIES;
  }

  private void cancelConnectionTimers() {
    mainHandler.removeCallbacksAndMessages(null);
  }

  private void clearTransportState() {
    writeQueue.clear();
    serialLineAccumulator.clear();
    clearSerialCharacteristics();
    motionGeneration = 0L;
    readValue = null;
  }

  private int safeDeviceIndex(int preferredIndex, BleDevice device) {
    if (preferredIndex >= 0
        && preferredIndex < deviceList.size()
        && deviceList.get(preferredIndex).address.equals(device.address)) return preferredIndex;
    for (int i = 0; i < deviceList.size(); i++) {
      if (deviceList.get(i).address.equals(device.address)) return i;
    }
    deviceList.add(device);
    return deviceList.size() - 1;
  }

  private void replaceDeviceAtIndex(int index, BleDevice device) {
    if (index >= 0 && index < deviceList.size()) deviceList.set(index, device);
    notifyAdapter();
  }

  private void logConnection(String event, long generation, BleDevice device, String details) {
    String address = device == null ? "none" : device.address;
    String message =
        "event=" + event + ",generation=" + generation + ",address=" + address + "," + details;
    observe("ble_" + event, message);
    Log.i("Bluetooth_Connection", message);
    Logger.i(message);
  }

  public synchronized String getConnectionStatus(String address) {
    if (address == null || activeAddress == null || !address.equals(activeAddress)) return "连接";
    switch (connectionState) {
      case CONNECTING:
        return "连接中";
      case ENABLING_NOTIFY:
        return "初始化串口";
      case SERIAL_READY:
        return "断开";
      case RETRYING:
        return "自动重试";
      case RESCANNING:
        return "重新扫描小车";
      case DISCONNECTING:
        return "断开中";
      case FAILED:
        return "连接失败";
      case DISCONNECTED:
      default:
        return "连接";
    }
  }

  public synchronized ConnectionState getConnectionState() {
    return connectionState;
  }

  public synchronized String getLastConnectionError() {
    return lastConnectionError;
  }

  public boolean isBleConnected() {
    return bleDevice != null && bleDevice.connected;
  }

  public boolean isSerialReady() {
    return connectionState == ConnectionState.SERIAL_READY
        && isBleConnected()
        && writeServiceInfo != null
        && writeCharacteristic != null
        && notifyServiceInfo != null
        && notifyCharacteristic != null
        && notifyEnabled;
  }

  private void clearSerialCharacteristics() {
    writeServiceInfo = null;
    writeCharacteristic = null;
    notifyServiceInfo = null;
    notifyCharacteristic = null;
    notifySuccessUuids.clear();
    notifyEnabled = false;
  }

  private void notifyAdapter() {
    if (adapter != null) adapter.notifyDataSetChanged();
  }

  private void onSerialDataReceived(String data) {
    Logger.i("Serial data received from BLE: " + data);
    for (String line : serialLineAccumulator.accept(data)) {
      localBroadcastManager.sendBroadcast(
          new Intent(Constants.DEVICE_ACTION_DATA_RECEIVED)
              .putExtra("from", "ble")
              .putExtra("data", line));
    }
  }
}
