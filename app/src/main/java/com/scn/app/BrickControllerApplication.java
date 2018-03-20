package com.scn.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.scn.dagger.DaggerApplicationComponent;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by steve on 2017. 09. 24..
 */

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "brickcontroller@gmail.com")
public final class BrickControllerApplication extends Application implements HasActivityInjector {

    //
    // Private members
    //

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    //
    // Constructor
    //

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    //
    // Application overrides
    //


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (!com.scn.ui.BuildConfig.DEBUG) {
            ACRA.init(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //new GlobalExceptionHandler(this);

        DaggerApplicationComponent
                .builder()
                .application(this)
                .build()
                .inject(this);
    }

    //
    // HasActivityInjector overrides
    //

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }
}
