package com.scn.devicemanagement;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;

/**
 * Created by steve on 2017. 03. 18..
 */

public abstract class Device implements Comparable<Device> {

    //
    // Private members
    //

    protected String name;
    protected String address;

    //
    // Constructor
    //

    Device(String name, String address) {
        this.name = name;
        this.address = address;
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

    public abstract Single<Void> connect();
    public abstract void disconnect();

    public abstract int getNumberOfChannels();

    public abstract Single<Map<String, String>> getDeviceInfo();

    public abstract Single<Void> setOutputLevel(int level);

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
