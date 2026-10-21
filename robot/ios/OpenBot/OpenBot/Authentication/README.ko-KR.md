## Google Firebase

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

Google Firebase는 모바일과 웹 애플리케이션 개발 플랫폼으로, 개발자가 고품질 앱을 빠르고 효율적으로 만들 수 있도록 다양한 서비스와 도구를 제공합니다. 실시간 데이터베이스, 사용자 인증, 호스팅, 클라우드 저장소 등 많은 기능이 하나의 플랫폼에 통합되어 있습니다. Firebase는 백엔드 인프라를 편리하고 확장성 있게 관리할 수 있게 해 주어, 개발자가 좋은 사용자 경험을 만드는 데 집중할 수 있습니다.

- ### Firebase Google 로그인 인증

  Firebase Google 로그인 인증은 사용자가 Google 계정으로 모바일 앱이나 웹 앱에 로그인할 수 있게 해 주는 Firebase 플랫폼의 기능입니다. 별도의 로그인 정보를 기억하고 관리하지 않아도 앱에 안전하고 편리하게 접근할 수 있습니다. Firebase는 Google로 사용자 신원을 확인하는 단계부터 앱 안에서 사용자 경험을 맞춤화하는 데 쓸 수 있는 고유 사용자 ID를 제공하는 단계까지 인증 과정 전체를 관리합니다. 또한 2단계 인증 같은 추가 보안 기능으로 무단 접근으로부터 사용자 계정을 보호합니다.

- ### 사용
  이 애플리케이션에서는 Google Drive에 업로드된 [OpenBot Playground](../../../../../open-code/README.ko-KR.md) 프로젝트에 접근하기 위해 Firebase Google 로그인 인증을 사용합니다.

