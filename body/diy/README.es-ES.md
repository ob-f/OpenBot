# OpenBot DIY

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Hemos diseñado un cuerpo para un robot con ruedas que se basa en hardware de hobby de bajo costo y fácilmente disponible. Puedes encontrar instrucciones para construir tu propio robot a continuación. Si tienes alguna pregunta o inquietud adicional, no dudes en contactarnos. ¡Feliz construcción de robots!

## Chasis

### Impresión 3D

Necesitarás imprimir y ensamblar las siguientes piezas para construir tu OpenBot.

#### Cuerpo del robot

Hay varias opciones para el cuerpo del robot dependiendo de tus necesidades y capacidades de tu impresora 3D. Te animamos a diseñar y construir el tuyo propio, pero aquí hay algunas opciones como punto de partida:

- [Cuerpo regular](cad/regular_body/README.es-ES.md): Este es el cuerpo estándar que diseñamos; requiere una placa de construcción de al menos 240mmx150mm.
- [Cuerpo delgado](cad/slim_body/README.es-ES.md): Dado que muchas impresoras 3D comunes tienen un volumen de construcción más pequeño, también hemos diseñado una versión más pequeña sin parachoques que cabe en una placa de construcción de 220mmx220mm a 45 grados.
- [Cuerpo pegable](cad/glue_body/README.es-ES.md): Para imprimir en impresoras 3D con volúmenes de construcción aún más pequeños, también hay un cuerpo modular diseñado por @sloretz con varias partes que necesitan ser pegadas; cabe en una placa de construcción de 150mmx140mm.
- [Cuerpo bloque](cad/block_body/README.es-ES.md): Este cuerpo diseñado por @Christos-P proporciona múltiples variantes con opciones para espacio extra dentro de la carcasa y una parte superior compatible con lego, manteniendo una huella pequeña que requiere solo 221mmx150mm para imprimir.

#### Montura para el teléfono

Además, necesitarás imprimir una montura para el teléfono que se fijará al cuerpo del robot.

- phone_mount_bottom ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
- phone_mount_top ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

#### Limpieza

Antes de proceder con la construcción, es posible que necesites limpiar la impresión 3D.
<p float="left">
  <img src="../../docs/images/clean_3d_print_1.jpg" width="32%" />
  <img src="../../docs/images/clean_3d_print_2.jpg" width="32%" /> 
  <img src="../../docs/images/clean_3d_print_3.jpg" width="32%" />
</p>

### Alternativas

Si no tienes acceso a una impresora 3D, hay varios kits de coches robot Arduino disponibles que puedes usar como punto de partida. Estos kits vienen con un chasis, motores y accesorios. Recomendamos obtener un kit básico, ya que no necesitarás muchos de los componentes electrónicos y sensores de los kits más caros. Aquí hay algunas opciones:

