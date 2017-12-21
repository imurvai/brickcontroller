package com.scn.ui.controlleraction;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-21.
 */

public class ControllerActionActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = ControllerActionActivity.class.getSimpleName();

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_action);
        ButterKnife.bind(this);


    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        super.onBackPressed();
    }

    //
    // Private methods
    //
}
