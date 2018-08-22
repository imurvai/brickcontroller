package com.scn.ui.controller;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.scn.common.StateChange;
import com.scn.creationmanagement.ControllerAction;
import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.creationmanagement.Creation;
import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;
import com.scn.ui.dialogs.MessageBox;

import java.util.HashMap;
import java.util.List;
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
    private ControllerProfile selectedControllerProfile;

    private Map<String, Device> deviceMap = new HashMap<>();
    private MutableLiveData<Map<Device, Device.State>> deviceStatesLiveData = new MutableLiveData<>();

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

        for (Device device : deviceMap.values()) {
            device.getStateChangeLiveData().removeObserver(deviceStateChangeObserver);
        }
    }

    //
    // API
    //

    @MainThread
    void initialize(String creationName) {
        Logger.i(TAG, "initialize - " + creationName);

        if (creationName == null || creationName.length() == 0) {
            Logger.w(TAG, "  Empty creation name.");
            return;
        }

        if (creation != null) {
            Logger.i(TAG, "  Creation has already been set.");
            return;
        }

        creation = creationManager.getCreation(creationName);
        selectedControllerProfile = creation.getControllerProfiles().get(0);

        for (String deviceId : creation.getUsedDeviceIds()) {
            Device device = deviceManager.getDevice(deviceId);
            if (device != null && !deviceMap.containsKey(deviceId)) {
                deviceMap.put(deviceId, device);

                device.getStateChangeLiveData().observeForever(deviceStateChangeObserver);
            }
        }
    }

    @MainThread
    Creation getCreation() {
        return creation;
    }

    @MainThread
    List<ControllerProfile> getControllerProfiles() {
        return creation.getControllerProfiles();
    }

    @MainThread
    LiveData<Map<Device, Device.State>> getDeviceStatesLiveData() {
        return deviceStatesLiveData;
    }

    @MainThread
    boolean connectDevices() {
        Logger.i(TAG, "connectDevices...");

        boolean allSuccess = true;
        for (Device device : deviceMap.values()) {
            if (!device.connect())
            {
                allSuccess = false;
                break;
            }
        }

        if (!allSuccess) {
            disconnectDevices();
        }

        return allSuccess;
    }

    @MainThread
    void disconnectDevices() {
        Logger.i(TAG, "disconnectDevices...");
        for (Device device : deviceMap.values()) {
            device.disconnect();
        }
    }

    @MainThread
    void selectControllerProfile(ControllerProfile controllerProfile) {
        Logger.i(TAG, "selectControllerProfile - " + controllerProfile);
        selectedControllerProfile = controllerProfile;
    }

    @MainThread
    void keyDownAction(int keyCode) {
        for (ControllerEvent controllerEvent : selectedControllerProfile.getControllerEvents()) {
            if (controllerEvent.getEventType() == ControllerEvent.ControllerEventType.KEY && controllerEvent.getEventCode() == keyCode) {
                for (ControllerAction controllerAction : controllerEvent.getControllerActions()) {
                    Device device = deviceMap.get(controllerAction.getDeviceId());
                    int channel = controllerAction.getChannel();

                    if (controllerAction.getIsToggle()) {
                        int currentValue = device.getOutput(controllerAction.getChannel());
                        if (currentValue == 0) {
                            int outputValue = calculateOutputValue(255, controllerAction.getIsInvert(), controllerAction.getMaxOutput());
                            device.setOutput(channel, outputValue);
                        }
                        else {
                            device.setOutput(channel, 0);
                        }
                    }
                    else {
                        int outputValue = calculateOutputValue(255, controllerAction.getIsInvert(), controllerAction.getMaxOutput());
                        device.setOutput(channel, outputValue);
                    }
                }
            }
        }
    }

    @MainThread
    void keyUpAction(int keyCode) {
        for (ControllerEvent controllerEvent : selectedControllerProfile.getControllerEvents()) {
            if (controllerEvent.getEventType() == ControllerEvent.ControllerEventType.KEY && controllerEvent.getEventCode() == keyCode) {
                for (ControllerAction controllerAction : controllerEvent.getControllerActions()) {
                    Device device = deviceMap.get(controllerAction.getDeviceId());
                    int channel = controllerAction.getChannel();

                    if (!controllerAction.getIsToggle()) {
                        device.setOutput(channel, 0);
                    }
                }
            }
        }
    }

    @MainThread
    void beginControllerActions() {
        actionMap.clear();
    }

    @MainThread
    void motionAction(int motionCode, int value) {
        for (ControllerEvent controllerEvent : selectedControllerProfile.getControllerEvents()) {
            if (controllerEvent.getEventType() == ControllerEvent.ControllerEventType.MOTION && controllerEvent.getEventCode() == motionCode) {
                for (ControllerAction controllerAction : controllerEvent.getControllerActions()) {
                    Device device = deviceMap.get(controllerAction.getDeviceId());
                    int channel = controllerAction.getChannel();

                    int outputValue = calculateOutputValue(value, controllerAction.getIsInvert(), controllerAction.getMaxOutput());
                    addAction(device, channel, outputValue);
                }
            }
        }
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

    //
    // Private methods
    //

    private Observer<StateChange<Device.State>> deviceStateChangeObserver = stateStateChange -> {
        Logger.i(TAG, "deviceStateChanged");

        Map<Device, Device.State> deviceStateMap = new HashMap<>();
        for (Device device : deviceMap.values()) {
            Device.State deviceState = device.getStateChangeLiveData().getValue().getCurrentState();
            deviceStateMap.put(device, deviceState);
        }

        deviceStatesLiveData.setValue(deviceStateMap);
    };

    private int calculateOutputValue(int value, boolean isInvert, int maxOutput) {
        int v = (value * maxOutput) / 100;
        return isInvert ? -v : v;
    }

    private void addAction(@NonNull Device device, int channel, int outputValue) {
        Pair<Device, Integer> deviceChannel = new Pair(device, channel);
        if (actionMap.containsKey(deviceChannel)) {
            int currentValue = actionMap.get(deviceChannel);
            int newValue = Math.min(255, Math.max(-255, currentValue + outputValue));
            actionMap.put(deviceChannel, newValue);
        }
        else {
            actionMap.put(deviceChannel, outputValue);
        }
    }
}
