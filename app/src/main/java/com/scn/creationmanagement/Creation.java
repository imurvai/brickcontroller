package com.scn.creationmanagement;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by steve on 2017. 11. 01..
 */

@Entity(tableName = "creations")
public final class Creation {

    //
    // Members
    //

    @Ignore
    private static final String TAG = Creation.class.getSimpleName();

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @Ignore
    private final List<ControllerProfile> controllerProfiles = new ArrayList<>();

    //
    // Constructor
    //

    Creation(long id, @NonNull String name) {
        Logger.i(TAG, "constructor - " + name);
        this.id = id;
        this.name = name;
    }

    //
    // API
    //

    public long getId() { return id; }
    void setId(long value) { id = value; }

    public String getName() { return name; }
    void setName(@NonNull String value) { name = value; }

    public List<ControllerProfile> getControllerProfiles() {
        Logger.i(TAG, "getControllerProfiles...");
        return controllerProfiles;
    }

    public List<String> getUsedDeviceIds() {
        Logger.i(TAG, "getUsedDeviceIds - " + this);

        List<String> deviceIds = new ArrayList<>();
        for (ControllerProfile controllerProfile : controllerProfiles) {
            for (ControllerEvent controllerEvent : controllerProfile.getControllerEvents()) {
                for (ControllerAction controllerAction : controllerEvent.getControllerActions()) {
                    String deviceId = controllerAction.getDeviceId();
                    if (!deviceIds.contains(deviceId)) {
                        deviceIds.add(deviceId);
                    }
                }
            }
        }

        return deviceIds;
    }

    public boolean checkControllerProfileName(@NonNull String name) {
        Logger.i(TAG, "checkControllerProfileName - " + name);

        for (ControllerProfile controllerProfile : controllerProfiles) {
            if (controllerProfile.getName().equals(name)) {
                Logger.i(TAG, "  There a controller profile with this name.");
                return false;
            }
        }

        return true;
    }

    boolean addControllerProfile(@NonNull ControllerProfile controllerProfile) {
        Logger.i(TAG, "addControllerProfile - " + controllerProfile);

        if (controllerProfiles.contains(controllerProfile)) {
            Logger.w(TAG, "  Controller profile with the same name already exists.");
            return false;
        }

        controllerProfiles.add(controllerProfile);
        return true;
    }

    boolean removeControllerProfile(@NonNull ControllerProfile controllerProfile) {
        Logger.i(TAG, "removeControllerProfile - " + controllerProfile);

        if (!controllerProfiles.contains(controllerProfile)) {
            Logger.w(TAG, "  No such controller profile.");
            return false;
        }

        controllerProfiles.remove(controllerProfile);
        return true;
    }

    //
    // Object overrides
    //

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Creation))
            return false;

        Creation other = (Creation)obj;
        return Objects.equals(other.name, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
