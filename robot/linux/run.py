import os
import argparse
from infer import InferNetwork

CUR_DIR = os.path.join(os.path.dirname(__file__))

# POLICY_PATH = f"{CUR_DIR}/tests/test_models/tf/"
# POLICY_PATH = f"{CUR_DIR}/tests/test_models/tflite/"
POLICY_PATH = f"{CUR_DIR}/tests/test_models/openvino/"

DATASET_PATH = f"{CUR_DIR}/tests/test_data/logs1/data/"

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Running the OpenBot Intel version via Python script"
    )
    parser.add_argument(
        "--policy_path", "-p", default=POLICY_PATH, help="Path to policy file."
    )
    parser.add_argument(
        "--dataset_path",
        "-d",
        default=DATASET_PATH,
        help="Path to dataset. Only used for debug mode",
    )
    parser.add_argument(
        "--log_path",
        "-l",
        default=None,
        help="Path to log folder, where runs are saved.",
    )
    parser.add_argument(
        "--inference_backend",
        "-i",
        default="openvino",
        help="Backend to use. Consider exporting all models as openvino model for maximum performance. Options: tf, tflite, openvino",
    )
    parser.add_argument(
        "--run_mode",
        "-m",
        default="joystick",
        help="Running mode. Options: debug, inference, joystick",
    )
    parser.add_argument(
        "--control_mode",
        "-c",
        default="dual",
        help="Control mode during joystick mode. Options: dual, joystick.",
    )

    args = parser.parse_args()

    policy = InferNetwork(**vars(args))

    policy.run()
    if args.run_mode != "debug":
        print("Finished run. Logging")
        policy.write_log()
        print("Finished writing log")
