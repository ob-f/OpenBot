# Application iOS Robot - Version Bêta

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## AVERTISSEMENTS

1. **Sécurité :** Assurez-vous toujours d'opérer dans un environnement sûr. Gardez à l'esprit que votre téléphone pourrait être endommagé en cas de collision ! Une attention particulière est nécessaire lors de l'utilisation du contrôle automatisé (par exemple, suivi de personne ou politique de conduite). Assurez-vous d'avoir toujours une manette de jeu connectée et de bien connaître la configuration des touches afin de pouvoir arrêter le véhicule à tout moment. Utilisez à vos propres risques !
2. **Application en développement :** L'application est en cours de développement et peut planter ou présenter un comportement inattendu selon le modèle de votre téléphone et la version du système d'exploitation. Assurez-vous de tester toutes les fonctionnalités sans roues connectées. Utilisez à vos propres risques !

3. **L'application sera bientôt `disponible` sur l'`App Store`.**

## Écrans de l'application

### Menu Principal

L'application démarre avec un écran de menu qui affiche tous les écrans disponibles. L'écran de connexion Bluetooth peut être ouvert en cliquant sur l'icône Bluetooth en haut à droite. L'écran des paramètres peut être ouvert en cliquant sur l'icône des paramètres juste à côté. En cliquant sur les autres icônes, l'utilisateur peut accéder à divers écrans dont les fonctionnalités sont expliquées dans les sections suivantes.

<p align="left">
<img src="../../../docs/images/ios_main_screen.jpg" alt="Menu Principal" height="400"/>
<img src="../../../docs/images/ios_settings_screen.jpg" alt="Paramètres" height="400"/>
<img src="../../../docs/images/ios_bluetooth_screen.jpg" alt="Bluetooth" height="400"/>
</p>

#### Connexion Bluetooth

