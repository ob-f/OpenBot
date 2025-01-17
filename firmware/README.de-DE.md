# Firmware

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Wir verwenden eine Mikrocontroller-Einheit (MCU) als Brücke zwischen dem Roboterkörper und dem Smartphone. Wir stellen unsere [Firmware](openbot_nano/openbot_nano.ino) für den Arduino Nano mit einem ATmega328P-Mikrocontroller sowie für das ESP32-Entwicklungskit zur Verfügung.

## Funktionen

Die Hauptaufgabe der MCU besteht darin, die Low-Level-Steuerung des Fahrzeugs zu übernehmen und Messwerte von am Fahrzeug montierten Sensoren bereitzustellen. Die MCU empfängt die Fahrzeugsteuerungen und Indikatorsignale über die serielle Verbindung. Sie wandelt die Steuerungen in PWM-Signale für den Motorcontroller um und schaltet die LEDs entsprechend dem Indikatorsignal. Das Arduino-Programm verfolgt auch die Radumdrehungen, indem es die Unterbrechungen der optischen Sensoren an den linken und rechten Vorderrädern zählt. Es berechnet die Batteriespannung durch einen skalierten gleitenden Durchschnitt der Messungen an der Spannungsteiler-Schaltung. Es kann auch den Abstand zu Hindernissen vor dem Auto mit einem optionalen Ultraschallsensor messen. Diese Messungen werden über die serielle Verbindung an die Android-Anwendung zurückgesendet.

## Einrichtung

Zuerst müssen Sie Ihre Hardwarekonfiguration am Anfang des Codes einrichten. Wenn Sie den DIY-Bau (mit dem L298N-Motortreiber) durchgeführt haben, setzen Sie `OPENBOT DIY`.
Wenn Sie die benutzerdefinierte Leiterplatte verwendet haben, überprüfen Sie die Version und setzen Sie entweder `OPENBOT PCB_V1` oder `OPENBOT PCB_V2`. Wenn Sie ein OpenBot-Kit haben, setzen Sie `OPENBOT RTR_TT`. Wenn Sie einen RC-Truck nachgerüstet haben, setzen Sie `OPENBOT RC_CAR`. Wenn Sie die kleinere DIY-Version für Bildungszwecke verwenden, setzen Sie `OPENBOT LITE`. Wenn Sie das OpenBot Ready-to-Run-Kit mit 520-Motoren verwenden, setzen Sie `OPENBOT RTR_520`. Wenn Sie das Multi Terrain Vehicle gebaut haben, sollten Sie `OPENBOT MTV` setzen. Um das `OpenBot DIY` mit dem ESP32 zu betreiben, setzen Sie `OpenBot DIY_ESP32`.

## Bluetooth

Sie können den OpenBot auch über Bluetooth betreiben. Dazu können Sie Bluetooth aktivieren, indem Sie `BLUETOOTH 1` setzen (deaktivieren: 0). Damit Bluetooth funktioniert, benötigen Sie OpenBot mit ESP32-Boards wie `(RTR_520, MTV, DIY_ESP32)`.

## Konfiguration

Als nächstes müssen Sie konfigurieren, welche Funktionen Sie aktivieren möchten. Deaktivierte Funktionen werden nicht kompiliert, um Speicher zu sparen und den Code schneller zu machen. Wenn eine Flagge nicht definiert ist, wird die Funktion deaktiviert. Jedes Modell hat einige Standardeinstellungen, die Sie je nach Konfiguration ändern müssen.

