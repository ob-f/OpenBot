import pytest
from infer import InferNetwork
import os
from download_data import get_data

PATH_TO_OPENBOT = "/home/yuankai/code/github/OpenBot"  # Replace with path to root directory of OpenBot repository
CUR_DIR = os.path.join(os.path.dirname(__file__))
DATASET_PATH = (
    f"{CUR_DIR}/test_data/logs1/data/"  # this dataset is acquired by running get_data
)

INFERENCE_BACKEND = "openvino"  # tf, tflite, or openvino


def create_network(backend):
    policy = InferNetwork(
        policy_path=f"{CUR_DIR}/test_models/{backend}",
        dataset_path=DATASET_PATH,
        log_path=None,
        run_mode="debug",
        inference_backend=backend,
    )
    return policy


def test_debug():
    create_network(INFERENCE_BACKEND).run()


def replace_path():
    print(
        "The test data in logs1 is generated using the associate_frames.py "
        + "script in OpenBot.policy.openbot, where the path to the images is "
        + "hardcoded in logs1/data/sensor_data/matched_frame_ctrl_cmd_processed.txt."
        + " Thus, replacing the /path/to/OpenBot with the actual path to the OpenBot repository."
    )
    assert len(PATH_TO_OPENBOT) > 0, "Please enter the PATH_TO_OPENBOT in test_infer.py"

    file_path = (
        f"{DATASET_PATH}/sensor_data/matched_frame_ctrl_cmd_processed_template.txt"
    )
    with open(file_path, "r") as file:
        filedata = file.read()

    filedata = filedata.replace("/path/to/OpenBot", PATH_TO_OPENBOT)

    file_path = f"{DATASET_PATH}/sensor_data/matched_frame_ctrl_cmd_processed.txt"
    with open(file_path, "w") as file:
        file.write(filedata)


if __name__ == "__main__":
    get_data(CUR_DIR)

    replace_path()

    test_debug()
