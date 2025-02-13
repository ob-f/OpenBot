import os
import requests
from dotenv import load_dotenv
import google.generativeai as gen_ai
from sqlalchemy import create_engine

# Load environment variables
load_dotenv()

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
gen_ai.configure(api_key=GOOGLE_API_KEY)
model = gen_ai.GenerativeModel('gemini-pro')

# Function to fetch README content
def fetch_readme_content(raw_url):
    try:
        response = requests.get(raw_url)
        if response.status_code != 200:
            return None
        return response.text
    except Exception as e:
        return None

# Function to summarize the README content
def summarize_readme(content):
    summary_prompt = f"Summarize the following README content briefly:\n\n{content}"
    try:
        summary_response = model.start_chat(history=[]).send_message(summary_prompt)
        return summary_response.text
    except Exception as e:
        return None

# Function to store summaries in a database (e.g., PostgreSQL)
def store_summary_in_db(summary, url):
    engine = create_engine(os.getenv("DATABASE_URL"))
    with engine.connect() as connection:
        connection.execute(
            "INSERT INTO readme_summaries (url, summary) VALUES (%s, %s)",
            (url, summary)
        )

# Process the README URLs
README_URLS = {
    "https://github.com/isl-org/OpenBot/blob/master/README.md": 
        "https://raw.githubusercontent.com/isl-org/OpenBot/master/README.md",
    "https://github.com/isl-org/OpenBot/blob/master/android/README.md":
        "https://raw.githubusercontent.com/isl-org/OpenBot/master/android/README.md",
    "https://github.com/isl-org/OpenBot/blob/master/android/controller/README.md":
        "https://raw.githubusercontent.com/isl-org/OpenBot/master/android/controller/README.md",
    "https://github.com/isl-org/OpenBot/blob/master/android/robot/README.md":
        "https://raw.githubusercontent.com/isl-org/OpenBot/master/android/robot/README.md",
    "https://github.com/isl-org/OpenBot/blob/master/android/robot/src/main/java/org/openbot/googleServices/README.md":
        "https://raw.githubusercontent.com/isl-org/OpenBot/master/android/robot/src/main/java/org/openbot/googleServices/README.md",
    "https://github.com/isl-org/OpenBot/blob/master/android/robot/ContributionGuide.md":
        "https://raw.githubusercontent.com/isl-org/OpenBot/master/android/robot/ContributionGuide.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/diy/cad/block_body/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/diy/cad/block_body/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/diy/cad/glue_body/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/diy/cad/glue_body/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/diy/cad/regular_body/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/diy/cad/regular_body/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/diy/cad/slim_body/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/diy/cad/slim_body/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/diy/pcb/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/diy/pcb/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/diy/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/diy/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/lite/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/lite/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/mtv/pcb/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/mtv/pcb/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/mtv/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/mtv/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/rc_truck/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/rc_truck/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/body/rtr/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/body/rtr/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/controller/flutter/ios/Runner/Assets.xcassets/LaunchImage.imageset/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/controller/flutter/ios/Runner/Assets.xcassets/LaunchImage.imageset/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/controller/flutter/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/controller/flutter/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/firmware/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/firmware/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/ios/OpenBot/OpenBot/Authentication/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/ios/OpenBot/OpenBot/Authentication/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/ios/OpenBot/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/ios/OpenBot/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/open-code/src/components/blockly/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/open-code/src/components/blockly/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/open-code/src/services/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/open-code/src/services/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/open-code/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/open-code/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/policy/frontend/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/policy/frontend/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/policy/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/policy/README.md",
    "https://github.com/ob-f/OpenBot/blob/master/python/README.md":
        "https://raw.githubusercontent.com/ob-f/OpenBot/master/python/README.md"
}

# Loop through each README URL and process
for display_url, raw_url in README_URLS.items():
    content = fetch_readme_content(raw_url)
    if content:
        summary = summarize_readme(content)
        if summary:
            store_summary_in_db(summary, raw_url)
