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
    <key>LSMinimumSystemVersion</key>
    <string>10.15</string>
    <key>NSAppleEventsUsageDescription</key>
    <string>FlowTimer requires access to system events for global keyboard shortcuts.</string>
    <key>NSMicrophoneUsageDescription</key>
    <string>FlowTimer may use the microphone for audio feedback features.</string>
    <key>LSSupportsOpeningDocumentsInPlace</key>
    <false/>
    <key>NSRequiresAquaSystemAppearance</key>
    <false/>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.productivity</string>
</dict>
</plist>
EOF

# Copy the JAR file
cp build/libs/FlowTimer.jar "${APP_BUNDLE}/Contents/Resources/"

# Copy necessary resources that FlowTimer expects
cp -r res/ "${APP_BUNDLE}/Contents/Resources/"
cp -r lib/ "${APP_BUNDLE}/Contents/Resources/"

# Copy the icon if it exists, otherwise use a default
if [ -f "res/image/icon.png" ]; then
    cp res/image/icon.png "${APP_BUNDLE}/Contents/Resources/icon.icns"
else
    echo "Warning: Icon file not found, app will use default icon"
fi

# Create launcher script
cat > "${APP_BUNDLE}/Contents/MacOS/FlowTimer" << 'EOF'
#!/bin/bash

# FlowTimer macOS App Bundle Launcher
# Supports both Intel and Apple Silicon Macs

# Debug log file for troubleshooting
LOG_FILE="/tmp/flowtimer_debug.log"
exec > >(tee -a "$LOG_FILE") 2>&1

echo "$(date): FlowTimer launcher started"
echo "Arguments: $@"
echo "Architecture: $(uname -m)"
echo "macOS Version: $(sw_vers -productVersion)"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Launcher directory: $DIR"

RESOURCES_DIR="${DIR}/../Resources"
echo "Resources directory: $RESOURCES_DIR"

if [ ! -d "$RESOURCES_DIR" ]; then
    echo "ERROR: Resources directory not found at $RESOURCES_DIR"
    osascript -e 'display alert "FlowTimer Error" message "Application resources not found. Please reinstall FlowTimer." as critical'
    exit 1
fi

# Find Java - try multiple locations and validate version
JAVA_CMD=""
MIN_JAVA_VERSION=17

find_java() {
    local java_paths=(
        "$(command -v java 2>/dev/null)"
        "/usr/bin/java"
        "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
        "/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"
        "$(/usr/libexec/java_home -v $MIN_JAVA_VERSION 2>/dev/null)/bin/java"
    )
    
    for java_path in "${java_paths[@]}"; do
        if [ -n "$java_path" ] && [ -x "$java_path" ]; then
            # Check Java version
            local version_output=$("$java_path" -version 2>&1 | head -n 1)
            local version_num=$("$java_path" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
            
            if [ -n "$version_num" ] && [ "$version_num" -ge "$MIN_JAVA_VERSION" ]; then
                echo "Found compatible Java at: $java_path"
                echo "Java version: $version_output"
                JAVA_CMD="$java_path"
                return 0
            else
                echo "Java at $java_path is too old (version $version_num, need $MIN_JAVA_VERSION+)"
            fi
        fi
    done
    return 1
}

if ! find_java; then
    echo "ERROR: Java $MIN_JAVA_VERSION+ not found"
    osascript -e 'display alert "Java Required" message "FlowTimer requires Java 17 or later. Please install Java and try again.\n\nRecommended: Download from https://adoptium.net/" as critical buttons {"OK"}'
    exit 1
fi

# Change to the Resources directory where all the FlowTimer files are located
cd "$RESOURCES_DIR"
echo "Changed to directory: $(pwd)"

# Check if JAR file exists
if [ ! -f "FlowTimer.jar" ]; then
    echo "ERROR: FlowTimer.jar not found in $(pwd)"
    osascript -e 'display alert "FlowTimer Error" message "FlowTimer.jar not found. Please reinstall FlowTimer." as critical'
    exit 1
fi

echo "Starting FlowTimer..."
echo "Working directory: $(pwd)"

# macOS-specific JVM arguments
JVM_ARGS=(
    "-Djava.awt.headless=false"
    "-Dapple.laf.useScreenMenuBar=true"
    "-Dcom.apple.mrj.application.apple.menu.about.name=FlowTimer"
    "-Dapple.awt.application.name=FlowTimer"
)

# Apple Silicon specific optimizations
if [[ "$(uname -m)" == "arm64" ]]; then
    echo "Detected Apple Silicon, using ARM64 optimizations"
    JVM_ARGS+=("-XX:+UnlockExperimentalVMOptions" "-XX:+UseZGC")
fi

echo "JVM arguments: ${JVM_ARGS[*]}"

# Run FlowTimer
exec "$JAVA_CMD" "${JVM_ARGS[@]}" -jar FlowTimer.jar "$@"
EOF

# Make launcher executable
chmod +x "${APP_BUNDLE}/Contents/MacOS/FlowTimer"

echo "Created ${APP_BUNDLE}"
echo ""
echo "To install:"
echo "cp -r ${APP_BUNDLE} /Applications/"
echo ""
echo "Then you can run it from Spotlight by searching 'FlowTimer'"
