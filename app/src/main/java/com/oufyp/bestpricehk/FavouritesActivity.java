package com.oufyp.bestpricehk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    //filter list
    private ArrayList<FavProduct> pkList = new ArrayList<>();
    private ArrayList<FavProduct> weList = new ArrayList<>();
    private ArrayList<FavProduct> juList = new ArrayList<>();
    private ArrayList<FavProduct> mpList = new ArrayList<>();
    private FavProductAdapter adapter;
    private ListView lv;
    private View loadingView;
    private Boolean success = false;
    private HashMap<String, String> pk = new HashMap<>();
    private HashMap<String, String> we = new HashMap<>();
    private HashMap<String, String> ju = new HashMap<>();
    private HashMap<String, String> mp = new HashMap<>();
    private int[] productCounter = {0, 0, 0, 0};

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
            intent.putExtra("PK", pk);
            intent.putExtra("WE", we);
            intent.putExtra("JU", ju);
            intent.putExtra("MP", mp);
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
                            setFilterList(favProduct, price);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                lv.removeFooterView(loadingView);
                adapter.notifyDataSetChanged();
                setBestStoreList();
                setBestPricesView();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    public void setBestStoreList() {
        for (Product p : favList) {
            if (p.getBestStore() == 0) {
                pk.put(p.getName(), p.getBestPrice());
            } else if (p.getBestStore() == 1) {
                we.put(p.getName(), p.getBestPrice());
            } else if (p.getBestStore() == 2) {
                ju.put(p.getName(), p.getBestPrice());
            } else if (p.getBestStore() == 3) {
                mp.put(p.getName(), p.getBestPrice());
            }
        }
    }

    public void setBestPricesView() {
        int totalBest = 0;
        int totalProduct = 0;
        double bestPrice = 0.0;
        double[] storePrice = {0.0, 0.0, 0.0, 0.0};

        for (FavProduct p : favList) {
            String[] price = p.getPrice();
            if (!p.getBestPrice().equals("none")) {
                totalBest++;
                bestPrice += Double.parseDouble(p.getBestPrice());
            }
            totalProduct++;
            if (!price[0].equals("--")) {
                storePrice[0] += Double.parseDouble(price[0]);
                productCounter[0]++;
            }
            if (!price[1].equals("--")) {
                storePrice[1] += Double.parseDouble(price[1]);
                productCounter[1]++;
            }
            if (!price[2].equals("--")) {
                storePrice[2] += Double.parseDouble(price[2]);
                productCounter[2]++;
            }
            if (!price[3].equals("--")) {
                storePrice[3] += Double.parseDouble(price[3]);
                productCounter[3]++;
            }
        }
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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPriceView(position, bestPricesView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(4);
        lv.addHeaderView(bestPricesView, null, false);
    }

    public void setPriceView(int displayflag, View view) {
        double price = 0.0;
        TextView tv = (TextView) view.findViewById(R.id.total_price);
        for (FavProduct favProduct : favList) {
            favProduct.setDisplayFlag(displayflag);
            price += favProduct.getSubTotal(displayflag);
            Log.d("price",""+ favProduct.getSubTotal(displayflag));
            adapter.notifyDataSetChanged();
        }
        tv.setText(getString(R.string.fav_total_price, price));
    }
    //change background colour to red when cannot buy all fav product in that shop

    /**
     * public void checkStore(View v) {
     * if (productCounter[0] < favList.size()) {
     * v.findViewById(R.id.pk_bg).setBackgroundColor(getResources().getColor(R.color.red));
     * }
     * if (productCounter[1] < favList.size()) {
     * v.findViewById(R.id.we_bg).setBackgroundColor(getResources().getColor(R.color.red));
     * }
     * if (productCounter[2] < favList.size()) {
     * v.findViewById(R.id.ju_bg).setBackgroundColor(getResources().getColor(R.color.red));
     * }
     * if (productCounter[3] < favList.size()) {
     * v.findViewById(R.id.mp_bg).setBackgroundColor(getResources().getColor(R.color.red));
     * <p/>
     * }
     * }
     */

    public void setFilterList(FavProduct p, String[] price) {
        if (!price[0].equals("--")) {
            pkList.add(p);
        }
        if (!price[1].equals("--")) {
            weList.add(p);
        }
        if (!price[2].equals("--")) {
            juList.add(p);
        }
        if (!price[3].equals("--")) {
            mpList.add(p);
        }
    }

    /**public void filter(View v) {
     int id = v.getId();
     if (id == R.id.pk_bg) {
     adapter.setFavProducts(pkList);
     } else if (id == R.id.we_bg) {
     adapter.setFavProducts(weList);
     } else if (id == R.id.ju_bg) {
     adapter.setFavProducts(juList);
     } else if (id == R.id.mp_bg) {
     adapter.setFavProducts(mpList);
     } else if (id == R.id.clear_filter) {
     adapter.setFavProducts(favList);
     }
     adapter.notifyDataSetChanged();
     }*/
}
