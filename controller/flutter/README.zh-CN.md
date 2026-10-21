# Flutter 控制器应用

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

这个控制器应用程序作为 [OpenBot](https://www.openbot.org) 车辆的 `远程控制器`，类似于 BT 控制器（例如 PS3/4 或 Xbox）。它运行在另一台 Android/iOS 设备上，除了控制功能外，还支持实时视频/音频流。

## 安装应用

在 Android 上，从最新[版本](https://github.com/ob-f/OpenBot/releases)的资源中下载 `Controller-<version>.apk`，然后在手机上[安装](https://www.lifewire.com/install-apk-on-android-4177185)。

> **现有用户注意：** 此应用取代旧版 Android 控制器应用，成为每个版本发布的控制器 APK。由于两个应用使用不同的包 ID（`org.openbot.app.controller` 与 `org.openbot.controller`），安装此 APK 不会更新旧版应用，而是作为单独的应用与其并存。切换完成后，你可以卸载旧版控制器。

## 入门指南
首先在你的系统上安装 [Flutter](https://flutter.dev/)。选择适合你操作系统的下载选项，包括 Windows、macOS、Linux 和 ChromeOS。请按照官方 Flutter 安装指南进行详细安装：[Flutter 安装指南](https://docs.flutter.dev/get-started/install)

### 使用终端
- 成功安装 Flutter 后，打开你的 **终端** 或 **命令提示符**。
- 将当前目录更改为存储 OpenBot 项目的位置，然后导航到 `OpenBot/controller/flutter`。
- 使用以下命令从终端运行 Flutter 应用程序。

  #### 安装依赖项：
    ```bash
     flutter pub get 
    ```
    运行项目：
    ```bash
     flutter run
    ```
    如果遇到任何问题，请运行以下命令：
    ```bash
     flutter doctor
    ```
### 使用编辑器
- 按照官方 Flutter 指南设置编辑器：[设置编辑器](https://docs.flutter.dev/tools/android-studio)
- 确保你的编辑器已配置为 Flutter 开发。安装任何所需的插件或扩展，按照 Flutter 文档中的编辑器特定说明进行设置，以获得最佳开发体验。

- 设置完成后，在编辑器中打开项目，它将显示如下图所示。

  <p float="left">
    <img src="../../docs/images/android_editor.jpg" width="50%" />
  </p>

- 请按照上述类似的指示运行 Flutter 在终端中的命令，并直接使用 ``run`` 按钮进行后续运行。

  <p float="left">
    <img src="../../docs/images/run_editor.jpg" width="50%" />
  </p>

## 构建签名的发布版本（Android）
发布版本（`flutter build apk --release` / `flutter build appbundle --release`）使用 `android/key.properties` 中引用的密钥库签名。该文件已被 **gitignore**，不会提交到仓库——每位开发者都需要创建自己的本地副本。调试版本（`flutter build apk --debug`、`flutter run`）不需要这些。

### 1. 生成密钥库（一次）
```bash
keytool -genkey -v -keystore ~/release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
```
请将生成的 `.keystore`/`.jks` 文件及其密码妥善保存在仓库之外——一旦丢失，就无法再以相同的应用签名发布更新。

### 2. 创建 `android/key.properties`
`android/app/build.gradle` 会读取 `controller/flutter/android/key.properties`，因此请在该位置创建此文件，内容如下：
```properties
storePassword=<your keystore password>
keyPassword=<your key password>
keyAlias=upload
storeFile=/absolute/path/to/release.keystore
```
`storeFile` 必须是指向第 1 步生成的密钥库的绝对路径（或可从 `controller/flutter/android/` 解析的路径）。

### 3. 构建
```bash
cd controller/flutter
flutter build appbundle --release   # 用于上传到 Play 商店（.aab）
# 或
flutter build apk --release         # 用于生成签名的可安装 APK
```
如果存在 `key.properties`，`app/build.gradle` 会自动加载它并用于 `release` 签名配置；如果缺少该文件，发布配置没有签名值，签名将失败（调试版本不受影响）。

## 连接

控制器手机和机器人手机必须连接到同一 Wi-Fi 网络，或其中一部手机连接到另一部手机的热点。无需互联网连接。

启动控制器应用程序时，它会立即尝试连接到机器人并显示以下屏幕：

<p float="left">
  <img src="../../docs/images/flutter_controller_home.jpg" width="50%" />
</p>

要将控制器连接到机器人，请将机器人的控制模式设置为 **Phone**。
例如，在 `FreeRoamFragment` 中，手机模式如下激活：

<p float="left">
  <img src="../../docs/images/phone_selection.gif" width="50%" />
</p>

连接后，控制器应用程序将如下所示：

<p float="left">
  <img src="../../docs/images/flutter_controller_connected.jpg" width="50%" />
</p>

在这里，你可以选择通过倾斜手机或使用屏幕上的控件来驾驶机器人。

***注意：*** 这应该足以连接，但如果在 30 秒后仍无法建立连接，请将机器人应用程序中的 `Control` 设置切换到 `Gamepad`，然后再切换回 `Phone` 以重新启动连接。如果仍然失败，请退出控制器应用程序并重新启动。再次在机器人应用程序中切换控制模式。

## 操作

### 屏幕控件

此模式允许用户通过 `双驱动` 模式中的两个滑块控制机器人车。你可以通过在每侧上下移动滑块拇指来左转/右转。当拇指移动到滑块中心以上/以下时，每侧的轮子将向前/向后转动。

<p float="left">
  <img src="../../docs/images/flutter_controller_dual_drive_mode.jpg" width="50%" />
</p>

- ``指示灯``：你还可以通过点击屏幕左上角的箭头来设置左/右转向指示灯 <img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/> <img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/>。

- ``切换摄像头``：在前置和后置摄像头模式之间切换。
- ``静音``：启用/禁用音频传输。
- ``镜像视图``：镜像视频流。

### 倾斜驾驶

控制器还可以使用其加速度计运动传感器来驾驶机器人。如果选择此选项，控制器将进入全屏（Zen）模式，仅显示视频和 `刹车` 和 `加速` 踏板。要退出此模式，请双击屏幕。

以下是 `倾斜模式` 屏幕的图片：

<p float="left">
  <img src="../../docs/images/flutter_controller_tilt_mode.jpg" width="50%" />
</p>

使用 `加速` 和 `刹车` 按钮前进/后退。

- 按下 `加速` 按钮，机器人将在 2 秒内加速到全速。当你松开按钮时，机器人将减速至停止（停止速度设置为最大速度的 0%，可以调整）。
- 按下 `刹车` 按钮，机器人将立即停止。如果我们再按住刹车一秒钟，机器人将开始倒退，直到在一秒钟内达到最大倒车速度。当我们松开刹车时，机器人将停止。
- 通过左右倾斜控制器手机来转向机器人。

### 控制器设置界面

下面是 `Controller Settings` 界面的图片：

- 控制器选择：使用图标在不同的控制器类型（例如手机、摇杆）之间切换。
 
- 服务器：显示当前服务器状态，并提供下拉菜单选择或更换服务器。
 
- 噪声开关：通过开关启用或停用噪声模拟。

<p float="left">
  <img src="../../docs/images/flutter_controller_setting.jpg" width="50%" />
</p>

这里有一个控制器应用程序的 [技术概述](../../docs/technical/OpenBotController.pdf)。
