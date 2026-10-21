import cv2
import time
import pyrealsense2 as rs
import numpy as np
import threading


def get_realsense_pipeline():
    """Getting realsense pipeline required for getting Realsense camera images.
        For further support of other Intel Realsense cameras, please see
        https://dev.intelrealsense.com/docs/python2
    Returns:
        pyrealsense2.pipeline: Returns Realsense pipeline object.
        The pipeline simplifies the user interaction with the device and
        computer vision processing modules. The class abstracts the camera
        configuration and streaming, and the vision modules triggering and
        threading. It lets the application focus on the computer vision output
        of the modules, or the device output data. The pipeline can manage
        computer vision modules, which are implemented as a processing blocks.
        The pipeline is the consumer of the processing block interface, while
        the application consumes the computer vision interface.
    """

    try:
        # Configure color streams
        pipeline = rs.pipeline()
        config = rs.config()

        # Get device product line for setting a supporting resolution
        pipeline_wrapper = rs.pipeline_wrapper(pipeline)
        pipeline_profile = config.resolve(pipeline_wrapper)
        device = pipeline_profile.get_device()
        device_product_line = str(device.get_info(rs.camera_info.product_line))

        found_rgb = False
        for s in device.sensors:
            if s.get_info(rs.camera_info.name) == "RGB Camera":
                found_rgb = True
                break
        if not found_rgb:
            raise RuntimeError("The demo requires camera with Color sensor")

        config.enable_stream(rs.stream.color, 640, 480, rs.format.bgr8, 30)

        # Start streaming
        pipeline.start(config)
    except Exception as e:
        print(f"Exception: {e}")
        print("Realsense not found")
        pipeline = None
    return pipeline


class Realsense(threading.Thread):
    def __init__(self, callback):
        """Thread for retrieving realsense images from

        Args:
            callback (function): Function that stores the passed images.
        """
        self.callback = callback
        self.img = None
        self.pipeline = get_realsense_pipeline()

        super().__init__(name="realsense-thread")

        if self.pipeline:
            self.start()
        else:
            print("Realsense not found. Not starting Realsense Thread.")

    def run(self):
        """Runs retrieving images in an infinite loop and stores the current image
        to self.img.
        """
        while True:
            self.img = self.get_img()
            self.callback(self.img)

    def get_img(self, color_image=None, show=False):
        """Queries realsense pipeline for image and postprocesses the image
            for network by scaling between 0 and 1 and cropping the top third
            of the image.

        Args:
            color_image (np.array, optional): If an image is passed, then this
                function does not query from realsense and only performs the
                postprocessing step. Defaults to None.
            show (bool, optional): Whether to show the images. Defaults to False.

        Returns:
            np.array: Returns an (256, 96, 3) image with pixel values between
                0 and 1
        """
        if color_image is None:
            # Wait for a coherent pair of frames: depth and color
            frames = self.pipeline.wait_for_frames()
            color_frame = frames.get_color_frame()
            # Convert images to numpy arrays
            color_image = np.asanyarray(color_frame.get_data())
        dim = (256, 144)
        image_resize = cv2.resize(color_image, dim, interpolation=cv2.INTER_AREA)

        # crop top 1/3:
        pixel_to_crop = int(dim[1] / 3)
        image_crop = image_resize[pixel_to_crop:, ...]
        image_input = image_crop / 255.0

        # Show images
        if show:
            cv2.namedWindow("RealSense", cv2.WINDOW_AUTOSIZE)
            cv2.imshow("RealSense", image_crop)
            cv2.waitKey(1)
        img = np.array(image_input, dtype=np.float32)
        return img


if __name__ == "__main__":
    print("See tests/test_realsense.py for usage.")
