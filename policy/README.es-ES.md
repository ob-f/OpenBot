# Política de Conducción (Avanzada)

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## DESCARGOS DE RESPONSABILIDAD

1. **Seguridad:** Las políticas de conducción no son perfectas y pueden hacer que el robot choque. ¡Asegúrate siempre de operar en un entorno seguro! Ten en cuenta que tu teléfono podría dañarse en una colisión. Asegúrate de tener siempre un controlador de juego conectado y estar familiarizado con el mapeo de teclas para poder detener el vehículo en cualquier momento. ¡Usa bajo tu propio riesgo!
2. **Hardware de cómputo:** Entrenar una política de conducción requiere muchos recursos y puede ralentizar o incluso congelar tu máquina. Se recomienda usar una laptop de alta gama o una estación de trabajo con gran cantidad de RAM y GPU dedicada, especialmente cuando se entrena con tamaños de lote grandes. La documentación actualmente tampoco es muy detallada. ¡Usa bajo tu propio riesgo!
3. **Paciencia requerida:** Obtener una buena política de conducción para tu conjunto de datos personalizado requerirá algo de paciencia. No es sencillo, implica recolección de datos, ajuste de hiperparámetros, etc. Si nunca has entrenado modelos de aprendizaje automático antes, será un desafío y puede llegar a ser frustrante.

Primero necesitas configurar tu entorno de entrenamiento.

## Dependencias

Recomendamos crear un entorno conda para OpenBot. Las instrucciones para instalar conda se pueden encontrar [aquí](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). La forma más fácil de crear un nuevo entorno con todas las dependencias es usar uno de los archivos de entorno proporcionados. En Windows, también necesitarás instalar [Microsoft C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/). Asegúrate de estar en la carpeta `policy` dentro de tu repositorio local de OpenBot. Según tu sistema operativo, ejecuta el comando correspondiente:

- **MacOS**: `conda env create -f environment_mac.yml`
- **Windows**: `conda env create -f environment_win.yml`
- **Linux**: `conda env create -f environment_linux.yml`

Para soporte de GPU, asegúrate de tener también los controladores apropiados instalados. En Mac y Windows, todo debería funcionar de inmediato. En Linux, puedes instalar los controladores con el siguiente comando:
```
sudo apt-get install nvidia-driver-510
```
En Linux, probablemente también necesitarás ejecutar lo siguiente para agregar cuda y cudnn a tu ruta:
```
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```

¡Listo! Estás listo para entrenar tus propios modelos. Si esto no funciona para ti, a continuación se encuentran las instrucciones para configurar dicho entorno manualmente.

### Configuración manual del entorno

Primero crea un nuevo entorno conda con el siguiente comando:

```bash
conda create -n openbot pip python=3.9 -y
```

Luego, necesitas activar tu entorno conda:

```bash
conda activate openbot
```

Si esto no funciona (por ejemplo, en Windows), es posible que necesites activar el entorno con `activate openbot` en su lugar.

Una vez que el entorno esté activo, necesitas instalar tensorflow. Ten en cuenta que el entrenamiento será muy lento en una laptop. Por lo tanto, si tienes acceso a una computadora con GPU dedicada, te recomendamos encarecidamente usarla instalando las bibliotecas necesarias; asegúrate de tener controladores de GPU recientes instalados. A continuación se encuentran los comandos para instalar tensorflow para diferentes sistemas operativos.

