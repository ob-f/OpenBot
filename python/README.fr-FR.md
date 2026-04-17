# Python

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Ce module est une alternative Linux embarqué au contrôle par smartphone d'un véhicule OpenBot. Écrit en Python, l'OpenBot peut être contrôlé à l'aide d'un ordinateur basé sur Linux et d'une caméra pour la détection.

Le robot peut être contrôlé de deux manières : par inférence d'une politique de réseau neuronal ou via un joystick.

```
├── __init__.py
├── README.md
├── requirements.txt
├── run.py
├── generate_data_for_training.py
├── export_openvino.py
├── infer.py
├── joystick.py
├── realsense.py
└── tests
    ├── test_data
    │   └── logs1
    │       └── ...
    ├── test_models
    │   ├── openvino
    │   ├── tflite
    │   └── tf.zip
    ├── test_export_openvino.py
    ├── test_infer.py
    ├── test_joystick.py
    ├── test_motor.py
    └── test_realsense.py

```
## Exécution du Robot

Pour faire fonctionner le robot, exécutez le `run.py`, qui est le script principal en Python. Le robot peut fonctionner en 3 modes :
- Debug : Ce mode exécute la politique hors ligne. C'est-à-dire qu'au lieu d'utiliser des images de caméra réelles et des commandes d'entrée de joystick, il utilise des données (commandes et images) chargées à partir d'un ensemble de données (voir `tests/test_data/logs1/data`) comme entrée pour la politique.
- Inference : Ce mode exécute la politique en ligne. Il utilise des images de caméra réelles et des commandes d'entrée de joystick comme entrée pour la politique. Ce mode peut être basculé en mode Joystick en appuyant sur la touche `A` du joystick.
- Joystick : Ce mode fait fonctionner le robot via des commandes de joystick en mode "Dual" (contrôle des roues gauche et droite via les joysticks gauche et droit) ou "Joystick" (contrôle de la direction avant, arrière, gauche, droite via un joystick) `control_mode`. La collecte de données pour l'entraînement se fait en mode Joystick. Ce mode peut être basculé en mode Inference en appuyant sur la touche `A` du joystick.

Le script run.py accepte six arguments (pour plus de détails, voir `run.py`) :
```
--policy_path: Chemin vers le fichier de politique.
--dataset_path: Chemin vers l'ensemble de données. Utilisé uniquement pour le mode debug.
--log_path: Chemin vers le dossier de logs, où les exécutions sont enregistrées.
--inference_backend: Backend à utiliser. Considérez l'exportation de tous les modèles en tant que modèle openvino pour des performances maximales. Options : tf, tflite, openvino.
--mode: Mode de fonctionnement. Options : debug, inference, joystick.
--control_mode: Mode de contrôle en mode joystick. Options : dual, joystick.
```
## Génération de Données d'Entraînement
Le script `generate_data_for_training.py` génère un dossier de données de log nécessaire pour entraîner une politique via le script `OpenBot/policy/openbot/train.py`. Le dossier de données de log contient un dossier `images` et un dossier `sensor_data` au format requis par `train.py`.

Voir `tests/test_generate_data.py` pour un exemple.

## OpenVino : Optimisation des Performances d'Inférence de la Politique
Pour optimiser la vitesse d'inférence sur le matériel Intel pris en charge (comme la carte [Up Core Plus](https://up-board.org/upcoreplus/specifications/)), le modèle entraîné doit être exporté vers OpenVino.

Le script `export_openvino.py` exporte un modèle TensorFlow entraîné vers un modèle OpenVino. Ce modèle OpenVino est ensuite chargé via `get_openvino_interpreter()` dans `infer.py`.

Voir `tests/test_export_openvino.py` pour un exemple.

## Tests et code d'exemple

**Remarque :** Pour tester le code, les données de test et le modèle de test appelés `test_data` et `test_model` respectivement doivent être dans `OpenBot/python/tests`. La fonction `get_data()` dans `download_data.py` fournit une fonctionnalité de téléchargement et est appelée au début de `test_infer.py`, `test_export_openvino.py`, et `test_generate_data.py`. Alternativement, veuillez exécuter le script `get_test_data.sh` (systèmes Unix uniquement) qui télécharge et décompresse un fichier zip contenant `test_data` et `test_models` avec les données pour le mode debug et les modèles pour l'inférence respectivement.

