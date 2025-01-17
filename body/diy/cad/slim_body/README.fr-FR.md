# Slim Body

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Certaines imprimantes 3D ont des volumes de construction trop petits pour imprimer le corps OpenBot en taille réelle.
Ce dossier contient la version mince du corps OpenBot.
Elle peut être imprimée avec une plaque de construction de 220mmx220mm lorsque les pièces sont tournées de 45 degrés.

![Slim Body](../../../../docs/images/slim_body.jpg)

## Pièces

1) `slim_body_bottom` ([STL](slim_body_bottom.stl), [STEP](slim_body_bottom.step))
2) `slim_body_top` ([STL](slim_body_top.stl), [STEP](slim_body_top.step))

Pour que cela rentre, vous devrez peut-être ajuster les paramètres suivants pour obtenir la zone d'impression maximale.

- Réglez *Type d'adhésion de la plaque de construction* sur "Aucun" (Brim, Skirt et Raft augmentent la taille globale de votre impression)
- Désactivez le blob de prime (dans la section *Plaque de construction*)
- Désactivez le deuxième extrudeur (si votre imprimante en a un)

Si vous avez un peu plus d'espace (223mmx223mm), vous pouvez également imprimer le `slim_body_top_rim` ([STL](slim_body_top_rim.stl), [STEP](slim_body_top_rim.step)). Il a un rebord légèrement plus grand, ce qui facilite le retrait du dessus.