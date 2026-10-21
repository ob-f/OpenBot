# Robot iOS App - Beta Release

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## 免责声明

1. **安全性：** 请确保您在安全的环境中操作。请记住，您的手机可能会在碰撞中受损！在使用自动控制（例如跟随人或驾驶策略）时需要特别小心。请确保您始终连接了游戏控制器，并熟悉按键映射，以便随时停止车辆。使用风险自负！
2. **应用程序正在开发中：** 该应用程序正在开发中，可能会根据您的手机型号和操作系统版本出现崩溃或意外行为。请确保在没有连接车轮的情况下测试所有功能。使用风险自负！

3. **该应用即将在 `App Store` 上`推出`。**

## 应用程序界面

### 主菜单

应用程序启动时会显示一个菜单屏幕，展示所有可用的屏幕。点击右上角的蓝牙图标可以打开蓝牙连接屏幕。点击旁边的设置图标可以打开设置屏幕。点击其他图标，用户可以访问各种屏幕，其功能将在后续部分中解释。

<p align="left">
<img src="../../../docs/images/ios_main_screen.jpg" alt="主菜单" height="400"/>
<img src="../../../docs/images/ios_settings_screen.jpg" alt="设置" height="400"/>
<img src="../../../docs/images/ios_bluetooth_screen.jpg" alt="蓝牙" height="400"/>
</p>

#### 蓝牙连接

