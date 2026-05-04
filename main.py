import os
import sys
from dotenv import load_dotenv
from llm_engine import generate_flutter_app
from flutter_manager import create_flutter_project, inject_code, build_apk

def main():
    # Load environment variables from .env file
    load_dotenv()

    # Define paths
    idea_file = "idea.txt"
    app_name = "generated_app"

    # Check if idea file exists
    if not os.path.exists(idea_file):
        print(f"Error: Could not find {idea_file}.")
        print("Please create an idea.txt file with your app's UI and idea description.")
        sys.exit(1)

    # Read the idea
    with open(idea_file, "r", encoding="utf-8") as f:
        idea_text = f.read()

    print("--- App Builder Started ---")
    print(f"Reading idea from {idea_file}...")

    try:
        # Step 1: Generate Code via LLM
        generated_data = generate_flutter_app(idea_text)
        dart_code = generated_data.get("main_dart")
        dependencies = generated_data.get("pubspec_dependencies")

        if not dart_code:
            print("Error: The LLM did not generate valid dart code.")
            sys.exit(1)

        # Step 2: Scaffold Flutter Project
        create_flutter_project(app_name)

        # Step 3: Inject Code and Dependencies
        inject_code(app_name, dart_code, dependencies)

        # Step 4: Build APK
        build_apk(app_name)

        print("\n--- Success! ---")
        print(f"Your application has been built successfully.")
        
    except Exception as e:
        print(f"\nAn error occurred during the build process: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
