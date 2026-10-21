## Google Firebase

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Google Firebase est une plateforme de développement d'applications mobiles et web qui propose de nombreux services et outils pour aider les développeurs à créer rapidement et efficacement des applications de qualité. Elle comprend une base de données en temps réel, l'authentification des utilisateurs, l'hébergement, le stockage dans le cloud et bien plus, le tout réuni dans une seule plateforme. Firebase offre aux développeurs une solution pratique et évolutive pour gérer leur infrastructure backend, afin qu'ils puissent se concentrer sur de bonnes expériences utilisateur.

- ### Connexion Google avec Firebase

  La connexion Google avec Firebase est une fonctionnalité de la plateforme Firebase qui permet aux utilisateurs de se connecter à des applications mobiles ou web avec leur compte Google. Les utilisateurs accèdent ainsi aux applications de façon sûre et pratique, sans avoir à retenir et gérer des identifiants distincts. Firebase gère tout le processus d'authentification, de la vérification de l'identité auprès de Google jusqu'à l'identifiant utilisateur unique qui permet de personnaliser l'expérience dans l'application. Des mesures de sécurité supplémentaires, comme l'authentification à deux facteurs, protègent les comptes contre les accès non autorisés.

- ### Utilisation
  Dans cette application, nous utilisons Firebase pour la connexion Google afin d'accéder aux projets de l'[OpenBot Playground](../../../../../../../../../../../open-code/README.fr-FR.md) envoyés sur Google Drive. Si vous clonez cette application Android pour la compiler vous-même, vous devez configurer votre propre [projet Firebase](#configurer-votre-projet-firebase), car la connexion Google a besoin de la clé SHA-1 de votre application.
  
  Vous devez aussi [configurer Firebase](../../../../../../../../../../../open-code/README.fr-FR.md) pour l'application web [OpenBot Playground](https://www.playground.openbot.org/). En effet, l'application Android récupère des fichiers dans le Google Drive de l'utilisateur, créés par les services Google Drive de Firebase. Utilisez le même projet Firebase pour l'application Android et l'application web pour que les services Google Drive fonctionnent.
  
  Si vous ne voulez pas utiliser les services de l'OpenBot Playground, vous n'avez besoin de configurer ni l'authentification Firebase ni la connexion Google.

  Les fichiers `google-services.json` de ce dépôt (`robot/android/app/google-services.json` et
  `robot/android/app/src/main/assets/google-services.json`) sont des **fichiers factices** avec de fausses clés et de faux identifiants.
  La connexion Google et les autres fonctionnalités Firebase ne fonctionnent pas tant que vous ne les remplacez pas par le vrai fichier
  téléchargé depuis votre propre projet Firebase, comme décrit ci-dessous.

### Prérequis
Pour intégrer Firebase à l'application Android OpenBot pour la connexion Google, il vous faut :
- **Compte Google:** Pour utiliser la connexion Google avec Firebase, vous devez avoir un compte Google. Si vous n'en avez pas, vous pouvez [créer](https://accounts.google.com/signup) un compte Google gratuit.
- **Compte Firebase:** Vous devez avoir un compte Firebase et créer un nouveau projet Firebase pour votre application Android. Vous pouvez créer un nouveau projet depuis la [console Firebase](https://console.firebase.google.com/).
****

### Configurer votre projet Firebase

- Ouvrez la [console Firebase](https://console.firebase.google.com/) et créez un `nouveau projet` :
  1. Cliquez sur « Create Project ».
  2. Saisissez un nom pour votre projet Firebase et cliquez sur « Next ».
  3. Vous pouvez désactiver Google Analytics si vous ne voulez pas l'utiliser.
  4. Cliquez sur « Create Project ».
<p>
<img src="../../../../../../../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
<img src="../../../../../../../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width=40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
</p>

- Pour ajouter une application Android à votre projet Firebase :
  1. Cliquez sur l'icône Android dans le projet Firebase.
  2. Saisissez le `nom de package` de votre application Android. Il doit être `unique` dans Firebase.
  3. Saisissez un surnom pour l'application.
  4. La **clé SHA-1** est **obligatoire** pour la connexion Google de Firebase afin de signer l'APK de votre application. Saisissez-la dans le champ prévu.
  5. Cliquez sur `Register`.
  

- L'empreinte `SHA-1` est un identifiant unique du `certificat de signature de votre application`. Firebase l'utilise pour vérifier l'authenticité de votre application lorsqu'elle communique avec ses serveurs. Si vous utilisez l'authentification Firebase dans votre application, vous devez fournir la clé `SHA-1` `du certificat de signature` utilisé pour signer la `version de publication` de votre application.
  
<p>
<img style="width: 47%" src="../../../../../../../../../../../docs/images/firebase_add_android_app.jpg" alt="Add Android Application" width="40%"/>
<img src="../../../../../../../../../../../docs/images/firebase_package_name.jpg" alt="Package Name" width="36%"/>
</p>

- Pour obtenir la clé `SHA-1`, utilisez `keytool`, un outil en ligne de commande fourni avec le `Java SDK`. Voici comment faire sur `Mac` et `Windows` :
  

  - **Sur Mac**
    ```shell
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
    ```
    Cette commande affiche les détails du certificat de signature de débogage situé dans `~/.android/debug.keystore`. L'option `-alias` indique l'alias du certificat de débogage, et les options `-storepass` et `-keypass` les mots de passe du keystore et de la clé.
  

  - **Sous Windows**
    ```shell 
    keytool -list -v -keystore "%USERPROFILE%.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    ```
    Cette commande est semblable à celle du Mac, mais utilise un `autre chemin` vers le fichier debug.keystore. `%USERPROFILE%` est une variable d'environnement système qui pointe vers le dossier du profil de l'utilisateur actuel, qui contient le dossier `.android` où se trouve le fichier `debug.keystore`.

- Téléchargez le fichier `google-services.json` et `ajoutez-le` dans le `dossier app` de l'application (`robot/android/app/google-services.json`) et dans le `dossier assets` (`robot/android/app/src/main/assets/google-services.json`).
- Cliquez sur « Next ». Vous pouvez sauter la troisième étape, car le **SDK Firebase** est déjà ajouté dans le fichier gradle de ce projet.
- Passez à la console Firebase pour configurer les services Firebase que vous voulez utiliser dans votre application Android.

<p>
<img style="width: 40%" src="../../../../../../../../../../../docs/images/firebase_google_service_json_file.jpg" alt="Google services json file" width="41%"/>
<img src="../../../../../../../../../../../docs/images/firebase_continue_to_console.jpg" alt="Continue to console" width="40.2%"/>
</p>

- Pour `activer la connexion Google` dans votre projet Firebase :
  1. Ouvrez la console Firebase et sélectionnez votre projet.
  2. Cliquez sur `All products` dans le `menu latéral` à gauche.
  3. Cliquez sur `Authentication`.
  4. Cliquez sur `Get Started`.
  5. Cliquez sur l'`icône Google`.
  6. Cliquez sur l'`interrupteur` pour `activer` la connexion Google.

<p>
<img style="width:36%" src="../../../../../../../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="39%"/>
</p>
<p>
<img src="../../../../../../../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="50%"/>
<img src="../../../../../../../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="26.1%"/>
</p>


- Si vous avez `sauté l'ajout de la clé SHA-1` lors de la configuration, vous pouvez `encore l'ajouter` plus tard :

  - Ouvrez la `console Firebase` et sélectionnez `votre projet`.
  - Cliquez sur l'`icône d'engrenage` en `haut à gauche` et sélectionnez `Project settings`.
  - Dans la section `Your apps`, sélectionnez l'`application Android` à laquelle vous voulez `ajouter la clé SHA-1`.
  - `Faites défiler` jusqu'à la section `SHA certificate fingerprints` et cliquez sur `Add fingerprint`.
  - Saisissez la `clé SHA-1` du certificat de signature de votre application.
  - Cliquez sur `Save` pour ajouter la clé SHA-1 à votre projet Firebase.

<p>
<img style="width: 44%;" src="../../../../../../../../../../../docs/images/firebase_gear_icon.jpg" alt="Gear icon" width="30%"/>
<img style="width: 53%" src="../../../../../../../../../../../docs/images/firebase_add_sha1.jpg" alt="Add SHA-1" width="43%"/>
</p>

- Si vous aviez `déjà configuré l'authentification Firebase` avant d'ajouter la clé SHA-1, vous devrez peut-être `mettre à jour` la `configuration` de votre application : `remplacez` le fichier `google-services.json` du dossier du projet par le `google-services.json mis à jour` depuis les paramètres du projet Firebase.

<p>
<img src="../../../../../../../../../../../docs/images/firebase_updated_google_service_json_file.jpg" alt="Updated Google service json file" width="40%"/>
</p>
