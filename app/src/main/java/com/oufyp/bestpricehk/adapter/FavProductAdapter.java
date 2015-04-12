package com.oufyp.bestpricehk.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oufyp.bestpricehk.R;
import com.oufyp.bestpricehk.model.FavProduct;

import java.util.ArrayList;

public class FavProductAdapter extends ArrayAdapter<FavProduct> {
    private Context context;
    private ArrayList<FavProduct> favProducts;

    public FavProductAdapter(Context context, ArrayList<FavProduct> productItems) {
        super(context, 0, productItems);
        this.context = context;
        this.favProducts = productItems;
    }

    public ArrayList<FavProduct> getFavProducts() {
        return this.favProducts;
    }

    public void setFavProducts(ArrayList<FavProduct> favProducts) {
        this.favProducts = favProducts;
    }

    @Override
    public int getCount() {
        return favProducts.size();
    }

    @Override
    public FavProduct getItem(int i) {
        return favProducts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavProduct favProduct = getItem(position);
        int displayFlag = favProduct.getDisplayFlag();
        String available = favProduct.getAvailable(displayFlag);
        double unitPrice = favProduct.getUnitPrice(displayFlag);
        double subtotal = favProduct.getSubTotal(displayFlag);
        String displayDiscount = favProduct.getDisplayDiscount(displayFlag);
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.row_list_fav, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.favImage = (ImageView) convertView.findViewById(R.id.fav_image);
            viewHolder.favName = (TextView) convertView.findViewById(R.id.fav_name);
            viewHolder.favAvailable = (TextView) convertView.findViewById(R.id.fav_availability);
            viewHolder.favQty = (TextView) convertView.findViewById(R.id.fav_qty);
            viewHolder.favQty = (TextView) convertView.findViewById(R.id.fav_qty);
            viewHolder.favSubtotal = (TextView) convertView.findViewById(R.id.fav_subtotal);
            viewHolder.favUnitPrice = (TextView) convertView.findViewById(R.id.fav_unit_price);
            viewHolder.favDiscount = (TextView) convertView.findViewById(R.id.fav_discount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.favImage.setImageResource(favProduct.getImage(favProduct.getType()));
        viewHolder.favName.setText(favProduct.getName());
        viewHolder.favAvailable.setText(available);
        viewHolder.favQty.setText(context.getString(R.string.fav_qty,favProduct.getQty()));
        if (unitPrice == 0.0) {
            viewHolder.favUnitPrice.setText(context.getString(R.string.fav_no_unit_price));
            viewHolder.favSubtotal.setText(context.getString(R.string.fav_no_subtotal));
        } else {
            viewHolder.favUnitPrice.setText(context.getString(R.string.fav_unit_price,unitPrice));
            viewHolder.favSubtotal.setText(context.getString(R.string.fav_subtotal, subtotal));
        }
        viewHolder.favDiscount.setText(context.getString(R.string.fav_discount,displayDiscount));
        return convertView;
    }

    static class ViewHolder {
        ImageView favImage;
        TextView favName;
        TextView favAvailable;
        TextView favQty;
        TextView favUnitPrice;
        TextView favSubtotal;
        TextView favDiscount;

    }
}