与允许通过USB电缆将智能手机连接到OpenBot低级控制板的Android应用程序不同，iOS应用程序仅依赖于蓝牙低功耗（BLE）无线连接。在iOS应用程序中打开蓝牙连接屏幕时（通过点击主屏幕或任何片段中的蓝牙标志），会显示所有兼容设备的列表。兼容性通过在[应用程序](https://github.com/3dwesupport/OpenBot/blob/090dcb28206195a7ee45a13b8ded968a8d365abe/ios/OpenBot/OpenBot/Utils/Constants.swift#L57)和[固件](https://github.com/3dwesupport/OpenBot/blob/090dcb28206195a7ee45a13b8ded968a8d365abe/firmware/openbot_nano/openbot_nano.ino#L115)级别分配给OpenBot车辆的一系列特定UUID来强制执行。您必须确保这些UUID匹配。将iOS设备与OpenBot车辆配对只需从列表中选择该车辆并按下“连接”按钮。连接的默认波特率设置为115200，可以在应用程序和固件级别更改。

<p align="left">
<img src="../../../docs/images/ios_ble.gif" alt="BLE连接" height="400" />
</p>

#### 多语言支持

点击右上角的齿轮图标打开设置界面，在“常规”部分点击`语言`，即可选择应用的显示语言。选择`系统默认`会跟随iPhone的语言，也可以从支持的语言中选择：English、Deutsch、Español、Français、中文、한국어、हिन्दी。新语言会立即生效。

<p align="left">
<img src="../../../docs/images/ios_language_screen.jpg" alt="语言选择" height="400"/>
</p>

#### Web信令服务器

在设置界面的`Web信令服务器`输入框中输入信令服务器的WebSocket地址，例如 `ws://<服务器IP>:8080/ws`。当控制模式设置为Web服务器时会用到它，iPhone和服务器需要连接到同一个Wi-Fi网络。设置方法请参见[Web服务器](../../../controller/web-server/README.md)。

<p align="left">
<img src="../../../docs/images/ios_web_signaling_screen.jpg" alt="Web 信令服务器" height="400"/>
</p>

### 自由漫游

自由漫游提供简单的机器人控制，并实时更新电池、电量和与表面的距离信息。它还提供与控制器、驾驶模式和速度相关的控制。

<p align="left">
<img src="../../../docs/images/ios_free_roam_screen.jpg" alt="自由漫游" width="50%" />
</p>

- **电池：** 电池图标显示连接机器人的实时电池电量。

- **驾驶模式：** 视图上显示三种驾驶模式：

    - D -> 驾驶，当机器人向前行驶时

    - N -> 空档，当机器人静止时

    - R -> 倒车，当机器人向后移动时

- **速度：** 速度计显示机器人的实时速度。

- **声纳：** 声纳视图显示机器人与迎面物体的距离（以厘米为单位）。

- **蓝牙：** 显示与微控制器的蓝牙连接状态。点击图标，用户还可以跳转到蓝牙屏幕查看/修改连接。

#### 控制

第一个按钮用于选择**控制模式**。有两种不同的控制模式：

- **游戏手柄：** 应用程序接收来自连接的蓝牙控制器的控制。
- **手机（即将推出）：** 机器人可以通过安装了控制器应用程序的另一部智能手机或通过连接到同一网络的计算机上运行的Python脚本进行控制。

第二个按钮用于选择**驾驶模式**。使用游戏控制器（例如PS4）时，有三种不同的驾驶模式：

- **游戏：** 使用右肩和左肩触发器（R2，L2）进行前进和倒车油门，并使用任意一个摇杆进行转向。此模式模仿赛车视频游戏的控制模式。
- **摇杆：** 使用任意一个摇杆控制机器人。
- **双摇杆：** 使用左摇杆和右摇杆分别控制汽车的左侧和右侧。这是原始的差速转向。

第三个按钮用于选择**速度模式**。有三种不同的速度模式：

- **慢速：** 应用于电机的电压限制为输入电压的50%（约6V）。
- **正常：** 应用于电机的电压限制为输入电压的75%（约9V）。
- **快速：** 没有限制。全油门时将对电机应用全输入电压（约12V）。*这是运行神经网络的默认设置。*

以更高的速度运行会减少电机的使用寿命，但更有趣。发送到机器人的控制显示在右侧。使用游戏控制器时，可以通过按下右摇杆（R3）增加速度模式，通过按下左摇杆（L3）减少速度模式。

### 数据收集

用于收集数据集的简单UI。

<p align="left">

<img src="../../../docs/images/ios_data_collection_screen.jpg" alt="数据收集" width="50%" />

</p>

- **预览分辨率：** 用于切换相机预览的分辨率。有3种设置：

    - ***高*** (1920x1080p)

    - ***中*** (1280x720p)

    - ***低*** (640x360)

- **模型分辨率：** 用于切换保存用于训练不同模型的图像分辨率。

- **服务器**：服务器功能正在开发中。

- **记录收集的数据：** 数据收集过程可以从屏幕或远程控制，例如通过蓝牙控制器进行控制。使用蓝牙控制器时，您可以：

    - 按下**A按钮**以**开始**数据收集过程

    - 再次按下**A按钮**以**停止**数据收集并将收集的数据保存到.zip文件中

    - 或者按下**R1按钮**以**停止**数据收集**而不保存**收集的数据（例如由于意外碰撞环境）

    - 请记住使用控制器映射片段以确保您使用的是正确的按钮。

- **车辆状态：** **电池**字段显示微控制器通过分压器测量的电池电压。**速度（左，右）**字段报告（前）轮的左侧和右侧速度（以rpm为单位）。它由微控制器通过光学车轮速度传感器测量。**声纳**字段显示汽车前方的空闲空间（以厘米为单位）。它由微控制器通过超声波传感器测量。请注意，USB连接建立后几秒钟内您才会收到数值。

- **传感器：** 报告车辆传感器的测量值。目前，我们记录以下传感器的读数：相机、陀螺仪、加速度计、磁力计、环境光传感器和气压计。使用iOS API，我们能够获得以下传感器读数：RGB图像、角速度、线性加速度、重力、磁场强度、光强度、大气压力、纬度、经度、高度、方位和速度。除了手机传感器外，我们还记录通过串行链接传输的车身传感器读数（车轮里程计、障碍物距离和电池电压）。如果存在连接的控制器，我们还记录并时间戳控制信号。最后，我们集成了几种用于跟随人和自主导航的神经网络。

### 控制器映射

用于检查连接的蓝牙控制器的按钮和摇杆映射的简单UI。

<p align="left">
<img src="../../../docs/images/ios_controller_mapping.jpg" alt="控制器映射" width="30%" />
</p>

### 机器人信息

用于获取机器人信息和测试基本功能的简单UI。固件中配置的**机器人类型**以文本和动画形式显示。**传感器**、**车轮里程计**和**LED**部分的复选标记显示连接的机器人支持哪些功能。**读数**部分提供最重要的传感器测量值。在**发送命令**部分，用户可以通过按下相应的按钮发送基本电机命令，并通过滑块控制前后LED。

<p align="left">
<img src="../../../docs/images/ios_screen_robot_info.gif" alt="机器人信息" width="50%" />
</p>

### 自动驾驶

用于运行自动驾驶模型的简单UI。

<p align="left">

<img src="../../../docs/images/ios_autopilot_screen.jpg" alt="自动驾驶" width="50%" />

</p>

- **服务器**：如果正在运行用于策略训练的 [Web 应用](../../../policy/README.zh-CN.md#web应用程序)，可以在此选择它，并将训练好的自动驾驶模型发送给机器人。
- **模型**：选择用于自动驾驶模式的训练模型。
- **设备**：使用 CPU、GPU 或 NNAPI 进行推理（详见[此处](../../android/app/README.zh-CN.md#设备)）。
- **线程**：使用的线程数（仅在选择 CPU 作为设备时有效）。
- **控制**：控制器、驾驶模式和速度用于设置机器人，详见[控制部分](#控制)。

### 物体跟踪

用于跟踪80种不同类别物体的简单UI。有关物体跟踪的不同AI模型和性能基准的简短描述可以在[模型管理](#模型管理)中找到。

<p align="left">
<img src="../../../docs/images/ios_object_tracking_screen.jpg" alt="物体跟踪" width="50%" />
</p>

- **动态速度**：在“自动模式”下，机器人越接近被跟踪的物体，速度越低。
  速度根据边界框的面积缩放（横屏效果最好）。
- **模型**：根据手机性能选择物体检测器。
- **物体**：选择要跟踪的物体。模型可以检测 80 种 COCO [物体类别](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/)。
- **置信度**：决定是否接受检测结果的阈值。出现误检时调高，检测不到目标物体时调低。
- **设备**：使用 CPU、GPU 或 NNAPI 进行推理（详见[此处](../../android/app/README.zh-CN.md#设备)）。
- **线程**：使用的线程数（仅在选择 CPU 作为设备时有效）。
- **控制**：控制器、驾驶模式和速度用于设置机器人，详见[控制部分](#控制)。

### 模型管理

所有模型都经过量化以提高嵌入式设备上的性能。请注意，具有较大输入分辨率的模型可能尽管mAP较低，但对较小物体更好。

<p align="left">
<img src="../../../docs/images/ios_screen_model_management.gif" alt="模型管理" width="25%" />
</p>

### 导航

在 `Point Goal Navigation` 界面中，可以用 3D 空间中向前和向左的值为机器人设置导航目标。在应用中点击相应按钮即可进入此界面。

点击后会弹出一个窗口，其中有 `Forward` 和 `Left` 两个输入框。在其中输入数值即可设置目标位置。弹窗中还有两个按钮：`Cancel` 和 `Start`。

- `Cancel` 按钮：返回主界面并取消导航。


- `Start` 按钮：根据输入的向前和向左的值在 3D 空间中创建一个点，并开始导航。机器人会朝目标位置移动，并根据需要调整轨迹和速度以到达目的地。

机器人成功到达目标位置后，会显示一条提示消息，机器人随即停止。

此功能使用 Apple 为增强现实应用开发的框架 `ARKit`。需要兼容 ARKit 的设备才能使用。

<p align="left">
<img src="../../../docs/images/ios_set_navigation_screen.jpg" alt="Model Management" width="25%" />
<img src="../../../docs/images/ios_navigation_screen.jpg" alt="Model Management" width="25%" />
</p>


### 项目界面

如果您已使用 Google 帐号登录，项目界面会列出您的 OpenBot Playground 项目。您可以运行这些项目来控制 OpenBot，也可以点击右上角的扫描图标扫描它们的二维码。如果未登录，界面会显示 Google 登录按钮，但您仍然可以在不登录的情况下扫描项目二维码。如果登录后界面显示 `Oops, no project found`，请确认该帐号在 Google Drive 中存有项目。

如果列表中没有显示最新的项目，可以在项目界面向下拉动来重新加载。

<p align="left">
<img src="../../../docs/images/projects_tab_screen_ios.gif" alt="Project Screen" width="25%"/>
<img src="../../../docs/images/no_projects_found_ios.jpg" alt="No project screen" style="padding-left: 0.3%" width="26.5%"/>
<img src="../../../docs/images/reload_projects_ios.gif" alt="Reload project screen" width="25%"/>
</p>

- **Google Drive 项目**：要运行 Google Drive 项目，点击该项目并等待读取项目文件。如果顺利读取且没有错误，会弹出一个窗口，显示项目名称以及 `Start` 和 `Cancel` 两个按钮。点击 Start 运行项目，点击 Cancel 取消。如果弹出 `Something went wrong` 的提示，可能是 Google Drive 文件有问题。请向下拉动刷新项目界面后重试。


- **二维码扫描**：要扫描 Playground 项目的二维码，点击界面右上角的二维码图标，并允许应用访问相机。扫描后等待读取文件。如果顺利读取且没有错误，会弹出一个窗口，显示项目名称以及 `Start` 和 `Cancel` 两个按钮。点击 Start 运行项目，点击 Cancel 取消。如果弹出 `Something went wrong` 的提示，可能是 Google Drive 文件有问题。请在 Playground 中生成新的二维码后重试。


- **运行项目**：如果 OpenBot Playground 项目运行成功，界面会显示代码块名称以及一个停止按钮，可用于停止执行 Playground 积木命令。


- **删除项目**：长按要删除的项目，会弹出确认删除的窗口。点击“Yes”即可删除。

<p align="left">
<img src="../../../docs/images/google_drive_projects_execute_ios.gif" alt="Google Drive project execute" width="25%" />
<img src="../../../docs/images/ios_qr_scan.gif" alt="Qr code scanner project execute" width="25%" />
<img src="../../../docs/images/delete_project_ios.jpg" alt="Delete project popup" style="padding-right: 1.2%" width="26%" />
</p>


### 个人资料界面
个人资料界面会根据用户是否已登录提供不同的选项。
如果未登录，会显示 `Google Sign-in` 按钮，提示用户登录 Google 帐号。登录后即可访问个人资料和其他功能。
如果已登录，`Profile` 标签页中会显示两个按钮：`Edit Profile` 和 `Logout`。

<p align="left">
<img src="../../../docs/images/logged_out_profile_screen_ios.jpg" alt="Logged out profile screen" width="25%"/>
<img src="../../../docs/images/logged_in_profile_screen_ios.jpg" alt="Logged in profile screen" width="25%"/>
</p>

- **Edit Profile**：点击后会打开一个新界面，用户可以在其中更新个人资料信息，例如姓名和头像。


- **Logout**：退出当前帐号并返回登录界面。

<p align="left">
<img src="../../../docs/images/ios_edit_profile_screen.jpg" alt="Edit profile screen" width="25%"/>
<img src="../../../docs/images/logout_dialog_box_ios.jpg" alt="Logout dialog box" width="25%"/>
</p>


### OpenBot PlayGround 界面

要使用 OpenBot Playground 服务，请点击项目界面工具栏顶部的 OpenBot Playground 图标。想了解更多关于 OpenBot Playground 的信息，请[点击这里](../../../open-code/README.zh-CN.md)。

<p align="left">
<img src="../../../docs/images/playground_services_ios.gif" alt="Playground Services" width="25%" />
<img src="../../../docs/images/playground_sign-in_ios.gif" alt="Playground sign-in" width="25%" />
</p>

## 代码结构

[TensorFlow Lite 物体检测 iOS 示例](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/ios)被用作集成TFLite模型和获取相机视频流的起点。[tflite](OpenBot/tflite)文件夹包含[自动驾驶](OpenBot/tflite/Autopilot.swift)和[检测器](OpenBot/tflite/Detector.swift)网络的模型定义。

## 下一步（可选）

训练您自己的[驾驶策略](../../../policy/README.md)

Firebase 身份验证 [Firebase](../OpenBot/OpenBot/Authentication/README.zh-CN.md)
