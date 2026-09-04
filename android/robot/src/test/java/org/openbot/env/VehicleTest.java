package org.openbot.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbot.utils.Enums;
import org.openbot.vehicle.Control;
import org.openbot.vehicle.RangeTelemetrySnapshot;
import org.openbot.vehicle.Vehicle;

@RunWith(AndroidJUnit4.class)
public class VehicleTest {

  private Vehicle vehicle;

  @Before
  public void setupVehicle() {
    vehicle = new Vehicle(ApplicationProvider.getApplicationContext(), 115200);
  }

  @Test
  public void getRotation() {
    assertEquals(0, vehicle.getRotation(), 0.0);

    vehicle.setControl(new Control(0.5f, 1));
    assertEquals(-60, vehicle.getRotation(), 0.0);

    vehicle.setControl(new Control(0f, 1));
    assertEquals(-180, vehicle.getRotation(), 0.0);
  }

  @Test
  public void getSpeed() {
    vehicle.setSpeedMultiplier(Enums.SpeedMode.SLOW.getValue());
    vehicle.setControl(new Control(-1, -1));

    assertEquals(-128, vehicle.getLeftSpeed(), 0.0);
    assertEquals(-128, vehicle.getRightSpeed(), 0.0);

    vehicle.setSpeedMultiplier(Enums.SpeedMode.NORMAL.getValue());
    vehicle.setControl(new Control(-1, -1));

    assertEquals(-192, vehicle.getLeftSpeed(), 0.0);
    assertEquals(-192, vehicle.getRightSpeed(), 0.0);

    vehicle.setSpeedMultiplier(Enums.SpeedMode.FAST.getValue());
    vehicle.setControl(new Control(1, 1));

    assertEquals(255, vehicle.getLeftSpeed(), 0.0);
    assertEquals(255, vehicle.getRightSpeed(), 0.0);
  }

  @Test
  public void cartFeatureHandshakeAdvertisesRangeAndCentimetersBecomeMillimeters() {
    vehicle.processVehicleConfig("CART_AT8236:s:");
    RangeTelemetrySnapshot initial = vehicle.getRangeTelemetry();
    assertTrue(initial.capabilityAdvertised);
    assertFalse(initial.hasReading);

    vehicle.setSonarReading(82f);
    RangeTelemetrySnapshot reading = vehicle.getRangeTelemetry();
    assertTrue(reading.hasReading);
    assertEquals(820, reading.minimumDistanceMm);
    assertTrue(reading.receivedAtMs >= 0L);
  }

  @Test
  public void nonRangeFirmwareAndInvalidReadingRemainUnavailable() {
    vehicle.processVehicleConfig("CART_AT8236:");
    assertFalse(vehicle.processSonarMessage("NaN"));
    assertFalse(vehicle.processSonarMessage("80,81"));
    assertFalse(vehicle.processSonarMessage("0"));
    RangeTelemetrySnapshot reading = vehicle.getRangeTelemetry();
    assertFalse(reading.capabilityAdvertised);
    assertFalse(reading.hasReading);
  }

  @Test
  public void firmwareErrorIsRetainedForDiagnostics() {
    vehicle.processVehicleConfig("CART_AT8236:s:");
    vehicle.recordFirmwareError("!ERR,sensor_center_unavailable");
    assertEquals("!ERR,sensor_center_unavailable", vehicle.getRangeTelemetry().lastFirmwareError);
  }
}
