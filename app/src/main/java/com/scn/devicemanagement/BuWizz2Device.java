package com.scn.devicemanagement;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.scn.logger.Logger;

import static com.scn.devicemanagement.BuWizz2Device.BuWizz2OutputLevel.NORMAL;

/**
 * Created by imurvai on 2017-12-30.
 */

public final class BuWizz2Device  extends BluetoothDevice {

    //
    // Members
    //

    private static final String TAG = BuWizz2Device.class.getSimpleName();

    // Service UUIDs
    private static final String SERVICE_UUID_REMOTE_CONTROL = "4e050000-74fb-4481-88b3-9919b1676e93";

    // Characteristic UUIDs
    private static final String CHARACTERISTIC_UUID_REMOTE_CONTROL = "000092d1-0000-1000-8000-00805f9b34fb";

    private BluetoothGattCharacteristic remoteControlCharacteristic;

    private Thread outputThread = null;
    private final Object outputThreadLock = new Object();

    private BuWizz2Data buWizz2Data = null;
    private boolean buWizz2DataChanged = false;

    private final int[] outputValues = new int[4];
    private boolean continueSending = true;

    //
    // Constructor
    //

    BuWizz2Device(@NonNull Context context, @NonNull String name, @NonNull String address, String deviceSpecificDataJSon, @NonNull BluetoothDeviceManager bluetoothDeviceManager) {
        super(context, name, address, bluetoothDeviceManager);
        Logger.i(TAG, "constructor...");
        Logger.i(TAG, "  name: " + name);
        Logger.i(TAG, "  address: " + address);

        setDeviceSpecificDataJSon(deviceSpecificDataJSon);
    }

    //
    // API
    //

    @Override
    public Device.DeviceType getType() { return Device.DeviceType.BUWIZZ2; }

    @Override
    public String getDeviceSpecificDataJSon() {
        Logger.i(TAG, "getDeviceSpecificDataJSon...");
        return new Gson().toJson(buWizz2Data);
    }

    @Override
    public void setDeviceSpecificDataJSon(String deviceSpecificDataJSon) {
        Logger.i(TAG, "setDeviceSpecificDataJSon - " + deviceSpecificDataJSon);
        buWizz2Data = null;
        if (deviceSpecificDataJSon != null) buWizz2Data = new Gson().fromJson(deviceSpecificDataJSon, BuWizz2Data.class);
        if (buWizz2Data == null) buWizz2Data = new BuWizz2Data(NORMAL);
        buWizz2DataChanged = true;
    }

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
        Logger.i(TAG, "setOutput - channel: " + channel + ", value: " + value);
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
        Logger.i(TAG, "onServiceDiscovered - device: " + BuWizz2Device.this);

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
        Logger.i(TAG, "onCharacteristicRead - device: " + BuWizz2Device.this);
        return true;
    }

    @Override
    protected boolean onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //Logger.i(TAG, "onCharacteristicWrite - device: " + BuWizz2Device.this);
        return true;
    }

    @Override
    protected void disconnectInternal() {
        Logger.i(TAG, "disconnectInternal - device: " + BuWizz2Device.this);
        stopOutputThread();
    }

    //
    // Private methods
    //

    private void startOutputThread() {
        Logger.i(TAG, "startOutputThread - device: " + this);

        synchronized (outputThreadLock) {
            stopOutputThread();

            buWizz2DataChanged = true;

            outputThread = new Thread(() -> {
                Logger.i(TAG, "Entering the output thread - device: " + BuWizz2Device.this);

                while (!Thread.currentThread().isInterrupted()) {
                    if (buWizz2DataChanged) {
                        sendOutputLevel(buWizz2Data.outputLevel);
                        buWizz2DataChanged = false;
                    }
                    else if (continueSending) {
                        int value0 = outputValues[0];
                        int value1 = outputValues[1];
                        int value2 = outputValues[2];
                        int value3 = outputValues[3];

                        if (sendOutputValues(value0, value1, value2, value3)) {
                            continueSending = value0 != 0 || value1 != 0 || value2 != 0 || value3 != 0;
                        }
                        else {
                            continueSending = true;
                        }
                    }

                    try {
                        Thread.sleep(60);
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                Logger.i(TAG, "Exiting from output thread - device: " + BuWizz2Device.this);
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

    private boolean sendOutputValues(int v0, int v1, int v2, int v3) {
        //Logger.i(TAG, "sendOutputValues...");

        try {
            byte[] buffer = new byte[] {
                    (byte)0x10,
                    (byte)(v0 / 2),
                    (byte)(v1 / 2),
                    (byte)(v2 / 2),
                    (byte)(v3 / 2),
                    (byte)0x00
            };

            remoteControlCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            if (remoteControlCharacteristic.setValue(buffer)) {
                if (bluetoothGatt.writeCharacteristic(remoteControlCharacteristic)) {
                    return true;
                }
                else {
                    Logger.w(TAG, "  Failed to write remote control characteristic.");
                }
            }
            else {
                Logger.w(TAG, "  Failed to set value on remote control characteristic.");
            }
        }
        catch (Exception e) {
            Logger.w(TAG, "Failed to send output values to characteristic.");
        }

        return false;
    }

    private boolean sendOutputLevel(BuWizz2OutputLevel outputLevel) {
        Logger.i(TAG, "sendOutputLevel - " + outputLevel);
        try {
            byte outputLevelValue = 2;
            switch (outputLevel) {
                case LOW: outputLevelValue = 1; break;
                case NORMAL: outputLevelValue = 2; break;
                case HIGH: outputLevelValue = 3; break;
                case LUDICROUS: outputLevelValue = 4; break;
            }

            byte[] buffer = new byte[] { (byte)0x11, outputLevelValue };

            remoteControlCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            if (remoteControlCharacteristic.setValue(buffer)) {
                if (bluetoothGatt.writeCharacteristic(remoteControlCharacteristic)) {
                    return true;
                }
                else {
                    Logger.w(TAG, "  Failed to write remote control characteristic.");
                }
            }
            else {
                Logger.w(TAG, "  Failed to set value on remote control characteristic.");
            }
        }
        catch (Exception e) {
            Logger.w(TAG, "Failed to send output level to characteristic.");
        }

        return false;
    }

    //
    // DeviceSpacificData
    //

    public enum BuWizz2OutputLevel {
        LOW,
        NORMAL,
        HIGH,
        LUDICROUS
    }

    public static class BuWizz2Data {
        public BuWizz2OutputLevel outputLevel;
        public BuWizz2Data(@NonNull BuWizz2OutputLevel outputLevel) {
            this.outputLevel = outputLevel;
        }
    }
}
