# PCB Personalizado

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

El PCB personalizado actúa como una placa portadora para el Arduino Nano e integra controladores de motor modernos, el circuito divisor de voltaje y resistencias para los LEDs. El Arduino se conecta simplemente al encabezado de pines y todos los sensores y LEDs se conectan a través de cables Dupont a los conectores correspondientes.

![PCB_2D](../../../docs/images/pcb_2d_v2.png)
![PCB_3D](../../../docs/images/pcb_3d_v2.png)

El PCB más reciente es [versión 2](v2). Aquí están los cambios en comparación con la [versión 1](v1):

- Mover el sensor de velocidad derecho al pin D3 para habilitar la funcionalidad de interrupción
- Añadir LED de encendido para la batería principal
- Actualizar algunos componentes que son más comúnmente disponibles
- Actualizar el divisor de voltaje a 20k/10k para mejor precisión
- Cambiar los conectores del motor a una versión vertical para un acceso más fácil

Si ya has pedido la [versión 1](v1) del PCB ([vista 2D](../../../docs/images/pcb_2d_v1.png), [vista 3D](../../../docs/images/pcb_3d_v1.png)), no te preocupes, funcionará bien. Solo asegúrate de configurar la bandera correcta en el firmware.

El PCB personalizado implica los siguientes pasos:

1) **Pedir el PCB**: Descarga los archivos [Gerber](v2/gerber_v2.zip) y pide el PCB en el proveedor de tu elección. También puedes pedir el PCB directamente en [PCBWay](https://www.pcbway.com/project/shareproject/OpenBot__Turning_Smartphones_into_Robots.html) donde hemos compartido un proyecto para OpenBot.
2) **Pedir los componentes:** Descarga la [BOM](v2/BOM_v2.csv) y pide los componentes en el proveedor de tu elección, por ejemplo [LCSC](https://lcsc.com).
3) **Montaje del PCB:** Puedes montar el PCB tú mismo o hacer que lo monte un proveedor. Para el montaje automatizado necesitarás el [Archivo Centroid](v2/centroid_file_v2.csv). Si pides el PCB en [JLCPCB](https://jlcpcb.com/), puedes usar su servicio de montaje SMT. Entonces solo necesitarás pedir y soldar los componentes de orificio pasante tú mismo. Encontramos que esta es la opción más conveniente, barata y rápida. En la [versión 2](v2) del PCB, hemos actualizado los componentes para asegurarnos de que todos estén disponibles directamente en [JLCPCB](https://jlcpcb.com/).

También puedes encontrar proveedores que te ofrecerán una solución TurnKey que cubra los 3 pasos. Fabricarán el PCB, obtendrán los componentes y montarán el PCB. Esto es muy conveniente y tampoco es muy caro. Sin embargo, los tiempos de entrega suelen ser muy largos (1-3 meses).

Al solicitar una cotización en [PCBWay](https://www.pcbway.com/orderonline.aspx), puedes seleccionar el servicio de montaje después de subir el archivo Gerber.
![Servicio de Montaje](../../../docs/images/assembly_service.jpg)
En el siguiente paso, necesitarás subir la [BOM](v2/BOM_v2.csv) y el [Archivo Centroid](v2/centroid_file_v2.csv). Tu cotización será revisada y actualizada en unos pocos días. Luego puedes optar por proceder con el pago después de revisar el costo y el tiempo de entrega.