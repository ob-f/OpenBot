# OpenBot: Cuerpo de Camión RC

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Hemos diseñado un cuerpo de robot para camiones/buggies de juguete RC a escala 1:16 ampliamente disponibles (como [este](https://www.amazon.de/dp/B00M3J7DJW) en Amazon).

![RC-Truck-Banner](/docs/images/rc-truck-banner.jpg)

También tenemos un [cuerpo](/body/) genérico diseñado para un robot con ruedas simple que se basa en hardware de hobby de bajo costo y fácilmente disponible. Las instrucciones de construcción para el OpenBot regular se pueden encontrar [aquí](/body/README.es-ES.md).

## Chasis

El chasis del OpenBot RC-Truck consta de dos componentes principales: (a) Un camión de juguete RC a escala 1:16 de tu elección y (b) una serie de piezas diseñadas a medida que proporcionamos y que se pueden imprimir en 3D.

### Camión de juguete RC a escala 1:16

Para construir tu propio OpenBot RC-Truck, necesitarás un camión/buggy de juguete RC a escala 1:16. Proporcionamos enlaces de Amazon a camiones de juguete RC compatibles para Alemania ([EU](https://www.amazon.de/dp/B00M3J7DJW)), ([EU](https://www.amazon.de/dp/B088FGVYNW)) y Estados Unidos ([US](https://www.amazon.com/gp/product/B09C8XMPQ9)) con envío rápido. También se pueden encontrar varios camiones de juguete similares a escala 1:16 en otros minoristas en línea como ebay, Alibaba o AliExpress, a menudo a precios reducidos pero con velocidad de envío lenta.

Independientemente del minorista y la versión del camión de juguete RC que elijas para tu construcción, asegúrate de que sea realmente un camión RC a escala 1:16. Esto es importante porque las piezas impresas en 3D que proporcionamos están actualmente diseñadas para adaptarse solo a camiones a escala 1:16 con algunos ajustes menores (más sobre esto más adelante). A continuación se muestran algunos ejemplos de camiones de juguete RC a escala 1:16 compatibles.

<p float="left">
  <a href="https://www.amazon.de/dp/B00M3J7DJW" target="_blank"> <img src="/docs/images/rc_toy_1.jpg" width="34%" /> &nbsp
  </a>
  <a href="https://www.amazon.com/gp/product/B09C8XMPQ9" target="_blank"> <img src="/docs/images/rc_toy_2.jpg" width="27%" /> &nbsp &nbsp &nbsp &nbsp
  </a>
  <a href="https://www.amazon.de/dp/B088FGVYNW" target="_blank"> <img src="/docs/images/rc_toy_3.jpg" width="27%" />
  </a>
</p>

### Impresión 3D

Necesitarás imprimir las siguientes piezas para construir tu OpenBot RC-Truck.

1) ```main_frame``` ([STL](cad/rc_truck_body/main_frame.stl), [STEP](cad/rc_truck_body/main_frame.step))
2) ```side_cover``` \[x2\] ([STL](cad/rc_truck_body/side_cover.stl), [STEP](cad/rc_truck_body/side_cover.step))
3) ```phone_mount_bottom``` ([STL](../phone_mount/phone_mount_bottom.stl), [STEP](../phone_mount/phone_mount_bottom.step))
4) ```phone_mount_top``` ([STL](../phone_mount/phone_mount_top.stl), [STEP](../phone_mount/phone_mount_top.step))

Nota que \[xN\] indica el número de copias (es decir, N) que necesitas imprimir de una pieza en particular (donde sea aplicable).

Las siguientes piezas son opcionales (pero recomendadas) para hacer tu OpenBot RC-Truck más compacto y estéticamente agradable.

5) ```camera_elevator``` ([STL](cad/rc_truck_body/camera_elevator.stl), [STEP](cad/rc_truck_body/camera_elevator.step))
6) ```electronics_cover``` \[x2\] ([STL](cad/rc_truck_body/electronics_cover.stl), [STEP](cad/rc_truck_body/electronics_cover.step))
7) ```spacer``` \[x4\] ([STL](cad/rc_truck_body/spacer.stl), [STEP](cad/rc_truck_body/spacer.step))
8) ```front_light_spacer``` \[x2\] ([STL](cad/rc_truck_body/front_light_spacer.stl), [STEP](cad/rc_truck_body/front_light_spacer.step))

