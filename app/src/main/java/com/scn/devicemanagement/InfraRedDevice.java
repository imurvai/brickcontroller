package com.scn.devicemanagement;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

/**
 * Created by steve on 2017. 09. 03..
 */

final class InfraRedDevice extends Device {

    //
    // Private members
    //

    private static final String TAG = InfraRedDevice.class.getSimpleName();

    private InfraRedDeviceManager infraRedDeviceManager;

    //
    // Constructor
    //

    InfraRedDevice(@NonNull String name, @NonNull String address, @NonNull InfraRedDeviceManager infraRedDeviceManager) {
        super(name, address);
        Logger.i(TAG, "constructor...");
        Logger.i(TAG, "  name: " + name);
        Logger.i(TAG, "  address: " + address);

        this.infraRedDeviceManager = infraRedDeviceManager;
    }

    //
    // API
    //

    @Override
    public DeviceType getType() { return DeviceType.INFRARED; }

    @Override
    public int getNumberOfChannels() {
        return 2;
    }

    @MainThread
    @Override
    public boolean connect() {
        Logger.i(TAG, "connectDevice - " + this);

        if (getCurrentState() == State.CONNECTED) {
            Logger.i(TAG, "  Already connected.");
            return false;
        }

        infraRedDeviceManager.connectDevice(this);
        setState(State.CONNECTED, false);
        return true;
    }

    @MainThread
    @Override
    public boolean disconnect() {
        Logger.i(TAG, "disconnectDevice - " + this);

        if (getCurrentState() == State.DISCONNECTED) {
            Logger.i(TAG, "  Already disconnected.");
            return false;
        }

        infraRedDeviceManager.disconnectDevice(this);
        setState(State.DISCONNECTED, false);
        return true;
    }

    @MainThread
    @Override
    public void setOutput(int channel, int value) {
        //Logger.i(TAG, "setOutput - channel: " + channel + ", value: " + value);
        checkChannel(channel);
        value = limitOutputValue(value);
        infraRedDeviceManager.setOutput(this, channel, value);
    }
}
