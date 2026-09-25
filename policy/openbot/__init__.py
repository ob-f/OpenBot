import os

# Force the legacy Keras (Keras 2) backend so this code, which targets the
# Keras 2 model API, also works on TensorFlow >= 2.16 (which switched to Keras 3
# by default and would otherwise raise "the layer ... has never been called"
# during model building). Must be set before TensorFlow is imported.
os.environ.setdefault("TF_USE_LEGACY_KERAS", "1")


def mkdir(path):
    if not (os.path.exists(path)):
        os.makedirs(path)


base_dir = os.path.dirname(os.path.dirname(__file__))
dataset_dir = os.path.join(base_dir, "dataset")
upload_dir = os.path.join(dataset_dir, "uploaded")
train_data_dir = os.path.join(dataset_dir, "train_data")
test_data_dir = os.path.join(dataset_dir, "test_data")
models_dir = os.path.join(base_dir, "models")


mkdir(upload_dir)
mkdir(train_data_dir)
mkdir(test_data_dir)
mkdir(models_dir)

if len(os.listdir(train_data_dir)) < 1:
    mkdir(os.path.join(train_data_dir, "my_dataset_1"))

if len(os.listdir(test_data_dir)) < 1:
    mkdir(os.path.join(test_data_dir, "my_dataset_2"))
