# Python 控制器

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

这个 Python 程序允许你通过（无线）键盘控制机器人，并从摄像头接收视频流。该程序可以在任何连接到与机器人手机相同网络的计算机上运行。它在 Raspberry Pi 3 和 MacBook 上开发和测试。在按照以下步骤操作之前，请确保你已经获取了[源代码](https://github.com/ob-f/OpenBot#get-the-source-code)并导航到 `controller` 文件夹。

## 依赖项

我们建议为 OpenBot 创建一个 conda 环境（如果尚未创建）。有关安装 conda 的说明可以在[这里](https://docs.conda.io/projects/conda/en/latest/user-guide/install/)找到。你可以使用以下命令创建一个新环境：

```bash
conda create -n openbot python=3.7
```

如果你不想全局安装依赖项，请先激活你的 conda 环境：

```bash
conda activate openbot
```

确保你在本地 OpenBot 仓库中的 `controller` 文件夹内。现在，你可以使用以下命令安装所有依赖项：

```bash
pip install -r requirements.txt
```

## 控制机器人

注意：成功连接后，除非重新启动机器人应用，否则可能无法再次连接。

Python 脚本将等待传入连接。在安装了机器人应用的手机上，进入 FreeRoam 碎片并将控制模式切换到手机图标。机器人现在将尝试连接到 Python 脚本（与连接控制器应用的方式相同）。或者，你也可以使用 DefaultActivity 并选择 `Phone` 作为控制器。

### 使用 Pygame

这些脚本允许你像玩赛车游戏一样使用键盘驾驶机器人。

运行不带视频的控制器：

`python keyboard-pygame.py`

运行带视频的控制器：

`python keyboard-pygame.py --video`

以下是使用方法：

```
    W:        前进
    S:        后退
    A:        向左转（驾驶时）
    D:        向右转（驾驶时）
    Q:        左旋转
    E:        右旋转

    M:        驾驶模式
    N:        切换噪声
    Left:     左转向灯
    Right:    右转向灯
    Up:       取消转向灯
    Down:     网络模式
    SPACE:    切换日志记录
    ESC:      退出
```

### 使用 Click

还有一个用于原型设计的脚本，允许以增量设置机器人控制，而不是动态控制。此脚本使用 click 库，需要终端保持焦点。

运行控制器：

`python keyboard-click.py`

以下是使用方法：

```bash
    W:        增加速度
    S:        减少速度
    A:        向左转更多
    D:        向右转更多
    R:        重置控制

    M:        驾驶模式
    N:        切换噪声
    Left:     左转向灯
    Right:    右转向灯
    Up:       取消转向灯
    Down:     网络模式
    SPACE:    切换日志记录
    ESC:      退出
```