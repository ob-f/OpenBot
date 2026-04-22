# OpenBot Nodejs Controller

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## Nomenclature

Voici quelques termes que nous utiliserons dans ce document :

* ```Robot, bot``` - c'est le logiciel Android fonctionnant sur le téléphone du véhicule [OpenBot](https://www.openbot.org/).
* ```Serveur``` - le serveur node, la partie serveur de ce projet.
* ```Client, UI``` - c'est la partie client de ce projet. Elle fonctionne dans le navigateur.

## Introduction

Ceci est un projet basé sur [Node.js](https://nodejs.org/) agissant comme un contrôleur pour le véhicule [OpenBot](https://www.openbot.org/). Le logiciel comprend deux parties : un serveur et un client. Le serveur est une application Node.js fonctionnant sur un ordinateur du même réseau que le Robot. La partie client fonctionne dans le navigateur.

Voici une capture d'écran du navigateur :

![Screenshot](images/Screenshot.png "image_tooltip")

## Pour Commencer

Vous pouvez exécuter ce logiciel sur un PC, un appareil de type RaspberryPi ou même des appareils [Pi Zero](https://www.raspberrypi.com/products/raspberry-pi-zero/) qui supportent l'environnement ```Node.js```. Assurez-vous d'abord d'avoir installé [Node.js](https://nodejs.org/), version 10 ou plus récente. Vérifiez la version :

    node --version

Le logiciel se trouve dans le répertoire ```/controller/node-js``` du projet OpenBot. Après avoir récupéré le code depuis [github](https://github.com/ob-f/OpenBot), changez de répertoire et exécutez les commandes suivantes :

    npm install
    npm start

La dernière commande démarrera le serveur. Si vous souhaitez exécuter le serveur en arrière-plan sans terminal, sur ```Linux/MacOS``` vous pouvez exécuter :

    npm run start-nohup

ou simplement :

    nohup npm start

Pointez votre navigateur vers l'adresse IP du serveur au port 8081, par exemple [http://localhost:8081](http://localhost:8081), ou [http://192.168.1.100:8081](http://192.168.1.100:8081). Notez que vous pouvez accéder au serveur depuis un autre ordinateur, mais le Robot, le serveur et le PC navigateur doivent être sur le même réseau. À l'avenir, nous pourrons ajouter la possibilité d'accéder au serveur à distance.

Assurez-vous que votre Robot est connecté au même réseau. Sur l'application Android du Robot, allez dans le panneau ```Général``` et sélectionnez ```Phone``` comme contrôleur. Cela connectera l'application Android au serveur Node, et une vidéo apparaîtra sur l'UI.

## Comment ça Marche

1. Le serveur Node crée et publie un service DNS de type ```openbot.tcp``` et un nom ```OPEN_BOT_CONTROLLER``` au port 19400. Cela permet au Robot de trouver automatiquement le serveur sans connaître son adresse IP. Le Robot recherche ce service et établira une connexion Socket lorsqu'il sera mis en mode contrôleur ```Phone```.

2. Le serveur Node crée un serveur HTTP au port 8081 et commence à servir les requêtes du navigateur.

3. De plus, le serveur node crée un serveur WebSocket au port 7071. Cela sera utilisé pour communiquer directement avec le navigateur. Pour résumer jusqu'à présent, le serveur a créé deux connexions Socket, une vers le Robot et une vers le Navigateur.

4. L'utilisateur entre des commandes clavier depuis le navigateur. Ces pressions de touches sont envoyées au Serveur via le WebSocket. Le serveur les convertit en commandes que le Robot peut comprendre, comme ```{driveCmd: {r:0.4, l:0.34}}``` (une liste de toutes les commandes peut être trouvée dans la documentation du contrôleur Android [ici](https://github.com/ob-f/OpenBot/blob/master/docs/technical/OpenBotController.pdf)). Ces commandes sont envoyées au Robot via la connexion Socket.

5. Le Robot envoie des informations de statut au serveur sur la connexion Socket, et le serveur les transmet à l'UI. L'UI peut utiliser ces informations pour améliorer son apparence, comme afficher des indicateurs clignotants, etc., mais actuellement ce statut est ignoré.

6. Le serveur node agit également comme un proxy de signalisation WebRTC. Il transmet les commandes de négociation WebRTC entre le robot et le navigateur. Il réutilise les connexions socket ouvertes à cet effet, donc aucune connexion ou configuration supplémentaire n'est nécessaire.

![drawing](images/HowItWorks.png)

## Développement

Ce code utilise [snowpack](https://www.snowpack.dev/) pour un outil de construction rapide et léger.

Nous utilisons [eslint](https://eslint.org/) pour le linting et le formatage automatique de votre code. Il est recommandé d'exécuter le lint et de corriger les erreurs avant de valider un nouveau code. Si vous utilisez Visual Code, vous pouvez obtenir un plugin [ici](https://marketplace.visualstudio.com/items?itemName=dbaeumer.vscode-eslint). Exécutez le linter comme ceci :

    npm run lint

## Production

Pour construire une version de production du ```client```, exécutez :

    npm run build

Cela optimisera le code client dans un répertoire ```build```, qui peut être déployé sur un serveur. De plus, nous devons configurer un gestionnaire de processus pour redémarrer le serveur, et éventuellement un proxy inverse comme [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/), ce qui n'est pas encore fait.

## Dépannage

* Parfois, le navigateur n'affiche pas le menu des commandes, juste le titre. Cela signifie que la connexion WebSocket n'a pas pu être établie. Cela se produit généralement juste après le démarrage du serveur. Si vous examinez la console du navigateur, vous pouvez trouver un message indiquant qu'il n'a pas pu se connecter, quelque chose comme ```WebSocket connection to 'ws://localhost:7071/ws' failed```. Tuez tous les processus node (pkill -9 node) et redémarrez-le. Rechargez la page et la connexion devrait être établie.
* Si vous ne pouvez pas connecter le téléphone à l'application, assurez-vous qu'une autre instance de cette application ne fonctionne pas sur cette machine ou une autre machine sur le même réseau.

## Bugs Connus

Aucun.

## Choses à faire/essayer

* Nous devons étudier si nous pouvons nous connecter au serveur à distance, et si WebRTC fonctionnera toujours. Nous devrions documenter la configuration du pare-feu pour rendre cela possible.
* Nous devons créer une configuration ```production```, éventuellement en utilisant [pm2 process manager](https://www.npmjs.com/package/pm2) et [nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/).