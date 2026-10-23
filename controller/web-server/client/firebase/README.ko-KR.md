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
  웹 애플리케이션에서는 각 클라이언트를 식별하고 로봇 앱과 웹 서버 사이의 교차 연결을 막기 위해 Firebase Google 로그인 인증을 사용합니다. 이 프로젝트를 복제해 자신의 기기에서 실행하려면 로그인 인증에 Firebase 구성이 필요하므로 자신의 Firebase 프로젝트를 설정해야 합니다.
- #### Google 로그인 소개
  Firebase Google 로그인 인증은 사용자가 Google 계정으로 모바일 앱이나 웹 앱에 로그인할 수 있게 해 주는 Firebase 플랫폼의 기능입니다. 별도의 로그인 정보를 기억하고 관리하지 않아도 앱에 안전하고 편리하게 접근할 수 있습니다. Firebase는 Google로 사용자 신원을 확인하는 단계부터 앱 안에서 사용자 경험을 맞춤화하는 데 쓸 수 있는 고유 사용자 ID를 제공하는 단계까지 인증 과정 전체를 관리합니다. 또한 2단계 인증 같은 추가 보안 기능으로 무단 접근으로부터 사용자 계정을 보호합니다.

**참고** - OpenBot Playground [문서](../../../../open-code/src/services/README.ko-KR.md)를 따라 Firebase 프로젝트를 설정하고 Google 인증을 활성화하세요. 지금은 Google Drive API를 활성화할 필요가 없습니다.

### 환경 변수 설정

Firebase 인증을 사용할 때는 API 키, 데이터베이스 자격 증명 등 민감한 정보를 저장해야 할 수 있습니다. 이런 정보는 환경 변수를 사용해 코드 밖에 안전하게 보관할 수 있습니다. 다음 단계를 따르세요.

1. web-server 안에 .env라는 새 파일을 만듭니다.

   <img src="../../images/firebase_web_server_env_variable.png" width="30%"/>

2. `authentication.js`에서 사용하는 다음 환경 변수를 `.env` 파일에 추가합니다.
   ```bash
   VITE_PUBLIC_FIREBASE_API_KEY=<VITE_PUBLIC_FIREBASE_API_KEY>
   VITE_PUBLIC_AUTH_DOMAIN=<VITE_PUBLIC_AUTH_DOMAIN>
   VITE_PUBLIC_PROJECT_ID=<VITE_PUBLIC_PROJECT_ID>
   VITE_PUBLIC_STORAGE_BUCKET=<VITE_PUBLIC_STORAGE_BUCKET>
   VITE_PUBLIC_MESSAGING_SENDER_ID=<VITE_PUBLIC_MESSAGING_SENDER_ID>
   VITE_PUBLIC_APP_ID=<VITE_PUBLIC_APP_ID>
   VITE_PUBLIC_MEASUREMENT_ID=<VITE_PUBLIC_MEASUREMENT_ID>
   ```
