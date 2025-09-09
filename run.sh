#!/bin/bash

# FlowTimer Launcher Script for macOS
# This script handles the Apple Silicon native libraries and macOS-specific JVM flags

JAR_PATH="$(dirname "$0")/build/libs/FlowTimer.jar"

# Check if JAR exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: FlowTimer.jar not found at $JAR_PATH"
    echo "Please run ./build.sh first to build the application."
    exit 1
fi

# Check if running on Apple Silicon
ARCH=$(uname -m)
echo "Running FlowTimer on $ARCH architecture..."

# On macOS, we need special JVM flags for GUI applications
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "Starting FlowTimer with macOS-specific settings..."
    echo "Note: FlowTimer requires accessibility permissions for global key capture."
    echo "If prompted, please grant accessibility permissions in System Settings > Privacy & Security > Accessibility"
    echo ""
    
    # The -XstartOnFirstThread flag can cause hangs, so we'll omit it
    java -Djava.awt.headless=false \
         -jar "$JAR_PATH"
else
    echo "Starting FlowTimer..."
    java -jar "$JAR_PATH"
fi
