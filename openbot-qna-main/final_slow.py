import json
import os
import streamlit as st
import google.generativeai as gen_ai
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Configure Streamlit page settings
st.set_page_config(
    page_title="OpenBot Chat",
    page_icon="🔍",
    layout="centered",
)

# Set up the Generative AI model
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
gen_ai.configure(api_key=GOOGLE_API_KEY)
model = gen_ai.GenerativeModel('gemini-pro')

# Load preprocessed summarized README content
@st.cache_resource
def load_preprocessed_summaries():
    try:
        with open('summarized_readmes.json', 'r') as f:
            return json.load(f)
    except Exception as e:
        st.error(f"Error loading preprocessed summaries: {e}")
        return {}

# Load the summarized content
summarized_readme_contents = load_preprocessed_summaries()

# Combine all summarized content into one string
combined_summary_content = "\n\n---\n\n".join([
    f"Summary from {url}:\n{summary}" for url, summary in summarized_readme_contents.items()
])

# Initialize session state for chat history if not already present
if "chat_history" not in st.session_state:
    st.session_state.chat_history = []

# CSS Styling for Chat UI
st.markdown("""
    <style>
        .response-card {
            background-color: #f9f9f9;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 15px;
            margin-top: 10px;
            color: #333;
        }
        .reference-text {
            font-size: 12px;
            color: #555;
        }
        .source-link {
            color: #0366d6;
            text-decoration: none;
            margin-right: 10px;
        }
        .source-link:hover {
            text-decoration: underline;
        }
    </style>
    """, unsafe_allow_html=True)

# Title of the Streamlit app
st.title("🔍 OpenBot Chat")

# Checkbox for debugging and displaying the combined summary content
#if st.checkbox("Show Summarized README Content"):
#    st.text_area("Combined Summarized README Content", combined_summary_content, height=200)

# User input area in the form
with st.form(key="user_input_form"):
    user_input = st.text_input(
        "Ask a question about OpenBot",
        placeholder="e.g., What is OpenBot?",
        key="user_input"
    )
    submit_button = st.form_submit_button("Ask")

# Function to check if the response contains a source link
def contains_source_link(response_text):
    """Check if the response contains a valid source URL."""
    return "Source:" in response_text and "http" or "github.com" in response_text

# Process user input and generate response
if submit_button and user_input:
    # Check if summarized content is loaded
    if not combined_summary_content:
        st.error("Could not load summarized README contents.")
        st.stop()

    # Save user input to the chat history
    st.session_state.chat_history.append(("user", user_input))

    # Split the summarized content into chunks if necessary (to avoid exceeding token limits)
    CHUNK_SIZE = 15000  # Adjust chunk size as needed to fit within token limits
    readme_chunks = [
        combined_summary_content[i:i + CHUNK_SIZE] 
        for i in range(0, len(combined_summary_content), CHUNK_SIZE)
    ]

    responses = []
    for chunk in readme_chunks:
        contextual_prompt = f"""Based on the following summarized README content chunk, please provide a detailed answer to the question. If the information comes from a specific README, include that source in your response:

{chunk}

Question: {user_input}

Please provide a comprehensive answer and cite which README file(s) the information comes from.
"""
        try:
            # Get the response from the AI model
            response = model.start_chat(history=[]).send_message(contextual_prompt)
            responses.append(response.text)
        except Exception as e:
            st.error(f"Error generating response for a chunk: {e}")
            continue

    # Filter responses to ensure valid sources are included
    valid_responses = [resp for resp in responses if contains_source_link(resp)]

    # If at least one response has a source, use it; otherwise, provide a fallback message
    if valid_responses:
        final_response = "\n\n---\n\n".join(valid_responses)
    else:
        final_response = "I could not find a direct answer. Try to search differently!"

    # Add the final response to chat history
    st.session_state.chat_history.append(("assistant", final_response))

# Display chat history (user and assistant messages)
for role, message in st.session_state.chat_history:
    if role == "user":
        st.markdown(f"""
            <div class="response-card">
                <strong>You:</strong>
                <p>{message}</p>
            </div>
            """, unsafe_allow_html=True)
    else:
        st.markdown(f"""
            <div class="response-card">
                <strong>OpenBot:</strong>
                <p>{message}</p>
            </div>
            """, unsafe_allow_html=True)
