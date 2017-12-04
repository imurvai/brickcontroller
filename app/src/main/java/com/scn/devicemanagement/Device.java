package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by steve on 2017. 03. 18..
 */

public abstract class Device implements Comparable<Device> {

    //
    // Constants
    //

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        GETTING_DEVICE_INFO,
        DISCONNECTING
    }

    //
    // Private members
    //

    private static final String TAG = Device.class.getSimpleName();

    protected String name;
    protected String address;

    protected MutableLiveData<StateChange<Device.State>> stateChangeLiveData = new MutableLiveData<>();
    protected MutableLiveData<Map<String, String>> deviceInfoLiveData = new MutableLiveData<>();

    //
    // Constructor
    //

    Device(@NonNull String name, @NonNull String address) {
        this.name = name;
        this.address = address;

        this.stateChangeLiveData.postValue(new StateChange(State.DISCONNECTED, State.DISCONNECTED, false));
        this.deviceInfoLiveData.postValue(new HashMap<>());
    }

    //
    // Comparable overrides
    //

    @Override
    public int compareTo(@NonNull Device device) {
        if (device.getType() != getType()) {
            return getType().compareTo(device.getType());
        }

        return getAddress().compareTo(device.getAddress());
    }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return "Name: " + getName() + ", type: " + getType() + ", address: " + getAddress();
    }


    //
    // API
    //

    public abstract String getId();
    public abstract DeviceType getType();

    public String getName() { return name; }
    public void setName(String value) { name = value; }

    public String getAddress() { return address; }

    public abstract boolean connect();
    public abstract boolean disconnect();

    public abstract int getNumberOfChannels();

    public abstract LiveData<Map<String, String>> getDeviceInfoLiveData();

    public abstract boolean setOutputLevel(int level);

    public abstract void setOutput(int channel, int level);

    public void setOutputs(@NonNull List<ChannelValue> channelValues) {
        for (ChannelValue cv : channelValues) {
            setOutput(cv.getChannel(), cv.getLevel());
        }
    }

    //
    // Protected methods
    //

    @MainThread
    protected Device.State getCurrentState() {
        return stateChangeLiveData.getValue().getCurrentState();
    }

    @MainThread
    protected void setState(Device.State newState, boolean isError) {
        Logger.i(TAG, "setState - " + getCurrentState() + " -> " + newState);
        Device.State currentState = getCurrentState();
        stateChangeLiveData.setValue(new StateChange(currentState, newState, isError));
    }

    protected void checkChannel(int channel) {
        if (channel < 0 || getNumberOfChannels() <= channel) {
            throw new IllegalArgumentException("Invalid channel " + channel);
        }
    }

    protected int clampOutputValue(int value) {
        if (value < -255) value = -255;
        if (255 < value) value = 255;
        return value;
    }

    //
    // Channel-level class
    //

    public static class ChannelValue {
        private int channel;
        private int level;

        public ChannelValue(int channel, int level) {
            this.channel = channel;
            this.level = level;
        }

        public int getChannel() { return channel; }
        public int getLevel() { return level; }
        public void setLevel(int value) { level = value; }
    }
}
