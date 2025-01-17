# OpenBot: RC-Truck Body

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

우리는 널리 사용 가능한 1:16 스케일의 RC 장난감 트럭/버기(예: [이 제품](https://www.amazon.de/dp/B00M3J7DJW))를 위해 설계된 로봇 바디를 개발했습니다.

![RC-Truck-Banner](/docs/images/rc-truck-banner.jpg)

우리는 또한 저비용의 일반적인 취미용 하드웨어를 활용한 간단한 바퀴형 로봇을 위한 [바디](/body/)를 제공합니다. 일반 OpenBot의 제작 설명서는 [여기](/body/README.ko-KR.md)에서 확인할 수 있어요!

## 섀시

OpenBot RC 트럭의 섀시는 두 가지 주요 구성 요소로 이루어져 있어요:  
(a) 여러분이 선택한 **1:16 크기의 RC 장난감 트럭**  
(b) 우리가 제공하는 **3D 프린트 가능한 커스텀 디자인 부품**이에요.

### 1:16 크기 RC 장난감 트럭

직접 OpenBot RC 트럭을 만들려면 **1:16 크기의 RC 장난감 트럭/버기카**가 필요해요.  
독일([EU](https://www.amazon.de/dp/B00M3J7DJW)), ([EU](https://www.amazon.de/dp/B088FGVYNW)), 미국([US](https://www.amazon.com/gp/product/B09C8XMPQ9))에서 빠르게 배송되는 호환 RC 트럭의 Amazon 링크를 제공해 드릴게요.  
또한, eBay, Alibaba, AliExpress 등 다른 온라인 쇼핑몰에서도 비슷한 1:16 크기의 장난감 트럭을 찾을 수 있어요. 다만, 가격은 저렴할 수 있지만 배송이 느릴 수 있다는 점을 참고하세요.

구매할 RC 트럭의 판매처나 버전에 관계없이, 반드시 **1:16 크기의 RC 트럭**인지 확인해야 해요!  
이 크기가 중요한 이유는, 우리가 제공하는 3D 프린트 부품이 현재 **1:16 크기 트럭**에 맞도록 설계되어 있기 때문이에요. (소소한 조정은 가능해요. 자세한 내용은 이후에 알려드릴게요!)  
호환 가능한 1:16 크기의 RC 트럭/버기카 몇 가지 예시는 아래와 같아요. 😊

<p float="left">
  <a href="https://www.amazon.de/dp/B00M3J7DJW" target="_blank"> <img src="/docs/images/rc_toy_1.jpg" width="34%" /> &nbsp
  </a>
  <a href="https://www.amazon.com/gp/product/B09C8XMPQ9" target="_blank"> <img src="/docs/images/rc_toy_2.jpg" width="27%" /> &nbsp &nbsp &nbsp &nbsp
  </a>
  <a href="https://www.amazon.de/dp/B088FGVYNW" target="_blank"> <img src="/docs/images/rc_toy_3.jpg" width="27%" />
  </a>
</p>

### 3D 프린팅

OpenBot RC 트럭을 만들기 위해서는 아래의 부품들을 프린트해야 해요.

1) ```main_frame``` ([STL](cad/rc_truck_body/main_frame.stl), [STEP](cad/rc_truck_body/main_frame.step))
2) ```side_cover``` \[x2\] ([STL](cad/rc_truck_body/side_cover.stl), [STEP](cad/rc_truck_body/side_cover.step))
3) ```phone_mount_bottom``` ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
4) ```phone_mount_top``` ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

\[xN\]은 해당 부품을 몇 개 프린트해야 하는지를 나타내며, 필요한 수만큼 프린트하면 돼요.

아래 부품들은 선택 사항이지만, OpenBot RC 트럭을 더 컴팩트하고 미적으로 보기 좋게 만들기 위해 추천됩니다.

