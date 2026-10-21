import os
import zipfile
import requests

CUR_DIR = os.path.join(os.path.dirname(__file__))


def download_file(url, path):
    # NOTE the stream=True parameter below
    with requests.get(url, stream=True) as r:
        r.raise_for_status()
        with open(f"{path}/test_data.zip", "wb") as f:
            for chunk in r.iter_content(chunk_size=8192):
                # If you have chunk encoded response uncomment if
                # and set chunk_size parameter to None.
                # if chunk:
                f.write(chunk)


def get_data(path):
    if os.path.exists(f"{path}/test_data"):
        print("Test data and model exist. Not downloading.")
        return
    url_link = "https://storage.googleapis.com/openbot_tests/test_data.zip"

    # Download if data not exist
    if not os.path.isfile(f"{path}/test_data.zip"):
        print(f"Dataset and model not found. Downloading from {url_link}")
        download_file(url_link, path)
    else:
        print("Zip file already exists. ")

    if not os.path.exists(f"{path}/test_data") and os.path.isfile(
        f"{path}/test_data.zip"
    ):
        print("Extracting test_data.zip")
        with zipfile.ZipFile(f"{path}/test_data.zip", "r") as zip_ref:
            zip_ref.extractall(f"{path}/")


if __name__ == "__main__":
    get_data(path=CUR_DIR)
