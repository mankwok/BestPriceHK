package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.adapter.ProductListAdapter;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class DisplayProducts extends Activity {
    public static final String TAG = DisplayProductInfo.class.getSimpleName();
    private Context mContext = this;
    private ArrayList<Product> productsList = new ArrayList<>();
    private ListView lv;
    private View loadingView;
    private ProductListAdapter adapter;
    private String type;
    private int nextStart = 0;
    private boolean noResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_products);
        Intent intent = getIntent();
        type = intent.getStringExtra(BrowseType.PRODUCT_TYPE);
        if (type.equals(getString(R.string.milk_powder))) {
            type = "powder";
        } else if (type.equals(getString(R.string.baby_care))) {
            type = "baby";
        }
        getActionBar().setTitle(intent.getStringExtra(BrowseType.PRODUCT_TYPE));
        loadingView = getLayoutInflater().inflate(R.layout.loading, null, false);
        adapter = new ProductListAdapter(mContext, productsList);
        lv = (ListView) findViewById(android.R.id.list);
        lv.addFooterView(loadingView, null, false);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Product aProduct = (Product) lv.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), DisplayProductInfo.class);
                intent.putExtra("product", aProduct);
                startActivity(intent);
            }
        });
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (lv.getLastVisiblePosition() == lv.getAdapter().getCount() - 1
                        && lv.getChildAt(lv.getChildCount() - 1) != null
                        && lv.getChildAt(lv.getChildCount() - 1).getBottom() <= lv.getHeight()) {
                    if (!noResult) {
                        getProducts();
                    } else {
                        lv.removeFooterView(loadingView);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
        getProducts();
    }

    public void getProducts() {
        String url = String.format("http://101.78.220.131:8909/bestpricehk/products/type/?type=%s&start=%d", type, nextStart);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    noResult = obj.getBoolean("no_result");
                    if (!noResult) {
                        JSONArray products = obj.getJSONArray("products");
                        nextStart = obj.getInt("next_start");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject jsonObject = products.getJSONObject(i);
                            String id = jsonObject.getString("pid");
                            String name = jsonObject.getString("name");
                            String type = jsonObject.getString("type");
                            String brand = jsonObject.getString("brand");
                            int countFav = Integer.parseInt(jsonObject.getString("count_fav"));
                            int countShare = Integer.parseInt(jsonObject.getString("count_share"));
                            String[] price = {"--", "--", "--", "--"};
                            price[0] = jsonObject.getString("price1");
                            price[1] = jsonObject.getString("price2");
                            price[2] = jsonObject.getString("price3");
                            price[3] = jsonObject.getString("price4");
                            String bestPrice = jsonObject.getString("bestPrice");
                            Product product = new Product(id, name, type, brand, countFav, countShare, bestPrice);
                            product.setPrice(price);
                            productsList.add(product);
                            obj.getBoolean("no_result");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                lv.removeFooterView(loadingView);
            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_products, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        menu.findItem(R.id.sort_by_type).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.sort_by_name) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getName().compareTo(rhs.getName()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        }
        else if (id == R.id.sort_by_brand) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getBrand().compareTo(rhs.getBrand()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        }
        else if (id == R.id.sort_by_share) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (rhs.getCountShare() - lhs.getCountShare());
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        }
        else if (id == R.id.sort_by_fav) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (rhs.getCountFav() - lhs.getCountFav());
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
