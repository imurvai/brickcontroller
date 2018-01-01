package com.scn.ui.controllerprofiledetails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;
import com.scn.ui.controlleraction.ControllerActionActivity;
import com.scn.ui.dialogs.ControllerEventDialog;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-20.
 */

public class ControllerProfileDetailsActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = ControllerProfileDetailsActivity.class.getSimpleName();

    private ControllerProfileDetailsViewModel viewModel;
    @Inject ControllerProfileDetailsAdapter controllerProfileDetailsAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton floatingActionButton;
    @BindView(R.id.controller_profile_name) TextView controllerProfileNameTextView;
    @BindView(R.id.edit) Button editControllerProfileNameButton;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_controller_profile_details);
        ButterKnife.bind(this);

        setupActivityComponents();

        long controllerProfileId = getIntent().getLongExtra(EXTRA_CONTROLLER_PROFILE_ID, -1);
        setupViewModel(controllerProfileId);
        setupRecyclerView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //
    // Private methods
    //

    private void setupActivityComponents() {
        setSupportActionBar(toolbar);

        editControllerProfileNameButton.setOnClickListener(view -> {
            showValueEnterDialog(
                    getString(R.string.enter_controller_profile_name),
                    viewModel.getControllerProfile().getName(),
                    newName -> {
                        if (newName.length() == 0) {
                            showAlertDialog(getString(R.string.controller_profile_name_empty));
                            return;
                        }

                        if (newName.equals(viewModel.getControllerProfile().getName())) {
                            return;
                        }

                        if (!viewModel.checkControllerProfileName(newName)) {
                            showAlertDialog(getString(R.string.controller_profile_name_exists));
                            return;
                        }

                        viewModel.renameControllerProfile(newName);
                    });
        });

        floatingActionButton.setOnClickListener(view -> {
            Logger.i(TAG, "Floating action button clicked...");

            if (viewModel.getDeviceIdNameMap().values().size() > 0) {
                ControllerEventDialog dialog = new ControllerEventDialog(
                        ControllerProfileDetailsActivity.this,
                        (eventType, eventCode) -> {
                            Logger.i(TAG, "onDismiss - event type: " + eventType + ", event code: " + eventCode);

                            ControllerEvent controllerEvent = viewModel.getControllerEvent(eventType, eventCode);
                            if (controllerEvent == null) {
                                Logger.i(TAG, "  Adding new controller event...");
                                viewModel.addControllerEvent(eventType, eventCode);
                            }
                            else {
                                Logger.i(TAG, "  Adding new controller action to controller event - " + controllerEvent);
                                startControllerActionActivity(controllerEvent.getId(), -1);
                            }
                        },
                        null);
                dialog.show();
            }
            else {
                Logger.i(TAG, "  No devices scanned yet.");
                showAlertDialog(getString(R.string.no_devices));
            }
        });
    }

    private void setupViewModel(long controllerProfileId) {
        viewModel = getViewModel(ControllerProfileDetailsViewModel.class);
        viewModel.initialize(controllerProfileId);

        viewModel.getCreationMangerStateChangeLiveData().observe(ControllerProfileDetailsActivity.this, stateChange -> {
            Logger.i(TAG, "Creation manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    dismissDialog();

                    switch (stateChange.getPreviousState()) {
                        case INSERTING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_adding_controller_event),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                stateChange.resetPreviousState();

                                long controllerEventId = (long)stateChange.getData();
                                startControllerActionActivity(controllerEventId, -1);
                            }
                            break;

                        case REMOVING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_removing_controller_event),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                stateChange.resetPreviousState();
                            }
                            break;

                        case UPDATING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_updating_controller_profile),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                stateChange.resetPreviousState();
                            }
                            break;
                    }
                    break;

                case INSERTING:
                    showProgressDialog(getString(R.string.saving));
                    break;

                case REMOVING:
                    showProgressDialog(getString(R.string.removing));
                    break;

                case UPDATING:
                    showProgressDialog(getString(R.string.updating));
                    break;
            }
        });

        viewModel.getCreationListLiveData().observe(ControllerProfileDetailsActivity.this, creations -> {
            ControllerProfile controllerProfile = viewModel.getControllerProfile();
            controllerProfileNameTextView.setText(controllerProfile.getName());
            controllerProfileDetailsAdapter.setControllerEventList(controllerProfile.getControllerEvents());
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(ControllerProfileDetailsActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ControllerProfileDetailsActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(controllerProfileDetailsAdapter);

        controllerProfileDetailsAdapter.setControllerEventOnListItemClickListener((controllerEvent, itemClickAction, data) -> {
            Logger.i(TAG, "onClick - controller event: " + controllerEvent + ", action: " + itemClickAction);
            switch (itemClickAction) {
                case REMOVE:
                    showQuestionDialog(
                            getString(R.string.are_you_sure_you_want_to_remove),
                            (dialogInterface, i) -> viewModel.removeControllerEvent(controllerEvent),
                            ((dialogInterface, i) -> {}));
                    break;
            }
        });

        controllerProfileDetailsAdapter.setControllerActionOnListItemClickListener((controllerAction, itemClickAction, data) -> {
            Logger.i(TAG, "onClick - controller action: " + controllerAction+ ", action: " + itemClickAction);
            switch (itemClickAction) {
                case CLICK:
                    if (viewModel.getDeviceIdNameMap().containsKey(controllerAction.getDeviceId())) {
                        startControllerActionActivity(controllerAction.getControllerEventId(), controllerAction.getId());
                    }
                    else {
                        showAlertDialog(getString(R.string.device_not_found));
                    }
                    break;

                case REMOVE:
                    showQuestionDialog(
                            getString(R.string.are_you_sure_you_want_to_remove),
                            (dialogInterface, i) -> viewModel.removeControllerAction(controllerAction),
                            ((dialogInterface, i) -> {}));
                    break;
            }
        });

        controllerProfileDetailsAdapter.setDeviceIdNameMap(viewModel.getDeviceIdNameMap());
    }

    private void startControllerActionActivity(long controllerEventId, long controllerActionId) {
        Logger.i(TAG, "Start controller action activity - controller event id: " + controllerEventId);

        Intent intent = new Intent(ControllerProfileDetailsActivity.this, ControllerActionActivity.class);
        intent.putExtra(EXTRA_CONTROLLER_EVENT_ID, controllerEventId);
        intent.putExtra(EXTRA_CONTROLLER_ACTION_ID, controllerActionId);
        startActivity(intent);
    }
}
