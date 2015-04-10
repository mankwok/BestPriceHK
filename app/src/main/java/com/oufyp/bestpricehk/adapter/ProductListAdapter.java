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
import com.oufyp.bestpricehk.model.Product;

import java.util.ArrayList;

public class ProductListAdapter extends ArrayAdapter<Product> {
    private Context context;
    private ArrayList<Product> productItems;

    public ProductListAdapter(Context context, ArrayList<Product> productItems) {
        super(context, 0, productItems);
        this.context = context;
        this.productItems = productItems;
    }

    public ArrayList<Product> getProductItems(){
        return this.productItems;
    }
    public void setProductItems(ArrayList<Product> productItems){
        this.productItems = productItems;
    }

    @Override
    public int getCount() {
        return productItems.size();
    }

    @Override
    public Product getItem(int i) {
        return productItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = getItem(position);
        String bestStore = "";
        switch (product.getBestStore()) {
            case 0:
                bestStore = context.getString(R.string.parknshop);
                break;
            case 1:
                bestStore = context.getString(R.string.wellcome);
                break;
            case 2:
                bestStore = context.getString(R.string.jusco);
                break;

            case 3:
                bestStore = context.getString(R.string.market_place);
                break;
        }
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.row_list_products, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.productImage = (ImageView) convertView.findViewById(R.id.list_product_image);
            viewHolder.productName = (TextView) convertView.findViewById(R.id.list_product_name);
            viewHolder.productBrand = (TextView) convertView.findViewById(R.id.list_product_brand);
            viewHolder.productType = (TextView) convertView.findViewById(R.id.list_product_type);
            viewHolder.productFav = (TextView) convertView.findViewById(R.id.list_product_favourites);
            viewHolder.productShare = (TextView) convertView.findViewById(R.id.list_product_shares);
            viewHolder.bestPrice = (TextView) convertView.findViewById(R.id.list_best_price);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.productImage.setImageResource(product.getImage(product.getType()));
        viewHolder.productName.setText(product.getName());
        viewHolder.productBrand.setText(product.getBrand());
        viewHolder.productType.setText(product.getType());
        viewHolder.productFav.setText("" + product.getCountFav());
        viewHolder.productShare.setText("" + product.getCountShare());
        if(bestStore.equals("")){
            viewHolder.bestPrice.setText(product.getBestPrice());
        }else {
            viewHolder.bestPrice.setText(product.getBestPrice() + " (" + bestStore + ")");
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productBrand;
        TextView productFav;
        TextView productShare;
        TextView productType;
        TextView bestPrice;

    }
}
