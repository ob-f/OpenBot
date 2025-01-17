# OpenBot Controller

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Es gibt mehrere Möglichkeiten, den OpenBot-Roboter zu steuern.

## Bluetooth-Controller

Die einfachste Möglichkeit, den Roboter zu steuern, besteht darin, einen Gamecontroller über Bluetooth zu verbinden. Die meisten BT-Gamecontroller sollten funktionieren. Wir haben den [PS4-Controller](https://www.amazon.de/-/en/Sony-Dualshock-Gamepad-Playstation-Black/dp/B01LYWPQUN), den [XBox-Controller](https://www.amazon.de/-/en/QAT-00002/dp/B07SDFLVKD) und mehrere Nachbau-Controller wie den [X3](https://www.amazon.com/Controller-Wireless-Joystick-Bluetooth-Android/dp/B08H5MM64P) getestet.

## [Node.js-Controller](node-js)

Dieser Controller erfordert, dass Ihr Telefon und ein Computer (z.B. Laptop, Raspberry PI) mit demselben WiFi-Netzwerk verbunden sind. Nach erfolgreicher Verbindung erhalten Sie einen Live-Video-Feed mit geringer Latenz und können Ihre Tastatur verwenden, um den Roboter über den Browser zu steuern.

Besonderer Dank geht an Ivo Zivkov [izivkov@gmail.com](mailto:izivkov@gmail.com) für die Entwicklung.

## [Python-Controller](python)

Dieser Controller erfordert, dass Ihr Telefon und ein Computer (z.B. Laptop, Raspberry PI) mit demselben WiFi-Netzwerk verbunden sind. Nach erfolgreicher Verbindung erhalten Sie einen Live-RTSP-Video-Stream und können Ihre Tastatur verwenden, um den Roboter über das Terminal zu steuern. Sie können das Python-Skript als Vorlage verwenden, um Ihren eigenen Controller zu entwickeln.

Besonderer Dank geht an Ivo Zivkov [izivkov@gmail.com](mailto:izivkov@gmail.com) für die Entwicklung.

## [Flutter-Controller-App](flutter)

Diese Controller-App dient als Fernsteuerung für das OpenBot-Fahrzeug, ähnlich wie ein BT-Controller (z.B. PS3/4 oder Xbox). Sie läuft auf einem anderen Android/iOS-Gerät und unterstützt neben der Steuerung auch Live-Video-/Audio-Streaming.

## [Web-Server-Controller](web-server)

Dies ist eine Cloud-Server-Version des lokalen Node.js-Controllers. Sie ermöglicht die Fernsteuerung des OpenBot-Roboters über das Internet.