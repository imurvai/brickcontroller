package com.scn.creationmanagement;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.Objects;

/**
 * Created by imurvai on 2017-12-16.
 */

@Entity(tableName = "controller_actions",
        foreignKeys = @ForeignKey(
                entity = ControllerEvent.class,
                parentColumns = { "id" },
                childColumns = { "controller_event_id" }
        ))
public final class ControllerAction {

    //
    // Private members
    //

    @Ignore
    private static final String TAG = ControllerAction.class.getSimpleName();

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "controller_event_id")
    private long controllerEventId;

    @ColumnInfo(name = "device_id")
    private String deviceId;

    @ColumnInfo(name = "channel")
    private int channel;

    @ColumnInfo(name = "is_invert")
    private boolean isInvert;

    @ColumnInfo(name = "is_toggle")
    private boolean isToggle;

    @ColumnInfo(name = "max_output")
    private int maxOutput;

    //
    // Constructor
    //

    ControllerAction(long id, long controllerEventId, @NonNull String deviceId, int channel, boolean isInvert, boolean isToggle, int maxOutput) {
        Logger.i(TAG, "constructor - deviceId: " + deviceId + ", channel: " + channel);
        this.id = id;
        this.controllerEventId = controllerEventId;
        this.deviceId = deviceId;
        this.channel = channel;
        this.isInvert = isInvert;
        this.isToggle = isToggle;
        this.maxOutput = maxOutput;
    }

    //
    // API
    //

    public long getId() { return id; }
    void setId(long value) { id = value; }

    public long getControllerEventId() { return controllerEventId; }
    void setControllerEventId(long value) { controllerEventId = value; }

    public String getDeviceId() { return deviceId; }
    void setDeviceId(String value) { deviceId = value; }

    public int getChannel() { return channel; }
    void setChannel(int value) { channel = value; }

    public boolean getIsInvert() { return isInvert; }
    void setIsInvert(boolean value) { isInvert = value; }

    public boolean getIsToggle() { return isToggle; }
    void setIsToggle(boolean value) { isToggle = value; }

    public int getMaxOutput() { return maxOutput; }
    void setMaxOutput(int value) { maxOutput = value; }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControllerAction))
            return false;

        ControllerAction other = (ControllerAction)obj;
        return Objects.equals(other.deviceId, deviceId) && other.channel == channel;
    }

    @Override
    public String toString() {
        return "DeviceId: " + deviceId + ", channel: " + channel;
    }
}
