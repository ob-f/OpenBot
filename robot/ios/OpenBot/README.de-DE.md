# Robot iOS App - Beta-Version

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## HAFTUNGSAUSSCHLÜSSE

1. **Sicherheit:** Stellen Sie immer sicher, dass Sie in einer sicheren Umgebung arbeiten. Denken Sie daran, dass Ihr Telefon bei einer Kollision beschädigt werden könnte! Besondere Vorsicht ist geboten, wenn Sie automatisierte Steuerungen verwenden (z.B. Personenverfolgung oder Fahrmodus). Stellen Sie sicher, dass Sie immer einen Game-Controller angeschlossen haben und mit der Tastenbelegung vertraut sind, damit Sie das Fahrzeug jederzeit stoppen können. Nutzung auf eigene Gefahr!
2. **App in Entwicklung:** Die Anwendung befindet sich in der Entwicklung und kann je nach Telefonmodell und Betriebssystemversion abstürzen oder unerwartetes Verhalten zeigen. Testen Sie alle Funktionen ohne angeschlossene Räder. Nutzung auf eigene Gefahr!

3. **Die App wird bald im `App Store` `verfügbar` sein.**

## App-Bildschirme

### Hauptmenü

Die App startet mit einem Menübildschirm, der alle verfügbaren Bildschirme anzeigt. Der Bluetooth-Verbindungsbildschirm kann durch Klicken auf das Bluetooth-Symbol oben rechts geöffnet werden. Der Einstellungsbildschirm kann durch Klicken auf das daneben befindliche Einstellungssymbol geöffnet werden. Durch Klicken auf die anderen Symbole kann der Benutzer auf verschiedene Bildschirme zugreifen, deren Funktionen in den folgenden Abschnitten erklärt werden.

<p align="left">
<img src="../../../docs/images/ios_main_screen.jpg" alt="Hauptmenü" height="400"/>
<img src="../../../docs/images/ios_settings_screen.jpg" alt="Einstellungen" height="400"/>
<img src="../../../docs/images/ios_bluetooth_screen.jpg" alt="Bluetooth" height="400"/>
</p>

#### Bluetooth-Verbindung

