package com.scn.ui.controller;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.creationmanagement.Creation;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by imurvai on 2017-12-13.
 */

public class ControllerViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = ControllerViewModel.class.getSimpleName();

    private DeviceManager deviceManager;
    private CreationManager creationManager;

    private Creation creation;

    private Map<String, Device> deviceMap = new HashMap<>();
    private Map<Pair<Device, Integer>, Integer> actionMap = new HashMap<>();

    //
    // Constructor
    //

    @Inject
    public ControllerViewModel(@NonNull DeviceManager deviceManager, @NonNull CreationManager creationManager) {
        Logger.i(TAG, "contructor...");
        this.deviceManager = deviceManager;
        this.creationManager = creationManager;
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
    void setCreation(String creationName) {
        Logger.i(TAG, "setCreation - " + creationName);

        if (creation != null) {
            Logger.i(TAG, "  Creation has already been set.");
            return;
        }

        creation = creationManager.getCreation(creationName);

        for (ControllerProfile controllerProfile : creation.getControllerProfiles()) {

        }
    }

    @MainThread
    void connectDevices() {
        Logger.i(TAG, "connectDevices...");
        throw new RuntimeException("not implemented.");
    }

    @MainThread
    void disconnectDevices() {
        Logger.i(TAG, "disconnectDevices...");
        throw new RuntimeException("not implemented.");
    }

    @MainThread
    void setControllerProfile(String controllerProfileId) {
        Logger.i(TAG, "setControllerProfile - " + controllerProfileId);
        throw new RuntimeException("not implemented.");
    }

    @MainThread
    void beginControllerActions() {
        actionMap.clear();
    }

    @MainThread
    void controllerAction(ControllerEvent controllerEvent, int value) {

    }

    @MainThread
    void commitControllerActions() {
        for (Map.Entry<Pair<Device, Integer>, Integer> entry : actionMap.entrySet()) {
            Device device = entry.getKey().first;
            int channel = entry.getKey().second;
            int outputValue = entry.getValue();
            device.setOutput(channel, outputValue);
        }
    }
}
