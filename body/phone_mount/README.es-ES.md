# Soporte de teléfono

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

El soporte de teléfono de dos piezas que usan las carrocerías [DIY](../diy/README.es-ES.md), [RC truck](../rc_truck/README.es-ES.md) y [Compact](../compact/README.es-ES.md). Una pinza deslizante sujeta el teléfono contra un apoyo y se tensa con una goma elástica o un muelle ligero. Una versión inclinable opcional mantiene el teléfono a 0°, 10°, 20° o 30° para que la cámara mire hacia arriba, por ejemplo a una persona de pie delante del robot.

## Piezas

| Pieza | Cant. | Notas |
|---|---:|---|
| `phone_mount_bottom` ([STL](phone_mount_bottom.stl), [STEP](phone_mount_bottom.step)) | 1 | Base y columna; se atornilla a la carrocería con dos tornillos M3 × 25 mm y tuercas |
| `phone_mount_top` ([STL](phone_mount_top.stl), [STEP](phone_mount_top.step)) | 1 | Pinza deslizante; sirve para las dos bases |
| `phone_mount_tilt_base` ([STL](phone_mount_tilt_base.stl), [STEP](phone_mount_tilt_base.step)) | 1 *(opcional)* | Soporte inclinable: pie con dos mejillas de bisagra; sustituye a `phone_mount_bottom` |
| `phone_mount_tilt_column` ([STL](phone_mount_tilt_column.stl), [STEP](phone_mount_tilt_column.step)) | 1 *(opcional)* | Soporte inclinable: columna que gira entre las mejillas |

## Impresión

Los archivos ya están en la orientación de impresión: imprime cada pieza de pie tal cual. PLA con capas de 0,2 mm, 4 perímetros y 30 % de relleno funciona bien. `phone_mount_tilt_base` no necesita soportes. `phone_mount_bottom`, `phone_mount_top` y `phone_mount_tilt_column` solo los necesitan bajo el reborde del saliente de la goma; con soportes de árbol y «Eliminar voladizos pequeños» desactivado se forma ahí un pequeño árbol, que sacas por la parte trasera abierta de la guía. El reborde rodea todo el saliente, así que la goma no puede soltarse aunque esté floja.

## Soporte de teléfono inclinable

<p align="center">
  <img src="../../docs/images/phone_mount/phone_tilt_hinge.jpg" alt="Bisagra del soporte de teléfono inclinable" width="32%" />
  <img src="../../docs/images/phone_mount/phone_tilt_angles.jpg" alt="Soporte de teléfono inclinable a 0, 10, 20 y 30 grados" width="64%" />
</p>

La base inclinable usa los mismos dos agujeros de montaje que la base estándar y encaja en los mismos huecos: la placa superior DIY, el bastidor y el elevador de cámara del RC truck y la tapa Compact. Sus mejillas empiezan 5 mm por encima de la parte inferior del pie, así que no tocan el borde de esos huecos. El teléfono queda 10,5 mm más alto que en el soporte estándar. A 30°, la parte superior de la columna se desplaza unos 22 mm hacia atrás; comprueba que nada de tu carrocería estorba.

Además de los tornillos de montaje, necesitas tres tornillos de cabeza alomada M3 × 10 mm y cuatro insertos roscados M3 (4,6 mm de diámetro exterior, 5,7 mm de largo). Las roscas de latón aguantan aunque cambies el ángulo a menudo.

1. Coloca en caliente cuatro insertos roscados M3 (4,6 mm de diámetro exterior, 5,7 mm de largo) en la columna: uno en cada extremo del agujero del eje, en la parte trasera, y uno en cada uno de los dos agujeros de bloqueo delanteros, que solo están abiertos por un lado.
2. Introduce dos tuercas M3 en el pie y atorníllalo a la carrocería igual que la base estándar.
3. Coloca la columna entre las mejillas con los agujeros de bloqueo abiertos hacia la mejilla que tiene cinco agujeros. Atornilla un tornillo M3 × 10 mm a través de cada mejilla en los insertos del eje y apriétalos hasta que la columna gire sin holgura. Pon una gota de fijador de roscas de resistencia media (azul) en los dos tornillos del eje para que no se aflojen.
4. Inclina la columna hacia atrás hasta que uno de sus insertos de bloqueo coincida con un agujero de la mejilla. El par de agujeros delantero da 0° (abajo) y 20° (arriba); el trasero, 10° (abajo) y 30° (arriba). Bloquéala con el tercer tornillo M3 × 10 mm.
5. Monta `phone_mount_top` y la goma como en el soporte estándar.
