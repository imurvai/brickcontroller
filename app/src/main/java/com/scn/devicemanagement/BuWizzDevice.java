package com.scn.devicemanagement;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.UUID;

/**
 * Created by steve on 2017. 03. 18..
 */

final class BuWizzDevice extends BluetoothDevice {

    //
    // Members
    //

    private static final String TAG = BuWizzDevice.class.getSimpleName();

    // Service UUIDs
    private static final String SERVICE_UUID_REMOTE_CONTROL = "0000ffe0-0000-1000-8000-00805f9b34fb";

    // Characteristic UUIDs
    private static final String CHARACTERISTIC_UUID_REMOTE_CONTROL = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private BluetoothGattCharacteristic remoteControlCharacteristic;

    private Thread outputThread = null;
    private final Object outputThreadLock = new Object();

    private OutputLevel outputLevel = OutputLevel.NORMAL;

    private final int[] outputValues = new int[4];
    private boolean continueSending = true;

    //
    // Constructor
    //

    BuWizzDevice(@NonNull Context context, @NonNull String name, @NonNull String address, @NonNull OutputLevel outputLevel, @NonNull BluetoothDeviceManager bluetoothDeviceManager) {
        super(context, name, address, bluetoothDeviceManager);
        Logger.i(TAG, "constructor...");
        Logger.i(TAG, "  name: " + name);
        Logger.i(TAG, "  address: " + address);
        Logger.i(TAG, "  output level: " + outputLevel);

        this.outputLevel = outputLevel;
    }

    //
    // API
    //


    @Override
    public DeviceType getType() { return DeviceType.BUWIZZ; }

    @Override
    public int getNumberOfChannels() {
        return 4;
    }

    @Override
    public OutputLevel getOutputLevel() {
        Logger.i(TAG, "getOutputLevel - " + outputLevel);
        return outputLevel;
    }

    @Override
    public void setOutputLevel(@NonNull OutputLevel outputLevel) {
        Logger.i(TAG, "setOutputLevel - " + outputLevel);
        this.outputLevel = outputLevel;
    }

    @Override
    public int getOutput(int channel) {
        checkChannel(channel);
        return outputValues[channel];
    }

    @Override
    public void setOutput(int channel, int value) {
        //Logger.i(TAG, "setOutput - channel: " + channel + ", value: " + value);
        checkChannel(channel);
        value = limitOutputValue(value);
        outputValues[channel] = value;
        continueSending = true;
    }

    //
    // Protected API
    //

    @Override
    protected boolean onServiceDiscovered(BluetoothGatt gatt) {
        Logger.i(TAG, "onServiceDiscovered - device: " + BuWizzDevice.this);

        remoteControlCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_REMOTE_CONTROL);
        if (remoteControlCharacteristic == null) {
            Logger.w(TAG, "  Could not get characteristic.");
            return false;
        }

        startOutputThread();
        return true;
    }

    @Override
    protected boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Logger.i(TAG, "onCharacteristicRead - device: " + BuWizzDevice.this);
        return true;
    }

    @Override
    protected boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //Logger.i(TAG, "onCharacteristicWrite - device: " + BuWizzDevice.this);
        return true;
    }

    @Override
    protected void disconnectInternal() {
        Logger.i(TAG, "disconnectInternal - device: " + BuWizzDevice.this);
        stopOutputThread();
    }

    //
    // Private methods
    //

    private void startOutputThread() {
        Logger.i(TAG, "startOutputThread - device: " + this);

        synchronized (outputThreadLock) {
            stopOutputThread();

            outputThread = new Thread(() -> {
                Logger.i(TAG, "Entering the output thread - device: " + BuWizzDevice.this);

                while (!Thread.currentThread().isInterrupted()) {
                    if (continueSending) {
                        int value0 = outputValues[0];
                        int value1 = outputValues[1];
                        int value2 = outputValues[2];
                        int value3 = outputValues[3];

                        sendOutputValues(value0, value1, value2, value3);

                        continueSending = value0 != 0 || value1 != 0 || value2 != 0 || value3 != 0;
                    }

                    try {
                        Thread.sleep(60);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                Logger.i(TAG, "Exiting from output thread - device: " + BuWizzDevice.this);
            });
            outputThread.start();
        }
    }

    private void stopOutputThread() {
        Logger.i(TAG, "stopOtuputThread...");

        synchronized (outputThreadLock) {
            if (outputThread == null) {
                Logger.i(TAG, "  Output thread is already null.");
                return;
            }

            if (!outputThread.isInterrupted()) {
                Logger.i(TAG, "  Interrupting the output thread...");
                outputThread.interrupt();
                try { outputThread.join(); } catch (InterruptedException e) {}
            }

            outputThread = null;
        }
    }

    private void sendOutputValues(int v0, int v1, int v2, int v3) {
        try {
            byte outputLevelValue = 0x20;
            switch (outputLevel) {
                case LOW: outputLevelValue = 0x00; break;
                case NORMAL: outputLevelValue = 0x20; break;
                case HIGH: outputLevelValue = 0x40; break;
            }

            byte[] buffer = new byte[] {
                    (byte)((Math.abs(v0) >> 2) | (v0 < 0 ? 0x40 : 0) | 0x80),
                    (byte)((Math.abs(v1) >> 2) | (v1 < 0 ? 0x40 : 0)),
                    (byte)((Math.abs(v2) >> 2) | (v2 < 0 ? 0x40 : 0)),
                    (byte)((Math.abs(v3) >> 2) | (v3 < 0 ? 0x40 : 0)),
                    outputLevelValue
            };

            if (remoteControlCharacteristic.setValue(buffer)) {
                bluetoothGatt.writeCharacteristic(remoteControlCharacteristic);
            }
        }
        catch (Exception e) {
            Logger.w(TAG, "Failed to send output values to characteristic.");
        }
    }
}
