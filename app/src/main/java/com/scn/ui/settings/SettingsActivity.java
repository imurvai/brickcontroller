package com.scn.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.scn.logger.Logger;
import com.scn.ui.BaseActivity;
import com.scn.ui.R;

/**
 * Created by imurvai on 2018-03-20.
 */

public class SettingsActivity extends BaseActivity {

    //
    // Members
    //

    private static final String TAG = SettingsActivity.class.getSimpleName();

    //
    // Activity overrides
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate...");
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    //
    // Fragment
    //

    public static class SettingsFragment extends PreferenceFragment {

        //
        // Members
        //

        private static final String TAG = SettingsFragment.class.getSimpleName();

        //
        // Fragment overrides
        //

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            Logger.i(TAG, "onCreate...");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }
}
