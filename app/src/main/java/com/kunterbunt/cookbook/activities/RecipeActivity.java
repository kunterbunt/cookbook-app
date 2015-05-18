package com.kunterbunt.cookbook.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.adapters.AbstractAdapter;
import com.kunterbunt.cookbook.data.Category;
import com.kunterbunt.cookbook.data.DatabaseHelper;
import com.kunterbunt.cookbook.data.Ingredient;
import com.kunterbunt.cookbook.data.Recipe;
import com.kunterbunt.cookbook.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecipeActivity extends DrawerBaseActivity implements ActionBar.TabListener {

    public static final String LOG_TAG = "RecipeActivity";
    /** Provides fragments for the different tabs. */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /** Hosts the section contents. */
    ViewPager mViewPager;

    public static final String ARG_CATEGORY = "ARG_CAT";

    private ActionMode mActionMode;

    /** Holds all current preparation steps. */
    private List<String> mPreparationList;
    /** Preparation step list adapter. */
    private AbstractAdapter<String> mPreparationAdapter;

    public static final int REQUEST_IMAGE_CAPTURE = 1234, SELECT_PHOTO = 1235;
    /** Current image path. */
    private String mCurrentImagePath;
    /** Current image. */
    private Bitmap currentImage;
    /** Holds all current ingrdients. */
    private List<Ingredient> mIngredientsList;
    /** Ingredient list adapter. */
    private AbstractAdapter mIngredientsListAdapter;

    private int difficulty = 0, preparationTime = 0;
    private float rating = 0;
    private String name = "no name";

    /** User choice of which categories fit the current recipe. */
    private static boolean[] chosenCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = inflater.inflate(R.layout.activity_edit_recipe, null, false);
        frameLayout.addView(activityView);

        int preselectedCategory = getIntent().getIntExtra(ARG_CATEGORY, -1);
        if (preselectedCategory == -1)
            chosenCategories = null;
        else {
            chosenCategories = new boolean[DatabaseHelper.getInstance().getAllCategories().size()];
            chosenCategories[preselectedCategory] = true;
        }

        // Set up preparation step list.
        mPreparationList = new ArrayList<String>();
        mPreparationAdapter = new AbstractAdapter<String>(getApplicationContext(), R.layout.list_preparation_item, mPreparationList) {
            @Override
            protected void populateFields(View view, int position) {
                TextView titleField = (TextView) view.findViewById(R.id.preparation_title);
                titleField.setText(getContext().getString(R.string.step) + " " + (position + 1));
                TextView bodyField = (TextView) view.findViewById(R.id.preparation_body);
                bodyField.setText((String) getItem(position));
            }
        };

        // Set up the ingredients list.
        mIngredientsList = new ArrayList<Ingredient>();
        mIngredientsListAdapter = new AbstractAdapter(getApplicationContext(), R.layout.list_ingredient_item, mIngredientsList) {
            @Override
            protected void populateFields(View view, int position) {
                Ingredient ingredient = (Ingredient) getItem(position);
                TextView amountField = (TextView) view.findViewById(R.id.ingredient_amount);
                amountField.setText("" + Tools.format(ingredient.getAmount()) + " " + context.getResources().getStringArray(R.array.units)[ingredient.getUnit()]);
                TextView nameField = (TextView) view.findViewById(R.id.ingredient_name);
                nameField.setText(ingredient.getName());
            }
        };

        // Set up the ViewPager with the sections mPreparationCardsAdapter.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    public AbstractAdapter getIngredientsListAdapter() {
        return mIngredientsListAdapter;
    }

    public AbstractAdapter getPreparationListAdapter() {
        return mPreparationAdapter;
    }

    public String getCurrentImagePath() {
        return  mCurrentImagePath;
    }

    public Bitmap getCurrentImage() {
        return currentImage;
    }

