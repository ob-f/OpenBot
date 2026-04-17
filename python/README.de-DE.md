# Python

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Dieses Modul ist eine Embedded-Linux-Alternative zur Smartphone-Steuerung eines OpenBot-Fahrzeugs. Geschrieben in Python, kann der OpenBot mit einem Linux-basierten Computer und einer Kamera zur Erkennung gesteuert werden.

Der Roboter kann auf zwei Arten gesteuert werden: durch Inferenz einer Neural Network-Policy oder über einen Joystick.

```
├── __init__.py
├── README.md
├── requirements.txt
├── run.py
├── generate_data_for_training.py
├── export_openvino.py
├── infer.py
├── joystick.py
├── realsense.py
└── tests
    ├── test_data
    │   └── logs1
    │       └── ...
    ├── test_models
    │   ├── openvino
    │   ├── tflite
    │   └── tf.zip
    ├── test_export_openvino.py
    ├── test_infer.py
    ├── test_joystick.py
    ├── test_motor.py
    └── test_realsense.py

```
## Roboter betreiben

Um den Roboter zu betreiben, führen Sie das `run.py`-Skript aus, welches das Haupt-Python-Skript ist. Der Roboter kann in 3 Modi betrieben werden:
- Debug: Dieser Modus führt die Policy offline aus. Das heißt, anstelle von echten Kamerabildern und Joystick-Eingabebefehlen verwendet er Daten (Befehle und Bilder), die aus einem Datensatz geladen werden (siehe `tests/test_data/logs1/data`) als Eingabe für die Policy.
- Inferenz: Dieser Modus führt die Policy online aus. Er verwendet echte Kamerabilder und Joystick-Eingabebefehle als Eingabe für die Policy. Dieser Modus kann durch Drücken der `A`-Taste am Joystick in den Joystick-Modus umgeschaltet werden.
- Joystick: Dieser Modus steuert den Roboter über Joystick-Befehle entweder im "Dual" (Steuerung des linken und rechten Rads über den linken und rechten Joystick) oder "Joystick" (Steuerung vorwärts, rückwärts, links, rechts über einen Joystick) `control_mode`. Die Datenerfassung für das Training erfolgt im Joystick-Modus. Dieser Modus kann durch Drücken der `A`-Taste am Joystick in den Inferenzmodus umgeschaltet werden.

Das run.py-Skript akzeptiert sechs Argumente (weitere Details siehe `run.py`):
```
--policy_path: Pfad zur Policy-Datei.
--dataset_path: Pfad zum Datensatz. Wird nur im Debug-Modus verwendet.
--log_path: Pfad zum Log-Ordner, in dem die Läufe gespeichert werden.
--inference_backend: Zu verwendendes Backend. Es wird empfohlen, alle Modelle als OpenVino-Modell zu exportieren, um maximale Leistung zu erzielen. Optionen: tf, tflite, openvino.
--mode: Ausführungsmodus. Optionen: debug, inference, joystick.
--control_mode: Steuerungsmodus im Joystick-Modus. Optionen: dual, joystick.
```
## Trainingsdaten generieren
Das Skript `generate_data_for_training.py` erzeugt einen Log-Datenordner, der für das Training einer Policy über das `OpenBot/policy/openbot/train.py`-Skript erforderlich ist. Der Log-Datenordner enthält einen `images`- und einen `sensor_data`-Ordner im Format, das von `train.py` benötigt wird.

Siehe `tests/test_generate_data.py` für ein Beispiel.