- Aktivieren Sie den Spannungsteiler, indem Sie `HAS_VOLTAGE_DIVIDER 1` setzen (deaktivieren: 0). Wenn Sie einen Spannungsteiler haben, sollten Sie auch den `VOLTAGE_DIVIDER_FACTOR` angeben, der als (R1+R2)/R2 berechnet wird, `VOLTAGE_MIN`, die minimale Spannung zum Antrieb der Motoren, `VOLTAGE_LOW`, die minimale Batteriespannung, und `VOLTAGE_MAX`, die maximale Batteriespannung.
- Aktivieren Sie die Indikator-LEDs, indem Sie `HAS_INDICATORS 1` setzen (deaktivieren: 0).
- Aktivieren Sie die Geschwindigkeitsensoren vorne/hinten, indem Sie `HAS_SPEED_SENSORS_FRONT 1` / `HAS_SPEED_SENSORS_BACK 1` setzen (deaktivieren: 0).
- Aktivieren Sie den Ultraschallsensor, indem Sie `HAS_SONAR 1` setzen (deaktivieren: 0). Aktivieren Sie den Medianfilter für Sonarmessungen, indem Sie `USE_MEDIAN 1` setzen (deaktivieren: 0).
- Aktivieren Sie den Stoßsensor, der zur Erkennung von Kollisionen verwendet wird, indem Sie `HAS_BUMPER 1` setzen (deaktivieren: 0).
- Aktivieren Sie das OLED-Display, indem Sie `HAS_OLED 1` setzen (deaktivieren: 0).
- Aktivieren Sie die LEDs vorne/hinten/Status, indem Sie `HAS_LEDS_FRONT 1` / `HAS_LEDS_BACK 1` / `HAS_LEDS_STATUS 1` setzen (deaktivieren: 0).

### Abhängigkeiten