Exécutez `pytest` dans le dossier `tests` ou exécutez les fichiers `test_*.py` individuellement pour tester les fonctionnalités de

- téléchargement des données de test et du modèle de test depuis le cloud via `test_download_data.py`
- exportation vers OpenVino via `test_export_openvino.py`
- génération de données d'entraînement via `test_generate_data.py`.
- inférence en mode debug pour OpenVino, Tensorflow, et Tflite via `test_infer.py`.
    - *Remarque*: Les données de test dans logs1 sont générées à l'aide du script `associate_frames.py` dans `OpenBot.policy.openbot`, où le chemin vers les images est codé en dur dans `logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt`.
    - Ainsi, veuillez remplacer le `path_to_openbot` par le chemin réel vers le dépôt `OpenBot` dans `test_infer.py`.
- connexion du joystick via `test_joystick.py`
- connexion du moteur du port série à l'Arduino via `test_motor.py`.
- flux vidéo vers la caméra Realsense via `test_realsense.py`.

# Installation
Le processus d'installation est détaillé ci-dessous.

L'implémentation Python pour contrôler OpenBot nécessite quelques modules Python pour l'inférence, le contrôle du joystick, la détection et l'actionnement.
De plus, des pilotes pour la caméra ou le contrôleur peuvent être nécessaires.

## Configuration
Actuellement, le code est testé sur :
- Carte : [Up Core Plus](https://up-board.org/upcoreplus/specifications/)
- Caméra : [Realsense D435i](https://www.intelrealsense.com/depth-camera-d435i/)
- Contrôleur : [Xbox One](https://www.microsoft.com/en-gb/store/collections/xboxcontrollers?source=lp)
- Arduino : [OpenBot Firmware](https://github.com/ob-f/OpenBot/blob/master/firmware/README.md)

## Modules Python

Le code est testé avec Python 3.9. En utilisant Anaconda3 :
```
conda create --name openbot python==3.9
```

Tout d'abord, installez les exigences de OpenBot.policy via
```
../policy && pip install -r requirements.txt
```

Ensuite, installez les modules requis via
```
pip install -r requirements.txt
```

En particulier,
- `pyserial` communique avec l'Arduino et donc les moteurs via le port série
- `pyrealsense2` et `opencv-python` sont nécessaires pour le traitement des images de la caméra.
- `pygame` est utilisé pour le contrôle du joystick et le traitement des entrées du joystick
- `openvino-dev[tensorflow2,extras]` est utilisé pour des performances accrues sur le matériel Intel pris en charge. Pour plus de détails sur l'inférence AI optimisée sur le matériel Intel, veuillez consulter [OpenVino](https://docs.openvino.ai/latest/home.html). OpenVino est le backend d'inférence recommandé. Tensorflow et Tflite sont également pris en charge (voir Tests). Pour exécuter des modules PyTorch, veuillez envisager de convertir PyTorch en un backend OpenVino (voir [ce tutoriel](https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html)).

## Pilotes
Si le code est exécuté sur Ubuntu, le dongle USB sans fil du contrôleur Xbox One nécessite un pilote, qui peut être trouvé à [ce lien](https://github.com/medusalix/xone).

## Tensorflow pour l'Inférence
Si TensorFlow est utilisé pour l'inférence, veuillez ajouter le module Python `policy` à `PYTHONPATH` via `export PYTHONPATH=$PYTHONPATH:/path/to/OpenBot/policy`. Cette solution de contournement évite d'avoir à installer openbot en tant que module et de trouver `openbot.utils.load_model()`, qui est nécessaire pour charger le modèle tensorflow. Pour plus de détails, voir `get_tf_interpreter()` dans `infer.py` et le code de test `tests/test_infer.py`.

## Support pour les distributions non-linux (MacOS, Windows)

Veuillez noter que le code est destiné à fonctionner sur des ordinateurs basés sur Linux, par exemple, Up Core Plus. Certains modules python peuvent ne pas être disponibles pour MacOs ou Windows.

Le code peut fonctionner sur MacOS à des fins de débogage avec les modifications suivantes :
- Utilisez `pyrealsense2-macosx` au lieu de `pyrealsense2` dans requirements.txt
- Pour tflite, suivez [ces instructions](https://github.com/milinddeore/TfLite-Standalone-build-Linux-MacOS)