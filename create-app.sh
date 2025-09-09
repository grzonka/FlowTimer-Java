#!/bin/bash

# FlowTimer.app creator script

APP_NAME="FlowTimer"
JAR_FILE="build/libs/FlowTimer.jar"
APP_BUNDLE="${APP_NAME}.app"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: $JAR_FILE not found. Run 'gradle clean build' first."
    exit 1
fi

# Create app bundle structure
mkdir -p "${APP_BUNDLE}/Contents/MacOS"
mkdir -p "${APP_BUNDLE}/Contents/Resources"

# Create Info.plist
cat > "${APP_BUNDLE}/Contents/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>FlowTimer</string>
    <key>CFBundleIdentifier</key>
    <string>com.github.stringflow.flowtimer</string>
    <key>CFBundleName</key>
    <string>FlowTimer</string>
    <key>CFBundleDisplayName</key>
    <string>FlowTimer</string>
    <key>CFBundleVersion</key>
    <string>1.8.1</string>
    <key>CFBundleShortVersionString</key>
    <string>1.8.1</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>CFBundleIconFile</key>
    <string>icon</string>
    <key>LSUIElement</key>
    <false/>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
EOF

# Copy the JAR file
cp build/libs/FlowTimer.jar "${APP_BUNDLE}/Contents/Resources/"

# Copy necessary resources that FlowTimer expects
cp -r res/ "${APP_BUNDLE}/Contents/Resources/"
cp -r lib/ "${APP_BUNDLE}/Contents/Resources/"

# Copy the ARM64 JNativeHook library (LWJGL will use its own built-in ARM64 libraries)
cp build/libs/libJNativeHook.arm64.dylib "${APP_BUNDLE}/Contents/Resources/"

# Copy the icon
cp res/image/icon.png "${APP_BUNDLE}/Contents/Resources/icon.icns"

# Create launcher script
cat > "${APP_BUNDLE}/Contents/MacOS/FlowTimer" << 'EOF'
#!/bin/bash

# Debug log file
LOG_FILE="/tmp/flowtimer_debug.log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "$(date): FlowTimer launcher started"
echo "Arguments: $@"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Launcher directory: $DIR"

RESOURCES_DIR="${DIR}/../Resources"
echo "Resources directory: $RESOURCES_DIR"

if [ ! -d "$RESOURCES_DIR" ]; then
    echo "ERROR: Resources directory not found at $RESOURCES_DIR"
    osascript -e 'display dialog "FlowTimer resources not found!" buttons {"OK"} default button "OK"'
    exit 1
fi

# Find Java - try common locations
JAVA_CMD=""
if command -v java &> /dev/null; then
    JAVA_CMD="java"
    echo "Found Java via command: $(which java)"
elif [ -f "/usr/bin/java" ]; then
    JAVA_CMD="/usr/bin/java"
    echo "Found Java at: /usr/bin/java"
elif [ -f "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java" ]; then
    JAVA_CMD="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
    echo "Found Java at plugin location"
else
    echo "ERROR: Java not found"
    osascript -e 'display dialog "Java 17+ is required but not found. Please install Java." buttons {"OK"} default button "OK"'
    exit 1
fi

# Check Java version
JAVA_VERSION=$("$JAVA_CMD" -version 2>&1 | head -n 1)
echo "Java version: $JAVA_VERSION"

# Change to the Resources directory where all the FlowTimer files are located
cd "$RESOURCES_DIR"
echo "Changed to directory: $(pwd)"

# Check if JAR file exists
if [ ! -f "FlowTimer.jar" ]; then
    echo "ERROR: FlowTimer.jar not found in $(pwd)"
    osascript -e 'display dialog "FlowTimer.jar not found!" buttons {"OK"} default button "OK"'
    exit 1
fi

echo "Starting FlowTimer..."
# Set the native library path for JNativeHook ARM64 library
# LWJGL will use its own built-in ARM64 libraries automatically
NATIVE_LIB_PATH="$(pwd)"
echo "Native library path: $NATIVE_LIB_PATH"

# Run FlowTimer with proper working directory and native library path
exec "$JAVA_CMD" -Djava.awt.headless=false -Djava.library.path="$NATIVE_LIB_PATH" -jar FlowTimer.jar "$@"
EOF

# Make launcher executable
chmod +x "${APP_BUNDLE}/Contents/MacOS/FlowTimer"

echo "Created ${APP_BUNDLE}"
echo ""
echo "To install:"
echo "cp -r ${APP_BUNDLE} /Applications/"
echo ""
echo "Then you can run it from Spotlight by searching 'FlowTimer'"
