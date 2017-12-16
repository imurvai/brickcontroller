package com.scn.creationmanagement;

import android.support.annotation.NonNull;

/**
 * Created by imurvai on 2017-12-16.
 */

public final class ControllerAction {

    private String deviceId;
    private int channel;
    private boolean isRevert;
    private boolean isToggle;
    private int maxOtput;

    public ControllerAction(@NonNull String deviceId, int channel, boolean isRevert, boolean isToggle, int maxOtput) {
        this.deviceId = deviceId;
        this.channel = channel;
        this.isRevert = isRevert;
        this.isToggle = isToggle;
        this.maxOtput = maxOtput;
    }

    public String getDeviceId() { return deviceId; }
    public int getChannel() { return channel; }
    public boolean getIsRevert() { return isRevert; }
    public boolean getIsToggle() { return isToggle; }
    public int getMaxOtput() { return maxOtput; }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ControllerAction)) return false;

        ControllerAction oca = (ControllerAction)other;
        return oca.deviceId == deviceId && oca.channel == channel;
    }
}
