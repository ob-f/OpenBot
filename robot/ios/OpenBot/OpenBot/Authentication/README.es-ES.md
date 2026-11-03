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
  En esta aplicación usamos Firebase para el inicio de sesión con Google y así acceder a los proyectos de [OpenBot Playground](../../../../../open-code/README.es-ES.md) subidos a Google Drive.

- ### Nota
  Si clonas esta aplicación iOS para compilarla tú mismo, también tendrás que [configurar Firebase](../../../../../open-code/README.es-ES.md) para la aplicación web [OpenBot Playground](https://www.playground.openbot.org/). La aplicación iOS obtiene archivos del Google Drive del usuario, creados por los servicios de Google Drive de Firebase. Usa el mismo proyecto de Firebase para la aplicación iOS y la aplicación web para que los servicios de Google Drive funcionen.

  El archivo `GoogleService-Info.plist` de este repositorio es un **marcador de posición** con claves e identificadores falsos.
  El inicio de sesión con Google y otras funciones de Firebase no funcionarán hasta que lo reemplaces por el archivo real
  descargado de tu propio proyecto de Firebase, como se describe a continuación.

- ### Requisitos previos
  Para integrar Firebase en la aplicación iOS de OpenBot para el inicio de sesión con Google, necesitas:
- **Cuenta de Google:** Para usar el inicio de sesión con Google en Firebase necesitas una cuenta de Google. Si no tienes una, puedes [crear](https://accounts.google.com/signup) una cuenta de Google gratuita.
- **Cocoapods:** un gestor de dependencias para proyectos Swift y Objective-C. Para instalar el SDK de Firebase, necesitas tener Cocoapods instalado en tu sistema.

### Configura tu proyecto de Firebase

- Abre la [consola de Firebase](https://console.firebase.google.com/) y crea un `proyecto nuevo`:
    1. Haz clic en «Create Project».
    2. Escribe un nombre para tu proyecto de Firebase.
    3. Haz clic en «Next» y desactiva Google Analytics si no quieres usarlo.
    4. Haz clic en «Create Project».

    <p>
    <img src="../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
    <img src="../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width="40%"/>
    <img src="../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
    </p>

- Para añadir una aplicación iOS a tu proyecto de Firebase:
    1. Haz clic en el icono de iOS en el proyecto de Firebase.
    2. Escribe el ID de paquete (bundle ID) de tu aplicación. Debe ser el mismo que usa tu proyecto de Xcode.
    3. Escribe un apodo para la aplicación.
    4. Haz clic en «Register app» para terminar.
    5. Descarga el archivo GoogleService-Info.plist y añádelo a tu proyecto de Xcode. Contiene la configuración que tu aplicación necesita para usar Firebase.

       <p>
       <img src="../../../../../docs/images/firebase_add_ios_app.jpg" alt="Add IOS Application" width="47.5%"/>
       <img src="../../../../../docs/images/firebase_bundle_id.jpg" alt="Bundle ID Name" width="30.5%"/>
       </p>

    6. Copia el valor `REVERSED_CLIENT_ID` de tu nuevo `GoogleService-Info.plist` y pégalo en la entrada
       `CFBundleURLSchemes` (dentro de `CFBundleURLTypes`) de `Info.plist`. El inicio de sesión con Google no funcionará hasta que este valor coincida con tu propio proyecto de Firebase.
    7. Haz clic en `Next` y sáltate el tercer paso (Add Firebase SDK) y el cuarto (Add initialization code), porque ya están hechos en este proyecto.
    8. Continúa en la consola de Firebase para configurar los servicios de Firebase que quieras usar en tu aplicación iOS.

   <p>
     <img src="../../../../../docs/images/firebase_google_service_info_plist.jpg" alt="Add IOS Application" width="35%"/>
     <img src="../../../../../docs/images/firebase_continue_to_console_ios.jpg" alt="Bundle ID Name" width="35%"/>
   </p>

- Para `activar el inicio de sesión con Google` en tu proyecto de Firebase:
    1. Abre la consola de Firebase y selecciona tu proyecto.
    2. Haz clic en `All products` en el `menú lateral` de la izquierda.
    3. Haz clic en `Authentication`.
    4. Haz clic en `Get Started`.
    5. Haz clic en el `icono de Google`.
    6. Haz clic en el `interruptor` para `activar` el inicio de sesión con Google.

    <p>
    <img src="../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="44.5%"/>
    <img src="../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="48%"/>
    </p>
    <p>
    <img src="../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="60%"/>
    <img src="../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="31.5%"/>
    </p>

### Configura el proyecto de iOS

El SDK de Firebase se instala con CocoaPods. El repositorio ya incluye un Podfile con todos los pods necesarios. Abre la aplicación Terminal en tu Mac, ve a `robot/ios/OpenBot` y ejecuta:

```
pod install
```

Después abre `OpenBot.xcworkspace` (no `OpenBot.xcodeproj`) en Xcode.

### Solución de problemas

Problemas habituales al configurar Firebase y sus soluciones:

```shell
1. Errores al instalar CocoaPods: si tienes problemas al instalar CocoaPods, por ejemplo si pod install no se ejecuta o no instala la versión correcta del SDK de Firebase, prueba lo siguiente:
```

- Actualiza CocoaPods con sudo gem install cocoapods.
- Borra el archivo Podfile.lock y vuelve a ejecutar pod install.
- Comprueba que has añadido bien el pod del SDK de Firebase a tu Podfile, con la versión exacta indicada en la consola de Firebase.
```shell
2. Reinstalar los pods (si tienes problemas de compilación o de paquetes)
```
- pod deintegrate 
- rm -rf Pods
- rm -rf Podfile.lock
- pod install

```shell
3. Errores de configuración de Firebase: si no consigues configurar Firebase, por ejemplo si Firebase no se inicializa en tu aplicación, prueba lo siguiente:
```

- Comprueba que has seguido bien todos los pasos de la sección «Configura el proyecto de iOS» de este documento.
- Comprueba que has añadido bien el archivo de configuración de Firebase (GoogleService-Info.plist) a tu proyecto de Xcode.
- Comprueba que el SDK de Firebase está bien añadido a tu proyecto de Xcode y se importa en tu código.
- Comprueba que tu proyecto de Firebase está bien configurado en la consola de Firebase, incluido el ID de paquete y otros ajustes del proyecto.
- Conflictos con otras bibliotecas: si tienes conflictos con otras bibliotecas de tu proyecto, por ejemplo dependencias contradictorias o versiones de SDK incompatibles, prueba lo siguiente:
- Comprueba que tu Podfile indica la versión correcta del SDK de Firebase.
- Actualiza tus otras bibliotecas a la última versión y comprueba si el problema desaparece.
- Busca dependencias o versiones de bibliotecas contradictorias y resuélvelas eliminando o actualizando las bibliotecas afectadas.
