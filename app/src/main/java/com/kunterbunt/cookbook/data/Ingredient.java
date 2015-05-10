package com.kunterbunt.cookbook.data;

/**
 * Created by kunterbunt on 11.11.14.
 */
public class Ingredient {

    public static final long NO_ID = -1;
    private String name;
    private float amount;
    private int unit;
    /** Database ID. */
    private long _id = NO_ID;

    public Ingredient(String name, float amount, int unit) {
        this.unit = unit;
        this.name = name;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Ingredient)
            return name.equals(((Ingredient) o).getName());
        else
            return false;
    }

    /**
     * Sets own _id by looking it up in the database, inserting itself into it if not present yet.
     */
    public void retrieveId() {
        _id = DatabaseHelper.getInstance().putIngredient(this);
    }

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public String toString() {
        return amount + "//" + unit + "//" + name;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

}
