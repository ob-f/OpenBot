## Autenticación de Inicio de Sesión con Google en Firebase

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

- #### Uso
  En nuestra aplicación web, utilizamos Firebase para la autenticación de inicio de sesión con Google para identificar a cada cliente único y prevenir conexiones cruzadas entre la aplicación del robot y el servidor web. Si clonas este proyecto y lo ejecutas en tu dispositivo, debes configurar tu propio proyecto de Firebase porque se requiere la configuración de Firebase para la autenticación de inicio de sesión.
- #### Acerca del Inicio de Sesión con Google
  La Autenticación de Inicio de Sesión con Google en Firebase es una característica de la plataforma Firebase que permite a los usuarios iniciar sesión en aplicaciones móviles o web utilizando sus credenciales de Google. Este servicio proporciona una forma segura y conveniente para que los usuarios accedan a las aplicaciones sin tener que recordar y gestionar credenciales de inicio de sesión separadas. Firebase gestiona todo el proceso de autenticación, desde la verificación de la identidad del usuario con Google hasta proporcionar un ID de usuario único que se puede utilizar para personalizar la experiencia del usuario dentro de la aplicación. Esta característica también incluye medidas de seguridad adicionales, como la autenticación de dos factores, para ayudar a proteger las cuentas de usuario contra accesos no autorizados.

**Nota** - Por favor, sigue la [documentación](../../../../open-code/src/services/README.es-ES.md) del playground de OpenBot para configurar tu proyecto de Firebase y habilitar la autenticación con Google. No es necesario habilitar la API de Google Drive por ahora.

### Configuración de Variables de Entorno

Uso de Variables de Entorno Al usar la Autenticación de Firebase, es posible que necesites almacenar información sensible como claves API, credenciales de base de datos y otros secretos. Para hacer esto de manera segura, puedes usar variables de entorno para almacenar esta información fuera de tu código. Sigue los siguientes pasos.

1. Crea un nuevo archivo en el servidor web llamado .env.

   <img src="../../images/firebase_web_server_env_variable.png" width="30%"/>

2. Agrega las siguientes variables de entorno al archivo .env que se utilizarán en el archivo authentication.js.
      ```bash
      REACT_APP_FIREBASE_API_KEY=<REACT_APP_FIREBASE_API_KEY>
      SNOWPACK_PUBLIC_FIREBASE_API_KEY=<SNOWPACK_PUBLIC_FIREBASE_API_KEY>
      SNOWPACK_PUBLIC_AUTH_DOMAIN=<SNOWPACK_PUBLIC_AUTH_DOMAIN>
      SNOWPACK_PUBLIC_PROJECT_ID=<SNOWPACK_PUBLIC_PROJECT_ID>
      SNOWPACK_PUBLIC_STORAGE_BUCKET=<SNOWPACK_PUBLIC_STORAGE_BUCKET>
      SNOWPACK_PUBLIC_MESSAGING_SENDER_ID=<SNOWPACK_PUBLIC_MESSAGING_SENDER_ID>
      SNOWPACK_PUBLIC_APP_ID=<SNOWPACK_PUBLIC_APP_ID>
      SNOWPACK_PUBLIC_MEASUREMENT_ID=<SNOWPACK_PUBLIC_MEASUREMENT_ID>
   ```