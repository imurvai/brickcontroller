package com.scn.ui.controllerprofiledetails;

import dagger.Module;
import dagger.Provides;

/**
 * Created by imurvai on 2017-12-20.
 */

@Module
public class ControllerProfileDetailsActivityModule {

    @Provides
    ControllerProfileDetailsAdapter provideControllerProfileDetailsAdapter() {
        return new ControllerProfileDetailsAdapter();
    }
}
