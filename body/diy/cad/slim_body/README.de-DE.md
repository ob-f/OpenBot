# Schlankes Chassis

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Einige 3D-Drucker haben ein Bauvolumen, das zu klein ist, um den OpenBot-Körper in voller Größe zu drucken.
Dieser Ordner enthält die schlanke Version des OpenBot-Körpers.
Er kann mit einer Bauplatte von 220mmx220mm gedruckt werden, wenn die Teile um 45 Grad gedreht werden.

![Schlanker Körper](../../../../docs/images/slim_body.jpg)

## Teile

1) `slim_body_bottom` ([STL](slim_body_bottom.stl), [STEP](slim_body_bottom.step))
2) `slim_body_top` ([STL](slim_body_top.stl), [STEP](slim_body_top.step))

Um sicherzustellen, dass es passt, müssen Sie möglicherweise die folgenden Einstellungen anpassen, um die maximale Druckfläche zu erreichen.

- Setzen Sie den *Build Plate Adhesion Type* auf "None" (Brim, Skirt und Raft vergrößern die Gesamtgröße Ihres Drucks)
- Deaktivieren Sie den Prime Blob (im Abschnitt *Build Plate*)
- Deaktivieren Sie den zweiten Extruder (falls Ihr Drucker einen hat)

Wenn Sie ein wenig zusätzlichen Platz haben (223mmx223mm), können Sie auch den `slim_body_top_rim` ([STL](slim_body_top_rim.stl), [STEP](slim_body_top_rim.step)) drucken. Er hat einen etwas größeren Rand, was das Abnehmen des Deckels erleichtert.