- Perseids DIY Robot Smart Car Chassis Kit ([EU](https://www.amazon.de/dp/B07DNXBNHY), [US](https://www.amazon.com/dp/B07DNXBFQN))
- SZDoit 4WD Smart Metal Robot Car Chassis Kit ([US](https://www.amazon.com/dp/B083K4RKBP), [AE](https://www.aliexpress.com/item/33048227237.html))
- Joy-it Robot Car Kit 01 ([EU](https://www.amazon.de/dp/B073ZGJF28))
- Smart Car Kit 4WD Smart Robot Car Chassis Kit ([AE](https://www.aliexpress.com/item/4001238626191.html))

También necesitarás una montura para el teléfono. Aquí hay algunas opciones:

- Montura para teléfono ([EU](https://www.amazon.de/dp/B06XDYJNSR), [US](https://www.amazon.com/dp/B09CY8MC2R))

También puedes ser creativo y construir tu propio chasis y montura para el teléfono utilizando un material de tu elección (por ejemplo, madera, cartón, espuma de poliestireno, etc.). Si lo haces, por favor publica algunas fotos en el [canal de Slack](https://github.com/ob-f/OpenBot#contact) para que otros puedan admirar tu creatividad. Aquí hay un ejemplo de [@custom-build-robots](https://custom-build-robots.com/roboter/openbot-dein-smartphone-steuert-ein-roboter-auto-chassis-bauen/13636):

<p float="left">
  <img src="../../docs/images/chassis_cardboard_1.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_2.jpg" width="32%" />
  <img src="../../docs/images/chassis_cardboard_3.jpg" width="32%" />
</p>

## Ensamblaje

Hay dos opciones diferentes para el ensamblaje del robot, DIY y PCB. El enfoque DIY se basa en el popular controlador de motor L298N y se recomienda para aficionados con algo de experiencia en electrónica. Requiere una cantidad considerable de cableado, especialmente si se instalan todos los sensores y LEDs. Sin embargo, todos los componentes están fácilmente disponibles en la mayoría de los países y, especialmente para construcciones individuales o simplemente para probar el proyecto, se recomienda la opción DIY. Para reducir el cableado y facilitar el ensamblaje, también hemos desarrollado una [PCB personalizada](pcb). Esto se recomienda si deseas una construcción más limpia o quieres construir múltiples OpenBots.

### Lista de materiales

Nuestro cuerpo de robot se basa en electrónica de hobby fácilmente disponible. Proporcionamos enlaces para Alemania (EU) y los Estados Unidos (US) con envío rápido. Si tienes la paciencia de esperar un poco más, también puedes obtener los componentes mucho más baratos en AliExpress (AE). Necesitarás los siguientes componentes.

#### Componentes requeridos

- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 4x Motores TT con neumáticos ([EU](https://www.conrad.de/de/p/joy-it-com-motor01-getriebemotor-gelb-schwarz-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspbe-1573543.html), [US](https://www.amazon.com/dp/B081YQM55P), [AE](https://www.aliexpress.com/item/4000126948489.html))
- 3x Batería 18650 ([EU](https://www.conrad.de/de/p/conrad-energy-18650-usb-spezial-akku-18650-li-ion-3-7-v-1400-mah-1525536.html), [US](https://www.amazon.com/dp/B083K4XSKG), [AE](https://www.aliexpress.com/item/32352434845.html))
- 1x Soporte para batería 18650 ([EU](https://www.amazon.de/dp/B075V25QJ9), [US](https://www.amazon.com/dp/B07DWQYD7H), [AE](https://www.aliexpress.com/item/33037738446.html))
- 1x Cable USB OTG ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x Resorte o banda elástica ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 16x Tornillo M3x25 ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 16x Tuerca M3 ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- 6x Tornillo M3x5 ([EU](https://www.amazon.de/dp/B01HBRG3W8), [US](https://www.amazon.com/dp/B07MBHMLL2), [AE](https://www.aliexpress.com/item/32892594230.html))
- Cables Dupont ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Componentes opcionales

- 2 x Sensor de velocidad ([EU](https://www.conrad.de/de/p/joy-it-sen-speed-erweiterungsmodul-passend-fuer-einplatinen-computer-arduino-banana-pi-cubieboard-raspberry-pi-pc-1646891.html), [US](https://www.amazon.com/dp/B081W2TY6Q), [AE](https://www.aliexpress.com/i/32850602744.html))
- 1x Sensor ultrasónico ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 1x Interruptor de encendido/apagado ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 2x LED naranja de 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 1x Pantalla OLED ([EU](https://www.amazon.de/dp/B079H2C7WH), [US](https://www.amazon.com/dp/B085NHM5TC), [AE](https://www.aliexpress.com/item/4001268387467.html))

#### Componentes DIY (Opción 1)

- 1x Controlador de motor L298N ([EU](https://www.conrad.de/de/p/joy-it-motormodul-2-u-4-phasen-6-bis-12v-1573541.html), [US](https://www.amazon.com/dp/B085XSLKFQ), [AE](https://www.aliexpress.com/item/32994608743.html))
- (Opcional) Resistencias (2x 150<span>&#8486;</span> para los LEDs y una de 20 k<span>&#8486;</span> y 10k<span>&#8486;</span> para el divisor de voltaje)
- (Combo) 4x Motores TT & neumáticos + 2x L298N + cables Dupont ([US](https://www.amazon.com/dp/B07ZT619TD))
- (Combo) 4x Motores TT & neumáticos + cables + tornillos ([US](https://www.amazon.com/dp/B07DRGTCTP))

#### Componentes PCB (Opción 2)

- 1x [PCB personalizada](pcb)
- 5x Cable Micro JST PH 2.0 ([EU](https://www.amazon.de/gp/product/B07449V33P), [US](https://www.amazon.com/dp/B09JZC28DP), [AE](https://www.aliexpress.com/item/32963304134.html))

### Instrucciones de construcción

**Consejo:** Haz clic en las imágenes para abrirlas en resolución completa en una nueva pestaña.

#### Opción 1: DIY

<p float="left">
  <img src="../../docs/images/diy_parts.jpg" height="300" />
  <img src="../../docs/images/wiring_diagram.png" height="300" /> 
</p>

**Consejo:** Para facilitar todo el cableado, puedes construir un pequeño distribuidor de energía para las conexiones de 5V y GND soldando un encabezado macho de 6x2 a una placa de pruebas. Luego conecta el distribuidor de energía con el 5V / GND del controlador de motor.

1. Suelda cables a los motores y agrega los discos codificadores a los dos motores frontales si planeas usar los sensores de velocidad.
    <p float="left">
      <img src="../../docs/images/add_wires_motor.jpg" width="32%" />
      <img src="../../docs/images/add_disk_motor.jpg" width="32%" /> 
    </p>
2. Inserta los cables positivo y negativo de los dos motores izquierdos en OUT1 (+) y OUT2 (-) de la placa L298N. Inserta los cables positivo y negativo de los dos motores derechos en OUT4 (+) y OUT3 (-) de la placa L298N.
3. Monta los motores con ocho tornillos y tuercas M3x25.
    <p float="left">
      <img src="../../docs/images/attach_motors_1.jpg" width="32%" />
      <img src="../../docs/images/attach_motors_2.jpg" width="32%" /> 
      <img src="../../docs/images/attach_motors_3.jpg" width="32%" />
    </p>
4. Monta la L298N con cuatro tornillos M3x5.
5. (Opcional) Instala el sensor ultrasónico y reemplaza el conector en ángulo con uno recto (o dobla cuidadosamente los pines).
    <p float="left">
      <img src="../../docs/images/sonar_front.jpg" width="32%" />
      <img src="../../docs/images/sonar_back.jpg" width="32%" /> 
      <img src="../../docs/images/sonar_bend_pins.jpg" width="32%" />
    </p>
6. (Opcional) Instala los LEDs naranjas para las señales de indicador.
    <p float="left">
      <img src="../../docs/images/led_insert.jpg" width="32%" />
      <img src="../../docs/images/led_left.jpg" width="32%" /> 
      <img src="../../docs/images/led_right.jpg" width="32%" />
    </p>
7. Monta la parte inferior de la montura del teléfono en la placa superior usando dos tornillos y tuercas M3x25.
    <p float="left">
      <img src="../../docs/images/install_camera_mount_1.jpg" width="32%" />
      <img src="../../docs/images/install_camera_mount_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_camera_mount_3.jpg" width="32%" />
    </p>
8. Inserta la parte superior de la montura del teléfono e instala el resorte o la banda elástica.
    <p float="left">
      <img src="../../docs/images/install_spring_1.jpg" width="32%" />
      <img src="../../docs/images/install_spring_2.jpg" width="32%" /> 
    </p>
9. Reemplaza el conector en ángulo con uno recto (o dobla cuidadosamente los pines) y luego monta los sensores de velocidad con un tornillo M3x5 cada uno.
    <p float="left">
      <img src="../../docs/images/install_speed_sensor_1.jpg" width="32%" />
      <img src="../../docs/images/install_speed_sensor_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_speed_sensor_3.jpg" width="32%" />
    </p>
10. Instala el soporte de la batería (por ejemplo, velcro).
    <p float="left">
      <img src="../../docs/images/install_battery_1.jpg" width="32%" />
      <img src="../../docs/images/install_battery_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_battery_3.jpg" width="32%" />
    </p>
11. (Opcional) Inserta el interruptor de encendido/apagado y colócalo en el camino de la corriente.
    1. Empuja el interruptor en la abertura correspondiente hasta que escuches un clic.
    2. Suelda los cables rojos (12V) del soporte de la batería y el cable de alimentación a cada uno de los pines del interruptor. Conecta los cables negros (GND) y cubre la conexión con un poco de termorretráctil.
    3. Fija los cables con un poco de cinta.
    <p float="left">
      <img src="../../docs/images/install_switch_1.jpg" width="32%" />
      <img src="../../docs/images/install_switch_2.jpg" width="32%" /> 
      <img src="../../docs/images/install_switch_3.jpg" width="32%" />
    </p>
12. (Opcional) Adjunta la pantalla OLED.
13. Conecta las entradas PWM del L298N a los pines D5, D6, D9 y D10 del Arduino.
14. Conecta los sensores de velocidad y el sensor ultrasónico a 5V y GND.
15. Conecta el pin D0 de los sensores de velocidad a los pines D2 (izquierda) y D3 (derecha) del Arduino.
16. Conecta los pines Echo y Trigger del sensor ultrasónico a los pines D11 y D12 del Arduino respectivamente.
17. (Opcional) Conecta los LEDs a los pines D4 (izquierda) y D7 (derecha) del Arduino y GND. Recomendamos agregar una resistencia de 150 Ohm en serie para limitar la corriente.
18. (Opcional) Conecta el divisor de voltaje al pin A7 del Arduino. Se utiliza para medir el voltaje de la batería.
19. (Opcional) Conecta la pantalla OLED (chip SSD1306) a través del bus I2C al Arduino Nano.
    1. Conecta los pines VIN y GND de la pantalla a 5V y GND.
    2. Conecta el pin SCL de la pantalla al pin A5.
   