package com.scn.ui.controller;

import dagger.Module;
import dagger.Provides;

/**
 * Created by imurvai on 2017-12-13.
 */

@Module
public class ControllerActivityModule {

    @Provides
    public ControllerAdapter provideControllerAdapter() {
        return new ControllerAdapter();
    }
}
