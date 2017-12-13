package com.scn.creationmanagement;

import android.content.Context;

import com.scn.logger.Logger;

/**
 * Created by steve on 2017. 11. 01..
 */

public final class CreationManager {

    //
    // Members
    //

    private static final String TAG = CreationManager.class.getSimpleName();

    private Context context;

    //
    // Constructor
    //

    public CreationManager(Context context) {
        Logger.i(TAG, "constructor...");

        this.context = context;
    }

    //
    // API
    //

    public void loadCreations() {

    }

    public void saveCreations() {

    }
}
