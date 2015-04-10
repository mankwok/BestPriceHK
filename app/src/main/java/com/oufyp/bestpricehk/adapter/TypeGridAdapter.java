package com.oufyp.bestpricehk.adapter;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oufyp.bestpricehk.R;

public class TypeGridAdapter extends BaseAdapter {
    private Context context;
    private final String[] type;
    private final int[] imageid;

    public TypeGridAdapter(Context c, String[] type, int[] imageid) {
        this.context = c;
        this.imageid = imageid;
        this.type = type;
    }

    @Override
    public int getCount() {
        return type.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.grid_single, null);
        }
        TextView textView = (TextView) view.findViewById(R.id.grid_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.grid_image);
        textView.setText(type[position]);
        imageView.setImageResource(imageid[position]);
        return view;
    }
}
