package com.kunterbunt.cookbook.data;

import java.util.List;

/**
 * Data representation of a category. Categories always have a set database _id.
 */
public class Category {
    private String name;
    /** Database ID. */
    private long _id;

    /**
     * Puts category of this name into database and sets own _id by looking it up and adding itself to the database if necessary.
     * @param name
     */
    public Category(String name) {
        this(name, DatabaseHelper.getInstance().putCategory(name));
    }

    /**
     * @param name
     * @param _id ID in database.
     */
    public Category(String name, long _id) {
        this.name = name;
        this._id = _id;
    }

    /**
     * @return All recipes saved for this category.
     */
    public List<Recipe> getRecipeList() {
        return DatabaseHelper.getInstance().getRecipesForCategory(_id);
    }

    /**
     * @return Category name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return ID in database.
     */
    public long getId() {
        return _id;
    }


    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Category)
            return name.equals(((Category) o).getName());
        else
            return false;
    }
}
