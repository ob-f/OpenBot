# OpenBot Compact

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

OpenBot Compact ist ein kompakter vierrädriger [OpenBot](../../README.de-DE.md)-Körper für einen Antrieb mit **ESP32 DevKit V1 + L298N**. Er hat einen LEGO-kompatiblen Deckel mit Noppen, eine verschraubte Abdeckung, eine neigbare Handyhalterung, abnehmbare, verschraubte Elektronikhalterungen und drei Akku-Optionen. Jedes gedruckte Teil passt auf einen **Bambu Lab A1 mini** (größtes Teil 104 × 168 mm).

<p align="center">
  <img src="../../docs/images/compact/teaser_front.jpg" alt="Ansicht von vorne" width="45%" />
</p>

| | |
|---|---|
| **Antrieb** | 4 × TT-Getriebemotoren mit Doppelwelle; linkes und rechtes Paar jeweils parallel an einem L298N |
| **Steuerung** | ESP32 DevKit V1 (30 Pins), OpenBot-Firmware `DIY_ESP32`; Verbindung zum Handy über Bluetooth oder USB-Kabel |
| **Sensoren** | HC-SR04-Ultraschall (vorne), 4 × optische LM393-Radencoder, Akkuspannung |
| **Stromversorgung** | Eine Option wählen: 2S- oder 3S-18650-Box auf dem Deckel oder ein kleiner LiPo-Akku im Inneren (siehe [Akku-Optionen](#akku-optionen)) |
| **Befestigung** | Nur M3 × 10 und M3 × 25 Flachkopfschrauben, M3-Muttern und 19 Gewindeeinsätze |
| **Druck** | PLA, zwei A1-mini-Druckplatten, ≈ 12 h und ≈ 305 g, plus die Teile für die gewählte Akku-Option |

> **Links und rechts** bedeuten in dieser Anleitung immer links und rechts **aus Sicht des Roboters**: Stellen Sie sich hinter den Roboter (Seite mit dem Schalter) und schauen Sie über den Deckel nach vorne zum Ultraschallsensor. Dieselbe Konvention verwendet die Firmware für die „linken“ und „rechten“ Motoren und Encoder.

## Inhalt

- [Gedruckte Teile](#gedruckte-teile) · [Drucken](#drucken)
- [Akku-Optionen](#akku-optionen)
- [Zu kaufende Hardware und Elektronik](#zu-kaufende-hardware-und-elektronik)
- Montage: [1 Einsätze](#1-einsätze-einsetzen) · [2 Motoren und Encoder](#2-motoren-und-drehzahlsensoren-einbauen) · [3 Ultraschallsensor und Front](#3-ultraschallsensor-und-frontteile-befestigen) · [4 ESP32 und Treiber](#4-esp32-und-motortreiber-einbauen) · [5 Verkabelung](#5-strom-und-signale-verkabeln) · [6 Firmware und Testlauf](#6-firmware-einrichten-und-testlauf) · [7 Akku, Handyhalterung und Deckel](#7-akku-handyhalterung-und-deckel-montieren)
- [Alternative Platine mit integriertem Treiber](#alternative-platine-mit-integriertem-treiber)
- [Fehlersuche](#fehlersuche) · [Ansichten](#ansichten) · [Weiter](#weiter)

## Gedruckte Teile

<p align="center">
  <img src="../../docs/images/compact/electronics_mounts.jpg" alt="Chassis mit Elektronikhalterungen" width="45%" />
</p>

| Teil | Anz. | Einbauort / Druckausrichtung |
|---|---:|---|
| [bottom_cover](cad/stl/bottom_cover.stl) ([STEP](cad/step/bottom_cover.step)) | 1 | Chassis; Boden nach unten, keine Stützen |
| [top_cover](cad/stl/top_cover.stl) ([STEP](cad/step/top_cover.step)) | 1 | Deckel mit Noppen und zwei Kabelöffnungen; flache Unterseite nach unten, Noppen nach oben |
| [lid_plug](cad/stl/lid_plug.stl) ([STEP](cad/step/lid_plug.step)) | 2 | Verschließt die Deckelöffnungen und ergänzt die zwei Noppen. Noppen nach oben. |
| [lid_grommet](cad/stl/lid_grommet.stl) ([STEP](cad/step/lid_grommet.step)) | 1 *(optional)* | Ersetzt einen Stopfen, wo ein Kabel durchgeht. Noppen nach oben. |
| [esp32_carrier](cad/stl/esp32_carrier.stl) ([STEP](cad/step/esp32_carrier.step)) | 1 | ESP32-Träger mit vier Schnapphaken; steht auf seiner flachen Basis, keine Stützen |
| [sensor_support](cad/stl/sensor_support.stl) ([STEP](cad/step/sensor_support.step)) | 4 | Halter für die Drehzahlsensoren; jeder Halter passt an jedes Rad |
| [power_board_stand](cad/stl/power_board_stand.stl) ([STEP](cad/step/power_board_stand.step)) | 1 | Hält den 5-V-Wandler und die Teiler-Lochrasterplatine hinter dem Kühlkörper. Basis nach unten. |
| [pcb_spacer](cad/stl/pcb_spacer.stl) ([STEP](cad/step/pcb_spacer.step)) | 4 | Isolierende 2,4-mm-Abstandshalter unter dem L298N |
| [pcb_washer](cad/stl/pcb_washer.stl) ([STEP](cad/step/pcb_washer.step)) | 8 | Isolierende 0,8-mm-Unterlegscheiben: 4 für den L298N, 4 für die Drehzahlsensoren |
| [phone_mount_tilt_base](../phone_mount/phone_mount_tilt_base.stl) ([STEP](../phone_mount/phone_mount_tilt_base.step)) | 1 | Fuß der Handyhalterung mit zwei Scharnierwangen; steht aufrecht auf dem Fuß, keine Stützen |
| [phone_mount_tilt_column](../phone_mount/phone_mount_tilt_column.stl) ([STEP](../phone_mount/phone_mount_tilt_column.step)) | 1 | Säule der Handyhalterung, neigt sich 0–30° nach hinten; steht aufrecht, Stützen nur unter dem Zapfen für das Gummiband |
| [phone_mount_top](../phone_mount/phone_mount_top.stl) ([STEP](../phone_mount/phone_mount_top.step)) | 1 | Verschiebbarer Handygriff; steht aufrecht, Stützen nur unter dem Zapfen für das Gummiband |
| [phone_mount_bottom](../phone_mount/phone_mount_bottom.stl) ([STEP](../phone_mount/phone_mount_bottom.step)) | 1 *(optional)* | Feste Basis der Handyhalterung statt Neigefuß und Säule; steht aufrecht auf ihrem Fuß, Stützen nur unter dem Zapfen für das Gummiband |
| [led_adapter_12mm](cad/stl/led_adapter_12mm.stl) ([STEP](cad/step/led_adapter_12mm.step)) | 4 | Adapter für 12-mm-Einbauleuchten: 2 vorne, 2 hinten |
| [led_adapter_10mm](cad/stl/led_adapter_10mm.stl) ([STEP](cad/step/led_adapter_10mm.step)) | 4 *(optional)* | Dasselbe für 10-mm-Einbauleuchten, statt `led_adapter_12mm`; 6 mm lang, damit auch kurze Gewinde die Mutter erreichen |
| [led_adapter_8mm](cad/stl/led_adapter_8mm.stl) ([STEP](cad/step/led_adapter_8mm.step)) | 4 *(optional)* | Dasselbe für 8-mm-Einbauleuchten, statt `led_adapter_12mm`; 6 mm lang, damit auch kurze Gewinde die Mutter erreichen |
| [led_adapter_6mm](cad/stl/led_adapter_6mm.stl) ([STEP](cad/step/led_adapter_6mm.step)) | 4 *(optional)* | Dasselbe für 6-mm-Einbauleuchten, statt `led_adapter_12mm`; 6 mm lang, damit auch kurze Gewinde die Mutter erreichen |
| [encoder_disk](cad/stl/encoder_disk.stl) ([STEP](cad/step/encoder_disk.step)) | 4 | Optional; in **lichtundurchlässigem** Filament drucken oder passende gekaufte Scheiben verwenden. Wird flach gedruckt. |

Die vier Sensorhalter sind identisch, daher passt jeder an jede Position. Die Teile für Ihren Akku sind unter [Akku-Optionen](#akku-optionen) aufgeführt.

## Drucken

STL- und STEP-Dateien für alle Teile liegen in [cad/stl](cad/stl) und [cad/step](cad/step), bereits in Druckausrichtung und in Millimetern; mit 100 % Skalierung drucken. Die Teile der Handyhalterung teilen sich alle Bodys; sie liegen in [body/phone_mount](../phone_mount).

Öffnen Sie **[compact_A1_mini.3mf](cad/compact_A1_mini.3mf)** in Bambu Studio. Die Druckplatten 1 und 2 enthalten alle Standardteile in der benötigten Anzahl. Die Druckplatten 3–5 enthalten jeweils eine Akku-Option; drucken Sie nur die gewählte. Druckplatte 6 enthält die optionale feste Handyhalterung, Druckplatte 7 die optionalen LED-Adapter für 10-, 8- und 6-mm-Leuchten.

| Druckplatte | Inhalt | Zeit* | Filament* |
|---|---|---:|---:|
| 1 – Chassis und Zubehör | Chassis, Ständer für die Stromplatinen, ESP32-Träger, 4 Sensorhalter, 4 LED-Adapter, 4 Abstandshalter, 8 Unterlegscheiben | ≈ 7 h 50 min | ≈ 215 g |
| 2 – Deckel und Handyhalterung | Deckel mit Noppen, neigbare Handyhalterung (Fuß, Säule, Griff), 4 Encoderscheiben, 2 Deckelstopfen | ≈ 3 h 50 min | ≈ 90 g |
| 3 – Akku A (2S-Box) | 2S-Akkubox, ihr Deckel, Kabeldurchführung für den Deckel | ≈ 1 h 40 min | ≈ 42 g |
| 4 – Akku B (3S-Box) | 3S-Akkubox, ihr Deckel, Kabeldurchführung für den Deckel | ≈ 2 h 20 min | ≈ 58 g |
| 5 – Akku C (LiPo-Halter) | LiPo-Halter | ≈ 15 min | ≈ 5 g |
| 6 – Feste Handyhalterung (optional) | feste Basis der Handyhalterung | ≈ 1 h | ≈ 23 g |
| 7 – LED-Adapter (optional) | je 4 für 10-, 8- und 6-mm-Leuchten; nur Ihre Größe drucken | ≈ 25 min | ≈ 10 g |

\*Schätzwerte mit Generic PLA; Druckplatten 2–7 aus dem Teilevolumen hochgerechnet. Ihre Zeiten werden abweichen. Klicken Sie vor dem Druck für jede Druckplatte auf **Slice plate**.

Profil: 0,4-mm-Düse, 0,20-mm-Schichten, 4 Wände, 5 obere und 5 untere Schichten, 30 % Gyroid-Füllung, texturierte PEI-Platte. **Nur die Teile der Handyhalterung brauchen Stützen, und nur unter dem kleinen Zapfen für das Gummiband.** Die Wandlöcher im Chassis sind rund und höchstens 20 mm groß. Die Teile der Handyhalterung werden stehend gedruckt, und das Projekt schaltet Stützen nur für sie ein: ein kleiner Baum unter dem Rand des Zapfens im Kanal. Ziehen Sie ihn mit einer Zange durch die offene Rückseite des Kanals heraus. Für alle anderen Teile sind Stützen aus; wenn Sie ein Teil aus dem STL-Ordner hinzufügen, lassen Sie sie aus, außer bei Teilen der Handyhalterung. Wählen Sie vor dem Druck das tatsächlich geladene PLA und prüfen Sie die Schichtvorschau.

- Der Deckel wird flach mit den Noppen nach oben gedruckt. Das Chassis wird mit dem Boden nach unten und offenem Innenraum gedruckt.
- Entfernen Sie nach dem Druck Fäden aus den Löchern für die Einsätze, den Aufnahmen und den Kabelöffnungen.
- Drucken Sie die 0,8-mm-Unterlegscheiben flach mit 0,20 mm. Sie isolieren die Platinen und verteilen die Schraubenkraft. Ziehen Sie nur an, bis die Schraube aufsitzt, und ersetzen Sie jede verformte Scheibe.
- Halten Sie PLA-Teile von heißen Autos und direkter Sommersonne fern, denn PLA wird bei etwa 55 °C weich.

## Akku-Optionen

Im Chassis ist kein Platz für einen 3-Zellen-18650-Halter (76 × 62 × 20 mm). Jede Stelle mit 20 mm Höhe ist zu schmal. Wählen Sie stattdessen eine dieser drei Optionen:

| | **A – 2S-18650-Box** (empfohlen) | **B – 3S-18650-Box** | **C – LiPo-Halter innen** |
|---|---|---|---|
| Ort | Auf dem Deckel, hinter der Handyhalterung | Auf dem Deckel, hinter der Handyhalterung | Innen, über dem Motortreiber |
| Akku | 2 × 18650 in einem 2-Zellen-Halter in Reihe (≈ 77 × 41 × 20 mm) | 3 × 18650 in einem 3-Zellen-Halter in Reihe (≈ 76 × 62 × 20 mm) | 2S- oder 3S-LiPo, **max. 75 × 31 × 20 mm** (z. B. 3S 450–650 mAh, 2S 1000–1300 mAh) |
| Spannung | 8,4 V voll, 7,4 V nominal | 12,6 V voll, 11,1 V nominal | 8,4 V oder 12,6 V voll |
| Energie | ≈ 21 Wh (2 × 2800 mAh) | ≈ 31 Wh (3 × 2800 mAh) | ≈ 5–10 Wh |
| Gedruckte Teile | [battery_box_2s](cad/stl/battery_box_2s.stl) + [Deckel](cad/stl/battery_box_2s_cover.stl), 1 [lid_grommet](cad/stl/lid_grommet.stl) | [battery_box_3s](cad/stl/battery_box_3s.stl) + [Deckel](cad/stl/battery_box_3s_cover.stl), 1 [lid_grommet](cad/stl/lid_grommet.stl) | [lipo_tray](cad/stl/lipo_tray.stl) |
| Ladegerät | Li-Ionen-Ladegerät mit getrennten Schächten | Li-Ionen-Ladegerät mit getrennten Schächten | LiPo-**Balance**-Ladegerät |
| Hinweise | Passt am besten zu TT-Motoren: nach dem Spannungsabfall des L298N kommen etwa 6,4 V an. | Überspannung für 3–6-V-TT-Motoren (≈ 10,6 V nach dem L298N). Motoren für höhere Spannung verwenden oder die Geschwindigkeit begrenzen. | Kleinste Kapazität. Der Akku ist unsichtbar und geschützt. |

Alle drei behalten den 30-k/10-k-Spannungsteiler der Firmware bei. Nur die Spannungsgrenzen in der Firmware ändern sich (siehe [Schritt 6](#6-firmware-einrichten-und-testlauf)).

<p align="center">
  <img src="../../docs/images/compact/battery_box_2s.jpg" alt="2S-Akkubox auf dem Deckel" width="32%" />
  <img src="../../docs/images/compact/battery_box_3s.jpg" alt="3S-Akkubox auf dem Deckel" width="32%" />
  <img src="../../docs/images/compact/battery_lipo_tray.jpg" alt="LiPo-Halter" width="32%" />
</p>

**A und B – Akkubox auf dem Deckel.** Die Box wird wie ein LEGO-Stein hinter der Handyhalterung auf die Noppen des Deckels gedrückt. Ihr Boden hat einen Kanal, der die Akkukabel nach unten durch die hintere Deckelöffnung führt. Legen Sie den Halter so in die Box, dass seine Kabel am Ende mit dem Kanal liegen. Führen Sie die Kabel durch die Bodenöffnung und die [lid_grommet](cad/stl/lid_grommet.stl) ins Chassis und drücken Sie dann den Deckel mit Noppen auf die vier Zapfen. Auch dieser Deckel hat Noppen, sodass LEGO-Steine weiterhin oben passen. Die Box ragt nicht über die Deckelschrauben in den Ecken. Die 3S-Box liegt vollständig auf dem Deckel, mit 14 mm Abstand zur Hinterkante. Alle vier Deckelschrauben bleiben bei montierter Box zugänglich.

**C – LiPo-Halter innen.** Der Halter steht auf zwei Beinen in den Lücken zwischen den Motoren und sitzt knapp über dem L298N und den Schnapphaken des ESP32-Trägers. Die Höhe des Akkus und der geschlossene Deckel halten ihn unten. Befestigen Sie den Akku mit einem 20-mm-Klettband durch die vier Schlitze am Halter. Ist der Akku dünner als 18 mm, legen Sie ein Schaumstoffpolster darauf, damit er nicht klappert. Erledigen Sie zuerst die gesamte Verkabelung des L298N: Der Halter verdeckt seine Klemmen, lässt sich aber gerade herausheben.

> **Lithium-Sicherheit.** Verwenden Sie Zellen von einem seriösen Händler. Schützen Sie 18650-Akkus mit einer Schutzplatine (BMS). Stecken Sie an einen LiPo einen Spannungswarner, denn weder die Firmware noch der Akku schalten bei leerem Akku ab. Bauen Sie nahe am Akku eine Sicherung ein. Laden Sie LiPos nur mit einem Balance-Ladegerät, auf einer nicht brennbaren Unterlage und nie unbeaufsichtigt. Verwenden Sie keinen aufgeblähten oder beschädigten Akku.

## Zu kaufende Hardware und Elektronik

**Es werden nur zwei Schraubenlängen benötigt: M3 × 10 mm und M3 × 25 mm**, beide mit Flachkopf und etwa 5,5 mm Kopfdurchmesser (keine Senkköpfe). Die Unterlegscheiben für die Platinen werden gedruckt. Legen Sie unter die Schrauben von Deckel und Träger keine Scheiben, denn deren Längen setzen voraus, dass der Kopf direkt auf dem Kunststoff aufliegt.

Die Links unten führen zu einem Händler oder einer Amazon-US-Suche. Prüfen Sie vor der Bestellung die angegebenen Maße und Kennwerte, denn Platinen mit demselben Namen können unterschiedliche Lochbilder haben.

### Befestigungsmaterial

| Anz. | Teil | Verwendung | Kaufen |
|---:|---|---|---|
| 19 (+ Reserve) | M3 × 5,7 mm Messing-Gewindeeinsätze zum Einschmelzen, gängige Größe mit 4,6 mm Außendurchmesser (z. B. ruthex RX-M3x5.7) | 14 im Chassis, 5 in der Handyhalterung | [ruthex](https://www.ruthex.de/products/ruthex-gewindeeinsatz-m3-100-stuck-rx-m3x5-7-messing-gewindebuchsen) |
| 15 | M3 × 10 mm Flachkopfschrauben | 4 Deckel, 2 Träger, 4 L298N, 5 Handyhalterung | [Suche](https://www.amazon.com/s?k=M3+x+10+pan+head+screws+ISO+7045) |
| 12 | M3 × 25 mm Flachkopfschrauben | 8 Motoren, 4 Drehzahlsensoren (+2 für die feste Handyhalterung) | [Suche](https://www.amazon.com/s?k=M3+x+25+pan+head+screws+ISO+7045) |
| 8 | M3-Sechskantmuttern, 5,5 mm Schlüsselweite, ≈ 2,4 mm dick | 8 Motoren (+2 für die feste Handyhalterung) | [Suche](https://www.amazon.com/s?k=M3+hex+nuts+DIN+934) |
| 4 | Kleine Kabelbinder (≤ 3 mm breit) | 5-V-Wandler und Lochrasterplatine am Ständer für die Stromplatinen | [Suche](https://www.amazon.com/s?k=small+cable+ties+2.5mm) |
| ≈ 350 g | 1,75-mm-PLA | alle gedruckten Teile | [Suche](https://www.amazon.com/s?k=PLA+filament+1.75mm) |

### Elektronik und Antrieb

| Anz. | Teil | Spezifikation / Hinweise | Kaufen |
|---:|---|---|---|
| 1 | ESP32 DevKit V1, 30 Pins | Platine etwa 27,94 × 50,97 mm. Prüfen Sie die Lochpositionen, denn DevKit-Klone unterscheiden sich. | [Suche](https://www.amazon.com/s?k=ESP32+DevKit+V1+30+pin+development+board) |
| 1 | L298N-Treibermodul | Etwa 43,5 × 43,5 mm mit Löchern auf einem Quadrat von 37 × 37 mm. | [Suche](https://www.amazon.com/s?k=L298N+motor+driver+module+43+43) |
| 4 | TT-Getriebemotoren mit Doppelwelle | Eine Welle treibt das Rad, die andere die Encoderscheibe. Passt für einen 53,35 mm langen Motorkörper. Wählen Sie eine Nennspannung, die zu Ihrer Akku-Option passt. | [Suche](https://www.amazon.com/s?k=TT+dual+shaft+gear+motor+robot+car) |
| 4 | TT-Räder | ≈ 65 mm Durchmesser | [Suche](https://www.amazon.com/s?k=65mm+TT+motor+robot+car+wheels) |
| 4 | Optische LM393-Drehzahlsensoren | Platine ≈ 14,2 mm breit und ≈ 38,1 mm lang, mit einem 3-mm-Loch. Alle vier werden montiert; standardmäßig liest die Firmware das vordere Paar. | [Suche](https://www.amazon.com/s?k=LM393+optical+speed+sensor+module+encoder+disk) |
| 4 | Encoderscheiben | Nur wenn sie nicht gedruckt werden; müssen zur TT-Welle und zum Sensorschlitz passen | [Suche](https://www.amazon.com/s?k=TT+motor+optical+encoder+disk) |
| 1 | HC-SR04-Ultraschallsensor | Platine 45,5 × 20,5 mm, Wandler 25,5 mm auseinander (Mitte zu Mitte) | [Suche](https://www.amazon.com/s?k=HC+SR04+ultrasonic+sensor+module) |
| 1 | Runder Wippschalter | Passt in das Loch der Rückwand; ausgelegt für Akkuspannung und Motorstrom | [Suche](https://www.amazon.com/s?k=20mm+round+rocker+switch+SPST+DC) |
| 1 | 5-V-Wandler, ≥ 2 A, max. 21 × 43 × 14 mm | Passt auf den Ständer für die Stromplatinen. Für 3S funktioniert ein LM2596-Modul. Für 2S einen Typ wählen, der auch aus 6 V Eingang noch 5 V liefert (z. B. MP1584 oder Buck-Boost). | [Suche](https://www.amazon.com/s?k=5V+buck+converter+2A+module) |
| 1 | Kleine Lochrasterplatine, max. 24 × 34 mm | Akku- und Echo-Spannungsteiler, Lichttreiber; sitzt auf der Rückseite des Ständers | [Suche](https://www.amazon.com/s?k=small+prototype+perfboard) |
| 1 Satz | 1-%-Widerstände | Akku-Teiler 30 k + 10 k; Echo-Teiler 10 k + 20 k; Leuchten 4 × 1 k + 4 × 100 k | [Suche](https://www.amazon.com/s?k=1+percent+metal+film+resistor+assortment+10k+20k+30k+1k+100k) |
| 2 + 2 | Weiße und rote Einbauleuchten *(optional)* | 12-mm-Gewinde passt in `led_adapter_12mm`; für 10-, 8- oder 6-mm-Leuchten den passenden Adapter drucken. Lampenspannung prüfen | [Suche](https://www.amazon.com/s?k=12mm+panel+LED+indicator+12V+white+red) |
| 4 | NPN-Transistoren, z. B. PN2222A *(optional, für die Leuchten)* | Low-Side-Schalter für jede Leuchte | [Suche](https://www.amazon.com/s?k=PN2222A+transistor) |
| 1 Satz | Dupont-Kabel, Silikon-Stromkabel, Schrumpfschlauch | Stromkabel für den Blockierstrom der Motoren auslegen | [Suche](https://www.amazon.com/s?k=silicone+wire+kit+20+22+AWG+heat+shrink) |

### Akku und Laden

Kaufen Sie die Teile für die gewählte [Akku-Option](#akku-optionen).

| Option | Teile | Kaufen |
|---|---|---|
| A – 2S-Box | 2 gleiche 18650-Zellen, 2-Zellen-Halter in **Reihe** (≈ 77 × 41 mm), 2S-Schutzplatine, Kabelsicherung (≈ 5 A), XT30-Paar, Li-Ionen-Ladegerät | [Zellen](https://www.18650batterystore.com/collections/18650-batteries) · [Halter](https://www.amazon.com/s?k=2+x+18650+battery+holder+series+wires) · [BMS/Sicherung/XT30](https://www.amazon.com/s?k=2S+BMS+18650+fuse+holder+XT30) · [Ladegerät](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| B – 3S-Box | 3 gleiche 18650-Zellen, 3-Zellen-Halter in **Reihe** (≈ 76 × 62 mm), 3S-Schutzplatine, Kabelsicherung, XT30-Paar, Li-Ionen-Ladegerät | [Zellen](https://www.18650batterystore.com/collections/18650-batteries) · [Halter](https://www.amazon.com/s?k=3+series+18650+battery+holder) · [BMS/Sicherung/XT30](https://www.amazon.com/s?k=3S+lithium+battery+protection+BMS+fuse+holder+XT30+connector) · [Ladegerät](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| C – LiPo-Halter | 2S- oder 3S-LiPo bis 75 × 31 × 20 mm mit XT30, 20-mm-Klettband, LiPo-Balance-Ladegerät, LiPo-Spannungswarner | [LiPo](https://www.amazon.com/s?k=3S+650mAh+lipo+XT30) · [Band](https://www.amazon.com/s?k=20mm+battery+strap+hook+loop) · [Ladegerät](https://www.amazon.com/s?k=lipo+balance+charger+2S+3S) |

### Sonstiges

| Anz. | Teil | Hinweise | Kaufen |
|---:|---|---|---|
| 1 | Gummiband oder leichte Feder | Spannt den Handygriff | [Suche](https://www.amazon.com/s?k=rubber+bands+assorted+sizes) |
| 1 | 90°-Micro-USB-Datenkabel (gewinkelt) | Flashen im eingebauten Zustand und kabelgebundene Verbindung zum Handy durch den Deckel. Ein gerader Stecker passt nicht neben den L298N (siehe [Schritt 6](#6-firmware-einrichten-und-testlauf)). Stecker passend zur Platine wählen (Micro-USB oder USB-C). | [Suche](https://www.amazon.com/s?k=90+degree+micro+USB+data+cable+up+down+angle) |
| 1 | USB-Power-Blocker / Nur-Daten-Adapter *(nur für die Kabelverbindung zum Handy)* | Verhindert, dass sich Handy und 5-V-Wandler gegenseitig speisen | [Suche](https://www.amazon.com/s?k=USB+data+only+power+blocker+adapter) |
| 1 | Android-Handy | Muss die OpenBot-App ausführen können. Prüfen Sie, ob es mit Hülle in die Halterung passt. | – |
| – | Werkzeug | Temperaturgeregelter Lötkolben mit M3-Einsatzspitze, Schraubendreher/Inbusschlüssel, Multimeter, Seitenschneider, Abisolierzange | [Suche](https://www.amazon.com/s?k=heat+set+insert+soldering+iron+tip+M3) |

## Montage

<p align="center">
  <img src="../../docs/images/compact/exploded.jpg" alt="Explosionsansicht" width="45%" />
</p>

### 1. Einsätze einsetzen

<p align="center">
  <img src="../../docs/images/compact/step_01_shell.jpg" alt="Leeres Gehäuse" width="40%" />
</p>

Verwenden Sie Standard-Gewindeeinsätze M3 × 5,7 mm (zum Beispiel ruthex RX-M3x5.7, 4,6 mm Außendurchmesser) in den 4,0-mm-Vorbohrungen. Drücken Sie jeden Einsatz mit einem temperaturgeregelten Lötkolben und Einsatzspitze gerade und bündig ein. Lassen Sie ihn abkühlen, bevor Sie hineinschrauben.

Setzen Sie **14 Einsätze im Chassis** ein:

- 4 in die Dome für den L298N
- 4 in die Dome für die Drehzahlsensoren
- 2 in das **vordere** Paar der Dome für die lange Platine (diese halten den ESP32-Träger)
- 4 in die Eckpfosten oben an den Wänden (diese halten den Deckel)

Lassen Sie die zwei hinteren Dome für die lange Platine leer, denn nur die [alternative Platine](#alternative-platine-mit-integriertem-treiber) verwendet sie.

### 2. Motoren und Drehzahlsensoren einbauen

<p align="center">
  <img src="../../docs/images/compact/step_02_motors.jpg" alt="Motoren eingebaut" width="49%" />
  <img src="../../docs/images/compact/step_03_sensors.jpg" alt="Sensoren eingebaut" width="49%" />
</p>

Setzen Sie die vier TT-Motoren so ein, dass die Radwellen durch die seitlichen Öffnungen ragen und die Kabel zur Mitte zeigen. Befestigen Sie jeden Motor mit zwei M3 × 25 mm Schrauben und Muttern. Achten Sie darauf, dass die Schrauben das Getriebeinnere nicht berühren. **Lassen Sie die Räder ab**, bis der Testlauf erledigt ist.

Stecken Sie auf jede innere Welle eine Encoderscheibe. Setzen Sie auf jeden der vier Sensordome einen Sensorhalter, mit der flachen Seite zum Motor. Die D-förmige Aufnahme verhindert, dass er sich dreht. Setzen Sie die LM393-Platine zwischen die beiden oberen Führungen und befestigen Sie sie mit einer gedruckten Unterlegscheibe und einer M3 × 25 mm Schraube.

Drehen Sie jede Welle von Hand. Die Scheibe muss ohne zu schleifen durch den optischen Schlitz laufen, und die Sensorbeinchen dürfen sie nicht berühren.

### 3. Ultraschallsensor und Frontteile befestigen

<p align="center">
  <img src="../../docs/images/compact/ultrasonic_retention.jpg" alt="Halterung des Ultraschallsensors" width="40%" />
</p>

Schieben Sie den HC-SR04 von innen gerade in die vordere Vertiefung, sodass beide Wandler durch die runden Öffnungen ragen, bis die zwei federnden Arme an seinen Enden über den oberen Ecken einrasten. Die Vertiefung positioniert die Platine, und die Arme verhindern, dass sie ins Chassis zurückfällt. Zum Herausnehmen drücken Sie die oberen Enden beider Arme nach außen und ziehen die Platine zurück.

Montieren Sie die zwei weißen Frontleuchten, die zwei roten Rückleuchten (jeweils im gedruckten LED-Adapter, der zu ihrem Gewinde passt) und den runden Wippschalter mit dem mitgelieferten Befestigungsmaterial. Verlegen Sie die Kabel weg von den Rädern und Encoderscheiben.

### 4. ESP32 und Motortreiber einbauen

**Flashen Sie zuerst die Firmware** ([Schritt 6](#6-firmware-einrichten-und-testlauf)), wenn Sie kein gewinkeltes USB-Kabel haben. Sobald der L298N eingebaut ist, passt kein gerader USB-Stecker mehr in den ESP32.

Schrauben Sie den ESP32-Träger mit zwei M3 × 10 mm Schrauben an die zwei vorderen Dome für die lange Platine; seine Basis liegt flach auf dem Chassisboden. Drücken Sie den ESP32 mit seinen eigenen Befestigungslöchern auf die vier 2,2-mm-Zentrierstifte, bis die vier Eckhaken über der Platine einrasten. Keine Schraube geht durch die ESP32-Platine. Zum Herausnehmen drücken Sie die zwei Haken an einem Ende nach außen und heben dieses Ende an. Wenn die Löcher Ihrer Platine nicht zu den Stiften passen, wenden Sie keine Gewalt an.

Legen Sie die vier gedruckten 2,4-mm-Abstandshalter auf die Dome für den L298N, setzen Sie den Treiber darauf und befestigen Sie jede Ecke mit einer gedruckten 0,8-mm-Unterlegscheibe und einer M3 × 10 mm Schraube (Lochbild 37 × 37 mm). Prüfen Sie, dass die Lötstellen auf der Unterseite das Chassis nicht berühren und der Kühlkörper Luft hat.

**Ständer für die Stromplatinen.** Binden Sie den 5-V-Wandler vorne und die Teiler-Lochrasterplatine hinten an den Ständer, jeweils mit zwei Kabelbindern durch die Schlitze. Der Ständer steht auf dem Boden hinter dem Kühlkörper des L298N, zwischen den zwei hinteren Drehzahlsensoren und vor dem Schalter. Der geschlossene Deckel hält ihn unten, daher braucht er keine Schrauben.

<p align="center">
  <img src="../../docs/images/compact/step_04_electronics.jpg" alt="Elektronik eingebaut" width="40%" />
</p>

### 5. Strom und Signale verkabeln

<p align="center">
  <img src="../../docs/images/compact/wiring_diagram.png" alt="Schaltplan" width="80%" />
</p>

Verwenden Sie die auf dem ESP32 aufgedruckten GPIO-Nummern. Alle Massen müssen miteinander verbunden sein. Halten Sie die Kabel von Zahnrädern, Encoderscheiben, Schraubenspitzen und der Deckelfuge fern und binden Sie sie gegen Vibrationen fest.

| Verbindung | ESP32-GPIO | Hinweise |
|---|---|---|
| L298N IN1 / IN2 (linke Motoren) | 13 / 12 | |
| L298N IN3 / IN4 (rechte Motoren) | 15 / 2 | |
| L298N ENA / ENB | – | Die Enable-Jumper gesteckt lassen (die Geschwindigkeit wird per PWM an den IN-Pins gesetzt) |
| Encoder vorne links / vorne rechts | 35 / 22 | |
| Encoder hinten links / hinten rechts *(optional)* | 34 / 23 | |
| Ultraschall TRIG | 5 | |
| Ultraschall ECHO | 18 | über einen Teiler 10 k (ECHO → GPIO18) / 20 k (GPIO18 → GND) |
| Akkuspannung | 4 | 30 k vom geschalteten Akku-Plus an GPIO4, 10 k von GPIO4 an GND |
| Frontleuchten L / R *(optional)* | 19 / 21 | über NPN-Transistorstufen |
| Rückleuchten L / R *(optional)* | 25 / 26 | über NPN-Transistorstufen, Firmware-Änderung nötig |

- **Motoren:** Schließen Sie beide linken Motoren parallel an OUT1/OUT2 und beide rechten Motoren an OUT3/OUT4 an. Dreht sich eine Seite falsch herum, tauschen Sie die zwei Motorkabel dieser Seite.
- **Encoder:** Versorgen Sie die LM393-Platinen mit **3,3 V**, nicht mit 5 V. Ihre eingebauten Pull-ups halten die Ausgänge dann auf einem für den ESP32 sicheren Pegel. GPIO34 und GPIO35 sind reine Eingänge und haben keine internen Pull-ups.
- **Leuchten:** Jede Leuchte wird über einen NPN-Transistor geschaltet (1 k Basiswiderstand, 100 k Pull-down von Basis nach Emitter). Treiben Sie eine Einbauleuchte nie direkt von einem ESP32-Pin.

#### Stromversorgung

Akku-Plus → Schutzplatine/BMS → Sicherung → Schalter → Motorversorgung des L298N (+12-V-Klemme). Der 5-V-Wandler bekommt die geschaltete Akkuspannung und versorgt den 5-V/VIN-Pin des ESP32 und den HC-SR04. Lassen Sie die 5-V-Klemme des L298N unbeschaltet. **Schließen Sie niemals USB und die externe 5-V-Versorgung gleichzeitig an**, außer es sitzt ein USB-Power-Blocker im Kabel. Lassen Sie den Akku während der Montage abgeklemmt.

Mit Option A (2S) bekommen die Motoren etwa 6,4 V, was zu gängigen 3–6-V-TT-Motoren passt. Mit Option B oder einem 3S-LiPo bekommen sie etwa 10,6 V. Verwenden Sie dafür ausgelegte Motoren oder begrenzen Sie die Geschwindigkeit in der App.

### 6. Firmware einrichten und Testlauf

Holen Sie die Firmware aus dem [Firmware-Ordner](../../firmware/README.de-DE.md) und öffnen Sie `firmware/openbot/openbot.ino`.

1. Setzen Sie den Robotertyp und lassen Sie den Handy-Modus an:

   ```cpp
   #define OPENBOT DIY_ESP32
   #define NO_PHONE_MODE 0
   ```

2. Schalten Sie im Block `#elif (OPENBOT == DIY_ESP32)` **die Indikatoren aus**. Ihre Standard-Pins (5 und 18) sind in diesem Aufbau die Sonar-Pins:

   ```cpp
   #define HAS_INDICATORS 0   // default is 1
   ```

3. Setzen Sie im selben Block die Akkugrenzen für Ihre Akku-Option. Der Teilerfaktor bleibt unverändert.

   | Option | `VOLTAGE_LOW` | `VOLTAGE_MAX` |
   |---|---:|---:|
   | A – 2S 18650 oder 2S-LiPo | 6.8f | 8.4f |
   | B – 3S 18650 oder 3S-LiPo | 10.2f | 12.6f |

   Die Akkuanzeige der App reicht von `VOLTAGE_LOW` (leer) bis `VOLTAGE_MAX` (voll). Lassen Sie `VOLTAGE_MIN` unverändert: Die Firmware verwendet es nur für die minimale Motorgeschwindigkeit, wenn `NO_PHONE_MODE` aktiv ist. Die Firmware schaltet die Motoren bei niedriger Spannung nie ab, daher müssen die Schutzplatine (Optionen A und B) oder ein LiPo-Spannungswarner (Option C) die Zellen schützen.

4. Optional, im selben Block:

   ```cpp
   #define HAS_SPEED_SENSORS_BACK 1   // read the rear encoders as well
   #define HAS_LEDS_FRONT 1           // white lights on GPIO19/21 (default pins)
   #define HAS_LEDS_BACK 1            // red lights, and change their pins:
   const int PIN_LED_LB = 25;         // default 5
   const int PIN_LED_RB = 26;         // default 18
   ```

5. Installieren Sie in der Arduino IDE das **esp32-Boardpaket 2.0.17**. Die Firmware verwendet `ledcSetup`/`ledcAttachPin`, die in Version 3.x entfernt wurden. Wählen Sie dann **ESP32 Dev Module** und laden Sie hoch.

**USB-Zugang.** Der ESP32 sitzt kopfüber in seinem Träger, daher liegt seine USB-Buchse unter der Platine und zeigt auf die Stromklemme des L298N, die nur etwa 8,5 mm entfernt ist. Mit eingebautem Treiber passt nur ein gewinkelter Stecker, dessen Kabel zwischen den beiden Platinen nach oben führt. Flashen Sie entweder vor dem Einbau des L298N oder verwenden Sie ein 90°-Kabel.

GPIO 2, 5, 12 und 15 sind Boot-Strapping-Pins. Wenn die Platine mit allem angeschlossen nicht bootet oder sich nicht flashen lässt, prüfen Sie, was an ihnen zieht, vor allem an GPIO12, der beim Reset low sein muss.

**Testlauf** (Räder ab, Chassis aufgebockt, Serieller Monitor mit 115200 Baud):

| Senden | Erwartet |
|---|---|
| `f` | Eine Feature-Zeichenkette, die `DIY_ESP32` meldet |
| *(Ausgabe beobachten)* | Eine plausible Akkuspannung und Sonar-Entfernung |
| `c60,0`, dann `c0,0` | Nur die linke Seite dreht sich, vorwärts |
| `c0,60`, dann `c0,0` | Nur die rechte Seite dreht sich, vorwärts |

Prüfen Sie, während sich die Räder drehen, dass sich die Encoderzählungen ändern. Brechen Sie sofort ab, wenn eine Scheibe schleift, ein Kabel warm wird oder der ESP32 neu startet.

Installieren Sie die Android-App **OpenBot Robot**. Wählen Sie **Bluetooth**, erteilen Sie die angefragten Berechtigungen, suchen Sie nach `OpenBot` und verbinden Sie sich. Für eine Kabelverbindung siehe den nächsten Schritt.

### 7. Akku, Handyhalterung und Deckel montieren

<p align="center">
  <img src="../../docs/images/compact/step_06_phone_lid.jpg" alt="Handyhalterung" width="32%" />
  <img src="../../docs/images/compact/lid_opening.jpg" alt="Deckelöffnungen mit Stopfen und Durchführung" width="32%" />
  <img src="../../docs/images/compact/step_07_closed.jpg" alt="Geschlossener Körper" width="32%" />
</p>

**Handyhalterung.** Die Halterung lässt sich neigen, sodass die Kamera auch nach oben schauen kann, etwa auf eine Person vor dem Roboter. Soll die Kamera stattdessen nach unten schauen, schrauben Sie den Fuß um 180° gedreht an und setzen das Handy mit dem Bildschirm zur Säule ein.

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="Scharnier der neigbaren Handyhalterung" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="Neigbare Handyhalterung bei 0, 15 und 30 Grad, oben normal und unten gedreht montiert" width="64%" />
</p>

1. Schmelzen Sie wie in Schritt 1 drei M3-Einsätze in die Säule ein: je einen in beide Enden des hinteren Drehlochs und einen in das vordere Arretierloch, das nur auf einer Seite offen ist.
2. Schmelzen Sie zwei M3-Einsätze in die Löcher an der Unterseite des Fußes ein. Schrauben Sie den Fuß mit zwei M3 × 10 mm Schrauben von unten an die vordere Fläche des Deckels.
3. Setzen Sie die Säule so zwischen die Wangen, dass das offene Arretierloch zur Wange mit vier Löchern zeigt. Drehen Sie durch jede Wange eine M3 × 10 mm Schraube in die Einsätze des Drehlochs und ziehen Sie sie an, bis sich die Säule ohne Spiel drehen lässt. Geben Sie auf beide Achsschrauben einen Tropfen mittelfeste (blaue) Schraubensicherung, damit sie sich nicht lösen.
4. Neigen Sie die Säule, bis ihr Arretiereinsatz mit einem der drei Löcher der Wange fluchtet: Das unterste ergibt 0°, das mittlere 15° und das oberste 30°. Arretieren Sie die Säule mit der dritten M3 × 10 mm Schraube.
5. Setzen Sie den verschiebbaren Griff ein und spannen Sie ihn mit einem Gummiband oder einer Feder. Prüfen Sie mit dem Handy in seiner Hülle, dass es fest gehalten wird, die Kamera freie Sicht hat und keine Tasten gedrückt werden.

Auch bei 30° bleibt etwa 15 mm Abstand zum Akkukasten. Wenn Sie keine Neigung brauchen, drucken Sie [phone_mount_bottom](../phone_mount/phone_mount_bottom.stl) statt Fuß und Säule: Drücken Sie zwei M3-Muttern in die Taschen und schrauben Sie die Basis mit zwei M3 × 25 mm Schrauben von unten an die Fläche des Deckels. Sie nimmt denselben Griff auf, und das Handy sitzt 10,5 mm tiefer.

**Deckelöffnungen.** Der Deckel hat zwei Öffnungen von 1 × 2 Noppen, eine vor und eine hinter der Handyfläche. Setzen Sie den Deckel so auf, dass die Handyfläche zum Ultraschallsensor vorne zeigt. Die Fläche liegt 16 mm vor der Deckelmitte; die hintere Noppenfläche trägt die Akkubox. Drücken Sie in jede ungenutzte Öffnung einen `lid_plug`; er ergänzt die Noppen, und eine 1 mm breite Kante unten in der Öffnung verhindert, dass er durchfällt. Wo ein Kabel durchgeht, verwenden Sie stattdessen die `lid_grommet`:

- **Kabelverbindung zum Handy:** Führen Sie ein 90°-USB-Kabel vom ESP32 durch die vordere Öffnung nach oben zum Handy. Setzen Sie einen USB-Power-Blocker in die Leitung, damit sich Handy und 5-V-Wandler nicht gegenseitig speisen. Wählen Sie in der App **USB** statt Bluetooth.
- **Akkubox (Optionen A und B):** Die Akkukabel kommen durch die hintere Öffnung nach unten.

Bauen Sie jetzt den LiPo-Halter ein, wenn Sie Option C verwenden. Legen Sie die Kabel vom Rand weg, setzen Sie den Deckel flach auf das Chassis und drehen Sie vier M3 × 10 mm Schrauben (ohne Scheiben) in die Einsätze in den Ecken. Ziehen Sie sie gleichmäßig an, bis sie aufsitzen. Der Deckel darf sich nicht anheben, und die Halterung darf nicht wackeln. Drücken Sie bei den Optionen A und B die Akkubox hinter der Handyhalterung auf die Noppen.

Montieren Sie die Räder erst, wenn der Testlauf bestanden ist. Fahren Sie das erste Mal langsam auf einem freien, ebenen Boden und prüfen Sie danach alle Schrauben und den Handygriff. Heben Sie den Roboter am Chassis an, nicht am Deckel, an der Handyhalterung oder an der Akkubox.

## Alternative Platine mit integriertem Treiber

<p align="center">
  <img src="../../docs/images/compact/pcb_option.jpg" alt="Platz für die alternative Platine" width="40%" />
</p>

Das Chassis nimmt auch die OpenBot-**PCB V2** (Arduino Nano + DRV8871-Treiber) auf, die ein Befestigungsraster von 121,031 × 28,448 mm hat. Das ist ein anderer elektrischer Aufbau und **kein** direkter Ersatz für den ESP32. Sie läuft mit der Firmware `PCB_V2` und der Nano-Verkabelung.

- Lassen Sie den ESP32-Träger, den L298N und den Ständer für die Stromplatinen weg.
- Setzen Sie 4 Einsätze in die vier Dome für die lange Platine. Mit den 4 Einsätzen für die Sensoren, 4 für den Deckel und 5 für die Handyhalterung sind das **insgesamt 17 Einsätze**.
- Montieren Sie die Platine auf vier gedruckten 2,4-mm-Abstandshaltern, mit einer gedruckten Unterlegscheibe und einer M3 × 10 mm Schraube an jedem Loch.
- Hardware insgesamt: **13 × M3 × 10** (4 Deckel, 4 Platine, 5 Handyhalterung), **12 × M3 × 25**, 8 Muttern.

Bestellen Sie die Platine mit den [Fertigungsdateien und der Stückliste der PCB V2](../diy/pcb/v2), zum Beispiel bei [JLCPCB](https://jlcpcb.com/) oder [PCBWay](https://www.pcbway.com/). Prüfen Sie vor dem Festschrauben die Höhe der bestückten Bauteile und den Abstand der Lötstellen. Die Akku-Optionen oben funktionieren mit dieser Platine genauso.

## Fehlersuche

| Symptom | Prüfen |
|---|---|
| Kompilierfehler zu `ledcSetup` / `ledcAttachPin` | Der esp32-Core ist 3.x. Installieren Sie 2.0.17. |
| USB-Stecker passt nicht in den ESP32 | Der L298N ist im Weg. Verwenden Sie einen 90°-Stecker oder bauen Sie zum Flashen den Treiber aus. |
| ESP32 bootet oder flasht nach dem Verkabeln nicht | Belastung an GPIO12/2/15/5. Trennen Sie die Eingänge des L298N und versuchen Sie es erneut. |
| ESP32 startet neu, wenn die Motoren anlaufen | Schwacher oder leerer Akku oder ein 5-V-Wandler, der aus 2S nicht regeln kann. Messen Sie die 5-V-Schiene unter Last. |
| Sonar immer 0 oder unregelmäßig | `HAS_INDICATORS` ist noch 1, oder der Echo-Teiler fehlt oder ist falsch |
| Keine Zählung von einem Encoder | Sensor nicht an 3,3 V, Scheibe nicht im Schlitz oder Scheibe nicht lichtundurchlässig |
| Roboter dreht, obwohl er geradeaus fahren soll | Die Motorkabel einer Seite sind vertauscht; tauschen Sie das Paar an OUT1/OUT2 oder OUT3/OUT4 |
| Akkuwert falsch oder App zeigt „niedrig“ | Der Teiler muss 30 k (oben) / 10 k (unten) sein, und die Spannungsgrenzen müssen zu Ihrer Option passen |
| Deckel sitzt nicht auf | Ein Kabel ist am Rand eingeklemmt, der LiPo ist zu dick, oder ein Deckeleinsatz steht über seinen Pfosten hinaus |
| Akkubox sitzt locker auf dem Deckel | Die gedruckten Noppen sind zu klein geraten. Erhöhen Sie den Fluss leicht oder prüfen Sie die Maßgenauigkeit des Druckers und drucken Sie die Box neu |

## Ansichten

| Vorne | Hinten |
|---|---|
| ![Ansicht von vorne](../../docs/images/compact/teaser_front.jpg) | ![Ansicht von hinten](../../docs/images/compact/teaser_rear.jpg) |
| **Oben** | **Unten** |
| ![Ansicht von oben](../../docs/images/compact/top_view.jpg) | ![Unterseite](../../docs/images/compact/underside.jpg) |

Die Renderings zeigen keine Verkabelung. Platinen, Akkuhalter und der LiPo-Akku sind als vereinfachte Formen dargestellt.

## Weiter

Installieren Sie die [OpenBot-Robot-App](../../android/robot/README.de-DE.md) auf Ihrem Handy und verbinden Sie sie wie in [Schritt 6](#6-firmware-einrichten-und-testlauf) beschrieben mit dem Roboter.
