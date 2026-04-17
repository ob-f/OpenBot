# OpenBot : Véhicules Prêts-à-l'Emploi (RTR)

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Les versions prêtes-à-l'emploi (RTR) du véhicule OpenBot sont destinées à un public qui n'a pas la volonté ou le temps de construire son propre robot. Les véhicules RTR sont livrés avec des composants électroniques entièrement intégrés, sont déjà pris en charge au niveau du firmware et ont été rigoureusement testés tant du point de vue logiciel que matériel. Les véhicules RTR sont disponibles en deux versions différentes, appelées "RTR_TT" et "RTR_520". Les deux véhicules sont construits autour de la même coque en ABS étanche, mais sont destinés à des usages différents. Alors que le RTR_TT est principalement destiné à une utilisation en intérieur, le RTR_520 est équipé d'un processeur plus puissant, de meilleurs moteurs, de boîtes de vitesses en métal plus robustes et dispose également d'un ensemble de roues tout-terrain permettant une utilisation à la fois en intérieur et en extérieur.
<p align="center">
  <a> <img src="/docs/images/RTR_TT.jpg" width="35.8%" /> &nbsp
  </a>
  <a> <img src="/docs/images/RTR_520.jpg" width="33%" />
  </a>
</p>

### Commande

Les véhicules OpenBot RTR peuvent être commandés [ici](http://www.openbot.info/).

## Construire votre propre RTR

Si vous souhaitez construire votre propre OpenBot RTR, vous devrez imprimer le châssis, fabriquer les PCB et acheter les moteurs et un support pour téléphone.

### Impression 3D

Si vous souhaitez toujours imprimer votre propre OpenBot RTR, vous devrez imprimer les pièces suivantes.

1) ```shell_bottom``` ([STL](cad/rtr_bottom.stl), [STEP](cad/rtr_bottom.step))
2) ```shell_top``` ([STL](cad/rtr_top.stl), [STEP](cad/rtr_top.step)) 
3) ```phone_mount``` ([STL](cad/rtr_mount.stl), [STEP](cad/rtr_mount.step))

<p align="center">
  <img src="../../docs/images/rtr_tt_assembly.gif" width="600" alt="App GUI"/>
</p>

### PCB

Pour chacun des PCB, il y a trois fichiers. Le fichier gerber contient le PCB réel, le fichier BOM (bill of materials) contient tous les composants à souder sur le PCB et le fichier centroid contient les coordonnées de chaque composant pour l'assemblage automatique du PCB. La carte de base contient la majorité des composants. Il existe trois variantes de la carte de base. La variante A est une carte nue avec des connecteurs pour des cartes de pilotes de moteur externes et une carte microcontrôleur externe. La variante B est une carte modulaire avec un connecteur à broches pour un microcontrôleur externe. La variante C est la carte de base entièrement intégrée que nous recommandons pour la plupart des utilisateurs. La carte de capteur de choc avant contient deux capteurs de choc, un capteur sonar et le pilote USB. Il existe deux variantes de la carte de capteur de choc avant, l'une avec le pilote USB CH340G moins cher et l'autre avec le pilote USB CP2102N plus fiable. Selon la version (moteur TT ou moteur 520) que vous souhaitez construire, vous aurez besoin des PCB suivants.

#### Moteur TT

- 1x Carte de base (Arduino)
- 1x Carte LED de statut
- 1x Carte de capteur de choc avant/haut/arrière
- 4x Carte de capteur de vitesse (Arduino)

#### Moteur 520

- 1x Carte de base (ESP32)
- 1x Carte LED de statut
- 1x Carte de capteur de choc avant/haut/arrière

#### Référence des cartes

- Carte LED de statut ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_Status_LED_Board_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_Status_LED_Board_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_Status_LED_Board_V1.csv))
- Carte de capteur de choc haut ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BumpSensorTop_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BumpSensorTop_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BumpSensorTop_V1.csv))
- Carte de capteur de choc arrière ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BumpSensorBack_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BumpSensorBack_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BumpSensorBack_V1.csv))
- Carte de capteur de choc avant (CH340G) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SensorBoardFront_CH340G_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SensorBoardFront_CH340G_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SensorBoardFront_CH340G_V1.csv))
- Carte de capteur de choc avant (CP2102N) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SensorBoardFront_CP2102N_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SensorBoardFront_CP2102N_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SensorBoardFront_CP2102N_V1.csv))
- Carte de capteur de vitesse (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SpeedSensor_Arduino_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SpeedSensor_Arduino_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SpeedSensor_Arduino_V1.csv))
- Carte de base intégrée C (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1C.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1C.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1C.csv))
- Carte de base intégrée C (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1C.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1C.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1C.csv))
- Carte de base modulaire B (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1B.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1B.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1B.csv))
- Carte de base modulaire B (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1B.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1B.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1B.csv))
- Carte de base nue A (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1A.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1A.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1A.csv))
- Carte de pilote de moteur DRV8870 (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_MotorBoard_Arduino_V1_DRV8870.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_MotorBoard_Arduino_V1_DRV8870.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_MotorBoard_Arduino_V1_DRV8870.csv))
- Carte de base nue A (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1A.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1A.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1A.csv))
- Carte de pilote de moteur DRV8870 (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_MotorBoard_ESP32_V1_DRV8870.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_MotorBoard_ESP32_V1_DRV8870.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_MotorBoard_ESP32_V1_DRV8870.csv))

## Ensuite

Flashez le [Firmware Arduino](../../firmware/README.md)