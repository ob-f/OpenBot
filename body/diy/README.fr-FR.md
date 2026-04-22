# OpenBot DIY

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Nous avons conçu un châssis pour un robot à roues qui repose sur du matériel de loisir peu coûteux et facilement disponible. Vous trouverez ci-dessous des instructions pour construire votre propre robot. Si vous avez d'autres questions ou préoccupations, n'hésitez pas à nous contacter. Bonne construction de robot !

## Châssis

### Impression 3D

Vous devrez imprimer et assembler les pièces suivantes pour construire votre OpenBot.

#### Corps du robot

Il existe plusieurs options pour le corps du robot en fonction de vos besoins et des capacités de votre imprimante 3D. Nous vous encourageons à concevoir et à construire le vôtre, mais voici quelques options comme point de départ :

- [Corps régulier](cad/regular_body/README.md) : C'est le corps standard que nous avons conçu ; il nécessite une plaque de construction d'au moins 240mmx150mm.
- [Corps mince](cad/slim_body/README.md) : Comme beaucoup d'imprimantes 3D courantes ont un volume de construction plus petit, nous avons également conçu une version plus petite sans pare-chocs qui tient sur une plaque de construction de 220mmx220mm à 45 degrés.
- [Corps collable](cad/glue_body/README.md) : Pour l'impression sur des imprimantes 3D avec des volumes de construction encore plus petits, il existe également un corps modulaire conçu par @sloretz avec plusieurs pièces à coller ensemble ; il tient sur une plaque de construction de 150mmx140mm.
- [Corps bloc](cad/block_body/README.md) : Ce corps conçu par @Christos-Ps propose plusieurs variantes avec des options pour plus d'espace à l'intérieur de la coque et un dessus compatible avec les Lego tout en conservant une petite empreinte nécessitant seulement 221mmx150mm pour l'impression.

#### Support de téléphone

De plus, vous devrez imprimer un support de téléphone à fixer au corps du robot.

- phone_mount_bottom ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
- phone_mount_top ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

#### Nettoyage

Avant de continuer avec la construction, vous devrez peut-être nettoyer l'impression 3D.
<p float="left">
  <img src="../../docs/images/clean_3d_print_1.jpg" width="32%" />
  <img src="../../docs/images/clean_3d_print_2.jpg" width="32%" /> 
  <img src="../../docs/images/clean_3d_print_3.jpg" width="32%" />
</p>

### Alternatives

Si vous n'avez pas accès à une imprimante 3D, il existe plusieurs kits de voiture robot Arduino disponibles que vous pouvez utiliser comme point de départ. Ces kits comprennent un châssis, des moteurs et des accessoires. Nous recommandons d'obtenir un kit de base, car vous n'aurez pas besoin de beaucoup d'électronique et de capteurs des kits plus chers. Voici quelques options :

