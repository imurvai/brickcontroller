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

    private static final int IR_MARK_US = 158;
    private static final int IR_START_GAP_US = 1026;
    private static final int IR_ONE_GAP_US = 553;
    private static final int IR_ZERO_GAP_US = 263;
    private static final int IR_STOP_GAP_US = 2000;

    private static final int MAX_SEND_ATTEMPTS = 8;

    private final AppPreferences appPreferences;
    private final ConsumerIrManager irManager;

    private int numConnectedDevices = 0;

    private Thread irThread = null;
    private final Object irThreadLock = new Object();

    private final int outputValues[][] = new int[4][2];
    private final int[] sendAttemptsLeft = new int[4];

    private final int irData[] = new int[18 * 2];

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
        }
        catch (Exception ex) {
            Logger.e(TAG, "Could not retrieve IR manager.", ex);
        }
        finally {
            this.irManager = irManager;
        }

        resetOutputs();
    }

    //
    // API
    //

    boolean isInfraRedSupported() {
        Logger.i(TAG, "isInfraRedSupported...");

        boolean isSupported = irManager != null;

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
        sendAttemptsLeft[address] = MAX_SEND_ATTEMPTS;
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
            sendAttemptsLeft[address] = MAX_SEND_ATTEMPTS;
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

            irThread = new Thread(() -> {
                Logger.i(TAG, "Entering IR thread...");

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        sendIrDataToDevice();
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

    private void sendIrDataToDevice() {
        for (int address = 0; address < 4; address++) {
            if (sendAttemptsLeft[address] > 0) {
                int value0 = outputValues[address][0];
                int value1 = outputValues[address][1];

                fillIrBuffer(value0, value1, address);

                try {
                    irManager.transmit(IR_FREQUENCY, irData);
                    Thread.sleep(2);
                }
                catch (Exception e) {
                    Logger.e(TAG, "Error sending IR data to infra.", e);
                }

                if (value0 != 0 || value1 != 0) {
                    sendAttemptsLeft[address] = MAX_SEND_ATTEMPTS;
                }
                else {
                    sendAttemptsLeft[address] -= 1;
                }
            }
        }
    }

    private void fillIrBuffer(int value0, int value1, int address) {
        int nibble1 = 0x4 | address;
        int nibble2 = calculateOutputNibble(value0);
        int nibble3 = calculateOutputNibble(value1);
        int nibbleLrc = 0xf ^ nibble1 ^ nibble2 ^ nibble3;

        int index = 0;
        index = appendStartForInfraOutput(index);
        index = appendNibbleForInfraOutput(index, nibble1);
        index = appendNibbleForInfraOutput(index, nibble2);
        index = appendNibbleForInfraOutput(index, nibble3);
        index = appendNibbleForInfraOutput(index, nibbleLrc);
        appendStopForInfraOutput(index);
    }

    private int calculateOutputNibble(int value) {
        if (value < 0) {
            return (8 - (Math.abs(value) >> 5)) | 8;
        }
        else {
            return value >> 5;
        }
    }

    private int appendStartForInfraOutput(int index) {
        irData[index++] = IR_MARK_US;
        irData[index++] = IR_START_GAP_US;
        return index;
    }

    private int appendStopForInfraOutput(int index) {
        irData[index++] = IR_MARK_US;
        irData[index++] = IR_STOP_GAP_US;
        return index;
    }

    private int appendBitForInfraOutput(int index, int bit) {
        irData[index++] = IR_MARK_US;
        irData[index++] = (bit != 0) ? IR_ONE_GAP_US : IR_ZERO_GAP_US;
        return index;
    }

    private int appendNibbleForInfraOutput(int index, int nibble) {
        index = appendBitForInfraOutput(index, nibble & 8);
        index = appendBitForInfraOutput(index, nibble & 4);
        index = appendBitForInfraOutput(index, nibble & 2);
        index = appendBitForInfraOutput(index, nibble & 1);
        return index;
    }
}
