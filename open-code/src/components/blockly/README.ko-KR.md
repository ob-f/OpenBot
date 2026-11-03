## OpenBot PlayGround:

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

### OpenBot 로봇 정보:

- OpenBot 로봇은 내부에 다음 구성 요소를 갖추고 있습니다.
    - 센서: 전압 분배기, 소나, 범퍼, 속도.
    - 바퀴 주행계: 앞바퀴와 뒷바퀴.
    - LED: 방향 지시등, 앞, 뒤, 상태.
    - 모터: 전진과 후진.

### OpenBot PlayGround 카테고리:

- ### 제어:

  OpenBot PlayGround에는 반복, 이벤트, 조건문처럼 프로그램의 흐름을 제어하는 데 쓰는 블록이 있습니다. 프로그램의 논리를 시각적으로 구성할 수 있습니다.

    <img src="../../../../docs/images/playground_blockly_control.jpg" height="50%" width="50%"/>

  몇 가지 제어 블록을 간단히 소개합니다.
    - 시작: 프로그램 실행을 시작하고 블록 코드를 한 번 실행합니다.
    - 조건문: ``If``와 ``If-else`` 블록으로 코드에 조건 분기를 만듭니다.
        - 기다리기: 코드 실행을 잠시 멈추거나 늦춥니다.
        - 영원히: 영원히 블록을 넣으면 무한 반복이 만들어져, 반복이 끝없이 계속됩니다.

- ### 반복:

  반복 카테고리에는 반복을 통해 프로그램의 흐름을 제어하는 여러 블록이 있습니다.

     <img src="../../../../docs/images/playground_blockly_loops.jpg" height="50%" width="50%"/>

  반복 블록의 예는 다음과 같습니다.

    - 반복하기: ``Repeat`` 블록으로 블록 묶음을 몇 번 실행할지 정할 수 있습니다.
    - ~하는 동안: ``While`` 블록은 지정한 조건이 참인 동안 블록 묶음을 계속 실행합니다.

- ### 연산자:

  연산자로 프로그램 안에서 여러 연산이나 계산을 할 수 있습니다. 필요에 따라 복잡한 식과 조건을 만들 수 있습니다.

  <img src="../../../../docs/images/playground_operator_blocks.jpg" height="50%" width="50%"/>

  OpenBot PlayGround에서 볼 수 있는 대표적인 연산자는 다음과 같습니다.

    - 산술: 덧셈, 뺄셈, 곱셈, 나눗셈 등의 산술 연산이 이 카테고리에 있습니다.
    - 수학 연산자: "거듭제곱", "제곱근", "임의의 분수" 같은 블록으로 더 복잡한 수학 계산을 합니다.


- ### 변수:

  변수는 블록 안에서 데이터를 저장하는 데 쓰이며, 변수 카테고리의 블록으로 변수를 선언하고, 설정하고, 바꾸고, 다룰 수 있습니다. OpenBot PlayGround의 변수로 프로그램의 데이터를 관리하고 다룰 수 있습니다.

  <img src="../../../../docs/images/playground_variable_blocks.jpg" height="50%" width="50%"/>

  변수 블록의 예를 살펴보세요.

    - 설정: 변수 설정 블록은 변수에 값을 지정합니다.
    - 바꾸기: 기존 변수의 값을 바꿉니다.

