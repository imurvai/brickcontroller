package com.scn.creationmanagement;

import android.support.annotation.NonNull;

import com.scn.logger.Logger;

/**
 * Created by imurvai on 2017-12-16.
 */

public final class ControllerAction {

    //
    // Private members
    //

    private static final String TAG = ControllerAction.class.getSimpleName();

    private long id;
    private String deviceId;
    private int channel;
    private boolean isRevert;
    private boolean isToggle;
    private int maxOutput;

    //
    // Constructor
    //

    public ControllerAction(long id, @NonNull String deviceId, int channel, boolean isRevert, boolean isToggle, int maxOtput) {
        Logger.i(TAG, "constructor - deviceId: " + deviceId + ", channel: " + channel);
        this.id = id;
        this.deviceId = deviceId;
        this.channel = channel;
        this.isRevert = isRevert;
        this.isToggle = isToggle;
        this.maxOutput = maxOtput;
    }

    //
    // API
    //

    public long getId() { return id; }
    public void setId(long value) { id = value; }

    public String getDeviceId() { return deviceId; }
    public int getChannel() { return channel; }
    public boolean getIsRevert() { return isRevert; }
    public boolean getIsToggle() { return isToggle; }
    public int getMaxOutput() { return maxOutput; }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControllerAction))
            return false;

        ControllerAction other = (ControllerAction)obj;
        return other.deviceId == deviceId && other.channel == channel;
    }

    @Override
    public String toString() {
        return "DeviceId: " + deviceId + ", channel: " + channel;
    }
}
