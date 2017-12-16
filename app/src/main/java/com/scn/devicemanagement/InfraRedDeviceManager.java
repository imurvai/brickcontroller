package com.scn.devicemanagement;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by steve on 2017. 09. 07..
 */

@Singleton
public final class InfraRedDeviceManager extends SpecificDeviceManager {

    //
    // Private members
    //

    private static final String TAG = InfraRedDeviceManager.class.getSimpleName();

    private static final int IR_FREQUENCY = 38000;
    private static final int IR_MARK = 158;
    private static final int IR_START_STOP_GAP = 1026;
    private static final int IR_ONE_GAP = 553;
    private static final int IR_ZERO_GAP = 263;

    private final ConsumerIrManager irManager;

    private int numConnectedDevices = 0;

    private Thread irThread = null;
    private final Object irThreadLock = new Object();

    private final int outputValues[][] = new int[4][2];
    private final boolean isContinueSending[] = new boolean[4];
    private final int irData[] = new int[18 * 2];

    //
    // Constructor
    //

    @Inject
    public InfraRedDeviceManager(@NonNull Context context) {
        super(context);
        Logger.i(TAG, "constructor...");

        ConsumerIrManager irManager = context.getSystemService(ConsumerIrManager.class);
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

        this.irManager = irManager;
        resetOutputs();
    }

    //
    // API
    //

    public boolean isInfraRedSupported() {
        Logger.i(TAG, "isInfraRedSupported...");
        Logger.i(TAG, "  Infra is " + (irManager != null ? "" : "NOT ") + "supported.");
        return irManager != null;
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
    void setOutput(@NonNull Device device, int channel, int value) {
        //Logger.i(TAG, "setOutput - " + device + ", channel: " + channel + ", value: " + value);

        int address = convertAddress(device.getAddress());
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
                createDevice(DeviceType.INFRARED, "PF Infra 1", "1", Device.OutputLevel.NORMAL),
                createDevice(DeviceType.INFRARED, "PF Infra 2", "2", Device.OutputLevel.NORMAL),
                createDevice(DeviceType.INFRARED, "PF Infra 3", "3", Device.OutputLevel.NORMAL),
                createDevice(DeviceType.INFRARED, "PF Infra 4", "4", Device.OutputLevel.NORMAL));
    }

    @MainThread
    @Override
    synchronized void stopScan() {
        Logger.i(TAG, "stopScan...");
    }

    @Override
    Device createDevice(@NonNull DeviceType type, @NonNull String name, @NonNull String address, @NonNull Device.OutputLevel outputLevel) {
        Logger.i(TAG, "createDevice...");

        if (!isInfraRedSupported()) {
            return null;
        }

        if (type != DeviceType.INFRARED) {
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

    private void startIrThread() {
        Logger.i(TAG, "startIrThread...");

        synchronized (irThreadLock) {
            stopIrThread();
            resetOutputs();

            irThread = new Thread(() -> {
                Logger.i(TAG, "Entering IR thread...");

                while (!Thread.currentThread().isInterrupted()) {
                    sendIrData();

                    try {
                        Thread.sleep(20);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                Logger.i(TAG, "Exiting from IR thread.");
            });
            irThread.start();
        }
    }

    private void stopIrThread() {
        Logger.i(TAG, "stopIrThread...");

        synchronized (irThreadLock) {
            if (irThread == null) {
                Logger.i(TAG, "  IR thread is already null.");
                return;
            }

            if (!irThread.isInterrupted()) {
                Logger.i(TAG, "  Interrupting the IR thread...");
                irThread.interrupt();
                try { irThread.join(); } catch (InterruptedException e) {}
            }

            irThread = null;
        }
    }

    private void sendIrData() {
        for (int address = 0; address < 4; address++) {
            if (isContinueSending[address]) {
                int index = 0;

                int value0 = outputValues[address][0];
                int value1 = outputValues[address][1];

                int nibble1 = 0x4 | address;
                int nibble2 = calculateOutputNibble(value0);
                int nibble3 = calculateOutputNibble(value1);
                int nibbleLrc = 0xf ^ nibble1 ^ nibble2 ^ nibble3;

                index = appendStartStop(index);
                index = appendNibble(index, nibble1);
                index = appendNibble(index, nibble2);
                index = appendNibble(index, nibble3);
                index = appendNibble(index, nibbleLrc);
                appendStartStop(index);

                irManager.transmit(IR_FREQUENCY, irData);

                if (value0 == 0 && value1 == 0) {
                    isContinueSending[address] = false;
                }
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

    private int appendStartStop(int index) {
        irData[index++] = IR_MARK;
        irData[index++] = IR_START_STOP_GAP;
        return index;
    }

    private int appendBit(int index, int bit) {
        irData[index++] = IR_MARK;
        irData[index++] = (bit != 0) ? IR_ONE_GAP : IR_ZERO_GAP;
        return index;
    }

    private int appendNibble(int index, int nibble) {
        index = appendBit(index, nibble & 8);
        index = appendBit(index, nibble & 4);
        index = appendBit(index, nibble & 2);
        index = appendBit(index, nibble & 1);
        return index;
    }
}
