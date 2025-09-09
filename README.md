# FlowTimer

Frame-precise timing tool for speedrunning and competitive gaming.

## VIDEO TUTORIAL

[![Video Tutorial](http://img.youtube.com/vi/hF1zg31TAxk/0.jpg)](http://www.youtube.com/watch?v=hF1zg31TAxk)

## Build

Requires Java 17+.

```bash
gradle clean build
java -XstartOnFirstThread -jar build/libs/FlowTimer.jar
```

## Changes in 1.8.1

- Configurable timer display update rates
- Target time calculation for Variable Offset timer shows when specified frame occurs
- Native ARM64 support for Apple Silicon
- Updated dependencies: LWJGL 3.3.3, JNativeHook 2.2.2

## Usage

Three timer modes:
- Fixed Offset: Pre-calculated timing sequences  
- Variable Offset: Real-time frame targeting with target time display
- Offset Calibration: Timing calibration tool

Global key hooks require accessibility permissions on macOS.

## License

Original FlowTimer project, modernized for current hardware.
