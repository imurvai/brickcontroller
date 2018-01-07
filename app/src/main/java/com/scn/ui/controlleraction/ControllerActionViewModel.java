package com.scn.ui.controlleraction;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
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

    private final CreationManager creationManager;
    private final DeviceManager deviceManager;

    private final List<Device> deviceList = new ArrayList<>();
    private final List<String> deviceNameList = new ArrayList<>();

    private ControllerEvent controllerEvent;
    private ControllerAction controllerAction;

    private Device selectedDevice;
    private int selectedChannel;
    private boolean selectedIsInvert;
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

        for (Device device : deviceManager.getDeviceListLiveData().getValue()) {
            if (deviceManager.getSupportedDeviceTypes().contains(device.getType())) {
                deviceList.add(device);
                deviceNameList.add(device.getName());
            }
        }
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

        controllerAction = creationManager.getControllerAction(controllerActionId);

        if (controllerAction != null) {
            controllerEvent = creationManager.getControllerEvent(controllerAction.getControllerEventId());

            selectedDevice = deviceManager.getDevice(controllerAction.getDeviceId());
            selectedChannel = controllerAction.getChannel();
            selectedIsInvert = controllerAction.getIsInvert();
            selectedIsToggle = controllerAction.getIsToggle();
            selectedMaxOutput = controllerAction.getMaxOutput();
        }
        else {
            controllerEvent = creationManager.getControllerEvent(controllerEventId);

            selectedDevice = deviceList.get(0);
            selectedChannel = 0;
            selectedIsInvert = false;
            selectedIsToggle = false;
            selectedMaxOutput = 100;
        }
    }

    @MainThread
    LiveData<StateChange<CreationManager.State>> getCreationManagerStateChangeLiveData() {
        return creationManager.getStateChangeLiveData();
    }

    @MainThread
    ControllerEvent getControllerEvent() {
        return controllerEvent;
    }

    @MainThread
    List<Device> getDeviceList() { return deviceList; }

    @MainThread
    List<String> getDeviceNameList() { return deviceNameList; }

    @MainThread
    Device getSelectedDevice() {
        return selectedDevice;
    }

    @MainThread
    int getSelectedChannel() {
        return selectedChannel;
    }

    @MainThread
    boolean getSelectedIsInvert() {
        return selectedIsInvert;
    }

    @MainThread
    boolean getSelectedIsToggle() {
        return selectedIsToggle;
    }

    @MainThread
    int getSelectedMaxOutput() {
        return selectedMaxOutput;
    }

    @MainThread
    void selectDevice(@NonNull Device device) {
        Logger.i(TAG, "selectDevice - " + device);

        if (device.getNumberOfChannels() <= selectedChannel) {
            selectedChannel = 0;
        }
        selectedDevice = device;
    }

    @MainThread
    void selectChannel(int channel) {
        Logger.i(TAG, "selectChannel - " + channel);
        selectedChannel = channel;
    }

    @MainThread
    void selectIsRevert(boolean isRevert) {
        Logger.i(TAG, "selectIsRevert - " + isRevert);
        selectedIsInvert = isRevert;
    }

    @MainThread
    void selectIsToggle(boolean isToggle) {
        Logger.i(TAG, "selectIsToggle - " + isToggle);
        selectedIsToggle = isToggle;
    }

    @MainThread
    void selectMaxOutput(int maxOutput) {
        Logger.i(TAG, "selectMaxOutput - " + maxOutput);
        selectedMaxOutput = maxOutput;
    }

    @MainThread
    boolean checkIfControllerActionCanBeSaved() {
        ControllerAction ca = controllerEvent.getControllerAction(selectedDevice.getId(), selectedChannel);
        if (ca == null) {
            return true;
        }

        if (controllerAction != null) {
            return ca.getId() == controllerAction.getId();
        }

        return false;
    }

    @MainThread
    boolean saveControllerAction() {
        Logger.i(TAG, "saveControllerAction...");

        if (controllerAction != null) {
            Logger.i(TAG, "  Updating the controller action...");
            return creationManager.updateControllerActionAsync(controllerAction, selectedDevice.getId(), selectedChannel, selectedIsInvert, selectedIsToggle, selectedMaxOutput);
        }
        else {
            Logger.i(TAG, "  Adding the controller action...");
            return creationManager.addControllerActionAsync(controllerEvent, selectedDevice.getId(), selectedChannel, selectedIsInvert, selectedIsToggle, selectedMaxOutput);
        }
    }
}
