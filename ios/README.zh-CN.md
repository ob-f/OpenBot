# iOS 应用程序 - 测试版发布

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

- [机器人应用](OpenBot/README.md)
- 控制器应用（即将推出）

## 安装应用程序
目前，在您的手机上运行 iOS 应用程序的唯一方法是通过开发者构建，如下节所述。

## 构建应用程序

### 先决条件

- [Xcode iOS 13 或更高版本](https://developer.apple.com/xcode/) 用于构建和安装应用程序。
- 系统上安装了 [Cocoapods](https://cocoapods.org/)。
- 目前，我们使用的 iOS 部署目标版本是 15.5。
- 项目配置为“自动管理签名”，因此您可以配置自己的独立帐户来构建应用程序 -
  <img alt="iOS App" width="100%" src="../docs/images/ios_automatically_manage_signing.png" />
- 要配置自己的团队，请通过 XCode > 设置 > 帐户或直接从上面的团队菜单中添加您的 iCloud 帐户。
- 运行 iOS 13 或更高版本的 iOS 设备。[支持设备列表](https://support.apple.com/en-in/guide/iphone/iphe3fa5df43/ios)。
- 在您的 iOS 设备上激活 [开发者模式](https://developer.apple.com/documentation/xcode/enabling-developer-mode-on-a-device)。
- 添加的 iCloud 帐户应被您的 iOS 设备 [信任](https://developer.apple.com/forums/thread/685271)。

### 构建过程

1. 打开 XCODE 并选择 *打开项目或文件*。
2. 要安装 [OpenBot 应用](OpenBot/README.md)，请确保选择 *OpenBot* 配置。
   <img alt="iOS App" width="100%" src="../docs/images/ios_openbot_configuration.png" />
3. 从可用设备列表中选择您的设备。
   <img alt="iOS App" width="100%" src="../docs/images/ios_device_selection.png" />
4. 通过点击 Xcode 屏幕左上角的 ▶️ 图标在设备上运行应用程序。