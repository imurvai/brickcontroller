package com.scn.devicemanagement;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.MainThread;
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
    private static final String SERVICE_PARTIAL_UUID_GENERIC_GAP = "1800";
    private static final String SERVICE_PARTIAL_UUID_DEVICE_INFORMATION = "180a";
    private static final String SERVICE_UUID_REMOTE_CONTROL = "4dc591b0-857c-41de-b5f1-15abda665b0c";

    // Characteristic UUIDs
    private static final String CHARACTERISTIC_PARTIAL_UUID_DEVICE_NAME = "2a00";
    private static final String CHARACTERISTIC_PARTIAL_UUID_APPEARANCE = "2a01";
    private static final String CHARACTERISTIC_PARTIAL_UUID_MODEL_NUMBER = "2a24";
    private static final String CHARACTERISTIC_PARTIAL_UUID_FIRMWARE_REVISION = "2a26";
    private static final String CHARACTERISTIC_PARTIAL_UUID_HARDWARE_REVISION = "2a27";
    private static final String CHARACTERISTIC_PARTIAL_UUID_SOFTWARE_REVISION = "2a28";
    private static final String CHARACTERISTIC_PARTIAL_UUID_MANUFACTURER_NAME = "2a29";
    private static final String CHARACTERISTIC_UUID_REMOTE_CONTROL = "2b8cbcc-0e25-4bda-8790-a15f53e6010f";
    private static final String CHARACTERISTIC_UUID_QUICK_DRIVE = "489a6ae0-c1ab-4c9c-bdb2-11d373c1b7fb";

    private BluetoothGattCharacteristic remoteControlCharacteristic;
    private BluetoothGattCharacteristic quickDriveCharacteristic;

    private final int[] outputValues = new int[4];

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
    public String getId() {
        return "SBrick-" + address;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.SBRICK;
    }

    @Override
    public int getNumberOfChannels() {
        return 4;
    }

    @MainThread
    @Override
    public boolean setOutputLevel(int level) {
        Logger.i(TAG, "setOutputLevel...");
        throw new RuntimeException("SBrick doesn't support setOutputLevel");
    }

    @Override
    public void setOutput(int channel, int value) {
        Logger.i(TAG, "setOutput - channel: " + channel + ", value: " + value);
        checkChannel(channel);
    }

    //
    // Protected API
    //

    @Override
    protected void onServiceDiscovered(BluetoothGatt gatt) {
        Logger.i(TAG, "onServiceDiscovered - device: " + SBrickDevice.this);

        remoteControlCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_REMOTE_CONTROL);
        quickDriveCharacteristic = getGattCharacteristic(gatt, SERVICE_UUID_REMOTE_CONTROL, CHARACTERISTIC_UUID_QUICK_DRIVE);
    }

    @Override
    protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Logger.i(TAG, "onCharacteristicRead - device: " + SBrickDevice.this);
    }

    @Override
    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Logger.i(TAG, "onCharacteristicWrite - device: " + SBrickDevice.this);
    }

    //
    // Private methods
    //

}
