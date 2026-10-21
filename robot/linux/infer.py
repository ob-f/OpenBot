import time
import os
from datetime import datetime
from joystick import Joystick
from realsense import Realsense
import numpy as np
import serial
import pandas as pd
from PIL import Image
import pickle as pickle

CUR_DIR = os.path.join(os.path.dirname(__file__))


def load_image(filename):
    """Helper function to retrieve image from filepath as numpy array.

    Args:
        filename (str): Path to the image file.

    Returns:
        np.array: Image as np array with values between 0, 255.
    """
    img = Image.open(filename)
    img.load()
    data = np.asarray(img, dtype="int32")
    return data


def get_arduino_serial(device_address="/dev/ttyUSB0", baudrate=115200):
    """Getting serial objet from a device address.

    Args:
        device_address (str, optional): Address to the serial communication.
            Defaults to '/dev/ttyUSB0'.
        baudrate (int, optional): Baudrate of communication. Defaults to 115200.

    Returns:
        serial.Serial: Returns serial object which can be interfaced with.
    """

    try:
        arduino_serial = serial.Serial(device_address, baudrate=baudrate)
    except Exception as e:
        print(e)
        print(f"Arduino not found at {device_address} with baudrate {baudrate}")
        arduino_serial = None
    return arduino_serial


def apply_action_serial(serial_obj, left, right, scale=255):
    """Writes left and right motor command to serial interface (e.g.,
    Arduino board connected to motor drivers).

    Args:
        serial_obj (serial.Serial): Serial object that is retrieved via
            get_arduino_serial().
        left (float): Left motor command between -1, 1. This value is scaled
            by `scale`.
        right (float): Right motor command between -1, 1. This value is scaled
            by `scale`.
        scale (int): Scaling factor for left and right motor command.
            Default: 255
    Returns:
        (int, int): Scaled left and right motor command sent to serial object.
    """
    left = np.clip(left, -1.0, 1)
    right = np.clip(right, -1.0, 1)

    command_string = f"c{int(left*scale)},{int(right*scale)}\n"

    serial_obj.write(bytes(command_string, encoding="utf-8"))
    return int(left * scale), int(right * scale)


