# Controlador de Python

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <span>Español</span> |
  <a href="README.ko-KR.md">한국어</a>
</p>

Este programa en Python te permite controlar el robot desde un teclado (inalámbrico) y recibir una transmisión de video desde la cámara. El programa puede ejecutarse en cualquier computadora conectada a la misma red que el teléfono del robot. Fue desarrollado y probado en una Raspberry Pi 3 y un MacBook. Antes de seguir los pasos a continuación, asegúrate de tener el [código fuente](https://github.com/ob-f/OpenBot#get-the-source-code) y navega a la carpeta `controller`.

## Dependencias

Recomendamos crear un entorno conda para OpenBot (si no se ha hecho ya). Las instrucciones para instalar conda se pueden encontrar [aquí](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). Puedes crear un nuevo entorno con el siguiente comando:

```bash
conda create -n openbot python=3.7
```

Si no deseas instalar las dependencias globalmente, activa primero tu entorno conda:

```bash
conda activate openbot
```

Asegúrate de estar en la carpeta `controller` dentro de tu repositorio local de OpenBot. Ahora, puedes instalar todas las dependencias con el siguiente comando:

```bash
pip install -r requirements.txt
```

## Controlando el robot

NOTA: Después de una conexión exitosa, puede que no sea posible conectar de nuevo a menos que la aplicación del robot se reinicie.

Los scripts en Python esperarán una conexión entrante. En el teléfono con la aplicación del robot, ve al fragmento FreeRoam y cambia el modo de control al ícono del teléfono. El robot ahora intentará conectarse al script de Python (de la misma manera que se conectaría a la aplicación del controlador). Alternativamente, también puedes usar DefaultActivity y seleccionar `Phone` como controlador.

### Usando Pygame

Estos scripts te permiten conducir el robot usando el teclado de manera similar a un juego de carreras de autos.

Ejecuta el controlador sin video:

`python keyboard-pygame.py`

Ejecuta el controlador con video:

`python keyboard-pygame.py --video`

Aquí está el uso:

```
    W:        Avanzar
    S:        Retroceder
    A:        Girar ligeramente a la izquierda (mientras conduces)
    D:        Girar ligeramente a la derecha (mientras conduces)
    Q:        Rotar a la izquierda
    E:        Rotar a la derecha

    M:        Modo de conducción
    N:        Alternar ruido
    Left:     Indicador izquierdo
    Right:    Indicador derecho
    Up:       Cancelar indicadores
    Down:     Modo de red
    SPACE:    Alternar registro
    ESC:      Salir
```

### Usando Click

También hay un script para prototipos que permite configurar el control del robot en incrementos en lugar de controlarlo dinámicamente. Este script usa la biblioteca click y requiere que el terminal permanezca en foco.

Ejecuta el controlador:

`python keyboard-click.py`

Aquí está el uso:

```bash
    W:        Aumentar velocidad
    S:        Disminuir velocidad
    A:        Girar más a la izquierda
    D:        Girar más a la derecha
    R:        Restablecer controles

    M:        Modo de conducción
    N:        Alternar ruido
    Left:     Indicador izquierdo
    Right:    Indicador derecho
    Up:       Cancelar indicadores
    Down:     Modo de red
    SPACE:    Alternar registro
    ESC:      Salir
```