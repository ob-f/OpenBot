# Application de Contrôle

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Cette application Android sert de `télécommande` pour le véhicule [OpenBot](https://www.openbot.org). Elle fonctionne de manière similaire à une manette PS3/4 ou Xbox, mais sur un autre appareil Android.

## Connexion

Lorsque l'application de contrôle est lancée, elle essaie immédiatement de se connecter au robot. Nous voyons l'écran suivant :

<p float="left">
  <img src="../../docs/images/controller_pre_connect.png" width="50%" />
</p>

Pour connecter la télécommande au robot, placez le paramètre de contrôle de l'application du robot en mode **Téléphone**.

<p float="left">
  <img src="../../docs/images/app_controller_settings_1.png" width="25%" />
  <img src="../../docs/images/app_controller_settings_2.png" width="25%" />
</p>

Vous pouvez également vous connecter à la télécommande depuis le `FreeRoamFragment` en sélectionnant le téléphone comme contrôleur :

<p float="left">
  <img src="../../docs/images/free-roam-fragment-selection.png" width="50%" />
</p>

En quelques secondes, vous entendrez un bip, et la télécommande changera son écran pour :

<p float="left">
  <img src="../../docs/images/controller_command_buttons.png" width="50%" />
</p>

Ici, vous pouvez choisir de piloter le robot en inclinant le téléphone ou en utilisant les commandes à l'écran.

***Remarque :*** Cela devrait suffire pour se connecter, mais si la connexion ne peut pas être établie après 30 secondes, basculez le paramètre `Contrôle` de l'application du robot sur `Manette` puis sur `Téléphone` à nouveau pour réinitialiser la connexion. Si cela échoue, quittez l'application de contrôle et redémarrez-la. Basculez à nouveau le mode de contrôle sur l'application du robot.

## Fonctionnement

### Commandes à l'écran
Ce mode permet à l'utilisateur de contrôler la voiture robot via deux curseurs en mode `Dual Drive`. Vous pouvez tourner à gauche/droite en déplaçant le curseur vers le haut et vers le bas de chaque côté. Les roues de chaque côté tournent vers l'avant/l'arrière lorsque vous déplacez le curseur au-dessus/en dessous du centre du curseur.

<p float="left">
  <img src="../../docs/images/controller_main_screen.png" width="50%" />
</p>

Vous pouvez également activer les clignotants gauche/droite 
<img src="../../docs/images/keyboard_arrow_left-24px.svg" height="24"/> 
<img src="../../docs/images/keyboard_arrow_right-24px.svg" height="24"/> 
en cliquant sur les flèches en haut à gauche de l'écran, et le bouton rouge entre elles pour annuler.

### Incliner pour conduire
La télécommande peut également utiliser son capteur de mouvement accéléromètre pour piloter le robot. Si vous sélectionnez cette option, la télécommande passera en mode plein écran (Zen) avec uniquement la vidéo affichée et des pédales de `frein` et `accélérateur`. Pour quitter ce mode, double-cliquez sur l'écran.

Voici une image de l'écran en `mode inclinaison` :

<p float="left">
  <img src="../../docs/images/tilt-mode-controller.jpg" width="50%" />
</p>

Utilisez les boutons `accélérateur` et `frein` pour avancer/reculer.

- Appuyer sur l'`accélérateur` fera accélérer le robot à pleine vitesse en 2 secondes. Lorsque vous relâchez le bouton, le robot ralentira jusqu'à s'arrêter (la vitesse d'arrêt est réglée à 0% de la vitesse maximale, peut être ajustée).
- Appuyer sur le bouton de `frein` arrêtera immédiatement le robot. Si nous maintenons le frein pendant une autre seconde, le robot commencera à reculer jusqu'à atteindre la vitesse maximale en marche arrière en une seconde. Lorsque nous relâchons le frein, le robot s'arrêtera.
- Nous dirigeons le robot en inclinant la télécommande à gauche ou à droite.

## Développement Futur

Certaines des fonctionnalités que nous envisageons d'ajouter sont :

- Ajouter des informations sur la télécommande pour plus de capteurs du robot, tels que le niveau de batterie et la vitesse.
- Flux vidéo de la caméra du robot vers la télécommande
- Utiliser le capteur gyroscopique de la télécommande pour contrôler le robot
- Envoyer des événements de collision et de choc du robot à la télécommande pour une expérience plus réaliste

Voici une [Vue d'ensemble technique](../../docs/technical/OpenBotController.pdf) de l'application de contrôle.
