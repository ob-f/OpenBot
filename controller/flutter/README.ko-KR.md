# Flutter 컨트롤러 앱

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

이 컨트롤러 앱은 [OpenBot](https://www.openbot.org) 차량의 `리모컨` 역할을 합니다. PS3/4나 Xbox와 같은 Bluetooth 컨트롤러와 유사한 기능을 제공하며, 또 다른 Android/iOS 기기에서 실행됩니다. 이 앱은 제어 기능 외에도 실시간 비디오/오디오 스트리밍을 지원합니다.

## 시작하기
먼저 시스템에 [Flutter](https://flutter.dev/)를 설치하세요. 운영 체제에 맞는 다운로드 옵션을 선택하고, Windows, macOS, Linux, ChromeOS를 포함한 모든 플랫폼에 대한 설치 방법은 공식 Flutter 설치 가이드에서 확인할 수 있습니다: [Flutter 설치 가이드](https://docs.flutter.dev/get-started/install)

### 터미널 사용하기
- Flutter가 성공적으로 설치되면 **터미널** 또는 **명령 프롬프트**를 엽니다.
- OpenBot 프로젝트가 저장된 위치로 이동한 후, `OpenBot/controller/flutter` 폴더로 이동합니다.
- 터미널에서 Flutter 애플리케이션을 실행하려면 아래 명령어를 사용하세요.

  #### 의존성 설치:
    ```bash
     flutter pub get 
    ```
  프로젝트 실행:
    ```bash
     flutter run
    ```
  문제가 발생하면 다음 명령어를 실행해보세요:
    ```bash
     flutter doctor
    ```

### 에디터 사용하기
- Flutter 에디터 설정을 위한 공식 가이드를 따르세요: [에디터 설정하기](https://docs.flutter.dev/tools/android-studio)
- 에디터가 Flutter 개발을 위한 설정이 되어 있는지 확인하세요. 필요한 플러그인이나 확장 프로그램을 설치하고, 에디터에 맞는 Flutter 문서를 참조하여 최상의 개발 환경을 설정합니다.

- 에디터에서 프로젝트를 열면 아래와 같은 화면이 나타납니다.

  <p float="left">
    <img src="../../docs/images/android_editor.jpg" width="50%" />
  </p>

- 위에서 설명한 터미널 명령어를 참고하여, 향후 반복 실행을 위해서는 에디터에서 ``run`` 버튼을 직접 눌러 실행할 수 있습니다.

  <p float="left">
    <img src="../../docs/images/run_editor.jpg" width="50%" />
  </p>

## 연결

컨트롤러 앱이 시작되면, 즉시 로봇에 연결을 시도하고 다음과 같은 화면을 표시합니다:

<p float="left">
  <img src="../../docs/images/flutter_controller_home.jpg" width="50%" />
</p>

컨트롤러를 로봇에 연결하려면, 로봇의 제어 모드를 **Phone**으로 설정해야 합니다. 예를 들어, `FreeRoamFragment`에서 전화 모드는 다음과 같이 활성화됩니다:

<p float="left">
  <img src="../../docs/images/phone_selection.gif" width="50%" />
</p>

연결이 완료되면 컨트롤러 앱은 다음과 같이 표시됩니다:

<p float="left">
  <img src="../../docs/images/flutter_controller_connected.jpg" width="50%" />
</p>

여기서 전화 기울기를 사용하여 로봇을 조종하거나 화면상의 컨트롤을 사용하여 로봇을 조종할 수 있습니다.

***참고:*** 이 방법으로 연결할 수 있어야 하지만, 30초 이내에 연결이 되지 않으면, 봇 앱에서 `Control` 설정을 `Gamepad`로 변경한 뒤 다시 `Phone`으로 설정하여 연결을 재시도해 보세요. 그래도 연결되지 않으면, 컨트롤러 앱을 종료하고 다시 시작한 후, 로봇 앱에서 제어 모드를 다시 전환해 보세요.

## 작동

### 화면 컨트롤

이 모드는 사용자가 `Dual Drive` 모드에서 두 개의 슬라이더를 통해 로봇 자동차를 제어할 수 있게 해줍니다. 각 슬라이더에서 엄지손가락을 위/아래로 움직여 왼쪽/오른쪽으로 회전할 수 있습니다. 슬라이더의 중앙 위/아래로 엄지손가락을 이동하면 각각 왼쪽/오른쪽 바퀴가 앞으로/뒤로 회전합니다.

<p float="left">
  <img src="../../docs/images/flutter_controller_dual_drive_mode.jpg" width="50%" />
</p>

- ``Indicators``: 화면 왼쪽 상단의 화살표 <img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/> <img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/>를 클릭하여 좌/우 회전 지시등을 설정할 수 있습니다.

- ``Switch Camera``: 전방과 후방 카메라 모드를 전환합니다.
- ``Mute``: 오디오 전송을 활성화/비활성화합니다.
- ``Mirror view``: 비디오 피드를 미러링합니다.

### 기울여서 운전

컨트롤러는 또한 가속도계 모션 센서를 사용하여 로봇을 운전할 수 있습니다. 이 옵션을 선택하면 컨트롤러는 전체 화면(Zen) 모드로 전환되어 비디오만 표시되고 `브레이크`와 `엑셀러레이터` 페달만 표시됩니다. 이 모드를 종료하려면 화면을 더블탭하세요.

`기울여서 운전` 모드 화면은 다음과 같습니다:

<p float="left">
  <img src="../../docs/images/flutter_controller_tilt_mode.jpg" width="50%" />
</p>

`엑셀러레이터`와 `브레이크` 버튼을 사용하여 전진/후진합니다.

- `엑셀러레이터`를 누르면 로봇은 2초 이내에 최대 속도로 가속됩니다. 버튼을 떼면 로봇은 속도를 줄여 정지합니다(정지 속도는 최대 속도의 0%로 설정 가능).
- `브레이크` 버튼을 누르면 로봇이 즉시 정지합니다. 브레이크를 1초 더 누르면 로봇이 후진을 시작하고 1초 이내에 최대 후진 속도에 도달합니다. 브레이크를 떼면 로봇은 정지합니다.
- 로봇은 컨트롤러 폰을 왼쪽/오른쪽으로 기울여 조종합니다.

여기에는 [기술 개요](../../docs/technical/OpenBotController.pdf)도 포함되어 있습니다.