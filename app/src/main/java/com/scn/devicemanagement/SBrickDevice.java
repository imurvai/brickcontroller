package com.scn.devicemanagement;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

/**
 * Created by steve on 2017. 03. 18..
 */

final class SBrickDevice extends BluetoothDevice {


    //
    // Members
    //

    private static final String TAG = SBrickDevice.class.getSimpleName();

    // Service UUIDs
    private static final String SERVICE_UUID_REMOTE_CONTROL = "4dc591b0-857c-41de-b5f1-15abda665b0c";

    // Characteristic UUIDs
    private static final String CHARACTERISTIC_UUID_QUICK_DRIVE = "489a6ae0-c1ab-4c9c-bdb2-11d373c1b7fb";

    private BluetoothGattCharacteristic quickDriveCharacteristic;

    private Thread outputThread = null;
    private final Object outputThreadLock = new Object();

    private final int[] outputValues = new int[4];
    private boolean continueSending = true;

    //
    // Constructor
    //

    SBrickDevice(@NonNull Context context, @NonNull String name, @NonNull String address, @NonNull BluetoothDeviceManager bluetoothDeviceManager) {
        super(context, name, address, bluetoothDeviceManager);
        Logger.i(TAG, "constructor...");
        Logger.i(TAG, "  name: " + name);
        Logger.i(TAG, "  address: " + address);
    }

    //
    // API
    //

    @Override
    public DeviceType getType() { return DeviceType.SBRICK; }

    @Override
    public int getNumberOfChannels() {
        return 4;
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

        if (outputValues[channel] == value) {
            return;
        }

        outputValues[channel] = value;
        continueSending = true;
    }

    //
    // Protected API
    //

    @Override
    protected boolean onServiceDiscovered(BluetoothGatt gatt) {
        Logger.i(TAG, "onServiceDiscovered - device: " + SBrickDevice.this);

        quickDriveCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_QUICK_DRIVE);
        if (quickDriveCharacteristic == null) {
            Logger.w(TAG, "  Could not get characteristic.");
            return false;
        }

        startOutputThread();
        return true;
    }

    @Override
    protected boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Logger.i(TAG, "onCharacteristicRead - device: " + SBrickDevice.this);
        return true;
    }

    @Override
    protected boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //Logger.i(TAG, "onCharacteristicWrite - device: " + SBrickDevice.this);
        return true;
    }

    @Override
    protected void disconnectInternal() {
        Logger.i(TAG, "disconnectInternal - device: " + SBrickDevice.this);
        Logger.i(TAG, "  Stopping output thread...");
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
                Logger.i(TAG, "Entering the output thread - device: " + SBrickDevice.this);

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

                Logger.i(TAG, "Exiting from output thread - device: " + SBrickDevice.this);
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
                try { outputThread.join(); } catch (InterruptedException ignored) {}
            }

            outputThread = null;
        }
    }

    private void sendOutputValues(int v0, int v1, int v2, int v3) {
        try {
            byte[] buffer = new byte[] {
                    (byte)((Math.abs(v0) & 0xfe) | 0x02 | (v0 < 0 ? 1 : 0)),
                    (byte)((Math.abs(v1) & 0xfe) | 0x02 | (v1 < 0 ? 1 : 0)),
                    (byte)((Math.abs(v2) & 0xfe) | 0x02 | (v2 < 0 ? 1 : 0)),
                    (byte)((Math.abs(v3) & 0xfe) | 0x02 | (v3 < 0 ? 1 : 0))
            };

            if (quickDriveCharacteristic.setValue(buffer)) {
                bluetoothGatt.writeCharacteristic(quickDriveCharacteristic);
            }
        }
        catch (Exception e) {
            Logger.w(TAG, "Failed to send output values to characteristic.");
        }
    }
}
