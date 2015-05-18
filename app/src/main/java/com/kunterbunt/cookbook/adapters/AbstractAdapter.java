package com.kunterbunt.cookbook.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kunterbunt.cookbook.R;

import java.util.List;

/**
 * Created by kunterbunt on 18.05.15.
 */
public abstract class AbstractAdapter<T> extends ArrayAdapter {

    protected Context context;
    protected int resource;

    public AbstractAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    public AbstractAdapter(Context context, int resource, Object[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    public AbstractAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate layout.
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource, parent, false);
        // Populate fields.
        populateFields(view, position);
        return view;
    }

    protected abstract void populateFields(View view, int position);
}
