package com.kunterbunt.cookbook.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.adapters.AbstractAdapter;
import com.kunterbunt.cookbook.data.Category;
import com.kunterbunt.cookbook.data.DatabaseHelper;
import com.kunterbunt.cookbook.data.Recipe;
import com.kunterbunt.cookbook.tools.Tools;
import com.nostra13.universalimageloader.core.ImageLoader;

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
                            // Restart activity.
                            finish();
                            startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
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
        private AbstractAdapter mRecipeListAdapter;
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
            mRecipeListAdapter = new AbstractAdapter<Recipe>(getActivity(), R.layout.list_recipe_item, mRecipeList) {

                @Override
                protected void populateFields(View view, final int position) {
                    // Get recipe from list.
                    Recipe recipe = (Recipe) getItem(position);
                    // Populate fields.
                    ImageView imageView = (ImageView) view.findViewById(R.id.recipe_image);
                    if (!recipe.getImagePath().equals(Recipe.NO_IMAGE)) {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ImageLoader.getInstance().displayImage("file://" + recipe.getImagePath(), imageView);
                    } else {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_cutlery));
                    }

                    ((TextView) view.findViewById(R.id.recipe_name)).setText(recipe.getName());
                    RatingBar ratingBar = (RatingBar) view.findViewById(R.id.recipe_rating_bar);
                    ratingBar.setRating(recipe.getRating());
                    // Color rating bar.
                    LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
                    stars.getDrawable(2).setColorFilter(getContext().getResources().getColor(R.color.app_color), PorterDuff.Mode.SRC_ATOP);
                    stars.getDrawable(1).setColorFilter(getContext().getResources().getColor(R.color.app_color_accent), PorterDuff.Mode.SRC_ATOP);
                    stars.getDrawable(0).setColorFilter(getContext().getResources().getColor(R.color.app_color_accent), PorterDuff.Mode.SRC_ATOP);

                    // Enable drag & drop re-ordering.
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            // No point in re-ordering 1 recipe (also buggy as it will disappear when dropped).
                            if (mRecipeList.size() < 2) {
                                Toast.makeText(getContext(), "There is only one recipe!", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            ClipData data = ClipData.newPlainText("", "");
                            // Drag shadow that is displayed during drag.
                            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                            view.startDrag(data, shadowBuilder, view, 0);
                            // Hide view being dragged.
                            view.setVisibility(View.INVISIBLE);
                            // Remember position where it was.
                            mDragFrom = position;
                            mViewBeingDragged = view;
                            // Reset target position.
                            mDragTo = -1;
                            // Work-around for many drag events being fired.
                            mDragStarted = true;
                            return true;
                        }
                    });

                    view.setOnDragListener(new View.OnDragListener() {
                        @Override
                        public boolean onDrag(View view, DragEvent dragEvent) {
                            // Work-around for many drag events being fired.
                            if (mDragStarted) {
                                switch (dragEvent.getAction()) {
                                    case DragEvent.ACTION_DRAG_ENTERED:
                                        // Remember target position.
                                        mDragTo = position;
                                        // Highlight target.
                                        view.setBackgroundColor(getActivity().getResources().getColor(R.color.app_color));
                                        view.invalidate();
                                        return true;
                                    case DragEvent.ACTION_DRAG_EXITED:
                                        // Reset target position.
                                        mDragTo = -1;
                                        // Reset highlighting.
                                        view.setBackgroundColor(Color.TRANSPARENT);
                                        break;
                                    case DragEvent.ACTION_DRAG_ENDED:
                                        // Reset all highlighting.
                                        view.setBackgroundColor(Color.TRANSPARENT);
                                        // Discard future drag events that were not initiated by the user.
                                        mDragStarted = false;
                                        // Valid target?
                                        if (mDragTo > -1) {
                                            // Re-order.
                                            Recipe recipeFrom = mRecipeList.get(mDragFrom);
                                            mRecipeList.remove(mDragFrom);
                                            mRecipeList.add(mDragTo, recipeFrom);
                                            // Make re-ordering persistent.
                                            long[] recipeIds = new long[mRecipeList.size()];
                                            int[] positions = new int[mRecipeList.size()];
                                            for (int i = 0; i < mRecipeList.size(); i++) {
                                                recipeIds[i] = mRecipeList.get(i).getId();
                                                mRecipeList.get(i).setListPosition(i);
                                                positions[i] = i;
                                            }
                                            DatabaseHelper.getInstance().orderRecipes(recipeIds, mCategory.getId(), positions);
                                        }
                                        // Re-draw list.
                                        mRecipeListAdapter.notifyDataSetChanged();
                                        break;
                                    case DragEvent.ACTION_DRAG_LOCATION:
                                        // y-coord of touch event inside the touched view (so between 0 and view.getBottom())
                                        float y = dragEvent.getY();
                                        // y-coord of top border of the view inside the current viewport.
                                        int viewTop = view.getTop();
                                        // y-coord that is still visible of the list view.
                                        int listBottom = mRecipeListView.getBottom(),
                                                listTop = mRecipeListView.getTop();
//                                        Log.i(LOG_TAG, "Y=" + y + " bottom=" + view.getBottom() + " top=" + view.getTop() + " viewY=" + view.getY()
//                                        + " listbtm=" + mRecipeListView.getBottom());

                                        // actual touch position on screen > bottom with 200px margin
                                        if (y + viewTop > listBottom - 200) {
                                            mRecipeListView.smoothScrollBy(100, 25);
                                            for (int i = 0; i < mRecipeListView.getChildCount(); i++) {
                                                mRecipeListView.getChildAt(i).setVisibility(View.GONE);
                                                mRecipeListView.getChildAt(i).setVisibility(View.VISIBLE);
                                            }
                                            mViewBeingDragged.setVisibility(View.INVISIBLE);
                                        } else if (y + viewTop < listTop + 200) {
                                            mRecipeListView.smoothScrollBy(-100, 25);
                                            for (int i = 0; i < mRecipeListView.getChildCount(); i++) {
                                                if (mRecipeListView.getChildAt(i) != mViewBeingDragged) {
                                                    mRecipeListView.getChildAt(i).setVisibility(View.GONE);
                                                    mRecipeListView.getChildAt(i).setVisibility(View.VISIBLE);
                                                }
                                            }
                                            mViewBeingDragged.setVisibility(View.INVISIBLE);
                                        }
                                        return true;
                                    default:
                                        break;
                                }
                                return true;
                            }
                            return false;
                        }
                    });
                }
            };
            mRecipeListView.setAdapter(mRecipeListAdapter);
        }



        /** Keep track of from where to where the user drags recipe cards. */
        private int mDragFrom, mDragTo;
        /** To discard drag events not started by the user. */
        private boolean mDragStarted = false;
        private View mViewBeingDragged = null;

    }

}
