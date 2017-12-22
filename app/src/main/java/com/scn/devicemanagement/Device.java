package com.scn.devicemanagement;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Looper;
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

    public enum DeviceType {
        INFRARED,
        SBRICK,
        BUWIZZ
    }

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        GETTING_DEVICE_INFO,
        DISCONNECTING
    }

    public enum OutputLevel {
        LOW,
        NORMAL,
        HIGH
    }

    //
    // Private members
    //

    private static final String TAG = Device.class.getSimpleName();

    protected String name;
    protected String address;

    protected MutableLiveData<StateChange<Device.State>> stateChangeLiveData = new MutableLiveData<>();

    //
    // Constructor
    //

    Device(@NonNull String name, @NonNull String address) {
        this.name = name;
        this.address = address;

        stateChangeLiveData.postValue(new StateChange(State.DISCONNECTED, State.DISCONNECTED, false));
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
        return "Id: " + getId() + ", type: " + getType() + ", name: " + getName()+ ", address: " + getAddress();
    }


    //
    // API
    //

    public String getId() { return String.format("%s-%s", getType(), getAddress()); }
    public abstract DeviceType getType();
    public String getName() { return name; }
    void setName(String value) { name = value; }
    public String getAddress() { return address; }

    public LiveData<StateChange<Device.State>> getStateChangeLiveData() { return stateChangeLiveData; }

    public abstract int getNumberOfChannels();

    public abstract boolean connect();
    public abstract boolean disconnect();

    public OutputLevel getOutputLevel() { return OutputLevel.NORMAL; }
    public void setOutputLevel(@NonNull OutputLevel value) {
        Logger.i(TAG, "setOutputLevel - " + value);
        Logger.i(TAG, "  Not supported.");
    }

    public abstract int getOutput(int channel);
    public abstract void setOutput(int channel, int level);

    //
    // Protected methods
    //

    @MainThread
    protected Device.State getCurrentState() {
        return stateChangeLiveData.getValue().getCurrentState();
    }

    protected void setState(Device.State newState, boolean isError) {
        Logger.i(TAG, "setState - " + getCurrentState() + " -> " + newState);
        Device.State currentState = getCurrentState();
        if (Looper.getMainLooper().isCurrentThread()) {
            stateChangeLiveData.setValue(new StateChange(currentState, newState, isError));
        }
        else {
            stateChangeLiveData.postValue(new StateChange(currentState, newState, isError));
        }
    }

    protected void checkChannel(int channel) {
        if (channel < 0 || getNumberOfChannels() <= channel) {
            throw new IllegalArgumentException("Invalid channel " + channel);
        }
    }

    protected int limitOutputValue(int value) {
        if (value < -255) value = -255;
        if (255 < value) value = 255;
        return value;
    }
}
