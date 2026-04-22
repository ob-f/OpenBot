# Fahrstrategie (Fortgeschritten)

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## HAFTUNGSAUSSCHLÜSSE

1. **Sicherheit:** Fahrstrategien sind nicht perfekt und können den Roboter zum Absturz bringen. Stellen Sie immer sicher, dass Sie in einer sicheren Umgebung arbeiten! Bedenken Sie, dass Ihr Telefon bei einer Kollision beschädigt werden könnte! Stellen Sie sicher, dass Sie immer einen Gamecontroller angeschlossen haben und mit der Tastenbelegung vertraut sind, damit Sie das Fahrzeug jederzeit stoppen können. Nutzung auf eigenes Risiko!
2. **Rechenhardware:** Das Training einer Fahrstrategie erfordert viele Ressourcen und kann Ihren Computer verlangsamen oder sogar einfrieren lassen. Es wird empfohlen, einen High-End-Laptop oder eine Workstation mit viel RAM und einer dedizierten GPU zu verwenden, insbesondere beim Training mit größeren Batch-Größen. Die Dokumentation ist derzeit auch nicht sehr detailliert. Nutzung auf eigenes Risiko!
3. **Geduld erforderlich:** Um eine gute Fahrstrategie für Ihr benutzerdefiniertes Dataset zu erhalten, ist Geduld erforderlich. Es ist nicht einfach, es beinhaltet Datensammlung, Hyperparameter-Tuning usw. Wenn Sie noch nie maschinelle Lernmodelle trainiert haben, wird es herausfordernd und möglicherweise frustrierend.

Zuerst müssen Sie Ihre Trainingsumgebung einrichten.

## Abhängigkeiten

Wir empfehlen, eine Conda-Umgebung für OpenBot zu erstellen. Anweisungen zur Installation von Conda finden Sie [hier](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). Der einfachste Weg, eine neue Umgebung mit allen Abhängigkeiten zu erstellen, ist die Verwendung einer der bereitgestellten Umgebungsdateien. Unter Windows müssen Sie auch die [Microsoft C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) installieren. Stellen Sie sicher, dass Sie sich im Ordner `policy` innerhalb Ihres lokalen OpenBot-Repositorys befinden. Führen Sie je nach Betriebssystem den entsprechenden Befehl aus:

- **MacOS**: `conda env create -f environment_mac.yml`
- **Windows**: `conda env create -f environment_win.yml`
- **Linux**: `conda env create -f environment_linux.yml`

Für GPU-Unterstützung stellen Sie sicher, dass Sie auch die entsprechenden Treiber installiert haben. Unter Mac und Windows sollte alles sofort funktionieren. Unter Linux können Sie die Treiber mit folgendem Befehl installieren:
```
sudo apt-get install nvidia-driver-510
```
Unter Linux müssen Sie wahrscheinlich auch Folgendes ausführen, um CUDA und cuDNN zu Ihrem Pfad hinzuzufügen:
```
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```

Fertig! Sie sind bereit, Ihre eigenen Modelle zu trainieren. Wenn dies nicht funktioniert, finden Sie unten Anweisungen zum manuellen Einrichten einer solchen Umgebung.

### Manuelles Einrichten der Umgebung

Erstellen Sie zuerst eine neue Conda-Umgebung mit folgendem Befehl:

```bash
conda create -n openbot pip python=3.9 -y
```

Aktivieren Sie als Nächstes Ihre Conda-Umgebung:

```bash
conda activate openbot
```

Wenn dies nicht funktioniert (z.B. unter Windows), müssen Sie die Umgebung möglicherweise mit `activate openbot` aktivieren.

Sobald die Umgebung aktiv ist, müssen Sie TensorFlow installieren. Beachten Sie, dass das Training auf einem Laptop sehr langsam sein wird. Wenn Sie Zugang zu einem Computer mit dedizierter GPU haben, empfehlen wir dringend, diesen zu verwenden, indem Sie die notwendigen Bibliotheken installieren; stellen Sie sicher, dass Sie aktuelle GPU-Treiber installiert haben. Unten sind die Befehle zur Installation von TensorFlow für verschiedene Betriebssysteme.

