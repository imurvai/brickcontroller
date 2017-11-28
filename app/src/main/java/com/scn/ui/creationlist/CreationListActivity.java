package com.scn.ui.creationlist;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.scn.devicemanagement.DeviceManager;
import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.Helper;
import com.scn.ui.R;
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

    @Inject DeviceManager deviceManager;

    CreationListViewModel viewModel;
    Dialog dialog;

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
        requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_REQUEST_COARSE_LOCATION);

        viewModel = getViewModel(CreationListViewModel.class);

        viewModel.getDeviceManagerStateChangeLiveData().observe(CreationListActivity.this, stateChange -> {
            Logger.i(TAG, "Device manager stateChange - " + stateChange.getPreviousState() + " -> " + stateChange.getCurrentState());

            switch (stateChange.getCurrentState()) {
                case Ok:
                    if (dialog != null) dialog.dismiss();
                    break;

                case Loading:
                    if (dialog != null) dialog.dismiss();
                    dialog = Helper.showProgressDialog(CreationListActivity.this, getString(R.string.loading));
                    break;

                case Scanning:
                    break;
            }
        });
    }

    @Override
    protected void onResume() {
        Logger.i(TAG, "onResume...");

        viewModel.loadDevices();

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Logger.i(TAG, "onRequestPermissionsResult...");

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Logger.i(TAG, "  permission deined, exiting...");
                CreationListActivity.this.finish();
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

            case R.id.nav_settings:
                Toast.makeText(CreationListActivity.this, "Settings selected.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_about:
                Toast.makeText(CreationListActivity.this, "About selected.", Toast.LENGTH_SHORT).show();
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
            Logger.i(TAG, "Floating action button clicked...");
            Helper.showMessageBox(CreationListActivity.this, "Add clicked.");
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }
}