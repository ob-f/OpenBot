# OpenBot: RC-Truck 车身

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

我们为广泛使用的1:16比例RC玩具卡车/越野车（例如[这个](https://www.amazon.de/dp/B00M3J7DJW)在亚马逊上）设计了一个机器人车身。

![RC-Truck-Banner](/docs/images/rc-truck-banner.jpg)

我们还有一个通用的[车身](/body/)，设计用于依赖低成本、易得的业余硬件的简单轮式机器人。常规OpenBot的构建说明可以在[这里](/body/README.md)找到。

## 底盘

OpenBot RC-Truck的底盘由两个主要组件组成：(a) 您选择的1:16比例RC玩具卡车和(b) 我们提供的一些定制设计的零件，这些零件可以3D打印。

### 1:16 RC玩具卡车

要构建您自己的OpenBot RC-Truck，您需要一个1:16比例的RC玩具卡车/越野车。我们提供了德国（[EU](https://www.amazon.de/dp/B00M3J7DJW)）、（[EU](https://www.amazon.de/dp/B088FGVYNW)）和美国（[US](https://www.amazon.com/gp/product/B09C8XMPQ9)）的兼容RC玩具卡车的亚马逊链接，具有快速运输速度。类似的1:16比例玩具卡车也可以在其他在线零售商如eBay、阿里巴巴或AliExpress上找到，通常价格更低但运输速度较慢。

无论您选择哪个零售商和版本的RC玩具卡车，请确保它确实是1:16比例的RC卡车。这很重要，因为我们提供的3D打印零件目前仅设计适合1:16比例的卡车，可能需要一些小调整（稍后会详细介绍）。以下是一些兼容的1:16比例RC玩具卡车/越野车的示例。

<p float="left">
  <a href="https://www.amazon.de/dp/B00M3J7DJW" target="_blank"> <img src="/docs/images/rc_toy_1.jpg" width="34%" /> &nbsp
  </a>
  <a href="https://www.amazon.com/gp/product/B09C8XMPQ9" target="_blank"> <img src="/docs/images/rc_toy_2.jpg" width="27%" /> &nbsp &nbsp &nbsp &nbsp
  </a>
  <a href="https://www.amazon.de/dp/B088FGVYNW" target="_blank"> <img src="/docs/images/rc_toy_3.jpg" width="27%" />
  </a>
</p>

### 3D打印

您需要打印以下零件以构建您的OpenBot RC-Truck。

1) ```main_frame``` ([STL](cad/rc_truck_body/main_frame.stl), [STEP](cad/rc_truck_body/main_frame.step))
2) ```side_cover``` \[x2\] ([STL](cad/rc_truck_body/side_cover.stl), [STEP](cad/rc_truck_body/side_cover.step))
3) ```phone_mount_bottom``` ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
4) ```phone_mount_top``` ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

注意，\[xN\]表示您需要打印的特定零件的副本数量（即N）。

以下零件是可选的（但推荐）以使您的OpenBot RC-Truck更紧凑和美观。

5) ```camera_elevator``` ([STL](cad/rc_truck_body/camera_elevator.stl), [STEP](cad/rc_truck_body/camera_elevator.step))
6) ```electronics_cover``` \[x2\] ([STL](cad/rc_truck_body/electronics_cover.stl), [STEP](cad/rc_truck_body/electronics_cover.step))
7) ```spacer``` \[x4\] ([STL](cad/rc_truck_body/spacer.stl), [STEP](cad/rc_truck_body/spacer.step))
8) ```front_light_spacer``` \[x2\] ([STL](cad/rc_truck_body/front_light_spacer.stl), [STEP](cad/rc_truck_body/front_light_spacer.step))

对于上述所有零件，您的打印板需要至少260mmx220mm，这是```main_frame```的打印尺寸。

由于许多常见的3D打印机具有较小的构建体积（通常为220mmx220mm），还有两个可行的选项。
第一个选项是以45度角打印```main_frame```，并增加支撑材料。
第二个选项需要修改原始的```main_frame```零件。我们推荐使用[Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview)进行此类CAD修改（Fusion 360提供免费的1年学术许可证）。
对于此选项，我们提供其[STEP](/body/cad/rc_truck_body/main_frame.step)文件，您可以将其切割成两/三个较小的部分。
然后，生成的子部分将适合标准（即220mmx220mm）的打印板，并可以在打印后组装在一起。
将来，我们可能还会在此处发布这种模块化版本的```main_frame```。所有其他零件需要最小的打印板尺寸为220mmx60mm。

在Ultimaker S5上，我们使用以下设置取得了良好的效果：

- 层高：0.2mm
- 壁厚：1.5mm
- 填充密度：20%
- 填充模式：网格
- 打印速度：80 mm/s
- 无支撑

