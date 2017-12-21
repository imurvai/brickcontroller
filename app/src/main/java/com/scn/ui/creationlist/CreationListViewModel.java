package com.scn.ui.creationlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.creationmanagement.Creation;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 2017. 11. 13..
 */

public class CreationListViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = CreationListViewModel.class.getSimpleName();

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
    LiveData<StateChange<DeviceManager.State>> getDeviceManagerStateChangeLiveData() {
        Logger.i(TAG, "getDeviceManagerStateLiveData...");
        return deviceManager.getStateChangeLiveData();
    }

    @MainThread
    LiveData<StateChange<CreationManager.State>> getCreationMangerStateChangeLiveData() {
        Logger.i(TAG, "getCreationMangerStateChangeLiveData...");
        return creationManager.getStateChangeLiveData();
    }

    @MainThread
    LiveData<List<Creation>> getCreationListListData() {
        Logger.i(TAG, "getCreationListListData...");
        return creationManager.getCreationListLiveData();
    }

    @MainThread
    void loadDevices() {
        Logger.i(TAG, "loadDevices...");
        deviceManager.loadDevicesAsync();
    }

    @MainThread
    void loadCreations() {
        Logger.i(TAG, "loadCreations...");
        creationManager.loadCreationsAsync();
    }

    @MainThread
    boolean checkCreationName(@NonNull String name) {
        Logger.i(TAG, "checkCreationName - " + name);
        return creationManager.checkCreationName(name);
    }

    @MainThread
    boolean addCreation(@NonNull String creationName) {
        Logger.i(TAG, "addCreation - " + creationName);
        return creationManager.addCreationAsync(creationName, true);
    }

    @MainThread
    boolean removeCreation(@NonNull Creation creation) {
        Logger.i(TAG, "removeCreation - " + creation);
        return creationManager.removeCreationAsync(creation);
    }
}
