# OpenBot Controllers

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

There are several ways for controlling the OpenBot robot.

## Bluetooth Controller

The easiest way to control the robot is to connect a game controller via bluetooth. Most BT game controllers should work. We have tested the [PS4 controller](https://www.amazon.de/-/en/Sony-Dualshock-Gamepad-Playstation-Black/dp/B01LYWPQUN), [XBox controller](https://www.amazon.de/-/en/QAT-00002/dp/B07SDFLVKD) and several aftermarket controllers such as the [X3](https://www.amazon.com/Controller-Wireless-Joystick-Bluetooth-Android/dp/B08H5MM64P).

## [Node.js Controller](node-js)

This controller requires your phone and a computer (e.g. laptop, rasberry PI) to be connected to the same WiFi network. After successful connection you get a live low-latency video feed and can use your keyboard to control the robot from the browser.

Special thanks to Ivo Zivkov [izivkov@gmail.com](mailto:izivkov@gmail.com) for the development.

## [Python Controller](python)

This controller requires your phone and a computer (e.g. laptop, rasberry PI) to be connected to the same WiFi network. After successful connection you get a live RTSP video stream and can use your keyboard to control the robot from the terminal. You can use the Python script as a template to develop your own controller.

Special thanks to Ivo Zivkov [izivkov@gmail.com](mailto:izivkov@gmail.com) for the development.

## [Flutter Controller App](flutter)

This controller app serves as a remote controller for the OpenBot vehicle similar as a BT controller (e.g. PS3/4 or Xbox). It runs on another Android/iOS device and supports live video/audio streaming in addition to control.

## [Web Server Controller](web-server)

This is a cloud server version of the local Node.js controller. It allows for remote teleoperation of the OpenBot robot via internet. 
