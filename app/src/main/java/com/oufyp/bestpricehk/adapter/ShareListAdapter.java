package com.oufyp.bestpricehk.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.oufyp.bestpricehk.R;
import com.oufyp.bestpricehk.model.Share;

import java.util.ArrayList;

public class ShareListAdapter extends ArrayAdapter<Share> {
    private Context context;
    private ArrayList<Share> shareItems;
    public ShareListAdapter(Context context, ArrayList<Share> shareItems) {
        super(context, 0, shareItems);
        this.context = context;
        this.shareItems = shareItems;
    }

    @Override
    public int getCount() {
        return shareItems.size();
    }

    @Override
    public Share getItem(int i) {
        return shareItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Share share = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.row_list_shares, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.username = (TextView) convertView.findViewById(R.id.shares_username);
            viewHolder.details = (TextView) convertView.findViewById(R.id.shares_details);
            viewHolder.timestamp = (TextView) convertView.findViewById(R.id.shares_timestamp);
            viewHolder.msg = (TextView) convertView.findViewById(R.id.shares_msg);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.username.setText(share.getUsername());
        viewHolder.details.setText(share.getProductName() + "\n" + share.getDetails());
        viewHolder.timestamp.setText(share.getTimestamp());
        viewHolder.msg.setText(share.getMsg());
        return convertView;
    }

    static class ViewHolder {
        TextView username;
        TextView details;
        TextView timestamp;
        TextView msg;
    }
}
