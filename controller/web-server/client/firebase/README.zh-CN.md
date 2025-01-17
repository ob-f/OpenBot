## Firebase Google 登录认证

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

- #### 使用方法
  在我们的网页应用中，我们使用 Firebase 进行 Google 登录认证，以识别每个独特的客户端，并防止机器人应用和网页服务器之间的交叉连接。如果你克隆此项目并在你的设备上运行，你必须设置你自己的 Firebase 项目，因为登录认证需要 Firebase 配置。
- #### 关于 Google 登录
  Firebase Google 登录认证是 Firebase 平台的一个功能，允许用户使用他们的 Google 凭证登录移动或网页应用。此服务为用户提供了一种安全便捷的方式来访问应用，而无需记住和管理单独的登录凭证。Firebase 管理整个认证过程，从使用 Google 验证用户身份到提供一个唯一的用户 ID，该 ID 可用于个性化用户在应用中的体验。此功能还包括额外的安全措施，如双因素认证，以帮助保护用户账户免受未经授权的访问。

**注意** - 请按照 OpenBot playground [文档](../../../../open-code/src/services/README.zh-CN.md) 设置你的 Firebase 项目并启用 Google 认证。目前无需启用 Google Drive API。

### 设置环境变量

使用环境变量 当使用 Firebase 认证时，你可能需要存储敏感信息，如 API 密钥、数据库凭证和其他秘密信息。为了安全地存储这些信息，你可以使用环境变量将这些信息存储在代码之外。请按照以下步骤操作。

1. 在 web-server 中创建一个名为 .env 的新文件。

   <img src="../../images/firebase_web_server_env_variable.png" width="30%"/>

2. 将以下环境变量添加到 .env 文件中，这些变量将在 authentication.js 文件中使用。
      ```bash
      REACT_APP_FIREBASE_API_KEY=<REACT_APP_FIREBASE_API_KEY>
      SNOWPACK_PUBLIC_FIREBASE_API_KEY=<SNOWPACK_PUBLIC_FIREBASE_API_KEY>
      SNOWPACK_PUBLIC_AUTH_DOMAIN=<SNOWPACK_PUBLIC_AUTH_DOMAIN>
      SNOWPACK_PUBLIC_PROJECT_ID=<SNOWPACK_PUBLIC_PROJECT_ID>
      SNOWPACK_PUBLIC_STORAGE_BUCKET=<SNOWPACK_PUBLIC_STORAGE_BUCKET>
      SNOWPACK_PUBLIC_MESSAGING_SENDER_ID=<SNOWPACK_PUBLIC_MESSAGING_SENDER_ID>
      SNOWPACK_PUBLIC_APP_ID=<SNOWPACK_PUBLIC_APP_ID>
      SNOWPACK_PUBLIC_MEASUREMENT_ID=<SNOWPACK_PUBLIC_MEASUREMENT_ID>
   ```