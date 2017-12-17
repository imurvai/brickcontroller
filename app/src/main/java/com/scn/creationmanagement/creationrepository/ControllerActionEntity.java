package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;

/**
 * Created by imurvai on 2017-12-17.
 */

@Entity(tableName = "controller_actions",
        foreignKeys = @ForeignKey(
                entity = ControllerEvent.class,
                parentColumns = { "id" },
                childColumns = { "controller_event_id" }
        ))
final class ControllerActionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "controller_event_id")
    public long controllerEventId;

    @ColumnInfo(name = "device_id")
    @NonNull public String deviceId;

    public int channel;
    public boolean isRevert;
    public boolean isToggle;
    public int maxOutput;

    public static ControllerActionEntity fromControllerAction(long controllerEventId, @NonNull ControllerAction controllerAction) {
        ControllerActionEntity cae = new ControllerActionEntity();
        cae.controllerEventId = controllerEventId;
        cae.deviceId = controllerAction.getDeviceId();
        cae.channel = controllerAction.getChannel();
        cae.isRevert = controllerAction.getIsRevert();
        cae.isToggle = controllerAction.getIsToggle();
        cae.maxOutput = controllerAction.getMaxOutput();
        return cae;
    }
}
