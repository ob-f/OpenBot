# OpenBot: RC-Truck Body

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Wir haben einen Roboterkörper für weit verbreitete RC-Spielzeug-Trucks/Buggys im Maßstab 1:16 entworfen (wie [diesen](https://www.amazon.de/dp/B00M3J7DJW) auf Amazon).

![RC-Truck-Banner](/docs/images/rc-truck-banner.jpg)

Wir haben auch einen generischen [Körper](/body/) für einen einfachen Radroboter entworfen, der auf kostengünstiger, leicht verfügbarer Hobby-Hardware basiert. Bauanleitungen für den regulären OpenBot finden Sie [hier](/body/README.md).

## Chassis

Das Chassis des OpenBot RC-Trucks besteht aus zwei Hauptkomponenten: (a) Einem RC-Spielzeug-Truck im Maßstab 1:16 Ihrer Wahl und (b) einer Anzahl von speziell entworfenen Teilen, die wir bereitstellen und die 3D-gedruckt werden können.

### RC-Spielzeug-Truck im Maßstab 1:16

Um Ihren eigenen OpenBot RC-Truck zu bauen, benötigen Sie einen RC-Spielzeug-Truck/Buggy im Maßstab 1:16. Wir bieten Amazon-Links zu kompatiblen RC-Spielzeug-Trucks für Deutschland ([EU](https://www.amazon.de/dp/B00M3J7DJW)), ([EU](https://www.amazon.de/dp/B088FGVYNW)) und die Vereinigten Staaten ([US](https://www.amazon.com/gp/product/B09C8XMPQ9)) mit schneller Lieferung an. Eine Reihe ähnlicher Spielzeug-Trucks im Maßstab 1:16 finden Sie auch bei anderen Online-Händlern wie ebay, Alibaba oder AliExpress, oft zu einem günstigeren Preis, aber mit langsamerer Versandgeschwindigkeit.

Unabhängig vom Händler und der Version des RC-Spielzeug-Trucks, die Sie für Ihren Bau wählen, stellen Sie sicher, dass es sich tatsächlich um einen RC-Truck im Maßstab 1:16 handelt. Dies ist wichtig, da die von uns bereitgestellten 3D-gedruckten Teile derzeit nur für Trucks im Maßstab 1:16 mit einigen geringfügigen Anpassungen (mehr dazu später) ausgelegt sind. Einige Beispiele für kompatible RC-Spielzeug-Trucks/Buggys im Maßstab 1:16 sind unten gezeigt.

<p float="left">
  <a href="https://www.amazon.de/dp/B00M3J7DJW" target="_blank"> <img src="/docs/images/rc_toy_1.jpg" width="34%" /> &nbsp
  </a>
  <a href="https://www.amazon.com/gp/product/B09C8XMPQ9" target="_blank"> <img src="/docs/images/rc_toy_2.jpg" width="27%" /> &nbsp &nbsp &nbsp &nbsp
  </a>
  <a href="https://www.amazon.de/dp/B088FGVYNW" target="_blank"> <img src="/docs/images/rc_toy_3.jpg" width="27%" />
  </a>
</p>

### 3D-Druck

Sie müssen die folgenden Teile drucken, um Ihren OpenBot RC-Truck zu bauen.

1) ```main_frame``` ([STL](cad/rc_truck_body/main_frame.stl), [STEP](cad/rc_truck_body/main_frame.step))
2) ```side_cover``` \[x2\] ([STL](cad/rc_truck_body/side_cover.stl), [STEP](cad/rc_truck_body/side_cover.step))
3) ```phone_mount_bottom``` ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
4) ```phone_mount_top``` ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

Beachten Sie, dass \[xN\] die Anzahl der Kopien (d. h. N) angibt, die Sie von einem bestimmten Teil drucken müssen (wo zutreffend).

Die folgenden Teile sind optional (aber empfohlen), um Ihren OpenBot RC-Truck kompakter und ästhetisch ansprechender zu machen.

5) ```camera_elevator``` ([STL](cad/rc_truck_body/camera_elevator.stl), [STEP](cad/rc_truck_body/camera_elevator.step))
6) ```electronics_cover``` \[x2\] ([STL](cad/rc_truck_body/electronics_cover.stl), [STEP](cad/rc_truck_body/electronics_cover.step))
7) ```spacer``` \[x4\] ([STL](cad/rc_truck_body/spacer.stl), [STEP](cad/rc_truck_body/spacer.step))
8) ```front_light_spacer``` \[x2\] ([STL](cad/rc_truck_body/front_light_spacer.stl), [STEP](cad/rc_truck_body/front_light_spacer.step))

