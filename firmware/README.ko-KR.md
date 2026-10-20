# 펌웨어

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

우리는 마이크로컨트롤러 유닛(MCU)을 사용하여 로봇 본체와 스마트폰 간의 다리 역할을 합니다. 우리는 ATmega328P 마이크로컨트롤러가 장착된 Arduino Nano와 ESP32 개발 키트를 위한 [펌웨어](openbot/openbot.ino)를 제공합니다.

## 기능

MCU의 주요 작업은 차량의 저수준 제어를 처리하고 차량에 장착된 저수준 센서로부터 읽기를 제공하는 것입니다. MCU는 직렬 연결을 통해 차량 제어 및 표시등 신호를 수신합니다. 제어를 모터 컨트롤러용 PWM 신호로 변환하고 표시등 신호에 따라 LED를 전환합니다. Arduino 프로그램은 또한 왼쪽 및 오른쪽 앞바퀴의 광학 센서 인터럽트를 계산하여 바퀴 회전을 추적합니다. 전압 분배 회로에서 측정한 값을 스케일링된 이동 평균으로 계산하여 배터리 전압을 계산합니다. 또한 선택적 초음파 센서를 사용하여 자동차 앞의 장애물까지의 거리를 측정할 수 있습니다. 이러한 측정값은 직렬 링크를 통해 Android 애플리케이션으로 다시 전송됩니다.

## 설정

모든 설정은 `openbot.ino` 맨 위의 **SETTINGS** 섹션에 있습니다. 그 아래는 바꿀 필요가 없습니다.

펌웨어를 업데이트할 때 설정을 유지하려면 SETTINGS 섹션의 `#else`와 `#endif` 사이 줄을 `openbot.ino` 옆의 새 파일 `openbot_settings.h`에 복사하고 거기서 바꾸세요. `openbot_settings.h`가 있으면 펌웨어는 `openbot.ino`의 섹션 대신 이 파일을 사용하며, git이 이 파일을 무시하므로 실수로 푸시되지 않습니다.

### 로봇 바디

`OPENBOT`를 로봇 바디로 설정하고 Arduino IDE에서 해당 바디가 지원하는 보드를 선택하세요. 펌웨어가 보드를 감지하며, 다른 보드에서는 빌드가 오류와 함께 중지됩니다.

| 바디 | `OPENBOT` | Arduino Nano | ESP32 Dev Module |
|---|---|:---:|:---:|
| DIY 빌드 (L298N 모터 드라이버) | `DIY` | ✓ | ✓ |
| 커스텀 PCB V1 / V2 | `PCB_V1` / `PCB_V2` | ✓ | |
| Ready-to-Run 키트, TT 모터 | `RTR_TT` | ✓ | |
| Ready-to-Run 키트, TT 모터, ESP32 | `RTR_TT2` | | ✓ |
| Ready-to-Run 키트, 520 모터 | `RTR_520` | | ✓ |
| RC 트럭 | `RC_CAR` | ✓ | |
| OpenBot Lite | `LITE` | ✓ | |
| Multi Terrain Vehicle | `MTV` | | ✓ |

`DIY_ESP32`는 ESP32 보드를 선택한 `DIY`로 대체되었습니다.

### 주행

