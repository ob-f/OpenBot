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
  In dieser App verwenden wir Firebase für die Google-Anmeldung, um auf Projekte des [OpenBot Playground](../../../../../../../../../../../open-code/README.de-DE.md) zuzugreifen, die in Google Drive hochgeladen wurden. Wenn Sie diese Android-App klonen und selbst bauen, müssen Sie Ihr eigenes [Firebase-Projekt](#firebase-projekt-einrichten) einrichten, denn die Google-Anmeldung braucht den SHA-1-Schlüssel Ihrer App.
  
  Außerdem müssen Sie [Firebase](../../../../../../../../../../../open-code/README.de-DE.md) auch für die Web-App [OpenBot Playground](https://www.playground.openbot.org/) einrichten. Die Android-App lädt nämlich Dateien aus dem Google Drive des Benutzers, die über die Google-Drive-Dienste von Firebase angelegt werden. Verwenden Sie für die Android-App und die Web-App dasselbe Firebase-Projekt, damit die Google-Drive-Dienste funktionieren.
  
  Wenn Sie die Dienste des OpenBot Playground nicht nutzen möchten, brauchen Sie weder die Firebase-Authentifizierung noch die Google-Anmeldung einzurichten.

  Die `google-services.json`-Dateien in diesem Repository (`robot/android/app/google-services.json` und
  `robot/android/app/src/main/assets/google-services.json`) sind **Platzhalter** mit erfundenen Schlüsseln und Kennungen.
  Die Google-Anmeldung und andere Firebase-Funktionen funktionieren erst, wenn Sie sie durch die echte Datei aus Ihrem
  eigenen Firebase-Projekt ersetzen, wie unten beschrieben.

### Voraussetzungen
Um Firebase für die Google-Anmeldung in die OpenBot-Android-App einzubinden, brauchen Sie:
- **Google-Konto:** Für die Google-Anmeldung mit Firebase brauchen Sie ein Google-Konto. Falls Sie keines haben, können Sie hier ein kostenloses Google-Konto [erstellen](https://accounts.google.com/signup).
- **Firebase-Konto:** Sie brauchen ein Firebase-Konto und ein neues Firebase-Projekt für Ihre Android-App. Ein neues Projekt erstellen Sie in der [Firebase-Konsole](https://console.firebase.google.com/).
****

### Firebase-Projekt einrichten

- Öffnen Sie die [Firebase-Konsole](https://console.firebase.google.com/) und erstellen Sie ein `neues Projekt`:
  1. Klicken Sie auf „Create Project“.
  2. Geben Sie einen Namen für das Firebase-Projekt ein und klicken Sie auf „Next“.
  3. Google Analytics können Sie deaktivieren, wenn Sie es nicht nutzen möchten.
  4. Klicken Sie auf „Create Project“.
<p>
<img src="../../../../../../../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
<img src="../../../../../../../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width=40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
</p>

- So fügen Sie Ihrem Firebase-Projekt eine Android-App hinzu:
  1. Klicken Sie im Firebase-Projekt auf das Android-Symbol.
  2. Geben Sie den `Paketnamen` Ihrer Android-App ein. Er muss in Firebase `eindeutig` sein.
  3. Geben Sie einen Spitznamen für die App ein.
  4. Der **SHA-1-Schlüssel** ist für die Firebase Google-Anmeldung **erforderlich**, um die APK Ihrer App zu signieren. Geben Sie ihn im passenden Feld ein.
  5. Klicken Sie auf `Register`.
  

- Der `SHA-1`-Fingerabdruck ist eine eindeutige Kennung für das `Signaturzertifikat Ihrer App`. Firebase prüft damit die Echtheit Ihrer App, wenn sie mit den Firebase-Servern kommuniziert. Wenn Sie die Firebase-Authentifizierung in Ihrer App nutzen, müssen Sie den `SHA-1`-Schlüssel `des Signaturzertifikats` angeben, mit dem die `Release-Version` Ihrer App signiert wird.
  
<p>
<img style="width: 47%" src="../../../../../../../../../../../docs/images/firebase_add_android_app.jpg" alt="Add Android Application" width="40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_package_name.jpg" alt="Package Name" width="36%"/>
</p>

- Den `SHA-1`-Schlüssel erhalten Sie mit `keytool`, einem Kommandozeilenwerkzeug des `Java SDK`. So geht es auf `Mac` und `Windows`:
  

  - **Auf dem Mac**
    ```shell
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
    ```
    Dieser Befehl zeigt die Details des Debug-Signaturzertifikats in `~/.android/debug.keystore`. `-alias` gibt den Alias des Debug-Zertifikats an, `-storepass` und `-keypass` die Passwörter für den Keystore und den Schlüssel.
  

  - **Unter Windows**
    ```shell 
    keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    ```
    Dieser Befehl entspricht dem Mac-Befehl, verwendet aber einen `anderen Pfad` zur Datei debug.keystore. `%USERPROFILE%` ist eine Systemumgebungsvariable für das Profilverzeichnis des aktuellen Benutzers, in dem der Ordner `.android` mit der Datei `debug.keystore` liegt.

- Laden Sie die Datei `google-services.json` herunter und `legen` Sie sie in den `app-Ordner` Ihrer App (`robot/android/app/google-services.json`) und in den `assets-Ordner` (`robot/android/app/src/main/assets/google-services.json`).
- Klicken Sie auf „Next“. Den dritten Schritt können Sie überspringen, weil das **Firebase SDK** in der Gradle-Datei dieses Projekts bereits eingebunden ist.
- Wechseln Sie zur Firebase-Konsole, um die Firebase-Dienste einzurichten, die Sie in Ihrer Android-App nutzen möchten.

<p>
<img style="width: 40%" src="../../../../../../../../../../../docs/images/firebase_google_service_json_file.jpg" alt="Google services json file" width="41%"/>
<img src="../../../../../../../../../../../docs/images/firebase_continue_to_console.jpg" alt="Continue to console" width="40.2%"/>
</p>

- So `aktivieren Sie die Google-Anmeldung` für Ihr Firebase-Projekt:
  1. Öffnen Sie die Firebase-Konsole und wählen Sie Ihr Projekt.
  2. Klicken Sie im `Seitenmenü` links auf `All products`.
  3. Klicken Sie auf `Authentication`.
  4. Klicken Sie auf `Get Started`.
  5. Klicken Sie auf das `Google-Symbol`.
  6. Schalten Sie die Google-Anmeldung mit dem `Schalter` `ein`.

<p>
<img style="width:36%" src="../../../../../../../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="39%"/>
</p>
<p>
<img src="../../../../../../../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="26.1%"/>
</p>


- Wenn Sie beim Einrichten den `SHA-1-Schlüssel übersprungen` haben, können Sie ihn `später hinzufügen`:

  - Öffnen Sie die `Firebase-Konsole` und wählen Sie `Ihr Projekt`.
  - Klicken Sie auf das `Zahnrad` `oben links` und wählen Sie `Project settings`.
  - Wählen Sie unter `Your apps` die `Android-App`, der Sie `den SHA-1-Schlüssel hinzufügen` möchten.
  - `Scrollen` Sie zu `SHA certificate fingerprints` und klicken Sie auf `Add fingerprint`.
  - Geben Sie den `SHA-1-Schlüssel` des Signaturzertifikats Ihrer App ein.
  - Klicken Sie auf `Save`, um den SHA-1-Schlüssel zu Ihrem Firebase-Projekt hinzuzufügen.

<p>
<img style="width: 44%;" src="../../../../../../../../../../../docs/images/firebase_gear_icon.jpg" alt="Gear icon" width="30%"/>
<img style="width: 53%" src="../../../../../../../../../../../docs/images/firebase_add_sha1.jpg" alt="Add SHA-1" width="43%"/>
</p>

- Wenn Sie die Firebase-Authentifizierung `schon eingerichtet` hatten, bevor Sie den SHA-1-Schlüssel hinzugefügt haben, müssen Sie die `Konfiguration` Ihrer App `aktualisieren`: `Ersetzen` Sie die Datei `google-services.json` im Projektordner durch die `aktualisierte google-services.json` aus den Projekteinstellungen von Firebase.

<p>
<img src="../../../../../../../../../../../docs/images/firebase_updated_google_service_json_file.jpg" alt="Updated Google service json file" width="40%"/>
</p>
