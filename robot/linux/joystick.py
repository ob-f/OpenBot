import pygame
import threading

THRESHOLD = 0.122  # Manual threshold for idle joystick position. Need to adjust based on joystick


class Joystick(threading.Thread):
    def __init__(
        self,
        callback,
        run_mode="joystick",  # inference or joystick
        control_mode="dual",  # dual or joystick
    ):
        """Thread for Joystick commands. Currently, the implementation supports
        Xbox controllers. See https://www.pygame.org/docs/ref/joystick.html
        for further implementation details of other controllers.

        Args:
            callback (function): Callback for processing commands received
                from controller
            run_mode (str, optional): The mode of the run. Either "inference" using
                a Neural Network for control or "joystick" for manual control.
                Toggle between modes via A button. Defaults to "joystick".
            control_mode (str, optional): The mode how to control the vehicle.
                Choose between "joystick" or "dual" mode. Defaults to "dual".
        """
        self.callback = callback
        self.control_mode = control_mode

        # Initialise and register joystick
        pygame.init()
        self.joystick = pygame.joystick.Joystick(0)
        self.joystick.init()

        # Flags sent via self.callback to infer.py
        self.abort_signal = False
        self.joystick_mode = True if run_mode == "joystick" else False
        self.cmd = 0  # currently always zero. Map to hat input of controller if needed

        # Start thread
        super().__init__(name="joystick-input-thread")
        self.start()

        print("Joystick thread started. Waiting for inputs")

    def run(self):
        """Infinite loop that receives inputs via get_control. Sends cmd,
        abort_signal, run_mode, and the action via callback
        """
        while True:
            run_mode = "joystick" if self.joystick_mode else "inference"
            self.callback(self.get_control(), self.cmd, run_mode, self.abort_signal)

    def get_thresholded_value(self, value):
        """Clips the read axis values. By default, idle axis are non-zero. Thus
        a threshold is required

        Args:
            value (float): The signal read from axis. Between -1 and 1.

        Returns:
            float: Returns the thresholded value
        """
        if abs(value) > THRESHOLD:
            return value
        else:
            return 0.0

    def get_left_x(self):
        """Reads the x axis value from left joystick

        Returns:
            float: Thresholded x axis value.
        """
        return self.get_thresholded_value(self.joystick.get_axis(0))

    def get_right_x(self):
        """Reads the x axis value from righ joystick

        Returns:
            float: Thresholded x axis value.
        """
        return self.get_thresholded_value(self.joystick.get_axis(3))

    def get_left_y(self):
        """Reads the y axis value from left joystick

        Returns:
            float: Thresholded y axis value.
        """
        return self.get_thresholded_value(self.joystick.get_axis(1))

    def get_right_y(self):
        """Reads the y axis value from right joystick

        Returns:
            float: Thresholded y axis value.
        """
        return self.get_thresholded_value(self.joystick.get_axis(4))

    def get_xaxis(self):
        """Reads the x axis value from either left or right joystick

        Returns:
            float: Thresholded x axis value.
        """
        x_axis = self.get_left_x()
        if x_axis == 0.0:
            x_axis = self.get_right_x()
        return x_axis

    def get_yaxis(self):
        """Reads the y axis value from either left or right joystick

        Returns:
            float: Thresholded y axis value.
        """
        y_axis = self.get_left_y()
        if y_axis == 0.0:
            y_axis = self.get_right_y()
        return y_axis

    def get_control(self):
        """Reads the joystick input. Either button or joystick actions.

        Returns:
            (float, float): Reads the motor commands to be send to left and
                right motor.
        """
        events = pygame.event.get()
        for event in events:
            if event.type == pygame.JOYBUTTONDOWN:
                if self.joystick.get_button(0):
                    # A Button
                    self.joystick_mode = not self.joystick_mode
                    print(f"Toggling joystick_mode to {self.joystick_mode}")
                elif self.joystick.get_button(1):
                    # B Button
                    # pass
                    print("B Button")
                elif self.joystick.get_button(2):
                    # X Button
                    print("Abort signal received")
                    self.abort_signal = True
                elif self.joystick.get_button(3):
                    # Y Button
                    print("Y Button pushed")

        if self.control_mode == "joystick":
            x_axis = self.get_xaxis()
            y_axis = self.get_yaxis()
            return self.convert_joystick_to_control(x_axis, y_axis)
        elif self.control_mode == "dual":
            left = self.get_left_y()
            right = self.get_right_y()
            return self.convert_dual_to_control(left, right)

    def convert_joystick_to_control(self, x_axis, y_axis, print_out=False):
        """Mapping for joystick to left and right wheel command (joystick control mode)

        Args:
            x_axis (float): x_axis value of joystick
            y_axis (float): y_axis value of joystick
            print_out (bool, optional): Whether to print out the calculated
                motor commands. Defaults to False.

        Returns:
            (float, float): calculated motor commands for left and right wheel.
        """
        left = -y_axis
        right = -y_axis

        if left >= 0:
            left += x_axis
        else:
            left -= x_axis

        if right >= 0:
            right -= x_axis
        else:
            right += x_axis

        if print_out:
            print(f"{x_axis}, {y_axis}: c{left}, {right}")
        return left, right

    def convert_dual_to_control(self, left, right):
        """Mapping to left and right wheel command (dual control mode)

        Args:
            left (float): left value of joystick
            right (float): right value of joystick

        Returns:
            (float, float): calculated motor commands for left and right wheel.
        """
        return -left, -right
