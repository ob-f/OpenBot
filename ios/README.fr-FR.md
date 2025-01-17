# Applications iOS - Version Bêta

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Fonctionnalités

Cliquez sur les liens ci-dessous pour lire les fonctionnalités des applications.

- [Application Robot](OpenBot/README.md)
- Application Contrôleur (Bientôt Disponible)

## Installer les applications
Actuellement, la seule façon de faire fonctionner les applications iOS sur vos téléphones est via les versions développeur, comme décrit dans la section ci-dessous.

## Construire les applications

### Prérequis

- [Xcode iOS 13 ou ultérieur](https://developer.apple.com/xcode/) pour construire et installer les applications.
- [Cocoapods](https://cocoapods.org/) installé sur votre système.
- Actuellement, nous utilisons la version 15.5 de la cible de déploiement iOS.
- Le projet est configuré en "Gestion automatique de la signature", vous pouvez donc configurer votre propre compte indépendant pour construire l'application -
  <img alt="Application iOS" width="100%" src="../docs/images/ios_automatically_manage_signing.png" />
- Pour configurer votre propre équipe, ajoutez votre compte iCloud via XCode > Paramètres > Comptes ou directement depuis le menu Équipe ci-dessus.
- Appareil iOS avec iOS 13 minimum ou ultérieur. [Liste des appareils pris en charge](https://support.apple.com/en-in/guide/iphone/iphe3fa5df43/ios).
- [Mode développeur](https://developer.apple.com/documentation/xcode/enabling-developer-mode-on-a-device) activé sur votre appareil iOS.
- Le compte iCloud ajouté doit être [de confiance pour votre appareil iOS](https://developer.apple.com/forums/thread/685271).

### Processus de construction

1. Ouvrez XCODE et sélectionnez *ouvrir un projet ou un fichier*.
2. Pour installer l'[application OpenBot](OpenBot/README.md), assurez-vous de sélectionner la configuration *OpenBot*.
   <img alt="Application iOS" width="100%" src="../docs/images/ios_openbot_configuration.png" />
3. Sélectionnez votre appareil dans la liste des appareils disponibles.
   <img alt="Application iOS" width="100%" src="../docs/images/ios_device_selection.png" />
4. Exécutez l'application sur l'appareil en cliquant sur l'icône ▶️ en haut à gauche de l'écran Xcode.