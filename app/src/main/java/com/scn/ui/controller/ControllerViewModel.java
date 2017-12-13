package com.scn.ui.controller;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.scn.creationmanagement.CreationManager;
import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;

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
}
