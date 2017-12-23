package com.scn.ui.controllerprofiledetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.creationmanagement.Creation;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by imurvai on 2017-12-20.
 */

public class ControllerProfileDetailsViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = ControllerProfileDetailsViewModel.class.getSimpleName();

    private CreationManager creationManager;

    private Creation creation;
    private ControllerProfile controllerProfile;

    private Map<String, String> deviceIdNameMap = new HashMap<>();

    //
    // Constructor
    //

    @Inject
    ControllerProfileDetailsViewModel(@NonNull CreationManager creationManager, @NonNull DeviceManager deviceManager) {
        Logger.i(TAG, "constructor...");
        this.creationManager = creationManager;

        for (Device device : deviceManager.getDeviceListLiveData().getValue()) {
            deviceIdNameMap.put(device.getId(), device.getName());
        }
    }

    //
    // API
    //

    @MainThread
    void initialize(long controllerProfileId) {
        Logger.i(TAG, "initialize - " + controllerProfileId);

        controllerProfile = creationManager.getControllerProfile(controllerProfileId);
        creation = creationManager.getCreation(controllerProfile.getCreationId());
    }

    @MainThread
    ControllerProfile getControllerProfile() {
        return controllerProfile;
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
    Map<String, String> getDeviceIdNameMap() {
        return deviceIdNameMap;
    }

    @MainThread
    boolean checkControllerProfileName(String name) {
        return creation.checkControllerProfileName(name);
    }

    @MainThread
    boolean renameControllerProfile(String name) {
        return creationManager.updateControllerProfileAsync(controllerProfile, name);
    }

    @MainThread
    ControllerEvent getControllerEvent(ControllerEvent.ControllerEventType eventType, int eventCode) {
        return controllerProfile.getControllerEvent(eventType, eventCode);
    }

    @MainThread
    boolean addControllerEvent(ControllerEvent.ControllerEventType eventType, int eventCode) {
        return creationManager.addControllerEventAsync(controllerProfile, eventType, eventCode);
    }

    @MainThread
    boolean removeControllerEvent(ControllerEvent controllerEvent) {
        return creationManager.removeControllerEventAsync(controllerProfile, controllerEvent);
    }

    @MainThread
    boolean removeControllerAction(ControllerAction controllerAction) {
        ControllerEvent controllerEvent = creationManager.getControllerEvent(controllerAction.getControllerEventId());
        return creationManager.removeControllerActionAsync(controllerProfile, controllerEvent, controllerAction, true);
    }
}
