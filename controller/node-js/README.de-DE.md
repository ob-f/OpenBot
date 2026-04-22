# OpenBot Nodejs Controller

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Nomenklatur

Hier sind einige Begriffe, die wir in diesem Dokument verwenden werden:

* ```Roboter, Bot``` - dies ist die Android-Software, die auf dem Telefon auf dem [OpenBot](https://www.openbot.org/) Fahrzeug läuft.
* ```Server``` - der Node-Server, der Serverteil dieses Projekts
* ```Client, UI``` - dies ist der Clientteil dieses Projekts. Er läuft im Browser.

## Einführung

Dies ist ein auf [Node.js](https://nodejs.org/) basierendes Projekt, das als Controller für das [OpenBot](https://www.openbot.org/) Fahrzeug fungiert. Die Software besteht aus zwei Teilen - einem Server und einem Client. Der Server ist eine Node.js-Anwendung, die auf einem Computer im selben Netzwerk wie der Roboter läuft. Der Clientteil läuft im Browser.

Hier ist ein Screenshot des Browsers:

![Screenshot](images/Screenshot.png "image_tooltip")

## Erste Schritte

Sie können diese Software auf einem PC, einem RaspberryPi-ähnlichen Gerät oder sogar auf [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/) Geräten ausführen, die die ```Node.js``` Umgebung unterstützen. Stellen Sie zunächst sicher, dass Sie [Node.js](https://nodejs.org/), Version 10 oder neuer, installiert haben. Überprüfen Sie die Version:

    node --version

Die Software befindet sich im Verzeichnis ```/controller/node-js``` des OpenBot-Projekts. Nachdem Sie den Code von [github](https://github.com/ob-f/OpenBot) ausgecheckt haben, wechseln Sie in dieses Verzeichnis und führen Sie die folgenden Befehle aus:

    npm install
    npm start

Der letzte Befehl startet den Server. Wenn Sie den Server im Hintergrund ohne Terminal ausführen möchten, können Sie unter ```Linux/MacOS``` Folgendes ausführen:

    npm run start-nohup

oder einfach:

    nohup npm start

Richten Sie Ihren Browser auf die IP-Adresse des Servers auf Port 8081, zum Beispiel [http://localhost:8081](http://localhost:8081) oder [http://192.168.1.100:8081](http://192.168.1.100:8081). Beachten Sie, dass Sie von einem anderen Computer aus auf den Server zugreifen können, aber der Roboter, der Server und der Browser-PC müssen sich im selben Netzwerk befinden. In Zukunft können wir die Möglichkeit hinzufügen, auf den Server aus der Ferne zuzugreifen.

Stellen Sie sicher, dass Ihr Roboter im selben Netzwerk verbunden ist. Gehen Sie in der Android-App des Roboters zum ```Allgemein```-Panel und wählen Sie ```Telefon``` als Controller. Dies verbindet die Android-App mit dem Node-Server, und ein Video wird in der UI angezeigt.

## Wie es funktioniert

1. Der Node-Server erstellt und veröffentlicht einen DNS-Dienst vom Typ ```openbot.tcp``` und einen Namen ```OPEN_BOT_CONTROLLER``` auf Port 19400. Dies ermöglicht es dem Roboter, den Server automatisch zu finden, ohne seine IP-Adresse zu kennen. Der Roboter sucht nach diesem Dienst und stellt eine Socket-Verbindung her, wenn er in den ```Telefon```-Controller-Modus versetzt wird.

2. Der Node-Server erstellt einen HTTP-Server auf Port 8081 und beginnt, Anfragen vom Browser zu bedienen.

3. Zusätzlich erstellt der Node-Server einen WebSocket-Server auf Port 7071. Dieser wird verwendet, um direkt mit dem Browser zu kommunizieren. Zusammengefasst hat der Server bisher zwei Socket-Verbindungen erstellt, eine zum Roboter und eine zum Browser.

4. Der Benutzer gibt Tastaturbefehle vom Browser aus ein. Diese Tastendrücke werden über den WebSocket an den Server gesendet. Der Server wandelt diese in Befehle um, die der Roboter verstehen kann, wie ```{driveCmd: {r:0.4, l:0.34}}``` (eine Liste aller Befehle finden Sie in der Dokumentation für den Android-Controller [hier](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)). Diese Befehle werden über die Socket-Verbindung an den Roboter gesendet.

5. Der Roboter sendet Statusinformationen über die Socket-Verbindung zurück an den Server, und der Server leitet sie an die UI weiter. Die UI kann diese Informationen verwenden, um ihr Erscheinungsbild zu verbessern, wie z.B. das Anzeigen blinkender Indikatoren, aber derzeit wird dieser Status ignoriert.

6. Der Node-Server fungiert auch als WebRTC-Signalisierungsproxy. Er leitet WebRTC-Verhandlungsbefehle zwischen dem Roboter und dem Browser weiter. Er nutzt die offenen Socket-Verbindungen für diesen Zweck, sodass keine zusätzliche Verbindung oder Konfiguration erforderlich ist.

![drawing](images/HowItWorks.png)

## Entwicklung

Dieser Code verwendet [snowpack](https://www.snowpack.dev/) für ein schnelles, leichtgewichtiges Build-Tool.

Wir verwenden [eslint](https://eslint.org/) für Linting und automatisches Formatieren Ihres Codes. Es wird empfohlen, dass Sie Lint ausführen und alle Fehler beheben, bevor Sie neuen Code einreichen. Wenn Sie Visual Code verwenden, können Sie ein Plugin [hier](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint) erhalten. Führen Sie den Linter wie folgt aus:

    npm run lint

## Produktion

Um eine Produktionsversion des ```client``` zu erstellen, führen Sie Folgendes aus:

    npm run build

Dies optimiert den Client-Code in ein ```build``` Verzeichnis, das auf einem Server bereitgestellt werden kann. Zusätzlich müssen wir einen Prozessmanager einrichten, um den Server neu zu starten, und möglicherweise einen Reverse-Proxy wie [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/), was noch nicht erledigt ist.

## Fehlerbehebung

* Manchmal zeigt der Browser nur das Befehlsmenü und nicht den Titel an. Dies bedeutet, dass die WebSocket-Verbindung nicht hergestellt werden konnte. Dies passiert normalerweise direkt nach dem Starten des Servers. Wenn Sie die Browserkonsole untersuchen, finden Sie eine Nachricht darüber, dass die Verbindung nicht hergestellt werden konnte, etwa ```WebSocket connection to 'ws://localhost:7071/ws' failed```. Beenden Sie alle Node-Prozesse (pkill -9 node) und starten Sie sie neu. Laden Sie die Seite neu und die Verbindung sollte hergestellt werden.
* Wenn Sie das Telefon nicht mit der App verbinden können, stellen Sie sicher, dass keine andere Instanz dieser Anwendung auf diesem oder einem anderen Computer im selben Netzwerk läuft.

## Bekannte Fehler

Keine.

## Dinge zu tun/auszuprobieren

* Wir müssen untersuchen, ob wir eine Verbindung zum Server aus der Ferne herstellen können und ob WebRTC weiterhin funktioniert. Wir sollten die Firewall-Konfiguration dokumentieren, um dies zu ermöglichen.
* Wir müssen eine ```Produktions```-Konfiguration erstellen, möglicherweise unter Verwendung des [pm2 Prozessmanagers](https://www.npmjs.com/package/pm2) und [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/).