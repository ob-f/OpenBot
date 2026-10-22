# 手机支架

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

[DIY](../diy/README.zh-CN.md)、[RC truck](../rc_truck/README.zh-CN.md) 和 [Compact](../compact/README.zh-CN.md) 车身通用的两件式手机支架。滑动手机夹把手机压在托架上，由橡皮筋或轻弹簧拉紧。可选的可倾斜版本能让手机保持 0°、15° 或 30°，使摄像头朝上，例如拍到站在机器人前面的人。把支架掉转 180° 安装，摄像头则会朝下。

## 零件

| 零件 | 数量 | 说明 |
|---|---:|---|
| `phone_mount_bottom` ([STL](phone_mount_bottom.stl), [STEP](phone_mount_bottom.step)) | 1 | 底座和立柱；用两颗 M3 × 25 mm 螺丝和螺母固定到车身 |
| `phone_mount_top` ([STL](phone_mount_top.stl), [STEP](phone_mount_top.step)) | 1 | 滑动手机夹；两种底座通用 |
| `phone_mount_tilt_base` ([STL](phone_mount_tilt_base.stl), [STEP](phone_mount_tilt_base.step)) | 1 *（可选）* | 可倾斜支架：带两片铰链侧板的底座，替代 `phone_mount_bottom` |
| `phone_mount_tilt_column` ([STL](phone_mount_tilt_column.stl), [STEP](phone_mount_tilt_column.step)) | 1 *（可选）* | 可倾斜支架：在两片侧板之间转动的立柱 |

## 打印

文件已按打印方向摆放：所有零件按原样竖立打印。PLA、0.2 mm 层高、4 圈外墙、30 % 填充效果良好。`phone_mount_tilt_base` 无需支撑。`phone_mount_bottom`、`phone_mount_top` 和 `phone_mount_tilt_column` 只在挂橡皮筋凸柱的凸缘下方需要支撑；使用树状支撑并关闭“移除小悬垂”，那里只会生成一个小树状支撑，可从槽的开口背面取出。凸缘环绕整个凸柱，因此橡皮筋即使松弛也不会滑落。

## 可倾斜手机支架

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="可倾斜手机支架的铰链" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="可倾斜手机支架处于 0°、15° 和 30°，上排为正常安装，下排为掉转安装" width="64%" />
</p>

可倾斜底座与标准底座使用相同的两个安装孔，也能放进相同的凹槽：DIY 顶板、RC truck 车架和摄像头垫高块，以及 Compact 顶盖。侧板从底座底面以上 5 mm 处开始，因此不会碰到这些凹槽的边缘。手机比标准支架高 10.5 mm。在 30° 时立柱顶部会向后移动约 22 mm（掉转安装时向前），请确认你的车身上没有东西挡住。底座也可以掉转 180° 安装，这时立柱向前倾斜：把手机屏幕朝向立柱放入，摄像头就会朝前并向下看。

除了安装螺丝外，还需要三颗 M3 × 10 mm 盘头螺丝和五个 M3 热熔螺母（外径 4.6 mm，长 5.7 mm）。黄铜螺纹不会因频繁调整角度而磨损。

1. 在立柱中压入三个 M3 热熔螺母（外径 4.6 mm，长 5.7 mm）：后部转轴孔的两端各一个，前面只在一侧开口的锁定孔一个。
2. 在底座底面的两个孔中压入两个 M3 热熔螺母。通过与标准底座相同的两个安装孔，从下方用螺丝把底座固定到车身上；螺丝应伸入底座约 5 mm（穿过 5 mm 厚的 Compact 顶盖时用 M3 × 10 mm）。
3. 把立柱放到两片侧板之间，让开口的锁定孔朝向有四个孔的侧板。从两侧各用一颗 M3 × 10 mm 螺丝穿过侧板拧入转轴热熔螺母，拧到立柱能转动且没有晃动为止。在两颗转轴螺丝上各涂一滴中等强度（蓝色）螺纹胶，防止松动。
4. 倾斜立柱，直到它的锁定热熔螺母与侧板上三个孔中的一个对齐：最下面的孔对应 0°，中间的对应 15°，最上面的对应 30°。用第三颗 M3 × 10 mm 螺丝锁定。
5. 像标准支架一样装上 `phone_mount_top` 和橡皮筋。
