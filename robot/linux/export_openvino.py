import os
import subprocess

CUR_DIR = os.path.join(os.path.dirname(__file__))


def export_tf_as_openvino(ckpt_dir, output_dir):
    """Uses openvino's Model Optimizer to convert tf model to openvino.
        See https://docs.openvino.ai/latest/notebooks/101-tensorflow-to-openvino-with-output.html
        for more details.

    Args:
        ckpt_dir (str): Path to the checkpoint directory
        output_dir (str): Path to which the openvino model will be saved.
    """
    # Construct the command for Model Optimizer
    mo_command = f"""mo
                    --saved_model_dir "{ckpt_dir}"
                    --input "cmd_input,img_input"
                    --input_shape "[1,1],[1,96,256,3]"
                    --data_type "FP16"
                    --output_dir "{output_dir}"
                    --model_name "openvino_model"
                    """
    mo_command = " ".join(mo_command.split())
    print("Model Optimizer command to convert TensorFlow to OpenVINO:")
    print(mo_command)

    # Run the Model Optimizer (overwrites the older model)
    print("Exporting TensorFlow model to IR... This may take a few minutes.")
    p = subprocess.run(mo_command, shell=True)


def export_torch_as_openvino(ckpt_dir, output_dir):
    """Untested. Exports onnx model to openvino. See https://docs.openvino.ai/latest/openvino_docs_MO_DG_prepare_model_convert_model_Convert_Model_From_PyTorch.html
        for details

    Args:
        ckpt_dir (str): Path to the checkpoint directory
        output_dir (str): Path to which the openvino model will be saved.
    """
    # Construct the command for Model Optimizer
    mo_command = f"""mo
                    --saved_model_dir "{ckpt_dir}"
                    --input "cmd_input,img_input"
                    --input_shape "[1,1],[1,96,256,3]"
                    --data_type "FP16"
                    --output_dir "{output_dir}"
                    --model_name "openvino_model"
                    """
    mo_command = " ".join(mo_command.split())
    print("Model Optimizer command to convert TensorFlow to OpenVINO:")
    print(mo_command)

    # Run the Model Optimizer (overwrites the older model)
    print("Exporting TensorFlow model to IR... This may take a few minutes.")
    p = subprocess.run(mo_command, shell=True)


if __name__ == "__main__":
    model_dir = f"{CUR_DIR}/models/tf/checkpoints/best.ckpt"
    output_dir = f"{CUR_DIR}/models/openvino"
    export_tf_as_openvino(model_dir, output_dir)
