# Politique de Conduite (Avancée)

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## AVERTISSEMENTS

1. **Sécurité :** Les politiques de conduite ne sont pas parfaites et peuvent provoquer des accidents du robot. Assurez-vous toujours d'opérer dans un environnement sûr ! Gardez à l'esprit que votre téléphone pourrait être endommagé en cas de collision ! Assurez-vous d'avoir toujours une manette de jeu connectée et de bien connaître la configuration des touches pour pouvoir arrêter le véhicule à tout moment. Utilisez à vos propres risques !
2. **Matériel informatique :** L'entraînement d'une politique de conduite nécessite beaucoup de ressources et peut ralentir ou même bloquer votre machine. Il est recommandé d'utiliser un ordinateur portable haut de gamme ou une station de travail avec une grande quantité de RAM et un GPU dédié, surtout lors de l'entraînement avec des lots de données plus importants. La documentation n'est actuellement pas très détaillée. Utilisez à vos propres risques !
3. **Patience requise :** Pour obtenir une bonne politique de conduite pour votre ensemble de données personnalisé, il faudra de la patience. Ce n'est pas simple, cela implique la collecte de données, le réglage des hyperparamètres, etc. Si vous n'avez jamais entraîné de modèles d'apprentissage automatique auparavant, cela sera difficile et peut même devenir frustrant.

Vous devez d'abord configurer votre environnement d'entraînement.

## Dépendances

Nous recommandons de créer un environnement conda pour OpenBot. Les instructions pour installer conda se trouvent [ici](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). La manière la plus simple de créer un nouvel environnement avec toutes les dépendances est d'utiliser l'un des fichiers d'environnement fournis. Sous Windows, vous devrez également installer [Microsoft C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/). Assurez-vous d'être dans le dossier `policy` de votre dépôt local OpenBot. En fonction de votre système d'exploitation, exécutez la commande correspondante :

- **MacOS** : `conda env create -f environment_mac.yml`
- **Windows** : `conda env create -f environment_win.yml`
- **Linux** : `conda env create -f environment_linux.yml`

Pour le support GPU, assurez-vous également d'avoir les pilotes appropriés installés. Sur Mac et Windows, tout devrait fonctionner directement. Sur Linux, vous pouvez installer les pilotes avec la commande suivante :
```
sudo apt-get install nvidia-driver-510
```
Sur Linux, vous devrez probablement également exécuter les commandes suivantes pour ajouter cuda et cudnn à votre chemin :
```
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```

C'est fait ! Vous êtes prêt à entraîner vos propres modèles. Si cela ne fonctionne pas pour vous, ci-dessous se trouvent les instructions pour configurer un tel environnement manuellement.

### Configuration manuelle de l'environnement

Créez d'abord un nouvel environnement conda avec la commande suivante :

```bash
conda create -n openbot pip python=3.9 -y
```

Ensuite, vous devez activer votre environnement conda :

```bash
conda activate openbot
```

Si cela ne fonctionne pas (par exemple sous Windows), vous devrez peut-être activer l'environnement avec `activate openbot` à la place.

Une fois l'environnement activé, vous devez installer tensorflow. Notez que l'entraînement sera très lent sur un ordinateur portable. Donc, si vous avez accès à un ordinateur avec un GPU dédié, nous vous recommandons fortement de l'utiliser en installant les bibliothèques nécessaires ; assurez-vous d'avoir des pilotes GPU récents installés. Voici les commandes pour installer tensorflow pour différents systèmes d'exploitation.

