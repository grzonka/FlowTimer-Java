# FlowTimer - Modernized for Apple Silicon

A Java-based timer application with global key hooks and OpenAL sound support, now fully compatible with Apple Silicon (ARM64) Macs.

## Quick Start

### Prerequisites
- Java 17 or higher
- macOS 10.14+ (for Apple Silicon support)

### Building and Running

1. **Easy build and run:**
   ```bash
   ./build.sh
   ```

2. **Or build manually:**
   ```bash
   gradle clean build
   ```

3. **Run the application:**
   ```bash
   ./run.sh
   ```
   
   Or manually:
   ```bash
   java -XstartOnFirstThread -jar build/libs/FlowTimer.jar
   ```

## What's New in 2.0

### ✅ Apple Silicon Support
- **Native ARM64 libraries**: Updated to LWJGL 3.3.3 with full Apple Silicon support
- **JNativeHook 2.2.2**: Latest version with ARM64 compatibility

### ✅ Modern Build System
- **Gradle build**: Automatic dependency management and native library selection
- **Cross-platform**: Automatically detects your OS and architecture
- **Fat JAR**: Single executable JAR with all dependencies included

### ✅ Updated Dependencies
- **LWJGL 3.x**: Modern OpenAL implementation with better performance
- **JNativeHook 2.2.2**: Updated global key capture with better macOS support
- **Java 17**: Modern Java with improved performance and security

## macOS Permissions

FlowTimer needs **Accessibility permissions** to capture global key events:

1. When you first run FlowTimer, macOS will prompt for accessibility permissions
2. Go to **System Settings** > **Privacy & Security** > **Accessibility**
3. Enable permissions for **Terminal** or **Java** (depending on how you're running it)

## Project Structure

```
FlowTimer-Java/
├── build.gradle          # Gradle build configuration
├── pom.xml               # Maven build configuration (alternative)
├── build.sh              # Build script (auto-detects Gradle/Maven)
├── run.sh                # Launch script for macOS
├── src/                  # Java source code
├── res/                  # Resources (images, sounds)
├── build/libs/           # Generated JAR files
└── README.md            # This file
```

## Architecture Support

| OS | Architecture | Status |
|----|-------------|--------|
| macOS | ARM64 (Apple Silicon) | ✅ Native |
| macOS | x86_64 (Intel) | ✅ Native |
| Windows | x86_64 | ✅ Native |
| Linux | x86_64 | ✅ Native |

## Development

### Building from Source
```bash
# Clone the repository
git clone [repository-url]
cd FlowTimer-Java

# Build with Gradle
gradle clean build

# Or build with Maven
mvn clean package

# Run
./run.sh
```

### IDE Setup
Import as a Gradle or Maven project in your IDE. The build files automatically handle:
- Native library selection based on your OS/architecture
- Resource inclusion
- Main class configuration

## Troubleshooting

### "AccessibilityPermissions" Error
- Grant accessibility permissions in System Settings
- Restart Terminal/IDE after granting permissions

### Performance Issues
- The Apple Silicon version should perform significantly better than the old Rosetta version
- Make sure you're running the native ARM64 build, not through Rosetta

## Original Project

This is a modernized version of the original FlowTimer project, updated for:
- Apple Silicon compatibility
- Modern Java versions
- Current dependency versions
- Simplified build process

The original functionality remains unchanged - this update focuses on compatibility and ease of use.

## VIDEO TUTORIAL

[![Video Tutorial](http://img.youtube.com/vi/hF1zg31TAxk/0.jpg)](http://www.youtube.com/watch?v=hF1zg31TAxk)
