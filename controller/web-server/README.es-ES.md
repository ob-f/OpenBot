# Controlador Web de OpenBot

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Nomenclatura

Aquí hay algunos términos que usaremos en este documento:

* ```Robot, bot``` - este es el software de Android que se ejecuta en el teléfono en el vehículo [OpenBot](https://www.openbot.org/).
* ```Servidor``` - el servidor web, la parte del servidor de este proyecto.
* ```Cliente, UI``` - esta es la parte del cliente de este proyecto. Se ejecuta en el navegador.

## Introducción

Este proyecto opera de forma remota en un servidor en la nube, funcionando como un controlador para el vehículo [OpenBot](https://www.openbot.org/). El software consta de dos componentes: un servidor y un cliente. El servidor es una aplicación en la nube desplegada en un entorno remoto. El componente cliente se ejecuta directamente en el navegador web. Aquí hay una captura de pantalla del navegador:

<img src="../../controller/web-server/images/openbot_controller.jpg" width="50%"/>

## Empezando

Puedes ejecutar este software en una PC, un dispositivo tipo RaspberryPi o incluso en dispositivos [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/) que soporten el entorno ```Node.js```. Primero asegúrate de haber instalado [Node.js](https://nodejs.org/), versión 10 o más reciente. Verifica la versión:

    node --version

El software se encuentra en el directorio ```/controller/web-server``` del proyecto OpenBot. Después de descargar el código desde [github](https://github.com/ob-f/OpenBot), cambia a este directorio y ejecuta los siguientes comandos:

    npm install
    npm start

El último comando iniciará el servidor. Si deseas ejecutar el servidor sin una terminal en segundo plano, en ```Linux/MacOS``` puedes ejecutar:

    npm run start-nohup

o simplemente:

    nohup npm start

## Características del controlador web

- El controlador de OpenBot opera en un servidor en la nube accesible de forma remota a través de internet. Los clientes pueden acceder al controlador directamente, permitiendo un proceso de ``inicio de sesión`` conveniente usando sus cuentas de Google. Además, en la aplicación del robot, los clientes utilizan el ``mismo ID de correo electrónico`` que su inicio de sesión en el controlador web para la autenticación. Esta implementación asegura que no haya conexiones cruzadas entre servidores y clientes.

- El servidor está alojado en ``ws://verdant-imported-peanut.glitch.me`` usando una conexión WebSocket segura, asegurando una comunicación rápida y confiable. Esta configuración permite a los clientes conectarse y controlar el robot desde cualquier ubicación sin depender de un servidor local.

- El controlador web está diseñado para facilitar la transmisión de video a través de WebRTC, un protocolo de comunicación en tiempo real. La latencia experimentada por los usuarios depende de la velocidad de su conexión a internet y la ubicación geográfica del servidor. Esto significa que los usuarios con conexiones a internet más rápidas y servidores ubicados más cerca generalmente experimentarán una menor latencia durante la sesión de transmisión de video.

- En la aplicación Android del Robot, ve al panel ```General``` y selecciona ```Web``` como el controlador. Esto conectará la aplicación Android al servidor en la nube, y un video aparecerá en la UI.

## Cómo Funciona

1. El WebSocket crea una conexión con el servidor en la nube. El servidor inicia una solicitud de identificación, típicamente en forma de dirección de correo electrónico, desde el navegador del usuario. Simultáneamente, la aplicación del robot se autentica usando Google Sign-In. Cuando el modo de controlador está configurado en ``Web``, el servidor solicita al usuario su dirección de correo electrónico.

    <img src="../../controller/web-server/images/web_server_signIn.gif" width="50%"/>

2. Cuando un usuario inicia sesión a través de Google en el navegador, el correo electrónico asociado con la cuenta se transmite al servidor. Posteriormente, se genera dinámicamente una ``sala`` dedicada, con el correo electrónico del usuario sirviendo como el ``identificador único``. Dentro de esta sala, se establecen dos candidatos. El candidato inicial se configura para el cliente del navegador, que luego entra en un estado de espera, listo para establecer una conexión con la aplicación del robot.

    <img src="../../controller/web-server/images/set_controller.gif" height="25%" width="25%">

3. Después de configurar el controlador en ```web```, la sala alcanza su capacidad máxima, con el segundo candidato designado como la aplicación del robot. Concurrentemente, la aplicación del robot envía una solicitud de oferta para WebRTC (Comunicación en Tiempo Real por Web). El primer candidato, que está asignado al cliente del navegador, responde con una respuesta a esta solicitud. Este intercambio exitoso resulta en el establecimiento de una conexión robusta y funcional. Y muestra la transmisión de video en la UI del navegador.

    <img src="../../controller/web-server/images/web_server_video_streaming.gif" width="50%"/>

4. El usuario ingresa comandos de teclado desde el navegador. Estas pulsaciones de teclas se envían al Servidor a través del WebSocket o webrtc. El servidor convierte estos comandos en comandos que el Robot puede entender, como ```{driveCmd: {l:0.4, r:0.34}}``` (una lista de todos los comandos se puede encontrar en la documentación del controlador de Android [aquí](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)). Estos comandos se envían al Robot a través de la conexión Socket.
5. El WebSocket sirve como un canal de datos crucial para el proxy de señalización de WebRTC. WebRTC aprovecha eficientemente las conexiones de socket abiertas existentes para este propósito, eliminando la necesidad de conexiones o configuraciones adicionales. Este enfoque simplificado mejora la eficiencia y minimiza los requisitos de configuración para una comunicación en tiempo real sin problemas.

### Crear tu propio servidor

- ``Configuración del servidor``: El código inicia un servidor WebSocket que escucha en el puerto 8080. Al estar listo el servidor, registra un mensaje de confirmación sobre su estado activo en el puerto especificado. Posteriormente, se inicializa un Mapa llamado "rooms", que sirve como un repositorio para gestionar y almacenar detalles sobre salas individuales. Cada sala se identifica de manera única por un ID.
  
- ``Manejo de Conexión del Cliente``: El cliente y la aplicación del robot actúan como dos candidatos en el proceso de generación de salas para establecer un servidor de forma remota. El sistema registra la conexión de un cliente, proporcionando información sobre el número total de clientes conectados. La función askIdOfClient interactúa con los clientes, solicitándoles que compartan sus respectivos IDs de sala. Además, el sistema escucha los mensajes entrantes de los clientes. El cliente del navegador, funcionando como el candidato inicial, se configura y pasa a un estado de espera, preparado para iniciar una conexión con la aplicación del robot.

- ``Gestión de Salas``: La función createOrJoinRoom evalúa la existencia de una sala identificada por el roomId especificado. Si la sala no está presente, inicia la creación de una nueva sala. En los casos en que la sala ya existe, la función facilita la unión a la sala existente, teniendo en cuenta su disponibilidad.

- ``Manejo de Desconexión del Cliente e Interacción``: Al desconectarse un cliente, el sistema genera registros que incluyen información sobre el número total de clientes conectados. Si hay salas asociadas, estas salas se cierran y la entrada del cliente desconectado se elimina del Mapa de salas. Además, el servidor solicita a los clientes que proporcionen sus respectivos IDs de sala durante el proceso de conexión.

- ``Funciones de Difusión``: 
  - wss.broadcast: Difunde un mensaje a todos los clientes conectados.
  - broadcastToRoom: Difunde un mensaje a todos los clientes dentro de una sala específica.
  - sendToBot: Envía un mensaje a un bot (difunde a todos los clientes excepto al remitente).

- Una vez que el cliente del navegador responde con una respuesta a la solicitud de oferta, finalmente llevará a la visualización de una transmisión de video en la interfaz de usuario del navegador.

## Implementación de servidor personalizado:

Para fines de prueba, hemos abierto un nuevo servidor remoto en ``glitch`` pero puedes usar cualquier entorno en la nube para la comunicación de tu servidor web con la aplicación openBot.
- Primero crea una nueva cuenta en [Glitch](https://glitch.com/). Luego, crea tu nuevo proyecto como se muestra en la imagen a continuación.

    <img src="../../controller/web-server/images/glitch.jpg" width="50%"/>

- Después, debes insertar el código del archivo [server.js](server/server.js) en el archivo server.js de tu proyecto (servidor remoto) como se muestra en la siguiente imagen.

    <img src="../../controller/web-server/images/server_code.jpg" alt="server code image" width="50%"/>

- En el siguiente paso, debes agregar la dependencia ``webSocket`` en tu proyecto como puedes ver en la imagen a continuación.

    <img src="../../controller/web-server/images/dependency.jpg" alt="server code image" width="50%"/>

- Para establecer tu propio servidor, necesitas especificar el nombre del proyecto en el archivo authentication.js, como se muestra, para iniciar la conexión WebSocket.

    ``new WebSocket(`ws://gossamer-southern-hygienic`);``

## Desarrollo

Este código usa [snowpack](https://www.snowpack.dev/) para una herramienta de construcción rápida y ligera.

Usamos [eslint](https://eslint.org/) para el linting y formateo automático de tu código. Se recomienda que ejecutes lint y corrijas cualquier error antes de comprometer nuevo código. Si estás usando Visual Code, puedes obtener un plugin [aquí](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint). Ejecuta el linter así:

    npm run lint

## Producción

Para construir una versión de producción del ```cliente```, ejecuta:

    npm run build

Esto optimizará el código del cliente en un directorio ```build```, que puede ser desplegado en un servidor. Además, necesitamos configurar un gestor de procesos para reiniciar el servidor, y posiblemente un proxy inverso como [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/), lo cual aún no se ha hecho.

## Solución de Problemas

* A veces el navegador no mostrará el menú de comandos, solo el título. Esto significa que no se pudo establecer la conexión WebSocket. Esto usualmente ocurre justo después de iniciar el servidor. Si examinas la consola del navegador, puedes encontrar un mensaje sobre no poder conectar, algo como ```WebSocket connection to 'ws://localhost:8081/ws' failed```. Mata todos los procesos de node (pkill -9 node) y reinícialo. Recarga la página y la conexión debería establecerse.
* Si no puedes conectar el teléfono a la aplicación, asegúrate de que no haya otra instancia de esta aplicación ejecutándose en esta máquina o en otra máquina con el mismo correo electrónico.

## Errores Conocidos

Ninguno.

## Cosas por hacer/probar

* Este software no ha sido probado en Windows. Sería útil si alguien puede probarlo y actualizar esta documentación.
* Necesitamos investigar si podemos conectarnos al servidor de forma remota, y si WebRTC seguirá funcionando. Deberíamos documentar la configuración del firewall para hacer esto posible.
* Necesitamos crear una configuración de ```producción```, posiblemente usando [pm2 process manager](https://www.npmjs.com/package/pm2) y [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/).