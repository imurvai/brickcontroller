package com.scn.creationmanagement;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imurvai on 2017-12-13.
 */

@Entity(tableName = "controller_profiles",
        foreignKeys = @ForeignKey(
                entity = Creation.class,
                parentColumns = { "id" },
                childColumns = { "creation_id" }
        ))
public final class ControllerProfile {

    //
    // Members
    //

    @Ignore
    private static final String TAG = ControllerProfile.class.getSimpleName();

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "creation_id")
    private long creationId;

    @ColumnInfo(name = "name")
    private String name;

    @Ignore
    private List<ControllerEvent> controllerEvents = new ArrayList<>();

    //
    // Constructor
    //

    ControllerProfile(long id, long creationId, @NonNull String name) {
        Logger.i(TAG, "constructor - " + name);
        this.id = id;
        this.creationId = creationId;
        this.name = name;
    }

    //
    // API
    //

    public long getId() { return id; }
    void setId(long value) { id = value; }

    public long getCreationId() { return creationId; }
    void setCreationId(long value) { creationId = value; }

    public String getName() { return name; }
    void setName(String value) { name = value; }

    public List<ControllerEvent> getControllerEvents() { return controllerEvents; }

    boolean addControllerEvent(ControllerEvent controllerEvent) {
        Logger.i(TAG, "addControllerEvent - " + controllerEvent);

        if (controllerEvents.contains(controllerEvent)) {
            Logger.w(TAG, "  Controller event with the same name already exists.");
            return false;
        }

        controllerEvents.add(controllerEvent);
        return true;
    }

    boolean removeControllerEvent(ControllerEvent controllerEvent) {
        Logger.i(TAG, "removeControllerEvent - " + controllerEvent);

        if (!controllerEvents.contains(controllerEvent)) {
            Logger.w(TAG, "  No such controller event.");
            return false;
        }

        controllerEvents.remove(controllerEvent);
        return true;
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControllerProfile))
            return false;

        ControllerProfile other = (ControllerProfile)obj;
        return other.name == name;
    }

    @Override
    public String toString() {
        return name;
    }
}
