# Corps Blocky avec Espace Supplémentaire et Support Lego

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Ce châssis de robot offre une hauteur supplémentaire pour faciliter l'intégration de tous les composants électroniques et un dessus compatible avec les Lego. Cette version offre les mêmes capacités de solidité structurelle que le [corps régulier](../regular_body/), avec des fonctionnalités supplémentaires telles qu'un espace supplémentaire à l'intérieur de la coque OpenBot, un dessus compatible avec les Lego pour le jeu et l'apprentissage, et une empreinte adaptée aux tailles de lit d'impression plus petites tout en maintenant les pare-chocs.

![Block CAD](../../../../docs/images/block_cad.jpg)

## Pièces

Vous devrez imprimer la partie inférieure et l'une des parties supérieures :

- `block_body_bottom`([STL](block_body_bottom.stl), [STEP](block_body_bottom.step)) : partie inférieure du corps
- `block_body_top`([STL](block_body_top.stl), [STEP](block_body_top.step)) : partie supérieure basique du corps
- `block_body_top_lego`([STL](block_body_top_lego.stl), [STEP](block_body_top_lego.step)) : partie supérieure basique du corps avec surface compatible Lego
- `block_body_top_big`([STL](block_body_top_big.stl), [STEP](block_body_top_big.step)) : grande partie supérieure du corps avec volume supplémentaire pour les composants électroniques
- `block_body_top_lego`([STL](block_body_top_big_lego.stl), [STEP](block_body_top_big_lego.step)) : grande partie supérieure du corps avec surface compatible Lego

Pour les pièces ci-dessus, votre plateau d'impression doit mesurer au moins 221x150mm.

## Paramètres d'Impression

Pour de meilleurs résultats, nous recommandons d'utiliser les paramètres d'impression suivants :

- Hauteur de couche : 0,2mm
- Nombre de parois : 3 (plus de parois pour une meilleure intégrité structurelle des grandes surfaces)
- Couches supérieures : 5
- Couches inférieures : 4
- Remplissage : 25%
- Motif de remplissage : Concentrique (ce réglage semble économiser du temps et du plastique)
- Vitesse d'impression : 50mm/sec
- Générer des supports : Oui
- Motif de support : Concentrique
- Densité du support : 15%
- Activer le bord de support : Oui
- Type d'adhésion au plateau : Aucun

Bonne construction de robot !

![Block Body](../../../../docs/images/block_body.jpg)