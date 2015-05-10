package com.kunterbunt.cookbook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.kunterbunt.cookbook.R;

public class SettingsActivity extends DrawerBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment {

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

        }
    }
}
