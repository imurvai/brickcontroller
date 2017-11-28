package com.scn.dagger;

import com.scn.ui.creationlist.CreationListActivity;
import com.scn.ui.creationlist.CreationListActivityModule;
import com.scn.ui.devicelist.DeviceListActivity;
import com.scn.ui.devicelist.DeviceListActivityModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by steve on 2017. 11. 12..
 */

@Module
public abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = CreationListActivityModule.class)
    abstract CreationListActivity bindCreationActivity();

    @ContributesAndroidInjector(modules = DeviceListActivityModule.class)
    abstract DeviceListActivity bindDeviceListActivity();
}