#### **Mac OS**
```
conda install -c apple tensorflow-deps -y
pip install tensorflow-macos~=2.9.0
```
GPU-Unterstützung
```
pip install tensorflow-metal~=0.5.0
```
[Fehlerbehebung](https://developer.apple.com/metal/tensorflow-plugin/)

#### **Linux**
```
pip install tensorflow~=2.9.0
```
GPU-Unterstützung
```
sudo apt-get install nvidia-driver-510
conda install -c conda-forge cudatoolkit=11.2 cudnn=8.1 -y
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```
[Fehlerbehebung](https://www.tensorflow.org/install/pip#linux)

#### **Windows**
```
pip install tensorflow~=2.9.0
```
GPU-Unterstützung
```
conda install cudatoolkit=11.3 cudnn=8.2 -y
```

#### **Zusätzliche Anforderungen**

Stellen Sie sicher, dass Sie sich im Ordner `policy` innerhalb Ihres lokalen OpenBot-Repositorys befinden. Jetzt können Sie alle verbleibenden Abhängigkeiten mit folgendem Befehl installieren:

```bash
pip install -r requirements.txt
```

Sie können auch pydot (`pip install pydot`) und graphviz ([siehe Anweisungen](https://graphviz.gitlab.io/download/)) installieren, wenn Sie die Netzwerkarchitektur visualisieren möchten.

Wenn Sie die [WebApp](#web-app) zur Datensammlung und zum Training verwenden möchten, müssen Sie zusätzlich die folgenden Abhängigkeiten installieren. (Auf dem Mac ist das `brotlipy`-Paket derzeit auf pip defekt, daher müssen Sie es zuerst mit Conda installieren: `conda install brotlipy=0.7`)

```bash
pip install -r requirements_web.txt
```

### Wesentliche Pakete

Zur Referenz und Fehlerbehebung finden Sie unten eine Liste der wesentlichen Pakete.

Training:

- [tensorflow](https://pypi.org/project/tensorflow/)
- [jupyter notebook](https://pypi.org/project/notebook/)
- [matplotlib](https://pypi.org/project/matplotlib/)
- [numpy](https://pypi.org/project/numpy/)
- [PIL](https://pypi.org/project/Pillow/)
- [black[jupyter]](https://pypi.org/project/black/)

Webschnittstelle:

- [aiohttp](https://pypi.org/project/aiohttp/)
- [aiozeroconf](https://pypi.org/project/aiozeroconf/)
- [imageio](https://pypi.org/project/imageio/)

### Hinweise

- Denken Sie daran, die Umgebung zu aktivieren, bevor Sie Befehle im Terminal ausführen: `conda activate openbot`
- Wenn Ihr TensorFlow-Import nicht funktioniert, versuchen Sie, es über `pip install tensorflow --user` zu installieren. (Siehe dieses [Problem](https://github.com/ob-f/OpenBot/issues/98).)

## Datensatz

### Datensammlung

Um eine autonome Fahrstrategie zu trainieren, müssen Sie zuerst einen Datensatz sammeln. Je mehr Daten Sie sammeln, desto besser wird die resultierende Fahrstrategie. Für die Experimente in unserem Papier haben wir etwa 30 Minuten Daten gesammelt. Beachten Sie, dass das Netzwerk Ihr Fahrverhalten nachahmen wird. Je besser und konsistenter Sie fahren, desto besser wird das Netzwerk lernen zu fahren.

1. Verbinden Sie einen Bluetooth-Gamecontroller mit dem Telefon (z.B. PS4-Controller: Um den Pairing-Modus zu aktivieren, drücken Sie die PS- und Share-Tasten, bis die LED schnell blinkt).
2. Wählen Sie das Modell `CIL-Mobile-Cmd` in der App aus.
3. Fahren Sie nun das Auto über einen Gamecontroller und zeichnen Sie einen Datensatz auf. Beim PS4-Controller kann das Logging mit der **X**-Taste umgeschaltet werden.

Sie finden nun einen Ordner namens *Documents/OpenBot* im internen Speicher Ihres Smartphones. Für jede Aufnahme gibt es eine ZIP-Datei. Der Name der ZIP-Datei hat das Format *yyyymmdd_hhmmss.zip*, entsprechend dem Zeitpunkt, zu dem die Aufnahme gestartet wurde.

Das Jupyter-Notebook erwartet einen Ordner namens `dataset` im selben Ordner. In diesem Ordner sollten sich zwei Unterordner befinden, `train_data` und `test_data`. Die Trainingsdaten werden verwendet, um die Fahrstrategie zu erlernen. Die Testdaten werden verwendet, um die gelernte Fahrstrategie auf nicht gesehenen Daten während des Trainingsprozesses zu validieren. Dies gibt einen Hinweis darauf, wie gut diese Strategie auf dem Roboter funktionieren wird. Auch wenn der Roboter die gleiche Route wie während des Trainings fährt, werden die genauen beobachteten Bilder bei jedem Lauf leicht unterschiedlich sein. Die übliche Aufteilung ist 80% Trainingsdaten und 20% Testdaten. Innerhalb der Ordner `train_data` und `test_data` müssen Sie für jede Aufnahmesitzung einen Ordner erstellen und ihm einen Namen wie `my_openbot_1`, `my_openbot_2` usw. geben. Die Idee dahinter ist, dass jede Aufnahmesitzung unterschiedliche Lichtverhältnisse, einen anderen Roboter, eine andere Route haben kann. Im Jupyter-Notebook können Sie dann nur auf einem Teil dieser Datensätze oder auf allen trainieren. Innerhalb jedes Aufnahmesitzungsordners legen Sie alle Aufnahmen dieser Aufnahmesitzung ab. Jede Aufnahme entspricht einer extrahierten ZIP-Datei, die Sie aus dem *Openbot*-Ordner auf Ihrem Telefon übertragen haben. Ihr Datensatzordner sollte so aussehen:

<img src="../docs/images/folder_structure.png" width="200" alt="Ordnerstruktur" />

Anstatt alle Dateien manuell vom Telefon zu kopieren, können Sie die Logs auch automatisch auf einen [Python-Server](#web-app) auf Ihrem Computer hochladen. In diesem Fall werden die ZIP-Dateien hochgeladen und in den Ordner `dataset/uploaded` entpackt. Sie müssen sie immer noch in die Ordnerstruktur für das Training verschieben. Sie können den Ordner `uploaded` einfach als Aufnahmesitzung behandeln und in `train_data` verschieben. Die Aufnahmen werden dann vom Jupyter-Notebook als Trainingsdaten erkannt. Wenn Sie noch keine Aufnahmesitzung im Ordner `test_data` haben, müssen Sie auch mindestens eine Aufnahme von `train_data/uploaded` in `test_data/uploaded` verschieben.

### Datenkonvertierung (optional)

Für eine bessere Trainingsleistung können Sie den gesammelten Datensatz in ein spezialisiertes Format konvertieren. Sie können ein TFRecord der Trainings- und Testdatensätze mit den folgenden Befehlen erstellen:

```bash
conda activate openbot
python -m openbot.tfrecord -i dataset/train_data -o dataset/tfrecords -n train.tfrec
python -m openbot.tfrecord -i dataset/test_data -o dataset/tfrecords -n test.tfrec
```

Standardmäßig wird diese Konvertierung automatisch zu Beginn des Trainings durchgeführt.

## Training der Fahrstrategie

Stellen Sie sicher, dass Ihre Conda-Umgebung für OpenBot aktiviert ist, indem Sie den folgenden Befehl ausführen:

```bash
conda activate openbot
```

### Jupyter Notebook

Wir bieten ein [Jupyter Notebook](policy_learning.ipynb) an, das Sie durch die Schritte zum Training einer autonomen Fahrstrategie führt. Öffnen Sie das Notebook mit folgendem Befehl.

```bash
jupyter notebook policy_learning.ipynb
```

Jetzt wird automatisch ein Webbrowser-Fenster geöffnet und das Jupyter-Notebook geladen. Folgen Sie den Schritten, um ein Modell mit Ihren eigenen Daten zu trainieren.

### Shell

Diese Methode setzt voraus, dass die Daten am richtigen Ort sind. Um die Hyperparameter anzupassen, können Sie die folgenden Argumente übergeben.

```bash
'--no_tf_record', action='store_true', help='lade kein TFRecord, sondern ein Verzeichnis von Dateien'
'--create_tf_record', action='store_true', help='erstelle ein neues TFRecord'
'--model', type=str, default='pilot_net', choices=['cil_mobile', 'cil_mobile_fast', 'cil', 'pilot_net'], help='Netzwerkarchitektur (Standard: cil_mobile)'
'--batch_size', type=int, default=16, help='Anzahl der Trainingsepochen (Standard: 16)'
'--learning_rate', type=float, default=0.0001, help='Lernrate (Standard: 0.0001)'
'--num_epochs', type=int, default=10, help='Anzahl der Epochen (Standard: 10)'
'--batch_norm', action='store_true', help='verwende Batch-Norm'
'--flip_aug', action='store_true', help='zufälliges Spiegeln von Bildern und Steuerungen zur Augmentation'
'--cmd_aug', action='store_true', help='füge Rauschen zum Befehlsinput zur Augmentation hinzu'
'--resume', action='store_true', help='vorheriges Training fortsetzen'
```

Wenn Ihr Datensatz bereits in ein TFRecord konvertiert wurde, können Sie die Strategie aus der Shell mit dem Befehl trainieren:

```bash
python -m openbot.train
```

Wenn Sie Ihren Datensatz vor dem Training in ein TFRecord konvertieren möchten, müssen Sie das folgende Flag hinzufügen:

```bash
python -m openbot.train --create_tf_record
```

Wenn Sie den Datensatz nicht in ein TFRecord konvertieren möchten und direkt mit den Dateien trainieren möchten, müssen Sie das folgende Flag hinzufügen:

```bash
python -m openbot.train --no_tf_record
```

Um ein Modell für den endgültigen Einsatz zu trainieren, sollten Sie eine große Batch-Größe und eine hohe Anzahl von Epochen verwenden. Das Aktivieren der Batch-Norm verbessert das Training in der Regel ebenfalls. Das Modell `pilot_net` ist größer als das Standardmodell `cil_mobile`, kann jedoch bei einigen Aufgaben eine bessere Leistung erzielen und läuft dennoch in Echtzeit auf den meisten Smartphones.

```bash
python -m openbot.train --model pilot_net --batch_size 128 --num_epochs 100 --batch_norm
```

### Einsatz

Am Ende des Trainingsprozesses werden zwei TFLite-Dateien generiert: eine entspricht dem besten Checkpoint gemäß den Validierungsmetriken und die andere dem letzten Checkpoint. Wählen Sie eine davon aus und benennen Sie sie in autopilot_float.tflite um. Ersetzen Sie das vorhandene Modell in Android Studio und kompilieren Sie die App neu.

<p align="center">
  <img src="../docs/images/android_studio_tflite_dir.jpg" width="200" alt="App GUI" />
</p>

Wenn Sie nach dem Ordner in Ihrem lokalen Verzeichnis suchen, finden Sie ihn unter: `app/src/main/assets/networks`.

## Web App

Wir bieten eine Web-App und einen Python-Webserver für einfaches Training von Fahrstrategien an. (Beta)

### Funktionen

- Automatisches Hochladen von Logs (Sitzungen)
  - siehe Fehlerbehebung für Details
- Aufgelistete hochgeladene Sitzungen mit GIF-Vorschau
- Aufgelistete Datensätze mit Basisinformationen
- Verschieben von Sitzungen in einen Datensatz
- Löschen von Sitzungen
- Aufgelistete trainierte Modelle und Anzeige von Diagrammen zum Training
- Training eines Modells mit grundlegenden Parametern, Anzeige des Fortschrittsbalkens

### Vorschau

<img src="../docs/images/web-app.gif" width="100%" alt="Web App Vorschau" />

### Schnellstart

```bash
conda activate openbot
python -m openbot.server
```

Sie können jetzt Ihren Browser öffnen, um den Datensatz zu visualisieren und eingehende Uploads zu sehen, indem Sie zu folgender Adresse gehen:
[http://localhost:8000/#/uploaded](http://localhost:8000/#/uploaded)

### Ausführen des Servers

Sie können den Python-Server mit folgendem Befehl ausführen:

```bash
python -m openbot.server
```

Es gibt auch einen Entwicklermodus:

```bash
adev runserver openbot/server
```

Für die Frontend-Entwicklung (React-App):

```
FE_DEV=1 adev runserver openbot/server
```

Wenn Sie den Server ausführen, sollten Sie etwas Ähnliches sehen:

```
Skip address 127.0.0.1 @ interface lo
Found address 192.168.x.x @ interface wlp2s0
Registration of a service, press Ctrl-C to exit...
Running frontend: 0.7.0
Frontend path: /home/USERNAME/miniconda3/envs/openbot/lib/python3.7/site-packages/openbot_frontend
======== Running on http://0.0.0.0:8000 ========
(Press CTRL+C to quit)
```

### Fehlerbehebung

Wenn der Upload zum Server nicht funktioniert, hier einige Tipps zur Fehlerbehebung:

- Versuchen Sie, den Server (Computer) und die OpenBot-App (Smartphone) neu zu starten
- Stellen Sie sicher, dass das Smartphone und Ihr Computer mit demselben WLAN-Netzwerk verbunden sind
- Wenn Ihr Router sowohl 2,4 GHz- als auch 5 GHz-Netzwerke mit demselben Namen hat, deaktivieren Sie das 5 GHz-Netzwerk
- Halten Sie das Telefon während des Betriebs der App mit Android Studio verbunden. Wählen Sie im Logcat-Tab Debug aus dem Dropdown-Menü. Geben Sie `NSD` in das Filterfeld ein, um die Debug-Nachrichten bezüglich der Serververbindung zu sehen. Geben Sie `Upload` in das Filterfeld ein, um die Debug-Nachrichten bezüglich des Uploads der Aufnahmedatei zu sehen.
- Wenn ein veröffentlichtes Modell kontinuierlich heruntergeladen wird, stellen Sie sicher, dass die Uhrzeit auf Ihrem Telefon und Laptop/Arbeitsplatz korrekt eingestellt ist.
