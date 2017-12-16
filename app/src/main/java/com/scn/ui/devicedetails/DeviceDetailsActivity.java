package com.scn.ui.devicedetails;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.scn.devicemanagement.Device;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-11-29.
 */

public class DeviceDetailsActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = DeviceDetailsActivity.class.getSimpleName();

    DeviceDetailsViewModel viewModel;
    @Inject DeviceDetailsAdapter deviceDetailsAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        String deviceId = getIntent().getStringExtra(EXTRA_DEVICE_ID);
        setupViewModel(deviceId);
        setupRecyclerView();

        viewModel.connectDevice();
    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        viewModel.disconnectDevice();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logger.i(TAG, "onCreateOptionsMenu...");
        getMenuInflater().inflate(R.menu.menu_device_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.i(TAG, "onOptionsItemSelected...");

        switch (item.getItemId()) {
            case R.id.menu_item_edit:
                Logger.i(TAG, "  edit selected.");

                showValueEnterDialog(
                        getString(R.string.enter_device_name),
                        viewModel.getDevice().getName(),
                        newName -> {
                            if (newName.length() == 0) {
                                showAlertDialog(getString(R.string.name_cannot_be_empty));
                            }
                            viewModel.updateDevice(newName);
                        });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods
    //

    private void setupViewModel(String deviceId) {
        Logger.i(TAG, "setupViewModel - " + deviceId);

        viewModel = getViewModel(DeviceDetailsViewModel.class);

        if (deviceId != null) {
            viewModel.init(deviceId);
        }

        viewModel.getDeviceMangerStateChangeLiveData().observe(DeviceDetailsActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    dismissDialog();

                    switch (stateChange.getPreviousState()) {
                        case UPDATING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.failed_to_update_device),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                deviceDetailsAdapter.setDevice(viewModel.getDevice());
                            }
                            break;
                    }
                    break;

                case REMOVING:
                    showProgressDialog(getString(R.string.removing));
                    break;
            }
        });

        viewModel.getDeviceStateChangeLiveData().observe(DeviceDetailsActivity.this, stateStateChange -> {
            Logger.i(TAG, "Device stateChange - " + stateStateChange.getPreviousState() + " -> " + stateStateChange.getCurrentState());

            switch (stateStateChange.getCurrentState()) {
                case CONNECTING:
                    showProgressDialog(
                            getString(R.string.connecting),
                            ((dialogInterface, i) -> {
                                viewModel.disconnectDevice();
                                DeviceDetailsActivity.this.finish();
                            }));
                    break;

                case CONNECTED:
                    dismissDialog();
                    break;

                case DISCONNECTING:
                    showProgressDialog(
                            getString(R.string.disconnecting),
                            (dialogInterface, i) -> {
                                viewModel.disconnectDevice();
                                DeviceDetailsActivity.this.finish();
                            });
                    break;

                case DISCONNECTED:
                    showProgressDialog(
                            getString(R.string.reconnecting),
                            (dialogInterface, i) -> {
                                viewModel.disconnectDevice();
                                DeviceDetailsActivity.this.finish();
                            });
                    break;
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(DeviceDetailsActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(DeviceDetailsActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(deviceDetailsAdapter);

        deviceDetailsAdapter.setDevice(viewModel.getDevice());
        deviceDetailsAdapter.setOutputChangedListener((localDevice, channel, value) -> {
            //Logger.i(TAG, "onOutputChanged - " + value);
            viewModel.setOutput(channel, value);
        });
        deviceDetailsAdapter.setOutputLevelChangedListener((localDevice, outputLevel) -> {
            viewModel.updateDevice(outputLevel);
        });
    }
}
