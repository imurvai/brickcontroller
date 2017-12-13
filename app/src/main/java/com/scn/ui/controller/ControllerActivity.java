package com.scn.ui.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;

/**
 * Created by imurvai on 2017-12-13.
 */

public class ControllerActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = ControllerActivity.class.getSimpleName();

    //
    // Activity overrides
    //


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        super.onBackPressed();
    }
}
