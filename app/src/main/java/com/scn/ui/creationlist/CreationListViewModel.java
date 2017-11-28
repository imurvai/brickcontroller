package com.scn.ui.creationlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;

import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import javax.inject.Inject;

/**
 * Created by steve on 2017. 11. 13..
 */

public class CreationListViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = CreationListActivity.class.getSimpleName();

    @Inject CreationManager creationManager;
    @Inject DeviceManager deviceManager;

    //
    // Constructor
    //

    @Inject
    CreationListViewModel(CreationManager creationManager, DeviceManager deviceManager) {
        this.creationManager = creationManager;
        this.deviceManager = deviceManager;
    }

    //
    // ViewModel overrides
    //

    @Override
    protected void onCleared() {
        Logger.i(TAG, "onCleared...");
        super.onCleared();
    }

    //
    // API
    //

    @MainThread
    public LiveData<DeviceManager.StateChange> getDeviceManagerStateChangeLiveData() {
        Logger.i(TAG, "getDeviceManagerStateLiveData");
        return deviceManager.getStateChangeLiveData();
    }

    @MainThread
    public void loadDevices() {
        Logger.i(TAG, "loadDevices...");
        deviceManager.startLoadingDevices();
    }
}
