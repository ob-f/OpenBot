## Google Firebase

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Google Firebase ist eine Entwicklungsplattform für mobile Apps und Web-Apps mit vielen Diensten und Werkzeugen, mit denen Entwickler schnell und effizient hochwertige Apps bauen können. Dazu gehören Echtzeit-Datenbank, Benutzerauthentifizierung, Hosting, Cloud-Speicher und vieles mehr, alles in einer Plattform. Firebase bietet Entwicklern eine bequeme und skalierbare Lösung für ihre Backend-Infrastruktur, damit sie sich auf gute Benutzererlebnisse konzentrieren können.

- ### Firebase Google-Anmeldung

  Die Firebase Google-Anmeldung ist eine Funktion der Firebase-Plattform, mit der sich Benutzer mit ihrem Google-Konto bei mobilen Apps oder Web-Apps anmelden. So greifen Benutzer sicher und bequem auf Apps zu, ohne sich eigene Zugangsdaten merken und verwalten zu müssen. Firebase übernimmt den gesamten Anmeldeablauf, von der Prüfung der Identität bei Google bis zur eindeutigen Benutzer-ID, mit der die App das Erlebnis personalisieren kann. Zusätzliche Sicherheitsfunktionen wie die Zwei-Faktor-Authentifizierung schützen die Konten vor unbefugtem Zugriff.

- ### Verwendung
  In dieser App verwenden wir Firebase für die Google-Anmeldung, um auf Projekte des [OpenBot Playground](../../../../../open-code/README.de-DE.md) zuzugreifen, die in Google Drive hochgeladen wurden.

