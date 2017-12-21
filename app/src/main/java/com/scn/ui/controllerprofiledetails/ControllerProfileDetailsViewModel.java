package com.scn.ui.controllerprofiledetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;

import com.scn.common.StateChange;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.creationmanagement.Creation;
import com.scn.creationmanagement.CreationManager;
import com.scn.logger.Logger;

import java.util.List;

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

    //
    // Constructor
    //

    @Inject
    ControllerProfileDetailsViewModel(CreationManager creationManager) {
        Logger.i(TAG, "constructor...");
        this.creationManager = creationManager;
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
    boolean checkControllerProfileName(String name) {
        return creation.checkControllerProfileName(name);
    }

    @MainThread
    boolean renameControllerProfile(String name) {
        return creationManager.updateControllerProfileAsync(controllerProfile, name);
    }

    @MainThread
    boolean addControllerEvent(ControllerEvent.ControllerEventType eventType, int eventCode) {
        return creationManager.addControllerEventAsync(controllerProfile, eventType, eventCode);
    }
}
