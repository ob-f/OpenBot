# Flutter Controller App

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Esta aplicación de control sirve como un `control remoto` para el vehículo [OpenBot](https://www.openbot.org) similar a un controlador BT (por ejemplo, PS3/4 o Xbox). Se ejecuta en otro dispositivo Android/iOS y admite transmisión de video/audio en vivo además del control.

## Comenzando
Comienza instalando [Flutter](https://flutter.dev/) en tu sistema. Elige la descarga adecuada para tu sistema operativo, que incluye opciones para Windows, macOS, Linux y ChromeOS. Sigue la guía oficial de instalación de Flutter para obtener instrucciones detalladas: [Guía de Instalación de Flutter](https://docs.flutter.dev/get-started/install)

### Usando Terminal
- Una vez que Flutter esté instalado correctamente, abre tu **terminal** o **símbolo del sistema**.
- Cambia tu directorio actual a la ubicación donde se almacena el proyecto OpenBot y luego navega a `OpenBot/controller/flutter`.
- Usa los siguientes comandos para ejecutar la aplicación Flutter desde la terminal.

  #### Instalar Dependencias:
    ```bash
     flutter pub get 
    ```
    Ejecutar el proyecto:
    ```bash
     flutter run
    ```
    Si encuentras algún problema, ejecuta el siguiente comando:
    ```bash
     flutter doctor
    ```
### Usando Editor
- Sigue la guía oficial de Flutter para configurar un editor: [Configurar un editor](https://docs.flutter.dev/tools/android-studio) 
- Asegúrate de que tu editor esté configurado para el desarrollo con Flutter. Instala los plugins o extensiones necesarios, siguiendo las instrucciones específicas del editor en la documentación de Flutter para la mejor experiencia de desarrollo.

- Una vez que abras tu proyecto en el editor después de la configuración, aparecerá como se muestra en la siguiente imagen.

  <p float="left">
    <img src="../../docs/images/android_editor.jpg" width="50%" />
  </p>

- Sigue las instrucciones similares a las mencionadas anteriormente para ejecutar Flutter en la terminal y ejecuta directamente usando el botón ``run`` para repeticiones futuras.

  <p float="left">
    <img src="../../docs/images/run_editor.jpg" width="50%" />
  </p>

## Conexión 

Cuando se inicia la aplicación de control, intenta conectarse inmediatamente al robot y muestra la siguiente pantalla:

<p float="left">
  <img src="../../docs/images/flutter_controller_home.jpg" width="50%" />
</p>

Para conectar el controlador al robot, configura el modo de control del robot en **Phone**.
Por ejemplo, en el `FreeRoamFragment` el modo teléfono se activa así:

<p float="left">
  <img src="../../docs/images/phone_selection.gif" width="50%" />
</p>

Una vez conectado, la aplicación de control se verá así:

<p float="left">
  <img src="../../docs/images/flutter_controller_connected.jpg" width="50%" />
</p>

Aquí puedes seleccionar conducir el robot inclinando el teléfono o usando los controles en pantalla.

***Nota:*** Esto debería ser suficiente para conectar, pero si la conexión no se puede establecer después de 30 segundos, alterna
la configuración de `Control` en la aplicación del robot a `Gamepad` y luego a `Phone` nuevamente para reiniciar la conexión. Si eso
falla, cierra la aplicación de control y vuelve a iniciarla. Alterna el modo de control nuevamente en la aplicación del robot.

## Operación

### Controles en pantalla

Este modo permite al usuario controlar el coche robot a través de dos deslizadores en modo `Dual Drive`. Puedes girar a la izquierda/derecha moviendo el pulgar del deslizador hacia arriba y hacia abajo en cada lado. Las ruedas de cada lado giran hacia adelante/atrás cuando se mueve el pulgar por encima/debajo del centro del deslizador.

<p float="left">
  <img src="../../docs/images/flutter_controller_dual_drive_mode.jpg" width="50%" />
</p>

- ``Indicadores``: También puedes configurar los indicadores de giro a la izquierda/derecha <img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/> <img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/> haciendo clic en las flechas en la parte superior izquierda de la pantalla.

- ``Cambiar Cámara``: cambiar entre los modos de cámara frontal y trasera.
- ``Silenciar``: habilitar/deshabilitar la transmisión de audio.
- ``Vista en espejo``: espejar la transmisión de video. 

### Inclinar para conducir

El controlador también puede usar su sensor de movimiento acelerómetro para conducir el robot. Si seleccionas esta opción, el
controlador entrará en un modo de pantalla completa (Zen) con solo el video mostrando y los pedales de `freno` y `acelerador`. Para
salir de este modo, toca dos veces en la pantalla.

Aquí hay una imagen de la pantalla del `modo inclinación`:

<p float="left">
  <img src="../../docs/images/flutter_controller_tilt_mode.jpg" width="50%" />
</p>

Usa los botones de `acelerador` y `freno` para moverte hacia adelante/atrás.

- Presionar el `acelerador` acelerará el robot a toda velocidad en 2 segundos. Cuando sueltes el botón, el
  robot se desacelerará hasta detenerse (la velocidad de parada se establece en 0% de la velocidad máxima, se puede ajustar).
- Presionar el botón de `freno` detendrá inmediatamente el robot. Si mantenemos el freno durante otro segundo, el robot
  comenzará a moverse hacia atrás hasta alcanzar la velocidad máxima en reversa en un segundo. Cuando soltemos el freno, el
  robot se detendrá.
- El robot se dirige inclinando el teléfono controlador hacia la izquierda o la derecha.

Aquí hay una [Descripción Técnica](../../docs/technical/OpenBotController.pdf) de la aplicación de control.