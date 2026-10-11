# OpenBot Compact

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

OpenBot Compact는 **ESP32 DevKit V1 + L298N** 구동을 위한 소형 4륜 [OpenBot](../../README.ko-KR.md) 몸체예요. 레고 호환 스터드 덮개, 나사로 고정하는 커버, 두 부분으로 된 휴대폰 거치대, 나사로 고정하는 분리형 전자 부품 마운트, 세 가지 배터리 옵션이 있어요. 모든 출력 부품이 **Bambu Lab A1 mini**에 맞아요 (가장 큰 부품 104 × 168 mm).

<p align="center">
  <img src="../../docs/images/compact/teaser_front.jpg" alt="앞에서 본 모습" width="45%" />
</p>

| | |
|---|---|
| **구동** | 듀얼 샤프트 TT 기어 모터 4개; 왼쪽 쌍과 오른쪽 쌍을 각각 병렬로 L298N 하나에 연결 |
| **컨트롤러** | ESP32 DevKit V1 (30핀), OpenBot 펌웨어 `DIY_ESP32`; 휴대폰과 블루투스 또는 USB 케이블로 연결 |
| **센서** | HC-SR04 초음파 (앞쪽), LM393 광학 휠 엔코더 4개, 배터리 전압 |
| **전원** | 하나를 선택하세요: 덮개 위의 2S 또는 3S 18650 박스, 또는 내부의 소형 LiPo 배터리 ([배터리 옵션](#배터리-옵션) 참고) |
| **체결 부품** | M3 × 10, M3 × 25 둥근머리 나사, M3 너트, 열압입 인서트 16개만 사용 |
| **출력** | PLA, A1 mini 출력판 2장, 약 11시간, 약 300 g, 그리고 선택한 배터리 옵션의 부품 |

> 이 가이드에서 **왼쪽과 오른쪽**은 항상 **로봇 기준**의 왼쪽과 오른쪽이에요. 로봇 뒤(스위치 쪽)에 서서 덮개 너머로 앞쪽의 초음파 센서를 바라보세요. 펌웨어도 "왼쪽"과 "오른쪽" 모터와 엔코더에 같은 기준을 사용해요.

## 목차

- [출력 부품](#출력-부품) · [출력](#출력)
- [배터리 옵션](#배터리-옵션)
- [구매할 하드웨어와 전자 부품](#구매할-하드웨어와-전자-부품)
- 조립: [1 인서트와 너트](#1-인서트와-덮개-너트-설치하기) · [2 모터와 엔코더](#2-모터와-속도-센서-설치하기) · [3 초음파 센서와 전면](#3-초음파-센서와-전면-부품-고정하기) · [4 ESP32와 드라이버](#4-esp32와-모터-드라이버-설치하기) · [5 배선](#5-전원과-신호-배선하기) · [6 펌웨어와 벤치 테스트](#6-펌웨어-설정과-벤치-테스트) · [7 배터리, 휴대폰 거치대, 덮개](#7-배터리-휴대폰-거치대-덮개-설치하기)
- [드라이버 통합형 대체 PCB](#드라이버-통합형-대체-pcb)
- [문제 해결](#문제-해결) · [보기](#보기) · [다음](#다음)

## 출력 부품

<p align="center">
  <img src="../../docs/images/compact/electronics_mounts.jpg" alt="전자 부품 마운트가 있는 섀시" width="45%" />
</p>

| 부품 | 수량 | 설치 위치 / 출력 방향 |
|---|---:|---|
| [bottom_cover](cad/stl/bottom_cover.stl) ([STEP](cad/step/bottom_cover.step)) | 1 | 섀시; 바닥을 아래로, 서포트 없음 |
| [top_cover](cad/stl/top_cover.stl) ([STEP](cad/step/top_cover.step)) | 1 | 케이블 구멍 두 개가 있는 스터드 덮개; 평평한 아랫면을 아래로, 스터드를 위로 |
| [lid_plug](cad/stl/lid_plug.stl) ([STEP](cad/step/lid_plug.step)) | 2 | 덮개 구멍을 막고 스터드 두 개를 복원해요. 스터드를 위로. |
| [lid_grommet](cad/stl/lid_grommet.stl) ([STEP](cad/step/lid_grommet.step)) | 1 *(선택)* | 케이블이 지나가는 곳에서 플러그 대신 사용해요. 스터드를 위로. |
| [esp32_carrier](cad/stl/esp32_carrier.stl) ([STEP](cad/step/esp32_carrier.step)) | 1 | ESP32 지지 프레임; 평평한 바닥을 아래로 |
| [esp32_retainer](cad/stl/esp32_retainer.stl) ([STEP](cad/step/esp32_retainer.step)) | 4 | ESP32 모서리 고정 탭; 어느 탭이든 어느 모서리에나 맞아요. 평평한 면을 아래로, 돌기를 위로. |
| [sensor_support](cad/stl/sensor_support.stl) ([STEP](cad/step/sensor_support.step)) | 4 | 속도 센서 지지대; 어느 지지대든 어느 바퀴에나 맞아요 |
| [ultrasonic_clip_left](cad/stl/ultrasonic_clip_left.stl) ([STEP](cad/step/ultrasonic_clip_left.step)) | 1 | HC-SR04 보드의 위쪽 **왼쪽** 모서리 |
| [ultrasonic_clip_right](cad/stl/ultrasonic_clip_right.stl) ([STEP](cad/step/ultrasonic_clip_right.step)) | 1 | HC-SR04 보드의 위쪽 **오른쪽** 모서리 |
| [power_board_stand](cad/stl/power_board_stand.stl) ([STEP](cad/step/power_board_stand.step)) | 1 | 방열판 뒤에서 5 V 컨버터와 분압기 만능기판을 고정해요. 바닥을 아래로. |
| [pcb_spacer](cad/stl/pcb_spacer.stl) ([STEP](cad/step/pcb_spacer.step)) | 4 | L298N 아래의 2.4 mm 절연 스페이서 |
| [pcb_washer](cad/stl/pcb_washer.stl) ([STEP](cad/step/pcb_washer.step)) | 8 | 0.8 mm 절연 와셔: L298N용 4개, 속도 센서용 4개 |
| [phone_mount_bottom](cad/stl/phone_mount_bottom.stl) ([STEP](cad/step/phone_mount_bottom.step)) | 1 | 휴대폰 거치대 받침; 발을 바닥에 두고 세워서 출력, 서포트 없음 |
| [phone_mount_top](cad/stl/phone_mount_top.stl) ([STEP](cad/step/phone_mount_top.step)) | 1 | 슬라이드식 휴대폰 그립; 세워서 출력, 서포트 없음 |
| [led_adapter](cad/stl/led_adapter.stl) ([STEP](cad/step/led_adapter.step)) | 4 | 패널 램프 어댑터: 앞쪽 2개, 뒤쪽 2개 |
| [encoder_disk](cad/stl/encoder_disk.stl) ([STEP](cad/step/encoder_disk.step)) | 4 | 선택 사항; **불투명** 필라멘트로 출력하거나 맞는 기성품 디스크를 사용하세요. 평평하게 출력해요. |

ESP32 고정 탭 4개는 모두 같고, 센서 지지대 4개도 모두 같아서 어느 부품이든 어느 위치에나 맞아요. 초음파 클립은 좌우 대칭 한 쌍이에요. 넓은 뒷면을 바닥에 눕혀서 출력하세요. 배터리용 부품은 [배터리 옵션](#배터리-옵션)에 정리되어 있어요.

## 출력

모든 부품의 STL과 STEP 파일은 [cad/stl](cad/stl)과 [cad/step](cad/step)에 있고, 이미 출력 방향으로 배치되어 있으며 단위는 밀리미터예요. 100 % 배율로 출력하세요.

Bambu Studio에서 **[compact_A1_mini.3mf](cad/compact_A1_mini.3mf)**를 여세요. 출력판 1과 2에는 모든 기본 부품이 필요한 수량만큼 배치되어 있어요. 출력판 3–5에는 배터리 옵션이 하나씩 들어 있어요. 선택한 옵션의 출력판만 출력하세요.

| 출력판 | 내용 | 시간* | 필라멘트* |
|---|---|---:|---:|
| 1 – 섀시와 부속품 | 섀시, 휴대폰 거치대 (두 부품), 센서 지지대 4개, LED 어댑터 4개, 엔코더 디스크 2개, ESP32 고정 탭 2개, 오른쪽 초음파 클립, 스페이서 2개, 와셔 6개 | 약 7시간 50분 | 약 215 g |
| 2 – 덮개와 부속품 | 스터드 덮개, ESP32 캐리어, 전원 보드 스탠드, 덮개 플러그 2개, 엔코더 디스크 2개, ESP32 고정 탭 2개, 왼쪽 초음파 클립, 스페이서 2개, 와셔 2개 | 약 3시간 20분 | 약 80 g |
| 3 – 배터리 A (2S 박스) | 2S 배터리 박스, 커버, 덮개 그로밋 | 약 1시간 40분 | 약 42 g |
| 4 – 배터리 B (3S 박스) | 3S 배터리 박스, 커버, 덮개 그로밋 | 약 2시간 20분 | 약 58 g |
| 5 – 배터리 C (LiPo 트레이) | LiPo 트레이 | 약 15분 | 약 5 g |

\*Generic PLA 기준 추정치예요. 출력판 3–5는 부품 부피로 계산했어요. 실제 시간은 다를 수 있어요. 출력 전에 각 출력판에서 **Slice plate**를 누르세요.

프로필: 0.4 mm 노즐, 0.20 mm 레이어, 벽 4개, 상단 5층과 하단 5층, 자이로이드 채움 30 %, 텍스처 PEI 출력판. **서포트가 필요한 부품은 없어요.** 섀시의 벽 구멍은 원형이고 최대 20 mm이며, 너트 포켓은 약 8 mm를 브리지로 출력해요. 휴대폰 거치대의 두 부품은 세워서 출력해요. 프로젝트에서는 서포트가 꺼져 있어요. STL 폴더에서 부품을 추가할 때도 서포트를 끈 상태로 두세요. 출력 전에 실제로 넣은 PLA를 선택하고 레이어 미리보기를 확인하세요.

- 덮개는 스터드를 위로 해서 평평하게 출력해요. 섀시는 바닥을 아래로, 내부를 위로 열어서 출력해요.
- 출력 후 너트 포켓 (각 포켓 입구의 작은 리브 두 개는 그대로 두세요), 인서트 구멍, 소켓, 케이블 구멍에서 실 찌꺼기를 제거하세요.
- 0.8 mm 와셔는 0.20 mm 레이어로 평평하게 출력하세요. 와셔는 보드를 절연하고 나사의 힘을 분산해요. 닿을 때까지만 조이고, 변형된 와셔는 교체하세요.
- PLA는 약 55 °C에서 물러지므로 PLA 부품을 뜨거운 차 안이나 한여름 직사광선에 두지 마세요.

## 배터리 옵션

섀시 안에는 3셀 18650 홀더 (76 × 62 × 20 mm)를 넣을 공간이 없어요. 높이가 20 mm인 곳은 모두 너무 좁아요. 대신 다음 세 가지 중 하나를 선택하세요:

| | **A – 2S 18650 박스** (추천) | **B – 3S 18650 박스** | **C – 내부 LiPo 트레이** |
|---|---|---|---|
| 위치 | 덮개 위, 휴대폰 거치대 뒤 | 덮개 위, 휴대폰 거치대 뒤 | 내부, 모터 드라이버 위 |
| 배터리 | 2셀 직렬 홀더에 18650 2개 (약 77 × 41 × 20 mm) | 3셀 직렬 홀더에 18650 3개 (약 76 × 62 × 20 mm) | 2S 또는 3S LiPo, **최대 75 × 31 × 20 mm** (예: 3S 450–650 mAh, 2S 1000–1300 mAh) |
| 전압 | 완충 8.4 V, 공칭 7.4 V | 완충 12.6 V, 공칭 11.1 V | 완충 8.4 V 또는 12.6 V |
| 에너지 | 약 21 Wh (2 × 2800 mAh) | 약 31 Wh (3 × 2800 mAh) | 약 5–10 Wh |
| 출력 부품 | [battery_box_2s](cad/stl/battery_box_2s.stl) + [커버](cad/stl/battery_box_2s_cover.stl), [lid_grommet](cad/stl/lid_grommet.stl) 1개 | [battery_box_3s](cad/stl/battery_box_3s.stl) + [커버](cad/stl/battery_box_3s_cover.stl), [lid_grommet](cad/stl/lid_grommet.stl) 1개 | [lipo_tray](cad/stl/lipo_tray.stl) |
| 충전기 | 슬롯이 독립된 리튬이온 충전기 | 슬롯이 독립된 리튬이온 충전기 | LiPo **밸런스** 충전기 |
| 참고 | TT 모터에 가장 잘 맞아요: L298N의 전압 강하 후 약 6.4 V가 모터에 걸려요. | 3–6 V TT 모터에는 과전압이에요 (L298N 이후 약 10.6 V). 더 높은 전압용 모터를 쓰거나 속도를 제한하세요. | 용량이 가장 작아요. 배터리가 보이지 않고 보호돼요. |

세 가지 모두 펌웨어의 30 k / 10 k 배터리 분압기를 그대로 사용해요. 펌웨어의 전압 한계값만 바뀌어요 ([6단계](#6-펌웨어-설정과-벤치-테스트) 참고).

<p align="center">
  <img src="../../docs/images/compact/battery_box_2s.jpg" alt="덮개 위의 2S 배터리 박스" width="32%" />
  <img src="../../docs/images/compact/battery_box_3s.jpg" alt="덮개 위의 3S 배터리 박스" width="32%" />
  <img src="../../docs/images/compact/battery_lipo_tray.jpg" alt="LiPo 트레이" width="32%" />
</p>

**A와 B – 덮개 위의 배터리 박스.** 박스는 휴대폰 거치대 뒤에서 레고 블록처럼 덮개의 스터드에 끼워져요. 박스 바닥에는 배터리 전선을 덮개 뒤쪽 구멍으로 내려보내는 홈이 있어요. 홀더의 전선이 홈이 있는 쪽 끝으로 오도록 박스에 넣으세요. 전선을 바닥 구멍과 [lid_grommet](cad/stl/lid_grommet.stl)을 통해 섀시 안으로 넣은 다음, 스터드 커버를 네 개의 핀에 눌러 끼우세요. 커버에도 스터드가 있어서 위에 레고 블록을 계속 끼울 수 있어요. 박스는 모서리의 덮개 나사와 겹치지 않아요. 3S 박스는 덮개 안쪽에 완전히 들어가고, 뒤쪽 가장자리와 14 mm 떨어져 있어요. 박스를 설치해도 덮개 나사 네 개에 모두 손이 닿아요.

**C – 내부 LiPo 트레이.** 트레이는 모터 사이의 빈 공간에 다리 두 개로 서고, L298N과 ESP32 고정 탭 나사 바로 위에 놓여요. 배터리 자체의 높이와 닫힌 덮개가 트레이를 눌러 고정해요. 20 mm 벨크로 스트랩을 슬롯 네 개에 끼워 배터리를 트레이에 고정하세요. 배터리 두께가 18 mm보다 얇으면 덜컹거리지 않도록 위에 폼 패드를 올리세요. L298N 배선을 먼저 모두 끝내세요. 트레이가 단자를 가리지만, 위로 곧게 들어 올리면 빠져요.

> **리튬 배터리 안전.** 신뢰할 수 있는 판매처의 셀을 사용하세요. 18650 팩은 보호 회로 (BMS)로 보호하세요. 펌웨어도 배터리도 방전 시 전원을 끊지 않으므로 LiPo에는 전압 경보기를 꽂으세요. 배터리 가까이에 퓨즈를 다세요. LiPo는 반드시 밸런스 충전기로, 불연성 표면 위에서, 지켜보는 상태에서만 충전하세요. 부풀었거나 손상된 배터리는 사용하지 마세요.

## 구매할 하드웨어와 전자 부품

**나사 길이는 M3 × 10 mm와 M3 × 25 mm 두 가지만 필요해요.** 둘 다 머리 지름 약 5.5 mm의 둥근머리 나사예요 (접시머리 제외). 회로 기판용 와셔는 출력해서 써요. 덮개, 캐리어, ESP32 고정 탭 나사 아래에는 와셔를 넣지 마세요. 나사 길이가 머리가 플라스틱에 바로 닿는 것을 전제로 정해졌어요.

아래 링크는 판매처 또는 Amazon US 검색으로 연결돼요. 같은 이름으로 팔리는 보드라도 구멍 배치가 다를 수 있으니 주문 전에 표시된 치수와 사양을 확인하세요.

### 체결 부품

| 수량 | 부품 | 용도 | 구매 |
|---:|---|---|---|
| 16 (+ 여분) | M3 × 5.7 mm 황동 열압입 인서트, 외경 4.6 mm 일반 규격 (예: ruthex RX-M3x5.7) | 섀시에 12개, ESP32 캐리어에 4개 | [ruthex](https://www.ruthex.de/products/ruthex-gewindeeinsatz-m3-100-stuck-rx-m3x5-7-messing-gewindebuchsen) |
| 16 | M3 × 10 mm 둥근머리 나사 | 덮개 4, 캐리어 2, ESP32 고정 탭 4, L298N 4, 초음파 클립 2 | [검색](https://www.amazon.com/s?k=M3+x+10+pan+head+screws+ISO+7045) |
| 14 | M3 × 25 mm 둥근머리 나사 | 모터 8, 속도 센서 4, 휴대폰 거치대 2 | [검색](https://www.amazon.com/s?k=M3+x+25+pan+head+screws+ISO+7045) |
| 14 | M3 육각 너트, 맞변 거리 5.5 mm, 두께 약 2.4 mm | 덮개 4, 모터 8, 휴대폰 거치대 2 | [검색](https://www.amazon.com/s?k=M3+hex+nuts+DIN+934) |
| 4 | 작은 케이블 타이 (폭 3 mm 이하) | 전원 보드 스탠드의 5 V 컨버터와 만능기판 | [검색](https://www.amazon.com/s?k=small+cable+ties+2.5mm) |
| 약 350 g | 1.75 mm PLA | 모든 출력 부품 | [검색](https://www.amazon.com/s?k=PLA+filament+1.75mm) |

### 전자 부품과 구동부

| 수량 | 부품 | 사양 / 참고 | 구매 |
|---:|---|---|---|
| 1 | ESP32 DevKit V1, 30핀 | 기판 약 27.94 × 50.97 mm. DevKit 호환 보드마다 다르니 구멍 위치를 확인하세요. | [검색](https://www.amazon.com/s?k=ESP32+DevKit+V1+30+pin+development+board) |
| 1 | L298N 드라이버 모듈 | 약 43.5 × 43.5 mm, 구멍은 37 × 37 mm 정사각형 배치. | [검색](https://www.amazon.com/s?k=L298N+motor+driver+module+43+43) |
| 4 | 듀얼 샤프트 TT 기어 모터 | 한쪽 샤프트는 바퀴를, 다른 쪽은 엔코더 디스크를 돌려요. 53.35 mm 모터 본체에 맞아요. 배터리 옵션에 맞는 정격 전압을 고르세요. | [검색](https://www.amazon.com/s?k=TT+dual+shaft+gear+motor+robot+car) |
| 4 | TT 바퀴 | 지름 약 65 mm | [검색](https://www.amazon.com/s?k=65mm+TT+motor+robot+car+wheels) |
| 4 | LM393 광학 속도 센서 | 기판 폭 약 14.2 mm, 길이 약 38.1 mm, 3 mm 구멍 1개. 네 개 모두 장착해요. 기본적으로 펌웨어는 앞쪽 두 개를 읽어요. | [검색](https://www.amazon.com/s?k=LM393+optical+speed+sensor+module+encoder+disk) |
| 4 | 엔코더 디스크 | 출력하지 않을 때만 필요해요. TT 샤프트와 센서 슬롯에 맞아야 해요 | [검색](https://www.amazon.com/s?k=TT+motor+optical+encoder+disk) |
| 1 | HC-SR04 초음파 센서 | 기판 45.5 × 20.5 mm, 트랜스듀서 간격 25.5 mm (중심 간) | [검색](https://www.amazon.com/s?k=HC+SR04+ultrasonic+sensor+module) |
| 1 | 원형 로커 스위치 | 뒤판 구멍에 맞고, 배터리 전압과 모터 전류를 견디는 정격 | [검색](https://www.amazon.com/s?k=20mm+round+rocker+switch+SPST+DC) |
| 1 | 5 V 컨버터, 2 A 이상, 최대 21 × 43 × 14 mm | 전원 보드 스탠드에 맞아요. 3S에는 LM2596 모듈을 쓸 수 있어요. 2S에는 6 V 입력에서도 5 V를 내는 종류를 고르세요 (예: MP1584 또는 벅-부스트). | [검색](https://www.amazon.com/s?k=5V+buck+converter+2A+module) |
| 1 | 작은 만능기판, 최대 24 × 34 mm | 배터리 분압기와 에코 분압기, 램프 구동 회로; 스탠드 뒷면에 붙여요 | [검색](https://www.amazon.com/s?k=small+prototype+perfboard) |
| 1세트 | 1 % 저항 | 배터리 분압기 30 k + 10 k; 에코 분압기 10 k + 20 k; 램프 1 k 4개 + 100 k 4개 | [검색](https://www.amazon.com/s?k=1+percent+metal+film+resistor+assortment+10k+20k+30k+1k+100k) |
| 2 + 2 | 흰색과 빨간색 패널 표시등 *(선택)* | 베젤 크기가 패널 구멍에 맞는지, 램프 전압이 맞는지 확인하세요 | [검색](https://www.amazon.com/s?k=12mm+panel+LED+indicator+12V+white+red) |
| 4 | NPN 트랜지스터, 예: PN2222A *(선택, 램프용)* | 각 램프의 로우사이드 스위치 | [검색](https://www.amazon.com/s?k=PN2222A+transistor) |
| 1세트 | 듀폰 점퍼선, 실리콘 전원선, 수축 튜브 | 전원선은 모터 구속 전류에 맞춰 선택하세요 | [검색](https://www.amazon.com/s?k=silicone+wire+kit+20+22+AWG+heat+shrink) |

### 배터리와 충전

선택한 [배터리 옵션](#배터리-옵션)에 맞는 부품을 구매하세요.

| 옵션 | 부품 | 구매 |
|---|---|---|
| A – 2S 박스 | 같은 종류의 18650 셀 2개, 2셀 **직렬** 홀더 (약 77 × 41 mm), 2S 보호 회로, 인라인 퓨즈 (약 5 A), XT30 커넥터 한 쌍, 리튬이온 충전기 | [셀](https://www.18650batterystore.com/collections/18650-batteries) · [홀더](https://www.amazon.com/s?k=2+x+18650+battery+holder+series+wires) · [BMS/퓨즈/XT30](https://www.amazon.com/s?k=2S+BMS+18650+fuse+holder+XT30) · [충전기](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| B – 3S 박스 | 같은 종류의 18650 셀 3개, 3셀 **직렬** 홀더 (약 76 × 62 mm), 3S 보호 회로, 인라인 퓨즈, XT30 커넥터 한 쌍, 리튬이온 충전기 | [셀](https://www.18650batterystore.com/collections/18650-batteries) · [홀더](https://www.amazon.com/s?k=3+series+18650+battery+holder) · [BMS/퓨즈/XT30](https://www.amazon.com/s?k=3S+lithium+battery+protection+BMS+fuse+holder+XT30+connector) · [충전기](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| C – LiPo 트레이 | XT30이 달린 최대 75 × 31 × 20 mm의 2S 또는 3S LiPo, 20 mm 벨크로 스트랩, LiPo 밸런스 충전기, LiPo 전압 경보기 | [LiPo](https://www.amazon.com/s?k=3S+650mAh+lipo+XT30) · [스트랩](https://www.amazon.com/s?k=20mm+battery+strap+hook+loop) · [충전기](https://www.amazon.com/s?k=lipo+balance+charger+2S+3S) |

### 기타

| 수량 | 부품 | 참고 | 구매 |
|---:|---|---|---|
| 1 | 고무줄 또는 약한 스프링 | 휴대폰 그립에 장력을 줘요 | [검색](https://www.amazon.com/s?k=rubber+bands+assorted+sizes) |
| 1 | 90° (ㄱ자) 마이크로 USB 데이터 케이블 | 조립된 상태에서 펌웨어를 올리거나 덮개를 통해 휴대폰과 유선 연결할 때 써요. 일자 플러그는 L298N 옆에 들어가지 않아요 ([6단계](#6-펌웨어-설정과-벤치-테스트) 참고). 보드에 맞는 플러그를 고르세요 (마이크로 USB 또는 USB-C). | [검색](https://www.amazon.com/s?k=90+degree+micro+USB+data+cable+up+down+angle) |
| 1 | USB 전원 차단기 / 데이터 전용 어댑터 *(휴대폰 유선 연결 시에만)* | 휴대폰과 5 V 컨버터가 서로 전원을 공급하지 않게 막아요 | [검색](https://www.amazon.com/s?k=USB+data+only+power+blocker+adapter) |
| 1 | 안드로이드 휴대폰 | OpenBot 앱이 실행되어야 해요. 케이스를 낀 채로 거치대에 맞는지 확인하세요. | – |
| – | 공구 | M3 인서트 팁이 있는 온도 조절 인두, 드라이버/육각 렌치, 멀티미터, 니퍼, 와이어 스트리퍼 | [검색](https://www.amazon.com/s?k=heat+set+insert+soldering+iron+tip+M3) |

## 조립

<p align="center">
  <img src="../../docs/images/compact/exploded.jpg" alt="분해도" width="45%" />
</p>

### 1. 인서트와 덮개 너트 설치하기

<p align="center">
  <img src="../../docs/images/compact/step_01_shell.jpg" alt="빈 셸" width="40%" />
</p>

4.0 mm 가이드 구멍에 표준 M3 × 5.7 mm 열압입 인서트 (예: ruthex RX-M3x5.7, 외경 4.6 mm)를 사용하세요. 온도 조절 인두와 인서트 팁으로 각 인서트를 똑바로, 표면과 같은 높이까지 눌러 넣으세요. 나사를 끼우기 전에 식히세요.

**섀시에 인서트 12개**를 설치하세요:

- L298N 보스에 4개
- 속도 센서 보스에 4개
- 긴 PCB용 보스 중 **앞쪽** 두 개에 2개 (ESP32 캐리어를 고정해요)
- 초음파 구멍 위의 안쪽을 향한 보스에 2개

**ESP32 캐리어의 고정 기둥에 4개**를 더 설치하세요. 긴 PCB용 보스 중 뒤쪽 두 개는 [대체 PCB](#드라이버-통합형-대체-pcb)만 쓰므로 비워 두세요.

모서리의 덮개 고정용 너트 포켓 네 곳에 M3 육각 너트를 하나씩 옆으로 밀어 넣으세요. 꼭짓점이 앞으로 가고 두 면이 포켓 벽에 닿게 넣어요. 각 포켓 입구의 작은 리브 두 개가 입구를 살짝 좁혀서, 너트를 힘주어 밀어야 하고 (일자 드라이버 끝을 쓰면 편해요) 리브를 지나면 딸깍 하고 들어가요. 한번 들어가면 섀시를 뒤집어도 빠지지 않아요. 너트가 너무 헐겁거나 들어가지 않으면 포켓에 실 찌꺼기가 없는지 확인하세요.

### 2. 모터와 속도 센서 설치하기

<p align="center">
  <img src="../../docs/images/compact/step_02_motors.jpg" alt="모터 설치" width="49%" />
  <img src="../../docs/images/compact/step_03_sensors.jpg" alt="센서 설치" width="49%" />
</p>

바퀴 샤프트가 옆 구멍으로 나오고 전선이 가운데를 향하도록 TT 모터 네 개를 넣으세요. 각 모터를 M3 × 25 mm 나사 두 개와 너트로 고정하세요. 나사가 기어박스 내부에 닿지 않는지 확인하세요. 벤치 테스트를 마칠 때까지 **바퀴는 끼우지 마세요**.

안쪽 샤프트마다 엔코더 디스크를 끼우세요. 센서 보스 네 곳에 센서 지지대를 평평한 면이 모터를 향하도록 끼우세요. D자형 소켓이 회전을 막아 줘요. LM393 보드를 위쪽 가이드 두 개 사이에 넣고 출력한 와셔 하나와 M3 × 25 mm 나사 하나로 고정하세요.

각 샤프트를 손으로 돌려 보세요. 디스크가 광학 슬롯을 스치지 않고 지나가야 하고, 센서 다리가 디스크에 닿으면 안 돼요.

### 3. 초음파 센서와 전면 부품 고정하기

<p align="center">
  <img src="../../docs/images/compact/ultrasonic_retention.jpg" alt="초음파 센서 고정" width="40%" />
</p>

두 트랜스듀서가 원형 구멍으로 나오도록 HC-SR04를 앞쪽 홈에 넣으세요. 홈이 보드의 위치를 잡아 주고, 클립 두 개가 안쪽에서 보드 윗부분을 잡아서 섀시 안으로 떨어지지 않게 해요.

`ultrasonic_clip_left`는 로봇의 왼쪽에, `ultrasonic_clip_right`는 오른쪽에 달고, 각각의 작은 립이 PCB 위쪽 모서리 위에 오게 하세요. 각 클립을 안쪽을 향한 인서트에 M3 × 10 mm 나사 하나로 고정하고, 나사 머리가 섀시 안쪽을 향하게 하세요. PCB가 휘지 않고 클립이 보스에 닿을 때까지 조이세요. 보드가 들리거나 빠지면 안 되고, 트랜스듀서는 전면판과 직각을 유지해야 해요.

흰색 전조등 두 개, 빨간색 후미등 두 개 (각각 출력한 LED 어댑터에 넣어요), 원형 로커 스위치를 함께 제공된 고정 부품으로 다세요. 전선은 바퀴와 엔코더 디스크에서 멀리 정리하세요.

### 4. ESP32와 모터 드라이버 설치하기

ㄱ자 USB 케이블이 없다면 **먼저 펌웨어를 올리세요** ([6단계](#6-펌웨어-설정과-벤치-테스트)). L298N을 설치하고 나면 일자 USB 플러그는 ESP32에 들어가지 않아요.

ESP32 캐리어를 긴 PCB용 보스 중 앞쪽 두 개에 M3 × 10 mm 나사 두 개로 고정하세요. ESP32 자체의 장착 구멍을 2.2 mm 위치 결정 핀 네 개에 맞춰 내려놓으세요. ESP32 PCB를 관통하는 나사는 없어요.

캐리어 기둥 네 개에 고정 탭을 하나씩 올리고, 돌기가 아래를 향하게 하세요. 돌기가 ESP32 모서리 구멍에 들어갈 때까지 탭을 돌린 다음 M3 × 10 mm 나사 하나로 고정하세요. 어느 탭이든 어느 모서리에나 맞아요. 탭은 기둥에 닿아 멈추고 PCB 위로 약 0.25 mm 틈을 남기므로, 조여도 보드가 눌리지 않아요. 보드의 구멍이 핀과 맞지 않으면 억지로 끼우지 마세요.

L298N 보스에 출력한 2.4 mm 스페이서 네 개를 올리고 드라이버를 얹은 다음, 각 모서리를 출력한 0.8 mm 와셔와 M3 × 10 mm 나사로 고정하세요 (37 × 37 mm 배치). 아래쪽 납땜 부위가 섀시에 닿지 않는지, 방열판 주위에 공기가 통하는지 확인하세요.

**전원 보드 스탠드.** 5 V 컨버터는 스탠드 앞면에, 분압기 만능기판은 뒷면에 각각 케이블 타이 두 개를 슬롯에 끼워 묶으세요. 스탠드는 L298N 방열판 뒤, 뒤쪽 속도 센서 두 개 사이, 스위치 앞의 바닥에 놓여요. 닫힌 덮개가 스탠드를 눌러 주므로 나사가 필요 없어요.

<p align="center">
  <img src="../../docs/images/compact/step_04_electronics.jpg" alt="전자 부품 설치" width="40%" />
</p>

### 5. 전원과 신호 배선하기

<p align="center">
  <img src="../../docs/images/compact/wiring_diagram.png" alt="배선도" width="80%" />
</p>

ESP32에 인쇄된 GPIO 번호를 사용하세요. 모든 GND는 서로 연결되어야 해요. 배선이 기어, 엔코더 디스크, 나사 끝, 덮개 이음매에 닿지 않게 하고, 진동에 대비해 묶어 두세요.

| 연결 | ESP32 GPIO | 참고 |
|---|---|---|
| L298N IN1 / IN2 (왼쪽 모터) | 13 / 12 | |
| L298N IN3 / IN4 (오른쪽 모터) | 15 / 2 | |
| L298N ENA / ENB | – | 인에이블 점퍼는 그대로 꽂아 두세요 (속도는 IN 핀의 PWM으로 설정해요) |
| 엔코더 앞 왼쪽 / 앞 오른쪽 | 35 / 22 | |
| 엔코더 뒤 왼쪽 / 뒤 오른쪽 *(선택)* | 34 / 23 | |
| 초음파 TRIG | 5 | |
| 초음파 ECHO | 18 | 10 k (ECHO → GPIO18) / 20 k (GPIO18 → GND) 분압기를 거쳐요 |
| 배터리 전압 | 4 | 스위치를 거친 배터리 +에서 GPIO4로 30 k, GPIO4에서 GND로 10 k |
| 전조등 L / R *(선택)* | 19 / 21 | NPN 트랜지스터 단을 거쳐요 |
| 후미등 L / R *(선택)* | 25 / 26 | NPN 트랜지스터 단을 거쳐요, 펌웨어 수정 필요 |

- **모터:** 왼쪽 모터 두 개는 병렬로 OUT1/OUT2에, 오른쪽 모터 두 개는 OUT3/OUT4에 연결하세요. 한쪽이 반대로 돌면 그쪽의 모터 전선 두 가닥을 바꾸세요.
- **엔코더:** LM393 보드에는 5 V가 아니라 **3.3 V**를 공급하세요. 그러면 보드에 있는 풀업 저항이 출력을 ESP32에 안전한 수준으로 유지해요. GPIO34와 GPIO35는 입력 전용이고 내부 풀업이 없어요.
- **램프:** 각 램프는 NPN 트랜지스터로 스위칭해요 (베이스 저항 1 k, 베이스-이미터 간 풀다운 100 k). 패널 램프를 ESP32 핀으로 직접 구동하지 마세요.

#### 전원

배터리 + → 보호 회로/BMS → 퓨즈 → 스위치 → L298N 모터 전원 (+12 V 단자). 5 V 컨버터는 스위치를 거친 배터리 전압을 받아 ESP32의 5 V/VIN 핀과 HC-SR04에 공급해요. L298N의 5 V 단자는 연결하지 마세요. 케이블에 USB 전원 차단기가 없다면 **USB와 외부 5 V 전원을 절대 동시에 연결하지 마세요**. 조립하는 동안에는 배터리를 분리해 두세요.

옵션 A (2S)에서는 모터에 약 6.4 V가 걸리는데, 일반적인 3–6 V TT 모터에 적합해요. 옵션 B나 3S LiPo에서는 약 10.6 V가 걸려요. 그 전압에 맞는 모터를 쓰거나 앱에서 속도를 제한하세요.

### 6. 펌웨어 설정과 벤치 테스트

[펌웨어 폴더](../../firmware/README.ko-KR.md)에서 펌웨어를 받아 `firmware/openbot/openbot.ino`를 여세요.

1. 로봇 종류를 설정하고 휴대폰 모드는 켜 두세요:

   ```cpp
   #define OPENBOT DIY_ESP32
   #define NO_PHONE_MODE 0
   ```

2. `#elif (OPENBOT == DIY_ESP32)` 블록에서 **인디케이터를 끄세요**. 기본 핀 (5와 18)이 이 구성에서는 초음파 센서 핀이에요:

   ```cpp
   #define HAS_INDICATORS 0   // default is 1
   ```

3. 같은 블록에서 배터리 옵션에 맞게 배터리 한계값을 설정하세요. 분압기 계수는 그대로 둬요.

   | 옵션 | `VOLTAGE_MIN` | `VOLTAGE_LOW` | `VOLTAGE_MAX` |
   |---|---:|---:|---:|
   | A – 2S 18650 또는 2S LiPo | 6.0f | 6.8f | 8.4f |
   | B – 3S 18650 또는 3S LiPo | 9.0f | 10.2f | 12.6f |

   앱은 이 한계값을 배터리 게이지와 배터리 부족 경고에 사용해요. 기본 최솟값 6.0 V를 그대로 두면 3S 팩이 셀당 2 V까지 사용 가능한 것으로 표시돼요. 펌웨어는 전압이 낮아도 모터를 끄지 않으므로, 보호 회로 (옵션 A와 B) 또는 LiPo 전압 경보기 (옵션 C)로 셀을 보호해야 해요.

4. 선택 사항, 같은 블록에서:

   ```cpp
   #define HAS_SPEED_SENSORS_BACK 1   // read the rear encoders as well
   #define HAS_LEDS_FRONT 1           // white lights on GPIO19/21 (default pins)
   #define HAS_LEDS_BACK 1            // red lights, and change their pins:
   const int PIN_LED_LB = 25;         // default 5
   const int PIN_LED_RB = 26;         // default 18
   ```

5. Arduino IDE에서 **esp32 보드 패키지 2.0.17**을 설치하세요. 펌웨어가 3.x에서 제거된 `ledcSetup`/`ledcAttachPin`을 사용해요. 그다음 **ESP32 Dev Module**을 선택하고 업로드하세요.

**USB 접근.** ESP32는 캐리어에 뒤집혀 있어서 USB 소켓이 보드 아래에 있고, 약 8.5 mm 떨어진 L298N 전원 단자를 마주 봐요. 드라이버를 설치한 뒤에는 케이블이 두 보드 사이로 위쪽으로 빠지는 ㄱ자 플러그만 들어가요. L298N을 달기 전에 펌웨어를 올리거나 90° 케이블을 사용하세요.

GPIO 2, 5, 12, 15는 부트 스트래핑 핀이에요. 모두 연결한 상태에서 보드가 부팅되지 않거나 펌웨어가 올라가지 않으면 이 핀에 무엇이 걸려 있는지 확인하세요. 특히 GPIO12는 리셋 시 LOW여야 해요.

**벤치 테스트** (바퀴 제거, 섀시를 받쳐 올림, 시리얼 모니터 115200 baud):

| 보내기 | 예상 결과 |
|---|---|
| `f` | `DIY_ESP32`를 알려 주는 기능 문자열 |
| *(출력 확인)* | 그럴듯한 배터리 전압과 초음파 거리 |
| `c60,0`, 그다음 `c0,0` | 왼쪽만 앞으로 돌아요 |
| `c0,60`, 그다음 `c0,0` | 오른쪽만 앞으로 돌아요 |

바퀴가 도는 동안 엔코더 카운트가 바뀌는지 확인하세요. 디스크가 스치거나, 전선이 뜨거워지거나, ESP32가 재시작하면 바로 멈추세요.

안드로이드 앱 **OpenBot Robot**을 설치하세요. **Bluetooth**를 선택하고 요청하는 권한을 허용한 다음 `OpenBot`을 검색해서 연결하세요. 유선 연결은 다음 단계를 참고하세요.

### 7. 배터리, 휴대폰 거치대, 덮개 설치하기

<p align="center">
  <img src="../../docs/images/compact/step_06_phone_lid.jpg" alt="휴대폰 거치대" width="32%" />
  <img src="../../docs/images/compact/lid_opening.jpg" alt="플러그와 그로밋을 끼운 덮개 구멍" width="32%" />
  <img src="../../docs/images/compact/step_07_closed.jpg" alt="닫힌 몸체" width="32%" />
</p>

거치대 받침의 포켓에 M3 너트 두 개를 눌러 넣으세요. 받침을 덮개 앞쪽 패드에 아래에서 M3 × 25 mm 나사 두 개로 고정하세요. 슬라이드 그립을 끼우고 고무줄이나 스프링으로 장력을 주세요. 케이스를 낀 휴대폰을 끼워서 단단히 고정되는지, 카메라 시야가 가려지지 않는지, 버튼이 눌리지 않는지 확인하세요.

**덮개 구멍.** 덮개에는 휴대폰 패드 앞과 뒤에 1 × 2 스터드 크기의 구멍이 하나씩 있어요. 휴대폰 패드가 앞쪽의 초음파 센서를 향하도록 덮개를 올리세요. 패드는 덮개 중심에서 16 mm 앞쪽에 있고, 뒤쪽 스터드 영역에는 배터리 박스가 올라가요. 쓰지 않는 구멍에는 `lid_plug`를 눌러 끼우세요. 스터드가 복원되고, 구멍 아래쪽의 1 mm 턱이 플러그가 빠지지 않게 받쳐 줘요. 케이블이 지나가는 곳에는 대신 `lid_grommet`을 사용하세요:

- **휴대폰 유선 연결:** 90° USB 케이블을 ESP32에서 앞쪽 구멍을 통해 위로 휴대폰까지 연결하세요. 휴대폰과 5 V 컨버터가 서로 전원을 공급하지 않도록 중간에 USB 전원 차단기를 넣으세요. 앱에서 Bluetooth 대신 **USB**를 선택하세요.
- **배터리 박스 (옵션 A와 B):** 배터리 전선이 뒤쪽 구멍으로 내려와요.

옵션 C를 쓴다면 지금 LiPo 트레이를 넣으세요. 전선을 가장자리에서 치우고 덮개를 섀시 위에 평평하게 올린 다음, M3 × 10 mm 나사 네 개 (와셔 없이)를 모서리 너트에 끼우세요. 닿을 때까지 고르게 조이세요. 덮개가 들리거나 거치대가 흔들리면 안 돼요. 옵션 A와 B는 휴대폰 거치대 뒤의 스터드에 배터리 박스를 눌러 끼우세요.

바퀴는 벤치 테스트를 통과한 뒤에만 끼우세요. 처음에는 넓고 평평한 바닥에서 천천히 달린 다음, 모든 나사와 휴대폰 그립을 다시 확인하세요. 로봇을 들 때는 덮개, 휴대폰 거치대, 배터리 박스가 아니라 섀시를 잡으세요.

## 드라이버 통합형 대체 PCB

<p align="center">
  <img src="../../docs/images/compact/pcb_option.jpg" alt="대체 PCB 자리" width="40%" />
</p>

섀시에는 OpenBot **PCB V2** (Arduino Nano + DRV8871 드라이버)도 장착할 수 있어요. 장착 구멍 배치는 121.031 × 28.448 mm예요. 이것은 다른 전기 구성이며, ESP32를 그대로 대체하는 보드가 **아니에요**. Nano 배선과 함께 `PCB_V2` 펌웨어로 동작해요.

- ESP32 캐리어, 고정 탭 4개, L298N, 전원 보드 스탠드는 빼세요.
- 긴 PCB용 보스 네 곳에 인서트 4개를 넣으세요. 센서용 4개와 초음파용 2개를 합쳐 **인서트는 모두 10개**예요.
- 보드는 출력한 2.4 mm 스페이서 네 개 위에 올리고, 각 구멍에 출력한 와셔 하나와 M3 × 10 mm 나사 하나를 쓰세요.
- 하드웨어 합계: **M3 × 10 10개** (덮개 4, PCB 4, 클립 2), **M3 × 25 14개**, 너트 14개.

[PCB V2 제조 파일과 부품 목록](../diy/pcb/v2)으로 보드를 주문하세요. 예를 들어 [JLCPCB](https://jlcpcb.com/)나 [PCBWay](https://www.pcbway.com/)를 이용할 수 있어요. 고정하기 전에 실장된 부품의 높이와 납땜 부위의 여유 공간을 확인하세요. 위의 배터리 옵션은 이 보드에서도 똑같이 쓸 수 있어요.

## 문제 해결

| 증상 | 확인할 점 |
|---|---|
| `ledcSetup` / `ledcAttachPin` 관련 컴파일 오류 | esp32 코어가 3.x예요. 2.0.17을 설치하세요. |
| USB 플러그가 ESP32에 들어가지 않음 | L298N이 가로막고 있어요. 90° 플러그를 쓰거나 드라이버를 빼고 펌웨어를 올리세요. |
| 배선 후 ESP32가 부팅되지 않거나 펌웨어가 올라가지 않음 | GPIO12/2/15/5에 부하가 걸려 있어요. L298N 입력을 분리하고 다시 시도하세요. |
| 모터가 돌기 시작하면 ESP32가 재시작함 | 배터리가 약하거나 방전되었거나, 5 V 컨버터가 2S 전압으로는 안정화하지 못해요. 부하 상태에서 5 V 라인을 측정하세요. |
| 초음파 값이 항상 0이거나 불규칙함 | `HAS_INDICATORS`가 아직 1이거나, 에코 분압기가 없거나 잘못되었어요 |
| 엔코더 하나에서 카운트가 없음 | 센서에 3.3 V가 공급되지 않았거나, 디스크가 슬롯에 없거나, 디스크가 불투명하지 않아요 |
| 직진해야 할 때 로봇이 돎 | 한쪽 모터 전선이 바뀌었어요. OUT1/OUT2 또는 OUT3/OUT4의 전선 쌍을 바꾸세요 |
| 배터리 값이 틀리거나 앱에 "부족"으로 표시됨 | 분압기는 30 k (위) / 10 k (아래)여야 하고, 전압 한계값이 선택한 옵션과 맞아야 해요 |
| 덮개가 제대로 닫히지 않음 | 전선이 가장자리에 끼었거나, LiPo가 너무 두껍거나, 너트가 리브를 완전히 지나 포켓 안까지 들어가지 않았어요 |
| 배터리 박스가 덮개에서 헐거움 | 출력된 스터드가 작게 나왔어요. 압출량을 조금 높이거나 프린터의 치수 정확도를 확인한 뒤 박스를 다시 출력하세요 |

## 보기

| 앞 | 뒤 |
|---|---|
| ![앞에서 본 모습](../../docs/images/compact/teaser_front.jpg) | ![뒤에서 본 모습](../../docs/images/compact/teaser_rear.jpg) |
| **위** | **아래** |
| ![위에서 본 모습](../../docs/images/compact/top_view.jpg) | ![아랫면](../../docs/images/compact/underside.jpg) |

렌더링에는 배선이 포함되어 있지 않아요. 보드, 배터리 홀더, LiPo 배터리는 단순화된 형태로 표시돼요.

## 다음

휴대폰에 [OpenBot Robot 앱](../../android/robot/README.ko-KR.md)을 설치하고 [6단계](#6-펌웨어-설정과-벤치-테스트)에 설명된 대로 로봇과 연결하세요.
