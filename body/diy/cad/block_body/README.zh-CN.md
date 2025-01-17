# Blocky Body with Additional Space and Lego Support

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

这个机器人底盘提供了一些额外的高度，使得安装所有电子设备更加容易，并且顶部兼容乐高。这一版本具有与[常规底盘](../regular_body/)相同的结构完整性能力，同时增加了额外的功能，例如OpenBot外壳内的额外空间、用于游戏和学习的乐高兼容顶部，以及适合较小打印床尺寸的占地面积，同时保持保险杠。

![Block CAD](../../../../docs/images/block_cad.jpg)

## 部件

您需要打印底部部件和其中一个顶部部件：

- `block_body_bottom`([STL](block_body_bottom.stl), [STEP](block_body_bottom.step))：底部部件
- `block_body_top`([STL](block_body_top.stl), [STEP](block_body_top.step))：基本顶部部件
- `block_body_top_lego`([STL](block_body_top_lego.stl), [STEP](block_body_top_lego.step))：带有乐高兼容表面的基本顶部部件
- `block_body_top_big`([STL](block_body_top_big.stl), [STEP](block_body_top_big.step))：带有额外电子设备空间的大型顶部部件
- `block_body_top_lego`([STL](block_body_top_big_lego.stl), [STEP](block_body_top_big_lego.step))：带有乐高兼容表面的大型顶部部件

对于上述部件，您的打印板需要至少221x150mm。

## 打印设置

为了获得最佳效果，我们建议使用以下打印设置：

- 层高：0.2mm
- 墙线数：3（更多的墙线可以提高较大表面的结构完整性）
- 顶层：5
- 底层：4
- 填充率：25%
- 填充图案：同心（这种设置似乎可以节省时间和塑料）
- 打印速度：50mm/秒
- 生成支撑：是
- 支撑图案：同心
- 支撑密度：15%
- 启用支撑边缘：是
- 构建板粘附类型：无

祝您机器人制作愉快！

![Block Body](../../../../docs/images/block_body.jpg)