package com.scn.dagger;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.scn.ui.BrickControllerViewModelFactory;
import com.scn.ui.controller.ControllerViewModel;
import com.scn.ui.controlleraction.ControllerActionViewModel;
import com.scn.ui.controllerprofiledetails.ControllerProfileDetailsViewModel;
import com.scn.ui.creationdetails.CreationDetailsViewModel;
import com.scn.ui.creationlist.CreationListViewModel;
import com.scn.ui.devicedetails.DeviceDetailsViewModel;
import com.scn.ui.devicelist.DeviceListViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Created by steve on 2017. 11. 13..
 */

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(CreationListViewModel.class)
    abstract ViewModel bindCreationListViewModel(CreationListViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(DeviceListViewModel.class)
    abstract ViewModel bindDeviceListViewModel(DeviceListViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(DeviceDetailsViewModel.class)
    abstract ViewModel bindDeviceDetailsViewModel(DeviceDetailsViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(CreationDetailsViewModel.class)
    abstract ViewModel bindCreationDetailsViewModel(CreationDetailsViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(ControllerProfileDetailsViewModel.class)
    abstract ViewModel bindControllerProfileDetailsViewModel(ControllerProfileDetailsViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(ControllerActionViewModel.class)
    abstract ViewModel bindControllerActionViewModel(ControllerActionViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(ControllerViewModel.class)
    abstract ViewModel bindControllerViewModel(ControllerViewModel vm);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(BrickControllerViewModelFactory factory);
}
