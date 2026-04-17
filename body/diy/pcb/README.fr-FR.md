# PCB Personnalisé

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Le PCB personnalisé agit comme une carte porteuse pour l'Arduino Nano et intègre des pilotes de moteur modernes, le circuit diviseur de tension et des résistances pour les LED. L'Arduino est simplement branché dans l'en-tête de broche et tous les capteurs et LED sont connectés via des câbles Dupont aux connecteurs appropriés.

![PCB_2D](../../../docs/images/pcb_2d_v2.png)
![PCB_3D](../../../docs/images/pcb_3d_v2.png)

Le dernier PCB est [version 2](v2). Voici les changements par rapport à la [version 1](v1) :

- Déplacer le capteur de vitesse droit vers la broche D3 pour activer la fonctionnalité d'interruption
- Ajouter une LED d'alimentation pour la batterie principale
- Mettre à jour certains composants qui sont plus couramment disponibles
- Mettre à jour le diviseur de tension à 20k/10k pour une meilleure précision
- Changer les connecteurs de moteur en version verticale pour un accès plus facile

Si vous avez déjà commandé la [version 1](v1) du PCB ([vue 2D](../../../docs/images/pcb_2d_v1.png), [vue 3D](../../../docs/images/pcb_3d_v1.png)), ne vous inquiétez pas, cela fonctionnera bien. Assurez-vous simplement de définir le bon drapeau dans le firmware.

Le PCB personnalisé implique les étapes suivantes :

1) **Commander le PCB** : Téléchargez les fichiers [Gerber](v2/gerber_v2.zip) et commandez le PCB chez le fournisseur de votre choix. Vous pouvez également commander le PCB directement sur [PCBWay](https://www.pcbway.com/project/shareproject/OpenBot__Turning_Smartphones_into_Robots.html) où nous avons partagé un projet pour OpenBot.
2) **Commander les composants :** Téléchargez la [BOM](v2/BOM_v2.csv) et commandez les composants chez le fournisseur de votre choix, par exemple [LCSC](https://lcsc.com).
3) **Assemblage du PCB :** Vous pouvez soit assembler le PCB vous-même, soit le faire assembler par un fournisseur. Pour l'assemblage automatisé, vous aurez besoin du [Fichier Centroid](v2/centroid_file_v2.csv). Si vous commandez le PCB chez [JLCPCB](https://jlcpcb.com/), vous pouvez utiliser leur service d'assemblage SMT. Vous n'aurez alors qu'à commander et souder les composants traversants vous-même. Nous avons trouvé que c'était l'option la plus pratique, la moins chère et la plus rapide. Dans la [version 2](v2) du PCB, nous avons mis à jour les composants pour nous assurer que tous sont disponibles directement chez [JLCPCB](https://jlcpcb.com/).

Vous pouvez également trouver des fournisseurs qui vous proposeront une solution clé en main couvrant les 3 étapes. Ils fabriqueront le PCB, sourceront les composants et assembleront le PCB. C'est très pratique et aussi pas trop cher. Cependant, les délais de livraison sont souvent très longs (1-3 mois).

Lors de la demande de devis chez [PCBWay](https://www.pcbway.com/orderonline.aspx), vous pouvez sélectionner le service d'assemblage après avoir téléchargé le fichier Gerber.
![Service d'assemblage](../../../docs/images/assembly_service.jpg)
À l'étape suivante, vous devrez télécharger la [BOM](v2/BOM_v2.csv) et le [Fichier Centroid](v2/centroid_file_v2.csv). Votre devis sera alors examiné et mis à jour dans les quelques jours suivants. Vous pourrez ensuite choisir de procéder au paiement après avoir examiné le coût et le délai de livraison.