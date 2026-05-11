"""
Created by Marcel Santos - Intel Intelligent Systems Lab - 2021
This script implements several utility routines for manipulating tensorflow records.
"""

import tensorflow as tf


def image_feature(value):
    """Returns a bytes_list from a string / byte."""
    return tf.train.Feature(
        bytes_list=tf.train.BytesList(value=[tf.io.encode_jpeg(value).numpy()])
    )


def bytes_feature(value):
    """Returns a bytes_list from a string / byte."""
    return tf.train.Feature(bytes_list=tf.train.BytesList(value=[value.encode()]))


def float_feature(value):
    """Returns a float_list from a float / double."""
    return tf.train.Feature(float_list=tf.train.FloatList(value=[value]))


def int64_feature(value):
    """Returns an int64_list from a bool / enum / int / uint."""
    return tf.train.Feature(int64_list=tf.train.Int64List(value=[value]))


def float_feature_list(value):
    """Returns a list of float_list from a float / double."""
    return tf.train.Feature(float_list=tf.train.FloatList(value=value))


def parse_tfrecord_fn_autopilot(example):
    """Parse the input `tf.train.Example` proto."""

    # Create a description of the features.
    feature_description = {
        "image": tf.io.FixedLenFeature([], tf.string),
        "path": tf.io.FixedLenFeature([], tf.string),
        "left": tf.io.FixedLenFeature([], tf.float32),
        "right": tf.io.FixedLenFeature([], tf.float32),
        "cmd": tf.io.FixedLenFeature([], tf.float32),
    }

    example = tf.io.parse_single_example(example, feature_description)
    img = tf.io.decode_jpeg(example["image"], channels=3)
    img = tf.image.convert_image_dtype(img, tf.float32)
    example["image"] = img
    return example


def parse_tfrecord_fn_point_goal_nav(example):
    """Parse the input `tf.train.Example` proto."""

    # Create a description of the features.
    feature_description = {
        "image": tf.io.FixedLenFeature([], tf.string),
        "path": tf.io.FixedLenFeature([], tf.string),
        "left": tf.io.FixedLenFeature([], tf.float32),
        "right": tf.io.FixedLenFeature([], tf.float32),
        "dist": tf.io.FixedLenFeature([], tf.float32),
        "sinYaw": tf.io.FixedLenFeature([], tf.float32),
        "cosYaw": tf.io.FixedLenFeature([], tf.float32),
    }

    example = tf.io.parse_single_example(example, feature_description)
    img = tf.io.decode_jpeg(example["image"], channels=3)
    img = tf.image.convert_image_dtype(img, tf.float32)
    example["image"] = img
    return example


def create_example_autopilot(image, path, ctrl_cmd):
    """Converts the train features into a `tf.train.Example` eady to be written to a tfrecord file."""

    # Create a dictionary mapping the feature name to the tf.train.Example-compatible data type.
    feature = {
        "image": image_feature(image),
        "path": bytes_feature(path),
        "left": float_feature(float(ctrl_cmd[0]) / 255.0),
        "right": float_feature(float(ctrl_cmd[1]) / 255.0),
        "cmd": float_feature(float(ctrl_cmd[2])),
    }

    return tf.train.Example(features=tf.train.Features(feature=feature))


def create_example_point_goal_nav(image, path, ctrl_goal):
    """Converts the train features into a `tf.train.Example` eady to be written to a tfrecord file."""

    # Create a dictionary mapping the feature name to the tf.train.Example-compatible data type.
    feature = {
        "image": image_feature(image),
        "path": bytes_feature(path),
        "left": float_feature(float(ctrl_goal[0])),
        "right": float_feature(float(ctrl_goal[1])),
        "dist": float_feature(float(ctrl_goal[2])),
        "sinYaw": float_feature(float(ctrl_goal[3])),
        "cosYaw": float_feature(float(ctrl_goal[4])),
    }

    return tf.train.Example(features=tf.train.Features(feature=feature))
