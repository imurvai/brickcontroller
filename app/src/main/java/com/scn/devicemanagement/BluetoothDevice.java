package com.scn.devicemanagement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by steve on 2017. 03. 18..
 */

abstract class BluetoothDevice extends Device {

    //
    // Private and Protected members
    //

    private static final String TAG = BluetoothDevice.class.getSimpleName();

    protected final Context context;
    protected final BluetoothDeviceManager bluetoothDeviceManager;
    protected final BluetoothAdapter bluetoothAdapter;
    protected final android.bluetooth.BluetoothDevice bluetoothDevice;
    protected BluetoothGatt bluetoothGatt = null;

    //
    // Constructor
    //

    BluetoothDevice(@NonNull Context context, @NonNull String name, @NonNull String address, @NonNull BluetoothDeviceManager bluetoothDeviceManager) {
        super(name, address);

        this.context = context;
        this.bluetoothDeviceManager = bluetoothDeviceManager;
        this.bluetoothAdapter = bluetoothDeviceManager.getBluetoothAdapter();
        this.bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
    }

    //
    // API
    //

}
