package com.scn.devicemanagement;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.app.AppPreferences;
import com.scn.logger.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by steve on 2017. 09. 07..
 */

@Singleton
final class InfraRedDeviceManager extends SpecificDeviceManager {

    //
    // Private members
    //

    private static final String TAG = InfraRedDeviceManager.class.getSimpleName();

    private static final int IR_FREQUENCY = 38000;

    private static final int IR_MARK_MS = 158;
    private static final int IR_START_STOP_GAP_MS = 1026;
    private static final int IR_ONE_GAP_MS = 553;
    private static final int IR_ZERO_GAP_MS = 263;

    private static final int IR_MARK_CYCLES = 6;
    private static final int IR_START_STOP_GAP_CYCLES = 39;
    private static final int IR_ONE_GAP_CYCLES = 21;
    private static final int IR_ZERO_GAP_CYCLES = 10;
    private static final int IR_LEAD_AND_TAIL_GAP_CYCLES = 30;
    private static final int IR_MAX_MESSAGE_CYCLES = 2 * (IR_MARK_CYCLES + IR_START_STOP_GAP_CYCLES) + 16 * (IR_MARK_CYCLES + IR_ONE_GAP_CYCLES) + 2 * IR_LEAD_AND_TAIL_GAP_CYCLES;

    private final AppPreferences appPreferences;
    private final ConsumerIrManager irManager;

    private int numConnectedDevices = 0;

    private Thread irThread = null;
    private final Object irThreadLock = new Object();

    private final int outputValues[][] = new int[4][2];
    private final boolean isContinueSending[] = new boolean[4];

    private final int irData[] = new int[18 * 2];
    private final byte[] audioBuffer;

    //
    // Constructor
    //

    @Inject
    InfraRedDeviceManager(@NonNull Context context, @NonNull AppPreferences appPreferences) {
        super(context);
        Logger.i(TAG, "constructor...");

        this.appPreferences = appPreferences;

        ConsumerIrManager irManager = null;
        try {
            irManager = (ConsumerIrManager)context.getSystemService(Context.CONSUMER_IR_SERVICE);
            if (irManager != null && irManager.hasIrEmitter()) {
                Logger.i(TAG, "  Supported frequency ranges:");
                boolean isIrFrequencySupported = false;

                ConsumerIrManager.CarrierFrequencyRange frequencyRanges[] = irManager.getCarrierFrequencies();
                for (ConsumerIrManager.CarrierFrequencyRange frequencyRange : frequencyRanges) {
                    int minFreq = frequencyRange.getMinFrequency();
                    int maxFreq = frequencyRange.getMaxFrequency();
                    Logger.i(TAG, "    Range: " + minFreq + " - " + maxFreq);
                    if (minFreq <= IR_FREQUENCY && IR_FREQUENCY <= maxFreq) isIrFrequencySupported = true;
                }

                if (!isIrFrequencySupported) {
                    Logger.i(TAG, "  IR frequency is not supported.");
                    irManager = null;
                }
            }
            else {
                Logger.i(TAG, "  No suitable IR emitter.");
                irManager = null;
            }

            resetOutputs();
        }
        catch (Exception ex) {
            Logger.e(TAG, "Could not retrieve IR manager.", ex);
        }
        finally {
            this.irManager = irManager;
        }

        byte[] audioBuffer = null;
        try {
            int maxMessageLength = IR_MAX_MESSAGE_CYCLES * 2 * 4;
            int minAudioBufferLength = AudioTrack.getMinBufferSize(IR_FREQUENCY, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT);

            Logger.i(TAG, "  Maximum message length:    " + maxMessageLength + " bytes.");
            Logger.i(TAG, "  Minimum audio buffer size: " + minAudioBufferLength + " bytes.");

            audioBuffer = new byte[(maxMessageLength < minAudioBufferLength) ? minAudioBufferLength : maxMessageLength];

            Logger.i(TAG, "  Actual audio buffer size: " + audioBuffer.length + " bytes.");
            Logger.i(TAG, "  Actual audio buffer time: " + ((audioBuffer.length * 1000) / (IR_FREQUENCY * 2)) + " ms");
        }
        catch (Exception ex) {
            Logger.e(TAG, "Could not allocate memory for audio buffer.", ex);
        }
        finally {
            this.audioBuffer = audioBuffer;
        }
    }

    //
    // API
    //

    boolean isInfraRedSupported() {
        Logger.i(TAG, "isInfraRedSupported...");

        boolean isSupported;
        switch (appPreferences.getInfraRedDeviceType()) {
            case BUILT_IN_OR_NONE:
                isSupported = irManager != null;
                break;

            case AUDIO_OUTPUT:
                isSupported = audioBuffer != null;
                break;

            default:
                isSupported = false;
                break;
        }

        Logger.i(TAG, "  Infra is " + (isSupported ? "" : "NOT ") + "supported.");
        return isSupported;
    }

