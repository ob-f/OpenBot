# 자작 OpenBot

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

우리는 저렴하고 쉽게 구할 수 있는 취미용 하드웨어를 이용해 바퀴 달린 로봇의 몸체를 디자인했어요. 아래에서 로봇을 만드는 방법을 확인할 수 있어요! 궁금한 점이나 걱정되는 부분이 있으면 언제든지 연락 주세요. 즐거운 로봇 만들기 되세요!

## 섀시

### 3D 프린팅

OpenBot을 만들기 위해 다음 부품들을 프린트하고 조립해야 해요!

#### 로봇 몸체

로봇 몸체는 여러분의 필요와 3D 프린터의 성능에 따라 여러 가지 선택지가 있어요! 직접 디자인해서 만들어보는 걸 추천하지만, 시작점으로 아래의 옵션들을 참고해보세요:

- [기본 몸체](cad/regular_body/README.ko-KR.md): 우리가 디자인한 표준 몸체로, 최소 240mmx150mm 크기의 빌드 플레이트가 필요해요.
- [슬림 몸체](cad/slim_body/README.ko-KR.md): 많은 3D 프린터가 작은 빌드 볼륨을 가지고 있기 때문에, 범퍼 없이 더 작은 크기로 디자인한 몸체예요. 이 몸체는 220mmx220mm 빌드 플레이트에서 45도 각도로 인쇄할 수 있어요.
- [접착식 몸체](cad/glue_body/README.ko-KR.md): 더 작은 빌드 볼륨을 가진 3D 프린터를 위한 모듈형 몸체예요. @sloretz가 디자인했으며, 여러 파츠를 접착제로 붙여야 해요. 150mmx140mm 빌드 플레이트에서 인쇄할 수 있어요.
- [블록형 몸체](cad/block_body/README.ko-KR.md): @Christos-Ps가 디자인한 이 몸체는 내부에 더 많은 공간을 제공하는 여러 가지 변형이 있고, 작은 공간을 유지하면서도 레고 호환 상단 옵션이 있어요. 인쇄에는 221mmx150mm만 필요해요.

#### 휴대폰 거치대

추가로, 로봇 몸체에 고정할 휴대폰 거치대를 출력해야 해요.

- phone_mount_bottom ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
- phone_mount_top ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

#### 세척하기

조립을 시작하기 전에 3D 출력물을 깨끗이 세척해야 할 수도 있어요.
<p float="left">
  <img src="../../docs/images/clean_3d_print_1.jpg" width="32%" />
  <img src="../../docs/images/clean_3d_print_2.jpg" width="32%" /> 
  <img src="../../docs/images/clean_3d_print_3.jpg" width="32%" />
</p>

### 대안

3D 프린터가 없다면, 시작점으로 사용할 수 있는 다양한 Arduino 로봇 자동차 키트를 사용할 수 있어요! 이런 키트들은 섀시, 모터, 그리고 액세서리를 포함하고 있어요. 비싼 키트에 포함된 전자 장치나 센서는 대부분 필요하지 않으니, 기본 키트를 추천드려요. 아래는 몇 가지 옵션이에요:

