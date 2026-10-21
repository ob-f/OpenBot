## Google Firebase

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Google Firebase es una plataforma de desarrollo de aplicaciones móviles y web que ofrece muchos servicios y herramientas para ayudar a los desarrolladores a crear aplicaciones de calidad de forma rápida y eficiente. Incluye base de datos en tiempo real, autenticación de usuarios, alojamiento, almacenamiento en la nube y mucho más, todo integrado en una sola plataforma. Firebase ofrece a los desarrolladores una solución cómoda y escalable para gestionar su infraestructura de backend, para que puedan centrarse en crear buenas experiencias de usuario.

- ### Inicio de sesión con Google en Firebase

  El inicio de sesión con Google en Firebase es una función de la plataforma Firebase que permite a los usuarios iniciar sesión en aplicaciones móviles o web con su cuenta de Google. Así acceden a las aplicaciones de forma segura y cómoda, sin tener que recordar y gestionar credenciales aparte. Firebase gestiona todo el proceso de autenticación, desde verificar la identidad del usuario con Google hasta proporcionar un ID de usuario único que la aplicación puede usar para personalizar la experiencia. También incluye medidas de seguridad adicionales, como la autenticación en dos pasos, para proteger las cuentas frente a accesos no autorizados.

- ### Uso
  En esta aplicación usamos Firebase para el inicio de sesión con Google y así acceder a los proyectos de [OpenBot Playground](../../../../../../../../../../../open-code/README.es-ES.md) subidos a Google Drive. Si clonas esta aplicación Android para compilarla tú mismo, debes configurar tu propio [proyecto de Firebase](#configura-tu-proyecto-de-firebase), porque el inicio de sesión con Google necesita la clave SHA-1 de tu aplicación.
  
  También tendrás que [configurar Firebase](../../../../../../../../../../../open-code/README.es-ES.md) para la aplicación web [OpenBot Playground](https://www.playground.openbot.org/). La aplicación Android obtiene archivos del Google Drive del usuario, creados por los servicios de Google Drive de Firebase. Usa el mismo proyecto de Firebase para la aplicación Android y la aplicación web para que los servicios de Google Drive funcionen.
  
  Si no quieres usar los servicios de OpenBot Playground, no necesitas configurar la autenticación de Firebase ni el inicio de sesión con Google.

  Los archivos `google-services.json` de este repositorio (`robot/android/app/google-services.json` y
  `robot/android/app/src/main/assets/google-services.json`) son **marcadores de posición** con claves e identificadores falsos.
  El inicio de sesión con Google y otras funciones de Firebase no funcionarán hasta que los reemplaces por el archivo real
  descargado de tu propio proyecto de Firebase, como se describe a continuación.

### Requisitos previos
Para integrar Firebase en la aplicación Android de OpenBot para el inicio de sesión con Google, necesitas:
- **Cuenta de Google:** Para usar el inicio de sesión con Google en Firebase necesitas una cuenta de Google. Si no tienes una, puedes [crear](https://accounts.google.com/signup) una cuenta de Google gratuita.
- **Cuenta de Firebase:** Necesitas una cuenta de Firebase y crear un nuevo proyecto de Firebase para tu aplicación Android. Puedes crear un proyecto nuevo desde la [consola de Firebase](https://console.firebase.google.com/).
****

### Configura tu proyecto de Firebase

- Abre la [consola de Firebase](https://console.firebase.google.com/) y crea un `proyecto nuevo`:
  1. Haz clic en «Create Project».
  2. Escribe un nombre para tu proyecto de Firebase y haz clic en «Next».
  3. Puedes desactivar Google Analytics si no quieres usarlo.
  4. Haz clic en «Create Project».
<p>
<img src="../../../../../../../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
<img src="../../../../../../../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width=40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
</p>

- Para añadir una aplicación Android a tu proyecto de Firebase:
  1. Haz clic en el icono de Android en el proyecto de Firebase.
  2. Escribe el `nombre del paquete` de tu aplicación Android. Debe ser `único` en Firebase.
  3. Escribe un apodo para la aplicación.
  4. La **clave SHA-1** es **obligatoria** para el inicio de sesión con Google de Firebase, para firmar el APK de tu aplicación. Escríbela en el campo correspondiente.
  5. Haz clic en `Register`.
  

- La huella `SHA-1` es un identificador único del `certificado de firma de tu aplicación`. Firebase la usa para comprobar la autenticidad de tu aplicación cuando se comunica con sus servidores. Si usas la autenticación de Firebase en tu aplicación, debes proporcionar la clave `SHA-1` `del certificado de firma` con el que se firma la `versión de lanzamiento` de tu aplicación.
  
<p>
<img style="width: 47%" src="../../../../../../../../../../../docs/images/firebase_add_android_app.jpg" alt="Add Android Application" width="40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_package_name.jpg" alt="Package Name" width="36%"/>
</p>

- Para obtener la clave `SHA-1`, usa `keytool`, una herramienta de línea de comandos incluida en el `Java SDK`. Así se usa en `Mac` y en `Windows`:
  

  - **En Mac**
    ```shell
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
    ```
    Este comando muestra los detalles del certificado de firma de depuración que está en `~/.android/debug.keystore`. La opción `-alias` indica el alias del certificado de depuración, y las opciones `-storepass` y `-keypass` las contraseñas del almacén de claves y de la clave.
  

  - **En Windows**
    ```shell 
    keytool -list -v -keystore "%USERPROFILE%.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    ```
    Este comando es parecido al de Mac, pero usa una `ruta distinta` al archivo debug.keystore. `%USERPROFILE%` es una variable de entorno del sistema que apunta a la carpeta del perfil del usuario actual, donde está la carpeta `.android` con el archivo `debug.keystore`.

- Descarga el archivo `google-services.json` y `añádelo` a la `carpeta app` de la aplicación (`robot/android/app/google-services.json`) y a la `carpeta assets` (`robot/android/app/src/main/assets/google-services.json`).
- Haz clic en «Next». Puedes saltarte el tercer paso, porque el **SDK de Firebase** ya está añadido en el archivo gradle de este proyecto.
- Continúa en la consola de Firebase para configurar los servicios de Firebase que quieras usar en tu aplicación Android.

<p>
<img style="width: 40%" src="../../../../../../../../../../../docs/images/firebase_google_service_json_file.jpg" alt="Google services json file" width="41%"/>
<img src="../../../../../../../../../../../docs/images/firebase_continue_to_console.jpg" alt="Continue to console" width="40.2%"/>
</p>

- Para `activar el inicio de sesión con Google` en tu proyecto de Firebase:
  1. Abre la consola de Firebase y selecciona tu proyecto.
  2. Haz clic en `All products` en el `menú lateral` de la izquierda.
  3. Haz clic en `Authentication`.
  4. Haz clic en `Get Started`.
  5. Haz clic en el `icono de Google`.
  6. Haz clic en el `interruptor` para `activar` el inicio de sesión con Google.

<p>
<img style="width:36%" src="../../../../../../../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="39%"/>
</p>
<p>
<img src="../../../../../../../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="26.1%"/>
</p>


- Si te `saltaste el paso de añadir la clave SHA-1` durante la configuración, `todavía puedes añadirla` más tarde:

  - Abre la `consola de Firebase` y selecciona `tu proyecto`.
  - Haz clic en el `icono del engranaje` en la `esquina superior izquierda` y selecciona `Project settings`.
  - En la sección `Your apps`, selecciona la `aplicación Android` a la que quieres `añadir la clave SHA-1`.
  - `Desplázate` hasta la sección `SHA certificate fingerprints` y haz clic en `Add fingerprint`.
  - Escribe la `clave SHA-1` del certificado de firma de tu aplicación.
  - Haz clic en `Save` para añadir la clave SHA-1 a tu proyecto de Firebase.

<p>
<img style="width: 44%;" src="../../../../../../../../../../../docs/images/firebase_gear_icon.jpg" alt="Gear icon" width="30%"/>
<img style="width: 53%" src="../../../../../../../../../../../docs/images/firebase_add_sha1.jpg" alt="Add SHA-1" width="43%"/>
</p>

- Si `ya habías configurado la autenticación de Firebase` antes de añadir la clave SHA-1, puede que tengas que `actualizar` la `configuración` de tu aplicación: `reemplaza` el archivo `google-services.json` de la carpeta del proyecto por el `google-services.json actualizado` de la configuración del proyecto de Firebase.

<p>
<img src="../../../../../../../../../../../docs/images/firebase_updated_google_service_json_file.jpg" alt="Updated Google service json file" width="40%"/>
</p>
