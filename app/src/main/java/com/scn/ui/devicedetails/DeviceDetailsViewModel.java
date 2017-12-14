package com.scn.ui.devicedetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
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

    @MainThread
    void init(String deviceId) {
        Logger.i(TAG, "init - " + deviceId);

        if (deviceId == null || deviceId.length() == 0) {
            Logger.i(TAG, "  Empty deviceId.");
            return;
        }

        if (device != null) {
            Logger.i(TAG, "  Already inited.");
            return;
        }

        device = deviceManager.getDevice(deviceId);
    }

    @MainThread
    Device getDevice() { return device; }

    @MainThread
    LiveData<StateChange<DeviceManager.State>> getDeviceMangerStateChangeLiveData() {
        Logger.i(TAG, "getDeviceMangerStateChangeLiveData...");
        return deviceManager.getStateChangeLiveData();
    }

    @MainThread
    LiveData<StateChange<Device.State>> getDeviceStateChangeLiveData() {
        Logger.i(TAG, "getDeviceStateChangeLiveData...");
        return device.getStateChangeLiveData();
    }

    @MainThread
    void removeDevice() {
        Logger.i(TAG, "deleteDevice - " + device);
        deviceManager.removeDeviceAsync(device);
    }

    @MainThread
    void connectDevice() {
        Logger.i(TAG, "connectDevice - " + device);
        device.connect();
    }

    @MainThread
    void disconnectDevice() {
        Logger.i(TAG, "disconnectDevice - " + device);
        device.disconnect();
    }

    @MainThread
    void updateDevice(@NonNull String newName) {
        Logger.i(TAG, "updateDeviceAsync - " + device);
        Logger.i(TAG, "  new name: " + newName);

        if (newName.length() == 0) {
            Logger.w(TAG, "  Name can't be empty.");
            return;
        }

        deviceManager.updateDeviceAsync(device, newName);
    }

    @MainThread
    void updateDevice(@NonNull Device.OutputLevel newOutputLevel) {
        Logger.i(TAG, "updateDeviceAsync - " + device);
        Logger.i(TAG, "  new output level: " + newOutputLevel);
        deviceManager.updateDeviceAsync(device, newOutputLevel);
    }

    @MainThread
    void setOutput(int channel, int value) {
        if (device == null) return;
        device.setOutput(channel, value);
    }
}