Wenn Sie die Geschwindigkeitsensoren oder den Ultraschallsensor aktiviert haben, müssen Sie die [PinChangeInterrupt](https://github.com/NicoHood/PinChangeInterrupt)-Bibliothek installieren. Der Arduino Nano hat nur zwei externe Interrupt-Pins (D2 und D3) und D3 ist auch einer von nur sechs Pins, die PWM unterstützen. Glücklicherweise hat er auch drei Port-Interrupts, die alle Pins auf dem Arduino abdecken. Diese Bibliothek analysiert diese Port-Interrupts und ermöglicht es, alle Pins des Arduino als Interrupts zu verwenden.

Wenn Sie das OLED aktiviert haben, müssen Sie die Bibliotheken [Adafruit_SSD1306](https://github.com/adafruit/Adafruit_SSD1306) und [Adafruit_GFX Library](https://github.com/adafruit/Adafruit-GFX-Library) installieren.

Sie können Bibliotheken installieren, indem Sie die folgenden Schritte ausführen:
1. Öffnen Sie den Bibliotheks-Manager: `Werkzeuge` :arrow_right: `Bibliotheken verwalten`
2. Geben Sie den Namen der Bibliothek in die Suchleiste ein.
3. Wählen Sie die neueste Version aus und klicken Sie auf Installieren. Wenn Sie die Bibliothek bereits installiert haben, wird dies angezeigt und Sie können sie möglicherweise aktualisieren.

<p float="left">
  <img src="../docs/images/manage_libraries.jpg" height="300" />
  <img src="../docs/images/install_library.jpg" height="300" /> 
</p>

### Chinesischer Klon Nano (z.B. US-Link)

Möglicherweise müssen Sie die [WCH340](http://www.wch.cn/product/CH340.html)-Treiber vom Chip-Hersteller herunterladen (Chinesisch):

- [Windows](http://www.wch.cn/downloads/CH341SER_EXE.html)
- [Linux](http://www.wch.cn/download/CH341SER_LINUX_ZIP.html)
- [Mac](http://www.wch.cn/download/CH341SER_MAC_ZIP.html)

### ESP32-Entwicklungskit

Um das ESP32-Board in Ihrer Arduino-IDE zu installieren, folgen Sie diesen Anweisungen:

1. Gehen Sie in Ihrer Arduino-IDE zu **Datei > Voreinstellungen**:
<p align="center">
  <img src="../docs/images/arduino-ide-open-preferences.png" width="300" alt="App GUI"/>
</p>

2. Geben Sie *https://dl.espressif.com/dl/package_esp32_index.json* in das Feld „*Zusätzliche Boardverwalter-URLs*“ ein, wie in der Abbildung unten gezeigt. Klicken Sie dann auf die Schaltfläche „OK“:
<p align="center">
  <img src="../docs/images/arduino_preferences.png" width="600" alt="App GUI"/>
</p>

**Hinweis:** Wenn Sie bereits die ESP8266-Boards-URL haben, können Sie die URLs durch ein Komma trennen, wie folgt:

    https://dl.espressif.com/dl/package_esp32_index.json, 
    http://arduino.esp8266.com/stable/package_esp8266com_index.json

3. Öffnen Sie den Boardverwalter. Gehen Sie zu **Werkzeuge > Board > Boardverwalter**:
<p align="center">
  <img src="../docs/images/arduino_boardsManager.png" width="800" alt="App GUI"/>
</p>

4. Suchen Sie nach ESP32 und drücken Sie die Installationsschaltfläche für „ESP32 by Espressif Systems“:
<p align="center">
  <img src="../docs/images/arduino_installing.png" width="600" alt="App GUI"/>
</p>

5. Sie sollten nun alles haben, um das ESP32-Board Ihres OpenBot erfolgreich mit der Arduino-Entwicklungsumgebung zu flashen.
<p align="center">
  <img src="../docs/images/arduino_ESP32-Board-add-on-in-Arduino-IDE-installed.png" width="600" alt="App GUI"/>
</p>

6. Um den OpenBot mit Ihrem neuen Code zu flashen, wählen Sie einfach **ESP32 Dev Module** im Menü **Werkzeuge > Board > ESP32 Arduino**. Beachten Sie, dass zusätzliche Inhalte sowie Fehlerbehebungen des ESP32-Flashvorgangs im folgenden [Link](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/) zu finden sind.

<p align="center">
  <img src="../docs/images/arduino_windows-select-board.png" width="600" alt="App GUI"/>
</p>

## Hochladen

### Einstellungen (Arduino Nano Setup)

- `Werkzeuge` :arrow_right: `Board` :arrow_right: `Arduino AVR Boards` :arrow_right: `Arduino Nano`
- `Werkzeuge` :arrow_right: `Prozessor` :arrow_right: `ATmega328P (Old Bootloader)`
- `Werkzeuge` :arrow_right: `Port` :arrow_right: `*Wählen Sie den USB-Port*`

:memo: HINWEIS: Derzeit kommen die meisten günstigen Arduino Nano-Boards mit dem *Old Bootloader*. Je nach Verkäufer können Sie jedoch auch eines mit dem neuen Bootloader erhalten. Wenn Sie die Firmware nicht hochladen können, müssen Sie möglicherweise den Prozessor auf *ATmega328P* ändern.

### Einstellungen (ESP32 Setup)

- `Werkzeuge` :arrow_right: `Board` :arrow_right: `ESP32 Arduino` :arrow_right: `ESP32 Dev Module`
- `Werkzeuge` :arrow_right: `Port` :arrow_right: `*Wählen Sie den USB-Port*`

### Hochladen der Firmware

Die Firmware kann nun über `Sketch` :arrow_right: `Hochladen` oder durch Drücken der Hochladetaste (rechter Pfeil) hochgeladen werden.
![Firmware Upload](../docs/images/firmware_upload.png)

### Testen

Dieser Abschnitt erklärt, wie alle Funktionen des Autos getestet werden können, nachdem die Firmware erfolgreich geflasht wurde.

1. Bestätigen Sie, dass:
    1. die Räder nicht mit dem Auto verbunden sind
    2. der Arduino mit dem Computer verbunden ist
    3. der richtige USB-Port ausgewählt ist
2. Öffnen Sie den Seriellen Monitor: `Werkzeuge` :arrow_right: `Serieller Monitor`

#### Nachrichten an den OpenBot senden

Sie können auch Nachrichten an den Arduino senden, indem Sie einen Befehl in das Eingabefeld oben eingeben und dann auf Senden drücken. Die folgenden Befehle sind verfügbar (vorausgesetzt, die notwendigen Funktionen werden vom Roboter unterstützt):

- `c<left>,<right>` wobei `<left>` und `<right>` beide im Bereich [-255,255] liegen. Ein Wert von `0` stoppt die Motoren. Ein Wert von `255` legt die maximale Spannung an, die die Motoren mit voller Geschwindigkeit vorwärts antreibt. Niedrigere Werte führen zu entsprechend niedrigeren Spannungen und Geschwindigkeiten. Negative Werte legen die entsprechenden Spannungen in umgekehrter Polarität an und treiben die Motoren rückwärts.
- `i<left>,<right>` wobei `<left>` und `<right>` beide im Bereich [0,1] liegen und den linken und rechten Indikator-LEDs entsprechen. Zum Beispiel schaltet `i1,0` den linken Indikator ein, `i0,1` den rechten Indikator und `i1,1` beide Indikatoren. Aktivierte Indikatorleuchten blinken einmal pro Sekunde. Ein Wert von `i0,0` schaltet die Indikatoren aus. Es ist nur ein Zustand gleichzeitig möglich.
- `l<front>,<back>` wobei `<front>` und `<back>` beide im Bereich [0,255] liegen und der Helligkeit der vorderen und hinteren LEDs entsprechen.
- `s<time_ms>` wobei `<time_ms>` der Zeit in ms zwischen ausgelösten Sonarmessungen entspricht (Standard = 1000). Nachdem die Sonarmessung erfasst wurde, wird die Nachricht an den Roboter gesendet. Wenn es zu einem Timeout kommt, wird die angegebene `MAX_SONAR_DISTANCE` gesendet.
- `w<time_ms>` wobei `<time_ms>` der Zeit in ms zwischen den an den Roboter gesendeten Radodometriemessungen entspricht (Standard = 1000). Die Raddrehzahl wird kontinuierlich überwacht und die Umdrehungen pro Minute werden als Durchschnitt über das angegebene Zeitintervall berechnet.
- `v<time_ms>` wobei `<time_ms>` der Zeit in ms zwischen den an den Roboter gesendeten Spannungsmessungen entspricht (Standard = 1000). Die Spannung wird kontinuierlich überwacht und über einen gleitenden Durchschnittsfilter der Größe 10 gefiltert. Zusätzlich zur Einstellung des Zeitintervalls für Spannungsmessungen löst das Senden dieses Befehls auch Nachrichten aus, die die minimale Spannung zum Antrieb der Motoren (`vmin:<value>`), die minimale Batteriespannung (`vlow:<value>`) und die maximale Batteriespannung (`vmax:<value>`) melden.
- `h<time_ms>` wobei `<time_ms>` der Zeit in ms entspricht, nach der der Roboter stoppt, wenn keine neue Herzschlagnachricht empfangen wurde (Standard = -1).
- `b<time_ms>` wobei `<time_ms>` der Zeit in ms entspricht, nach der der Stoßsensor zurückgesetzt wird (Standard = 750).
- `n<color>,<state>` wobei `<color>` einer Status-LED entspricht (`b` = blau, `g` = grün, `y` = gelb) und `state` ihrem Wert (`0` = aus, `1` = an).
- `f` sendet eine Anfrage an den OpenBot, eine Nachricht mit dem Robotertyp und seinen Funktionen zurückzusenden, z.B. Spannungsmessung (`v`), Indikatoren (`i`), Sonar (`s`), Stoßsensoren (`b`), Radodometrie (`wf`, `wb`), LEDs (`lf`, `lb`, `ls`), etc. Zum Beispiel würde die Nachricht für die `RTR_V1`-Version von OpenBot so aussehen: `fRTR_V1:v:i:s:b:wf:wb:lf:lb:ls:`.

#### Nachrichten vom OpenBot empfangen

Je nach Konfiguration können Sie unterschiedliche Nachrichten sehen.

![Serial Monitor](../docs/images/serial_monitor.png)

- Nachrichten, die mit `v` beginnen, melden die Batteriespannung. Wenn Sie die Batterie an das Auto anschließen (d.h. den Schalter einschalten), sollte die Batteriespannung angezeigt werden. Wenn Sie die Batterie trennen (d.h. den Schalter ausschalten), sollte ein kleiner Wert angezeigt werden.
- Nachrichten, die mit `w` beginnen, melden die Messwerte der Geschwindigkeitsensoren in Umdrehungen pro Minute (rpm). Jedes Loch in der Encoder-Scheibe erhöht den Zähler um plus/minus eins, je nach Richtung. Sie können die Anzahl der Löcher mit dem Parameter `DISK_HOLES` einstellen. Wenn Sie die Standardscheibe mit 20 Löchern verwenden, gibt es 20 Zählungen für jede Umdrehung des Rades.
- Nachrichten, die mit `s` beginnen, melden den geschätzten freien Raum vor dem Ultraschallsensor in cm.
- Nachrichten, die mit `b` beginnen, melden Kollisionen. Die Codes `lf` (links vorne), `rf` (rechts vorne), `cf` (Mitte vorne), `lb` (links hinten), `rb` (rechts hinten) geben an, welcher Sensor die Kollision ausgelöst hat.

#### Testverfahren

Bevor Sie fortfahren, stellen Sie sicher, dass die Reifen entfernt sind. Sie benötigen den Seriellen Monitor, um Befehle zu senden, und Sie sehen die empfangenen Nachrichten von Ihrem OpenBot. Wenn Sie das OLED-Display installiert haben, sehen Sie den Fahrzeugstatus auch dort in einem benutzerfreundlicheren Format. Das folgende Testverfahren kann verwendet werden, um alle Funktionen des Autos zu testen:

1. Schalten Sie das Auto ein und beobachten Sie die Batteriespannung (die Zahl nach dem `v`). Sie können die Messung mit einem Multimeter überprüfen und den `VOLTAGE_DIVIDER_FACTOR` bei Bedarf anpassen.
2. Wenn Sie einen Ultraschallsensor installiert haben:
    1. Halten Sie Ihre Hand vor den Sensor und bewegen Sie sie hin und her. Sie sollten sehen, dass sich die Messwerte (die Zahl nach dem `s`) entsprechend ändern.
    2. Wir haben beobachtet, dass der Ultraschallsensor sehr empfindlich auf Vibrationen reagiert! Daher ist es ratsam, sicherzustellen, dass Sie während des Betriebs zuverlässige Messwerte erhalten, indem Sie den folgenden Test durchführen:
        1. Platzieren Sie den OpenBot mit installiertem Ultraschallsensor so, dass mindestens 200 cm freier Raum vor ihm ist. Sie sollten eine Messung von `200` oder mehr sehen.
        2. Beobachten Sie die Messwerte im seriellen Monitor eine Weile und geben Sie dann den Befehl `c128,128` ein.
        3. Wenn sich die Sensormesswerte erheblich ändern, müssen Sie die Vibrationen, die vom Chassis auf den Ultraschallsensor übertragen werden, dämpfen (z.B. etwas Silikon hinzufügen, die Montageposition anpassen).
3. Wenn Sie die Geschwindigkeitssensoren installiert haben:
    1. Stellen Sie sicher, dass vor dem Ultraschallsensor viel freier Raum ist. Die Messung (die Zahl nach dem `s`) muss mindestens über dem `STOP_DISTANCE` liegen, der standardmäßig `10` beträgt.
    2. Senden Sie den Befehl `c128,128`. Die Motoren beginnen sich mit *langsamer Geschwindigkeit* (50% PWM) zu drehen. Die Messwerte der Geschwindigkeitssensoren (Werte nach dem `w`) werden in U/min angegeben und sollten je nach SOC der Batterie für die RTR_TT-Version zwischen 250 und 300 liegen. Wenn Sie die DIY-Version oder eine schwächere Batterie verwenden, können die Werte niedriger sein. Überprüfen Sie, ob sich alle Motoren vorwärts drehen und ob die Messwerte der Geschwindigkeitssensoren positiv sind.
    3. Versuchen Sie, verschiedene Steuerungen zu senden und beobachten Sie die Messwerte der Geschwindigkeitssensoren. Zum Beispiel dreht der Befehl `c-128,-128` alle Motoren rückwärts mit *langsamer Geschwindigkeit* (50% PWM). Der Befehl `c255,-255` dreht die linken Motoren vorwärts und die rechten Motoren rückwärts mit *schneller Geschwindigkeit* (100% PWM). Der Befehl `c-192,192` dreht die linken Motoren rückwärts und die rechten Motoren vorwärts mit *normaler Geschwindigkeit* (75% PWM).
4. Stoppen Sie die Motoren, indem Sie den Befehl `c0,0` senden oder Ihre Hand vor den Ultraschallsensor halten.
5. Wenn Sie die Blinker-LEDs installiert haben, senden Sie den Befehl `i1,0` und beobachten Sie das Blinken des linken Blinkers. Senden Sie dann den Befehl `i0,1` und beobachten Sie das Blinken des rechten Blinkers. Schalten Sie schließlich den Blinker aus, indem Sie den Befehl `i0,0` senden.

### Kein Telefon-Modus

Bevor Sie das Auto mit einem Smartphone testen, auf dem die OpenBot-Anwendung installiert ist, können Sie das Auto auch ohne Telefon testen. Stellen Sie einfach die Option `NO_PHONE_MODE` auf `1`. Das Auto fährt nun mit *normaler Geschwindigkeit* (75% PWM) und verlangsamt sich, wenn es Hindernisse mit dem Ultraschallsensor erkennt. Sobald es sich dem `TURN_THRESHOLD` (Standard: 50 cm) nähert, beginnt es, sich in eine zufällige Richtung zu drehen und schaltet die LED auf dieser Seite ein. Wenn der geschätzte freie Raum vor dem Auto unter den `TURN_THRESHOLD` fällt, fährt es langsam rückwärts und beide LEDs schalten sich ein. Beachten Sie, dass sowohl das Auto als auch das Arduino mit Strom versorgt werden müssen. Das Arduino kann mit Strom versorgt werden, indem der 5V-Pin mit dem 5V-Ausgang des L298N-Motortreibers verbunden wird, oder indem das USB-Kabel an eine Stromquelle (z.B. Telefon) angeschlossen wird.

Bevor Sie das Auto fahren lassen, empfehlen wir, die Reifen zu entfernen, das Arduino mit einem Computer zu verbinden und den seriellen Monitor wie im Abschnitt [Testen](#testing) zu beobachten. Die Ausgabe auf dem seriellen Monitor ist etwas leichter zu lesen (gleich wie OLED) und zeigt die Batteriespannung, die U/min für die linken und rechten Motoren und den geschätzten freien Raum vor dem Auto an. Sie können ein großes Objekt vor dem Ultraschallsensor hin und her bewegen und beobachten, wie sich die Geschwindigkeit der Motoren ändert.

:warning: WARNUNG: Wenn Sie keinen Ultraschallsensor installiert haben oder dieser deaktiviert ist, fährt das Auto einfach mit *normaler Geschwindigkeit* (75% PWM) vorwärts und wird schließlich kollidieren. Auch mit installiertem Sensor kann das Auto gelegentlich aufgrund von Rauschmessungen kollidieren.

## Verwendung anderer MCUs (Anforderungen)

Sie können jede andere MCU mit den folgenden Funktionen verwenden:

- 1x USB-zu-TTL-Seriell (Kommunikation mit dem Smartphone)
- 4x PWM-Ausgang (Steuerung der Motoren)
- 1x Analog-Pin zur Batterieüberwachung
- 2x Digital-Pin für die Geschwindigkeitssensoren
- 1x Digital-Pin für den Ultraschallsensor (optional)
- 2x Digital-Pin für die Blinker-LEDs (optional)

## Weiter

Kompilieren und führen Sie die [Android-App](../android/README.md) aus.
