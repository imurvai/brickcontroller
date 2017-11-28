package com.scn.dagger;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.scn.ui.BrickControllerViewModelFactory;
import com.scn.ui.creationlist.CreationListViewModel;
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
    abstract ViewModelProvider.Factory bindViewModelFactory(BrickControllerViewModelFactory factory);
}
