package com.scn.dagger;

import com.scn.ui.about.AboutActivity;
import com.scn.ui.about.AboutActivityModule;
import com.scn.ui.controller.ControllerActivity;
import com.scn.ui.controller.ControllerActivityModule;
import com.scn.ui.controller.ControllerViewModel;
import com.scn.ui.controlleraction.ControllerActionActivity;
import com.scn.ui.controlleraction.ControllerActionActivityModule;
import com.scn.ui.controllerprofiledetails.ControllerProfileDetailsActivity;
import com.scn.ui.controllerprofiledetails.ControllerProfileDetailsActivityModule;
import com.scn.ui.creationdetails.CreationDetailsActivity;
import com.scn.ui.creationdetails.CreationDetailsActivityModule;
import com.scn.ui.creationlist.CreationListActivity;
import com.scn.ui.creationlist.CreationListActivityModule;
import com.scn.ui.devicedetails.DeviceDetailsActivity;
import com.scn.ui.devicedetails.DeviceDetailsActivityModule;
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

    @ContributesAndroidInjector(modules = DeviceDetailsActivityModule.class)
    abstract DeviceDetailsActivity bindDeviceDetailsActivity();

    @ContributesAndroidInjector(modules = CreationDetailsActivityModule.class)
    abstract CreationDetailsActivity bindCreationDetailsActivity();

    @ContributesAndroidInjector(modules = ControllerProfileDetailsActivityModule.class)
    abstract ControllerProfileDetailsActivity bindControllerProfileDetailsActivity();

    @ContributesAndroidInjector(modules = ControllerActionActivityModule.class)
    abstract ControllerActionActivity bindControllerActionActivity();

    @ContributesAndroidInjector(modules = ControllerActivityModule.class)
    abstract ControllerActivity bindControllerActivity();

    @ContributesAndroidInjector(modules = AboutActivityModule.class)
    abstract AboutActivity bindAboutActivity();
}
