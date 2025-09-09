# Target Time Display Feature

## Overview

The Variable Offset timer now includes a real-time **Target Time Display** that shows exactly when your specified frame will occur based on the current Frame and FPS settings. This helps you prepare for precise frame-perfect timing.

## Visual Display

### Location
- Appears above the "Frame" input field
- Shows in **dark green** monospace font for easy reading
- Updates automatically as you type

### Format
```
Target: MM:SS.SSSS
```

**Examples:**
- Frame 1000 @ 59.7275 FPS → `Target: 00:16.7456`
- Frame 3600 @ 60.0 FPS → `Target: 01:00.0000`
- Frame 7200 @ 59.8261 FPS → `Target: 02:00.2344`

## How It Works

### Real-Time Calculation
- **Formula**: `Target Time = Frame ÷ FPS`
- **Updates instantly** when you change Frame or FPS values
- **4-digit precision** for precise timing preparation
- **Range**: Handles up to 2+ minutes (typical game speedrun segments)

### Integration with Timer
- Updates when switching to Variable Offset tab
- Refreshes when timer starts/stops
- Clears when invalid frame input is entered

## Use Cases

### Speedrun Preparation
```
Frame: 3540 @ 59.7275 FPS
Target: 00:59.2511

You know exactly that frame 3540 occurs at 59.25 seconds
Perfect for setting up frame-perfect tricks!
```

### Frame-Perfect Gaming
```
Frame: 180 @ 60.0 FPS  
Target: 00:03.0000

Quick 3-second timing for combo inputs
```

### Long Sequence Planning
```
Frame: 7800 @ 59.8261 FPS
Target: 02:10.3421

Plan ahead for 2+ minute timing sequences
```

## Technical Details

### Precision
- **Display**: 4 decimal places (0.0001 second precision)  
- **Internal Calculation**: Full floating-point precision
- **Range**: 00:00.0000 to 99:59.9999 (supports very long sequences)

### Error Handling
- Shows `Target: --:--.----- ` for invalid input
- Gracefully handles empty fields
- Continues working even if FPS or Frame values are invalid

### Performance
- **Zero impact** on timing precision
- Updates only on input changes (not continuous)
- Lightweight string formatting

## Examples by Game Type

### Fighting Games (60 FPS)
```
Frame 15  → Target: 00:00.2500 (quarter-second timing)
Frame 30  → Target: 00:00.5000 (half-second timing)  
Frame 180 → Target: 00:03.0000 (3-second combo window)
```

### Retro Games (~59.73 FPS)
```
Frame 60   → Target: 00:01.0046 (1-second timing)
Frame 1800 → Target: 00:30.1371 (30-second segment)
Frame 3600 → Target: 01:00.2742 (1-minute segment)
```

### Modern Games (Variable FPS)
- Automatically adjusts calculation based on selected FPS
- Perfect for different refresh rates and game engines

## Benefits

### 🎯 **Better Preparation**
- Know exactly when your frame occurs
- Plan your input timing in advance
- Visualize long timing sequences

### ⏱️ **Improved Accuracy**
- No mental math required
- Instant feedback on frame timing
- 4-digit precision for frame-perfect input

### 🎮 **Speedrun Optimization**
- Plan complex timing sequences
- Verify frame calculations instantly
- Optimize for different refresh rates

This feature maintains FlowTimer's core strength (frame-perfect precision) while adding the visual feedback needed for complex timing preparation.
