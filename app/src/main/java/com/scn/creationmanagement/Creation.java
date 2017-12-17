package com.scn.creationmanagement;

import android.support.annotation.NonNull;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 2017. 11. 01..
 */

public final class Creation {

    //
    // Members
    //

    private static final String TAG = Creation.class.getSimpleName();

    private long id;
    private String name;

    private final List<ControllerProfile> controllerProfiles = new ArrayList<>();

    //
    // Constructor
    //

    public Creation(String name) {
        Logger.i(TAG, "constructor...");
        this.name = name;
    }

    //
    // API
    //

    public long getId() { return id; }
    public void setId(long value) { id = value; }

    public String getName() { return name; }
    public void setName(@NonNull String value) { name = value; }

    public List<ControllerProfile> getControllerProfiles() {
        Logger.i(TAG, "getControllerProfiles...");
        return controllerProfiles;
    }

    public boolean checkControllerProfilerName(@NonNull String name) {
        Logger.i(TAG, "checkControllerProfilerName - " + name);

        for (ControllerProfile controllerProfile : controllerProfiles) {
            if (controllerProfile.getName().equals(name)) {
                Logger.i(TAG, "  There a controller profile with this name.");
                return false;
            }
        }

        return true;
    }

    public boolean addControllerProfile(ControllerProfile controllerProfile) {
        Logger.i(TAG, "addControllerProfile - " + controllerProfile);

        if (controllerProfiles.contains(controllerProfile)) {
            Logger.w(TAG, "  Controller profile with the same name already exists.");
            return false;
        }

        controllerProfiles.add(controllerProfile);
        return true;
    }

    public boolean removeControllerProfile(ControllerProfile controllerProfile) {
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
        return other.name == name;
    }

    @Override
    public String toString() {
        return name;
    }
}
