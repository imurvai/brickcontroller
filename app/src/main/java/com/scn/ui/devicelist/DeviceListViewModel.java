package com.scn.ui.devicelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.scn.common.StateChange;
import com.scn.devicemanagement.Device;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 2017. 11. 13..
 */

public class DeviceListViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = DeviceListViewModel.class.getSimpleName();

    private final DeviceManager deviceManager;

    private final MutableLiveData<Integer> secondsToStopScanLiveData = new MutableLiveData<>();

    //
    // Constructor
    //

    @Inject
    DeviceListViewModel(@NonNull DeviceManager deviceManager) {
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
    public LiveData<StateChange<DeviceManager.State>> getDeviceManagerStateChangeLiveData() {
        Logger.i(TAG, "getDeviceManagerStateLiveData");
        return deviceManager.getStateChangeLiveData();
    }

    @MainThread
    public LiveData<List<Device>> getDeviceListLiveData() {
        Logger.i(TAG, "getDeviceListLiveData...");
        return deviceManager.getDeviceListLiveData();
    }

    @MainThread
    public void startDeviceScan() {
        Logger.i(TAG, "startDeviceScan...");
        deviceManager.startDeviceScan();
    }

    @MainThread
    public void stopDeviceScan() {
        Logger.i(TAG, "stopDeviceScan...");
        deviceManager.stopDeviceScan();
    }

    @MainThread
    public void deleteAllDevices() {
        Logger.i(TAG, "deleteAllDevices...");
        deviceManager.removeAllDevicesAsync();
    }

    @MainThread
    void removeDevice(Device device) {
        Logger.i(TAG, "deleteDevice - " + device);
        deviceManager.removeDeviceAsync(device);
    }
}
