# Robot App

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## DISCLAIMERS

1. **Safety:** Always make sure you operate in a safe environment. Keep in mind, that your phone could be damaged in a collision! Special care is necessary when using automated control (e.g. person following or driving policy). Make sure you always have a game controller connected and are familiar with the key mapping, so you can stop the vehicle at any time. Use at your own risk!
2. **App under development:** The application is under development and may crash or exhibit unexpected behaviour depending on your phone model and version of the operating system. Make sure to test all functionalities with no wheels connected. Use at your own risk!

## App Screens

### Main Menu

The app starts with a menu screen that shows all available screens. The settings screen can be opened with a click on the icon at the top right corner. By clicking on the other icons the user can access various screens whose functionalities are explained in the following.

<p align="left">
<img src="../../docs/images/screen_main.jpg" alt="Main Menu" width="21.6%"/>
<img src="../../docs/images/screen_settings.jpg" alt="Settings Menu" width="20%"/>
<img src="../../docs/images/dialog_stream_mode.jpg" alt="Settings Menu" width="20%"/>
<img src="../../docs/images/dialog_connectivity_mode.jpg" alt="Settings Menu" width="20%"/>
</p>

### Settings Menu

#### USB Connection

Tap the USB icon to open the USB options. The drop-down menu is used to set the baud rate. The default is 115200, and you should not need to change this unless you mess with the Arduino firmware. The app will attempt to connect automatically, but in case you encounter issues you can use this switch to disconnect/connect.

<p align="left">
<img src="../../docs/images/usb_disconnected.jpg" alt="Connecting device" width="25%"/>
<img src="../../docs/images/usb_connected.jpg" alt="Disconnect button" width="25%"/>
</p>

#### Permissions

Here you can check the permissions of the app and adjust them if needed.

#### Video Streaming

You can choose between `WebRTC` and `RTSP` for streaming video to an external device. The phone controller app and node-js server both need this to be set to `WebRTC`. The python controller expects the stream to be set to `RTSP`.

#### Bluetooth connection

Make sure that your Android device has BLE (Bluetooth Low Energy) support. If your Android version is greater than or equal to 7.0, you also need to turn on the location service and allow the location permission in the settings in order to search for nearby BLE devices. To enable BLE, change the connectivity mode from USB to Bluetooth in the settings menu. You will get a Bluetooth icon at the top of the home screen. Tap the Bluetooth icon to start BLE scanning; it takes 4 seconds to scan and get a list of all nearby OpenBot BLE devices. Connect with your OpenBot by tapping on the `Connect` button. After successful connection the `Connect` button will change to `Disconnect`. You can now go back to the Home screen.

<p align="left">
<img src="../../docs/images/ble_devices_list.jpg" alt="BLE devices" width="25%"/>
<img src="../../docs/images/ble_device_connecting.jpg" alt="Connecting device" width="25%"/>
<img src="../../docs/images/ble_device_connected.jpg" alt="Disconnect button" width="25%"/>
</p>

### Free Roam

Free Roam offers simple robot control with real time updates and information about battery, speed and distance from surfaces.

<p align="left">
<img src="../../docs/images/screen_free_roam.jpg" alt="Free Roam" width="50%" />
</p>

- **Battery**: The battery icon shows realtime battery levels of the connected robot.
- **Drive State**: There are 3 drive states displayed on the view:
  - D -> Drive, when the robot is driving forward
  - N -> Neutral, when the robot is stationary
  - R -> Reverse, when the robot is moving backwards
  The steering wheel rotates proportionally to the steering angle.
