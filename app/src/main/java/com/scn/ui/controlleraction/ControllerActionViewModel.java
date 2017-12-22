package com.scn.ui.controlleraction;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import java.util.ArrayList;
import java.util.List;

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

    private Device selectedDevice;
    private int selctedChannel;
    private boolean selectedIsRevert;
    private boolean selectedIsToggle;
    private int selectedMaxOutput;

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
    void initialize(long controllerEventId, long controllerActionId) {
        Logger.i(TAG, "initialize - controllerEventId: " + controllerEventId + ", controllerActionId: " + controllerActionId);

        if (controllerEvent != null) {
            Logger.i(TAG, "  Already inited.");
            return;
        }

        this.controllerAction = creationManager.getControllerAction(controllerActionId);

        if (controllerAction != null) {
            this.controllerEvent = creationManager.getControllerEvent(controllerAction.getControllerEventId());
        }
        else {
            this.controllerEvent = creationManager.getControllerEvent(controllerEventId);
        }
    }

    @MainThread
    List<String> getDeviceNames() {
        List<String> deviceNames = new ArrayList<>();
        for (Device device : deviceManager.getDeviceListLiveData().getValue()) {
            deviceNames.add(device.getName());
        }
        return deviceNames;
    }

    @MainThread
    ControllerAction getControllerAction() {
        //return controllerEvent.getControllerAction();
        return null;
    }

    @MainThread
    boolean saveControllerAction() {
        Logger.i(TAG, "saveControllerAction...");

        if (controllerAction != null) {
            Logger.i(TAG, "  Updating the controller action...");
            //return creationManager.updateControllerActionAsync(controllerAction);
        }
        else {
            Logger.i(TAG, "  Adding the controller action...");
            //return creationManager.addControllerActionAsync(controllerEvent, )
        }

        return false;
    }
}
