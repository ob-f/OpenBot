# 驾驶策略（高级）

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

## 免责声明

1. **安全性：** 驾驶策略并不完美，可能会导致机器人发生碰撞。请确保在安全的环境中操作！请记住，您的手机可能会在碰撞中受损！确保始终连接游戏控制器，并熟悉按键映射，以便随时停止车辆。风险自负！
2. **计算硬件：** 训练驾驶策略需要大量资源，可能会减慢甚至冻结您的机器。建议使用高端笔记本电脑或工作站，具有大量RAM和专用GPU，尤其是在使用较大批量大小进行训练时。文档目前也不太详细。风险自负！
3. **需要耐心：** 要为您的自定义数据集获得良好的驾驶策略需要一些耐心。这并不是一个简单的过程，涉及数据收集、超参数调整等。如果您从未训练过机器学习模型，这将是一个挑战，甚至可能令人沮丧。

您首先需要设置您的训练环境。

## 依赖项

我们建议为OpenBot创建一个conda环境。关于安装conda的说明可以在[这里](https://docs.conda.io/projects/conda/en/latest/user-guide/install/)找到。创建包含所有依赖项的新环境的最简单方法是使用提供的环境文件之一。在Windows上，您还需要安装[Microsoft C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/)。确保您在本地OpenBot存储库中的`policy`文件夹中。根据您的操作系统，运行相应的命令：

- **MacOS**: `conda env create -f environment_mac.yml`
- **Windows**: `conda env create -f environment_win.yml`
- **Linux**: `conda env create -f environment_linux.yml`

为了支持GPU，请确保您也安装了适当的驱动程序。在Mac和Windows上，一切都应该开箱即用。在Linux上，您可以使用以下命令安装驱动程序：
```
sudo apt-get install nvidia-driver-510
```
在Linux上，您可能还需要运行以下命令将cuda和cudnn添加到路径中：
```
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```

完成！您已准备好训练自己的模型。如果这对您不起作用，下面是手动设置此类环境的说明。

### 手动环境设置

首先使用以下命令创建一个新的conda环境：

```bash
conda create -n openbot pip python=3.9 -y
```

接下来，您需要激活您的conda环境：

```bash
conda activate openbot
```

如果这不起作用（例如在Windows上），您可能需要使用`activate openbot`来激活环境。

一旦环境激活，您需要安装tensorflow。请注意，在笔记本电脑上训练速度会非常慢。因此，如果您可以访问具有专用GPU的计算机，我们强烈建议通过安装必要的库来使用它；确保您安装了最新的GPU驱动程序。以下是为不同操作系统安装tensorflow的命令。

#### **Mac OS**
```
conda install -c apple tensorflow-deps -y
pip install tensorflow-macos~=2.9.0
```
GPU支持
```
pip install tensorflow-metal~=0.5.0
```
[故障排除](https://developer.apple.com/metal/tensorflow-plugin/)

#### **Linux**
```
pip install tensorflow~=2.9.0
```
GPU支持
```
sudo apt-get install nvidia-driver-510
conda install -c conda-forge cudatoolkit=11.2 cudnn=8.1 -y
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```
[故障排除](https://www.tensorflow.org/install/pip#linux)

#### **Windows**
```
pip install tensorflow~=2.9.0
```
GPU支持
```
conda install cudatoolkit=11.3 cudnn=8.2 -y
```

#### **附加要求**

确保您在本地OpenBot存储库中的`policy`文件夹中。现在，您可以使用以下命令安装所有剩余的依赖项：

```bash
pip install -r requirements.txt
```

如果您想要可视化网络架构，您还可以安装pydot（`pip install pydot`）和graphviz（[参见说明](https://graphviz.gitlab.io/download/)）。

如果您想使用[WebApp](#web-app)进行数据收集和训练，您还需要安装以下依赖项。（在Mac上，`brotlipy`包在pip上当前有问题，因此您需要首先使用conda安装它：`conda install brotlipy=0.7`）

```bash
pip install -r requirements_web.txt
```

### 必要的软件包

为了参考和故障排除，以下是必要软件包的列表。

训练：

- [tensorflow](https://pypi.org/project/tensorflow/)
- [jupyter notebook](https://pypi.org/project/notebook/)
- [matplotlib](https://pypi.org/project/matplotlib/)
- [numpy](https://pypi.org/project/numpy/)
- [PIL](https://pypi.org/project/Pillow/)
- [black[jupyter]](https://pypi.org/project/black/)

Web界面：

- [aiohttp](https://pypi.org/project/aiohttp/)
- [aiozeroconf](https://pypi.org/project/aiozeroconf/)
- [imageio](https://pypi.org/project/imageio/)

### 注意事项

- 记得在终端运行命令之前激活环境：`conda activate openbot`
- 如果您的tensorflow导入不起作用，请尝试通过`pip install tensorflow --user`安装。（参见这个[问题](https://github.com/ob-f/OpenBot/issues/98)。）

## 数据集

### 数据收集

为了训练一个自动驾驶策略，您首先需要收集一个数据集。您收集的数据越多，最终的驾驶策略就越好。在我们的论文实验中，我们收集了大约30分钟的数据。请注意，网络将模仿您的驾驶行为。您驾驶得越好越一致，网络将学会驾驶得越好。

1. 将蓝牙游戏控制器连接到手机（例如PS4控制器：按住PS和分享按钮直到LED快速闪烁以进入配对模式）。
2. 在应用程序中选择`CIL-Mobile-Cmd`模型。
3. 现在通过游戏控制器驾驶汽车并记录数据集。在PS4控制器上，可以通过**X**按钮切换日志记录。

您现在会在智能手机的内部存储中找到一个名为*Documents/OpenBot*的文件夹。每次录制都会有一个zip文件。zip文件的名称格式为*yyyymmdd_hhmmss.zip*，对应于录制开始时的时间戳。

Jupyter notebook期望在同一文件夹中有一个名为`dataset`的文件夹。在这个文件夹中，应该有两个子文件夹，`train_data`和`test_data`。训练数据用于学习驾驶策略。测试数据用于在训练过程中验证在未见过的数据上的学习驾驶策略。这提供了一些关于该策略在机器人上工作的指示。即使机器人沿着训练期间看到的相同路线行驶，每次运行观察到的确切图像也会略有不同。常见的分割是80%的训练数据和20%的测试数据。在`train_data`和`test_data`文件夹中，您需要为每个录制会话创建一个文件夹，并给它一个名称，例如`my_openbot_1`，`my_openbot_2`等。这里的想法是，每个录制会话可能有不同的光照条件，不同的机器人，不同的路线。在Jupyter notebook中，您可以仅在这些数据集的子集上或在所有数据集上进行训练。在每个录制会话文件夹中，您放置所有从该录制会话中提取的zip文件。您的数据集文件夹应如下所示：

<img src="../docs/images/folder_structure.png" width="200" alt="文件夹结构" />

与其手动从手机复制所有文件，您还可以自动将日志上传到计算机上的[Python服务器](#web-app)。在这种情况下，zip文件将被上传并解压到文件夹`dataset/uploaded`中。您仍然需要将它们移动到训练的文件夹结构中。您可以简单地将`uploaded`文件夹视为一个录制会话并将其移动到`train_data`中。然后，Jupyter notebook将识别这些录音为训练数据。如果您在`test_data`文件夹中还没有录制会话，您还需要将至少一个录音从`train_data/uploaded`移动到`test_data/uploaded`中。

### 数据转换（可选）

为了更好的训练性能，您可以将收集的数据集转换为专门的格式。您可以使用以下命令创建训练和测试数据集的tfrecord：

```bash
conda activate openbot
python -m openbot.tfrecord -i dataset/train_data -o dataset/tfrecords -n train.tfrec
python -m openbot.tfrecord -i dataset/test_data -o dataset/tfrecords -n test.tfrec
```

默认情况下，此转换将在训练开始时自动完成。

## 策略训练

通过执行以下命令确保您的openbot conda环境已激活：

```bash
conda activate openbot
```

### Jupyter Notebook

我们提供了一个[Jupyter Notebook](policy_learning.ipynb)，指导您完成训练自动驾驶策略的步骤。使用以下命令打开笔记本。

```bash
jupyter notebook policy_learning.ipynb
```

现在将自动打开一个网页浏览器窗口并加载Jupyter notebook。按照步骤使用您的数据训练模型。

### Shell

此方法假设数据在正确的位置。要调整超参数，您可以传递以下参数。

```bash
'--no_tf_record', action='store_true', help='不加载tfrecord而是文件目录'
'--create_tf_record', action='store_true', help='创建新的tfrecord'
'--model', type=str, default='pilot_net', choices=['cil_mobile', 'cil_mobile_fast', 'cil', 'pilot_net'], help='网络架构（默认：cil_mobile）'
'--batch_size', type=int, default=16, help='训练批次大小（默认：16）'
'--learning_rate', type=float, default=0.0001, help='学习率（默认：0.0001）'
'--num_epochs', type=int, default=10, help='训练轮数（默认：10）'
'--batch_norm', action='store_true', help='使用批量归一化'
'--flip_aug', action='store_true', help='随机翻转图像和控制以进行增强'
'--cmd_aug', action='store_true', help='为命令输入添加噪声以进行增强'
'--resume', action='store_true', help='恢复之前的训练'
```

如果您的数据集已经转换为tfrecord，您可以使用以下命令从shell训练策略：

```bash
python -m openbot.train
```

如果您想在训练之前将数据集转换为tfrecord，您需要添加以下标志：

```bash
python -m openbot.train --create_tf_record
```

如果您不想将数据集转换为tfrecord，并直接使用文件进行训练，您需要添加以下标志：

```bash
python -m openbot.train --no_tf_record
```

要训练用于最终部署的模型，您需要使用较大的批次大小和训练轮数。启用批量归一化通常会改善训练。模型`pilot_net`比默认的`cil_mobile`更大，但在某些任务上可以实现更好的性能，同时仍能在大多数智能手机上实时运行。

```bash
python -m openbot.train --model pilot_net --batch_size 128 --num_epochs 100 --batch_norm
```

### 部署

在训练过程结束时，会生成两个tflite文件：一个对应于根据验证指标的最佳检查点，另一个对应于最后一个检查点。选择其中一个并将其重命名为autopilot_float.tflite。替换Android Studio中的现有模型并重新编译应用程序。

<p align="center">
  <img src="../docs/images/android_studio_tflite_dir.jpg" width="200" alt="应用程序GUI" />
</p>

如果您在本地目录中查找文件夹，您会在以下位置找到它：`app/src/main/assets/networks`。

## Web应用程序

我们提供了一个Web应用程序和一个Python Web服务器，以便于策略训练。（测试版）

### 功能

- 自动日志（会话）上传
  - 参见故障排除详情
- 列出上传的会话，带有GIF预览
- 列出数据集，带有基本信息
- 将会话移动到数据集
- 删除会话
- 列出训练的模型，并显示关于训练的图表
- 使用基本参数训练模型，显示进度条

### 预览

<img src="../docs/images/web-app.gif" width="100%" alt="Web应用程序预览" />

### 快速开始

```bash
conda activate openbot
python -m openbot.server
```

您现在可以打开浏览器，通过访问以下地址来可视化数据集并查看传入的上传：
[http://localhost:8000/#/uploaded](http://localhost:8000/#/uploaded)

### 运行服务器

您可以使用以下命令运行Python服务器：

```bash
python -m openbot.server
```

还有一个开发者模式：

```bash
adev runserver openbot/server
```

用于前端开发（react应用程序）：

```
FE_DEV=1 adev runserver openbot/server
```

当您运行服务器时，您应该会看到类似以下内容：

```
Skip address 127.0.0.1 @ interface lo
Found address 192.168.x.x @ interface wlp2s0
Registration of a service, press Ctrl-C to exit...
Running frontend: 0.7.0
Frontend path: /home/USERNAME/miniconda3/envs/openbot/lib/python3.7/site-packages/openbot_frontend
======== Running on http://0.0.0.0:8000 ========
(Press CTRL+C to quit)
```

### 故障排除

如果上传到服务器不起作用，以下是一些故障排除提示：

- 尝试重新启动服务器（计算机）和OpenBot应用程序（智能手机）
- 确保智能手机和计算机连接到同一个WiFi网络
- 如果您的路由器具有相同名称的2.4 GHz和5 GHz网络，请禁用5 GHz网络
- 在运行应用程序时将手机连接到Android Studio。在Logcat选项卡中，从下拉菜单中选择Debug。在过滤字段中输入`NSD`以查看有关服务器连接的调试消息。在过滤字段中输入`Upload`以查看有关录制文件上传的调试消息。
- 如果发布的模型不断下载，请确保手机和笔记本电脑/工作站的时间设置正确。