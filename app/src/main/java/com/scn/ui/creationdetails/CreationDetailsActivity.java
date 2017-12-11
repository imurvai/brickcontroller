package com.scn.ui.creationdetails;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 2017. 12. 11..
 */

public class CreationDetailsActivity extends BaseActivity {

    //
    // Private members
    //

    private static final String TAG = CreationDetailsActivity.class.getSimpleName();

    public CreationDetailsViewModel viewModel;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    //
    // Activity overrides
    //


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_creation_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);


    }

    @Override
    public void onBackPressed() {
        Logger.i(TAG, "onBackPressed...");
        super.onBackPressed();
    }

    //
    // Private methods
    //

    private void setupViewModel() {
        viewModel = getViewModel(CreationDetailsViewModel.class);


    }
}
