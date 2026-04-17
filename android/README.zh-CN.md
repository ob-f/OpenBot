# Android Apps

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## 功能

点击下面的链接阅读应用程序的功能。

- [机器人应用](robot/README.md)
- [控制器应用](controller/README.md)

## 安装应用程序

获取应用程序的最简单方法是使用相应的二维码直接下载到手机上。如果你在手机浏览器上，也可以直接点击二维码。然后你可以在手机上打开apk并[安装](https://www.lifewire.com/install-apk-on-android-4177185)它。请注意，apk仅使用调试密钥签名。

<table style="width:100%;border:none;text-align:center">
  <tr>
    <td>  <a href="https://app.openbot.org/robot" target="_blank">
    <img alt="🤖 App" width="50%" src="../docs/images/robot_app_qr_code.png" />
  </a>
    </td>
    <td>
  <a href="https://app.openbot.org/controller" target="_blank">
    <img alt="🎮 App" width="50%" src="../docs/images/controller_app_qr_code.png" />
  </a>
      </td>
  </tr>
  <tr>
    <td>🤖 App</td>
    <td>🎮 App</td>
  </tr>
</table>

或者，你可以从任何[发布](https://github.com/ob-f/OpenBot/releases)的资产中下载apk。如果你想要来自主分支的最新应用程序，你也可以从构建工件[这里](https://github.com/ob-f/OpenBot/actions?query=workflow%3A%22Java+CI+with+Gradle%22)下载。请注意，它可能不稳定。如果你以后想对应用程序进行更改，请按照以下步骤编译应用程序并将其部署到手机上。

## 构建应用程序

### 先决条件

- [Android Studio Electric Eel | 2022.1.1或更高版本](https://developer.android.com/studio/index.html)用于构建和安装apk。
- Android设备和最低API 21的Android开发环境。
- 目前，我们使用API 33作为编译SDK和API 32作为目标SDK。它应该会自动安装，但如果没有，你可以手动安装SDK。进入Android Studio -> Preferences -> Appearance & Behaviour -> System Settings -> Android SDK。确保API 33已选中并点击应用。

![Android SDK](../docs/images/android_studio_sdk.jpg)

### 构建过程

1. 打开Android Studio并选择*打开现有的Android Studio项目*。
2. 选择OpenBot/android目录并点击确定。
3. 如果你想安装[OpenBot应用](app/README.md)，请确保选择*app*配置。如果你想安装[控制器应用](controller/README.md)，请选择*controller*配置。如有必要，确认Gradle同步。要手动执行Gradle同步，请点击gradle图标。
  ![Gradle Sync](../docs/images/android_studio_bar_gradle.jpg)
4. 连接你的Android设备，并确保在[开发者选项](https://developer.android.com/studio/debug/dev-options)中启用USB调试。根据你的开发环境，可能需要[进一步的步骤](https://developer.android.com/studio/run/device)。现在你应该可以在顶部的导航栏中看到你的设备。
  ![Phone](../docs/images/android_studio_bar_phone.jpg)
5. 点击运行按钮（绿色箭头）或从顶部菜单中选择运行 > 运行'android'。你可能需要使用构建 > 重建项目来重新构建项目。
  ![Run](../docs/images/android_studio_bar_run.jpg)
6. 如果它要求你使用即时运行，请点击*继续不使用即时运行*。

### 故障排除

#### 版本

如果你收到类似`项目正在使用不兼容版本（AGP 7.4.0）的Android Gradle插件。最新支持的版本是AGP 7.3.0`的消息，你需要升级Android Studio或降级你的gradle插件。你可以在[这里](https://developer.android.com/studio/releases/gradle-plugin#android_gradle_plugin_and_android_studio_compatibility)阅读更多关于Android Studio和gradle插件之间版本兼容性的信息。