package com.scn.app;

import android.content.Context;
import android.content.Intent;

import com.scn.logger.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by imurvai on 2017-12-29.
 */

@Singleton
public final class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    //
    // Members
    //

    private static final String TAG = GlobalExceptionHandler.class.getSimpleName();

    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultExceptionHandler;

    //
    // Constructor
    //

    @Inject
    public GlobalExceptionHandler(Context context) {
        this.context = context;
        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    //
    // UncaughtExceptionHandler overrides
    //

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.i(TAG, "uncaughtException...");
        try {
            StringBuilder sb = new StringBuilder();

            sb.append("BrickController crash report.\n");
            sb.append("Version: " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + "\n\n\n");

            sb.append("------ Exception ------\n\n");
            sb.append(throwable.toString() + "\n\n\n");

            sb.append("------ Stack trace ------\n\n");
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                sb.append(stackTraceElement.toString() + "\n");
            }

            Throwable cause = throwable.getCause();
            if (cause != null) {
                sb.append("\n\n------ Cause ------\n\n");

                for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                    sb.append(stackTraceElement.toString() + "\n");
                }
            }

            sb.append("\n\n\n------ End of report ------");

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "imurvai@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "BrickController crash report");
            intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            context.startActivity(Intent.createChooser(intent, "Send Email"));
        }
        catch (Exception e) {
            Logger.e(TAG, "  Error in uncaughtException.", e);
        }

        defaultExceptionHandler.uncaughtException(thread, throwable);
    }
}
