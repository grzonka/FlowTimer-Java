# FlowTimer

Frame-precise timing tool for speedrunning and competitive gaming.

## VIDEO TUTORIAL

[![Video Tutorial](http://img.youtube.com/vi/hF1zg31TAxk/0.jpg)](http://www.youtube.com/watch?v=hF1zg31TAxk)

## Installation

### macOS (Intel & Apple Silicon)

1. **Build the application:**
   ```bash
   ./build.sh gradle
   ```

2. **Create macOS app bundle:**
   ```bash
   ./create-app.sh
   ```

3. **Install to Applications folder:**
   ```bash
   cp -r FlowTimer.app /Applications/
   ```

4. **Grant accessibility permissions:**
   - Open System Settings → Privacy & Security → Accessibility
   - Click the "+" button and add FlowTimer
   - This is required for global keyboard shortcuts

5. **Launch from Spotlight:**
   - Press Cmd+Space and search "FlowTimer"

### Validation

Run the validation script to ensure the app bundle is correctly configured:
```bash
./test-app-bundle.sh
```

## Build

Requires Java 17+.

### Using Gradle
```bash
./build.sh gradle
# or
gradle clean build
java -jar build/libs/FlowTimer.jar
```

### Using Maven
```bash
./build.sh maven
# or  
mvn clean package
java -jar target/FlowTimer.jar
```

## Changes in 1.8.1

- Configurable timer display update rates
- Target time calculation for Variable Offset timer shows when specified frame occurs
- Native ARM64 support for Apple Silicon
- Updated dependencies: LWJGL 3.3.3, JNativeHook 2.2.2
- Improved macOS app bundle with robust launcher script
- Fixed Apple Silicon compatibility issues

## Usage

Three timer modes:
- Fixed Offset: Pre-calculated timing sequences  
- Variable Offset: Real-time frame targeting with target time display
- Offset Calibration: Timing calibration tool

Global key hooks require accessibility permissions on macOS.

## Troubleshooting

### App won't start from Applications folder
- Check the debug log: `tail -f /tmp/flowtimer_debug.log`
- Ensure Java 17+ is installed: `java -version`
- Verify accessibility permissions are granted

### Architecture issues on Apple Silicon
- The app includes native ARM64 libraries for both JNativeHook and LWJGL
- Launcher automatically detects architecture and applies optimizations
- No need for Rosetta 2 compatibility mode

## License

Original FlowTimer project, modernized for current hardware.
