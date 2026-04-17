# Python Controller

<p align="center">
  <span>English</span> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

This python program allows you to control the robot from a (wireless) keyboard and receive a video stream from the camera. The program can run on any computer connected to the same network as the robot's phone. It was developed and tested on a Raspberry Pi 3 and a MacBook. Before following the steps below, make sure you have the [source code](https://github.com/ob-f/OpenBot#get-the-source-code) and navigate to the `controller` folder.

## Dependencies

We recommend to create a conda environment for OpenBot (if not already done). Instructions on installing conda can be found [here](https://docs.conda.io/projects/conda/en/latest/user-guide/install/). You can create a new environment with the following command:

```bash
conda create -n openbot python=3.7
```

If you do not want install the dependencies globally, activate your conda environment first:

```bash
conda activate openbot
```

Make sure you are in the folder `controller` within your local OpenBot repository. Now, you can install all the dependencies with the following command:

```bash
pip install -r requirements.txt
```

## Controlling the robot

NOTE: After a successful connection, it may not be possible to connect again unless the robot app is restarted.

The python scripts will wait for an incoming connection. On the phone with the robot app, go to the FreeRoam fragment and toggle the control mode to the phone icon. The robot will now try to connect to the Python script (same way as it would connect to the controller app). Alternatively, you can also use the DefaultActivity and select `Phone` as controller.

### Using Pygame

These scripts allow you to drive the robot using the keyboard similar to a car racing game.

Run the controller without video:

`python keyboard-pygame.py`

Run the controller with video:

`python keyboard-pygame.py --video`

Here is the usage:

```
    W:        Go forward
    S:        Go backward
    A:        Turn slightly left (while driving)
    D:        Turn slightly right (while driving)
    Q:        Rotate left
    E:        Rotate right

    M:        Drive mode
    N:        Toggle noise
    Left:     Left indicator
    Right:    Right indicator
    Up:       Cancel indicators
    Down:     Network mode
    SPACE:    Toggle logging
    ESC:      Quit
```

### Using Click

There is also a script for prototyping that allows setting the robot control in increments rather than controlling it dynamically. This script uses the click library and requires the terminal to stay in focus. 

Run the controller:

`python keyboard-click.py`

Here is the usage:

```bash
    W:        Increase speed
    S:        Decrease speed
    A:        Turn more left
    D:        Turn more right
    R:        Reset controls

    M:        Drive mode
    N:        Toggle noise
    Left:     Left indicator
    Right:    Right indicator
    Up:       Cancel indicators
    Down:     Network mode
    SPACE:    Toggle logging
    ESC:      Quit
```
