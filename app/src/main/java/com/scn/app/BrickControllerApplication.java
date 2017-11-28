package com.scn.app;

import android.app.Activity;
import android.app.Application;

import com.scn.dagger.DaggerApplicationComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Created by steve on 2017. 09. 24..
 */

public final class BrickControllerApplication extends Application implements HasActivityInjector {

    //
    // Private members
    //

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    //
    // Application overrides
    //

    @Override
    public void onCreate() {
        super.onCreate();

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
