# 안드로이드 앱

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

## 기능

아래 링크를 클릭하여 앱의 기능에 대해 읽어보세요.

- [로봇 앱](robot/README.md)
- [컨트롤러 앱](controller/README.md)

## 앱 설치하기

앱을 얻는 가장 쉬운 방법은 해당 QR 코드를 사용하여 직접 전화로 다운로드하는 것입니다. 전화 브라우저에서 QR 코드를 클릭하면 됩니다. 그런 다음 휴대폰에서 apk를 열고 [설치](https://www.lifewire.com/install-apk-on-android-4177185)할 수 있습니다. 참고로 apk는 디버그 키로만 서명되어 있습니다.

<table style="width:100%;border:none;text-align:center">
  <tr>
    <td>  <a href="https://app.openbot.org/robot" target="_blank">
    <img alt="🤖 앱" width="50%" src="../docs/images/robot_app_qr_code.png" />
  </a>
    </td>
    <td>
  <a href="https://app.openbot.org/controller" target="_blank">
    <img alt="🎮 앱" width="50%" src="../docs/images/controller_app_qr_code.png" />
  </a>
      </td>
  </tr>
  <tr>
    <td>🤖 앱</td>
    <td>🎮 앱</td>
  </tr>
</table>

대안으로, [릴리스](https://github.com/ob-f/OpenBot/releases)의 자산에서 apk를 다운로드할 수 있습니다. 최신 앱을 마스터 브랜치에서 원하신다면, [여기](https://github.com/ob-f/OpenBot/actions?query=workflow%3A%22Java+CI+with+Gradle%22)에서 빌드 아티팩트로 다운로드할 수 있습니다. 안정적이지 않을 수 있습니다. 앱을 나중에 변경하고 싶다면 아래 단계를 따라 앱을 컴파일하고 휴대폰에 배포하세요.

## 앱 빌드하기

### 필수 조건

- [Android Studio Electric Eel | 2022.1.1 이상](https://developer.android.com/studio/index.html)으로 apk를 빌드하고 설치합니다.
- 최소 API 21을 갖춘 Android 기기 및 Android 개발 환경이 필요합니다.
- 현재 API 33을 컴파일 SDK로, API 32를 타겟 SDK로 사용합니다. 자동으로 설치되어야 하지만, 그렇지 않은 경우 수동으로 SDK를 설치할 수 있습니다. Android Studio -> Preferences -> Appearance & Behaviour -> System Settings -> Android SDK로 이동합니다. API 33이 체크되어 있는지 확인하고 적용을 클릭하세요.

![Android SDK](../docs/images/android_studio_sdk.jpg)

### 빌드 과정

1. Android Studio를 열고 *기존 Android Studio 프로젝트 열기*를 선택합니다.
2. OpenBot/android 디렉토리를 선택하고 OK를 클릭합니다.
3. [OpenBot 앱](app/README.md)을 설치하려면 *app* 구성을 선택합니다. [컨트롤러 앱](controller/README.md)을 설치하려면 *controller* 구성을 선택합니다. 필요한 경우 Gradle Sync를 확인합니다. 수동으로 Gradle Sync를 수행하려면 gradle 아이콘을 클릭하세요.
   ![Gradle Sync](../docs/images/android_studio_bar_gradle.jpg)
4. Android 기기를 연결하고 [개발자 옵션](https://developer.android.com/studio/debug/dev-options)에서 USB 디버깅이 활성화되어 있는지 확인합니다. 개발 환경에 따라 [추가 단계](https://developer.android.com/studio/run/device)가 필요할 수 있습니다. 이제 상단 내비게이션 바에서 기기를 확인할 수 있어야 합니다.
   ![전화](../docs/images/android_studio_bar_phone.jpg)
5. 실행 버튼(초록색 화살표)을 클릭하거나 상단 메뉴에서 Run > Run 'android'를 선택합니다. Build > Rebuild Project를 사용하여 프로젝트를 다시 빌드해야 할 수도 있습니다.
   ![실행](../docs/images/android_studio_bar_run.jpg)
6. Instant Run을 사용하라는 메시지가 나타나면 *Proceed Without Instant Run*을 클릭하세요.

### 문제 해결

#### 버전

`The project is using an incompatible version (AGP 7.4.0) of the Android Gradle plugin. Latest supported version is AGP 7.3.0`와 같은 메시지가 나타나면 Android Studio를 업그레이드하거나 gradle 플러그인을 다운그레이드해야 합니다. Android Studio와 gradle 플러그인 간의 버전 호환성에 대한 자세한 내용은 [여기](https://developer.android.com/studio/releases/gradle-plugin#android_gradle_plugin_and_android_studio_compatibility)를 참조하세요.