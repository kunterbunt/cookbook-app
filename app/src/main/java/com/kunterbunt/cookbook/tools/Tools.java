package com.kunterbunt.cookbook.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.data.Category;
import com.kunterbunt.cookbook.data.DatabaseHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by kunterbunt on 04.11.14.
 */
public class Tools {

    public static final String LOG_TAG = "Tools";
    public static final String RECIPES_DIR = "recipes";
    public static final int ACTIVITY_BROWSE = 0, ACTIVITY_ADD_RECIPE = 1,
            ACTIVITY_FIND_RECIPE = 2, ACTIVITY_SETTINGS = 3;
    public static final String PREFS_CURRENT_ACITIVTY = "current_activity",
        PREFS_HAS_LEARNED_NAV_DRAWER = "nav_drawer_learned",
        PREFS_CATEGORIES = "categories";
    private static final String CATEGORY_PREFIX = "category_", CATEGORY_NUM = "category_total";

    private static boolean hasConfigured = false;

    public static void preStartupConfiguration(Context context) {
        // Make sure to only run once per app start.
        if (!hasConfigured)
            hasConfigured = true;
        else
            return;
        Log.i(LOG_TAG, "Running pre-start configuration.");
        // Initialize database.
        DatabaseHelper.init(context);
//        DatabaseHelper.getInstance().reset();
        // Initialize image loader.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(config);

        // Load default categories if the database contains none.
        List<Category> existingCategories = DatabaseHelper.getInstance().getAllCategories();
        if (existingCategories.size() == 0) {
            String[] defaultCategories = context.getResources().getStringArray(R.array.default_categories);
            for (String category : defaultCategories)
                DatabaseHelper.getInstance().putCategory(category);
            Log.i(LOG_TAG, "Loaded default categories");
        }
        // Load must-have categories if they don't exist.
        String[] mustHaveCategories = context.getResources().getStringArray(R.array.must_have_categories);
        for (String mustHaveCategory : mustHaveCategories) {
            boolean contained = false;
            for (Category existingCategory : existingCategories)
                if (existingCategory.getName().equals(mustHaveCategory)) {
                    contained = true;
                    break;
                }
            if (!contained)
                DatabaseHelper.getInstance().putCategory(mustHaveCategory);
        }
//        List<Category> categories = DatabaseHelper.getInstance().getAllCategories();
//        for (int i = 0; i < categories.size(); i++) {
//            Log.i(LOG_TAG, categories.get(i).getName() + " ID=" + categories.get(i).getId());
//            for (Recipe recipe : DatabaseHelper.getInstance().getRecipesForCategory(categories.get(i).getId()))
//                Log.i(LOG_TAG, recipe.getName());
//        }

//        // Set current activity so that navigation drawer highlighting is correct.
//        setPref(PREFS_CURRENT_ACITIVTY, ACTIVITY_BROWSE, context);
//        // If there are no categories saved, create default ones.
//        if (getSavedCategories(context).size() == 0) {
//            List<String> defaultCategories = new ArrayList<String>();
//            String[] categories = context.getResources().getStringArray(R.array.default_categories);
//            for (String category : categories)
//                defaultCategories.add(category);
//            saveCategories(defaultCategories, context);
//            Log.i(LOG_TAG, "Default categories initialized.");
//        }
    }

    public static String getFilePathFromUri(Uri contentUri, Context context) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    /**
     * Scale image found at path to fit given width and height.
     * @param targetWidth
     * @param targetHeight
     * @param path
     * @return
     */
    public static Bitmap getFittingBitmap(int targetWidth, int targetHeight, String path) {
        // Get bitmap size.
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int imageWidth = bmOptions.outWidth,
                imageHeight = bmOptions.outHeight;
        // Determine scale.
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions, targetWidth, targetHeight);
        bmOptions.inJustDecodeBounds = false;
        // Decode file.
        return BitmapFactory.decodeFile(path, bmOptions);
    }

    private static int calculateInSampleSize (BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image.
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Populate a spinner by setting an adapter pointing to the given resource id.
     * @param spinner
     * @param resource
     * @param context
     */
    public static void populateSpinner(Spinner spinner, int resource, Context context) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                resource, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Format float value as to cut off comma if it is unnecessary.
     * @param value
     * @return
     */
    public static String format(float value) {
        if (value == (long) value)
            return String.format("%d", (long) value);
        else
            return String.format("%s", value);
    }

    /**
     * Show information dialog with an "Okay" button.
     * @param context
     * @param message
     * @param title Can be null.
     */
    public static void showInfoDialog(Context context, String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        if (title != null)
            builder.setTitle(title);
        builder.setPositiveButton(context.getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    public static String getCurrentActivityTitle(Context context) {
        int mCurrentActivity = getPref(Tools.PREFS_CURRENT_ACITIVTY, -1, context);
        switch (mCurrentActivity) {
            case Tools.ACTIVITY_ADD_RECIPE: return context.getString(R.string.title_activity_edit_recipe);
            case Tools.ACTIVITY_BROWSE: return context.getString(R.string.title_activity_index);
            case Tools.ACTIVITY_FIND_RECIPE: return context.getString(R.string.title_activity_find_recipe);
            case Tools.ACTIVITY_SETTINGS: return context.getString(R.string.settings);
            default: return context.getString(R.string.app_name);
        }
    }

    /**
     * @return If external storage is available for read and write.
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * @return If external storage is available to at least read.
     */
    public static boolean isExternalStorageReadable() {
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    public static File getAppFolder(Context context) {
        return new File(context.getFilesDir().getAbsolutePath());
    }

    public static File getRecipeFolder(Context context) {
        return new File(getAppFolder(context) + File.separator + RECIPES_DIR);
    }

    public static void writeToFile(String filename, String content, Context context) {
        File dir = getRecipeFolder(context);
        dir.mkdirs();
        File file = new File(dir, filename);
        try {
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException ex) {
            Log.e(LOG_TAG, ex.getMessage());
            return;
        }
        Log.i(LOG_TAG, "Saved file to \"" + file.getAbsolutePath() + "\".");
    }

    public static void setPref(String name, int value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public static void setPref(String name, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static void setPref(String name, float value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(name, value);
        editor.apply();
    }

    public static void setPref(String name, boolean value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public static void setPref(String name, long value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(name, value);
        editor.apply();
    }

    public static void setPref(String name, Set<String> value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(name, value);
        editor.apply();
    }

    public static int getPref(String name, int defaultValue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        return prefs.getInt(name, defaultValue);
    }

    public static boolean getPref(String name, boolean defaultValue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        return prefs.getBoolean(name, defaultValue);
    }

    public static String getPref(String name, String defaultValue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        return prefs.getString(name, defaultValue);
    }

    public static float getPref(String name, float defaultValue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        return prefs.getFloat(name, defaultValue);
    }

    public static long getPref(String name, long defaultValue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        return prefs.getLong(name, defaultValue);
    }

    public static Set<String> getPref(String name, Set<String> defaultValue, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
        return prefs.getStringSet(name, defaultValue);
    }

    /**
     * @param array
     * @param object
     * @return Position in array where object is found or -1.
     */
    public static int contains(Object[] array, Object object) {
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(object))
                return i;
        return -1;
    }

}
