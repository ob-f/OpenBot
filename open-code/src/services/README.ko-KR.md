## Firebase Google 로그인 인증

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

- #### 사용
  웹 애플리케이션에서는 OpenBot Playground 프로젝트를 Google Drive에 업로드하기 위해 Firebase Google 로그인 인증을 사용합니다. 이 프로젝트를 복제해 자신의 기기에서 실행하려면 로그인 인증에 Firebase 구성이 필요하므로 자신의 Firebase 프로젝트를 설정해야 합니다.
- #### Google 로그인 소개
  Firebase Google 로그인 인증은 사용자가 Google 계정으로 모바일 앱이나 웹 앱에 로그인할 수 있게 해 주는 Firebase 플랫폼의 기능입니다. 별도의 로그인 정보를 기억하고 관리하지 않아도 앱에 안전하고 편리하게 접근할 수 있습니다. Firebase는 Google로 사용자 신원을 확인하는 단계부터 앱 안에서 사용자 경험을 맞춤화하는 데 쓸 수 있는 고유 사용자 ID를 제공하는 단계까지 인증 과정 전체를 관리합니다. 또한 2단계 인증 같은 추가 보안 기능으로 무단 접근으로부터 사용자 계정을 보호합니다.

****

### Firebase 프로젝트 설정

