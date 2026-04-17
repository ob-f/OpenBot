# iOS 앱 - 베타 릴리스

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

## 기능

아래 링크를 클릭하여 앱의 기능에 대해 알아보세요.

- [로봇 앱](OpenBot/README.ko-KR.md)
- 컨트롤러 앱 (곧 출시 예정)

## 앱 설치

현재 iOS 앱을 실행하는 유일한 방법은 아래 섹션에 설명된 대로 개발자 빌드를 사용하는 것입니다.

## 앱 빌드하기

### 사전 요구 사항

- [Xcode iOS 13 이상](https://developer.apple.com/xcode/)이 필요합니다.
- 시스템에 [Cocoapods](https://cocoapods.org/)가 설치되어 있어야 합니다.
- 현재 iOS 배포 대상 버전은 15.5로 설정되어 있습니다.
- 프로젝트는 "자동으로 서명 관리"로 구성되어 있어, 앱 빌드를 위해 독립적인 계정을 설정할 수 있습니다:
  <img alt="iOS App" width="100%" src="../docs/images/ios_automatically_manage_signing.png" />
- iOS 13 이상을 실행하는 iOS 기기가 필요합니다. [지원 기기 목록](https://support.apple.com/en-in/guide/iphone/iphe3fa5df43/ios).
- [개발자 모드](https://developer.apple.com/documentation/xcode/enabling-developer-mode-on-a-device)가 iOS 기기에서 활성화되어 있어야 합니다.
- 추가된 iCloud 계정은 [iOS 기기에서 신뢰를 받아야](https://developer.apple.com/forums/thread/685271) 합니다.

### 빌드 프로세스

1. Xcode를 열고 *프로젝트 또는 파일 열기*를 선택합니다.
2. [OpenBot 앱](OpenBot/README.ko-KR.md)을 설치하려면 *OpenBot* 구성을 선택해야 합니다:
   <img alt="iOS App" width="100%" src="../docs/images/ios_openbot_configuration.png" />
3. 사용 가능한 기기 목록에서 자신의 기기를 선택합니다:
   <img alt="iOS App" width="100%" src="../docs/images/ios_device_selection.png" />
4. Xcode 화면 왼쪽 상단의 ▶️ 아이콘을 클릭하여 기기에서 앱을 실행합니다.