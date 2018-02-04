package com.scn.app;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

/**
 * Created by imurvai on 2018-01-29.
 */

@Singleton
public final class AppPreferences {

    //
    // Constants
    //

    private static final String PreferencesName = "com.scn.BrickController.Prefs";

    private static final String InfraRedDeviceTypeKey = "InfraRedDeviceTypeKey";

    public enum InfraRedDeviceType {
        BUILT_IN_OR_NONE,
        AUDIO_OUTPUT
        // USB
    }

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

    public InfraRedDeviceType getInfraRedDeviceType() {
        switch (getIntValue(InfraRedDeviceTypeKey)) {
            case 1: return InfraRedDeviceType.AUDIO_OUTPUT;
            default: return InfraRedDeviceType.BUILT_IN_OR_NONE;
        }
    }

    public void putInfraRedDeviceType(InfraRedDeviceType value) {
        switch (value) {
            case BUILT_IN_OR_NONE: putIntValue(InfraRedDeviceTypeKey, 0);
            case AUDIO_OUTPUT: putIntValue(InfraRedDeviceTypeKey, 1);
        }
    }

    //
    // Private methods
    //

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getSharedPreferencesEditor() {
        return getSharedPreferences().edit();
    }

    private int getIntValue(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    private void putIntValue(String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor();
        editor.putInt(key, value);
        editor.commit();
    }
}
