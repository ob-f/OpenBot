from realsense import get_realsense_pipeline
import numpy as np
import cv2
import os

CUR_DIR = os.path.join(os.path.dirname(__file__))


def test_video():
    pipeline = get_realsense_pipeline()
    print("Opening cv stream")
    counter = 0
    while True:
        # Wait for a coherent pair of frames: depth and color
        frames = pipeline.wait_for_frames()
        color_frame = frames.get_color_frame()
        # Convert images to numpy arrays
        color_image = np.asanyarray(color_frame.get_data())

        dim = (256, 144)
        image_resize = cv2.resize(color_image, dim, interpolation=cv2.INTER_AREA)

        # crop top 1/3:
        pixel_to_crop = int(dim[1] / 3)
        image_crop = image_resize[pixel_to_crop:, ...]

        cv2.namedWindow("RealSense", cv2.WINDOW_AUTOSIZE)
        cv2.imshow("RealSense", image_crop)
        cv2.waitKey(1)
        if counter > 1e6:
            break
        counter += 1


if __name__ == "__main__":
    test_video()
