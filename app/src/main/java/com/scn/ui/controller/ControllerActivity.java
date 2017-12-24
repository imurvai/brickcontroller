package com.scn.ui.controller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.scn.devicemanagement.Device;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import java.util.Map;

import javax.inject.Inject;

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
    @Inject ControllerAdapter controllerAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.creation_name) TextView creationNameTextView;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

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
        setupRecyclerView();

        creationNameTextView.setText(viewModel.getCreation().getName());
    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        super.onBackPressed();

        viewModel.disconnectDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_close:
                viewModel.disconnectDevices();
                ControllerActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {
            viewModel.keyDownAction(keyCode);
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) != 0 && event.getRepeatCount() == 0) {
            viewModel.keyUpAction(keyCode);
            return true;
        }

        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) != 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
            viewModel.beginControllerActions();

            for (int motionCode = 0; motionCode < 64; motionCode++) {
                int axisValue = (int)(event.getAxisValue(motionCode) * 255);

                if (Math.abs(axisValue) < 10) axisValue = 0;

                viewModel.motionAction(motionCode, axisValue);
            }

            viewModel.commitControllerActions();
            return true;
        }

        return false;
    }

    //
    // Private methods
    //

    private void setupViewModel(@NonNull String creationName) {
        viewModel = getViewModel(ControllerViewModel.class);
        viewModel.initialize(creationName);

        viewModel.getDeviceStatesLiveData().observe(ControllerActivity.this, deviceStateMap -> {
            Logger.i(TAG, "Device states have changed...");

            boolean allDevicesConnected = true;
            for (Map.Entry<Device, Device.State> kvp : deviceStateMap.entrySet()) {
                if (kvp.getValue() != Device.State.CONNECTED) {
                    Logger.i(TAG, "Device (" + kvp.getKey() + ") is not connected.");
                    allDevicesConnected = false;
                }
            }

            if (!allDevicesConnected) {
                showProgressDialog(
                        getString(R.string.connecting),
                        ((dialogInterface, i) -> {
                            viewModel.disconnectDevices();
                            ControllerActivity.this.finish();
                        }));
            }
            else {
                Logger.i(TAG, "  All devices have connected.");
                dismissDialog();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(ControllerActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ControllerActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(controllerAdapter);

        controllerAdapter.setControllerProfileList(viewModel.getControllerProfiles());
        controllerAdapter.setListItemClickListener((controllerProfile, itemClickAction, data) -> {
            Logger.i(TAG, "onClick - " + controllerProfile);
            switch (itemClickAction) {
                case CLICK:
                    viewModel.selectControllerProfile(controllerProfile);
                    break;
            }
        });
    }
}
