package com.kunterbunt.cookbook.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.adapters.RecipeListAdapter;
import com.kunterbunt.cookbook.data.Category;
import com.kunterbunt.cookbook.data.DatabaseHelper;
import com.kunterbunt.cookbook.data.Recipe;
import com.kunterbunt.cookbook.tools.Tools;

import java.util.List;
import java.util.Locale;

public class BrowseActivity extends DrawerBaseActivity {

    public static final String LOG_TAG = "Browse";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    /** Keeps track of the category the user is currently viewing. */
    private Category mCurrentCategory;
    /** Keeps track of the position of the category in the list inside the SectionsPagerAdapter which the user wants to delete. */
    private int categoryToDelete = -1;
    /** Work-around so that after category deletion it doesn't jump two categories forth. Not ideal. */
    private boolean jumpOneBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Calls DrawerBaseActivity.onCreate().
        super.onCreate(savedInstanceState);
        // The FrameLayout that will hold this activity's content.
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        // Inflate the actual content.
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = inflater.inflate(R.layout.activity_browse, null, false);
        frameLayout.addView(activityView);
        // Create the adapter that will return a fragment for each of the primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager_browse);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Set initial current category.
        mCurrentCategory = mSectionsPagerAdapter.categories.get(0);
        // Tell this listener to keep track of the category that the user is currently viewing.
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                mCurrentCategory = mSectionsPagerAdapter.categories.get(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_IDLE && categoryToDelete > -1) {
                    int currentPos = mViewPager.getCurrentItem();
                    mSectionsPagerAdapter.categories.remove(categoryToDelete);
                    mSectionsPagerAdapter.notifyDataSetChanged();
                    if (jumpOneBack) {
                        mViewPager.setCurrentItem(currentPos - 1, false);
                        jumpOneBack = false;
                    }
                    categoryToDelete = -1;
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Reset database.
        if (id == R.id.action_reset_database) {
            DatabaseHelper.getInstance().reset();
            return true;
        // Category deletion.
        } else if (id == R.id.action_delete_category) {
            String[] mustHaveCategories = getResources().getStringArray(R.array.must_have_categories);
            if (Tools.contains(mustHaveCategories, mCurrentCategory.getName()) != -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.cannot_delete_must_have_category), Toast.LENGTH_SHORT).show();
                return true;
            }
            // Are there recipes whose sole category is the one we are deleting?
            List<Recipe> recipesAttachedList = DatabaseHelper.getInstance().getRecipesForCategory(mCurrentCategory.getId());
            for (Recipe recipe : recipesAttachedList)
                if (recipe.getCategories().size() > 1)
                    recipesAttachedList.remove(recipe);
            // Construct warning message.
            String message = getString(R.string.confirm_deletion_message) + " " + mCurrentCategory.getName() + "?";
            if (recipesAttachedList.size() > 0) {
                message += "\nThe following recipes will also be deleted:";
                for (Recipe toBeDeleted : recipesAttachedList)
                    message += "\n" + toBeDeleted.getName();
            }
            // Warn about lost recipes.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.confirm_deletion_title))
                    .setMessage(message)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            categoryToDelete = mSectionsPagerAdapter.categories.indexOf(mCurrentCategory);
                            DatabaseHelper.getInstance().removeCategory(mCurrentCategory);
                            // Move to next category if there is one, to the first otherwise.
                            int currentPosition = mViewPager.getCurrentItem();
                            int numberOfCategories = mSectionsPagerAdapter.getCount();
                            int positionToScrollTo = currentPosition < numberOfCategories - 1 ? currentPosition + 1 : 0;
                            // Work-around for jumping forward two categories bug.
                            if (positionToScrollTo > currentPosition)
                                jumpOneBack = true;
                            mViewPager.setCurrentItem(positionToScrollTo);
                            mCurrentCategory = mSectionsPagerAdapter.categories.get(positionToScrollTo);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing.
                        }
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNewRecipeButton(View view) {
        Intent intent = new Intent(this, RecipeActivity.class);
        // Pre-select the current category.
        intent.putExtra(RecipeActivity.ARG_CATEGORY, mSectionsPagerAdapter.categories.indexOf(mCurrentCategory));
        startActivity(intent);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        protected List<Category> categories;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            categories = DatabaseHelper.getInstance().getAllCategories();
        }

        @Override
        public Fragment getItem(int position) {
            return CategoryFragment.newInstance(categories.get(position));
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position).getName().toUpperCase(Locale.getDefault());
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    public static class CategoryFragment extends Fragment {

        private Category mCategory;
        private RecipeListAdapter mRecipeListAdapter;
        private ListView mRecipeListView;
        private List<Recipe> mRecipeList;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CategoryFragment newInstance(Category category) {
            CategoryFragment fragment = new CategoryFragment();
            fragment.mCategory = category;
            return fragment;
        }

        public CategoryFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_browse, container, false);
            mRecipeList = mCategory.getRecipeList();
            // Hide no-recipes-text if there are recipes.
            if (mRecipeList.size() > 0)
                rootView.findViewById(R.id.category_no_recipes_text).setVisibility(View.INVISIBLE);
            // Set variable but don't set the adapter yet, because it needs the size/height of the view,
            // which is only set after the onCreate of the activity.
            mRecipeListView = (ListView) rootView.findViewById(R.id.category_recipe_list);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Set the list view adapter.
            mRecipeListAdapter = new RecipeListAdapter(getActivity(), R.layout.list_recipe_item, mRecipeList);
            mRecipeListView.setAdapter(mRecipeListAdapter);
        }

    }

}