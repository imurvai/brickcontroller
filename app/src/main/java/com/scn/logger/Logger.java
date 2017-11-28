package com.scn.logger;

import android.util.Log;

import com.scn.ui.BuildConfig;

/**
 * Created by steve on 2017. 11. 20..
 */

public final class Logger {

    private Logger() {}

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) Log.d(tag, message);
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) Log.i(tag, message);
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) Log.w(tag, message);
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) Log.e(tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable);
    }
}
