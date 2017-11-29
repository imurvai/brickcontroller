package com.scn.ui.devicedetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import javax.inject.Inject;

/**
 * Created by imurvai on 2017-11-29.
 */

public class DeviceDetailsViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = DeviceDetailsViewModel.class.getSimpleName();

    private DeviceManager deviceManager;
    private Device device;

    //
    // Constructor
    //

    @Inject
    public DeviceDetailsViewModel(@NonNull DeviceManager deviceManager) {
        Logger.i(TAG, "constructor...");
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

    LiveData<StateChange<DeviceManager.State>> getDeviceMangerStateChangeLiveData() {
        Logger.i(TAG, "getDeviceMangerStateChangeLiveData...");
        return deviceManager.getStateChangeLiveData();
    }

    void removeDevice() {
        Logger.i(TAG, "deleteDevice - " + device);
        deviceManager.removeDevice(device);
    }

    void connectDevice() {
        Logger.i(TAG, "connectDevice - " + device);
        device.connect();
    }

    void disconnectDevice() {
        Logger.i(TAG, "disconnectDevice - " + device);
        device.disconnect();
    }

    void updateDevice(String newName) {
        Logger.i(TAG, "updateDevice - " + device);
        Logger.i(TAG, "  new name: " + newName);
        deviceManager.updateDevice(device, newName);
    }
}
