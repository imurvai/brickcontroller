package com.scn.ui.controllerprofiledetails;

import android.arch.lifecycle.ViewModel;

import com.scn.creationmanagement.CreationManager;
import com.scn.logger.Logger;

import javax.inject.Inject;

/**
 * Created by imurvai on 2017-12-20.
 */

public class ControllerProfileDetailsViewModel extends ViewModel {

    //
    // Members
    //

    private static final String TAG = ControllerProfileDetailsViewModel.class.getSimpleName();

    private CreationManager creationManager;

    //
    // Constructor
    //

    @Inject
    ControllerProfileDetailsViewModel(CreationManager creationManager) {
        Logger.i(TAG, "constructor...");
        this.creationManager = creationManager;
    }

    //
    // API
    //
}
