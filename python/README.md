# Python

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

This module is an embedded Linux alternative to the smartphone control of an OpenBot vehicle. Written in Python, the OpenBot can be controlled using a linux-based computer and a camera for sensing.

The robot can be controlled in two ways: through inference of a Neural Network policy or via joystick.

```
├── __init__.py
├── README.md
├── requirements.txt
├── run.py
├── generate_data_for_training.py
├── export_openvino.py
├── infer.py
├── joystick.py
├── realsense.py
└── tests
    ├── test_data
    │   └── logs1
    │       └── ...
    ├── test_models
    │   ├── openvino
    │   ├── tflite
    │   └── tf.zip
    ├── test_export_openvino.py
    ├── test_infer.py
    ├── test_joystick.py
    ├── test_motor.py
    └── test_realsense.py

```
## Running Robot

To operate the robot, run the `run.py`, which is the main Python script. The robot can be run in 3 modes:
- Debug: This mode runs the policy off-line. I.e., instead of real camera images and joystick input commands, it uses data (command and images) loaded from a dataset (see `tests/test_data/logs1/data`) as input to the policy.
- Inference: This mode runs the policy on-line. It uses real camera images and joystick input commands as input to the policy. This mode can be toggled to Joystick mode by pressing the `A` key on the joystick.
- Joystick: This mode operates the robot via joystick command in either "Dual" (controlling left and right wheel via left and right joystick) or "Joystick" (controlling forward, backward, left, right direction via one joystick) `control_mode`. Data collection for training is conducted in Joystick mode. This mode can be toggled to Inference mode by pressing the `A` key on the joystick.

The run.py script accepts six arguments (further details, see `run.py`):
```
--policy_path: Path to policy file.
--dataset_path: Path to dataset. Only used for debug mode.
--log_path: Path to log folder, where runs are saved.
--inference_backend: Backend to use. Consider exporting all models as openvino model for maximum performance. Options: tf, tflite, openvino.
--mode: Running mode. Options: debug, inference, joystick.
--control_mode: Control mode during joystick mode. Options: dual, joystick.
```
## Generating Training Data
The script `generate_data_for_training.py` generates a log data folder that is required for training a policy via the `OpenBot/policy/openbot/train.py` script. The log data folder contains an `images` and a `sensor_data` folder in the format required by `train.py`.

See `tests/test_generate_data.py` for an example.

## OpenVino: Optimising Policy Inference Performance
To optimise the inference speed on supported Intel hardware (such as the [Up Core Plus](https://up-board.org/upcoreplus/specifications/) board), the trained model needs to be exported to OpenVino.

The `export_openvino.py` script exports a trained TensorFlow model to an OpenVino model. This OpenVino model is then loaded via `get_openvino_interpreter()` in `infer.py`.

See `tests/test_export_openvino.py` for an example.

## Tests and example code

**Note:** For testing the code, the test data and test model called `test_data` and `test_model` respectively are required to be in `OpenBot/python/tests`. The function `get_data()` in `download_data.py` provides download functionality and is called at the beginning of `test_infer.py`, `test_export_openvino.py`, and `test_generate_data.py`. Alternatively,
 please run the script `get_test_data.sh` (unix systems only) that downloads and unzips a zip file containing `test_data` and `test_models` with the data for debug mode and models for inference respectively.

Run `pytest` in the folder `tests` or run the `test_*.py` files individually to test the functionalities of

- downloading test data and test model from the cloud via `test_download_data.py`
- export to OpenVino via `test_export_openvino.py`
- generating training data via `test_generate_data.py`.
- inference in debug mode for OpenVino, Tensorflow, and Tflite via `test_infer.py`.
    - *Note*: The test data in logs1 is generated using the `associate_frames.py` script in `OpenBot.policy.openbot`, where the path to the images is hardcoded in `logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt`.
    - Thus, please replace the `path_to_openbot` with the actual path to the `OpenBot` repository in `test_infer.py`.
- joystick connection via `test_joystick.py`
- motor connection from serial port to Arduino via `test_motor.py`.
- video stream to Realsense camera via `test_realsense.py`.

# Installation
The installation process is detailed in the following.

The python implementation for controlling OpenBot requires a few Python modules for inference, joystick control, sensing, and actuation.
Further, drivers for the camera or controller might be required.

## Setup
Currently, the code is tested on:
- Board: [Up Core Plus](https://up-board.org/upcoreplus/specifications/)
- Camera: [Realsense D435i](https://www.intelrealsense.com/depth-camera-d435i/)
- Controller: [Xbox One](https://www.microsoft.com/en-gb/store/collections/xboxcontrollers?source=lp)
- Arduino: [OpenBot Firmware](https://github.com/ob-f/OpenBot/blob/master/firmware/README.md)

## Python modules

The code is tested with Python 3.9. Using Anaconda3:
```
conda create --name openbot python==3.9
```

First, install the requirements of OpenBot.policy via
```
../policy && pip install -r requirements.txt
```

Then, install the required modules via
```
pip install -r requirements.txt
```

In particular,
- `pyserial` communicates with the Arduino and thus motors via serial port
- `pyrealsense2` and `opencv-python` are required for camera image processing.
- `pygame` is used for joystick control and processing the joystick inputs
- `openvino-dev[tensorflow2,extras]` is used for boosted performance on supported Intel hardware. For further details on optimised AI inference on Intel hardware, please see [OpenVino](https://docs.openvino.ai/latest/home.html). OpenVino is the recommended inference backend. Tensorflow and Tflite are also supported (see Tests). For running PyTorch modules, please consider converting PyTorch to an OpenVino backend (see [this Tutorial](https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html)).

## Drivers
If the code is executed on Ubuntu, the Xbox One controller USB Wireless Dongle requires a driver, which can be found at [this link](https://github.com/medusalix/xone).

## Tensorflow for Inference
If TensorFlow is used for inference, please add the Python `policy` module to `PYTHONPATH` via `export PYTHONPATH=$PYTHONPATH:/path/to/OpenBot/policy`. This workaround avoids having to install openbot as module and to find `openbot.utils.load_model()`, which is required to load the tensorflow model. Further details, see `get_tf_interpreter()` in `infer.py` and the test code `tests/test_infer.py`.

## Support for non-linux distributions (MacOS, Windows)

Please note that the code is intended to run on Linux-based computers, e.g., Up Core Plus. Some python modules may not be available for MacOs or Windows.

The code can run on MacOS for debugging purposes with the following changes:
- Use `pyrealsense2-macosx` instead of `pyrealsense2` in requirements.txt
- For tflite follow [these instructions](https://github.com/milinddeore/TfLite-Standalone-build-Linux-MacOS)