Für alle oben genannten Teile muss Ihre Bauplatte mindestens 260mmx220mm groß sein, was der Druckgröße des ```main_frame``` entspricht.

Da viele gängige 3D-Drucker ein kleineres Bauvolumen haben (normalerweise 220mmx220mm), gibt es zwei weitere Optionen, die funktionieren können.
Die erste Option besteht darin, das ```main_frame``` in einem Winkel von 45 Grad mit zusätzlichem Stützmaterial zu drucken.
Die zweite Option erfordert die Modifikation des ursprünglichen ```main_frame```-Teils. Wir empfehlen die Verwendung von [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) für solche CAD-Modifikationen (Fusion 360 bietet eine kostenlose 1-Jahres-Akademie-Lizenz an).
Für diese Option stellen wir die [STEP](/body/cad/rc_truck_body/main_frame.step)-Datei zur Verfügung, die Sie in zwei/drei kleinere Teile schneiden können.
Die resultierenden Unterteile passen dann auf eine Standard-Bauplatte (d. h. 220mmx220mm) und können nach dem Drucken zusammengefügt werden.
In Zukunft werden wir möglicherweise auch eine solche modulare Version des ```main_frame``` hier veröffentlichen. Alle anderen Teile erfordern eine Mindestbauplatte von 220mmx60mm.

Auf einem Ultimaker S5 haben wir mit den folgenden Einstellungen gute Ergebnisse erzielt:

- Schichthöhe: 0,2mm
- Wandstärke: 1,5mm
- Fülldichte: 20%
- Füllmuster: Gitter
- Druckgeschwindigkeit: 80 mm/s
- keine Unterstützung

Wir konnten das Chassis mit PLA, CPE und ABS drucken. Nach unserer Erfahrung wurde der Druck nicht sehr stark von den Druckeinstellungen beeinflusst. Wenn Sie jedoch die Geduld haben, wird ein langsamerer Druck mit kleinerer Schichthöhe den Druck verbessern. Auch das Hinzufügen einer Stützstruktur kann den Druck verbessern, erfordert jedoch zusätzliche Arbeit beim Entfernen danach.

Bevor Sie mit dem Bau fortfahren, müssen Sie möglicherweise den 3D-Druck reinigen. Mit den oben genannten Einstellungen war jedoch während unseres Bauprozesses keine Feil- oder Reinigungsarbeit erforderlich. Wenn möglich, empfehlen wir die Verwendung einer Kombination aus zwei verschiedenen Farben (zum Beispiel grün/schwarz oder rot/schwarz) für den Druck verschiedener Teile desselben OpenBot RC-Trucks, wie unten gezeigt.

**Tipp:** Klicken Sie auf die Bilder, um sie in voller Auflösung in einem neuen Tab zu öffnen.

<p float="left">
  <img src="/docs/images/3d_print_rc_1.png" width="32%" />
  <img src="/docs/images/3d_print_rc_2.png" width="32%" /> 
  <img src="/docs/images/3d_print_rc_3.png" width="32%" />
</p>

## Zusammenbau

Während es möglich ist, Ihren OpenBot RC-Truck mit einem DIY-Ansatz ähnlich dem regulären OpenBot zu bauen (siehe DIY-Bauteile und Anleitungen für OpenBot [hier](/body/README.md)), empfehlen wir die Verwendung der OpenBot [benutzerdefinierten Leiterplatte](/body/pcb) für den Bau und Zusammenbau des OpenBot RC-Trucks. Diese Option wird empfohlen, wenn Sie einen saubereren Aufbau wünschen oder mehrere OpenBot RC-Trucks bauen möchten. Ein zusätzlicher Vorteil der Verwendung unserer [benutzerdefinierten Leiterplatte](/body/pcb) besteht darin, dass Sie dieselben Komponenten verwenden können, um verschiedene OpenBot-Körper zu bauen und zwischen ihnen zu wechseln.

### Stückliste

Der OpenBot RC-Truck basiert hauptsächlich auf leicht verfügbaren Hobby-Elektronikkomponenten. Wir bieten Amazon-Links für Deutschland (EU) und die Vereinigten Staaten (US) mit schneller Lieferung an. Wenn Sie die Geduld haben, etwas länger zu warten, können Sie die Komponenten auch viel günstiger bei AliExpress (AE) erhalten. Sie benötigen die folgenden Komponenten.

