package com.scn.ui.controlleraction;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

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

}
