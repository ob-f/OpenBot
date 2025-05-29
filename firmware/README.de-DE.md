# Firmware

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Wir verwenden eine Mikrocontroller-Einheit (MCU), um als Brücke zwischen dem Roboterkörper und dem Smartphone zu fungieren. Wir stellen unsere [Firmware](openbot/openbot.ino) für den Arduino Nano mit einem ATmega328P-Mikrocontroller sowie für das ESP32-Entwicklungskit bereit.

## Funktionen

Die Hauptaufgabe der MCU besteht darin, die Steuerung des Fahrzeugs auf niedriger Ebene zu übernehmen und Messwerte von fahrzeugmontierten Sensoren auf niedriger Ebene bereitzustellen. Die MCU empfängt die Fahrzeugsteuerungen und Anzeigesignale über die serielle Verbindung. Sie wandelt die Steuerungen in PWM-Signale für den Motorcontroller um und schaltet die LEDs entsprechend dem Anzeigesignal um. Das Arduino-Programm verfolgt auch die Radumdrehungen, indem es die Unterbrechungen der optischen Sensoren an den linken und rechten Vorderrädern zählt. Es berechnet die Batteriespannung durch einen skalierten gleitenden Durchschnitt der Messungen an der Spannungsteiler-Schaltung. Es kann auch den Abstand zu Hindernissen vor dem Auto mit einem optionalen Ultraschallsensor messen. Diese Messungen werden über die serielle Verbindung an die Android-Anwendung zurückgesendet.

## Einrichtung

Zuerst müssen Sie Ihre Hardwarekonfiguration am Anfang des Codes einrichten. Wenn Sie den DIY-Bau (mit dem L298N-Motortreiber) durchgeführt haben, setzen Sie `OPENBOT DIY`. Wenn Sie die benutzerdefinierte Leiterplatte verwendet haben, überprüfen Sie die Version und setzen Sie entweder `OPENBOT PCB_V1` oder `OPENBOT PCB_V2`. Wenn Sie ein OpenBot-Kit haben, setzen Sie `OPENBOT RTR_TT`. Wenn Sie einen RC-Truck nachgerüstet haben, setzen Sie `OPENBOT RC_CAR`. Wenn Sie die kleinere DIY-Version für Bildungszwecke verwenden, setzen Sie `OPENBOT LITE`. Wenn Sie das OpenBot Ready-to-Run-Kit mit 520-Motoren verwenden, setzen Sie `OPENBOT RTR_520`. Wenn Sie das Multi Terrain Vehicle gebaut haben, sollten Sie `OPENBOT MTV` setzen. Um den `OpenBot DIY` mit dem ESP32 zu betreiben, setzen Sie OpenBot `DIY_ESP32`.

## Bluetooth

Sie können den OpenBot auch über Bluetooth betreiben, dazu können Sie Bluetooth aktivieren, indem Sie `BLUETOOTH 1` setzen (deaktivieren: 0). Damit Bluetooth funktioniert, benötigen Sie OpenBot mit ESP32-Boards wie `(RTR_520, MTV, DIY_ESP32)`.

## Konfiguration

Als nächstes müssen Sie konfigurieren, welche Funktionen Sie aktivieren möchten. Deaktivierte Funktionen werden nicht kompiliert, um Speicher zu sparen und den Code schneller zu machen. Wenn ein Flag nicht definiert ist, wird die Funktion deaktiviert. Jedes Modell hat einige Standardeinstellungen, die Sie je nach Konfiguration ändern müssen.

