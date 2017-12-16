package com.scn.creationmanagement.dagger;

import android.content.Context;

import com.scn.creationmanagement.CreationManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by imurvai on 2017-12-17.
 */

@Module
public class CreationManagementModule {

    @Provides
    @Singleton
    CreationManager providesCreationManager(Context context) {
        return new CreationManager(context);
    }
}
