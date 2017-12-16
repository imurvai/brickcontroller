package com.scn.creationmanagement;

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

    private String name;
    private final List<ControllerProfile> controllerProfiles = new ArrayList<>();

    //
    // Constructor
    //

    Creation(String name) {
        Logger.i(TAG, "constructor...");
        this.name = name;
    }

    //
    // API
    //

    public String getName() { return name; }
    public void setName(String value) { name = value; }
}
