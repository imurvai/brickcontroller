package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.support.annotation.MainThread;

import com.scn.logger.Logger;

import java.util.List;
import java.util.Map;

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

    InfraRedDevice(String name, String address, InfraRedDeviceManager infraRedDeviceManager) {
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
    public String getId() {
        return "Infra-" + address;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.INFRARED;
    }

    @MainThread
    @Override
    public boolean connect() {
        Logger.i(TAG, "connectDevice - " + this);
        infraRedDeviceManager.connectDevice(this);
        return true;
    }

    @MainThread
    @Override
    public boolean disconnect() {
        Logger.i(TAG, "disconnectDevice - " + this);
        infraRedDeviceManager.disconnectDevice(this);
        return true;
    }

    @Override
    public int getNumberOfChannels() {
        return 2;
    }

    @MainThread
    @Override
    public LiveData<Map<String, String>> getDeviceInfoLiveData() {
        Logger.i(TAG, "getDeviceInfo - " + getId());

        throw new RuntimeException("not implemented.");
    }

    @MainThread
    @Override
    public boolean setOutputLevel(int level) {
        Logger.i(TAG, "setOutputLevel...");
        throw new RuntimeException("Infrared doesn't support setOutputLevel");
    }

    @MainThread
    @Override
    public boolean setOutput(int channel, int value) {
        Logger.i(TAG, "setOutput - channel: " + channel + ", value: " + value);
        checkChannel(channel);
        infraRedDeviceManager.setOutput(this, channel, value);
        return true;
    }

    @MainThread
    @Override
    public boolean setOutputs(List<ChannelValue> channelValues) {
        for (ChannelValue cv : channelValues) {
            setOutput(cv.getChannel(), cv.getLevel());
        }
        return true;
    }

    //
    // Private methods and classes
    //


}