- Firebase 콘솔(https://console.firebase.google.com/)로 이동해 Google 계정으로 로그인합니다.

- `Add Project` 버튼을 클릭해 새 Firebase 프로젝트를 만듭니다.

- 프로젝트 이름을 입력하고 국가/지역을 선택한 다음 `Create Project` 버튼을 클릭합니다.
    <p align="left">
    <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%" src="../../../docs/images/firebase_create_project.jpg" alt="Create New Project" width="30%"/>
    <img style="padding-right: 2%;padding-top: 2%; padding-bottom: 2% " src="../../../docs/images/firebase_success_creation.jpg" alt="Create New Project" width="30%"/>
    </p>

- 프로젝트가 만들어지면 `Web` 아이콘을 클릭해 웹 앱에 Firebase를 추가하고, 앱 닉네임을 입력한 다음 `Register App` 버튼을 클릭합니다.
  <p align="left">
  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_web_icon.jpg" alt="Create New Project" width="30%"/>
  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_register_app.jpg" alt="Create New Project" width="30%"/>
  </p>

    - 프로젝트의 `env` 파일에 `firebase SDK` 설정을 추가합니다.
        - 프로젝트를 만들 때 여기에서 Firebase 설정을 받거나, 프로젝트 설정에서 가져올 수 있습니다.
          <p align="left">
          <img style="padding-right: 2%;padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_sdk.jpg" alt="Create New Project" width="30%"/>
          <img style="padding-right: 2%;padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_project_setting.jpg" alt="Create New Project" width="30%"/>
          <img style="padding-right: 2%;padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_project_setting_config.jpg" alt="Create New Project" width="30%"/>
          </p>

        - 환경 변수 사용: Firebase 인증을 사용할 때는 API 키, 데이터베이스 자격 증명 등 민감한 정보를 저장해야 할 수 있습니다. 이런 정보는 환경 변수를 사용해 코드 밖에 안전하게 보관할 수 있습니다. 다음 단계를 따르세요.

            1. OpenBot Playground 안에 .env라는 새 파일을 만듭니다.
                 <p align="left">
                <img style="padding-right: 2%;padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_env.jpg" alt="Create New Project" width="30%"/>
                 </p> 

            2. firebase.js 파일에서 사용할 다음 환경 변수를 .env 파일에 추가합니다.

            ```bash
              REACT_APP_FIREBASE_API_KEY=<REACT_APP_FIREBASE_API_KEY>
              REACT_APP_AUTH_DOMAIN=<REACT_APP_AUTH_DOMAIN>
              REACT_APP_PROJECT_ID=<REACT_APP_PROJECT_ID>
              REACT_APP_STORAGE_BUCKET=<REACT_APP_STORAGE_BUCKET>
              REACT_APP_MESSAGING_SENDER_ID=<REACT_APP_MESSAGING_SENDER_ID>
              REACT_APP_APP_ID=<REACT_APP_APP_ID>
              REACT_APP_MEASUREMENT_ID=<REACT_APP_MEASUREMENT_ID>
              GENERATE_SOURCEMAP=false
            ```

- Firebase 인증에서 Google 로그인 방법을 활성화합니다.

  <p align="left">

  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_authentication.jpg" alt="Create New Project" width="30%"/>

  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_option.jpg" alt="Create New Project" width="30%"/>

  <img style="padding-right: 2%;padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_signin_enable.jpg" alt="Create New Project" width="22.6%"/>

  </p>


- Firestore 데이터베이스를 활성화하려면 왼쪽 사이드바의 Build 메뉴로 이동합니다. 옵션에서 ``Firestore Database``를 클릭한 다음 ``Create database`` 버튼을 클릭합니다.

  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firestore_database_setup.jpg" alt="Google Cloud Console" width="50%"/>

    - 안전한 규칙을 위해 ``Start in production mode``를 선택하고 앱의 Firestore 위치를 고른 다음 ``Enable`` 버튼을 클릭합니다.

      <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_database_production_build.jpg" alt="Google Cloud Console" width="30%"/>
      <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_database_location.jpg" alt="Google Cloud Console" width="30%"/>

        - 데이터베이스가 만들어지면 ``Rules``를 클릭해 읽기와 쓰기 권한을 설정합니다.

          <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_database_rules.jpg" alt="Google Cloud Console" width="30%"/>

        - 기본 규칙을 아래 코드로 바꾸고 ``Publish`` 버튼을 클릭합니다.

          ```bash
          rules_version = '2';
          service cloud.firestore {
              match /databases/{database}/documents {
                  match /{document=**} {
                      allow read, write: if request.auth != null;
                  }
              }
          }
          ```
        
### Google Drive 서비스 설정

- #### API 활성화
  Google Cloud 콘솔(https://console.cloud.google.com/)로 이동해 Firebase에서 사용하는 것과 같은 Google 계정으로 로그인합니다. 그래야 서비스 사이의 연동이 매끄럽게 이루어집니다. 페이지 맨 위에 현재 프로젝트 이름이 보입니다. 이를 클릭해 프로젝트 선택기를 연 다음, `ALL` 섹션에서 Firebase에 추가한 프로젝트를 선택해 전환합니다.

  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_cloud_console.jpg" alt="Google Cloud Console" width="30%"/>
  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_cloud_project.jpg" alt="Google Cloud Console" width="30%"/>

- 전환한 뒤 Quick access 아래에 ``APIs & Services`` 옵션이 보입니다. 이를 클릭합니다. 바로 보이지 않으면 왼쪽 위의 메뉴 아이콘(보통 가로줄 세 개)을 클릭해 메뉴를 펼쳐야 할 수 있습니다.

  <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_api_services.jpg" alt="Google Cloud Console" width="50%"/>

    - "APIs & Services"를 연 다음 ``Library`` 섹션으로 이동합니다. 여기에서 Google Drive API를 검색할 수 있습니다.
      <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_drive_library.jpg" alt="Google Cloud Console" width="50%"/>

    - 검색 결과에 Google Drive API가 나타나면 클릭합니다. 다음 페이지에서 API 정보를 확인할 수 있습니다. "Enable" 버튼을 클릭해 프로젝트에서 활성화합니다. 활성화하면 Google Drive와 Drive API 설정에 접근하고 관리할 수 있습니다.
  
      <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_drive_result.jpg" alt="Google Cloud Console" width="30%"/>
      <img style="padding-right: 2%; padding-top: 2%; padding-bottom: 2%;" src="../../../docs/images/firebase_google_drive_enable_api.jpg" alt="Google Cloud Console" width="30%"/>

### 문제 해결

Firebase 구성 과정에서 자주 생기는 문제와 해결 방법입니다.

```bash
  1. 잘못된 자격 증명 오류: 브라우저 콘솔에서 Invalid Credentials 관련 오류 메시지나 경고를 확인하세요.
```

- Firebase 콘솔에 올바른 클라이언트 ID와 API 키를 입력했는지 확인합니다.
- 환경 변수에 입력한 값에 오타나 오류가 없는지 다시 확인합니다.
- firebase.auth().signInWithPopup() 함수를 호출할 때 구성 설정을 올바르게 활성화했는지 확인합니다.
- 올바른 Firebase SDK 버전을 지정했는지, 유효한 Google 계정으로 로그인하는지 확인합니다.

```bash
  2.  사용자 계정 비활성화 오류.
```

- 이 문제를 해결하는 유일한 방법은 기존 계정을 다시 활성화하거나 새 계정을 만드는 것입니다.
- 또한 Firebase Google 로그인으로 인증하기 전에 계정이 비활성화되거나 삭제되었는지 확인하고, 계정이 활성 상태가 아니면 오류 메시지를 표시할 수 있습니다.

```bash
  3.  CORS(Cross-Origin Resource Sharing) 오류: 데이터가 로드되지 않거나 제대로 표시되지 않는 등 웹 애플리케이션이 예상대로 동작하지 않는 경우.
```

- Firebase 콘솔의 Authentication 섹션에서 "Sign-in method" 탭을 선택합니다. "Authorized domains" 섹션에 웹 애플리케이션 도메인이 추가되어 있고 CORS가 활성화되어 있는지 확인합니다.
- 서버 측 인증 흐름을 사용한다면 웹 애플리케이션 도메인의 요청을 허용하도록 서버 응답에 필요한 CORS 헤더를 추가했는지 확인합니다.
- 웹 애플리케이션을 Firebase Hosting에 호스팅하면 도메인에 CORS가 자동으로 활성화됩니다. Firebase Cloud Run을 사용해 CORS 헤더가 포함된 API 요청을 처리할 수도 있습니다.
