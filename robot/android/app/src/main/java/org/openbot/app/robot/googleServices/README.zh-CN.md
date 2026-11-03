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
  在本应用中，我们使用 Firebase 进行 Google 登录身份验证，以访问上传到 Google Drive 的 [OpenBot Playground](../../../../../../../../../../../open-code/README.zh-CN.md) 项目。如果您打算克隆此 Android 应用并在自己的设备上构建，需要设置您自己的 [Firebase 项目](#设置您的-firebase-项目)，因为 Google 登录身份验证需要应用的 SHA-1 密钥。
  
  您还需要为 [OpenBot Playground](https://www.playground.openbot.org/) Web 应用[设置 Firebase](../../../../../../../../../../../open-code/README.zh-CN.md)。这是因为 Android 应用会从用户的 Google Drive 中获取由 Firebase Google Drive 服务创建的文件。Android 应用和 Web 应用必须使用同一个 Firebase 项目，Google Drive 服务才能正常工作。
  
  如果您不想使用 OpenBot Playground 服务，则无需设置 Firebase 身份验证或 Google 登录身份验证。

  本仓库中提交的 `google-services.json` 文件（`robot/android/app/google-services.json` 和
  `robot/android/app/src/main/assets/google-services.json`）是带有虚假密钥和标识符的**占位文件**。
  在您按下文所述用从自己的 Firebase 项目下载的真实文件替换它们之前，Google 登录和其他 Firebase 功能都无法使用。

### 前提条件
要将 Firebase 集成到 OpenBot Android 应用中以使用 Google 登录，需要以下几项：
- **Google 帐号:** 要通过 Firebase 使用 Google 登录，您必须拥有 Google 帐号。如果没有，可以在此[创建](https://accounts.google.com/signup)免费的 Google 帐号。
- **Firebase 帐号:** 您需要一个 Firebase 帐号，并为您的 Android 应用创建一个新的 Firebase 项目。可以在 [Firebase 控制台](https://console.firebase.google.com/)中创建新项目。
****

### 设置您的 Firebase 项目

- 打开 [Firebase 控制台](https://console.firebase.google.com/)，按以下步骤创建`新项目`：
  1. 点击“Create Project”按钮。
  2. 输入 Firebase 项目名称并点击“Next”按钮。
  3. 如果不想使用 Google Analytics，可以将其停用。
  4. 点击“Create Project”按钮。
<p>
<img src="../../../../../../../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
<img src="../../../../../../../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width=40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
</p>

- 要在 Firebase 项目中添加新的 Android 应用，请执行以下操作：
  1. 在 Firebase 项目中点击 Android 图标。
  2. 输入 Android 应用的`包名`。它在 Firebase 中必须是`唯一`的。
  3. 输入应用的昵称。
  4. Firebase Google 登录服务**必须**提供 **SHA-1 密钥**，用于签名应用的 APK。请在相应字段中输入。
  5. 点击 `Register` 按钮。
  

- `SHA-1` 指纹/密钥是`应用签名证书`的唯一标识符，Firebase 在您的应用与 Firebase 服务器通信时用它来验证应用的真实性。如果您打算在应用中使用 Firebase 身份验证，需要提供用于签名应用`发布版本`的`签名证书`的 `SHA-1` 密钥。
  
<p>
<img style="width: 47%" src="../../../../../../../../../../../docs/images/firebase_add_android_app.jpg" alt="Add Android Application" width="40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_package_name.jpg" alt="Package Name" width="36%"/>
</p>

- 要获取 `SHA-1` 密钥，可以使用 `Java SDK` 自带的命令行工具 `keytool`。以下是在 `Mac` 和 `Windows` 上的用法：
  

  - **在 Mac 上**
    ```shell
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
    ```
    此命令会列出位于 `~/.android/debug.keystore` 的调试签名证书的详细信息。`-alias` 参数指定用于标识调试签名证书的别名，`-storepass` 和 `-keypass` 参数分别指定密钥库和密钥的密码。
  

  - **在 Windows 上**
    ```shell 
    keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    ```
    此命令与 Mac 上的命令类似，但使用`不同的路径`来定位 debug.keystore 文件。`%USERPROFILE%` 是指向当前用户配置文件目录的系统环境变量，该目录中包含存放 `debug.keystore` 文件的 `.android` 目录。

- 下载 `google-services.json` 文件，并将其`添加`到应用的 `app 目录`（`robot/android/app/google-services.json`）以及 `assets 目录`（`robot/android/app/src/main/assets/google-services.json`）。
- 点击“Next”按钮。第三步可以跳过，因为本项目的 gradle 文件中已经**添加了 Firebase SDK**。
- 继续前往 Firebase 控制台，配置您想在 Android 应用中使用的 Firebase 服务。

<p>
<img style="width: 40%" src="../../../../../../../../../../../docs/images/firebase_google_service_json_file.jpg" alt="Google services json file" width="41%"/>
<img src="../../../../../../../../../../../docs/images/firebase_continue_to_console.jpg" alt="Continue to console" width="40.2%"/>
</p>

- 要为 Firebase 项目`启用 Google 登录身份验证`，请按以下步骤操作：
  1. 打开 Firebase 控制台并选择您的项目。
  2. 点击左侧`侧边栏菜单`中的 `All products` 选项。
  3. 点击 `Authentication`。
  4. 点击 `Get Started` 按钮。
  5. 点击 `Google 图标`。
  6. 点击`开关按钮`以`启用` Google 登录身份验证。

<p>
<img style="width:36%" src="../../../../../../../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="39%"/>
</p>
<p>
<img src="../../../../../../../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="26.1%"/>
</p>


- 如果您在设置过程中`跳过了添加 SHA-1 密钥的步骤`，之后`仍可以添加`：

  - 打开 `Firebase 控制台`并选择`您的项目`。
  - 点击`左上角`的`齿轮图标`，选择 `Project settings`。
  - 在 `Your apps` 部分，选择要`添加 SHA-1 密钥`的 `Android 应用`。
  - `向下滚动`到 `SHA certificate fingerprints` 部分，点击 `Add fingerprint`。
  - 输入应用签名证书的 `SHA-1 密钥`。
  - 点击 `Save`，将 SHA-1 密钥添加到 Firebase 项目中。

<p>
<img style="width: 44%;" src="../../../../../../../../../../../docs/images/firebase_gear_icon.jpg" alt="Gear icon" width="30%"/>
<img style="width: 53%" src="../../../../../../../../../../../docs/images/firebase_add_sha1.jpg" alt="Add SHA-1" width="43%"/>
</p>

- 如果您在添加 SHA-1 密钥之前`已经实现了 Firebase 身份验证`，可能需要`更新`应用的`配置`：用 Firebase 项目设置中`更新后的 google-services.json` 文件`替换`项目目录中的 `google-services.json` 文件。

<p>
<img src="../../../../../../../../../../../docs/images/firebase_updated_google_service_json_file.jpg" alt="Updated Google service json file" width="40%"/>
</p>