- ### Hinweis
  Wenn Sie diese iOS-App klonen und selbst bauen, müssen Sie [Firebase](../../../../../open-code/README.de-DE.md) auch für die Web-App [OpenBot Playground](https://www.playground.openbot.org/) einrichten. Die iOS-App lädt nämlich Dateien aus dem Google Drive des Benutzers, die über die Google-Drive-Dienste von Firebase angelegt werden. Verwenden Sie für die iOS-App und die Web-App dasselbe Firebase-Projekt, damit die Google-Drive-Dienste funktionieren.

  Die Datei `GoogleService-Info.plist` in diesem Repository ist ein **Platzhalter** mit erfundenen Schlüsseln und Kennungen.
  Die Google-Anmeldung und andere Firebase-Funktionen funktionieren erst, wenn Sie sie durch die echte Datei aus Ihrem
  eigenen Firebase-Projekt ersetzen, wie unten beschrieben.

- ### Voraussetzungen
  Um Firebase für die Google-Anmeldung in die OpenBot-iOS-App einzubinden, brauchen Sie:
- **Google-Konto:** Für die Google-Anmeldung mit Firebase brauchen Sie ein Google-Konto. Falls Sie keines haben, können Sie hier ein kostenloses Google-Konto [erstellen](https://accounts.google.com/signup).
- **Cocoapods:** ein Abhängigkeitsmanager für Swift- und Objective-C-Projekte. Um das Firebase SDK zu installieren, muss Cocoapods auf Ihrem System installiert sein.

### Firebase-Projekt einrichten

- Öffnen Sie die [Firebase-Konsole](https://console.firebase.google.com/) und erstellen Sie ein `neues Projekt`:
    1. Klicken Sie auf „Create Project“.
    2. Geben Sie einen Namen für das Firebase-Projekt ein.
    3. Klicken Sie auf „Next“ und deaktivieren Sie Google Analytics, wenn Sie es nicht nutzen möchten.
    4. Klicken Sie auf „Create Project“.

    <p>
    <img src="../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
    <img src="../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width="40%"/>
    <img src="../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
    </p>

- So fügen Sie Ihrem Firebase-Projekt eine iOS-App hinzu:
    1. Klicken Sie im Firebase-Projekt auf das iOS-Symbol.
    2. Geben Sie die Bundle-ID Ihrer App ein. Sie muss mit der Bundle-ID in Ihrem Xcode-Projekt übereinstimmen.
    3. Geben Sie einen Spitznamen für die App ein.
    4. Klicken Sie auf „Register app“.
    5. Laden Sie die Datei GoogleService-Info.plist herunter und fügen Sie sie Ihrem Xcode-Projekt hinzu. Sie enthält die Konfiguration, die Ihre App für Firebase braucht.

       <p>
       <img src="../../../../../docs/images/firebase_add_ios_app.jpg" alt="Add IOS Application" width="47.5%"/>
       <img src="../../../../../docs/images/firebase_bundle_id.jpg" alt="Bundle ID Name" width="30.5%"/>
       </p>

    6. Kopieren Sie den Wert `REVERSED_CLIENT_ID` aus Ihrer neuen `GoogleService-Info.plist` und fügen Sie ihn in `Info.plist` beim Eintrag
       `CFBundleURLSchemes` (unter `CFBundleURLTypes`) ein. Die Google-Anmeldung funktioniert erst, wenn dieser Wert zu Ihrem eigenen Firebase-Projekt passt.
    7. Klicken Sie auf `Next` und überspringen Sie den dritten (Add Firebase SDK) und vierten Schritt (Add initialization code), denn beides ist in diesem Projekt schon erledigt.
    8. Wechseln Sie zur Firebase-Konsole, um die Firebase-Dienste einzurichten, die Sie in Ihrer iOS-App nutzen möchten.

   <p>
     <img src="../../../../../docs/images/firebase_google_service_info_plist.jpg" alt="Add IOS Application" width="35%"/>
     <img src="../../../../../docs/images/firebase_continue_to_console_ios.jpg" alt="Bundle ID Name" width="35%"/>
   </p>

- So `aktivieren Sie die Google-Anmeldung` für Ihr Firebase-Projekt:
    1. Öffnen Sie die Firebase-Konsole und wählen Sie Ihr Projekt.
    2. Klicken Sie im `Seitenmenü` links auf `All products`.
    3. Klicken Sie auf `Authentication`.
    4. Klicken Sie auf `Get Started`.
    5. Klicken Sie auf das `Google-Symbol`.
    6. Schalten Sie die Google-Anmeldung mit dem `Schalter` `ein`.

    <p>
    <img src="../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="44.5%"/>
    <img src="../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="48%"/>
    </p>
    <p>
    <img src="../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="60%"/>
    <img src="../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="31.5%"/>
    </p>

### iOS-Projekt einrichten

Das Firebase SDK wird mit CocoaPods installiert. Das Repository enthält bereits eine Podfile mit allen nötigen Pods. Öffnen Sie das Terminal auf Ihrem Mac, wechseln Sie nach `robot/ios/OpenBot` und führen Sie aus:

```
pod install
```

Öffnen Sie danach `OpenBot.xcworkspace` (nicht `OpenBot.xcodeproj`) in Xcode.

### Fehlerbehebung

Häufige Probleme beim Einrichten von Firebase und ihre Lösungen:

```shell
1. Fehler bei der CocoaPods-Installation: Wenn bei der CocoaPods-Installation Probleme auftreten, etwa wenn pod install nicht läuft oder nicht die richtige Firebase-SDK-Version installiert, versuchen Sie Folgendes:
```

- Aktualisieren Sie CocoaPods mit sudo gem install cocoapods.
- Löschen Sie die Datei Podfile.lock und führen Sie pod install erneut aus.
- Prüfen Sie, ob Sie den Firebase-SDK-Pod richtig zur Podfile hinzugefügt haben, mit genau der Version aus der Firebase-Konsole.
```shell
2. Pods neu installieren (bei Problemen mit dem Build oder den Paketen)
```
- pod deintegrate 
- rm -rf Pods
- rm -rf Podfile.lock
- pod install

```shell
3. Fehler in der Firebase-Konfiguration: Wenn Sie Firebase nicht richtig einrichten können, etwa weil sich Firebase in Ihrer App nicht initialisieren lässt, versuchen Sie Folgendes:
```

- Prüfen Sie, ob Sie alle Schritte im Abschnitt „iOS-Projekt einrichten“ dieses Dokuments richtig ausgeführt haben.
- Prüfen Sie, ob Sie die Firebase-Konfigurationsdatei (GoogleService-Info.plist) richtig zu Ihrem Xcode-Projekt hinzugefügt haben.
- Prüfen Sie, ob das Firebase SDK richtig zu Ihrem Xcode-Projekt hinzugefügt ist und in Ihrem Code importiert wird.
- Prüfen Sie, ob Ihr Firebase-Projekt in der Firebase-Konsole richtig eingerichtet ist, einschließlich der Bundle-ID und anderer Projekteinstellungen.
- Konflikte mit anderen Bibliotheken: Wenn es Konflikte mit anderen Bibliotheken in Ihrem Projekt gibt, etwa widersprüchliche Abhängigkeiten oder inkompatible SDK-Versionen, versuchen Sie Folgendes:
- Prüfen Sie, ob in Ihrer Podfile die richtige Firebase-SDK-Version angegeben ist.
- Aktualisieren Sie Ihre anderen Bibliotheken auf die neueste Version und prüfen Sie, ob das Problem dann behoben ist.
- Suchen Sie nach widersprüchlichen Abhängigkeiten oder Bibliotheksversionen und lösen Sie sie, indem Sie die betroffenen Bibliotheken entfernen oder aktualisieren.
