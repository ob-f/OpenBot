# Robot App

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## 免责声明

1. **安全性:** 请确保您在安全的环境中操作。请记住，您的手机可能会在碰撞中受损！在使用自动控制（例如跟随人或驾驶策略）时需要特别小心。确保您始终连接有游戏控制器，并熟悉按键映射，以便随时停止车辆。使用风险自负！
2. **应用程序开发中:** 该应用程序正在开发中，可能会因您的手机型号和操作系统版本而崩溃或表现出意外行为。请确保在没有连接车轮的情况下测试所有功能。使用风险自负！

## 应用程序界面

### 主菜单

应用程序启动时会显示一个菜单屏幕，列出所有可用的屏幕。可以通过点击右上角的图标打开设置屏幕。点击其他图标，用户可以访问各种屏幕，其功能在下文中解释。

<p align="left">
<img style="padding-right: 2%;" src="../../docs/images/screen_main.png" alt="主菜单" width="24.5%"/>
<img src="../../docs/images/screen_settings.png" alt="设置菜单" width="24.5%"/>
<img src="../../docs/images/dialog_stream_mode.png" alt="设置菜单" width="24.5%"/>
<img src="../../docs/images/dialog_connectivity_mode.png" alt="设置菜单" width="24.5%"/>
</p>

### 设置菜单

#### USB连接

点击USB图标打开USB选项。下拉菜单用于设置波特率。默认值为115200，除非您更改了Arduino固件，否则无需更改。应用程序会尝试自动连接，但如果遇到问题，可以使用此开关断开/连接。

<p align="left">
<img src="../../docs/images/usb_disconnected.jpg" alt="连接设备" width="25%"/>
<img src="../../docs/images/usb_connected.jpg" alt="断开按钮" width="25%"/>
</p>

#### 权限

在这里您可以检查应用程序的权限，并根据需要进行调整。

#### 视频流

您可以选择 `WebRTC` 或 `RTSP` 来将视频流传输到外部设备。手机控制器应用程序和node-js服务器都需要将其设置为 `WebRTC`。Python控制器则期望将流设置为 `RTSP`。

#### 蓝牙连接

确保您的Android设备支持BLE（低功耗蓝牙）。如果您的Android版本大于或等于7.0，还需要在设置中打开位置服务并允许位置权限，以便搜索附近的BLE设备。要启用BLE，请在设置菜单中将连接模式从USB更改为蓝牙。您将在主屏幕顶部看到一个蓝牙图标。点击蓝牙图标开始BLE扫描；扫描需要4秒钟，并获取所有附近OpenBot BLE设备的列表。点击 `连接` 按钮与您的OpenBot连接。成功连接后，`连接` 按钮将变为 `断开`。您现在可以返回主屏幕。

<p align="left">
<img src="../../docs/images/ble_devices_list.jpg" alt="BLE设备" width="25%"/>
<img src="../../docs/images/ble_device_connecting.jpg" alt="连接设备" width="25%"/>
<img src="../../docs/images/ble_device_connected.jpg" alt="断开按钮" width="25%"/>
</p>

### 自由漫游

自由漫游提供简单的机器人控制，并实时更新电池、电量和与表面的距离信息。

<p align="left">
<img src="../../docs/images/screen_free_roam.jpg" alt="自由漫游" width="50%" />
</p>

- **电池**: 电池图标显示连接机器人的实时电池电量。
- **驾驶状态**: 视图上显示了3种驾驶状态：
  - D -> 驾驶，机器人向前行驶时
  - N -> 空档，机器人静止时
  - R -> 倒车，机器人向后移动时
  方向盘会根据转向角度比例旋转。
