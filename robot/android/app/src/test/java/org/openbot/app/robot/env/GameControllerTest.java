package org.openbot.app.robot.env;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openbot.app.robot.utils.Enums;
import org.openbot.app.robot.vehicle.Control;

public class GameControllerTest {
  @Test
  public void convertDualToControl_test() {
    GameController gameController = new GameController(Enums.DriveMode.DUAL);
    assertEquals(Enums.DriveMode.DUAL, gameController.getDriveMode());
    Control control = gameController.convertDualToControl(0.5f, -0.5f);
    assertEquals(control.getLeft(), -0.5f, 0.0f);
    assertEquals(control.getRight(), 0.5f, 0.0f);
  }

  @Test
  public void convertGameToControl_test() {
    GameController gameController = new GameController(Enums.DriveMode.GAME);
    assertEquals(Enums.DriveMode.GAME, gameController.getDriveMode());
    Control control;
    control = gameController.convertGameToControl(0.0f, 0.5f, 1.0f);
    assertEquals(control.getLeft(), 1.0f, 0.0f);
    assertEquals(control.getRight(), -0.5f, 0.0f);

    control = gameController.convertGameToControl(0.0f, 0.5f, -1.0f);
    assertEquals(control.getLeft(), -0.5f, 0.0f);
    assertEquals(control.getRight(), 1.0f, 0.0f);

    control = gameController.convertGameToControl(0.0f, 0.5f, 0.0f);
    assertEquals(control.getLeft(), 0.5f, 0.0f);
    assertEquals(control.getRight(), 0.5f, 0.0f);
  }

  @Test
  public void convertJoystickToControl_test() {
    GameController gameController = new GameController(Enums.DriveMode.JOYSTICK);
    assertEquals(Enums.DriveMode.JOYSTICK, gameController.getDriveMode());
    Control control;
    control = gameController.convertJoystickToControl(0.5f, -0.5f);
    assertEquals(control.getLeft(), 1.0f, 0.0f);
    assertEquals(control.getRight(), 0.0f, 0.0f);

    control = gameController.convertJoystickToControl(-0.5f, -0.5f);
    assertEquals(control.getLeft(), 0.0f, 0.0f);
    assertEquals(control.getRight(), 1.0f, 0.0f);
  }
}
