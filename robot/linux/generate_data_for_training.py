import os
import pickle
import csv
import numpy as np
from PIL import Image

CUR_DIR = os.path.join(os.path.dirname(__file__))


class Logger:
    def __init__(self, log_name, logs_dir=None):
        """Generates data folder from <log_name>.p

        Args:
            log_name (_type_): _description_
        """
        if logs_dir is None:
            logs_dir = f"{CUR_DIR}/logs"

        self.data_dir = f"{logs_dir}/dataset/uploaded/{log_name}/data"
        os.makedirs(f"{self.data_dir}/sensor_data", exist_ok=True)
        os.makedirs(f"{self.data_dir}/images", exist_ok=True)

        with open(f"{logs_dir}/{log_name}.p", "rb") as f:
            log_file = pickle.load(f)

        self.ctrls = []
        self.images = []
        self.timestamps = []

        for data in log_file:
            self.ctrls.append(data["action"])
            self.images.append(data["image"])
            self.timestamps.append(data["timestamp"])
            self.cmd_history = data["cmd"]  # cmd_history is already a list

    def write_ctrls(self):
        # Low-level commands sent to the motors
        ctrls = np.array(self.ctrls).squeeze()
        with open(
            f"{self.data_dir}/sensor_data/ctrlLog.txt", "w", encoding="utf-8"
        ) as f:
            writer_ctrl = csv.writer(f, delimiter=",")
            writer_ctrl.writerow(("timestamp[ns]", "leftCtrl", "rightCtrl"))
            for idx in range(len(self.timestamps)):
                writer_ctrl.writerow(
                    (self.timestamps[idx], ctrls[idx, 0], ctrls[idx, 1])
                )

    def write_indicator_logs(self):
        # Log of whenever cmd is changed.
        with open(
            f"{self.data_dir}/sensor_data/indicatorLog.txt", "w", encoding="utf-8"
        ) as f:
            writer_indicator = csv.writer(f, delimiter=",")
            writer_indicator.writerow(("timestamp[ns]", "signal"))
            for idx, timestamp in enumerate(self.cmd_history["timestamp"]):
                writer_indicator.writerow((timestamp, self.cmd_history["cmd"][idx]))

    def write_images(self):
        image_names = self.save_images()

        # Low-level commands sent to the motors
        with open(
            f"{self.data_dir}/sensor_data/rgbFrames.txt", "w", encoding="utf-8"
        ) as f:
            writer_rgb = csv.writer(f, delimiter=",")
            writer_rgb.writerow(("timestamp[ns]", "frame"))
            for idx in range(len(self.timestamps)):
                writer_rgb.writerow((self.timestamps[idx], image_names[idx]))

    def write_goals(self):
        # Low-level commands sent to the motors
        with open(
            f"{self.data_dir}/sensor_data/goalLog.txt", "w", encoding="utf-8"
        ) as f:
            writer_rgb = csv.writer(f, delimiter=",")
            writer_rgb.writerow(("timestamp[ns]", "dist", "sin_yaw", "cos_yaw"))
            for idx in range(len(self.timestamps)):
                writer_rgb.writerow((self.timestamps[idx], 0.0, 0.0, 1.0))

    def write_pose(self):
        # Low-level commands sent to the motors
        with open(
            f"{self.data_dir}/sensor_data/poseData.txt", "w", encoding="utf-8"
        ) as f:
            writer_rgb = csv.writer(f, delimiter=",")
            writer_rgb.writerow(
                (
                    "timestamp[ns]",
                    "posX",
                    "posY",
                    "posZ",
                    "rollAngle",
                    "pitchAngle",
                    "yawAngle",
                )
            )

            for idx in range(len(self.timestamps)):
                writer_rgb.writerow(
                    (self.timestamps[idx], 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                )

    def write_files(self):
        self.remove_zero_ctrls()
        self.write_ctrls()
        self.write_images()
        self.write_goals()
        self.write_pose()
        self.write_indicator_logs()

    def save_images(self):
        names = []
        for idx, image in enumerate(self.images):
            img = Image.fromarray(np.uint8(image * 255))
            names.append(idx)
            img.save(f"{self.data_dir}/images/{idx}.jpeg")

        return names

    def remove_zero_ctrls(self):
        idx_to_remove = []

        for idx, ctrl in enumerate(self.ctrls):
            if abs(np.sum(ctrl)) < 1e-3:
                idx_to_remove.append(idx)

        for idx in reversed(idx_to_remove):
            del self.timestamps[idx]
            del self.ctrls[idx]
            del self.images[idx]


if __name__ == "__main__":
    print("See tests/test_generate_data.py for usage")
