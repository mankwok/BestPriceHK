package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

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
import java.util.HashMap;


public class SearchActivity extends Activity {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private ArrayList<Product> productsList = new ArrayList<>();
    private ProductListAdapter adapter;
    private ListView lv;
    private View loadingView;
    private TextView tv;
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        lv = (ListView) findViewById(android.R.id.list);
        loadingView = getLayoutInflater().inflate(R.layout.loading, null, false);
        lv.addFooterView(loadingView, null, false);
        tv = (TextView) findViewById(R.id.search_query);
        Intent intent = getIntent();
        // search widget
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            tv.setText(getString(R.string.label_search, query));
            search(query);
        }//advanced search
        else if (intent.getExtras().getString("TAG") != null && intent.getExtras().getString("TAG").equals("AdvSearchFragment")) {
            HashMap<String, String> params = (HashMap<String, String>) intent.getSerializableExtra("params");
            tv.setText(getString(R.string.label_adv_search, params.get("keyword").replace("%20"," "), params.get("type"), params.get("store"), params.get("startPrice"), params.get("endPrice")));
            advSearch(params);
        }
        // browse more
        else if (intent.getExtras().getString("type") != null && intent.getExtras().getString("store") != null) {
            tv = (TextView) findViewById(R.id.search_query);
            String type = intent.getExtras().getString("type");
            String store = intent.getExtras().getString("store");
            tv.setText(getString(R.string.label_search_in_store, type, store));
            browseMore(type);
        }
        adapter = new ProductListAdapter(this, productsList);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_products, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
        } else if (id == R.id.sort_by_brand) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getBrand().compareTo(rhs.getBrand()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_type) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getType().compareTo(rhs.getType()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_fav) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (rhs.getCountFav() - lhs.getCountFav());
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_share) {
            Collections.sort(productsList, new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (rhs.getCountShare() - lhs.getCountShare());
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void search(String query) {
        String url = String.format("http://101.78.220.131:8909/bestpricehk/products/search/%s", query);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new SearchResponseListener(), new SearchErrorListener());
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void advSearch(HashMap<String, String> params) {
        String key = params.get("keyword");
        String type = params.get("type");
        if (type.equals(getString(R.string.milk_powder))) {
            type = "powder";
        } else if (type.equals(getString(R.string.baby_care))) {
            type = "baby";
        }
        //String store = params.get("store");
        String startPrice = params.get("startPrice");
        String endPrice = params.get("endPrice");
        String url = String.format("http://101.78.220.131:8909/bestpricehk/adv_search.php?key=%s&type=%s&start=%s&end=%s", key, type, startPrice, endPrice);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new SearchResponseListener(), new SearchErrorListener());
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void browseMore(String type) {
        String url = String.format("http://101.78.220.131:8909/bestpricehk/search.php?type=%s", type);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new SearchResponseListener(), new SearchErrorListener());
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    private class SearchResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject obj) {
            try {
                success = obj.getBoolean("success");
                lv.removeFooterView(loadingView);
                if (success) {
                    JSONArray products = obj.getJSONArray("products");
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
                    }
                } else {
                    ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.view_no_product, lv, false);
                    lv = (ListView) findViewById(android.R.id.list);
                    lv.addHeaderView(header, null, false);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class SearchErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d(TAG, "Error: " + error.getMessage());
            lv.removeFooterView(loadingView);
            ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.view_no_product, lv, false);
            lv.addHeaderView(header, null, false);
        }
    }
}
