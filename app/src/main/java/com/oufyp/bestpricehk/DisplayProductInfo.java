package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.app.CustomRequest;
import com.oufyp.bestpricehk.app.UserFunctions;
import com.oufyp.bestpricehk.database.DatabaseHandler;
import com.oufyp.bestpricehk.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayProductInfo extends Activity {
    public static final String TAG = DisplayProductInfo.class.getSimpleName();
    private Context mContext = this;
    private UserFunctions uf = new UserFunctions();
    private DatabaseHandler db;
    private Product product;
    private String[] price = {"--", "--", "--", "--"};
    private String[] discount = {"--", "--", "--", "--"};
    private String lastDate = "--";
    private ImageView image;
    private TextView description;
    private TextView countFav;
    private TextView countShare;
    private boolean success = false;
    private String selectedQty = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        db = DatabaseHandler.getInstance(mContext);
        Intent intent = getIntent();
        if (intent.getStringExtra("TAG") != null && (intent.getStringExtra("TAG").equals("FavouritesActivity") || intent.getStringExtra("TAG").equals("DisplayShares") || intent.getStringExtra("TAG").equals("CommunityFragment"))) {
            // handle the intent from DisplayShares activity and community fragment
            String pid = intent.getStringExtra("PID");
            product = new Product();
            String url = String.format("http://101.78.220.131:8909/bestpricehk/products.php?pid=%s", pid);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                JSONObject info = jsonObject.getJSONObject("simple_info");
                                product.setId(info.getString("pid"));
                                product.setName(info.getString("name"));
                                product.setType(info.getString("type"));
                                product.setBrand(info.getString("brand"));
                                product.setCountFav(Integer.parseInt(info.getString("count_fav")));
                                product.setCountShare(Integer.parseInt(info.getString("count_share")));
                                setDetailsView(product.getId());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                        }
                    });
            AppController.getInstance().addToRequestQueue(jsObjRequest);
        } else {
            product = (Product) intent.getExtras().getSerializable("product");
            setDetailsView(product.getId());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_product_info, menu);
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
        if (item.getItemId() == R.id.action_map) {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("PRODUCT", product);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void socialBtnHandler(View v){
        switch(v.getId()){
            case R.id.button_favourite:
                if (uf.isUserLoggedIn(mContext)) {
                    if (db.isFavourited(product.getId())) {
                        // remove favourite product
                        new AlertDialog.Builder(this)
                                .setTitle("Remove Favourite Product")
                                .setMessage("Are you sure want to remove product in favourite list? ")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        removeFavourite(product);
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                        }).create().show();
                    }// add favourite product
                    else {
                        setFavourite(product);
                    }
                } // user is not logged in
                else {
                    Toast.makeText(mContext, "Please login first.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_share:
                if (uf.isUserLoggedIn(mContext)) {
                    Intent intent = new Intent(this, ShareActivity.class);
                    intent.putExtra("PRODUCT", product);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Please login first.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void setPriceView(Product product) {
        String[] price = product.getPrice();
        String[] discount = product.getDiscount();
        TextView tv = (TextView) findViewById(R.id.pk_price);
        tv.setText(getString(R.string.tv_price, price[0]));
        tv = (TextView) findViewById(R.id.pk_dicount);
        tv.setText(getString(R.string.tv_discount, discount[0]));
        tv = (TextView) findViewById(R.id.wellcome_price);
        tv.setText(getString(R.string.tv_price, price[1]));
        tv = (TextView) findViewById(R.id.wellcome_dicount);
        tv.setText(getString(R.string.tv_discount, discount[1]));
        tv = (TextView) findViewById(R.id.jusco_price);
        tv.setText(getString(R.string.tv_price, price[2]));
        tv = (TextView) findViewById(R.id.jusco_dicount);
        tv.setText(getString(R.string.tv_discount, discount[2]));
        tv = (TextView) findViewById(R.id.mp_price);
        tv.setText(getString(R.string.tv_price, price[3]));
        tv = (TextView) findViewById(R.id.mp_dicount);
        tv.setText(getString(R.string.tv_discount, discount[3]));
        tv = (TextView) findViewById(R.id.last_updated);
        tv.setText(getString(R.string.last_updated, lastDate));
    }

    public void setDetailsView(String pid) {
        setContentView(R.layout.activity_display_product_info);
        String url = String.format("http://101.78.220.131:8909/bestpricehk/products/details/%s", pid);
        image = (ImageView) findViewById(R.id.productImg);
        description = (TextView) findViewById(R.id.productDescription);
        countFav = (TextView) findViewById(R.id.count_favourite);
        countShare = (TextView) findViewById(R.id.count_share);
        image.setImageResource(product.getImage(product.getType()));
        description.setText(product.getDescription());
        countFav.setText("" + product.getCountFav());
        countShare.setText("" + product.getCountShare());
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (url, null, new ResponseListener(), new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        //set default value to price and discount;
                        product.setDiscount(discount);
                        product.setPrice(price);

                        setPriceView(product);
                    }
                });
        AppController.getInstance().addToRequestQueue(jsObjRequest);

    }

    public void browseStore(View view) {
        int id = view.getId();
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        intent.putExtra("type", product.getType());
        switch (id) {
            case R.id.wellcome_img:
                intent.putExtra("store", "Wellcome");
                break;
            case R.id.pk_img:
                intent.putExtra("store", "ParknShop");
                break;
            case R.id.jusco_img:
                intent.putExtra("store", "Jusco");
                break;
            case R.id.mp_img:
                intent.putExtra("store", "Market Place");
                break;
        }
        intent.putExtra("TAG", TAG);
        startActivity(intent);
    }

    public void setFavourite(Product product) {
        if (db.isFavourited(product.getId())) {
            CharSequence text = product.getName() + " has already added to favourite list.";
            Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            addFavToServer(product);
        }
    }

    public void removeFavourite(Product product) {
        if (db.isFavourited(product.getId())) {
            db.deleteFavProduct(product);
            delFavFromServer(product);
        } else {
            CharSequence text = "Error in removing";
            Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void addFavToServer(final Product product) {
        final String uid = uf.getUserID(mContext);
        final String url = "http://101.78.220.131:8909/bestpricehk/fav_api/favproducts.php";
        List<String> qtyList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            qtyList.add("" + i);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, qtyList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner sp = new Spinner(this);
        sp.setAdapter(dataAdapter);
        new AlertDialog.Builder(this)
                .setTitle("Add product to favourite list")
                .setMessage("Select favourite product's quantity: ")
                .setView(sp)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedQty = sp.getSelectedItem().toString();
                        Map<String, String> params = new HashMap<>();
                        params.put("tag", "add");
                        params.put("uid", uid);
                        params.put("pid", product.getId());
                        params.put("qty", selectedQty);
                        CustomRequest request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject obj) {
                                db.addFavProduct(product); // add to local database
                                try {
                                    success = obj.getBoolean("success");
                                    if (success) {
                                        CharSequence text = "Added to favourite list.";
                                        Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
                                        toast.show();
                                    } else {
                                        Toast toast = Toast.makeText(mContext, "Cant add to favourite list", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.d(TAG, "Error: " + error.getMessage());
                            }
                        });
                        AppController.getInstance().addToRequestQueue(request);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).create().show();

    }

    public void delFavFromServer(Product product) {
        String uid = uf.getUserID(mContext);
        String url = "http://101.78.220.131:8909/bestpricehk/fav_api/favproducts.php";
        Map<String, String> params = new HashMap<>();
        params.put("tag", "delete");
        params.put("uid", uid);
        params.put("pid", product.getId());
        CustomRequest request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    success = obj.getBoolean("success");
                    if (success) {
                        Toast toast = Toast.makeText(mContext, "Removed from favourite list.", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(mContext, "Cant delete to favourite list", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(request);
    }

    public void viewShare(View view) {
        if (uf.isUserLoggedIn(mContext)) {
            Intent intent = new Intent(getApplicationContext(), DisplayShares.class);
            intent.putExtra("TAG", TAG);
            intent.putExtra("PID", product.getId());
            intent.putExtra("UID", uf.getUserID(mContext));
            startActivity(intent);
        } else {
            Toast.makeText(mContext, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
        }
    }

    public void reportProblem(View view) {
        if (uf.isUserLoggedIn(mContext)) {

            if (uf.isGoldMember(mContext)) {
                Intent intent = new Intent(this, ReportProblemActivity.class);
                intent.putExtra("PRODUCT", product);
                startActivity(intent);
            } else {
                Toast.makeText(mContext, getString(R.string.message_no_permission), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, getString(R.string.please_login), Toast.LENGTH_SHORT).show();
        }
    }

    private class ResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject obj) {
            try {
                success = obj.getBoolean("success");
                if (success) {
                    success = obj.getBoolean("success");
                    JSONArray details = obj.getJSONArray("details");
                    JSONObject c = details.getJSONObject(0);
                    //saving price and discount
                    price[0] = c.getString("price1");
                    discount[0] = c.getString("discount1");
                    price[1] = c.getString("price2");
                    discount[1] = c.getString("discount2");
                    price[2] = c.getString("price3");
                    discount[2] = c.getString("discount3");
                    price[3] = c.getString("price4");
                    discount[3] = c.getString("discount4");
                    product.setPrice(price);
                    product.setDiscount(discount);
                    lastDate = c.getString("dDate");
                } else {
                    //set default value to price and discount;
                    product.setDiscount(discount);
                    product.setPrice(price);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setPriceView(product);
        }
    }

}
