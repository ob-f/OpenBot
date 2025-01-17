# OpenBot : Carrosserie de camion RC

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Nous avons conçu une carrosserie de robot pour des camions/buggies RC à l'échelle 1:16 largement disponibles (comme [celui-ci](https://www.amazon.de/dp/B00M3J7DJW) sur Amazon).

![RC-Truck-Banner](/docs/images/rc-truck-banner.jpg)

Nous avons également une [carrosserie](/body/) générique conçue pour un robot à roues simple qui repose sur du matériel de loisir à faible coût et facilement disponible. Les instructions de montage pour l'OpenBot classique se trouvent [ici](/body/README.md).

## Châssis

Le châssis de l'OpenBot RC-Truck se compose de deux composants principaux : (a) un camion RC à l'échelle 1:16 de votre choix et (b) un certain nombre de pièces conçues sur mesure que nous fournissons et qui peuvent être imprimées en 3D.

### Camion RC à l'échelle 1:16

Pour construire votre propre OpenBot RC-Truck, vous aurez besoin d'un camion/buggy RC à l'échelle 1:16. Nous fournissons des liens Amazon vers des camions RC compatibles pour l'Allemagne ([EU](https://www.amazon.de/dp/B00M3J7DJW)), ([EU](https://www.amazon.de/dp/B088FGVYNW)), et les États-Unis ([US](https://www.amazon.com/gp/product/B09C8XMPQ9)) avec une livraison rapide. Un certain nombre de camions jouets similaires à l'échelle 1:16 peuvent également être trouvés chez d'autres détaillants en ligne tels qu'ebay, Alibaba ou AliExpress, souvent à prix réduit mais avec une vitesse de livraison lente.

Quel que soit le détaillant et la version du camion RC que vous choisissez pour votre construction, assurez-vous qu'il s'agit bien d'un camion RC à l'échelle 1:16. C'est important car les pièces imprimées en 3D que nous fournissons sont actuellement conçues pour s'adapter uniquement aux camions à l'échelle 1:16 avec quelques ajustements mineurs (plus d'informations à ce sujet plus tard). Voici quelques exemples de camions/buggies RC compatibles à l'échelle 1:16.

<p float="left">
  <a href="https://www.amazon.de/dp/B00M3J7DJW" target="_blank"> <img src="/docs/images/rc_toy_1.jpg" width="34%" /> &nbsp
  </a>
  <a href="https://www.amazon.com/gp/product/B09C8XMPQ9" target="_blank"> <img src="/docs/images/rc_toy_2.jpg" width="27%" /> &nbsp &nbsp &nbsp &nbsp
  </a>
  <a href="https://www.amazon.de/dp/B088FGVYNW" target="_blank"> <img src="/docs/images/rc_toy_3.jpg" width="27%" />
  </a>
</p>

### Impression 3D

Vous devrez imprimer les pièces suivantes pour construire votre OpenBot RC-Truck.

1) ```main_frame``` ([STL](cad/rc_truck_body/main_frame.stl), [STEP](cad/rc_truck_body/main_frame.step))
2) ```side_cover``` \[x2\] ([STL](cad/rc_truck_body/side_cover.stl), [STEP](cad/rc_truck_body/side_cover.step))
3) ```phone_mount_bottom``` ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
4) ```phone_mount_top``` ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

Notez que \[xN\] indique le nombre de copies (c'est-à-dire N) que vous devez imprimer d'une pièce particulière (le cas échéant).

Les pièces suivantes sont optionnelles (mais recommandées) pour rendre votre OpenBot RC-Truck plus compact et esthétiquement agréable.

5) ```camera_elevator``` ([STL](cad/rc_truck_body/camera_elevator.stl), [STEP](cad/rc_truck_body/camera_elevator.step))
6) ```electronics_cover``` \[x2\] ([STL](cad/rc_truck_body/electronics_cover.stl), [STEP](cad/rc_truck_body/electronics_cover.step))
7) ```spacer``` \[x4\] ([STL](cad/rc_truck_body/spacer.stl), [STEP](cad/rc_truck_body/spacer.step))
8) ```front_light_spacer``` \[x2\] ([STL](cad/rc_truck_body/front_light_spacer.stl), [STEP](cad/rc_truck_body/front_light_spacer.step))

