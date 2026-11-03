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
  이 애플리케이션에서는 Google Drive에 업로드된 [OpenBot Playground](../../../../../../../../../../../open-code/README.ko-KR.md) 프로젝트에 접근하기 위해 Firebase Google 로그인 인증을 사용합니다. 이 Android 앱을 복제해 직접 빌드하려면 자신의 [Firebase 프로젝트](#firebase-프로젝트-설정)를 설정해야 합니다. Google 로그인 인증에는 앱의 SHA-1 키가 필요하기 때문입니다.
  
  [OpenBot Playground](https://www.playground.openbot.org/) 웹 애플리케이션에도 [Firebase를 설정](../../../../../../../../../../../open-code/README.ko-KR.md)해야 합니다. Android 앱은 Firebase Google Drive 서비스가 만든 파일을 사용자의 Google Drive에서 가져오기 때문입니다. Google Drive 서비스가 제대로 동작하려면 Android 앱과 웹 앱에 같은 Firebase 프로젝트를 사용해야 합니다.
  
  OpenBot Playground 서비스를 사용하지 않는다면 Firebase 인증이나 Google 로그인 인증을 설정할 필요가 없습니다.

  이 저장소에 커밋된 `google-services.json` 파일(`robot/android/app/google-services.json`과
  `robot/android/app/src/main/assets/google-services.json`)은 가짜 키와 식별자가 들어 있는 **자리 표시자**입니다.
  아래 설명대로 자신의 Firebase 프로젝트에서 내려받은 실제 파일로 바꾸기 전까지는 Google 로그인과 다른 Firebase 기능이 동작하지 않습니다.

### 사전 요구 사항
Google 로그인을 위해 OpenBot Android 앱에 Firebase를 연동하려면 다음이 필요합니다.
- **Google 계정:** Firebase로 Google 로그인을 사용하려면 Google 계정이 있어야 합니다. 계정이 없다면 여기에서 무료 Google 계정을 [만드세요](https://accounts.google.com/signup).
- **Firebase 계정:** Firebase 계정이 있어야 하며, Android 앱을 위한 새 Firebase 프로젝트를 만들어야 합니다. 새 프로젝트는 [Firebase 콘솔](https://console.firebase.google.com/)에서 만들 수 있습니다.
****

### Firebase 프로젝트 설정

- [Firebase 콘솔](https://console.firebase.google.com/)로 이동해 다음 단계에 따라 `새 프로젝트`를 만듭니다.
  1. "Create Project" 버튼을 클릭합니다.
  2. Firebase 프로젝트 이름을 입력하고 "Next" 버튼을 클릭합니다.
  3. Google Analytics를 사용하지 않으려면 비활성화할 수 있습니다.
  4. "Create Project" 버튼을 클릭합니다.
<p>
<img src="../../../../../../../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
<img src="../../../../../../../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width=40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
</p>

- Firebase 프로젝트에 새 Android 앱을 추가하려면 다음과 같이 합니다.
  1. Firebase 프로젝트에서 Android 아이콘을 클릭합니다.
  2. Android 앱의 `패키지 이름`을 입력합니다. Firebase에서 `고유`해야 합니다.
  3. 앱의 닉네임을 입력합니다.
  4. Firebase Google 로그인 서비스가 앱의 APK에 서명하려면 **SHA-1 키**가 **필수**입니다. 해당 필드에 입력합니다.
  5. `Register` 버튼을 클릭합니다.
  

- `SHA-1` 지문(키)은 `앱 서명 인증서`의 고유 식별자로, 앱이 Firebase 서버와 통신할 때 Firebase가 앱의 진위를 확인하는 데 사용합니다. 앱에서 Firebase 인증을 사용하려면 앱의 `릴리스 버전`에 서명하는 `서명 인증서의` `SHA-1` 키를 제공해야 합니다.
  
<p>
<img style="width: 47%" src="../../../../../../../../../../../docs/images/firebase_add_android_app.jpg" alt="Add Android Application" width="40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_package_name.jpg" alt="Package Name" width="36%"/>
</p>

- `SHA-1` 키는 `Java SDK`에 포함된 명령줄 도구 `keytool`로 얻을 수 있습니다. `Mac`과 `Windows`에서 사용하는 방법은 다음과 같습니다.
  

  - **Mac에서**
    ```shell
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
    ```
    이 명령은 `~/.android/debug.keystore`에 있는 디버그 서명 인증서의 세부 정보를 보여 줍니다. `-alias` 플래그는 디버그 서명 인증서를 식별하는 별칭을, `-storepass`와 `-keypass` 플래그는 각각 키 저장소와 키의 비밀번호를 지정합니다.
  

  - **Windows에서**
    ```shell 
    keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    ```
    이 명령은 Mac 명령과 비슷하지만 debug.keystore 파일을 찾는 `경로가 다릅니다`. `%USERPROFILE%`은 현재 사용자의 프로필 디렉터리를 가리키는 시스템 환경 변수이며, 이 디렉터리 안의 `.android` 디렉터리에 `debug.keystore` 파일이 있습니다.

- `google-services.json` 파일을 내려받아 앱의 `app 디렉터리`(`robot/android/app/google-services.json`)와 `assets 디렉터리`(`robot/android/app/src/main/assets/google-services.json`)에 `추가`합니다.
- 다음 버튼을 클릭합니다. 이 프로젝트의 gradle 파일에 이미 **Firebase SDK가 추가**되어 있으므로 세 번째 단계는 건너뜁니다.
- Firebase 콘솔로 이동해 Android 앱에서 사용할 Firebase 서비스를 구성합니다.

<p>
<img style="width: 40%" src="../../../../../../../../../../../docs/images/firebase_google_service_json_file.jpg" alt="Google services json file" width="41%"/>
<img src="../../../../../../../../../../../docs/images/firebase_continue_to_console.jpg" alt="Continue to console" width="40.2%"/>
</p>

- Firebase 프로젝트에서 `Google 로그인 인증을 활성화`하려면 다음 단계를 따르세요.
  1. Firebase 콘솔로 이동해 프로젝트를 선택합니다.
  2. 왼쪽 `사이드바 메뉴`에서 `All products` 옵션을 클릭합니다.
  3. `Authentication`을 클릭합니다.
  4. `Get Started` 버튼을 클릭합니다.
  5. `Google 아이콘`을 클릭합니다.
  6. `토글 버튼`을 클릭해 Google 로그인 인증을 `활성화`합니다.

<p>
<img style="width:36%" src="../../../../../../../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="39%"/>
</p>
<p>
<img src="../../../../../../../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="26.1%"/>
</p>


- 설정 과정에서 `SHA-1 키를 추가하는 단계를 건너뛰었다면` 다음 단계로 나중에 `추가할 수 있습니다`.

  - `Firebase 콘솔`로 이동해 `프로젝트`를 선택합니다.
  - `왼쪽 위`의 `톱니바퀴 아이콘`을 클릭하고 `Project settings`를 선택합니다.
  - `Your apps` 섹션에서 `SHA-1 키를 추가`할 `Android 앱`을 선택합니다.
  - `SHA certificate fingerprints` 섹션까지 `아래로 스크롤`하고 `Add fingerprint`를 클릭합니다.
  - 앱 서명 인증서의 `SHA-1 키`를 입력합니다.
  - `Save`를 클릭해 Firebase 프로젝트에 SHA-1 키를 추가합니다.

<p>
<img style="width: 44%;" src="../../../../../../../../../../../docs/images/firebase_gear_icon.jpg" alt="Gear icon" width="30%"/>
<img style="width: 53%" src="../../../../../../../../../../../docs/images/firebase_add_sha1.jpg" alt="Add SHA-1" width="43%"/>
</p>

- SHA-1 키를 추가하기 전에 `이미 Firebase 인증을 구현했다면` 앱의 `구성`을 `업데이트`해야 할 수 있습니다. 프로젝트 디렉터리의 `google-services.json` 파일을 Firebase 프로젝트 설정의 `업데이트된 google-services.json` 파일로 `바꾸세요`.

<p>
<img src="../../../../../../../../../../../docs/images/firebase_updated_google_service_json_file.jpg" alt="Updated Google service json file" width="40%"/>
</p>
