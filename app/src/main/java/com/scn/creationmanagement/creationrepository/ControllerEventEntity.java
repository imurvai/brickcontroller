package com.scn.creationmanagement.creationrepository;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;

/**
 * Created by imurvai on 2017-12-17.
 */

@Entity(tableName = "controller_events",
        foreignKeys = @ForeignKey(
                entity = ControllerProfile.class,
                parentColumns = { "id" },
                childColumns = { "controller_profile_id" }
        ))
final class ControllerEventEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "controller_profile_id")
    public long controllerProfileId;

    @ColumnInfo(name = "event_type")
    @NonNull public ControllerEvent.ControllerEventType eventType;

    @ColumnInfo(name = "event_code")
    public int eventCode;

    public static ControllerEventEntity fromControllerEvent(long controllerProfileId, @NonNull ControllerEvent controllerEvent) {
        ControllerEventEntity cee = new ControllerEventEntity();
        cee.controllerProfileId = controllerProfileId;
        cee.eventType = controllerEvent.getEventType();
        cee.eventCode = controllerEvent.getEventCode();
        return cee;
    }
}
