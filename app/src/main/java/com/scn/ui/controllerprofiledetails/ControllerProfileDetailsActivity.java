package com.scn.ui.controllerprofiledetails;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

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

        setSupportActionBar(toolbar);

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

    private void setupViewModel(long controllerProfileId) {

    }

    private void setupRecyclerView() {

    }
}
