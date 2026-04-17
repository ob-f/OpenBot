
# 운전 정책(고급)

<p align="center">
 <a href="README.md">English</a> |
 <a href="README.zh-CN.md">简体中文</a> |
 <a href="README.de-DE.md">Deutsch</a> |
 <a href="README.fr-FR.md">Français</a> |
 <a href="README.es-ES.md">Español</a> |
 <span>한국어</span>
</p>

## 면책 조항

1. **안전:** 운전 정책은 완벽하지 않으며 로봇이 충돌할 수 있어요. 항상 안전한 환경에서 작동하도록 하세요! 충돌 시 휴대폰이 손상될 수 있다는 점을 기억하세요! 항상 게임 컨트롤러를 연결하고 키 매핑을 숙지하여 언제든 차량을 멈출 수 있도록 하세요. 사용 시 모든 책임은 사용자에게 있어요!
2. **컴퓨터 하드웨어:** 운전 정책을 학습하려면 많은 자원이 필요하며, 컴퓨터가 느려지거나 멈출 수도 있어요. 큰 RAM과 전용 GPU가 있는 고성능 노트북이나 워크스테이션을 사용하는 것이 좋아요, 특히 더 큰 배치 크기로 학습할 때요. 문서도 아직 자세하지 않으니, 사용 시 주의하세요!
3. **인내심 필요:** 사용자 지정 데이터셋에 적합한 운전 정책을 얻으려면 인내심이 필요해요. 간단하지 않으며, 데이터 수집, 하이퍼파라미터 튜닝 등이 포함돼요. 머신러닝 모델 학습을 처음 해보는 경우 어렵거나 답답할 수 있어요.

먼저 학습 환경을 설정해야 해요.

## 의존성

