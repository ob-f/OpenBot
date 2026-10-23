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
  Dans cette application, nous utilisons Firebase pour la connexion Google afin d'accéder aux projets de l'[OpenBot Playground](../../../../../open-code/README.fr-FR.md) envoyés sur Google Drive.

- ### Remarque
  Si vous clonez cette application iOS pour la compiler vous-même, vous devez aussi [configurer Firebase](../../../../../open-code/README.fr-FR.md) pour l'application web [OpenBot Playground](https://www.playground.openbot.org/). En effet, l'application iOS récupère des fichiers dans le Google Drive de l'utilisateur, créés par les services Google Drive de Firebase. Utilisez le même projet Firebase pour l'application iOS et l'application web pour que les services Google Drive fonctionnent.

  Le fichier `GoogleService-Info.plist` de ce dépôt est un **fichier factice** avec de fausses clés et de faux identifiants.
  La connexion Google et les autres fonctionnalités Firebase ne fonctionnent pas tant que vous ne le remplacez pas par le vrai fichier
  téléchargé depuis votre propre projet Firebase, comme décrit ci-dessous.

- ### Prérequis
  Pour intégrer Firebase à l'application iOS OpenBot pour la connexion Google, il vous faut :
- **Compte Google:** Pour utiliser la connexion Google avec Firebase, vous devez avoir un compte Google. Si vous n'en avez pas, vous pouvez [créer](https://accounts.google.com/signup) un compte Google gratuit.
- **Cocoapods:** un gestionnaire de dépendances pour les projets Swift et Objective-C. Pour installer le SDK Firebase, Cocoapods doit être installé sur votre système.

### Configurer votre projet Firebase

- Ouvrez la [console Firebase](https://console.firebase.google.com/) et créez un `nouveau projet` :
    1. Cliquez sur « Create Project ».
    2. Saisissez un nom pour votre projet Firebase.
    3. Cliquez sur « Next » et désactivez Google Analytics si vous ne voulez pas l'utiliser.
    4. Cliquez sur « Create Project ».

    <p>
    <img src="../../../../../docs/images/firebase_create_app_project.jpg" alt="Create project" width="25%"/>
    <img src="../../../../../docs/images/firebase_enter_project_name.jpg" alt="Enter project name" width="40%"/>
    <img src="../../../../../docs/images/firebase_disable_analytics.jpg" alt="Disable analytics" width="27.5%"/>
    </p>

- Pour ajouter une application iOS à votre projet Firebase :
    1. Cliquez sur l'icône iOS dans le projet Firebase.
    2. Saisissez l'identifiant de bundle de votre application. Il doit être identique à celui de votre projet Xcode.
    3. Saisissez un surnom pour l'application.
    4. Cliquez sur « Register app » pour terminer.
    5. Téléchargez le fichier GoogleService-Info.plist et ajoutez-le à votre projet Xcode. Il contient la configuration dont votre application a besoin pour utiliser Firebase.

       <p>
       <img src="../../../../../docs/images/firebase_add_ios_app.jpg" alt="Add IOS Application" width="47.5%"/>
       <img src="../../../../../docs/images/firebase_bundle_id.jpg" alt="Bundle ID Name" width="30.5%"/>
       </p>

    6. Copiez la valeur `REVERSED_CLIENT_ID` de votre nouveau `GoogleService-Info.plist` et collez-la dans l'entrée
       `CFBundleURLSchemes` (sous `CFBundleURLTypes`) de `Info.plist`. La connexion Google ne fonctionne pas tant que cette valeur ne correspond pas à votre propre projet Firebase.
    7. Cliquez sur `Next` et sautez la troisième étape (Add Firebase SDK) et la quatrième (Add initialization code), car elles sont déjà faites dans ce projet.
    8. Passez à la console Firebase pour configurer les services Firebase que vous voulez utiliser dans votre application iOS.

   <p>
     <img src="../../../../../docs/images/firebase_google_service_info_plist.jpg" alt="Add IOS Application" width="35%"/>
     <img src="../../../../../docs/images/firebase_continue_to_console_ios.jpg" alt="Bundle ID Name" width="35%"/>
   </p>

- Pour `activer la connexion Google` dans votre projet Firebase :
    1. Ouvrez la console Firebase et sélectionnez votre projet.
    2. Cliquez sur `All products` dans le `menu latéral` à gauche.
    3. Cliquez sur `Authentication`.
    4. Cliquez sur `Get Started`.
    5. Cliquez sur l'`icône Google`.
    6. Cliquez sur l'`interrupteur` pour `activer` la connexion Google.

    <p>
    <img src="../../../../../docs/images/firebase_product_services.jpg" alt="Firebase product services" width="44.5%"/>
    <img src="../../../../../docs/images/firebase_app_authentication.jpg" alt="Firebase authentication" width="48%"/>
    </p>
    <p>
    <img src="../../../../../docs/images/firebase_app_google_signin.jpg" alt="Google Sign-In" width="60%"/>
    <img src="../../../../../docs/images/firebase_google_signin_enable.jpg" alt="Google Sign-In enable" width="31.5%"/>
    </p>

### Configurer le projet iOS

Le SDK Firebase s'installe avec CocoaPods. Le dépôt contient déjà un Podfile avec tous les pods nécessaires. Ouvrez le Terminal sur votre Mac, allez dans `robot/ios/OpenBot` et exécutez :

```
pod install
```

Ouvrez ensuite `OpenBot.xcworkspace` (et non `OpenBot.xcodeproj`) dans Xcode.

### Dépannage

Problèmes fréquents lors de la configuration de Firebase et leurs solutions :

```shell
1. Erreurs d'installation de CocoaPods : si l'installation de CocoaPods pose problème, par exemple si pod install ne s'exécute pas ou n'installe pas la bonne version du SDK Firebase, essayez ceci :
```

- Mettez CocoaPods à jour avec sudo gem install cocoapods.
- Supprimez le fichier Podfile.lock et relancez pod install.
- Vérifiez que vous avez bien ajouté le pod du SDK Firebase dans votre Podfile, avec exactement la version indiquée dans la console Firebase.
```shell
2. Réinstaller les pods (en cas de problèmes de compilation ou de paquets)
```
- pod deintegrate 
- rm -rf Pods
- rm -rf Podfile.lock
- pod install

```shell
3. Erreurs de configuration de Firebase : si vous n'arrivez pas à configurer Firebase, par exemple si Firebase ne s'initialise pas dans votre application, essayez ceci :
```

- Vérifiez que vous avez bien suivi toutes les étapes de la section « Configurer le projet iOS » de ce document.
- Vérifiez que vous avez bien ajouté le fichier de configuration Firebase (GoogleService-Info.plist) à votre projet Xcode.
- Vérifiez que le SDK Firebase est bien ajouté à votre projet Xcode et importé dans votre code.
- Vérifiez que votre projet Firebase est bien configuré dans la console Firebase, y compris l'identifiant de bundle et les autres paramètres du projet.
- Conflits avec d'autres bibliothèques : en cas de conflit avec d'autres bibliothèques de votre projet, par exemple des dépendances contradictoires ou des versions de SDK incompatibles, essayez ceci :
- Vérifiez que la bonne version du SDK Firebase est indiquée dans votre Podfile.
- Mettez vos autres bibliothèques à jour vers la dernière version et voyez si le problème disparaît.
- Cherchez les dépendances ou versions de bibliothèques contradictoires et résolvez-les en supprimant ou en mettant à jour les bibliothèques concernées.