class InferNetwork:
    def __init__(
        self,
        policy_path=None,
        control_frequency=10,
        dataset_path=None,
        log_path=None,
        run_mode="inference",
        control_mode="dual",
        inference_backend="openvino",
    ):
        """Class for inference of policy, joystick control, and debug mode.

        Args:
            policy_path (str, optional): Path to policy. Required if run_mode is
                debug or inference. Defaults to None.
            control_frequency (int, optional): Frequency in Hz at which commands
                to motor are sent. Defaults to 10.
            dataset_path (str, optional): Path to dataset. Required for debug
                mode, where the input to the policy is loaded from dataset_path.
                Defaults to None.
            log_path (str, optional): Path to which the logs are saved. If None
                is passed, then saved to default log path. Defaults to None.
            run_mode (str, optional): Mode in which the program will run.
                Options: inference, debug, and joystick. Inference runs the
                policy from camera images and commands and sends actions to the
                motors. Joystick controls the robot via joystick command. Debug
                runs the policy from offline data passed through dataset_path.
                Defaults to "inference".
            control_mode (str, optional): Control mode for joystick. Control
                modes are "dual" and "joystick". Defaults to "dual".
            inference_backend (str, optional): Backends for inference. Options
                are openvino, tf, and tfline. Openvino is recommended for Intel
                hardware due to using Model Optimizer for optimised code.
                Defaults to "openvino".
        """

        self.inference_backend = inference_backend
        if inference_backend != "openvino":
            print(
                "Openvino backend recommended due to optimised inference on Intel CPU."
            )
        self.run_mode = run_mode
        self.dataset_path = dataset_path
        self.log_path = f"{CUR_DIR}/logs/" if log_path is None else log_path
        self.control_frequency = control_frequency
        self.policy_path = policy_path

        self.interpreter = self.get_interpreter()

        if run_mode == "debug":
            (
                self.img_dataset,
                self.cmd_dataset,
                self.action_dataset,
            ) = self.get_reference_dataset()
        else:
            self.joystick = Joystick(
                callback=self.joystick_callback,
                control_mode=control_mode,
                run_mode=run_mode,
            )
            # instantiating without any argument
            self.joystick_action = np.array([[0, 0]])

        self.realsense = Realsense(self.realsense_callback)

        self.serial = get_arduino_serial()

        self.img_input = None  # Comes from Realsense thread
        # Comes from Joystick thread
        self.cmd_input = np.array([[0]], np.float32)

        self.cmd_history = {"timestamp": [], "cmd": []}

        self.abort_signal = False

        self.state_log = []

    def realsense_callback(self, img):
        """Callback for realsense. The passed img is written to self.img_input.

        Args:
            img (np.array): Realsense thread passes an image in form of np.array
        """
        self.img_input = img

    def joystick_callback(self, action, cmd, run_mode, abort_signal):
        """Callback for joystick. The button commands are mapped to actions,
        cmd, control_mode, and abort_signal

        Args:
            action (np.array): Left and right motor commands
            cmd (int): Cmd for policy
            run_mode (str): Run mode either inference or joystick. Toggled
                by joystick
            abort_signal (bool): Whether to stop the running.
        """
        self.abort_signal = abort_signal
        self.cmd_input = np.array([[cmd]], np.float32)
        self.run_mode = run_mode
        self.joystick_action = np.array([[action[0], action[1]]])

    def get_reference_dataset(self):
        """Retrieves the dataset from self.dataset_path. Required for debug mode.

        Returns:
            np.array, np.array, np.array: Returns images, cmds, and actions as
                np.array.
        """
        filename = (
            f"{self.dataset_path}/sensor_data/matched_frame_ctrl_cmd_processed.txt"
        )
        df = pd.read_csv(filename)
        action = df[["left", "right"]].astype(np.float32)
        cmd = df[["cmd"]].astype(np.float32)
        image = df["frame"]
        return image, cmd, action

    def get_interpreter(self):
        """Retrieves the models that will be used for inference.

        Returns:
            model: Model either as tf, tflite, or openvino object depending on
                backend option.
        """
        try:
            if self.inference_backend == "tf":
                self.interpreter = self.get_tf_interpreter()
            elif self.inference_backend == "tflite":
                self.interpreter = self.get_tflite_interpreter()
            elif self.inference_backend == "openvino":
                self.interpreter = self.get_openvino_interpreter()
            return self.interpreter
        except Exception as e:
            print(e)
            print("Interpreter not found")
            return None

    def get_tflite_interpreter(self):
        """Retrieves tf lite model

        Returns:
            tflite.Interpreter: Trained model
        """
        import tflite_runtime.interpreter as tflite

        self.interpreter = tflite.Interpreter(f"{self.policy_path}/best.tflite")
        self.interpreter.allocate_tensors()

        # Get input and output tensors.
        self.input_details = self.interpreter.get_input_details()
        self.output_details = self.interpreter.get_output_details()
        return self.interpreter

    def get_tf_interpreter(self):
        """Retrieves tensorflow model

        Returns:
            tf.keras.Model: Returns a keras model.
        """
        import openbot.utils as utils

        with open(f"{self.policy_path}/trainer.p", "rb") as f:
            tr = pickle.load(f)
        self.interpreter = utils.load_model(
            f"{self.policy_path}/checkpoints/best.ckpt",
            tr["loss_fn"],
            tr["metric_list"],
            tr["custom_objects"],
        )
        return self.interpreter

    def get_openvino_interpreter(self):
        """Retrieves an openvino model

        Returns:
            openvino.runtime.Core: Returns compiled openvino model.
        """
        from openvino.runtime import Core

        model_xml = f"{self.policy_path}/openvino_model.xml"
        # Load model
        ie = Core()
        model = ie.read_model(model=model_xml)

        # Neural Compute Stick
        self.interpreter = ie.compile_model(model=model, device_name="CPU")

        input_layer1 = self.interpreter.input(0)
        input_layer2 = self.interpreter.input(1)
        self.output_layer = self.interpreter.output(0)
        return self.interpreter

    def predict_model(self, img_input, cmd_input):
        """Peforms inference of model

        Args:
            img_input (np.array): image from camera
            cmd_input (np.array): Command from joystick

        Returns:
            (float, float): Action to be send to motors
        """
        if self.inference_backend == "tf":
            output = self.interpreter([img_input[None, ...], cmd_input[None, ...]])
        elif self.inference_backend == "tflite":
            self.interpreter.set_tensor(
                self.input_details[0]["index"], np.expand_dims(img_input, axis=0)
            )
            self.interpreter.set_tensor(self.input_details[1]["index"], cmd_input)
            self.interpreter.invoke()

            output = self.interpreter.get_tensor(self.output_details[0]["index"])
        elif self.inference_backend == "openvino":
            output = self.interpreter([cmd_input, img_input[None, ...]])[
                self.output_layer
            ]

        return output

    def apply_action(self, left, right):
        """Applies action to motor

        Args:
            left (float): Left motor command
            right (float): Right motor command

        Returns:
            (int, int): Returns the commands sent to motor (scaled).
        """
        if not (self.run_mode == "debug") and self.serial is not None:
            return apply_action_serial(self.serial, left, right)
        else:
            return 0, 0

    def run_debug(self):
        """Runs the debug mode, i.e., not using real camera images, but loaded
        from a collected dataset.
        """
        time_steps = {"time_infer": []}
        while True:
            if not hasattr(self, "counter"):
                self.counter = 0

            cmd_input = np.array(
                [self.cmd_dataset.iloc[self.counter]], dtype=np.float32
            )
            img_input = load_image(self.img_dataset.iloc[self.counter])

            if np.max(img_input) > 1.0:
                img_input = np.array(img_input / 255, dtype=np.float32)

            time_start = time.time()  # Start stopwatch
            action = self.predict_model(img_input, cmd_input) * 255
            time_steps["time_infer"].append(time.time() - time_start)

            desired_action = self.action_dataset.iloc[self.counter, :]
            print(
                f"Action: {action[0,0]:.2f}, {action[0,1]:.2f},"
                + f" Desired action: {desired_action[0]:.2f}, {desired_action[1]:.2f}"
            )

            self.counter += 1
            if self.counter >= self.action_dataset.shape[0]:
                break

        infer_time = time_steps["time_infer"]
        print(f"Average infer time: {np.mean(np.array(infer_time[1:])):.3f}")
        print(f"Control frequency: {1/np.mean(np.array(infer_time[1:])):.1f}")
        self.apply_action(0, 0)

    def run(self):
        """Run function."""
        if self.run_mode == "debug":
            self.run_debug()
        else:
            self.run_robot()

    def run_robot(self):
        """Runs the robot either in joystick or inference mode."""
        time_steps = {"time_sleep": [], "control_frequency": [], "time_infer": []}

        try:
            print("Waiting 2s for joystick and realsense thread to startup")
            time.sleep(2)
            print("Starting to run")
            while True:
                time_start = time.time()  # Start stopwatch

                if self.abort_signal:  # Signal comes from joystick, pressing x button
                    break

                if self.run_mode == "inference":
                    action = self.predict_model(self.img_input, self.cmd_input)
                elif self.run_mode == "joystick":
                    action = self.joystick_action

                # Send signal via serial port to motor
                sent_action = self.apply_action(action[0, 0], action[0, 1])

                self.store_state(action, sent_action)

                # Calculating time to sleep to achieve control_frequency
                time_steps["time_infer"].append(time.time() - time_start)
                time_per_step = 1 / self.control_frequency
                time_sleep = time_per_step - time_steps["time_infer"][-1]
                time_steps["time_sleep"].append(time_sleep)

                if time_sleep < 0.0:
                    print(
                        f"Inference time ({time_steps['time_infer'][-1]:.3f}s) "
                        + f"exceeds control frequency: {time_per_step:.3f}s. "
                    )
                else:
                    time.sleep(time_sleep)

                time_steps["control_frequency"].append(time.time() - time_start)

            self.apply_action(0, 0)
        except KeyboardInterrupt:
            print("EXITING NOW")

        infer_time = time_steps["time_infer"]
        control_freq = time_steps["control_frequency"]
        control_freq = 1 / np.mean(np.array(control_freq[1:]))
        print(f"Average infer time: {np.mean(np.array(infer_time[1:])):.4f}")
        print(f"Average control frequency: {control_freq:.4f}")

        self.apply_action(0, 0)

    def store_state(self, action, sent_action):
        """Store recorded states. The stored states will be saved in write_log().

        Args:
            action ([float, float]): Left and right motor command as output from
                either policy or joystic
            sent_action ([float, float]): Actual sent (scaled) command to motors.
        """

        timestamp = time.time_ns()

        if (
            not self.cmd_history["cmd"]
            or self.cmd_input[0, 0] != self.cmd_history["cmd"][-1]
        ):
            # Log change in cmd (for indicatroLog.txt)
            self.cmd_history["timestamp"].append(timestamp)
            self.cmd_history["cmd"].append(self.cmd_input[0, 0])

        log_dict = {
            "timestamp": timestamp,
            "action": action,
            "sent_action": sent_action,
            "cmd": self.cmd_history,
            "image": self.img_input,
        }
        self.state_log.append(log_dict)

    def write_log(self):
        """Pickled file will be converted to training
        data in `generate_data_for_training.py`
        """
        now = datetime.now().strftime("%m%d%Y-%H%M%S")
        with open(f"{self.log_path}/logs_{now}.p", "wb") as f:
            pickle.dump(self.state_log, f)