- Perseids DIY Robot Smart Car Chassis Kit ([EU](https://www.amazon.de/dp/B07DNXBNHY), [US](https://www.amazon.com/dp/B07DNXBFQN))
- SZDoit 4WD Smart Metal Robot Car Chassis Kit ([US](https://www.amazon.com/dp/B083K4RKBP), [AE](https://www.aliexpress.com/item/33048227237.html))
- Joy-it Robot Car Kit 01 ([EU](https://www.amazon.de/dp/B073ZGJF28))
- Smart Car Kit 4WD Smart Robot Car Chassis Kit ([AE](https://www.aliexpress.com/item/4001238626191.html))

또한, 전화 거치대도 필요해요. 여기 몇 가지 옵션이 있어요:

- Phone Mount ([EU](https://www.amazon.de/dp/B06XDYJNSR), [US](https://www.amazon.com/dp/B09CY8MC2R))

창의력을 발휘해서 원하는 재료(예: 나무, 판지, 스티로폼 등)로 OpenBot 섀시와 전화 거치대를 직접 만들어볼 수도 있어요! 만약 그렇게 한다면, [Slack 채널](https://github.com/ob-f/OpenBot#contact)에 사진을 올려주세요. 다른 사람들이 여러분의 멋진 창작물을 볼 수 있도록요! 아래는 [@custom-build-robots](https://custom-build-robots.com/roboter/openbot-dein-smartphone-steuert-ein-roboter-auto-chassis-bauen/13636) 님이 만든 예시예요:

<p float="left">
  <img src="../../docs/images/chassis_cardboard_1.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_2.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_3.jpg" width="32%" />
</p>

## 조립

로봇을 조립하는 방법은 DIY와 PCB 두 가지 옵션이 있어요.  
DIY 방식은 많이 사용되는 L298N 모터 드라이버를 사용하며, 전자 공학 경험이 조금 있는 취미가들에게 추천해요.  
모든 센서와 LED를 설치한다면 배선 작업이 꽤 많아지지만, 대부분의 나라에서 부품을 쉽게 구할 수 있어요.  
특히, 로봇을 한 대만 만들거나 프로젝트를 시험삼아 해보는 경우 DIY 옵션이 좋아요.

배선을 줄이고 조립을 더 쉽게 하기 위해 [맞춤형 PCB](pcb)도 개발했어요!  
더 깔끔한 작업을 원하거나 여러 대의 OpenBots를 만들 계획이라면 이 PCB를 사용하는 것을 추천해요.

### 부품 목록

우리의 로봇 몸체는 쉽게 구할 수 있는 취미 전자 부품들로 만들어져요.  
독일(EU)과 미국(US)을 위한 빠른 배송 링크를 제공해 드릴게요.  
조금 더 기다릴 수 있다면, AliExpress(AE)에서 훨씬 저렴하게 부품을 구할 수도 있어요.  
필요한 부품은 다음과 같아요!

#### 필요한 부품들

- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 4x TT 모터와 타이어 ([EU](https://www.conrad.de/de/p/joy-it-com-motor01-getriebemotor-gelb-schwarz-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspbe-1573543.html), [US](https://www.amazon.com/dp/B081YQM55P), [AE](https://www.aliexpress.com/item/4000126948489.html))
- 3x 18650 배터리 ([EU](https://www.conrad.de/de/p/conrad-energy-18650-usb-spezial-akku-18650-li-ion-3-7-v-1400-mah-1525536.html), [US](https://www.amazon.com/dp/B083K4XSKG), [AE](https://www.aliexpress.com/item/32352434845.html))
- 1x 18650 배터리 홀더 ([EU](https://www.amazon.de/dp/B075V25QJ9), [US](https://www.amazon.com/dp/B07DWQYD7H), [AE](https://www.aliexpress.com/item/33037738446.html))
- 1x USB OTG 케이블 ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x 스프링 또는 고무밴드 ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 16x M3x25 나사 ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 16x M3 너트 ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- 6x M3x5 나사 ([EU](https://www.amazon.de/dp/B01HBRG3W8), [US](https://www.amazon.com/dp/B07MBHMLL2), [AE](https://www.aliexpress.com/item/32892594230.html))
- 듀퐁 케이블 ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### 선택 부품

- 속도 센서 2개 ([EU](https://www.conrad.de/de/p/joy-it-sen-speed-erweiterungsmodul-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspberry-pi-pc-1646891.html), [US](https://www.amazon.com/dp/B081W2TY6Q), [AE](https://www.aliexpress.com/i/32850602744.html))
- 초음파 센서 1개 ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- On/Off 스위치 1개 ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 주황색 LED 5mm 2개 ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- OLED 디스플레이 1개 ([EU](https://www.amazon.de/dp/B079H2C7WH), [US](https://www.amazon.com/dp/B085NHM5TC), [AE](https://www.aliexpress.com/item/4001268387467.html))

#### DIY 구성품 (옵션 1)

- 1x L298N 모터 드라이버 ([EU](https://www.conrad.de/de/p/joy-it-motormodul-2-u-4-phasen-6-bis-12v-1573541.html), [US](https://www.amazon.com/dp/B085XSLKFQ), [AE](https://www.aliexpress.com/item/32994608743.html))
- (선택 사항) 저항 (LED용 150<span>&#8486;</span> 2개, 전압 분배기용 20k<span>&#8486;</span>와 10k<span>&#8486;</span> 1개씩)
- (세트) TT 모터 4개 & 타이어 + L298N 2개 + 듀퐁 케이블 ([US](https://www.amazon.com/dp/B07ZT619TD))
- (세트) TT 모터 4개 & 타이어 + 전선 + 나사 ([US](https://www.amazon.com/dp/B07DRGTCTP))

#### PCB 구성 요소 (옵션 2)

- 1x [커스텀 PCB](pcb)
- 5x Micro JST PH 2.0 케이블 ([EU](https://www.amazon.de/gp/product/B07449V33P), [US](https://www.amazon.com/dp/B09JZC28DP), [AE](https://www.aliexpress.com/item/32963304134.html))

### 빌드 안내

**팁:** 이미지를 클릭하면 새 탭에서 전체 해상도로 볼 수 있어요!

#### 옵션 1: DIY

<p float="left">
  <img src="../../docs/images/diy_parts.jpg" height="300" />
  <img src="../../docs/images/wiring_diagram.png" height="300" /> 
</p>

**팁:** 모든 배선을 쉽게 하기 위해 5V와 GND 연결을 위한 작은 전원 분배기를 제작할 수 있습니다. 퍼포보드에 6x2 남성 헤더를 납땜하고, 이를 모터 드라이버의 5V / GND에 연결하세요.

1. 모터에 배선을 납땜하고, 속도 센서를 사용할 경우 두 개의 앞 모터에 엔코더 디스크를 추가하세요.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. 왼쪽 모터 두 개의 양극과 음극을 L298N 보드의 OUT1 (+)과 OUT2 (-)에 연결하세요. 오른쪽 모터 두 개의 양극과 음극을 OUT4 (+)와 OUT3 (-)에 연결하세요.
3. 모터를 여덟 개의 M3x25 나사와 너트를 사용하여 장착하세요.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
4. L298N을 네 개의 M3x5 나사로 장착하세요.
5. (선택 사항) 초음파 센서를 설치하고, 각도를 조정한 커넥터를 직선형으로 교체하세요 (혹은 핀을 조심스럽게 구부릴 수 있어요).
    <p float="left">
      <img src="../../docs/images/sonar_front.jpg" width="32%" />
      <img src="../../docs/images/sonar_back.jpg" width="32%" /> 
      <img src="../../docs/images/sonar_bend_pins.jpg" width="32%" />
    </p>
6. (선택 사항) 지시 신호용 주황색 LED를 설치하세요.
    <p float="left">
      <img src="../../docs/images/led_insert.jpg" width="32%" />
      <img src="../../docs/images/led_left.jpg" width="32%" /> 
      <img src="../../docs/images/led_right.jpg" width="32%" />
    </p>
7. 전화기 장착 부품의 하단을 상판에 M3x25 나사 두 개와 너트를 사용해 장착하세요.
    <p float="left">
      <img src="../../docs/images/install_camera_mount_1.jpg" width="32%" />
      <img src="../../docs/images/install_camera_mount_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_camera_mount_3.jpg" width="32%" />
    </p>
8. 전화기 장착 부품의 상단을 삽입하고 스프링이나 고무 밴드를 설치하세요.
    <p float="left">
      <img src="../../docs/images/install_spring_1.jpg" width="32%" />
      <img src="../../docs/images/install_spring_2.jpg" width="32%" /> 
    </p>
9. 각도를 조정한 커넥터를 직선형으로 교체하고 (혹은 핀을 구부린 후) 속도 센서를 M3x5 나사로 하나씩 장착하세요.
    <p float="left">
      <img src="../../docs/images/install_speed_sensor_1.jpg" width="32%" />
      <img src="../../docs/images/install_speed_sensor_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_speed_sensor_3.jpg" width="32%" />
    </p>
10. 배터리 케이스를 설치하세요 (예: 벨크로).
    <p float="left">
      <img src="../../docs/images/install_battery_1.jpg" width="32%" />
      <img src="../../docs/images/install_battery_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_battery_3.jpg" width="32%" />
    </p>
11. (선택 사항) 전원 스위치를 삽입하고 전류 경로에 넣으세요.
    1. 스위치를 적당한 구멍에 넣고 클릭 소리가 날 때까지 밀어 넣으세요.
    2. 배터리 케이스의 빨간색 와이어(12V)와 전원 케이블의 빨간 와이어를 각각 스위치의 핀에 납땜하세요. 검정색 와이어(GND)는 연결하고 열 수축 튜브로 덮으세요.
    3. 케이블을 테이프 등으로 고정하세요.
    <p float="left">
      <img src="../../docs/images/install_switch_1.jpg" width="32%" />
      <img src="../../docs/images/install_switch_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_switch_3.jpg" width="32%" />
    </p>
12. (선택 사항) OLED 디스플레이를 장착하세요.
13. L298N의 PWM 입력을 Arduino의 D5, D6, D9, D10 핀에 연결하세요.
14. 속도 센서와 초음파 센서를 5V와 GND에 연결하세요.
15. 속도 센서의 D0 핀을 Arduino의 D2 (왼쪽) 및 D3 (오른쪽) 핀에 연결하세요.
16. 초음파 센서의 Echo와 Trigger 핀을 각각 Arduino의 D11과 D12 핀에 연결하세요.
17. (선택 사항) LED를 Arduino의 D4 (왼쪽) 및 D7 (오른쪽) 핀에 연결하고 GND에 연결하세요. 전류 제한을 위해 150Ω 저항을 직렬로 추가하는 것을 권장합니다.
18. (선택 사항) 전압 분배기를 Arduino의 A7 핀에 연결하세요. 이 기능은 배터리 전압을 측정하는 데 사용됩니다.
19. (선택 사항) OLED 디스플레이(SSD1306 칩)를 I2C 버스를 통해 Arduino Nano에 연결하세요.
    1. 디스플레이의 VIN과 GND 핀을 5V와 GND에 연결하세요.
    2. 디스플레이의 SCL 핀을 A5 핀에 연결하세요.
    3. 디스플레이의 SDA 핀을 A4 핀에 연결하세요.
20. L298N의 +12V와 GND에 전원 케이블을 연결하세요.
21. USB 케이블을 Arduino에 연결하고 상판을 통해 케이블을 통과시키세요.
22. 하판에 여섯 개의 M3 너트를 넣고 여섯 개의 M3x25 나사로 상판을 장착하세요.
23. 바퀴를 장착하세요.

#### 옵션 2: 맞춤형 PCB

1. 모터에 Micro JST PH 2.0 커넥터를 납땜하고, 속도 센서를 사용하려면 두 개의 앞 모터에 인코더 디스크를 추가하세요.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. M3x25 나사와 너트를 사용하여 모터를 장착하세요.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
3. 왼쪽 두 모터는 M3과 M4에, 오른쪽 두 모터는 M1과 M2에 연결하세요.
    <p float="left">
      <img src="../../docs/images/connect_motors_pcb.jpg" width="32%" />
    </p>
4. PCB는 M3x5 나사로 4개를 사용하고, 모터는 M3x25 나사와 너트로 8개를 사용하여 장착하세요.
    <p float="left">
      <img src="../../docs/images/attach_pcb.jpg" width="32%" />
      <img src="../../docs/images/chassis_motors_pcb.jpg" width="32%" />
    </p>
5. DIY 옵션의 5-12단계를 따르세요.
6. 초음파 센서 (VCC/+, Trig, Echo, GND/ -)를 PCB의 *SONAR*라고 표시된 4핀 헤더에 연결하세요.
    <p float="left">
      <img src="../../docs/images/connect_sonar_sensor.jpg" width="32%" />
    </p>
7. 왼쪽과 오른쪽 표시등 신호 (오렌지 LED)를 PCB의 *SIGNAL_L*과 *SIGNAL_R*로 표시된 2핀 헤더에 연결하세요. 긴 다리는 +이고 짧은 다리는 -입니다.
8. 왼쪽과 오른쪽 속도 센서 (VCC/+, GND/ -, D0)를 *SPEED_L*과 *SPEED_R*로 표시된 3핀 헤더에 연결하세요.
9. (선택 사항) OLED 디스플레이 (SSD1306 칩)를 PCB의 IO2 헤더에 연결하세요.
    1. 디스플레이의 VIN과 GND 핀을 5V와 GND에 연결하세요.
    2. 디스플레이의 SCL 핀을 A5 핀에 연결하세요.
    3. 디스플레이의 SDA 핀을 A4 핀에 연결하세요.
10. 전원 케이블을 PCB의 Vin (Micro JST PH 2.0 커넥터)에 연결하세요.
11. DIY 옵션의 21-23단계를 따르세요.

## 다음

[Arduino 펌웨어](../../firmware/README.ko-KR.md)를 플래시하세요!