Para todas las piezas anteriores, tu placa de construcción necesita ser al menos de 260mmx220mm, que es el tamaño de impresión del ```main_frame```.

Dado que muchas impresoras 3D comunes tienen un volumen de construcción más pequeño (generalmente 220mmx220mm), hay dos opciones más que pueden funcionar.
La primera opción es imprimir el ```main_frame``` a 45 grados con material de soporte adicional.
La segunda opción requiere modificar la pieza original ```main_frame```. Recomendamos usar [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) para tales modificaciones CAD (Fusion 360 tiene una licencia académica gratuita de 1 año disponible).
Para esta opción, ponemos a disposición su archivo [STEP](/body/cad/rc_truck_body/main_frame.step), que puedes cortar en dos/tres piezas más pequeñas.
Las sub-piezas resultantes se ajustarán a una placa de construcción estándar (es decir, 220mmx220mm) y se pueden unir después de la impresión.
En el futuro, también podemos lanzar una versión modular del ```main_frame``` aquí. Todas las demás piezas requieren una placa de construcción mínima de 220mmx60mm.

En una Ultimaker S5, logramos buenos resultados con los siguientes ajustes:

- altura de capa: 0.2mm
- grosor de pared: 1.5mm
- densidad de relleno: 20%
- patrón de relleno: cuadrícula
- velocidad de impresión: 80 mm/s
- sin soporte

Pudimos imprimir el chasis con PLA, CPE y ABS. En nuestra experiencia, la impresión no se vio muy afectada por los ajustes de impresión. Sin embargo, si tienes paciencia, imprimir más lento y con una altura de capa más pequeña mejorará la impresión. Además, agregar una estructura de soporte puede mejorar la impresión, pero requerirá trabajo adicional para eliminarla después.

Antes de proceder con la construcción, es posible que necesites limpiar la impresión 3D. Sin embargo, usando los ajustes anteriores, no necesitamos ningún limado o limpieza durante nuestro proceso de construcción. Si es posible, recomendamos usar una combinación de dos colores diferentes (por ejemplo, verde/negro o rojo/negro) para imprimir diferentes partes del mismo OpenBot RC-Truck como se muestra a continuación.

**Consejo:** Haz clic en las imágenes para abrirlas en resolución completa en una nueva pestaña.

<p float="left">
  <img src="/docs/images/3d_print_rc_1.png" width="32%" />
  <img src="/docs/images/3d_print_rc_2.png" width="32%" /> 
  <img src="/docs/images/3d_print_rc_3.png" width="32%" />
</p>

## Ensamblaje

Si bien es posible construir tu OpenBot RC-Truck con un enfoque de bricolaje similar al OpenBot regular (ver componentes e instrucciones de construcción de bricolaje para OpenBot [aquí](/body/README.es-ES.md)), recomendamos usar la [PCB personalizada](/body/pcb) de OpenBot para construir y ensamblar el OpenBot RC-Truck. Esta opción es recomendada si deseas una construcción más limpia o quieres construir múltiples OpenBot RC-Trucks. Una ventaja adicional de usar nuestra [PCB personalizada](/body/pcb) es que puedes usar los mismos componentes para construir y cambiar entre diferentes cuerpos de OpenBot.

### Lista de materiales

El OpenBot RC-Truck se basa principalmente en electrónica de hobby fácilmente disponible. Proporcionamos enlaces de Amazon para Alemania (EU) y Estados Unidos (US) con envío rápido. Si tienes paciencia para esperar un poco más, también puedes obtener los componentes mucho más baratos en AliExpress (AE). Necesitarás los siguientes componentes.

#### Componentes requeridos

