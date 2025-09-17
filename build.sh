#!/bin/bash

# FlowTimer Build and Run Script

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}FlowTimer Build Script${NC}"
echo "======================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed or not in PATH${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Error: Java 17 or higher is required. Found Java $JAVA_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}Using Java version: $(java -version 2>&1 | head -n 1)${NC}"
echo -e "${GREEN}Architecture: $(uname -m)${NC}"
echo ""

# Check build system preference
BUILD_SYSTEM=""
if [ "$1" = "maven" ] || [ "$1" = "mvn" ]; then
    BUILD_SYSTEM="maven"
elif [ "$1" = "gradle" ]; then
    BUILD_SYSTEM="gradle"
elif command -v ./gradlew &> /dev/null; then
    BUILD_SYSTEM="gradle"
elif command -v gradle &> /dev/null; then
    BUILD_SYSTEM="gradle"
elif command -v mvn &> /dev/null; then
    BUILD_SYSTEM="maven"
else
    echo -e "${RED}Error: Neither Gradle nor Maven is available${NC}"
    echo "Please install either Gradle or Maven, or specify the build system:"
    echo "  $0 gradle   # Use Gradle"
    echo "  $0 maven    # Use Maven"
    exit 1
fi

echo -e "${BLUE}Using build system: $BUILD_SYSTEM${NC}"
echo ""

# Build with selected system
if [ "$BUILD_SYSTEM" = "gradle" ]; then
    echo -e "${BLUE}Building with Gradle...${NC}"
    if [ -f "./gradlew" ]; then
        ./gradlew clean build
        BUILD_SUCCESS=$?
        JAR_FILE="build/libs/FlowTimer.jar"
    else
        gradle clean build
        BUILD_SUCCESS=$?
        JAR_FILE="build/libs/FlowTimer.jar"
    fi
else
    echo -e "${BLUE}Building with Maven...${NC}"
    mvn clean package
    BUILD_SUCCESS=$?
    JAR_FILE="target/FlowTimer.jar"
fi

if [ $BUILD_SUCCESS -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi

# Check if JAR was created
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR file not found at $JAR_FILE${NC}"
    exit 1
fi

echo -e "${GREEN}Build successful!${NC}"
echo -e "${GREEN}JAR file created: $JAR_FILE${NC}"
echo ""

# Ask if user wants to run the application
read -p "Do you want to run FlowTimer now? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}Starting FlowTimer...${NC}"
    echo "Note: FlowTimer requires accessibility permissions to capture global key events."
    echo "If prompted, please grant accessibility permissions in System Preferences."
    echo ""
    
    # Run with appropriate JVM arguments for macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        java -Djava.awt.headless=false -jar "$JAR_FILE"
    else
        java -jar "$JAR_FILE"
    fi
fi