#### **Mac OS**
```
conda install -c apple tensorflow-deps -y
pip install tensorflow-macos~=2.9.0
```
Support GPU
```
pip install tensorflow-metal~=0.5.0
```
[Résolution des problèmes](https://developer.apple.com/metal/tensorflow-plugin/)

#### **Linux**
```
pip install tensorflow~=2.9.0
```
Support GPU
```
sudo apt-get install nvidia-driver-510
conda install -c conda-forge cudatoolkit=11.2 cudnn=8.1 -y
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```
[Résolution des problèmes](https://www.tensorflow.org/install/pip#linux)

#### **Windows**
```
pip install tensorflow~=2.9.0
```
Support GPU
```
conda install cudatoolkit=11.3 cudnn=8.2 -y
```

#### **Dépendances supplémentaires**

Assurez-vous d'être dans le dossier `policy` de votre dépôt local OpenBot. Maintenant, vous pouvez installer toutes les dépendances restantes avec la commande suivante :

```bash
pip install -r requirements.txt
```

Vous pouvez également installer pydot (`pip install pydot`) et graphviz ([voir les instructions](https://graphviz.gitlab.io/download/)) si vous souhaitez visualiser l'architecture du réseau.

Si vous souhaitez utiliser la [WebApp](#web-app) pour la collecte de données et l'entraînement, vous devez installer les dépendances suivantes en plus. (Sur Mac, le package `brotlipy` est actuellement cassé sur pip, vous devez donc l'installer d'abord en utilisant conda : `conda install brotlipy=0.7`)

```bash
pip install -r requirements_web.txt
```

### Packages essentiels

Pour référence et dépannage, voici une liste des packages essentiels.

Entraînement :

- [tensorflow](https://pypi.org/project/tensorflow/)
- [jupyter notebook](https://pypi.org/project/notebook/)
- [matplotlib](https://pypi.org/project/matplotlib/)
- [numpy](https://pypi.org/project/numpy/)
- [PIL](https://pypi.org/project/Pillow/)
- [black[jupyter]](https://pypi.org/project/black/)

Interface web :

- [aiohttp](https://pypi.org/project/aiohttp/)
- [aiozeroconf](https://pypi.org/project/aiozeroconf/)
- [imageio](https://pypi.org/project/imageio/)

### Remarques

- N'oubliez pas d'activer l'environnement avant d'exécuter des commandes dans le terminal : `conda activate openbot`
- Si l'importation de tensorflow ne fonctionne pas, essayez d'installer via `pip install tensorflow --user`. (Voir ce [problème](https://github.com/ob-f/OpenBot/issues/98).)

## Ensemble de données

### Collecte de données

Pour entraîner une politique de conduite autonome, vous devrez d'abord collecter un ensemble de données. Plus vous collectez de données, meilleure sera la politique de conduite résultante. Pour les expériences dans notre article, nous avons collecté environ 30 minutes de données. Notez que le réseau imitera votre comportement de conduite. Plus vous conduisez de manière cohérente et précise, mieux le réseau apprendra à conduire.

1. Connectez une manette de jeu Bluetooth au téléphone (par exemple, manette PS4 : pour entrer en mode de couplage, appuyez sur les boutons PS et partage jusqu'à ce que la LED clignote rapidement).
2. Sélectionnez le modèle `CIL-Mobile-Cmd` dans l'application.
3. Conduisez maintenant la voiture via une manette de jeu et enregistrez un ensemble de données. Sur la manette PS4, l'enregistrement peut être activé/désactivé avec le bouton **X**.

Vous trouverez maintenant un dossier appelé *Documents/OpenBot* dans le stockage interne de votre smartphone. Pour chaque enregistrement, il y aura un fichier zip. Le nom du fichier zip sera au format *yyyymmdd_hhmmss.zip* correspondant à l'horodatage du début de l'enregistrement.

Le notebook Jupyter s'attend à un dossier appelé `dataset` dans le même dossier. Dans ce dossier, il doit y avoir deux sous-dossiers, `train_data` et `test_data`. Les données d'entraînement sont utilisées pour apprendre la politique de conduite. Les données de test sont utilisées pour valider la politique de conduite apprise sur des données non vues pendant le processus d'entraînement. Cela donne une indication de la performance de cette politique sur le robot. Même si le robot peut conduire le long de la même route que celle vue pendant l'entraînement, les images exactes observées seront légèrement différentes à chaque passage. La répartition courante est de 80 % de données d'entraînement et 20 % de données de test. À l'intérieur des dossiers `train_data` et `test_data`, vous devez créer un dossier pour chaque session d'enregistrement et lui donner un nom tel que `my_openbot_1`, `my_openbot_2`, etc. L'idée ici est que chaque session d'enregistrement peut avoir des conditions d'éclairage différentes, un robot différent, une route différente. Dans le notebook Jupyter, vous pouvez ensuite entraîner uniquement sur un sous-ensemble de ces ensembles de données ou sur tous. À l'intérieur de chaque dossier de session d'enregistrement, vous déposez tous les enregistrements de cette session. Chaque enregistrement correspond à un fichier zip extrait que vous avez transféré du dossier *Openbot* sur votre téléphone. Votre dossier de données devrait ressembler à ceci :

<img src="../docs/images/folder_structure.png" width="200" alt="structure du dossier" />

Plutôt que de copier tous les fichiers manuellement depuis le téléphone, vous pouvez également télécharger les journaux automatiquement sur un [serveur Python](#web-app) sur votre ordinateur. Dans ce cas, les fichiers zip seront téléchargés et décompressés dans le dossier `dataset/uploaded`. Vous devrez toujours les déplacer dans la structure de dossiers pour l'entraînement. Vous pouvez simplement traiter le dossier `uploaded` comme une session d'enregistrement et le déplacer dans `train_data`. Les enregistrements seront alors reconnus comme des données d'entraînement par le notebook Jupyter. Si vous n'avez pas déjà une session d'enregistrement dans le dossier `test_data`, vous devez également déplacer au moins un enregistrement de `train_data/uploaded` vers `test_data/uploaded`.

### Conversion des données (optionnel)

Pour de meilleures performances d'entraînement, vous pouvez convertir l'ensemble de données collecté en un format spécialisé. Vous pouvez créer un tfrecord des ensembles de données d'entraînement et de test avec les commandes suivantes :

```bash
conda activate openbot
python -m openbot.tfrecord -i dataset/train_data -o dataset/tfrecords -n train.tfrec
python -m openbot.tfrecord -i dataset/test_data -o dataset/tfrecords -n test.tfrec
```

Par défaut, cette conversion sera effectuée automatiquement au début de l'entraînement.

## Entraînement de la politique

Assurez-vous que votre environnement conda pour openbot est activé en exécutant la commande suivante :

```bash
conda activate openbot
```

### Jupyter Notebook

Nous fournissons un [Jupyter Notebook](policy_learning.ipynb) qui vous guide à travers les étapes pour entraîner une politique de conduite autonome. Ouvrez le notebook avec la commande suivante.

```bash
jupyter notebook policy_learning.ipynb
```

Une fenêtre de navigateur web s'ouvrira automatiquement et chargera le notebook Jupyter. Suivez les étapes pour entraîner un modèle avec vos propres données.

### Shell

Cette méthode suppose que les données sont au bon endroit. Pour ajuster les hyperparamètres, vous pouvez passer les arguments suivants.

```bash
'--no_tf_record', action='store_true', help='ne pas charger un tfrecord mais un répertoire de fichiers'
'--create_tf_record', action='store_true', help='créer un nouveau tfrecord'
'--model', type=str, default='pilot_net', choices=['cil_mobile', 'cil_mobile_fast', 'cil', 'pilot_net'], help='architecture du réseau (par défaut : cil_mobile)'
'--batch_size', type=int, default=16, help='nombre d'époques d'entraînement (par défaut : 16)'
'--learning_rate', type=float, default=0.0001, help='taux d'apprentissage (par défaut : 0.0001)'
'--num_epochs', type=int, default=10, help='nombre d'époques (par défaut : 10)'
'--batch_norm', action='store_true', help='utiliser la normalisation par lots'
'--flip_aug', action='store_true', help='retourner aléatoirement les images et les commandes pour l'augmentation'
'--cmd_aug', action='store_true', help='ajouter du bruit à l'entrée de commande pour l'augmentation'
'--resume', action='store_true', help='reprendre l'entraînement précédent'
```

Si votre ensemble de données a déjà été converti en tfrecord, vous pouvez entraîner la politique depuis le shell avec la commande :

```bash
python -m openbot.train
```

Si vous souhaitez convertir votre ensemble de données en tfrecord avant l'entraînement, vous devez ajouter le drapeau suivant :

```bash
python -m openbot.train --create_tf_record
```

Si vous ne souhaitez pas convertir l'ensemble de données en tfrecord et entraîner en utilisant directement les fichiers, vous devez ajouter le drapeau suivant :

```bash
python -m openbot.train --no_tf_record
```

Pour entraîner un modèle pour le déploiement final, vous voudrez utiliser une grande taille de lot et un nombre d'époques élevé. Activer la normalisation par lots améliore généralement l'entraînement également. Le modèle `pilot_net` est plus grand que le `cil_mobile` par défaut mais peut obtenir de meilleures performances sur certaines tâches tout en fonctionnant en temps réel sur la plupart des smartphones.

```bash
python -m openbot.train --model pilot_net --batch_size 128 --num_epochs 100 --batch_norm
```

### Déploiement

À la fin du processus d'entraînement, deux fichiers tflite sont générés : l'un correspond au meilleur point de contrôle selon les métriques de validation et l'autre au dernier point de contrôle. Choisissez l'un d'eux et renommez-le en autopilot_float.tflite. Remplacez le modèle existant dans Android Studio et recompilez l'application.

<p align="center">
  <img src="../docs/images/android_studio_tflite_dir.jpg" width="200" alt="GUI de l'application" />
</p>

Si vous cherchez le dossier dans votre répertoire local, vous le trouverez à : `app/src/main/assets/networks`.

## Application Web

Nous fournissons une application web et un serveur web python pour un entraînement de politique facile. (Beta)

### Fonctionnalités

- Téléchargement automatique des journaux (session)
  - voir Résolution des problèmes pour plus de détails
- Lister les sessions téléchargées, avec aperçu GIF
- Lister les ensembles de données, avec des informations de base
- Déplacer une session vers un ensemble de données
- Supprimer une session
- Lister les modèles entraînés et afficher des graphiques sur l'entraînement
- Entraîner un modèle avec des paramètres de base, afficher une barre de progression

### Aperçu

<img src="../docs/images/web-app.gif" width="100%" alt="Aperçu de l'application web" />

### Démarrage rapide

```bash
conda activate openbot
python -m openbot.server
```

Vous pouvez maintenant ouvrir votre navigateur pour visualiser l'ensemble de données et voir les téléchargements entrants en allant à :
[http://localhost:8000/#/uploaded](http://localhost:8000/#/uploaded)

### Exécution du serveur

Vous pouvez exécuter le serveur python avec la commande :

```bash
python -m openbot.server
```

Il existe également un mode développeur :

```bash
adev runserver openbot/server
```

Pour le développement frontend (application react) :

```
FE_DEV=1 adev runserver openbot/server
```

Lorsque vous exécutez le serveur, vous devriez voir quelque chose comme :

```
Skip address 127.0.0.1 @ interface lo
Found address 192.168.x.x @ interface wlp2s0
Registration of a service, press Ctrl-C to exit...
Running frontend: 0.7.0
Frontend path: /home/NOM_UTILISATEUR/miniconda3/envs/openbot/lib/python3.7/site-packages/openbot_frontend
======== Running on http://0.0.0.0:8000 ========
(Press CTRL+C to quit)
```

### Résolution des problèmes

Si le téléchargement vers le serveur ne fonctionne pas, voici quelques conseils de dépannage :

- Essayez de redémarrer le serveur (ordinateur) et l'application OpenBot (smartphone)
- Assurez-vous que le smartphone et votre ordinateur sont connectés au même réseau WiFi
- Si votre routeur a à la fois des réseaux 2,4 GHz et 5 GHz avec le même nom, désactivez le réseau 5 GHz
- Gardez le téléphone connecté à Android Studio pendant l'exécution de l'application. Dans l'onglet Logcat, sélectionnez Debug dans le menu déroulant. Tapez `NSD` dans le champ de filtre pour voir les messages de débogage concernant la connexion au serveur. Tapez `Upload` dans le champ de filtre pour les messages de débogage concernant le téléchargement du fichier d'enregistrement.
- Si un modèle publié est téléchargé en continu, assurez-vous que l'heure sur votre téléphone et votre ordinateur portable / station de travail est correctement réglée.