5) ```camera_elevator``` ([STL](cad/rc_truck_body/camera_elevator.stl), [STEP](cad/rc_truck_body/camera_elevator.step))
6) ```electronics_cover``` \[x2\] ([STL](cad/rc_truck_body/electronics_cover.stl), [STEP](cad/rc_truck_body/electronics_cover.step))
7) ```spacer``` \[x4\] ([STL](cad/rc_truck_body/spacer.stl), [STEP](cad/rc_truck_body/spacer.step))
8) ```front_light_spacer``` \[x2\] ([STL](cad/rc_truck_body/front_light_spacer.stl), [STEP](cad/rc_truck_body/front_light_spacer.step))

위의 모든 부품은 **최소 260mm x 220mm** 크기의 빌드 플레이트가 필요해요. 이는 ```main_frame```의 크기와 일치해요.

많은 일반적인 3D 프린터는 빌드 용량이 더 작아서(보통 220mm x 220mm), 이를 해결할 수 있는 두 가지 방법이 있어요.
1. 첫 번째 방법은 ```main_frame```을 45도 각도로 기울여서 추가적인 서포트 재료를 사용해 프린트하는 방법이에요.
2. 두 번째 방법은 원래의 ```main_frame``` 부품을 수정하는 방법이에요. [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) 같은 CAD 소프트웨어를 사용하여 원본 파일을 자르고, 이를 두 개 또는 세 개의 더 작은 부품으로 나누어 프린트할 수 있어요. 이렇게 하면 **220mm x 220mm** 크기의 빌드 플레이트에 맞출 수 있고, 프린트한 후에 이들을 결합할 수 있어요.  
   추후에는 이와 같은 **모듈형 버전의 ```main_frame```**을 제공할 수도 있어요. 그 외 모든 부품은 **최소 220mm x 60mm** 크기의 빌드 플레이트가 필요합니다.

**Ultimaker S5**에서 프린트한 결과는 아래와 같은 설정으로 좋았어요:

- 층 높이: 0.2mm
- 벽 두께: 1.5mm
- 인필 밀도: 20%
- 인필 패턴: 그리드
- 인쇄 속도: 80 mm/s
- 서포트 없음

이 설정으로 PLA, CPE, ABS로 프린팅을 시도했어요. 프린트 결과는 프린팅 설정에 크게 영향을 받지 않았어요. 그러나 인내심이 있다면 더 작은 층 높이로 더 느리게 프린트하면 퀄리티가 향상될 수 있고, 서포트 구조를 추가하면 더 나은 결과를 얻을 수 있어요. 다만, 서포트를 추가하면 이후 제거 작업이 필요해요.

프린트 후 부품을 청소할 필요가 있을 수도 있어요. 하지만 위의 설정을 사용했을 때, 우리의 빌드 과정에서는 특별한 정리나 파일링이 필요하지 않았어요. 가능하다면 두 가지 색상(예: 초록/검정 또는 빨강/검정)을 조합해서 부품을 프린트하는 것도 좋습니다. 아래 이미지는 그 예시입니다.

**팁:** 이미지를 클릭하면 더 큰 해상도로 볼 수 있어요.

<p float="left">
  <img src="/docs/images/3d_print_rc_1.png" width="32%" />
  <img src="/docs/images/3d_print_rc_2.png" width="32%" /> 
  <img src="/docs/images/3d_print_rc_3.png" width="32%" />
</p>

### Assembly

OpenBot RC-Truck을 구축하는 방법은 일반적인 OpenBot처럼 DIY 방식으로 진행할 수도 있지만, **OpenBot [커스텀 PCB](/body/pcb)**를 사용하는 방법을 추천합니다. 이 옵션은 보다 깔끔한 빌드를 원하거나 여러 개의 OpenBot RC-Truck을 만들 계획이 있다면 이상적입니다. 커스텀 PCB를 사용하면 하나의 PCB로 여러 OpenBot 바디 간에 부품을 쉽게 교체하고 사용할 수 있는 장점도 있습니다.

### 자재 목록 (Bill of Materials)

