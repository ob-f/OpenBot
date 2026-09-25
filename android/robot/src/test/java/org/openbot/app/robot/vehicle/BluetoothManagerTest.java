package org.openbot.app.robot.vehicle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.bean.CharacteristicInfo;
import com.ficat.easyble.gatt.bean.ServiceInfo;
import com.ficat.easyble.gatt.callback.BleMtuCallback;
import com.ficat.easyble.gatt.callback.BleNotifyCallback;
import com.ficat.easyble.gatt.callback.BleWriteCallback;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.app.robot.utils.Constants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, shadows = BluetoothManagerTest.FakeBleManager.class)
public class BluetoothManagerTest {
  private static final String SERVICE_UUID = "61653dc3-4021-4d1e-ba83-8b4eec61d613";
  private static final String RX_UUID = "06386c14-86ea-4d71-811c-48f97c58f8c9";
  private static final String TX_UUID = "9bf1103b-834c-47cf-b149-c9e4bcf778a7";

  private BluetoothManager bluetoothManager;
  private FakeBleManager transport;
  private BleDevice device;

  @Before
  public void setUp() {
    ReflectionHelpers.setStaticField(BleManager.class, "instance", null);
    bluetoothManager = new BluetoothManager(RuntimeEnvironment.getApplication());
    transport = Shadow.extract(BleManager.getInstance());
    device = createDevice("8C:94:DF:A1:CC:A6");
    device.connected = true;
    bluetoothManager.bleDevice = device;
  }

  @Test
  public void usesOnlyOfficialOpenBotUartCharacteristics() {
    ServiceInfo unrelatedService = new ServiceInfo("0000180f-0000-1000-8000-00805f9b34fb");
    ServiceInfo uartService = new ServiceInfo(SERVICE_UUID);
    transport.services = new LinkedHashMap<>();
    transport.services.put(
        unrelatedService,
        Arrays.asList(
            new CharacteristicInfo("unrelated-write", false, true, false, false),
            new CharacteristicInfo("unrelated-notify", false, false, true, false)));
    transport.services.put(
        uartService,
        Arrays.asList(
            new CharacteristicInfo(TX_UUID, false, false, true, false),
            new CharacteristicInfo(RX_UUID, false, true, false, false)));

    bluetoothManager.addDeviceInfoDataAndUpdate();

    assertEquals(1, transport.mtuRequests);
    assertFalse(bluetoothManager.isSerialReady());
    bluetoothManager.mtuCallback.onMtuChanged(64, device);
    assertEquals(SERVICE_UUID, transport.notifyServiceUuid);
    assertEquals(TX_UUID, transport.notifyCharacteristicUuid);

    bluetoothManager.notifyCallback.onNotifySuccess(TX_UUID, device);
    assertTrue(bluetoothManager.isSerialReady());
    bluetoothManager.write("c0,0\n");
    assertEquals(1, transport.writes);
    assertEquals(SERVICE_UUID, transport.writeServiceUuid);
    assertEquals(RX_UUID, transport.writeCharacteristicUuid);
  }

  @Test
  public void writeBeforeNotificationReadyIsIgnored() {
    bluetoothManager.write("c0,0\n");
    assertEquals(0, transport.writes);
  }

  @Test
  public void notificationFailureClearsSerialReadyState() {
    makeSerialReady();
    assertTrue(bluetoothManager.isSerialReady());

    bluetoothManager.notifyCallback.onFailure(1, "failed", device);

    assertFalse(bluetoothManager.isSerialReady());
    bluetoothManager.write("c0,0\n");
    assertEquals(0, transport.writes);
  }

  @Test
  public void disconnectClearsSerialReadyState() {
    makeSerialReady();

    bluetoothManager.connectCallback.onDisconnected("remote", 0, device);

    assertFalse(bluetoothManager.isSerialReady());
  }

  @Test
  public void receivedDataIdentifiesBleTransport() {
    Context context = RuntimeEnvironment.getApplication();
    Intent[] received = new Intent[1];
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            received[0] = intent;
          }
        };
    LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(context);
    broadcasts.registerReceiver(receiver, new IntentFilter(Constants.DEVICE_ACTION_DATA_RECEIVED));

    bluetoothManager.notifyCallback.onCharacteristicChanged("fDIY\n".getBytes(), device);
    org.robolectric.Shadows.shadowOf(Looper.getMainLooper()).idle();

    assertEquals("ble", received[0].getStringExtra("from"));
    assertEquals("fDIY\n", received[0].getStringExtra("data"));
    broadcasts.unregisterReceiver(receiver);
  }

  @Test
  public void vehicleBleInitializationIsIdempotent() {
    Vehicle vehicle = new Vehicle(RuntimeEnvironment.getApplication(), 115200);
    vehicle.initBle();
    BluetoothManager first = ReflectionHelpers.getField(vehicle, "bluetoothManager");

    vehicle.initBle();

    assertSame(first, ReflectionHelpers.getField(vehicle, "bluetoothManager"));
  }

  private void makeSerialReady() {
    ServiceInfo uartService = new ServiceInfo(SERVICE_UUID);
    transport.services = new LinkedHashMap<>();
    transport.services.put(
        uartService,
        Arrays.asList(
            new CharacteristicInfo(TX_UUID, false, false, true, false),
            new CharacteristicInfo(RX_UUID, false, true, false, false)));
    bluetoothManager.addDeviceInfoDataAndUpdate();
    bluetoothManager.notifyCallback.onNotifySuccess(TX_UUID, device);
  }

  private static BleDevice createDevice(String address) {
    BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    return ReflectionHelpers.callConstructor(
        BleDevice.class, ClassParameter.from(BluetoothDevice.class, bluetoothDevice));
  }

  @Implements(BleManager.class)
  public static class FakeBleManager {
    @RealObject BleManager realManager;
    Map<ServiceInfo, List<CharacteristicInfo>> services = new LinkedHashMap<>();
    int mtuRequests;
    int writes;
    String notifyServiceUuid;
    String notifyCharacteristicUuid;
    String writeServiceUuid;
    String writeCharacteristicUuid;

    @Implementation
    protected static boolean supportBle(Context context) {
      return true;
    }

    @Implementation
    protected BleManager init(Context context) {
      return realManager;
    }

    @Implementation
    protected Map<ServiceInfo, List<CharacteristicInfo>> getDeviceServices(String address) {
      return services;
    }

    @Implementation
    protected void setMtu(BleDevice device, int mtu, BleMtuCallback callback) {
      mtuRequests++;
    }

    @Implementation
    protected void notify(
        BleDevice device,
        String serviceUuid,
        String characteristicUuid,
        BleNotifyCallback callback) {
      notifyServiceUuid = serviceUuid;
      notifyCharacteristicUuid = characteristicUuid;
    }

    @Implementation
    protected void write(
        BleDevice device,
        String serviceUuid,
        String characteristicUuid,
        byte[] data,
        BleWriteCallback callback) {
      writes++;
      writeServiceUuid = serviceUuid;
      writeCharacteristicUuid = characteristicUuid;
    }
  }
}
