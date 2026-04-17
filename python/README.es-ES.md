# Python

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Este módulo es una alternativa embebida en Linux para el control de un vehículo OpenBot mediante un smartphone. Escrito en Python, el OpenBot puede ser controlado usando una computadora basada en Linux y una cámara para la detección.

El robot puede ser controlado de dos maneras: a través de la inferencia de una política de Red Neuronal o mediante un joystick.

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
## Ejecutando el Robot

Para operar el robot, ejecuta `run.py`, que es el script principal de Python. El robot puede ejecutarse en 3 modos:
- Debug: Este modo ejecuta la política fuera de línea. Es decir, en lugar de usar imágenes de cámara reales y comandos de entrada del joystick, utiliza datos (comandos e imágenes) cargados desde un conjunto de datos (ver `tests/test_data/logs1/data`) como entrada para la política.
- Inference: Este modo ejecuta la política en línea. Utiliza imágenes de cámara reales y comandos de entrada del joystick como entrada para la política. Este modo puede cambiarse al modo Joystick presionando la tecla `A` en el joystick.
- Joystick: Este modo opera el robot mediante comandos del joystick en el `control_mode` "Dual" (controlando la rueda izquierda y derecha mediante el joystick izquierdo y derecho) o "Joystick" (controlando la dirección hacia adelante, atrás, izquierda, derecha mediante un solo joystick). La recolección de datos para el entrenamiento se realiza en el modo Joystick. Este modo puede cambiarse al modo Inference presionando la tecla `A` en el joystick.

El script run.py acepta seis argumentos (para más detalles, ver `run.py`):
```
--policy_path: Ruta al archivo de la política.
--dataset_path: Ruta al conjunto de datos. Solo se usa para el modo debug.
--log_path: Ruta a la carpeta de logs, donde se guardan las ejecuciones.
--inference_backend: Backend a usar. Considera exportar todos los modelos como modelo openvino para un rendimiento máximo. Opciones: tf, tflite, openvino.
--mode: Modo de ejecución. Opciones: debug, inference, joystick.
--control_mode: Modo de control durante el modo joystick. Opciones: dual, joystick.
```
## Generando Datos de Entrenamiento
El script `generate_data_for_training.py` genera una carpeta de datos de log que es requerida para entrenar una política mediante el script `OpenBot/policy/openbot/train.py`. La carpeta de datos de log contiene una carpeta `images` y una carpeta `sensor_data` en el formato requerido por `train.py`.

Ver `tests/test_generate_data.py` para un ejemplo.