OpenBot용 Conda 환경을 만드는 것을 추천드려요. Conda 설치 방법은 [여기](https://docs.conda.io/projects/conda/en/latest/user-guide/install/)에서 확인할 수 있어요. 모든 의존성을 포함하는 새 환경을 만드는 가장 쉬운 방법은 제공된 환경 파일 중 하나를 사용하는 것이에요. Windows에서는 [Microsoft C++ Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/)도 설치해야 해요. 로컬 OpenBot 저장소 내에서 `policy` 폴더에 있는지 확인하세요. 운영 체제에 따라 아래 명령어를 실행하세요:

- **MacOS**: `conda env create -f environment_mac.yml`
- **Windows**: `conda env create -f environment_win.yml`
- **Linux**: `conda env create -f environment_linux.yml`

GPU를 지원하려면 적절한 드라이버도 설치해야 해요. Mac과 Windows에서는 기본적으로 작동할 거예요. Linux에서는 다음 명령어로 드라이버를 설치할 수 있어요:
```
sudo apt-get install nvidia-driver-510
```
Linux에서는 다음을 실행해 cuda와 cudnn을 경로에 추가해야 할 수도 있어요:
```
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```

완료! 이제 자신만의 모델을 학습시킬 준비가 되었어요. 작동하지 않을 경우, 아래 수동 설정 방법을 참고하세요.

### 수동 환경 설정

먼저 다음 명령어로 새 Conda 환경을 만드세요:

```bash
conda create -n openbot pip python=3.9 -y
```

그다음 Conda 환경을 활성화하세요:

```bash
conda activate openbot
```

만약 작동하지 않으면(예: Windows), activate openbot 명령어로 활성화해보세요.

환경이 활성화되면 tensorflow를 설치해야 해요. 노트북에서는 학습이 매우 느릴 수 있어요. 전용 GPU가 있는 컴퓨터가 있다면 필수 라이브러리를 설치한 후 사용하는 것이 좋아요. 아래는 운영 체제별 tensorflow 설치 명령어예요.

#### **Mac OS**
```
conda install -c apple tensorflow-deps -y
pip install tensorflow-macos~=2.9.0
```
GPU 지원
```
pip install tensorflow-metal~=0.5.0
```
[문제 해결](https://developer.apple.com/metal/tensorflow-plugin/)

#### **Linux**
```
pip install tensorflow~=2.9.0
```
GPU 지원
```
sudo apt-get install nvidia-driver-510
conda install -c conda-forge cudatoolkit=11.2 cudnn=8.1 -y
echo 'export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$CONDA_PREFIX/lib/' >> ~/.bashrc
source ~/.bashrc
```
[문제 해결](https://www.tensorflow.org/install/pip#linux)

#### **Windows**
```
pip install tensorflow~=2.9.0
```
GPU 지원
```
conda install cudatoolkit=11.3 cudnn=8.2 -y
```

#### **추가 요구 사항**

로컬 OpenBot 저장소 내 policy 폴더에 있는지 확인하세요. 이제 다음 명령어로 나머지 의존성을 설치할 수 있어요:

```bash
pip install -r requirements.txt
```

네트워크 아키텍처를 시각화하려면 pip install pydot와 graphviz(설치 안내)도 설치할 수 있어요.

데이터 수집 및 학습을 위한 웹앱을 사용하려면 아래 추가 의존성을 설치해야 해요. (Mac에서는 brotlipy 패키지가 현재 pip에서 깨져 있어요. 먼저 conda로 설치해야 해요: conda install brotlipy=0.7)

```bash
pip install -r requirements_web.txt
```

### 필수 패키지

참조 및 문제 해결을 위해, 아래는 필수 패키지 목록이에요.

학습:

- [tensorflow](https://pypi.org/project/tensorflow/)
- [jupyter notebook](https://pypi.org/project/notebook/)
- [matplotlib](https://pypi.org/project/matplotlib/)
- [numpy](https://pypi.org/project/numpy/)
- [PIL](https://pypi.org/project/Pillow/)
- [black[jupyter]](https://pypi.org/project/black/)

웹 인터페이스:

- [aiohttp](https://pypi.org/project/aiohttp/)
- [aiozeroconf](https://pypi.org/project/aiozeroconf/)
- [imageio](https://pypi.org/project/imageio/)

### 참고 사항

- 명령어를 실행하기 전에 환경을 활성화하는 것을 기억하세요: `conda activate openbot`
- tensorflow를 가져올 수 없는 경우 `pip install tensorflow --user` 로 설치해보세요. ([문제](https://github.com/ob-f/OpenBot/issues/98) 참고)

## 데이터셋

### 데이터 수집

자율 주행 정책을 학습시키려면 먼저 데이터셋을 수집해야 해요. 데이터를 많이 수집할수록 더 좋은 운전 정책이 나와요. 논문 실험에서는 약 30분 분량의 데이터를 수집했어요. 네트워크는 여러분의 운전 행동을 따라 해요. 운전을 더 잘하고 일관성 있게 할수록 네트워크도 더 잘 배우게 돼요.

1. 블루투스 게임 컨트롤러를 휴대폰에 연결하세요. (예: PS4 컨트롤러: PS 버튼과 공유 버튼을 동시에 눌러 LED가 빠르게 깜박일 때 페어링 모드로 들어가요.)
2. 앱에서 `CIL-Mobile-Cmd` 모델을 선택하세요.
3. 이제 게임 컨트롤러로 자동차를 운전하며 데이터셋을 기록하세요. PS4 컨트롤러에서 **X** 버튼으로 로깅을 전환할 수 있어요.

스마트폰 내부 저장소에 *Documents/OpenBot* 폴더가 생성될 거예요. 각 기록에 대해 zip 파일이 생성돼요. zip 파일 이름은 기록 시작 시점의 타임스탬프에 따라 *yyyymmdd_hhmmss.zip* 형식으로 저장돼요.

Jupyter 노트북은 동일한 폴더 내에서 `dataset`이라는 폴더를 기대해요. 이 폴더에는 `train_data`와 `test_data`라는 두 개의 하위 폴더가 있어야 해요. 학습 데이터는 운전 정책 학습에 사용되고, 테스트 데이터는 학습 과정 중에 보지 못한 데이터로 학습된 정책을 검증하는 데 사용돼요. 이로써 로봇에서 정책이 얼마나 잘 작동할지 일부 알 수 있어요. 학습 데이터는 80%, 테스트 데이터는 20%로 분할하는 것이 일반적이에요. `train_data`와 `test_data` 폴더 안에는 각 기록 세션을 위한 폴더를 만들어야 해요. 이름은 `my_openbot_1`, `my_openbot_2`처럼 지정하세요. 각 기록 세션마다 조명 조건, 로봇, 경로 등이 다를 수 있어요. Jupyter 노트북에서는 이러한 데이터셋의 일부 또는 전체로만 학습할 수 있어요. 각 기록 세션 폴더에는 데이터셋과 대응되는 `.zip` 파일이 있어야 해요. `.zip` 파일은 압축을 해제하지 않아요. 학습 중에 데이터를 자동으로 불러와요.

<img src="../docs/images/folder_structure.png" width="200" alt="folder structure" />

휴대폰에서 모든 파일을 수동으로 복사하지 않아도, [Python 서버](#웹-앱)를 사용해서 로그를 컴퓨터로 자동 업로드할 수 있어요. 이렇게 하면 zip 파일이 `dataset/uploaded` 폴더에 업로드되고 압축이 풀릴 거예요. 하지만 여전히 학습을 위해 파일들을 폴더 구조에 맞게 이동시켜야 해요. `uploaded` 폴더를 하나의 녹화 세션으로 간주하고 이를 `train_data`로 옮기면 돼요. 그러면 녹화 데이터가 Jupyter 노트북에서 학습 데이터로 인식될 거예요. 만약 `test_data` 폴더에 녹화 세션이 하나도 없다면, `train_data/uploaded`에서 적어도 하나의 녹화를 `test_data/uploaded`로 이동해야 해요.

### 데이터 변환 (선택 사항)

더 나은 학습 성능을 위해, 수집한 데이터를 특수한 형식으로 변환할 수 있어요. 아래 명령어를 사용해서 학습 데이터와 테스트 데이터를 tfrecord 형식으로 만들 수 있어요:

```bash
conda activate openbot
python -m openbot.tfrecord -i dataset/train_data -o dataset/tfrecords -n train.tfrec
python -m openbot.tfrecord -i dataset/test_data -o dataset/tfrecords -n test.tfrec
```

기본적으로 이 변환은 학습 시작 시 자동으로 수행돼요.

## 정책 훈련

OpenBot을 위한 conda 환경이 활성화되어 있는지 다음 명령어로 확인해주세요:

```bash
conda activate openbot
```

### Jupyter 노트북

우리는 자율 주행 정책 훈련 과정을 안내하는 [Jupyter 노트북](policy_learning.ipynb)을 제공합니다. 아래 명령어로 노트북을 열 수 있어요.

```bash
jupyter notebook policy_learning.ipynb
```

이제 웹 브라우저 창이 자동으로 열리고 Jupyter 노트북이 로드될 거예요. 자신의 데이터를 사용해 모델을 훈련하는 과정을 따라가면 돼요!

### 셸

이 방법은 데이터가 올바른 위치에 있다고 가정해요. 하이퍼파라미터를 조정하려면 다음 인수들을 전달할 수 있어요.

```bash
'--no_tf_record', action='store_true', help='tfrecord을 로드하지 않고 파일 디렉토리를 로드해요'
'--create_tf_record', action='store_true', help='새로운 tfrecord를 생성해요'
'--model', type=str, default='pilot_net', choices=['cil_mobile', 'cil_mobile_fast', 'cil', 'pilot_net'], help='네트워크 아키텍처 (기본값: cil_mobile)'
'--batch_size', type=int, default=16, help='훈련 배치 크기 (기본값: 16)'
'--learning_rate', type=float, default=0.0001, help='학습률 (기본값: 0.0001)'
'--num_epochs', type=int, default=10, help='에폭 수 (기본값: 10)'
'--batch_norm', action='store_true', help='배치 정규화를 사용해요'
'--flip_aug', action='store_true', help='이미지와 제어를 랜덤으로 뒤집어서 증강해요'
'--cmd_aug', action='store_true', help='명령 입력에 노이즈를 추가하여 증강해요'
'--resume', action='store_true', help='이전 훈련을 이어서 해요'
```

만약 데이터셋이 이미 tfrecord 형식으로 변환되었다면, 아래 명령어로 셸에서 정책을 훈련시킬 수 있어요:

```bash
python -m openbot.train
```

데이터셋을 tfrecord 형식으로 변환하고 싶다면, 훈련 전에 다음 플래그를 추가해야 해요:

```bash
python -m openbot.train --create_tf_record
```

데이터셋을 tfrecord로 변환하지 않고 파일들을 직접 사용해서 훈련하려면, 다음 플래그를 추가해야 해요:

```bash
python -m openbot.train --no_tf_record
```

최종 배포용 모델을 훈련하려면 큰 배치 크기와 더 많은 에폭을 사용해야 해요. 배치 정규화를 활성화하면 훈련이 보통 더 잘 되요. `pilot_net` 모델은 기본 `cil_mobile`보다 크지만, 일부 작업에서는 더 좋은 성능을 낼 수 있고 대부분의 스마트폰에서도 실시간으로 실행돼요.

```bash
python -m openbot.train --model pilot_net --batch_size 128 --num_epochs 100 --batch_norm
```

### 배포

훈련 과정이 끝나면 두 개의 tflite 파일이 생성됩니다: 하나는 검증 지표에 따라 가장 좋은 체크포인트에 해당하고, 다른 하나는 마지막 체크포인트에 해당합니다. 그 중 하나를 선택하고 이름을 `autopilot_float.tflite`로 변경하세요. Android Studio에서 기존 모델을 교체하고 앱을 다시 컴파일하세요.

<p align="center">
  <img src="../docs/images/android_studio_tflite_dir.jpg" width="200" alt="App GUI" />
</p>

로컬 디렉토리에서 폴더를 찾고 있다면, `app/src/main/assets/networks`에 있습니다.

## 웹 앱

우리는 정책 훈련을 쉽게 할 수 있도록 웹 앱과 파이썬 웹 서버를 제공합니다. (베타)

### 기능

- 자동 로그 (세션) 업로드
  - 자세한 사항은 문제 해결을 참조하세요.
- 업로드된 세션 목록, GIF 미리보기 제공
- 데이터셋 목록, 기본 정보 제공
- 세션을 데이터셋으로 이동
- 세션 삭제
- 훈련된 모델 목록 및 훈련에 대한 그래프 표시
- 기본 매개변수로 모델 훈련, 진행 바 표시

### 미리보기

<img src="../docs/images/web-app.gif" width="100%" alt="Web App preview" />

### 빠른 시작

```bash
conda activate openbot
python -m openbot.server
```

이제 브라우저를 열어 데이터셋을 시각화하고 업로드된 파일을 보려면 아래 링크로 가세요:
[http://localhost:8000/#/uploaded](http://localhost:8000/#/uploaded)

### 서버 실행

다음 명령어로 파이썬 서버를 실행할 수 있습니다:

```bash
python -m openbot.server
```

개발자 모드도 있습니다:

```bash
adev runserver openbot/server
```

프론트엔드 개발 (React 앱):

```
FE_DEV=1 adev runserver openbot/server
```

서버를 실행하면 다음과 같은 메시지를 볼 수 있습니다:

```
Skip address 127.0.0.1 @ interface lo
Found address 192.168.x.x @ interface wlp2s0
Registration of a service, press Ctrl-C to exit...
Running frontend: 0.7.0
Frontend path: /home/USERNAME/miniconda3/envs/openbot/lib/python3.7/site-packages/openbot_frontend
======== Running on http://0.0.0.0:8000 ========
(Press CTRL+C to quit)
```

### 문제 해결

서버에 업로드가 제대로 되지 않는다면, 다음과 같은 해결 방법을 시도해 보세요:

- 서버 (컴퓨터)와 OpenBot 앱 (스마트폰)을 다시 시작해 보세요.
- 스마트폰과 컴퓨터가 동일한 WiFi 네트워크에 연결되어 있는지 확인하세요.
- 라우터에 2.4 GHz와 5 GHz 네트워크가 동일한 이름을 가지고 있으면, 5 GHz 네트워크를 비활성화해 보세요.
- 앱을 실행하는 동안 스마트폰을 Android Studio에 연결해 두세요. Logcat 탭에서 드롭다운 메뉴에서 Debug를 선택하고, 필터 창에 `NSD`를 입력하면 서버 연결에 대한 디버그 메시지를 볼 수 있습니다. `Upload`를 입력하면 업로드 관련 디버그 메시지를 볼 수 있습니다.
- 공개된 모델이 계속 다운로드된다면, 스마트폰과 랩탑/워크스테이션의 시간이 정확하게 설정되어 있는지 확인하세요.