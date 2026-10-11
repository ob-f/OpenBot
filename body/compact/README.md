# OpenBot Compact

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

OpenBot Compact is a small four-wheel [OpenBot](../../README.md) body for an **ESP32 DevKit V1 + L298N** drive. It has a LEGO-compatible studded lid, a screw-fastened cover, a tilting phone holder, removable screw-retained electronics mounts and three battery options. Every printed part fits a **Bambu Lab A1 mini** (largest part 104 × 168 mm).

<p align="center">
  <img src="../../docs/images/compact/teaser_front.jpg" alt="Front view" width="45%" />
</p>

| | |
|---|---|
| **Drive** | 4 × TT dual-shaft gear motors, left pair and right pair in parallel on one L298N |
| **Controller** | ESP32 DevKit V1 (30-pin), OpenBot firmware `DIY_ESP32`; phone link by Bluetooth or USB cable |
| **Sensors** | HC-SR04 ultrasonic (front), 4 × LM393 optical wheel encoders, battery voltage |
| **Power** | Choose one: 2S or 3S 18650 box on the lid, or a small LiPo pack inside (see [Battery options](#battery-options)) |
| **Fasteners** | Only M3 × 10 and M3 × 25 pan-head screws, M3 nuts and 20 heat-set inserts |
| **Printing** | PLA, two A1 mini plates, ≈ 12 h and ≈ 305 g, plus the parts for your chosen battery option |

> **Left and right** in this guide always mean the **robot's own** left and right: stand behind the robot (switch end) and look forward over the lid toward the ultrasonic sensor. This is the same convention the firmware uses for "left" and "right" motors and encoders.

## Contents

- [Printed parts](#printed-parts) · [Printing](#printing)
- [Battery options](#battery-options)
- [Hardware and electronics to buy](#hardware-and-electronics-to-buy)
- Assembly: [1 Inserts and nuts](#1-install-inserts-and-lid-nuts) · [2 Motors and encoders](#2-install-motors-and-speed-sensors) · [3 Ultrasonic sensor and panel](#3-secure-the-ultrasonic-sensor-and-panel-parts) · [4 ESP32 and driver](#4-fit-the-esp32-and-motor-driver) · [5 Wiring](#5-wire-power-and-signals) · [6 Firmware and bench test](#6-configure-firmware-and-bench-test) · [7 Battery, phone holder and lid](#7-fit-the-battery-phone-holder-and-lid)
- [Alternative integrated-driver PCB](#alternative-integrated-driver-pcb)
- [Troubleshooting](#troubleshooting) · [Views](#views) · [Next](#next)

## Printed parts

<p align="center">
  <img src="../../docs/images/compact/electronics_mounts.jpg" alt="Chassis with electronics mounts" width="45%" />
</p>

| Part | Qty | Where it goes / print orientation |
|---|---:|---|
| [bottom_cover](cad/stl/bottom_cover.stl) ([STEP](cad/step/bottom_cover.step)) | 1 | Chassis; floor down, no supports |
| [top_cover](cad/stl/top_cover.stl) ([STEP](cad/step/top_cover.step)) | 1 | Studded lid with two cable openings; flat underside down, studs up |
| [lid_plug](cad/stl/lid_plug.stl) ([STEP](cad/step/lid_plug.step)) | 2 | Closes the lid openings; restores the two studs. Studs up. |
| [lid_grommet](cad/stl/lid_grommet.stl) ([STEP](cad/step/lid_grommet.step)) | 1 *(optional)* | Replaces a plug where a cable passes through. Studs up. |
| [esp32_carrier](cad/stl/esp32_carrier.stl) ([STEP](cad/step/esp32_carrier.step)) | 1 | ESP32 support frame; flat base down |
| [esp32_retainer](cad/stl/esp32_retainer.stl) ([STEP](cad/step/esp32_retainer.step)) | 4 | ESP32 corner tabs; any tab fits any corner. Flat down, nib up. |
| [sensor_support](cad/stl/sensor_support.stl) ([STEP](cad/step/sensor_support.step)) | 4 | Speed-sensor supports; any support fits any wheel |
| [ultrasonic_clip_left](cad/stl/ultrasonic_clip_left.stl) ([STEP](cad/step/ultrasonic_clip_left.step)) | 1 | Upper **left** corner of the HC-SR04 board |
| [ultrasonic_clip_right](cad/stl/ultrasonic_clip_right.stl) ([STEP](cad/step/ultrasonic_clip_right.step)) | 1 | Upper **right** corner of the HC-SR04 board |
| [power_board_stand](cad/stl/power_board_stand.stl) ([STEP](cad/step/power_board_stand.step)) | 1 | Holds the 5 V converter and the divider perfboard behind the heatsink. Base down. |
| [pcb_spacer](cad/stl/pcb_spacer.stl) ([STEP](cad/step/pcb_spacer.step)) | 4 | 2.4 mm insulating standoffs under the L298N |
| [pcb_washer](cad/stl/pcb_washer.stl) ([STEP](cad/step/pcb_washer.step)) | 8 | 0.8 mm insulating washers: 4 for the L298N, 4 for the speed sensors |
| [phone_mount_tilt_base](../phone_mount/phone_mount_tilt_base.stl) ([STEP](../phone_mount/phone_mount_tilt_base.step)) | 1 | Phone-holder base with two hinge cheeks; stands upright on its foot, no supports |
| [phone_mount_tilt_column](../phone_mount/phone_mount_tilt_column.stl) ([STEP](../phone_mount/phone_mount_tilt_column.step)) | 1 | Phone-holder column, tilts back 0–30°; stands upright, supports only under the band stud |
| [phone_mount_top](../phone_mount/phone_mount_top.stl) ([STEP](../phone_mount/phone_mount_top.step)) | 1 | Sliding phone grip; stands upright, supports only under the band stud |
| [phone_mount_bottom](../phone_mount/phone_mount_bottom.stl) ([STEP](../phone_mount/phone_mount_bottom.step)) | 1 *(optional)* | Fixed phone-holder base instead of the tilting base and column; stands upright on its foot, supports only under the band stud |
| [led_adapter](cad/stl/led_adapter.stl) ([STEP](cad/step/led_adapter.step)) | 4 | Adapters for 12 mm panel lights: 2 front, 2 rear |
| [led_adapter_10mm](cad/stl/led_adapter_10mm.stl) ([STEP](cad/step/led_adapter_10mm.step)) | 4 *(optional)* | Same for 10 mm panel lights, instead of `led_adapter`; 6 mm long so short threads still reach the nut |
| [led_adapter_8mm](cad/stl/led_adapter_8mm.stl) ([STEP](cad/step/led_adapter_8mm.step)) | 4 *(optional)* | Same for 8 mm panel lights, instead of `led_adapter`; 6 mm long so short threads still reach the nut |
| [led_adapter_6mm](cad/stl/led_adapter_6mm.stl) ([STEP](cad/step/led_adapter_6mm.step)) | 4 *(optional)* | Same for 6 mm panel lights, instead of `led_adapter`; 6 mm long so short threads still reach the nut |
| [encoder_disk](cad/stl/encoder_disk.stl) ([STEP](cad/step/encoder_disk.step)) | 4 | Optional; print in **opaque** filament, or use matching purchased disks. Prints flat. |

The four ESP32 retainers are identical, and so are the four sensor supports, so any one fits any position. The ultrasonic clips are a mirrored left/right pair. Print them lying on their broad back faces. The parts for your battery are listed under [Battery options](#battery-options).

## Printing

STL and STEP files for every part are in [cad/stl](cad/stl) and [cad/step](cad/step), already in print orientation and in millimetres; print at 100 % scale. The phone holder parts are shared with the other bodies and live in [body/phone_mount](../phone_mount).

Open **[compact_A1_mini.3mf](cad/compact_A1_mini.3mf)** in Bambu Studio. Plates 1 and 2 hold every standard part in the required quantity. Plates 3–5 each hold one battery option; print only the one you chose. Plate 6 holds the optional fixed phone holder, plate 7 the optional LED adapters for 10, 8 and 6 mm lights.

| Plate | Contents | Time* | Filament* |
|---|---|---:|---:|
| 1 – Chassis and accessories | chassis, power-board stand, ESP32 carrier, 4 sensor supports, 4 LED adapters, both ultrasonic clips, 4 ESP32 retainers, 4 spacers, 8 washers | ≈ 7 h 50 min | ≈ 215 g |
| 2 – Lid and phone holder | studded lid, tilting phone holder (base, column, grip), 4 encoder disks, 2 lid plugs | ≈ 3 h 50 min | ≈ 90 g |
| 3 – Battery A (2S box) | 2S battery box, its cover, lid grommet | ≈ 1 h 40 min | ≈ 42 g |
| 4 – Battery B (3S box) | 3S battery box, its cover, lid grommet | ≈ 2 h 20 min | ≈ 58 g |
| 5 – Battery C (LiPo tray) | LiPo tray | ≈ 15 min | ≈ 5 g |
| 6 – Fixed phone holder (optional) | fixed phone-holder base | ≈ 1 h | ≈ 23 g |
| 7 – LED adapters (optional) | 4 each for 10, 8 and 6 mm lights; print only your size | ≈ 25 min | ≈ 10 g |

\*Estimates with Generic PLA; plates 2–7 are scaled from part volume. Your times will differ. Press **Slice plate** for each plate before printing.

Profile: 0.4 mm nozzle, 0.20 mm layers, 4 walls, 5 top and 5 bottom layers, 30 % gyroid infill, textured PEI plate. **Only the phone-holder parts need supports, and only under the small band stud.** The wall holes in the chassis are round and at most 20 mm across, and its nut pockets bridge about 8 mm. The phone-holder parts print standing upright, and the project turns supports on for them alone: a small tree under the lip of the band stud, inside the channel. Pull it out with pliers through the open back of the channel. Supports are off for every other part; if you add a part from the STL folder, leave them off unless it is a phone-holder part. Before printing, select the PLA that is actually loaded and check the layer preview.

- The lid prints flat with its studs up. The chassis prints floor down with the cavity open.
- After printing, clear strings from nut pockets (leave the two small ribs at each pocket mouth in place), insert holes, sockets and cable openings.
- Print the 0.8 mm washers flat at 0.20 mm. They insulate the boards and spread the screw load. Tighten only until seated and replace any washer that deforms.
- Keep PLA parts out of hot cars and direct summer sun, because PLA softens at about 55 °C.

## Battery options

There isn't room inside the chassis for a 3-cell 18650 holder (76 × 62 × 20 mm). Every spot with 20 mm of headroom is too narrow. Pick one of these three options instead:

| | **A – 2S 18650 box** (recommended) | **B – 3S 18650 box** | **C – LiPo tray inside** |
|---|---|---|---|
| Where | On the lid, behind the phone holder | On the lid, behind the phone holder | Inside, above the motor driver |
| Battery | 2 × 18650 in a 2-cell series holder (≈ 77 × 41 × 20 mm) | 3 × 18650 in a 3-cell series holder (≈ 76 × 62 × 20 mm) | 2S or 3S LiPo, **max. 75 × 31 × 20 mm** (e.g. 3S 450–650 mAh, 2S 1000–1300 mAh) |
| Voltage | 8.4 V full, 7.4 V nominal | 12.6 V full, 11.1 V nominal | 8.4 V or 12.6 V full |
| Energy | ≈ 21 Wh (2 × 2800 mAh) | ≈ 31 Wh (3 × 2800 mAh) | ≈ 5–10 Wh |
| Printed parts | [battery_box_2s](cad/stl/battery_box_2s.stl) + [cover](cad/stl/battery_box_2s_cover.stl), 1 [lid_grommet](cad/stl/lid_grommet.stl) | [battery_box_3s](cad/stl/battery_box_3s.stl) + [cover](cad/stl/battery_box_3s_cover.stl), 1 [lid_grommet](cad/stl/lid_grommet.stl) | [lipo_tray](cad/stl/lipo_tray.stl) |
| Charger | Li-ion charger with separate slots | Li-ion charger with separate slots | LiPo **balance** charger |
| Notes | Best match for TT motors: about 6.4 V reaches them after the L298N's drop. | Over-voltage for 3–6 V TT motors (≈ 10.6 V after the L298N). Use higher-voltage motors or limit speed. | Smallest capacity. The pack is out of sight and protected. |

All three keep the firmware's 30 k / 10 k battery divider. Only the voltage limits in the firmware change (see [step 6](#6-configure-firmware-and-bench-test)).

<p align="center">
  <img src="../../docs/images/compact/battery_box_2s.jpg" alt="2S battery box on the lid" width="32%" />
  <img src="../../docs/images/compact/battery_box_3s.jpg" alt="3S battery box on the lid" width="32%" />
  <img src="../../docs/images/compact/battery_lipo_tray.jpg" alt="LiPo tray" width="32%" />
</p>

**A and B – battery box on the lid.** The box presses onto the lid's studs behind the phone holder like a LEGO brick. Its floor has a channel that leads the battery wires down through the rear lid opening. Lay the holder in the box with its wires at the end with the channel. Feed the wires through the floor opening and the [lid_grommet](cad/stl/lid_grommet.stl) into the chassis, then press the studded cover onto the four pegs. The cover has studs too, so LEGO bricks still fit on top. The box clears the corner lid screws. The 3S box sits fully within the lid, with 14 mm between the box and the rear edge. All four lid screws remain accessible with the box installed.

**C – LiPo tray inside.** The tray stands on two legs in the gaps between the motors and sits just above the L298N and the ESP32 retainer screws. The battery's own height and the closed lid hold it down. Strap the pack to the tray with a 20 mm hook-and-loop strap through the four slots. If the pack is thinner than 18 mm, add a foam pad on top so it can't rattle. Do all the L298N wiring first: the tray covers its terminals, but lifts straight out.

> **Lithium safety.** Use cells from a reputable dealer. Protect 18650 packs with a protection board (BMS). Fit a plug-in voltage alarm to a LiPo, because neither the firmware nor the pack switches off when it is empty. Fit a fuse close to the battery. Charge LiPos only on a balance charger, on a non-flammable surface, and never unattended. Don't use a pack that is swollen or damaged.

## Hardware and electronics to buy

**Only two screw lengths are needed: M3 × 10 mm and M3 × 25 mm**, both pan head with a head about 5.5 mm across (no countersunk heads). Washers for the circuit boards are printed. Don't add washers under the lid, carrier or ESP32-retainer screws, because their lengths assume the head seats directly on the plastic.

The links below go to a supplier or an Amazon US search. Check the listed dimensions and ratings before you order, because boards sold under the same name can have different hole patterns.

### Fasteners

| Qty | Part | Used for | Buy |
|---:|---|---|---|
| 20 (+ spares) | M3 × 5.7 mm brass heat-set inserts, the common 4.6 mm OD size (e.g. ruthex RX-M3x5.7) | 12 in the chassis, 4 in the ESP32 carrier, 4 in the phone-holder column | [ruthex](https://www.ruthex.de/products/ruthex-gewindeeinsatz-m3-100-stuck-rx-m3x5-7-messing-gewindebuchsen) |
| 19 | M3 × 10 mm pan-head screws | 4 lid, 2 carrier, 4 ESP32 retainers, 4 L298N, 2 ultrasonic clips, 3 phone holder | [search](https://www.amazon.com/s?k=M3+x+10+pan+head+screws+ISO+7045) |
| 14 | M3 × 25 mm pan-head screws | 8 motors, 4 speed sensors, 2 phone holder | [search](https://www.amazon.com/s?k=M3+x+25+pan+head+screws+ISO+7045) |
| 14 | M3 hex nuts, 5.5 mm across flats, ≈ 2.4 mm thick | 4 lid, 8 motors, 2 phone holder | [search](https://www.amazon.com/s?k=M3+hex+nuts+DIN+934) |
| 4 | Small cable ties (≤ 3 mm wide) | 5 V converter and perfboard on the power-board stand | [search](https://www.amazon.com/s?k=small+cable+ties+2.5mm) |
| ≈ 350 g | 1.75 mm PLA | all printed parts | [search](https://www.amazon.com/s?k=PLA+filament+1.75mm) |

### Electronics and drive

| Qty | Part | Specification / notes | Buy |
|---:|---|---|---|
| 1 | ESP32 DevKit V1, 30-pin | PCB about 27.94 × 50.97 mm. Check the hole positions, because DevKit clones differ. | [search](https://www.amazon.com/s?k=ESP32+DevKit+V1+30+pin+development+board) |
| 1 | L298N driver module | About 43.5 × 43.5 mm with holes on a 37 × 37 mm square. | [search](https://www.amazon.com/s?k=L298N+motor+driver+module+43+43) |
| 4 | TT dual-shaft gear motors | One shaft drives the wheel and the other drives the encoder disk. Fits a 53.35 mm motor body. Choose a voltage rating that suits your battery option. | [search](https://www.amazon.com/s?k=TT+dual+shaft+gear+motor+robot+car) |
| 4 | TT wheels | ≈ 65 mm diameter | [search](https://www.amazon.com/s?k=65mm+TT+motor+robot+car+wheels) |
| 4 | LM393 optical speed sensors | PCB ≈ 14.2 mm wide and ≈ 38.1 mm long, with one 3 mm hole. All four are mounted; by default the firmware reads the front pair. | [search](https://www.amazon.com/s?k=LM393+optical+speed+sensor+module+encoder+disk) |
| 4 | Encoder disks | Only if not printing them; must match the TT shaft and the sensor slot | [search](https://www.amazon.com/s?k=TT+motor+optical+encoder+disk) |
| 1 | HC-SR04 ultrasonic sensor | 45.5 × 20.5 mm PCB, transducers 25.5 mm apart (centre to centre) | [search](https://www.amazon.com/s?k=HC+SR04+ultrasonic+sensor+module) |
| 1 | Round rocker switch | Fits the rear panel hole; rated for the battery voltage and motor current | [search](https://www.amazon.com/s?k=20mm+round+rocker+switch+SPST+DC) |
| 1 | 5 V converter, ≥ 2 A, max. 21 × 43 × 14 mm | Fits the power-board stand. For 3S, an LM2596 module works. For 2S, choose a type that still gives 5 V from a 6 V input (e.g. MP1584 or a buck-boost). | [search](https://www.amazon.com/s?k=5V+buck+converter+2A+module) |
| 1 | Small perfboard, max. 24 × 34 mm | Battery and echo dividers, light drivers; sits on the back of the stand | [search](https://www.amazon.com/s?k=small+prototype+perfboard) |
| 1 set | 1 % resistors | Battery divider 30 k + 10 k; echo divider 10 k + 20 k; lights 4 × 1 k + 4 × 100 k | [search](https://www.amazon.com/s?k=1+percent+metal+film+resistor+assortment+10k+20k+30k+1k+100k) |
| 2 + 2 | White and red panel indicators *(optional)* | 12 mm thread fits `led_adapter`; for 10, 8 or 6 mm lights print the matching adapter. Check the lamp voltage | [search](https://www.amazon.com/s?k=12mm+panel+LED+indicator+12V+white+red) |
| 4 | NPN transistors, e.g. PN2222A *(optional, for lights)* | Low-side switch for each lamp | [search](https://www.amazon.com/s?k=PN2222A+transistor) |
| 1 set | Dupont jumpers, silicone power wire, heat-shrink | Size the power wire for the motor stall current | [search](https://www.amazon.com/s?k=silicone+wire+kit+20+22+AWG+heat+shrink) |

### Battery and charging

Buy the items for the [battery option](#battery-options) you chose.

| Option | Parts | Buy |
|---|---|---|
| A – 2S box | 2 matched 18650 cells, 2-cell **series** holder (≈ 77 × 41 mm), 2S protection board, inline fuse (≈ 5 A), XT30 pair, Li-ion charger | [cells](https://www.18650batterystore.com/collections/18650-batteries) · [holder](https://www.amazon.com/s?k=2+x+18650+battery+holder+series+wires) · [BMS/fuse/XT30](https://www.amazon.com/s?k=2S+BMS+18650+fuse+holder+XT30) · [charger](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| B – 3S box | 3 matched 18650 cells, 3-cell **series** holder (≈ 76 × 62 mm), 3S protection board, inline fuse, XT30 pair, Li-ion charger | [cells](https://www.18650batterystore.com/collections/18650-batteries) · [holder](https://www.amazon.com/s?k=3+series+18650+battery+holder) · [BMS/fuse/XT30](https://www.amazon.com/s?k=3S+lithium+battery+protection+BMS+fuse+holder+XT30+connector) · [charger](https://www.amazon.com/s?k=18650+lithium+ion+battery+charger+independent+slots) |
| C – LiPo tray | 2S or 3S LiPo up to 75 × 31 × 20 mm with XT30, 20 mm hook-and-loop strap, LiPo balance charger, LiPo voltage alarm | [LiPo](https://www.amazon.com/s?k=3S+650mAh+lipo+XT30) · [strap](https://www.amazon.com/s?k=20mm+battery+strap+hook+loop) · [charger](https://www.amazon.com/s?k=lipo+balance+charger+2S+3S) |

### Other

| Qty | Part | Notes | Buy |
|---:|---|---|---|
| 1 | Rubber band or light spring | Tensions the phone grip | [search](https://www.amazon.com/s?k=rubber+bands+assorted+sizes) |
| 1 | 90° (right-angle) micro-USB data cable | Flashing in place, and a wired phone link through the lid. A straight plug doesn't fit next to the L298N (see [step 6](#6-configure-firmware-and-bench-test)). Match the plug to your board (micro-USB or USB-C). | [search](https://www.amazon.com/s?k=90+degree+micro+USB+data+cable+up+down+angle) |
| 1 | USB power blocker / data-only adapter *(only for a wired phone link)* | Stops the phone and the 5 V converter from feeding each other | [search](https://www.amazon.com/s?k=USB+data+only+power+blocker+adapter) |
| 1 | Android phone | Must run the OpenBot app. Check that it fits the holder in its case. | – |
| – | Tools | Temperature-controlled soldering iron with an M3 insert tip, screwdriver/hex keys, multimeter, cutters, wire stripper | [search](https://www.amazon.com/s?k=heat+set+insert+soldering+iron+tip+M3) |

## Assembly

<p align="center">
  <img src="../../docs/images/compact/exploded.jpg" alt="Exploded view" width="45%" />
</p>

### 1. Install inserts and lid nuts

<p align="center">
  <img src="../../docs/images/compact/step_01_shell.jpg" alt="Empty shell" width="40%" />
</p>

Use standard M3 × 5.7 mm heat-set inserts (for example ruthex RX-M3x5.7, 4.6 mm outer diameter) in the 4.0 mm pilot holes. Press each insert in square and flush with a temperature-controlled iron and insert tip. Let it cool before screwing into it.

Install **12 inserts in the chassis**:

- 4 in the L298N bosses
- 4 in the speed-sensor bosses
- 2 in the **front** pair of long-PCB bosses (these hold the ESP32 carrier)
- 2 in the inward-facing bosses above the ultrasonic opening

Install **4 more in the ESP32 carrier's** retaining posts. Leave the two rear long-PCB bosses empty, because only the [alternative PCB](#alternative-integrated-driver-pcb) uses them.

Push one M3 hex nut sideways into each of the four corner lid-lock pockets, with a corner leading and two flats against the pocket walls. Two small ribs at the mouth of each pocket narrow it slightly, so the nut needs a firm push (a flat screwdriver tip helps) and then clicks past them. Once in, it stays put even when the chassis is turned over. If a nut is too loose or won't go in, check that the pocket is free of strings.

### 2. Install motors and speed sensors

<p align="center">
  <img src="../../docs/images/compact/step_02_motors.jpg" alt="Motors installed" width="49%" />
  <img src="../../docs/images/compact/step_03_sensors.jpg" alt="Sensors installed" width="49%" />
</p>

Fit the four TT motors with the wheel shafts through the side openings and the wires facing the centre. Fasten each motor with two M3 × 25 mm screws and nuts. Make sure the screws clear the gearbox internals. **Leave the wheels off** until the bench test is done.

Push an encoder disk onto each inboard shaft. Place a sensor support over each of the four sensor bosses with its flat side toward the motor. The D-shaped socket stops it rotating. Seat the LM393 board between the two upper guides and fasten it with one printed washer and one M3 × 25 mm screw.

Turn each shaft by hand. The disk must pass through the optical slot without rubbing, and the sensor leads must stay clear of it.

### 3. Secure the ultrasonic sensor and panel parts

<p align="center">
  <img src="../../docs/images/compact/ultrasonic_retention.jpg" alt="Ultrasonic retention" width="40%" />
</p>

Seat the HC-SR04 in the front recess with both transducers through the round openings. The recess locates the board, and two clips hold its upper edge from inside so it can't fall back into the chassis.

Fit `ultrasonic_clip_left` on the robot's left and `ultrasonic_clip_right` on the robot's right, each with its small lip over the upper PCB corner. Fix each clip with one M3 × 10 mm screw into its inward-facing insert, screw head toward the inside of the chassis. Tighten until the clip sits on its boss without bending the PCB. The board must not lift or pull out, and the transducers should stay square to the front panel.

Fit the two white front lights, the two red rear lights (each in the printed LED adapter that matches its thread size) and the round rocker switch, using the retaining hardware supplied with them. Route the wires away from the wheels and encoder disks.

### 4. Fit the ESP32 and motor driver

**Flash the firmware first** ([step 6](#6-configure-firmware-and-bench-test)) if you don't have a right-angle USB cable. Once the L298N is in, a straight USB plug no longer fits the ESP32.

Screw the ESP32 carrier to the two front long-PCB bosses with two M3 × 10 mm screws. Lower the ESP32 onto the four 2.2 mm locating pegs through its own mounting holes. No screw passes through the ESP32 PCB.

Put a retaining tab on each of the four carrier posts with its nib facing down. Swing the tab until the nib drops into the ESP32's corner hole, then fasten it with one M3 × 10 mm screw. Any tab fits any corner. The tab bottoms out on its post and leaves about 0.25 mm above the PCB, so tightening can't crush the board. If your board's holes don't line up with the pegs, don't force it.

Put the four 2.4 mm printed spacers on the L298N bosses, set the driver on them, and fix each corner with a printed 0.8 mm washer and an M3 × 10 mm screw (37 × 37 mm pattern). Check that solder joints underneath clear the chassis and that the heatsink has room to breathe.

**Power-board stand.** Strap the 5 V converter to the front of the stand and the divider perfboard to the back, each with two cable ties through the slots. The stand sits on the floor behind the L298N heatsink, between the two rear speed sensors and in front of the switch. The closed lid holds it down, so it needs no screws.

<p align="center">
  <img src="../../docs/images/compact/step_04_electronics.jpg" alt="Electronics installed" width="40%" />
</p>

### 5. Wire power and signals

<p align="center">
  <img src="../../docs/images/compact/wiring_diagram.png" alt="Wiring diagram" width="80%" />
</p>

Use the GPIO numbers printed on the ESP32. All grounds must be connected together. Keep wiring clear of gears, encoder disks, screw tips and the lid seam, and tie it down against vibration.

| Connection | ESP32 GPIO | Notes |
|---|---|---|
| L298N IN1 / IN2 (left motors) | 13 / 12 | |
| L298N IN3 / IN4 (right motors) | 15 / 2 | |
| L298N ENA / ENB | – | Leave the enable jumpers fitted (speed is set by PWM on the IN pins) |
| Encoder front left / front right | 35 / 22 | |
| Encoder rear left / rear right *(optional)* | 34 / 23 | |
| Ultrasonic TRIG | 5 | |
| Ultrasonic ECHO | 18 | through a 10 k (ECHO → GPIO18) / 20 k (GPIO18 → GND) divider |
| Battery voltage | 4 | 30 k from switched battery + to GPIO4, 10 k from GPIO4 to GND |
| Front lights L / R *(optional)* | 19 / 21 | through NPN transistor stages |
| Rear lights L / R *(optional)* | 25 / 26 | through NPN transistor stages, firmware edit needed |

- **Motors:** wire both left motors in parallel to OUT1/OUT2 and both right motors to OUT3/OUT4. If a side turns the wrong way, swap that side's two motor wires.
- **Encoders:** power the LM393 boards from **3.3 V**, not 5 V. Their on-board pull-ups then keep the outputs at a safe level for the ESP32. GPIO34 and GPIO35 are input-only and have no internal pull-ups.
- **Lights:** each lamp is switched by an NPN transistor (1 k base resistor, 100 k base-to-emitter pull-down). Never drive a panel lamp directly from an ESP32 pin.

#### Power

Battery + → protection/BMS → fuse → switch → L298N motor supply (+12 V terminal). The 5 V converter takes the switched battery voltage and feeds the ESP32 5 V/VIN pin and the HC-SR04. Leave the L298N's 5 V terminal unconnected. **Never connect USB and the external 5 V supply at the same time** unless a USB power blocker sits in the cable. Keep the battery disconnected while you assemble.

With option A (2S), the motors see about 6.4 V, which suits common 3–6 V TT motors. With option B or a 3S LiPo, they see about 10.6 V. Use motors rated for that, or limit the speed in the app.

### 6. Configure firmware and bench test

Get the firmware from the [firmware folder](../../firmware/README.md) and open `firmware/openbot/openbot.ino`.

1. Set the robot type and keep phone mode on:

   ```cpp
   #define OPENBOT DIY_ESP32
   #define NO_PHONE_MODE 0
   ```

2. In the `#elif (OPENBOT == DIY_ESP32)` block, **turn off the indicators**. Their default pins (5 and 18) are the sonar pins in this build:

   ```cpp
   #define HAS_INDICATORS 0   // default is 1
   ```

3. In the same block, set the battery limits for your battery option. The divider factor stays as it is.

   | Option | `VOLTAGE_MIN` | `VOLTAGE_LOW` | `VOLTAGE_MAX` |
   |---|---:|---:|---:|
   | A – 2S 18650, or 2S LiPo | 6.0f | 6.8f | 8.4f |
   | B – 3S 18650, or 3S LiPo | 9.0f | 10.2f | 12.6f |

   The app uses these limits for its battery gauge and low-battery warning. The default minimum of 6.0 V would show a 3S pack as usable down to 2 V per cell. The firmware never cuts the motors on low voltage, so the protection board (options A and B) or a LiPo voltage alarm (option C) has to protect the cells.

4. Optional, in the same block:

   ```cpp
   #define HAS_SPEED_SENSORS_BACK 1   // read the rear encoders as well
   #define HAS_LEDS_FRONT 1           // white lights on GPIO19/21 (default pins)
   #define HAS_LEDS_BACK 1            // red lights, and change their pins:
   const int PIN_LED_LB = 25;         // default 5
   const int PIN_LED_RB = 26;         // default 18
   ```

5. In the Arduino IDE, install the **esp32 board package 2.0.17**. The firmware uses `ledcSetup`/`ledcAttachPin`, which were removed in core 3.x. Then select **ESP32 Dev Module** and upload.

**USB access.** The ESP32 sits upside down in its carrier, so its USB socket is underneath the board and faces the L298N's power terminal only about 8.5 mm away. With the driver installed, only a right-angle plug fits, with the cable leaving upward between the two boards. Either flash before fitting the L298N, or use a 90° cable.

GPIO 2, 5, 12 and 15 are boot-strapping pins. If the board won't boot or flash with everything connected, check what is pulling on them, and GPIO12 most of all, since it must be low at reset.

**Bench test** (wheels off, chassis propped up, Serial Monitor at 115200 baud):

| Send | Expect |
|---|---|
| `f` | A feature string that reports `DIY_ESP32` |
| *(watch output)* | A plausible battery voltage and sonar distance |
| `c60,0`, then `c0,0` | Only the left side turns, forward |
| `c0,60`, then `c0,0` | Only the right side turns, forward |

While the wheels turn, check that the encoder counts change. Stop straight away if a disk rubs, a wire gets warm or the ESP32 resets.

Install the **OpenBot Robot** Android app. Choose **Bluetooth**, grant the permissions it asks for, scan for `OpenBot` and connect. For a wired link instead, see the next step.

### 7. Fit the battery, phone holder and lid

<p align="center">
  <img src="../../docs/images/compact/step_06_phone_lid.jpg" alt="Phone holder" width="32%" />
  <img src="../../docs/images/compact/lid_opening.jpg" alt="Lid openings with plug and grommet" width="32%" />
  <img src="../../docs/images/compact/step_07_closed.jpg" alt="Closed body" width="32%" />
</p>

**Phone holder.** The holder tilts, so the camera can also look up, for example at a person standing in front of the robot.

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="Hinge of the tilting phone holder" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="Tilting phone holder at 0, 10, 20 and 30 degrees" width="64%" />
</p>

1. Melt four M3 inserts into the column, the same way as in step 1: one into each end of the pivot hole at the back, and one into each of the two front lock holes, which are open on one side only.
2. Press two M3 nuts into the pockets in the foot. Screw the foot to the forward pad of the lid with two M3 × 25 mm screws from underneath.
3. Set the column between the cheeks with the open lock holes toward the cheek that has five holes. Drive an M3 × 10 mm screw through each cheek into the pivot inserts and tighten them until the column turns without play. Put a drop of medium-strength (blue) threadlocker on both pivot screws so they cannot work loose.
4. Tilt the column back until one of its lock inserts lines up with a cheek hole. The front pair of cheek holes gives 0° (lower) and 20° (upper); the rear pair gives 10° (lower) and 30° (upper). Lock it there with the third M3 × 10 mm screw.
5. Fit the sliding grip and tension it with a rubber band or spring. With the phone in its case, check that it is held firmly, the camera has a clear view and no buttons are pressed.

At 30° the phone still clears the battery box by about 15 mm. If you don't need the tilt, print [phone_mount_bottom](../phone_mount/phone_mount_bottom.stl) instead of the foot and column: press two M3 nuts into its pockets and screw it to the lid pad the same way. It takes the same grip, and the phone sits 10.5 mm lower.

**Lid openings.** The lid has two 1 × 2-stud openings, one in front of and one behind the phone pad. Fit the lid with the phone pad toward the ultrasonic sensor at the front. The pad is 16 mm forward of the lid centre; the rear stud area carries the battery box. Press a `lid_plug` into each unused opening; it restores the studs, and a 1 mm ledge at the bottom of the opening stops it from falling through. Where a cable passes, use the `lid_grommet` instead:

- **Wired phone link:** route a 90° USB cable from the ESP32 up through the front opening to the phone. Put a USB power blocker in the line so the phone and the 5 V converter don't feed each other. In the app, choose **USB** instead of Bluetooth.
- **Battery box (options A and B):** the battery wires come down through the rear opening.

Fit the LiPo tray now if you use option C. Tuck the wires away from the rim, lay the lid flat on the chassis, and drive four M3 × 10 mm screws (no washers) into the corner nuts. Tighten them evenly until seated. The lid must not lift, and the holder must not rock. For options A and B, press the battery box onto the studs behind the phone holder.

Fit the wheels only after the bench test passes. Do the first drive slowly on a clear, level floor, then recheck all screws and the phone grip. Lift the robot by the chassis, not the lid, phone holder or battery box.

## Alternative integrated-driver PCB

<p align="center">
  <img src="../../docs/images/compact/pcb_option.jpg" alt="Alternative PCB footprint" width="40%" />
</p>

The chassis also takes the OpenBot **PCB V2** (Arduino Nano + DRV8871 drivers), which uses a 121.031 × 28.448 mm mounting pattern. This is a different electrical build, **not** an ESP32 drop-in. It runs the `PCB_V2` firmware with the Nano wiring.

- Leave out the ESP32 carrier, its four retainers, the L298N and the power-board stand.
- Put 4 inserts in the four long-PCB bosses. With the 4 sensor and 2 ultrasonic inserts, that is **10 inserts in total**.
- Mount the board on four printed 2.4 mm spacers, with a printed washer and an M3 × 10 mm screw at each hole.
- Hardware totals: **13 × M3 × 10** (4 lid, 4 PCB, 2 clips, 3 phone holder), **14 × M3 × 25**, 14 nuts.

Order the board from the [PCB V2 manufacturing files and BOM](../diy/pcb/v2), for example through [JLCPCB](https://jlcpcb.com/) or [PCBWay](https://www.pcbway.com/). Check the height of the populated components and the solder-joint clearance before you fasten it. The battery options above work the same way with this board.

## Troubleshooting

| Symptom | Check |
|---|---|
| Compile errors about `ledcSetup` / `ledcAttachPin` | The esp32 core is 3.x. Install 2.0.17. |
| USB plug won't go into the ESP32 | The L298N is in the way. Use a 90° plug or remove the driver to flash. |
| ESP32 won't boot or flash once wired | Loading on GPIO12/2/15/5. Disconnect the L298N inputs and retry. |
| ESP32 resets when the motors start | Weak or flat battery, or a 5 V converter that can't regulate from 2S. Check the 5 V rail under load. |
| Sonar always 0 or erratic | `HAS_INDICATORS` is still 1, or the echo divider is missing or wrong |
| No counts from an encoder | Sensor not on 3.3 V, disk not in the slot, or disk not opaque |
| Robot turns when it should go straight | One side's motor wires are swapped, so swap the pair on OUT1/OUT2 or OUT3/OUT4 |
| Battery reading wrong or app shows "low" | Divider must be 30 k (top) / 10 k (bottom), and the voltage limits must match your option |
| Lid won't seat | A wire is trapped on the rim, the LiPo is too thick, or a nut is not fully pushed past the ribs into its pocket |
| Battery box loose on the lid | Printed studs came out undersize. Raise flow slightly or check the printer's dimensional accuracy, then reprint the box |

## Views

| Front | Rear |
|---|---|
| ![Front view](../../docs/images/compact/teaser_front.jpg) | ![Rear view](../../docs/images/compact/teaser_rear.jpg) |
| **Top** | **Underside** |
| ![Top view](../../docs/images/compact/top_view.jpg) | ![Underside](../../docs/images/compact/underside.jpg) |

Renders leave out wiring. Boards, battery holders and the LiPo pack are shown as simplified shapes.

## Next

Install the [OpenBot Robot app](../../android/robot/README.md) on your phone and connect to the robot as described in [step 6](#6-configure-firmware-and-bench-test).
