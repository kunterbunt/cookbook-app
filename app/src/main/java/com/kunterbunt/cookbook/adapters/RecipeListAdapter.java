package com.kunterbunt.cookbook.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.data.Recipe;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by kunterbunt on 19.11.14.
 */
public class RecipeListAdapter extends ArrayAdapter<Recipe> {

    private int resource;

    public RecipeListAdapter(Context context, int resource, List<Recipe> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View recipeView = convertView;
        if (recipeView == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            recipeView = inflater.inflate(resource, parent, false);
        }
        // Get recipe from list.
        Recipe recipe = getItem(position);
        // Populate fields.
        ImageView imageView = (ImageView) recipeView.findViewById(R.id.recipe_image);
        if (!recipe.getImagePath().equals(Recipe.NO_IMAGE)) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoader.getInstance().displayImage("file://" + recipe.getImagePath(), imageView);
        } else {
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_cutlery));
        }

        ((TextView) recipeView.findViewById(R.id.recipe_name)).setText(recipe.getName());
        RatingBar ratingBar = (RatingBar) recipeView.findViewById(R.id.recipe_rating_bar);
        ratingBar.setRating(recipe.getRating());
        // Color rating bar.
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getContext().getResources().getColor(R.color.app_color), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(getContext().getResources().getColor(R.color.app_color_accent), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getContext().getResources().getColor(R.color.app_color_accent), PorterDuff.Mode.SRC_ATOP);
        // Set on touch listener.
        recipeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("A", "Clicked");
            }
        });
        recipeView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        return recipeView;
    }

}
