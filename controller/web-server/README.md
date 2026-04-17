# OpenBot Web Controller

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Nomenclature

Here are some terms we will be using in this document:

* ```Robot, bot``` - this is the Android software running on the phone on the [OpenBot](https://www.openbot.org/)
vehicle.
* ```Server``` - the web server, the server part of this project.
* ```Client, UI``` - this is the client part of this project. It runs in the browser.

## Introduction

This project operates remotely on a cloud server, functioning as a controller for
the [OpenBot](https://www.openbot.org/) vehicle. The software comprises two components: a server and a client. The
server is a cloud application deployed in a remote environment. The client component runs directly in the web browser.
Here is a screenshot of the browser:

<img src="../../controller/web-server/images/openbot_controller.jpg" width="50%"/>

## Getting Started

You can run this software on a PC, RaspberryPi-type device or
even [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/) devices which support ```Node.js``` environment.
First make sure you have installed [Node.js](https://nodejs.org/), version 10 or newer. Check the version:

    node --version

The software is located in the ```/controller/web-server``` directory of the OpenBot project. After checking out the
code from [github](https://github.com/ob-f/OpenBot), change into this directory and run the following commands:

    npm install
    npm start

The last command will start the server. If you like to run the server without a terminal in the background,
on ```Linux/MacOS``` you can run:

    npm run start-nohup

or just:

    nohup npm start

## Features of web controller

- The OpenBot controller operates on a cloud server accessible remotely via the internet. Clients can seamlessly access
  the controller directly, allowing for a convenient ``sign-in`` process using their Google accounts. Moreover, on the
  robot app, clients utilize the ``same email ID`` as their web controller login for authentication. This implementation ensures that there are no cross-connections between servers and clients.

- The server is hosted at ``ws://verdant-imported-peanut.glitch.me`` using a secure WebSocket connection, ensuring swift
  and reliable communication. This setup empowers clients with the ability to connect and control the robot from any
  location without the dependency on a local server.

- The web controller is designed to facilitate video streaming through WebRTC, a real-time communication protocol. The
  latency experienced by users is contingent upon their individual internet connection speeds and the geographical
  location of the server. This means that users with faster internet connections and servers located in closer proximity
  will generally experience lower latency during video streaming session.

- On the Robot Android app, go to the ```General``` panel and select ```Web``` as the controller. This will connect the
Android app to the cloud server, and a video will appear on the UI.

## How it Works

1. The WebSocket creates a connection with cloud server. The server initiates a request for an identification, typically
   in the form of an email address, from the user's browser. Simultaneously, the robot application undergoes
   authentication using Google Sign-In. When the controller mode is set to ``Web``, server prompts the user for their
   email address.

    <img src="../../controller/web-server/images/web_server_signIn.gif" width="50%"/>

2. When a user signs in via Google on the browser, the email associated with the account is transmitted to the server.
   Subsequently, a dedicated ``room`` is dynamically generated, with the user's email serving as
   the ``unique identifier``. Within this room, two candidates are established. The initial candidate is configured for
   the browser client, which then enters a waiting state, poised to establish a connection with the robot application.

    <img src="../../controller/web-server/images/set_controller.gif" height="25%" width="25%">

3. After the controller is set to ```web```, the room reaches full capacity, with the second candidate being designated
   as the robot application. Concurrently, the robot application sends a request for an offer for WebRTC (Web Real-Time
   Communication). The first candidate, which is assigned to the browser client, responds with an answer to this
   request. This successful exchange results in the establishment of a robust and functional connection. And displays
   video stream on browser's UI.

    <img src="../../controller/web-server/images/web_server_video_streaming.gif" width="50%"/>

 
4. The user enters keyboard commands from the browser. These key presses are sent to the Server via the WebSocket or
   webrtc. The server converts these to commands that the Robot can understand, like ```{driveCmd: {l:0.4, r:0.34}}``` (
   a list of all commands can be found in the documentation for the Android
   controller [here](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)). These
   commands are sent to the Robot via the Socket connection.
5. The WebSocket serves as a crucial data channel for WebRTC signaling proxy. WebRTC efficiently leverages the existing
   open socket connections for this purpose, eliminating the need for any additional connections or configurations. This
   streamlined approach enhances efficiency and minimizes setup requirements for seamless real-time communication
   
### Create your own server 

- ``Server setup``: The code initiates a WebSocket server that listens on port 8080. Upon the server's readiness, it logs a confirmation message about its active state on the specified port. Subsequently, a Map named "rooms" is initialized, serving as a repository to manage and store details about individual rooms. Each room is uniquely identified by an ID.
  
- ``Client Connection Handling`` : The client and the robot app act as two candidates in the room generation process for establishing a server remotely. The system logs the connection of a client, providing information about the total number of connected clients. The askIdOfClient function engages with clients, prompting them to share their respective room IDs. Additionally, the system listens for incoming messages from clients. The browser client, functioning as the initial candidate, is configured and transitions into a waiting state, prepared to initiate a connection with the robot application.

- `` Room Management``: The createOrJoinRoom function assesses the existence of a room identified by the specified roomId. If the room is not present, it initiates the creation of a new room. In cases where the room already exists, the function facilitates the joining of the existing room, taking into consideration its availability.

- `` Client Disconnection Handling and interaction``: Upon a client's disconnection, the system generates logs that include information about the total number of connected clients. If there are associated rooms, these rooms are closed, and the entry of the disconnected client is expunged from the rooms Map. Additionally, the server prompts clients to provide their respective room IDs during the connection process.

- ``Broadcast Functions``: 
  - wss.broadcast: Broadcasts a message to all connected clients.
  - broadcastToRoom: Broadcasts a message to all clients within a specific room.
  - sendToBot: Sends a message to a bot (broadcasts to all clients except the sender).


- Once the browser client responds with an answer to the offer request, it will finally lead to the display of a video stream on the browser's user interface.

## Implementation of custom server:

For testing purposes, we have opened new remote server on ``glitch`` but you can use any cloud environment for your web-server communication with openBot application.
- First of all create a new account on [Glitch](https://glitch.com/). After, create your new project as shown in below image.

    <img src="../../controller/web-server/images/glitch.jpg" width="50%"/>

- After that you have to insert the code from [server.js](server/server.js) file to server.js file of your project(remote server) as shown in the following image.

    <img src="../../controller/web-server/images/server_code.jpg" alt="server code image" width="50%"/>

- In next step, you have to add ``webSocket`` dependency in your project as you can see in the below image

    <img src="../../controller/web-server/images/dependency.jpg" alt="server code image" width="50%"/>

-  To establish your own server, you need to specify the project name in the authentication.js file, as demonstrated, to initiate the WebSocket connection.

    ``new WebSocket(`ws://gossamer-southern-hygienic`);``




## Development

This code uses [snowpack](https://www.snowpack.dev/) for a fast, lightweight building tool.

We use [eslint](https://eslint.org/) for linting and automatically formatting your code. It is recommended that you run
lint and fix any errors before committing new code. If you are using Visual Code, you can get a
plugin [here](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint). Run the linter like this:

    npm run lint

## Production

To build a production version of the ```client```, run:

    npm run build

This will optimize the client code into a ```build``` directory, which can be deplyed on a server. In addition we need
to setup a process manager to restart the server, and possibly a reverse proxy
like [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/), which is not yet done.

## Troubleshooting

* Sometimes the browser will not show the commands menu, just the title. This means the WebSocket connection could not
  be established. This usually happens right after starting the server. If you examine the browser console, you can find
  a message about not being able to connect, something
  like ```WebSocket connection to 'ws://localhost:8081/ws' failed```, Kill all node processes (pkill -9 node)  and
  restart it. Reload the page and connection should be established.
* If you cannot connect the phone to the app, make sure another instance of this application is not running on this
  machine or another machine on the same email.

## Known Bugs

None.

## Things to do/try

* This software has not been tested on Windows. It would be useful if somebody can test and update this documentation.
* We need to investigate if we can connect to the server remotely, and if WebRTC will still work. We should document
  firewall configuration to make this possible.
* We need to create a ```production``` configuration, possibly
  using [pm2 process manager](https://www.npmjs.com/package/pm2)
  and [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/).
