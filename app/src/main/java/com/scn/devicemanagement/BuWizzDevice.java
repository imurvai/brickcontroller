package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
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

final class BuWizzDevice extends BluetoothDevice {

    //
    // Members
    //

    private static final String TAG = BuWizzDevice.class.getSimpleName();
    private static final int numberOfChannels = 4;

    private int[] channelValues = new int[numberOfChannels];

    //
    // Constructor
    //

    BuWizzDevice(@NonNull Context context, @NonNull String name, @NonNull String address, @NonNull BluetoothDeviceManager bluetoothDeviceManager) {
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
        return "BuWizz-" + address;
    }

    @Override
    public DeviceType getType() { return DeviceType.BUWIZZ; }

    @MainThread
    @Override
    public boolean connect() {
        Logger.i(TAG, "connectDevice - " + this);
        return false;
    }

    @MainThread
    @Override
    public boolean disconnect() {
        Logger.i(TAG, "disconnectDevice - " + this);

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
        return numberOfChannels;
    }

    @MainThread
    @Override
    public LiveData<Map<String, String>> getDeviceInfoLiveData() {
        Logger.i(TAG, "getDeviceInfo - " + getId());
        throw new RuntimeException("setOutputLevel not implemented yet.");
    }

    @MainThread
    @Override
    public boolean setOutputLevel(int level) {
        Logger.i(TAG, "setOutputLevel - " + getId());
        throw new RuntimeException("setOutputLevel not implemented yet.");
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
}
