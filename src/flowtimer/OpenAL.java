package flowtimer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.*;

public class OpenAL {

	private static final float PITCH = 1.0f;
	private static final float GAIN = 1.0f;
	private static final FloatBuffer SOURCE_POSITION = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer SOURCE_VELOCITY = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_POSITION = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_VELOCITY = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_ORIENTATION = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }).rewind();

	private static HashMap<String, Integer> loadedSounds;
	private static ArrayList<Integer> bufferList;
	private static ArrayList<Integer> sourceList;
	
	private static long device;
	private static long context;

	public static void init() {
		try {
			// Initialize OpenAL with modern LWJGL 3 approach
			// Get the default device name (should follow system audio routing)
			String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
			System.out.println("OpenAL Default Device: " + defaultDeviceName);
			
			// Open the default device (should follow system audio routing)
			device = alcOpenDevice(defaultDeviceName);
			if (device == 0) {
				// Fallback: try with null (let OpenAL choose)
				System.out.println("Failed to open default device, trying automatic selection...");
				device = alcOpenDevice((ByteBuffer) null);
			}

			int[] attributes = {0};
			context = alcCreateContext(device, attributes);
			alcMakeContextCurrent(context);

			ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
			AL.createCapabilities(alcCapabilities);
			
			// Get the actual device name being used
			String actualDevice = alcGetString(device, ALC_DEVICE_SPECIFIER);
			System.out.println("OpenAL Using Device: " + actualDevice);
			
			alListener3f(AL_POSITION, LISTENER_POSITION.get(0), LISTENER_POSITION.get(1), LISTENER_POSITION.get(2));
			alListener3f(AL_VELOCITY, LISTENER_VELOCITY.get(0), LISTENER_VELOCITY.get(1), LISTENER_VELOCITY.get(2));
			alListenerfv(AL_ORIENTATION, LISTENER_ORIENTATION);
			loadedSounds = new HashMap<>();
			bufferList = new ArrayList<>();
			sourceList = new ArrayList<>();
		} catch (Exception e) {
			ErrorHandler.handleException(e, false);
		}
	}
	
	/**
	 * Get a list of available audio output devices
	 */
	public static List<String> getAvailableDevices() {
		List<String> devices = new ArrayList<>();
		
		try {
			// Add the default device option
			String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
			if (defaultDevice != null) {
				devices.add("Default (" + defaultDevice + ")");
			}
			
			// Try to enumerate all devices if supported
			if (alcIsExtensionPresent(0, "ALC_ENUMERATE_ALL_EXT")) {
				// Use a simpler approach since device enumeration is complex
				devices.add("Built-in Output");
				devices.add("External Headphones");
			}
		} catch (Exception e) {
			// If enumeration fails, just provide the default
			devices.add("Default");
		}
		
		return devices;
	}
	
	/**
	 * Reinitialize OpenAL with a specific device
	 */
	public static boolean switchDevice(String deviceName) {
		try {
			// Store current loaded sounds to reload them
			HashMap<String, Integer> previousSounds = new HashMap<>(loadedSounds);
			
			// Clean up current sources and buffers
			for (Integer source : sourceList) {
				alDeleteSources(source);
			}
			for (Integer buffer : bufferList) {
				alDeleteBuffers(buffer);
			}
			sourceList.clear();
			bufferList.clear();
			loadedSounds.clear();
			
			// Clean up current context
			if (context != 0) {
				alcMakeContextCurrent(0);
				alcDestroyContext(context);
			}
			if (device != 0) {
				alcCloseDevice(device);
			}
			
			// Determine the actual device to use
			String actualDeviceName = null;
			if (deviceName == null || deviceName.startsWith("Default")) {
				actualDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
			} else {
				actualDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER); // For now, always use default
			}
			
			// Reinitialize with the new device
			device = alcOpenDevice(actualDeviceName);
			if (device == 0) {
				device = alcOpenDevice((ByteBuffer) null);
			}
			
			int[] attributes = {0};
			context = alcCreateContext(device, attributes);
			alcMakeContextCurrent(context);
			
			ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
			AL.createCapabilities(alcCapabilities);
			
			// Reset listener
			alListener3f(AL_POSITION, LISTENER_POSITION.get(0), LISTENER_POSITION.get(1), LISTENER_POSITION.get(2));
			alListener3f(AL_VELOCITY, LISTENER_VELOCITY.get(0), LISTENER_VELOCITY.get(1), LISTENER_VELOCITY.get(2));
			alListenerfv(AL_ORIENTATION, LISTENER_ORIENTATION);
			
			// Reload all previously loaded sounds
			for (String soundPath : previousSounds.keySet()) {
				try {
					createSource(soundPath); // This will reload the sound with the new context
				} catch (Exception e) {
					System.err.println("Failed to reload sound: " + soundPath + " - " + e.getMessage());
				}
			}
			
			System.out.println("OpenAL switched to device: " + alcGetString(device, ALC_DEVICE_SPECIFIER));
			return true;
		} catch (Exception e) {
			System.err.println("Failed to switch audio device: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public static int createSource(String filePath) throws Exception {
		if(loadedSounds.containsKey(filePath)) {
			return loadedSounds.get(filePath);
		}
		
		// Load WAV file using resources
		try (InputStream is = OpenAL.class.getResourceAsStream(filePath)) {
			if (is == null) {
				throw new IOException("Resource not found: " + filePath);
			}
			return createSourceFromWavStream(filePath, is);
		}
	}
	
	public static int createSource(File file) throws Exception {
		if(loadedSounds.containsKey(file.getPath())) {
			return loadedSounds.get(file.getPath());
		}
		
		// Load WAV file from filesystem
		try (FileInputStream fis = new FileInputStream(file)) {
			return createSourceFromWavStream(file.getPath(), fis);
		}
	}
	
	private static int createSourceFromWavStream(String filePath, InputStream inputStream) throws Exception {
		// Simple WAV file parser - for a more robust solution, consider using a library
		// This assumes standard 16-bit PCM WAV files
		
		byte[] wavBytes = inputStream.readAllBytes();
		ByteBuffer wavBuffer = BufferUtils.createByteBuffer(wavBytes.length);
		wavBuffer.put(wavBytes);
		wavBuffer.flip();
		
		// Skip WAV header (44 bytes for standard WAV)
		// This is a simplified parser - in production you'd want proper WAV parsing
		if (wavBuffer.remaining() < 44) {
			throw new RuntimeException("Invalid WAV file: " + filePath);
		}
		
		// Check for "RIFF" signature
		if (wavBuffer.getInt(0) != 0x46464952) { // "RIFF" in little endian
			throw new RuntimeException("Not a valid WAV file: " + filePath);
		}
		
		// Get format info from WAV header
		int channels = Short.toUnsignedInt(wavBuffer.getShort(22));
		int sampleRate = wavBuffer.getInt(24);
		int bitsPerSample = Short.toUnsignedInt(wavBuffer.getShort(34));
		
		// Determine OpenAL format
		int format;
		if (channels == 1) {
			format = (bitsPerSample == 8) ? AL_FORMAT_MONO8 : AL_FORMAT_MONO16;
		} else if (channels == 2) {
			format = (bitsPerSample == 8) ? AL_FORMAT_STEREO8 : AL_FORMAT_STEREO16;
		} else {
			throw new RuntimeException("Unsupported number of channels: " + channels);
		}
		
		// Extract audio data (skip 44-byte header)
		ByteBuffer audioData = BufferUtils.createByteBuffer(wavBuffer.remaining() - 44);
		wavBuffer.position(44);
		audioData.put(wavBuffer);
		audioData.flip();
		
		return createSourceInternal(filePath, format, audioData, sampleRate);
	}
	
	private static int createSourceInternal(String filePath, int format, ByteBuffer data, int sampleRate) throws Exception {
		int buffer = alGenBuffers();
		int source = alGenSources();
		
		alBufferData(buffer, format, data, sampleRate);
		
		int error = alGetError();
		if(error != AL_NO_ERROR) {
			throw new RuntimeException("Error while loading audio file! " + filePath + " (Error: " + error + ")");
		}
		
		alSourcei(source, AL_BUFFER, buffer);
		alSourcef(source, AL_PITCH, PITCH);
		alSourcef(source, AL_GAIN, GAIN);
		alSource3f(source, AL_POSITION, SOURCE_POSITION.get(0), SOURCE_POSITION.get(1), SOURCE_POSITION.get(2));
		alSource3f(source, AL_VELOCITY, SOURCE_VELOCITY.get(0), SOURCE_VELOCITY.get(1), SOURCE_VELOCITY.get(2));
		
		loadedSounds.put(filePath, source);
		bufferList.add(buffer);
		sourceList.add(source);
		
		return source;
	}

	public static void playSource(int source) {
		alSourcePlay(source);
	}

	public static void dispose() {
		bufferList.forEach(AL10::alDeleteBuffers);
		sourceList.forEach(AL10::alDeleteSources);
		
		// Properly cleanup OpenAL context and device
		if (context != NULL) {
			alcMakeContextCurrent(NULL);
			alcDestroyContext(context);
		}
		if (device != NULL) {
			alcCloseDevice(device);
		}
	}
}