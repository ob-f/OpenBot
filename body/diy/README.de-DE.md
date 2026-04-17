# OpenBot DIY

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Wir haben ein Chassis für einen fahrenden Roboter entworfen, der auf kostengünstiger, leicht verfügbarer Hobby-Hardware basiert. Unten finden Sie Anweisungen zum Bau Ihres eigenen Roboters. Wenn Sie weitere Fragen oder Bedenken haben, können Sie uns gerne kontaktieren. Viel Spaß beim Roboterbau!

## Chassis

### 3D-Druck

Sie müssen die folgenden Teile drucken und zusammenbauen, um Ihren OpenBot zu bauen.

#### Roboterkörper

Es gibt mehrere Optionen für den Roboterkörper, abhängig von Ihren Bedürfnissen und den Fähigkeiten Ihres 3D-Druckers. Wir ermutigen Sie, Ihren eigenen zu entwerfen und zu bauen, aber hier sind einige Optionen als Ausgangspunkt:

- [Regulärer Körper](cad/regular_body/README.md): Dies ist der Standardkörper, den wir entworfen haben; er benötigt eine Bauplatte mit mindestens 240mmx150mm.
- [Schlanker Körper](cad/slim_body/README.md): Da viele gängige 3D-Drucker ein kleineres Bauvolumen haben, haben wir auch eine kleinere Version ohne Stoßfänger entworfen, die auf eine 220mmx220mm Bauplatte bei 45 Grad passt.
- [Klebbarer Körper](cad/glue_body/README.md): Für den Druck auf 3D-Druckern mit noch kleineren Bauvolumen gibt es auch einen modularen Körper, der von @sloretz entworfen wurde und aus mehreren Teilen besteht, die zusammengeklebt werden müssen; er passt auf eine 150mmx140mm Bauplatte.
- [Blockiger Körper](cad/block_body/README.md): Dieser Körper, entworfen von @Christos-Ps, bietet mehrere Varianten mit Optionen für zusätzlichen Platz im Inneren der Hülle und einer lego-kompatiblen Oberseite, während er eine kleine Stellfläche beibehält, die nur 221mmx150mm für den Druck benötigt.

#### Handyhalterung

Zusätzlich müssen Sie eine Handyhalterung drucken, die am Roboterkörper befestigt wird.

- phone_mount_bottom ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
- phone_mount_top ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

#### Reinigung

Bevor Sie mit dem Bau fortfahren, müssen Sie möglicherweise den 3D-Druck reinigen.
<p float="left">
  <img src="../../docs/images/clean_3d_print_1.jpg" width="32%" />
  <img src="../../docs/images/clean_3d_print_2.jpg" width="32%" /> 
  <img src="../../docs/images/clean_3d_print_3.jpg" width="32%" />
</p>

### Alternativen

Wenn Sie keinen Zugang zu einem 3D-Drucker haben, gibt es mehrere Arduino-Roboter-Autokits, die Sie als Ausgangspunkt verwenden können. Diese Kits enthalten ein Chassis, Motoren und Zubehör. Wir empfehlen, ein einfaches Kit zu kaufen, da Sie viele der Elektronik- und Sensoren der teureren Kits nicht benötigen. Hier sind einige Optionen:

