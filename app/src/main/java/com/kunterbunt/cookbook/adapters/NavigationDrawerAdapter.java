package com.kunterbunt.cookbook.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.tools.Tools;

/**
 * Created by kunterbunt on 13.11.14.
 */
public class NavigationDrawerAdapter extends ArrayAdapter<String> {

    private int mSelectedItem, mResource;

    public NavigationDrawerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mResource = resource;
    }

//    public int getSelectedItem() {
//        return mSelectedItem;
//    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        // inflate layout
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        row = inflater.inflate(mResource, parent, false);
        // populate title
        TextView title = (TextView) row.findViewById(R.id.drawer_list_item_title);
        title.setText(getItem(position));
        int mSelectedItem = Tools.getPref(Tools.PREFS_CURRENT_ACITIVTY, 0, getContext());
        if (position == mSelectedItem) {
            title.setTextColor(getContext().getResources().getColor(R.color.app_color));
            title.setTypeface(Typeface.DEFAULT_BOLD);
        } else
            title.setTextColor(getContext().getResources().getColor(R.color.white));
        return row;
    }
}
