package com.scn.ui.devicelist;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 2017. 11. 09..
 */

@Module
public class DeviceListActivityModule {

    @Provides
    public DeviceListAdapter provideDeviceListAdapter() {
        return new DeviceListAdapter();
    }
}
