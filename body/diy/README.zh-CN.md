# OpenBot DIY

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

我们设计了一种轮式机器人的主体，依赖于低成本、易于获取的业余硬件。你可以在下面找到构建你自己的机器人的说明。如果你有任何进一步的问题或疑虑，请随时联系我们。祝你构建机器人愉快！

## 底盘

### 3D打印

你需要打印并组装以下部件来构建你的OpenBot。

#### 机器人主体

根据你的需求和3D打印机的能力，有几种不同的机器人主体可供选择。我们鼓励你设计和构建自己的主体，但这里有一些起点供你参考：

- [常规主体](cad/regular_body/README.md)：这是我们设计的标准主体；需要一个至少240mmx150mm的打印板。
- [纤细主体](cad/slim_body/README.md)：由于许多常见的3D打印机有较小的打印体积，我们还设计了一个没有保险杠的较小版本，可以在45度角的220mmx220mm打印板上打印。
- [可粘合主体](cad/glue_body/README.md)：对于打印体积更小的3D打印机，还有一个由@sloretz设计的模块化主体，需要将几个部分粘合在一起；适合150mmx140mm的打印板。
- [块状主体](cad/block_body/README.md)：由@Christos-Ps设计的这个主体提供了多个变体，具有外壳内额外空间和兼容乐高的顶部，同时保持了仅需221mmx150mm的打印面积。

#### 手机支架

此外，你还需要打印一个手机支架并固定到机器人主体上。

- phone_mount_bottom ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
- phone_mount_top ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

#### 清理

在你继续构建之前，你可能需要清理3D打印件。
<p float="left">
  <img src="../../docs/images/clean_3d_print_1.jpg" width="32%" />
  <img src="../../docs/images/clean_3d_print_2.jpg" width="32%" /> 
  <img src="../../docs/images/clean_3d_print_3.jpg" width="32%" />
</p>

### 替代方案

如果你无法使用3D打印机，有几种Arduino机器人车套件可供选择，这些套件可以作为起点。这些套件包含底盘、电机和配件。我们建议购买一个基本套件，因为你不需要更昂贵套件中的许多电子元件和传感器。以下是一些选项：

