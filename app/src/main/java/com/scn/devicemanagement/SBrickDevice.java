package com.scn.devicemanagement;

import android.support.annotation.MainThread;

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
    private static final int numberOfChannels = 4;

    private int[] channelValues = new int[numberOfChannels];

    //
    // Constructor
    //

    SBrickDevice(String name, String address, BluetoothDeviceManager bluetoothDeviceManager) {
        super(name, address, bluetoothDeviceManager);
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
    public Single<Void> connect() {
        Logger.i(TAG, "connect - " + getId());
        return null;
    }

    @MainThread
    @Override
    public void disconnect() {
        Logger.i(TAG, "disconnect - " + getId());
    }

    @Override
    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    @MainThread
    @Override
    public Single<Map<String, String>> getDeviceInfo() {
        Logger.i(TAG, "getDeviceInfo - " + getId());

        return null;
    }

    @MainThread
    @Override
    public Single<Void> setOutputLevel(int level) {
        Logger.i(TAG, "setOutputLevel...");
        return Single.error(new RuntimeException("SBrick doesn't support setOutputLevel"));
    }

    @MainThread
    @Override
    public boolean setOutput(int channel, int value) {
        Logger.i(TAG, "setOutput - channel: " + channel + ", value: " + value);
        checkChannel(channel);
        return false;
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
    // Private methods
    //
}
