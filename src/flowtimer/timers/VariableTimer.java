package flowtimer.timers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import flowtimer.FlowTimer;
import flowtimer.IntTextField;

public class VariableTimer extends BaseTimer {

	private static final long serialVersionUID = 8201416389693271334L;
	
	private VariableComponent<IntTextField> frameComponent;
	private VariableComponent<JComboBox<Float>> fpsComponent;
	private VariableComponent<IntTextField> offsetComponent;
	private VariableComponent<IntTextField> intervalComponent;
	private VariableComponent<IntTextField> numBeepsComponent;
	private JButton submitButton;
	private JLabel errorLabel;
	private JLabel targetTimeLabel;
	private JCheckBox frameDisplayMode;
	private JLabel offsetMsLabel;
	private JLabel frameAdjustmentLabel;
	private long timerStartTime;
	private int frameAdjustment = 0; // UP/DOWN adjustment to target frame

	public VariableTimer(FlowTimer flowtimer) {
		super(flowtimer);

		frameComponent = new VariableComponent<IntTextField>(0, "Frame", new IntTextField(false), 80, 20);
		fpsComponent = new VariableComponent<JComboBox<Float>>(1, "FPS", new JComboBox<Float>(new Float[] { 59.7275f, 59.8261f, 60.0f, 30.0f, 15.0f }), 80, 20);
		offsetComponent = new VariableComponent<IntTextField>(2, "Offset Frames", new IntTextField(true), 80, 20);
		intervalComponent = new VariableComponent<IntTextField>(3, "Interval", new IntTextField(false), 80, 20);
		numBeepsComponent = new VariableComponent<IntTextField>(4, "Beeps", new IntTextField(false), 80, 20);

		frameComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		fpsComponent.getComponent().addActionListener(e -> {
			updateTargetTimeDisplay();
			updateOffsetMsDisplay();
		});
		offsetComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				super.changedUpdate(e);
				updateOffsetMsDisplay();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				super.insertUpdate(e);
				updateOffsetMsDisplay();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				super.removeUpdate(e);
				updateOffsetMsDisplay();
			}
		});
		intervalComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		numBeepsComponent.getComponent().getDocument().addDocumentListener(new VariableElementDocumentListener());
		
		submitButton = new JButton("Submit");
		submitButton.setBounds(330, 26, 80, 22);
		submitButton.setEnabled(false);
		submitButton.addActionListener(e -> {
			long passedTime = (System.nanoTime() - flowtimer.getTimerStartTime()) / 1_000_000;
			long offsets[] = { getVariableOffset() - passedTime };
			int interval = intervalComponent.getComponent().getValue();
			int numBeeps = numBeepsComponent.getComponent().getValue();
			if(offsets[0] < interval * numBeeps) {
				Toolkit.getDefaultToolkit().beep();
				errorLabel.setText("Too much time has passed for that frame");
				new Thread(() -> {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					errorLabel.setText("");
				}).start();	
				return;
			}
			long universalOffset = getOffsetInMs();
			flowtimer.scheduleActions(offsets, interval, numBeeps, universalOffset);
			setVariableInterface(false);
			submitButton.setEnabled(false);
		});
		
		errorLabel = new JLabel();
		errorLabel.setFont(new Font("Default", 0, 12));
		errorLabel.setBounds(frameComponent.getLabel().getX(), frameComponent.getLabel().getY() - 15, 230, 22);
		errorLabel.setForeground(Color.RED);
		
		// Target time display label - centered horizontally above the form controls
		targetTimeLabel = new JLabel();
		targetTimeLabel.setFont(new Font("Consolas", Font.BOLD, 11));
		// Center horizontally across the full app width (451px), positioned above Frame/Submit
		targetTimeLabel.setBounds(125, 5, 200, 15);
		targetTimeLabel.setForeground(new Color(0, 120, 0)); // Dark green
		targetTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		targetTimeLabel.setText("Target: --:--.----- ");
		updateTargetTimeDisplay();
		
		// Frame display mode checkbox
		frameDisplayMode = new JCheckBox("Show Current Frame");
		frameDisplayMode.setBounds(10, 132, 150, 20);
		frameDisplayMode.addActionListener(e -> updateTimerDisplay());
		
		// Offset in MS display label (positioned next to the offset field)
		offsetMsLabel = new JLabel("(-- ms)");
		offsetMsLabel.setFont(new Font("Default", Font.ITALIC, 10));
		offsetMsLabel.setBounds(offsetComponent.getComponent().getX() + offsetComponent.getComponent().getWidth() + 5, 
							   offsetComponent.getComponent().getY(), 60, 20);
		offsetMsLabel.setForeground(Color.GRAY);
		
		// Frame adjustment display label (positioned next to the frame field)
		frameAdjustmentLabel = new JLabel("");
		frameAdjustmentLabel.setFont(new Font("Default", Font.BOLD, 11));
		frameAdjustmentLabel.setBounds(frameComponent.getComponent().getX() + frameComponent.getComponent().getWidth() + 5, 
									  frameComponent.getComponent().getY(), 60, 20);
		frameAdjustmentLabel.setForeground(new Color(0, 100, 200)); // Blue color
		
		// Click submit button when enter is hit while editing the frame text field
		frameComponent.getComponent().addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			
			public void keyReleased(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					submitButton.doClick();
				}
			}
		});
		
		frameComponent.add(this);
		fpsComponent.add(this);
		offsetComponent.add(this);
		intervalComponent.add(this);
		numBeepsComponent.add(this);
		add(submitButton);
		add(errorLabel);
		add(targetTimeLabel);
		add(frameDisplayMode);
		add(offsetMsLabel);
		add(frameAdjustmentLabel);
		
		// Initialize displays
		updateOffsetMsDisplay();
		updateFrameDisplay();
	}
	
	private boolean isVariableDataValid() {
		if(!intervalComponent.getComponent().getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!numBeepsComponent.getComponent().getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!frameComponent.getComponent().getText().matches("^0*[1-9]\\d*$")) {
			return false;
		}
		if(!offsetComponent.getComponent().getText().matches("^-?\\d+$")) {
			return false;
		}
		if(flowtimer.isTimerRunning()) {
			if(getVariableOffset() - (intervalComponent.getComponent().getValue() * numBeepsComponent.getComponent().getValue()) < flowtimer.getTimerStartTime() - System.nanoTime()) {
				return false;
			}
		}
		return true;
	}
	
	private long getVariableOffset() {
		// Parse the original frame from the input field
		String frameText = frameComponent.getComponent().getText().trim();
		int originalFrame = Integer.parseInt(frameText);
		
		// Calculate target time using adjusted frame
		int adjustedFrame = originalFrame + frameAdjustment;
		float fps = (Float) fpsComponent.getComponent().getSelectedItem();
		return (long) (adjustedFrame / fps * 1000.0f);
	}
	
	/**
	 * Calculates the offset in milliseconds based on offset frames and FPS
	 */
	private long getOffsetInMs() {
		try {
			int offsetFrames = Integer.parseInt(offsetComponent.getComponent().getText().trim());
			float fps = (Float) fpsComponent.getComponent().getSelectedItem();
			return (long) ((offsetFrames / fps) * 1000.0);
		} catch (NumberFormatException e) {
			return 0; // Default to 0 if parsing fails
		}
	}
	
	/**
	 * Updates the target time display based on current frame and FPS values
	 */
	private void updateTargetTimeDisplay() {
		try {
			String frameText = frameComponent.getComponent().getText().trim();
			if (frameText.isEmpty()) {
				targetTimeLabel.setText("Target: --:--.----- ");
				return;
			}
			
			// Parse original frame from input field
			int originalFrame = Integer.parseInt(frameText);
			
			// Calculate target time using adjusted frame
			int adjustedFrame = originalFrame + frameAdjustment;
			if (adjustedFrame <= 0) {
				targetTimeLabel.setText("Target: --:--.----- ");
				return;
			}
			
			float fps = (Float) fpsComponent.getComponent().getSelectedItem();
			double targetTimeSeconds = adjustedFrame / fps;
			
			// Format as MM:SS.SSSS
			int minutes = (int) (targetTimeSeconds / 60);
			double seconds = targetTimeSeconds % 60;
			
			String formattedTime = String.format("%02d:%06.4f", minutes, seconds);
			targetTimeLabel.setText("Target: " + formattedTime);
			
		} catch (Exception e) {
			// If any parsing fails, show placeholder
			targetTimeLabel.setText("Target: --:--.----- ");
		}
	}
	
	/**
	 * Updates the offset MS display based on current offset frames and FPS values
	 */
	private void updateOffsetMsDisplay() {
		try {
			if (offsetComponent.getComponent().getText().isEmpty()) {
				offsetMsLabel.setText("(-- ms)");
				return;
			}
			
			int offsetFrames = Integer.parseInt(offsetComponent.getComponent().getText().trim());
			float fps = (Float) fpsComponent.getComponent().getSelectedItem();
			double offsetMs = (offsetFrames / fps) * 1000.0;
			offsetMsLabel.setText(String.format("(%.2f ms)", offsetMs));
		} catch (NumberFormatException e) {
			offsetMsLabel.setText("(Invalid)");
		}
	}
	
	/**
	 * Updates the frame adjustment display label
	 */
	private void updateFrameDisplay() {
		if (frameAdjustment == 0) {
			// No adjustment, hide the label
			frameAdjustmentLabel.setText("");
		} else if (frameAdjustment > 0) {
			// Positive adjustment
			frameAdjustmentLabel.setText("+" + frameAdjustment);
		} else {
			// Negative adjustment
			frameAdjustmentLabel.setText(String.valueOf(frameAdjustment));
		}
	}
	
	/**
	 * Updates the timer display based on the frame display mode
	 */
	private void updateTimerDisplay() {
		if (frameDisplayMode.isSelected()) {
			// Show frame counter and update it with current frame
			flowtimer.setFrameCounterVisible(true);
			if (timerStartTime > 0) {
				long elapsedMs = System.currentTimeMillis() - timerStartTime;
				try {
					float fps = (Float) fpsComponent.getComponent().getSelectedItem();
					long currentFrame = Math.round((elapsedMs / 1000.0) * fps);
					flowtimer.setFrameCounterText(String.format("Frame: %,d", currentFrame));
				} catch (Exception e) {
					flowtimer.setFrameCounterText("Frame: --");
				}
			} else {
				flowtimer.setFrameCounterText("Frame: 0");
			}
		} else {
			// Hide frame counter
			flowtimer.setFrameCounterVisible(false);
		}
	}
	
	/**
	 * Called by TimerDisplayUpdater to update frame display during timer runs
	 */
	public void updateTimerDisplayFromUpdater() {
		updateTimerDisplay();
	}

	public void onLoad() {
		flowtimer.setTimerLabel(0);
		flowtimer.setFrameCounterVisible(false); // Hide frame counter on load
		flowtimer.setSize(FlowTimer.WIDTH, 228);
		updateTargetTimeDisplay(); // Update target time when tab loads
	}

	public void onTimerStart(long startTime) {
		timerStartTime = startTime;
		submitButton.setEnabled(true);
	}

	public void onTimerStop() {
		timerStartTime = 0;
		flowtimer.setTimerLabel(0);
		flowtimer.setFrameCounterVisible(false); // Hide frame counter when timer stops
		submitButton.setEnabled(false);
		errorLabel.setText("");
		setVariableInterface(true);
		
		// Reset frame adjustment and display
		frameAdjustment = 0;
		updateFrameDisplay();
		updateTargetTimeDisplay(); // Refresh display when timer stops
	}

	public void onKeyEvent(NativeKeyEvent e) {
		// Only handle UP/DOWN keys while timer is running
		if (!flowtimer.isTimerRunning()) {
			return;
		}
		
		// Get the configured UP and DOWN key codes
		int upKeyCode = flowtimer.getSettings().getUpInput().getPrimaryInput().getKeyCode();
		int downKeyCode = flowtimer.getSettings().getDownInput().getPrimaryInput().getKeyCode();
		
		// Check if UP or DOWN key was pressed
		if (e.getKeyCode() == upKeyCode) {
			adjustOffsetFrames(1); // Increment by 1 frame
		} else if (e.getKeyCode() == downKeyCode) {
			adjustOffsetFrames(-1); // Decrement by 1 frame
		}
	}
	
	/**
	 * Adjusts the target frame by the specified amount while timer is running
	 */
	private void adjustOffsetFrames(int adjustment) {
		try {
			// Adjust the target frame, not the offset
			frameAdjustment += adjustment;
			
			// Update the frame display to show original±adjustment
			updateFrameDisplay();
			
			// Update target time display with adjusted frame
			updateTargetTimeDisplay();
			
			// Reschedule actions with new target frame if timer is running
			if (flowtimer.isTimerRunning()) {
				// Do rescheduling in a separate thread to avoid blocking UI
				new Thread(() -> {
					rescheduleWithNewOffset();
				}, "FrameReschedule").start();
			}
			
		} catch (Exception e) {
			// If adjustment fails, just ignore
		}
	}
	
	/**
	 * Cancels current actions and reschedules with updated offset
	 */
	private void rescheduleWithNewOffset() {
		try {
			// Cancel existing scheduled actions (should be quick)
			flowtimer.cancelActiveActions();
			
			// Small delay to ensure cancellation is processed
			Thread.sleep(10);
			
			// Get current values - using same logic as submit button
			long passedTime = (System.nanoTime() - flowtimer.getTimerStartTime()) / 1_000_000;
			long remainingTimeToFrame = getVariableOffset() - passedTime;
			int interval = intervalComponent.getComponent().getValue();
			int numBeeps = numBeepsComponent.getComponent().getValue();
			long universalOffset = getOffsetInMs();
			
			// Check if we still have enough time for at least one beep
			// We need at least 'interval' milliseconds to make sense
			if (remainingTimeToFrame <= interval) {
				// Target time has passed or too close, don't reschedule
				System.out.println("Not rescheduling - target too close: " + remainingTimeToFrame + "ms remaining");
				return;
			}
			
			// Calculate how many beeps we can still fit
			int maxPossibleBeeps = (int) Math.max(1, remainingTimeToFrame / interval);
			int beepsToSchedule = Math.min(numBeeps, maxPossibleBeeps);
			
			// Only reschedule if we have reasonable time
			if (beepsToSchedule > 0 && remainingTimeToFrame >= beepsToSchedule * interval) {
				System.out.println("Rescheduling with " + beepsToSchedule + " beeps, " + remainingTimeToFrame + "ms remaining");
				long offsets[] = { remainingTimeToFrame };
				flowtimer.scheduleActions(offsets, interval, beepsToSchedule, universalOffset);
			} else {
				System.out.println("Not rescheduling - insufficient time for beeps");
			}
			
		} catch (Exception e) {
			// If rescheduling fails, just ignore but don't crash
			System.err.println("Rescheduling failed: " + e.getMessage());
		}
	}

	public void setInterface(boolean enabled) {
	}
	
	public void setVariableInterface(boolean enabled) {
		fpsComponent.setEnabled(enabled);
		offsetComponent.setEnabled(enabled);
		intervalComponent.setEnabled(enabled);
		numBeepsComponent.setEnabled(enabled);
		frameComponent.setEnabled(enabled);
	}
	
	public boolean canStartTimer() {
		return true;
	}
	
	public VariableComponent<IntTextField> getFrameComponent() {
		return frameComponent;
	}

	public VariableComponent<JComboBox<Float>> getFpsComponent() {
		return fpsComponent;
	}

	public VariableComponent<IntTextField> getOffsetComponent() {
		return offsetComponent;
	}

	public VariableComponent<IntTextField> getIntervalComponent() {
		return intervalComponent;
	}

	public VariableComponent<IntTextField> getNumBeepsComponent() {
		return numBeepsComponent;
	}

	public class VariableComponent<E extends JComponent> {
		
		public static final int X_BASE = 150;
		public static final int X_OFFSET = 50;
		public static final int Y_BASE = 20;
		public static final int Y_MARGIN = 25;

		private JLabel label;
		private E component;
		
		public VariableComponent(int index, String name, E component, int width, int height) {
			int y = Y_BASE + index * Y_MARGIN;
			label = new JLabel(name + ":");
			label.setBounds(X_BASE, y, X_OFFSET - 5, 35);
			this.component = component;
			component.setBounds(X_BASE + X_OFFSET, y + (35 - height) / 2, width, height);
		}
		
		public void add(JPanel parent) {
			parent.add(label);
			parent.add(component);
		}
		
		public void setEnabled(boolean enabled) {
			component.setEnabled(enabled);
		}

		public JLabel getLabel() {
			return label;
		}

		public E getComponent() {
			return component;
		}
	}

	public class VariableElementDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent e) {
			onChange();
		}

		public void removeUpdate(DocumentEvent e) {
			onChange();
		}

		public void changedUpdate(DocumentEvent e) {
			onChange();
		}

		private void onChange() {
			submitButton.setEnabled(flowtimer.isTimerRunning() && isVariableDataValid());
			updateTargetTimeDisplay();
		}
	}
}
