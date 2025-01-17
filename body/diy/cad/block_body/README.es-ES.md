# Cuerpo Bloque con Espacio Adicional y Soporte para Lego

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Este chasis de robot proporciona algo de altura adicional para facilitar la inclusión de todos los componentes electrónicos y una parte superior compatible con lego. Esta versión ofrece las mismas capacidades de integridad estructural que el [cuerpo regular](../regular_body/), con características adicionales como espacio extra dentro de la carcasa de OpenBot, una parte superior compatible con lego para jugar y aprender, y un tamaño adecuado para camas de impresión más pequeñas manteniendo los parachoques.

![Block CAD](../../../../docs/images/block_cad.jpg)

## Piezas

Necesitarás imprimir la parte inferior y una de las partes superiores:

- `block_body_bottom`([STL](block_body_bottom.stl), [STEP](block_body_bottom.step)): parte inferior del cuerpo
- `block_body_top`([STL](block_body_top.stl), [STEP](block_body_top.step)): parte superior básica del cuerpo
- `block_body_top_lego`([STL](block_body_top_lego.stl), [STEP](block_body_top_lego.step)): parte superior básica del cuerpo con superficie compatible con lego
- `block_body_top_big`([STL](block_body_top_big.stl), [STEP](block_body_top_big.step)): parte superior grande del cuerpo con volumen adicional para electrónica
- `block_body_top_lego`([STL](block_body_top_big_lego.stl), [STEP](block_body_top_big_lego.step)): parte superior grande del cuerpo con superficie compatible con lego

Para las piezas mencionadas, tu placa de construcción necesita ser al menos de 221x150mm.

## Configuración de Impresión

Para obtener los mejores resultados, recomendamos usar la siguiente configuración de impresión:

- Altura de capa: 0.2mm
- Cantidad de paredes: 3 (más paredes para mejor integridad estructural de superficies más grandes)
- Capas superiores: 5
- Capas inferiores: 4
- Relleno: 25%
- Patrón de relleno: Concéntrico (esta configuración parece ahorrar tiempo y plástico)
- Velocidad de impresión: 50mm/seg
- Generar soporte: Sí
- Patrón de soporte: Concéntrico
- Densidad de soporte: 15%
- Habilitar borde de soporte: Sí
- Tipo de adhesión a la placa de construcción: Ninguno

¡Feliz construcción de robots!

![Block Body](../../../../docs/images/block_body.jpg)