    @MainThread
    synchronized void connectDevice(@NonNull Device device) {
        Logger.i(TAG, "connectDevice - " + device);

        if (numConnectedDevices == 4) {
            Logger.i(TAG, "  4 devices have already connected.");
            return;
        }

        numConnectedDevices++;
        if (numConnectedDevices == 1) {
            // TODO: check if connection is successful
            startIrThread();
        }
    }

    @MainThread
    synchronized void disconnectDevice(@NonNull Device device) {
        Logger.i(TAG, "disconnectDevice - " + device);

        if (numConnectedDevices == 0) {
            Logger.i(TAG, "  All devices have already been disconnected.");
            return;
        }

        numConnectedDevices--;
        if (numConnectedDevices == 0) {
            stopIrThread();
        }
    }

    @MainThread
    int getOutput(@NonNull Device device, int channel) {
        int address = convertAddress(device.getAddress());
        return outputValues[address][channel];
    }

    @MainThread
    void setOutput(@NonNull Device device, int channel, int value) {
        //Logger.i(TAG, "setOutput - " + device + ", channel: " + channel + ", value: " + value);

        int address = convertAddress(device.getAddress());

        if (outputValues[address][channel] == value) {
            return;
        }

        outputValues[address][channel] = value;
        isContinueSending[address] = true;
    }

    //
    // SpecificDeviceManager overrides
    //

    @MainThread
    @Override
    synchronized Observable<Device> startScan() {
        Logger.i(TAG, "startScan...");

        if (!isInfraRedSupported()) {
            return Observable.empty();
        }

        return Observable.fromArray(
                createDevice(Device.DeviceType.INFRARED, "PF Infra 1", "1", null),
                createDevice(Device.DeviceType.INFRARED, "PF Infra 2", "2", null),
                createDevice(Device.DeviceType.INFRARED, "PF Infra 3", "3", null),
                createDevice(Device.DeviceType.INFRARED, "PF Infra 4", "4", null));
    }

    @MainThread
    @Override
    synchronized void stopScan() {
        Logger.i(TAG, "stopScan...");
    }

    @Override
    public Device createDevice(@NonNull Device.DeviceType type, @NonNull String name, @NonNull String address, String deviceSpecificDataJSon) {
        Logger.i(TAG, "createDevice...");

        if (!isInfraRedSupported()) {
            return null;
        }

        if (type != Device.DeviceType.INFRARED) {
            Logger.i(TAG, "  Not infrared device.");
            return null;
        }

        return new InfraRedDevice(name, address, this);
    }

    //
    // Private methods
    //

    private void resetOutputs() {
        Logger.i(TAG, "resetOutputs");
        for (int address = 0; address < 4; address++) {
            outputValues[address][0] = 0;
            outputValues[address][1] = 0;
            isContinueSending[address] = false;
        }
    }

    private int convertAddress(@NonNull String address) {
        //Logger.i(TAG, "convertAddress - " + address);

        switch (address) {
            case "1": return 0;
            case "2": return 1;
            case "3": return 2;
            case "4": return 3;
            default: throw new IllegalArgumentException("Invalid infra address.");
        }
    }

    private boolean startIrThread() {
        Logger.i(TAG, "startIrThread...");

        synchronized (irThreadLock) {
            stopIrThread();
            resetOutputs();

            final AppPreferences.InfraRedDeviceType infraRedDeviceType = appPreferences.getInfraRedDeviceType();

            irThread = new Thread(() -> {
                Logger.i(TAG, "Entering IR thread...");

                while (!Thread.currentThread().isInterrupted()) {
                    switch (infraRedDeviceType) {
                        case BUILT_IN_OR_NONE:
                            sendIrDataToInfra();
                            break;

                        case AUDIO_OUTPUT:
                            sendIrDataToAudio();
                            break;
                    }

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                Logger.i(TAG, "Exiting from IR thread.");
            });
            irThread.start();
        }

        return true;
    }

    private void stopIrThread() {
        Logger.i(TAG, "stopIrThread...");

        synchronized (irThreadLock) {
            if (irThread != null) {
                if (!irThread.isInterrupted()) {
                    Logger.i(TAG, "  Interrupting the IR thread...");
                    irThread.interrupt();
                    try { irThread.join(); } catch (InterruptedException ignored) {}
                }

                irThread = null;
            }
            else {
                Logger.i(TAG, "  IR thread is already null.");
            }
        }
    }

    private void sendIrDataToInfra() {
        for (int address = 0; address < 4; address++) {
            if (isContinueSending[address]) {
                int value0 = outputValues[address][0];
                int value1 = outputValues[address][1];

                int nibble1 = 0x4 | address;
                int nibble2 = calculateOutputNibble(value0);
                int nibble3 = calculateOutputNibble(value1);
                int nibbleLrc = 0xf ^ nibble1 ^ nibble2 ^ nibble3;

                int index = 0;
                index = appendStartStopForInfraOutput(index);
                index = appendNibbleForInfraOutput(index, nibble1);
                index = appendNibbleForInfraOutput(index, nibble2);
                index = appendNibbleForInfraOutput(index, nibble3);
                index = appendNibbleForInfraOutput(index, nibbleLrc);
                appendStartStopForInfraOutput(index);

                irManager.transmit(IR_FREQUENCY, irData);
                try { Thread.sleep(10); } catch (InterruptedException e) {}

                if (value0 == 0 && value1 == 0) {
                    isContinueSending[address] = false;
                }
            }
        }
    }

    private void sendIrDataToAudio() {
        int index = 0;
        for (int address = 0; address < 4; address++) {
            if (isContinueSending[address]) {
                int value0 = outputValues[address][0];
                int value1 = outputValues[address][1];

                int nibble1 = 0x4 | address;
                int nibble2 = calculateOutputNibble(value0);
                int nibble3 = calculateOutputNibble(value1);
                int nibbleLrc = 0xf ^ nibble1 ^ nibble2 ^ nibble3;

                index = appendPauseForAudioOutput(index, IR_LEAD_AND_TAIL_GAP_CYCLES);
                index = appendStartStopForAudioOutput(index);
                index = appendNibbleForAudioOutput(index, nibble1);
                index = appendNibbleForAudioOutput(index, nibble2);
                index = appendNibbleForAudioOutput(index, nibble3);
                index = appendNibbleForAudioOutput(index, nibbleLrc);
                index = appendStartStopForAudioOutput(index);
                index = appendPauseForAudioOutput(index, IR_LEAD_AND_TAIL_GAP_CYCLES);

                if (value0 == 0 && value1 == 0) {
                    isContinueSending[address] = false;
                }
            }
        }

        //clearAudioBuffer(index);
        //fillAudioBuffer();

        if (index > 0) {
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, IR_FREQUENCY, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, audioBuffer.length, AudioTrack.MODE_STREAM);
            if (audioTrack != null) {
                audioTrack.setStereoVolume(1, 1);
                audioTrack.write(audioBuffer, 0, index);
                audioTrack.play();
                try { Thread.sleep(20); } catch (InterruptedException ignore) {}
                audioTrack.stop();
                audioTrack.release();
            }
        }
    }

