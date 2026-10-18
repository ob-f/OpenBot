# Aplicaciones Android

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Características

Haz clic en los enlaces a continuación para leer sobre las características de las aplicaciones.

- [Aplicación Robot](robot/README.es-ES.md)
- [Aplicación Controlador](controller/README.es-ES.md)

## Instalar las aplicaciones

La forma más fácil de obtener cualquiera de las aplicaciones es descargándola directamente al teléfono usando el código QR correspondiente. Si estás en el navegador del teléfono, también puedes hacer clic en el código QR. Luego puedes abrir el apk en tu teléfono e [instalarlo](https://www.lifewire.com/install-apk-on-android-4177185). Ten en cuenta que el apk solo está firmado con una clave de depuración.

<table style="width:100%;border:none;text-align:center">
  <tr>
    <td>  <a href="https://app.openbot.org/robot" target="_blank">
    <img alt="🤖 App" width="50%" src="../docs/images/robot_app_qr_code.png" />
  </a>
    </td>
    <td>
  <a href="https://app.openbot.org/controller" target="_blank">
    <img alt="🎮 App" width="50%" src="../docs/images/controller_app_qr_code.png" />
  </a>
      </td>
  </tr>
  <tr>
    <td>🤖 App</td>
    <td>🎮 App</td>
  </tr>
</table>

Alternativamente, puedes descargar los apks desde los assets de cualquier [release](https://github.com/ob-f/OpenBot/releases). Si deseas la última aplicación de la rama principal, también puedes descargarla desde los artefactos de compilación [aquí](https://github.com/ob-f/OpenBot/actions?query=workflow%3A%22Java+CI+with+Gradle%22). Ten en cuenta que puede no ser estable. Si deseas realizar cambios en la aplicación más adelante, sigue los pasos a continuación para compilar la aplicación y desplegarla en tu teléfono.

> **Nota:** El archivo `Controller-<version>.apk` de cada release ahora se compila a partir de la [aplicación Flutter Controller](../controller/flutter/README.es-ES.md) en lugar de la antigua aplicación de control para Android de esta carpeta. El controlador de Flutter usa un ID de paquete distinto (`org.openbot.app.controller` en lugar de `org.openbot.controller`), por lo que **no** actualizará una instalación existente del controlador antiguo. Si ya tienes instalado el controlador antiguo, instala el nuevo por separado; después puedes desinstalar la aplicación antigua.

## Compilar las aplicaciones

### Requisitos previos

- [Android Studio Electric Eel | 2022.1.1 o posterior](https://developer.android.com/studio/index.html) para compilar e instalar los apks.
- Dispositivo Android y entorno de desarrollo Android con API mínima 21.
- Actualmente, usamos API 33 como SDK de compilación y API 32 como SDK de destino. Debería instalarse automáticamente, pero si no es así, puedes instalar el SDK manualmente. Ve a Android Studio -> Preferences -> Appearance & Behaviour -> System Settings -> Android SDK. Asegúrate de que API 33 esté marcada y haz clic en aplicar.

![Android SDK](../docs/images/android_studio_sdk.jpg)

### Proceso de compilación

1. Abre Android Studio y selecciona *Open an existing Android Studio project*.
2. Selecciona el directorio OpenBot/android y haz clic en OK.
3. Si deseas instalar la [aplicación OpenBot](app/README.es-ES.md) asegúrate de seleccionar la configuración *app*. Si deseas instalar la [aplicación Controlador](controller/README.es-ES.md), selecciona la configuración *controller*. Confirma la sincronización de Gradle si es necesario. Para realizar una sincronización de Gradle manualmente, haz clic en el icono de gradle.
  ![Gradle Sync](../docs/images/android_studio_bar_gradle.jpg)
4. Conecta tu dispositivo Android y asegúrate de que la depuración USB en las [opciones de desarrollador](https://developer.android.com/studio/debug/dev-options) esté habilitada. Dependiendo de tu entorno de desarrollo, pueden ser necesarios [otros pasos](https://developer.android.com/studio/run/device). Ahora deberías ver tu dispositivo en la barra de navegación superior.
  ![Phone](../docs/images/android_studio_bar_phone.jpg)
5. Haz clic en el botón de ejecutar (la flecha verde) o selecciona Run > Run 'android' desde el menú superior. Es posible que necesites reconstruir el proyecto usando Build > Rebuild Project.
  ![Run](../docs/images/android_studio_bar_run.jpg)
6. Si te pregunta si deseas usar Instant Run, haz clic en *Proceed Without Instant Run*.

### Solución de problemas

#### Versiones

Si recibes un mensaje como `The project is using an incompatible version (AGP 7.4.0) of the Android Gradle plugin. Latest supported version is AGP 7.3.0` necesitas actualizar Android Studio o degradar tu plugin de gradle. Puedes leer más sobre la compatibilidad de versiones entre Android Studio y el plugin de gradle [aquí](https://developer.android.com/studio/releases/gradle-plugin#android_gradle_plugin_and_android_studio_compatibility).