기본적으로 로봇은 휴대폰 앱으로 조종합니다. [No Phone 모드](#no-phone-모드)를 사용하려면 `NO_PHONE_MODE 1`을 설정하세요. ESP32에서는 [게임패드](#게임패드-조종)로도 주행할 수 있습니다 (`HAS_GAMEPAD 1`). `COAST_MODE`는 조종 입력이 없을 때 모터가 관성으로 굴러갈지(1) 브레이크를 걸지(0) 정하며, `DEBUG 1`은 시리얼 모니터에 디버그 출력을 표시합니다.

### 부품

각 바디는 기본으로 갖춘 부품을 켭니다:

| `OPENBOT` | 켜지는 부품 |
|---|---|
| `DIY`, `PCB_V1`, `PCB_V2` | 전압 분배기, 방향 지시등, 소나, 앞 속도 센서 |
| `RTR_TT`, `RTR_TT2`, `RTR_520` | 전압 분배기, 방향 지시등, 소나, 범퍼, 앞뒤 속도 센서, 앞·뒤·상태 LED |
| `LITE` | 방향 지시등 |
| `RC_CAR` | 없음 |
| `MTV` | 앞·중간·뒤 속도 센서 |

ESP32에서는 휴대폰과의 블루투스 연결도 켜집니다.

설정에서 각 부품은 바디의 기본값으로 되어 있으며(예: `#define HAS_SONAR DEFAULT_HAS_SONAR`), 그 위의 표에 바디별 기본값이 나와 있습니다. 부품을 바꾸려면 대신 `1`(켜기) 또는 `0`(끄기)으로 설정하세요. 예를 들어 초음파 센서를 설치하지 않았다면 `#define HAS_SONAR 0`입니다. 켜져 있지만 설치되지 않은 부품은 해가 없지만, 앱에 300 cm 소나 거리처럼 의미 없는 값이 표시됩니다.

| 설정 | 부품 |
|---|---|
| `HAS_VOLTAGE_DIVIDER` | 배터리 전압 측정 |
| `HAS_INDICATORS` | 방향 지시등 |
| `HAS_SONAR`, `SONAR_MEDIAN` | 초음파 센서와 측정값용 중앙값 필터 |
| `HAS_BUMPER` | 충돌 감지용 범퍼 |
| `HAS_SPEED_SENSORS_FRONT`, `_BACK`, `_MIDDLE` | 바퀴 속도 센서 |
| `HAS_LEDS_FRONT`, `HAS_LEDS_BACK`, `HAS_LEDS_STATUS` | 전조등, 후미등, 상태 LED |
| `HAS_OLED` | OLED 디스플레이 |
| `HAS_BLUETOOTH` | 휴대폰과의 블루투스 연결 (ESP32) |

### 배터리

앱의 배터리 표시는 `VOLTAGE_LOW`(빈 상태)에서 `VOLTAGE_MAX`(가득 찬 상태)까지입니다. 바디의 기본값으로 설정되어 있으며, 다른 배터리를 쓴다면 설정에서 바꾸세요. 예: `#define VOLTAGE_LOW 6.8f`

| 배터리 | `VOLTAGE_LOW` | `VOLTAGE_MAX` |
|---|---:|---:|
| 2S 리튬이온 / LiPo | 6.8f | 8.4f |
| 3S 리튬이온 / LiPo | 10.2f | 12.6f |
| AA 6개 | 6.0f | 9.0f |
| AA 8개 | 8.0f | 12.0f |

전압 분배기가 바디의 것과 다르면 `VOLTAGE_DIVIDER_FACTOR`도 (R1 + R2) / R2로 설정하세요. `VOLTAGE_MIN`은 모터가 아직 회전하는 가장 낮은 전압이며, 펌웨어는 No Phone 모드에서만 사용합니다.

### 구성 확인

로봇이 시작되면 시리얼 모니터(115200 baud)에 바디와 켜진 부품이 표시됩니다. 예:

```
OpenBot DIY (ESP32): voltage, indicators, sonar, speed sensors front, Bluetooth
```

두 부품이 같은 핀을 사용하면 빌드가 "Two parts use the same pin" 오류와 함께 중지됩니다.

### 종속성

속도 센서 또는 초음파 센서를 활성화한 경우 [PinChangeInterrupt](https://github.com/NicoHood/PinChangeInterrupt) 라이브러리를 설치해야 합니다. Arduino Nano에는 두 개의 외부 인터럽트 핀(D2 및 D3)만 있으며 D3는 PWM을 지원하는 6개 핀 중 하나이기도 합니다. 다행히도 Arduino의 모든 핀을 커버하는 세 개의 포트 인터럽트가 있습니다. 이 라이브러리는 이러한 포트 인터럽트를 구문 분석하여 Arduino의 모든 핀을 인터럽트로 사용할 수 있도록 합니다.

OLED를 활성화한 경우 [Adafruit_SSD1306](https://github.com/adafruit/Adafruit_SSD1306) 및 [Adafruit_GFX Library](https://github.com/adafruit/Adafruit-GFX-Library) 라이브러리를 설치해야 합니다.

라이브러리를 설치하려면 다음 단계를 따르세요:

1. 라이브러리 관리자 열기: `도구` :arrow_right: `라이브러리 관리`
2. 검색창에 라이브러리 이름 입력.
3. 최신 버전을 선택하고 설치를 클릭합니다. 이미 라이브러리를 설치한 경우 표시되며 업데이트할 수 있습니다.

<p float="left">
  <img src="../docs/images/manage_libraries.jpg" height="300" />
  <img src="../docs/images/install_library.jpg" height="300" /> 
</p>

### 중국제 Nano 클론 (예: 미국 링크)

칩 제조업체(중국)에서 [WCH340](http://www.wch.cn/product/CH340.html) 드라이버를 다운로드해야 할 수 있습니다:

- [Windows](http://www.wch.cn/downloads/CH341SER_EXE.html)
- [Linux](http://www.wch.cn/download/CH341SER_LINUX_ZIP.html)
- [Mac](http://www.wch.cn/download/CH341SER_MAC_ZIP.html)

### ESP32 개발 키트

Arduino IDE에 ESP32 보드를 설치하려면 다음 지침을 따르세요:

1. Arduino IDE에서 **파일 > 기본 설정**으로 이동합니다:
<p align="center">
  <img src="../docs/images/arduino-ide-open-preferences.png" width="300" alt="App GUI"/>
</p>

2. 아래 그림과 같이 “_추가 보드 관리자 URL_” 필드에 *https://dl.espressif.com/dl/package_esp32_index.json*을 입력합니다. 그런 다음 “확인” 버튼을 클릭합니다:
<p align="center">
  <img src="../docs/images/arduino_preferences.png" width="600" alt="App GUI"/>
</p>

**참고:** 이미 ESP8266 보드 URL이 있는 경우 다음과 같이 URL을 쉼표로 구분할 수 있습니다:

    https://dl.espressif.com/dl/package_esp32_index.json,
    http://arduino.esp8266.com/stable/package_esp8266com_index.json

3. 보드 관리자를 엽니다. **도구 > 보드 > 보드 관리자**로 이동합니다:
<p align="center">
  <img src="../docs/images/arduino_boardsManager.png" width="800" alt="App GUI"/>
</p>

4. ESP32를 검색합니다. “Espressif Systems의 ESP32”를 찾아 드롭다운에서 버전 2.0.17을 선택했는지 확인합니다. 3.x.x 버전은 작동하지 않습니다! 설치를 클릭합니다:
<p align="center">
  <img src="../docs/images/arduino_installing.png" width="600" alt="App GUI"/>
</p>

5. 이제 Arduino 개발 환경을 사용하여 OpenBot의 ESP32 보드를 성공적으로 플래시할 수 있는 모든 것을 갖추어야 합니다.
<p align="center">
  <img src="../docs/images/arduino_ESP32-Board-add-on-in-Arduino-IDE-installed.png" width="600" alt="App GUI"/>
</p>

6. 새 코드로 OpenBot을 플래시하려면 메뉴 **도구 > 보드 > ESP32 Arduino**에서 **ESP32 Dev Module**을 선택하기만 하면 됩니다. ESP32 플래싱 프로세스의 추가 콘텐츠 및 문제 해결은 다음 [링크](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/)에서 찾을 수 있습니다.
<p align="center">
  <img src="../docs/images/arduino_windows-select-board.png" width="600" alt="App GUI"/>
</p>

업로드에 문제가 있는 경우 전송 속도를 줄여보세요. 도구로 이동하여 업로드 속도를 115200으로 선택합니다.
<p align="center">
<img src="../docs/images/arduino_upload-speed-select.png" width="600" alt="App GUI"/>
</p>

## 업로드

### 설정 (Arduino nano 설정)

- `도구` :arrow_right: `보드` :arrow_right: `Arduino AVR 보드` :arrow_right: `Arduino Nano`
- `도구` :arrow_right: `프로세서` :arrow_right: `ATmega328P (구형 부트로더)`
- `도구` :arrow_right: `포트` :arrow_right: `*USB 포트 선택*`

:memo: 참고: 현재 대부분의 저렴한 Arduino Nano 보드는 _구형 부트로더_와 함께 제공됩니다. 그러나 판매자에 따라 새 부트로더가 포함된 제품을 받을 수도 있습니다. 펌웨어를 업로드할 수 없는 경우 프로세서를 _ATmega328P_로 변경해야 할 가능성이 큽니다.

### 설정 (ESP32 설정)

- `도구` :arrow_right: `보드` :arrow_right: `ESP32 Arduino` :arrow_right: `ESP32 Dev Module`
- `도구` :arrow_right: `포트` :arrow_right: `*USB 포트 선택*`

### 펌웨어 업로드

이제 `스케치` :arrow_right: `업로드`를 통해 펌웨어를 업로드하거나 업로드 버튼(오른쪽 화살표)을 눌러 업로드할 수 있습니다.
![Firmware Upload](../docs/images/firmware_upload.png)

### 테스트

이 섹션에서는 펌웨어가 성공적으로 플래시된 후 자동차의 모든 기능을 테스트하는 방법을 설명합니다.

1. 다음을 확인합니다:
   1. 바퀴가 자동차에 연결되어 있지 않음
   2. Arduino가 컴퓨터에 연결됨
   3. 올바른 USB 포트가 선택됨
2. 직렬 모니터 열기: `도구` :arrow_right: `직렬 모니터`

#### OpenBot에 메시지 보내기

입력 필드에 명령을 입력한 다음 전송을 눌러 Arduino에 메시지를 보낼 수도 있습니다. 다음 명령을 사용할 수 있습니다(로봇이 필요한 기능을 지원하는 경우):

- `c<left>,<right>` 여기서 `<left>` 및 `<right>`는 모두 [-255,255] 범위에 있습니다. `0` 값은 모터를 정지시킵니다. `255` 값은 모터를 최대 속도로 앞으로 구동하는 최대 전압을 적용합니다. 더 낮은 값은 비례적으로 더 낮은 전압과 속도로 이어집니다. 음수 값은 모터를 역방향으로 구동하여 해당 전압을 역극성으로 적용합니다.
- `i<left>,<right>` 여기서 `<left>` 및 `<right>`는 모두 [0,1] 범위에 있으며 왼쪽 및 오른쪽 표시등 LED에 해당합니다. 예를 들어, `i1,0`은 왼쪽 표시등을 켜고, `i0,1`은 오른쪽 표시등을 켜고, `i1,1`은 두 표시등을 켭니다. 활성화된 표시등은 1초에 한 번 깜박입니다. `i0,0` 값은 표시등을 끕니다. 한 번에 하나의 상태만 가능합니다.
- `l<front>,<back>` 여기서 `<front>` 및 `<back>`은 모두 [0,255] 범위에 있으며 전/후방 LED의 밝기에 해당합니다.
- `s<time_ms>` 여기서 `<time_ms>`는 초음파 측정이 트리거되는 시간(ms)을 나타냅니다(기본값 = 1000). 초음파 판독값이 획득된 후 메시지가 로봇으로 전송됩니다. 시간이 초과되면 지정된 `MAX_SONAR_DISTANCE`가 전송됩니다.
- `w<time_ms>` 여기서 `<time_ms>`는 로봇으로 전송되는 바퀴 주행 거리 측정 사이의 시간(ms)을 나타냅니다(기본값 = 1000). 바퀴 속도는 지속적으로 모니터링되며 지정된 시간 간격에 대한 평균으로 rpm이 계산됩니다.
- `v<time_ms>` 여기서 `<time_ms>`는 로봇으로 전송되는 전압 측정 사이의 시간(ms)을 나타냅니다(기본값 = 1000). 전압은 지속적으로 모니터링되며 크기 10의 이동 평균 필터를 통해 필터링됩니다. 전압 판독값의 시간 간격을 설정하는 것 외에도 이 명령을 보내면 모터를 구동하기 위한 최소 전압(`vmin:<value>`), 최소 배터리 전압(`vlow:<value>`) 및 최대 배터리 전압(`vmax:<value>`)을 보고하는 메시지도 트리거됩니다.
- `h<time_ms>` 여기서 `<time_ms>`는 새 하트비트 메시지를 수신하지 않으면 로봇이 정지할 시간(ms)을 나타냅니다(기본값 = -1).
- `b<time_ms>` 여기서 `<time_ms>`는 범퍼 트리거가 재설정될 시간(ms)을 나타냅니다(기본값 = 750).
- `n<color>,<state>` 여기서 `<color>`는 상태 LED(`b` = 파란색, `g` = 녹색, `y` = 노란색)에 해당하고 `state`는 해당 값(`0` = 꺼짐, `1` = 켜짐)에 해당합니다.
- `f`는 OpenBot에 로봇 유형 및 기능(예: 전압 측정(`v`), 표시등(`i`), 초음파(`s`), 범퍼 센서(`b`), 바퀴 주행 거리 측정(`wf`, `wb`), LED(`lf`, `lb`, `ls`) 등)을 포함한 메시지를 반환하도록 요청을 보냅니다. 예를 들어 OpenBot의 `RTR_V1` 버전의 경우 메시지는 다음과 같습니다: `fRTR_V1:v:i:s:b:wf:wb:lf:lb:ls:`.

#### OpenBot으로부터 메시지 수신

구성에 따라 다른 메시지가 표시될 수 있습니다.

![Serial Monitor](../docs/images/serial_monitor.png)

- `v`로 시작하는 메시지는 배터리 전압을 보고합니다. 배터리를 자동차에 연결하면(즉, 스위치를 켜면) 배터리 전압이 표시됩니다. 배터리를 분리하면(즉, 스위치를 끄면) 작은 값이 표시됩니다.
- `w`로 시작하는 메시지는 회전 속도 센서의 판독값을 보고합니다. 인코더 디스크의 각 구멍은 방향에 따라 카운터를 플러스/마이너스 1씩 증가시킵니다. `DISK_HOLES` 매개변수를 사용하여 구멍 수를 설정할 수 있습니다. 20개의 구멍이 있는 표준 디스크를 사용하는 경우 바퀴가 한 바퀴 회전할 때마다 20번의 카운트가 발생합니다.
- `s`로 시작하는 메시지는 초음파 센서 앞의 추정 여유 공간을 cm 단위로 보고합니다.
- `b`로 시작하는 메시지는 충돌을 보고합니다. `lf`(왼쪽 앞), `rf`(오른쪽 앞), `cf`(중앙 앞), `lb`(왼쪽 뒤), `rb`(오른쪽 뒤) 코드는 충돌을 감지한 센서를 나타냅니다.

#### 테스트 절차

진행하기 전에 타이어가 제거되었는지 확인하세요. 명령을 보내려면 직렬 모니터를 열어야 하며 OpenBot에서 수신한 메시지를 볼 수 있습니다. OLED 디스플레이가 설치된 경우 차량 상태가 더 읽기 쉬운 형식으로 표시됩니다. 다음 테스트 절차를 사용하여 자동차의 모든 기능을 테스트할 수 있습니다:

1. 자동차를 켜고 배터리 전압(`v` 뒤의 숫자)을 관찰합니다. 멀티미터로 판독값을 확인하고 필요한 경우 `VOLTAGE_DIVIDER_FACTOR`를 조정할 수 있습니다.
2. 초음파 센서가 설치된 경우:
   1. 센서 앞에 손을 대고 앞뒤로 움직입니다. 판독값(`s` 뒤의 숫자)이 이에 따라 변경되는 것을 볼 수 있어야 합니다.
   2. 초음파 센서가 진동에 매우 민감하다는 것을 관찰했습니다! 따라서 다음 테스트를 수행하여 작동 중에 신뢰할 수 있는 판독값을 얻을 수 있는지 확인하는 것이 좋습니다:
      1. 초음파 센서가 설치된 OpenBot을 앞에 최소 200cm의 여유 공간이 있도록 배치합니다. `200` 이상의 판독값이 표시되어야 합니다.
      2. 일정 시간 동안 직렬 모니터의 판독값을 관찰한 다음 명령 `c128,128`을 입력합니다.
      3. 센서 판독값이 크게 변경되면 섀시에서 초음파 센서로 전달되는 진동을 완화해야 합니다(예: 실리콘 추가, 장착 위치 조정).
3. 속도 센서가 설치된 경우:
   1. 초음파 센서 앞에 충분한 여유 공간이 있는지 확인합니다. 판독값(`s` 뒤의 숫자)은 기본값으로 `10`인 `STOP_DISTANCE` 이상이어야 합니다.
   2. 명령 `c128,128`을 보냅니다. 모터가 _느린 속도_ (50% PWM)로 회전하기 시작합니다. 속도 센서 판독값(`w` 뒤의 값)은 rpm 단위로 보고되며 배터리의 SOC에 따라 RTR_TT 버전의 경우 250에서 300 사이여야 합니다. DIY 버전이나 더 약한 배터리를 사용하는 경우 값이 더 낮을 수 있습니다. 모든 모터가 앞으로 회전하고 속도 센서 판독값이 양수인지 확인합니다.
   3. 다른 제어를 시도하고 속도 센서 판독값을 관찰합니다. 예를 들어, 명령 `c-128,-128`은 모든 모터를 _느린 속도_ (50% PWM)로 역방향으로 회전시킵니다. 명령 `c255,-255`는 왼쪽 모터를 앞으로, 오른쪽 모터를 _빠른 속도_ (100% PWM)로 역방향으로 회전시킵니다. 명령 `c-192,192`는 왼쪽 모터를 역방향으로, 오른쪽 모터를 _일반 속도_ (75% PWM)로 앞으로 회전시킵니다.
4. 명령 `c0,0`을 보내거나 초음파 센서 앞에 손을 대어 모터를 정지시킵니다.
5. 표시등 LED가 설치된 경우 명령 `i1,0`을 보내고 왼쪽 표시등이 깜박이는 것을 관찰합니다. 그런 다음 명령 `i0,1`을 보내고 오른쪽 표시등이 깜박이는 것을 관찰합니다. 마지막으로 명령 `i0,0`을 보내 표시등을 끕니다.

### No Phone 모드

OpenBot 애플리케이션이 설치된 스마트폰으로 자동차를 테스트하기 전에 먼저 전화 없이 자동차를 테스트할 수도 있습니다. 설정에서 `NO_PHONE_MODE 1`로 설정하기만 하면 됩니다. 이제 자동차는 _일반 속도_ (75% PWM)로 주행하며 초음파 센서로 장애물을 감지하면 속도가 느려집니다. `TURN_THRESHOLD`(기본값: 50cm)에 가까워지면 무작위 방향으로 회전하기 시작하고 해당 측면의 LED가 켜집니다. 자동차 앞의 추정 여유 공간이 `TURN_THRESHOLD`보다 낮아지면 천천히 후진하고 두 LED가 켜집니다. 자동차와 Arduino 모두 전원이 공급되어야 합니다. Arduino는 L298N 모터 드라이버의 5V 출력에 5V 핀을 연결하거나 USB 케이블을 전원 공급원(예: 전화)에 연결하여 전원을 공급할 수 있습니다.

자동차를 실행하기 전에 타이어를 제거하고 Arduino를 컴퓨터에 연결하고 [테스트](#testing) 섹션과 같이 직렬 모니터를 관찰하는 것이 좋습니다. 직렬 모니터의 출력은 약간 더 쉽게 구문 분석할 수 있으며(OLED와 동일) 배터리 전압, 왼쪽 및 오른쪽 모터의 rpm, 자동차 앞의 추정 여유 공간을 보여줍니다. 초음파 센서 앞에서 큰 물체를 앞뒤로 움직이고 모터 속도가 변하는 것을 관찰할 수 있습니다.

:warning: 경고: 초음파 센서가 설치되어 있지 않거나 비활성화된 경우 자동차는 _일반 속도_ (75% PWM)로 앞으로 주행하며 결국 충돌하게 됩니다. 센서가 설치되어 있어도 판독값이 노이즈가 많아 자동차가 가끔 충돌할 수 있습니다.

### 게임패드 조종

No Phone 모드에서는 ESP32 본체가 스스로 주행하게 하는 대신 블루투스 게임패드로 조종할 수 있습니다.

#### 설정

1. Arduino IDE의 **환경설정 > 추가 보드 관리자 URL**에 다음 URL을 추가합니다:
   `https://raw.githubusercontent.com/ricardoquesada/esp32-arduino-lib-builder/master/bluepad32_files/package_esp32_bluepad32_index.json`
2. 보드 관리자에서 **esp32_bluepad32** (4.1.0)를 설치합니다. 일반 펌웨어와 같은 esp32 코어(2.0.17)를 기반으로 합니다.
3. esp32_bluepad32 보드 중에서 **ESP32 Dev Module**을 선택합니다.
4. 설정에서 `NO_PHONE_MODE 1`과 `HAS_GAMEPAD 1`을 설정하고 업로드합니다.

게임패드가 ESP32의 블루투스를 사용하므로 이 모드에서는 휴대폰과의 블루투스 연결이 꺼집니다. 휴대폰 앱으로 조종하려면 `HAS_GAMEPAD 0`으로 두고 게임패드를 휴대폰에 페어링하세요.

#### 페어링

로봇의 전원을 켜고 게임패드를 페어링 모드로 전환합니다. 몇 초 안에 연결되며, 그 후에는 게임패드를 켤 때마다 다시 연결됩니다. 다른 게임패드를 페어링하려면 먼저 연결된 게임패드를 끄세요. 한 번에 하나의 게임패드만 조종할 수 있습니다.

| 게임패드 | 페어링 모드 |
|---|---|
| PS5 DualSense | **Create**를 누른 채로 라이트 바가 깜박일 때까지 **PS**를 길게 누릅니다. |
| PS4 DualShock 4 | **Share**를 누른 채로 라이트 바가 깜박일 때까지 **PS**를 길게 누릅니다. |
| Xbox 무선 (모델 1708 이상) | 전원을 켠 다음 페어링 버튼을 3초간 누릅니다. 먼저 컨트롤러 펌웨어를 5.15 이상으로 업데이트하세요 (예: Xbox 액세서리 앱). |
| Switch Pro, Joy-Con, 8BitDo 등 | 게임패드의 블루투스 페어링 모드를 사용합니다 (설명서 참조). |

[Bluepad32 지원 게임패드 목록](https://bluepad32.readthedocs.io/en/latest/supported_gamepads/)에 더 많은 모델이 있으며, 추가 설정이 필요한 PS3 DualShock 3도 포함되어 있습니다. `DEBUG 1`로 설정하면 시리얼 모니터에 연결되는 게임패드의 이름이 표시됩니다.

#### 조작

버튼은 PlayStation / Xbox 순으로 표기합니다.

| 조작 | 동작 |
|---|---|
| **R2 / L2** | 전진 / 후진 (게임 모드) |
| **왼쪽 스틱** | 조향 (게임 모드), 주행과 조향 (조이스틱 모드) 또는 왼쪽 (듀얼 모드) |
| **오른쪽 스틱** | 오른쪽 (듀얼 모드) |
| **L1** | 다음 주행 모드: 게임 → 조이스틱 → 듀얼. 게임패드가 한 번 진동합니다: 게임은 짧게, 조이스틱은 더 길게, 듀얼은 가장 길게. |
| **방향 패드 위 / R3** | 빠르게 |
| **방향 패드 아래 / L3** | 느리게 |
| **네모 / X** | 왼쪽 방향 지시등 켜기 또는 끄기 |
| **동그라미 / B** | 오른쪽 방향 지시등 켜기 또는 끄기 |
| **세모 / Y** | 방향 지시등 끄기 |
| **엑스 / A** | 조명 켜기 또는 끄기 |
| **Start** | 탐색 켜기 또는 끄기 |
| **Select / Back** | 장애물 정지 켜기 또는 끄기 |

Start와 Select는 긴 진동이 켜짐, 짧은 진동이 꺼짐을 뜻합니다.

**주행 모드.** 게임패드는 `GAMEPAD_DRIVE_MODE`에 설정된 모드로 시작합니다 (기본값: 게임).
- **게임:** R2로 전진, L2로 후진하고 왼쪽 스틱으로 조향합니다. 후진할 때는 자동차처럼 조향이 반대가 됩니다.
- **조이스틱:** 왼쪽 스틱으로 주행과 조향을 합니다. 후진할 때도 로봇은 항상 스틱이 가리키는 방향으로 돕니다.
- **듀얼:** 탱크처럼 각 스틱이 한쪽을 움직입니다.

**속도.** 속도는 세 단계입니다: 느림 (50 %), 보통 (75 %, 시작 속도), 빠름 (100 %). 게임패드는 플레이어 LED (1~3개) 또는 라이트 바 (초록, 주황, 빨강)로 속도를 표시합니다.

**탐색.** 탐색은 위에서 설명한 No Phone 모드 주행으로, 로봇이 스스로 돌아다니며 장애물을 피합니다. 로봇을 켜면 꺼져 있으며, **Start**를 누르면 켜집니다. 탐색 중에 스틱이나 트리거를 움직이면 게임패드가 조종을 넘겨받습니다. 손을 떼면 로봇이 잠시 멈춘 뒤 탐색을 계속합니다. 탐색에는 연결된 초음파 센서와 `HAS_SONAR 1`이 필요하며, 없으면 로봇이 그냥 직진합니다 (위의 경고 참조).

**장애물 정지.** 게임패드로 조종하는 동안 초음파 센서가 `STOP_DISTANCE`보다 가까운 장애물을 감지하면 로봇이 전진하지 않습니다. 로봇을 켜면 켜져 있으며, 무언가를 밀 때처럼 끄려면 **Select**를 누르세요. 연결된 초음파 센서와 `HAS_SONAR 1`이 있을 때만 동작하며, 그렇지 않으면 로봇은 장애물 앞에서 절대 멈추지 않습니다.

**안전.** 게임패드 연결이 끊기면 로봇이 멈춥니다. 대부분의 게임패드는 데이터를 계속 보내는데, 이런 게임패드에서 0.5초 동안 데이터가 오지 않으면 (범위를 벗어남, 배터리 방전) 연결이 끊길 때까지 기다리지 않고 바로 멈춥니다.

## 다른 MCU 사용 (요구 사항)

다음 기능이 있는 다른 MCU를 사용할 수 있습니다:

- 1x USB-to-TTL 직렬(스마트폰과의 통신)
- 4x PWM 출력(모터 제어)
- 배터리 모니터링용 1x 아날로그 핀
- 속도 센서용 2x 디지털 핀
- 초음파 센서용 1x 디지털 핀(선택 사항)
- 표시등 LED용 2x 디지털 핀(선택 사항)

## 다음

[Android 앱](../robot/android/README.ko-KR.md)을 컴파일하고 실행하세요.