- Perseids DIY Robot Smart Car Chassis Kit ([EU](https://www.amazon.de/dp/B07DNXBNHY), [US](https://www.amazon.com/dp/B07DNXBFQN))
- SZDoit 4WD Smart Metal Robot Car Chassis Kit ([US](https://www.amazon.com/dp/B083K4RKBP), [AE](https://www.aliexpress.com/item/33048227237.html))
- Joy-it Robot Car Kit 01 ([EU](https://www.amazon.de/dp/B073ZGJF28))
- Smart Car Kit 4WD Smart Robot Car Chassis Kit ([AE](https://www.aliexpress.com/item/4001238626191.html))

你还需要一个手机支架。以下是一些选项：

- Phone Mount ([EU](https://www.amazon.de/dp/B06XDYJNSR), [US](https://www.amazon.com/dp/B09CY8MC2R))

你也可以发挥创意，使用你选择的材料（例如木材、纸板、泡沫塑料等）构建自己的OpenBot底盘和手机支架。如果你这样做了，请在[Slack频道](https://github.com/ob-f/OpenBot#contact)上发布一些照片，让其他人欣赏你的创意。以下是[@custom-build-robots](https://custom-build-robots.com/roboter/openbot-dein-smartphone-steuert-ein-roboter-auto-chassis-bauen/13636)的一个例子：

<p float="left">
  <img src="../../docs/images/chassis_cardboard_1.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_2.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_3.jpg" width="32%" />
</p>

## 组装

有两种不同的机器人组装选项，DIY和PCB。DIY方法依赖于流行的L298N电机驱动器，推荐给有一些电子经验的爱好者。它需要相当多的布线，特别是如果安装了所有传感器和LED。然而，所有组件在大多数国家都很容易获得，特别是对于单个构建或只是尝试该项目，推荐使用DIY选项。为了减少布线并使组装更容易，我们还开发了一个[定制PCB](pcb)。如果你希望构建更整洁或想要构建多个OpenBot，推荐使用这个选项。

### 材料清单

我们的机器人主体依赖于易于获取的业余电子元件。我们提供了德国（EU）和美国（US）的快速运输链接。如果你有耐心等待更长时间，你也可以从AliExpress（AE）以更便宜的价格获取组件。你将需要以下组件。

#### 必需组件

- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 4x TT电机和轮胎 ([EU](https://www.conrad.de/de/p/joy-it-com-motor01-getriebemotor-gelb-schwarz-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspbe-1573543.html), [US](https://www.amazon.com/dp/B081YQM55P), [AE](https://www.aliexpress.com/item/4000126948489.html))
- 3x 18650电池 ([EU](https://www.conrad.de/de/p/conrad-energy-18650-usb-spezial-akku-18650-li-ion-3-7-v-1400-mah-1525536.html), [US](https://www.amazon.com/dp/B083K4XSKG), [AE](https://www.aliexpress.com/item/32352434845.html))
- 1x 18650电池架 ([EU](https://www.amazon.de/dp/B075V25QJ9), [US](https://www.amazon.com/dp/B07DWQYD7H), [AE](https://www.aliexpress.com/item/33037738446.html))
- 1x USB OTG电缆 ([EU](https://www.amazon.de/gp/product/B075M4CQHZ) ,[US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x 弹簧或橡皮筋 ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 16x M3x25螺丝 ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 16x M3螺母 ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- 6x M3x5螺丝 ([EU](https://www.amazon.de/dp/B01HBRG3W8), [US](https://www.amazon.com/dp/B07MBHMLL2), [AE](https://www.aliexpress.com/item/32892594230.html))
- 杜邦线 ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### 可选组件

- 2 x 速度传感器 ([EU](https://www.conrad.de/de/p/joy-it-sen-speed-erweiterungsmodul-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspberry-pi-pc-1646891.html), [US](https://www.amazon.com/dp/B081W2TY6Q), [AE](https://www.aliexpress.com/i/32850602744.html))
- 1x 超声波传感器 ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 1x 开关 ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 2x 橙色LED 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 1x OLED显示屏 ([EU](https://www.amazon.de/dp/B079H2C7WH), [US](https://www.amazon.com/dp/B085NHM5TC), [AE](https://www.aliexpress.com/item/4001268387467.html))

#### DIY组件（选项1）

- 1x L298N电机驱动器 ([EU](https://www.conrad.de/de/p/joy-it-motormodul-2-u-4-phasen-6-bis-12v-1573541.html), [US](https://www.amazon.com/dp/B085XSLKFQ), [AE](https://www.aliexpress.com/item/32994608743.html))
- （可选）电阻器（2x 150<span>&#8486;</span>用于LED，20 k<span>&#8486;</span>和10k<span>&#8486;</span>用于分压器）
- （组合）4x TT电机和轮胎 + 2x L298N + 杜邦线 ([US](https://www.amazon.com/dp/B07ZT619TD))
- （组合）4x TT电机和轮胎 + 电线 + 螺丝 ([US](https://www.amazon.com/dp/B07DRGTCTP))

#### PCB组件（选项2）

- 1x [定制PCB](pcb)
- 5x Micro JST PH 2.0电缆 ([EU](https://www.amazon.de/gp/product/B07449V33P), [US](https://www.amazon.com/dp/B09JZC28DP), [AE](https://www.aliexpress.com/item/32963304134.html))

### 构建说明

**提示：** 点击图片可以在新标签页中以全分辨率打开。

#### 选项1：DIY

<p float="left">
  <img src="../../docs/images/diy_parts.jpg" height="300" />
  <img src="../../docs/images/wiring_diagram.png" height="300" /> 
</p>

**提示：** 为了使所有布线更容易，你可以通过将一个6x2公头焊接到一个洞洞板上来构建一个小型电源分配器。然后将电源分配器连接到电机驱动器的5V / GND。

# 组装指南

1. 将电线焊接到电机上，如果你打算使用速度传感器，请将编码器盘安装到前面的两个电机上。
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. 将左侧两个电机的正负极插入L298N板的OUT1（+）和OUT2（-），将右侧两个电机的正负极插入L298N板的OUT4（+）和OUT3（-）。
3. 使用八颗M3x25螺丝和螺母安装电机。
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
4. 使用四颗M3x5螺丝安装L298N。
5. （可选）安装超声波传感器，并将角度连接器替换为直连接器（或小心弯曲引脚）。
    <p float="left">
      <img src="../../docs/images/sonar_front.jpg" width="32%" />
      <img src="../../docs/images/sonar_back.jpg" width="32%" /> 
      <img src="../../docs/images/sonar_bend_pins.jpg" width="32%" />
    </p>
6. （可选）安装用于指示信号的橙色LED。
    <p float="left">
      <img src="../../docs/images/led_insert.jpg" width="32%" />
      <img src="../../docs/images/led_left.jpg" width="32%" /> 
      <img src="../../docs/images/led_right.jpg" width="32%" />
    </p>
7. 使用两颗M3x25螺丝和螺母将手机支架的底部安装到顶板上。
    <p float="left">
      <img src="../../docs/images/install_camera_mount_1.jpg" width="32%" />
      <img src="../../docs/images/install_camera_mount_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_camera_mount_3.jpg" width="32%" />
    </p>
8. 插入手机支架的顶部并安装弹簧或橡皮筋。
    <p float="left">
      <img src="../../docs/images/install_spring_1.jpg" width="32%" />
      <img src="../../docs/images/install_spring_2.jpg" width="32%" /> 
    </p>
9. 将角度连接器替换为直连接器（或小心弯曲引脚），然后使用每个M3x5螺丝安装速度传感器。
    <p float="left">
      <img src="../../docs/images/install_speed_sensor_1.jpg" width="32%" />
      <img src="../../docs/images/install_speed_sensor_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_speed_sensor_3.jpg" width="32%" />
    </p>
10. 安装电池盒（例如魔术贴）。
    <p float="left">
      <img src="../../docs/images/install_battery_1.jpg" width="32%" />
      <img src="../../docs/images/install_battery_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_battery_3.jpg" width="32%" />
    </p>
11. （可选）插入开关并将其放入电流路径中。
    1. 将开关推入相应的开口，直到听到咔哒声。
    2. 将电池盒的红色电线（12V）和电源线分别焊接到开关的一个引脚上。连接黑色电线（GND），并用热缩管覆盖连接处。
    3. 用胶带固定电线。
    <p float="left">
      <img src="../../docs/images/install_switch_1.jpg" width="32%" />
      <img src="../../docs/images/install_switch_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_switch_3.jpg" width="32%" />
    </p>
12. （可选）安装OLED显示屏。
13. 将L298N的PWM输入连接到Arduino的D5、D6、D9和D10引脚。
14. 将速度传感器和超声波传感器连接到5V和GND。
15. 将速度传感器的D0引脚分别连接到Arduino的D2（左）和D3（右）引脚。
16. 将超声波传感器的Echo和Trigger引脚分别连接到Arduino的D11和D12引脚。
17. （可选）将LED连接到Arduino的D4（左）和D7（右）引脚以及GND。建议串联一个150欧姆的电阻以限制电流。
18. （可选）将分压器连接到Arduino的A7引脚。它用于测量电池电压。
19. （可选）通过I2C总线将OLED显示屏（SSD1306芯片）连接到Arduino Nano。
    1. 将显示屏的VIN和GND引脚连接到5V和GND。
    2. 将显示屏的SCL引脚连接到A5引脚。
    3. 将显示屏的SDA引脚连接到A4引脚。
20. 将电源线连接到L298N的+12V和GND。
21. 将USB线连接到Arduino，并通过顶盖引出。
22. 将六颗M3螺母插入底板，并用六颗M3x25螺丝安装顶盖。
23. 安装轮子。

#### 选项2：定制PCB

1. 将带有Micro JST PH 2.0连接器的电线焊接到电机上，如果你打算使用速度传感器，请将编码器盘安装到前面的两个电机上。
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. 使用八颗M3x25螺丝和螺母安装电机。
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
3. 将左侧两个电机连接到M3和M4，将右侧两个电机连接到M1和M2。
    <p float="left">
      <img src="../../docs/images/connect_motors_pcb.jpg" width="32%" />
    </p>
4. 使用四颗M3x5螺丝安装PCB，并使用八颗M3x25螺丝和螺母安装电机。
    <p float="left">
      <img src="../../docs/images/attach_pcb.jpg" width="32%" />
      <img src="../../docs/images/chassis_motors_pcb.jpg" width="32%" />
    </p>
5. 按照DIY选项中的步骤5-12进行操作。
6. 将超声波传感器（VCC/+、Trig、Echo、GND/-）连接到PCB上标有*SONAR*的4针插头。
    <p float="left">
      <img src="../../docs/images/connect_sonar_sensor.jpg" width="32%" />
    </p>
7. 将左侧和右侧指示信号（橙色LED）连接到PCB上标有*SIGNAL_L*和*SIGNAL_R*的2针插头。较长的引脚为+，较短的为-。
8. 将左侧和右侧速度传感器（VCC/+、GND/-、D0）连接到PCB上标有*SPEED_L*和*SPEED_R*的3针插头。
9. （可选）将OLED显示屏（SSD1306芯片）连接到PCB上的IO2插头。
    1. 将显示屏的VIN和GND引脚连接到5V和GND。
    2. 将显示屏的SCL引脚连接到A5引脚。
    3. 将显示屏的SDA引脚连接到A4引脚。
10. 将电源线连接到PCB的Vin（Micro JST PH 2.0连接器）。
11. 按照DIY选项中的步骤21-23进行操作。

## 下一步

刷写 [Arduino 固件](../../firmware/README.md)