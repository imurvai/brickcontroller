package com.scn.creationmanagement;

import android.content.Context;
import android.support.annotation.NonNull;

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

    public CreationManager(@NonNull Context context) {
        Logger.i(TAG, "constructor...");

        this.context = context;
    }

    //
    // API
    //

    public void loadCreationsAsync() {
        Logger.i(TAG, "loadCreationsAsync...");

    }

    public void removeCreationAsync() {
        Logger.i(TAG, "removeCreationAsync...");

    }

    public boolean checkCreationName(String name) {
        Logger.i(TAG, "checkCreationName - " + name);
        return false;
    }

    public Creation getCreation(String creationName) {
        Logger.i(TAG, "getCreation - " + creationName);
        throw new RuntimeException("not implemented.");
    }
}
