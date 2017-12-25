package com.scn.ui.about;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imurvai on 2017-12-18.
 */

public class AboutActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = AboutActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.version) TextView verisionTextView;

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        String versionName = "unknown";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {}

        verisionTextView.setText(versionName);
    }
}
