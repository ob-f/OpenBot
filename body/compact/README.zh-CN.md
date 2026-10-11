# OpenBot Compact

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

OpenBot Compact 是一款紧凑的四轮 [OpenBot](../../README.zh-CN.md) 车身，适用于 **ESP32 DevKit V1 + L298N** 驱动。它带有兼容乐高的凸点顶盖、螺丝固定的盖板、两件式手机支架、可拆卸的螺丝固定电子元件支架，以及三种电池方案。所有打印件都能放进 **Bambu Lab A1 mini**（最大零件 104 × 168 mm）。

<p align="center">
  <img src="../../docs/images/compact/teaser_front.jpg" alt="前视图" width="45%" />
</p>

| | |
|---|---|
| **驱动** | 4 个双轴 TT 减速电机；左侧一对和右侧一对分别并联到一块 L298N |
| **控制器** | ESP32 DevKit V1（30 针），OpenBot 固件 `DIY_ESP32`；通过蓝牙或 USB 线连接手机 |
| **传感器** | HC-SR04 超声波（前方），4 个 LM393 光电车轮编码器，电池电压 |
| **供电** | 三选一：顶盖上的 2S 或 3S 18650 电池盒，或车内的小型 LiPo 电池（见[电池方案](#电池方案)） |
| **紧固件** | 只用 M3 × 10 和 M3 × 25 盘头螺丝、M3 螺母和 16 个热熔螺母 |
| **打印** | PLA，两块 A1 mini 打印板，约 11 小时、约 300 g，另加所选电池方案的零件 |

> 本指南中的**左和右**始终指**机器人自身**的左和右：站在机器人后方（开关一侧），越过顶盖向前看向超声波传感器。固件对“左”“右”电机和编码器也使用同样的约定。

## 目录

- [打印零件](#打印零件) · [打印](#打印)
- [电池方案](#电池方案)
- [需要购买的硬件和电子元件](#需要购买的硬件和电子元件)
- 组装：[1 热熔螺母和顶盖螺母](#1-安装热熔螺母和顶盖螺母) · [2 电机和编码器](#2-安装电机和测速传感器) · [3 超声波传感器和前面板](#3-固定超声波传感器和前面板零件) · [4 ESP32 和驱动板](#4-安装-esp32-和电机驱动板) · [5 接线](#5-连接电源和信号线) · [6 固件和台架测试](#6-配置固件和台架测试) · [7 电池、手机支架和顶盖](#7-安装电池手机支架和顶盖)
- [集成驱动的替代 PCB](#集成驱动的替代-pcb)
- [故障排除](#故障排除) · [视图](#视图) · [下一步](#下一步)

## 打印零件

<p align="center">
  <img src="../../docs/images/compact/electronics_mounts.jpg" alt="带电子元件支架的底盘" width="45%" />
</p>

| 零件 | 数量 | 安装位置 / 打印方向 |
|---|---:|---|
| [bottom_cover](cad/stl/bottom_cover.stl) ([STEP](cad/step/bottom_cover.step)) | 1 | 底盘；底面朝下，无需支撑 |
| [top_cover](cad/stl/top_cover.stl) ([STEP](cad/step/top_cover.step)) | 1 | 带两个线缆开口的凸点顶盖；平整的底面朝下，凸点朝上 |
| [lid_plug](cad/stl/lid_plug.stl) ([STEP](cad/step/lid_plug.step)) | 2 | 封住顶盖开口并补全两个凸点。凸点朝上。 |
| [lid_grommet](cad/stl/lid_grommet.stl) ([STEP](cad/step/lid_grommet.step)) | 1 *(可选)* | 在有线缆穿过的位置代替堵头。凸点朝上。 |
| [esp32_carrier](cad/stl/esp32_carrier.stl) ([STEP](cad/step/esp32_carrier.step)) | 1 | ESP32 支撑框架；平底朝下 |
| [esp32_retainer](cad/stl/esp32_retainer.stl) ([STEP](cad/step/esp32_retainer.step)) | 4 | ESP32 角压片；任意压片都适合任意角。平面朝下，凸舌朝上。 |
| [sensor_support](cad/stl/sensor_support.stl) ([STEP](cad/step/sensor_support.step)) | 4 | 测速传感器支架；任意支架都适合任意车轮 |
| [ultrasonic_clip_left](cad/stl/ultrasonic_clip_left.stl) ([STEP](cad/step/ultrasonic_clip_left.step)) | 1 | HC-SR04 电路板的**左**上角 |
| [ultrasonic_clip_right](cad/stl/ultrasonic_clip_right.stl) ([STEP](cad/step/ultrasonic_clip_right.step)) | 1 | HC-SR04 电路板的**右**上角 |
| [power_board_stand](cad/stl/power_board_stand.stl) ([STEP](cad/step/power_board_stand.step)) | 1 | 在散热片后方固定 5 V 转换器和分压洞洞板。底座朝下。 |
| [pcb_spacer](cad/stl/pcb_spacer.stl) ([STEP](cad/step/pcb_spacer.step)) | 4 | L298N 下方的 2.4 mm 绝缘垫柱 |
| [pcb_washer](cad/stl/pcb_washer.stl) ([STEP](cad/step/pcb_washer.step)) | 8 | 0.8 mm 绝缘垫圈：4 个用于 L298N，4 个用于测速传感器 |
| [phone_mount_bottom](cad/stl/phone_mount_bottom.stl) ([STEP](cad/step/phone_mount_bottom.step)) | 1 | 手机支架底座；立在其底脚上打印，无需支撑 |
| [phone_mount_top](cad/stl/phone_mount_top.stl) ([STEP](cad/step/phone_mount_top.step)) | 1 | 滑动式手机夹；竖立打印，无需支撑 |
| [led_adapter](cad/stl/led_adapter.stl) ([STEP](cad/step/led_adapter.step)) | 4 | 面板指示灯适配件：前 2 个，后 2 个 |
| [encoder_disk](cad/stl/encoder_disk.stl) ([STEP](cad/step/encoder_disk.step)) | 4 | 可选；用**不透光**耗材打印，或使用匹配的成品码盘。平放打印。 |

四个 ESP32 压片完全相同，四个传感器支架也完全相同，因此任意零件都适合任意位置。超声波卡扣是一对左右镜像件。请将它们宽大的背面朝下平放打印。电池所需零件列在[电池方案](#电池方案)中。

## 打印

所有零件的 STL 和 STEP 文件位于 [cad/stl](cad/stl) 和 [cad/step](cad/step)，已按打印方向摆放，单位为毫米；请按 100 % 比例打印。

在 Bambu Studio 中打开 **[compact_A1_mini.3mf](cad/compact_A1_mini.3mf)**。打印板 1 和 2 包含所有标准零件及所需数量。打印板 3–5 各包含一种电池方案；只打印你选择的那一块。

| 打印板 | 内容 | 时间* | 耗材* |
|---|---|---:|---:|
| 1 – 底盘和配件 | 底盘、手机支架（两个零件）、4 个传感器支架、4 个 LED 适配件、2 个编码器码盘、2 个 ESP32 压片、右超声波卡扣、2 个垫柱、6 个垫圈 | 约 7 小时 50 分钟 | 约 215 g |
| 2 – 顶盖和配件 | 凸点顶盖、ESP32 托架、电源板支架、2 个顶盖堵头、2 个编码器码盘、2 个 ESP32 压片、左超声波卡扣、2 个垫柱、2 个垫圈 | 约 3 小时 20 分钟 | 约 80 g |
| 3 – 电池方案 A（2S 电池盒） | 2S 电池盒、盒盖、顶盖穿线圈 | 约 1 小时 40 分钟 | 约 42 g |
| 4 – 电池方案 B（3S 电池盒） | 3S 电池盒、盒盖、顶盖穿线圈 | 约 2 小时 20 分钟 | 约 58 g |
| 5 – 电池方案 C（LiPo 托盘） | LiPo 托盘 | 约 15 分钟 | 约 5 g |

\*使用 Generic PLA 的估算值；打印板 3–5 按零件体积推算。实际时间会有所不同。打印前请对每块打印板点击 **Slice plate**。

配置：0.4 mm 喷嘴，0.20 mm 层高，4 圈外壁，顶部 5 层、底部 5 层，30 % 螺旋二十四面体（gyroid）填充，纹理 PEI 打印板。**所有零件都不需要支撑。**底盘侧壁上的孔都是圆孔，最大 20 mm，螺母槽的桥接长度约 8 mm。手机支架的两个零件竖立打印。项目中已关闭支撑；如果从 STL 文件夹添加零件，也请保持关闭。打印前请选择实际装载的 PLA，并检查层预览。

- 顶盖凸点朝上平放打印。底盘底面朝下、内腔朝上打印。
- 打印后清除螺母槽（保留每个槽口处的两条小筋）、热熔螺母孔、插座和线缆开口中的拉丝。
- 0.8 mm 垫圈以 0.20 mm 层高平放打印。它们为电路板绝缘并分散螺丝压力。只拧到贴合即可，变形的垫圈请更换。
- PLA 约在 55 °C 时变软，请让 PLA 零件远离高温车内和夏季阳光直射。

## 电池方案

底盘内没有空间放下 3 节 18650 电池盒（76 × 62 × 20 mm）。所有有 20 mm 高度的位置都太窄。请改为从以下三种方案中选择一种：

| | **A – 2S 18650 电池盒**（推荐） | **B – 3S 18650 电池盒** | **C – 内置 LiPo 托盘** |
|---|---|---|---|
| 位置 | 顶盖上，手机支架后方 | 顶盖上，手机支架后方 | 车内，电机驱动板上方 |
| 电池 | 2 节 18650 装在 2 节串联电池座中（约 77 × 41 × 20 mm） | 3 节 18650 装在 3 节串联电池座中（约 76 × 62 × 20 mm） | 2S 或 3S LiPo，**最大 75 × 31 × 20 mm**（例如 3S 450–650 mAh，2S 1000–1300 mAh） |
| 电压 | 满电 8.4 V，标称 7.4 V | 满电 12.6 V，标称 11.1 V | 满电 8.4 V 或 12.6 V |
| 能量 | 约 21 Wh（2 × 2800 mAh） | 约 31 Wh（3 × 2800 mAh） | 约 5–10 Wh |
| 打印零件 | [battery_box_2s](cad/stl/battery_box_2s.stl) + [盖](cad/stl/battery_box_2s_cover.stl)，1 个 [lid_grommet](cad/stl/lid_grommet.stl) | [battery_box_3s](cad/stl/battery_box_3s.stl) + [盖](cad/stl/battery_box_3s_cover.stl)，1 个 [lid_grommet](cad/stl/lid_grommet.stl) | [lipo_tray](cad/stl/lipo_tray.stl) |
| 充电器 | 各槽独立的锂离子充电器 | 各槽独立的锂离子充电器 | LiPo **平衡**充电器 |
| 说明 | 最适合 TT 电机：经过 L298N 压降后电机得到约 6.4 V。 | 对 3–6 V TT 电机而言电压过高（经过 L298N 后约 10.6 V）。请使用更高电压的电机或限制速度。 | 容量最小。电池藏在车内并受到保护。 |

三种方案都保留固件的 30 k / 10 k 电池分压电阻。只需修改固件中的电压阈值（见[第 6 步](#6-配置固件和台架测试)）。

<p align="center">
  <img src="../../docs/images/compact/battery_box_2s.jpg" alt="顶盖上的 2S 电池盒" width="32%" />
  <img src="../../docs/images/compact/battery_box_3s.jpg" alt="顶盖上的 3S 电池盒" width="32%" />
  <img src="../../docs/images/compact/battery_lipo_tray.jpg" alt="LiPo 托盘" width="32%" />
</p>

**A 和 B – 顶盖上的电池盒。** 电池盒像乐高积木一样按在手机支架后方的顶盖凸点上。盒底有一条槽，把电池线引向下方，穿过顶盖后部开口。将电池座放入盒中，让引线位于有槽的一端。把引线穿过底部开口和 [lid_grommet](cad/stl/lid_grommet.stl) 进入底盘，然后把带凸点的盒盖按到四根定位柱上。盒盖上也有凸点，因此上面仍能拼插乐高积木。电池盒不会碍到顶盖四角的螺丝。3S 电池盒完全位于顶盖范围内，与后缘相距 14 mm。装上电池盒后，四颗顶盖螺丝仍然都能拧到。

**C – 内置 LiPo 托盘。** 托盘的两条腿立在电机之间的空隙里，正好位于 L298N 和 ESP32 压片螺丝上方。电池自身的高度和合上的顶盖把它压住。用一条 20 mm 魔术贴绑带穿过四个槽，把电池固定在托盘上。如果电池薄于 18 mm，请在上面垫一块泡棉，防止晃动。请先完成 L298N 的全部接线：托盘会挡住它的接线端子，但可以直接向上取出。

> **锂电池安全。** 请使用正规商家的电芯。18650 电池组请配保护板（BMS）。LiPo 请插上电压报警器，因为固件和电池都不会在电量耗尽时断电。在靠近电池处安装保险丝。LiPo 只能用平衡充电器、在不可燃的表面上、在有人看守时充电。不要使用鼓包或损坏的电池。

## 需要购买的硬件和电子元件

**只需要两种长度的螺丝：M3 × 10 mm 和 M3 × 25 mm**，都是头径约 5.5 mm 的盘头螺丝（不要沉头）。电路板用的垫圈是打印的。顶盖、托架和 ESP32 压片的螺丝下面不要加垫圈，因为它们的长度是按螺丝头直接压在塑料上计算的。

以下链接指向供应商或 Amazon 美国站搜索。下单前请核对标注的尺寸和参数，因为同名电路板的孔位可能不同。

### 紧固件

| 数量 | 零件 | 用途 | 购买 |
|---:|---|---|---|
| 16（+ 备用） | M3 × 5.7 mm 黄铜热熔螺母，常见的 4.6 mm 外径规格（例如 ruthex RX-M3x5.7） | 底盘 12 个，ESP32 托架 4 个 | [ruthex](https://www.ruthex.de/products/ruthex-gewindeeinsatz-m3-100-stuck-rx-m3x5-7-messing-gewindebuchsen) |
| 16 | M3 × 10 mm 盘头螺丝 | 顶盖 4、托架 2、ESP32 压片 4、L298N 4、超声波卡扣 2 | [搜索](https://www.amazon.com/s?k=M3+x+10+pan+head+screws+ISO+7045) |
| 14 | M3 × 25 mm 盘头螺丝 | 电机 8、测速传感器 4、手机支架 2 | [搜索](https://www.amazon.com/s?k=M3+x+25+pan+head+screws+ISO+7045) |
| 14 | M3 六角螺母，对边 5.5 mm，厚约 2.4 mm | 顶盖 4、电机 8、手机支架 2 | [搜索](https://www.amazon.com/s?k=M3+hex+nuts+DIN+934) |
| 4 | 小扎带（宽 ≤ 3 mm） | 在电源板支架上固定 5 V 转换器和洞洞板 | [搜索](https://www.amazon.com/s?k=small+cable+ties+2.5mm) |
| 约 350 g | 1.75 mm PLA | 所有打印零件 | [搜索](https://www.amazon.com/s?k=PLA+filament+1.75mm) |

### 电子元件和驱动

| 数量 | 零件 | 规格 / 说明 | 购买 |
|---:|---|---|---|
| 1 | ESP32 DevKit V1，30 针 | 电路板约 27.94 × 50.97 mm。不同 DevKit 仿制板有差异，请核对孔位。 | [搜索](https://www.amazon.com/s?k=ESP32+DevKit+V1+30+pin+development+board) |
| 1 | L298N 驱动模块 | 约 43.5 × 43.5 mm，孔位为 37 × 37 mm 正方形。 | [搜索](https://www.amazon.com/s?k=L298N+motor+driver+module+43+43) |
| 4 | 双轴 TT 减速电机 | 一根轴驱动车轮，另一根驱动编码器码盘。适合 53.35 mm 长的电机本体。请选择与电池方案相符的额定电压。 | [搜索](https://www.amazon.com/s?k=TT+dual+shaft+gear+motor+robot+car) |
| 4 | TT 车轮 | 直径约 65 mm | [搜索](https://www.amazon.com/s?k=65mm+TT+motor+robot+car+wheels) |
| 4 | LM393 光电测速传感器 | 电路板宽约 14.2 mm、长约 38.1 mm，带一个 3 mm 孔。四个都要安装；固件默认读取前面一对。 | [搜索](https://www.amazon.com/s?k=LM393+optical+speed+sensor+module+encoder+disk) |
| 4 | 编码器码盘 | 仅在不打印时需要；必须匹配 TT 电机轴和传感器槽 | [搜索](https://www.amazon.com/s?k=TT+motor+optical+encoder+disk) |
| 1 | HC-SR04 超声波传感器 | 电路板 45.5 × 20.5 mm，两个探头中心距 25.5 mm | [搜索](https://www.amazon.com/s?k=HC+SR04+ultrasonic+sensor+module) |
| 1 | 圆形船型开关 | 适合后面板的开孔；额定值满足电池电压和电机电流 | [搜索](https://www.amazon.com/s?k=20mm+round+rocker+switch+SPST+DC) |
| 1 | 5 V 转换器，≥ 2 A，最大 21 × 43 × 14 mm | 装在电源板支架上。3S 可用 LM2596 模块。2S 请选择输入 6 V 时仍能输出 5 V 的型号（例如 MP1584 或升降压模块）。 | [搜索](https://www.amazon.com/s?k=5V+buck+converter+2A+module) |
| 1 | 小洞洞板，最大 24 × 34 mm | 电池分压和回波分压电路、灯驱动；装在支架背面 | [搜索](https://www.amazon.com/s?k=small+prototype+perfboard) |
| 1 套 | 1 % 精度电阻 | 电池分压 30 k + 10 k；回波分压 10 k + 20 k；灯 4 × 1 k + 4 × 100 k | [搜索](https://www.amazon.com/s?k=1+percent+metal+film+resistor+assortment+10k+20k+30k+1k+100k) |
| 2 + 2 | 白色和红色面板指示灯 *(可选)* | 核对灯座尺寸与面板开孔是否匹配，以及灯的电压 | [搜索](https://www.amazon.com/s?k=12mm+panel+LED+indicator+12V+white+red) |
| 4 | NPN 三极管，例如 PN2222A *(可选，用于指示灯)* | 每个灯的低边开关 | [搜索](https://www.amazon.com/s?k=PN2222A+transistor) |
| 1 套 | 杜邦线、硅胶电源线、热缩管 | 电源线按电机堵转电流选型 | [搜索](https://www.amazon.com/s?k=silicone+wire+kit+20+22+AWG+heat+shrink) |

### 电池和充电

购买所选[电池方案](#电池方案)需要的部件。

| 方案 | 部件 | 购买 |
|---|---|---|
| A – 2S 电池盒 | 2 节配对的 18650 电芯，2 节**串联**电池座（约 77 × 41 mm），2S 保护板，线上保险丝（约 5 A），一对 XT30，锂离子充电器 | [电芯](https://www.18650batterystore.com/collections/18650-batteries) · [电池座](https://www.amazon.com/s?k=2+x+18650+battery+holder+series+wires) · [BMS/保险丝/XT30](https://www.amazon.com/s?k=2S+BMS+18650+fuse+holder+XT30) · [充电器](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| B – 3S 电池盒 | 3 节配对的 18650 电芯，3 节**串联**电池座（约 76 × 62 mm），3S 保护板，线上保险丝，一对 XT30，锂离子充电器 | [电芯](https://www.18650batterystore.com/collections/18650-batteries) · [电池座](https://www.amazon.com/s?k=3+series+18650+battery+holder) · [BMS/保险丝/XT30](https://www.amazon.com/s?k=3S+lithium+battery+protection+BMS+fuse+holder+XT30+connector) · [充电器](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| C – LiPo 托盘 | 带 XT30、最大 75 × 31 × 20 mm 的 2S 或 3S LiPo，20 mm 魔术贴绑带，LiPo 平衡充电器，LiPo 电压报警器 | [LiPo](https://www.amazon.com/s?k=3S+650mAh+lipo+XT30) · [绑带](https://www.amazon.com/s?k=20mm+battery+strap+hook+loop) · [充电器](https://www.amazon.com/s?k=lipo+balance+charger+2S+3S) |

### 其他

| 数量 | 零件 | 说明 | 购买 |
|---:|---|---|---|
| 1 | 橡皮筋或轻弹簧 | 为手机夹提供张力 | [搜索](https://www.amazon.com/s?k=rubber+bands+assorted+sizes) |
| 1 | 90°（弯头）micro-USB 数据线 | 用于在装好的状态下烧录固件，以及穿过顶盖与手机有线连接。直插头在 L298N 旁边插不进去（见[第 6 步](#6-配置固件和台架测试)）。请按电路板选择插头（micro-USB 或 USB-C）。 | [搜索](https://www.amazon.com/s?k=90+degree+micro+USB+data+cable+up+down+angle) |
| 1 | USB 电源隔离器 / 仅数据转接头 *(仅用于手机有线连接)* | 防止手机和 5 V 转换器互相供电 | [搜索](https://www.amazon.com/s?k=USB+data+only+power+blocker+adapter) |
| 1 | 安卓手机 | 必须能运行 OpenBot 应用。请确认手机带壳时能放进支架。 | – |
| – | 工具 | 带 M3 热熔螺母烙铁头的恒温烙铁、螺丝刀/内六角扳手、万用表、斜口钳、剥线钳 | [搜索](https://www.amazon.com/s?k=heat+set+insert+soldering+iron+tip+M3) |

## 组装

<p align="center">
  <img src="../../docs/images/compact/exploded.jpg" alt="爆炸图" width="45%" />
</p>

### 1. 安装热熔螺母和顶盖螺母

<p align="center">
  <img src="../../docs/images/compact/step_01_shell.jpg" alt="空壳体" width="40%" />
</p>

在 4.0 mm 预留孔中使用标准 M3 × 5.7 mm 热熔螺母（例如 ruthex RX-M3x5.7，外径 4.6 mm）。用恒温烙铁和热熔螺母烙铁头将每个螺母垂直压入并与表面齐平。冷却后再拧入螺丝。

在**底盘中安装 12 个热熔螺母**：

- L298N 凸台 4 个
- 测速传感器凸台 4 个
- 长 PCB 凸台中**前面**一对 2 个（用于固定 ESP32 托架）
- 超声波开口上方朝内的凸台 2 个

在 **ESP32 托架的固定柱中再装 4 个**。长 PCB 凸台中后面两个留空，因为只有[替代 PCB](#集成驱动的替代-pcb) 才会用到。

将 M3 六角螺母从侧面推入四个角上的顶盖锁紧螺母槽，每槽一个，一个角朝前、两个平面贴着槽壁。每个槽口处有两条小筋让槽口略微变窄，所以需要用力推（用一字螺丝刀头会更方便），螺母越过小筋时会“咔”一声进去。装好后即使把底盘翻过来，螺母也不会掉出。如果螺母太松或推不进去，请检查槽内是否有拉丝。

### 2. 安装电机和测速传感器

<p align="center">
  <img src="../../docs/images/compact/step_02_motors.jpg" alt="已装好电机" width="49%" />
  <img src="../../docs/images/compact/step_03_sensors.jpg" alt="已装好传感器" width="49%" />
</p>

装入四个 TT 电机，让车轮轴穿出侧面开口，引线朝向中间。每个电机用两颗 M3 × 25 mm 螺丝和螺母固定。确保螺丝不碰到齿轮箱内部。台架测试完成前**先不要装车轮**。

在每根内侧轴上压入一个编码器码盘。在四个传感器凸台上各放一个传感器支架，平面一侧朝向电机。D 形插口可防止它转动。把 LM393 电路板放在上方两条导轨之间，用一个打印垫圈和一颗 M3 × 25 mm 螺丝固定。

用手转动每根轴。码盘必须无摩擦地穿过光电槽，传感器引脚不能碰到码盘。

### 3. 固定超声波传感器和前面板零件

<p align="center">
  <img src="../../docs/images/compact/ultrasonic_retention.jpg" alt="超声波传感器的固定" width="40%" />
</p>

把 HC-SR04 放入前部凹槽，两个探头穿过圆孔。凹槽为电路板定位，两个卡扣从内侧压住电路板上沿，使它不会掉进底盘。

将 `ultrasonic_clip_left` 装在机器人左侧，`ultrasonic_clip_right` 装在右侧，每个卡扣的小唇边压在 PCB 上角。每个卡扣用一颗 M3 × 10 mm 螺丝拧入朝内的热熔螺母，螺丝头朝向底盘内侧。拧到卡扣贴住凸台且不压弯 PCB 为止。电路板不能翘起或被拔出，探头应与前面板保持垂直。

用随附的固定件安装两个白色前灯、两个红色尾灯（各装在一个打印的 LED 适配件中）和圆形船型开关。走线时远离车轮和编码器码盘。

### 4. 安装 ESP32 和电机驱动板

如果没有弯头 USB 线，请**先烧录固件**（[第 6 步](#6-配置固件和台架测试)）。装上 L298N 后，直插 USB 插头就插不进 ESP32 了。

用两颗 M3 × 10 mm 螺丝把 ESP32 托架固定在前面两个长 PCB 凸台上。将 ESP32 自带的安装孔对准四根 2.2 mm 定位销放下。没有螺丝穿过 ESP32 电路板。

在四根托架柱上各放一个压片，凸舌朝下。转动压片，直到凸舌落入 ESP32 的角孔，然后用一颗 M3 × 10 mm 螺丝固定。任意压片都适合任意角。压片抵住固定柱后，与 PCB 之间约留 0.25 mm，因此拧紧也不会压坏电路板。如果电路板的孔与定位销对不上，不要硬装。

把四个打印的 2.4 mm 垫柱放到 L298N 凸台上，放上驱动板，每个角用一个打印的 0.8 mm 垫圈和一颗 M3 × 10 mm 螺丝固定（孔距 37 × 37 mm）。检查底部焊点不碰底盘，散热片周围有通风空间。

**电源板支架。** 用两根扎带穿过槽孔，将 5 V 转换器绑在支架正面，将分压洞洞板绑在背面。支架放在 L298N 散热片后方的底板上，位于两个后测速传感器之间、开关前方。合上的顶盖会把它压住，因此不需要螺丝。

<p align="center">
  <img src="../../docs/images/compact/step_04_electronics.jpg" alt="已装好电子元件" width="40%" />
</p>

### 5. 连接电源和信号线

<p align="center">
  <img src="../../docs/images/compact/wiring_diagram.png" alt="接线图" width="80%" />
</p>

使用 ESP32 上印的 GPIO 编号。所有 GND 必须连在一起。走线时远离齿轮、编码器码盘、螺丝尖和顶盖接缝，并扎好以防振动。

| 连接 | ESP32 GPIO | 说明 |
|---|---|---|
| L298N IN1 / IN2（左侧电机） | 13 / 12 | |
| L298N IN3 / IN4（右侧电机） | 15 / 2 | |
| L298N ENA / ENB | – | 保留使能跳线帽（速度由 IN 引脚上的 PWM 设定） |
| 编码器 左前 / 右前 | 35 / 22 | |
| 编码器 左后 / 右后 *(可选)* | 34 / 23 | |
| 超声波 TRIG | 5 | |
| 超声波 ECHO | 18 | 经过 10 k（ECHO → GPIO18）/ 20 k（GPIO18 → GND）分压 |
| 电池电压 | 4 | 从开关后的电池正极到 GPIO4 接 30 k，从 GPIO4 到 GND 接 10 k |
| 前灯 左 / 右 *(可选)* | 19 / 21 | 经过 NPN 三极管驱动 |
| 尾灯 左 / 右 *(可选)* | 25 / 26 | 经过 NPN 三极管驱动，需要修改固件 |

- **电机：**将两个左侧电机并联接到 OUT1/OUT2，两个右侧电机接到 OUT3/OUT4。如果某一侧转向相反，调换该侧的两根电机线。
- **编码器：**LM393 电路板用 **3.3 V** 供电，不要用 5 V。这样板载上拉电阻会让输出保持在 ESP32 可安全接受的电平。GPIO34 和 GPIO35 只能作输入，且没有内部上拉。
- **指示灯：**每个灯由一个 NPN 三极管开关（基极电阻 1 k，基极到发射极下拉 100 k）。切勿直接用 ESP32 引脚驱动面板指示灯。

#### 电源

电池正极 → 保护板/BMS → 保险丝 → 开关 → L298N 电机电源（+12 V 端子）。5 V 转换器接开关后的电池电压，为 ESP32 的 5 V/VIN 引脚和 HC-SR04 供电。L298N 的 5 V 端子不要接。除非线中串有 USB 电源隔离器，否则**切勿同时连接 USB 和外部 5 V 电源**。组装过程中请断开电池。

使用方案 A（2S）时，电机得到约 6.4 V，适合常见的 3–6 V TT 电机。使用方案 B 或 3S LiPo 时，电机得到约 10.6 V。请使用相应额定电压的电机，或在应用中限制速度。

### 6. 配置固件和台架测试

从[固件文件夹](../../firmware/README.zh-CN.md)获取固件，打开 `firmware/openbot/openbot.ino`。

1. 设置机器人类型，并保持手机模式开启：

   ```cpp
   #define OPENBOT DIY_ESP32
   #define NO_PHONE_MODE 0
   ```

2. 在 `#elif (OPENBOT == DIY_ESP32)` 代码块中**关闭指示器**。它们的默认引脚（5 和 18）在本方案中是超声波引脚：

   ```cpp
   #define HAS_INDICATORS 0   // default is 1
   ```

3. 在同一代码块中，按电池方案设置电池阈值。分压系数保持不变。

   | 方案 | `VOLTAGE_MIN` | `VOLTAGE_LOW` | `VOLTAGE_MAX` |
   |---|---:|---:|---:|
   | A – 2S 18650 或 2S LiPo | 6.0f | 6.8f | 8.4f |
   | B – 3S 18650 或 3S LiPo | 9.0f | 10.2f | 12.6f |

   应用使用这些阈值显示电量并发出低电量警告。若保留默认最小值 6.0 V，3S 电池会被显示为可用到每节 2 V。固件在低电压时从不切断电机，因此必须由保护板（方案 A 和 B）或 LiPo 电压报警器（方案 C）保护电芯。

4. 可选，在同一代码块中：

   ```cpp
   #define HAS_SPEED_SENSORS_BACK 1   // read the rear encoders as well
   #define HAS_LEDS_FRONT 1           // white lights on GPIO19/21 (default pins)
   #define HAS_LEDS_BACK 1            // red lights, and change their pins:
   const int PIN_LED_LB = 25;         // default 5
   const int PIN_LED_RB = 26;         // default 18
   ```

5. 在 Arduino IDE 中安装 **esp32 开发板包 2.0.17**。固件使用了 `ledcSetup`/`ledcAttachPin`，它们在 3.x 中已被移除。然后选择 **ESP32 Dev Module** 并上传。

**USB 接口。** ESP32 倒装在托架中，所以它的 USB 口在电路板下方，正对约 8.5 mm 外的 L298N 电源端子。装上驱动板后，只有弯头插头能插入，线缆从两块板之间向上引出。请在安装 L298N 之前烧录，或使用 90° 数据线。

GPIO 2、5、12 和 15 是启动配置引脚。如果全部接好后电路板无法启动或烧录，请检查这些引脚上接了什么，尤其是 GPIO12，它在复位时必须为低电平。

**台架测试**（拆下车轮、垫高底盘，串口监视器 115200 波特）：

| 发送 | 预期结果 |
|---|---|
| `f` | 返回包含 `DIY_ESP32` 的功能字符串 |
| *(观察输出)* | 合理的电池电压和超声波距离 |
| `c60,0`，然后 `c0,0` | 只有左侧向前转动 |
| `c0,60`，然后 `c0,0` | 只有右侧向前转动 |

车轮转动时，检查编码器计数是否变化。如果码盘摩擦、导线发热或 ESP32 重启，请立即停止。

安装安卓应用 **OpenBot Robot**。选择 **Bluetooth**，授予它请求的权限，搜索 `OpenBot` 并连接。如需有线连接，请看下一步。

### 7. 安装电池、手机支架和顶盖

<p align="center">
  <img src="../../docs/images/compact/step_06_phone_lid.jpg" alt="手机支架" width="32%" />
  <img src="../../docs/images/compact/lid_opening.jpg" alt="带堵头和穿线圈的顶盖开口" width="32%" />
  <img src="../../docs/images/compact/step_07_closed.jpg" alt="合上的车身" width="32%" />
</p>

把两颗 M3 螺母压入支架底座的螺母槽。用两颗 M3 × 25 mm 螺丝从下方把底座固定到顶盖前部的平台上。装上滑动夹，用橡皮筋或弹簧拉紧。把带壳的手机放进去，检查是否夹牢、摄像头视野无遮挡、没有按到任何按键。

**顶盖开口。** 顶盖有两个 1 × 2 凸点大小的开口，一个在手机平台前方，一个在后方。安装顶盖时，让手机平台朝向前方的超声波传感器。平台位于顶盖中心前方 16 mm 处；后部凸点区域用于放电池盒。在每个不用的开口中按入一个 `lid_plug`，它会补全凸点，开口底部 1 mm 宽的台阶可防止它掉下去。有线缆穿过的地方改用 `lid_grommet`：

- **手机有线连接：**用 90° USB 数据线从 ESP32 向上穿过前开口连到手机。在线中串入 USB 电源隔离器，防止手机和 5 V 转换器互相供电。在应用中选择 **USB** 而不是 Bluetooth。
- **电池盒（方案 A 和 B）：**电池线从后开口向下穿入。

如果使用方案 C，现在装入 LiPo 托盘。把导线从边沿理开，将顶盖平放在底盘上，把四颗 M3 × 10 mm 螺丝（不加垫圈）拧入四角的螺母。均匀拧紧直到贴合。顶盖不能翘起，支架不能晃动。方案 A 和 B 请把电池盒按到手机支架后方的凸点上。

台架测试通过后再装车轮。第一次请在空旷平坦的地面上慢速行驶，然后重新检查所有螺丝和手机夹。搬动机器人时请拿底盘，不要拿顶盖、手机支架或电池盒。

## 集成驱动的替代 PCB

<p align="center">
  <img src="../../docs/images/compact/pcb_option.jpg" alt="替代 PCB 的安装位置" width="40%" />
</p>

底盘也可以安装 OpenBot **PCB V2**（Arduino Nano + DRV8871 驱动），其安装孔距为 121.031 × 28.448 mm。这是另一套电气方案，**不能**直接替换 ESP32。它使用 `PCB_V2` 固件和 Nano 的接线方式。

- 不安装 ESP32 托架、四个压片、L298N 和电源板支架。
- 在四个长 PCB 凸台中装 4 个热熔螺母。加上传感器的 4 个和超声波的 2 个，**共 10 个热熔螺母**。
- 把电路板装在四个打印的 2.4 mm 垫柱上，每个孔用一个打印垫圈和一颗 M3 × 10 mm 螺丝。
- 硬件合计：**10 颗 M3 × 10**（顶盖 4、PCB 4、卡扣 2），**14 颗 M3 × 25**，14 颗螺母。

根据 [PCB V2 制造文件和物料清单](../diy/pcb/v2) 下单制作电路板，例如通过 [JLCPCB](https://jlcpcb.com/) 或 [PCBWay](https://www.pcbway.com/)。固定前请检查已焊元件的高度和焊点间隙。上述电池方案同样适用于这块电路板。

## 故障排除

| 现象 | 检查 |
|---|---|
| 编译时出现 `ledcSetup` / `ledcAttachPin` 相关错误 | esp32 内核是 3.x。请安装 2.0.17。 |
| USB 插头插不进 ESP32 | 被 L298N 挡住了。使用 90° 插头，或拆下驱动板后烧录。 |
| 接线后 ESP32 无法启动或烧录 | GPIO12/2/15/5 上有负载。断开 L298N 的输入后重试。 |
| 电机启动时 ESP32 重启 | 电池电量不足或已耗尽，或 5 V 转换器在 2S 电压下无法稳压。在负载下测量 5 V 电源。 |
| 超声波读数始终为 0 或不稳定 | `HAS_INDICATORS` 仍为 1，或回波分压缺失或接错 |
| 某个编码器没有计数 | 传感器没有接 3.3 V、码盘不在槽中，或码盘不是不透光的 |
| 应该直行时机器人在转弯 | 一侧的电机线接反了；调换 OUT1/OUT2 或 OUT3/OUT4 上的那对线 |
| 电池读数错误或应用显示“低” | 分压必须是 30 k（上）/ 10 k（下），且电压阈值必须与所选方案一致 |
| 顶盖盖不严 | 有导线被压在边沿上、LiPo 太厚，或螺母没有完全越过小筋进入槽内 |
| 电池盒在顶盖上松动 | 打印出的凸点偏小。稍微提高流量或检查打印机的尺寸精度，然后重新打印电池盒 |

## 视图

| 前 | 后 |
|---|---|
| ![前视图](../../docs/images/compact/teaser_front.jpg) | ![后视图](../../docs/images/compact/teaser_rear.jpg) |
| **顶部** | **底部** |
| ![顶视图](../../docs/images/compact/top_view.jpg) | ![底面](../../docs/images/compact/underside.jpg) |

渲染图未包含接线。电路板、电池座和 LiPo 电池以简化形状表示。

## 下一步

在手机上安装 [OpenBot Robot 应用](../../android/robot/README.zh-CN.md)，并按照[第 6 步](#6-配置固件和台架测试)连接机器人。
