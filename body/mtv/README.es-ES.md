# OpenBot: Vehículo Multi-Terreno (MTV)

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Aquí proponemos una alternativa todoterreno al vehículo original [OpenBot](https://www.openbot.org/). Desarrollado en colaboración con **Ivy Tech LTD**, el Vehículo Multi-Terreno (MTV) también se basa en componentes económicos y fácilmente imprimibles. El MTV está inspirado en varios conceptos de vehículos de exploración desarrollados en las últimas décadas para la exploración lunar y marciana. El MTV está diseñado alrededor de un conjunto de 6 ruedas actuadas, organizadas en una configuración Rocker-Bogie. A diferencia de la mayoría de los rovers lunares o marcianos, las ruedas no pueden girar radialmente. Por lo tanto, el vehículo opera de la misma manera que un tanque, lo que hace que el control manual sea bastante intuitivo.

<p align="center">
  <img src="../../docs/images/MTV/MTV.jpg" width="400" alt="App GUI"/>
</p>

## I. Impresión 3D

Necesitarás imprimir las siguientes piezas para construir tu OpenBot MTV.

<p align="center">
  <img src="../../docs/images/MTV/CAD_Assembly.jpg" width="800" alt="App GUI"/>
</p>

<table>
    <thead>
        <tr>
            <th>ID </th>
            <th>Grupo</th>
            <th>Nombre del Ítem</th>
            <th>Cantidad</th>
            <th>Imagen (no a escala)</th>
            <th>Material</th>
            <th>Duración</th>
            <th>Costo</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>A1</td>
            <td rowspan=3>Ensamblaje del motor</td>
            <td>Parte Superior de la Caja del Motor <br> (<a href="cad/MotorAssembly/MotorEnclosure_Top.stl">STL</a>, <a href="cad/MotorAssembly/MotorEnclosure_Top.step">STEP</a>) </td>
            <td>6</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A1.jpg" width="150" alt="App GUI" /></p></td>
            <td rowspan=2>498g</td>
            <td rowspan=2>36h</td>
            <td rowspan=2>€23.51</td>
        </tr>
        <tr>
            <td>A2</td>
            <td>Parte Inferior de la Caja del Motor <br> (<a href="cad/MotorAssembly/MotorEnclosure_Bottom.stl">STL</a>, <a href="cad/MotorAssembly/MotorEnclosure_Bottom.step">STEP</a>) </td>
            <td>6</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A2.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A3</td>
            <td>Soporte del Motor <br> (<a href="cad/MotorAssembly/Motor_Bracket.stl">STL</a>, <a href="cad/MotorAssembly/Motor_Bracket.step">STEP</a>) </td>
            <td>6</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A3.jpg" width="150" alt="App GUI" /></p></td>
            <td>251g</td>
            <td>17.5h</td>
            <td>€11.98</td>
        </tr>
        <tr>
            <td>A4</td>
            <td rowspan=4>Juntas</td>
            <td>Junta de 90 grados <br> (<a href="cad/Joints/90deg_Joint.stl">STL</a>, <a href="cad/Joints/90deg_Joint.step">STEP</a>) </td>
            <td>2</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A4.jpg" width="150" alt="App GUI" /></p></td>
            <td rowspan=4>228g</td>
            <td rowspan=4>20.5h</td>
            <td rowspan=4>€15.05</td>
        </tr>
        <tr>
            <td>A5</td>
            <td>Junta de 100 grados <br> (<a href="cad/Joints/100deg_Joint.stl">STL</a>, <a href="cad/Joints/100deg_Joint.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A5.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A6</td>
            <td>Junta de 100 grados Espejo <br> (<a href="cad/Joints/100deg_Joint_Mirror.stl">STL</a>, <a href="cad/Joints/100deg_Joint_Mirror.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A6.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A7</td>
            <td>Junta Final <br> (<a href="cad/Joints/End_Joint.stl">STL</a>, <a href="cad/Joints/End_Joint.step">STEP</a>) </td>
            <td>2</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A7.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A8</td>
            <td rowspan=3>Patas</td>
            <td>Pata Delantera <br> (<a href="cad/Legs/Front_Leg.stl">STL</a>, <a href="cad/Legs/Front_Leg.step">STEP</a>) </td>
            <td>4</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A8.jpg" width="150" alt="App GUI" /></p></td>
            <td rowspan=3>317g</td>
            <td rowspan=3>22.5h</td>
            <td rowspan=3>€14.97</td>
        </tr>
        <tr>
            <td>A9</td>
            <td>Pata Media <br> (<a href="cad/Legs/Mid_Leg.stl">STL</a>, <a href="cad/Legs/Mid_Leg.step">STEP</a>) </td>
            <td>2</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A9.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A10</td>
            <td>Pata Trasera <br> (<a href="cad/Legs/Rear_Leg.stl">STL</a>, <a href="cad/Legs/Rear_Leg.step">STEP</a>) </td>
            <td>2</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A10.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A11</td>
            <td>Cubierta del Rodamiento</td>
            <td>Cubierta del Rodamiento <br> (<a href="cad/BearingCover/BearingCover.stl">STL</a>, <a href="cad/BearingCover/BearingCover.step">STEP</a>) </td>
            <td>4</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A11.jpg" width="150" alt="App GUI" /></p></td>
            <td>13g</td>
            <td>1.5h</td>
            <td>€0.60</td>
        </tr>
        <tr>
            <td>A12</td>
            <td>Plataforma de Montaje del Teléfono</td>
            <td>Plataforma de Montaje del Teléfono <br> (<a href="cad/PhoneMount/Phone_Mount.stl">STL</a>, <a href="cad/PhoneMount/Phone_Mount.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A12.jpg" width="150" alt="App GUI" /></p></td>
            <td>19g</td>
            <td>2h</td>
            <td>€0.91</td>
        </tr>
        <tr>
            <td>A13</td>
            <td rowspan=4>Parachoques Delantero</td>
            <td>Nombre Delantero <br> (<a href="cad/Buffer/Name_Front.stl">STL</a>, <a href="cad/Buffer/Name_Front.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A13.jpg" width="150" alt="App GUI" /></p></td>
            <td rowspan=4>228g</td>
            <td rowspan=4>20.5h</td>
            <td rowspan=4>€15.04</td>
        </tr>
        <tr>
            <td>A14</td>
            <td>Nombre Trasero <br> (<a href="cad/Buffer/Name_Back.stl">STL</a>, <a href="cad/Buffer/Name_Back.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A14.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A15</td>
            <td>Parachoques Izquierdo <br> (<a href="cad/Buffer/Buffer_Left.stl">STL</a>, <a href="cad/Buffer/Buffer_Left.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A15.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A16</td>
            <td>Parachoques Derecho <br> (<a href="cad/Buffer/Buffer_Right.stl">STL</a>, <a href="cad/Buffer/Buffer_Right.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A16.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A17</td>
            <td rowspan=8>Compartimento </td>
            <td>Parte Trasera del Compartimento <br> (<a href="cad/Compartment/Compartment_Rear.stl">STL</a>, <a href="cad/Compartment/Compartment_Rear.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A17.jpg" width="150" alt="App GUI" /></p></td>
            <td>413g</td>
            <td>32h</td>
            <td>€22.75</td>
        </tr>
        <tr>
            <td>A18</td>
            <td>Parte Delantera del Compartimento <br> (<a href="cad/Compartment/Compartment_Front.stl">STL</a>, <a href="cad/Compartment/Compartment_Front.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A18.jpg" width="150" alt="App GUI" /></p></td>
            <td>316g</td>
            <td>22h</td>
            <td>€17.42</td>
        </tr>
        <tr>
            <td>A19</td>
            <td>Techo Delantero <br> (<a href="cad/Compartment/Roof_Front.stl">STL</a>, <a href="cad/Compartment/Roof_Front.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A19.jpg" width="150" alt="App GUI" /></p></td>
            <td>58g</td>
            <td>5.5h</td>
            <td>€3.19</td>
        </tr>
        <tr>
            <td>A20</td>
            <td>Techo Trasero <br> (<a href="cad/Compartment/Roof_Rear.stl">STL</a>, <a href="cad/Compartment/Roof_Rear.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A20.jpg" width="150" alt="App GUI" /></p></td>
            <td>140g</td>
            <td>13.5h</td>
            <td>€7.73</td>
        </tr>
        <tr>
            <td>A21ab</td>
            <td>Montaje de la Batería P1 & P2 <br> (<a href="cad/Compartment/Compartment_Battery_Mount-P1.stl">STL</a>, <a href="cad/Compartment/Compartment_Battery_Mount-P1.step">STEP</a>)
            <br> (<a href="cad/Compartment/Compartment_Battery_Mount-P2.stl">STL</a>, <a href="cad/Compartment/Compartment_Battery_Mount-P2.step">STEP</a>) </td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A21ab.jpg" width="150" alt="App GUI" /></p></td>
            <td>15g</td>
            <td>1.5h</td>
            <td>€0.80</td>
        </tr>
        <tr>
            <td>A22</td>
            <td>Tapas de los Faros <br> (<a href="cad/Compartment/Headlight_Rear.stl">STL</a>, <a href="cad/Compartment/Headlight_Rear.step">STEP</a>)</td>
            <td>4</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A22.jpg" width="150" alt="App GUI" /></p></td>
            <td rowspan=3>47g</td>
            <td rowspan=3>4h</td>
            <td rowspan=3>€2.58</td>
        </tr>
        <tr>
            <td>A23</td>
            <td>Faro Delantero <br> (<a href="cad/Compartment/Headlight_Front.stl">STL</a>, <a href="cad/Compartment/Headlight_Front.step">STEP</a>)</td>
            <td>4</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A23.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
        <tr>
            <td>A24</td>
            <td>Tapa de Acceso a la Batería <br> (<a href="cad/Compartment/Compartment_Door.stl">STL</a>, <a href="cad/Compartment/Compartment_Door.step">STEP</a>)</td>
            <td>1</td>
            <td><p align="center"> <img src="../../docs/images/MTV/A25.jpg" width="150" alt="App GUI" /></p></td>
        </tr>
    </tbody>
</table>

Para las piezas anteriores, tu placa de construcción necesita ser al menos de 240mmx150mm.

En una Ultimaker S5, logramos buenos resultados con los siguientes ajustes:

- altura de capa: 0.2mm
- grosor de pared: 1.5mm
- densidad de relleno: 20%
- patrón de relleno: cuadrícula
- velocidad de impresión: 80 mm/s
- sin soporte

## II. Ensamblaje Electro-Mecánico

### II.1. Lista de materiales

Nuestro cuerpo de robot se basa en electrónica de hobby fácilmente disponible. Proporcionamos enlaces para Alemania (UE) y los Estados Unidos (EE.UU.) con envío rápido. Si tienes la paciencia de esperar un poco más, también puedes obtener los componentes mucho más baratos en AliExpress (AE). Necesitarás los siguientes componentes.

<table>
    <thead>
        <tr>
            <th>ID </th>
            <th>Descripción</th>
            <th>Imagen</th>
            <th>Proveedor</th>
            <th>Precio Unitario</th>
            <th>Cantidad</th>
            <th>Costo</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>O1</td>
            <td>Motor DC JGB37-520 con encoders - 12V | 178RPM </td>
            <td><p align="center"> <img src="../../docs/images/MTV/O1.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://de.aliexpress.com/item/4001339371886.html?gatewayAdapt=glo2deu&spm=a2g0s.9042311.0.0.1fe54c4dR1WTdj/">AE</a></td>
            <td>$8.93 | €8.12</td>
            <td>6</td>
            <td>$53.58 | €48.72</td>
        </tr>
        <tr>
            <td>O2</td>
            <td>Neumáticos Talon de 2.8" (2 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O2.jpg" width="150" alt="App GUI" /></p></td>
            <td> <a href="https://www.robotshop.com/en/28-talon-tires-pair.html">US</a> | <a href="https://www.robotshop.com/eu/en/28-talon-tires-pair.html">EU</a></td>
            <td>$26.95 | €23.06</td>
            <td>6</td>
            <td>$161.7 | €138.36</td>
        </tr>
        <tr>
            <td>O3</td>
            <td>Cable de 7 hilos 0.5 mm² (5 m)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O3.jpg" width="150" alt="App GUI" /></p></td>
          <td>  <a href="https://www.amazon.com/Heavy-Gauge-Conductor-Trailer-Cable/dp/B01N3Q0YGS/ref=sr_1_16?crid=3SK9Y7DSOR0OL&keywords=caravan+cable+7+wire&qid=1649847035&sprefix=carava+cable+7+wire%2Caps%2C190&sr=8-16">US</a> | <a href="https://www.amazon.de/-/en/1119757-Classic-Control-Protective-Conductor/dp/B08CY2WPM4/ref=sr_1_5?crid=1QGOB5LF0GZYO&keywords=7+adriges+kabel+0%2C5mm%C2%B2&qid=1644173962&sprefix=7+core+cable+0.5mm+%2Caps%2C289&sr=8-5">EU</a></td>
            <td> $25.53 | €20.61</td>
            <td>0.25</td>
            <td>$6.38 | €5.15</td>
        </tr>
        <tr>
            <td>O4</td>
            <td>Canaleta para cables D-Line de 20 mm x 10 mm x 1 m (2 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O4.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/D-Line-Raceway-1D3015W-Electrical-Paintable/dp/B07KFNYR7G/ref=sr_1_10?crid=137L39X59R8AQ&keywords=D-Line%2Bcable%2Bduct&qid=1649851731&refinements=p_36%3A1253503011&rnid=386442011&s=electronics&sprefix=d-line%2Bcable%2Bduct%2Caps%2C409&sr=1-10&th=1">US</a> | <a href="https://www.amazon.de/-/en/D-Line-Micro-Cable-Management-Strip-White/dp/B082WVQXT5/ref=sr_1_fkmr0_1?crid=3CBV1RRPR6K9B&keywords=d-line%2Bmicro%2B%2Bkabelkanal%2B(2%2Bmeter)%2C%2Bselbstklebende%2Bkabelabdeckungen%2C%2Belektrische%2Bkabelf%C3%BChrung%2C%2Bbeliebte%2Bkabelmanagementl%C3%B6sung%2C%2B20%2Bmm%2B(b)%2Bx%2B10%2Bmm%2B(h)%2B-%2B2%2Bx%2B1%2Bm%2Bl%C3%A4nge%2B-%2Bschwarz&qid=1644149200&sprefix=d-line%2Bmicro%2B%2Bcable%2Btrunking%2B2-meter%2Bself-adhesive%2Bcable%2Bcovers%2Belectrical%2Bcable%2Btidy%2Bpopular%2Bcable%2Bmanagement%2Bsolution%2B20mm%2Bw%2Bx%2B10mm%2Bh%2B-%2B2%2Bx%2B1%2Bmeter%2Blengths%2B-%2Bblack%2B%2Caps%2C381&sr=8-1-fkmr0&th=1">EU</a></td>
            <td>$12.00 | €9.99</td>
            <td>1</td>
            <td>$12.00 | €9.99</td>
        </tr>
        <tr>
            <td>O5</td>
            <td>Prensaestopas PG7 de 3~6.5 mm (50 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O5.jpg" width="150" alt="App GUI" /></p></td>
          <td>  <a href="https://www.amazon.com/ARTGEAR-Waterproof-Adjustable-Connector-Protector/dp/B07JH2LPZF/ref=sr_1_11?crid=1UH8URDCXAHJJ&keywords=Cable+Gland+Set+Plastic+Cable+Glands+Adjustable+PG7+Waterproof+Closure+Cable+Screw+Connection+for+Diameter+3.5+mm+-+6.5+mm+Black+Pack+of+50&qid=1649852081&sprefix=cable+gland+set+plastic+cable+glands+adjustable+pg7+waterproof+closure+cable+screw+connection+for+diameter+3.5+mm+-+6.5+mm+black+pack+of+50+%2Caps%2C243&sr=8-11"> US </a> | <a href="https://www.amazon.de/-/en/Plastic-Adjustable-Waterproof-Connection-Diameter/dp/B08Q458H3N/ref=sr_1_fkmr0_1?crid=1H5VCAQKXD2XZ&keywords=pg7+kabelverschraubung%2C+50+st%C3%BCck%2C+3-6%2C5+mm%2C+verstellbar%2C+wasserdicht%2C+kabelverschraubungen%2C+verbindungsst%C3%BCck+mit+dichtungen&qid=1644149525&sprefix=pg7+cable+gland+50+pack+3+6.5mm+adjustable+waterproof+cable+glands+joints+connector+with+gaskets+%2Caps%2C80&sr=8-1-fkmr0">EU</a></td>
            <td>$8.99 | €9.99</td>
            <td>0.5</td>
            <td>$4.49 | €4.99</td>
        </tr>
        <tr>
            <td>O6</td>
            <td>Rodamientos de bolas MR126ZZ de 6x12x4 mm (4 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O6.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://de.aliexpress.com/item/1005001697683913.html?gatewayAdapt=glo2deu&spm=a2g0s.9042311.0.0.1fe54c4dR1WTdj">AE</a></td>
            <td>$5.52 | €3.45</td>
            <td>1</td>
            <td>$5.52 | €3.45</td>
        </tr>
        <tr>
            <td>O7</td>
            <td>Amortiguador de muelle INJORA de 90 mm para coche RC (2 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O7.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://de.aliexpress.com/item/4000309686709.html?gatewayAdapt=glo2deu&spm=a2g0o.productlist.0.0.17b17ca7oMXyhJ&algo_pvid=c7d8ba55-28b2-4d27-97e4-f338994958f7&algo_exp_id=c7d8ba55-28b2-4d27-97e4-f338994958f7-14&pdp_ext_f=%7B%22sku_id%22%3A%2210000001286270094%22%7D&pdp_pi=-1%3B8.3%3B-1%3B-1%40salePrice%3BUSD%3Bsearch-mainSearch">AE</a></td>
            <td>$8.30 | €7.99</td>
            <td>1</td>
            <td>$8.30 | €7.99</td>
        </tr>
        <tr>
            <td>O8</td>
            <td>LED AXSPEED para coche RC 4.2 V-6 V blanco 17 mm (2 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O8.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.aliexpress.com/item/1005003306484898.html?spm=a2g0s.9042311.0.0.d4954c4dpsjiiC">AE</a></td>
            <td>$7.43 | €6.87</td>
            <td>1</td>
            <td>$7.43 | €6.87</td>
        </tr>
        <tr>
            <td>O9</td>
            <td>LED AXSPEED para coche RC 4.2 V-6 V rojo 17 mm (2 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O9.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.aliexpress.com/item/1005003306484898.html?spm=a2g0s.9042311.0.0.d4954c4dpsjiiC">AE</a></td>
            <td>$7.43 | €6.87</td>
            <td>1</td>
            <td>$7.43 | €6.87</td>
        </tr>
        <tr>
            <td>O10</td>
            <td>Aisladores de vibración con espárragos M3 x 8 mm (4 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O10.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/MroMax-Rubber-Vibration-Isolator-Absorber/dp/B07Z76J5N5/ref=sr_1_5?crid=2LSR8ZMHRIL2O&keywords=m3+rubber+mount+shock+absorbers&qid=1649862366&sprefix=keesin+m3+rubber+mounts+shock+absorbers+%2Caps%2C362&sr=8-5">US</a> | <a href="https://www.amazon.de/-/en/gp/product/B076SSPHP6/ref=ppx_yo_dt_b_asin_title_o03_s01?ie=UTF8&psc=1">EU</a></td>
            <td>$8.09 | €9.49</td>
            <td>1</td>
            <td>$8.09 | €9.49</td>
        </tr>
        <tr>
            <td>O11</td>
            <td>Soporte universal para smartphone Zeadio</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O11.jpg" width="150" alt="App GUI" /></p></td>
            <td> <a href="https://www.amazon.com/SharingMoment-Smartphone-Horizontal-Rotatable-Adjustable/dp/B07S8TTH34/ref=sr_1_4?crid=X7XQ9LC110JJ&keywords=Zeadio+Smartphone+Tripod+Adapter%2C+Mobile+Phone+Holder&qid=1649862548&refinements=p_36%3A2491155011&rnid=2491154011&s=wireless&sprefix=zeadio+smartphone+tripod+adapter%2C+mobile+phone+holder+%2Caps%2C577&sr=1-4">US</a> | <a href="https://www.amazon.de/-/en/Zeadio-Smartphone-Tripod-Adapter-Mobile/dp/B06XDYJNSR/ref=sr_1_8?crid=2ZH6V545D45E3&keywords=zeadio%2Buniversal%2Bsmartphone%2Bhalterung&qid=1644150427&sprefix=zeadio%2Buniversal%2Bsmartphone%2Bholder%2Caps%2C104&sr=8-8&th=1">EU</a></td>
            <td>$11.99 | €10.99</td>
            <td>1</td>
            <td>$11.99 | €10.99</td>
        </tr>
        <tr>
            <td>O12</td>
            <td>Voltímetro de coche DC 12-24 V</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O12.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Nilight-Voltmeter-Waterproof-VoltVolt-Motorcycle/dp/B06ZZJ48VQ/ref=sr_1_fkmr1_2?crid=376857DCCJICB&keywords=mini+led+digital+voltmeter+batterietester+12+volt+%2F+24+volt+voltmeter&qid=1649862763&sprefix=mini+led+digital+voltmeter+batterietester+12+volt+%2F+24+volt+voltmeter%2Caps%2C404&sr=8-2-fkmr1">US</a> | <a href="https://www.amazon.de/-/en/Intckwan-Digital-Voltmeter-Waterproof-Motorcycle/dp/B09T5XRYM9/ref=sr_1_5?crid=1GPFOX9O2VX85&keywords=mini+led+digital+voltmeter+batterietester+12+volt+%2F+24+volt+voltmeter&qid=1649862700&sprefix=mini+led+digital+voltmeter+battery+tester+12+volt+%2F+24+volt+voltmeter%2Caps%2C91&sr=8-5">EU</a></td>
            <td>$10.99 | €11.88</td>
            <td>1</td>
            <td>$10.99 | €11.88</td>
        </tr>
        <tr>
            <td>O13</td>
            <td>Mini interruptor basculante (20 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O13.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/DaierTek-250VAC-Rocker-KCD1-101-Plastic/dp/B07S2QJKTX/ref=sr_1_4?keywords=RUNCCI-YUN+20Pcs+Mini+Rocker+Switch&qid=1650638471&sr=8-4">US</a> | <a href="https://www.amazon.de/-/en/RUNCCI-Rocker-Switches-Household-Appliances/dp/B07MW92CW8/ref=sr_1_2?keywords=RUNCCI-YUN+20+St%C3%BCck+Mini-Wippschalter&qid=1650638352&sr=8-2">EU</a></td>
            <td>$15.98 | €9.44</td>
            <td>0.15</td>
            <td>$2.4 | €1.42</td>
        </tr>
        <tr>
            <td>O14</td>
            <td>Cable Micro USB de montaje en panel Duttek</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O14.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Duttek-Angled-Extension-Motorcycle-Dashboard/dp/B08RSGYV1S/ref=sr_1_1?keywords=Duttek+90+Degree+Micro+USB+Panel+Mount+Cable+Micro+USB+Mount+Extension+Cable+Male+to+Female+for+Car+Boat+Motorcycle+Truck+Dashboard+30cm&qid=1650638585&sr=8-1">US</a> | <a href="https://www.amazon.de/-/en/Duttek-Degree-Extension-Motorcycle-Dashboard/dp/B08RSGYV1S/ref=psdc_1365661031_t1_B07ZCZ8NL1">EU</a></td>
            <td>$10.68 | €11.44</td>
            <td>1</td>
            <td>$10.68 | €11.44</td>
        </tr>
        <tr>
            <td>O15</td>
            <td>PCB personalizada</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O15.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.eurocircuits.de/">US</a> | <a href="https://www.eurocircuits.de/">EU</a></td>
            <td>$10.00 | €10.00</td>
            <td>1</td>
            <td>$10.00 | €10.00</td>
        </tr>
        <tr>
            <td>O16</td>
            <td>Controlador de motor DC con escobillas Cytron 30 A 5-30 V (un canal)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O16.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.robotshop.com/en/cytron-30a-5-30v-single-brushed-dc-motor-driver.html">US</a> | <a href="https://www.robotshop.com/eu/en/cytron-30a-5-30v-single-brushed-dc-motor-driver.html">EU </a></td>
            <td>$34.38 | €37.15</td>
            <td>2</td>
            <td>$68.76 | €74.30</td>
        </tr>
        <tr>
            <td>O17</td>
            <td>Convertidor DC-DC LM2596S de 12 V-36 V a 5 V | 2 A (4 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O17.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Adjustable-Converter-Step-Down-Regulator-Stabilizer/dp/B081N6WWJS/ref=sr_1_3?keywords=4+pieces+adjustable+LM2596S+DC-DC+down+converter&qid=1650638822&sr=8-3">US</a> | <a href="https://www.amazon.de/-/en/adjustable-converter-regulator-stabiliser-voltmeter/dp/B081N6WWJS/ref=pd_sbs_6/262-3535180-9041508?pd_rd_w=m3EIw&pf_rd_p=7cf49d79-ae26-401a-94b7-1dec7a725ba4&pf_rd_r=KEN7A8F4YCHEBXB3ZK9B&pd_rd_r=b6829a4e-476a-48dd-8821-a64b9945da1d&pd_rd_wg=m3HQp&pd_rd_i=B081N6WWJS&psc=1">EU</a></td>
            <td>$15.99 | €16.99</td>
            <td>0.25</td>
            <td>$3.99 | €4.25</td>
        </tr>
        <tr>
            <td>O18</td>
            <td>Batería LiPo 4500 mAh 3S-25C 11.1 V en carcasa rígida</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O18.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/HRB-50C-100C-Quadcopter-Helicopter-Airplane/dp/B06XNTHQRZ?ref_=ast_sto_dp&th=1&psc=1">US</a> | <a href="https://www.amazon.de/-/en/Roaring-Battery-Deans-Connection-Aeroplane/dp/B08Z3JYK1X/ref=sr_1_5?crid=3TW2SFFYW1BQY&keywords=4500%2Bmah%2B3s%2B11%2C1v%2B55c%2Blipo&qid=1644164015&sprefix=4500mah%2B3s%2B11.1v%2B55c%2Blipo%2Caps%2C82&sr=8-5&th=1">EU</a></td>
            <td>$46.99 | €29.97</td>
            <td>1</td>
            <td>$46.99 | €29.97</td>
        </tr>
        <tr>
            <td>O19</td>
            <td>Placa de desarrollo AITRIP ESP32-DevKitC (3 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O19.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Development-Microcontroller-Integrated-Antenna-Amplifiers/dp/B09GK74F7N/ref=sr_1_2?crid=2SK0PFZJXONGJ&keywords=3pcs+ESP32+DevKitC+Core&qid=1650638989&sprefix=3pcs+esp32+devkitc+core%2Caps%2C162&sr=8-2">US</a> | <a href="https://www.amazon.de/-/en/DevKitC-Development-ESP32-WROOM-32D-Bluetooth-Arduino/dp/B09LS6SCF7/ref=sr_1_2?crid=8EUG1EBFJ9AX&keywords=aitrip+esp32-devkitc&qid=1644164236&sprefix=aitrip+esp32-devkitc+core%2Caps%2C192&sr=8-2">EU</a></td>
            <td>$19.99 | €21.00</td>
            <td>0.33</td>
            <td>$6.66 | €7.00</td>
        </tr>
        <tr>
            <td>O20</td>
            <td>Bornas para PCB de 5.08 mm (11 x 2 pines + 11 x 4 pines)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O20.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/LuoQiuFa10-4-Pin-5-08mm-Female-Terminal/dp/B093DNBV5D/ref=sr_1_2?crid=1W2RRUJFR1XY2&keywords=Cococity+22+Pairs+5.08+mm+2+Pins+PCB+Screw+Terminal+Connector+Pluggable+%2B+2+Pins+Plug+in+Screw+PCB+Terminal+Block+Plug+Right+Angle+%282+Pin+11+Pairs+4+Pin+11+Pairs%29+300V+10A+%28Green%29&qid=1650639050&sprefix=3pcs+esp32+devkitc+core%2Caps%2C186&sr=8-2">US</a> | <a href="https://www.amazon.de/-/en/Cococity-Pairs-Terminal-Connector-Pluggable/dp/B083GNB8BY/ref=sr_1_7?crid=SJKL0W8ANGUI&keywords=4-polige+leiterplatten-schraubklemmleiste%2C+5%2C08+mm&qid=1644165018&sprefix=4+pin+5.08mm+pitch+pcb+screw+terminal+block%2Caps%2C82&sr=8-7">EU</a></td>
            <td>$7.99 | €11.99</td>
            <td>1</td>
            <td>$7.99 | €11.99</td>
        </tr>
        <tr>
            <td>O21</td>
            <td>Varilla roscada M6 x 1 m (1000 mm) zincada</td>
            <td><p align="center"> <img src="../../docs/images/MTV/O22.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Threaded-Plated-Thread-Length-Threads/dp/B00G3QM076/ref=sr_1_18?crid=1MBLSICIHE3FX&keywords=M6+x+1m+%281000mm%29+Zinc+Plated+Threaded+Rod%2FBar&qid=1650639302&sprefix=m6+x+1m+1000mm+zinc+plated+threaded+rod%2Fbar%2Caps%2C306&sr=8-18">US</a> | <a href="https://www.amazon.de/-/en/Threaded-quality-galvanised-standard-parts/dp/B07X3LX6RX/ref=sr_1_21?crid=3UFFWWONRN5FQ&keywords=gewindestange%2C%2Bverzinkt%2C%2Bm6%2Bx%2B1%2Bm%2B(1000%2Bmm)%2C%2B10%2Bst%C3%BCck&qid=1644165235&sprefix=pack%2Bof%2B10%2Bm6%2Bx%2B1m%2B1000mm%2Bzinc%2Bplated%2Bthreaded%2Brod%2Fbar%2Caps%2C170&sr=8-21&th=1">EU</a></td>
            <td>$22.73 | €4.31</td>
            <td>1</td>
            <td>$22.73 | €4.31</td>
        </tr>
        <tr>
            <td>O23</td>
            <td>Tornillos M4 x 50 mm (30 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M4x50.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/MroMax-M4x50mm-Machine-Stainless-Phillips/dp/B07ZPSWW24/ref=sr_1_2?crid=6FFC31FUFZC&keywords=M4+x+50mm+screws+30pcs&qid=1650639360&sprefix=m4+x+50mm+screws+30pc%2Caps%2C175&sr=8-2">US</a> | <a href="https://www.amazon.de/-/en/M4-50-stainless-cylinder-DIN912/dp/B07YW6Q5VT/ref=sr_1_10?crid=24VLWXHS6RUKJ&keywords=m4x50%2Bmm%2Bsechskant&qid=1644166982&sprefix=m4%2Bx%2B50mm%2Bhex%2B%2Caps%2C94&sr=8-10&th=1">EU</a></td>
            <td>$11.39 | €9.83</td>
            <td>1</td>
            <td>$11.39 | €9.83</td>
        </tr>
        <tr>
            <td>O24</td>
            <td>Tornillos M4 x 40 mm (30 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M4x40.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Uxcell-a15111200ux1682-Cabinet-Handles-Phillips/dp/B019ZESG9I/ref=sr_1_5?crid=3FFNS1NMT4XPE&keywords=m4+x+40mm+screws+30+pcs&qid=1650639393&sprefix=m4+x+40mm+screws+30pcs%2Caps%2C163&sr=8-5">US</a> | <a href="https://www.amazon.de/-/en/M4-50-stainless-cylinder-DIN912/dp/B07YWDJS99/ref=sr_1_10?crid=24VLWXHS6RUKJ&keywords=m4x50%2Bmm%2Bsechskant&qid=1644166982&sprefix=m4%2Bx%2B50mm%2Bhex%2B%2Caps%2C94&sr=8-10&th=1">EU</a></td>
            <td>$10.56 | €9.13</td>
            <td>1</td>
            <td>$10.56 | €9.13</td>
        </tr>
        <tr>
            <td>O25</td>
            <td>Juego de tornillos y tuercas M3 (440 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M3.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Shapenty-100PCS-Stainless-Female-Fastener/dp/B071NLDW56/ref=sr_1_9?crid=I0ZQA6TBI3N9&keywords=M3+screws+and+nuts+sets+%28440+pcs%29&qid=1650639424&sprefix=m3+screws+and+nuts+sets+440+pcs+%2Caps%2C257&sr=8-9">US</a> | <a href="https://www.amazon.de/-/en/pieces-screws-stainless-hexagon-socket/dp/B093GNHWKR/ref=sr_1_3?crid=36NK6MT1K8LSC&keywords=satz+m3+innensechskant&qid=1644166735&s=diy&sprefix=set+m3+hex+socket%2Cdiy%2C87&sr=1-3">EU</a></td>
            <td>$6.49 | €11.99</td>
            <td>1</td>
            <td>$6.49 | €11.99</td>
        </tr>
        <tr>
            <td>O26</td>
            <td>Tornillos M4 x 120 mm (15 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M4x120.jpg" width="150" alt="App GUI" /></p></td>
            <td> <a href="https://www.amazon.com/XunLiu-Socket-Screws-Wrench-Knurled/dp/B07CHJ7ZPP/ref=sr_1_38?crid=1U3GJUWD14XQB&keywords=screw%2BM4%2Bx%2B120mm&qid=1650753655&sprefix=screw%2Bm4%2Bx%2B120mm%2Caps%2C167&sr=8-38&th=1">US</a> | <a href="https://www.amazon.de/-/en/sourcing-Phillips-Furniture-Hanging-External/dp/B08JYCP7TD/ref=sr_1_64?crid=25999O4GLCN83&keywords=m4+x+120+mm&qid=1644165946&sprefix=m4+x+120mm+socket%2Caps%2C109&sr=8-64">EU</a></td>
            <td>$15.29 | €15.64</td>
            <td>1</td>
            <td>$15.29 | €15.64</td>
        </tr>
        <tr>
            <td>O27</td>
            <td>Tornillos M6 x 100 mm (2 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M6.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/MroMax-Machine-Stainless-Phillips-Cabinet/dp/B07YFMN8FP/ref=sr_1_5?crid=3GFYNNIBEO5UA&keywords=M6+x+100mm+screws&qid=1650639588&sprefix=m6+x+100mm+screws+2+pcs+%2Caps%2C161&sr=8-5">US</a> | <a href="https://www.amazon.de/-/en/AG-BOX%C2%AE-Cylinder-Screws-Stainless-Steel/dp/B09N3DNDZK/ref=sr_1_5?crid=1NROTQHUR7F2K&keywords=m6x100+mm&qid=1644171138&sprefix=m6+x+100mm%2Caps%2C90&sr=8-5">EU</a></td>
            <td>$3.84 | €4.18</td>
            <td>1</td>
            <td>$3.84 | €4.18</td>
        </tr>
        <tr>
            <td>O28</td>
            <td>Surtido de tuercas (normales y autoblocantes) y arandelas M3, M4 y M5 (45 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M3M4M5nuts.jpg" width="150" alt="App GUI" /></p></td>
            <td> <a href="https://www.amazon.com/Assortment-AETTL-Stainless-Assorted-Wrenches/dp/B098ND1GW8/ref=sr_1_2?crid=3AFZPJ5SIBJD9&keywords=Assorted+Nuts+%28Full+and+Self+Locking%29+and+Washers+M3%2C+M4+%26+M5+%2845+pcs%29&qid=1650639662&sprefix=assorted+nuts+full+and+self+locking+and+washers+m3%2C+m4+%26+m5+45+pcs+%2Caps%2C226&sr=8-2">US</a> |  <a href="https://www.amazon.de/-/en/Assorted-Locking-Washers-Stainless-Steel/dp/B01CO9S1RI/ref=sr_1_98?crid=P8DDSVM9ZTG1&keywords=m3+m4+muttern+unterlegscheiben&qid=1644172060&sprefix=m3+m4+nuts+washers%2Caps%2C101&sr=8-98">EU</a></td>
            <td>$25.64 | €23.01</td>
            <td>1</td>
            <td>$25.64 | €23.01</td>
        </tr>
        <tr>
            <td>O29</td>
            <td>Juego de tuercas/arandelas M6 (40 uds)</td>
            <td><p align="center"> <img src="../../docs/images/MTV/M6nuts.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Stainless-Washer-Machine-Thread-Assortment/dp/B09HNJBX1G/ref=sr_1_3?crid=1N4R1D32PS8W6&keywords=40-Piece+Nuts%2FWashers+Set+for+M6&qid=1650639826&sprefix=40-piece+nuts%2Fwashers+set+for+m6+threaded+rods+a2+stainless+steel%2Caps%2C227&sr=8-3">US</a> | <a href="https://www.amazon.de/-/en/40-Piece-Washers-Threaded-Stainless-Steel/dp/B01G77C0DY/ref=sr_1_31?crid=EUR5CW3K5BLD&keywords=m6+muttern+und+unterlegscheiben&qid=1644172515&refinements=p_36%3A118557031&rnid=118555031&s=diy&sprefix=m6+nuts+and+washers%2Caps%2C87&sr=1-31">EU</a></td>
            <td>$13.99 | €8.99</td>
            <td>1</td>
            <td>€$13.99 | €8.99</td>
        </tr>
      <tr>
            <td>O30</td>
            <td>Surtido de arandelas planas y de presión de acero inoxidable </td>
            <td><p align="center"> <img src="../../docs/images/MTV/washers2.jpg" width="150" alt="App GUI" /></p></td>
            <td>  <a href="https://www.amazon.com/Split-Washer-Assortment-Stainless-Silver/dp/B08QN3ZR23/ref=sr_1_35?keywords=Stainless+Steel+Flat+and+Spring+Washers+Assortment+Kit&qid=1650753391&sr=8-35">US</a> | <a href="https://www.amazon.de/-/en/300-pieces-Stainless-Washers-Assortment-M2-M3-M4-M5-M6/dp/B07CQX6NPP/ref=sr_1_15?keywords=m4+federscheibe&qid=1650753193&sr=8-15">EU</a></td>
            <td>$12.49 | €10.38</td>
            <td>1</td>
            <td>€$12.49 | €10.38</td>
        </tr>
    </tbody>
</table>

El coste total de los componentes comprados se estima en unos **500 €**, lo que da un coste total de unos **600 €** por MTV.

### II.2. Ensamblaje mecánico

#### II.2.1. Resumen

Este capítulo trata del proceso de montaje del MTV. Ten en cuenta que izquierda y derecha corresponden a babor y estribor del robot, y en este documento se usan indistintamente. Salvo que se indique lo contrario, se supone que todos los tornillos llevan arandelas planas para proteger el plástico impreso en 3D. Además, las arandelas de presión y las tuercas autoblocantes suelen evitar que las tuercas se aflojen por las vibraciones. 

El montaje del MTV se puede dividir en siete pasos principales:
1. montaje de los 6 módulos de motor
2. montaje de las patas
3. montaje del chasis
4. montaje del compartimento principal 
5. montaje del vehículo
6. montaje de la unidad de alimentación, la unidad de control y conexión de los distintos componentes
7. pruebas, programación y uso. 

<p align="center">
  <img src="../../docs/images/MTV/Rover_Assembly.jpg" width="1000" alt="App GUI"/>
</p>

#### II.2.2. Montaje de los módulos de motor

Cada módulo de motor consta de las siguientes piezas:

- 1 x <a href="cad/MotorAssembly/MotorEnclosure_Top.stl">Carcasa superior del motor</a> (impresión 3D) (A1)
- 1 x <a href="cad/MotorAssembly/MotorEnclosure_Bottom.stl">Carcasa inferior del motor</a> (impresión 3D) (A2)
- 1 x <a href="cad/MotorAssembly/Motor_Bracket.stl">Soporte del motor</a> (impresión 3D) (A3)
- 1 x motor DC JGB37-520 con encoders y adaptador de rueda (O1)
- 1 x prensaestopas PG7 (O5)

Para montar un módulo de motor:

1. Empieza conectando al motor el cable de control incluido en el kit (O1). Se recomienda encarecidamente asegurar esta conexión con un poco de cola caliente. 
2. Alinea con cuidado los agujeros de fijación del motor con los de la carcasa del motor impresa en 3D (A1). No hagas fuerza sobre el encoder para no dañarlo. Atornilla el motor a la carcasa impresa en 3D (A1) con los tornillos incluidos en el kit (O1). 
3. Fija el acoplamiento del kit (O1) al eje del motor con la llave Allen incluida. Deja la tuerca que sujeta la rueda al acoplamiento bien enroscada en el buje hexagonal, porque la necesitarás más tarde para montar los neumáticos (O2) en el vehículo.  
4. Encaja el soporte del motor (A3) sobre la carcasa superior del motor (A1). Es un ajuste a presión y puede requerir alinearlo con cuidado al deslizarlo. El resalte del soporte del motor (como se ve en la figura de abajo) debe quedar hacia el lado del eje del motor para un montaje más limpio. Comprueba que el soporte del motor (A3) queda bien ajustado a la carcasa superior (A1). Asegura el conjunto con cola caliente o pegamento instantáneo.  
5. Coloca el prensaestopas (O5) en la carcasa inferior del motor (A2) y pasa el cable del motor por él.
6. Encaja con cuidado la carcasa inferior del motor (A2) en la carcasa superior (A1) hasta que entre del todo. Comprueba que el soporte del motor (A3) queda a ras con la carcasa superior (A1). Ten cuidado de no aprisionar los cables contra el encoder: el encoder tiene que girar libremente. 
7. La carcasa del motor (A2) y el soporte del motor (A3) se sujetan con tornillos M4 x 60 mm (O23) y tuercas autoblocantes (O28)

<p align="center">
  <img src="../../docs/images/MTV/Motor_assemb.jpg" width="800" alt="App GUI"/>
</p>
<p align="center">
  <img src="../../docs/images/MTV/Wheel.jpg" width="400" alt="App GUI"/>
</p>

#### II.2.3. Montaje de las patas

La pata izquierda del MTV consta de las siguientes piezas:

- 1 x <a href="cad/Joints/90deg_Joint.stl">Unión de 90° </a> (impresión 3D) (A4)
- 1 x <a href="cad/Joints/End_Joint.stl">Unión final </a> (impresión 3D) (A7)
- 1 x <a href="cad/BearingCover/BearingCover.stl">Tapa del rodamiento </a> (impresión 3D) (A11)
- 1 x <a href="cad/Joints/100deg_Joint.stl">Unión de 100° </a> (impresión 3D) (A5)
- 2 x <a href="cad/Legs/Front_Leg.stl">Pata delantera </a> (impresión 3D) (A8)
- 1 x <a href="cad/Legs/Mid_Leg.stl">Pata central </a> (impresión 3D) (A9)
- 1 x <a href="cad/Legs/Rear_Leg.stl">Pata trasera </a> (impresión 3D) (A10) 
- 1 x canaleta para cables D-Line (O4)
- 1 x cable de 7 hilos (O3)
- 1 x rodamiento de bolas MR126ZZ (O6)

La pata derecha del MTV consta de las siguientes piezas:

- 1 x <a href="cad/Joints/90deg_Joint.stl">Unión de 90° </a> (impresión 3D) (A4)
- 1 x <a href="cad/Joints/End_Joint.stl">Unión final </a> (impresión 3D) (A7)
- 1 x <a href="cad/BearingCover/BearingCover.stl">Tapa del rodamiento </a> (impresión 3D) (A11)
- 1 x <a href="cad/Joints/100deg_Joint_Mirror.stl">Unión de 100° (simétrica)</a> (impresión 3D) (A6)
- 2 x <a href="cad/Legs/Front_Leg.stl">Pata delantera </a> (impresión 3D) (A8)
- 1 x <a href="cad/Legs/Mid_Leg.stl">Pata central </a> (impresión 3D) (A9)
- 1 x <a href="cad/Legs/Rear_Leg.stl">Pata trasera </a> (impresión 3D) (A10)
- 1 x canaleta para cables D-Line (O4)
- 1 x cable de 7 hilos (O3)
- 1 x rodamiento de bolas MR126ZZ (O6)

Monta la unión de 90° (A4) con sus patas (A8) (2 delanteras + la central) para formar el conjunto de la pata delantera (derecha e izquierda). Las patas se sujetan a la unión con tornillos M4 x 40 mm (O24), arandela plana (O30), arandela de presión (O30) y tuercas (O28). Taladra a M4 los agujeros guía de los conjuntos de patas (la pata central tiene 3 agujeros guía: los 2 de los extremos deben ser M4 y el del centro M3, que se usará para montar los amortiguadores).
Une la unión de 100° de la pata trasera (A5) (o la simétrica A6) y la pata trasera para formar los conjuntos completos de las patas de babor y estribor, y sujeta la pata con tornillos M4 x 40 mm (O24), arandela plana (O30), arandela de presión (O30) y tuercas (O28).  

<p align="center">
  <img src="../../docs/images/MTV/Leg.jpg" width="800" alt="App GUI"/>
</p>

#### II.2.4. Montaje del chasis

Fija los soportes de los motores a los extremos libres de cada pata (2 delanteras y la trasera, en los lados izquierdo y derecho) con tornillos M4 x 40 mm (O24), arandelas planas y de presión (O30) y tuercas (O28). Monta los neumáticos (O2) en el buje hexagonal y sujétalos con la tuerca del buje. Los motores del MTV tienen un encoder magnético integrado para estimar la velocidad y una interfaz de 6 pines: `[Motor power +, Motor power - , Encoder power + , Encoder power - , Encoder data 1, Encoder data 2]`. La conexión eléctrica entre los motores y las PCB de control se hace con el cable de 7 hilos (O3):

<p align="center">
  <img src="../../docs/images/MTV/MotAssembly.jpg" width="800" alt="App GUI"/>
</p>

Completa el montaje del chasis, incluida la canaleta para cables (O4) donde haga falta:

<p align="center">
  <img src="../../docs/images/MTV/Leg2.jpg" width="600" alt="App GUI"/>
</p>

#### II.2.5. Montaje del compartimento principal

El compartimento principal consta de las siguientes piezas:

- 2 x <a href="cad/BearingCover/BearingCover.stl">Tapa del rodamiento </a> (impresión 3D) (A11)
- 1 x <a href="cad/PhoneMount/Phone_Mount.stl">Plataforma del soporte del teléfono </a> (impresión 3D) (A12)
- 1 x <a href="cad/Buffer/Name_Front.stl">Placa de nombre delantera </a> (impresión 3D) (A13)
- 1 x <a href="cad/Buffer/Name_Back.stl">Placa de nombre trasera </a> (impresión 3D) (A14)
- 1 x <a href="cad/Buffer/Buffer_Left.stl">Parachoques izquierdo </a> (impresión 3D) (A15)
- 1 x <a href="cad/Buffer/Buffer_Right.stl">Parachoques derecho </a> (impresión 3D) (A16)
- 1 x <a href="cad/Compartment/Compartment_Rear.stl">Compartimento trasero </a> (impresión 3D) (A17)
- 1 x <a href="cad/Compartment/Compartment_Front.stl">Compartimento delantero </a> (impresión 3D) (A18)
- 1 x <a href="cad/Compartment/Roof_Front.stl">Techo delantero </a> (impresión 3D) (A19)
- 1 x <a href="cad/Compartment/Roof_Rear.stl">Techo trasero </a> (impresión 3D) (A20)
- 1 x <a href="cad/Compartment/Compartment_Battery_Mount-P1.stl">Soporte de batería del compartimento P1 </a> (impresión 3D) (A21a)
- 1 x <a href="cad/Compartment/Compartment_Battery_Mount-P2.stl">Soporte de batería del compartimento P2 </a> (impresión 3D) (A21b)
- 4 x tornillos M4 x 120 mm (O26)
- 6 x prensaestopas PG7 (O5)
- 1 x varilla roscada M6 (O21) 

Une el compartimento trasero (A17) con el delantero (A18) para formar el conjunto del compartimento con tornillos M3 x 15 mm (O25), arandelas planas (O28) y tuercas autoblocantes (O28). Usa los agujeros guía para taladrar los agujeros M3 de la unión.

<p align="center">
  <img src="../../docs/images/MTV/Body.jpg" width="800" alt="App GUI"/>
</p>

Taladra los agujeros de los rodamientos en el chasis, donde están los agujeros guía, mete los rodamientos (O6) en la tapa del rodamiento (A11) y fija el conjunto del rodamiento por fuera del compartimento con 4 tornillos M3 x 30 mm (O25), arandelas de presión (O30), arandelas (O28) y tuercas autoblocantes (O28).

<p align="center">
  <img src="../../docs/images/MTV/Body1.jpg" width="300" alt="App GUI"/>
</p>

Coloca los prensaestopas (O5) en los seis agujeros del compartimento trasero (A17). Por ellos pasan los cables de los motores al compartimento:

<p align="center">
  <img src="../../docs/images/MTV/Body2.jpg" width="800" alt="App GUI"/>
</p>

Fija los parachoques izquierdo y derecho (A15 y A16) al conjunto del compartimento con 4 tornillos M3 x 20 mm (O25), arandelas de presión (O30), arandelas (O28) y tuercas autoblocantes (O28). 

<p align="center">
  <img src="../../docs/images/MTV/Front.jpg" width="800" alt="App GUI"/>
</p>

Fija el soporte de batería P1 y P2 (A21 a y b) al compartimento trasero con tornillos M3 x 15 mm (O25) y tuercas autoblocantes (O28). Mete la varilla roscada M6 (O21) en el conjunto del chasis a través del agujero central de los rodamientos y del soporte de batería P1 y P2. Como la varilla se introduce desde un lado, no olvides poner las tuercas M6 (O29) que van dentro del compartimento.  
- Hay dos tuercas M6 (O29) por fuera del compartimento que hacen de separadores entre el chasis y el conjunto del compartimento. 
- La varilla roscada debe quedar centrada entre la izquierda y la derecha para dejar sitio al montaje del chasis. 
- Ten cuidado al enroscar la varilla en el compartimento para no tensar las paredes laterales.

<p align="center">
  <img src="../../docs/images/MTV/Assembly.jpg" width="800" alt="App GUI"/>
</p>

#### II.2.6. Montaje del vehículo

Fija el conjunto del chasis a la varilla roscada M6 a ambos lados del compartimento y sujétalo con tuercas M6 (O29):

<p align="center">
  <img src="../../docs/images/MTV/MTV_assemb.jpg" width="800" alt="App GUI"/>
</p>

Monta los amortiguadores (O7) entre la pata central y el chasis:

<p align="center">
  <img src="../../docs/images/MTV/Damper.jpg" width="800" alt="App GUI"/>
</p>

Cambia los muelles de fábrica de los amortiguadores por el juego de dureza media incluido (muelles dorados) para que el cuerpo del robot quede nivelado con el suelo. Fija el extremo del pistón al compartimento del robot con el separador y el tornillo que vienen con el amortiguador (O10) y una tuerca autoblocante M3 (O28). Fija el extremo de la varilla con tornillos M3 x 40 (O25) y una tuerca autoblocante (O28). Lleva los cables desde el conjunto de las patas al cuerpo principal y conéctalos según el esquema de cableado (usando también las canaletas para ordenar los cables). Monta el soporte del teléfono en el techo delantero (A19) 
- Fija el soporte universal para smartphone (O11) a la plataforma del soporte del teléfono (A12). 
- Fija los aisladores de vibración (O10) a la plataforma del soporte del teléfono (A12) y sujétala al techo delantero (A19) con tuercas autoblocantes y arandelas, como en la imagen.   

<p align="center">
  <img src="../../docs/images/MTV/Phone.jpg" width="800" alt="App GUI"/>
</p>

Fija el indicador de tensión (O12), los interruptores de alimentación (O13) y el cable Micro USB de montaje en panel (O14) en sus huecos del techo trasero. Saca los faros y las luces traseras (O8 y O9) por los recortes del techo y fija el techo delantero (A19) y el trasero (A20) al conjunto del compartimento. Mete los faros (O8) en el portafaros (A23) y las luces traseras (O9) en el portaluces trasero (A24), y fíjalos en sus huecos del techo delantero (A19) y trasero (A20). Cierra los portaluces con las tapas (A22) como en la figura de abajo y pégalas con pegamento instantáneo. Desliza las placas de nombre personalizadas delantera (A13) y trasera (A14). 

<p align="center">
  <img src="../../docs/images/MTV/LightMount.jpg" width="800" alt="App GUI"/>
</p>

### II.3. Montaje de la electrónica 

Esta parte solo trata de la integración de las placas electrónicas en el vehículo. En el [siguiente enlace](./pcb) encontrarás más detalles sobre la arquitectura y los componentes de las distintas PCB. Las figuras siguientes muestran los prototipos de las PCB: 

<p align="center">
  <img src="../../docs/images/MTV/PCB_4.jpg" width="600" alt="App GUI"/>
</p>

#### II.3.1. Conexión batería – PCB 
La batería LiPo recargable elegida para el MTV se conecta a la PCB de distribución de energía con unos conectores específicos. Un interruptor evita daños por chispas en los conectores de la batería. Como este interruptor va en la tapa del MTV, se pueden usar conectores de empalme para facilitar el mantenimiento:

<p align="center">
  <img src="../../docs/images/MTV/Bat.jpg" width="500" alt="App GUI"/>
</p>

#### II.3.2. Conexión PCB – indicador de tensión 

Un indicador de tensión muestra el estado de la batería. Se conecta a la PCB de distribución de energía.

<p align="center">
  <img src="../../docs/images/MTV/V_disp.jpg" width="500" alt="App GUI"/>
</p>

#### II.3.3. Conexión PCB – luces 

Las luces delanteras y traseras se conectan a la PCB principal del MTV. Un interruptor en la tapa del MTV las controla.  

<p align="center">
  <img src="../../docs/images/MTV/Light.jpg" width="800" alt="App GUI"/>
</p>

<p align="center">
  <img src="../../docs/images/MTV/LightMount.jpg" width="800" alt="App GUI"/>
</p>

#### II.3.4. Montaje de la PCB de distribución de energía

La placa de distribución de energía se monta en la carcasa principal del MTV. Una vez colocada, los cables de los motores se pasan por las ranuras de montaje. Después se fija la PCB principal al MTV como se ve en la figura siguiente:

<p align="center">
  <img src="../../docs/images/MTV/PCB_mount.jpg" width="800" alt="App GUI"/>
</p>

Con la PCB principal en su sitio, se acortan los cables de los motores y se conectan a la placa principal con bornas de tornillo de 2 pines y paso de 5.08 mm. Después vienen las conexiones de las luces. En el último paso se conectan al MTV las salidas de 12 V y 5 V de la PCB de distribución de energía. 

<table>
    <thead>
        <tr>
            <th>PCB </th>
            <th>Nombre impreso en la PCB</th>
            <th>Imagen</th>
            <th>Comentarios </th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=2>PCB de distribución de energía </td>
            <td>
            Mount_1 
            Mount_2
            Mount_3
            Mount_4
            </td>
            <td><p align="center"> <img src="../../docs/images/MTV/Mount_1.jpg" width="150" alt="App GUI" /></p></td>
            <td>Estos agujeros de montaje M3 sirven para fijar la PCB de distribución de energía a los puntos de anclaje del MTV con tornillos y tuercas.  </td>
        </tr>
        <tr>
            <td>
            VC_Mount_1 
            VC_Mount_2
            VC_Mount_3
            VC_Mount_4
            </td>
            <td><p align="center"> <img src="../../docs/images/MTV/VC_Mount_1.jpg" width="150" alt="App GUI" /></p></td>
            <td>Estos agujeros de montaje M3 sirven para fijar el convertidor de tensión a la PCB de distribución de energía con tornillos y tuercas.</td>
        </tr>
        <tr>
            <td rowspan=2>PCB principal </td>
            <td>
            Mount_1 
            Mount_2
            Mount_3
            Mount_4
            </td>
            <td><p align="center"> <img src="../../docs/images/MTV/Mount_2.jpg" width="150" alt="App GUI" /></p></td>
            <td>Estos agujeros de montaje M3 sirven para fijar la PCB principal a los puntos de anclaje del MTV con tornillos y tuercas.</td>
        </tr>
        <tr>
            <td>
            MD_Mount_1 
            MD_Mount_2
            MD_Mount_3
            MD_Mount_4
            </td>
            <td><p align="center"> <img src="../../docs/images/MTV/MD_Mount_4.jpg" width="150" alt="App GUI" /></p></td>
            <td>Estos agujeros de montaje M3 sirven para fijar los controladores de motor a la PCB principal con tornillos y tuercas. </td>
        </tr>
</table>

#### II.3.5. Montaje de los cables del techo del MTV 

Con la PCB principal fijada, los interruptores y el indicador del techo (tapa) del MTV se conectan con conectores de empalme. Además, el puerto Micro USB de programación del ESP32 se conecta a un cable alargador Micro USB acodado a 90° hacia la izquierda (macho a hembra). El extremo hembra del alargador de montaje en panel se fija en la tapa del MTV, así se pueden conectar los cables USB sin quitar la tapa. 

<p align="center">
  <img src="../../docs/images/MTV/Roof_cables.jpg" width="800" alt="App GUI"/>
</p>


### II.4. Aviso de seguridad 

*Se supone que el MTV lo montarán personas con un nivel intermedio o avanzado de experiencia en el montaje de dispositivos mecánicos y electrónicos. Cuida tu propia seguridad al montar y usar el robot. Ni **Ivy Tech LTD** ni **Intel Deutschland GmbH** se hacen responsables de las consecuencias de accidentes causados por negligencia del usuario.*

- Lee el manual con atención antes del montaje.
- Cuidado con los bordes afilados de las piezas impresas en 3D. 
- Mantén una distancia de seguridad con el robot mientras funciona.
- Ten cuidado de no pillarte los dedos entre las uniones y las ruedas del robot.
- No guardes el robot a pleno sol.
- Este producto **no es resistente al agua**. No recomendamos usar el MTV con lluvia o en entornos húmedos. 
- No uses el MTV cerca del fuego ni de fuentes de calor.
- No dejes caer el MTV desde alturas.
- Usa solo las herramientas indicadas o recomendadas para montar el robot.
- No hagas demasiada fuerza sobre tuercas, tornillos o piezas del robot.
- No enciendas el robot en sitios altos para evitar caídas repentinas.
- No dañes la batería LiPo ni la metas en agua. 
- No conectes la batería LiPo ni dejes que toque otros conductores aparte de los terminales indicados.
- Cuando el robot se quede sin carga, se recomienda sacar la batería y meterla en una bolsa de seguridad para LiPo antes de cargarla. Una vez cargada del todo, se puede volver a colocar en el robot como se muestra abajo
<p align="center">
  <img src="../../docs/images/MTV/Lipo_1.jpg" width="400" alt="App GUI" />
</p>

<p align="center">
  <img src="../../docs/images/MTV/Lipo_2.jpg" width="1000" alt="App GUI" />
</p>

## III: Siguiente

Flashea el [firmware de Arduino](../../firmware/README.es-ES.md)
