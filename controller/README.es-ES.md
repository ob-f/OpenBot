# Controladores de OpenBot

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Hay varias formas de controlar el robot OpenBot.

## Controlador Bluetooth

La forma más fácil de controlar el robot es conectar un mando de juego vía bluetooth. La mayoría de los mandos BT deberían funcionar. Hemos probado el [mando de PS4](https://www.amazon.de/-/en/Sony-Dualshock-Gamepad-Playstation-Black/dp/B01LYWPQUN), el [mando de XBox](https://www.amazon.de/-/en/QAT-00002/dp/B07SDFLVKD) y varios mandos de terceros como el [X3](https://www.amazon.com/Controller-Wireless-Joystick-Bluetooth-Android/dp/B08H5MM64P).

## [Controlador Node.js](node-js)

Este controlador requiere que tu teléfono y una computadora (por ejemplo, laptop, Raspberry PI) estén conectados a la misma red WiFi. Después de una conexión exitosa, obtienes una transmisión de video en vivo de baja latencia y puedes usar tu teclado para controlar el robot desde el navegador.

Agradecimientos especiales a Ivo Zivkov [izivkov@gmail.com](mailto:izivkov@gmail.com) por el desarrollo.

## [Controlador Python](python)

Este controlador requiere que tu teléfono y una computadora (por ejemplo, laptop, Raspberry PI) estén conectados a la misma red WiFi. Después de una conexión exitosa, obtienes una transmisión de video RTSP en vivo y puedes usar tu teclado para controlar el robot desde la terminal. Puedes usar el script de Python como plantilla para desarrollar tu propio controlador.

Agradecimientos especiales a Ivo Zivkov [izivkov@gmail.com](mailto:izivkov@gmail.com) por el desarrollo.

## [Aplicación Controladora Flutter](flutter)

Esta aplicación controladora sirve como un control remoto para el vehículo OpenBot similar a un controlador BT (por ejemplo, PS3/4 o Xbox). Funciona en otro dispositivo Android/iOS y soporta transmisión de video/audio en vivo además del control.

## [Controlador de Servidor Web](web-server)

Esta es una versión en la nube del controlador local de Node.js. Permite la teleoperación remota del robot OpenBot a través de internet.