# OpenBot Nodejs 控制器

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

* ```机器人, bot``` - 这是运行在 [OpenBot](https://www.openbot.org/) 车辆上的手机上的 Android 软件。
* ```服务器``` - Node 服务器，本项目的服务器部分
* ```客户端, UI``` - 这是本项目的客户端部分。它在浏览器中运行。

## 简介

这是一个基于 [Node.js](https://nodejs.org/) 的项目，用于控制 [OpenBot](https://www.openbot.org/) 车辆。该软件由两部分组成——服务器和客户端。服务器是一个运行在与机器人同一网络中的计算机上的 Node.js 应用程序。客户端部分在浏览器中运行。

以下是浏览器的截图：

![Screenshot](images/Screenshot.png "image_tooltip")

## 入门指南

您可以在 PC、RaspberryPi 类型设备甚至支持 ```Node.js``` 环境的 [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/) 设备上运行此软件。首先确保您已安装 [Node.js](https://nodejs.org/)，版本为 10 或更新版本。检查版本：

    node --version

该软件位于 OpenBot 项目的 ```/controller/node-js``` 目录中。从 [github](https://github.com/ob-f/OpenBot) 检出代码后，进入此目录并运行以下命令：

    npm install
    npm start

最后一个命令将启动服务器。如果您希望在后台无终端运行服务器，在 ```Linux/MacOS``` 上可以运行：

    npm run start-nohup

或者直接运行：

    nohup npm start

将浏览器指向服务器的 IP 地址和端口 8081，例如 [http://localhost:8081](http://localhost:8081)，或 [http://192.168.1.100:8081](http://192.168.1.100:8081)。请注意，您可以从不同的计算机访问服务器，但机器人、服务器和浏览器 PC 需要在同一网络中。未来我们可以添加远程访问服务器的功能。

确保您的机器人连接在同一网络上。在机器人 Android 应用中，进入 ```General``` 面板并选择 ```Phone``` 作为控制器。这将把 Android 应用连接到 Node 服务器，并在 UI 上显示视频。

## 工作原理

1. Node 服务器创建并发布类型为 ```openbot.tcp``` 和名称为 ```OPEN_BOT_CONTROLLER``` 的 DNS 服务，端口为 19400。这使得机器人可以在不知道其 IP 地址的情况下自动找到服务器。机器人正在寻找此服务，并将在进入 ```Phone``` 控制器模式时建立 Socket 连接。

2. Node 服务器在端口 8081 创建一个 HTTP 服务器，并开始处理来自浏览器的请求。

3. 此外，Node 服务器在端口 7071 创建一个 WebSocket 服务器。这将用于直接与浏览器通信。因此，总结到目前为止，服务器已创建了两个 Socket 连接，一个到机器人，一个到浏览器。

4. 用户从浏览器输入键盘命令。这些按键通过 WebSocket 发送到服务器。服务器将这些命令转换为机器人可以理解的命令，如 ```{driveCmd: {r:0.4, l:0.34}}```（所有命令的列表可以在 Android 控制器的文档中找到 [这里](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)）。这些命令通过 Socket 连接发送到机器人。

5. 机器人通过 Socket 连接将状态信息发送回服务器，服务器将其转发到 UI。UI 可以使用此信息来增强其外观，如显示闪烁的指示灯等，但目前此状态被忽略。

6. Node 服务器还充当 WebRTC 信令代理。它在机器人和浏览器之间转发 WebRTC 协商命令。它重用开放的 Socket 连接，因此不需要额外的连接或配置。

![drawing](images/HowItWorks.png)

## 开发

此代码使用 [snowpack](https://www.snowpack.dev/) 作为快速、轻量级的构建工具。

我们使用 [eslint](https://eslint.org/) 进行代码检查和自动格式化。建议在提交新代码之前运行 lint 并修复任何错误。如果您使用 Visual Code，可以在 [这里](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) 获取插件。运行 linter 如下：

    npm run lint

## 生产

要构建 ```client``` 的生产版本，请运行：

    npm run build

这将优化客户端代码到 ```build``` 目录中，可以部署在服务器上。此外，我们需要设置一个进程管理器来重启服务器，可能还需要一个反向代理如 [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)，这尚未完成。

## 故障排除

* 有时浏览器不会显示命令菜单，只显示标题。这意味着 WebSocket 连接无法建立。这通常发生在刚启动服务器之后。如果检查浏览器控制台，可以找到无法连接的消息，如 ```WebSocket connection to 'ws://localhost:7071/ws' failed```，杀死所有 node 进程（pkill -9 node）并重新启动。重新加载页面，连接应能建立。
* 如果无法将手机连接到应用程序，请确保此应用程序的另一个实例未在此机器或同一网络中的另一台机器上运行。

## 已知错误

无。

## 待办事项/尝试

* 我们需要调查是否可以远程连接到服务器，以及 WebRTC 是否仍然有效。我们应该记录防火墙配置以使其成为可能。
* 我们需要创建一个 ```production``` 配置，可能使用 [pm2 进程管理器](https://www.npmjs.com/package/pm2) 和 [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)。