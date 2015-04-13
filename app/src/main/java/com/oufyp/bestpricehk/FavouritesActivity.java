package com.oufyp.bestpricehk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.adapter.FavProductAdapter;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.database.DatabaseHandler;
import com.oufyp.bestpricehk.model.FavProduct;
import com.oufyp.bestpricehk.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class FavouritesActivity extends Activity {
    public static final String TAG = FavouritesActivity.class.getSimpleName();
    private Context mContext = this;
    private DatabaseHandler db;
    private ArrayList<FavProduct> favList = new ArrayList<>();
    private FavProductAdapter adapter;
    private ListView lv;
    private View loadingView;
    private Boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        db = DatabaseHandler.getInstance(mContext);
        loadingView = getLayoutInflater().inflate(R.layout.loading, null, false);
        adapter = new FavProductAdapter(mContext, favList);
        lv = (ListView) findViewById(android.R.id.list);
        lv.addFooterView(loadingView, null, false);
        lv.setAdapter(adapter);
        getFavProducts();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FavProduct favProduct = (FavProduct) lv.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), DisplayProductInfo.class);
                intent.putExtra("TAG", TAG);
                intent.putExtra("PID", favProduct.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fav_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_map) {
            Intent intent = new Intent(this, FavProductsLocation.class);
            intent.putExtra("TAG", TAG);
            intent.putExtra("FAVLIST", favList);
            startActivity(intent);
            return true;
        } else if (id == R.id.sort_by_name) {

            Collections.sort(adapter.getFavProducts(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getName().compareTo(rhs.getName()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_brand) {
            Collections.sort(adapter.getFavProducts(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getBrand().compareTo(rhs.getBrand()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_type) {
            Collections.sort(adapter.getFavProducts(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getType().compareTo(rhs.getType()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getFavProducts() {
        HashMap<String, String> user = db.getUserDetails();
        String uid = user.get("uid");
        String url = String.format("http://101.78.220.131:8909/bestpricehk/fav_api/favlist.php?uid=%s", uid);
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    success = obj.getBoolean("success");
                    if (success) {
                        JSONArray products = obj.getJSONArray("products");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject jsonObject = products.getJSONObject(i);
                            String id = jsonObject.getString("pid");
                            String name = jsonObject.getString("name");
                            String type = jsonObject.getString("type");
                            String brand = jsonObject.getString("brand");
                            int qty = jsonObject.getInt("qty");
                            String[] price = {"--", "--", "--", "--"};
                            price[0] = jsonObject.getString("price1");
                            price[1] = jsonObject.getString("price2");
                            price[2] = jsonObject.getString("price3");
                            price[3] = jsonObject.getString("price4");
                            String bestPrice = jsonObject.getString("bestPrice");
                            String[] discount = {"--", "--", "--", "--"};
                            discount[0] = jsonObject.getString("discount1");
                            discount[1] = jsonObject.getString("discount2");
                            discount[2] = jsonObject.getString("discount3");
                            discount[3] = jsonObject.getString("discount4");
                            FavProduct favProduct = new FavProduct(id, name, type, brand, qty, price, discount, bestPrice, 4);
                            favProduct.setPrice(price);
                            favList.add(favProduct);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lv.removeFooterView(loadingView);
                setFavListView();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void setFavListView() {
        final View bestPricesView = getLayoutInflater().inflate(R.layout.view_best_price, null, false);
        Spinner spinner = (Spinner) bestPricesView.findViewById(R.id.spinner);
        List<String> shopOption = new ArrayList<>();
        shopOption.add("Shop in ParknShop");
        shopOption.add("Shop in Wellcome");
        shopOption.add("Shop in Jusco");
        shopOption.add("Shop in Market Place");
        shopOption.add("Shop with best price");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shopOption);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(4);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFavListView(position, bestPricesView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lv.addHeaderView(bestPricesView, null, false);
    }

    public void updateFavListView(int displayflag, View headerView) {
        double totaPrice = 0.0;
        double subtotal;
        int counter = 0;
        TextView tv = (TextView) headerView.findViewById(R.id.total_price);
        for (FavProduct favProduct : favList) {
            favProduct.setDisplayFlag(displayflag);
            subtotal = favProduct.getSubTotal(displayflag);
            if(subtotal>0){
                counter++;
            }
            totaPrice += subtotal;
        }
        tv.setText(getString(R.string.fav_total_price, totaPrice,counter,favList.size()));
        adapter.notifyDataSetChanged();
    }

}