## OpenVino: Optimizando el Rendimiento de la Inferencia de la Política
Para optimizar la velocidad de inferencia en hardware Intel compatible (como la placa [Up Core Plus](https://up-board.org/upcoreplus/specifications/)), el modelo entrenado necesita ser exportado a OpenVino.

El script `export_openvino.py` exporta un modelo TensorFlow entrenado a un modelo OpenVino. Este modelo OpenVino luego se carga mediante `get_openvino_interpreter()` en `infer.py`.

Ver `tests/test_export_openvino.py` para un ejemplo.

## Pruebas y código de ejemplo

**Nota:** Para probar el código, los datos de prueba y el modelo de prueba llamados `test_data` y `test_model` respectivamente deben estar en `OpenBot/python/tests`. La función `get_data()` en `download_data.py` proporciona la funcionalidad de descarga y se llama al inicio de `test_infer.py`, `test_export_openvino.py`, y `test_generate_data.py`. Alternativamente, por favor ejecuta el script `get_test_data.sh` (solo sistemas Unix) que descarga y descomprime un archivo zip que contiene `test_data` y `test_models` con los datos para el modo debug y modelos para la inferencia respectivamente.

Ejecuta `pytest` en la carpeta `tests` o ejecuta los archivos `test_*.py` individualmente para probar las funcionalidades de

- descargar datos de prueba y modelo de prueba desde la nube mediante `test_download_data.py`
- exportar a OpenVino mediante `test_export_openvino.py`
- generar datos de entrenamiento mediante `test_generate_data.py`.
- inferencia en modo debug para OpenVino, Tensorflow, y Tflite mediante `test_infer.py`.
    - *Nota*: Los datos de prueba en logs1 se generan usando el script `associate_frames.py` en `OpenBot.policy.openbot`, donde la ruta a las imágenes está codificada en `logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt`.
    - Por lo tanto, por favor reemplaza `path_to_openbot` con la ruta real al repositorio `OpenBot` en `test_infer.py`.
- conexión del joystick mediante `test_joystick.py`
- conexión del motor desde el puerto serie al Arduino mediante `test_motor.py`.
- transmisión de video a la cámara Realsense mediante `test_realsense.py`.

# Instalación
El proceso de instalación se detalla a continuación.

La implementación en Python para controlar OpenBot requiere algunos módulos de Python para inferencia, control del joystick, detección y actuación.
Además, pueden ser necesarios controladores para la cámara o el controlador.

## Configuración
Actualmente, el código se prueba en:
- Placa: [Up Core Plus](https://up-board.org/upcoreplus/specifications/)
- Cámara: [Realsense D435i](https://www.intelrealsense.com/depth-camera-d435i/)
- Controlador: [Xbox One](https://www.microsoft.com/en-gb/store/collections/xboxcontrollers?source=lp)
- Arduino: [Firmware de OpenBot](https://github.com/ob-f/OpenBot/blob/master/firmware/README.md)

## Módulos de Python

El código se prueba con Python 3.9. Usando Anaconda3:
```
conda create --name openbot python==3.9
```

Primero, instala los requisitos de OpenBot.policy mediante
```
../policy && pip install -r requirements.txt
```

Luego, instala los módulos requeridos mediante
```
pip install -r requirements.txt
```

En particular,
- `pyserial` se comunica con el Arduino y, por lo tanto, con los motores mediante el puerto serie
- `pyrealsense2` y `opencv-python` son necesarios para el procesamiento de imágenes de la cámara.
- `pygame` se usa para el control del joystick y el procesamiento de las entradas del joystick
- `openvino-dev[tensorflow2,extras]` se usa para un rendimiento mejorado en hardware Intel compatible. Para más detalles sobre la inferencia de IA optimizada en hardware Intel, por favor ver [OpenVino](https://docs.openvino.ai/latest/home.html). OpenVino es el backend de inferencia recomendado. Tensorflow y Tflite también son compatibles (ver Pruebas). Para ejecutar módulos PyTorch, por favor considera convertir PyTorch a un backend OpenVino (ver [este Tutorial](https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html)).

## Controladores
Si el código se ejecuta en Ubuntu, el dongle USB inalámbrico del controlador Xbox One requiere un controlador, que se puede encontrar en [este enlace](https://github.com/medusalix/xone).

## Tensorflow para Inferencia
Si se usa TensorFlow para la inferencia, por favor agrega el módulo Python `policy` a `PYTHONPATH` mediante `export PYTHONPATH=$PYTHONPATH:/path/to/OpenBot/policy`. Este truco evita tener que instalar openbot como módulo y encontrar `openbot.utils.load_model()`, que es necesario para cargar el modelo tensorflow. Para más detalles, ver `get_tf_interpreter()` en `infer.py` y el código de prueba `tests/test_infer.py`.

## Soporte para distribuciones no Linux (MacOS, Windows)

Por favor, ten en cuenta que el código está destinado a ejecutarse en computadoras basadas en Linux, por ejemplo, Up Core Plus. Algunos módulos de Python pueden no estar disponibles para MacOs o Windows.

El código puede ejecutarse en MacOS para fines de depuración con los siguientes cambios:
- Usa `pyrealsense2-macosx` en lugar de `pyrealsense2` en requirements.txt
- Para tflite sigue [estas instrucciones](https://github.com/milinddeore/TfLite-Standalone-build-Linux-MacOS)