package com.scn.ui.controllerprofiledetails;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.scn.creationmanagement.ControllerEvent;
import com.scn.creationmanagement.ControllerProfile;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_creation_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit:
                showValueEnterDialog(
                        getString(R.string.enter_controller_profile_name),
                        viewModel.getControllerProfile().getName(),
                        newName -> {
                            if (newName.length() == 0) {
                                showAlertDialog(getString(R.string.controller_profile_name_empty));
                                return;
                            }

                            if (!viewModel.checkControllerProfileName(newName)) {
                                showAlertDialog(getString(R.string.controller_profile_name_exists));
                                return;
                            }

                            viewModel.renameControllerProfile(newName);
                        });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //
    // Private methods
    //

    private void setupActivityComponents() {
        setSupportActionBar(toolbar);

        floatingActionButton.setOnClickListener(view -> {
            Logger.i(TAG, "Floating action button clicked...");
            ControllerEventDialog dialog = new ControllerEventDialog(ControllerProfileDetailsActivity.this, (eventType, eventCode) -> {
                Logger.i(TAG, "onDismiss...");
                // TODO: add controller event
            });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
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

                                // TODO: open controller action editor
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

    }
}
