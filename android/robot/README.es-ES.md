# Robot App

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## AVISOS

1. **Seguridad:** Asegúrate siempre de operar en un entorno seguro. Ten en cuenta que tu teléfono podría dañarse en una colisión. Se requiere un cuidado especial al usar el control automatizado (por ejemplo, seguimiento de personas o política de conducción). Asegúrate de tener siempre un controlador de juegos conectado y estar familiarizado con la asignación de teclas para poder detener el vehículo en cualquier momento. ¡Usa bajo tu propio riesgo!
2. **Aplicación en desarrollo:** La aplicación está en desarrollo y puede fallar o mostrar un comportamiento inesperado dependiendo del modelo de tu teléfono y la versión del sistema operativo. Asegúrate de probar todas las funcionalidades sin las ruedas conectadas. ¡Usa bajo tu propio riesgo!

## Pantallas de la Aplicación

### Menú Principal

La aplicación comienza con una pantalla de menú que muestra todas las pantallas disponibles. La pantalla de configuración se puede abrir con un clic en el ícono en la esquina superior derecha. Al hacer clic en los otros íconos, el usuario puede acceder a varias pantallas cuyas funcionalidades se explican a continuación.

<p align="left">
<img style="padding-right: 2%;" src="../../docs/images/screen_main.png" alt="Menú Principal" width="24.5%"/>
<img src="../../docs/images/screen_settings.png" alt="Menú de Configuración" width="24.5%"/>
<img src="../../docs/images/dialog_stream_mode.png" alt="Menú de Configuración" width="24.5%"/>
<img src="../../docs/images/dialog_connectivity_mode.png" alt="Menú de Configuración" width="24.5%"/>
</p>

### Menú de Configuración

#### Conexión USB

Toca el ícono USB para abrir las opciones USB. El menú desplegable se usa para configurar la velocidad en baudios. El valor predeterminado es 115200 y no deberías necesitar cambiarlo a menos que modifiques el firmware de Arduino. La aplicación intentará conectarse automáticamente, pero en caso de que encuentres problemas, puedes usar este interruptor para desconectar/conectar.

<p align="left">
<img src="../../docs/images/usb_disconnected.jpg" alt="Conectando dispositivo" width="25%"/>
<img src="../../docs/images/usb_connected.jpg" alt="Botón de desconexión" width="25%"/>
</p>

#### Permisos

Aquí puedes verificar los permisos de la aplicación y ajustarlos si es necesario.

#### Transmisión de Video

Puedes elegir entre `WebRTC` y `RTSP` para transmitir video a un dispositivo externo. La aplicación controladora del teléfono y el servidor node-js necesitan que esto esté configurado en `WebRTC`. El controlador de Python espera que la transmisión esté configurada en `RTSP`.

#### Conexión Bluetooth

Asegúrate de que tu dispositivo Android tenga soporte BLE (Bluetooth Low Energy). Si tu versión de Android es mayor o igual a 7.0, también necesitas activar el servicio de ubicación y permitir el permiso de ubicación en la configuración para buscar dispositivos BLE cercanos. Para habilitar BLE, cambia el modo de conectividad de USB a Bluetooth en el menú de configuración. Obtendrás un ícono de Bluetooth en la parte superior de la pantalla de inicio. Toca el ícono de Bluetooth para iniciar el escaneo BLE; toma 4 segundos escanear y obtener una lista de todos los dispositivos OpenBot BLE cercanos. Conéctate con tu OpenBot tocando el botón `Conectar`. Después de una conexión exitosa, el botón `Conectar` cambiará a `Desconectar`. Ahora puedes volver a la pantalla de inicio.

<p align="left">
<img src="../../docs/images/ble_devices_list.jpg" alt="Dispositivos BLE" width="25%"/>
<img src="../../docs/images/ble_device_connecting.jpg" alt="Conectando dispositivo" width="25%"/>
<img src="../../docs/images/ble_device_connected.jpg" alt="Botón de desconexión" width="25%"/>
</p>

### Exploración Libre

Exploración Libre ofrece un control simple del robot con actualizaciones en tiempo real e información sobre la batería, velocidad y distancia de las superficies.

