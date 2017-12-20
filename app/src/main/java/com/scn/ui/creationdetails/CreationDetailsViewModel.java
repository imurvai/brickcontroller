package com.scn.ui.creationdetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.creationmanagement.Creation;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 2017. 12. 11..
 */

public class CreationDetailsViewModel extends ViewModel {

    //
    // Private members
    //

    private static final String TAG = CreationDetailsViewModel.class.getSimpleName();

    private CreationManager creationManager;
    private DeviceManager deviceManager;

    private Creation creation;

    //
    // Constructor
    //

    @Inject
    public CreationDetailsViewModel(@NonNull CreationManager creationManager, @NonNull DeviceManager deviceManager) {
        Logger.i(TAG, "constructor...");
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
    void initialize(String creationName) {
        Logger.i(TAG, "initialize - " + creationName);

        if (creationName == null || creationName.length() == 0) {
            return;
        }

        creation = creationManager.getCreation(creationName);
    }

    @MainThread
    Creation getCreation() {
        return creation;
    }

    @MainThread
    LiveData<StateChange<CreationManager.State>> getCreationMangerStateChangeLiveData() {
        return creationManager.getStateChangeLiveData();
    }

    @MainThread
    LiveData<List<Creation>> getCreationListLiveData() {
        return creationManager.getCreationListLiveData();
    }

    @MainThread
    boolean checkCreationName(@NonNull String name) {
        Logger.i(TAG, "checkCreationName - " + name);
        return creationManager.checkCreationName(name);
    }

    @MainThread
    boolean checkIfCreationPlayable() {
        Logger.i(TAG, "checkCreationPlayability...");
        return creation.getUsedDeviceIds().size() > 0;
    }

    @MainThread
    boolean checkIfDevicesAreAvailable() {
        Logger.i(TAG, "checkIfDevicesAreAvailable...");

        for (String deviceId : creation.getUsedDeviceIds()) {
            if (deviceManager.getDevice(deviceId) == null) {
                Logger.w(TAG, "  Device (" + deviceId + ") is missing.");
                return false;
            }
        }

        return true;
    }

    @MainThread
    boolean renameCreation(@NonNull String newName) {
        Logger.i(TAG, "renameCreation - " + newName);
        return creationManager.updateCreationAsync(creation, newName);
    }

    @MainThread
    boolean checkControllerProfileName(@NonNull String name) {
        Logger.i(TAG, "checkControllerProfileName - " + name);
        return creation.checkControllerProfileName(name);
    }

    @MainThread
    boolean addControllerProfile(@NonNull String name) {
        Logger.i(TAG, "addControllerProfile - " + name);
        return creationManager.addControllerProfileAsync(creation, name);
    }

    @MainThread
    boolean removeControllerProfile(ControllerProfile controllerProfile) {
        Logger.i(TAG, "removeControllerProfile - " + controllerProfile);
        return creationManager.removeControllerProfileAsync(creation, controllerProfile);
    }
}
