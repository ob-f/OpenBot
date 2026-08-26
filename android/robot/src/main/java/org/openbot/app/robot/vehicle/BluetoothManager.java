package org.openbot.app.robot.vehicle;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.Logger;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.gatt.callback.BleMtuCallback;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import com.ficat.easyble.scan.BleScanCallback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openbot.app.robot.main.ScanDeviceAdapter;
import org.openbot.app.robot.utils.Constants;

public class BluetoothManager {
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
  private final LocalBroadcastManager localBroadcastManager;
  private String serviceUUID = "61653dc3-4021-4d1e-ba83-8b4eec61d613";
  UUID[] uuidArray = new UUID[] {UUID.fromString(serviceUUID)};

  public BluetoothManager(Context context) {
    this.context = context;
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

  public void startScan() {
    manager.startScan(
        new BleScanCallback() {
          @Override
          public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
            for (BleDevice d : deviceList) {
              if (device.address.equals(d.address)) {
                return;
              }
            }
            deviceList.add(device);
            adapter.notifyDataSetChanged();
          }

          @Override
          public void onStart(boolean startScanSuccess, String info) {
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
            adapter.notifyDataSetChanged();
          }
        });
  }

  public void stopScan() {
    manager.stopScan();
  }

  public void toggleConnection(int position, BleDevice device) {
    if (bleDevice.connecting) return;
    indexValue = position;
    if (isBleConnected()) {
      if (bleDevice.address.equals(device.address)) {
        BleManager.getInstance().disconnect(bleDevice.address);
        bleDevice = null;
      }
    } else {
      BleManager.getInstance().connect(bleDevice.address, connectCallback);
    }
  }

  public BleConnectCallback connectCallback =
      new BleConnectCallback() {
        @Override
        public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
          bleDevice = device;
          deviceList.remove(indexValue);
          deviceList.add(indexValue, device);
          adapter.notifyDataSetChanged();
        }

        @Override
        public void onConnected(BleDevice device) {
          bleDevice = device;
          deviceList.remove(indexValue);
          deviceList.add(indexValue, device);
          adapter.notifyDataSetChanged();
          addDeviceInfoDataAndUpdate();
          Logger.i("Successfully connected: " + " " + device);
        }

        @Override
        public void onDisconnected(String info, int status, BleDevice device) {
          bleDevice = null;
          adapter.notifyDataSetChanged();
          Logger.i("disconnected!");
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
          Logger.e("connect fail:" + info);
          bleDevice = null;
          deviceList.remove(indexValue);
          deviceList.add(indexValue, device);
          Toast.makeText(context, "Connection fail: " + info, Toast.LENGTH_LONG).show();
          adapter.notifyDataSetChanged();
        }
      };

  public void addDeviceInfoDataAndUpdate() {
    if (bleDevice == null) return;
    Map<ServiceInfo, List<CharacteristicInfo>> deviceInfo =
        BleManager.getInstance().getDeviceServices(bleDevice.address);
    if (deviceInfo == null) {
      return;
    }
    for (Map.Entry<ServiceInfo, List<CharacteristicInfo>> e : deviceInfo.entrySet()) {
      for (CharacteristicInfo characteristicInfo : e.getValue()) {
        if (characteristicInfo.notify) {
          notifyCharacteristic = characteristicInfo;
          notifyServiceInfo = e.getKey();
          if (isBleConnected())
            // Set the MTU size to 64 bytes
            BleManager.getInstance().setMtu(bleDevice, 64, mtuCallback);
        }
        if (characteristicInfo.writable) {
          writeServiceInfo = e.getKey();
          writeCharacteristic = characteristicInfo;
        }
      }
    }
  }

  public void write(String msg) {
    if (isBleConnected()) {
      BleManager.getInstance()
          .write(
              bleDevice,
              writeServiceInfo.uuid,
              writeCharacteristic.uuid,
              msg.getBytes(UTF_8),
              writeCallback);
    }
  }

  public BleMtuCallback mtuCallback =
      new BleMtuCallback() {
        @Override
        public void onMtuChanged(int mtu, BleDevice device) {
          BleManager.getInstance()
              .notify(bleDevice, notifyServiceInfo.uuid, notifyCharacteristic.uuid, notifyCallback);
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
          Logger.e("mtu fail:" + info + " " + failCode);
        }
      };
  public BleWriteCallback writeCallback =
      new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] data, BleDevice device) {
          String value = new String(data, StandardCharsets.UTF_8);
          Logger.i("write success:" + value);
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
          Logger.e("write fail:" + info + " " + failCode);
        }
      };

  public BleNotifyCallback notifyCallback =
      new BleNotifyCallback() {
        @Override
        public void onCharacteristicChanged(byte[] data, BleDevice device) {
          readValue = new String(data);
          onSerialDataReceived(readValue);
        }

        @Override
        public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
          if (!notifySuccessUuids.contains(notifySuccessUuid)) {
            notifySuccessUuids.add(notifySuccessUuid);
          }
        }

        @Override
        public void onFailure(int failCode, String info, BleDevice device) {
          Logger.e("notify fail:" + info);
        }
      };

  public boolean isBleConnected() {
    return bleDevice != null && bleDevice.connected;
  }

  private void onSerialDataReceived(String data) {
    // Add whatever you want here
    Logger.i("Serial data received from BLE: " + data);
    localBroadcastManager.sendBroadcast(
        new Intent(Constants.DEVICE_ACTION_DATA_RECEIVED)
            .putExtra("from", "usb")
            .putExtra("data", data));
  }
}