## OpenVino: Optimierung der Policy-Inferenzleistung
Um die Inferenzgeschwindigkeit auf unterstützter Intel-Hardware (wie dem [Up Core Plus](https://up-board.org/upcoreplus/specifications/) Board) zu optimieren, muss das trainierte Modell nach OpenVino exportiert werden.

Das `export_openvino.py`-Skript exportiert ein trainiertes TensorFlow-Modell zu einem OpenVino-Modell. Dieses OpenVino-Modell wird dann über `get_openvino_interpreter()` in `infer.py` geladen.

Siehe `tests/test_export_openvino.py` für ein Beispiel.

## Tests und Beispielcode

**Hinweis:** Für das Testen des Codes werden die Testdaten und das Testmodell `test_data` und `test_model` benötigt, die sich in `OpenBot/python/tests` befinden. Die Funktion `get_data()` in `download_data.py` bietet eine Download-Funktionalität und wird zu Beginn von `test_infer.py`, `test_export_openvino.py` und `test_generate_data.py` aufgerufen. Alternativ können Sie das Skript `get_test_data.sh` (nur Unix-Systeme) ausführen, das eine ZIP-Datei mit `test_data` und `test_models` herunterlädt und entpackt, die die Daten für den Debug-Modus und die Modelle für die Inferenz enthält.

Führen Sie `pytest` im Ordner `tests` aus oder führen Sie die `test_*.py`-Dateien einzeln aus, um die Funktionalitäten zu testen:

- Herunterladen von Testdaten und Testmodellen aus der Cloud über `test_download_data.py`
- Export nach OpenVino über `test_export_openvino.py`
- Generierung von Trainingsdaten über `test_generate_data.py`.
- Inferenz im Debug-Modus für OpenVino, Tensorflow und Tflite über `test_infer.py`.
    - *Hinweis*: Die Testdaten in logs1 werden mit dem Skript `associate_frames.py` in `OpenBot.policy.openbot` generiert, wobei der Pfad zu den Bildern in `logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt` fest codiert ist.
    - Ersetzen Sie daher bitte den `path_to_openbot` durch den tatsächlichen Pfad zum `OpenBot`-Repository in `test_infer.py`.
- Joystick-Verbindung über `test_joystick.py`
- Motorverbindung vom seriellen Port zum Arduino über `test_motor.py`.
- Video-Stream zur Realsense-Kamera über `test_realsense.py`.

# Installation
Der Installationsprozess wird im Folgenden detailliert beschrieben.

Die Python-Implementierung zur Steuerung von OpenBot erfordert einige Python-Module für Inferenz, Joystick-Steuerung, Erkennung und Betätigung.
Darüber hinaus könnten Treiber für die Kamera oder den Controller erforderlich sein.

## Einrichtung
Derzeit ist der Code getestet auf:
- Board: [Up Core Plus](https://up-board.org/upcoreplus/specifications/)
- Kamera: [Realsense D435i](https://www.intelrealsense.com/depth-camera-d435i/)
- Controller: [Xbox One](https://www.microsoft.com/en-gb/store/collections/xboxcontrollers?source=lp)
- Arduino: [OpenBot Firmware](https://github.com/ob-f/OpenBot/blob/master/firmware/README.md)

## Python-Module

Der Code wurde mit Python 3.9 getestet. Verwenden Sie Anaconda3:
```
conda create --name openbot python==3.9
```

Installieren Sie zunächst die Anforderungen von OpenBot.policy über
```
../policy && pip install -r requirements.txt
```

Installieren Sie dann die erforderlichen Module über
```
pip install -r requirements.txt
```

Insbesondere,
- `pyserial` kommuniziert mit dem Arduino und somit den Motoren über den seriellen Port
- `pyrealsense2` und `opencv-python` werden für die Verarbeitung von Kamerabildern benötigt.
- `pygame` wird für die Joystick-Steuerung und die Verarbeitung der Joystick-Eingaben verwendet
- `openvino-dev[tensorflow2,extras]` wird für eine gesteigerte Leistung auf unterstützter Intel-Hardware verwendet. Weitere Details zur optimierten KI-Inferenz auf Intel-Hardware finden Sie unter [OpenVino](https://docs.openvino.ai/latest/home.html). OpenVino ist das empfohlene Inferenz-Backend. Tensorflow und Tflite werden ebenfalls unterstützt (siehe Tests). Für die Ausführung von PyTorch-Modulen sollten Sie in Betracht ziehen, PyTorch in ein OpenVino-Backend zu konvertieren (siehe [dieses Tutorial](https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html)).

## Treiber
Wenn der Code unter Ubuntu ausgeführt wird, benötigt der Xbox One Controller USB Wireless Dongle einen Treiber, der unter [diesem Link](https://github.com/medusalix/xone) zu finden ist.

## Tensorflow für Inferenz
Wenn TensorFlow für die Inferenz verwendet wird, fügen Sie das Python-`policy`-Modul über `export PYTHONPATH=$PYTHONPATH:/path/to/OpenBot/policy` zu `PYTHONPATH` hinzu. Dieser Workaround vermeidet es, OpenBot als Modul installieren zu müssen, und ermöglicht das Finden von `openbot.utils.load_model()`, das zum Laden des TensorFlow-Modells erforderlich ist. Weitere Details finden Sie in `get_tf_interpreter()` in `infer.py` und im Testcode `tests/test_infer.py`.

## Unterstützung für nicht-Linux-Distributionen (MacOS, Windows)

Bitte beachten Sie, dass der Code für die Ausführung auf Linux-basierten Computern, z.B. Up Core Plus, vorgesehen ist. Einige Python-Module sind möglicherweise nicht für MacOS oder Windows verfügbar.

Der Code kann zu Debugging-Zwecken auf MacOS mit den folgenden Änderungen ausgeführt werden:
- Verwenden Sie `pyrealsense2-macosx` anstelle von `pyrealsense2` in requirements.txt
- Für tflite folgen Sie [diesen Anweisungen](https://github.com/milinddeore/TfLite-Standalone-build-Linux-MacOS)
