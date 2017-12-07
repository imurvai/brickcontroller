package com.scn.ui.devicedetails;

import dagger.Module;
import dagger.Provides;

/**
 * Created by imurvai on 2017-11-29.
 */

@Module
public class DeviceDetailsActivityModule {

    @Provides
    public DeviceDetailsAdapter provideDeviceDetails() { return new DeviceDetailsAdapter(); }
}
