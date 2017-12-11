package com.scn.ui.creationdetails;

import android.arch.lifecycle.ViewModel;

import com.scn.logger.Logger;

import javax.inject.Inject;

/**
 * Created by steve on 2017. 12. 11..
 */

public class CreationDetailsViewModel extends ViewModel {

    //
    // Private members
    //

    private static final String TAG = CreationDetailsViewModel.class.getSimpleName();

    //
    // Constructor
    //

    @Inject
    public CreationDetailsViewModel() {
        Logger.i(TAG, "constructor...");
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
