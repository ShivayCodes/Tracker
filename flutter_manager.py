import os
import subprocess
import shutil

def run_command(command, cwd=None):
    """Runs a shell command and streams the output."""
    print(f"Running: {' '.join(command)}")
    process = subprocess.Popen(
        command,
        cwd=cwd,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        shell=True if os.name == 'nt' else False
    )

    for line in process.stdout:
        print(line, end="")

    process.wait()
    if process.returncode != 0:
        raise RuntimeError(f"Command failed with return code {process.returncode}: {' '.join(command)}")

def create_flutter_project(app_name="generated_app"):
    """Creates a new Flutter project."""
    if os.path.exists(app_name):
        print(f"Cleaning up existing project directory: {app_name}")
        shutil.rmtree(app_name, ignore_errors=True)

    # Use shell=True for flutter on Windows
    command = ["flutter", "create", app_name]
    run_command(command)

def inject_code(app_name, dart_code, dependencies):
    """Writes the generated code into the Flutter project."""
    main_dart_path = os.path.join(app_name, "lib", "main.dart")
    
    # Write main.dart
    print(f"Writing generated code to {main_dart_path}")
    with open(main_dart_path, "w", encoding="utf-8") as f:
        f.write(dart_code)

    # Append dependencies to pubspec.yaml
    if dependencies:
        pubspec_path = os.path.join(app_name, "pubspec.yaml")
        print(f"Updating {pubspec_path} with dependencies...")
        with open(pubspec_path, "a", encoding="utf-8") as f:
            f.write("\n")
            f.write(dependencies)
            f.write("\n")
        
        # Run flutter pub get
        run_command(["flutter", "pub", "get"], cwd=app_name)

def build_apk(app_name):
    """Builds the Android APK."""
    print("Building Android APK...")
    run_command(["flutter", "build", "apk"], cwd=app_name)
    print(f"Build complete! APK should be in {app_name}\\build\\app\\outputs\\flutter-apk\\app-release.apk")