Pour toutes les pièces ci-dessus, votre plateau d'impression doit mesurer au moins 260mmx220mm, ce qui est la taille d'impression du ```main_frame```.

Comme beaucoup d'imprimantes 3D courantes ont un volume de construction plus petit (généralement 220mmx220mm), il existe deux autres options qui peuvent fonctionner.
La première option consiste à imprimer le ```main_frame``` à 45 degrés avec du matériel de support supplémentaire.
La deuxième option nécessite de modifier la pièce originale ```main_frame```. Nous recommandons d'utiliser [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) pour de telles modifications CAO (Fusion 360 dispose d'une licence académique gratuite d'un an). 
Pour cette option, nous mettons à disposition son fichier [STEP](/body/cad/rc_truck_body/main_frame.step), que vous pouvez découper en deux/trois pièces plus petites. 
Les sous-pièces résultantes s'adapteront alors à un plateau de construction standard (c'est-à-dire 220mmx220mm) et pourront être assemblées après impression. 
À l'avenir, nous pourrions également publier une version modulaire du ```main_frame``` ici. Toutes les autres pièces nécessitent un plateau de construction minimum de 220mmx60mm.

Sur une Ultimaker S5, nous avons obtenu de bons résultats avec les paramètres suivants :

- hauteur de couche : 0,2mm
- épaisseur de paroi : 1,5mm
- densité de remplissage : 20%
- motif de remplissage : grille
- vitesse d'impression : 80 mm/s
- pas de support

Nous avons pu imprimer le châssis avec du PLA, du CPE et de l'ABS. D'après notre expérience, l'impression n'a pas été très affectée par les paramètres d'impression. Cependant, si vous avez la patience, imprimer plus lentement et avec une hauteur de couche plus petite améliorera l'impression. Ajouter une structure de support peut également améliorer l'impression, mais nécessitera un travail supplémentaire pour la retirer par la suite.

Avant de procéder à l'assemblage, vous devrez peut-être nettoyer l'impression 3D. Cependant, en utilisant les paramètres ci-dessus, nous n'avons eu besoin d'aucun limage ou nettoyage pendant notre processus de construction. Si possible, nous recommandons d'utiliser une combinaison de deux couleurs différentes (par exemple vert/noir ou rouge/noir) pour imprimer différentes pièces du même OpenBot RC-Truck comme illustré ci-dessous.

**Astuce :** Cliquez sur les images pour les ouvrir en pleine résolution dans un nouvel onglet.

<p float="left">
  <img src="/docs/images/3d_print_rc_1.png" width="32%" />
  <img src="/docs/images/3d_print_rc_2.png" width="32%" /> 
  <img src="/docs/images/3d_print_rc_3.png" width="32%" />
</p>

## Assemblage

Bien qu'il soit possible de construire votre OpenBot RC-Truck avec une approche DIY similaire à l'OpenBot classique (voir les composants et instructions de construction DIY pour OpenBot [ici](/body/README.md)), nous recommandons d'utiliser le [PCB personnalisé](/body/pcb) d'OpenBot pour construire et assembler l'OpenBot RC-Truck. Cette option est recommandée si vous souhaitez une construction plus propre ou si vous souhaitez construire plusieurs OpenBot RC-Trucks. Un avantage supplémentaire de l'utilisation de notre [PCB personnalisé](/body/pcb) est que vous pouvez utiliser les mêmes composants pour construire et passer d'un corps OpenBot à un autre.

### Liste des matériaux

