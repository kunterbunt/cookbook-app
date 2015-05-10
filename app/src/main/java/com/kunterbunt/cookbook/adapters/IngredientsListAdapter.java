package com.kunterbunt.cookbook.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.data.Ingredient;
import com.kunterbunt.cookbook.tools.Tools;

import java.util.List;

/**
 * Created by kunterbunt on 11.11.14.
 */
public class IngredientsListAdapter extends ArrayAdapter<Ingredient> {

    private Context context;
    private int resource;

    public IngredientsListAdapter(Context context, int resource, List<Ingredient> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        // get ingredient from list
        Ingredient ingredient = getItem(position);
        // inflate layout
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(resource, parent, false);
        // populate fields
        TextView amountField = (TextView) row.findViewById(R.id.ingredient_amount);
        amountField.setText("" + Tools.format(ingredient.getAmount()) + " " + context.getResources().getStringArray(R.array.units)[ingredient.getUnit()]);
        TextView nameField = (TextView) row.findViewById(R.id.ingredient_name);
        nameField.setText(ingredient.getName());
        return row;
    }
}