我们能够使用PLA、CPE和ABS打印底盘。根据我们的经验，打印设置对打印影响不大。然而，如果您有耐心，较慢的打印速度和较小的层高将改善打印效果。此外，添加支撑结构可以改善打印，但需要额外的工作来移除。

在您继续构建之前，您可能需要清理3D打印件。然而，使用上述设置，我们在构建过程中不需要任何打磨或清理。如果可能，我们建议使用两种不同颜色（例如绿色/黑色或红色/黑色）组合打印同一个OpenBot RC-Truck的不同部分，如下所示。

**提示：** 点击图片在新标签页中以全分辨率打开。

<p float="left">
  <img src="/docs/images/3d_print_rc_1.png" width="32%" />
  <img src="/docs/images/3d_print_rc_2.png" width="32%" /> 
  <img src="/docs/images/3d_print_rc_3.png" width="32%" />
</p>

## 组装

虽然可以采用类似于常规OpenBot的DIY方法构建您的OpenBot RC-Truck（参见OpenBot的DIY构建组件和说明[这里](/body/README.md)），我们推荐使用OpenBot的[定制PCB](/body/pcb)来构建和组装OpenBot RC-Truck。如果您希望构建更整洁或希望构建多个OpenBot RC-Truck，这个选项是推荐的。使用我们的[定制PCB](/body/pcb)的另一个优势是，您可以使用相同的组件构建并在不同的OpenBot车身之间切换。

### 材料清单

OpenBot RC-Truck主要依赖于易得的业余电子产品。我们提供了德国（EU）和美国（US）具有快速运输速度的亚马逊链接。如果您有耐心等待更长时间，您也可以从AliExpress（AE）以更便宜的价格获得组件。您将需要以下组件。

#### 必需组件

