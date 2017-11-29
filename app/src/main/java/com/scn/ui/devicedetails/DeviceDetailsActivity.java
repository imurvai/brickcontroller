package com.scn.ui.devicedetails;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.Helper;
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

    @Inject
    DeviceManager deviceManager;

    @BindView(R.id.toolbar) Toolbar toolbar;

    DeviceDetailsViewModel viewModel;
    Dialog dialog;

    //
    // Activity overrides
    //

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState, persistentState);

        setContentView(R.layout.activity_device_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        viewModel = getViewModel(DeviceDetailsViewModel.class);

        viewModel.getDeviceMangerStateChangeLiveData().observe(DeviceDetailsActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    if (dialog != null) dialog.dismiss();

                    switch (stateChange.getPreviousState()) {
                        case UPDATING:
                            if (stateChange.isError()) {
                                Helper.showAlertDialog(
                                        DeviceDetailsActivity.this,
                                        getString(R.string.error),
                                        getString(R.string.failed_to_update_device),
                                        getString(R.string.ok),
                                        dialogInterface -> stateChange.setErrorHandled());
                            }
                            break;

                        case REMOVING:
                            if (stateChange.isError()) {
                                Helper.showAlertDialog(
                                        DeviceDetailsActivity.this,
                                        getString(R.string.error),
                                        getString(R.string.failed_to_remove_device),
                                        getString(R.string.ok),
                                        dialogInterface -> stateChange.setErrorHandled());
                            }
                            else {
                                DeviceDetailsActivity.this.finish();
                            }
                            break;
                    }
                    break;

                case REMOVING:
                    if (dialog != null) dialog.dismiss();
                    dialog = Helper.showProgressDialog(DeviceDetailsActivity.this, getString(R.string.removing));
                    break;
            }
        });
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
                Helper.showQuestionDialog(
                        DeviceDetailsActivity.this,
                        getString(R.string.are_you_sure_you_want_to_remove),
                        getString(R.string.ok),
                        getString(R.string.cancel),
                        (dialogInterface, i) -> viewModel.removeDevice(),
                        (dialogInterface, i) -> {});
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
