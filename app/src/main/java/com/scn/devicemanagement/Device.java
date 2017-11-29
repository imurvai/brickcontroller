package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;

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

    Device(String name, String address) {
        this.name = name;
        this.address = address;

        this.stateChangeLiveData.setValue(new StateChange(State.DISCONNECTED, State.DISCONNECTED, false));
        this.deviceInfoLiveData.setValue(new HashMap<>());
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
    void setName(String value) { name = value; }

    public String getAddress() { return address; }

    public abstract boolean connect();
    public abstract void disconnect();

    public abstract int getNumberOfChannels();

    public abstract LiveData<Map<String, String>> getDeviceInfoLiveData();

    public abstract boolean setOutputLevel(int level);

    public abstract boolean setOutput(int channel, int level);
    public abstract boolean setOutputs(List<ChannelValue> channelValues);

    protected void checkChannel(int channel) {
        if (channel < 0 || getNumberOfChannels() <= channel) {
            throw new IllegalArgumentException("Invalid channel " + channel);
        }
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
