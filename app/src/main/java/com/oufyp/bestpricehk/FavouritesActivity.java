package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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


public class FavouritesActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = FavouritesActivity.class.getSimpleName();
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1000;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Context mContext = this;
    private DatabaseHandler db;
    private ArrayList<FavProduct> favList = new ArrayList<>();
    private FavProductAdapter adapter;
    private ListView lv;
    private View loadingView;
    private View headerView;
    private Boolean success = false;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        db = DatabaseHandler.getInstance(mContext);
        loadingView = getLayoutInflater().inflate(R.layout.loading, null, false);
        adapter = new FavProductAdapter(mContext, favList);
        headerView = getLayoutInflater().inflate(R.layout.view_best_price, null, false);
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
        if (servicesConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
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

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(), "Location Updates");
            }
            return false;
        }
    }

    public void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
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
        final TextView storeLocation = (TextView) headerView.findViewById(R.id.store_location);
        Spinner spinner = (Spinner) headerView.findViewById(R.id.spinner);
        List<String> shopOption = new ArrayList<>();
        storeLocation.setVisibility(View.GONE);
        shopOption.add("Shop in ParknShop");
        shopOption.add("Shop in Wellcome");
        shopOption.add("Shop in Jusco");
        shopOption.add("Shop in Market Place");
        shopOption.add("Shop with best price");
        shopOption.add("Shop in nearest store");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, shopOption);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(4);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 5) {
                    storeLocation.setVisibility(View.VISIBLE);
                    findNearestStore(mCurrentLocation, 500);
                } else {
                    storeLocation.setVisibility(View.GONE);
                    updateFavListView(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lv.addHeaderView(headerView, null, false);
    }

    public void updateFavListView(int displayflag) {
        double totaPrice = 0.0;
        double subtotal;
        int counter = 0;
        TextView tv = (TextView) headerView.findViewById(R.id.total_price);
        for (FavProduct favProduct : favList) {
            favProduct.setDisplayFlag(displayflag);
            subtotal = favProduct.getSubTotal(displayflag);
            if (subtotal > 0) {
                counter++;
            }
            totaPrice += subtotal;
        }
        tv.setText(getString(R.string.fav_total_price, totaPrice, counter, favList.size()));
        adapter.notifyDataSetChanged();
    }

    public void findNearestStore(Location currentLocation, int radius) {
        String url = String.format("https://maps.googleapis.com/maps/api/place/search/json?location=%f,%f&radius=%d&keyword=wellcome|parknshop|jusco|market&types=grocery_or_supermarket|department_store&sensor=true&key=%s",
                currentLocation.getLatitude(), currentLocation.getLongitude(), radius, getString(R.string.api_key));
        final TextView tv = (TextView) headerView.findViewById(R.id.store_location);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.locate_store));
        pDialog.show();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                JSONArray results;
                JSONObject result;
                //JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                //LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                //String id = "";
                String name = "";
                String vicinity = "";
                int displayFlag = 0;
                try {
                    results = obj.getJSONArray("results");
                    result = results.getJSONObject(0);
                    name = result.getString("name");
                    vicinity = result.getString("vicinity");
                    displayFlag = 0;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (name.toLowerCase().contains("parknshop")) {
                    displayFlag = 0;
                } else if (name.toLowerCase().contains("wellcome")) {
                    displayFlag = 1;
                } else if (name.toLowerCase().contains("jusco")) {
                    displayFlag = 2;
                } else if (name.toLowerCase().contains("market place")) {
                    displayFlag = 3;
                }
                tv.setText("Store: " + name + "\nAddress: " + vicinity);
                updateFavListView(displayFlag);
                pDialog.cancel();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                pDialog.cancel();

            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