- 1x RC玩具卡车/越野车（[EU](https://www.amazon.de/dp/B00M3J7DJW), [EU](https://www.amazon.de/dp/B088FGVYNW), [US](https://www.amazon.com/gp/product/B09C8XMPQ9)）
- 1x Arduino Nano（[EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html)）
- 1x OpenBot [定制PCB](/body/pcb)
- 1x USB OTG电缆（[EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html)）
- 1x 弹簧或橡皮筋（[EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html)）
- 6x M3x25螺丝（[EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html)）
- 6x M3螺母（[EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html)）
- 杜邦线（[EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html)）

#### 可选组件

- 1x 超声波传感器（[EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html)）
- 2x 开关（[EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html)）
- 4x 橙色LED 5mm（[EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html)）
- 4x 红色LED 5mm（[EU](https://www.amazon.de/dp/B083HN3CLY), [US](https://www.amazon.com/dp/B077X95F7C), [AE](https://www.aliexpress.com/item/4000329069943.html)）
- 2x 白色LED灯（[EU](https://www.amazon.de/-/en/gp/product/B06XTQSZDX), [US](https://www.amazon.com/gp/product/B01N2UPAD8), [AE](https://de.aliexpress.com/item/1005002991235830.html)）
- LED用可变电阻器（[EU](https://www.amazon.de/gp/product/B081TXJJGV), [US](https://www.amazon.com/dp/B0711MB4TL), [AE](https://de.aliexpress.com/item/1005003610664176.html)）

### 构建说明

**提示：** 点击图片在新标签页中以全分辨率打开。

# 下一步

1. 拆卸遥控玩具卡车。移除其顶部盖子，并按照下图所示从底座上拧下四个安装销。保管好所有四个安装销及其对应的螺丝，因为在所有布线完成后，你将使用它们将```main_frame```安装到遥控卡车车身上。所有兼容的遥控玩具卡车都配有两个电机：一个用于油门，另一个用于转向，还有一个用于油门电机的速度控制器（内置5-7V UBEC）和一个2S 7.4V LiPo电池组。从卡车底座上卸下并移除电池组，并使用随车附带的充电器为其充电。暴露/松开两个电机的线连接器以及速度控制器的UBEC输出。在我们的案例中，UBEC输出为6V。
    <p float="left">
      <img src="/docs/images/rc_truck_disassembly_1.JPG" width="32%" />
      <img src="/docs/images/rc_truck_disassembly_2.JPG" width="32%" /> 
      <img src="/docs/images/rc_truck_disassembly_3.JPG" width="32%" />
    </p>
2. 注意到```main_frame```上的两个尺寸d1和d2（如下图所示）取决于所使用的遥控玩具卡车的型号。我们为[这个](https://www.amazon.de/dp/B00M3J7DJW)遥控玩具卡车型号设计了我们的```main_frame```部件。根据你使用的卡车（1:16比例），你可能需要使用```main_frame``` [STEP](/body/cad/rc_truck_body/main_frame.step)文件稍微调整这些尺寸。我们推荐使用[Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview)进行此类CAD修改（Fusion 360提供免费的一年学术许可证）。另外，注意```main_frame```上的小楔形/三角形表示前进方向。
    <p float="left">
      <img src="/docs/images/main-frame-dimensions.png" width="32%" />
      <img src="/docs/images/main-frame-direction.png" width="32%" />
    </p>   
3. （可选）安装用于为机器人供电的开关。你可以简单地通过剪断从速度控制器到电池的正极线，并将开关焊接在这条线的两个分开的部分之间来实现这一点。请确保开关连接器通过热缩管或电工胶带绝缘，并且电源线足够长，以便在组装后开关可以通过```main_frame```背面的矩形开口（见下图）。
    <p float="left">
      <img src="/docs/images/main-frame-switch.png" width="32%" />
      <img src="/docs/images/switch-power.jpg" width="32%" />
    </p>
4. （可选）通过```main_frame```的前格栅安装超声波传感器。如果需要，可以使用热熔胶将其固定到位。在放置之前，轻轻地将连接器推直。这将使组装后更容易访问连接器。将杜邦线从超声波连接器一直拉到```main_frame```背面的矩形开口。
    <p float="left">
      <img src="/docs/images/install-ultrasonic-1.png" width="32%" />
      <img src="/docs/images/ultrasonic-sensor.jpg" width="32%" />
      <img src="/docs/images/install-ultrasonic-2.png" width="32%" />
    </p>
5. （可选）安装用于指示信号的橙色LED灯，分别在```main_frame```的前部和后部。如果需要，可以使用热熔胶将其固定到位。对于每一侧，即左侧和右侧，你需要将前后LED灯并联连接。为此，只需分别将它们的正极和负极连接在一起。类似于超声波传感器电缆，将正负杜邦线从左右指示信号一直拉到```main_frame```背面的矩形开口，在那里它们将连接到PCB上的相应指示信号引脚（正极和负极）。
    <p float="left">
      <img src="/docs/images/insert-leds-orange-1.png" width="32%" />
      <img src="/docs/images/orange-led.jpg" width="32%" />
      <img src="/docs/images/insert-leds-orange-2.png" width="32%" />
    </p>
**提示：** 为了避免布线时的混乱和潜在的接地错误，建议为所有LED的负极形成一个统一的接地环。这意味着在```main_frame```下方运行一根连接所有LED负极的线。然后，这个接地环可以通过一根杜邦线连接到Arduino Nano的接地引脚，该杜邦线运行到```main_frame```背面的矩形开口。

6. （可选）安装前LED灯。你可以使用热熔胶将底座固定到位，并通过每侧的前开口将灯拧入其相应的底座。通过分别连接正极和负极，将两个前LED灯并联连接。由于这些灯工作在6V，你可以将它们的正极直接连接到UBEC输出。将负极连接到接地环（见上面的提示）。这些LED的内部电阻相当高，因此无需添加任何外部电阻。安装LED灯后，在每侧插入并用热熔胶固定两个```front_light_spacers```以锁定LED。
    <p float="left">
      <img src="/docs/images/insert-lamps-1.png" width="32%" />
      <img src="/docs/images/led-lamp-wiring.jpg" width="32%" />
      <img src="/docs/images/add_front_light_spacer.png" width="32%" />
    </p>
7. （可选）安装用于后灯的红色LED。如果需要，可以使用热熔胶将其固定到位。将所有四个红色LED并联连接；即分别连接它们的正极和负极。负极将接地，而正极将通过适当的分压器连接到UBEC输出（有关分压器构造的详细信息，请参见下一步）。
    <p float="left">
      <img src="/docs/images/insert-leds-red.png" width="32%" />
      <img src="/docs/images/red-led.jpg" width="32%" />
    </p>
8. （可选）为后部红色LED安装分压器。大多数彩色LED（例如红色、橙色、黄色等）工作在2-3V，而不是传统的5V，这是Arduino Nano的正常工作电压。因此，需要一个分压器来安全地操作这些LED。对于指示信号，我们的定制PCB中已经内置了一个分压器。因此，你不需要为使用指示信号（即橙色）LED做任何事情。然而，如果你选择添加后灯，即红色LED，那么需要为它们安装一个外部分压器。我们建议使用10kΩ或更高的可变电阻来制作你的分压器。根据你的UBEC输出电压（在我们的案例中为6V），你需要设置一个输出为2-3V的分压器。这可以通过在电阻的外端施加UBEC输出，并通过旋转其顶部的螺丝并使用数字万用表监测地和中间端子之间的输出电压来完成（见下图）。一旦可变电阻的输出电压，即分压器的电压设置在适当的2-3V范围内，用热熔胶固定其螺丝，并将其固定在```main_frame```下方的一个方便位置。
    <p float="left">
      <img src="/docs/images/variable-resistor.jpg" width="32%" />
      <img src="/docs/images/voltage-divider-animation.png" width="32%" />
    </p>
9. （可选）你还可以使用一个或两个单独的开关来打开和关闭前后LED灯。请按照步骤3中的说明安装一个或多个开关以实现此目的。
10. 现在你几乎完成了机器人的布线。此时，请花一些时间确保```main_frame```下方的所有电线和连接正确并使用热缩管或电工胶带进行良好绝缘。使用热熔胶将任何松散的电线固定到位，以防止它们在组装后接触到车轮或机器人的任何活动部件。确保所有来自电机、速度控制器UBEC、LED和超声波传感器的电缆都可以自由地从```main_frame```背面的矩形开口出来。
11. 使用两颗M3x25螺丝和螺母将```phone_mount_bottom```安装到```main_frame```上。如果你想调整手机支架的垂直高度，可以选择在中间插入一个或多个```camera_elevators```。如果使用```camera_elevator```，你将需要M3x35或更长的螺丝将手机支架安装到```main_frame```上。
    <p float="left">
      <img src="/docs/images/add_phone_mount_bottom.png" width="32%" />
      <img src="/docs/images/add_phone_mount_bottom_elevator.png" width="32%" /> 
    </p>
10. 插入```phone_mount_top```并安装弹簧或橡皮筋。
    <p float="left">
      <img src="/docs/images/add_phone_mount_top.png" width="32%" />
    </p>
11. 将两个```side_covers```插入各自的槽中。
    <p float="left">
      <img src="/docs/images/add_side_covers.png" width="32%" />
      <img src="/docs/images/add_side_covers_2.png" width="32%" />
    </p>    
12. 使用四个安装销及其对应的螺丝将```main_frame```安装到遥控卡车车身上。确保所有电缆连接器和机器人的电源开关可以通过```main_frame```背面的矩形开口进行PCB连接。从```main_frame```前面的三角形开口拉出电池连接器。
    <p float="left">
      <img src="/docs/images/add_main_frame_1.JPG" width="32%" />
      <img src="/docs/images/add_main_frame_2.png" width="32%" />
      <img src="/docs/images/add_main_frame_3.JPG" width="32%" />
    </p>
12. 使用四颗M3x25螺丝和螺母在```main_frame```背面安装PCB，并在中间放置四个```spacers```。将Arduino Nano安装到PCB上，并将USB OTG电缆连接到Arduino Nano的USB端口。
    <p float="left">
      <img src="/docs/images/pcb_assembly.JPG" width="32%" />
    </p>
13. 将超声波传感器电缆连接到PCB上标有“sonar”的连接器。确保传感器和PCB端口之间的正负极和数据线正确匹配。
14. 将左右指示灯LED电缆连接到PCB上的相应指示信号连接器。确保LED正负极的正确极性。
15. 将UBEC输出（+6V）连接到Arduino Nano的Vin引脚（可选，Arduino也可以由手机供电），并将UBEC GND连接到Arduino的GND引脚（在Vin旁边）。
16. 将UBEC输出（+6V）连接到转向伺服电机、前LED灯和通过分压器的后红色LED的正极端子。
17. 将转向伺服电机的地线连接到Arduino的GND引脚。
18. 将油门伺服电机的PWM电缆（来自速度控制器）连接到Arduino Nano或PCB扩展板上的A0引脚。
19. 将转向伺服电机的PWM电缆连接到Arduino Nano或PCB扩展板上的A1引脚。
**提示：** 如果你已经为LED布线创建了一个统一的接地环，那么将接地环电缆连接到Arduino的一个GND引脚。Arduino Nano有三个GND引脚可用。如果你没有构建接地环，那么确保所有LED、转向伺服电机、传感器、Arduino Nano和速度控制器的UBEC共享相同的接地，并进行适当的布线和连接。
21. 将电池组连接到前部，并使用魔术贴或安装胶带将其固定到位。将电池放在前部可以方便地进行充电。这种放置方式还可以在智能手机安装在顶部时帮助平衡机器人的重量。
22. 安装前后```electronics_covers```。从后```electronics_cover```的缝隙中拉出USB OTG电缆，以便将其连接到安卓智能手机。
<p float="left">
      <img src="/docs/images/add_covers_1.png" width="32%" />
      <img src="/docs/images/add_covers_2.JPG" width="32%" />
    </p>

## 下一步

刷入[Arduino固件](../../firmware/README.md)