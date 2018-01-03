package com.scn.ui.creationlist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;
import com.scn.ui.about.AboutActivity;
import com.scn.ui.creationdetails.CreationDetailsActivity;
import com.scn.ui.devicelist.DeviceListActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreationListActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    //
    // Members
    //

    private static final String TAG = CreationListActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0x1234;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton floatingActionButton;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    private CreationListViewModel viewModel;
    @Inject CreationListAdapter creationListAdapter;
    @Inject DeviceManager deviceManager;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creation_list);
        ButterKnife.bind(this);
        setupActivityComponents();

        setupViewModel();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        Logger.i(TAG, "onResume...");
        super.onResume();

        if (!deviceManager.isBluetoothLESupported()) {
            showAlertDialog(
                    getString(R.string.ble_not_supported),
                    dialogInterface -> CreationListActivity.this.finishAffinity());
        }
        else if (ContextCompat.checkSelfPermission(CreationListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreationListActivity.this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        else {
            viewModel.loadDevices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Logger.i(TAG, "onRequestPermissionsResult...");

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults == null || grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Logger.i(TAG, "  permission deined, exiting...");
                CreationListActivity.this.finishAffinity();
            }
            else {
                viewModel.loadDevices();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Logger.i(TAG, "onNavigationItemSelected...");

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_device_management:
                startActivity(new Intent(CreationListActivity.this, DeviceListActivity.class));
                break;

            //case R.id.nav_settings:
                //Toast.makeText(CreationListActivity.this, "Settings selected.", Toast.LENGTH_SHORT).show();
                //break;

            case R.id.nav_about:
                startActivity(new Intent(CreationListActivity.this, AboutActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    //
    // Private methods
    //

    private void setupActivityComponents() {
        setSupportActionBar(toolbar);

        floatingActionButton.setOnClickListener(view -> {
            showValueEnterDialog(
                    getString(R.string.enter_creation_name),
                    "",
                    value -> {
                        if (value.length() == 0) {
                            showAlertDialog(getString(R.string.creation_name_empty));
                            return;
                        }

                        if (!viewModel.checkCreationName(value)) {
                            showAlertDialog(getString(R.string.creation_name_exists));
                            return;
                        }

                        viewModel.addCreation(value);
                    });
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(CreationListActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(CreationListActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(creationListAdapter);

        creationListAdapter.setListItemClickListener((creation, itemClickAction, data) -> {
            Logger.i(TAG, "onClick - creation: " + creation);
            switch (itemClickAction) {
                case CLICK:
                    Intent intent = new Intent(CreationListActivity.this, CreationDetailsActivity.class);
                    intent.putExtra(EXTRA_CREATION_NAME, creation.getName());
                    startActivity(intent);
                    break;

                case REMOVE:
                    showQuestionDialog(
                            getString(R.string.are_you_sure_you_want_to_remove),
                            (dialogInterface, i) -> viewModel.removeCreation(creation),
                            (dialogInterface, i) -> {});
                    break;
            }
        });
    }

    private void setupViewModel() {
        viewModel = getViewModel(CreationListViewModel.class);

        viewModel.getDeviceManagerStateChangeLiveData().observe(CreationListActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    switch (stateChange.getPreviousState()) {
                        case LOADING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_loading_devices),
                                        dialogInterface -> {
                                            stateChange.resetPreviousState();
                                            viewModel.loadCreations();
                                        });
                            }
                            else {
                                dismissDialog();
                                stateChange.resetPreviousState();
                                viewModel.loadCreations();
                            }
                            break;
                    }
                    break;

                case LOADING:
                    showProgressDialog(getString(R.string.loading));
                    break;
            }
        });

        viewModel.getCreationMangerStateChangeLiveData().observe(CreationListActivity.this, stateChange -> {
            Logger.i(TAG, "Creation manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case OK:
                    switch (stateChange.getPreviousState()) {
                        case LOADING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_loading_creations),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                dismissDialog();
                                stateChange.resetPreviousState();
                            }
                            break;

                        case INSERTING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_adding_creation),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                dismissDialog();
                                stateChange.resetPreviousState();

                                Intent intent = new Intent(CreationListActivity.this, CreationDetailsActivity.class);
                                intent.putExtra(EXTRA_CREATION_NAME, (String)stateChange.getData());
                                startActivity(intent);
                            }
                            break;

                        case REMOVING:
                            if (stateChange.isError()) {
                                showAlertDialog(
                                        getString(R.string.error_during_removing_creation),
                                        dialogInterface -> stateChange.resetPreviousState());
                            }
                            else {
                                dismissDialog();
                                stateChange.resetPreviousState();
                            }
                            break;
                    }
                    break;

                case LOADING:
                    showProgressDialog(getString(R.string.loading));
                    break;

                case INSERTING:
                    showProgressDialog(getString(R.string.saving));
                    break;

                case REMOVING:
                    showProgressDialog(getString(R.string.removing));
                    break;
            }
        });

        viewModel.getCreationListListData().observe(CreationListActivity.this, creations -> {
            Logger.i(TAG, "Creation list changed.");
            creationListAdapter.setCreationList(creations);
        });
    }
}
