# 固件

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

我们使用微控制器单元（MCU）作为机器人主体和智能手机之间的桥梁。我们为带有ATmega328P微控制器的Arduino Nano以及ESP32开发套件提供[固件](openbot/openbot.ino)。

## 功能

MCU的主要任务是处理车辆的低级控制，并提供来自车辆安装的低级传感器的读数。MCU通过串行连接接收车辆控制和指示灯信号。它将控制信号转换为电机控制器的PWM信号，并根据指示灯信号切换LED。Arduino程序还通过计算左前轮和右前轮的光学传感器中断来跟踪车轮旋转。它通过电压分压电路的缩放移动平均值计算电池电压。它还可以通过可选的超声波传感器测量车前的障碍物距离。这些测量值通过串行链接发送回Android应用程序。

## 设置

所有设置都在 `openbot.ino` 顶部的 **SETTINGS** 部分。其下方的内容无需修改。

为了在更新固件时保留您的设置，请将 SETTINGS 部分中 `#else` 和 `#endif` 之间的行复制到 `openbot.ino` 旁边的新文件 `openbot_settings.h` 中，并在那里修改。如果存在 `openbot_settings.h`，固件会使用它代替 `openbot.ino` 中的部分；git 会忽略该文件，因此不会被误推送。

### 机器人车身

将 `OPENBOT` 设置为您的机器人车身，并在 Arduino IDE 中选择它支持的开发板。固件会检测开发板；使用其他开发板时，编译会报错停止。

| 车身 | `OPENBOT` | Arduino Nano | ESP32 Dev Module |
|---|---|:---:|:---:|
| DIY 版本（L298N 电机驱动） | `DIY` | ✓ | ✓ |
| 定制 PCB V1 / V2 | `PCB_V1` / `PCB_V2` | ✓ | |
| Ready-to-Run 套件，TT 电机 | `RTR_TT` | ✓ | |
| Ready-to-Run 套件，TT 电机，ESP32 | `RTR_TT2` | | ✓ |
| Ready-to-Run 套件，520 电机 | `RTR_520` | | ✓ |
| RC 卡车 | `RC_CAR` | ✓ | |
| OpenBot Lite | `LITE` | ✓ | |
| Multi Terrain Vehicle | `MTV` | | ✓ |

`DIY_ESP32` 已被选择 ESP32 开发板的 `DIY` 取代。

### 驾驶

