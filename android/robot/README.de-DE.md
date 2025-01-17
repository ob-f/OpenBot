# Roboter-App

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## HAFTUNGSAUSSCHLÜSSE

1. **Sicherheit:** Stellen Sie immer sicher, dass Sie in einer sicheren Umgebung arbeiten. Denken Sie daran, dass Ihr Telefon bei einer Kollision beschädigt werden könnte! Besondere Vorsicht ist geboten, wenn Sie automatisierte Steuerungen verwenden (z.B. Personenverfolgung oder Fahrmodi). Stellen Sie sicher, dass Sie immer einen Gamecontroller angeschlossen haben und mit der Tastenbelegung vertraut sind, damit Sie das Fahrzeug jederzeit stoppen können. Nutzung auf eigenes Risiko!
2. **App in Entwicklung:** Die Anwendung befindet sich in der Entwicklung und kann je nach Telefonmodell und Betriebssystemversion abstürzen oder unerwartetes Verhalten zeigen. Stellen Sie sicher, dass Sie alle Funktionen ohne angeschlossene Räder testen. Nutzung auf eigenes Risiko!

## App-Bildschirme

### Hauptmenü

Die App startet mit einem Menübildschirm, der alle verfügbaren Bildschirme anzeigt. Der Einstellungsbildschirm kann durch Klicken auf das Symbol in der oberen rechten Ecke geöffnet werden. Durch Klicken auf die anderen Symbole kann der Benutzer auf verschiedene Bildschirme zugreifen, deren Funktionen im Folgenden erklärt werden.

<p align="left">
<img style="padding-right: 2%;" src="../../docs/images/screen_main.png" alt="Hauptmenü" width="24.5%"/>
<img src="../../docs/images/screen_settings.png" alt="Einstellungsmenü" width="24.5%"/>
<img src="../../docs/images/dialog_stream_mode.png" alt="Einstellungsmenü" width="24.5%"/>
<img src="../../docs/images/dialog_connectivity_mode.png" alt="Einstellungsmenü" width="24.5%"/>
</p>

### Einstellungsmenü

#### USB-Verbindung

Tippen Sie auf das USB-Symbol, um die USB-Optionen zu öffnen. Das Dropdown-Menü wird verwendet, um die Baudrate einzustellen. Der Standardwert ist 115200 und Sie sollten dies nicht ändern müssen, es sei denn, Sie ändern die Arduino-Firmware. Die App versucht automatisch eine Verbindung herzustellen, aber falls Sie auf Probleme stoßen, können Sie diesen Schalter verwenden, um die Verbindung zu trennen/herzustellen.

<p align="left">
<img src="../../docs/images/usb_disconnected.jpg" alt="Gerät verbinden" width="25%"/>
<img src="../../docs/images/usb_connected.jpg" alt="Trennen-Schaltfläche" width="25%"/>
</p>

#### Berechtigungen

Hier können Sie die Berechtigungen der App überprüfen und bei Bedarf anpassen.

#### Video-Streaming

Sie können zwischen `WebRTC` und `RTSP` für das Streaming von Videos auf ein externes Gerät wählen. Die Telefon-Controller-App und der Node-js-Server müssen beide auf `WebRTC` eingestellt sein. Der Python-Controller erwartet, dass der Stream auf `RTSP` eingestellt ist.

#### Bluetooth-Verbindung

Stellen Sie sicher, dass Ihr Android-Gerät BLE (Bluetooth Low Energy) unterstützt. Wenn Ihre Android-Version größer oder gleich 7.0 ist, müssen Sie auch den Standortdienst aktivieren und die Standortberechtigung in den Einstellungen zulassen, um nach nahegelegenen BLE-Geräten zu suchen. Um BLE zu aktivieren, ändern Sie den Verbindungsmodus im Einstellungsmenü von USB auf Bluetooth. Sie erhalten ein Bluetooth-Symbol oben auf dem Startbildschirm. Tippen Sie auf das Bluetooth-Symbol, um das BLE-Scannen zu starten; es dauert 4 Sekunden, um eine Liste aller nahegelegenen OpenBot-BLE-Geräte zu scannen und zu erhalten. Verbinden Sie sich mit Ihrem OpenBot, indem Sie auf die Schaltfläche `Verbinden` tippen. Nach erfolgreicher Verbindung ändert sich die Schaltfläche `Verbinden` zu `Trennen`. Sie können jetzt zum Startbildschirm zurückkehren.

