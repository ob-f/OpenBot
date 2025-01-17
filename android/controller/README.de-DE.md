# Controller App

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Diese Android-App dient als `Fernsteuerung` für das [OpenBot](https://www.openbot.org) Fahrzeug. Im Grunde erfüllt sie eine ähnliche Funktion wie ein PS3/4- oder Xbox-Controller, läuft jedoch auf einem anderen Android-Gerät.

## Verbindung

Wenn die Controller-App gestartet wird, versucht sie sofort, eine Verbindung zum Roboter herzustellen. Wir sehen den folgenden Bildschirm:

<p float="left">
  <img src="../../docs/images/controller_pre_connect.png" width="50%" />
</p>

Um den Controller mit dem Roboter zu verbinden, stellen Sie die Steuerungseinstellung der Roboter-App auf den **Phone**-Modus.

<p float="left">
  <img src="../../docs/images/app_controller_settings_1.png" width="25%" />
  <img src="../../docs/images/app_controller_settings_2.png" width="25%" />
</p>

Sie können auch vom `FreeRoamFragment` aus eine Verbindung zum Controller herstellen, indem Sie das Telefon als Controller auswählen:

<p float="left">
  <img src="../../docs/images/free-roam-fragment-selection.png" width="50%" />
</p>

Nach ein paar Sekunden hören Sie einen Piepton, und der Controller wechselt seinen Bildschirm zu:

<p float="left">
  <img src="../../docs/images/controller_command_buttons.png" width="50%" />
</p>

Hier können Sie auswählen, ob Sie den Roboter durch Neigen des Telefons oder durch die Verwendung der Bildschirmsteuerung fahren möchten.

***Hinweis:*** Dies sollte ausreichen, um eine Verbindung herzustellen, aber wenn die Verbindung nach 30 Sekunden nicht hergestellt werden kann, schalten Sie die `Control`-Einstellung in der Roboter-App auf `Gamepad` und dann wieder auf `Phone`, um die Verbindung erneut zu initiieren. Wenn das fehlschlägt, beenden Sie die Controller-App und starten Sie sie erneut. Schalten Sie den Steuerungsmodus erneut in der Roboter-App um.

## Bedienung

### Bildschirmsteuerung
Dieser Modus ermöglicht es dem Benutzer, das Roboterauto über zwei Schieberegler im `Dual Drive`-Modus zu steuern. Sie können nach links/rechts lenken, indem Sie den Schieberegler auf jeder Seite nach oben und unten bewegen. Die Räder auf jeder Seite drehen sich vorwärts/rückwärts, wenn Sie den Daumen über/unter die Mitte des Schiebereglers bewegen.

<p float="left">
  <img src="../../docs/images/controller_main_screen.png" width="50%" />
</p>

Sie können auch die Blinker links/rechts setzen 
<img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/> 
<img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/> 
indem Sie auf die Pfeile oben links auf dem Bildschirm klicken und den roten Knopf dazwischen, um abzubrechen.

### Neigen zum Fahren
Der Controller kann auch seinen Beschleunigungssensor verwenden, um den Roboter zu steuern. Wenn Sie diese Option auswählen, wechselt der Controller in einen Vollbildmodus (Zen-Modus), in dem nur das Video und die `Bremse` und `Gaspedal` angezeigt werden. Um diesen Modus zu verlassen, doppeltippen Sie auf den Bildschirm.

Hier ist ein Bild des `Neigemodus`-Bildschirms:

<p float="left">
  <img src="../../docs/images/tilt-mode-controller.jpg" width="50%" />
</p>

Verwenden Sie die `Gaspedal`- und `Bremse`-Tasten, um vorwärts/rückwärts zu fahren.

- Durch Drücken des `Gaspedals` beschleunigt der Roboter innerhalb von 2 Sekunden auf volle Geschwindigkeit. Wenn Sie die Taste loslassen, verlangsamt der Roboter bis zum Stillstand (Stoppgeschwindigkeit auf 0% der Höchstgeschwindigkeit eingestellt, kann angepasst werden).
- Durch Drücken der `Bremse`-Taste stoppt der Roboter sofort. Wenn wir die Bremse eine weitere Sekunde halten, beginnt der Roboter rückwärts zu fahren, bis er innerhalb einer Sekunde die maximale Rückwärtsgeschwindigkeit erreicht. Wenn wir die Bremse loslassen, kommt der Roboter zum Stillstand.
- Wir lenken den Roboter, indem wir den Controller nach links oder rechts neigen.

## Zukünftige Entwicklung

Einige der Funktionen, die wir hinzufügen möchten, sind:

- Hinzufügen von Informationen auf dem Controller für weitere Roboter-Sensoren, wie z.B. Batteriestand und Geschwindigkeit.
- Video-Stream von der Kamera des Roboters zum Controller
- Verwendung des Gyroskopsensors des Controllers zur Steuerung des Roboters
- Senden von Crash- und Stoßereignissen vom Roboter zum Controller für ein realistischeres Erlebnis

Hier ist eine [Technische Übersicht](../../docs/technical/OpenBotController.pdf) der Controller-App.
