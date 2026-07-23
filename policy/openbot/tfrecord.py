"""
Created by Marcel Santos - Intel Intelligent Systems Lab - 2021
This script implements a tf-records generator for more robust data loading.
Specifically, the user specifies to path of the collected dataset and the path for storing the output tfrecord
file and this script will create a tfrecord with the dataset ready to be used for training a drive policy.
"""

import argparse
import os
import json
import tensorflow as tf

from . import associate_frames
from . import tfrecord_utils


def get_parser():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--dataset-dir",
        "-i",
        type=str,
        required=True,
        help="Path of the collected dataset",
    )
    parser.add_argument(
        "--output-dir",
        "-o",
        type=str,
        required=True,
        help="Path for storing the output file",
    )
    parser.add_argument("--tfrecord-name", "-n", type=str, default="dataset.tfrec")
    return parser


def load_labels(data_dir, datasets, policy="autopilot"):
    """Returns a dictionary of matched images path[string] and actions tuple, namely (left[int], right[int], cmd[int]) for autopilot policy and (left[int], right[int], dist[float],sinYaw[float],cosYaw[float]) for point_goal_nav policy."""

    if policy == "autopilot":
        processed_frames_file_name = "matched_frame_ctrl_cmd_processed.txt"
    elif policy == "point_goal_nav":
        processed_frames_file_name = "matched_frame_ctrl_goal_processed.txt"
    else:
        raise Exception("Unknown policy")

    corpus = []
    for dataset in datasets:
        dataset_folders = [
            f
            for f in os.listdir(os.path.join(data_dir, dataset))
            if not f.startswith(".")
        ]
        for folder in dataset_folders:
            sensor_data_dir = os.path.join(data_dir, dataset, folder, "sensor_data")
            with open(
                os.path.join(sensor_data_dir, processed_frames_file_name)
            ) as f_input:
                header = f_input.readline()  # discard header
                data = f_input.read()
                print(data)
                lines = (
                    data.replace(",", " ")
                    .replace("\\", "/")
                    .replace("\r", "")
                    .replace("\t", " ")
                    .split("\n")
                )
                data = [
                    [v.strip() for v in line.split(" ") if v.strip() != ""]
                    for line in lines
                    if len(line) > 0 and line[0] != "#"
                ]
                # Tuples containing id: framepath and label: "left,right,cmd" for autopiot policy
                # and for "left,right,dist,sinYaw,cosYaw" point_goal_nav policy.
                data = [(l[1], l[2:]) for l in data if len(l) > 1]
                corpus.extend(data)
    return dict(corpus)


def convert_dataset(
    data_dir,
    tfrecords_dir,
    tfrecords_name,
    redo_matching=True,
    remove_zeros=True,
    policy="autopilot",
):
    print(f"Reading dataset from {data_dir}")
    print(f"TFRecord will be saved at {tfrecords_dir}/{tfrecords_name}")

    if policy == "autopilot":
        processed_frames_file_name = "matched_frame_ctrl_cmd_processed.txt"
    elif policy == "point_goal_nav":
        processed_frames_file_name = "matched_frame_ctrl_goal_processed.txt"
    else:
        raise Exception("Unknown policy")

    # load the datasets avaible.
    datasets = [
        d for d in os.listdir(data_dir) if os.path.isdir(os.path.join(data_dir, d))
    ]
    print("Number of Datasets Available: ", len(datasets))

    # match frames.
    max_offset = 1e3  # 1ms
    frames = associate_frames.match_frame_ctrl_input(
        data_dir,
        datasets,
        max_offset,
        redo_matching=redo_matching,
        remove_zeros=remove_zeros,
        policy=policy,
    )

    # creating TFRecords output folder.
    if not os.path.exists(tfrecords_dir):
        os.makedirs(tfrecords_dir)

    # generate data in the TFRecord format.
    samples = load_labels(data_dir, datasets, policy)
    with tf.io.TFRecordWriter(tfrecords_dir + "/" + tfrecords_name) as writer:
        for image_path, ctrl_input in samples.items():
            try:
                image = tf.io.decode_jpeg(tf.io.read_file(image_path))
                if policy == "autopilot":
                    example = tfrecord_utils.create_example_autopilot(
                        image, image_path, ctrl_input
                    )
                elif policy == "point_goal_nav":
                    example = tfrecord_utils.create_example_point_goal_nav(
                        image, image_path, ctrl_input
                    )

                writer.write(example.SerializeToString())
            except:
                print(f"Oops! Image {image_path} cannot be found.")

    print("TFRecord file created successfully.")


if __name__ == "__main__":
    parser = get_parser()
    args = parser.parse_args()
    data_dir = args.dataset_dir
    tfrecords_dir = args.output_dir
    tfrecords_name = args.tfrecord_name

    convert_dataset(data_dir, tfrecords_dir, tfrecords_name)
