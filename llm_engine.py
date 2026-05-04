import os
import re
from google import genai
from google.genai import types

def extract_code_block(text, language="dart"):
    """Extracts code block from markdown string."""
    pattern = rf"```{language}\n(.*?)```"
    match = re.search(pattern, text, re.DOTALL)
    if match:
        return match.group(1).strip()
    return ""

def generate_flutter_app(idea_text):
    """
    Calls the Gemini API to generate the Flutter code based on the user's idea.
    It expects the LLM to return `main.dart` code and `pubspec.yaml` dependencies.
    """
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key or api_key == "your_api_key_here":
        raise ValueError("Please set a valid GEMINI_API_KEY in the .env file.")

    client = genai.Client(api_key=api_key)

    system_instruction = """
    You are an expert Flutter Developer. Your task is to generate the complete code for a Flutter application based on a user's idea.
    
    You must output exactly TWO markdown code blocks:
    1. A `yaml` block containing ONLY the `dependencies` section needed for the `pubspec.yaml` (excluding the flutter/sdk part which is already there, just additional packages like provider, http, etc.). If none are needed, you can leave it empty.
    2. A `dart` block containing the COMPLETE, ready-to-run `main.dart` file code.
    
    Do NOT output any other text or explanations.
    """

    prompt = f"User's App Idea & UI:\n{idea_text}"

    print("Generating Flutter code using Gemini API...")
    
    response = client.models.generate_content(
        model='gemini-2.5-pro',
        contents=prompt,
        config=types.GenerateContentConfig(
            system_instruction=system_instruction,
            temperature=0.2,
        ),
    )

    response_text = response.text
    
    yaml_deps = extract_code_block(response_text, "yaml")
    dart_code = extract_code_block(response_text, "dart")

    if not dart_code:
        # Fallback if the LLM didn't use the exact markdown block
        print("Warning: Could not extract dart code block cleanly, trying generic extraction.")
        dart_code = response_text # might need to manually strip markdown

    return {
        "pubspec_dependencies": yaml_deps,
        "main_dart": dart_code
    }
