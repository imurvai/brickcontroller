package com.scn.ui.devicelist;

import android.content.Intent;
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
import com.scn.ui.devicedetails.DeviceDetailsActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceListActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = DeviceListActivity.class.getSimpleName();

    private DeviceListViewModel viewModel;
    @Inject DeviceListAdapter deviceListAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        setupRecyclerView();
        setupViewModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logger.i(TAG, "onCreateOptionsMenu...");
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.i(TAG, "onOptionsItemSelected...");

        switch (item.getItemId()) {
            case R.id.menu_item_scan:
                Logger.i(TAG, "  scan selected.");
                viewModel.startDeviceScan();
                return true;

            case R.id.menu_item_delete:
                Logger.i(TAG, "  delete selected.");
                showQuestionDialog(
                        getString(R.string.are_you_sure_you_want_to_remove_all_devices),
                        getString(R.string.yes),
                        getString(R.string.no),
                        (dialogInterface, i) -> viewModel.deleteAllDevices(),
                        (dialogInterface, i) -> {});
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods
    //

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(DeviceListActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(DeviceListActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(deviceListAdapter);

        deviceListAdapter.setDeviceClickListener(new DeviceListAdapter.OnDeviceClickListener() {
            @Override
            public void onClick(final Device device) {
                Logger.i(TAG, "onClick - device: " + device);
                Intent intent = new Intent(DeviceListActivity.this, DeviceDetailsActivity.class);
                intent.putExtra(EXTRA_DEVICE_ID, device.getId());
                startActivity(intent);
            }

            @Override
            public void onRemoveClick(final Device device) {
                Logger.i(TAG, "onRemoveClick - device: " + device);
                showQuestionDialog(
                     getString(R.string.are_you_sure_you_want_to_remove),
                     getString(R.string.yes),
                     getString(R.string.no),
                     (dialogInterface, i) -> viewModel.removeDevice(device),
                     (dialogInterface, i) -> {});
            }
        });
    }

    private void setupViewModel() {
        viewModel = getViewModel(DeviceListViewModel.class);
        viewModel.getDeviceManagerStateChangeLiveData().observe(DeviceListActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    dismissDialog();

                    switch (stateChange.getPreviousState()) {
                        case REMOVING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_removing_device),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                stateChange.resetPreviousState();
                            }
                            break;

                        case SCANNING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_scanning_for_devices),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            break;
                    }
                    break;

                case SCANNING:
                    showProgressDialog(
                            getString(R.string.scanning),
                            (dialogInterface, i) -> viewModel.stopDeviceScan());
                    break;

                case REMOVING:
                    showProgressDialog(getString(R.string.removing));
                    break;
            }
        });

        viewModel.getDeviceListLiveData().observe(DeviceListActivity.this, deviceList -> {
            Logger.i(TAG, "Device list changed...");
            deviceListAdapter.setDeviceList(deviceList);
        });
    }
}