- Kit de châssis de voiture robot intelligente Perseids DIY ([EU](https://www.amazon.de/dp/B07DNXBNHY), [US](https://www.amazon.com/dp/B07DNXBFQN))
- Kit de châssis de voiture robot intelligente en métal 4WD SZDoit ([US](https://www.amazon.com/dp/B083K4RKBP), [AE](https://www.aliexpress.com/item/33048227237.html))
- Kit de voiture robot Joy-it 01 ([EU](https://www.amazon.de/dp/B073ZGJF28))
- Kit de châssis de voiture robot intelligente 4WD ([AE](https://www.aliexpress.com/item/4001238626191.html))

Vous aurez également besoin d'un support de téléphone. Voici quelques options :

- Support de téléphone ([EU](https://www.amazon.de/dp/B06XDYJNSR), [US](https://www.amazon.com/dp/B09CY8MC2R))

Vous pouvez également faire preuve de créativité et construire votre propre châssis OpenBot et support de téléphone en utilisant un matériau de votre choix (par exemple, bois, carton, polystyrène, etc.). Si vous le faites, veuillez poster des photos sur le [canal Slack](https://github.com/ob-f/OpenBot#contact) pour que les autres puissent admirer votre créativité. Voici un exemple par [@custom-build-robots](https://custom-build-robots.com/roboter/openbot-dein-smartphone-steuert-ein-roboter-auto-chassis-bauen/13636) :

<p float="left">
  <img src="../../docs/images/chassis_cardboard_1.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_2.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_3.jpg" width="32%" />
</p>

## Assemblage

Il existe deux options différentes pour l'assemblage du robot, DIY et PCB. L'approche DIY repose sur le populaire pilote de moteur L298N et est recommandée pour les amateurs ayant une certaine expérience en électronique. Elle nécessite une bonne quantité de câblage, surtout si tous les capteurs et LED sont installés. Cependant, tous les composants sont facilement disponibles dans la plupart des pays et, en particulier pour des constructions uniques ou juste pour essayer le projet, l'option DIY est recommandée. Afin de réduire le câblage et de faciliter l'assemblage, nous avons également développé un [PCB personnalisé](pcb). Cela est recommandé si vous souhaitez une construction plus propre ou si vous souhaitez construire plusieurs OpenBots.

### Liste des matériaux

Notre corps de robot repose sur de l'électronique de loisir facilement disponible. Nous fournissons des liens pour l'Allemagne (EU) et les États-Unis (US) avec une livraison rapide. Si vous avez la patience d'attendre un peu plus longtemps, vous pouvez également obtenir les composants beaucoup moins chers sur AliExpress (AE). Vous aurez besoin des composants suivants.

#### Composants requis

- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 4x moteurs TT avec pneus ([EU](https://www.conrad.de/de/p/joy-it-com-motor01-getriebemotor-gelb-schwarz-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspbe-1573543.html), [US](https://www.amazon.com/dp/B081YQM55P), [AE](https://www.aliexpress.com/item/4000126948489.html))
- 3x batterie 18650 ([EU](https://www.conrad.de/de/p/conrad-energy-18650-usb-spezial-akku-18650-li-ion-3-7-v-1400-mah-1525536.html), [US](https://www.amazon.com/dp/B083K4XSKG), [AE](https://www.aliexpress.com/item/32352434845.html))
- 1x support de batterie 18650 ([EU](https://www.amazon.de/dp/B075V25QJ9), [US](https://www.amazon.com/dp/B07DWQYD7H), [AE](https://www.aliexpress.com/item/33037738446.html))
- 1x câble USB OTG ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x ressort ou élastique ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 16x vis M3x25 ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 16x écrou M3 ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- 6x vis M3x5 ([EU](https://www.amazon.de/dp/B01HBRG3W8), [US](https://www.amazon.com/dp/B07MBHMLL2), [AE](https://www.aliexpress.com/item/32892594230.html))
- Câbles Dupont ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Composants optionnels

- 2 x capteur de vitesse ([EU](https://www.conrad.de/de/p/joy-it-sen-speed-erweiterungsmodul-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspberry-pi-pc-1646891.html), [US](https://www.amazon.com/dp/B081W2TY6Q), [AE](https://www.aliexpress.com/i/32850602744.html))
- 1x capteur ultrasonique ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 1x interrupteur marche/arrêt ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 2x LED orange 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 1x écran OLED ([EU](https://www.amazon.de/dp/B079H2C7WH), [US](https://www.amazon.com/dp/B085NHM5TC), [AE](https://www.aliexpress.com/item/4001268387467.html))

#### Composants DIY (Option 1)

- 1x pilote de moteur L298N ([EU](https://www.conrad.de/de/p/joy-it-motormodul-2-u-4-phasen-6-bis-12v-1573541.html), [US](https://www.amazon.com/dp/B085XSLKFQ), [AE](https://www.aliexpress.com/item/32994608743.html))
- (Optionnel) Résistances (2x 150<span>&#8486;</span> pour les LED et une 20 k<span>&#8486;</span> et 10k<span>&#8486;</span> pour le diviseur de tension)
- (Combo) 4x moteurs TT & pneus + 2x L298N + câbles Dupont ([US](https://www.amazon.com/dp/B07ZT619TD))
- (Combo) 4x moteurs TT & pneus + fils + vis ([US](https://www.amazon.com/dp/B07DRGTCTP))

#### Composants PCB (Option 2)

- 1x [PCB personnalisé](pcb)
- 5x câble Micro JST PH 2.0 ([EU](https://www.amazon.de/gp/product/B07449V33P), [US](https://www.amazon.com/dp/B09JZC28DP), [AE](https://www.aliexpress.com/item/32963304134.html))

### Instructions de construction

**Astuce :** Cliquez sur les images pour les ouvrir en pleine résolution dans un nouvel onglet.

#### Option 1 : DIY

<p float="left">
  <img src="../../docs/images/diy_parts.jpg" height="300" />
  <img src="../../docs/images/wiring_diagram.png" height="300" /> 
</p>

**Astuce :** Pour faciliter tout le câblage, vous pouvez construire un petit distributeur de puissance pour les connexions 5V et GND en soudant un connecteur mâle 6x2 sur une plaque de prototypage. Ensuite, connectez le distributeur de puissance avec le 5V / GND du pilote de moteur.

1. Soudez les fils aux moteurs et ajoutez les disques encodeurs aux deux moteurs avant si vous avez l'intention d'utiliser les capteurs de vitesse.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. Insérez les fils positifs et négatifs des deux moteurs gauches dans OUT1 (+) et OUT2 (-) de la carte L298N. Insérez les fils positifs et négatifs des deux moteurs droits dans OUT4 (+) et OUT3 (-) de la carte L298N.
3. Montez les moteurs avec huit vis et écrous M3x25.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
4. Montez la carte L298N avec quatre vis M3x5.
5. (Optionnel) Installez le capteur ultrasonique et remplacez le connecteur coudé par un connecteur droit (ou pliez soigneusement les broches).
    <p float="left">
      <img src="../../docs/images/sonar_front.jpg" width="32%" />
      <img src="../../docs/images/sonar_back.jpg" width="32%" /> 
      <img src="../../docs/images/sonar_bend_pins.jpg" width="32%" />
    </p>
6. (Optionnel) Installez les LED orange pour les signaux indicateurs.
    <p float="left">
      <img src="../../docs/images/led_insert.jpg" width="32%" />
      <img src="../../docs/images/led_left.jpg" width="32%" /> 
      <img src="../../docs/images/led_right.jpg" width="32%" />
    </p>
7. Montez la base du support de téléphone sur la plaque supérieure à l'aide de deux vis et écrous M3x25.
    <p float="left">
      <img src="../../docs/images/install_camera_mount_1.jpg" width="32%" />
      <img src="../../docs/images/install_camera_mount_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_camera_mount_3.jpg" width="32%" />
    </p>
8. Insérez le haut du support de téléphone et installez le ressort ou l'élastique.
    <p float="left">
      <img src="../../docs/images/install_spring_1.jpg" width="32%" />
      <img src="../../docs/images/install_spring_2.jpg" width="32%" /> 
    </p>
9. Remplacez le connecteur coudé par un connecteur droit (ou pliez soigneusement les broches) puis montez les capteurs de vitesse avec une vis M3x5 chacun.
    <p float="left">
      <img src="../../docs/images/install_speed_sensor_1.jpg" width="32%" />
      <img src="../../docs/images/install_speed_sensor_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_speed_sensor_3.jpg" width="32%" />
    </p>
10. Installez le boîtier de la batterie (par exemple, avec du velcro).
    <p float="left">
      <img src="../../docs/images/install_battery_1.jpg" width="32%" />
      <img src="../../docs/images/install_battery_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_battery_3.jpg" width="32%" />
    </p>
11. (Optionnel) Insérez l'interrupteur marche/arrêt et placez-le dans le circuit.
    1. Poussez l'interrupteur dans l'ouverture appropriée jusqu'à entendre un clic.
    2. Soudez les fils rouges (12V) du boîtier de la batterie et du câble d'alimentation chacun à une des broches de l'interrupteur. Connectez les fils noirs (GND) et couvrez la connexion avec de la gaine thermorétractable.
    3. Fixez les câbles avec du ruban adhésif.
    <p float="left">
      <img src="../../docs/images/install_switch_1.jpg" width="32%" />
      <img src="../../docs/images/install_switch_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_switch_3.jpg" width="32%" />
    </p>
12. (Optionnel) Fixez l'écran OLED.
13. Connectez les entrées PWM de la carte L298N aux broches D5, D6, D9 et D10 de l'Arduino.
14. Connectez les capteurs de vitesse et le capteur ultrasonique à 5V et GND.
15. Connectez la broche D0 des capteurs de vitesse aux broches D2 (gauche) et D3 (droite) de l'Arduino.
16. Connectez les broches Echo et Trigger du capteur ultrasonique aux broches D11 et D12 de l'Arduino respectivement.
17. (Optionnel) Connectez les LED aux broches D4 (gauche) et D7 (droite) de l'Arduino et à GND. Nous recommandons d'ajouter une résistance de 150 Ohms en série pour limiter la consommation de courant.
18. (Optionnel) Connectez le diviseur de tension à la broche A7 de l'Arduino. Il est utilisé pour mesurer la tension de la batterie.
19. (Optionnel) Connectez l'écran OLED (puce SSD1306) via le bus I2C à l'Arduino Nano.
    1. Connectez les broches VIN et GND de l'écran à 5V et GND.
    2. Connectez la broche SCL de l'écran à la broche A5.
    3. Connectez la broche SDA de l'écran à la broche A4.
20. Connectez les câbles d'alimentation à +12V et GND de la carte L298N.
21. Connectez le câble USB à l'Arduino et faites-le passer par le couvercle supérieur.
22. Insérez six écrous M3 dans la plaque inférieure et montez le couvercle supérieur avec six vis M3x25.
23. Installez les roues.

#### Option 2 : PCB personnalisé

1. Soudez des fils avec des connecteurs Micro JST PH 2.0 aux moteurs et ajoutez les disques encodeurs aux deux moteurs avant si vous avez l'intention d'utiliser les capteurs de vitesse.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. Montez les moteurs avec huit vis et écrous M3x25.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
3. Connectez les deux moteurs gauches à M3 et M4 et les deux moteurs droits à M1 et M2.
    <p float="left">
      <img src="../../docs/images/connect_motors_pcb.jpg" width="32%" />
    </p>
4. Montez le PCB avec quatre vis M3x5 et les moteurs avec huit vis et écrous M3x25.
    <p float="left">
      <img src="../../docs/images/attach_pcb.jpg" width="32%" />
      <img src="../../docs/images/chassis_motors_pcb.jpg" width="32%" />
    </p>
5. Suivez les étapes 5-12 de l'option DIY.
6. Connectez le capteur ultrasonique (VCC/+, Trig, Echo, GND/-) à l'en-tête 4 broches étiqueté *SONAR* sur le PCB.
    <p float="left">
      <img src="../../docs/images/connect_sonar_sensor.jpg" width="32%" />
    </p>
7. Connectez les signaux indicateurs gauche et droit (LED orange) aux en-têtes 2 broches étiquetés *SIGNAL_L* et *SIGNAL_R* sur le PCB. La patte la plus longue est + et la plus courte -.
8. Connectez les capteurs de vitesse gauche et droit (VCC/+, GND/-, D0) aux en-têtes 3 broches étiquetés *SPEED_L* et *SPEED_R*.
9. (Optionnel) Connectez l'écran OLED (puce SSD1306) à l'en-tête IO2 sur le PCB.
    1. Connectez les broches VIN et GND de l'écran à 5V et GND.
    2. Connectez la broche SCL de l'écran à la broche A5.
    3. Connectez la broche SDA de l'écran à la broche A4.
10. Connectez les câbles d'alimentation à Vin (connecteur Micro JST PH 2.0) du PCB.
11. Suivez les étapes 21-23 de l'option DIY.

## Ensuite

Flashez le [Firmware Arduino](../../firmware/README.md)
