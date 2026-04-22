# iOS Apps - Beta-Version

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Funktionen

Klicken Sie auf die untenstehenden Links, um mehr über die Funktionen der Apps zu erfahren.

- [Robot App](OpenBot/README.md)
- Controller App (Kommt bald)

## Installation der Apps
Derzeit ist der einzige Weg, die iOS-Apps auf Ihrem Telefon auszuführen, über die Entwickler-Builds, wie im folgenden Abschnitt beschrieben.

## Apps bauen

### Voraussetzungen

- [Xcode iOS 13 oder später](https://developer.apple.com/xcode/) zum Bauen und Installieren der Apps.
- [Cocoapods](https://cocoapods.org/) auf Ihrem System installiert.
- Derzeit verwenden wir die iOS-Zielversion 15.5.
- Das Projekt ist so konfiguriert, dass es "Signing automatisch verwalten" verwendet, sodass Sie Ihr eigenes unabhängiges Konto zum Bauen der App konfigurieren können -
  <img alt="iOS App" width="100%" src="../docs/images/ios_automatically_manage_signing.png" />
- Um Ihr eigenes Team zu konfigurieren, fügen Sie Ihr iCloud-Konto über XCode > Einstellungen > Konten oder direkt über das Team-Menü oben hinzu.
- iOS-Gerät mit mindestens iOS 13 oder später. [Liste der unterstützten Geräte](https://support.apple.com/en-in/guide/iphone/iphe3fa5df43/ios).
- [Entwicklermodus](https://developer.apple.com/documentation/xcode/enabling-developer-mode-on-a-device) auf Ihrem iOS-Gerät aktiviert.
- Das hinzugefügte iCloud-Konto sollte von Ihrem iOS-Gerät [vertraut werden](https://developer.apple.com/forums/thread/685271).

### Build-Prozess

1. Öffnen Sie XCODE und wählen Sie *ein Projekt oder eine Datei öffnen*.
2. Um die [OpenBot-App](OpenBot/README.md) zu installieren, stellen Sie sicher, dass Sie die *OpenBot*-Konfiguration auswählen.
   <img alt="iOS App" width="100%" src="../docs/images/ios_openbot_configuration.png" />
3. Wählen Sie Ihr Gerät aus der Liste der verfügbaren Geräte aus.
   <img alt="iOS App" width="100%" src="../docs/images/ios_device_selection.png" />
4. Führen Sie die App auf dem Gerät aus, indem Sie auf das ▶️-Symbol oben links auf dem Xcode-Bildschirm klicken.