//    public CardArrayAdapter getCardListAdapterDescription() {
//        return mPreparationCardsAdapter;
//    }

    private void onAddPreparationStep() {
        // Build dialog that asks for description step.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inflated = inflater.inflate(R.layout.dialog_text_input, null);
        final EditText descriptionStep = (EditText) inflated.findViewById(R.id.dialog_preparation_step_txt);
        descriptionStep.setHint(getString(R.string.hint_step_description));
        descriptionStep.requestFocus();
        final Context context = this;
        builder.setView(inflated)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String description = descriptionStep.getText().toString();
                        // Ignore empty descriptions.
                        if (description.equals(""))
                            return;
                        mPreparationList.add(description);
                        // Hide how-to hint.
                        if (findViewById(R.id.add_steps_hint).getVisibility() == View.VISIBLE)
                            findViewById(R.id.add_steps_hint).setVisibility(View.INVISIBLE);
                        // Notify adapter.
                        mPreparationAdapter.notifyDataSetChanged();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void onAddIngredient() {
        // dialog that asks for description step
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inflated = inflater.inflate(R.layout.dialog_new_ingredient, null);
        final EditText ingredient_name = (EditText) inflated.findViewById(R.id.dialog_ingredient_name);
        final EditText ingredient_amount = (EditText) inflated.findViewById(R.id.dialog_ingredient_amount);
        final Spinner ingredient_unit = (Spinner) inflated.findViewById(R.id.dialog_ingredient_unit);
        Tools.populateSpinner(ingredient_unit, R.array.units, this);
        ingredient_name.requestFocus();
        final Context context = this;
        builder.setView(inflated)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = ingredient_name.getText().toString();
                        if (name.equals(""))
                            return;
                        int unit = ingredient_unit.getSelectedItemPosition();
                        float amount = 0;
                        try {
                            amount = Float.parseFloat(ingredient_amount.getText().toString());
                        } catch (NumberFormatException ex) {
                            Log.e(LOG_TAG, ex.getMessage());
                        }
                        Ingredient ingredient = new Ingredient(name, amount, unit);
                        mIngredientsList.add(ingredient);
                        TextView hint = (TextView) findViewById(R.id.add_ingredients_hint);
                        hint.setVisibility(View.INVISIBLE);
                        mIngredientsListAdapter.notifyDataSetChanged();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void onIngredientLongClick(int position) {
        final Ingredient ingredient = mIngredientsList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inflated = inflater.inflate(R.layout.dialog_new_ingredient, null);
        final EditText ingredient_name = (EditText) inflated.findViewById(R.id.dialog_ingredient_name);
        ingredient_name.setText(ingredient.getName());
        final EditText ingredient_amount = (EditText) inflated.findViewById(R.id.dialog_ingredient_amount);
        ingredient_amount.setText(Tools.format(ingredient.getAmount()));
        final Spinner ingredient_unit = (Spinner) inflated.findViewById(R.id.dialog_ingredient_unit);
        Tools.populateSpinner(ingredient_unit, R.array.units, this);
        ingredient_unit.setSelection(ingredient.getUnit(), true);
        ingredient_name.requestFocus();
        final Context context = this;
        builder.setView(inflated)
                .setNegativeButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIngredientsList.remove(ingredient);
                        if (mIngredientsList.size() == 0) {
                            TextView hint = (TextView) findViewById(R.id.add_ingredients_hint);
                            hint.setVisibility(View.VISIBLE);
                        }
                        mIngredientsListAdapter.notifyDataSetChanged();
                        return;
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = ingredient_name.getText().toString();
                        if (name.equals(""))
                            return;
                        int unit = ingredient_unit.getSelectedItemPosition();
                        float amount = 0;
                        try {
                            amount = Float.parseFloat(ingredient_amount.getText().toString());
                        } catch (NumberFormatException ex) {
                            Log.e("add_ingredient", ex.getMessage());
                        }
                        ingredient.setName(name);
                        ingredient.setAmount(amount);
                        ingredient.setUnit(unit);
                        mIngredientsListAdapter.notifyDataSetChanged();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void saveRecipe() {
        if (name == null || name.equals("")) {
            Toast.makeText(this, "You need to find a name for your recipe first.", Toast.LENGTH_LONG).show();
            mViewPager.setCurrentItem(0);
            EditText nameField = (EditText) findViewById(R.id.basics_name_txt);
            nameField.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(nameField, InputMethodManager.SHOW_IMPLICIT);
            return;
        }
        if (mPreparationList.size() == 0) {
            Toast.makeText(this, "You need at least one preparation step.", Toast.LENGTH_LONG).show();
            mViewPager.setCurrentItem(1);
            onAddPreparationStep();
            return;
        }
        if (mIngredientsList.size() == 0) {
            Toast.makeText(this, "You need at least one ingredient.", Toast.LENGTH_LONG).show();
            mViewPager.setCurrentItem(2);
            onAddIngredient();
            return;
        }
        String imagePath = mCurrentImagePath == null ? "none" : mCurrentImagePath;

        final Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setPreparationTime(preparationTime);
        recipe.setDifficulty(difficulty);
        recipe.setRating(rating);
        recipe.setImagePath(imagePath);
        recipe.setIngredients(mIngredientsList);
        recipe.setPreparationSteps(mPreparationList);

        // Ask for categories.
        final List<Category> existingCategories = DatabaseHelper.getInstance().getAllCategories();
        final CharSequence[] categoriesStrings = new CharSequence[existingCategories.size()];
        for (int i = 0; i < existingCategories.size(); i++)
            categoriesStrings[i] = existingCategories.get(i).getName();
        if (chosenCategories == null) {
            chosenCategories = new boolean[categoriesStrings.length];
            for (int i = 0; i < chosenCategories.length; i++)
                chosenCategories[i] = false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeActivity.this);
        builder.setTitle(getResources().getString(R.string.choose_categories))
            .setMultiChoiceItems(categoriesStrings, chosenCategories, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    chosenCategories[which] = isChecked;
                }
            })
            .setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<Category> categories = new ArrayList<Category>();
                    for (int i = 0; i < chosenCategories.length; i++)
                        if (chosenCategories[i])
                            categories.add(existingCategories.get(i));
                    if (categories.size() == 0)
                        categories.add(new Category(getResources().getString(R.string.uncategorized)));
                    recipe.setCategories(categories);
                    Log.i(LOG_TAG, "#Categories=" + recipe.getCategories().size());
                    DatabaseHelper.getInstance().putRecipe(recipe);
                    Toast.makeText(getApplicationContext(), "Saved recipe.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), BrowseActivity.class));
                    finish();
                }
            })
            .setNeutralButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onAddCategory();
                }
            })
            .show();
    }

    private void onAddCategory() {
        // Dialog that asks for category name.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inflated = inflater.inflate(R.layout.dialog_text_input, null);
        final EditText textInput = (EditText) inflated.findViewById(R.id.dialog_preparation_step_txt);
        textInput.setHint(getString(R.string.hint_add_category));
        textInput.requestFocus();
        final Context context = this;
        builder.setView(inflated)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                })
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String input = textInput.getText().toString();
                        if (input.trim().equals(""))
                            return;
                        DatabaseHelper.getInstance().putCategory(input);
                        boolean[] newChosenCategories = new boolean[chosenCategories.length + 1];
                        for (int j = 0; j < chosenCategories.length; j++)
                            newChosenCategories[j] = chosenCategories[j];
                        newChosenCategories[newChosenCategories.length - 1] = true;
                        chosenCategories = newChosenCategories;
                        saveRecipe();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void setPictureFromCamera() {
        // Create image file.
        String timestamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss").format(new Date());
        String filename = "cookbook_" + timestamp;
        File image;
        try {
            // Save to Cookbook folder in Pictures.
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Cookbook");
            if (!storageDir.exists())
                storageDir.mkdirs();
            image = File.createTempFile(filename, ".jpg", storageDir);
            mCurrentImagePath = image.getAbsolutePath();
        } catch (IOException ex) {
            Log.e(LOG_TAG, ex.toString());
            Toast.makeText(this, "Taking picture failed, sorry.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Start camera.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Log.e(LOG_TAG, "No camera app.");
            Toast.makeText(this, "Couldn't find a camera app. You kinda need one to take a picture.", Toast.LENGTH_LONG).show();
        }
    }

    private void setPictureFromGallery() {
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK);
        choosePictureIntent.setType("image/*");
        startActivityForResult(choosePictureIntent, SELECT_PHOTO);
    }

    /**
     * Called when user presses the image button.
     * @param view
     */
    public void onChooseImage(View view) {
        // Ask whether to take a picture or choose from gallery.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.choose_picture_or_take));
        builder.setPositiveButton(getResources().getString(R.string.take_picture), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setPictureFromCamera();
            }
        });
        builder.setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mCurrentImagePath = null;
                ImageButton imageButton = (ImageButton) findViewById(R.id.basics_choose_img_btn);
                imageButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_picture));
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.choose_from_gallery), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setPictureFromGallery();
            }
        });
        builder.create().show();
    }

    /**
     * Called when image is chosen.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Resize image.
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    // Image path is already set from the intent that started the camera.
                    break;
                case SELECT_PHOTO:
                    // Image path must be set to whatever image was chosen by the user.
                    mCurrentImagePath = Tools.getFilePathFromUri(data.getData(), this);
                    break;
            }
            ImageButton imageButton = (ImageButton) findViewById(R.id.basics_choose_img_btn);
            currentImage = Tools.getFittingBitmap(imageButton.getWidth(), imageButton.getHeight(), mCurrentImagePath);
            imageButton.setImageBitmap(currentImage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case  R.id.edit_recipe_done:
                saveRecipe();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddIngredientButton(View view) {
        onAddIngredient();
    }

    public void onAddPreparationStepButton(View view) {
        onAddPreparationStep();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        if (mActionMode != null)
            mActionMode.finish();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

//    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
//        @Override
//        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
//            MenuInflater inflater = actionMode.getMenuInflater();
//            inflater.inflate(R.menu.contextual_action_menu_add_recipe_descr, menu);
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
//            return false;
//        }

//        @Override
//        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//            switch (menuItem.getItemId()) {
//                case R.id.descr_cam_delete:
//                    mPreparationCardsList.remove(mSelectedCard);
//                    for (int i = 0; i < mPreparationCardsList.size() - 1; i++) {
//                        Card currentCard = mPreparationCardsList.get(i);
//                        currentCard.getCardHeader().setTitle("Step " + (i + 1));
//                    }
//                    mPreparationCardsAdapter.notifyDataSetChanged();
//                    mActionMode.finish();
//                    if (mPreparationCardsList.size() == 0)
//                        findViewById(R.id.add_steps_hint).setVisibility(View.VISIBLE);
//                    return true;
//                case R.id.descr_cam_edit:
//                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipeActivity.this);
//                    LayoutInflater inflater = getLayoutInflater();
//                    final View inflated = inflater.inflate(R.layout.dialog_text_input, null);
//                    final EditText descriptionStep = (EditText) inflated.findViewById(R.id.dialog_preparation_step_txt);
//                    descriptionStep.setText(mSelectedCard.getTitle());
//                    descriptionStep.requestFocus();
//                    builder.setView(inflated)
//                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    return;
//                                }
//                            })
//                            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    String description = descriptionStep.getText().toString();
//                                    if (description.equals(""))
//                                        return;
//                                    mSelectedCard.setTitle(description);
//                                    mPreparationCardsAdapter.notifyDataSetChanged();
//                                }
//                            });
//                    AlertDialog dialog = builder.create();
//                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//                    dialog.show();
//                    mActionMode.finish();
//                    return true;
//                case R.id.descr_cam_up:
//                    // find current position
//                    int positionUp = 0;
//                    for (int i = 0; i < mPreparationCardsList.size(); i++) {
//                        if (mPreparationCardsList.get(i) == mSelectedCard) {
//                            positionUp = i;
//                            break;
//                        }
//                    }
//                    // nothing to do for top card
//                    if (positionUp == 0)
//                        return true;
//                    // re-sort
//                    Card otherCardUp = mPreparationCardsList.get(positionUp - 1);
//                    mPreparationCardsList.set(positionUp - 1, mSelectedCard);
//                    mPreparationCardsList.set(positionUp, otherCardUp);
//                    // adjust titles
//                    mSelectedCard.getCardHeader().setTitle("Step " + (positionUp));
//                    otherCardUp.getCardHeader().setTitle("Step " + (positionUp + 1));
//                    // adjust highlighting
//                    mSelectedCardView.setActivated(false);
//                    mSelectedCardView = otherCardUp.getCardView();
//                    mSelectedCardView.setActivated(true);
//                    mPreparationCardsAdapter.notifyDataSetChanged();
//                    return true;
//                case R.id.descr_cam_down:
//                    // find current position
//                    int positionDown = 0;
//                    for (int i = 0; i < mPreparationCardsList.size(); i++) {
//                        if (mPreparationCardsList.get(i) == mSelectedCard) {
//                            positionDown = i;
//                            break;
//                        }
//                    }
//                    // nothing to do for bottom card
//                    if (positionDown == mPreparationCardsList.size() - 1)
//                        return true;
//                    // re-sort
//                    Card otherCardDown = mPreparationCardsList.get(positionDown + 1);
//                    mPreparationCardsList.set(positionDown + 1, mSelectedCard);
//                    mPreparationCardsList.set(positionDown, otherCardDown);
//                    // adjust titles
//                    mSelectedCard.getCardHeader().setTitle("Step " + (positionDown + 2));
//                    otherCardDown.getCardHeader().setTitle("Step " + (positionDown + 1));
//                    // adjust highlighting
//                    mSelectedCardView.setActivated(false);
//                    mSelectedCardView = otherCardDown.getCardView();
//                    mSelectedCardView.setActivated(true);
//                    mPreparationCardsAdapter.notifyDataSetChanged();
//                    return true;
//                default:
//                    return false;
//            }
//        }

//        @Override
//        public void onDestroyActionMode(ActionMode actionMode) {
//            mActionMode = null;
//            if (mSelectedCardView != null) {
//                mSelectedCardView.setActivated(false);
//            }
//        }
//    };

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new BasicsFragment();
                case 1: return new PreparationFragment();
                case 2: return new IngredientsFragment();
                default: Log.e(LOG_TAG, "Invalid tab position."); return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.basics).toUpperCase(l);
                case 1:
                    return getString(R.string.preparation).toUpperCase(l);
                case 2:
                    return getString(R.string.ingredients).toUpperCase(l);
            }
            return null;
        }
    }

    public static class BasicsFragment extends Fragment {

        public BasicsFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_basics, container, false);
            // show image if there is one
            String currentImagePath = ((RecipeActivity) getActivity()).getCurrentImagePath();
            Bitmap currentImage = ((RecipeActivity) getActivity()).getCurrentImage();
            if (currentImagePath != null) {
                ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.basics_choose_img_btn);
                imageButton.setImageBitmap(currentImage);
            }
            // populate preparation time spinner
            Spinner preparationTimeSpinner = (Spinner) rootView.findViewById(R.id.basics_prep_time_spinner);
            Tools.populateSpinner(preparationTimeSpinner, R.array.preparation_time_choices, getActivity());
            ((RecipeActivity) getActivity()).setPreparationTime(preparationTimeSpinner.getSelectedItemPosition());
            preparationTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ((RecipeActivity) getActivity()).setPreparationTime(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            // populate difficulty spinner
            final Spinner difficultySpinner = (Spinner) rootView.findViewById(R.id.basics_difficulty_spinner);
            Tools.populateSpinner(difficultySpinner, R.array.difficulty_choices, getActivity());
            ((RecipeActivity) getActivity()).setDifficulty(difficultySpinner.getSelectedItemPosition());
            difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ((RecipeActivity) getActivity()).setDifficulty(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            // set listener for difficulty so that it shows a description as a toast
            TextView difficultyText = (TextView) rootView.findViewById(R.id.bascis_difficulty_txt);
            difficultyText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int difficulty = difficultySpinner.getSelectedItemPosition();
                    Toast.makeText(getActivity(), getResources().getStringArray(R.array.difficulty_choices_description)[difficulty], Toast.LENGTH_LONG).show();
                }
            });
            final EditText nameField = (EditText) rootView.findViewById(R.id.basics_name_txt);
            // color rating bar
            RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.basics_rating_bar);
            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.app_color), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.app_color_accent), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.app_color_accent), PorterDuff.Mode.SRC_ATOP);
            ((RecipeActivity) getActivity()).setRating(ratingBar.getRating());
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    ((RecipeActivity) getActivity()).setRating(v);
                }
            });
            // hide name field cursor
            nameField.setCursorVisible(false);
            ((RecipeActivity) getActivity()).setName(nameField.getText().toString());
            nameField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ((RecipeActivity) getActivity()).setName(nameField.getText().toString());
                }
            });

            return rootView;
        }
    }

    public static class PreparationFragment extends Fragment {

        private ListView mPreparationList;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_preparation, container, false);
            mPreparationList = (ListView) rootView.findViewById(R.id.preparation_list);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mPreparationList.setAdapter(((RecipeActivity) getActivity()).getPreparationListAdapter());
        }
    }

    public static class IngredientsFragment extends Fragment {

        private ListView mListViewIngredients;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_recipe_ingredients, container, false);
            mListViewIngredients = (ListView) rootView.findViewById(R.id.ingredients_list);
            mListViewIngredients.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ((RecipeActivity) getActivity()).onIngredientLongClick(i);
                    return true;
                }
            });
            if (((RecipeActivity) getActivity()).getIngredientsListAdapter().getCount() > 0)
                rootView.findViewById(R.id.add_ingredients_hint).setVisibility(View.INVISIBLE);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mListViewIngredients.setAdapter(((RecipeActivity) getActivity()).getIngredientsListAdapter());
        }
    }

}