#### **Mac OS**
```
conda install -c apple tensorflow-deps -y
pip install tensorflow-macos~=2.9.0
```
Soporte de GPU
```
pip install tensorflow-metal~=0.5.0
```
[Solución de problemas](https://developer.apple.com/metal/tensorflow-plugin/)

#### **Linux**
```
pip install tensorflow~=2.9.0
```
Soporte de GPU
```
sudo apt-get install nvidia-driver-510
conda install -c conda-forge cudatoolkit=11.2 cudnn=8.1 -y
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```
[Solución de problemas](https://www.tensorflow.org/install/pip#linux)

#### **Windows**
```
pip install tensorflow~=2.9.0
```
Soporte de GPU
```
conda install cudatoolkit=11.3 cudnn=8.2 -y
```

#### **Requisitos adicionales**

Asegúrate de estar en la carpeta `policy` dentro de tu repositorio local de OpenBot. Ahora, puedes instalar todas las dependencias restantes con el siguiente comando:

```bash
pip install -r requirements.txt
```

También puedes instalar pydot (`pip install pydot`) y graphviz ([ver instrucciones](https://graphviz.gitlab.io/download/)) si deseas visualizar la arquitectura de la red.

Si deseas usar la [WebApp](#web-app) para la recolección de datos y el entrenamiento, necesitas instalar las siguientes dependencias adicionales. (En Mac, el paquete `brotlipy` está actualmente roto en pip, por lo que necesitas instalarlo primero usando conda: `conda install brotlipy=0.7`)

```bash
pip install -r requirements_web.txt
```

### Paquetes esenciales

Para referencia y solución de problemas, a continuación se muestra una lista de los paquetes esenciales.

Entrenamiento:

- [tensorflow](https://pypi.org/project/tensorflow/)
- [jupyter notebook](https://pypi.org/project/notebook/)
- [matplotlib](https://pypi.org/project/matplotlib/)
- [numpy](https://pypi.org/project/numpy/)
- [PIL](https://pypi.org/project/Pillow/)
- [black[jupyter]](https://pypi.org/project/black/)

Interfaz web:

- [aiohttp](https://pypi.org/project/aiohttp/)
- [aiozeroconf](https://pypi.org/project/aiozeroconf/)
- [imageio](https://pypi.org/project/imageio/)

### Notas

- Recuerda activar el entorno antes de ejecutar comandos en la terminal: `conda activate openbot`
- Si la importación de tensorflow no funciona, intenta instalarlo a través de `pip install tensorflow --user`. (Ver este [problema](https://github.com/ob-f/OpenBot/issues/98).)

## Conjunto de Datos

### Recolección de Datos

Para entrenar una política de conducción autónoma, primero necesitarás recolectar un conjunto de datos. Cuantos más datos recolectes, mejor será la política de conducción resultante. Para los experimentos en nuestro artículo, recolectamos aproximadamente 30 minutos de datos. Ten en cuenta que la red imitará tu comportamiento de conducción. Cuanto mejor y más consistente conduzcas, mejor aprenderá la red a conducir.

1. Conecta un controlador de juego bluetooth al teléfono (por ejemplo, controlador PS4: para entrar en modo de emparejamiento, presiona los botones PS y share hasta que el LED parpadee rápidamente).
2. Selecciona el modelo `CIL-Mobile-Cmd` en la aplicación.
3. Ahora conduce el coche a través de un controlador de juego y graba un conjunto de datos. En el controlador PS4, el registro se puede alternar con el botón **X**.

Ahora encontrarás una carpeta llamada *Documents/OpenBot* en el almacenamiento interno de tu smartphone. Para cada grabación, habrá un archivo zip. El nombre del archivo zip estará en el formato *yyyymmdd_hhmmss.zip* correspondiente a la marca de tiempo de cuando se inició la grabación.

El cuaderno de Jupyter espera una carpeta llamada `dataset` en la misma carpeta. En esta carpeta, debe haber dos subcarpetas, `train_data` y `test_data`. Los datos de entrenamiento se utilizan para aprender la política de conducción. Los datos de prueba se utilizan para validar la política de conducción aprendida en datos no vistos durante el proceso de entrenamiento. Esto proporciona alguna indicación de qué tan bien funcionará esta política en el robot. Aunque el robot puede conducir por la misma ruta vista durante el entrenamiento, las imágenes exactas observadas serán ligeramente diferentes en cada ejecución. La división común es 80% datos de entrenamiento y 20% datos de prueba. Dentro de las carpetas `train_data` y `test_data`, necesitas crear una carpeta para cada sesión de grabación y darle un nombre como `my_openbot_1`, `my_openbot_2`, etc. La idea aquí es que cada sesión de grabación puede tener diferentes condiciones de iluminación, un robot diferente, una ruta diferente. En el cuaderno de Jupyter, puedes entrenar solo en un subconjunto de estos conjuntos de datos o en todos ellos. Dentro de cada carpeta de sesión de grabación, debes colocar todas las grabaciones de esa sesión de grabación. Cada grabación corresponde a un archivo zip extraído que has transferido desde la carpeta *Openbot* en tu teléfono. Tu carpeta de conjunto de datos debería verse así:

<img src="../docs/images/folder_structure.png" width="200" alt="estructura de carpetas" />

En lugar de copiar todos los archivos manualmente desde el teléfono, también puedes subir los registros automáticamente a un [servidor Python](#web-app) en tu computadora. En este caso, los archivos zip se subirán y desempaquetarán en la carpeta `dataset/uploaded`. Aún necesitarás moverlos a la estructura de carpetas para el entrenamiento. Puedes simplemente tratar la carpeta `uploaded` como una sesión de grabación y moverla a `train_data`. Las grabaciones serán entonces reconocidas como datos de entrenamiento por el cuaderno de Jupyter. Si aún no tienes una sesión de grabación en la carpeta `test_data`, también necesitas mover al menos una grabación de `train_data/uploaded` a `test_data/uploaded`.

### Conversión de Datos (opcional)

Para un mejor rendimiento de entrenamiento, puedes convertir el conjunto de datos recolectado en un formato especializado. Puedes crear un tfrecord de los conjuntos de datos de entrenamiento y prueba con los siguientes comandos:

```bash
conda activate openbot
python -m openbot.tfrecord -i dataset/train_data -o dataset/tfrecords -n train.tfrec
python -m openbot.tfrecord -i dataset/test_data -o dataset/tfrecords -n test.tfrec
```

Por defecto, esta conversión se realizará automáticamente al inicio del entrenamiento.

## Entrenamiento de la Política

Asegúrate de que tu entorno conda para openbot esté activado ejecutando el siguiente comando:

```bash
conda activate openbot
```

### Cuaderno de Jupyter

Proporcionamos un [Cuaderno de Jupyter](policy_learning.ipynb) que te guía a través de los pasos para entrenar una política de conducción autónoma. Abre el cuaderno con el siguiente comando.

```bash
jupyter notebook policy_learning.ipynb
```

Ahora se abrirá automáticamente una ventana del navegador web y cargará el cuaderno de Jupyter. Sigue los pasos para entrenar un modelo con tus propios datos.

### Shell

Este método asume que los datos están en el lugar correcto. Para ajustar los hiperparámetros, puedes pasar los siguientes argumentos.

```bash
'--no_tf_record', action='store_true', help='no cargar un tfrecord sino un directorio de archivos'
'--create_tf_record', action='store_true', help='crear un nuevo tfrecord'
'--model', type=str, default='pilot_net', choices=['cil_mobile', 'cil_mobile_fast', 'cil', 'pilot_net'], help='arquitectura de red (por defecto: cil_mobile)'
'--batch_size', type=int, default=16, help='número de épocas de entrenamiento (por defecto: 16)'
'--learning_rate', type=float, default=0.0001, help='tasa de aprendizaje (por defecto: 0.0001)'
'--num_epochs', type=int, default=10, help='número de épocas (por defecto: 10)'
'--batch_norm', action='store_true', help='usar normalización por lotes'
'--flip_aug', action='store_true', help='voltear imágenes y controles aleatoriamente para la augmentación'
'--cmd_aug', action='store_true', help='agregar ruido a la entrada de comandos para la augmentación'
'--resume', action='store_true', help='reanudar el entrenamiento anterior'
```

Si tu conjunto de datos ya ha sido convertido a un tfrecord, puedes entrenar la política desde la shell con el comando:

```bash
python -m openbot.train
```

Si deseas convertir tu conjunto de datos a un tfrecord antes de entrenar, necesitas agregar la siguiente bandera:

```bash
python -m openbot.train --create_tf_record
```

Si no deseas convertir el conjunto de datos a un tfrecord y entrenar usando los archivos directamente, necesitas agregar la siguiente bandera:

```bash
python -m openbot.train --no_tf_record
```

Para entrenar un modelo para el despliegue final, querrás usar un tamaño de lote grande y un número de épocas. Habilitar la normalización por lotes generalmente mejora el entrenamiento también. El modelo `pilot_net` es más grande que el `cil_mobile` por defecto, pero puede lograr un mejor rendimiento en algunas tareas mientras sigue funcionando en tiempo real en la mayoría de los smartphones.

```bash
python -m openbot.train --model pilot_net --batch_size 128 --num_epochs 100 --batch_norm
```

### Despliegue

Al final del proceso de entrenamiento, se generan dos archivos tflite: uno corresponde al mejor punto de control según las métricas de validación y el otro al último punto de control. Elige uno de ellos y renómbralo a autopilot_float.tflite. Reemplaza el modelo existente en Android Studio y recompila la aplicación.

<p align="center">
  <img src="../docs/images/android_studio_tflite_dir.jpg" width="200" alt="App GUI" />
</p>

Si estás buscando la carpeta en tu directorio local, la encontrarás en: `app/src/main/assets/networks`.

## Aplicación Web

Proporcionamos una aplicación web y un servidor web en Python para facilitar el entrenamiento de políticas. (Beta)

### Características

- Carga automática de registros (sesiones)
  - ver Solución de problemas para más detalles
- Listar sesiones subidas, con vista previa en GIF
- Listar conjuntos de datos, con información básica
- Mover sesión a un conjunto de datos
- Eliminar sesión
- Listar modelos entrenados y mostrar gráficos sobre el entrenamiento
- Entrenar un modelo con parámetros básicos, mostrar barra de progreso

### Vista previa

<img src="../docs/images/web-app.gif" width="100%" alt="Vista previa de la aplicación web" />

### Inicio rápido

```bash
conda activate openbot
python -m openbot.server
```

Ahora puedes abrir tu navegador para visualizar el conjunto de datos y ver las cargas entrantes yendo a:
[http://localhost:8000/#/uploaded](http://localhost:8000/#/uploaded)

### Ejecutando el servidor

Puedes ejecutar el servidor de Python con el comando:

```bash
python -m openbot.server
```

También hay un modo desarrollador:

```bash
adev runserver openbot/server
```

Para el desarrollo del frontend (aplicación react):

```
FE_DEV=1 adev runserver openbot/server
```

Cuando ejecutes el servidor deberías ver algo como:

```
Skip address 127.0.0.1 @ interface lo
Found address 192.168.x.x @ interface wlp2s0
Registration of a service, press Ctrl-C to exit...
Running frontend: 0.7.0
Frontend path: /home/USERNAME/miniconda3/envs/openbot/lib/python3.7/site-packages/openbot_frontend
======== Running on http://0.0.0.0:8000 ========
(Press CTRL+C to quit)
```

### Solución de problemas

Si la carga al servidor no está funcionando, aquí hay algunos consejos para solucionar problemas:

- Intenta reiniciar el servidor (computadora) y la aplicación OpenBot (smartphone)
- Asegúrate de que el smartphone y tu computadora estén conectados a la misma red WiFi
- Si tu router tiene redes de 2.4 GHz y 5 GHz con el mismo nombre, desactiva la red de 5 GHz
- Mantén el teléfono conectado a Android Studio mientras ejecutas la aplicación. En la pestaña Logcat, selecciona Debug en el menú desplegable. Escribe `NSD` en el campo de filtro para ver los mensajes de depuración relacionados con la conexión del servidor. Escribe `Upload` en el campo de filtro para ver los mensajes de depuración relacionados con la carga del archivo de grabación.
- Si un modelo publicado se descarga continuamente, asegúrate de que la hora en tu teléfono y laptop/estación de trabajo esté configurada correctamente.