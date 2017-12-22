package com.scn.ui.controlleraction;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import javax.inject.Inject;

/**
 * Created by imurvai on 2017-12-21.
 */

public class ControllerActionViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = ControllerActionViewModel.class.getSimpleName();

    private CreationManager creationManager;
    private DeviceManager deviceManager;

    private ControllerEvent controllerEvent;
    private ControllerAction controllerAction;

    //
    // Constructor
    //

    @Inject
    public ControllerActionViewModel(@NonNull CreationManager creationManager, @NonNull DeviceManager deviceManager) {
        Logger.i(TAG, "constructor...");
        this.creationManager = creationManager;
        this.deviceManager = deviceManager;
    }

    //
    // API
    //

    @MainThread
    void initialize(@NonNull ControllerEvent controllerEvent) {
        this.controllerEvent = controllerEvent;
        this.controllerAction = null;
    }

    @MainThread
    void initialize(@NonNull ControllerAction controllerAction) {
        this.controllerEvent = null;
        this.controllerAction = controllerAction;
    }

    @MainThread
    void saveControllerAction() {
        Logger.i(TAG, "saveControllerAction...");

        if (controllerEvent != null) {
            Logger.i(TAG, "  Adding the controller action...");
            //creationManager.addControllerActionAsync(controllerEvent, )
        }

        if (controllerAction != null) {
            Logger.i(TAG, "  Updating the controller action...");
            //creationManager.updateControllerActionAsync(controllerAction);
        }
    }
}
