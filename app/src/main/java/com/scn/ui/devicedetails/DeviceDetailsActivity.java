package com.scn.ui.devicedetails;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

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

    @BindView(R.id.toolbar) Toolbar toolbar;

    DeviceDetailsViewModel viewModel;

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

        String deviceId = getIntent().getStringExtra("EXTRA_DEVICE_ID");
        setupViewModel(deviceId);
    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");

        // TODO: disconnect device

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
            case R.id.menu_item_delete:
                Logger.i(TAG, "  delete selected.");
                showQuestionDialog(
                        getString(R.string.are_you_sure_you_want_to_remove),
                        getString(R.string.yes),
                        getString(R.string.no),
                        (dialogInterface, i) -> viewModel.removeDevice(),
                        (dialogInterface, i) -> {});
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
        viewModel.init(deviceId);

        viewModel.getDeviceMangerStateChangeLiveData().observe(DeviceDetailsActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    dismissDialog();

                    switch (stateChange.getPreviousState()) {
                        case UPDATING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error),
                                        getString(R.string.failed_to_update_device),
                                        getString(R.string.ok),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            break;

                        case REMOVING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error),
                                        getString(R.string.failed_to_remove_device),
                                        getString(R.string.ok),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                dismissDialog();
                                stateChange.resetPreviousState();
                                onBackPressed();
                            }
                            break;
                    }
                    break;

                case REMOVING:
                    showProgressDialog(getString(R.string.removing));
                    break;
            }
        });
    }
}
