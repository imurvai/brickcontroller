package com.scn.creationmanagement;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by imurvai on 2017-12-13.
 */

public final class ControllerProfile {

    //
    // Members
    //

    private static final String TAG = ControllerProfile.class.getSimpleName();

    private long id;
    private String name;
    private List<ControllerEvent> controllerEvents = new ArrayList<>();

    //
    // Constructor
    //

    public ControllerProfile(long id, @NonNull String name) {
        Logger.i(TAG, "constructor - " + name);
        this.name = name;
    }

    //
    // API
    //

    public long getId() { return id; }
    public void setId(long value) { id = value; }

    public String getName() { return name; }
    public void setName(String value) { name = value; }

    public boolean addControllerEvent(ControllerEvent controllerEvent) {
        Logger.i(TAG, "addControllerEvent - " + controllerEvent);

        if (controllerEvents.contains(controllerEvent)) {
            Logger.w(TAG, "  Controller event with the same name already exists.");
            return false;
        }

        controllerEvents.add(controllerEvent);
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
