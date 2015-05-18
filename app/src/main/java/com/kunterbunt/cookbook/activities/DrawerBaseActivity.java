package com.kunterbunt.cookbook.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.adapters.NavigationDrawerAdapter;
import com.kunterbunt.cookbook.tools.Tools;

/**
 * Created by kunterbunt on 13.11.14.
 */
public class DrawerBaseActivity extends ActionBarActivity {
    public static final String LOG_TAG = "DrawerBase";
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationDrawerAdapter mDrawerAdapter;

    private boolean mShouldChangeActivity = false, mIsDrawerOpen = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure for startup.
        Tools.preStartupConfiguration(getApplicationContext());

        setContentView(R.layout.navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        // R.id.drawer_layout should be in every activity with exactly the same id.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle((Activity) this, mDrawerLayout, R.drawable.ic_drawer, 0, 0) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mIsDrawerOpen = false;
                getSupportActionBar().setTitle(Tools.getCurrentActivityTitle(getApplicationContext()));
                invalidateOptionsMenu();
                if (mShouldChangeActivity) {
                    mShouldChangeActivity = false;
                    int currentActivity = Tools.getPref(Tools.PREFS_CURRENT_ACITIVTY, -1, getApplicationContext());
                    switch (currentActivity) {
                        case Tools.ACTIVITY_ADD_RECIPE:
                            startActivity(new Intent(getApplicationContext(), RecipeActivity.class));
                            break;
                        case Tools.ACTIVITY_BROWSE:
                            startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
                            break;
                        case Tools.ACTIVITY_FIND_RECIPE:
//                            startActivity(new Intent(getApplicationContext(), IndexActivity.class));
                            break;
                        case Tools.ACTIVITY_SETTINGS:
                            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                            break;
                        default:
                            Log.e(LOG_TAG, "Unsupported navigation drawer item selected: " + currentActivity);
                            break;
                    }
                }
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mIsDrawerOpen = true;
                getSupportActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList = (ListView) findViewById(R.id.nav_drawer_list);
        final String section_browse = getString(R.string.section_browse);
        final String section_find = getString(R.string.section_find);
        final String section_settings = getString(R.string.section_settings);
        String[] sections = new String[]{section_browse, section_find, section_settings};

        mDrawerAdapter = new NavigationDrawerAdapter(this, R.layout.list_drawer_item, sections);
        mDrawerList.setAdapter(mDrawerAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                mDrawerAdapter.notifyDataSetChanged();
                showProgressScreen();
                // Find.
                if (mDrawerAdapter.getItem(pos).equals(section_find)) {
                    mShouldChangeActivity = true;
                    Tools.setPref(Tools.PREFS_CURRENT_ACITIVTY, Tools.ACTIVITY_FIND_RECIPE, getApplicationContext());
                // Settings.
                } else if (mDrawerAdapter.getItem(pos).equals(section_settings)) {
                    mShouldChangeActivity = true;
                    Tools.setPref(Tools.PREFS_CURRENT_ACITIVTY, Tools.ACTIVITY_SETTINGS, getApplicationContext());
                // Browse.
                } else if (mDrawerAdapter.getItem(pos).equals(section_browse)) {
                    mShouldChangeActivity = true;
                    Tools.setPref(Tools.PREFS_CURRENT_ACITIVTY, Tools.ACTIVITY_BROWSE, getApplicationContext());
                } else {
                    Toast.makeText(getApplicationContext(), "Not implemented yet.", Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, "Unsupported navigation drawer item selected.");
                }
                mDrawerLayout.closeDrawers();
            }
        });

        // Open navigation drawer if this is a first start to show the user it exists.
        if (!Tools.getPref(Tools.PREFS_HAS_LEARNED_NAV_DRAWER, false, this)) {
            mDrawerLayout.openDrawer(findViewById(R.id.nav_drawer));
            Tools.setPref(Tools.PREFS_HAS_LEARNED_NAV_DRAWER, true, this);
        }

    }

    /**
     * Remove all current views and replace them with an indefinite progressbar.
     * This is called before a new activity is started, while waiting for the navigation drawer to close.
     */
    private void showProgressScreen() {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        frameLayout.removeAllViews();
        ProgressBar progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(params);
        frameLayout.addView(progressBar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsDrawerOpen) {
            getMenuInflater().inflate(R.menu.empty, menu);
            return false;
        }
        return super.onPrepareOptionsMenu(menu);
    }
}