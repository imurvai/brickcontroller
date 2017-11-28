package com.scn.devicemanagement;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by steve on 2017. 03. 18..
 */

abstract class BluetoothDevice extends Device {

    //
    // Protected members
    //

    protected BluetoothDeviceManager bluetoothDeviceManager;

    //
    // Constructor
    //

    BluetoothDevice(String name, String address, BluetoothDeviceManager bluetoothDeviceManager) {
        super(name, address);

        this.bluetoothDeviceManager = bluetoothDeviceManager;
    }

    //
    // API
    //

}