<p align="left">
<img src="../../docs/images/screen_free_roam.jpg" alt="Exploración Libre" width="50%" />
</p>

- **Batería**: El ícono de la batería muestra los niveles de batería en tiempo real del robot conectado.
- **Estado de Conducción**: Hay 3 estados de conducción mostrados en la vista:
  - D -> Conducción, cuando el robot está avanzando
  - N -> Neutro, cuando el robot está estacionario
  - R -> Reversa, cuando el robot está retrocediendo
  El volante gira proporcionalmente al ángulo de dirección.
- **Velocidad**: El velocímetro muestra la velocidad del robot.
- **Sonar**: La distancia libre frente al robot en cm.
- **Control**: Controlador, Modo de Conducción y Velocidad se usan para controlar la configuración del robot como se describe en la [sección de control](#control).

### Recolección de Datos

Interfaz simple para la recolección de conjuntos de datos.

<p align="left">
<img src="../../docs/images/screen_data_collection.jpg" alt="Recolección de Datos" width="50%" />
</p>

- **Servidor**: Si tienes la [aplicación web](../../policy#web-app) para el entrenamiento de políticas en funcionamiento, puedes seleccionarla aquí para cargar datos automáticamente.
- **Resolución de Vista Previa**: Se usa para cambiar entre resoluciones de vista previa de la cámara. Hay 3 configuraciones:
  - ***FULL_HD*** (1920x1080p)
  - ***HD*** (1280x720p)
  - ***SD*** (640x360)
- **Resolución del Modelo**: Se usa para cambiar entre resoluciones de imágenes guardadas para entrenar diferentes modelos.
- **Guardar/Descartar los Datos Recogidos**: el proceso de recolección de datos se puede controlar desde la pantalla o de forma remota, por ejemplo, desde un controlador Bluetooth. Al usar un controlador Bluetooth, puedes:
  - Presionar el **botón A** para **iniciar** el proceso de recolección de datos
  - Presionar el **botón A nuevamente** para **detener** la recolección de datos y guardar los datos recogidos en un archivo .zip
  - Alternativamente, presionar el **botón R1** para **detener** la recolección de datos **sin guardar** los datos recogidos (por ejemplo, debido a una colisión inesperada con el entorno)
  - Recuerda usar el fragmento de asignación de controladores para asegurarte de estar usando los botones correctos.

### Asignación de Controladores

Interfaz simple para verificar la asignación de botones y joysticks de un controlador BT conectado.

<p align="left">
<img src="../../docs/images/screen_controller_mapping.jpg" alt="Asignación de Controladores" width="50%" />
</p>

### Información del Robot

Interfaz simple para obtener información del robot y probar la funcionalidad básica. El **Tipo de Robot** configurado en el firmware se muestra como texto y animación. Las marcas de verificación en las secciones **Sensores**, **Odómetro de Ruedas** y **LEDs** muestran qué características son compatibles con el robot conectado. La sección **Lecturas** proporciona las mediciones de sensores más importantes. En la sección **Enviar Comandos**, los usuarios pueden enviar comandos básicos de motor presionando los botones correspondientes y controlar los LEDs delanteros y traseros con un deslizador.

<p align="left">
<img src="../../docs/images/screen_robot_info.gif" alt="Información del Robot" width="50%" />
</p>

### Piloto Automático

Interfaz simple para ejecutar modelos de piloto automático.

<p align="left">
<img src="../../docs/images/screen_autopilot.jpg" alt="Piloto Automático" width="50%" />
</p>

- **Servidor**: Si tienes la [aplicación web](../../policy#web-app) para el entrenamiento de políticas en funcionamiento, puedes seleccionarla aquí y enviar modelos de piloto automático entrenados al robot.
- **Modelo**: Elige un modelo entrenado para usar en el modo de piloto automático.
- **Dispositivo**: Usa CPU, GPU o NNAPI para la inferencia (más detalles [aquí](#dispositivo)).
- **Hilos**: Número de hilos a usar (solo hace una diferencia cuando se selecciona CPU como dispositivo).
- **Control**: Controlador, Modo de Conducción y Velocidad se usan para controlar la configuración del robot como se describe en la [sección de control](#control).

### Seguimiento de Objetos

Interfaz simple para el seguimiento de objetos de 80 clases diferentes. Una breve descripción de los diferentes modelos de IA para el seguimiento de objetos y los puntos de referencia de rendimiento se pueden encontrar en [Gestión de Modelos](#gestión-de-modelos).

<p align="left">
<img src="../../docs/images/screen_object_tracking_1.jpg" alt="Texto alternativo" width="49%" />
<img src="../../docs/images/screen_object_tracking_2.jpg" alt="Texto alternativo" width="49%" />
</p>

- **Velocidad Dinámica**: reduce la velocidad del robot en "Modo Automático" si se acerca al objeto rastreado.
  La velocidad se escala en función del área del cuadro delimitador (funciona mejor en orientación horizontal).
- **Modelo**: Elige un detector de objetos basado en el rendimiento de tu teléfono (ver más abajo para [resultados de referencia](#punto-de-referencia)).
- **Objeto**: Elige el objeto que deseas rastrear. Los modelos pueden detectar las 80 [clases de objetos COCO](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/).
- **Confianza**: Umbral de confianza para determinar si se aceptan las detecciones. Aumenta si obtienes detecciones falsas, disminuye si el objeto de interés no se detecta.
- **Dispositivo**: Usa CPU, GPU o NNAPI para la inferencia (más detalles [aquí](#dispositivo)).
- **Hilos**: Número de hilos a usar (solo hace una diferencia cuando se selecciona CPU como dispositivo).
- **Control**: Controlador, Modo de Conducción y Velocidad se usan para controlar la configuración del robot como se describe en la [sección de control](#control).

### Navegación a Punto de Destino

Ten en cuenta que este fragmento requiere ARCore y permiso de cámara. Si tu dispositivo no es compatible con ARCore y continúas de todos modos, la aplicación se bloqueará. En esta pantalla puedes especificar un objetivo a través de un vector 2D con respecto a la posición y orientación actual del robot. El vector 2D contiene la distancia al frente y a la izquierda del robot en metros. Ambos valores también pueden ser negativos y corresponder a la parte trasera y derecha del robot en ese caso. Después de especificar el objetivo y presionar `Iniciar`, el robot ejecutará una política de IA que intentará alcanzar el objetivo mientras evita obstáculos.

<p align="left">
<img src="../../docs/images/screen_point_goal_nav.gif" alt="Texto alternativo" width="50%" />
</p>

### Gestión de Modelos

Todos los modelos están cuantizados para un mejor rendimiento en dispositivos integrados. Consulta la sección a continuación para una breve descripción de los modelos disponibles y los resultados de referencia. La [precisión media promedio (mAP)](https://kharshit.github.io/blog/2019/09/20/evaluation-metrics-for-object-detection-and-segmentation) se calcula en el conjunto de validación del conjunto de datos [COCO Detection 2017](https://cocodataset.org/#detection-2017). Cada modelo se ejecuta durante aproximadamente 1 minuto; el tiempo de inferencia se promedia en los últimos 100 fotogramas y se informa en fotogramas por segundo (fps). Ten en cuenta que los modelos con mayor resolución de entrada pueden ser mejores para objetos más pequeños a pesar de un mAP más bajo.

<p align="left">
<img src="../../docs/images/screen_model_management.gif" alt="Gestión de Modelos" width="25%" />
</p>

### Punto de Referencia

#### Teléfonos

| Nombre del Modelo | Chipset        | RAM  | SO |
|-------------------|----------------|------|----|
| Samsung S22 Ultra | Exynos 2200    | 12GB | 12 |
| Samsung S20FE 5G  | Snapdragon 865 |  6GB | 12 |
| Huawei P30 Pro    | Kirin 980      |  8GB | 10 |
| Google Pixel 6XL  | Google Tensor  | 12GB | 12 |
| Xiaomi Mi9        | Snapdragon 855 |  6GB | 10 |
| Google Pixel 4XL  | Snapdragon 855 |  6GB | 13 |

#### MobileNetV1-300 (preinstalado) - mAP: 18%

Detector de objetos SSD con [MobileNet V1](https://tfhub.dev/tensorflow/lite-model/ssd_mobilenet_v1/1/metadata/2) como backbone y resolución de entrada de 300x300.

|teléfono/dispositivo (fps)| CPU | GPU | NNAPI |
|--------------------------|-----|-----|-------|
| Samsung S22 Ultra        |  33 |  13 |   30  |
| Samsung S20FE 5G         |  34 |  57 |   87  |
| Huawei P30 Pro           |  36 |  25 |   10  |
| Google Pixel 6XL         |  35 |  42 |   53  |
| Xiaomi Mi9               |  22 |  41 |   33  |
| Google Pixel 4XL         |  37 |  36 |   45  |

#### MobileNetV3-320 - mAP: 16%

Detector de objetos SSD con MobileNet V3 como backbone y resolución de entrada de 320x320.

|teléfono/dispositivo (fps)| CPU | GPU | NNAPI |
|--------------------------|-----|-----|-------|
| Samsung S22 Ultra        |  30 |  17 |   30  |
| Samsung S20FE 5G         |  34 |  42 |   28  |
| Huawei P30 Pro           |  32 |  27 |   23  |
| Google Pixel 6XL         |  33 |  43 |   27  |
| Xiaomi Mi9               |  20 |  45 |   10  |
| Google Pixel 4XL         |  32 |  38 |   21  |

#### YoloV4-tiny-224 - mAP: 22%

Versión pequeña de [YoloV4](https://arxiv.org/abs/2004.10934) con resolución de entrada de 224x224.

|teléfono/dispositivo (fps)| CPU | GPU | NNAPI |
|--------------------------|-----|-----|-------|
| Samsung S22 Ultra        |  31 |  12 |   31  |
| Samsung S20FE 5G         |  30 |  21 |   14  |
| Huawei P30 Pro           |  27 |  17 |   22  |
| Google Pixel 6XL         |  29 |  24 |   19  |
| Xiaomi Mi9               |  16 |  14 |  9.3  |
| Google Pixel 4XL         |  22 |  19 |   14  |

#### YoloV4-tiny-416 - mAP: 29%

Versión pequeña de [YoloV4](https://arxiv.org/abs/2004.10934) con resolución de entrada de 416x416.

|teléfono/dispositivo (fps)| CPU | GPU | NNAPI |
|--------------------------|-----|-----|-------|
| Samsung S22 Ultra        |  13 | 9.8 |   13  |
| Samsung S20FE 5G         |  12 | 9.4 |  7.7  |
| Huawei P30 Pro           | 8.4 | 7.6 |  6.9  |
| Google Pixel 6XL         |  10 | 9.6 |  7.2  |
| Xiaomi Mi9               | 9.0 | 7.3 |  5.0  |
| Google Pixel 4XL         | 7.2 | 7.4 |  6.2  |

#### YoloV4-224 - mAP: 40%

[YoloV4](https://arxiv.org/abs/2004.10934) con resolución de entrada de 224x224.

|teléfono/dispositivo (fps)| CPU | GPU | NNAPI |
|--------------------------|-----|-----|-------|
| Samsung S22 Ultra        | 3.7 | 5.6 |  3.5  |
| Samsung S20FE 5G         | 3.1 | 7.1 |  4.2  |
| Huawei P30 Pro           | 2.4 | 6.2 |  0.7  |
| Google Pixel 6XL         | 2.7 |  11 |  0.9  |
| Xiaomi Mi9               | 2.1 | 6.4 |  1.7  |
| Google Pixel 4XL         | 1.8 | 5.0 |  3.7  |

#### YoloV5s-320 - mAP: 28%

[YoloV5](https://github.com/ultralytics/yolov5) con resolución de entrada de 320x320.

|teléfono/dispositivo (fps)| CPU | GPU | NNAPI |
|--------------------------|-----|-----|-------|
| Samsung S22 Ultra        |  21 |  10 |   21  |
| Xiaomi Mi9               |  13 |  15 |  0.8  |
| Google Pixel 4XL         |  12 |  17 |   18  |

#### YoloV5s-640 - mAP: 34%

[YoloV5](https://github.com/ultralytics/yolov5) con resolución de entrada de 640x640.

|teléfono/dispositivo (fps)|