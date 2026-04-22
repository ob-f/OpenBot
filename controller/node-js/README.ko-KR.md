# OpenBot Nodejs 컨트롤러

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

## 용어 정리

이 문서에서 사용될 몇 가지 용어는 다음과 같습니다:

* **`Robot, bot`** - [OpenBot](https://www.openbot.org/) 차량의 Android 소프트웨어를 의미합니다. 이 소프트웨어는 로봇의 물리적 움직임과 기능을 제어합니다.
* **`Server`** - 이 프로젝트의 서버 측 컴포넌트를 호스팅하는 노드 서버입니다. 로봇과 클라이언트 간의 통신을 용이하게 하며, 데이터 처리, WebSocket 연결 및 기타 서비스를 처리합니다.
* **`Client, UI`** - 프로젝트의 클라이언트 측으로, 보통 웹 브라우저를 통해 접근합니다. 사용자는 이 인터페이스를 통해 로봇을 제어하고, 비디오 스트림을 보고, 시스템의 다른 기능들과 상호작용할 수 있습니다.

## 소개

이 프로젝트는 [Node.js](https://nodejs.org/) 기반으로 [OpenBot](https://www.openbot.org/) 차량을 제어하는 소프트웨어입니다. 이 소프트웨어는 서버와 클라이언트로 구성됩니다. 서버는 로봇과 같은 네트워크에 있는 컴퓨터에서 실행되는 Node.js 애플리케이션입니다. 클라이언트는 브라우저에서 실행됩니다.

다음은 브라우저의 스크린샷입니다:

![스크린샷](images/Screenshot.png "image_tooltip")

## 시작하기

이 소프트웨어는 PC, RaspberryPi 타입의 장치 또는 [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/)와 같은 Node.js 환경을 지원하는 장치에서 실행할 수 있습니다. 먼저 [Node.js](https://nodejs.org/) 버전 10 이상이 설치되어 있는지 확인하세요. 버전을 확인하려면 다음 명령어를 입력하세요:

    node --version

소프트웨어는 OpenBot 프로젝트의 ```/controller/node-js``` 디렉토리에 있습니다. [GitHub](https://github.com/ob-f/OpenBot)에서 코드를 다운로드한 후, 이 디렉토리로 이동하여 다음 명령어를 실행합니다:

    npm install
    npm start

마지막 명령어는 서버를 시작합니다. 터미널 없이 백그라운드에서 서버를 실행하려면, ```Linux/MacOS```에서 다음 명령어를 사용할 수 있습니다:

    npm run start-nohup

또는 간단히:

    nohup npm start

브라우저에서 서버의 IP 주소와 포트 8081로 접속하세요. 예를 들어 [http://localhost:8081](http://localhost:8081) 또는 [http://192.168.1.100:8081](http://192.168.1.100:8081)입니다. 다른 컴퓨터에서도 서버에 접근할 수 있지만, 로봇, 서버 및 브라우저 PC가 동일한 네트워크에 있어야 합니다. 미래에는 원격으로 서버에 접근할 수 있는 기능을 추가할 예정입니다.

로봇이 동일한 네트워크에 연결되어 있는지 확인하세요. 로봇의 Android 앱에서 ```General``` 패널로 이동한 후, ```Phone```을 제어기로 선택하세요. 그러면 Android 앱이 Node 서버에 연결되고 UI에 영상이 표시됩니다.

## 작동 원리

1. Node 서버는 ```openbot.tcp``` 유형의 DNS 서비스를 생성하고, 포트 19400에서 ```OPEN_BOT_CONTROLLER```라는 이름으로 게시합니다. 이를 통해 로봇은 서버의 IP 주소를 알지 못해도 자동으로 서버를 찾을 수 있습니다. 로봇은 이 서비스를 찾고, ```Phone``` 컨트롤러 모드로 설정되면 소켓 연결을 설정합니다.

2. Node 서버는 포트 8081에서 HTTP 서버를 생성하고, 브라우저로부터 요청을 처리하기 시작합니다.

3. 또한, Node 서버는 포트 7071에서 WebSocket 서버를 생성합니다. 이 서버는 브라우저와 직접 통신하는 데 사용됩니다. 지금까지 요약하자면, 서버는 로봇과 브라우저를 위한 두 개의 소켓 연결을 생성했습니다.

4. 사용자는 브라우저에서 키보드 명령을 입력합니다. 이 키 입력은 WebSocket을 통해 서버로 전송됩니다. 서버는 이를 로봇이 이해할 수 있는 명령으로 변환합니다. 예를 들어, ```{driveCmd: {r:0.4, l:0.34}}```와 같은 명령으로 변환됩니다 (모든 명령 목록은 안드로이드 컨트롤러 문서 [여기](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)에서 확인할 수 있습니다). 이 명령은 소켓 연결을 통해 로봇으로 전송됩니다.

5. 로봇은 상태 정보를 소켓 연결을 통해 서버로 전송하고, 서버는 이를 UI로 전달합니다. UI는 이 정보를 사용하여 표시를 향상시킬 수 있지만, 현재 이 상태는 무시되고 있습니다.

6. Node 서버는 WebRTC 신호 프록시 역할도 합니다. 로봇과 브라우서 간에 WebRTC 협상 명령을 전달합니다. 이 목적을 위해 열린 소켓 연결을 재사용하므로 추가적인 연결이나 설정이 필요하지 않습니다.

![작동 원리](images/HowItWorks.png)

## 개발

이 코드는 빠르고 가벼운 빌드 도구인 [snowpack](https://www.snowpack.dev/)을 사용합니다.

[eslint](https://eslint.org/)를 사용하여 코드의 문법을 검사하고 자동으로 포맷을 적용합니다. 새로운 코드를 커밋하기 전에 린트를 실행하고 오류를 수정하는 것이 좋습니다. Visual Code를 사용 중이라면, [여기](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint)에서 플러그인을 설치할 수 있습니다. 린터를 다음과 같이 실행할 수 있습니다:

    npm run lint

## 프로덕션

```client```의 프로덕션 버전을 빌드하려면 다음 명령을 실행하세요:

    npm run build

이 명령은 클라이언트 코드를 최적화하여 ```build``` 디렉토리로 저장하며, 이 디렉토리는 서버에 배포할 수 있습니다. 또한, 서버를 재시작할 프로세스 관리자를 설정하고, 아마도 [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)와 같은 리버스 프록시도 설정해야 합니다. (이는 아직 구현되지 않았습니다.)

## 문제 해결

* 가끔 브라우저에서 명령 메뉴만 표시되고 제목은 보이지만, 명령이 보이지 않는 경우가 있습니다. 이는 WebSocket 연결이 설정되지 않았다는 의미입니다. 보통 서버 시작 직후에 발생합니다. 브라우저 콘솔을 살펴보면 ```WebSocket connection to 'ws://localhost:7071/ws' failed```와 같은 메시지를 볼 수 있습니다. 모든 Node 프로세스를 종료한 후 (pkill -9 node) 다시 시작하고 페이지를 새로 고침하면 연결이 설정됩니다.
* 앱에 전화기를 연결할 수 없는 경우, 이 애플리케이션의 다른 인스턴스가 동일한 네트워크 상의 다른 컴퓨터에서 실행되고 있지 않은지 확인해보세요.

## 알려진 버그

없음.

## 할 일/시도해볼 것들

* 원격으로 서버에 연결할 수 있는지, WebRTC가 여전히 작동하는지 조사해야 합니다. 이를 가능하게 하기 위한 방화벽 설정을 문서화해야 합니다.
* ```production``` 구성을 만들어야 합니다. 아마도 [pm2 프로세스 관리자](https://www.npmjs.com/package/pm2)와 [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)를 사용할 수 있습니다.
