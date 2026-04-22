# Python

<p align="center">
  <a href="README.md">English</a> |
  <span>简体中文</span> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <a href="README.ko-KR.md">한국어</a>
</p>

该模块是嵌入式Linux系统中用于控制OpenBot车辆的智能手机控制替代方案。使用Python编写，OpenBot可以通过基于Linux的计算机和摄像头进行感知和控制。

机器人可以通过两种方式进行控制：通过神经网络策略推理或通过操纵杆。

```
├── __init__.py
├── README.md
├── requirements.txt
├── run.py
├── generate_data_for_training.py
├── export_openvino.py
├── infer.py
├── joystick.py
├── realsense.py
└── tests
    ├── test_data
    │   └── logs1
    │       └── ...
    ├── test_models
    │   ├── openvino
    │   ├── tflite
    │   └── tf.zip
    ├── test_export_openvino.py
    ├── test_infer.py
    ├── test_joystick.py
    ├── test_motor.py
    └── test_realsense.py

```
## 运行机器人

要操作机器人，请运行`run.py`，这是主要的Python脚本。机器人可以在三种模式下运行：
- 调试模式：此模式离线运行策略。即，它使用从数据集（参见`tests/test_data/logs1/data`）加载的数据（命令和图像）作为策略的输入，而不是实际的摄像头图像和操纵杆输入命令。
- 推理模式：此模式在线运行策略。它使用实际的摄像头图像和操纵杆输入命令作为策略的输入。可以通过按操纵杆上的`A`键切换到操纵杆模式。
- 操纵杆模式：此模式通过操纵杆命令操作机器人，可以在“Dual”（通过左右操纵杆控制左右轮）或“Joystick”（通过一个操纵杆控制前进、后退、左转、右转方向）`control_mode`下运行。训练数据的收集是在操纵杆模式下进行的。可以通过按操纵杆上的`A`键切换到推理模式。

`run.py`脚本接受六个参数（详细信息见`run.py`）：
```
--policy_path: 策略文件的路径。
--dataset_path: 数据集的路径。仅用于调试模式。
--log_path: 日志文件夹的路径，运行记录将保存在此处。
--inference_backend: 使用的后端。为了获得最佳性能，建议将所有模型导出为openvino模型。选项：tf, tflite, openvino。
--mode: 运行模式。选项：debug, inference, joystick。
--control_mode: 操纵杆模式下的控制模式。选项：dual, joystick。
```
## 生成训练数据
脚本`generate_data_for_training.py`生成一个日志数据文件夹，该文件夹是通过`OpenBot/policy/openbot/train.py`脚本训练策略所需的。日志数据文件夹包含一个`images`和一个`sensor_data`文件夹，格式符合`train.py`的要求。

参见`tests/test_generate_data.py`以获取示例。

## OpenVino：优化策略推理性能
为了在支持的Intel硬件（如[Up Core Plus](https://up-board.org/upcoreplus/specifications/)板）上优化推理速度，需要将训练好的模型导出为OpenVino。

`export_openvino.py`脚本将训练好的TensorFlow模型导出为OpenVino模型。然后通过`infer.py`中的`get_openvino_interpreter()`加载此OpenVino模型。

参见`tests/test_export_openvino.py`以获取示例。

## 测试和示例代码

**注意：** 要测试代码，需要将名为`test_data`和`test_model`的测试数据和测试模型分别放在`OpenBot/python/tests`中。`download_data.py`中的`get_data()`函数提供下载功能，并在`test_infer.py`、`test_export_openvino.py`和`test_generate_data.py`的开头调用。或者，请运行脚本`get_test_data.sh`（仅限Unix系统），该脚本会下载并解压包含`test_data`和`test_models`的zip文件，分别用于调试模式的数据和推理模型。

在`tests`文件夹中运行`pytest`或单独运行`test_*.py`文件以测试以下功能：

- 通过`test_download_data.py`从云端下载测试数据和测试模型
- 通过`test_export_openvino.py`导出到OpenVino
- 通过`test_generate_data.py`生成训练数据
- 在调试模式下进行OpenVino、Tensorflow和Tflite的推理，通过`test_infer.py`
    - *注意*：logs1中的测试数据是使用`OpenBot.policy.openbot`中的`associate_frames.py`脚本生成的，其中图像路径硬编码在`logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt`中。
    - 因此，请在`test_infer.py`中将`path_to_openbot`替换为`OpenBot`仓库的实际路径。
- 通过`test_joystick.py`进行操纵杆连接
- 通过`test_motor.py`从串口到Arduino的电机连接
- 通过`test_realsense.py`进行Realsense摄像头的视频流

# 安装
安装过程如下所述。

用于控制OpenBot的Python实现需要一些用于推理、操纵杆控制、感知和执行的Python模块。
此外，可能需要摄像头或控制器的驱动程序。

## 设置
目前，代码在以下设备上进行了测试：
- 板： [Up Core Plus](https://up-board.org/upcoreplus/specifications/)
- 摄像头： [Realsense D435i](https://www.intelrealsense.com/depth-camera-d435i/)
- 控制器： [Xbox One](https://www.microsoft.com/en-gb/store/collections/xboxcontrollers?source=lp)
- Arduino： [OpenBot Firmware](https://github.com/ob-f/OpenBot/blob/master/firmware/README.md)

## Python模块

代码在Python 3.9上进行了测试。使用Anaconda3：
```
conda create --name openbot python==3.9
```

首先，通过以下命令安装OpenBot.policy的依赖项：
```
../policy && pip install -r requirements.txt
```

然后，通过以下命令安装所需的模块：
```
pip install -r requirements.txt
```

特别地，
- `pyserial`通过串口与Arduino及电机通信
- `pyrealsense2`和`opencv-python`用于摄像头图像处理
- `pygame`用于操纵杆控制和处理操纵杆输入
- `openvino-dev[tensorflow2,extras]`用于在支持的Intel硬件上提升性能。有关在Intel硬件上优化AI推理的更多详细信息，请参见[OpenVino](https://docs.openvino.ai/latest/home.html)。OpenVino是推荐的推理后端。Tensorflow和Tflite也受支持（参见测试）。对于运行PyTorch模块，请考虑将PyTorch转换为OpenVino后端（参见[此教程](https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html)）。

## 驱动程序
如果代码在Ubuntu上执行，Xbox One控制器的USB无线适配器需要一个驱动程序，可以在[此链接](https://github.com/medusalix/xone)找到。

## 用于推理的Tensorflow
如果使用TensorFlow进行推理，请通过`export PYTHONPATH=$PYTHONPATH:/path/to/OpenBot/policy`将Python `policy`模块添加到`PYTHONPATH`。此解决方案避免了将openbot安装为模块并找到`openbot.utils.load_model()`的需求，这对于加载tensorflow模型是必需的。更多详细信息，请参见`infer.py`中的`get_tf_interpreter()`和测试代码`tests/test_infer.py`。

## 对非Linux发行版（MacOS，Windows）的支持

请注意，代码旨在在基于Linux的计算机上运行，例如Up Core Plus。某些Python模块可能不适用于MacOS或Windows。

代码可以在MacOS上运行以进行调试，需进行以下更改：
- 在requirements.txt中使用`pyrealsense2-macosx`代替`pyrealsense2`
- 对于tflite，请遵循[这些说明](https://github.com/milinddeore/TfLite-Standalone-build-Linux-MacOS)