- Aktivieren Sie den Spannungsteiler, indem Sie `HAS_VOLTAGE_DIVIDER 1` setzen (deaktivieren: 0). Wenn Sie einen Spannungsteiler haben, sollten Sie auch den `VOLTAGE_DIVIDER_FACTOR` angeben, der als (R1+R2)/R2 berechnet wird, `VOLTAGE_MIN`, die minimale Spannung zum Antrieb der Motoren, `VOLTAGE_LOW`, die minimale Batteriespannung und `VOLTAGE_MAX`, die maximale Batteriespannung.
- Aktivieren Sie die Anzeigelampen, indem Sie `HAS_INDICATORS 1` setzen (deaktivieren: 0).
- Aktivieren Sie die Geschwindigkeitsmesser vorne/hinten, indem Sie `HAS_SPEED_SENSORS_FRONT 1` / `HAS_SPEED_SENSORS_BACK 1` setzen (deaktivieren: 0).
- Aktivieren Sie den Ultraschallsensor, indem Sie `HAS_SONAR 1` setzen (deaktivieren: 0). Aktivieren Sie den Medianfilter für Sonarmessungen, indem Sie `USE_MEDIAN 1` setzen (deaktivieren: 0).
- Aktivieren Sie den Stoßfängersensor, der zur Erkennung von Kollisionen verwendet wird, indem Sie `HAS_BUMPER 1` setzen (deaktivieren: 0).
- Aktivieren Sie das OLED-Display, indem Sie `HAS_OLED 1` setzen (deaktivieren: 0).
- Aktivieren Sie die LEDs vorne/hinten/Status, indem Sie `HAS_LEDS_FRONT 1` / `HAS_LEDS_BACK 1` / `HAS_LEDS_STATUS 1` setzen (deaktivieren: 0).

### Abhängigkeiten