OpenBot RC-Truck은 주로 일반적인 취미용 전자 부품을 사용합니다. 우리는 독일(EU)과 미국(US)에서 빠르게 배송되는 아마존 링크를 제공하며, 조금 더 기다릴 수 있다면 AliExpress(AE)에서 더 저렴하게 부품을 구할 수 있습니다. 다음은 필요한 부품 목록입니다.

#### 자재 목록(필수)

- 1x RC-toy truck/buggy ([EU](https://www.amazon.de/dp/B00M3J7DJW), [EU](https://www.amazon.de/dp/B088FGVYNW), [US](https://www.amazon.com/gp/product/B09C8XMPQ9))
- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 1x OpenBot [Custom PCB](/body/pcb)
- 1x USB OTG cable ([EU](https://www.amazon.de/gp/product/B075M4CQHZ) ,[US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x spring or rubber band ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 6x M3x25 screw ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 6x M3 nut ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- Dupont cables ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### 자재 목록(선택)

- 1x Ultrasonic Sensor ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 2x On/Off Switch ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 4x Orange LED 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 4x Red LED 5mm ([EU](https://www.amazon.de/dp/B083HN3CLY), [US](https://www.amazon.com/dp/B077X95F7C), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 2x White LED lamps ([EU](https://www.amazon.de/-/en/gp/product/B06XTQSZDX), [US](https://www.amazon.com/gp/product/B01N2UPAD8), [AE](https://de.aliexpress.com/item/1005002991235830.html))
- Variable Resistor for LEDs ([EU](https://www.amazon.de/gp/product/B081TXJJGV), [US](https://www.amazon.com/dp/B0711MB4TL), [AE](https://de.aliexpress.com/item/1005003610664176.html))


### 빌드 지침

**팁:** 이미지를 클릭하면 새로운 탭에서 전체 해상도로 열립니다.

1. RC 장난감 트럭을 분해하세요. 트럭의 상단 커버를 제거하고, 아래 그림처럼 네 개의 장착 핀을 분리하세요. 이 네 개의 장착 핀과 나사는 나중에 ```main_frame```을 RC 트럭 본체에 장착할 때 사용하므로 안전하게 보관해 주세요. 호환 가능한 RC 장난감 트럭에는 두 개의 모터가 있습니다: 하나는 스로틀용, 다른 하나는 조향용, 그리고 스로틀 모터용 속도 제어기(내장 5-7V UBEC 포함)와 2S 7.4V LiPo 배터리 팩이 있습니다. 배터리 팩을 트럭 바닥에서 분리하여 충전기를 사용해 충전하세요. 두 모터의 와이어 커넥터와 속도 제어기의 UBEC 출력을 분리하고 풀어주세요. 우리 경우 UBEC 출력은 6V였습니다.
    <p float="left">
      <img src="/docs/images/rc_truck_disassembly_1.JPG" width="32%" />
      <img src="/docs/images/rc_truck_disassembly_2.JPG" width="32%" /> 
      <img src="/docs/images/rc_truck_disassembly_3.JPG" width="32%" />
    </p>
2. ```main_frame```의 두 치수 d1과 d2(아래 그림 참조)는 사용한 RC 장난감 트럭 모델에 따라 달라집니다. 우리는 [이 모델](https://www.amazon.de/dp/B00M3J7DJW)을 위해 ```main_frame``` 부품을 설계했습니다. 사용하는 (1:16 스케일) 트럭에 따라 이 치수를 조금 조정해야 할 수도 있습니다. 이를 위해서는 ```main_frame``` [STEP](/body/cad/rc_truck_body/main_frame.step) 파일을 사용해 주세요. CAD 수정에는 [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview)을 사용하는 것을 추천드립니다 (Fusion 360은 1년 무료 학술 라이센스를 제공합니다). 또한, ```main_frame```의 작은 쐐기/삼각형 모양은 전방 방향을 나타낸다는 점을 기억해 주세요.
    <p float="left">
      <img src="/docs/images/main-frame-dimensions.png" width="32%" />
      <img src="/docs/images/main-frame-direction.png" width="32%" />
    </p>   
3. (선택 사항) 로봇을 켜고 끄는 ON/OFF 스위치를 설치할 수 있습니다. 이는 스피드 컨트롤러에서 배터리로 가는 양극선을 자르고 그 사이에 스위치를 납땜하여 간단히 설치할 수 있습니다. 스위치 커넥터는 수축 튜브나 전기 테이프로 절연해 주세요. 또한, 스위치가 ```main_frame```의 후면 사각형 구멍을 통해 들어갈 수 있도록 전선이 충분히 길어야 합니다 (아래 그림 참조).
    <p float="left">
      <img src="/docs/images/main-frame-switch.png" width="32%" />
      <img src="/docs/images/switch-power.jpg" width="32%" />
    </p>
4. (선택 사항) ```main_frame```의 앞부분 그릴을 통해 초음파 센서를 설치할 수 있습니다. 필요하면 핫 글루로 고정해 주세요. 커넥터를 직선으로 눌러서 제자리에 놓기 전에, 조심스럽게 연결을 정렬하면 조립 후 커넥터 접근이 더 쉬워집니다. 초음파 커넥터에서 나오는 듀폰 케이블을 뒤쪽 ```main_frame```의 사각형 구멍까지 연결해 주세요.
    <p float="left">
      <img src="/docs/images/install-ultrasonic-1.png" width="32%" />
      <img src="/docs/images/ultrasonic-sensor.jpg" width="32%" />
      <img src="/docs/images/install-ultrasonic-2.png" width="32%" />
    </p>
5. (선택 사항) ```main_frame```의 앞과 뒤에 오렌지색 LED를 설치할 수 있습니다. 필요하면 핫 글루로 고정해 주세요. 각 측면(왼쪽, 오른쪽)마다 앞뒤 LED를 병렬로 연결해야 합니다. 이를 위해 각 LED의 양극과 음극을 각각 연결하세요. 초음파 센서 케이블과 비슷하게, 왼쪽과 오른쪽 표시등의 양극과 음극 듀폰 케이블을 모두 뒤쪽 ```main_frame```의 사각형 구멍까지 연결하여 PCB의 해당 표시등 신호 핀(양극 및 음극)에 연결합니다.
    <p float="left">
      <img src="/docs/images/insert-leds-orange-1.png" width="32%" />
      <img src="/docs/images/orange-led.jpg" width="32%" />
      <img src="/docs/images/insert-leds-orange-2.png" width="32%" />
    </p>
**팁:** 배선 중에 혼잡함을 피하고 접지 실수를 방지하려면, 모든 LED의 음극을 연결하는 통합 접지 루프를 만드는 것이 좋습니다. 이는 ```main_frame``` 아래에 전선을 깔아 모든 LED의 음극을 연결하는 방법입니다. 그런 다음 이 접지 루프는 하나의 듀폰 케이블을 통해 Arduino Nano의 접지 핀에 연결할 수 있습니다. 이 케이블은 뒤쪽 ```main_frame```의 사각형 구멍을 통해 나옵니다.

6. (선택 사항) 앞 LED 램프를 설치할 수 있습니다. 베이스를 핫 글루로 고정하고, 각 측면의 앞부분에 있는 구멍을 통해 램프를 설치하세요. 앞 LED 램프는 병렬로 연결하고, 양극과 음극을 각각 연결합니다. 이 램프는 6V에서 작동하므로 UBEC 출력의 양극에 직접 연결할 수 있습니다. 음극은 접지 루프에 연결하세요(위 팁 참조). 이 LED의 내부 저항이 꽤 높기 때문에 외부 저항을 추가할 필요는 없습니다. LED 램프를 설치한 후, 두 개의 ```front_light_spacers```를 양쪽에 삽입하여 LED를 고정해 주세요.
    <p float="left">
      <img src="/docs/images/insert-lamps-1.png" width="32%" />
      <img src="/docs/images/led-lamp-wiring.jpg" width="32%" />
      <img src="/docs/images/add_front_light_spacer.png" width="32%" />
    </p>
7. (선택 사항) 후면 빨간 LED를 설치할 수 있습니다. 필요하면 핫 글루로 고정해 주세요. 빨간 LED 네 개를 병렬로 연결합니다. 즉, 양극과 음극을 각각 연결하세요. 음극은 접지에 연결하고, 양극은 UBEC 출력을 통해 전압 분배기를 사용하여 연결합니다 (다음 단계에서 전압 분배기 만드는 방법 참조).
    <p float="left">
      <img src="/docs/images/insert-leds-red.png" width="32%" />
      <img src="/docs/images/red-led.jpg" width="32%" />
    </p>
8. (선택 사항) 후면 빨간 LED를 위한 전압 분배기를 설치할 수 있습니다. 대부분의 색상 LED(예: 빨간색, 오렌지색, 노란색 등)는 5V가 아닌 2-3V에서 작동합니다. 따라서 이러한 LED를 안전하게 작동시키려면 전압 분배기가 필요합니다. 표시등 LED에는 이미 커스텀 PCB에 내장된 전압 분배기가 있기 때문에 따로 조정할 필요는 없습니다. 하지만 후면 LED인 빨간색 LED를 추가하려면 외부 전압 분배기가 필요합니다. 전압 분배기를 만들 때는 10kΩ 이상의 가변 저항기를 사용하는 것이 좋습니다. UBEC 출력 전압(우리 경우 6V)을 사용하여 2-3V 출력이 나오도록 전압 분배기를 설정하세요. 이를 위해 가변 저항기의 양쪽 끝에 UBEC 출력을 적용하고, 멀티미터로 중간 단자의 전압을 확인하면서 저항기의 나사를 조정하세요. 적절한 2-3V 범위에 전압을 설정한 후, 그 나사를 고정하고 핫 글루로 고정한 뒤 ```main_frame``` 아래에 적당한 위치에 놓으세요.
    <p float="left">
      <img src="/docs/images/variable-resistor.jpg" width="32%" />
      <img src="/docs/images/voltage-divider-animation.png" width="32%" />
    </p>
9. (선택 사항) 앞뒤 LED를 켜고 끄기 위해 하나 또는 두 개의 ON/OFF 스위치를 사용할 수 있어요. 이 목적을 위해 스위치(또는 여러 스위치)를 설치하려면 3단계의 지침을 따라주세요.
10. 이제 로봇의 배선 작업은 거의 끝났어요. 이 시점에서, ```main_frame``` 아래의 모든 배선과 연결이 정확하고 잘 절연되어 있는지 확인하는 시간을 가지세요. 축소 튜브나 전기 테이프를 사용해서 잘 보호하고, 뜨거운 접착제를 사용해 느슨한 배선이 로봇 조립 후 바퀴나 움직이는 부품에 닿지 않도록 고정해 주세요. 모터, 속도 조절기 UBEC, LED, 초음파 센서에서 나온 모든 케이블이 ```main_frame``` 뒷면의 직사각형 구멍을 자유롭게 통과할 수 있는지 확인하세요.
11. ```phone_mount_bottom```을 두 개의 M3x25 나사와 너트를 사용하여 ```main_frame```에 장착하세요. 원하는 경우, ```camera_elevators```를 하나 이상 넣어 전화 거치대의 수직 높이를 조정할 수 있어요. 만약 ```camera_elevator```를 사용한다면, 전화 거치대를 ```main_frame```에 장착하기 위해 M3x35 이상 길이의 나사가 필요해요.
    <p float="left">
      <img src="/docs/images/add_phone_mount_bottom.png" width="32%" />
      <img src="/docs/images/add_phone_mount_bottom_elevator.png" width="32%" /> 
    </p>
10. ```phone_mount_top```을 삽입하고 스프링 또는 고무밴드를 설치하세요.
    <p float="left">
      <img src="/docs/images/add_phone_mount_top.png" width="32%" />
    </p>
11. 두 개의 ```side_covers```를 각자의 슬롯에 삽입하세요.
    <p float="left">
      <img src="/docs/images/add_side_covers.png" width="32%" />
      <img src="/docs/images/add_side_covers_2.png" width="32%" />
    </p>    
12. 네 개의 장착 핀과 해당 나사를 사용하여 ```main_frame```을 RC-트럭 본체에 장착하세요. 모든 케이블 커넥터와 로봇의 전원 스위치가 ```main_frame```의 뒷면에 있는 직사각형 구멍을 통해 PCB 연결을 위해 접근할 수 있도록 하세요. 배터리 커넥터는 ```main_frame```의 앞쪽 삼각형 구멍에서 꺼내세요.
    <p float="left">
      <img src="/docs/images/add_main_frame_1.JPG" width="32%" />
      <img src="/docs/images/add_main_frame_2.png" width="32%" />
      <img src="/docs/images/add_main_frame_3.JPG" width="32%" />
    </p>
12. 네 개의 M3x25 나사와 너트로 PCB를 ```main_frame```의 뒷면에 네 개의 ```spacers```를 사이에 두고 장착하세요. PCB에 Arduino Nano를 장착하고 Arduino Nano의 USB 포트에 USB OTG 케이블을 연결하세요.
    <p float="left">
      <img src="/docs/images/pcb_assembly.JPG" width="32%" />
    </p>
13. 초음파 센서 케이블을 PCB의 "sonar" 커넥터에 연결하세요. +ve/-ve 극성과 데이터 라인이 센서와 PCB 포트 간에 올바르게 일치하는지 확인하세요.
14. 왼쪽과 오른쪽 표시등 LED 케이블을 PCB의 해당 표시 신호 커넥터에 연결하세요. LED의 +ve 및 -ve 단자가 올바르게 연결되었는지 확인하세요.
15. UBEC 출력 (+6V)을 Arduino Nano의 Vin 핀에 연결하세요 (선택 사항, Arduino는 전화로도 전원을 공급받을 수 있음), UBEC GND는 Arduino GND 핀 (Vin 옆)에 연결하세요.
16. UBEC 출력 (+6V)을 스티어링 서보, 앞쪽 LED 램프, 뒤쪽 빨간색 LED에 전압 분배기를 통해 +ve 단자에 연결하세요.
17. 스티어링 서보의 접지 케이블을 Arduino의 GND 핀에 연결하세요.
18. 스로틀 서보의 PWM 케이블 (속도 컨트롤러에서)을 Arduino Nano의 A0 핀 또는 PCB 브레이크아웃에 연결하세요.
19. 스티어링 서보의 PWM 케이블을 Arduino Nano의 A1 핀 또는 PCB 브레이크아웃에 연결하세요.
    **팁:** LED 배선에 대해 통합 접지 루프를 만들었다면, 접지 루프 케이블을 Arduino GND 핀 중 하나에 연결하세요. Arduino Nano에는 GND 핀이 세 개가 있어요. 접지 루프를 만들지 않았다면, 모든 LED, 스티어링 서보, 센서, Arduino Nano, 속도 컨트롤러의 UBEC가 같은 접지를 공유하도록 적절하게 배선하고 연결하세요.
21. 배터리 팩을 앞쪽에 연결하고 벨크로나 장착 테이프를 사용해 고정하세요. 배터리를 앞쪽에 두면 재충전이 쉽게 가능해요. 또한 스마트폰을 위에 장착할 때 로봇의 무게 균형을 맞추는 데에도 도움이 돼요.
22. 앞면과 뒷면의 ```electronics_covers```를 장착하세요. 뒤쪽 ```electronics_cover```의 틈에서 USB OTG 케이블을 꺼내어 안드로이드 스마트폰에 연결하세요.
<p float="left">
      <img src="/docs/images/add_covers_1.png" width="32%" />
      <img src="/docs/images/add_covers_2.JPG" width="32%" />
    </p>


## 다음

[Arduino 펌웨어](../../firmware/README.ko-KR.md)를 플래시하세요!
