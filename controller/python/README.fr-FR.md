# Contrôleur Python

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Ce programme Python vous permet de contrôler le robot à partir d'un clavier (sans fil) et de recevoir un flux vidéo de la caméra. Le programme peut fonctionner sur n'importe quel ordinateur connecté au même réseau que le téléphone du robot. Il a été développé et testé sur un Raspberry Pi 3 et un MacBook. Avant de suivre les étapes ci-dessous, assurez-vous d'avoir le [code source](https://github.com/ob-f/OpenBot#get-the-source-code) et de naviguer vers le dossier `controller`.

## Dépendances

Nous recommandons de créer un environnement conda pour OpenBot (si ce n'est pas déjà fait). Les instructions pour installer conda se trouvent [ici](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). Vous pouvez créer un nouvel environnement avec la commande suivante :

```bash
conda create -n openbot python=3.7
```

Si vous ne souhaitez pas installer les dépendances globalement, activez d'abord votre environnement conda :

```bash
conda activate openbot
```

Assurez-vous d'être dans le dossier `controller` de votre dépôt local OpenBot. Maintenant, vous pouvez installer toutes les dépendances avec la commande suivante :

```bash
pip install -r requirements.txt
```

## Contrôler le robot

REMARQUE : Après une connexion réussie, il peut ne pas être possible de se reconnecter à moins que l'application du robot ne soit redémarrée.

Les scripts Python attendront une connexion entrante. Sur le téléphone avec l'application du robot, allez au fragment FreeRoam et basculez le mode de contrôle sur l'icône du téléphone. Le robot essaiera maintenant de se connecter au script Python (de la même manière qu'il se connecterait à l'application du contrôleur). Alternativement, vous pouvez également utiliser DefaultActivity et sélectionner `Phone` comme contrôleur.

### Utilisation de Pygame

Ces scripts vous permettent de piloter le robot en utilisant le clavier de manière similaire à un jeu de course automobile.

Exécutez le contrôleur sans vidéo :

`python keyboard-pygame.py`

Exécutez le contrôleur avec vidéo :

`python keyboard-pygame.py --video`

Voici l'utilisation :

```
    W:        Avancer
    S:        Reculer
    A:        Tourner légèrement à gauche (en conduisant)
    D:        Tourner légèrement à droite (en conduisant)
    Q:        Pivoter à gauche
    E:        Pivoter à droite

    M:        Mode de conduite
    N:        Basculer le bruit
    Gauche:   Indicateur gauche
    Droite:   Indicateur droit
    Haut:     Annuler les indicateurs
    Bas:      Mode réseau
    ESPACE:   Basculer la journalisation
    ÉCHAP:    Quitter
```

### Utilisation de Click

Il existe également un script pour le prototypage qui permet de régler le contrôle du robot par incréments plutôt que de le contrôler dynamiquement. Ce script utilise la bibliothèque click et nécessite que le terminal reste au premier plan.

Exécutez le contrôleur :

`python keyboard-click.py`

Voici l'utilisation :

```bash
    W:        Augmenter la vitesse
    S:        Diminuer la vitesse
    A:        Tourner plus à gauche
    D:        Tourner plus à droite
    R:        Réinitialiser les contrôles

    M:        Mode de conduite
    N:        Basculer le bruit
    Gauche:   Indicateur gauche
    Droite:   Indicateur droit
    Haut:     Annuler les indicateurs
    Bas:      Mode réseau
    ESPACE:   Basculer la journalisation
    ÉCHAP:    Quitter
```