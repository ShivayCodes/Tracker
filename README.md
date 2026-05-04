# App Builder 🚀

An open-source Python-based Application Builder that leverages the power of Large Language Models (LLMs) to automatically generate, scaffold, and compile Flutter applications for Android in minutes. Simply describe your app's UI and idea in a text file, and watch the magic happen!

## Features

- **AI Code Generation**: Uses Google's Gemini AI to write complete `main.dart` code and generate `pubspec.yaml` dependencies based on your natural language description.
- **Automated Scaffolding**: Automatically creates a fresh Flutter project using the Flutter CLI.
- **Code Injection**: Seamlessly injects the AI-generated Dart code and required dependencies into the project.
- **Automated Build System**: Runs the Android build process to output a ready-to-install `.apk` file.

## Prerequisites

Before running the App Builder, ensure you have the following installed on your system:

1. **Python 3.8+**
2. **Flutter SDK**: Installed and added to your system's PATH.
3. **Android SDK & Toolchain**: Required for building the APK. 
   - *Tip: Run `flutter doctor` in your terminal to verify your environment is correctly set up.*
4. **Google Gemini API Key**: You can get a free API key from [Google AI Studio](https://aistudio.google.com/).

## Installation

1. **Clone the repository** (or download the source code):
   ```bash
   git clone <your-repo-url>
   cd Tracker
   ```

2. **Install Python Dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

3. **Configure Environment Variables**:
   Open the `.env` file in the root directory and add your Gemini API key:
   ```env
   GEMINI_API_KEY="your_api_key_here"
   ```

## Usage

1. **Describe Your App**:
   Open `idea.txt` and write down a detailed description of your desired application. Be as specific as possible about the UI layout, colors, and functionality.
   *Example:*
   > A simple counter application with a beautiful dark theme, using a Floating Action Button to increment the counter and a text display in the center of the screen showing the current count.

2. **Run the Builder**:
   Execute the orchestrator script:
   ```bash
   python main.py
   ```

3. **Get Your APK**:
   The script will print its progress to the console. Once the build is complete, you can find your generated Android APK at:
   `generated_app/build/app/outputs/flutter-apk/app-release.apk`

## Architecture

- `main.py`: The main entry point that reads the idea, orchestrates the LLM, and manages the build flow.
- `llm_engine.py`: Handles API communication with Google Gemini, utilizing prompt engineering to extract structured Dart code and YAML dependencies.
- `flutter_manager.py`: A Python wrapper around the Flutter CLI for creating the project, injecting code, and executing build commands.

## Limitations

- The quality of the generated application relies heavily on the capabilities of the LLM. Complex apps with multiple screens might require manual tweaks.
- Currently designed to output monolithic `main.dart` files.
- Build times depend on your local machine's performance and network speed (for downloading Flutter dependencies).

## License

This project is open-source and available under the MIT License.
