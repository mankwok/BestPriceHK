package com.oufyp.bestpricehk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.adapter.ProductListAdapter;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.database.DatabaseHandler;
import com.oufyp.bestpricehk.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class FavouritesActivity extends Activity {
    public static final String TAG = FavouritesActivity.class.getSimpleName();
    private Context mContext = this;
    private DatabaseHandler db;
    private ArrayList<Product> favList = new ArrayList<>();
    //filter list
    private ArrayList<Product> pkList = new ArrayList<>();
    private ArrayList<Product> weList = new ArrayList<>();
    private ArrayList<Product> juList = new ArrayList<>();
    private ArrayList<Product> mpList = new ArrayList<>();
    private ProductListAdapter adapter;
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
        adapter = new ProductListAdapter(mContext, favList);
        lv = (ListView) findViewById(android.R.id.list);
        lv.addFooterView(loadingView, null, false);
        lv.setAdapter(adapter);
        getFavProducts();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

            Collections.sort(adapter.getProductItems(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getName().compareTo(rhs.getName()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_brand) {
            Collections.sort(adapter.getProductItems(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getBrand().compareTo(rhs.getBrand()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_type) {
            Collections.sort(adapter.getProductItems(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (lhs.getType().compareTo(rhs.getType()));
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_share) {
            Collections.sort(adapter.getProductItems(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (rhs.getCountShare() - lhs.getCountShare());
                }
            });
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sort_by_fav) {
            Collections.sort(adapter.getProductItems(), new Comparator<Product>() {
                public int compare(Product lhs, Product rhs) {
                    return (rhs.getCountFav() - lhs.getCountFav());
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
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
                            favList.add(product);
                            setFilterList(product, price);
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

        for (Product p : favList) {
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
        View bestPricesView = getLayoutInflater().inflate(R.layout.view_best_price, null, false);
        TextView tv = (TextView) bestPricesView.findViewById(R.id.best_price);
        tv.setText(getString(R.string.total_price, totalBest, totalProduct, bestPrice));
        tv = (TextView) bestPricesView.findViewById(R.id.pk_price);
        tv.setText(getString(R.string.pk_price, storePrice[0],productCounter[0]));
        tv = (TextView) bestPricesView.findViewById(R.id.wellcome_price);
        tv.setText(getString(R.string.wellcome_price, storePrice[1],productCounter[1]));
        tv = (TextView) bestPricesView.findViewById(R.id.jusco_price);
        tv.setText(getString(R.string.jusco_price, storePrice[2],productCounter[2]));
        tv = (TextView) bestPricesView.findViewById(R.id.mp_price);
        tv.setText(getString(R.string.mp_price, storePrice[3],productCounter[3]));
        checkStore(bestPricesView);
        lv.addHeaderView(bestPricesView, null, false);
    }

    //change background colour to red when cannot buy all fav product in that shop
    public void checkStore(View v) {
        if (productCounter[0] < favList.size()) {
            v.findViewById(R.id.pk_bg).setBackgroundColor(getResources().getColor(R.color.red));
        }
        if (productCounter[1] < favList.size()) {
            v.findViewById(R.id.we_bg).setBackgroundColor(getResources().getColor(R.color.red));
        }
        if (productCounter[2] < favList.size()) {
            v.findViewById(R.id.ju_bg).setBackgroundColor(getResources().getColor(R.color.red));
        }
        if (productCounter[3] < favList.size()) {
            v.findViewById(R.id.mp_bg).setBackgroundColor(getResources().getColor(R.color.red));

        }
    }

    public void setFilterList(Product p, String[] price) {
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

    public void filter(View v) {
        int id = v.getId();
        if (id == R.id.pk_bg) {
            adapter.setProductItems(pkList);
        } else if (id == R.id.we_bg) {
            adapter.setProductItems(weList);
        } else if (id == R.id.ju_bg) {
            adapter.setProductItems(juList);
        } else if (id == R.id.mp_bg) {
            adapter.setProductItems(mpList);
        } else if (id == R.id.clear_filter) {
            adapter.setProductItems(favList);
        }
        adapter.notifyDataSetChanged();
    }
}