L'OpenBot RC-Truck repose principalement sur des composants électroniques de loisir facilement disponibles. Nous fournissons des liens Amazon pour l'Allemagne (EU) et les États-Unis (US) avec une livraison rapide. Si vous avez la patience d'attendre un peu plus longtemps, vous pouvez également obtenir les composants beaucoup moins chers sur AliExpress (AE). Vous aurez besoin des composants suivants.

#### Composants requis

- 1x camion/buggy RC ([EU](https://www.amazon.de/dp/B00M3J7DJW), [EU](https://www.amazon.de/dp/B088FGVYNW), [US](https://www.amazon.com/gp/product/B09C8XMPQ9))
- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 1x [PCB personnalisé](/body/pcb) OpenBot
- 1x câble USB OTG ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x ressort ou élastique ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 6x vis M3x25 ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 6x écrou M3 ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- Câbles Dupont ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Composants optionnels

- 1x Capteur ultrasonique ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 2x Interrupteur On/Off ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 4x LED orange 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 4x LED rouge 5mm ([EU](https://www.amazon.de/dp/B083HN3CLY), [US](https://www.amazon.com/dp/B077X95F7C), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 2x Lampes LED blanches ([EU](https://www.amazon.de/-/en/gp/product/B06XTQSZDX), [US](https://www.amazon.com/gp/product/B01N2UPAD8), [AE](https://de.aliexpress.com/item/1005002991235830.html))
- Résistance variable pour LEDs ([EU](https://www.amazon.de/gp/product/B081TXJJGV), [US](https://www.amazon.com/dp/B0711MB4TL), [AE](https://de.aliexpress.com/item/1005003610664176.html))

### Instructions de montage

**Astuce :** Cliquez sur les images pour les ouvrir en pleine résolution dans un nouvel onglet.

1. Démontez le camion jouet RC. Retirez son couvercle supérieur et dévissez les quatre broches de montage de la base comme indiqué sur les figures ci-dessous. Gardez les quatre broches de montage et leurs vis respectives en sécurité, car vous les utiliserez pour monter le ```main_frame``` sur le corps du camion RC après avoir terminé tout le câblage. Tous les camions jouets RC compatibles sont équipés de deux moteurs : un pour l'accélérateur et l'autre pour la direction, un contrôleur de vitesse (avec un UBEC intégré de 5-7V) pour le moteur d'accélérateur, et une batterie LiPo 2S 7.4V. Démontez et retirez la batterie de la base du camion et rechargez-la avec le chargeur fourni avec le camion. Exposez/desserrez les connecteurs de fils pour les deux moteurs ainsi que la sortie UBEC du contrôleur de vitesse. Dans notre cas, la sortie UBEC était de 6V.
    <p float="left">
      <img src="/docs/images/rc_truck_disassembly_1.JPG" width="32%" />
      <img src="/docs/images/rc_truck_disassembly_2.JPG" width="32%" /> 
      <img src="/docs/images/rc_truck_disassembly_3.JPG" width="32%" />
    </p>
2. Notez que les deux dimensions d1 et d2 (comme indiqué ci-dessous) sur le ```main_frame``` dépendent du modèle de camion jouet RC utilisé. Nous avons conçu notre pièce ```main_frame``` pour [ce modèle](https://www.amazon.de/dp/B00M3J7DJW) de camion jouet RC. En fonction du camion à l'échelle 1:16 que vous utilisez, vous devrez peut-être ajuster légèrement ces dimensions en utilisant le fichier ```main_frame``` [STEP](/body/cad/rc_truck_body/main_frame.step). Nous recommandons d'utiliser [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) pour de telles modifications CAO (Fusion 360 dispose d'une licence académique gratuite d'un an). Notez également que le petit coin/triangle sur le ```main_frame``` représente la direction avant.
    <p float="left">
      <img src="/docs/images/main-frame-dimensions.png" width="32%" />
      <img src="/docs/images/main-frame-direction.png" width="32%" />
    </p>   
3. (Optionnel) Installez l'interrupteur ON/OFF pour alimenter le robot. Vous pouvez simplement le faire en coupant le fil positif qui va du contrôleur de vitesse à la batterie et en soudant l'interrupteur entre les deux parties coupées de ce fil. Veuillez vous assurer que les connecteurs de l'interrupteur sont isolés via un tube thermorétractable ou du ruban électrique et que le câble d'alimentation est suffisamment long pour que l'interrupteur puisse passer par l'ouverture rectangulaire à l'arrière du ```main_frame``` après l'assemblage (voir la figure ci-dessous).
    <p float="left">
      <img src="/docs/images/main-frame-switch.png" width="32%" />
      <img src="/docs/images/switch-power.jpg" width="32%" />
    </p>
4. (Optionnel) Installez le capteur ultrasonique à travers la grille avant du ```main_frame```. Vous pouvez utiliser de la colle chaude pour le maintenir en place si nécessaire. Poussez doucement le connecteur en position droite avant de le mettre en place. Cela facilitera l'accès au connecteur après l'assemblage. Faites passer les câbles dupont du connecteur ultrasonique jusqu'à l'ouverture rectangulaire à l'arrière du ```main_frame```.
    <p float="left">
      <img src="/docs/images/install-ultrasonic-1.png" width="32%" />
      <img src="/docs/images/ultrasonic-sensor.jpg" width="32%" />
      <img src="/docs/images/install-ultrasonic-2.png" width="32%" />
    </p>
5. (Optionnel) Installez les LED orange pour les signaux de clignotant à l'avant et à l'arrière du ```main_frame```. Vous pouvez utiliser de la colle chaude pour les maintenir en place si nécessaire. Pour chaque côté, c'est-à-dire gauche et droite, vous devez connecter les LED avant et arrière en parallèle. Pour ce faire, connectez simplement leurs bornes positives et négatives respectivement. Comme pour le câble du capteur ultrasonique, faites passer les câbles dupont positifs et négatifs des signaux de clignotant gauche et droit jusqu'à l'ouverture rectangulaire à l'arrière du ```main_frame``` où ils se connecteront à leurs broches de signal de clignotant respectives (à la fois +ve et -ve) sur le PCB.
    <p float="left">
      <img src="/docs/images/insert-leds-orange-1.png" width="32%" />
      <img src="/docs/images/orange-led.jpg" width="32%" />
      <img src="/docs/images/insert-leds-orange-2.png" width="32%" />
    </p>
**Astuce :** Pour éviter l'encombrement et les erreurs de mise à la terre potentielles lors du câblage, il est recommandé de former une boucle de masse unifiée pour les bornes négatives de toutes les LED. Cela signifie simplement faire passer un fil sous le ```main_frame``` qui connecte toutes les bornes négatives des LED. Cette boucle de masse peut ensuite être connectée à la broche de masse de l'Arduino Nano à l'aide d'un seul câble dupont, qui est acheminé vers l'ouverture rectangulaire à l'arrière du ```main_frame```.

6. (Optionnel) Installez les lampes LED avant. Vous pouvez utiliser de la colle chaude pour maintenir la base en place et visser la lampe dans sa base respective à travers l'ouverture avant de chaque côté. Connectez les deux lampes LED avant en parallèle en connectant leurs bornes positives et négatives respectivement. Comme ces lampes fonctionnent en 6V, vous pouvez les connecter directement à la sortie UBEC par leurs bornes positives. Connectez les bornes négatives à la boucle de masse (voir l'astuce ci-dessus). La résistance interne de ces LED est assez élevée, il n'est donc pas nécessaire d'ajouter une résistance externe. Après avoir installé les lampes LED, insérez et collez à chaud les deux ```front_light_spacers``` de chaque côté pour verrouiller les LED en place.
    <p float="left">
      <img src="/docs/images/insert-lamps-1.png" width="32%" />
      <img src="/docs/images/led-lamp-wiring.jpg" width="32%" />
      <img src="/docs/images/add_front_light_spacer.png" width="32%" />
    </p>
7. (Optionnel) Installez les LED rouges pour les feux arrière. Vous pouvez utiliser de la colle chaude pour les maintenir en place si nécessaire. Connectez les quatre LED rouges en parallèle ; c'est-à-dire connectez leurs bornes positives et négatives respectivement. Les bornes négatives iront à la masse, tandis que les bornes positives seront collectivement connectées à la sortie UBEC via un diviseur de tension approprié (voir l'étape suivante pour les détails sur la construction du diviseur de tension).
    <p float="left">
      <img src="/docs/images/insert-leds-red.png" width="32%" />
      <img src="/docs/images/red-led.jpg" width="32%" />
    </p>
8. (Optionnel) Installez le diviseur de tension pour les LED rouges arrière. La plupart des LED de couleur (par exemple, rouge, orange, jaune, etc.) fonctionnent en 2-3V et non à la tension traditionnelle de 5V, qui est la tension de fonctionnement normale de l'Arduino Nano. Par conséquent, un diviseur de tension est nécessaire pour faire fonctionner ces LED en toute sécurité. Pour les signaux de clignotant, nous avons déjà un diviseur de tension intégré dans notre PCB personnalisé. Vous n'avez donc rien à faire pour utiliser les LED de signal de clignotant (c'est-à-dire orange). Cependant, si vous choisissez d'ajouter des feux arrière, c'est-à-dire des LED rouges, un diviseur de tension externe est nécessaire pour elles. Nous recommandons d'utiliser une résistance variable de 10kΩ ou plus pour fabriquer votre diviseur de tension. En fonction de la tension de sortie de votre UBEC (6V dans notre cas), vous devez configurer un diviseur de tension avec une sortie de 2-3V. Cela peut être fait en appliquant la sortie UBEC aux extrémités externes de la résistance et en tournant la vis sur son dessus et en surveillant la tension de sortie à l'aide d'un multimètre numérique entre la masse et la borne centrale (voir figure ci-dessous). Une fois la tension de sortie de la résistance variable, c'est-à-dire le diviseur de tension, réglée à la plage appropriée de 2-3V, verrouillez sa vis en place à l'aide de colle chaude et fixez sa position sous le ```main_frame``` à un endroit pratique.
    <p float="left">
      <img src="/docs/images/variable-resistor.jpg" width="32%" />
      <img src="/docs/images/voltage-divider-animation.png" width="32%" />
    </p>
9. (Optionnel) Vous pouvez également utiliser un ou deux interrupteurs ON/OFF séparés pour allumer et éteindre les LED avant et arrière. Veuillez suivre les instructions de l'étape 3 pour installer un interrupteur (ou plusieurs interrupteurs) à cet effet.
10. Vous avez maintenant presque terminé le câblage du robot. À ce stade, prenez le temps de vous assurer que tous les fils et connexions sous le ```main_frame``` sont corrects et bien isolés à l'aide de tubes thermorétractables ou de ruban électrique. Utilisez de la colle chaude pour maintenir les fils lâches en place afin qu'ils ne soient pas en contact avec les roues ou les pièces mobiles du robot après l'assemblage. Assurez-vous que tous les câbles des moteurs, du contrôleur de vitesse UBEC, des LED et du capteur ultrasonique peuvent sortir librement de l'ouverture rectangulaire à l'arrière du ```main_frame```.
11. Montez le ```phone_mount_bottom``` sur le ```main_frame``` à l'aide de deux vis et écrous M3x25. Optionnellement, vous pouvez insérer un ou plusieurs ```camera_elevators``` entre les deux si vous souhaitez ajuster la hauteur verticale de votre support de téléphone. Si vous utilisez un ```camera_elevator```, vous aurez besoin de vis M3x35 ou plus longues pour monter le support de téléphone sur le ```main_frame```.
    <p float="left">
      <img src="/docs/images/add_phone_mount_bottom.png" width="32%" />
      <img src="/docs/images/add_phone_mount_bottom_elevator.png" width="32%" /> 
    </p>
10. Insérez le ```phone_mount_top``` et installez le ressort ou l'élastique.
    <p float="left">
      <img src="/docs/images/add_phone_mount_top.png" width="32%" />
    </p>
11. Insérez les deux ```side_covers``` dans leurs emplacements respectifs.
    <p float="left">
      <img src="/docs/images/add_side_covers.png" width="32%" />
      <img src="/docs/images/add_side_covers_2.png" width="32%" />
    </p>    
12. Montez le ```main_frame``` sur le corps du camion RC à l'aide des quatre broches de montage et de leurs vis respectives. Assurez-vous que tous les connecteurs de câbles et l'interrupteur d'alimentation du robot sont accessibles par l'ouverture rectangulaire à l'arrière du ```main_frame``` pour les connexions PCB. Sortez le connecteur de la batterie par l'ouverture triangulaire à l'avant du ```main_frame```.
    <p float="left">
      <img src="/docs/images/add_main_frame_1.JPG" width="32%" />
      <img src="/docs/images/add_main_frame_2.png" width="32%" />
      <img src="/docs/images/add_main_frame_3.JPG" width="32%" />
    </p>
12. Montez le PCB avec quatre vis et écrous M3x25 avec quatre ```spacers``` entre eux à l'arrière du ```main_frame```. Montez l'Arduino Nano sur le PCB et attachez le câble USB OTG au port USB de l'Arduino Nano.
    <p float="left">
      <img src="/docs/images/pcb_assembly.JPG" width="32%" />
    </p>
13. Connectez les câbles du capteur ultrasonique au connecteur marqué "sonar" sur le PCB. Assurez-vous que la polarité +ve/-ve et les lignes de données sont correctement appariées entre le capteur et les ports du PCB.
14. Connectez les câbles des LED de clignotant gauche et droit à leurs connecteurs de signal de clignotant respectifs sur le PCB. Assurez-vous de la polarité correcte des bornes +ve et -ve des LED.
15. Connectez la sortie UBEC (+6V) à la broche Vin de l'Arduino Nano (optionnel, l'Arduino peut également être alimenté par le téléphone), et la masse UBEC à la broche GND de l'Arduino (à côté de Vin).
16. Connectez la sortie UBEC (+6V) aux bornes +ve du servo de direction, des lampes LED avant et des LED rouges arrière via le diviseur de tension.
17. Connectez le câble de masse du servo de direction à la broche GND de l'Arduino également.
18. Connectez le câble PWM du servo d'accélérateur (du contrôleur de vitesse) à la broche A0 de l'Arduino Nano ou du breakout PCB.
19. Connectez le câble PWM du servo de direction à la broche A1 de l'Arduino Nano ou du breakout PCB.
**Astuce :** Si vous avez créé une boucle de masse unifiée pour le câblage des LED, connectez le câble de la boucle de masse à l'une des broches GND de l'Arduino également. L'Arduino Nano dispose de trois broches GND disponibles. Si vous n'avez pas construit de boucle de masse, assurez-vous que toutes les LED, le servo de direction, les capteurs, l'Arduino Nano et l'UBEC du contrôleur de vitesse partagent la même masse avec un câblage et des connexions appropriés.
21. Connectez le pack de batteries à l'avant et maintenez-le en place à l'aide de velcro ou de ruban de montage. Avoir la batterie à l'avant la rend facilement accessible pour la recharge. Cette position aide également à équilibrer le poids du robot lorsqu'un smartphone est monté sur le dessus.
22. Mettez en place les ```electronics_covers``` avant et arrière. Sortez le câble USB OTG par l'écart du ```electronics_cover``` arrière pour le connecter à un smartphone Android.
<p float="left">
      <img src="/docs/images/add_covers_1.png" width="32%" />
      <img src="/docs/images/add_covers_2.JPG" width="32%" />
    </p>

## Ensuite

Flashez le [Firmware Arduino](../../firmware/README.md)
