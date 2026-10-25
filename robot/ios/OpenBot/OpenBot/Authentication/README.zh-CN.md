## Google Firebase

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Google Firebase 是一个移动和 Web 应用开发平台，提供多种服务和工具，帮助开发者快速高效地构建高质量的应用。它包括实时数据库、用户身份验证、托管、云存储等众多功能，全部集成在一个平台中。Firebase 为开发者提供了便捷且可扩展的后端基础设施管理方案，让他们专注于打造出色的用户体验。

- ### Firebase Google 登录身份验证

  Firebase Google 登录身份验证是 Firebase 平台的一项功能，让用户可以使用 Google 帐号登录移动应用或 Web 应用。用户无需记住和管理单独的登录凭据，就能安全便捷地访问应用。Firebase 负责整个身份验证过程，从通过 Google 验证用户身份，到提供可用于在应用内个性化用户体验的唯一用户 ID。它还包括双重身份验证等额外的安全措施，保护用户帐号免受未经授权的访问。

- ### 用途
  在本应用中，我们使用 Firebase 进行 Google 登录身份验证，以访问上传到 Google Drive 的 [OpenBot Playground](../../../../../open-code/README.zh-CN.md) 项目。

- ### 注意
  如果您打算克隆此 iOS 应用并在自己的设备上构建，还需要为 [OpenBot Playground](https://www.playground.openbot.org/) Web 应用[设置 Firebase](../../../../../open-code/README.zh-CN.md)。这是因为 iOS 应用会从用户的 Google Drive 中获取由 Firebase Google Drive 服务创建的文件。iOS 应用和 Web 应用必须使用同一个 Firebase 项目，Google Drive 服务才能正常工作。

  本仓库中提交的 `GoogleService-Info.plist` 文件是带有虚假密钥和标识符的**占位文件**。
  在您按下文所述用从自己的 Firebase 项目下载的真实文件替换它之前，Google 登录和其他 Firebase 功能都无法使用。

- ### 前提条件
  要将 Firebase 集成到 OpenBot iOS 应用中以使用 Google 登录，需要以下几项：
- **Google 帐号:** 要通过 Firebase 使用 Google 登录，您必须拥有 Google 帐号。如果没有，可以在此[创建](https://accounts.google.com/signup)免费的 Google 帐号。
- **Cocoapods:** Swift 和 Objective-C 项目的依赖管理工具。要安装 Firebase SDK，系统中需要安装 Cocoapods。

### 设置您的 Firebase 项目

- 打开 [Firebase 控制台](https://console.firebase.google.com/)，按以下步骤创建`新项目`：
    1. 点击“Create Project”按钮。
    2. 输入 Firebase 项目名称。
    3. 点击“Next”，如果不想使用 Google Analytics 服务，可以将其停用。
    4. 点击“Create Project”按钮。

    <p>
    <img src="../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
    <img src="../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width="40%"/>
    <img src="../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
    </p>

- 要在 Firebase 项目中添加新的 iOS 应用，请执行以下操作：
    1. 在 Firebase 项目中点击 iOS 图标。
    2. 输入应用的 Bundle ID。它应与 Xcode 项目中使用的 Bundle ID 相同。
    3. 输入应用的昵称。
    4. 点击“Register app”按钮完成操作。
    5. 下载 GoogleService-Info.plist 文件并将其添加到 Xcode 项目中。该文件包含应用使用 Firebase 所需的配置信息。

       <p>
       <img src="../../../../../docs/images/firebase_add_ios_app.jpg" alt="Add IOS Application" width="47.5%"/>
       <img src="../../../../../docs/images/firebase_bundle_id.jpg" alt="Bundle ID Name" width="30.5%"/>
       </p>

    6. 从新的 `GoogleService-Info.plist` 中复制 `REVERSED_CLIENT_ID` 的值，粘贴到 `Info.plist` 中的
       `CFBundleURLSchemes` 条目（位于 `CFBundleURLTypes` 下）。在此值与您自己的 Firebase 项目一致之前，Google 登录无法使用。
    7. 点击 `Next` 按钮，跳过第三步（Add Firebase SDK）和第四步（Add initialization code），因为本项目已经完成了这些步骤。
    8. 继续前往 Firebase 控制台，配置您想在 iOS 应用中使用的 Firebase 服务。

   <p>
     <img src="../../../../../docs/images/firebase_google_service_info_plist.jpg" alt="Add IOS Application" width="35%"/>
     <img src="../../../../../docs/images/firebase_continue_to_console_ios.jpg" alt="Bundle ID Name" width="35%"/>
   </p>

- 要为 Firebase 项目`启用 Google 登录身份验证`，请按以下步骤操作：
    1. 打开 Firebase 控制台并选择您的项目。
    2. 点击左侧`侧边栏菜单`中的 `All products` 选项。
    3. 点击 `Authentication`。
    4. 点击 `Get Started` 按钮。
    5. 点击 `Google 图标`。
    6. 点击`开关按钮`以`启用` Google 登录身份验证。

    <p>
    <img src="../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="44.5%"/>
    <img src="../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="48%"/>
    </p>
    <p>
    <img src="../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="60%"/>
    <img src="../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="31.5%"/>
    </p>

### 设置 iOS 项目

Firebase SDK 通过 CocoaPods 安装。仓库中已经包含带有所有所需 pod 的 Podfile。在 Mac 上打开“终端”应用，进入 `robot/ios/OpenBot`，然后运行：

```
pod install
```

然后在 Xcode 中打开 `OpenBot.xcworkspace`（而不是 `OpenBot.xcodeproj`）。

### 故障排除

Firebase 配置过程中可能出现的常见问题及解决方法：

```shell
1. CocoaPods 安装错误：如果安装 CocoaPods 时遇到问题，例如 pod install 无法运行或没有安装正确版本的 Firebase SDK，请尝试以下方法：
```

- 运行 sudo gem install cocoapods 更新 CocoaPods。
- 删除 Podfile.lock 文件，然后重新运行 pod install。
- 确认已正确将 Firebase SDK pod 添加到 Podfile 中，并使用 Firebase 控制台中指定的确切版本。
```shell
2. 重新安装 Pods（遇到构建或包相关问题时）
```
- pod deintegrate 
- rm -rf Pods
- rm -rf Podfile.lock
- pod install

```shell
3. Firebase 配置错误：如果无法正确配置 Firebase，例如无法在应用中初始化 Firebase，请尝试以下方法：
```

- 仔细检查是否正确完成了本文档“设置 iOS 项目”一节中的所有步骤。
- 确认已将 Firebase 配置文件（GoogleService-Info.plist）正确添加到 Xcode 项目中。
- 确认 Firebase SDK 已正确添加到 Xcode 项目中，并在代码中导入。
- 确认 Firebase 控制台中的 Firebase 项目设置正确，包括正确的 Bundle ID 和其他项目设置。
- 与其他库冲突：如果项目中与其他库发生冲突，例如依赖冲突或 SDK 版本不兼容，请尝试以下方法：
- 确认 Podfile 中指定了正确的 Firebase SDK 版本。
- 尝试将其他库更新到最新版本，看看问题是否解决。
- 检查是否存在冲突的依赖或库版本，并通过删除或更新冲突的库来解决。
