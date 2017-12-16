package com.scn.creationmanagement;

import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
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

    private String name;
    private Map<Integer, Set<ControllerAction>> controllerActionMap = new HashMap();

    //
    // Constructor
    //

    ControllerProfile(String name) {
        Logger.i(TAG, "constructor...");
        this.name = name;
    }

    //
    // API
    //

    public String getName() { return name; }
    public void setName(String value) { name = value; }


}