#### Erforderliche Komponenten

- 1x RC-Spielzeug-Truck/Buggy ([EU](https://www.amazon.de/dp/B00M3J7DJW), [EU](https://www.amazon.de/dp/B088FGVYNW), [US](https://www.amazon.com/gp/product/B09C8XMPQ9))
- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 1x OpenBot [benutzerdefinierte Leiterplatte](/body/pcb)
- 1x USB OTG-Kabel ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x Feder oder Gummiband ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 6x M3x25 Schraube ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 6x M3 Mutter ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- Dupont-Kabel ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Optionale Komponenten

- 1x Ultraschallsensor ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 2x Ein/Aus-Schalter ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 4x Orange LED 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 4x Rote LED 5mm ([EU](https://www.amazon.de/dp/B083HN3CLY), [US](https://www.amazon.com/dp/B077X95F7C), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 2x Weiße LED-Lampen ([EU](https://www.amazon.de/-/en/gp/product/B06XTQSZDX), [US](https://www.amazon.com/gp/product/B01N2UPAD8), [AE](https://de.aliexpress.com/item/1005002991235830.html))
- Variabler Widerstand für LEDs ([EU](https://www.amazon.de/gp/product/B081TXJJGV), [US](https://www.amazon.com/dp/B0711MB4TL), [AE](https://de.aliexpress.com/item/1005003610664176.html))

### Bauanleitung

**Tipp:** Klicken Sie auf die Bilder, um sie in voller Auflösung in einem neuen Tab zu öffnen.

1. Zerlegen Sie den RC-Spielzeug-LKW. Entfernen Sie die obere Abdeckung und schrauben Sie die vier Befestigungsstifte von der Basis ab, wie in den Abbildungen unten gezeigt. Bewahren Sie alle vier Befestigungsstifte und deren Schrauben sicher auf, da Sie diese verwenden werden, um den ```main_frame``` nach Abschluss der Verkabelung am RC-LKW-Körper zu befestigen. Alle kompatiblen RC-Spielzeug-LKWs sind mit zwei Motoren ausgestattet: einem für den Antrieb und einem für die Lenkung, einem Geschwindigkeitsregler (mit integriertem 5-7V UBEC) für den Antriebsmotor und einem 2S 7.4V LiPo-Akkupack. Demontieren und entfernen Sie das Akkupack von der Basis des LKWs und laden Sie es mit dem Ladegerät auf, das mit dem LKW geliefert wurde. Legen Sie die Drahtverbinder für beide Motoren sowie den UBEC-Ausgang vom Geschwindigkeitsregler frei/lockern Sie sie. In unserem Fall betrug der UBEC-Ausgang 6V.
    <p float="left">
      <img src="/docs/images/rc_truck_disassembly_1.JPG" width="32%" />
      <img src="/docs/images/rc_truck_disassembly_2.JPG" width="32%" /> 
      <img src="/docs/images/rc_truck_disassembly_3.JPG" width="32%" />
    </p>
2. Beachten Sie, dass die beiden Dimensionen d1 und d2 (wie unten gezeigt) auf dem ```main_frame``` vom Modell des verwendeten RC-Spielzeug-LKWs abhängen. Wir haben unser ```main_frame```-Teil für [dieses](https://www.amazon.de/dp/B00M3J7DJW) RC-Spielzeug-LKW-Modell entworfen. Abhängig davon, welchen (1:16 Maßstab) LKW Sie verwenden, müssen Sie diese Dimensionen möglicherweise leicht anpassen, indem Sie die ```main_frame``` [STEP](/body/cad/rc_truck_body/main_frame.step) Datei verwenden. Wir empfehlen die Verwendung von [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) für solche CAD-Änderungen (Fusion 360 bietet eine kostenlose 1-Jahres-Akademische Lizenz an). Beachten Sie auch, dass der kleine Keil/Dreieck auf dem ```main_frame``` die Vorwärtsrichtung anzeigt.
    <p float="left">
      <img src="/docs/images/main-frame-dimensions.png" width="32%" />
      <img src="/docs/images/main-frame-direction.png" width="32%" />
    </p>   
3. (Optional) Installieren Sie den EIN/AUS-Schalter zur Stromversorgung des Roboters. Sie können dies einfach tun, indem Sie das positive Kabel, das vom Geschwindigkeitsregler zur Batterie führt, durchschneiden und den Schalter zwischen die beiden geteilten Teile dieses Kabels löten. Stellen Sie sicher, dass die Schalteranschlüsse mit Schrumpfschlauch oder Isolierband isoliert sind und das Stromkabel lang genug ist, damit der Schalter nach der Montage durch die rechteckige Öffnung auf der Rückseite des ```main_frame``` passt (siehe Abbildung unten).
    <p float="left">
      <img src="/docs/images/main-frame-switch.png" width="32%" />
      <img src="/docs/images/switch-power.jpg" width="32%" />
    </p>
4. (Optional) Installieren Sie den Ultraschallsensor durch das vordere Gitter des ```main_frame```. Sie können Heißkleber verwenden, um ihn bei Bedarf an Ort und Stelle zu halten. Drücken Sie den Stecker vorsichtig in eine gerade Position, bevor Sie ihn an Ort und Stelle setzen. Dies erleichtert den Zugang zum Stecker nach der Montage. Führen Sie die Dupont-Kabel vom Ultraschallstecker bis zur rechteckigen Öffnung auf der Rückseite des ```main_frame```.
    <p float="left">
      <img src="/docs/images/install-ultrasonic-1.png" width="32%" />
      <img src="/docs/images/ultrasonic-sensor.jpg" width="32%" />
      <img src="/docs/images/install-ultrasonic-2.png" width="32%" />
    </p>
5. (Optional) Installieren Sie die orangefarbenen LEDs für die Blinker sowohl vorne als auch hinten am ```main_frame```. Sie können Heißkleber verwenden, um sie bei Bedarf an Ort und Stelle zu halten. Für jede Seite, d.h. links und rechts, müssen Sie die vorderen und hinteren LEDs parallel schalten. Dazu verbinden Sie einfach ihre positiven und negativen Anschlüsse miteinander. Ähnlich wie beim Ultraschallsensorkabel führen Sie die positiven und negativen Dupont-Kabel von beiden linken und rechten Blinkern bis zur rechteckigen Öffnung auf der Rückseite des ```main_frame```, wo sie mit ihren jeweiligen Blinkeranschlüssen (sowohl +ve als auch -ve) auf der Platine verbunden werden.
    <p float="left">
      <img src="/docs/images/insert-leds-orange-1.png" width="32%" />
      <img src="/docs/images/orange-led.jpg" width="32%" />
      <img src="/docs/images/insert-leds-orange-2.png" width="32%" />
    </p>
**Tipp:** Um Unordnung und potenzielle Erdungsfehler während der Verkabelung zu vermeiden, wird empfohlen, eine einheitliche Erdungsschleife für die negativen Anschlüsse aller LEDs zu bilden. Dies bedeutet einfach, ein Kabel unter dem ```main_frame``` zu verlegen, das alle negativen Anschlüsse der LEDs verbindet. Diese Erdungsschleife kann dann mit einem einzigen Dupont-Kabel mit dem Erdungsstift des Arduino Nano verbunden werden, das zur rechteckigen Öffnung auf der Rückseite des ```main_frame``` geführt wird.

6. (Optional) Installieren Sie die vorderen LED-Lampen. Sie können Heißkleber verwenden, um die Basis an Ort und Stelle zu halten, und die Lampe durch die vordere Öffnung auf jeder Seite in ihre jeweilige Basis schrauben. Verbinden Sie beide vorderen LED-Lampen parallel, indem Sie ihre positiven und negativen Anschlüsse jeweils miteinander verbinden. Da diese Lampen mit 6V betrieben werden, können Sie sie direkt an den UBEC-Ausgang anschließen, indem Sie ihre positiven Anschlüsse verwenden. Verbinden Sie die negativen Anschlüsse mit der Erdungsschleife (siehe Tipp oben). Der interne Widerstand dieser LEDs ist ziemlich hoch, sodass kein externer Widerstand erforderlich ist. Nach der Installation der LED-Lampen setzen Sie die beiden ```front_light_spacers``` auf jeder Seite ein und kleben sie mit Heißkleber fest, um die LEDs an Ort und Stelle zu fixieren.
    <p float="left">
      <img src="/docs/images/insert-lamps-1.png" width="32%" />
      <img src="/docs/images/led-lamp-wiring.jpg" width="32%" />
      <img src="/docs/images/add_front_light_spacer.png" width="32%" />
    </p>
7. (Optional) Installieren Sie die roten LEDs für die Rücklichter. Sie können Heißkleber verwenden, um sie bei Bedarf an Ort und Stelle zu halten. Verbinden Sie alle vier roten LEDs parallel; d.h. verbinden Sie ihre positiven und negativen Anschlüsse jeweils miteinander. Die negativen Anschlüsse gehen zur Erdung, während die positiven Anschlüsse kollektiv über einen geeigneten Spannungsteiler mit dem UBEC-Ausgang verbunden werden (siehe den nächsten Schritt für Details zur Konstruktion des Spannungsteilers).
    <p float="left">
      <img src="/docs/images/insert-leds-red.png" width="32%" />
      <img src="/docs/images/red-led.jpg" width="32%" />
    </p>
8. (Optional) Installieren Sie den Spannungsteiler für die hinteren roten LEDs. Die meisten farbigen LEDs (z.B. Rot, Orange, Gelb usw.) arbeiten mit 2-3V und nicht mit den traditionellen 5V, die die normale Betriebsspannung des Arduino Nano sind. Daher ist ein Spannungsteiler erforderlich, um diese LEDs sicher zu betreiben. Für Blinker haben wir bereits einen eingebauten Spannungsteiler in unserer benutzerdefinierten Platine. Sie müssen also nichts tun, um die Blinker-LEDs (d.h. orange) zu verwenden. Wenn Sie jedoch auch Rücklichter, d.h. rote LEDs, hinzufügen möchten, ist ein externer Spannungsteiler erforderlich. Wir empfehlen die Verwendung eines variablen Widerstands von 10kΩ oder höher zur Herstellung Ihres Spannungsteilers. Basierend auf Ihrer UBEC-Ausgangsspannung (6V in unserem Fall) müssen Sie einen Spannungsteiler mit 2-3V Ausgang einrichten. Dies kann erreicht werden, indem der UBEC-Ausgang an den äußeren Enden des Widerstands angelegt wird und die Schraube oben gedreht wird, während die Ausgangsspannung mit einem digitalen Multimeter zwischen der Erdung und dem mittleren Anschluss überwacht wird (siehe Abbildung unten). Sobald die Ausgangsspannung des variablen Widerstands, d.h. des Spannungsteilers, auf den geeigneten Bereich von 2-3V eingestellt ist, fixieren Sie die Schraube mit etwas Heißkleber und befestigen Sie sie in einer bequemen Position unter dem ```main_frame```.
    <p float="left">
      <img src="/docs/images/variable-resistor.jpg" width="32%" />
      <img src="/docs/images/voltage-divider-animation.png" width="32%" />
    </p>
9. (Optional) Sie können auch einen einzelnen oder zwei separate EIN/AUS-Schalter verwenden, um die vorderen und hinteren LEDs ein- und auszuschalten. Befolgen Sie die Anweisungen in Schritt 3, um einen Schalter (oder mehrere Schalter) für diesen Zweck zu installieren.
10. Jetzt sind Sie fast fertig mit der Verkabelung des Roboters. Nehmen Sie sich an diesem Punkt etwas Zeit, um sicherzustellen, dass alle Kabel und Verbindungen unter dem ```main_frame``` korrekt und gut isoliert sind, entweder mit Schrumpfschlauch oder Isolierband. Verwenden Sie Heißkleber, um lose Kabel an Ort und Stelle zu halten, damit sie nach der Montage nicht mit den Rädern oder anderen beweglichen Teilen des Roboters in Kontakt kommen. Stellen Sie sicher, dass alle Kabel von Motoren, Geschwindigkeitsregler-UBEC, LEDs und Ultraschallsensor frei durch die rechteckige Öffnung auf der Rückseite des ```main_frame``` geführt werden können.
11. Befestigen Sie den ```phone_mount_bottom``` am ```main_frame``` mit zwei M3x25 Schrauben und Muttern. Optional können Sie einen oder mehrere ```camera_elevators``` dazwischen einfügen, wenn Sie die vertikale Höhe Ihrer Handyhalterung anpassen möchten. Wenn Sie einen ```camera_elevator``` verwenden, benötigen Sie M3x35 oder längere Schrauben, um die Handyhalterung am ```main_frame``` zu befestigen.
    <p float="left">
      <img src="/docs/images/add_phone_mount_bottom.png" width="32%" />
      <img src="/docs/images/add_phone_mount_bottom_elevator.png" width="32%" /> 
    </p>
10. Setzen Sie den ```phone_mount_top``` ein und installieren Sie die Feder oder das Gummiband.
    <p float="left">
      <img src="/docs/images/add_phone_mount_top.png" width="32%" />
    </p>
11. Setzen Sie die beiden ```side_covers``` in ihre jeweiligen Schlitze ein.
    <p float="left">
      <img src="/docs/images/add_side_covers.png" width="32%" />
      <img src="/docs/images/add_side_covers_2.png" width="32%" />
    </p>    
12. Befestigen Sie den ```main_frame``` am RC-LKW-Körper mit den vier Befestigungsstiften und deren Schrauben. Stellen Sie sicher, dass alle Kabelverbinder und der Netzschalter für den Roboter durch die rechteckige Öffnung auf der Rückseite des ```main_frame``` für PCB-Verbindungen zugänglich sind. Ziehen Sie den Batterieanschluss aus der dreieckigen Öffnung an der Vorderseite des ```main_frame```.
    <p float="left">
      <img src="/docs/images/add_main_frame_1.JPG" width="32%" />
      <img src="/docs/images/add_main_frame_2.png" width="32%" />
      <img src="/docs/images/add_main_frame_3.JPG" width="32%" />
    </p>
12. Montieren Sie die Platine mit vier M3x25 Schrauben und Muttern mit vier ```spacers``` dazwischen auf der Rückseite des ```main_frame```. Montieren Sie den Arduino Nano auf der Platine und schließen Sie das USB-OTG-Kabel an den USB-Anschluss des Arduino Nano an.
    <p float="left">
      <img src="/docs/images/pcb_assembly.JPG" width="32%" />
    </p>
13. Verbinden Sie die Kabel des Ultraschallsensors mit dem Anschluss "sonar" auf der Platine. Stellen Sie sicher, dass die +ve/-ve Polarität und die Datenleitungen zwischen dem Sensor und den PCB-Anschlüssen korrekt übereinstimmen.
14. Verbinden Sie die Kabel der linken und rechten Blinker-LEDs mit ihren jeweiligen Blinkeranschlüssen auf der Platine. Stellen Sie die korrekte Polarität der +ve und -ve LED-Anschlüsse sicher.
15. Verbinden Sie den UBEC-Ausgang (+6V) mit dem Vin-Pin des Arduino Nano (optional, Arduino kann auch über das Telefon betrieben werden) und den UBEC-GND mit dem GND-Pin des Arduino (neben Vin).
16. Verbinden Sie den UBEC-Ausgang (+6V) mit den +ve Anschlüssen des Lenkservos, der vorderen LED-Lampen und der hinteren roten LEDs über den Spannungsteiler.
17. Verbinden Sie das Erdungskabel des Lenkservos ebenfalls mit dem GND-Pin des Arduino.
18. Verbinden Sie das PWM-Kabel des Antriebsservos (vom Geschwindigkeitsregler) mit Pin A0 auf dem Arduino Nano oder PCB-Breakout.
19. Verbinden Sie das PWM-Kabel des Lenkservos mit Pin A1 auf dem Arduino Nano oder PCB-Breakout.
**Tipp:** Wenn Sie eine einheitliche Erdungsschleife für die LED-Verkabelung erstellt haben, verbinden Sie das Erdungsschleifenkabel ebenfalls mit einem der GND-Pins des Arduino. Der Arduino Nano hat drei GND-Pins zur Verfügung. Wenn Sie keine Erdungsschleife erstellt haben, stellen Sie sicher, dass alle LEDs, der Lenkservo, die Sensoren, der Arduino Nano und das UBEC des Geschwindigkeitsreglers die gleiche Erdung mit geeigneter Verkabelung und Verbindungen teilen.
21. Schließen Sie das Akkupack vorne an und halten Sie es mit etwas Klettband oder Montageband an Ort und Stelle. Das Akku vorne zu haben, macht es leicht zugänglich zum Aufladen. Diese Platzierung hilft auch, das Gewicht des Roboters auszugleichen, wenn ein Smartphone oben montiert ist.
22. Setzen Sie die vorderen und hinteren ```electronics_covers``` auf. Ziehen Sie das USB-OTG-Kabel durch die Lücke der hinteren ```electronics_cover``` heraus, um es mit einem Android-Smartphone zu verbinden.
<p float="left">
      <img src="/docs/images/add_covers_1.png" width="32%" />
      <img src="/docs/images/add_covers_2.JPG" width="32%" />
    </p>

## Nächstes

Flashen Sie die [Arduino Firmware](../../firmware/README.md)
