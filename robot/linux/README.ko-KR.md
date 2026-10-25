# Python

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh-CN.md">简体中文</a> |
  <a href="README.de-DE.md">Deutsch</a> |
  <a href="README.fr-FR.md">Français</a> |
  <a href="README.es-ES.md">Español</a> |
  <span>한국어</span>
</p>

이 모듈은 스마트폰 대신 임베디드 Linux로 OpenBot 차량을 제어하는 방법입니다. Python으로 작성되었으며, Linux 기반 컴퓨터와 센싱용 카메라로 OpenBot을 제어할 수 있습니다.

로봇은 두 가지 방법으로 제어할 수 있습니다. 신경망 정책의 추론을 이용하거나 조이스틱을 사용합니다.

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
    │   └── logs1
    │       └── ...
    ├── test_models
    │   ├── openvino
    │   ├── tflite
    │   └── tf.zip
    ├── test_export_openvino.py
    ├── test_infer.py
    ├── test_joystick.py
    ├── test_motor.py
    └── test_realsense.py

```
## 로봇 실행

로봇을 작동하려면 메인 Python 스크립트인 `run.py`를 실행합니다. 로봇은 3가지 모드로 실행할 수 있습니다.
- 디버그: 정책을 오프라인으로 실행합니다. 즉, 실제 카메라 이미지와 조이스틱 입력 대신 데이터셋(`tests/test_data/logs1/data` 참고)에서 불러온 데이터(명령과 이미지)를 정책의 입력으로 사용합니다.
- 추론: 정책을 온라인으로 실행합니다. 실제 카메라 이미지와 조이스틱 입력 명령을 정책의 입력으로 사용합니다. 조이스틱의 `A` 키를 누르면 조이스틱 모드로 전환됩니다.
- 조이스틱: "Dual"(왼쪽과 오른쪽 조이스틱으로 왼쪽과 오른쪽 바퀴를 제어) 또는 "Joystick"(조이스틱 하나로 앞, 뒤, 왼쪽, 오른쪽 방향을 제어) `control_mode`로 조이스틱 명령에 따라 로봇을 작동합니다. 학습용 데이터 수집은 조이스틱 모드에서 진행합니다. 조이스틱의 `A` 키를 누르면 추론 모드로 전환됩니다.

run.py 스크립트는 여섯 가지 인수를 받습니다(자세한 내용은 `run.py` 참고).
```
--policy_path: Path to policy file.
--dataset_path: Path to dataset. Only used for debug mode.
--log_path: Path to log folder, where runs are saved.
--inference_backend: Backend to use. Consider exporting all models as openvino model for maximum performance. Options: tf, tflite, openvino.
--mode: Running mode. Options: debug, inference, joystick.
--control_mode: Control mode during joystick mode. Options: dual, joystick.
```
## 학습 데이터 생성
`generate_data_for_training.py` 스크립트는 `OpenBot/policy/openbot/train.py` 스크립트로 정책을 학습하는 데 필요한 로그 데이터 폴더를 만듭니다. 로그 데이터 폴더에는 `train.py`가 요구하는 형식의 `images` 폴더와 `sensor_data` 폴더가 들어 있습니다.

예시는 `tests/test_generate_data.py`를 참고하세요.

## OpenVino: 정책 추론 성능 최적화
지원되는 Intel 하드웨어(예: [Up Core Plus](https://up-board.org/upcoreplus/specifications/) 보드)에서 추론 속도를 최적화하려면 학습된 모델을 OpenVino로 내보내야 합니다.

`export_openvino.py` 스크립트는 학습된 TensorFlow 모델을 OpenVino 모델로 내보냅니다. 이 OpenVino 모델은 `infer.py`의 `get_openvino_interpreter()`로 불러옵니다.

예시는 `tests/test_export_openvino.py`를 참고하세요.

## 테스트와 예제 코드

**참고:** 코드를 테스트하려면 `test_data`와 `test_model`이라는 테스트 데이터와 테스트 모델이 `OpenBot/robot/linux/tests`에 있어야 합니다. `download_data.py`의 `get_data()` 함수가 다운로드 기능을 제공하며, `test_infer.py`, `test_export_openvino.py`, `test_generate_data.py`의 시작 부분에서 호출됩니다. 또는 `get_test_data.sh` 스크립트(유닉스 시스템 전용)를 실행하면 디버그 모드용 데이터인 `test_data`와 추론용 모델인 `test_models`가 담긴 zip 파일을 내려받아 압축을 풉니다.

`tests` 폴더에서 `pytest`를 실행하거나 `test_*.py` 파일을 하나씩 실행해 다음 기능을 테스트하세요.

- `test_download_data.py`로 클라우드에서 테스트 데이터와 테스트 모델 다운로드
- `test_export_openvino.py`로 OpenVino 내보내기
- `test_generate_data.py`로 학습 데이터 생성
- `test_infer.py`로 OpenVino, Tensorflow, Tflite의 디버그 모드 추론
    - *참고*: logs1의 테스트 데이터는 `OpenBot.policy.openbot`의 `associate_frames.py` 스크립트로 만들었으며, 이미지 경로가 `logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt`에 고정되어 있습니다.
    - `test_infer.py`가 이 경로를 여러분의 `OpenBot` 저장소 경로로 바꿉니다.
- `test_joystick.py`로 조이스틱 연결
- `test_motor.py`로 시리얼 포트를 통한 Arduino 모터 연결
- `test_realsense.py`로 Realsense 카메라 영상 스트림

# 설치
설치 과정은 다음과 같습니다.

OpenBot을 제어하는 Python 구현에는 추론, 조이스틱 제어, 센싱, 구동을 위한 몇 가지 Python 모듈이 필요합니다. 또한 카메라나 컨트롤러 드라이버가 필요할 수 있습니다.

## 구성
현재 다음 환경에서 코드를 테스트했습니다.
- 보드: [Up Core Plus](https://up-board.org/upcoreplus/specifications/)
- 카메라: [Realsense D435i](https://www.intelrealsense.com/depth-camera-d435i/)
- 컨트롤러: [Xbox One](https://www.microsoft.com/en-gb/store/collections/xboxcontrollers?source=lp)
- Arduino: [OpenBot 펌웨어](https://github.com/ob-f/OpenBot/blob/master/firmware/README.ko-KR.md)

## Python 모듈

코드는 Python 3.9에서 테스트했습니다. Anaconda3 사용:
```
conda create --name openbot python==3.9
```

먼저 다음 명령으로 OpenBot.policy의 요구 사항을 설치합니다.
```
cd ../../policy && pip install -r requirements.txt && cd ../robot/linux
```

그다음 필요한 모듈을 설치합니다.
```
pip install -r requirements.txt
```

특히,
- `pyserial`은 시리얼 포트로 Arduino, 즉 모터와 통신합니다.
- `pyrealsense2`와 `opencv-python`은 카메라 이미지 처리에 필요합니다.
- `pygame`은 조이스틱 제어와 조이스틱 입력 처리에 사용됩니다.
- `openvino-dev[tensorflow2,extras]`는 지원되는 Intel 하드웨어에서 성능을 높이는 데 사용됩니다. Intel 하드웨어에서 최적화된 AI 추론에 대한 자세한 내용은 [OpenVino](https://docs.openvino.ai/latest/home.html)를 참고하세요. OpenVino를 추론 백엔드로 권장합니다. Tensorflow와 Tflite도 지원합니다(테스트 참고). PyTorch 모듈을 실행하려면 PyTorch를 OpenVino 백엔드로 변환하는 것을 고려하세요([이 튜토리얼](https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html) 참고).

## 드라이버
Ubuntu에서 코드를 실행하면 Xbox One 컨트롤러 USB 무선 동글에 드라이버가 필요하며, [이 링크](https://github.com/medusalix/xone)에서 찾을 수 있습니다.

## 추론용 Tensorflow
추론에 TensorFlow를 사용한다면 `export PYTHONPATH=$PYTHONPATH:/path/to/OpenBot/policy`로 Python `policy` 모듈을 `PYTHONPATH`에 추가하세요. 이렇게 하면 openbot을 모듈로 설치하지 않고도 tensorflow 모델을 불러오는 데 필요한 `openbot.utils.load_model()`을 찾을 수 있습니다. 자세한 내용은 `infer.py`의 `get_tf_interpreter()`와 테스트 코드 `tests/test_infer.py`를 참고하세요.

## Linux 이외의 배포판 지원(MacOS, Windows)

이 코드는 Up Core Plus 같은 Linux 기반 컴퓨터에서 실행하도록 만들어졌습니다. 일부 Python 모듈은 MacOS나 Windows에서 사용할 수 없을 수 있습니다.

다음과 같이 바꾸면 디버깅 목적으로 MacOS에서 코드를 실행할 수 있습니다.
- requirements.txt에서 `pyrealsense2` 대신 `pyrealsense2-macosx`를 사용합니다.
- tflite는 [이 안내](https://github.com/milinddeore/TfLite-Standalone-build-Linux-MacOS)를 따르세요.
