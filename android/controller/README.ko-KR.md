# Controller App

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

이 안드로이드 앱은 [OpenBot](https://www.openbot.org) 차량의 `리모컨` 역할을 해요. 기본적으로 PS3/4나 Xbox 리모컨처럼 비슷한 기능을 하지만, 다른 안드로이드 기기에서 실행된답니다!

## 연결

컨트롤러 앱을 실행하면 바로 로봇에 연결을 시도해요. 다음 화면이 보여요:

<p float="left">
  <img src="../../docs/images/controller_pre_connect.png" width="50%" />
</p>

로봇을 컨트롤러에 연결하려면 로봇의 앱 설정을 **전화** 모드로 바꿔 주세요.

<p float="left">
  <img src="../../docs/images/app_controller_settings_1.png" width="25%" />
  <img src="../../docs/images/app_controller_settings_2.png" width="25%" />
</p>

또한 `FreeRoamFragment`에서 핸드폰을 컨트롤러로 선택하여 연결할 수 있어요:

<p float="left">
  <img src="../../docs/images/free-roam-fragment-selection.png" width="50%" />
</p>

몇 초 후에 삐- 소리가 나고, 컨트롤러 화면이 다음과 같이 바뀌어요:

<p float="left">
  <img src="../../docs/images/controller_command_buttons.png" width="50%" />
</p>

여기서 핸드폰을 기울여서 로봇을 조종하거나 화면의 버튼을 이용해 조종할 수 있어요.

***참고:*** 이 방법으로 연결이 되지만, 30초 이내에 연결이 안 된다면, 로봇 앱에서 `Control` 설정을 `Gamepad`로 바꾼 후 다시 `Phone`으로 바꿔서 연결을 다시 시도해 보세요. 그래도 안 되면, 컨트롤러 앱을 종료하고 다시 실행해 보세요. 그 후, 로봇 앱에서 `Control` 모드를 다시 바꿔 주세요.

## 작동법

### 화면상의 조작
이 모드는 사용자가 `듀얼 드라이브` 모드에서 두 개의 슬라이더를 통해 로봇 자동차를 조종할 수 있게 해줘요. 각 슬라이더의 손잡이를 위아래로 움직이면 좌우로 회전할 수 있어요. 슬라이더의 중앙을 기준으로 위아래로 손잡이를 움직이면 각 바퀴가 전진하거나 후진해요.

<p float="left">
  <img src="../../docs/images/controller_main_screen.png" width="50%" />
</p>

또한 좌/우 방향 지시등을 설정할 수 있어요
<img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/>
<img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/>
화면 왼쪽 상단에 있는 화살표를 클릭하고, 그 사이의 빨간 버튼을 눌러서 취소할 수 있어요.

### 기울여서 운전하기
컨트롤러는 가속도계 모션 센서를 사용해 로봇을 조종할 수도 있어요. 이 옵션을 선택하면, 컨트롤러는 화면이 전체 화면(젠 모드)으로 바뀌고, 영상만 보여지며 `브레이크`와 `엑셀러레이터` 페달만 나타나요. 이 모드를 종료하려면 화면을 두 번 탭하면 돼요.

여기 `기울이기 모드` 화면이 있어요:

<p float="left">
  <img src="../../docs/images/tilt-mode-controller.jpg" width="50%" />
</p>

`엑셀러레이터`와 `브레이크` 버튼을 사용해서 전진하거나 후진할 수 있어요.

- `엑셀러레이터`를 누르면 로봇이 2초 이내에 최고 속도로 가속해요. 버튼을 놓으면 로봇이 멈추도록 서서히 속도가 줄어들어요 (최고 속도의 0%로 설정된 정지 속도, 조정 가능).
- `브레이크` 버튼을 누르면 로봇이 즉시 멈춰요. 브레이크를 1초 더 누르고 있으면 로봇이 후진을 시작하고, 1초 이내에 최대 후진 속도에 도달해요. 브레이크를 놓으면 로봇이 멈춰요.
- 로봇을 조향하려면 컨트롤러를 왼쪽이나 오른쪽으로 기울여요.

## 향후 개발 계획

추가하려는 기능은 다음과 같아요:

- 로봇 센서에 대한 정보(예: 배터리 상태, 속도)를 컨트롤러에 추가
- 로봇 카메라에서 컨트롤러로 비디오 스트리밍
- 컨트롤러의 자이로스코프 센서를 이용해 로봇 제어
- 로봇의 충돌 및 충격 이벤트를 컨트롤러로 전송해 더 현실감 있는 경험 제공

여기 컨트롤러 앱에 대한 [기술 개요](../../docs/technical/OpenBotController.pdf)가 있어요.
