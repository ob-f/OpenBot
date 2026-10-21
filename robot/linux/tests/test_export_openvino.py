import os
import pytest
from export_openvino import export_tf_as_openvino
from download_data import get_data

CUR_DIR = os.path.join(os.path.dirname(__file__))


def test_export_tf():
    # Data is acquired from get_data (downloading the model).
    model_dir = f"{CUR_DIR}/test_models/tf/checkpoints/best.ckpt"
    output_dir = f"{CUR_DIR}/test_models/openvino"
    export_tf_as_openvino(model_dir, output_dir)


if __name__ == "__main__":
    get_data(CUR_DIR)

    test_export_tf()
