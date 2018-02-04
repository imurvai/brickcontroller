package com.scn.dagger;

import android.app.Application;
import android.content.Context;

import com.scn.app.AppPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 2017. 11. 08..
 */

@Module
public class ApplicationModule {

    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    AppPreferences provideAppPreferences(Context context) { return new AppPreferences(context); }
}