- **Speed**: The speedometer shows the speed of the robot.
- **Sonar**: The free distance in front of the robot in cm.
- **Control**: Controller, Drive Mode and Speed are used to control robot settings as described in the [control section](#control).

### Data Collection

Simple UI for collection of data sets.

<p align="left">
<img src="../../docs/images/screen_data_collection.jpg" alt="Data Collection" width="50%" />
</p>

- **Server**: If you have the [web app](../../policy#web-app) for policy training running, you can select it here to automatically upload data.
- **Preview Resolution**: Used to switch between resolutions of camera preview. There are 3 settings:
  - ***FULL_HD*** (1920x1080p)
  - ***HD*** (1280x720p)
  - ***SD*** (640x360)
- **Model Resolution**: Used to switch between resolutions of images saved for training different models.
- **Save/Discard the Collected Data**: the data collection process can be controlled from the screen or remotely, for instance from a bluetooth controller. When using a bluetooth controller, you may:
  - Press the **A button** to **start** the data collection process
  - Press the **A button again** to **stop** data collection and save the collected data in a .zip file
  - Alternatively press the **R1 button** to **stop** data collection **without saving** the collected data (for instance because of an unexpected collision with the environment)
  - Remember to use the controller mapping fragment to ensure you are using the correct buttons.

### Controller Mapping

Simple UI to check the button and joystick mapping of a connected BT controller.

<p align="left">
<img src="../../docs/images/screen_controller_mapping.jpg" alt="Controller Mapping" width="50%" />
</p>

### Robot Info

Simple UI to get robot info and test basic functionality. The **Robot Type** as configured in the firmware is displayed as text and animation. The checkmarks in the sections **Sensors**, **Wheel Odometry** and **LEDs** show which features are supported by the connected robot. The section **Readings** provides the most important sensor measurements. In the section **Send Commands**, users can send basic motor commands by pressing the corresponding buttons and control the front and rear LEDs with a slider.

<p align="left">
<img src="../../docs/images/screen_robot_info.gif" alt="Robot Info" width="50%" />
</p>

### Autopilot

Simple UI for running autopilot models.

<p align="left">
<img src="../../docs/images/screen_autopilot.jpg" alt="Autopilot" width="50%" />
</p>

- **Server**: If you have the [web app](../../policy#web-app) for policy training running, you can select it here and send trained autopilot models to the robot.
- **Model**: Choose a trained model to use for autopilot mode.
- **Device**: Use CPU, GPU or NNAPI for inference (more details [here](#device)).
- **Threads**: Number of threads to use (only makes a difference when CPU is selected as device).
- **Control**: Controller, Drive Mode and Speed are used to control robot settings as described in the [control section](#control).

### Object Tracking

Simple UI for tracking objects of 80 different classes. A short description of the different AI models for object tracking and performance benchmarks can be found in [Model Management](#model-management).

<p align="left">
<img src="../../docs/images/screen_object_tracking_1.jpg" alt="Alt text" width="49%" />
<img src="../../docs/images/screen_object_tracking_2.jpg" alt="Alt text" width="49%" />
</p>

- **Dynamic Speed**: reduces the robot speed in "Auto Mode" if it gets closer to the tracked object.
  The speed is scaled based on the area of the bounding box (works best in landscape orientation).
- **Model**: Choose an object detector based on your phone performance (see below for [benchmarking results](#benchmark)).
- **Object**: Pick the object you want to track. The models can detect the 80 COCO [object classes](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/).
- **Confidence**: Confidence threshold to determine if detections are accepted. Increase if you get false detections, decrease if the object of interest it not detected.
- **Device**: Use CPU, GPU or NNAPI for inference (more details [here](#device)).
- **Threads**: Number of threads to use (only makes a difference when CPU is selected as device).
- **Control**: Controller, Drive Mode and Speed are used to control robot settings as described in the [control section](#control).


### Point Goal Navigation

Note that this fragment requires ARCore and camera permission. If your device does not support ARCore and you continue anyway, the app will crash. In this screen you can specify a goal via a 2D vector with respect to the current position and orientation of the robot. The 2D vector contains the distance to the front and left of the robot in meters. Both values can also be negative and correspond to back and right of the robot in that case. After specifying the goal and pressing `Start` the robot will exectue an AI policy that attempts to reach the goal while avoiding obstacles.

<p align="left">
<img src="../../docs/images/screen_point_goal_nav.gif" alt="Alt text" width="50%" />
</p>

### Model Management

All models are quantized for better performance on embedded devices. Please refer to section below for a short description of the available models and benchmarking results. The [mean Average Precision (mAP)](https://kharshit.github.io/blog/2019/09/20/evaluation-metrics-for-object-detection-and-segmentation) is computed on the validation set of the [COCO Detection 2017](https://cocodataset.org/#detection-2017) dataset. Each model is run for about 1 minute; the inference time is averaged across the last 100 frames and reported in frames per second (fps). Note that models with larger input resolution might be better for smaller objects despite lower mAP.

<p align="left">
<img src="../../docs/images/screen_model_management.gif" alt="Model Management" width="25%" />
</p>

### Benchmark

#### Phones

| Model Name       | Chipset        | RAM  | OS |
|------------------|----------------|------|----|
| Samsung S22 Ultra| Exynos 2200    | 12GB | 12 |
| Samsung S20FE 5G | Snapdragon 865 |  6GB | 12 |
| Huawei P30 Pro   | Kirin 980      |  8GB | 10 |
| Google Pixel 6XL | Google Tensor  | 12GB | 12 |
| Xiaomi Mi9       | Snapdragon 855 |  6GB | 10 |
| Google Pixel 4XL | Snapdragon 855 |  6GB | 13 |

#### MobileNetV1-300 (pre-installed) - mAP: 18%

SSD object detector with [MobileNet V1](https://tfhub.dev/tensorflow/lite-model/ssd_mobilenet_v1/1/metadata/2) backbone and input resolution of 300x300.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  33 |  13 |   30  |
| Samsung S20FE 5G |  34 |  57 |   87  |
| Huawei P30 Pro   |  36 |  25 |   10  |
| Google Pixel 6XL |  35 |  42 |   53  |
| Xiaomi Mi9       |  22 |  41 |   33  |
| Google Pixel 4XL |  37 |  36 |   45  |

#### MobileNetV3-320 - mAP: 16%

SSD object detector with MobileNet V3 backbone and input resolution of 320x320.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  30 |  17 |   30  |
| Samsung S20FE 5G |  34 |  42 |   28  |
| Huawei P30 Pro   |  32 |  27 |   23  |
| Google Pixel 6XL |  33 |  43 |   27  |
| Xiaomi Mi9       |  20 |  45 |   10  |
| Google Pixel 4XL |  32 |  38 |   21  |

#### YoloV4-tiny-224 - mAP: 22%

Tiny version of [YoloV4](https://arxiv.org/abs/2004.10934) with input resolution of 224x224.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  31 |  12 |   31  |
| Samsung S20FE 5G |  30 |  21 |   14  |
| Huawei P30 Pro   |  27 |  17 |   22  |
| Google Pixel 6XL |  29 |  24 |   19  |
| Xiaomi Mi9       |  16 |  14 |  9.3  |
| Google Pixel 4XL |  22 |  19 |   14  |

#### YoloV4-tiny-416 - mAP: 29%

Tiny version of [YoloV4](https://arxiv.org/abs/2004.10934) with input resolution of 416x416.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  13 | 9.8 |   13  |
| Samsung S20FE 5G |  12 | 9.4 |  7.7  |
| Huawei P30 Pro   | 8.4 | 7.6 |  6.9  |
| Google Pixel 6XL |  10 | 9.6 |  7.2  |
| Xiaomi Mi9       | 9.0 | 7.3 |  5.0  |
| Google Pixel 4XL | 7.2 | 7.4 |  6.2  |

#### YoloV4-224 - mAP: 40%

[YoloV4](https://arxiv.org/abs/2004.10934) with input resolution of 224x224.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra| 3.7 | 5.6 |  3.5  |
| Samsung S20FE 5G | 3.1 | 7.1 |  4.2  |
| Huawei P30 Pro   | 2.4 | 6.2 |  0.7  |
| Google Pixel 6XL | 2.7 |  11 |  0.9  |
| Xiaomi Mi9       | 2.1 | 6.4 |  1.7  |
| Google Pixel 4XL | 1.8 | 5.0 |  3.7  |

#### YoloV5s-320 - mAP: 28%

[YoloV5](https://github.com/ultralytics/yolov5) with input resolution of 320x320.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  21 |  10 |   21  |
| Xiaomi Mi9       |  13 |  15 |  0.8  |
| Google Pixel 4XL |  12 |  17 |   18  |

#### YoloV5s-640 - mAP: 34%

[YoloV5](https://github.com/ultralytics/yolov5) with input resolution of 640x640.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra| 5.5 | 4.9 |  5.0  |
| Xiaomi Mi9       | 4.1 | 4.6 |   -   |
| Google Pixel 4XL | 3.7 | 4.6 |  4.6  |

#### YoloV5m-320 - mAP: 35%

[YoloV5](https://github.com/ultralytics/yolov5) with input resolution of 320x320.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  13 | 8.2 |   11  |
| Xiaomi Mi9       | 9.7 | 9.9 |   -   |
| Google Pixel 4XL | 7.9 | 9.2 |   15  |

#### YoloV5l-320 - mAP: 38%

[YoloV5](https://github.com/ultralytics/yolov5) with input resolution of 320x320.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra| 7.6 | 3.4 |  7.6  |
| Xiaomi Mi9       | 5.5 | 5.0 |   -   |
| Google Pixel 4XL | 5.3 | 4.0 |  5.3  |

#### EfficientDet-L0-320 - mAP: 26%

[EfficientDet-L0](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite0/detection/metadata/1) with input resolution of 320x320. Note: Model performance deteriorates in landscape mode; the confidence threshold might need to be adjusted.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  18 |  10 |   16  |
| Xiaomi Mi9       |  16 |  20 |  1.2  |
| Google Pixel 4XL |  17 |  17 |   16  |

#### EfficientDet-L1-384 - mAP: 31%

[EfficientDet-L1](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite1/detection/metadata/1) with input resolution of 384x384. Note: Model performance deteriorates in landscape mode; the confidence threshold might need to be adjusted.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  12 | 9.2 |   10  |
| Xiaomi Mi9       |  10 |  13 |    -  |
| Google Pixel 4XL |  11 |  11 |   10  |

#### EfficientDet-L2-448 - mAP: 34%

[EfficientDet-L2](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite2/detection/metadata/1) with input resolution of 448x448. Note: Model performance deteriorates in landscape mode; the confidence threshold might need to be adjusted.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra| 9.8 | 8.4 |  8.2  |
| Xiaomi Mi9       | 6.4 | 9.4 |   -   |
| Google Pixel 4XL | 7.7 | 8.3 |  7.6  |

### Default

The [DefaultActivity](src/main/java/org/openbot/original/DefaultActivity.java) includes the most important features of the OpenBot app in a single screen. It displays the connection status to the vehicle and reports measurements from vehicle sensors. The robot can be controlled by standard BT game controllers or another smartphone running the OpenBot [controller app](../controller). We have also implemented a data logger to collect datasets with the robot. Currently, we record readings from following sensors: camera, gyroscope, accelerometer, magnetometer, ambient light sensor, and barometer. Using the Android API, we are able to obtain the following sensor readings: RGB images, angular speed, linear acceleration, gravity, magnetic field strength, light intensity, atmospheric pressure, latitude, longitude, altitude, bearing, and speed. In addition to the phone sensors, we record body sensor readings (wheel odometry, obstacle distance and battery voltage), which are transmitted via the serial link. We also record and timestamp control signals received from a connected controller, if present. Lastly, we integrate several neural networks for person following and autonomous navigation.

<p align="left">
  <img src="../../docs/images/screen_default.jpg" alt="App GUI" width="50%"/>
</p>

#### USB Connection

Same as in the [settings menu](#settings-menu).

#### Vehicle Status

The field **Battery** displays the battery voltage as measured by the Arduino via the voltage divider. The field **Speed (l,r)** reports the left and right speed of the (front) wheels in rpm. It is measured by the Arduino via the optical wheel speed sensors. The field **Sonar** shows the free space in front of the car in centimeters. It is measured by the Arduino via the ultrasonic sensor. Note, you will only receive values a few seconds after the USB connections has been established.

#### Control

The first button is for selecting the **control mode**. There are two different control modes:

- **Gamepad**: The app receives controls from a connected BT controller.
- **Phone**:  The robot can be controlled via another smartphone with the controller app installed or though a Python script running on a computer connected to the same network.

The second button is for selecting the **drive mode**. There are three different drive modes when using a game controller (e.g. PS4):

- **Game**: Use the right and left shoulder triggers (R2, L2) for forward and reverse throttle and either joystick for steering. This mode imitates the control mode of car racing video games.
- **Joystick**: Use either one of the joysticks to control the robot.
- **Dual**: Use the left and right joystick to control the left and right side of the car. This is raw differential steering.

The third button is for selecting the **speed mode**. There are three different speed modes:

- **Slow**: The voltage applied to the motors is limited to 50% of the input voltage (~6V).
- **Normal**: The voltage applied to the motors is limited to 75% of the input voltage (~9V).
- **Fast**: There is no limit. The full input voltage will be applied to the motors at full throttle (~12V). *This is the default setting for running the neural networks.*

Running at higher speeds will reduce the lifetime of the motors but is more fun. The controls that are sent to the robot are displayed on the right side. When using the game controller, the speed mode can be increased by pressing down the right joystick (R3) and decrased by pressing down the left joystick (L3).

#### Data Log

There are four different logging modes:

- **only_sensors**: All sensor data but no images are saved.
- **crop_img**: All sensor data and a cropped images that have the input size of the network are saved. This is the default setting and is what should be used for data collection.
- **preview_img**: All sensor data and a full-size images are saved. This will require a lot of memory and can be slow. However, it is nice for compiling FPV videos.
- **all_imgs**: All sensor data and both cropped and full-size images are saved. This will require a lot of memory and can be slow.

The switch on the right is used to toggle logging on and off. On the game controller this switch can be toggled with the X button.

#### Camera

The first item shows the preview resolution. The second item shows the crop resolution. This is the image that is used as input to the neural networks. You will notice that this resolution changes depending on which model you select below. If you train your own autopilot, make sure to select the `AUTOPILOT_F` model. The crop resolution should show `256x96`. The switch on the right is used to toggle between the rear and the front camera.

#### Model

There are two models that come with the app:

- **MobileNetV1-300**: This model is used for person following. It uses a SSD object detector with MobileNet V1 backbone. The model is quantized for better performance on embedded devices. It comes with the app.
- **CIL-Mobile**: This model is used for autonomous navigation. It will predict controls directly from the camera input. Chances are that it will not work in your environment. You should follow our instructions to train your own [Driving Policy](../../policy) and replace it.

Additional models can be downloaded from the Model Management screen.

The switch on the right is used to turn the network on and off. When the network is running, it produces the controls for the robot and the game controller is disabled. However, you may still use the buttons on the game controller, for example to toggle this switch with the R1 trigger button to regain control of the robot.

#### Device

Use the drop-down menu to select the device on which the neural network should be executed. You have the following choices:

- **CPU**: Using the CPU works on most phones and is the default choice. You can adjust the number of threads to optimize performance.
- **GPU**: Most smartphones have a GPU. Networks with large inputs such as images often run faster on a GPU.
- **NNAPI**: This will use the [TensorFlow Lite NNAPI delegate](https://www.tensorflow.org/lite/performance/nnapi). Modern smartphones often come with dedicated AI accelerators. The [Neural Network API](https://developer.android.com/ndk/guides/neuralnetworks) (NNAPI) provides acceleration for TensorFlow Lite models on Android devices with Graphics Processing Unit (GPU), Digital Signal Processor (DSP) and Neural Processing Unit (NPU). Note that on some older phones this can be very slow!

If a model is active, the inference speed in [ms] will be displayed next to the device which is running the model.

### Projects Screen

The Projects Screen displays a list of your OpenBot Playground projects if you are signed in with your Google account. You can execute these projects to connect with your OpenBot, or scan their QR codes by clicking the scanner icon in the top right corner. If you are not signed in, the screen will display a Google Sign-In button, but you can still scan your project's QR code without signing in. If you get the message `Oops, no project found` on the screen after signing in, make sure that the account has projects stored on Google Drive.

If you don't see your latest projects in the project list, you can reload them by pulling down on the project screen.
<p align="left">
<img src="../../docs/images/projects_tab_screen.gif" alt="Project Screen" width="24.7%"/>
<img src="../../docs/images/no_projects_found.jpg" alt="No project screen" width="25%"/>
<img src="../../docs/images/reload_projects.gif" altq="Reload project screen" width="24.5%"/>
</p>

- **Google Drive projects**: To run a Google Drive project, tap on the project you want to execute and wait for the contents of the project file to be read. If the file is successfully retrieved without any errors, a pop-up will appear with two buttons: `Start` and `Cancel`. The pop-up will also display the name of the project you are about to run. To execute the project, click on the Start button. If you want to stop the activity, click on the Cancel button. If you receive a pop-up message stating `Something went wrong`, there may be an error with the Google Drive file. To resolve this issue, refresh the project screen by pulling down and then repeating the same process.


- **Qr code scanner**: To scan the QR code of a Playground project, click on the QR code icon located in the top right corner of the screen. Grant camera access to the app so that it can scan the QR code. Once the code is scanned, wait for the contents of the file to be read. If the file is retrieved successfully without any errors, a pop-up will appear with two buttons: `Start` and `Cancel`. The pop-up will also display the name of the project you are about to run. To execute the project, click on the Start button. If you want to stop the activity, click on the Cancel button. If you receive a pop-up message stating `Something went wrong`, there may be an error with the Google Drive file. To resolve this issue, generate a new QR code in Playground and repeat the process.


- **Executing Project**: If your OpenBot Playground project runs successfully, the screen will display the names of code blocks along with a stop button that can be used to stop the execution of playground block commands.


- **Delete Project**: To delete a project, long-press on the project you wish to delete. This will bring up a popup screen asking to confirm the deletion. Tap on 'Yes' to delete the project.

<p align="left">
<img src="../../docs/images/android_google_drive_projects_execute.gif" alt="Google Drive project execute" width="25%"/>
<img src="../../docs/images/android_qr_code_scanning.gif" alt="Qr code scanner project execute" width="25%"/>
<img src="../../docs/images/android_delete_project.jpg" alt="Delete Project" width="24.7%"/>
</p>

### Profile Screen
The Profile Screen in the app provides different options based on whether the user is signed in or not.
If the user is not signed in, a `Google Sign-in` button will appear, prompting the user to sign in their Google account. Once signed in, the user will be able to access their profile and other features.
If the user is signed in, two buttons will be listed in the  `Profile` tab: `Edit Profile` and `Logout`.

<p align="left">
<img src="../../docs/images/android_logged_out_profile_screen.jpg" alt="Logged out profile screen" width="25%"/>
<img src="../../docs/images/android_logged_in_profile_screen.jpg" alt="Logged in profile screen" width="24.9%"/>
</p>

- **Edit Profile**: Tapping on this button will open a new screen where the user can update their profile information, such as their name and profile picture.


- **Logout**: This button allows the user to log out of their account. Tapping on this button will log the user out and return them to the login screen.

  <p align="left">
  <img src="../../docs/images/android_edit_profile.jpg" alt="Edit profile screen" width="25%"/>
  <img src="../../docs/images/android_logout_dialog_box.jpg" alt="Logout dialog box" width="24.4%"/>
  </p>

### OpenBot PlayGround Screen

To access OpenBot Playground services, click on the OpenBot Playground icon located at the top of the screen in the toolbar options. If you want to learn more about OpenBot Playground, [click here](../../open-code/README.md).

<p align="left">
<img src="../../docs/images/android_playground_sign-in.gif" alt="openBot playground Sign-in" width="25%"/>
<img src="../../docs/images/android_playground_services.gif" alt="openBot playground Services" width="25%"/>
</p>

## Add your own fragment

Please refer to the [ContributionGuide](ContributionGuide.md) to learn how to add your own fragments to the OpenBot app.

## Code Structure

The [TensorFlow Lite Object Detection Android Demo](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android) was used as starting point to integrate TFLite models and obtain the camera feed. The [DefaultActivity](src/main/java/org/openbot/robot/DefaultActivity.java) runs the main thread and inherits from the [CameraActivity](src/main/java/org/openbot/robot/CameraActivity.java) to manage the camera and UI. The [SensorService](src/main/java/org/openbot/robot/SensorService.java) reads all other phone sensors and logs them. The [ServerService](src/main/java/org/openbot/robot/ServerService.java) and [NsdService](src/main/java/org/openbot/robot/NsdService.java) establish a connection to a local [Python server](../../policy/README.md#web-app) with a React frontend. If you collect data it can be uploaded automatically for visualization, training ML models and downloading trained models to the robot. The [env](src/main/java/org/openbot/env) folder contains utility classes such as the [Vehicle](src/main/java/org/openbot/env/Vehicle.java) interface, [GameController](src/main/java/org/openbot/env/GameController.java) interface, [PhoneController](src/main/java/org/openbot/env/PhoneController.java) interface and an [AudioPlayer](src/main/java/org/openbot/env/AudioPlayer.java) for the audible feedback. The [tflite](src/main/java/org/openbot/tflite) folder contains the model definitions for the [Autopilot](src/main/java/org/openbot/tflite/Autopilot.java) and [Detector](src/main/java/org/openbot/tflite/Detector.java) networks.

## Next (optional)

Train your own [Driving Policy](../../policy/README.md)
