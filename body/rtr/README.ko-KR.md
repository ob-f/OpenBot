# 오픈봇: 준비된 RTR 차량

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

오픈봇 차량의 준비된 실행(RTR) 버전은 직접 로봇을 제작할 시간이나 의지가 없는 사용자를 대상으로 합니다. RTR 차량은 완전 통합된 전자 장치를 포함하고 있으며, 펌웨어 차원에서 이미 지원되고 소프트웨어와 하드웨어 관점에서 철저히 테스트되었습니다. RTR 차량은 "RTR_TT"와 "RTR_520"이라는 두 가지 종류로 제공됩니다. 두 차량 모두 방수 ABS 외관을 기반으로 제작되었지만, 서로 다른 목적에 맞춰 설계되었습니다. RTR_TT는 주로 실내 사용을 목적으로 하며, RTR_520은 더 강력한 프로세서, 더 나은 모터, 견고한 금속 기어박스를 갖추고 있고, 실내외 모두에서 사용할 수 있는 올터레인 휠을 제공합니다.
<p align="center">
  <a> <img src="/docs/images/RTR_TT.jpg" width="35.8%" /> &nbsp
  </a>
  <a> <img src="/docs/images/RTR_520.jpg" width="33%" />
  </a>
</p>

### 주문하기

RTR(OpenBot 준비된 차량)은 [여기](http://www.openbot.info/)에서 주문할 수 있습니다.

## RTR 직접 제작하기

RTR OpenBot을 직접 만들고 싶다면, 섀시를 3D 프린팅하고, PCB를 제작하며, 모터와 스마트폰 거치대를 구매해야 합니다.

### 3D 프린팅

직접 RTR OpenBot을 프린팅하려면 다음 부품들을 출력해야 합니다.

1) ```shell_bottom``` ([STL](cad/rtr_bottom.stl), [STEP](cad/rtr_bottom.step))
2) ```shell_top``` ([STL](cad/rtr_top.stl), [STEP](cad/rtr_top.step))
3) ```phone_mount``` ([STL](cad/rtr_mount.stl), [STEP](cad/rtr_mount.step))

<p align="center">
  <img src="../../docs/images/rtr_tt_assembly.gif" width="600" alt="App GUI"/>
</p>

### PCB 제작

각 PCB에는 세 가지 파일이 있어요. Gerber 파일은 실제 PCB를 포함하고, BOM(자재 명세서) 파일은 PCB에 납땜할 모든 부품을 포함하며, Centroid 파일은 자동 PCB 조립을 위한 각 부품의 좌표를 포함해요. 기본 보드는 대부분의 부품이 포함된 보드예요. 기본 보드에는 세 가지 변형이 있어요. 변형 A는 외부 모터 드라이버 보드와 외부 마이크로컨트롤러 보드를 위한 커넥터가 있는 맨땅의 보드예요. 변형 B는 외부 마이크로컨트롤러를 위한 핀 헤더가 있는 모듈형 보드예요. 변형 C는 완전히 통합된 기본 보드로, 대부분의 사용자에게 추천하는 보드예요. 전면 범퍼 센서 보드는 두 개의 범퍼 센서, 소나 센서, 그리고 USB 드라이버를 포함해요. 전면 범퍼 센서 보드는 두 가지 변형이 있어요. 하나는 더 저렴한 CH340G USB 드라이버가 있는 것이고, 다른 하나는 더 신뢰성 있는 CP2102N USB 드라이버가 있는 거예요. TT-motor 또는 520-motor 중 어떤 버전을 만들지에 따라 필요한 PCB가 달라져요.

#### TT 모터

- 1x Base 보드 (Arduino)
- 1x 상태 LED 보드
- 1x 앞/위/뒤 범퍼 센서 보드
- 4x 속도 센서 보드 (Arduino)

#### 520 모터

- 1x Base 보드 (ESP32)
- 1x 상태 LED 보드
- 1x 앞/위/뒤 범퍼 센서 보드

#### 보드 참조

- **상태 LED 보드** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_Status_LED_Board_V1.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_Status_LED_Board_V1.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_Status_LED_Board_V1.csv))
- **상단 범퍼 센서 보드** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BumpSensorTop_V1.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BumpSensorTop_V1.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BumpSensorTop_V1.csv))
- **후면 범퍼 센서 보드** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BumpSensorBack_V1.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BumpSensorBack_V1.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BumpSensorBack_V1.csv))
- **전면 범퍼 센서 보드 (CH340G)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SensorBoardFront_CH340G_V1.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SensorBoardFront_CH340G_V1.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SensorBoardFront_CH340G_V1.csv))
- **전면 범퍼 센서 보드 (CP2102N)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SensorBoardFront_CP2102N_V1.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SensorBoardFront_CP2102N_V1.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SensorBoardFront_CP2102N_V1.csv))
- **속도 센서 보드 (Arduino)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SpeedSensor_Arduino_V1.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SpeedSensor_Arduino_V1.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SpeedSensor_Arduino_V1.csv))
- **통합 베이스 보드 C (Arduino)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1C.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1C.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1C.csv))
- **통합 베이스 보드 C (ESP32)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1C.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1C.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1C.csv))
- **모듈형 베이스 보드 B (Arduino)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1B.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1B.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1B.csv))
- **모듈형 베이스 보드 B (ESP32)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1B.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1B.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1B.csv))
- **기본 베이스 보드 A (Arduino)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1A.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1A.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1A.csv))
- **기본 베이스 보드 A (ESP32)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1A.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1A.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1A.csv))
- **모터 드라이버 DRV8870 보드 (Arduino)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_MotorBoard_Arduino_V1_DRV8870.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_MotorBoard_Arduino_V1_DRV8870.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_MotorBoard_Arduino_V1_DRV8870.csv))
- **모터 드라이버 DRV8870 보드 (ESP32)** ([Gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_MotorBoard_ESP32_V1_DRV8870.zip), [BOM](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_MotorBoard_ESP32_V1_DRV8870.csv), [Centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_MotorBoard_ESP32_V1_DRV8870.csv))

## 다음 단계

[Arduino 펌웨어](../../firmware/README.ko-KR.md)를 플래시하세요!

