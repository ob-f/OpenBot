# OpenBot Web 控制器

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## 术语

以下是本文档中将使用的一些术语：

* ```机器人, bot``` - 这是运行在 [OpenBot](https://www.openbot.org/) 车辆上的安卓软件。
* ```服务器``` - 这是项目的服务器部分，即网络服务器。
* ```客户端, UI``` - 这是项目的客户端部分，运行在浏览器中。

## 简介

该项目在云服务器上远程操作，作为 [OpenBot](https://www.openbot.org/) 车辆的控制器。软件由两个组件组成：服务器和客户端。服务器是部署在远程环境中的云应用程序。客户端组件直接在网页浏览器中运行。以下是浏览器的截图：

<img src="../../controller/web-server/images/openbot_controller.jpg" width="50%"/>

## 入门指南

你可以在 PC、RaspberryPi 类型设备甚至 [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/) 设备上运行此软件，这些设备支持 ```Node.js``` 环境。首先确保你已安装 [Node.js](https://nodejs.org/)，版本为 10 或更新版本。检查版本：

    node --version

软件位于 OpenBot 项目的 ```/controller/web-server``` 目录中。从 [github](https://github.com/ob-f/OpenBot) 检出代码后，进入此目录并运行以下命令：

    npm install
    npm start

最后一个命令将启动服务器。如果你希望在后台无终端运行服务器，在 ```Linux/MacOS``` 上可以运行：

    npm run start-nohup

或者直接运行：

    nohup npm start

## Web 控制器的功能

- OpenBot 控制器在云服务器上运行，可以通过互联网远程访问。客户端可以直接访问控制器，允许使用 Google 账户进行便捷的 ``登录``。此外，在机器人应用程序上，客户端使用与其 Web 控制器登录相同的 ``电子邮件 ID`` 进行身份验证。这种实现确保服务器和客户端之间没有交叉连接。

- 服务器托管在 ``ws://verdant-imported-peanut.glitch.me`` 上，使用安全的 WebSocket 连接，确保快速可靠的通信。此设置使客户端能够从任何位置连接和控制机器人，而无需依赖本地服务器。

- Web 控制器设计用于通过 WebRTC 进行视频流传输，这是一种实时通信协议。用户体验的延迟取决于其个人的互联网连接速度和服务器的地理位置。这意味着具有更快互联网连接和服务器位置更近的用户在视频流传输期间通常会体验到较低的延迟。

- 在机器人安卓应用程序上，进入 ```General``` 面板并选择 ```Web``` 作为控制器。这将把安卓应用程序连接到云服务器，并在 UI 上显示视频。

## 工作原理

1. WebSocket 与云服务器建立连接。服务器从用户的浏览器发起一个身份请求，通常是电子邮件地址。同时，机器人应用程序通过 Google 登录进行身份验证。当控制器模式设置为 ``Web`` 时，服务器会提示用户输入其电子邮件地址。

    <img src="../../controller/web-server/images/web_server_signIn.gif" width="50%"/>

2. 当用户通过浏览器使用 Google 登录时，与账户关联的电子邮件会传输到服务器。随后，一个专用的 ``房间`` 动态生成，用户的电子邮件作为 ``唯一标识符``。在这个房间内，两个候选者被建立。第一个候选者配置为浏览器客户端，然后进入等待状态，准备与机器人应用程序建立连接。

    <img src="../../controller/web-server/images/set_controller.gif" height="25%" width="25%">

3. 当控制器设置为 ```web``` 后，房间达到满员状态，第二个候选者被指定为机器人应用程序。同时，机器人应用程序发送一个 WebRTC（Web 实时通信）的请求。第一个候选者，即浏览器客户端，响应此请求。这次成功的交换建立了一个稳健且功能齐全的连接，并在浏览器的 UI 上显示视频流。

    <img src="../../controller/web-server/images/web_server_video_streaming.gif" width="50%"/>

4. 用户从浏览器输入键盘命令。这些按键通过 WebSocket 或 webrtc 发送到服务器。服务器将这些命令转换为机器人可以理解的命令，例如 ```{driveCmd: {l:0.4, r:0.34}}```（所有命令的列表可以在安卓控制器的文档中找到 [这里](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)）。这些命令通过 Socket 连接发送到机器人。
5. WebSocket 作为 WebRTC 信令代理的关键数据通道。WebRTC 高效利用现有的开放 socket 连接，无需任何额外的连接或配置。这种简化的方法提高了效率，减少了无缝实时通信的设置要求。

### 创建你自己的服务器

- ``服务器设置``：代码启动一个监听 8080 端口的 WebSocket 服务器。服务器准备就绪后，它会记录一条确认消息，说明其在指定端口上的活动状态。随后，初始化一个名为 "rooms" 的 Map，用于管理和存储各个房间的详细信息。每个房间都有一个唯一的 ID。

- ``客户端连接处理``：客户端和机器人应用程序作为房间生成过程中的两个候选者，用于远程建立服务器。系统记录客户端的连接，提供有关连接客户端总数的信息。askIdOfClient 函数与客户端交互，提示他们分享各自的房间 ID。此外，系统监听来自客户端的传入消息。浏览器客户端作为初始候选者进行配置，并进入等待状态，准备与机器人应用程序建立连接。

- ``房间管理``：createOrJoinRoom 函数评估由指定 roomId 标识的房间是否存在。如果房间不存在，它会启动创建新房间的过程。如果房间已经存在，函数会在考虑其可用性的情况下，促进加入现有房间。

- ``客户端断开连接处理和交互``：客户端断开连接后，系统生成日志，包括有关连接客户端总数的信息。如果有相关房间，这些房间将关闭，并从 rooms Map 中删除断开连接的客户端条目。此外，服务器在连接过程中提示客户端提供各自的房间 ID。

- ``广播功能``：
  - wss.broadcast：向所有连接的客户端广播消息。
  - broadcastToRoom：向特定房间内的所有客户端广播消息。
  - sendToBot：向机器人发送消息（向除发送者以外的所有客户端广播）。

- 一旦浏览器客户端响应要约请求，最终将在浏览器的用户界面上显示视频流。

## 自定义服务器的实现：

为了测试目的，我们在 ``glitch`` 上开启了新的远程服务器，但你可以使用任何云环境进行 web-server 与 openBot 应用程序的通信。
- 首先在 [Glitch](https://glitch.com/) 上创建一个新账户。然后，创建一个新项目，如下图所示。

    <img src="../../controller/web-server/images/glitch.jpg" width="50%"/>

- 之后，你需要将 [server.js](server/server.js) 文件中的代码插入到你项目（远程服务器）的 server.js 文件中，如下图所示。

    <img src="../../controller/web-server/images/server_code.jpg" alt="server code image" width="50%"/>

- 下一步，你需要在项目中添加 ``webSocket`` 依赖项，如下图所示

    <img src="../../controller/web-server/images/dependency.jpg" alt="server code image" width="50%"/>

- 要建立你自己的服务器，你需要在 authentication.js 文件中指定项目名称，如图所示，以启动 WebSocket 连接。

    ``new WebSocket(`ws://gossamer-southern-hygienic`);``

## 开发

此代码使用 [snowpack](https://www.snowpack.dev/) 作为快速、轻量级的构建工具。

我们使用 [eslint](https://eslint.org/) 进行代码检查和自动格式化。建议在提交新代码之前运行 lint 并修复所有错误。如果你使用 Visual Code，可以在 [这里](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) 获取插件。运行 linter 如下：

    npm run lint

## 生产

要构建 ```client``` 的生产版本，运行：

    npm run build

这将优化客户端代码到 ```build``` 目录，可以部署在服务器上。此外，我们需要设置一个进程管理器来重启服务器，可能还需要一个反向代理
如 [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)，这还没有完成。

## 故障排除

* 有时浏览器不会显示命令菜单，只显示标题。这意味着 WebSocket 连接无法建立。这通常发生在刚启动服务器之后。如果检查浏览器控制台，可以找到无法连接的消息，例如 ```WebSocket connection to 'ws://localhost:8081/ws' failed```，杀死所有节点进程（pkill -9 node）并重新启动。重新加载页面，连接应该会建立。
* 如果无法将手机连接到应用程序，请确保此应用程序的另一个实例未在此机器或同一电子邮件的另一台机器上运行。

## 已知问题

无。

## 待办事项/尝试事项

* 此软件尚未在 Windows 上测试。如果有人能测试并更新此文档，将非常有用。
* 我们需要调查是否可以远程连接到服务器，以及 WebRTC 是否仍然有效。我们应该记录防火墙配置以使其成为可能。
* 我们需要创建一个 ```生产``` 配置，可能使用 [pm2 进程管理器](https://www.npmjs.com/package/pm2) 和 [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)。