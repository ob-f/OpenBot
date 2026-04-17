# Slim Body

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

一些3D打印机的构建体积太小，无法打印全尺寸的OpenBot机身。
此文件夹包含OpenBot机身的瘦身版本。
当部件旋转45度时，可以在220mmx220mm的构建板上打印。

![Slim Body](../../../../docs/images/slim_body.jpg)

## 部件

1) `slim_body_bottom` ([STL](slim_body_bottom.stl), [STEP](slim_body_bottom.step))
2) `slim_body_top` ([STL](slim_body_top.stl), [STEP](slim_body_top.step))

为了使其适应，您可能需要调整以下设置以获得最大的打印区域。

- 将*构建板粘附类型*设置为“无”（边缘、裙边和筏子会增加打印的整体尺寸）
- 禁用预热块（在*构建板*部分）
- 禁用第二个挤出机（如果您的打印机有的话）

如果您有一点额外的空间（223mmx223mm），您还可以打印`slim_body_top_rim` ([STL](slim_body_top_rim.stl), [STEP](slim_body_top_rim.step))。它有一个稍大的边缘，使得更容易取下顶部。