    private int calculateOutputNibble(int value) {
        if (value < 0) {
            return (8 - (Math.abs(value) >> 5)) | 8;
        }
        else {
            return value >> 5;
        }
    }

    private int appendStartStopForInfraOutput(int index) {
        irData[index++] = IR_MARK_MS;
        irData[index++] = IR_START_STOP_GAP_MS;
        return index;
    }

    private int appendBitForInfraOutput(int index, int bit) {
        irData[index++] = IR_MARK_MS;
        irData[index++] = (bit != 0) ? IR_ONE_GAP_MS : IR_ZERO_GAP_MS;
        return index;
    }

    private int appendNibbleForInfraOutput(int index, int nibble) {
        index = appendBitForInfraOutput(index, nibble & 8);
        index = appendBitForInfraOutput(index, nibble & 4);
        index = appendBitForInfraOutput(index, nibble & 2);
        index = appendBitForInfraOutput(index, nibble & 1);
        return index;
    }

    private int appendStartStopForAudioOutput(int index) {
        index = appendIrMarkForAudioOutput(index);
        index = appendPauseForAudioOutput(index, IR_START_STOP_GAP_CYCLES);
        return index;
    }

    private int appendNibbleForAudioOutput(int index, int nibble) {
        index = appendBitForAudioOutput(index, nibble & 0x8);
        index = appendBitForAudioOutput(index, nibble & 0x4);
        index = appendBitForAudioOutput(index, nibble & 0x2);
        index = appendBitForAudioOutput(index, nibble & 0x1);
        return index;
    }

    private int appendBitForAudioOutput(int index, int bit) {
        index = appendIrMarkForAudioOutput(index);
        index = appendPauseForAudioOutput(index, (bit == 0) ? IR_ZERO_GAP_CYCLES : IR_ONE_GAP_CYCLES);
        return index;
    }

    private int appendIrMarkForAudioOutput(int index) {
        for (int i = 0; i < IR_MARK_CYCLES; i++) {
            int value = (i & 0x1) == 0 ? 127 : -127;
            audioBuffer[index++] = (byte)(128 + value);
            audioBuffer[index++] = (byte)(128 - value);
        }
        return index;
    }

    private int appendPauseForAudioOutput(int index, int pauseCycles) {
        for (int i = 0; i < pauseCycles; i++) {
            audioBuffer[index++] = (byte)128;
            audioBuffer[index++] = (byte)128;
        }
        return index;
    }

    private void clearAudioBuffer(int fromIndex) {
        int length = audioBuffer.length;
        for (int i = fromIndex; i < length; i++) {
            audioBuffer[i] = (byte)128;
        }
    }

    private void fillAudioBuffer() {
        int index = 0;
        int length = audioBuffer.length / 2;
        for (int i = 0; i < length; i++) {
            //int value = (i & 0x1) == 0 ? 127 : -127;
            int value = (i % 10) < 5 ? 127 : -127;
            audioBuffer[index++] = (byte)(128 + value);
            audioBuffer[index++] = (byte)(128 - value);
        }
    }
}