- Perseids DIY Robot Smart Car Chassis Kit ([EU](https://www.amazon.de/dp/B07DNXBNHY), [US](https://www.amazon.com/dp/B07DNXBFQN))
- SZDoit 4WD Smart Metal Robot Car Chassis Kit ([US](https://www.amazon.com/dp/B083K4RKBP), [AE](https://www.aliexpress.com/item/33048227237.html))
- Joy-it Robot Car Kit 01 ([EU](https://www.amazon.de/dp/B073ZGJF28))
- Smart Car Kit 4WD Smart Robot Car Chassis Kit ([AE](https://www.aliexpress.com/item/4001238626191.html))

Sie benötigen auch eine Handyhalterung. Hier sind einige Optionen:

- Handyhalterung ([EU](https://www.amazon.de/dp/B06XDYJNSR), [US](https://www.amazon.com/dp/B09CY8MC2R))

Sie können auch kreativ werden und Ihr eigenes OpenBot-Chassis und Handyhalterung aus einem Material Ihrer Wahl (z.B. Holz, Karton, Styropor, etc.) bauen. Wenn Sie dies tun, posten Sie bitte einige Bilder im [Slack-Kanal](https://github.com/ob-f/OpenBot#contact), damit andere Ihre Kreativität bewundern können. Hier ist ein Beispiel von [@custom-build-robots](https://custom-build-robots.com/roboter/openbot-dein-smartphone-steuert-ein-roboter-auto-chassis-bauen/13636):

<p float="left">
  <img src="../../docs/images/chassis_cardboard_1.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_2.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_3.jpg" width="32%" />
</p>

## Zusammenbau

Es gibt zwei verschiedene Optionen für den Zusammenbau des Roboters, DIY und PCB. Der DIY-Ansatz basiert auf dem beliebten L298N-Motortreiber und wird für Hobbyisten mit etwas Elektronik-Erfahrung empfohlen. Er erfordert eine beträchtliche Menge an Verkabelung, insbesondere wenn alle Sensoren und LEDs installiert sind. Allerdings sind alle Komponenten in den meisten Ländern leicht verfügbar und insbesondere für Einzelbauten oder nur zum Ausprobieren des Projekts wird die DIY-Option empfohlen. Um die Verkabelung zu reduzieren und den Zusammenbau zu erleichtern, haben wir auch eine [benutzerdefinierte Leiterplatte (PCB)](pcb) entwickelt. Diese wird empfohlen, wenn Sie einen saubereren Aufbau wünschen oder mehrere OpenBots bauen möchten.

### Stückliste

Unser Roboterkörper basiert auf leicht verfügbarer Hobbyelektronik. Wir bieten Links für Deutschland (EU) und die Vereinigten Staaten (US) mit schneller Lieferung. Wenn Sie die Geduld haben, etwas länger zu warten, können Sie die Komponenten auch viel günstiger bei AliExpress (AE) bekommen. Sie benötigen die folgenden Komponenten.

#### Erforderliche Komponenten

- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 4x TT-Motoren mit Reifen ([EU](https://www.conrad.de/de/p/joy-it-com-motor01-getriebemotor-gelb-schwarz-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspbe-1573543.html), [US](https://www.amazon.com/dp/B081YQM55P), [AE](https://www.aliexpress.com/item/4000126948489.html))
- 3x 18650 Batterie ([EU](https://www.conrad.de/de/p/conrad-energy-18650-usb-spezial-akku-18650-li-ion-3-7-v-1400-mah-1525536.html), [US](https://www.amazon.com/dp/B083K4XSKG), [AE](https://www.aliexpress.com/item/32352434845.html))
- 1x 18650 Batteriehalter ([EU](https://www.amazon.de/dp/B075V25QJ9), [US](https://www.amazon.com/dp/B07DWQYD7H), [AE](https://www.aliexpress.com/item/33037738446.html))
- 1x USB OTG-Kabel ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x Feder oder Gummiband ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 16x M3x25 Schraube ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 16x M3 Mutter ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- 6x M3x5 Schraube ([EU](https://www.amazon.de/dp/B01HBRG3W8), [US](https://www.amazon.com/dp/B07MBHMLL2), [AE](https://www.aliexpress.com/item/32892594230.html))
- Dupont-Kabel ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Optionale Komponenten

- 2 x Geschwindigkeitssensor ([EU](https://www.conrad.de/de/p/joy-it-sen-speed-erweiterungsmodul-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspberry-pi-pc-1646891.html), [US](https://www.amazon.com/dp/B081W2TY6Q), [AE](https://www.aliexpress.com/i/32850602744.html))
- 1x Ultraschallsensor ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 1x Ein/Aus-Schalter ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 2x Orange LED 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 1x OLED-Display ([EU](https://www.amazon.de/dp/B079H2C7WH), [US](https://www.amazon.com/dp/B085NHM5TC), [AE](https://www.aliexpress.com/item/4001268387467.html))

#### DIY-Komponenten (Option 1)

- 1x L298N Motortreiber ([EU](https://www.conrad.de/de/p/joy-it-motormodul-2-u-4-phasen-6-bis-12v-1573541.html), [US](https://www.amazon.com/dp/B085XSLKFQ), [AE](https://www.aliexpress.com/item/32994608743.html))
- (Optional) Widerstände (2x 150<span>&#8486;</span> für die LEDs und ein 20 k<span>&#8486;</span> und 10k<span>&#8486;</span> für den Spannungsteiler)
- (Kombination) 4x TT-Motoren & Reifen + 2x L298N + Dupont-Kabel ([US](https://www.amazon.com/dp/B07ZT619TD))
- (Kombination) 4x TT-Motoren & Reifen + Kabel + Schrauben ([US](https://www.amazon.com/dp/B07DRGTCTP))

#### PCB-Komponenten (Option 2)

- 1x [Benutzerdefinierte PCB](pcb)
- 5x Micro JST PH 2.0 Kabel ([EU](https://www.amazon.de/gp/product/B07449V33P), [US](https://www.amazon.com/dp/B09JZC28DP), [AE](https://www.aliexpress.com/item/32963304134.html))

### Bauanleitung

**Tipp:** Klicken Sie auf die Bilder, um sie in voller Auflösung in einem neuen Tab zu öffnen.

#### Option 1: DIY

<p float="left">
  <img src="../../docs/images/diy_parts.jpg" height="300" />
  <img src="../../docs/images/wiring_diagram.png" height="300" /> 
</p>

**Tipp:** Um die gesamte Verkabelung zu erleichtern, können Sie einen kleinen Stromverteiler für die 5V- und GND-Verbindungen bauen, indem Sie einen 6x2-Männchen-Header auf eine Lochrasterplatine löten. Verbinden Sie dann den Stromverteiler mit den 5V / GND des Motortreibers.

1. Löten Sie die Drähte an die Motoren und fügen Sie die Encoder-Scheiben zu den beiden vorderen Motoren hinzu, wenn Sie die Geschwindigkeitssensoren verwenden möchten.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. Stecken Sie die positiven und negativen Anschlüsse der beiden linken Motoren in OUT1 (+) und OUT2 (-) der L298N-Platine. Stecken Sie die positiven und negativen Anschlüsse der beiden rechten Motoren in OUT4 (+) und OUT3 (-) der L298N-Platine.
3. Befestigen Sie die Motoren mit acht M3x25 Schrauben und Muttern.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
4. Befestigen Sie die L298N mit vier M3x5 Schrauben.
5. (Optional) Installieren Sie den Ultraschallsensor und ersetzen Sie den abgewinkelten Stecker durch einen geraden (oder biegen Sie die Pins vorsichtig).
    <p float="left">
      <img src="../../docs/images/sonar_front.jpg" width="32%" />
      <img src="../../docs/images/sonar_back.jpg" width="32%" /> 
      <img src="../../docs/images/sonar_bend_pins.jpg" width="32%" />
    </p>
6. (Optional) Installieren Sie die orangefarbenen LEDs für die Blinker.
    <p float="left">
      <img src="../../docs/images/led_insert.jpg" width="32%" />
      <img src="../../docs/images/led_left.jpg" width="32%" /> 
      <img src="../../docs/images/led_right.jpg" width="32%" />
    </p>
7. Befestigen Sie den unteren Teil der Handyhalterung mit zwei M3x25 Schrauben und Muttern an der oberen Platte.
    <p float="left">
      <img src="../../docs/images/install_camera_mount_1.jpg" width="32%" />
      <img src="../../docs/images/install_camera_mount_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_camera_mount_3.jpg" width="32%" />
    </p>
8. Setzen Sie den oberen Teil der Handyhalterung ein und installieren Sie die Feder oder das Gummiband.
    <p float="left">
      <img src="../../docs/images/install_spring_1.jpg" width="32%" />
      <img src="../../docs/images/install_spring_2.jpg" width="32%" /> 
    </p>
9. Ersetzen Sie den abgewinkelten Stecker durch einen geraden (oder biegen Sie die Pins vorsichtig) und montieren Sie dann die Geschwindigkeitssensoren mit jeweils einer M3x5 Schraube.
    <p float="left">
      <img src="../../docs/images/install_speed_sensor_1.jpg" width="32%" />
      <img src="../../docs/images/install_speed_sensor_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_speed_sensor_3.jpg" width="32%" />
    </p>
10. Installieren Sie das Batteriefach (z.B. Klettverschluss).
    <p float="left">
      <img src="../../docs/images/install_battery_1.jpg" width="32%" />
      <img src="../../docs/images/install_battery_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_battery_3.jpg" width="32%" />
    </p>
11. (Optional) Setzen Sie den Ein-/Ausschalter ein und integrieren Sie ihn in den Stromkreis.
    1. Drücken Sie den Schalter in die entsprechende Öffnung, bis Sie ein Klicken hören.
    2. Löten Sie die roten Drähte (12V) des Batteriefachs und des Stromkabels jeweils an einen der Pins des Schalters. Verbinden Sie die schwarzen Drähte (GND) und bedecken Sie die Verbindung mit etwas Schrumpfschlauch.
    3. Befestigen Sie die Kabel mit etwas Klebeband.
    <p float="left">
      <img src="../../docs/images/install_switch_1.jpg" width="32%" />
      <img src="../../docs/images/install_switch_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_switch_3.jpg" width="32%" />
    </p>
12. (Optional) Befestigen Sie das OLED-Display.
13. Verbinden Sie die PWM-Eingänge des L298N mit den Pins D5, D6, D9 und D10 des Arduino.
14. Verbinden Sie die Geschwindigkeitssensoren und den Ultraschallsensor mit 5V und GND.
15. Verbinden Sie den Pin D0 der Geschwindigkeitssensoren mit den Pins D2 (links) und D3 (rechts) des Arduino.
16. Verbinden Sie die Pins Echo und Trigger des Ultraschallsensors mit den Pins D11 und D12 des Arduino.
17. (Optional) Verbinden Sie die LEDs mit den Pins D4 (links) und D7 (rechts) des Arduino und GND. Wir empfehlen, einen 150 Ohm Widerstand in Serie hinzuzufügen, um den Stromverbrauch zu begrenzen.
18. (Optional) Verbinden Sie den Spannungsteiler mit dem Pin A7 des Arduino. Er wird verwendet, um die Batteriespannung zu messen.
19. (Optional) Verbinden Sie das OLED-Display (SSD1306-Chip) über den I2C-Bus mit dem Arduino Nano.
    1. Verbinden Sie die VIN- und GND-Pins des Displays mit 5V und GND.
    2. Verbinden Sie den SCL-Pin des Displays mit dem A5-Pin.
    3. Verbinden Sie den SDA-Pin des Displays mit dem A4-Pin.
20. Verbinden Sie die Stromkabel mit +12V und GND des L298N.
21. Verbinden Sie das USB-Kabel mit dem Arduino und führen Sie es durch die obere Abdeckung.
22. Setzen Sie sechs M3-Muttern in die Bodenplatte ein und befestigen Sie die obere Abdeckung mit sechs M3x25 Schrauben.
23. Installieren Sie die Räder.

#### Option 2: Custom PCB

1. Löten Sie Drähte mit Micro JST PH 2.0 Steckern an die Motoren und fügen Sie die Encoder-Scheiben zu den beiden vorderen Motoren hinzu, wenn Sie die Geschwindigkeitssensoren verwenden möchten.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. Befestigen Sie die Motoren mit acht M3x25 Schrauben und Muttern.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
3. Verbinden Sie die beiden linken Motoren mit M3 und M4 und die beiden rechten Motoren mit M1 und M2.
    <p float="left">
      <img src="../../docs/images/connect_motors_pcb.jpg" width="32%" />
    </p>
4. Befestigen Sie die Platine mit vier M3x5 Schrauben und die Motoren mit acht M3x25 Schrauben und Muttern.
    <p float="left">
      <img src="../../docs/images/attach_pcb.jpg" width="32%" />
      <img src="../../docs/images/chassis_motors_pcb.jpg" width="32%" />
    </p>
5. Folgen Sie den Schritten 5-12 der DIY-Option.
6. Verbinden Sie den Ultraschallsensor (VCC/+, Trig, Echo, GND/-) mit dem 4-poligen Header, der mit *SONAR* auf der Platine beschriftet ist.
    <p float="left">
      <img src="../../docs/images/connect_sonar_sensor.jpg" width="32%" />
    </p>
7. Verbinden Sie die linken und rechten Blinker (orange LEDs) mit den 2-poligen Headern, die mit *SIGNAL_L* und *SIGNAL_R* auf der Platine beschriftet sind. Das längere Bein ist + und das kürzere -.
8. Verbinden Sie die linken und rechten Geschwindigkeitssensoren (VCC/+, GND/-, D0) mit den 3-poligen Headern, die mit *SPEED_L* und *SPEED_R* beschriftet sind.
9. (Optional) Verbinden Sie das OLED-Display (SSD1306-Chip) mit dem IO2-Header auf der Platine.
    1. Verbinden Sie die VIN- und GND-Pins des Displays mit 5V und GND.
    2. Verbinden Sie den SCL-Pin des Displays mit dem A5-Pin.
    3. Verbinden Sie den SDA-Pin des Displays mit dem A4-Pin.
10. Verbinden Sie die Stromkabel mit Vin (Micro JST PH 2.0 Stecker) der Platine.
11. Folgen Sie den Schritten 21-23 der DIY-Option.

## Weiter

Flashen Sie die [Arduino Firmware](../../firmware/README.md)
