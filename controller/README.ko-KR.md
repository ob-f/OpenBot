# OpenBot 컨트롤러

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

OpenBot 로봇을 제어하는 여러 가지 방법이 있습니다.

## 블루투스 컨트롤러

로봇을 제어하는 가장 쉬운 방법은 게임 컨트롤러를 블루투스를 통해 연결하는 것입니다. 대부분의 BT 게임 컨트롤러가 작동해야 합니다. 우리는 [PS4 컨트롤러](https://www.amazon.de/-/en/Sony-Dualshock-Gamepad-Playstation-Black/dp/B01LYWPQUN), [Xbox 컨트롤러](https://www.amazon.de/-/en/QAT-00002/dp/B07SDFLVKD) 및 여러 서드파티 컨트롤러(예: [X3](https://www.amazon.com/Controller-Wireless-Joystick-Bluetooth-Android/dp/B08H5MM64P))를 테스트했습니다.

## [Node.js 컨트롤러](node-js)

이 컨트롤러는 전화와 컴퓨터(예: 노트북, 라즈베리 파이)가 동일한 WiFi 네트워크에 연결되어 있어야 합니다. 성공적으로 연결되면 실시간 저지연 비디오 피드를 얻고 브라우저에서 키보드를 사용하여 로봇을 제어할 수 있습니다.

개발에 대한 특별한 감사의 말씀을 이보 지브코프 [izivkov@gmail.com](mailto:izivkov@gmail.com)에게 전합니다.

## [파이썬 컨트롤러](python)

이 컨트롤러는 전화와 컴퓨터(예: 노트북, 라즈베리 파이)가 동일한 WiFi 네트워크에 연결되어 있어야 합니다. 성공적으로 연결되면 실시간 RTSP 비디오 스트림을 얻고 터미널에서 키보드를 사용하여 로봇을 제어할 수 있습니다. 파이썬 스크립트를 템플릿으로 사용하여 자신만의 컨트롤러를 개발할 수 있습니다.

개발에 대한 특별한 감사의 말씀을 이보 지브코프 [izivkov@gmail.com](mailto:izivkov@gmail.com)에게 전합니다.

## [플러터 컨트롤러 앱](flutter)

이 컨트롤러 앱은 OpenBot 차량의 원격 제어기로 블루투스 컨트롤러(예: PS3/4 또는 Xbox)와 유사합니다. 다른 Android/iOS 장치에서 실행되며 제어 외에도 실시간 비디오/오디오 스트리밍을 지원합니다.

## [웹 서버 컨트롤러](web-server)

이것은 로컬 Node.js 컨트롤러의 클라우드 서버 버전입니다. 인터넷을 통해 OpenBot 로봇의 원격 원격 조작을 허용합니다.