默认情况下，您通过手机应用驾驶机器人。要使用[无手机模式](#无手机模式)，请设置 `NO_PHONE_MODE 1`；使用 ESP32 时，还可以用[手柄](#手柄控制)驾驶（`HAS_GAMEPAD 1`）。`COAST_MODE` 决定无控制时电机滑行（1）还是刹车（0），`DEBUG 1` 会在串口监视器上输出调试信息。

### 部件

每种车身会开启其通常具备的部件：

| `OPENBOT` | 开启的部件 |
|---|---|
| `DIY`, `PCB_V1`, `PCB_V2` | 分压器、转向灯、声纳、前轮速度传感器 |
| `RTR_TT`, `RTR_TT2`, `RTR_520` | 分压器、转向灯、声纳、保险杠、前后轮速度传感器、前灯、尾灯和状态 LED |
| `LITE` | 转向灯 |
| `RC_CAR` | 无 |
| `MTV` | 前、中、后轮速度传感器 |

在 ESP32 上，与手机的蓝牙连接也会开启。

在设置中，每个部件都设为车身的默认值，例如 `#define HAS_SONAR DEFAULT_HAS_SONAR`，上方的表格列出了每种车身的默认值。要更改某个部件，请将其改为 `1`（开）或 `0`（关），例如未安装超声波传感器时设为 `#define HAS_SONAR 0`。开启但未安装的部件不会造成损害，但应用会显示无意义的读数，例如 300 cm 的声纳距离。

| 设置 | 部件 |
|---|---|
| `HAS_VOLTAGE_DIVIDER` | 电池电压测量 |
| `HAS_INDICATORS` | 转向灯 |
| `HAS_SONAR`, `SONAR_MEDIAN` | 超声波传感器及其读数的中值滤波 |
| `HAS_BUMPER` | 用于检测碰撞的保险杠 |
| `HAS_SPEED_SENSORS_FRONT`, `_BACK`, `_MIDDLE` | 车轮速度传感器 |
| `HAS_LEDS_FRONT`, `HAS_LEDS_BACK`, `HAS_LEDS_STATUS` | 前灯、尾灯和状态 LED |
| `HAS_OLED` | OLED 显示屏 |
| `HAS_BLUETOOTH` | 与手机的蓝牙连接（ESP32） |

### 电池

应用的电量显示范围从 `VOLTAGE_LOW`（空）到 `VOLTAGE_MAX`（满）。它们默认为车身的默认值；使用其他电池时，请在设置中修改，例如 `#define VOLTAGE_LOW 6.8f`：

| 电池 | `VOLTAGE_LOW` | `VOLTAGE_MAX` |
|---|---:|---:|
| 2S 锂离子 / LiPo | 6.8f | 8.4f |
| 3S 锂离子 / LiPo | 10.2f | 12.6f |
| 6 节 AA | 6.0f | 9.0f |
| 8 节 AA | 8.0f | 12.0f |

如果您的分压器与车身的不同，还需将 `VOLTAGE_DIVIDER_FACTOR` 设置为 (R1 + R2) / R2。`VOLTAGE_MIN` 是电机仍能转动的最低电压；固件仅在无手机模式下使用它。

### 检查配置

机器人启动时，串口监视器（115200 波特）会显示车身和已开启的部件，例如：

```
OpenBot DIY (ESP32): voltage, indicators, sonar, speed sensors front, Bluetooth
```

如果两个部件使用同一个引脚，编译会以错误 "Two parts use the same pin" 停止。

### 依赖项

如果您启用了速度传感器或超声波传感器，则需要安装[PinChangeInterrupt](https://github.com/NicoHood/PinChangeInterrupt)库。Arduino Nano只有两个外部中断引脚（D2和D3），而D3也是仅有的六个支持PWM的引脚之一。幸运的是，它还有三个端口中断，覆盖了Arduino上的所有引脚。此库解析这些端口中断，使Arduino的所有引脚都可以用作中断。

如果您启用了OLED，则需要安装[Adafruit_SSD1306](https://github.com/adafruit/Adafruit_SSD1306)和[Adafruit_GFX Library](https://github.com/adafruit/Adafruit-GFX-Library)库。

您可以按照以下步骤安装库：

1. 打开库管理器：`工具` :arrow_right: `管理库`
2. 在搜索栏中输入库的名称。
3. 选择最新版本并点击安装。如果您已经安装了库，它将显示，您可能可以更新它。

<p float="left">
  <img src="../docs/images/manage_libraries.jpg" height="300" />
  <img src="../docs/images/install_library.jpg" height="300" /> 
</p>

### 中国克隆Nano（例如美国链接）

您可能需要从芯片制造商（中国）下载[WCH340](http://www.wch.cn/product/CH340.html)驱动程序：

- [Windows](http://www.wch.cn/downloads/CH341SER_EXE.html)
- [Linux](http://www.wch.cn/download/CH341SER_LINUX_ZIP.html)
- [Mac](http://www.wch.cn/download/CH341SER_MAC_ZIP.html)

### ESP32开发套件

要在Arduino IDE中安装ESP32板，请按照以下说明进行操作：

1. 在Arduino IDE中，转到**文件>首选项**：
<p align="center">
  <img src="../docs/images/arduino-ide-open-preferences.png" width="300" alt="App GUI"/>
</p>

2. 在“_Additional Board Manager URLs_”字段中输入*https://dl.espressif.com/dl/package_esp32_index.json*，如下面的图所示。然后，点击“OK”按钮：
<p align="center">
  <img src="../docs/images/arduino_preferences.png" width="600" alt="App GUI"/>
</p>

**注意：**如果您已经有ESP8266板的URL，您可以用逗号分隔URL，如下所示：

    https://dl.espressif.com/dl/package_esp32_index.json,
    http://arduino.esp8266.com/stable/package_esp8266com_index.json

3. 打开板管理器。转到**工具 > 板 > 板管理器**：
<p align="center">
  <img src="../docs/images/arduino_boardsManager.png" width="800" alt="App GUI"/>
</p>

4. 搜索ESP32。查找“ESP32 by Espressif Systems”，确保从下拉菜单中选择版本2.0.17。任何版本3.x.x都无法工作！点击安装：
<p align="center">
  <img src="../docs/images/arduino_installing.png" width="600" alt="App GUI"/>
</p>

5. 现在，您应该拥有一切，可以使用Arduino开发环境成功刷新OpenBot的ESP32板。
<p align="center">
  <img src="../docs/images/arduino_ESP32-Board-add-on-in-Arduino-IDE-installed.png" width="600" alt="App GUI"/>
</p>

6. 要用新代码刷新OpenBot，只需在菜单**工具 > 板 > ESP32 Arduino**中选择**ESP32 Dev Module**。请注意，ESP32刷新过程的其他内容以及故障排除可以在以下[链接](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/)中找到。
<p align="center">
  <img src="../docs/images/arduino_windows-select-board.png" width="600" alt="App GUI"/>
</p>

如果上传有问题，请尝试降低波特率。转到工具并选择上传速度为115200。
<p align="center">
<img src="../docs/images/arduino_upload-speed-select.png" width="600" alt="App GUI"/>
</p>

## 上传

### 设置（Arduino nano设置）

- `工具` :arrow_right: `板` :arrow_right: `Arduino AVR Boards` :arrow_right: `Arduino Nano`
- `工具` :arrow_right: `处理器` :arrow_right: `ATmega328P (Old Bootloader)`
- `工具` :arrow_right: `端口` :arrow_right: `*选择USB端口*`

:memo: 注意：目前，大多数便宜的Arduino Nano板都带有_旧引导加载程序_。但是，根据卖家，您也可能会得到一个带有新引导加载程序的。因此，如果您无法上传固件，可能需要将处理器更改为_ATmega328P_。

### 设置（ESP32设置）

- `工具` :arrow_right: `板` :arrow_right: `ESP32 Arduino` :arrow_right: `ESP32 Dev Module`
- `工具` :arrow_right: `端口` :arrow_right: `*选择USB端口*`

### 上传固件

现在可以通过`草图` :arrow_right: `上传`或按上传按钮（右箭头）上传固件。
![Firmware Upload](../docs/images/firmware_upload.png)

### 测试

本节解释了在固件成功刷新后如何测试汽车的所有功能。

1. 确认：
   1. 车轮未连接到汽车
   2. Arduino已连接到计算机
   3. 已选择正确的USB端口
2. 打开串行监视器：`工具` :arrow_right: `串行监视器`

#### 向OpenBot发送消息

您还可以通过在顶部的输入字段中输入命令然后按发送来向Arduino发送消息。以下命令可用（前提是机器人支持必要的功能）：

- `c<left>,<right>`，其中`<left>`和`<right>`均在范围[-255,255]内。值为`0`将停止电机。值为`255`将以最大电压驱动电机以全速前进。较低的值导致成比例较低的电压和速度。负值以反极性施加相应的电压，使电机反向运行。
- `i<left>,<right>`，其中`<left>`和`<right>`均在范围[0,1]内，分别对应于左指示灯和右指示灯。例如，`i1,0`打开左指示灯，`i0,1`打开右指示灯，`i1,1`打开两个指示灯。启用的指示灯每秒闪烁一次。值为`i0,0`关闭指示灯。一次只能有一个状态。
- `l<front>,<back>`，其中`<front>`和`<back>`均在范围[0,255]内，分别对应于前后LED的亮度。
- `s<time_ms>`，其中`<time_ms>`对应于触发声纳测量之间的时间（默认=1000）。声纳读取完成后，消息将发送到机器人。如果超时，将发送指定的`MAX_SONAR_DISTANCE`。
- `w<time_ms>`，其中`<time_ms>`对应于发送到机器人的车轮里程测量之间的时间（默认=1000）。车轮速度持续监控，并在指定时间间隔内计算平均转速。
- `v<time_ms>`，其中`<time_ms>`对应于发送到机器人的电压测量之间的时间（默认=1000）。电压持续监控，并通过大小为10的移动平均滤波器进行滤波。除了设置电压读数的时间间隔外，发送此命令还将触发报告驱动电机的最低电压（`vmin:<value>`）、最低电池电压（`vlow:<value>`）和最高电池电压（`vmax:<value>`）的消息。
- `h<time_ms>`，其中`<time_ms>`对应于在未收到新的心跳消息后机器人将停止的时间（默认=-1）。
- `b<time_ms>`，其中`<time_ms>`对应于在保险杠触发后重置的时间（默认=750）。
- `n<color>,<state>`，其中`<color>`对应于状态LED（`b`=蓝色，`g`=绿色，`y`=黄色），`state`对应于其值（`0`=关闭，`1`=打开）。
- `f`将向OpenBot发送请求，以返回带有机器人类型及其功能的消息，例如电压测量（`v`）、指示灯（`i`）、声纳（`s`）、保险杠传感器（`b`）、车轮里程（`wf`，`wb`）、LED（`lf`，`lb`，`ls`）等。例如，对于OpenBot的`RTR_V1`版本，消息将如下所示：`fRTR_V1:v:i:s:b:wf:wb:lf:lb:ls:`。

#### 从OpenBot接收消息

根据您的配置，您可能会看到不同的消息。

![Serial Monitor](../docs/images/serial_monitor.png)

- 以`v`开头的消息报告电池电压。如果您将电池连接到汽车（即打开开关），它应显示电池电压。如果您断开电池连接（即关闭开关），它应显示一个小值。
- 以`w`开头的消息报告速度传感器的读数，以每分钟转数（rpm）为单位。编码器盘上的每个孔将根据方向将计数器增加或减少一个。您可以使用参数`DISK_HOLES`设置孔的数量。如果您使用的是带有20个孔的标准盘，每次车轮旋转将有20个计数。
- 以`s`开头的消息报告超声波传感器前方的估计空闲空间，以厘米为单位。
- 以`b`开头的消息报告碰撞。代码`lf`（左前）、`rf`（右前）、`cf`（中前）、`lb`（左后）、`rb`（右后）指示哪个传感器触发了碰撞。

#### 测试程序

在继续之前，请确保已移除轮胎。您将需要打开串行监视器以发送命令，并将看到从OpenBot接收到的消息。如果您安装了OLED显示屏，您还将在更易于阅读的格式中看到车辆状态。以下测试程序可用于测试汽车的所有功能：

1. 打开汽车并观察电池电压（`v`后的数字）。您可以使用万用表验证读数，并在必要时调整`VOLTAGE_DIVIDER_FACTOR`。
2. 如果您安装了超声波传感器：
   1. 将手放在传感器前方，并来回移动。您应该看到读数（`s`后的数字）相应变化。
   2. 我们观察到超声波传感器对振动非常敏感！因此，建议通过执行以下测试确保在操作期间获得可靠的读数：
      1. 将安装有超声波传感器的OpenBot放置在其前方至少有200厘米的空闲空间的位置。您应该看到`200`或更高的读数。
      2. 在串行监视器上观察读数一段时间，然后输入命令`c128,128`。
      3. 如果传感器读数发生显著变化，您将需要减轻从底盘传递到超声波传感器的振动（例如，添加一些硅胶，调整安装位置）。
3. 如果您安装了速度传感器：
   1. 确保超声波传感器前方有足够的空闲空间。读数（`s`后的数字）至少需要高于`STOP_DISTANCE`，默认值为`10`。
   2. 发送命令`c128,128`。电机将以_慢速_（50% PWM）开始旋转。速度传感器读数（`w`后的值）以rpm为单位报告，对于RTR_TT版本，应在250到300之间，具体取决于电池的SOC。如果您使用的是DIY版本或较弱的电池，值可能会较低。检查所有电机是否向前旋转，并且速度传感器读数为正。
   3. 尝试发送不同的控制并观察速度传感器读数。例如，命令`c-128,-128`将以_慢速_（50% PWM）向后旋转所有电机。命令`c255,-255`将以_快速_（100% PWM）向前旋转左电机，向后旋转右电机。命令`c-192,192`将以_正常速度_（75% PWM）向后旋转左电机，向前旋转右电机。
4. 通过发送命令`c0,0`或将手放在超声波传感器前方来停止电机。
5. 如果您安装了指示灯LED，发送命令`i1,0`并观察左指示灯闪烁。然后发送命令`i0,1`并观察右指示灯闪烁。最后，通过发送命令`i0,0`关闭指示灯。

### 无手机模式

在使用安装了OpenBot应用程序的智能手机测试汽车之前，您还可以先在没有手机的情况下测试汽车。只需在设置中设置 `NO_PHONE_MODE 1`。汽车现在将以_正常速度_（75% PWM）行驶，并在检测到障碍物时减速。一旦接近`TURN_THRESHOLD`（默认值：50厘米），它将开始向随机方向转弯，并打开该侧的LED。如果汽车前方的估计空闲空间低于`TURN_THRESHOLD`，它将缓慢倒退，并打开两个LED。请注意，汽车和Arduino都需要供电。Arduino可以通过将5V引脚连接到L298N电机驱动器的5V输出来供电，或者通过将USB电缆连接到电源（例如手机）来供电。

在运行汽车之前，我们建议移除轮胎，将Arduino连接到计算机，并观察串行监视器，如[测试](#testing)部分所述。串行监视器上的输出更容易解析（与OLED相同），显示电池电压、左电机和右电机的rpm以及汽车前方的估计空闲空间。您可以在超声波传感器前方来回移动一个大物体，并观察电机速度的变化。

:warning: 警告：如果您没有安装超声波传感器或禁用了它，汽车将仅以_正常速度_（75% PWM）向前行驶，并最终发生碰撞。即使安装了传感器，由于读数噪声，汽车也可能偶尔发生碰撞。

### 手柄控制

在无手机模式下，您可以用蓝牙手柄驾驶 ESP32 车体，而不是让它自行行驶。

#### 设置

1. 在 Arduino IDE 的 **首选项 > 附加开发板管理器网址** 中添加此网址：
   `https://raw.githubusercontent.com/ricardoquesada/esp32-arduino-lib-builder/master/bluepad32_files/package_esp32_bluepad32_index.json`
2. 在开发板管理器中安装 **esp32_bluepad32**（4.1.0）。它基于与普通固件相同的 esp32 内核（2.0.17）。
3. 在 esp32_bluepad32 开发板中选择 **ESP32 Dev Module**。
4. 在设置中设置 `NO_PHONE_MODE 1` 和 `HAS_GAMEPAD 1`，然后上传。

手柄使用 ESP32 的蓝牙，因此在此模式下与手机的蓝牙连接会关闭。要用手机应用驾驶，请保持 `HAS_GAMEPAD 0`，并将手柄与手机配对。

#### 配对

打开机器人电源，让手柄进入配对模式。几秒钟内即可连接，之后每次打开手柄都会自动重新连接。要配对另一个手柄，请先关闭已连接的手柄：同一时间只能由一个手柄驾驶。

| 手柄 | 配对模式 |
|---|---|
| PS5 DualSense | 按住 **Create**，再按住 **PS**，直到灯条闪烁。 |
| PS4 DualShock 4 | 按住 **Share**，再按住 **PS**，直到灯条闪烁。 |
| Xbox 无线控制器（1708 型或更新型号） | 开机后按住配对按钮 3 秒。请先将控制器固件更新到 5.15 或更高版本（例如使用 Xbox 配件应用）。 |
| Switch Pro、Joy-Con、8BitDo 等 | 使用手柄的蓝牙配对模式（参见其说明书）。 |

[Bluepad32 支持的手柄列表](https://bluepad32.readthedocs.io/en/latest/supported_gamepads/) 中有更多型号，包括需要额外设置的 PS3 DualShock 3。设置 `DEBUG 1` 后，串口监视器会显示每个连接的手柄名称。

#### 操作

按键按 PlayStation / Xbox 标注。

| 操作 | 功能 |
|---|---|
| **R2 / L2** | 前进 / 后退（游戏模式） |
| **左摇杆** | 转向（游戏模式）、行驶和转向（摇杆模式）或左侧（双摇杆模式） |
| **右摇杆** | 右侧（双摇杆模式） |
| **L1** | 切换驾驶模式：游戏 → 摇杆 → 双摇杆。手柄振动一次：游戏为短振，摇杆较长，双摇杆最长。 |
| **方向键上 / R3** | 加速 |
| **方向键下 / L3** | 减速 |
| **方块 / X** | 左转向灯开或关 |
| **圆圈 / B** | 右转向灯开或关 |
| **三角 / Y** | 关闭转向灯 |
| **叉 / A** | 车灯开或关 |
| **Start** | 探索开或关 |
| **Select / Back** | 障碍物停车开或关 |

对于 Start 和 Select，长振表示开启，短振表示关闭。

**驾驶模式。** 手柄以 `GAMEPAD_DRIVE_MODE` 设置的模式启动（默认：游戏）。
- **游戏：** R2 前进，L2 后退，左摇杆转向。倒车时转向反向，和汽车一样。
- **摇杆：** 左摇杆控制行驶和转向。即使倒车，机器人也总是朝摇杆指向的方向转。
- **双摇杆：** 每个摇杆控制一侧，像坦克一样。

**速度。** 共有三档速度：慢（50 %）、正常（75 %，启动时的速度）和快（100 %）。手柄通过玩家指示灯（一到三个）或灯条（绿、橙、红）显示速度。

**探索。** 探索就是上文介绍的无手机模式行驶：机器人自行行驶并避开障碍物。启动时探索为关闭状态，按 **Start** 开启。探索开启时，只要移动摇杆或扳机，手柄就会接管控制。松开后，机器人会短暂停止，然后继续探索。探索需要连接超声波传感器并设置 `HAS_SONAR 1`；否则机器人只会一直直行（参见上文的警告）。

**障碍物停车。** 用手柄驾驶时，如果超声波传感器检测到比 `STOP_DISTANCE` 更近的障碍物，机器人不会前进。启动时为开启状态；按 **Select** 可关闭，例如需要推动物体时。只有连接超声波传感器并设置 `HAS_SONAR 1` 时才有效；否则机器人永远不会因障碍物停车。

**安全。** 手柄断开连接时，机器人会停止。大多数手柄还会持续发送数据；如果这类手柄 0.5 秒没有数据（超出范围、电池耗尽），机器人会立即停止，而不必等到断开连接。

## 使用其他MCU（要求）

您可以使用具有以下功能的任何其他MCU：

- 1x USB-to-TTL串行（与智能手机通信）
- 4x PWM输出（控制电机）
- 1x模拟引脚用于电池监控
- 2x数字引脚用于速度传感器
- 1x数字引脚用于超声波传感器（可选）
- 2x数字引脚用于指示灯LED（可选）

## 下一步

编译并运行[Android应用程序](../robot/android/README.zh-CN.md)