- ### 조명:

  조명은 OpenBot PlayGround가 제공하는 또 다른 카테고리로, 방향 지시등을 사용하고 밝기 값을 동적으로 설정할 수 있게 해 줍니다.

  <img src="../../../../docs/images/playground_light_blocks.jpg" height="50%" width="50%"/>

  예는 다음과 같습니다.
    - 방향 지시등: 방향 지시등을 켜거나 끄는 블록입니다.
    - 밝기: 동적인 값으로 후미등과 전조등 LED의 밝기를 설정합니다.
    - [데모 영상](https://www.youtube.com/watch?v=ICSi-1ip22U&list=PLXGqaCYqUFZDK6CWP1bP3J6upNOFDbvkA&index=3)

  참고: 밝기를 0으로 두면 밝기 모드가 꺼지고, 최고값인 100으로 두면 밝기 모드가 켜집니다.

- ### 컨트롤러:

  컨트롤러 블록에서 모드를 선택하면 OpenBot 로봇 앱의 다른 모든 화면에 똑같이 적용됩니다.

  <img src="../../../../docs/images/playground_controller_blocks.jpg" height="50%" width="50%"/>

  컨트롤러 블록의 예는 다음과 같습니다.

    - 컨트롤러 전환: 게임패드와 휴대폰 중에서 컨트롤러 방식을 고릅니다.
    - 주행 모드: 조이스틱, 게임, 듀얼 중에서 주행 모드를 바꿉니다.

   <p style="color:yellow ">팁: 컨트롤러로 휴대폰을 선택하면 블록에서 고른 주행 모드와 상관없이 로봇 앱의 주행 모드가 자동으로 듀얼로 설정됩니다.</p>

- ### 소리:

  소리 블록으로 주행 모드와 로봇 속도에 대한 소리를 재생할 수 있습니다.

  <img src="../../../../docs/images/playground_sound_blocks.jpg" height="50%" width="50%"/>

  예를 들어 보겠습니다.

    - 속도: 느림, 보통, 빠름 소리를 재생합니다.
    - 모드: 듀얼, 조이스틱, 게임 소리를 재생합니다.
    - [데모 영상](https://www.youtube.com/watch?v=8oPbCGZmwTU&list=PLXGqaCYqUFZDK6CWP1bP3J6upNOFDbvkA&index=1)

- ### 센서:

  센서 블록은 OpenBot의 상태와 주변 환경에 대한 여러 측정값을 돌려줍니다.

  <img src="../../../../docs/images/playground_sensors_blocks.jpg" height="50%" width="50%"/>

  개요:
    - 휴대폰 센서: 자이로스코프, 가속도, 자기장 값을 세 축(3차원)으로 측정합니다.
    - 자동차 센서: 소나, 속도 같은 측정값을 제공합니다. 범퍼가 장애물에 부딪혔는지도 확인합니다.
    - [데모 영상](https://www.youtube.com/watch?v=Ubcb1bTgfuc&list=PLXGqaCYqUFZDK6CWP1bP3J6upNOFDbvkA&index=2)

- ### 이동:

  이름 그대로 로봇을 원하는 속도와 방향으로 움직이며, 속도 범위는 0–255입니다.

  <img src="../../../../docs/images/playground_movement_blocks.jpg" height="50%" width="50%"/>

  예를 들어 보겠습니다.

    - 속도 설정: 속도를 느림, 보통, 빠름으로 설정합니다.
    - 이동: 원하는 속도로 앞뒤, 좌우로 움직입니다.
    - [데모 영상](https://www.youtube.com/watch?v=X7M45-PGOVg&list=PLXGqaCYqUFZDK6CWP1bP3J6upNOFDbvkA&index=4)

  핵심:
    - 왼쪽 속도를 오른쪽보다 낮게 설정하면 로봇은 시계 반대 방향으로 움직이고, 반대로 하면 시계 방향으로 움직입니다.
    - 왼쪽과 오른쪽 속도를 같게 하면 똑바로 움직입니다.
    - 왼쪽에 양수, 오른쪽에 음수를 설정하면 로봇이 제자리에서 돕니다.


- ### 인공지능(AI):

  OpenBot Playground에는 인공지능이라는 중요한 카테고리도 있어, 물체 추적, 자율 주행, 목표 지점 내비게이션 같은 기능을 설정할 수 있습니다.

  <img src="../../../../docs/images/playground_ai_blocks.jpg" height="50%" width="50%"/>

  블록 예를 통해 살펴보겠습니다.
    - ``Object Tracking``: 주된 기능은 물체 감지입니다. 이 AI 화면에서 추적할 물체를 고를 수 있습니다. 휴대폰 성능에 따라 물체 감지 모델을 고를 수 있으며, 기본 모델은 "MobileNetV1-300"입니다. 원하는 모델을 직접 추가할 수도 있습니다.
    - ``AutoPilot``: 데이터 수집을 활용하며, 미리 학습된 모델(ML 모델 CIL-Mobile-Cmd)이 포함되어 있습니다. 실행하면 카메라 화면이 나타나고 기록된 경로를 따라가기 시작합니다.
    - ``Point Goal Navigation``: 내비게이션으로 지정한 지점에 도달하는 것이 목적입니다. 블록 안의 내비게이션 모델로 3차원 공간에서 앞쪽과 왼쪽 값을 설정할 수 있습니다. 휴대폰에서 프로젝트를 실행하면 증강 현실(AR) 화면으로 목표 지점 내비게이션 화면이 나타나고, 로봇이 목표에 도달할 때까지 움직입니다.
    - [데모 영상](https://www.youtube.com/watch?v=MPzbwUoG97Q&list=PLXGqaCYqUFZDK6CWP1bP3J6upNOFDbvkA&index=6)

   <p style="color: yellow">팁: 외부 모델을 추가했다면 Playground에서 AutoSync를 켜세요. 새로 추가한 모델이 블록에 표시되고, 로봇 앱에서 모델을 사용할 수 있고 제대로 다운로드되었는지 확인하는 데 도움이 됩니다.</p>

- ### 고급 인공지능(AI):

  OpenBot Playground에는 고급 인공지능(AI) 카테고리도 있어, 감지와 자율 주행 기능을 위한 모듈형 블록을 제공합니다.

  <img src="../../../../docs/images/playground_advance_ai_blocks.jpg" height="50%" width="50%"/>

  #### 다중 감지 블록:

    - 이 고급 모듈은 사람, 자동차, 책, 신호등 같은 여러 클래스의 물체 추적을 위해 만들어졌습니다. 물체 식별은 내장된 AI 모델이 수행하며, 동작은 지정한 조건에 따라 달라집니다.
    - 이 블록은 여러 물체 감지를 지원하며, 지정한 클래스에 대해 감지를 시작합니다. 선택한 클래스가 감지되면 로봇은 첫 번째 'do' 문에 있는 작업을 모두 실행합니다. 지정한 연속 프레임 수 안에 해당 클래스가 감지되지 않으면 다음 ``do`` 문에 있는 작업을 실행합니다. 이 블록은 서로 다른 클래스에 대해 Playground에서 여러 번 사용할 수 있습니다.
