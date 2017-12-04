package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;

/**
 * Created by steve on 2017. 03. 18..
 */

final class SBrickDevice extends BluetoothDevice {

    //
    // Members
    //

    private static final String TAG = SBrickDevice.class.getSimpleName();

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

    @MainThread
    @Override
    public boolean connect() {
        Logger.i(TAG, "connectDevice - " + this);

        if (getCurrentState() == State.CONNECTING) {
            Logger.i(TAG, "  Already connecting.");
            return true;
        }

        if (getCurrentState() != State.DISCONNECTED) {
            Logger.i(TAG, "  Wrong state - " + getCurrentState());
            return false;
        }

        bluetoothGatt = bluetoothDevice.connectGatt(context, true, gattCallback);
        if (bluetoothGatt == null) {
            Logger.w(TAG, "  Failed to connectDevice GATT.");
            return false;
        }

        setState(State.CONNECTING, false);
        return true;
    }

    @MainThread
    @Override
    public boolean disconnect() {
        Logger.i(TAG, "disconnectDevice - " + this);

        if (getCurrentState() == State.DISCONNECTING) {
            Logger.i(TAG, "  Already disconnecting.");
            return true;
        }

        if (getCurrentState() != State.CONNECTED || getCurrentState() != State.CONNECTING) {
            Logger.i(TAG, "  Wrong state - " + getCurrentState());
            return false;
        }

        if (bluetoothGatt == null) {
            Logger.w(TAG, "  bluetoothGatt is null.");
            return false;
        }

        bluetoothGatt.disconnect();
        setState(State.DISCONNECTING, false);
        return true;
    }

    @Override
    public int getNumberOfChannels() {
        return 4;
    }

    @MainThread
    @Override
    public LiveData<Map<String, String>> getDeviceInfoLiveData() {
        Logger.i(TAG, "getDeviceInfo - " + getId());
        return null;
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
    // Private methods
    //

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Logger.i(TAG, "onConnectionStateChange - device: " + SBrickDevice.this.toString());

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    Logger.i(TAG, "  Connecting.");
                    break;

                case BluetoothProfile.STATE_CONNECTED:
                    Logger.i(TAG, "  Connected.");
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    Logger.i(TAG, "  Disconnecting.");
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Logger.i(TAG, "  Disconnected.");
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Logger.i(TAG, "onServicesDiscovered...");

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Logger.i(TAG, "  Error - " + status);
                disconnect();
                return;
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };
}
