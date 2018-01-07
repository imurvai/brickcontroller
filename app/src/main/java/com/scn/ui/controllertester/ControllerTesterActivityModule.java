package com.scn.ui.controllertester;

import dagger.Module;
import dagger.Provides;

/**
 * Created by imurvai on 2018-01-07.
 */

@Module
public final class ControllerTesterActivityModule {

    @Provides
    ControllerTesterAdapter provideControllerTesterAdapter() { return new ControllerTesterAdapter(); }
}
