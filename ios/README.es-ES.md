# Aplicaciones iOS - Versión Beta

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

- [Aplicación Robot](OpenBot/README.es-ES.md)
- Aplicación Controladora (Próximamente)

## Instalar las aplicaciones
Actualmente, la única forma de ejecutar las aplicaciones iOS en tus teléfonos es a través de las versiones de desarrollador, como se describe en la sección a continuación.

## Construir las aplicaciones

### Requisitos previos

- [Xcode iOS 13 o posterior](https://developer.apple.com/xcode/) para construir e instalar las aplicaciones.
- [Cocoapods](https://cocoapods.org/) instalado en tu sistema.
- Actualmente, estamos utilizando la versión 15.5 del objetivo de despliegue de iOS.
- El proyecto está configurado como "Gestionar la firma automáticamente", por lo que puedes configurar tu propia cuenta independiente para construir la aplicación -
  <img alt="Aplicación iOS" width="100%" src="../docs/images/ios_automatically_manage_signing.png" />
- Para configurar tu propio equipo, agrega tu cuenta de iCloud a través de XCode > Configuración > Cuentas o directamente desde el menú Equipo arriba.
- Dispositivo iOS con iOS 13 o posterior como mínimo. [Lista de dispositivos compatibles](https://support.apple.com/en-in/guide/iphone/iphe3fa5df43/ios).
- [Modo desarrollador](https://developer.apple.com/documentation/xcode/enabling-developer-mode-on-a-device) activado en tu dispositivo iOS.
- La cuenta de iCloud agregada debe ser [confiable por tu dispositivo iOS](https://developer.apple.com/forums/thread/685271).

### Proceso de construcción

1. Abre XCODE y selecciona *abrir un proyecto o archivo*.
2. Para instalar la [aplicación OpenBot](OpenBot/README.es-ES.md) asegúrate de seleccionar la configuración *OpenBot*.
   <img alt="Aplicación iOS" width="100%" src="../docs/images/ios_openbot_configuration.png" />
3. Selecciona tu dispositivo de la lista de dispositivos disponibles.
   <img alt="Aplicación iOS" width="100%" src="../docs/images/ios_device_selection.png" />
4. Ejecuta la aplicación en el dispositivo haciendo clic en el icono ▶️ en la parte superior izquierda de la pantalla de Xcode.