# Block Chassis mit zusätzlichem Raum und Lego-Unterstützung

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Dieses Roboterchassis bietet zusätzliche Höhe, um es einfacher zu machen, alle Elektronikteile unterzubringen, und eine lego-kompatible Oberseite. Diese Version bietet die gleiche strukturelle Integrität wie der [normale Körper](../regular_body/), mit zusätzlichen Funktionen wie mehr Platz im OpenBot-Gehäuse, einer lego-kompatiblen Oberseite zum Spielen und Lernen und einer Größe, die für kleinere Druckbettgrößen geeignet ist, während die Stoßfänger beibehalten werden.

![Block CAD](../../../../docs/images/block_cad.jpg)

## Teile

Sie müssen den unteren Teil und einen der oberen Teile drucken:

- `block_body_bottom`([STL](block_body_bottom.stl), [STEP](block_body_bottom.step)): unterer Teil des Körpers
- `block_body_top`([STL](block_body_top.stl), [STEP](block_body_top.step)): grundlegender oberer Teil des Körpers
- `block_body_top_lego`([STL](block_body_top_lego.stl), [STEP](block_body_top_lego.step)): grundlegender oberer Teil des Körpers mit lego-kompatibler Oberfläche
- `block_body_top_big`([STL](block_body_top_big.stl), [STEP](block_body_top_big.step)): großer oberer Teil des Körpers mit zusätzlichem Volumen für Elektronik
- `block_body_top_lego`([STL](block_body_top_big_lego.stl), [STEP](block_body_top_big_lego.step)): großer oberer Teil des Körpers mit lego-kompatibler Oberfläche

Für die oben genannten Teile muss Ihre Bauplatte mindestens 221x150mm groß sein.

## Druckeinstellungen

Für beste Ergebnisse empfehlen wir die folgenden Druckeinstellungen:

- Schichthöhe: 0.2mm
- Wandlinienanzahl: 3 (mehr Wände für bessere strukturelle Integrität größerer Flächen)
- Obere Schichten: 5
- Untere Schichten: 4
- Füllung: 25%
- Füllmuster: Konzentrisch (diese Einstellung scheint Zeit und Plastik zu sparen)
- Druckgeschwindigkeit: 50mm/sec
- Unterstützung generieren: Ja
- Unterstützungsmuster: Konzentrisch
- Unterstützungsdichte: 15%
- Unterstützungsrand aktivieren: Ja
- Bauplattenhaftungstyp: Keine

Viel Spaß beim Roboterbau!

![Block Body](../../../../docs/images/block_body.jpg)
