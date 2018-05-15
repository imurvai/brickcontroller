package com.scn.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.scn.logger.Logger;

import javax.inject.Singleton;

/**
 * Created by imurvai on 2018-01-29.
 */

@Singleton
public final class AppPreferences {

    //
    // Constants
    //

    private static final String TAG = AppPreferences.class.getSimpleName();

    //
    // Members
    //

    private final Context context;

    //
    // Constructor
    //

    public AppPreferences(Context context) {
        this.context = context;
    }

    //
    // API
    //

    //
    // Private methods
    //

    private String getStringValue(String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "0");
    }

    private void putStringValue(String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }
}