- **速度**: 速度表显示机器人的速度。
- **声纳**: 机器人前方的自由距离，以厘米为单位。
- **控制**: 控制器、驾驶模式和速度用于控制机器人设置，如[控制部分](#control)所述。

### 数据收集

用于收集数据集的简单UI。

<p align="left">
<img src="../../docs/images/screen_data_collection.jpg" alt="数据收集" width="50%" />
</p>

- **服务器**: 如果您正在运行用于策略训练的[web应用程序](../../policy#web-app)，可以在此选择它以自动上传数据。
- **预览分辨率**: 用于切换相机预览的分辨率。有3种设置：
  - ***FULL_HD*** (1920x1080p)
  - ***HD*** (1280x720p)
  - ***SD*** (640x360)
- **模型分辨率**: 用于切换保存用于训练不同模型的图像分辨率。
- **保存/丢弃收集的数据**: 数据收集过程可以从屏幕或远程控制，例如通过蓝牙控制器。当使用蓝牙控制器时，您可以：
  - 按 **A按钮** **开始** 数据收集过程
  - 再次按 **A按钮** **停止** 数据收集并将收集的数据保存为.zip文件
  - 或者按 **R1按钮** **停止** 数据收集 **不保存** 收集的数据（例如由于意外碰撞）
  - 请记住使用控制器映射片段以确保您使用正确的按钮。

### 控制器映射

用于检查连接的蓝牙控制器的按钮和摇杆映射的简单UI。

<p align="left">
<img src="../../docs/images/screen_controller_mapping.jpg" alt="控制器映射" width="50%" />
</p>

### 机器人信息

用于获取机器人信息和测试基本功能的简单UI。固件中配置的 **机器人类型** 以文本和动画形式显示。**传感器**、**轮子里程计** 和 **LED** 部分的复选标记显示连接的机器人支持哪些功能。**读数** 部分提供最重要的传感器测量值。在 **发送命令** 部分，用户可以通过按相应的按钮发送基本的电机命令，并通过滑块控制前后LED。

<p align="left">
<img src="../../docs/images/screen_robot_info.gif" alt="机器人信息" width="50%" />
</p>

### 自动驾驶

用于运行自动驾驶模型的简单UI。

<p align="left">
<img src="../../docs/images/screen_autopilot.jpg" alt="自动驾驶" width="50%" />
</p>

- **服务器**: 如果您正在运行用于策略训练的[web应用程序](../../policy#web-app)，可以在此选择它并将训练好的自动驾驶模型发送到机器人。
- **模型**: 选择一个训练好的模型用于自动驾驶模式。
- **设备**: 使用CPU、GPU或NNAPI进行推理（更多详情见[这里](#device)）。
- **线程**: 使用的线程数（仅在选择CPU作为设备时有所不同）。
- **控制**: 控制器、驾驶模式和速度用于控制机器人设置，如[控制部分](#control)所述。

### 物体跟踪

用于跟踪80种不同类别物体的简单UI。关于物体跟踪的不同AI模型和性能基准的简短描述可以在[模型管理](#model-management)中找到。

<p align="left">
<img src="../../docs/images/screen_object_tracking_1.jpg" alt="物体跟踪" width="49%" />
<img src="../../docs/images/screen_object_tracking_2.jpg" alt="物体跟踪" width="49%" />
</p>

- **动态速度**: 在“自动模式”下，如果机器人接近被跟踪物体，会减少速度。速度根据边界框的面积进行缩放（在横向模式下效果最佳）。
- **模型**: 根据您的手机性能选择一个物体检测器（见下文[基准测试结果](#benchmark)）。
- **物体**: 选择您要跟踪的物体。模型可以检测80种COCO[物体类别](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/)。
- **置信度**: 确定是否接受检测结果的置信度阈值。如果出现误检，请增加；如果未检测到感兴趣的物体，请减少。
- **设备**: 使用CPU、GPU或NNAPI进行推理（更多详情见[这里](#device)）。
- **线程**: 使用的线程数（仅在选择CPU作为设备时有所不同）。
- **控制**: 控制器、驾驶模式和速度用于控制机器人设置，如[控制部分](#control)所述。

### 目标导航

请注意，此片段需要ARCore和相机权限。如果您的设备不支持ARCore并继续操作，应用程序将崩溃。在此屏幕中，您可以根据机器人的当前位置和方向指定一个目标。2D向量包含到机器人前方和左侧的距离（以米为单位）。这两个值也可以为负数，对应于机器人的后方和右侧。指定目标并按下 `开始` 后，机器人将执行一个AI策略，尝试在避开障碍物的同时到达目标。

<p align="left">
<img src="../../docs/images/screen_point_goal_nav.gif" alt="目标导航" width="50%" />
</p>

### 模型管理

所有模型都经过量化，以在嵌入式设备上获得更好的性能。请参阅下文，了解可用模型和基准测试结果的简短描述。[平均精度（mAP）](https://kharshit.github.io/blog/2019/09/20/evaluation-metrics-for-object-detection-and-segmentation) 在[COCO检测2017](https://cocodataset.org/#detection-2017)数据集的验证集上计算。每个模型运行约1分钟；推理时间在最后100帧中平均，并以每秒帧数（fps）报告。请注意，尽管mAP较低，但输入分辨率较大的模型可能对较小的物体更好。

<p align="left">
<img src="../../docs/images/screen_model_management.gif" alt="模型管理" width="25%" />
</p>

### 基准测试

#### 手机

| 型号名称         | 芯片组          | 内存 | 操作系统 |
|------------------|----------------|------|---------|
| 三星 S22 Ultra   | Exynos 2200    | 12GB | 12      |
| 三星 S20FE 5G    | Snapdragon 865 |  6GB | 12      |
| 华为 P30 Pro     | Kirin 980      |  8GB | 10      |
| 谷歌 Pixel 6XL   | Google Tensor  | 12GB | 12      |
| 小米 Mi9         | Snapdragon 855 |  6GB | 10      |
| 谷歌 Pixel 4XL   | Snapdragon 855 |  6GB | 13      |

#### MobileNetV1-300（预装） - mAP: 18%

具有[MobileNet V1](https://tfhub.dev/tensorflow/lite-model/ssd_mobilenet_v1/1/metadata/2)主干和300x300输入分辨率的SSD物体检测器。

|手机/设备 (fps)   | CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| 三星 S22 Ultra   |  33 |  13 |   30  |
| 三星 S20FE 5G    |  34 |  57 |   87  |
| 华为 P30 Pro     |  36 |  25 |   10  |
| 谷歌 Pixel 6XL   |  35 |  42 |   53  |
| 小米 Mi9         |  22 |  41 |   33  |
| 谷歌 Pixel 4XL   |  37 |  36 |   45  |

#### MobileNetV3-320 - mAP: 16%

具有MobileNet V3主干和320x320输入分辨率的SSD物体检测器。

|手机/设备 (fps)   | CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| 三星 S22 Ultra   |  30 |  17 |   30  |
| 三星 S20FE 5G    |  34 |  42 |   28  |
| 华为 P30 Pro     |  32 |  27 |   23  |
| 谷歌 Pixel 6XL   |  33 |  43 |   27  |
| 小米 Mi9         |  20 |  45 |   10  |
| 谷歌 Pixel 4XL   |  32 |  38 |   21  |

#### YoloV4-tiny-224 - mAP: 22%

[YoloV4](https://arxiv.org/abs/2004.10934)的精简版，输入分辨率为224x224。

|手机/设备 (fps)   | CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| 三星 S22 Ultra   |  31 |  12 |   31  |
| 三星 S20FE 5G    |  30 |  21 |   14  |
| 华为 P30 Pro     |  27 |  17 |   22  |
| 谷歌 Pixel 6XL   |  29 |  24 |   19  |
| 小米 Mi9         |  16 |  14 |  9.3  |
| 谷歌 Pixel 4XL   |  22 |  19 |   14  |

#### YoloV4-tiny-416 - mAP: 29%

[YoloV4](https://arxiv.org/abs/2004.10934)的精简版，输入分辨率为416x416。

|手机/设备 (fps)   | CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| 三星 S22 Ultra   |  13 | 9.8 |   13  |
| 三星 S20FE 5G    |  12 | 9.4 |  7.7  |
| 华为 P30 Pro     | 8.4 | 7.6 |  6.9  |
| 谷歌 Pixel 6XL   |  10 | 9.6 |  7.2  |
| 小米 Mi9         | 9.0 | 7.3 |  5.0  |
| 谷歌 Pixel 4XL   | 7.2 | 7.4 |  6.2  |

#### YoloV4-224 - mAP: 40%

[YoloV4](https://arxiv.org/abs/2004.10934)输入分辨率为224x224。

|手机/设备 (fps)   | CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| 三星 S22 Ultra   | 3.7 | 5.6 |  3.5  |
| 三星 S20FE 5G    | 3.1 | 7.1 |  4.2  |
| 华为 P30 Pro     | 2.4 | 6.2 |  0.7  |
| 谷歌 Pixel 6XL   | 2.7 |  11 |  0.9  |
| 小米 Mi9         | 2.1 | 6.4 |  1.7  |
| 谷歌 Pixel 4XL   | 1.8 | 5.0 |  3.7  |

#### YoloV5s-320 - mAP: 28%

[YoloV5](https://github.com/ultralytics/yolov5)输入分辨率为320x320。

|手机/设备 (fps)   | CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| 三星 S22 Ultra   |  21 |  10 |   21  |
| 小米 Mi9         |  13 |  15 |  0.8  |
| 谷歌 Pixel 4XL   |  12 |  17 |   18  |

#### YoloV5s-640 - mAP: 34%

[YoloV5](https://github.com/ultralytics/yolov5)输入分辨率为640x640。

|手机/设备 (fps)| CPU | GPU | NNAPI |
|--------------|-----|-----|-------|
| Samsung S22 Ultra| 5.5 | 4.9 |  5.0  |
| Xiaomi Mi9       | 4.1 | 4.6 |   -   |
| Google Pixel 4XL | 3.7 | 4.6 |  4.6  |

#### YoloV5m-320 - mAP: 35%

[YoloV5](https://github.com/ultralytics/yolov5) 输入分辨率为320x320。

|手机/设备 (fps)| CPU | GPU | NNAPI |
|--------------|-----|-----|-------|
| Samsung S22 Ultra|  13 | 8.2 |   11  |
| Xiaomi Mi9       | 9.7 | 9.9 |   -   |
| Google Pixel 4XL | 7.9 | 9.2 |   15  |

#### YoloV5l-320 - mAP: 38%

[YoloV5](https://github.com/ultralytics/yolov5) 输入分辨率为320x320。

|手机/设备 (fps)| CPU | GPU | NNAPI |
|--------------|-----|-----|-------|
| Samsung S22 Ultra| 7.6 | 3.4 |  7.6  |
| Xiaomi Mi9       | 5.5 | 5.0 |   -   |
| Google Pixel 4XL | 5.3 | 4.0 |  5.3  |

#### EfficientDet-L0-320 - mAP: 26%

[EfficientDet-L0](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite0/detection/metadata/1) 输入分辨率为320x320。注意：模型在横屏模式下性能会下降，可能需要调整置信度阈值。

|手机/设备 (fps)| CPU | GPU | NNAPI |
|--------------|-----|-----|-------|
| Samsung S22 Ultra|  18 |  10 |   16  |
| Xiaomi Mi9       |  16 |  20 |  1.2  |
| Google Pixel 4XL |  17 |  17 |   16  |

#### EfficientDet-L1-384 - mAP: 31%

[EfficientDet-L1](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite1/detection/metadata/1) 输入分辨率为384x384。注意：模型在横屏模式下性能会下降，可能需要调整置信度阈值。

|手机/设备 (fps)| CPU | GPU | NNAPI |
|--------------|-----|-----|-------|
| Samsung S22 Ultra|  12 | 9.2 |   10  |
| Xiaomi Mi9       |  10 |  13 |    -  |
| Google Pixel 4XL |  11 |  11 |   10  |

#### EfficientDet-L2-448 - mAP: 34%

[EfficientDet-L2](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite2/detection/metadata/1) 输入分辨率为448x448。注意：模型在横屏模式下性能会下降，可能需要调整置信度阈值。

|手机/设备 (fps)| CPU | GPU | NNAPI |
|--------------|-----|-----|-------|
| Samsung S22 Ultra| 9.8 | 8.4 |  8.2  |
| Xiaomi Mi9       | 6.4 | 9.4 |   -   |
| Google Pixel 4XL | 7.7 | 8.3 |  7.6  |

### 默认

[DefaultActivity](src/main/java/org/openbot/original/DefaultActivity.java) 包含了OpenBot应用的所有重要功能，集成在一个屏幕上。它显示与车辆的连接状态，并报告来自车辆传感器的测量值。机器人可以通过标准的蓝牙游戏控制器或运行OpenBot [控制器应用](../controller)的另一部智能手机进行控制。我们还实现了一个数据记录器，用于收集机器人的数据集。目前，我们记录以下传感器的读数：相机、陀螺仪、加速度计、磁力计、环境光传感器和气压计。通过Android API，我们能够获得以下传感器读数：RGB图像、角速度、线性加速度、重力、磁场强度、光强度、大气压力、纬度、经度、高度、方位和速度。除了手机传感器外，我们还记录通过串行链接传输的车身传感器读数（车轮里程计、障碍物距离和电池电压）。如果连接了控制器，我们还会记录和时间戳控制信号。最后，我们集成了几种用于跟随人和自主导航的神经网络。

<p align="left">
  <img src="../../docs/images/screen_default.jpg" alt="App GUI" width="50%"/>
</p>

#### USB连接

与[设置菜单](#settings-menu)相同。

#### 车辆状态

**电池**字段显示Arduino通过分压器测量的电池电压。**速度 (l,r)**字段报告（前）轮的左、右速度（单位：rpm）。它由Arduino通过光学车轮速度传感器测量。**声纳**字段显示车前的空闲空间（单位：厘米）。它由Arduino通过超声波传感器测量。注意，只有在USB连接建立几秒钟后，您才会收到数值。

#### 控制

第一个按钮用于选择**控制模式**。有两种不同的控制模式：

- **游戏手柄**：应用程序接收来自连接的蓝牙控制器的控制信号。
- **手机**：机器人可以通过安装了控制器应用的另一部智能手机或通过连接到同一网络的计算机上运行的Python脚本进行控制。

第二个按钮用于选择**驾驶模式**。使用游戏控制器（例如PS4）时，有三种不同的驾驶模式：

- **游戏**：使用右肩和左肩触发器（R2，L2）进行前进和倒车油门，并使用任意一个摇杆进行转向。此模式模仿赛车视频游戏的控制模式。
- **摇杆**：使用任意一个摇杆控制机器人。
- **双摇杆**：使用左、右摇杆分别控制车的左、右侧。这是原始的差速转向。

第三个按钮用于选择**速度模式**。有三种不同的速度模式：

- **慢速**：施加到电机的电压限制为输入电压的50%（约6V）。
- **正常**：施加到电机的电压限制为输入电压的75%（约9V）。
- **快速**：没有限制。全油门时将施加输入电压的全部电压（约12V）。*这是运行神经网络的默认设置。*

以更高的速度运行会减少电机的寿命，但更有趣。发送到机器人的控制信号显示在右侧。使用游戏控制器时，可以通过按下右摇杆（R3）增加速度模式，通过按下左摇杆（L3）减少速度模式。

#### 数据记录

有四种不同的记录模式：

- **only_sensors**：保存所有传感器数据，但不保存图像。
- **crop_img**：保存所有传感器数据和裁剪后的图像，图像的输入尺寸与网络一致。这是默认设置，应该用于数据收集。
- **preview_img**：保存所有传感器数据和全尺寸图像。这将需要大量内存并且可能会很慢。然而，它非常适合编译FPV视频。
- **all_imgs**：保存所有传感器数据以及裁剪后的和全尺寸图像。这将需要大量内存并且可能会很慢。

右侧的开关用于切换记录开关。在游戏控制器上，可以通过X按钮切换此开关。

#### 相机

第一个项目显示预览分辨率。第二个项目显示裁剪分辨率。这是用作神经网络输入的图像。您会注意到，这个分辨率会根据您选择的模型而变化。如果您训练自己的自动驾驶仪，请确保选择`AUTOPILOT_F`模型。裁剪分辨率应显示为`256x96`。右侧的开关用于在后置和前置相机之间切换。

#### 模型

应用程序附带两个模型：

- **MobileNetV1-300**：此模型用于跟随人。它使用带有MobileNet V1骨干的SSD对象检测器。该模型经过量化以在嵌入式设备上获得更好的性能。它随应用程序一起提供。
- **CIL-Mobile**：此模型用于自主导航。它将直接从相机输入预测控制。它可能在您的环境中不起作用。您应该按照我们的说明训练自己的[驾驶策略](../../policy)并替换它。

可以从模型管理屏幕下载其他模型。

右侧的开关用于打开和关闭网络。当网络运行时，它会为机器人生成控制信号，并禁用游戏控制器。然而，您仍然可以使用游戏控制器上的按钮，例如使用R1触发按钮切换此开关以重新获得对机器人的控制。

#### 设备

使用下拉菜单选择神经网络应在其上执行的设备。您有以下选择：

- **CPU**：使用CPU适用于大多数手机，是默认选择。您可以调整线程数以优化性能。
- **GPU**：大多数智能手机都有GPU。具有大输入（如图像）的网络通常在GPU上运行更快。
- **NNAPI**：这将使用[TensorFlow Lite NNAPI委托](https://www.tensorflow.org/lite/performance/nnapi)。现代智能手机通常配有专用的AI加速器。[神经网络API](https://developer.android.com/ndk/guides/neuralnetworks)（NNAPI）为具有图形处理单元（GPU）、数字信号处理器（DSP）和神经处理单元（NPU）的Android设备上的TensorFlow Lite模型提供加速。注意，在一些较旧的手机上，这可能会非常慢！

如果模型处于活动状态，运行模型的设备旁边将显示推理速度（单位：毫秒）。

## 添加您自己的片段

请参考[贡献指南](ContributionGuide.md)了解如何向OpenBot应用添加您自己的片段。

## 代码结构

[TensorFlow Lite对象检测Android演示](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android)被用作集成TFLite模型和获取相机视频流的起点。[DefaultActivity](src/main/java/org/openbot/robot/DefaultActivity.java)运行主线程，并继承自[CameraActivity](src/main/java/org/openbot/robot/CameraActivity.java)以管理相机和UI。[SensorService](src/main/java/org/openbot/robot/SensorService.java)读取所有其他手机传感器并记录它们。[ServerService](src/main/java/org/openbot/robot/ServerService.java)和[NsdService](src/main/java/org/openbot/robot/NsdService.java)与具有React前端的本地[Python服务器](../../policy/README.md#web-app)建立连接。如果您收集数据，它可以自动上传以进行可视化、训练ML模型并将训练好的模型下载到机器人。[env](src/main/java/org/openbot/env)文件夹包含实用类，如[Vehicle](src/main/java/org/openbot/env/Vehicle.java)接口、[GameController](src/main/java/org/openbot/env/GameController.java)接口、[PhoneController](src/main/java/org/openbot/env/PhoneController.java)接口和用于声音反馈的[AudioPlayer](src/main/java/org/openbot/env/AudioPlayer)。[tflite](src/main/java/org/openbot/tflite)文件夹包含[Autopilot](src/main/java/org/openbot/tflite/Autopilot.java)和[Detector](src/main/java/org/openbot/tflite/Detector.java)网络的模型定义。

## 下一步（可选）

训练您自己的[驾驶策略](../../policy/README.md)