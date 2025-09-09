package flowtimer.timers;

import java.util.concurrent.atomic.AtomicLong;
import javax.swing.SwingUtilities;

/**
 * High-performance timer display updater that maintains timing precision
 * while providing visual feedback at configurable intervals.
 */
public class TimerDisplayUpdater {
    
    public static enum UpdateRate {
        NONE(0),           // No visual updates (original behavior)
        CONSERVATIVE(1000), // Every 1 second
        BALANCED(250),     // Every 250ms  
        RESPONSIVE(100),   // Every 100ms
        SMOOTH(50);        // Every 50ms (should still be fine on modern hardware)
        
        private final long intervalMs;
        
        UpdateRate(long intervalMs) {
            this.intervalMs = intervalMs;
        }
        
        public long getIntervalMs() {
            return intervalMs;
        }
    }
    
    private final flowtimer.FlowTimer flowTimer;
    private volatile boolean isRunning = false;
    private AtomicLong startTime = new AtomicLong(0);
    private Thread updateThread;
    private UpdateRate currentRate = UpdateRate.BALANCED;
    
    public TimerDisplayUpdater(flowtimer.FlowTimer flowTimer) {
        this.flowTimer = flowTimer;
    }
    
    public void startUpdating(long timerStartTime, UpdateRate rate) {
        if (rate == UpdateRate.NONE) {
            return; // No visual updates
        }
        
        this.currentRate = rate;
        startTime.set(timerStartTime);
        isRunning = true;
        
        // Use a separate daemon thread to avoid interfering with timing precision
        updateThread = new Thread(this::updateLoop, "TimerDisplay-Update");
        updateThread.setDaemon(true);
        updateThread.setPriority(Thread.MIN_PRIORITY); // Low priority to not interfere with timing
        updateThread.start();
    }
    
    public void stopUpdating() {
        isRunning = false;
        if (updateThread != null) {
            updateThread.interrupt();
            updateThread = null;
        }
    }
    
    private void updateLoop() {
        long updateInterval = currentRate.getIntervalMs();
        
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                // Calculate elapsed time with nanosecond precision
                long currentTime = System.nanoTime();
                long elapsedNanos = currentTime - startTime.get();
                long elapsedMs = elapsedNanos / 1_000_000;
                
                // Update UI on EDT - but don't block if EDT is busy
                SwingUtilities.invokeLater(() -> {
                    if (isRunning) { // Double-check we're still running
                        flowTimer.setTimerLabel(elapsedMs);
                        
                        // If current timer is VariableTimer, also update frame display
                        BaseTimer currentTimer = flowTimer.getSelectedTimer();
                        if (currentTimer instanceof VariableTimer) {
                            VariableTimer variableTimer = (VariableTimer) currentTimer;
                            // Call updateTimerDisplay to handle frame counter updates
                            variableTimer.updateTimerDisplayFromUpdater();
                        }
                    }
                });
                
                // Sleep for the update interval
                Thread.sleep(updateInterval);
                
            } catch (InterruptedException e) {
                // Thread was interrupted, exit gracefully
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Log error but don't crash the update loop
                System.err.println("Timer display update error: " + e.getMessage());
            }
        }
    }
    
    public UpdateRate getCurrentRate() {
        return currentRate;
    }
    
    public void setUpdateRate(UpdateRate rate) {
        this.currentRate = rate;
        // If we're currently running, restart with new rate
        if (isRunning) {
            stopUpdating();
            startUpdating(startTime.get(), rate);
        }
    }
}
