package com.scn.ui.devicelist;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.Helper;
import com.scn.ui.R;
import com.scn.ui.creationlist.CreationListActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceListActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = DeviceListActivity.class.getSimpleName();

    private DeviceListViewModel viewModel;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    private Dialog dialog;

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
        viewModel = getViewModel(DeviceListViewModel.class);

        DeviceListAdapter deviceListAdapter = new DeviceListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(DeviceListActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(DeviceListActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(deviceListAdapter);

        viewModel.getDeviceManagerStateChangeLiveData().observe(DeviceListActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    if (dialog != null) dialog.dismiss();

                    switch (stateChange.getPreviousState()) {
                        case REMOVING:
                            if (stateChange.isError()) {
                                Helper.showAlertDialog(
                                        DeviceListActivity.this,
                                        getString(R.string.error),
                                        getString(R.string.failed_to_remove_devices),
                                        getString(R.string.ok),
                                        dialogInterface -> stateChange.setErrorHandled());
                            }
                            break;

                        case SCANNING:
                            if (stateChange.isError()) {
                                Helper.showAlertDialog(
                                        DeviceListActivity.this,
                                        getString(R.string.error),
                                        getString(R.string.error_during_scanning_for_devices),
                                        getString(R.string.ok),
                                        dialogInterface -> stateChange.setErrorHandled());
                            }
                            break;
                    }
                    break;

                case SCANNING:
                    if (dialog != null) dialog.dismiss();
                    dialog = Helper.showProgressDialog(
                            DeviceListActivity.this,
                            getString(R.string.scanning),
                            (dialogInterface, i) -> viewModel.stopDeviceScan());
                    break;

                case REMOVING:
                    if (dialog != null) dialog.dismiss();
                    dialog = Helper.showProgressDialog(DeviceListActivity.this, getString(R.string.removing));
                    break;
            }
        });

        viewModel.getDeviceListLiveData().observe(DeviceListActivity.this, deviceList -> {
            Logger.i(TAG, "Device list changed...");
            deviceListAdapter.setDeviceList(deviceList);
        });
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
                Helper.showQuestionDialog(
                        DeviceListActivity.this,
                        getString(R.string.are_you_sure_you_want_to_remove),
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
}
