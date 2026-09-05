package org.openbot.vehicle;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Looper;
import com.ficat.easyble.BleDevice;
import com.ficat.easyble.BleManager;
import com.ficat.easyble.gatt.callback.BleConnectCallback;
import com.ficat.easyble.scan.BleScanCallback;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, shadows = BluetoothRescanRecoveryTest.FakeBle.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class BluetoothRescanRecoveryTest {
  private BluetoothManager manager;
  private FakeBle transport;
  private BleDevice original;

  @Before
  public void setup() {
    ReflectionHelpers.setStaticField(BleManager.class, "instance", null);
    manager = new BluetoothManager(RuntimeEnvironment.getApplication(), null);
    transport = Shadow.extract(BleManager.getInstance());
    original = device("8C:94:DF:A1:CC:A6");
    manager.toggleConnection(0, original);
  }

  private static BleDevice device(String address) {
    BluetoothDevice bt = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    return ReflectionHelpers.callConstructor(
        BleDevice.class, ClassParameter.from(BluetoothDevice.class, bt));
  }

  private void failAndScan() {
    transport.connection.onFailure(201, "status=133", original);
    assertEquals(BluetoothManager.ConnectionState.RESCANNING, manager.getConnectionState());
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(750));
    assertNotNull(transport.scan);
    assertEquals(1, transport.connections);
  }

  @Test
  public void repeatedUiClicksCannotTruncateRecoveryScan() {
    failAndScan();
    manager.stopScan();
    manager.toggleConnection(0, original);
    manager.startScan();
    shadowOf(Looper.getMainLooper()).idle();
    assertEquals(1, transport.scans);
    assertEquals(1, transport.connections);
    assertEquals(BluetoothManager.ConnectionState.RESCANNING, manager.getConnectionState());
  }

  @Test
  public void retryUsesFreshSameAddressObjectOnlyAfterScanFinishes() {
    failAndScan();
    BleDevice fresh = device(original.address);
    transport.scan.onLeScan(fresh, -45, new byte[0]);
    shadowOf(Looper.getMainLooper()).idle();
    assertEquals(1, transport.connections);
    transport.scan.onFinish();
    shadowOf(Looper.getMainLooper()).idle();
    assertEquals(2, transport.connections);
    assertSame(fresh, transport.lastDevice);
    assertFalse(manager.isSerialReady());
    assertEquals(BluetoothManager.ConnectionState.RETRYING, manager.getConnectionState());
  }

  @Test
  public void wrongDeviceAndMissingCompletionCannotCauseBlindRetry() {
    failAndScan();
    transport.scan.onLeScan(device("AA:BB:CC:DD:EE:FF"), -30, new byte[0]);
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(5000));
    assertEquals(1, transport.connections);
    assertEquals(BluetoothManager.ConnectionState.FAILED, manager.getConnectionState());
  }

  @Test
  public void delayedConnectionAndScanCallbacksCannotReplaceNewAttempt() {
    BleConnectCallback oldConnection = transport.connection;
    failAndScan();
    BleScanCallback oldScan = transport.scan;
    BleDevice fresh = device(original.address);
    oldScan.onLeScan(fresh, -45, new byte[0]);
    oldScan.onFinish();
    shadowOf(Looper.getMainLooper()).idle();
    oldConnection.onDisconnected("late", 133, original);
    oldScan.onFinish();
    shadowOf(Looper.getMainLooper()).idle();
    assertEquals(2, transport.connections);
    assertEquals(BluetoothManager.ConnectionState.RETRYING, manager.getConnectionState());
  }

  @Test
  public void scanFailureEndsAttemptWithoutConnectingCachedDevice() {
    failAndScan();
    transport.scan.onStart(false, "permission denied");
    shadowOf(Looper.getMainLooper()).idle();
    assertEquals(1, transport.connections);
    assertEquals(BluetoothManager.ConnectionState.FAILED, manager.getConnectionState());
  }

  @Test
  public void secondConnectionFailureDoesNotStartAnUnlimitedRetryLoop() {
    failAndScan();
    BleDevice fresh = device(original.address);
    transport.scan.onLeScan(fresh, -45, new byte[0]);
    transport.scan.onFinish();
    shadowOf(Looper.getMainLooper()).idle();
    transport.connection.onFailure(201, "status=133", fresh);
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(20));
    assertEquals(2, transport.connections);
    assertEquals(1, transport.scans);
    assertEquals(BluetoothManager.ConnectionState.FAILED, manager.getConnectionState());
  }

  @Implements(BleManager.class)
  public static class FakeBle {
    @RealObject BleManager real;
    BleConnectCallback connection;
    BleScanCallback scan;
    BleDevice lastDevice;
    int connections;
    int scans;

    @Implementation
    protected static boolean supportBle(Context context) {
      return true;
    }

    @Implementation
    protected BleManager init(Context context) {
      return real;
    }

    @Implementation
    protected void destroy() {}

    @Implementation
    protected void connect(BleDevice device, BleConnectCallback callback) {
      lastDevice = device;
      connection = callback;
      connections++;
    }

    @Implementation
    protected void startScan(BleScanCallback callback) {
      scan = callback;
      scans++;
      callback.onStart(true, "started");
    }

    @Implementation
    protected void stopScan() {
      if (scan != null) scan.onFinish();
    }
  }
}
