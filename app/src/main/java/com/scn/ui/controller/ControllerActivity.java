package com.scn.ui.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-13.
 */

public class ControllerActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = ControllerActivity.class.getSimpleName();

    ControllerViewModel viewModel;

    @BindView(R.id.toolbar) Toolbar toolbar;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        String creationName = getIntent().getStringExtra(EXTRA_CREATION_NAME);
        setupViewModel(creationName);

        viewModel.connectDevices();
    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        super.onBackPressed();

        viewModel.disconnectDevices();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {

            // TODO:

            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {

            // TODO:

            return true;
        }

        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {

            // TODO:

            return true;
        }

        return false;
    }

    //
    // Private methods
    //

    private void setupViewModel(String creationName) {
        viewModel = getViewModel(ControllerViewModel.class);
        viewModel.initialize(creationName);
    }
}
