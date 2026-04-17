# OpenBot: Ready-To-Run (RTR) Fahrzeuge

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <span>Deutsch</span> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Die Ready-To-Run (RTR) Versionen des OpenBot-Fahrzeugs richten sich an ein Publikum, das nicht die Zeit oder den Willen hat, seinen eigenen Roboter zu bauen. Die RTR-Fahrzeuge kommen mit vollständig integrierter Elektronik, sind bereits auf Firmware-Ebene unterstützt und wurden sowohl aus Software- als auch aus Hardware-Perspektive gründlich getestet. Die RTR-Fahrzeuge sind in zwei verschiedenen Varianten erhältlich, die als "RTR_TT" und "RTR_520" bezeichnet werden. Beide Fahrzeuge basieren auf dem gleichen spritzwassergeschützten ABS-Gehäuse, sind jedoch für unterschiedliche Zwecke gedacht. Während der RTR_TT hauptsächlich für den Innenbereich vorgesehen ist, verfügt der RTR_520 über einen leistungsstärkeren Prozessor, bessere Motoren, stärkere Metallgetriebe und hat auch einen Satz Geländereifen, die sowohl den Innen- als auch den Außenbereich unterstützen.
<p align="center">
  <a> <img src="/docs/images/RTR_TT.jpg" width="35.8%" /> &nbsp
  </a>
  <a> <img src="/docs/images/RTR_520.jpg" width="33%" />
  </a>
</p>

### Bestellung

Die RTR OpenBot-Fahrzeuge können [hier](http://www.openbot.info/) bestellt werden.

## Bau des RTR selbst

Falls Sie Ihren eigenen OpenBot RTR bauen möchten, müssen Sie das Chassis drucken, die Leiterplatten herstellen und die Motoren sowie eine Handyhalterung kaufen.

### 3D-Druck

Falls Sie dennoch Ihren eigenen OpenBot RTR drucken möchten, müssen Sie die folgenden Teile drucken.

1) ```shell_bottom``` ([STL](cad/rtr_bottom.stl), [STEP](cad/rtr_bottom.step))
2) ```shell_top``` ([STL](cad/rtr_top.stl), [STEP](cad/rtr_top.step)) 
3) ```phone_mount``` ([STL](cad/rtr_mount.stl), [STEP](cad/rtr_mount.step))

<p align="center">
  <img src="../../docs/images/rtr_tt_assembly.gif" width="600" alt="App GUI"/>
</p>

### Leiterplatten (PCBs)

Für jede der Leiterplatten gibt es drei Dateien. Die Gerber-Datei enthält die eigentliche Leiterplatte, die BOM (Stückliste) Datei enthält alle Komponenten, die auf die Leiterplatte gelötet werden müssen, und die Centroid-Datei enthält die Koordinaten jeder Komponente für die automatische Leiterplattenmontage. Die Basisplatine enthält die Mehrheit der Komponenten. Es gibt drei Varianten der Basisplatine. Variante A ist eine nackte Platine mit Anschlüssen für externe Motortreiberplatinen und eine externe Mikrocontrollerplatine. Variante B ist eine modulare Platine mit einem Pin-Header für einen externen Mikrocontroller. Variante C ist die vollständig integrierte Basisplatine, die wir den meisten Benutzern empfehlen würden. Die Frontstoßsensorplatine enthält zwei Stoßsensoren, einen Sonarsensor und den USB-Treiber. Es gibt zwei Varianten der Frontstoßsensorplatine, eine mit dem günstigeren CH340G USB-Treiber und eine mit dem zuverlässigeren CP2102N USB-Treiber. Je nachdem, welche Version (TT-Motor oder 520-Motor) Sie bauen möchten, benötigen Sie die folgenden Leiterplatten.

#### TT-Motor

- 1x Basisplatine (Arduino)
- 1x Status-LED-Platine
- 1x Front-/Top-/Back-Stoßsensorplatine
- 4x Geschwindigkeitssensorplatine (Arduino)

#### 520-Motor

- 1x Basisplatine (ESP32)
- 1x Status-LED-Platine
- 1x Front-/Top-/Back-Stoßsensorplatine

#### Platinenreferenz

- Status-LED-Platine ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_Status_LED_Board_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_Status_LED_Board_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_Status_LED_Board_V1.csv))
- Top-Stoßsensorplatine ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BumpSensorTop_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BumpSensorTop_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BumpSensorTop_V1.csv))
- Back-Stoßsensorplatine ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BumpSensorBack_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BumpSensorBack_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BumpSensorBack_V1.csv))
- Front-Stoßsensorplatine (CH340G) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SensorBoardFront_CH340G_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SensorBoardFront_CH340G_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SensorBoardFront_CH340G_V1.csv))
- Front-Stoßsensorplatine (CP2102N) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SensorBoardFront_CP2102N_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SensorBoardFront_CP2102N_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SensorBoardFront_CP2102N_V1.csv))
- Geschwindigkeitssensorplatine (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_SpeedSensor_Arduino_V1.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_SpeedSensor_Arduino_V1.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_SpeedSensor_Arduino_V1.csv))
- Integrierte Basisplatine C (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1C.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1C.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1C.csv))
- Integrierte Basisplatine C (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1C.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1C.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1C.csv))
- Modulare Basisplatine B (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1B.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1B.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1B.csv))
- Modulare Basisplatine B (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1B.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1B.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1B.csv))
- Nackte Basisplatine A (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_Arduino_V1A.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_Arduino_V1A.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_Arduino_V1A.csv))
- Motortreiber DRV8870 Platine (Arduino) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_MotorBoard_Arduino_V1_DRV8870.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_MotorBoard_Arduino_V1_DRV8870.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_MotorBoard_Arduino_V1_DRV8870.csv))
- Nackte Basisplatine A (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_BaseBoard_ESP32_V1A.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_BaseBoard_ESP32_V1A.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_BaseBoard_ESP32_V1A.csv))
- Motortreiber DRV8870 Platine (ESP32) ([gerber](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/Gerber_MotorBoard_ESP32_V1_DRV8870.zip),[bom](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/BOM_MotorBoard_ESP32_V1_DRV8870.csv),[centroid](https://github.com/ob-f/OpenBot/blob/thias15/rtr/body/rtr/pcb/PickAndPlace_MotorBoard_ESP32_V1_DRV8870.csv))

## Weiter

Flashen Sie die [Arduino Firmware](../../firmware/README.md)