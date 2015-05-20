package com.kunterbunt.cookbook.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kunterbunt on 17.02.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "cookbook-recipes.db";
    public static final String LOG_TAG = "DatabaseHelper";
    public static DatabaseHelper instance = null;
    private SQLiteDatabase writableDatabase;
    private SQLiteDatabase readableDatabase;

    /**
     * @return All currently saved categories.
     */
    public List<Category> getAllCategories() {
        // Query database.
        Cursor cursor = readableDatabase.query(
                Contract.CategoryEntry.TABLE_NAME,
                new String[]{Contract.CategoryEntry.COLUMN_NAME_TITLE, Contract.CategoryEntry._ID},
                null, null, null, null, null);
        List<Category> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            categories.add(new Category(cursor.getString(cursor.getColumnIndexOrThrow(Contract.CategoryEntry.COLUMN_NAME_TITLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(Contract.CategoryEntry._ID))));
        }
        cursor.close();
        return categories;
    }

    /**
     * @return All recipes.
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor cursor = readableDatabase.query(
                Contract.RecipeEntry.TABLE_NAME,
                new String[]{Contract.RecipeEntry._ID},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            long _id = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.RecipeEntry._ID));
            recipes.add(getRecipe(_id));
        }
        cursor.close();
        return recipes;
    }

    /**
     * @return 1st=IDs 2nd=Categories
     */
    public List<Ingredient> getAllIngredients() {
        // Query database.
        Cursor cursor = readableDatabase.query(
                Contract.IngredientEntry.TABLE_NAME,
                new String[]{Contract.IngredientEntry.COLUMN_NAME_TITLE, Contract.IngredientEntry._ID},
                null, null, null, null, null);
        List<Ingredient> ingredients = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.IngredientEntry.COLUMN_NAME_TITLE));
            long _id = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.IngredientEntry._ID));
            Ingredient ingredient = new Ingredient(name, -1, -1);
            ingredient.setId(_id);
            ingredients.add(ingredient);
        }
        cursor.close();
        return ingredients;
    }

    /**
     * @param categoryId
     * @return All recipes attached to the provided category.
     */
    public List<Recipe> getRecipesForCategory(long categoryId) {
        List<Recipe> recipes = new ArrayList<>();
        Cursor cursor = readableDatabase.query(
                Contract.Recipe2CategoryEntry.TABLE_NAME,
                new String[]{Contract.Recipe2CategoryEntry.COLUMN_NAME_RECIPE, Contract.Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION},
                Contract.Recipe2CategoryEntry.COLUMN_NAME_CATEGORY + "=?", new String[]{String.valueOf(categoryId)}, null, null, Contract.Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION);
        while (cursor.moveToNext()) {
            long _id = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Recipe2CategoryEntry.COLUMN_NAME_RECIPE));
            recipes.add(getRecipe(_id));
        }
        return recipes;
    }

    /**
     * @param _id Database _id of the recipe.
     * @return Complete recipe.
     */
    public Recipe getRecipe(long _id) {
        Recipe recipe = new Recipe();
        // Basic information.
        Cursor cursor = readableDatabase.query(
                Contract.RecipeEntry.TABLE_NAME,
                new String[]{Contract.RecipeEntry.COLUMN_NAME_DATE_ADDED, Contract.RecipeEntry.COLUMN_NAME_PREPARATION_DIFFICULTY,
                Contract.RecipeEntry.COLUMN_NAME_PREPARATION_TIME, Contract.RecipeEntry.COLUMN_NAME_RATING, Contract.RecipeEntry.COLUMN_NAME_TITLE},
                Contract.RecipeEntry._ID + "=?", new String[]{String.valueOf(_id)}, null, null, null);
        cursor.moveToFirst();
        recipe.setDateCreated(cursor.getLong(cursor.getColumnIndexOrThrow(Contract.RecipeEntry.COLUMN_NAME_DATE_ADDED)));
        recipe.setDifficulty(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RecipeEntry.COLUMN_NAME_PREPARATION_DIFFICULTY)));
        recipe.setPreparationTime(cursor.getInt(cursor.getColumnIndexOrThrow(Contract.RecipeEntry.COLUMN_NAME_PREPARATION_TIME)));
        recipe.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.RecipeEntry.COLUMN_NAME_RATING)));
        recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(Contract.RecipeEntry.COLUMN_NAME_TITLE)));
        cursor.close();
        // Categories.
        cursor = readableDatabase.query(
            Contract.Recipe2CategoryEntry.TABLE_NAME,
            new String[]{Contract.Recipe2CategoryEntry.COLUMN_NAME_CATEGORY, Contract.Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION},
            Contract.Recipe2CategoryEntry.COLUMN_NAME_RECIPE + "=?", new String[]{String.valueOf(_id)}, null, null, null);
        List<Category> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            long categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.Recipe2CategoryEntry.COLUMN_NAME_CATEGORY));
            Cursor categoryCursor = readableDatabase.query(
                    Contract.CategoryEntry.TABLE_NAME,
                    new String[]{Contract.CategoryEntry.COLUMN_NAME_TITLE},
                    Contract.CategoryEntry._ID + "=?", new String[]{String.valueOf(categoryId)}, null, null, null);
            categoryCursor.moveToFirst();
            String name = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow(Contract.CategoryEntry.COLUMN_NAME_TITLE));
            categoryCursor.close();
            categories.add(new Category(name, categoryId));
            recipe.setListPosition(cursor.getColumnIndexOrThrow(Contract.Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION));
        }
        cursor.close();
        recipe.setCategories(categories);
        // Image.
        cursor = readableDatabase.query(
                Contract.Recipe2ImageEntry.TABLE_NAME,
                new String[]{Contract.Recipe2ImageEntry.COLUMN_NAME_IMAGE},
                Contract.Recipe2ImageEntry.COLUMN_NAME_RECIPE + "=?", new String[]{String.valueOf(_id)}, null, null, null);
        cursor.moveToFirst();
        long imageId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.Recipe2ImageEntry.COLUMN_NAME_IMAGE));
        cursor.close();
        cursor = readableDatabase.query(
                Contract.ImageEntry.TABLE_NAME,
                new String[]{Contract.ImageEntry.COLUMN_NAME_PATH},
                Contract.ImageEntry._ID + "=?", new String[]{String.valueOf(imageId)}, null, null, null);
        cursor.moveToFirst();
        recipe.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(Contract.ImageEntry.COLUMN_NAME_PATH)));
        cursor.close();
        // Ingredients.
        cursor = readableDatabase.query(
                Contract.Recipe2IngredientEntry.TABLE_NAME,
                new String[]{Contract.Recipe2IngredientEntry.COLUMN_NAME_INGREDIENT, Contract.Recipe2IngredientEntry.COLUMN_NAME_AMOUNT, Contract.Recipe2IngredientEntry.COLUMN_NAME_UNIT},
                Contract.Recipe2IngredientEntry.COLUMN_NAME_RECIPE + "=?", new String[]{String.valueOf(_id)}, null, null, null);
        List<Ingredient> ingredients = new ArrayList<>();
        while (cursor.moveToNext()) {
            long ingredientId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.Recipe2IngredientEntry.COLUMN_NAME_INGREDIENT));
            float amount = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.Recipe2IngredientEntry.COLUMN_NAME_AMOUNT));
            int unit = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.Recipe2IngredientEntry.COLUMN_NAME_UNIT));
            Cursor ingredientCursor = readableDatabase.query(
                    Contract.IngredientEntry.TABLE_NAME,
                    new String[]{Contract.IngredientEntry.COLUMN_NAME_TITLE},
                    Contract.IngredientEntry._ID + "=?", new String[]{String.valueOf(ingredientId)}, null, null, null);
            ingredientCursor.moveToFirst();
            String name = ingredientCursor.getString(ingredientCursor.getColumnIndexOrThrow(Contract.IngredientEntry.COLUMN_NAME_TITLE));
            ingredientCursor.close();
            ingredients.add(new Ingredient(name, amount, unit));
        }
        cursor.close();
        recipe.setIngredients(ingredients);
        // Preparation steps.
        cursor = readableDatabase.query(
                Contract.Recipe2PreparationStepEntry.TABLE_NAME,
                new String[]{Contract.Recipe2PreparationStepEntry.COLUMN_NAME_PREPARATION_STEP},
                Contract.Recipe2PreparationStepEntry.COLUMN_NAME_RECIPE + "=?", new String[]{String.valueOf(_id)}, null, null, null);
        List<String> preparationSteps = new ArrayList<>();
        while (cursor.moveToNext()) {
            long stepId = cursor.getInt(cursor.getColumnIndexOrThrow(Contract.Recipe2PreparationStepEntry.COLUMN_NAME_PREPARATION_STEP));
            Cursor stepCursor = readableDatabase.query(
                    Contract.PreparationStepEntry.TABLE_NAME,
                    new String[]{Contract.PreparationStepEntry.COLUMN_NAME_DESCRIPTION},
                    Contract.PreparationStepEntry._ID + "=?", new String[]{String.valueOf(stepId)}, null, null, null);
            stepCursor.moveToFirst();
            String step = stepCursor.getString(stepCursor.getColumnIndexOrThrow(Contract.PreparationStepEntry.COLUMN_NAME_DESCRIPTION));
            stepCursor.close();
            preparationSteps.add(step);
        }
        cursor.close();
        recipe.setPreparationSteps(preparationSteps);
        recipe.setId(_id);
        return recipe;
    }

    /**
     * Looks for a category with the given name. If none are found a new category is added.
     * @param category
     * @return _id
     */
    public long putCategory(String category) {
        // Does category exist already?
        List<Category> existingCategories = getAllCategories();
        for (int i = 0; i < existingCategories.size(); i++) {
            if (existingCategories.get(i).getName().equals(category)) {
                return existingCategories.get(i).getId();
            }
        }
        // Put into database.
        ContentValues values = new ContentValues();
        values.put(Contract.CategoryEntry.COLUMN_NAME_TITLE, category);
        long categoryId = writableDatabase.insert(Contract.CategoryEntry.TABLE_NAME, null, values);
        if (categoryId == -1)
            Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.CategoryEntry.TABLE_NAME);
        Log.i(LOG_TAG, "Added category: " + category);
        return categoryId;
    }

    /**
     * Removes this recipe from the database.
     * @param recipe
     */
    public void removeRecipe(Recipe recipe) {
        // Try to set own ID.
        if (recipe.getId() == Recipe.NOT_SAVED) {
            if (recipe.setId() == Recipe.NOT_SAVED) {
                Log.e(LOG_TAG, "Recipe \"" + recipe.getName() + "\" is not saved in the database, no point in deleting it.");
                return;
            }
        }
        String[] _id = new String[]{String.valueOf(recipe.getId())};
        // Recipe table.
        writableDatabase.delete(Contract.RecipeEntry.TABLE_NAME, Contract.RecipeEntry._ID + "=?", _id);
        // Linker tables.
        writableDatabase.delete(Contract.Recipe2CategoryEntry.TABLE_NAME, Contract.Recipe2CategoryEntry.COLUMN_NAME_RECIPE + "=?", _id);
        writableDatabase.delete(Contract.Recipe2PreparationStepEntry.TABLE_NAME, Contract.Recipe2PreparationStepEntry.COLUMN_NAME_RECIPE + "=?", _id);
        writableDatabase.delete(Contract.Recipe2IngredientEntry.TABLE_NAME, Contract.Recipe2IngredientEntry.COLUMN_NAME_RECIPE + "=?", _id);
        writableDatabase.delete(Contract.Recipe2ImageEntry.TABLE_NAME, Contract.Recipe2ImageEntry.COLUMN_NAME_RECIPE + "=?", _id);
        Log.i(LOG_TAG, "Recipe \"" + recipe.getName() + "\" deleted from database.");
    }

    /**
     * Removes this category from the database and all recipes whose sole category is this one as well.
      * @param category
     */
    public void removeCategory(Category category) {
        // Remove all recipes whose sole category is the one we are deleting.
        List<Recipe> recipes = getRecipesForCategory(category.getId());
        for (Recipe recipe : recipes)
            if (recipe.getCategories().size() == 1)
                removeRecipe(recipe);
        // Delete the category.
        writableDatabase.delete(Contract.CategoryEntry.TABLE_NAME, Contract.CategoryEntry._ID + "=?", new String[]{String.valueOf(category.getId())});
        // Delete all linker-table items that contain this category.
        writableDatabase.delete(Contract.Recipe2CategoryEntry.TABLE_NAME, Contract.Recipe2CategoryEntry.COLUMN_NAME_CATEGORY + "=?", new String[]{String.valueOf(category.getId())});
        Log.i(LOG_TAG, "Category \"" + category.getName() + "\" deleted from database.");
    }

    /**
     * Looks for an ingredient matching the name of the one provided. If none are found a new item is inserted.
     * @param ingredient
     * @return _id
     */
    public long putIngredient(Ingredient ingredient) {
        List<Ingredient> ingredients = getAllIngredients();
        ContentValues values = new ContentValues();
        values.put(Contract.IngredientEntry.COLUMN_NAME_TITLE, ingredient.getName());
        long ingredientId = writableDatabase.insert(Contract.IngredientEntry.TABLE_NAME, null, values);
        if (ingredientId == -1)
            Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.IngredientEntry.TABLE_NAME);
        Log.i(LOG_TAG, "Added ingredient: " + ingredient.getName());
        return ingredientId;
    }

    /**
     * Inserts the ingredient step into the database.
     * @param preparationStep
     * @return _id
     */
    public long putPreparationStep(String preparationStep) {
        ContentValues values = new ContentValues();
        values.put(Contract.PreparationStepEntry.COLUMN_NAME_DESCRIPTION, preparationStep);
        long stepId = writableDatabase.insert(Contract.PreparationStepEntry.TABLE_NAME, null, values);
        if (stepId == -1)
            Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.PreparationStepEntry.TABLE_NAME);
        return stepId;
    }

    /**
     * Handles insertion of the given recipe.
     * @param recipe
     * @return _id
     */
    public long putRecipe(Recipe recipe) {
        // Save basic recipe values.
        ContentValues values = new ContentValues();
        values.put(Contract.RecipeEntry.COLUMN_NAME_TITLE, recipe.getName());
        values.put(Contract.RecipeEntry.COLUMN_NAME_DATE_ADDED, recipe.getDateCreated());
        values.put(Contract.RecipeEntry.COLUMN_NAME_PREPARATION_DIFFICULTY, recipe.getDifficulty());
        values.put(Contract.RecipeEntry.COLUMN_NAME_PREPARATION_TIME, recipe.getPreparationTime());
        values.put(Contract.RecipeEntry.COLUMN_NAME_RATING, recipe.getRating());
        long _id = writableDatabase.insert(Contract.RecipeEntry.TABLE_NAME, null, values);
        if (_id == -1)
            Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.RecipeEntry.TABLE_NAME);
        values.clear();

        // Categories.
        List<Category> existingCategories = getAllCategories();
        List<Category> recipeCategories = recipe.getCategories();
        for (Category category : recipeCategories) {
            long categoryId = existingCategories.indexOf(category);
            // Insert if necessary.
            if (categoryId == -1)
                categoryId = putCategory(category.getName());
            else
                categoryId = category.getId();
            // Insert into linker table.
            values.put(Contract.Recipe2CategoryEntry.COLUMN_NAME_CATEGORY, categoryId);
            values.put(Contract.Recipe2CategoryEntry.COLUMN_NAME_RECIPE, _id);
            values.put(Contract.Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION, recipe.getListPosition());
            if (writableDatabase.insert(Contract.Recipe2CategoryEntry.TABLE_NAME, null, values) == -1)
                Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.Recipe2CategoryEntry.TABLE_NAME);
            values.clear();
        }

        // Insert image path.
        values.put(Contract.ImageEntry.COLUMN_NAME_PATH, recipe.getImagePath());
        long imageId = writableDatabase.insert(Contract.ImageEntry.TABLE_NAME, null, values);
        if (imageId == -1)
            Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.ImageEntry.TABLE_NAME);
        values.clear();
        // Insert image path linker values.
        values.put(Contract.Recipe2ImageEntry.COLUMN_NAME_IMAGE, imageId);
        values.put(Contract.Recipe2ImageEntry.COLUMN_NAME_RECIPE, _id);
        if (writableDatabase.insert(Contract.Recipe2ImageEntry.TABLE_NAME, null, values) == -1)
            Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.Recipe2ImageEntry.TABLE_NAME);
        values.clear();

        // Insert missing ingredients.
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.getId() == Ingredient.NO_ID)
                ingredient.retrieveId();
            values.put(Contract.Recipe2IngredientEntry.COLUMN_NAME_RECIPE, _id);
            values.put(Contract.Recipe2IngredientEntry.COLUMN_NAME_INGREDIENT, ingredient.getId());
            values.put(Contract.Recipe2IngredientEntry.COLUMN_NAME_AMOUNT, ingredient.getAmount());
            values.put(Contract.Recipe2IngredientEntry.COLUMN_NAME_UNIT, ingredient.getUnit());
            if (writableDatabase.insert(Contract.Recipe2IngredientEntry.TABLE_NAME, null, values) == -1)
                Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.Recipe2IngredientEntry.TABLE_NAME);
            values.clear();
        }

        // Insert all preparation steps.
        for (String step : recipe.getPreparationSteps()) {
            long stepId = putPreparationStep(step);
            values.put(Contract.Recipe2PreparationStepEntry.COLUMN_NAME_PREPARATION_STEP, stepId);
            values.put(Contract.Recipe2PreparationStepEntry.COLUMN_NAME_RECIPE, _id);
            if (writableDatabase.insert(Contract.Recipe2PreparationStepEntry.TABLE_NAME, null, values) == -1)
                Log.e(LOG_TAG, "ID is -1 after inserting into " + Contract.Recipe2PreparationStepEntry.TABLE_NAME);
            values.clear();
        }

        Log.i(LOG_TAG, "Added recipe: " + recipe.getName() + " (" + recipe.getPreparationSteps().size() + " preparation steps)");
        return _id;
    }

    /**
     * Set list positions of the given recipes in the linker table to the given category so that looking them up orders them in the future.
     * @param recipeIds Recipe IDs.
     * @param categoryId Category ID.
     * @param positions Recipe ordering.
     */
    public void orderRecipes(long[] recipeIds, long categoryId, int[] positions) {
        assert recipeIds.length == positions.length;
        for (int i = 0; i < recipeIds.length; i++) {
            ContentValues values = new ContentValues();
            values.put(Contract.Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION, positions[i]);
            writableDatabase.update(Contract.Recipe2CategoryEntry.TABLE_NAME,
                    values,
                    Contract.Recipe2CategoryEntry.COLUMN_NAME_CATEGORY + "=?" + " AND " + Contract.Recipe2CategoryEntry.COLUMN_NAME_RECIPE + "=?",
                    new String[]{"" + categoryId, "" + recipeIds[i]});
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        writableDatabase = super.getWritableDatabase();
        readableDatabase = super.getReadableDatabase();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
            Log.i(LOG_TAG, "Database instantiated.");
        } else
            Log.e(LOG_TAG, "Attempted to re-instantiate database.");
    }

    public static DatabaseHelper getInstance() {
        return instance;
    }

    public static void stop() {
        instance.writableDatabase.close();
        instance.readableDatabase.close();
        Log.i(LOG_TAG, "Database connection closed.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contract.SQL_CREATE_TABLE_RECIPES);

        db.execSQL(Contract.SQL_CREATE_TABLE_INGREDIENTS);
        db.execSQL(Contract.SQL_CREATE_TABLE_RECIPE2INGREDIENT);

        db.execSQL(Contract.SQL_CREATE_TABLE_PREPARATION_STEPS);
        db.execSQL(Contract.SQL_CREATE_TABLE_RECIPE2PREPARATION_STEP);

        db.execSQL(Contract.SQL_CREATE_TABLE_CATEGORIES);
        db.execSQL(Contract.SQL_CREATE_TABLE_RECIPE2CATEGORY);

        db.execSQL(Contract.SQL_CREATE_TABLE_IMAGES);
        db.execSQL(Contract.SQL_CREATE_TABLE_RECIPE2IMAGE);
        Log.i(LOG_TAG, "Database created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Contract.SQL_DROP_TABLE_RECIPES);

        db.execSQL(Contract.SQL_DROP_TABLE_INGREDIENTS);
        db.execSQL(Contract.SQL_DROP_TABLE_RECIPE2INGREDIENT);

        db.execSQL(Contract.SQL_DROP_TABLE_PREPARATION_STEPS);
        db.execSQL(Contract.SQL_DROP_TABLE_RECIPE2PREPARATION_STEP);

        db.execSQL(Contract.SQL_DROP_TABLE_CATEGORIES);
        db.execSQL(Contract.SQL_DROP_TABLE_RECIPE2CATEGORY);

        db.execSQL(Contract.SQL_DROP_TABLE_IMAGES);
        db.execSQL(Contract.SQL_DROP_TABLE_RECIPE2IMAGE);
        onCreate(db);
        Log.i(LOG_TAG, "Database upgraded.");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        Log.i(LOG_TAG, "Database downgraded.");
    }

    public void reset() {
        onUpgrade(super.getWritableDatabase(), DATABASE_VERSION, DATABASE_VERSION);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        Log.e(LOG_TAG, "Attempted to retrieve database object. All database access must be done through DatabaseHelper class.");
        return null;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getWritableDatabase();
    }





    private static class Contract {

        private static final String TYPE_TEXT = " TEXT ";
        private static final String TYPE_INTEGER = " INTEGER ";
        private static final String TYPE_FLOAT = " FLOAT ";
        private static final String COMMA_SEP = ",";
        private static final String DEFAULT = " DEFAULT ";
        private static final String NOT_NULL = " NOT NULL ";

        private Contract() {}

        protected static abstract class RecipeEntry implements BaseColumns {
            public static final String TABLE_NAME = "recipes";
            public static final String COLUMN_NAME_TITLE = "title";
            public static final String COLUMN_NAME_RATING = "rating";
            public static final String COLUMN_NAME_PREPARATION_TIME = "preparation_time";
            public static final String COLUMN_NAME_PREPARATION_DIFFICULTY = "preparation_difficulty";
            public static final String COLUMN_NAME_DATE_ADDED = "date_added";
        }
        protected static final String SQL_CREATE_TABLE_RECIPES =
                "CREATE TABLE " + RecipeEntry.TABLE_NAME + " (" +
                        RecipeEntry._ID + " INTEGER PRIMARY KEY, " +
                        RecipeEntry.COLUMN_NAME_TITLE + TYPE_TEXT + NOT_NULL + COMMA_SEP +
                        RecipeEntry.COLUMN_NAME_DATE_ADDED + TYPE_INTEGER + COMMA_SEP +
                        RecipeEntry.COLUMN_NAME_PREPARATION_DIFFICULTY + TYPE_FLOAT + COMMA_SEP +
                        RecipeEntry.COLUMN_NAME_RATING + TYPE_FLOAT + COMMA_SEP +
                        RecipeEntry.COLUMN_NAME_PREPARATION_TIME + TYPE_INTEGER +
                        " )";
        protected static final String SQL_DROP_TABLE_RECIPES =
                "DROP TABLE IF EXISTS " + RecipeEntry.TABLE_NAME;

        // ---

        protected static abstract class IngredientEntry implements BaseColumns {
            public static final String TABLE_NAME = "ingredients";
            public static final String COLUMN_NAME_TITLE = "title";
            public static final String COLUMN_NAME_CALORIES = "calories";
        }
        protected static final String SQL_CREATE_TABLE_INGREDIENTS =
                "CREATE TABLE " + IngredientEntry.TABLE_NAME + " (" +
                        IngredientEntry._ID + " INTEGER PRIMARY KEY, " +
                        IngredientEntry.COLUMN_NAME_TITLE + TYPE_TEXT + NOT_NULL + COMMA_SEP +
                        IngredientEntry.COLUMN_NAME_CALORIES + TYPE_INTEGER + DEFAULT + "-1" +
                        " )";
        protected static final String SQL_DROP_TABLE_INGREDIENTS =
                "DROP TABLE IF EXISTS " + IngredientEntry.TABLE_NAME;

        // ---

        protected static abstract class Recipe2IngredientEntry implements BaseColumns {
            public static final String TABLE_NAME = "recipe2ingredient";
            public static final String COLUMN_NAME_RECIPE = "recipe";
            public static final String COLUMN_NAME_INGREDIENT = "ingredient";
            public static final String COLUMN_NAME_AMOUNT = "amount";
            public static final String COLUMN_NAME_UNIT = "unit";
        }
        protected static final String SQL_CREATE_TABLE_RECIPE2INGREDIENT =
                "CREATE TABLE " + Recipe2IngredientEntry.TABLE_NAME + " (" +
                        Recipe2IngredientEntry._ID + " INTEGER PRIMARY KEY, " +
                        Recipe2IngredientEntry.COLUMN_NAME_RECIPE + TYPE_INTEGER + COMMA_SEP +
                        Recipe2IngredientEntry.COLUMN_NAME_INGREDIENT + TYPE_INTEGER + COMMA_SEP +
                        Recipe2IngredientEntry.COLUMN_NAME_AMOUNT + TYPE_FLOAT + COMMA_SEP +
                        Recipe2IngredientEntry.COLUMN_NAME_UNIT + TYPE_INTEGER + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2IngredientEntry.COLUMN_NAME_RECIPE + ") REFERENCES " + RecipeEntry.TABLE_NAME + "(" + RecipeEntry._ID + ")" + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2IngredientEntry.COLUMN_NAME_INGREDIENT + ") REFERENCES " + IngredientEntry.TABLE_NAME + "(" + IngredientEntry._ID + ")" +
                        " )";
        protected static final String SQL_DROP_TABLE_RECIPE2INGREDIENT =
                "DROP TABLE IF EXISTS " + Recipe2IngredientEntry.TABLE_NAME;

        // ---

        protected static abstract class PreparationStepEntry implements BaseColumns {
            public static final String TABLE_NAME = "preparation_steps";
            public static final String COLUMN_NAME_DESCRIPTION = "description";
        }
        protected static final String SQL_CREATE_TABLE_PREPARATION_STEPS =
                "CREATE TABLE " + PreparationStepEntry.TABLE_NAME + " (" +
                        PreparationStepEntry._ID + " INTEGER PRIMARY KEY, " +
                        PreparationStepEntry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + NOT_NULL +
                        " )";
        protected static final String SQL_DROP_TABLE_PREPARATION_STEPS =
                "DROP TABLE IF EXISTS " + PreparationStepEntry.TABLE_NAME;

        // ---

        protected static abstract class Recipe2PreparationStepEntry implements BaseColumns {
            public static final String TABLE_NAME = "recipe2preparation_step";
            public static final String COLUMN_NAME_RECIPE = "recipe";
            public static final String COLUMN_NAME_PREPARATION_STEP = "preparation_step";
        }
        protected static final String SQL_CREATE_TABLE_RECIPE2PREPARATION_STEP =
                "CREATE TABLE " + Recipe2PreparationStepEntry.TABLE_NAME + " (" +
                        Recipe2PreparationStepEntry._ID + " INTEGER PRIMARY KEY," +
                        Recipe2PreparationStepEntry.COLUMN_NAME_RECIPE + TYPE_INTEGER + COMMA_SEP +
                        Recipe2PreparationStepEntry.COLUMN_NAME_PREPARATION_STEP + TYPE_INTEGER + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2PreparationStepEntry.COLUMN_NAME_RECIPE + ") REFERENCES " + RecipeEntry.TABLE_NAME + "(" + RecipeEntry._ID + ")" + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2PreparationStepEntry.COLUMN_NAME_PREPARATION_STEP + ") REFERENCES " + PreparationStepEntry.TABLE_NAME + "(" + PreparationStepEntry._ID + ")" +
                        " )";
        protected static final String SQL_DROP_TABLE_RECIPE2PREPARATION_STEP =
                "DROP TABLE IF EXISTS " + Recipe2PreparationStepEntry.TABLE_NAME;

        // ---

        protected static abstract class CategoryEntry implements BaseColumns {
            public static final String TABLE_NAME = "categories";
            public static final String COLUMN_NAME_TITLE = "title";
        }
        protected static final String SQL_CREATE_TABLE_CATEGORIES =
                "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                        CategoryEntry._ID + " INTEGER PRIMARY KEY," +
                        CategoryEntry.COLUMN_NAME_TITLE + TYPE_TEXT + NOT_NULL +
                        " )";
        protected static final String SQL_DROP_TABLE_CATEGORIES =
                "DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME;

        // ---

        protected static abstract class Recipe2CategoryEntry implements BaseColumns {
            public static final String TABLE_NAME = "recipe2category";
            public static final String COLUMN_NAME_RECIPE = "recipe";
            public static final String COLUMN_NAME_CATEGORY = "category";
            public static final String COLUMN_NAME_LIST_POSITION = "position";
        }
        protected static final String SQL_CREATE_TABLE_RECIPE2CATEGORY =
                "CREATE TABLE " + Recipe2CategoryEntry.TABLE_NAME + " (" +
                        Recipe2CategoryEntry._ID + " INTEGER PRIMARY KEY," +
                        Recipe2CategoryEntry.COLUMN_NAME_RECIPE + TYPE_INTEGER + COMMA_SEP +
                        Recipe2CategoryEntry.COLUMN_NAME_CATEGORY + TYPE_INTEGER + COMMA_SEP +
                        Recipe2CategoryEntry.COLUMN_NAME_LIST_POSITION + TYPE_INTEGER + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2CategoryEntry.COLUMN_NAME_RECIPE + ") REFERENCES " + RecipeEntry.TABLE_NAME + "(" + RecipeEntry._ID + ")" + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2CategoryEntry.COLUMN_NAME_CATEGORY + ") REFERENCES " + CategoryEntry.TABLE_NAME + "(" + CategoryEntry._ID + ")" +
                        " )";
        protected static final String SQL_DROP_TABLE_RECIPE2CATEGORY =
                "DROP TABLE IF EXISTS " + Recipe2CategoryEntry.TABLE_NAME;

        // ---

        protected static abstract class ImageEntry implements BaseColumns {
            public static final String TABLE_NAME = "images";
            public static final String COLUMN_NAME_PATH = "path_to_image";
        }
        protected static final String SQL_CREATE_TABLE_IMAGES =
                "CREATE TABLE " + ImageEntry.TABLE_NAME + " (" +
                        ImageEntry._ID + " INTEGER PRIMARY KEY," +
                        ImageEntry.COLUMN_NAME_PATH + TYPE_TEXT + DEFAULT + "'none'" +
                        " )";
        protected static final String SQL_DROP_TABLE_IMAGES =
                "DROP TABLE IF EXISTS " + ImageEntry.TABLE_NAME;

        // ---

        protected static abstract class Recipe2ImageEntry implements BaseColumns {
            public static final String TABLE_NAME = "recipe2image";
            public static final String COLUMN_NAME_RECIPE = "recipe";
            public static final String COLUMN_NAME_IMAGE = "image";
        }
        protected static final String SQL_CREATE_TABLE_RECIPE2IMAGE =
                "CREATE TABLE " + Recipe2ImageEntry.TABLE_NAME + " (" +
                        Recipe2ImageEntry._ID + " INTEGER PRIMARY KEY," +
                        Recipe2ImageEntry.COLUMN_NAME_IMAGE + TYPE_INTEGER + COMMA_SEP +
                        Recipe2ImageEntry.COLUMN_NAME_RECIPE + TYPE_INTEGER + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2ImageEntry.COLUMN_NAME_RECIPE + ") REFERENCES " + RecipeEntry.TABLE_NAME + "(" + RecipeEntry._ID + ")" + COMMA_SEP +
                        "FOREIGN KEY (" + Recipe2ImageEntry.COLUMN_NAME_IMAGE + ") REFERENCES " + ImageEntry.TABLE_NAME + "(" + ImageEntry._ID + ")" +
                        " )";
        protected static final String SQL_DROP_TABLE_RECIPE2IMAGE =
                "DROP TABLE IF EXISTS " + Recipe2ImageEntry.TABLE_NAME;


    }
}
