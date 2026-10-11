# Support de téléphone

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <span>Français</span> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Le support de téléphone en deux pièces utilisé par les carrosseries [DIY](../diy/README.fr-FR.md), [RC truck](../rc_truck/README.fr-FR.md) et [Compact](../compact/README.fr-FR.md). Une pince coulissante plaque le téléphone contre un appui ; elle est tendue par un élastique ou un ressort léger. Une version inclinable optionnelle tient le téléphone à 0°, 10°, 20° ou 30° pour que la caméra regarde vers le haut, par exemple vers une personne debout devant le robot.

## Pièces

| Pièce | Qté | Remarques |
|---|---:|---|
| `phone_mount_bottom` ([STL](phone_mount_bottom.stl), [STEP](phone_mount_bottom.step)) | 1 | Base et colonne ; se fixe sur la carrosserie avec deux vis M3 × 25 mm et des écrous |
| `phone_mount_top` ([STL](phone_mount_top.stl), [STEP](phone_mount_top.step)) | 1 | Pince coulissante ; s'utilise avec les deux bases |
| `phone_mount_tilt_base` ([STL](phone_mount_tilt_base.stl), [STEP](phone_mount_tilt_base.step)) | 1 *(optionnel)* | Support inclinable : pied avec deux joues de charnière ; remplace `phone_mount_bottom` |
| `phone_mount_tilt_column` ([STL](phone_mount_tilt_column.stl), [STEP](phone_mount_tilt_column.step)) | 1 *(optionnel)* | Support inclinable : colonne qui pivote entre les joues |

## Impression

Les fichiers sont déjà orientés pour l'impression : imprimez chaque pièce debout telle quelle. Du PLA en couches de 0,2 mm avec 4 parois et 30 % de remplissage convient bien. `phone_mount_tilt_base` n'a pas besoin de supports. `phone_mount_bottom`, `phone_mount_top` et `phone_mount_tilt_column` n'en ont besoin que sous le rebord du téton de l'élastique ; des supports arborescents avec « Supprimer les petits surplombs » désactivé y forment un petit arbre, que vous retirez par l'arrière ouvert de la glissière. Le rebord fait tout le tour du téton, donc l'élastique ne peut pas glisser même détendu.

## Support de téléphone inclinable

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="Charnière du support de téléphone inclinable" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="Support de téléphone inclinable à 0, 10, 20 et 30 degrés" width="64%" />
</p>

La base inclinable utilise les deux mêmes trous de fixation que la base standard et s'adapte aux mêmes logements : le plateau supérieur DIY, le châssis et la rehausse de caméra du RC truck, et le couvercle Compact. Ses joues commencent 5 mm au-dessus du dessous du pied et ne touchent donc pas le bord de ces logements. Le téléphone est 10,5 mm plus haut que sur le support standard. À 30°, le haut de la colonne recule d'environ 22 mm ; vérifiez que rien ne gêne sur votre carrosserie.

En plus des vis de fixation, il faut trois vis à tête cylindrique bombée M3 × 10 mm et quatre inserts filetés M3 (4,6 mm de diamètre extérieur, 5,7 mm de long). Les filetages en laiton tiennent même si vous changez souvent d'angle.

1. Posez à chaud quatre inserts filetés M3 (4,6 mm de diamètre extérieur, 5,7 mm de long) dans la colonne : un à chaque extrémité du trou d'axe à l'arrière, et un dans chacun des deux trous de blocage avant, qui ne sont ouverts que d'un côté.
2. Enfoncez deux écrous M3 dans le pied et fixez-le sur la carrosserie comme la base standard.
3. Placez la colonne entre les joues, les trous de blocage ouverts tournés vers la joue qui a cinq trous. Vissez une vis M3 × 10 mm à travers chaque joue dans les inserts du trou d'axe et serrez jusqu'à ce que la colonne tourne sans jeu. Mettez une goutte de frein-filet moyen (bleu) sur les deux vis d'axe pour qu'elles ne se desserrent pas.
4. Inclinez la colonne vers l'arrière jusqu'à ce qu'un de ses inserts de blocage soit en face d'un trou de la joue. La paire de trous avant donne 0° (en bas) et 20° (en haut) ; la paire arrière donne 10° (en bas) et 30° (en haut). Bloquez-la avec la troisième vis M3 × 10 mm.
5. Montez `phone_mount_top` et l'élastique comme sur le support standard.
