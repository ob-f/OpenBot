# Slim Body

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Algunas impresoras 3D tienen volúmenes de construcción que son demasiado pequeños para imprimir el cuerpo completo de OpenBot.
Esta carpeta contiene la versión delgada del cuerpo de OpenBot.
Se puede imprimir con una placa de construcción de 220mmx220mm cuando las piezas se giran 45 grados.

![Slim Body](../../../../docs/images/slim_body.jpg)

## Piezas

1) `slim_body_bottom` ([STL](slim_body_bottom.stl), [STEP](slim_body_bottom.step))
2) `slim_body_top` ([STL](slim_body_top.stl), [STEP](slim_body_top.step))

Para que encaje, es posible que tengas que ajustar las siguientes configuraciones para obtener el área máxima de impresión.

- Configura *Tipo de Adhesión de la Placa de Construcción* a "Ninguno" (Brim, Skirt y Raft aumentan el tamaño total de tu impresión)
- Desactiva el blob de imprimación (en la sección *Placa de Construcción*)
- Desactiva el segundo extrusor (si tu impresora tiene uno)

Si tienes un poco más de espacio (223mmx223mm), también puedes imprimir el `slim_body_top_rim` ([STL](slim_body_top_rim.stl), [STEP](slim_body_top_rim.step)). Tiene un borde ligeramente más grande, lo que facilita quitar la parte superior.