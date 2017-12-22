package com.scn.ui.controlleraction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-21.
 */

public class ControllerActionActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = ControllerActionActivity.class.getSimpleName();

    private ControllerActionViewModel viewModel;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.devices_spinner) Spinner devicesSpinner;
    @BindView(R.id.channel_group) RadioGroup channelGroup;
    @BindView(R.id.channel1) RadioButton channel1RadioButton;
    @BindView(R.id.channel2) RadioButton channel2RadioButton;
    @BindView(R.id.channel3) RadioButton channel3RadioButton;
    @BindView(R.id.channel4) RadioButton channel4RadioButton;
    @BindView(R.id.revert_channel) CheckBox revertChannelCheckBox;
    @BindView(R.id.toggle_button) CheckBox toggleButtonCheckBox;
    @BindView(R.id.max_output) AppCompatSeekBar maxOutputSeekBar;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_action);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        long controllerEventId = getIntent().getLongExtra(EXTRA_CONTROLLER_EVENT_ID, -1);
        long controllerActionId = getIntent().getLongExtra(EXTRA_CONTROLLER_ACTION_ID, -1);
        setupViewModel(controllerEventId, controllerActionId);

        devicesSpinner.setAdapter(new ArrayAdapter<String>(ControllerActionActivity.this, android.R.layout.simple_list_item_1, viewModel.getDeviceNames()));

    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_controller_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_ok:

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods
    //

    private void setupViewModel(long controllerEventId, long controllerActionId) {
        viewModel = getViewModel(ControllerActionViewModel.class);
        viewModel.initialize(controllerEventId, controllerActionId);

    }
}
