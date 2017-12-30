package com.scn.ui.controlleraction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.devicemanagement.Device;
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
    @BindView(R.id.channel_selector_container) RelativeLayout channelSelectorLayout;
    @BindView(R.id.part_buwizz_channels) ConstraintLayout buwizzChannelsLayout;
    @BindView(R.id.buwizz_channel1) RadioButton buwizzChannel1RadioButton;
    @BindView(R.id.buwizz_channel2) RadioButton buwizzChannel2RadioButton;
    @BindView(R.id.buwizz_channel3) RadioButton buwizzChannel3RadioButton;
    @BindView(R.id.buwizz_channel4) RadioButton buwizzChannel4RadioButton;
    @BindView(R.id.part_infra_channels) ConstraintLayout infraChannelsLayout;
    @BindView(R.id.infra_channel1) RadioButton infraChannel1RadioButton;
    @BindView(R.id.infra_channel2) RadioButton infraChannel2RadioButton;
    @BindView(R.id.part_sbrick_channels) ConstraintLayout sbrickChannelsLayout;
    @BindView(R.id.sbrick_channel1) RadioButton sbrickChannel1RadioButton;
    @BindView(R.id.sbrick_channel2) RadioButton sbrickChannel2RadioButton;
    @BindView(R.id.sbrick_channel3) RadioButton sbrickChannel3RadioButton;
    @BindView(R.id.sbrick_channel4) RadioButton sbrickChannel4RadioButton;
    @BindView(R.id.revert_channel) CheckBox isRevertChannelCheckBox;
    @BindView(R.id.toggle_button) CheckBox isToggleButtonCheckBox;
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
        setupViews();
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

                if (viewModel.checkIfControllerActionCanBeSaved()) {
                    viewModel.saveControllerAction();
                }
                else {
                    showAlertDialog(getString(R.string.controller_action_exists));
                }

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

        viewModel.getCreationManagerStateChangeLiveData().observe(ControllerActionActivity.this, stateChange -> {
            Logger.i(TAG, "Creation manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    dismissDialog();

                    switch (stateChange.getPreviousState()) {
                        case INSERTING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_adding_controller_action),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                stateChange.resetPreviousState();
                                ControllerActionActivity.this.finish();
                            }
                            break;

                        case UPDATING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_updating_controller_action),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                stateChange.resetPreviousState();
                                ControllerActionActivity.this.finish();
                            }
                            break;
                    }
                    break;

                case INSERTING:
                    showProgressDialog(getString(R.string.saving));
                    break;

                case UPDATING:
                    showProgressDialog(getString(R.string.updating));
                    break;
            }
        });
    }

    private void setupViews() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ControllerActionActivity.this, android.R.layout.simple_spinner_item, viewModel.getDeviceNameList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devicesSpinner.setAdapter(adapter);
        devicesSpinner.setSelection(viewModel.getDeviceList().indexOf(viewModel.getSelectedDevice()));
        devicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Logger.i(TAG, "onItemSelected - " + i);

                Device oldDevice = viewModel.getSelectedDevice();
                Device device = viewModel.getDeviceList().get(i);
                if (oldDevice != device) {
                    viewModel.selectDevice(device);

                    if (oldDevice.getType() != device.getType()) {
                        viewModel.selectChannel(0);
                    }
                }

                setupChannelSelectorView(viewModel.getSelectedDevice());
                setupChannelRadioButtons(viewModel.getSelectedChannel());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        setupChannelSelectorView(viewModel.getSelectedDevice());
        setupChannelRadioButtons(viewModel.getSelectedChannel());
        buwizzChannel1RadioButton.setOnClickListener(view -> { viewModel.selectChannel(0); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        buwizzChannel2RadioButton.setOnClickListener(view -> { viewModel.selectChannel(1); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        buwizzChannel3RadioButton.setOnClickListener(view -> { viewModel.selectChannel(2); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        buwizzChannel4RadioButton.setOnClickListener(view -> { viewModel.selectChannel(3); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        infraChannel1RadioButton.setOnClickListener(view -> { viewModel.selectChannel(0); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        infraChannel2RadioButton.setOnClickListener(view -> { viewModel.selectChannel(1); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        sbrickChannel1RadioButton.setOnClickListener(view -> { viewModel.selectChannel(0); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        sbrickChannel2RadioButton.setOnClickListener(view -> { viewModel.selectChannel(1); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        sbrickChannel3RadioButton.setOnClickListener(view -> { viewModel.selectChannel(2); setupChannelRadioButtons(viewModel.getSelectedChannel()); });
        sbrickChannel4RadioButton.setOnClickListener(view -> { viewModel.selectChannel(3); setupChannelRadioButtons(viewModel.getSelectedChannel()); });

        isRevertChannelCheckBox.setChecked(viewModel.getSelectedIsRevert());
        isRevertChannelCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            Logger.i(TAG, "onCheckChanged is revert - " + b);
            viewModel.selectIsRevert(b);
        });

        boolean isToggleEnabled = viewModel.getControllerEvent().getEventType() == ControllerEvent.ControllerEventType.KEY;
        isToggleButtonCheckBox.setEnabled(isToggleEnabled);
        isToggleButtonCheckBox.setChecked(viewModel.getSelectedIsToggle());
        isToggleButtonCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            Logger.i(TAG, "onCheckChanged is toggle - " + b);
            viewModel.selectIsToggle(b);
        });

        maxOutputSeekBar.setProgress(viewModel.getSelectedMaxOutput());
        maxOutputSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                viewModel.selectMaxOutput(i);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupChannelSelectorView(Device selectedDevice) {
        switch (selectedDevice.getType()) {
            case INFRARED: {
                buwizzChannelsLayout.setVisibility(View.GONE);
                infraChannelsLayout.setVisibility(View.VISIBLE);
                sbrickChannelsLayout.setVisibility(View.GONE);
                break;
            }

            case SBRICK: {
                buwizzChannelsLayout.setVisibility(View.GONE);
                infraChannelsLayout.setVisibility(View.GONE);
                sbrickChannelsLayout.setVisibility(View.VISIBLE);
                break;
            }

            case BUWIZZ:
            case BUWIZZ2: {
                buwizzChannelsLayout.setVisibility(View.VISIBLE);
                infraChannelsLayout.setVisibility(View.GONE);
                sbrickChannelsLayout.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void setupChannelRadioButtons(int selectedChannel) {
        buwizzChannel1RadioButton.setChecked(selectedChannel == 0);
        buwizzChannel2RadioButton.setChecked(selectedChannel == 1);
        buwizzChannel3RadioButton.setChecked(selectedChannel == 2);
        buwizzChannel4RadioButton.setChecked(selectedChannel == 3);
        infraChannel1RadioButton.setChecked(selectedChannel == 0);
        infraChannel2RadioButton.setChecked(selectedChannel == 1);
        sbrickChannel1RadioButton.setChecked(selectedChannel == 0);
        sbrickChannel2RadioButton.setChecked(selectedChannel == 1);
        sbrickChannel3RadioButton.setChecked(selectedChannel == 2);
        sbrickChannel4RadioButton.setChecked(selectedChannel == 3);
    }
}
