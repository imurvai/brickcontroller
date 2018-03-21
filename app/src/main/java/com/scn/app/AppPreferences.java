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
        Logger.i(TAG, "getInfraRedDeviceType...");

        switch (getStringValue(InfraRedDeviceTypeKey)) {
            case "1":
                Logger.i(TAG, "  Infra device type: Audio");
                return InfraRedDeviceType.AUDIO_OUTPUT;

            default:
                Logger.i(TAG, "  Infra device type: build in or none");
                return InfraRedDeviceType.BUILT_IN_OR_NONE;
        }
    }

    public void putInfraRedDeviceType(InfraRedDeviceType value) {
        Logger.i(TAG, "putInfraRedDeviceType: " + value);

        switch (value) {
            case BUILT_IN_OR_NONE:
                putStringValue(InfraRedDeviceTypeKey, "0");
                break;

            case AUDIO_OUTPUT:
                putStringValue(InfraRedDeviceTypeKey, "1");
                break;
        }
    }

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
