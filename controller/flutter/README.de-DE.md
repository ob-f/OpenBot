# Flutter Controller App

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Diese Controller-App dient als `Fernbedienung` für das [OpenBot](https://www.openbot.org) Fahrzeug, ähnlich wie ein BT-Controller (z.B. PS3/4 oder Xbox). Sie läuft auf einem anderen Android/iOS-Gerät und unterstützt neben der Steuerung auch Live-Video-/Audio-Streaming.

## App installieren

Laden Sie unter Android `Controller-<version>.apk` aus den Assets des neuesten [Release](https://github.com/ob-f/OpenBot/releases) herunter und [installieren](https://www.lifewire.com/install-apk-on-android-4177185) Sie die Datei auf Ihrem Telefon.

> **Hinweis für bestehende Nutzer:** Diese App ersetzt die alte Android-Controller-App als Controller-APK, die mit jedem Release veröffentlicht wird. Da die beiden Apps unterschiedliche Paket-IDs verwenden (`org.openbot.app.controller` bzw. `org.openbot.controller`), wird die alte App durch die Installation dieser APK nicht aktualisiert; die neue App wird stattdessen als separate App daneben installiert. Sobald Sie umgestiegen sind, können Sie den alten Controller deinstallieren.

## Erste Schritte
Beginnen Sie mit der Installation von [Flutter](https://flutter.dev/) auf Ihrem System. Wählen Sie den passenden Download für Ihr Betriebssystem, einschließlich Optionen für Windows, macOS, Linux und ChromeOS. Folgen Sie der offiziellen Flutter-Installationsanleitung für detaillierte Anweisungen: [Flutter Installationsanleitung](https://docs.flutter.dev/get-started/install)

### Verwendung des Terminals
- Sobald Flutter erfolgreich installiert ist, öffnen Sie Ihr **Terminal** oder **Eingabeaufforderung**.
- Wechseln Sie in das Verzeichnis, in dem das OpenBot-Projekt gespeichert ist, und navigieren Sie dann zu `OpenBot/controller/flutter`.
- Verwenden Sie die folgenden Befehle, um die Flutter-Anwendung vom Terminal auszuführen.

  #### Abhängigkeiten installieren:
    ```bash
     flutter pub get 
    ```
    Projekt ausführen:
    ```bash
     flutter run
    ```
    Wenn Sie auf Probleme stoßen, führen Sie den folgenden Befehl aus:
    ```bash
     flutter doctor
    ```
### Verwendung eines Editors
- Folgen Sie der offiziellen Flutter-Anleitung zur Einrichtung eines Editors: [Editor einrichten](https://docs.flutter.dev/tools/android-studio)
- Stellen Sie sicher, dass Ihr Editor für die Flutter-Entwicklung konfiguriert ist. Installieren Sie alle erforderlichen Plugins oder Erweiterungen und folgen Sie den editor-spezifischen Anweisungen in der Flutter-Dokumentation für die beste Entwicklungserfahrung.

- Sobald Sie Ihr Projekt nach der Einrichtung im Editor öffnen, wird es wie im folgenden Bild angezeigt.

  <p float="left">
    <img src="../../docs/images/android_editor.jpg" width="50%" />
  </p>

- Folgen Sie den Anweisungen ähnlich wie oben beschrieben, um Flutter im Terminal auszuführen, und führen Sie es direkt mit der ``run``-Schaltfläche für zukünftige Wiederholungen aus.

  <p float="left">
    <img src="../../docs/images/run_editor.jpg" width="50%" />
  </p>

## Signierte Release-Version bauen (Android)
Release-Builds (`flutter build apk --release` / `flutter build appbundle --release`) werden mit einem Keystore signiert, auf den `android/key.properties` verweist. Diese Datei ist **in .gitignore** und wird nie committet – jeder Entwickler muss seine eigene lokale Kopie anlegen. Debug-Builds (`flutter build apk --debug`, `flutter run`) brauchen nichts davon.

### 1. Keystore erzeugen (einmalig)
```bash
keytool -genkey -v -keystore ~/release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
```
Bewahren Sie die erzeugte `.keystore`/`.jks`-Datei und ihre Passwörter sicher außerhalb des Repositorys auf – wenn Sie sie verlieren, können Sie keine Updates mit derselben App-Signatur mehr veröffentlichen.

### 2. `android/key.properties` anlegen
`android/app/build.gradle` liest diese Datei unter `controller/flutter/android/key.properties`, legen Sie sie also dort an mit:
```properties
storePassword=<your keystore password>
keyPassword=<your key password>
keyAlias=upload
storeFile=/absolute/path/to/release.keystore
```
`storeFile` muss ein absoluter Pfad (oder ein von `controller/flutter/android/app/` aus auflösbarer Pfad) zu dem in Schritt 1 erzeugten Keystore sein.

### 3. Bauen
```bash
cd controller/flutter
flutter build appbundle --release   # für den Upload in den Play Store (.aab)
# oder
flutter build apk --release         # für eine signierte, installierbare APK
```
`app/build.gradle` lädt `key.properties` automatisch, wenn die Datei vorhanden ist, und verwendet sie für die `release`-Signierung; fehlt die Datei, hat die Release-Konfiguration keine Signaturwerte und das Signieren schlägt fehl (Debug-Builds sind nicht betroffen).

## Verbindung

Controller- und Roboter-Smartphone müssen sich im selben WLAN befinden, oder ein Smartphone muss mit dem Hotspot des anderen verbunden sein. Ein Internetzugang ist nicht erforderlich.

Wenn die Controller-App gestartet wird, versucht sie sofort, eine Verbindung zum Roboter herzustellen und zeigt den folgenden Bildschirm an:

<p float="left">
  <img src="../../docs/images/flutter_controller_home.jpg" width="50%" />
</p>

Um den Controller mit dem Roboter zu verbinden, stellen Sie den Steuerungsmodus des Roboters auf **Phone**.
Zum Beispiel wird im `FreeRoamFragment` der Telefonmodus wie folgt aktiviert:

<p float="left">
  <img src="../../docs/images/phone_selection.gif" width="50%" />
</p>

Sobald die Verbindung hergestellt ist, sieht die Controller-App so aus:

<p float="left">
  <img src="../../docs/images/flutter_controller_connected.jpg" width="50%" />
</p>

Hier können Sie auswählen, ob Sie den Roboter durch Kippen des Telefons oder durch die Verwendung der Bildschirmsteuerung fahren möchten.

***Hinweis:*** Dies sollte ausreichen, um eine Verbindung herzustellen, aber wenn die Verbindung nach 30 Sekunden nicht hergestellt werden kann, schalten Sie die `Control`-Einstellung in der Bot-App auf `Gamepad` und dann wieder auf `Phone`, um die Verbindung erneut zu initiieren. Wenn das fehlschlägt, beenden Sie die Controller-App und starten Sie sie erneut. Schalten Sie den Steuerungsmodus erneut in der Roboter-App um.

## Betrieb

### Bildschirmsteuerung

Dieser Modus ermöglicht es dem Benutzer, das Roboterauto über zwei Schieberegler im `Dual Drive`-Modus zu steuern. Sie können nach links/rechts abbiegen, indem Sie den Schieberegler-Daumen auf jeder Seite nach oben und unten bewegen. Die Räder auf jeder Seite drehen sich vorwärts/rückwärts, wenn der Daumen über/unter die Mitte des Schiebereglers bewegt wird.

<p float="left">
  <img src="../../docs/images/flutter_controller_dual_drive_mode.jpg" width="50%" />
</p>

- ``Blinker``: Sie können auch die linken/rechten Blinker <img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/> <img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/> setzen, indem Sie auf die Pfeile oben links auf dem Bildschirm klicken.

- ``Kamera wechseln``: zwischen Front- und Rückkamera-Modus wechseln.
- ``Stummschalten``: Audioübertragung ein-/ausschalten.
- ``Spiegelansicht``: das Videobild spiegeln.

### Kippen zum Fahren

Der Controller kann auch seinen Beschleunigungssensor verwenden, um den Roboter zu steuern. Wenn Sie diese Option auswählen, wechselt der Controller in einen Vollbildmodus (Zen-Modus), in dem nur das Video und die `Bremse`- und `Gaspedal`-Tasten angezeigt werden. Um diesen Modus zu verlassen, doppeltippen Sie auf den Bildschirm.

Hier ist ein Bild des `Kippmodus`-Bildschirms:

<p float="left">
  <img src="../../docs/images/flutter_controller_tilt_mode.jpg" width="50%" />
</p>

Verwenden Sie die `Gaspedal`- und `Bremse`-Tasten, um vorwärts/rückwärts zu fahren.

- Durch Drücken des `Gaspedals` beschleunigt der Roboter innerhalb von 2 Sekunden auf volle Geschwindigkeit. Wenn Sie die Taste loslassen, verlangsamt der Roboter bis zum Stillstand (Stoppgeschwindigkeit auf 0% der maximalen Geschwindigkeit eingestellt, kann angepasst werden).
- Durch Drücken der `Bremse`-Taste wird der Roboter sofort gestoppt. Wenn wir die Bremse eine weitere Sekunde gedrückt halten, beginnt der Roboter rückwärts zu fahren, bis er innerhalb einer Sekunde die maximale Rückwärtsgeschwindigkeit erreicht. Wenn wir die Bremse loslassen, kommt der Roboter zum Stillstand.
- Der Roboter wird durch Kippen des Controller-Telefons nach links oder rechts gesteuert.

### Controller-Einstellungen

Hier ist ein Bild des Bildschirms `Controller Settings`:

- Controller-Auswahl: Wechseln Sie mit den Symbolen zwischen verschiedenen Controller-Typen (z. B. Smartphone, Joystick).
 
- Server: Zeigt den aktuellen Serverstatus mit einem Dropdown-Menü, um den Server auszuwählen oder zu wechseln.
 
- Rauschen: Schalten Sie die Rauschsimulation mit dem Schalter ein oder aus.

<p float="left">
  <img src="../../docs/images/flutter_controller_setting.jpg" width="50%" />
</p>

Hier ist eine [Technische Übersicht](../../docs/technical/OpenBotController.pdf) der Controller-App.
