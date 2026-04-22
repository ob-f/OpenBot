# Python 컨트롤러

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

이 Python 프로그램은 무선 키보드로 로봇을 제어하고, 카메라로부터 비디오 스트림을 수신할 수 있게 해줍니다. 프로그램은 로봇의 전화와 동일한 네트워크에 연결된 컴퓨터에서 실행할 수 있습니다. Raspberry Pi 3와 MacBook에서 개발 및 테스트되었습니다. 아래의 단계를 따르기 전에 [소스 코드](https://github.com/ob-f/OpenBot#get-the-source-code)를 다운로드하고 `controller` 폴더로 이동하십시오.

## 의존성

OpenBot을 위한 conda 환경을 만들 것을 권장합니다(이미 만든 경우 생략 가능). conda 설치에 대한 지침은 [여기](https://docs.conda.io/projects/conda/en/latest/user-guide/install/)에서 찾을 수 있습니다. 새로운 환경을 만들려면 다음 명령을 사용합니다:

```bash
conda create -n openbot python=3.7
```

전역적으로 의존성을 설치하고 싶지 않으면 먼저 conda 환경을 활성화하십시오:

```bash
conda activate openbot
```

이제 `controller` 폴더 내에서 로컬 OpenBot 리포지토리에 있는지 확인하고, 다음 명령으로 모든 의존성을 설치하십시오:

```bash
pip install -r requirements.txt
```

## 로봇 제어하기

**참고:** 성공적으로 연결된 후에는 로봇 앱을 다시 시작하지 않으면 다시 연결할 수 없습니다.

Python 스크립트는 들어오는 연결을 기다립니다. 로봇 앱이 설치된 전화에서 `FreeRoam` 프래그먼트로 이동하여 제어 모드를 `전화 아이콘`으로 전환합니다. 로봇은 이제 Python 스크립트와 연결을 시도합니다(컨트롤러 앱과 연결할 때와 같은 방식으로). 또는 `DefaultActivity`를 사용하여 `Phone`을 제어기로 선택할 수도 있습니다.

### Pygame 사용하기

이 스크립트들은 키보드를 사용해 자동차 경주 게임처럼 로봇을 조정할 수 있게 해줍니다.

비디오 없이 컨트롤러 실행:

`python keyboard-pygame.py`

비디오와 함께 컨트롤러 실행:

`python keyboard-pygame.py --video`

사용법은 다음과 같습니다:

```
    W:        앞으로 가기
    S:        뒤로 가기
    A:        왼쪽으로 살짝 돌기 (주행 중)
    D:        오른쪽으로 살짝 돌기 (주행 중)
    Q:        왼쪽으로 회전
    E:        오른쪽으로 회전

    M:        주행 모드
    N:        소음 토글
    Left:     왼쪽 방향 지시등
    Right:    오른쪽 방향 지시등
    Up:       방향 지시등 취소
    Down:     네트워크 모드
    SPACE:    로그 토글
    ESC:      종료
```

### Click 라이브러리 사용하기

또한 로봇 제어를 동적으로 제어하는 대신 증가 단위로 설정할 수 있는 프로토타입 스크립트가 있습니다. 이 스크립트는 click 라이브러리를 사용하며, 터미널이 포커스를 유지해야 합니다.

컨트롤러 실행:

`python keyboard-click.py`

사용법은 다음과 같습니다:

```bash
    W:        속도 증가
    S:        속도 감소
    A:        왼쪽 더 많이 돌기
    D:        오른쪽 더 많이 돌기
    R:        제어 초기화

    M:        주행 모드
    N:        소음 전환
    Left:     왼쪽 방향 지시등
    Right:    오른쪽 방향 지시등
    Up:       방향 지시등 취소
    Down:     네트워크 모드
    SPACE:    로그 전환
    ESC:      종료
```