- ### 참고
  이 iOS 앱을 복제해 직접 빌드하려면 [OpenBot Playground](https://www.playground.openbot.org/) 웹 애플리케이션에도 [Firebase를 설정](../../../../../open-code/README.ko-KR.md)해야 합니다. iOS 앱은 Firebase Google Drive 서비스가 만든 파일을 사용자의 Google Drive에서 가져오기 때문입니다. Google Drive 서비스가 제대로 동작하려면 iOS 앱과 웹 앱에 같은 Firebase 프로젝트를 사용해야 합니다.

  이 저장소에 커밋된 `GoogleService-Info.plist` 파일은 가짜 키와 식별자가 들어 있는 **자리 표시자**입니다.
  아래 설명대로 자신의 Firebase 프로젝트에서 내려받은 실제 파일로 바꾸기 전까지는 Google 로그인과 다른 Firebase 기능이 동작하지 않습니다.

- ### 사전 요구 사항
  Google 로그인을 위해 OpenBot iOS 앱에 Firebase를 연동하려면 다음이 필요합니다.
- **Google 계정:** Firebase로 Google 로그인을 사용하려면 Google 계정이 있어야 합니다. 계정이 없다면 여기에서 무료 Google 계정을 [만드세요](https://accounts.google.com/signup).
- **Cocoapods:** Swift와 Objective-C 프로젝트용 의존성 관리자입니다. Firebase SDK를 설치하려면 시스템에 Cocoapods가 설치되어 있어야 합니다.

### Firebase 프로젝트 설정

- [Firebase 콘솔](https://console.firebase.google.com/)로 이동해 다음 단계에 따라 `새 프로젝트`를 만듭니다.
    1. "Create Project" 버튼을 클릭합니다.
    2. Firebase 프로젝트 이름을 입력합니다.
    3. "Next"를 클릭하고, Google Analytics를 사용하지 않으려면 비활성화합니다.
    4. "Create Project" 버튼을 클릭합니다.

    <p>
    <img src="../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
    <img src="../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width="40%"/>
    <img src="../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
    </p>

- Firebase 프로젝트에 새 iOS 앱을 추가하려면 다음과 같이 합니다.
    1. Firebase 프로젝트에서 iOS 아이콘을 클릭합니다.
    2. 앱의 번들 ID를 입력합니다. Xcode 프로젝트에서 사용하는 번들 ID와 같아야 합니다.
    3. 앱의 닉네임을 입력합니다.
    4. "Register app" 버튼을 클릭해 완료합니다.
    5. GoogleService-Info.plist 파일을 내려받아 Xcode 프로젝트에 추가합니다. 이 파일에는 앱에서 Firebase를 사용하는 데 필요한 구성 정보가 들어 있습니다.

       <p>
       <img src="../../../../../docs/images/firebase_add_ios_app.jpg" alt="Add IOS Application" width="47.5%"/>
       <img src="../../../../../docs/images/firebase_bundle_id.jpg" alt="Bundle ID Name" width="30.5%"/>
       </p>

    6. 새 `GoogleService-Info.plist`에서 `REVERSED_CLIENT_ID` 값을 복사해 `Info.plist`의
       `CFBundleURLSchemes` 항목(`CFBundleURLTypes` 아래)에 붙여 넣습니다. 이 값이 자신의 Firebase 프로젝트와 일치해야 Google 로그인이 동작합니다.
    7. `Next` 버튼을 클릭하고, 이 프로젝트에는 이미 되어 있으므로 세 번째(Add Firebase SDK)와 네 번째(Add initialization code) 단계는 건너뜁니다.
    8. Firebase 콘솔로 이동해 iOS 앱에서 사용할 Firebase 서비스를 구성합니다.

   <p>
     <img src="../../../../../docs/images/firebase_google_service_info_plist.jpg" alt="Add IOS Application" width="35%"/>
     <img src="../../../../../docs/images/firebase_continue_to_console_ios.jpg" alt="Bundle ID Name" width="35%"/>
   </p>

- Firebase 프로젝트에서 `Google 로그인 인증을 활성화`하려면 다음 단계를 따르세요.
    1. Firebase 콘솔로 이동해 프로젝트를 선택합니다.
    2. 왼쪽 `사이드바 메뉴`에서 `All products` 옵션을 클릭합니다.
    3. `Authentication`을 클릭합니다.
    4. `Get Started` 버튼을 클릭합니다.
    5. `Google 아이콘`을 클릭합니다.
    6. `토글 버튼`을 클릭해 Google 로그인 인증을 `활성화`합니다.

    <p>
    <img src="../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="44.5%"/>
    <img src="../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="48%"/>
    </p>
    <p>
    <img src="../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="60%"/>
    <img src="../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="31.5%"/>
    </p>

### iOS 프로젝트 설정

CocoaPods로 Firebase SDK를 설치합니다. Mac에서 터미널 앱을 열고 Xcode 프로젝트의 루트 디렉터리로 이동합니다. 다음 명령을 실행해 Xcode 프로젝트의 루트 디렉터리에 Podfile을 만듭니다.

```
pod init
```

원하는 텍스트 편집기로 Podfile을 열고 파일 끝에 다음 줄을 추가합니다.

```
pod 'Firebase/Core'
pod 'Firebase/Storage'
pod 'Firebase/Auth'
pod 'GoogleSignIn'
pod 'GoogleSignInSwiftSupport'
pod 'GoogleAPIClientForREST/Drive' 
```

Podfile을 저장하고 닫은 다음, 다음 명령으로 Firebase SDK를 설치합니다.

```
pod install
```

### 문제 해결

Firebase 설정 과정에서 자주 생기는 문제와 해결 방법입니다.

```shell
1. CocoaPods 설치 오류: pod install이 실행되지 않거나 올바른 Firebase SDK 버전이 설치되지 않는 등 CocoaPods 설치 중 문제가 생기면 다음을 시도하세요.
```

- sudo gem install cocoapods로 CocoaPods를 업데이트합니다.
- Podfile.lock 파일을 삭제하고 pod install을 다시 실행합니다.
- Firebase 콘솔에 표시된 정확한 버전으로 Podfile에 Firebase SDK pod를 올바르게 추가했는지 확인합니다.
```shell
2. Pod 다시 설치(빌드나 패키지 관련 문제가 생길 때)
```
- pod deintegrate 
- rm -rf Pods
- rm -rf Podfile.lock
- pod install

```shell
3. Firebase 구성 오류: 앱에서 Firebase를 초기화할 수 없는 등 Firebase를 제대로 구성할 수 없으면 다음을 시도하세요.
```

- 이 문서의 "iOS 프로젝트 설정" 섹션의 모든 단계를 정확히 따랐는지 다시 확인합니다.
- Firebase 구성 파일(GoogleService-Info.plist)을 Xcode 프로젝트에 제대로 추가했는지 확인합니다.
- Firebase SDK가 Xcode 프로젝트에 제대로 추가되었고 코드에서 임포트되는지 확인합니다.
- 번들 ID와 기타 프로젝트 설정을 포함해 Firebase 콘솔에서 Firebase 프로젝트가 올바르게 설정되었는지 확인합니다.
- 다른 라이브러리와의 충돌: 의존성이 충돌하거나 SDK 버전이 호환되지 않는 등 프로젝트의 다른 라이브러리와 충돌이 생기면 다음을 시도하세요.
- Podfile에 올바른 Firebase SDK 버전을 지정했는지 확인합니다.
- 다른 라이브러리를 최신 버전으로 업데이트해 문제가 해결되는지 확인합니다.
- 충돌하는 의존성이나 라이브러리 버전이 있는지 확인하고, 해당 라이브러리를 제거하거나 업데이트해 해결합니다.