Im Gegensatz zur Android-App, die es ermöglicht, das Smartphone über ein USB-Kabel mit der Low-Level-Steuerplatine eines OpenBot zu verbinden, verlässt sich die iOS-App ausschließlich auf eine Bluetooth Low-Energy (BLE) Drahtlosverbindung. Beim Öffnen des Bluetooth-Verbindungsbildschirms in der iOS-Anwendung (durch Klicken auf das Bluetooth-Logo vom Hauptbildschirm oder von jedem Fragment aus) wird eine Liste aller kompatiblen Geräte angezeigt. Die Kompatibilität wird hier durch die Verwendung einer Reihe spezifischer UUIDs gewährleistet, die einem OpenBot-Fahrzeug sowohl auf der [App](https://github.com/3dwesupport/OpenBot/blob/090dcb28206195a7ee45a13b8ded968a8d365abe/ios/OpenBot/OpenBot/Utils/Constants.swift#L57)- als auch auf der [Firmware](https://github.com/3dwesupport/OpenBot/blob/090dcb28206195a7ee45a13b8ded968a8d365abe/firmware/openbot_nano/openbot_nano.ino#L115)-Ebene zugewiesen sind. Sie müssen sicherstellen, dass diese UUIDs übereinstimmen. Das Koppeln eines iOS-Geräts mit einem OpenBot-Fahrzeug erfordert dann lediglich die Auswahl dieses Fahrzeugs aus der Liste und das Drücken der "Verbinden"-Taste. Die Standard-Baudrate für die Verbindung ist auf 115200 eingestellt und kann auf App- und Firmware-Ebene geändert werden.

<p align="left">
<img src="../../../docs/images/ios_ble.gif" alt="BLE-Verbindung" height="400" />
</p>

#### Mehrsprachige Unterstützung

Öffnen Sie den Einstellungsbildschirm über das Zahnrad-Symbol oben rechts und tippen Sie im Abschnitt „Allgemein“ auf `Sprache`, um die Anzeigesprache der App zu wählen. Wählen Sie `Systemstandard`, um die Sprache Ihres iPhones zu verwenden, oder eine der unterstützten Sprachen: English, Deutsch, Español, Français, 中文, 한국어 oder हिन्दी. Die App wendet die neue Sprache sofort an.

<p align="left">
<img src="../../../docs/images/ios_language_screen.jpg" alt="Sprachauswahl" height="400"/>
</p>

#### Web-Signalisierungsserver

Geben Sie im Einstellungsbildschirm im Feld `Web-Signalisierungsserver` die WebSocket-Adresse des Signalisierungsservers ein, zum Beispiel `ws://<server-ip>:8080/ws`. Sie wird verwendet, wenn der Steuerungsmodus auf Webserver eingestellt ist. iPhone und Server müssen sich dafür im selben WLAN befinden. Die Einrichtung ist im [Webserver](../../../controller/web-server/README.md) beschrieben.

<p align="left">
<img src="../../../docs/images/ios_web_signaling_screen.jpg" alt="Web-Signalisierungsserver" height="400"/>
</p>

### Freies Fahren

Freies Fahren bietet einfache Robotersteuerung mit Echtzeit-Updates und Informationen über Batterie, Geschwindigkeit und Abstand zu Oberflächen. Es bietet auch Steuerungen im Zusammenhang mit Controller, Fahrmodus und Geschwindigkeit.

<p align="left">
<img src="../../../docs/images/ios_free_roam_screen.jpg" alt="Freies Fahren" width="50%" />
</p>

- **Batterie**: Das Batteriesymbol zeigt die Echtzeit-Batteriestände des verbundenen Roboters an.

- **Fahrmodus**: Es gibt 3 Fahrmodi, die auf der Ansicht angezeigt werden:

    - D -> Fahren, wenn der Roboter vorwärts fährt

    - N -> Neutral, wenn der Roboter stillsteht

    - R -> Rückwärts, wenn der Roboter rückwärts fährt

- **Geschwindigkeit**: Der Tachometer zeigt die Echtzeit-Geschwindigkeit des Roboters an.

- **Sonar**: Die Sonaransicht zeigt den Abstand des Roboters zu einem entgegenkommenden Objekt in cm.

- **Bluetooth**: Zeigt den Status der Bluetooth-Verbindung mit dem Mikrocontroller an. Durch Tippen auf das Symbol kann der Benutzer auch zum Bluetooth-Bildschirm weitergeleitet werden, um die Verbindung anzuzeigen/zu ändern.

#### Steuerung

Die erste Taste dient zur Auswahl des **Steuermodus**. Es gibt zwei verschiedene Steuermodi:

- **Gamepad**: Die App empfängt Steuerungen von einem verbundenen BT-Controller.
- **Telefon (Demnächst)**: Der Roboter kann über ein anderes Smartphone mit installierter Controller-App oder über ein Python-Skript gesteuert werden, das auf einem Computer im selben Netzwerk läuft.

Die zweite Taste dient zur Auswahl des **Fahrmodus**. Es gibt drei verschiedene Fahrmodi bei Verwendung eines Game-Controllers (z.B. PS4):

- **Spiel**: Verwenden Sie die rechten und linken Schultertasten (R2, L2) für Vorwärts- und Rückwärtsschub und einen der Joysticks zum Lenken. Dieser Modus imitiert den Steuerungsmodus von Autorennspielen.
- **Joystick**: Verwenden Sie einen der Joysticks, um den Roboter zu steuern.
- **Dual**: Verwenden Sie den linken und rechten Joystick, um die linke und rechte Seite des Autos zu steuern. Dies ist eine rohe Differentiallenkung.

Die dritte Taste dient zur Auswahl des **Geschwindigkeitsmodus**. Es gibt drei verschiedene Geschwindigkeitsmodi:

- **Langsam**: Die Spannung, die an die Motoren angelegt wird, ist auf 50% der Eingangsspannung (~6V) begrenzt.
- **Normal**: Die Spannung, die an die Motoren angelegt wird, ist auf 75% der Eingangsspannung (~9V) begrenzt.
- **Schnell**: Es gibt keine Begrenzung. Die volle Eingangsspannung wird bei Vollgas an die Motoren angelegt (~12V). *Dies ist die Standardeinstellung für den Betrieb der neuronalen Netze.*

Das Fahren mit höheren Geschwindigkeiten verkürzt die Lebensdauer der Motoren, macht aber mehr Spaß. Die Steuerungen, die an den Roboter gesendet werden, werden auf der rechten Seite angezeigt. Bei Verwendung des Game-Controllers kann der Geschwindigkeitsmodus durch Drücken des rechten Joysticks (R3) erhöht und durch Drücken des linken Joysticks (L3) verringert werden.

### Datenerfassung

Einfache Benutzeroberfläche zur Sammlung von Datensätzen.

<p align="left">
<img src="../../../docs/images/ios_data_collection_screen.jpg" alt="Datenerfassung" width="50%" />
</p>

- **Vorschauauflösung**: Wird verwendet, um zwischen den Auflösungen der Kameravorschau zu wechseln. Es gibt 3 Einstellungen:

    - ***HOCH*** (1920x1080p)

    - ***MITTEL*** (1280x720p)

    - ***NIEDRIG*** (640x360)

- **Modellauflösung**: Wird verwendet, um zwischen den Auflösungen der für das Training verschiedener Modelle gespeicherten Bilder zu wechseln.

- **Server**: Die Server-Funktion ist noch in Entwicklung.

- **Gesammelte Daten protokollieren**: Der Datenerfassungsprozess kann vom Bildschirm oder aus der Ferne, z.B. von einem Bluetooth-Controller, gesteuert werden. Bei Verwendung eines Bluetooth-Controllers können Sie:

    - die **A-Taste** drücken, um den Datenerfassungsprozess zu **starten**

    - die **A-Taste erneut drücken**, um die Datenerfassung zu **stoppen** und die gesammelten Daten in einer .zip-Datei zu speichern

    - alternativ die **R1-Taste** drücken, um die Datenerfassung **ohne Speichern** der gesammelten Daten zu **stoppen** (z.B. aufgrund einer unerwarteten Kollision mit der Umgebung)

    - denken Sie daran, das Controller-Mapping-Fragment zu verwenden, um sicherzustellen, dass Sie die richtigen Tasten verwenden.

- **Fahrzeugstatus**: Das Feld **Batterie** zeigt die Batteriespannung an, die vom Mikrocontroller über den Spannungsteiler gemessen wird. Das Feld **Geschwindigkeit (l,r)** gibt die linke und rechte Geschwindigkeit der (vorderen) Räder in U/min an. Es wird vom Mikrocontroller über die optischen Raddrehzahlsensoren gemessen. Das Feld **Sonar** zeigt den freien Raum vor dem Auto in Zentimetern an. Es wird vom Mikrocontroller über den Ultraschallsensor gemessen. Beachten Sie, dass Sie Werte erst einige Sekunden nach dem Herstellen der USB-Verbindung erhalten.

- **Sensoren**: Meldet Messungen von Fahrzeugsensoren. Derzeit erfassen wir Messwerte von folgenden Sensoren: Kamera, Gyroskop, Beschleunigungsmesser, Magnetometer, Umgebungslichtsensor und Barometer. Mithilfe der iOS-API können wir die folgenden Sensormesswerte erhalten: RGB-Bilder, Winkelgeschwindigkeit, lineare Beschleunigung, Schwerkraft, Magnetfeldstärke, Lichtintensität, atmosphärischer Druck, Breitengrad, Längengrad, Höhe, Peilung und Geschwindigkeit. Zusätzlich zu den Telefonsensoren erfassen wir Messwerte von Körpersensoren (Raddrehzahl, Hindernisabstand und Batteriespannung), die über die serielle Verbindung übertragen werden. Wir erfassen auch und stempeln Steuersignale, die von einem verbundenen Controller empfangen werden, falls vorhanden. Schließlich integrieren wir mehrere neuronale Netze für die Personenverfolgung und autonome Navigation.

### Controller-Mapping

Einfache Benutzeroberfläche zur Überprüfung der Tasten- und Joystick-Belegung eines verbundenen BT-Controllers.

<p align="left">
<img src="../../../docs/images/ios_controller_mapping.jpg" alt="Controller-Mapping" width="30%" />
</p>

### Roboter-Info

Einfache Benutzeroberfläche, um Roboterinformationen zu erhalten und grundlegende Funktionen zu testen. Der **Robotertyp**, wie in der Firmware konfiguriert, wird als Text und Animation angezeigt. Die Häkchen in den Abschnitten **Sensoren**, **Raddrehzahl** und **LEDs** zeigen, welche Funktionen vom verbundenen Roboter unterstützt werden. Der Abschnitt **Messwerte** liefert die wichtigsten Sensormessungen. Im Abschnitt **Befehle senden** können Benutzer grundlegende Motorbefehle durch Drücken der entsprechenden Tasten senden und die vorderen und hinteren LEDs mit einem Schieberegler steuern.

<p align="left">
<img src="../../../docs/images/ios_screen_robot_info.gif" alt="Roboter-Info" width="50%" />
</p>

### Autopilot

Einfache Benutzeroberfläche zum Ausführen von Autopilot-Modellen.

<p align="left">
<img src="../../../docs/images/ios_autopilot_screen.jpg" alt="Autopilot" width="50%" />
</p>

- **Server**: Wenn die [Web-App](../../../policy/README.de-DE.md#web-app) für das Training läuft, können Sie sie hier auswählen und trainierte Autopilot-Modelle an den Roboter senden.
- **Modell**: Wählen Sie ein trainiertes Modell für den Autopilot-Modus.
- **Gerät**: CPU, GPU oder NNAPI für die Inferenz (mehr dazu [hier](../../android/app/README.de-DE.md#gerät)).
- **Threads**: Anzahl der Threads (wirkt sich nur aus, wenn als Gerät die CPU gewählt ist).
- **Steuerung**: Controller, Fahrmodus und Geschwindigkeit stellen den Roboter ein, wie im [Abschnitt Steuerung](#steuerung) beschrieben.

### Objektverfolgung

Einfache Benutzeroberfläche zur Verfolgung von Objekten aus 80 verschiedenen Klassen. Eine kurze Beschreibung der verschiedenen KI-Modelle zur Objektverfolgung und Leistungsbenchmarks finden Sie im Abschnitt [Modellverwaltung](#modellverwaltung).

<p align="left">
<img src="../../../docs/images/ios_object_tracking_screen.jpg" alt="Objektverfolgung" width="50%" />
</p>

- **Dynamische Geschwindigkeit**: verringert im „Auto-Modus“ die Geschwindigkeit, je näher der Roboter dem verfolgten Objekt kommt.
  Die Geschwindigkeit richtet sich nach der Fläche des Begrenzungsrahmens (funktioniert am besten im Querformat).
- **Modell**: Wählen Sie einen Objektdetektor passend zur Leistung Ihres Telefons.
- **Objekt**: Wählen Sie das Objekt, das Sie verfolgen möchten. Die Modelle erkennen die 80 COCO-[Objektklassen](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/).
- **Konfidenz**: Schwellwert, ab dem Erkennungen akzeptiert werden. Erhöhen Sie ihn bei Fehlerkennungen, verringern Sie ihn, wenn das gesuchte Objekt nicht erkannt wird.
- **Gerät**: CPU, GPU oder NNAPI für die Inferenz (mehr dazu [hier](../../android/app/README.de-DE.md#gerät)).
- **Threads**: Anzahl der Threads (wirkt sich nur aus, wenn als Gerät die CPU gewählt ist).
- **Steuerung**: Controller, Fahrmodus und Geschwindigkeit stellen den Roboter ein, wie im [Abschnitt Steuerung](#steuerung) beschrieben.

### Modellverwaltung

Alle Modelle sind quantisiert, um eine bessere Leistung auf eingebetteten Geräten zu erzielen. Beachten Sie, dass Modelle mit größerer Eingangsauflösung trotz niedrigerer mAP besser für kleinere Objekte geeignet sein können.

<p align="left">
<img src="../../../docs/images/ios_screen_model_management.gif" alt="Modellverwaltung" width="25%" />
</p>

### Navigation

Auf dem Bildschirm `Point Goal Navigation` legen Sie ein Ziel fest, zu dem der Roboter fährt, angegeben mit Werten für vorwärts und links im 3D-Raum. Tippen Sie dazu in der App auf die entsprechende Schaltfläche.

Danach erscheint ein Pop-up mit zwei Eingabefeldern, `Forward` und `Left`. Geben Sie dort die Werte für das Ziel ein. Das Pop-up hat außerdem zwei Schaltflächen: `Cancel` und `Start`.

- Schaltfläche `Cancel`: bringt Sie zurück zum Startbildschirm und bricht die Navigation ab.


- Schaltfläche `Start`: legt aus den eingegebenen Werten für vorwärts und links einen Punkt im 3D-Raum fest und startet die Navigation. Der Roboter fährt auf das Ziel zu und passt Richtung und Geschwindigkeit an, um es zu erreichen.

Erreicht der Roboter das Ziel, erscheint eine Meldung, und der Roboter hält an.

Diese Funktion nutzt `ARKit`, das Augmented-Reality-Framework von Apple. Sie brauchen dafür ein ARKit-fähiges Gerät.

<p align="left">
<img src="../../../docs/images/ios_set_navigation_screen.jpg" alt="Model Management" width="25%" />
<img src="../../../docs/images/ios_navigation_screen.jpg" alt="Model Management" width="25%" />
</p>


### Projekte

Der Bildschirm Projekte zeigt Ihre Projekte aus dem OpenBot Playground, wenn Sie mit Ihrem Google-Konto angemeldet sind. Sie können diese Projekte ausführen, um Ihren OpenBot zu steuern, oder ihre QR-Codes über das Scanner-Symbol oben rechts scannen. Wenn Sie nicht angemeldet sind, zeigt der Bildschirm eine Schaltfläche für die Google-Anmeldung, Sie können den QR-Code Ihres Projekts aber trotzdem scannen. Erscheint nach der Anmeldung `Oops, no project found`, prüfen Sie, ob das Konto Projekte in Google Drive gespeichert hat.

Wenn Ihre neuesten Projekte nicht in der Liste stehen, ziehen Sie den Bildschirm nach unten, um sie neu zu laden.

<p align="left">
<img src="../../../docs/images/projects_tab_screen_ios.gif" alt="Project Screen" width="25%"/>
<img src="../../../docs/images/no_projects_found_ios.jpg" alt="No project screen" style="padding-left: 0.3%" width="26.5%"/>
<img src="../../../docs/images/reload_projects_ios.gif" alt="Reload project screen" width="25%"/>
</p>

- **Google-Drive-Projekte**: Um ein Projekt aus Google Drive auszuführen, tippen Sie darauf und warten Sie, bis die Projektdatei gelesen ist. Klappt das ohne Fehler, erscheint ein Pop-up mit dem Namen des Projekts und den Schaltflächen `Start` und `Cancel`. Mit Start führen Sie das Projekt aus, mit Cancel brechen Sie ab. Erscheint die Meldung `Something went wrong`, gibt es vermutlich ein Problem mit der Datei in Google Drive. Laden Sie den Bildschirm dann durch Herunterziehen neu und versuchen Sie es noch einmal.


- **QR-Code-Scanner**: Um den QR-Code eines Playground-Projekts zu scannen, tippen Sie auf das QR-Code-Symbol oben rechts. Erlauben Sie der App den Zugriff auf die Kamera. Nach dem Scannen wird die Datei gelesen. Klappt das ohne Fehler, erscheint ein Pop-up mit dem Namen des Projekts und den Schaltflächen `Start` und `Cancel`. Mit Start führen Sie das Projekt aus, mit Cancel brechen Sie ab. Erscheint die Meldung `Something went wrong`, gibt es vermutlich ein Problem mit der Datei in Google Drive. Erzeugen Sie dann im Playground einen neuen QR-Code und versuchen Sie es noch einmal.


- **Projekt ausführen**: Läuft Ihr Projekt erfolgreich, zeigt der Bildschirm die Namen der Code-Blöcke und eine Stopp-Schaltfläche, mit der Sie die Ausführung beenden können.


- **Projekt löschen**: Um ein Projekt zu löschen, halten Sie es lange gedrückt. Bestätigen Sie die Abfrage mit „Yes“.

<p align="left">
<img src="../../../docs/images/google_drive_projects_execute_ios.gif" alt="Google Drive project execute" width="25%" />
<img src="../../../docs/images/ios_qr_scan.gif" alt="Qr code scanner project execute" width="25%" />
<img src="../../../docs/images/delete_project_ios.jpg" alt="Delete project popup" style="padding-right: 1.2%" width="26%" />
</p>


### Profil
Der Bildschirm Profil bietet verschiedene Optionen, je nachdem, ob Sie angemeldet sind.
Wenn Sie nicht angemeldet sind, erscheint eine Schaltfläche `Google Sign-in`, mit der Sie sich mit Ihrem Google-Konto anmelden. Danach haben Sie Zugriff auf Ihr Profil und die anderen Funktionen.
Wenn Sie angemeldet sind, zeigt der Tab `Profile` zwei Schaltflächen: `Edit Profile` und `Logout`.

<p align="left">
<img src="../../../docs/images/logged_out_profile_screen_ios.jpg" alt="Logged out profile screen" width="25%"/>
<img src="../../../docs/images/logged_in_profile_screen_ios.jpg" alt="Logged in profile screen" width="25%"/>
</p>

- **Edit Profile**: öffnet einen Bildschirm, auf dem Sie Ihre Profildaten ändern, etwa Ihren Namen und Ihr Profilbild.


- **Logout**: meldet Sie von Ihrem Konto ab und bringt Sie zurück zum Anmeldebildschirm.

<p align="left">
<img src="../../../docs/images/ios_edit_profile_screen.jpg" alt="Edit profile screen" width="25%"/>
<img src="../../../docs/images/logout_dialog_box_ios.jpg" alt="Logout dialog box" width="25%"/>
</p>


### OpenBot PlayGround

Die Dienste des OpenBot Playground erreichen Sie über das OpenBot-Playground-Symbol oben in der Werkzeugleiste des Bildschirms Projekte. Mehr über den OpenBot Playground erfahren Sie [hier](../../../open-code/README.de-DE.md).

<p align="left">
<img src="../../../docs/images/playground_services_ios.gif" alt="Playground Services" width="25%" />
<img src="../../../docs/images/playground_sign-in_ios.gif" alt="Playground sign-in" width="25%" />
</p>

## Code-Struktur

Die [TensorFlow Lite Object Detection iOS Demo](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/ios) wurde als Ausgangspunkt verwendet, um TFLite-Modelle zu integrieren und den Kamerafeed zu erhalten. Der [tflite](OpenBot/tflite)-Ordner enthält die Modelldefinitionen für die [Autopilot](OpenBot/tflite/Autopilot.swift)- und [Detektor](OpenBot/tflite/Detector.swift)-Netzwerke.

## Nächstes (optional)

Trainieren Sie Ihre eigene [Fahrstrategie](../../../policy/README.md)

Firebase-Authentifizierung [Firebase](../OpenBot/OpenBot/Authentication/README.de-DE.md)