<p align="left">
<img src="../../docs/images/ble_devices_list.jpg" alt="BLE-Geräte" width="25%"/>
<img src="../../docs/images/ble_device_connecting.jpg" alt="Gerät verbinden" width="25%"/>
<img src="../../docs/images/ble_device_connected.jpg" alt="Trennen-Schaltfläche" width="25%"/>
</p>

### Freies Fahren

Freies Fahren bietet einfache Robotersteuerung mit Echtzeit-Updates und Informationen über Batterie, Geschwindigkeit und Abstand zu Oberflächen.

<p align="left">
<img src="../../docs/images/screen_free_roam.jpg" alt="Freies Fahren" width="50%" />
</p>

- **Batterie**: Das Batteriesymbol zeigt den Echtzeit-Batteriestand des verbundenen Roboters an.
- **Fahrzustand**: Es gibt 3 Fahrzustände, die auf dem Bildschirm angezeigt werden:
  - D -> Fahren, wenn der Roboter vorwärts fährt
  - N -> Neutral, wenn der Roboter stillsteht
  - R -> Rückwärts, wenn der Roboter rückwärts fährt
  Das Lenkrad dreht sich proportional zum Lenkwinkel.
- **Geschwindigkeit**: Der Tachometer zeigt die Geschwindigkeit des Roboters an.
- **Sonar**: Der freie Abstand vor dem Roboter in cm.
- **Steuerung**: Controller, Fahrmodus und Geschwindigkeit werden verwendet, um die Roboter-Einstellungen wie im Abschnitt [Steuerung](#steuerung) beschrieben zu steuern.

### Datensammlung

Einfache Benutzeroberfläche zur Sammlung von Datensätzen.

<p align="left">
<img src="../../docs/images/screen_data_collection.jpg" alt="Datensammlung" width="50%" />
</p>

- **Server**: Wenn Sie die [Web-App](../../policy#web-app) für das Training von Richtlinien ausführen, können Sie sie hier auswählen, um Daten automatisch hochzuladen.
- **Vorschauauflösung**: Wird verwendet, um zwischen den Auflösungen der Kameravorschau zu wechseln. Es gibt 3 Einstellungen:
  - ***FULL_HD*** (1920x1080p)
  - ***HD*** (1280x720p)
  - ***SD*** (640x360)
- **Modellauflösung**: Wird verwendet, um zwischen den Auflösungen der für das Training verschiedener Modelle gespeicherten Bilder zu wechseln.
- **Daten speichern/verwerfen**: Der Datensammlungsprozess kann vom Bildschirm oder aus der Ferne, beispielsweise von einem Bluetooth-Controller, gesteuert werden. Wenn Sie einen Bluetooth-Controller verwenden, können Sie:
  - Drücken Sie die **A-Taste**, um den Datensammlungsprozess zu **starten**
  - Drücken Sie die **A-Taste erneut**, um die Datensammlung zu **stoppen** und die gesammelten Daten in einer .zip-Datei zu speichern
  - Alternativ drücken Sie die **R1-Taste**, um die Datensammlung **ohne Speichern** der gesammelten Daten zu **stoppen** (zum Beispiel wegen einer unerwarteten Kollision mit der Umgebung)
  - Denken Sie daran, das Controller-Mapping-Fragment zu verwenden, um sicherzustellen, dass Sie die richtigen Tasten verwenden.

### Controller-Mapping

Einfache Benutzeroberfläche, um die Tasten- und Joystick-Zuordnung eines verbundenen BT-Controllers zu überprüfen.

<p align="left">
<img src="../../docs/images/screen_controller_mapping.jpg" alt="Controller-Mapping" width="50%" />
</p>

### Roboter-Info

Einfache Benutzeroberfläche, um Roboterinformationen zu erhalten und grundlegende Funktionen zu testen. Der **Robotertyp**, wie in der Firmware konfiguriert, wird als Text und Animation angezeigt. Die Häkchen in den Abschnitten **Sensoren**, **Raddrehzahlmessung** und **LEDs** zeigen, welche Funktionen vom verbundenen Roboter unterstützt werden. Der Abschnitt **Messwerte** liefert die wichtigsten Sensormessungen. Im Abschnitt **Befehle senden** können Benutzer grundlegende Motorbefehle durch Drücken der entsprechenden Tasten senden und die vorderen und hinteren LEDs mit einem Schieberegler steuern.

<p align="left">
<img src="../../docs/images/screen_robot_info.gif" alt="Roboter-Info" width="50%" />
</p>

### Autopilot

Einfache Benutzeroberfläche zum Ausführen von Autopilot-Modellen.

<p align="left">
<img src="../../docs/images/screen_autopilot.jpg" alt="Autopilot" width="50%" />
</p>

- **Server**: Wenn Sie die [Web-App](../../policy#web-app) für das Training von Richtlinien ausführen, können Sie sie hier auswählen und trainierte Autopilot-Modelle an den Roboter senden.
- **Modell**: Wählen Sie ein trainiertes Modell für den Autopilot-Modus.
- **Gerät**: Verwenden Sie CPU, GPU oder NNAPI für die Inferenz (mehr Details [hier](#gerät)).
- **Threads**: Anzahl der zu verwendenden Threads (macht nur einen Unterschied, wenn CPU als Gerät ausgewählt ist).
- **Steuerung**: Controller, Fahrmodus und Geschwindigkeit werden verwendet, um die Roboter-Einstellungen wie im Abschnitt [Steuerung](#steuerung) beschrieben zu steuern.

### Objektverfolgung

Einfache Benutzeroberfläche zur Verfolgung von Objekten aus 80 verschiedenen Klassen. Eine kurze Beschreibung der verschiedenen KI-Modelle zur Objektverfolgung und Leistungsbenchmarks finden Sie unter [Modellverwaltung](#modellverwaltung).

<p align="left">
<img src="../../docs/images/screen_object_tracking_1.jpg" alt="Alt-Text" width="49%" />
<img src="../../docs/images/screen_object_tracking_2.jpg" alt="Alt-Text" width="49%" />
</p>

- **Dynamische Geschwindigkeit**: Reduziert die Geschwindigkeit des Roboters im "Auto-Modus", wenn er sich dem verfolgten Objekt nähert.
  Die Geschwindigkeit wird basierend auf der Fläche des Begrenzungsrahmens skaliert (funktioniert am besten im Querformat).
- **Modell**: Wählen Sie einen Objektdetektor basierend auf der Leistung Ihres Telefons (siehe unten für [Benchmark-Ergebnisse](#benchmark)).
- **Objekt**: Wählen Sie das Objekt aus, das Sie verfolgen möchten. Die Modelle können die 80 COCO [Objektklassen](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/) erkennen.
- **Vertrauen**: Schwellenwert für das Vertrauen, um zu bestimmen, ob Erkennungen akzeptiert werden. Erhöhen Sie den Wert, wenn Sie falsche Erkennungen erhalten, verringern Sie ihn, wenn das gewünschte Objekt nicht erkannt wird.
- **Gerät**: Verwenden Sie CPU, GPU oder NNAPI für die Inferenz (mehr Details [hier](#gerät)).
- **Threads**: Anzahl der zu verwendenden Threads (macht nur einen Unterschied, wenn CPU als Gerät ausgewählt ist).
- **Steuerung**: Controller, Fahrmodus und Geschwindigkeit werden verwendet, um die Roboter-Einstellungen wie im Abschnitt [Steuerung](#steuerung) beschrieben zu steuern.

### Punktziel-Navigation

Beachten Sie, dass dieses Fragment ARCore und Kameraberechtigung erfordert. Wenn Ihr Gerät ARCore nicht unterstützt und Sie trotzdem fortfahren, stürzt die App ab. Auf diesem Bildschirm können Sie ein Ziel über einen 2D-Vektor in Bezug auf die aktuelle Position und Ausrichtung des Roboters angeben. Der 2D-Vektor enthält den Abstand zur Vorderseite und zur linken Seite des Roboters in Metern. Beide Werte können auch negativ sein und entsprechen in diesem Fall der Rückseite und der rechten Seite des Roboters. Nach der Angabe des Ziels und dem Drücken von `Start` führt der Roboter eine KI-Richtlinie aus, die versucht, das Ziel zu erreichen und dabei Hindernisse zu vermeiden.

<p align="left">
<img src="../../docs/images/screen_point_goal_nav.gif" alt="Alt-Text" width="50%" />
</p>

### Modellverwaltung

Alle Modelle sind quantisiert, um eine bessere Leistung auf eingebetteten Geräten zu erzielen. Bitte beziehen Sie sich auf den untenstehenden Abschnitt für eine kurze Beschreibung der verfügbaren Modelle und Benchmark-Ergebnisse. Die [mean Average Precision (mAP)](https://kharshit.github.io/blog/2019/09/20/evaluation-metrics-for-object-detection-and-segmentation) wird auf dem Validierungssatz des [COCO Detection 2017](https://cocodataset.org/#detection-2017) Datensatzes berechnet. Jedes Modell wird etwa 1 Minute lang ausgeführt; die Inferenzzeit wird über die letzten 100 Frames gemittelt und in Frames pro Sekunde (fps) angegeben. Beachten Sie, dass Modelle mit höherer Eingangsauflösung trotz niedrigerer mAP besser für kleinere Objekte geeignet sein könnten.

<p align="left">
<img src="../../docs/images/screen_model_management.gif" alt="Modellverwaltung" width="25%" />
</p>

### Benchmark

#### Telefone

| Modellname       | Chipsatz        | RAM  | OS |
|------------------|-----------------|------|----|
| Samsung S22 Ultra| Exynos 2200     | 12GB | 12 |
| Samsung S20FE 5G | Snapdragon 865  |  6GB | 12 |
| Huawei P30 Pro   | Kirin 980       |  8GB | 10 |
| Google Pixel 6XL | Google Tensor   | 12GB | 12 |
| Xiaomi Mi9       | Snapdragon 855  |  6GB | 10 |
| Google Pixel 4XL | Snapdragon 855  |  6GB | 13 |

#### MobileNetV1-300 (vorinstalliert) - mAP: 18%

SSD-Objektdetektor mit [MobileNet V1](https://tfhub.dev/tensorflow/lite-model/ssd_mobilenet_v1/1/metadata/2) Backbone und Eingangsauflösung von 300x300.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra |  33 |  13 |   30  |
| Samsung S20FE 5G  |  34 |  57 |   87  |
| Huawei P30 Pro    |  36 |  25 |   10  |
| Google Pixel 6XL  |  35 |  42 |   53  |
| Xiaomi Mi9        |  22 |  41 |   33  |
| Google Pixel 4XL  |  37 |  36 |   45  |

#### MobileNetV3-320 - mAP: 16%

SSD-Objektdetektor mit MobileNet V3 Backbone und Eingangsauflösung von 320x320.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra |  30 |  17 |   30  |
| Samsung S20FE 5G  |  34 |  42 |   28  |
| Huawei P30 Pro    |  32 |  27 |   23  |
| Google Pixel 6XL  |  33 |  43 |   27  |
| Xiaomi Mi9        |  20 |  45 |   10  |
| Google Pixel 4XL  |  32 |  38 |   21  |

#### YoloV4-tiny-224 - mAP: 22%

Kleine Version von [YoloV4](https://arxiv.org/abs/2004.10934) mit Eingangsauflösung von 224x224.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra |  31 |  12 |   31  |
| Samsung S20FE 5G  |  30 |  21 |   14  |
| Huawei P30 Pro    |  27 |  17 |   22  |
| Google Pixel 6XL  |  29 |  24 |   19  |
| Xiaomi Mi9        |  16 |  14 |  9.3  |
| Google Pixel 4XL  |  22 |  19 |   14  |

#### YoloV4-tiny-416 - mAP: 29%

Kleine Version von [YoloV4](https://arxiv.org/abs/2004.10934) mit Eingangsauflösung von 416x416.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra |  13 | 9.8 |   13  |
| Samsung S20FE 5G  |  12 | 9.4 |  7.7  |
| Huawei P30 Pro    | 8.4 | 7.6 |  6.9  |
| Google Pixel 6XL  |  10 | 9.6 |  7.2  |
| Xiaomi Mi9        | 9.0 | 7.3 |  5.0  |
| Google Pixel 4XL  | 7.2 | 7.4 |  6.2  |

#### YoloV4-224 - mAP: 40%

[YoloV4](https://arxiv.org/abs/2004.10934) mit Eingangsauflösung von 224x224.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra | 3.7 | 5.6 |  3.5  |
| Samsung S20FE 5G  | 3.1 | 7.1 |  4.2  |
| Huawei P30 Pro    | 2.4 | 6.2 |  0.7  |
| Google Pixel 6XL  | 2.7 |  11 |  0.9  |
| Xiaomi Mi9        | 2.1 | 6.4 |  1.7  |
| Google Pixel 4XL  | 1.8 | 5.0 |  3.7  |

#### YoloV5s-320 - mAP: 28%

[YoloV5](https://github.com/ultralytics/yolov5) mit einer Eingangsauflösung von 320x320.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  21 |  10 |   21  |
| Xiaomi Mi9       |  13 |  15 |  0.8  |
| Google Pixel 4XL |  12 |  17 |   18  |

#### YoloV5s-640 - mAP: 34%

[YoloV5](https://github.com/ultralytics/yolov5) mit einer Eingangsauflösung von 640x640.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra| 5.5 | 4.9 |  5.0  |
| Xiaomi Mi9       | 4.1 | 4.6 |   -   |
| Google Pixel 4XL | 3.7 | 4.6 |  4.6  |

#### YoloV5m-320 - mAP: 35%

[YoloV5](https://github.com/ultralytics/yolov5) mit einer Eingangsauflösung von 320x320.

|phone/device (fps)| CPU | GPU | NNAPI |
|------------------|-----|-----|-------|
| Samsung S22 Ultra|  13 | 8.2 |   11  |
| Xiaomi Mi9       | 9.7 | 9.9 |   -   |
| Google Pixel 4XL | 7.9 | 9.2 |   15  |

#### YoloV5l-320 - mAP: 38%

[YoloV5](https://github.com/ultralytics/yolov5) mit einer Eingangsauflösung von 320x320.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra | 7.6 | 3.4 |  7.6  |
| Xiaomi Mi9        | 5.5 | 5.0 |   -   |
| Google Pixel 4XL  | 5.3 | 4.0 |  5.3  |

#### EfficientDet-L0-320 - mAP: 26%

[EfficientDet-L0](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite0/detection/metadata/1) mit einer Eingangsauflösung von 320x320. Hinweis: Die Modellleistung verschlechtert sich im Querformat; der Konfidenzschwellenwert muss möglicherweise angepasst werden.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra |  18 |  10 |   16  |
| Xiaomi Mi9        |  16 |  20 |  1.2  |
| Google Pixel 4XL  |  17 |  17 |   16  |

#### EfficientDet-L1-384 - mAP: 31%

[EfficientDet-L1](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite1/detection/metadata/1) mit einer Eingangsauflösung von 384x384. Hinweis: Die Modellleistung verschlechtert sich im Querformat; der Konfidenzschwellenwert muss möglicherweise angepasst werden.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra |  12 | 9.2 |   10  |
| Xiaomi Mi9        |  10 |  13 |    -  |
| Google Pixel 4XL  |  11 |  11 |   10  |

#### EfficientDet-L2-448 - mAP: 34%

[EfficientDet-L2](https://tfhub.dev/tensorflow/lite-model/efficientdet/lite2/detection/metadata/1) mit einer Eingangsauflösung von 448x448. Hinweis: Die Modellleistung verschlechtert sich im Querformat; der Konfidenzschwellenwert muss möglicherweise angepasst werden.

|Telefon/Gerät (fps)| CPU | GPU | NNAPI |
|-------------------|-----|-----|-------|
| Samsung S22 Ultra | 9.8 | 8.4 |  8.2  |
| Xiaomi Mi9        | 6.4 | 9.4 |   -   |
| Google Pixel 4XL  | 7.7 | 8.3 |  7.6  |

### Standard

Die [DefaultActivity](src/main/java/org/openbot/original/DefaultActivity.java) umfasst die wichtigsten Funktionen der OpenBot-App auf einem einzigen Bildschirm. Sie zeigt den Verbindungsstatus zum Fahrzeug an und meldet Messungen von Fahrzeugsensoren. Der Roboter kann mit Standard-BT-Gamecontrollern oder einem anderen Smartphone, auf dem die OpenBot-[Controller-App](../controller) läuft, gesteuert werden. Wir haben auch einen Datenlogger implementiert, um Datensätze mit dem Roboter zu sammeln. Derzeit zeichnen wir Messwerte der folgenden Sensoren auf: Kamera, Gyroskop, Beschleunigungsmesser, Magnetometer, Umgebungslichtsensor und Barometer. Mit der Android-API können wir die folgenden Sensordaten erfassen: RGB-Bilder, Winkelgeschwindigkeit, lineare Beschleunigung, Schwerkraft, Magnetfeldstärke, Lichtintensität, Luftdruck, Breiten- und Längengrad, Höhe, Kurs und Geschwindigkeit. Zusätzlich zu den Telefonsensoren zeichnen wir auch Körper-Sensordaten (Radodometrie, Hindernisabstand und Batteriespannung) auf, die über die serielle Verbindung übertragen werden. Wir zeichnen auch Steuerungssignale auf und stempeln sie mit einem Zeitstempel, wenn ein verbundener Controller vorhanden ist. Schließlich integrieren wir mehrere neuronale Netzwerke für die Personenverfolgung und die autonome Navigation.

<p align="left">
  <img src="../../docs/images/screen_default.jpg" alt="App GUI" width="50%"/>
</p>

#### USB-Verbindung

Wie im [Einstellungsmenü](#settings-menu) beschrieben.

#### Fahrzeugstatus

Das Feld **Batterie** zeigt die Batteriespannung an, die vom Arduino über den Spannungsteiler gemessen wird. Das Feld **Geschwindigkeit (l,r)** zeigt die linke und rechte Geschwindigkeit der (vorderen) Räder in U/min an. Diese wird vom Arduino über die optischen Raddrehzahlsensoren gemessen. Das Feld **Sonar** zeigt den freien Raum vor dem Auto in Zentimetern an. Diese wird vom Arduino über den Ultraschallsensor gemessen. Beachten Sie, dass Sie Werte erst einige Sekunden nach dem Herstellen der USB-Verbindung erhalten.

#### Steuerung

Der erste Button dient zur Auswahl des **Steuermodus**. Es gibt zwei verschiedene Steuermodi:

- **Gamepad**: Die App empfängt Steuerungen von einem verbundenen BT-Controller.
- **Telefon**: Der Roboter kann über ein anderes Smartphone mit installierter Controller-App oder über ein Python-Skript gesteuert werden, das auf einem mit demselben Netzwerk verbundenen Computer läuft.

Der zweite Button dient zur Auswahl des **Fahrmodus**. Es gibt drei verschiedene Fahrmodi bei Verwendung eines Gamecontrollers (z.B. PS4):

- **Spiel**: Verwenden Sie die rechten und linken Schultertasten (R2, L2) für Vorwärts- und Rückwärtsgas und einen der Joysticks zum Lenken. Dieser Modus imitiert den Steuerungsmodus von Autorennspielen.
- **Joystick**: Verwenden Sie einen der Joysticks, um den Roboter zu steuern.
- **Dual**: Verwenden Sie den linken und rechten Joystick, um die linke und rechte Seite des Autos zu steuern. Dies ist eine rohe Differentiallenkung.

Der dritte Button dient zur Auswahl des **Geschwindigkeitsmodus**. Es gibt drei verschiedene Geschwindigkeitsmodi:

- **Langsam**: Die an die Motoren angelegte Spannung ist auf 50% der Eingangsspannung (~6V) begrenzt.
- **Normal**: Die an die Motoren angelegte Spannung ist auf 75% der Eingangsspannung (~9V) begrenzt.
- **Schnell**: Es gibt keine Begrenzung. Die volle Eingangsspannung wird bei Vollgas an die Motoren angelegt (~12V). *Dies ist die Standardeinstellung für das Ausführen der neuronalen Netzwerke.*

Das Fahren mit höheren Geschwindigkeiten verringert die Lebensdauer der Motoren, macht aber mehr Spaß. Die an den Roboter gesendeten Steuerungen werden auf der rechten Seite angezeigt. Bei Verwendung des Gamecontrollers kann der Geschwindigkeitsmodus durch Drücken des rechten Joysticks (R3) erhöht und durch Drücken des linken Joysticks (L3) verringert werden.

#### Datenprotokoll

Es gibt vier verschiedene Protokollierungsmodi:

- **only_sensors**: Alle Sensordaten, aber keine Bilder werden gespeichert.
- **crop_img**: Alle Sensordaten und ein zugeschnittenes Bild, das die Eingangsgröße des Netzwerks hat, werden gespeichert. Dies ist die Standardeinstellung und sollte für die Datenerfassung verwendet werden.
- **preview_img**: Alle Sensordaten und ein Bild in voller Größe werden gespeichert. Dies erfordert viel Speicherplatz und kann langsam sein. Es ist jedoch schön, um FPV-Videos zu erstellen.
- **all_imgs**: Alle Sensordaten sowie zugeschnittene und Bilder in voller Größe werden gespeichert. Dies erfordert viel Speicherplatz und kann langsam sein.

Der Schalter auf der rechten Seite wird verwendet, um die Protokollierung ein- und auszuschalten. Auf dem Gamecontroller kann dieser Schalter mit der X-Taste umgeschaltet werden.

#### Kamera

Der erste Punkt zeigt die Vorschauauflösung. Der zweite Punkt zeigt die Zuschneideauflösung. Dies ist das Bild, das als Eingabe für die neuronalen Netzwerke verwendet wird. Sie werden feststellen, dass sich diese Auflösung je nach ausgewähltem Modell ändert. Wenn Sie Ihren eigenen Autopiloten trainieren, stellen Sie sicher, dass Sie das Modell `AUTOPILOT_F` auswählen. Die Zuschneideauflösung sollte `256x96` anzeigen. Der Schalter auf der rechten Seite wird verwendet, um zwischen der hinteren und der vorderen Kamera umzuschalten.

#### Modell

Es gibt zwei Modelle, die mit der App geliefert werden:

- **MobileNetV1-300**: Dieses Modell wird zur Personenverfolgung verwendet. Es verwendet einen SSD-Objektdetektor mit MobileNet V1-Backbone. Das Modell ist quantisiert, um eine bessere Leistung auf eingebetteten Geräten zu erzielen. Es wird mit der App geliefert.
- **CIL-Mobile**: Dieses Modell wird für die autonome Navigation verwendet. Es wird Steuerungen direkt aus der Kameraeingabe vorhersagen. Es besteht die Möglichkeit, dass es in Ihrer Umgebung nicht funktioniert. Sie sollten unseren Anweisungen folgen, um Ihre eigene [Fahrstrategie](../../policy) zu trainieren und sie zu ersetzen.

Zusätzliche Modelle können vom Modellverwaltungsbildschirm heruntergeladen werden.

Der Schalter auf der rechten Seite wird verwendet, um das Netzwerk ein- und auszuschalten. Wenn das Netzwerk läuft, erzeugt es die Steuerungen für den Roboter und der Gamecontroller ist deaktiviert. Sie können jedoch weiterhin die Tasten auf dem Gamecontroller verwenden, um beispielsweise diesen Schalter mit der R1-Taste umzuschalten und die Kontrolle über den Roboter wiederzuerlangen.

#### Gerät

Verwenden Sie das Dropdown-Menü, um das Gerät auszuwählen, auf dem das neuronale Netzwerk ausgeführt werden soll. Sie haben folgende Auswahlmöglichkeiten:

- **CPU**: Die Verwendung der CPU funktioniert auf den meisten Telefonen und ist die Standardwahl. Sie können die Anzahl der Threads anpassen, um die Leistung zu optimieren.
- **GPU**: Die meisten Smartphones haben eine GPU. Netzwerke mit großen Eingaben wie Bildern laufen oft schneller auf einer GPU.
- **NNAPI**: Dies wird den [TensorFlow Lite NNAPI-Delegaten](https://www.tensorflow.org/lite/performance/nnapi) verwenden. Moderne Smartphones sind oft mit dedizierten KI-Beschleunigern ausgestattet. Die [Neural Network API](https://developer.android.com/ndk/guides/neuralnetworks) (NNAPI) bietet Beschleunigung für TensorFlow Lite-Modelle auf Android-Geräten mit Graphics Processing Unit (GPU), Digital Signal Processor (DSP) und Neural Processing Unit (NPU). Beachten Sie, dass dies auf einigen älteren Telefonen sehr langsam sein kann!

Wenn ein Modell aktiv ist, wird die Inferenzgeschwindigkeit in [ms] neben dem Gerät angezeigt, auf dem das Modell ausgeführt wird.

## Fügen Sie Ihr eigenes Fragment hinzu

Bitte beachten Sie den [Beitragsleitfaden](ContributionGuide.md), um zu erfahren, wie Sie Ihre eigenen Fragmente zur OpenBot-App hinzufügen können.

## Code-Struktur

Die [TensorFlow Lite Object Detection Android Demo](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android) wurde als Ausgangspunkt verwendet, um TFLite-Modelle zu integrieren und den Kamerafeed zu erhalten. Die [DefaultActivity](src/main/java/org/openbot/robot/DefaultActivity.java) führt den Hauptthread aus und erbt von der [CameraActivity](src/main/java/org/openbot/robot/CameraActivity.java), um die Kamera und die Benutzeroberfläche zu verwalten. Der [SensorService](src/main/java/org/openbot/robot/SensorService.java) liest alle anderen Telefonsensoren und protokolliert sie. Der [ServerService](src/main/java/org/openbot/robot/ServerService.java) und der [NsdService](src/main/java/org/openbot/robot/NsdService.java) stellen eine Verbindung zu einem lokalen [Python-Server](../../policy/README.md#web-app) mit einer React-Frontend her. Wenn Sie Daten sammeln, können diese automatisch zur Visualisierung, zum Training von ML-Modellen und zum Herunterladen trainierter Modelle auf den Roboter hochgeladen werden. Der [env](src/main/java/org/openbot/env)-Ordner enthält Dienstprogrammklassen wie die [Vehicle](src/main/java/org/openbot/env/Vehicle.java)-Schnittstelle, die [GameController](src/main/java/org/openbot/env/GameController.java)-Schnittstelle, die [PhoneController](src/main/java/org/openbot/env/PhoneController.java)-Schnittstelle und einen [AudioPlayer](src/main/java/org/openbot/env/AudioPlayer.java) für das akustische Feedback. Der [tflite](src/main/java/org/openbot/tflite)-Ordner enthält die Modelldefinitionen für die [Autopilot](src/main/java/org/openbot/tflite/Autopilot.java)- und [Detector](src/main/java/org/openbot/tflite/Detector.java)-Netzwerke.

## Nächstes (optional)

Trainieren Sie Ihre eigene [Fahrstrategie](../../policy/README.md)
