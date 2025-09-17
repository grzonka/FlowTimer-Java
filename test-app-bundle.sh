#!/bin/bash

# Test script for FlowTimer app bundle validation
# This script validates the app bundle structure and basic functionality

set -e

APP_BUNDLE="FlowTimer.app"
SUCCESS_COLOR='\033[0;32m'
ERROR_COLOR='\033[0;31m'
INFO_COLOR='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${INFO_COLOR}FlowTimer App Bundle Validation${NC}"
echo "=================================="

# Check if app bundle exists
if [ ! -d "$APP_BUNDLE" ]; then
    echo -e "${ERROR_COLOR}ERROR: $APP_BUNDLE not found${NC}"
    echo "Please run ./create-app.sh first"
    exit 1
fi

echo -e "${SUCCESS_COLOR}✓ App bundle exists${NC}"

# Check bundle structure
required_paths=(
    "$APP_BUNDLE/Contents"
    "$APP_BUNDLE/Contents/Info.plist"
    "$APP_BUNDLE/Contents/MacOS"
    "$APP_BUNDLE/Contents/MacOS/FlowTimer"
    "$APP_BUNDLE/Contents/Resources"
    "$APP_BUNDLE/Contents/Resources/FlowTimer.jar"
)

for path in "${required_paths[@]}"; do
    if [ -e "$path" ]; then
        echo -e "${SUCCESS_COLOR}✓ $path exists${NC}"
    else
        echo -e "${ERROR_COLOR}✗ $path missing${NC}"
        exit 1
    fi
done

# Check launcher script is executable
if [ -x "$APP_BUNDLE/Contents/MacOS/FlowTimer" ]; then
    echo -e "${SUCCESS_COLOR}✓ Launcher script is executable${NC}"
else
    echo -e "${ERROR_COLOR}✗ Launcher script is not executable${NC}"
    exit 1
fi

# Validate Info.plist structure (basic XML check)
if grep -q "<?xml version" "$APP_BUNDLE/Contents/Info.plist" && grep -q "</plist>" "$APP_BUNDLE/Contents/Info.plist"; then
    echo -e "${SUCCESS_COLOR}✓ Info.plist has valid XML structure${NC}"
else
    echo -e "${ERROR_COLOR}✗ Info.plist has invalid XML structure${NC}"
    exit 1
fi

# Check bundle identifier
if grep -q "com.github.stringflow.flowtimer" "$APP_BUNDLE/Contents/Info.plist"; then
    echo -e "${SUCCESS_COLOR}✓ Bundle identifier is correct${NC}"
else
    echo -e "${ERROR_COLOR}✗ Bundle identifier is incorrect or missing${NC}"
    exit 1
fi

# Check JAR file integrity
if unzip -t "$APP_BUNDLE/Contents/Resources/FlowTimer.jar" &>/dev/null; then
    echo -e "${SUCCESS_COLOR}✓ JAR file is valid${NC}"
else
    echo -e "${ERROR_COLOR}✗ JAR file is corrupted${NC}"
    exit 1
fi

# Check for required native libraries in JAR
required_natives=(
    "com/github/kwhat/jnativehook/lib/darwin/arm64/libJNativeHook.dylib"
    "com/github/kwhat/jnativehook/lib/darwin/x86_64/libJNativeHook.dylib"
)

for native in "${required_natives[@]}"; do
    if unzip -l "$APP_BUNDLE/Contents/Resources/FlowTimer.jar" | grep -q "$native"; then
        echo -e "${SUCCESS_COLOR}✓ Found $native${NC}"
    else
        echo -e "${ERROR_COLOR}✗ Missing $native${NC}"
        exit 1
    fi
done

# Test launcher script syntax
if bash -n "$APP_BUNDLE/Contents/MacOS/FlowTimer"; then
    echo -e "${SUCCESS_COLOR}✓ Launcher script syntax is valid${NC}"
else
    echo -e "${ERROR_COLOR}✗ Launcher script has syntax errors${NC}"
    exit 1
fi

echo ""
echo -e "${SUCCESS_COLOR}🎉 All tests passed! App bundle is ready for deployment.${NC}"
echo ""
echo "Installation instructions:"
echo "1. Copy the app bundle to Applications:"
echo "   cp -r FlowTimer.app /Applications/"
echo ""
echo "2. Grant accessibility permissions in System Settings:"
echo "   System Settings > Privacy & Security > Accessibility > Add FlowTimer"
echo ""
echo "3. Launch from Spotlight by searching 'FlowTimer'"