- 1x camión/buggy de juguete RC ([EU](https://www.amazon.de/dp/B00M3J7DJW), [EU](https://www.amazon.de/dp/B088FGVYNW), [US](https://www.amazon.com/gp/product/B09C8XMPQ9))
- 1x Arduino Nano ([EU](https://www.amazon.de/dp/B01MS7DUEM), [US](https://www.amazon.com/dp/B00NLAMS9C), [AE](https://www.aliexpress.com/item/32866959979.html))
- 1x OpenBot [PCB personalizada](/body/pcb)
- 1x cable USB OTG ([EU](https://www.amazon.de/gp/product/B075M4CQHZ), [US](https://www.amazon.com/dp/B07LBHKTMM), [AE](https://www.aliexpress.com/item/10000330515850.html))
- 1x resorte o banda elástica ([EU](https://www.amazon.de/gp/product/B01N30EAZO/), [US](https://www.amazon.com/dp/B008RFVWU2), [AE](https://www.aliexpress.com/item/33043769059.html))
- 6x tornillo M3x25 ([EU](https://www.amazon.de/dp/B07KFL3SSV), [US](https://www.amazon.com/dp/B07WJL3P3X), [AE](https://www.aliexpress.com/item/4000173341865.html))
- 6x tuerca M3 ([EU](https://www.amazon.de/dp/B07JMF3KMD), [US](https://www.amazon.com/dp/B071NLDW56), [AE](https://www.aliexpress.com/item/32977174437.html))
- Cables Dupont ([EU](https://www.amazon.de/dp/B07KYHBVR7), [US](https://www.amazon.com/dp/B07GD2BWPY), [AE](https://www.aliexpress.com/item/4000766001685.html))

#### Componentes opcionales

- 1x Sensor ultrasónico ([EU](https://www.amazon.de/dp/B00LSJWRXU), [US](https://www.amazon.com/dp/B0852V181G/), [AE](https://www.aliexpress.com/item/32713522570.html))
- 2x Interruptor de encendido/apagado ([EU](https://www.amazon.de/dp/B07QB22J62), [US](https://www.amazon.com/dp/B01N2U8PK0), [AE](https://www.aliexpress.com/item/1000005699023.html))
- 4x LED naranja de 5mm ([EU](https://www.amazon.de/gp/product/B01NCL0UTQ), [US](https://www.amazon.com/dp/B077XD7MVB), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 4x LED rojo de 5mm ([EU](https://www.amazon.de/dp/B083HN3CLY), [US](https://www.amazon.com/dp/B077X95F7C), [AE](https://www.aliexpress.com/item/4000329069943.html))
- 2x Lámparas LED blancas ([EU](https://www.amazon.de/-/en/gp/product/B06XTQSZDX), [US](https://www.amazon.com/gp/product/B01N2UPAD8), [AE](https://de.aliexpress.com/item/1005002991235830.html))
- Resistor variable para LEDs ([EU](https://www.amazon.de/gp/product/B081TXJJGV), [US](https://www.amazon.com/dp/B0711MB4TL), [AE](https://de.aliexpress.com/item/1005003610664176.html))

### Instrucciones de construcción

**Consejo:** Haz clic en las imágenes para abrirlas en resolución completa en una nueva pestaña.

1. Desmonta el camión de juguete RC. Retira su cubierta superior y desatornilla los cuatro pasadores de montaje de la base como se muestra en las figuras a continuación. Guarda todos los pasadores de montaje y sus respectivos tornillos, ya que los usarás para montar el ```main_frame``` en el cuerpo del RC-Truck después de realizar todo el cableado. Todos los camiones de juguete RC compatibles vienen con dos motores: uno para el acelerador y otro para la dirección, un controlador de velocidad (con un UBEC incorporado de 5-7V) para el motor del acelerador y un paquete de baterías LiPo 2S 7.4V. Desmonta y retira el paquete de baterías de la base del camión y recárgalo con el cargador que vino con el camión. Expon/afloja los conectores de cables para ambos motores, así como la salida del UBEC del controlador de velocidad. En nuestro caso, la salida del UBEC era de 6V.
    <p float="left">
      <img src="/docs/images/rc_truck_disassembly_1.JPG" width="32%" />
      <img src="/docs/images/rc_truck_disassembly_2.JPG" width="32%" /> 
      <img src="/docs/images/rc_truck_disassembly_3.JPG" width="32%" />
    </p>
2. Nota que las dos dimensiones d1 y d2 (como se muestra a continuación) en el ```main_frame``` dependen del modelo del camión de juguete RC utilizado. Diseñamos nuestra pieza ```main_frame``` para [este](https://www.amazon.de/dp/B00M3J7DJW) modelo de camión de juguete RC. Según el camión (a escala 1:16) que uses, es posible que necesites ajustar estas dimensiones ligeramente usando el archivo ```main_frame``` [STEP](/body/cad/rc_truck_body/main_frame.step). Recomendamos usar [Autodesk Fusion 360](https://www.autodesk.com/products/fusion-360/overview) para tales modificaciones CAD (Fusion 360 tiene una licencia académica gratuita de 1 año disponible). Además, ten en cuenta que la pequeña cuña/triángulo en el ```main_frame``` representa la dirección hacia adelante.
    <p float="left">
      <img src="/docs/images/main-frame-dimensions.png" width="32%" />
      <img src="/docs/images/main-frame-direction.png" width="32%" />
    </p>   
3. (Opcional) Instala el interruptor de encendido/apagado para alimentar el robot. Puedes hacer esto simplemente cortando el cable positivo que va del controlador de velocidad a la batería y soldando el interruptor entre las dos partes divididas de este cable. Asegúrate de que los conectores del interruptor estén aislados con tubo termorretráctil o cinta eléctrica y que el cable de alimentación sea lo suficientemente largo para que el interruptor pueda encajar a través de la abertura rectangular en la parte trasera del ```main_frame``` después del ensamblaje (ver la figura a continuación).
    <p float="left">
      <img src="/docs/images/main-frame-switch.png" width="32%" />
      <img src="/docs/images/switch-power.jpg" width="32%" />
    </p>
4. (Opcional) Instala el sensor ultrasónico a través de la parrilla frontal del ```main_frame```. Puedes usar pegamento caliente para mantenerlo en su lugar si es necesario. Empuja suavemente el conector a una posición recta antes de colocarlo en su lugar. Esto hará que sea más fácil acceder al conector después del ensamblaje. Pasa los cables Dupont desde el conector ultrasónico hasta la abertura rectangular en la parte trasera del ```main_frame```.
    <p float="left">
      <img src="/docs/images/install-ultrasonic-1.png" width="32%" />
      <img src="/docs/images/ultrasonic-sensor.jpg" width="32%" />
      <img src="/docs/images/install-ultrasonic-2.png" width="32%" />
    </p>
5. (Opcional) Instala los LEDs naranjas para las señales de los indicadores tanto en la parte delantera como en la trasera del ```main_frame```. Puedes usar pegamento caliente para mantenerlos en su lugar si es necesario. Para cada lado, es decir, izquierdo y derecho, necesitas conectar los LEDs delanteros y traseros en paralelo. Para lograr esto, simplemente conecta sus terminales positivos y negativos juntos respectivamente. Al igual que el cable del sensor ultrasónico, pasa los cables positivos y negativos Dupont desde las señales de los indicadores izquierdo y derecho hasta la abertura rectangular en la parte trasera del ```main_frame``` donde se conectarán a sus respectivos pines de señal de indicador (tanto +ve como -ve) en la PCB.
    <p float="left">
      <img src="/docs/images/insert-leds-orange-1.png" width="32%" />
      <img src="/docs/images/orange-led.jpg" width="32%" />
      <img src="/docs/images/insert-leds-orange-2.png" width="32%" />
    </p>
**Consejo:** Para evitar el desorden y posibles errores de conexión a tierra durante el cableado, se recomienda formar un bucle de tierra unificado para los terminales negativos de todos los LEDs. Esto simplemente significa pasar un cable debajo del ```main_frame``` que conecte todos los terminales negativos de los LEDs. Este bucle de tierra luego se puede conectar al pin de tierra del Arduino Nano usando un solo cable Dupont, que se pasa a la abertura rectangular en la parte trasera del ```main_frame```.

6. (Opcional) Instala las lámparas LED delanteras. Puedes usar pegamento caliente para mantener la base en su lugar y atornillar la lámpara en su respectiva base a través de la abertura frontal en cada lado. Conecta ambas lámparas LED delanteras en paralelo conectando sus terminales positivos y negativos juntos respectivamente. Dado que estas lámparas funcionan a 6V, puedes conectarlas directamente a la salida del UBEC a través de sus terminales positivos. Conecta los terminales negativos al