Contrairement à l'application Android, qui permet de connecter le smartphone à la carte de contrôle bas niveau d'un OpenBot via un câble USB, l'application iOS repose uniquement sur une connexion sans fil Bluetooth Low-Energy (BLE). En ouvrant l'écran de connexion Bluetooth dans l'application iOS (en cliquant sur le logo Bluetooth depuis l'écran principal ou depuis n'importe quel fragment), une liste de tous les appareils compatibles est affichée. La compatibilité est ici assurée en utilisant une gamme de UUID spécifiques attribués à un véhicule OpenBot au niveau de l'[application](https://github.com/3dwesupport/OpenBot/blob/090dcb28206195a7ee45a13b8ded968a8d365abe/ios/OpenBot/OpenBot/Utils/Constants.swift#L57) et du [firmware](https://github.com/3dwesupport/OpenBot/blob/090dcb28206195a7ee45a13b8ded968a8d365abe/firmware/openbot_nano/openbot_nano.ino#L115). Vous devez vous assurer que ces UUID correspondent. Appairer un appareil iOS à un véhicule OpenBot nécessite simplement de sélectionner ce véhicule dans la liste et d'appuyer sur le bouton "Connecter". Le débit par défaut de la connexion est fixé à 115200 et peut être modifié au niveau de l'application et du firmware.

<p align="left">
<img src="../../../docs/images/ios_ble.gif" alt="Connexion BLE" height="400" />
</p>

#### Prise en charge multilingue

Ouvrez l'écran des paramètres avec l'icône en forme d'engrenage en haut à droite et appuyez sur `Langue` dans la section Général pour choisir la langue d'affichage de l'application. Sélectionnez `Par défaut du système` pour suivre la langue de votre iPhone, ou choisissez l'une des langues prises en charge : English, Deutsch, Español, Français, 中文, 한국어 ou हिन्दी. L'application applique la nouvelle langue immédiatement.

<p align="left">
<img src="../../../docs/images/ios_language_screen.jpg" alt="Sélection de la langue" height="400"/>
</p>

#### Serveur de signalisation web

Dans l'écran des paramètres, saisissez l'adresse WebSocket du serveur de signalisation dans le champ `Serveur de signalisation web`, par exemple `ws://<ip-du-serveur>:8080/ws`. Elle est utilisée lorsque le mode de contrôle est réglé sur serveur web ; l'iPhone et le serveur doivent donc être sur le même réseau Wi-Fi. Consultez le [serveur web](../../../controller/web-server/README.md) pour sa configuration.

<p align="left">
<img src="../../../docs/images/ios_web_signaling_screen.jpg" alt="Serveur de signalisation web" height="400"/>
</p>

### Libre Circulation

Libre Circulation offre un contrôle simple du robot avec des mises à jour en temps réel et des informations sur la batterie, la vitesse et la distance par rapport aux surfaces. Il offre également des contrôles liés au contrôleur, au mode de conduite et à la vitesse.

<p align="left">
<img src="../../../docs/images/ios_free_roam_screen.jpg" alt="Libre Circulation" width="50%" />
</p>

- **Batterie** : L'icône de la batterie montre les niveaux de batterie en temps réel du robot connecté.

- **Mode de Conduite** : Il y a 3 modes de conduite affichés sur la vue :

    - D -> Conduite, lorsque le robot avance

    - N -> Neutre, lorsque le robot est à l'arrêt

    - R -> Marche arrière, lorsque le robot recule

- **Vitesse** : Le compteur de vitesse montre la vitesse en temps réel du robot.

- **Sonar** : La vue sonar montre la distance du robot par rapport à un objet en approche en cm.

- **Bluetooth** : Montre l'état de la connexion Bluetooth avec le microcontrôleur. En appuyant sur l'icône, l'utilisateur peut également être redirigé vers l'écran Bluetooth pour voir/modifier la connexion.

#### Contrôle

Le premier bouton sert à sélectionner le **mode de contrôle**. Il existe deux modes de contrôle différents :

- **Manette de jeu** : L'application reçoit les commandes d'un contrôleur BT connecté.
- **Téléphone (Bientôt disponible)** : Le robot peut être contrôlé via un autre smartphone avec l'application de contrôle installée ou via un script Python exécuté sur un ordinateur connecté au même réseau.

Le deuxième bouton sert à sélectionner le **mode de conduite**. Il existe trois modes de conduite différents lors de l'utilisation d'une manette de jeu (par exemple, PS4) :

- **Jeu** : Utilisez les gâchettes d'épaule droite et gauche (R2, L2) pour l'accélération avant et arrière et l'un des joysticks pour la direction. Ce mode imite le mode de contrôle des jeux vidéo de course automobile.
- **Joystick** : Utilisez l'un des joysticks pour contrôler le robot.
- **Double** : Utilisez le joystick gauche et le joystick droit pour contrôler les côtés gauche et droit de la voiture. C'est une direction différentielle brute.

Le troisième bouton sert à sélectionner le **mode de vitesse**. Il existe trois modes de vitesse différents :

- **Lent** : La tension appliquée aux moteurs est limitée à 50 % de la tension d'entrée (~6V).
- **Normal** : La tension appliquée aux moteurs est limitée à 75 % de la tension d'entrée (~9V).
- **Rapide** : Il n'y a pas de limite. La tension d'entrée complète sera appliquée aux moteurs à pleine accélération (~12V). *C'est le réglage par défaut pour exécuter les réseaux neuronaux.*

Rouler à des vitesses plus élevées réduira la durée de vie des moteurs mais est plus amusant. Les commandes envoyées au robot sont affichées sur le côté droit. Lors de l'utilisation de la manette de jeu, le mode de vitesse peut être augmenté en appuyant sur le joystick droit (R3) et diminué en appuyant sur le joystick gauche (L3).

### Collecte de Données

Interface utilisateur simple pour la collecte de jeux de données.

<p align="left">

<img src="../../../docs/images/ios_data_collection_screen.jpg" alt="Collecte de Données" width="50%" />

</p>

- **Résolution de Prévisualisation** : Utilisée pour basculer entre les résolutions de prévisualisation de la caméra. Il y a 3 réglages :

    - ***HAUTE*** (1920x1080p)

    - ***MOYENNE*** (1280x720p)

    - ***BASSE*** (640x360)

- **Résolution du Modèle** : Utilisée pour basculer entre les résolutions des images enregistrées pour l'entraînement de différents modèles.

- **Serveur** : la fonction serveur est en cours de développement.

- **Journaliser les Données Collectées** : le processus de collecte de données peut être contrôlé depuis l'écran ou à distance, par exemple depuis un contrôleur Bluetooth. Lors de l'utilisation d'un contrôleur Bluetooth, vous pouvez :

    - appuyer sur le **bouton A** pour **démarrer** le processus de collecte de données

    - appuyer à nouveau sur le **bouton A** pour **arrêter** la collecte de données et enregistrer les données collectées dans un fichier .zip

    - appuyer alternativement sur le **bouton R1** pour **arrêter** la collecte de données **sans enregistrer** les données collectées (par exemple en raison d'une collision inattendue avec l'environnement)

    - n'oubliez pas d'utiliser le fragment de mappage du contrôleur pour vous assurer que vous utilisez les bons boutons.

- **État du Véhicule** : Le champ **Batterie** affiche la tension de la batterie mesurée par le microcontrôleur via le diviseur de tension. Le champ **Vitesse (g,d)** rapporte la vitesse gauche et droite des roues (avant) en tr/min. Elle est mesurée par le microcontrôleur via les capteurs de vitesse des roues optiques. Le champ **Sonar** montre l'espace libre devant la voiture en centimètres. Il est mesuré par le microcontrôleur via le capteur ultrasonique. Notez que vous ne recevrez des valeurs que quelques secondes après l'établissement de la connexion USB.

- **Capteurs** : Rapporte les mesures des capteurs du véhicule. Actuellement, nous enregistrons les lectures des capteurs suivants : caméra, gyroscope, accéléromètre, magnétomètre, capteur de lumière ambiante et baromètre. En utilisant l'API iOS, nous sommes en mesure d'obtenir les lectures de capteurs suivantes : images RGB, vitesse angulaire, accélération linéaire, gravité, intensité du champ magnétique, intensité lumineuse, pression atmosphérique, latitude, longitude, altitude, cap et vitesse. En plus des capteurs du téléphone, nous enregistrons les lectures des capteurs du corps (odométrie des roues, distance des obstacles et tension de la batterie), qui sont transmises via le lien série. Nous enregistrons également et horodatons les signaux de contrôle reçus d'un contrôleur connecté, le cas échéant. Enfin, nous intégrons plusieurs réseaux neuronaux pour le suivi de personne et la navigation autonome.

### Mappage du Contrôleur

Interface utilisateur simple pour vérifier le mappage des boutons et des joysticks d'un contrôleur BT connecté.

<p align="left">
<img src="../../../docs/images/ios_controller_mapping.jpg" alt="Mappage du Contrôleur" width="30%" />
</p>

### Infos sur le Robot

Interface utilisateur simple pour obtenir des informations sur le robot et tester les fonctionnalités de base. Le **Type de Robot** tel que configuré dans le firmware est affiché sous forme de texte et d'animation. Les coches dans les sections **Capteurs**, **Odométrie des Roues** et **LEDs** montrent quelles fonctionnalités sont prises en charge par le robot connecté. La section **Lectures** fournit les mesures de capteurs les plus importantes. Dans la section **Envoyer des Commandes**, les utilisateurs peuvent envoyer des commandes de moteur de base en appuyant sur les boutons correspondants et contrôler les LEDs avant et arrière avec un curseur.

<p align="left">
<img src="../../../docs/images/ios_screen_robot_info.gif" alt="Infos sur le Robot" width="50%" />
</p>

### Pilote Automatique

Interface utilisateur simple pour exécuter des modèles de pilote automatique.

<p align="left">

<img src="../../../docs/images/ios_autopilot_screen.jpg" alt="Pilote Automatique" width="50%" />

</p>

- **Serveur** : si l'[application web](../../../policy/README.fr-FR.md#application-web) d'entraînement est lancée, vous pouvez la choisir ici et envoyer des modèles de pilote automatique entraînés au robot.
- **Modèle** : choisissez un modèle entraîné pour le mode pilote automatique.
- **Appareil** : CPU, GPU ou NNAPI pour l'inférence (plus de détails [ici](../../android/app/README.fr-FR.md#appareil)).
- **Threads** : nombre de threads à utiliser (ne compte que si l'appareil choisi est le CPU).
- **Contrôle** : contrôleur, mode de conduite et vitesse règlent le robot comme décrit dans la [section Contrôle](#contrôle).

### Suivi d'Objets

Interface utilisateur simple pour suivre des objets de 80 classes différentes. Une brève description des différents modèles d'IA pour le suivi d'objets et des benchmarks de performance peut être trouvée dans [Gestion des Modèles](#gestion-des-modèles).

<p align="left">
<img src="../../../docs/images/ios_object_tracking_screen.jpg" alt="Suivi d'Objets" width="50%" />
</p>

- **Vitesse dynamique** : réduit la vitesse du robot en « mode auto » à mesure qu'il s'approche de l'objet suivi.
  La vitesse dépend de la surface du cadre de détection (fonctionne mieux en mode paysage).
- **Modèle** : choisissez un détecteur d'objets adapté aux performances de votre téléphone.
- **Objet** : choisissez l'objet à suivre. Les modèles détectent les 80 [classes d'objets](https://tech.amikelive.com/node-718/what-object-categories-labels-are-in-coco-dataset/) COCO.
- **Confiance** : seuil à partir duquel les détections sont acceptées. Augmentez-le en cas de fausses détections, baissez-le si l'objet voulu n'est pas détecté.
- **Appareil** : CPU, GPU ou NNAPI pour l'inférence (plus de détails [ici](../../android/app/README.fr-FR.md#appareil)).
- **Threads** : nombre de threads à utiliser (ne compte que si l'appareil choisi est le CPU).
- **Contrôle** : contrôleur, mode de conduite et vitesse règlent le robot comme décrit dans la [section Contrôle](#contrôle).

### Gestion des Modèles

Tous les modèles sont quantifiés pour de meilleures performances sur les appareils embarqués. Notez que les modèles avec une résolution d'entrée plus grande peuvent être meilleurs pour les petits objets malgré un mAP inférieur.

<p align="left">
<img src="../../../docs/images/ios_screen_model_management.gif" alt="Gestion des Modèles" width="25%" />
</p>

### Navigation

L'écran `Point Goal Navigation` permet de définir une destination pour le robot, avec des valeurs vers l'avant et vers la gauche dans l'espace 3D. Pour l'ouvrir, touchez le bouton correspondant dans l'application.

Une fenêtre apparaît alors avec deux champs, `Forward` et `Left`. Saisissez-y les valeurs de la destination. La fenêtre a aussi deux boutons : `Cancel` et `Start`.

- Bouton `Cancel` : ramène à l'écran d'accueil et annule la navigation.


- Bouton `Start` : crée un point dans l'espace 3D à partir des valeurs saisies et lance la navigation. Le robot se dirige vers la destination et ajuste sa trajectoire et sa vitesse pour l'atteindre.

Si le robot atteint la destination, un message l'indique et le robot s'arrête.

Cette fonctionnalité utilise `ARKit`, le framework de réalité augmentée d'Apple. Il faut un appareil compatible ARKit.

<p align="left">
<img src="../../../docs/images/ios_set_navigation_screen.jpg" alt="Model Management" width="25%" />
<img src="../../../docs/images/ios_navigation_screen.jpg" alt="Model Management" width="25%" />
</p>


### Écran Projets

L'écran Projets affiche la liste de vos projets OpenBot Playground si vous êtes connecté avec votre compte Google. Vous pouvez exécuter ces projets pour piloter votre OpenBot, ou scanner leur code QR avec l'icône du scanner en haut à droite. Si vous n'êtes pas connecté, l'écran affiche un bouton de connexion Google, mais vous pouvez quand même scanner le code QR de votre projet. Si le message `Oops, no project found` s'affiche après la connexion, vérifiez que le compte a des projets enregistrés sur Google Drive.

Si vos derniers projets n'apparaissent pas dans la liste, tirez l'écran vers le bas pour les recharger.

<p align="left">
<img src="../../../docs/images/projects_tab_screen_ios.gif" alt="Project Screen" width="25%"/>
<img src="../../../docs/images/no_projects_found_ios.jpg" alt="No project screen" style="padding-left: 0.3%" width="26.5%"/>
<img src="../../../docs/images/reload_projects_ios.gif" alt="Reload project screen" width="25%"/>
</p>

- **Projets Google Drive** : pour exécuter un projet Google Drive, touchez-le et attendez que le fichier du projet soit lu. Si tout se passe bien, une fenêtre apparaît avec le nom du projet et deux boutons : `Start` et `Cancel`. Start exécute le projet, Cancel l'annule. Si le message `Something went wrong` s'affiche, le fichier Google Drive pose probablement problème : rechargez l'écran en le tirant vers le bas et recommencez.


- **Scanner de code QR** : pour scanner le code QR d'un projet Playground, touchez l'icône du code QR en haut à droite. Autorisez l'application à utiliser la caméra. Une fois le code scanné, le fichier est lu. Si tout se passe bien, une fenêtre apparaît avec le nom du projet et deux boutons : `Start` et `Cancel`. Start exécute le projet, Cancel l'annule. Si le message `Something went wrong` s'affiche, le fichier Google Drive pose probablement problème : générez un nouveau code QR dans le Playground et recommencez.


- **Exécution du projet** : si votre projet s'exécute correctement, l'écran affiche le nom des blocs de code et un bouton d'arrêt pour stopper l'exécution.


- **Supprimer un projet** : appuyez longuement sur le projet à supprimer, puis confirmez avec « Yes ».

<p align="left">
<img src="../../../docs/images/google_drive_projects_execute_ios.gif" alt="Google Drive project execute" width="25%" />
<img src="../../../docs/images/ios_qr_scan.gif" alt="Qr code scanner project execute" width="25%" />
<img src="../../../docs/images/delete_project_ios.jpg" alt="Delete project popup" style="padding-right: 1.2%" width="26%" />
</p>


### Écran Profil
L'écran Profil propose différentes options selon que vous êtes connecté ou non.
Si vous n'êtes pas connecté, un bouton `Google Sign-in` vous invite à vous connecter avec votre compte Google. Une fois connecté, vous avez accès à votre profil et aux autres fonctionnalités.
Si vous êtes connecté, l'onglet `Profile` affiche deux boutons : `Edit Profile` et `Logout`.

<p align="left">
<img src="../../../docs/images/logged_out_profile_screen_ios.jpg" alt="Logged out profile screen" width="25%"/>
<img src="../../../docs/images/logged_in_profile_screen_ios.jpg" alt="Logged in profile screen" width="25%"/>
</p>

- **Edit Profile** : ouvre un écran où vous pouvez modifier les informations de votre profil, comme votre nom et votre photo.


- **Logout** : vous déconnecte de votre compte et vous ramène à l'écran de connexion.

<p align="left">
<img src="../../../docs/images/ios_edit_profile_screen.jpg" alt="Edit profile screen" width="25%"/>
<img src="../../../docs/images/logout_dialog_box_ios.jpg" alt="Logout dialog box" width="25%"/>
</p>


### Écran OpenBot PlayGround

Pour accéder aux services de l'OpenBot Playground, touchez l'icône OpenBot Playground en haut, dans la barre d'outils de l'écran Projets. Pour en savoir plus sur l'OpenBot Playground, [cliquez ici](../../../open-code/README.fr-FR.md).

<p align="left">
<img src="../../../docs/images/playground_services_ios.gif" alt="Playground Services" width="25%" />
<img src="../../../docs/images/playground_sign-in_ios.gif" alt="Playground sign-in" width="25%" />
</p>

## Structure du Code

Le [TensorFlow Lite Object Detection iOS Demo](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/ios) a été utilisé comme point de départ pour intégrer les modèles TFLite et obtenir le flux de la caméra. Le dossier [tflite](OpenBot/tflite) contient les définitions de modèles pour les réseaux [Pilote Automatique](OpenBot/tflite/Autopilot.swift) et [Détecteur](OpenBot/tflite/Detector.swift).

## Prochaines Étapes (optionnel)

Entraînez votre propre [Politique de Conduite](../../../policy/README.md)

Authentification Firebase [Firebase](../OpenBot/OpenBot/Authentication/README.fr-FR.md)
