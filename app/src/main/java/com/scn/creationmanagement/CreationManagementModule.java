package com.scn.creationmanagement;

import android.content.Context;

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
    CreationRepository provideCreationRepository(Context context) {
        return new CreationRepository(context);
    }

    @Provides
    @Singleton
    CreationManager provideCreationManager(Context context, CreationRepository creationRepository) {
        return new CreationManager(context, creationRepository);
    }
}
