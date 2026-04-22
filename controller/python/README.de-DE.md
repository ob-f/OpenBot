# Python Controller

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Dieses Python-Programm ermöglicht es Ihnen, den Roboter von einer (drahtlosen) Tastatur aus zu steuern und einen Videostream von der Kamera zu empfangen. Das Programm kann auf jedem Computer ausgeführt werden, der mit demselben Netzwerk wie das Telefon des Roboters verbunden ist. Es wurde auf einem Raspberry Pi 3 und einem MacBook entwickelt und getestet. Bevor Sie die folgenden Schritte ausführen, stellen Sie sicher, dass Sie den [Quellcode](https://github.com/ob-f/OpenBot#get-the-source-code) haben und navigieren Sie zum Ordner `controller`.

## Abhängigkeiten

Wir empfehlen, eine Conda-Umgebung für OpenBot zu erstellen (falls noch nicht geschehen). Anweisungen zur Installation von Conda finden Sie [hier](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). Sie können eine neue Umgebung mit dem folgenden Befehl erstellen:

```bash
conda create -n openbot python=3.7
```

Wenn Sie die Abhängigkeiten nicht global installieren möchten, aktivieren Sie zuerst Ihre Conda-Umgebung:

```bash
conda activate openbot
```

Stellen Sie sicher, dass Sie sich im Ordner `controller` innerhalb Ihres lokalen OpenBot-Repositorys befinden. Jetzt können Sie alle Abhängigkeiten mit dem folgenden Befehl installieren:

```bash
pip install -r requirements.txt
```

## Den Roboter steuern

HINWEIS: Nach einer erfolgreichen Verbindung ist es möglicherweise nicht möglich, erneut eine Verbindung herzustellen, es sei denn, die Roboter-App wird neu gestartet.

Die Python-Skripte warten auf eine eingehende Verbindung. Gehen Sie auf dem Telefon mit der Roboter-App zum FreeRoam-Fragment und schalten Sie den Steuerungsmodus auf das Telefonsymbol um. Der Roboter wird nun versuchen, eine Verbindung zum Python-Skript herzustellen (genauso wie er eine Verbindung zur Controller-App herstellen würde). Alternativ können Sie auch die DefaultActivity verwenden und `Phone` als Controller auswählen.

### Verwendung von Pygame

Diese Skripte ermöglichen es Ihnen, den Roboter mit der Tastatur ähnlich wie in einem Autorennspiel zu steuern.

Führen Sie den Controller ohne Video aus:

`python keyboard-pygame.py`

Führen Sie den Controller mit Video aus:

`python keyboard-pygame.py --video`

Hier ist die Verwendung:

```
    W:        Vorwärts fahren
    S:        Rückwärts fahren
    A:        Leicht nach links drehen (während der Fahrt)
    D:        Leicht nach rechts drehen (während der Fahrt)
    Q:        Nach links drehen
    E:        Nach rechts drehen

    M:        Fahrmodus
    N:        Rauschen umschalten
    Links:    Linker Blinker
    Rechts:   Rechter Blinker
    Oben:     Blinker abbrechen
    Unten:    Netzwerkmodus
    LEERTASTE: Protokollierung umschalten
    ESC:      Beenden
```

### Verwendung von Click

Es gibt auch ein Skript zum Prototyping, das es ermöglicht, die Robotersteuerung in Schritten einzustellen, anstatt sie dynamisch zu steuern. Dieses Skript verwendet die Click-Bibliothek und erfordert, dass das Terminal im Fokus bleibt.

Führen Sie den Controller aus:

`python keyboard-click.py`

Hier ist die Verwendung:

```bash
    W:        Geschwindigkeit erhöhen
    S:        Geschwindigkeit verringern
    A:        Mehr nach links drehen
    D:        Mehr nach rechts drehen
    R:        Steuerung zurücksetzen

    M:        Fahrmodus
    N:        Rauschen umschalten
    Links:    Linker Blinker
    Rechts:   Rechter Blinker
    Oben:     Blinker abbrechen
    Unten:    Netzwerkmodus
    LEERTASTE: Protokollierung umschalten
    ESC:      Beenden
```