Wenn Sie die Geschwindigkeitsmesser oder den Ultraschallsensor aktiviert haben, müssen Sie die [PinChangeInterrupt](https://github.com/NicoHood/PinChangeInterrupt) Bibliothek installieren. Der Arduino Nano hat nur zwei externe Interrupt-Pins (D2 und D3) und D3 ist auch einer von nur sechs Pins, die PWM unterstützen. Glücklicherweise hat er auch drei Port-Interrupts, die alle Pins auf dem Arduino abdecken. Diese Bibliothek analysiert diese Port-Interrupts und ermöglicht es, alle Pins des Arduino als Interrupts zu verwenden.

Wenn Sie das OLED aktiviert haben, müssen Sie die Bibliotheken [Adafruit_SSD1306](https://github.com/adafruit/Adafruit_SSD1306) und [Adafruit_GFX Library](https://github.com/adafruit/Adafruit-GFX-Library) installieren.

Sie können Bibliotheken installieren, indem Sie diese Schritte befolgen:

1. Öffnen Sie den Bibliotheksmanager: `Werkzeuge` :arrow_right: `Bibliotheken verwalten`
2. Geben Sie den Namen der Bibliothek in die Suchleiste ein.
3. Wählen Sie die neueste Version aus und klicken Sie auf Installieren. Wenn Sie die Bibliothek bereits installiert haben, wird dies angezeigt und Sie können sie möglicherweise aktualisieren.

<p float="left">
  <img src="../docs/images/manage_libraries.jpg" height="300" />
  <img src="../docs/images/install_library.jpg" height="300" /> 
</p>

### Chinesischer Klon Nano (z.B. US-Link)

Möglicherweise müssen Sie die [WCH340](http://www.wch.cn/product/CH340.html) Treiber vom Chip-Hersteller (Chinesisch) herunterladen:

- [Windows](http://www.wch.cn/downloads/CH341SER_EXE.html)
- [Linux](http://www.wch.cn/download/CH341SER_LINUX_ZIP.html)
- [Mac](http://www.wch.cn/download/CH341SER_MAC_ZIP.html)

### ESP32 Entwicklungskit

Um das ESP32-Board in Ihrer Arduino-IDE zu installieren, folgen Sie diesen Anweisungen:

1. Gehen Sie in Ihrer Arduino-IDE zu **Datei > Voreinstellungen**:
<p align="center">
  <img src="../docs/images/arduino-ide-open-preferences.png" width="300" alt="App GUI"/>
</p>

2. Geben Sie *https://dl.espressif.com/dl/package_esp32_index.json* in das Feld „_Zusätzliche Boardverwalter-URLs_“ ein, wie in der Abbildung unten gezeigt. Klicken Sie dann auf die Schaltfläche „OK“:
<p align="center">
  <img src="../docs/images/arduino_preferences.png" width="600" alt="App GUI"/>
</p>

**Hinweis:** Wenn Sie bereits die ESP8266-Boards-URL haben, können Sie die URLs mit einem Komma trennen, wie folgt:

    https://dl.espressif.com/dl/package_esp32_index.json,
    http://arduino.esp8266.com/stable/package_esp8266com_index.json

3. Öffnen Sie den Boardverwalter. Gehen Sie zu **Werkzeuge > Board > Boardverwalter**:
<p align="center">
  <img src="../docs/images/arduino_boardsManager.png" width="800" alt="App GUI"/>
</p>

4. Suchen Sie nach ESP32. Suchen Sie nach „ESP32 by Espressif Systems“ und stellen Sie sicher, dass Sie Version 2.0.17 aus dem Dropdown-Menü auswählen. Jede Version 3.x.x wird nicht funktionieren! Klicken Sie auf Installieren:
<p align="center">
  <img src="../docs/images/arduino_installing.png" width="600" alt="App GUI"/>
</p>

5. Sie sollten nun alles haben, um das ESP32-Board Ihres OpenBot erfolgreich mit der Arduino-Entwicklungsumgebung zu flashen.
<p align="center">
  <img src="../docs/images/arduino_ESP32-Board-add-on-in-Arduino-IDE-installed.png" width="600" alt="App GUI"/>
</p>

6. Um den OpenBot mit Ihrem neuen Code zu flashen, wählen Sie einfach **ESP32 Dev Module** im Menü **Werkzeuge > Board > ESP32 Arduino**. Beachten Sie, dass zusätzlicher Inhalt sowie die Fehlerbehebung des ESP32-Flashvorgangs im folgenden [Link](https://randomnerdtutorials.com/installing-the-esp32-board-in-arduino-ide-windows-instructions/) zu finden sind.
<p align="center">
  <img src="../docs/images/arduino_windows-select-board.png" width="600" alt="App GUI"/>
</p>

Wenn Sie Probleme beim Hochladen haben, versuchen Sie, die Baudrate zu reduzieren. Gehen Sie zu Werkzeuge und wählen Sie die Upload-Geschwindigkeit als 115200.
<p align="center">
<img src="../docs/images/arduino_upload-speed-select.png" width="600" alt="App GUI"/>
</p>

## Hochladen

### Einstellungen (Arduino Nano Setup)

- `Werkzeuge` :arrow_right: `Board` :arrow_right: `Arduino AVR Boards` :arrow_right: `Arduino Nano`
- `Werkzeuge` :arrow_right: `Prozessor` :arrow_right: `ATmega328P (Old Bootloader)`
- `Werkzeuge` :arrow_right: `Port` :arrow_right: `*Wählen Sie den USB-Port*`

:memo: HINWEIS: Derzeit kommen die meisten günstigen Arduino Nano Boards mit dem _Old Bootloader_. Je nach Verkäufer können Sie jedoch auch einen mit dem neuen Bootloader erhalten. Wenn Sie die Firmware nicht hochladen können, müssen Sie möglicherweise den Prozessor auf _ATmega328P_ ändern.

### Einstellungen (ESP32 Setup)

- `Werkzeuge` :arrow_right: `Board` :arrow_right: `ESP32 Arduino` :arrow_right: `ESP32 Dev Module`
- `Werkzeuge` :arrow_right: `Port` :arrow_right: `*Wählen Sie den USB-Port*`

### Hochladen der Firmware

Die Firmware kann jetzt über `Sketch` :arrow_right: `Hochladen` oder durch Drücken der Hochladetaste (rechter Pfeil) hochgeladen werden.
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

- `c<left>,<right>` wobei `<left>` und `<right>` beide im Bereich [-255,255] liegen. Ein Wert von `0` stoppt die Motoren. Ein Wert von `255` wendet die maximale Spannung an, die die Motoren mit voller Geschwindigkeit vorwärts antreibt. Niedrigere Werte führen zu proportional niedrigeren Spannungen und Geschwindigkeiten. Negative Werte wenden die entsprechenden Spannungen in umgekehrter Polarität an, die die Motoren rückwärts antreiben.
- `i<left>,<right>` wobei `<left>` und `<right>` beide im Bereich [0,1] liegen und den linken und rechten Anzeigelampen entsprechen. Zum Beispiel schaltet `i1,0` den linken Blinker ein, `i0,1` den rechten Blinker und `i1,1` beide Blinker. Aktivierte Blinker blinken einmal pro Sekunde. Ein Wert von `i0,0` schaltet die Blinker aus. Es ist nur ein Zustand gleichzeitig möglich.
- `l<front>,<back>` wobei `<front>` und `<back>` beide im Bereich [0,255] liegen und der Helligkeit der vorderen und hinteren LEDs entsprechen.
- `s<time_ms>` wobei `<time_ms>` der Zeit in ms zwischen ausgelösten Sonarmessungen entspricht (Standard = 1000). Nachdem die Sonarmessung erfasst wurde, wird die Nachricht an den Roboter gesendet. Wenn es zu einem Timeout kommt, wird die angegebene `MAX_SONAR_DISTANCE` gesendet.
- `w<time_ms>` wobei `<time_ms>` der Zeit in ms zwischen Radodometriemessungen entspricht, die an den Roboter gesendet werden (Standard = 1000). Die Radgeschwindigkeit wird kontinuierlich überwacht und die Umdrehungen pro Minute werden als Durchschnitt über das angegebene Zeitintervall berechnet.
- `v<time_ms>` wobei `<time_ms>` der Zeit in ms zwischen Spannungsmessungen entspricht, die an den Roboter gesendet werden (Standard = 1000). Die Spannung wird kontinuierlich überwacht und über einen gleitenden Durchschnittsfilter der Größe 10 gefiltert. Zusätzlich zur Einstellung des Zeitintervalls für Spannungsmessungen löst das Senden dieses Befehls auch Nachrichten aus, die die minimale Spannung zum Antrieb der Motoren (`vmin:<value>`), die minimale Batteriespannung (`vlow:<value>`) und die maximale Batteriespannung (`vmax:<value>`) melden.
- `h<time_ms>` wobei `<time_ms>` der Zeit in ms entspricht, nach der der Roboter stoppt, wenn keine neue Herzschlagnachricht empfangen wurde (Standard = -1).
- `b<time_ms>` wobei `<time_ms>` der Zeit in ms entspricht, nach der der Stoßfänger-Trigger zurückgesetzt wird (Standard = 750).
- `n<color>,<state>` wobei `<color>` einer Status-LED entspricht (`b` = blau, `g` = grün, `y` = gelb) und `state` ihrem Wert (`0` = aus, `1` = an).
- `f` sendet eine Anfrage an den OpenBot, um eine Nachricht mit dem Robotertyp und seinen Funktionen zurückzugeben, z.B. Spannungsmessung (`v`), Blinker (`i`), Sonar (`s`), Stoßsensoren (`b`), Radodometrie (`wf`, `wb`), LEDs (`lf`, `lb`, `ls`), etc. Zum Beispiel würde für die `RTR_V1` Version von OpenBot die Nachricht so aussehen: `fRTR_V1:v:i:s:b:wf:wb:lf:lb:ls:`.

#### Nachrichten vom OpenBot empfangen

Je nach Konfiguration können Sie unterschiedliche Nachrichten sehen.

![Serial Monitor](../docs/images/serial_monitor.png)

- Nachrichten, die mit `v` beginnen, melden die Batteriespannung. Wenn Sie die Batterie an das Auto anschließen (d.h. den Schalter einschalten), sollte die Batteriespannung angezeigt werden. Wenn Sie die Batterie trennen (d.h. den Schalter ausschalten), sollte ein kleiner Wert angezeigt werden.
- Nachrichten, die mit `w` beginnen, melden Messwerte der Geschwindigkeitsmesser in Umdrehungen pro Minute (rpm). Jedes Loch in der Encoderscheibe erhöht einen Zähler um plus/minus eins, je nach Richtung. Sie können die Anzahl der Löcher mit dem Parameter `DISK_HOLES` einstellen. Wenn Sie die Standardscheibe mit 20 Löchern verwenden, gibt es 20 Zählungen für jede Umdrehung des Rades.
- Nachrichten, die mit `s` beginnen, melden den geschätzten freien Raum vor dem Ultraschallsensor in cm.
- Nachrichten, die mit `b` beginnen, melden Kollisionen. Die Codes `lf` (links vorne), `rf` (rechts vorne), `cf` (Mitte vorne), `lb` (links hinten), `rb` (rechts hinten) geben an, welcher Sensor die Kollision ausgelöst hat.

#### Testverfahren

Bevor Sie fortfahren, stellen Sie sicher, dass die Reifen entfernt sind. Sie benötigen den Seriellen Monitor, um Befehle zu senden, und Sie werden die empfangenen Nachrichten von Ihrem OpenBot sehen. Wenn Sie das OLED-Display installiert haben, sehen Sie auch den Fahrzeugstatus dort in einem besser lesbaren Format angezeigt. Das folgende Testverfahren kann verwendet werden, um alle Funktionen des Autos zu testen:

1. Schalten Sie das Auto ein und beobachten Sie die Batteriespannung (die Zahl nach dem `v`). Sie können die Messung mit einem Multimeter überprüfen und den `VOLTAGE_DIVIDER_FACTOR` bei Bedarf anpassen.
2. Wenn Sie einen Ultraschallsensor installiert haben:
   1. Halten Sie Ihre Hand vor den Sensor und bewegen Sie sie hin und her. Sie sollten sehen, dass sich die Messwerte (die Zahl nach dem `s`) entsprechend ändern.
   2. Wir haben festgestellt, dass der Ultraschallsensor sehr empfindlich auf Vibrationen reagiert! Es ist daher ratsam, sicherzustellen, dass Sie während des Betriebs zuverlässige Messwerte erhalten, indem Sie den folgenden Test durchführen:
      1. Platzieren Sie den OpenBot mit installiertem Ultraschallsensor so, dass mindestens 200 cm freier Raum vor ihm sind. Sie sollten eine Messung von `200` oder mehr sehen.
      2. Beobachten Sie die Messwerte auf dem seriellen Monitor eine Weile und geben Sie dann den Befehl `c128,128` ein.
      3. Wenn sich die Sensorwerte erheblich ändern, müssen Sie die Vibrationen, die vom Chassis auf den Ultraschallsensor übertragen werden, dämpfen (z.B. etwas Silikon hinzufügen, die Montageposition anpassen).
3. Wenn Sie die Geschwindigkeitsmesser installiert haben:
   1. Stellen Sie sicher, dass Sie viel freien Raum vor dem Ultraschallsensor haben. Die Messung (die Zahl nach dem `s`) muss mindestens über dem `STOP_DISTANCE` liegen, der standardmäßig `10` beträgt.
   2. Senden Sie den Befehl `c128,128`. Die Motoren beginnen mit _langsamer Geschwindigkeit_ (50% PWM) zu drehen. Die Messwerte der Geschwindigkeitsmesser (Werte nach dem `w`) werden in Umdrehungen pro Minute gemeldet und sollten für die RTR_TT-Version je nach SOC der Batterie zwischen 250 und 300 liegen. Wenn Sie die DIY-Version oder eine schwächere Batterie verwenden, können die Werte niedriger sein. Überprüfen Sie, ob alle Motoren vorwärts drehen und dass die Messwerte der Geschwindigkeitsmesser positiv sind.
   3. Versuchen Sie, verschiedene Steuerungen zu senden und beobachten Sie die Messwerte der Geschwindigkeitsmesser. Zum Beispiel dreht der Befehl `c-128,-128` alle Motoren rückwärts mit _langsamer Geschwindigkeit_ (50% PWM). Der Befehl `c255,-255` dreht die linken Motoren vorwärts und die rechten Motoren rückwärts mit _schneller Geschwindigkeit_ (100% PWM). Der Befehl `c-192,192` dreht die linken Motoren rückwärts und die rechten Motoren vorwärts mit _normaler Geschwindigkeit_ (75% PWM).
4. Stoppen Sie die Motoren, indem Sie den Befehl `c0,0` senden oder indem Sie Ihre Hand vor den Ultraschallsensor halten.
5. Wenn Sie die Anzeigelampen installiert haben, senden Sie den Befehl `i1,0` und beobachten Sie das Blinken der linken Anzeigelampe. Senden Sie dann den Befehl `i0,1` und beobachten Sie das Blinken der rechten Anzeigelampe. Schalten Sie schließlich die Anzeigelampen aus, indem Sie den Befehl `i0,0` senden.

### Kein Telefon-Modus

Bevor Sie das Auto mit einem Smartphone testen, auf dem die OpenBot-Anwendung installiert ist, können Sie das Auto auch ohne Telefon testen. Setzen Sie einfach die Option `NO_PHONE_MODE` auf `1`. Das Auto fährt nun mit _normaler Geschwindigkeit_ (75% PWM) und verlangsamt sich, wenn es Hindernisse mit dem Ultraschallsensor erkennt. Sobald es sich dem `TURN_THRESHOLD` (Standard: 50 cm) nähert, beginnt es, sich in eine zufällige Richtung zu drehen und schaltet die LED auf dieser Seite ein. Wenn der geschätzte freie Raum vor dem Auto unter den `TURN_THRESHOLD` fällt, fährt es langsam rückwärts und beide LEDs leuchten auf. Beachten Sie, dass sowohl das Auto als auch der Arduino mit Strom versorgt werden müssen. Der Arduino kann mit Strom versorgt werden, indem der 5V-Pin mit dem 5V-Ausgang des L298N-Motortreibers verbunden wird oder indem das USB-Kabel an eine Stromquelle (z.B. Telefon) angeschlossen wird.

Bevor Sie das Auto laufen lassen, empfehlen wir, die Reifen zu entfernen, den Arduino mit einem Computer zu verbinden und den seriellen Monitor wie im Abschnitt [Testen](#testing) zu beobachten. Die Ausgabe auf dem seriellen Monitor ist etwas einfacher zu parsen (gleich wie OLED) und zeigt die Batteriespannung, die Umdrehungen pro Minute für die linken und rechten Motoren und den geschätzten freien Raum vor dem Auto an. Sie können ein großes Objekt vor dem Ultraschallsensor hin und her bewegen und beobachten, wie sich die Geschwindigkeit der Motoren ändert.

:warning: WARNUNG: Wenn Sie keinen Ultraschallsensor installiert haben oder wenn er deaktiviert ist, fährt das Auto einfach mit _normaler Geschwindigkeit_ (75% PWM) vorwärts und wird schließlich kollidieren. Selbst mit installiertem Sensor kann das Auto gelegentlich aufgrund von Rauschmessungen kollidieren.

## Verwendung anderer MCUs (Anforderungen)

Sie können jede andere MCU mit den folgenden Funktionen verwenden:

- 1x USB-zu-TTL-Seriell (Kommunikation mit dem Smartphone)
- 4x PWM-Ausgang (Steuerung der Motoren)
- 1x analoger Pin zur Batterieüberwachung
- 2x digitaler Pin für die Geschwindigkeitsmesser
- 1x digitaler Pin für den Ultraschallsensor (optional)
- 2x digitale Pins für die Anzeigelampen (optional)

## Weiter

Kompilieren und ausführen der [Android App](../android/README.de-DE.md)