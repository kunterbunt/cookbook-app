package com.kunterbunt.cookbook.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.kunterbunt.cookbook.R;
import com.kunterbunt.cookbook.tools.Tools;

/**
 * Created by kunterbunt on 13.11.14.
 */
public class NavigationDrawerAdapter extends AbstractAdapter<String> {

    public NavigationDrawerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    @Override
    protected void populateFields(View view, int position) {
        TextView title = (TextView) view.findViewById(R.id.drawer_list_item_title);
        title.setText((String) getItem(position));
        int mSelectedItem = Tools.getPref(Tools.PREFS_CURRENT_ACITIVTY, 0, getContext());
        if (position == mSelectedItem) {
            title.setTextColor(getContext().getResources().getColor(R.color.app_color));
            title.setTypeface(Typeface.DEFAULT_BOLD);
        } else
            title.setTextColor(getContext().getResources().getColor(R.color.white));
    }
}
