package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.model.FavProduct;
import com.oufyp.bestpricehk.model.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FavProductsLocation extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = MapActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1000;
    private static final int MARKER_PK = R.drawable.marker_pk;
    private int pinIcon = MARKER_PK;
    private static final int MARKER_WELLCOME = R.drawable.marker_wellcome;
    private static final int MARKER_JUSCO = R.drawable.marker_ju;
    private static final int MARKER_MP = R.drawable.marker_mp;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<FavProduct> favList = new ArrayList<>();

    private boolean firstTime = true;
    private GoogleMap map;
    private int storeType;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        favList = (ArrayList<FavProduct>) intent.getSerializableExtra("FAVLIST");
        if (servicesConnected()) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            // Get a handle to the Map Fragment
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMyLocationEnabled(true);
        }
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return getInfoView(marker);
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //Store store = markerStoreMap.get(marker);
                //Toast.makeText(getApplicationContext(), " id: " + store.getId(), Toast.LENGTH_SHORT).show();
            }
        });
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
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_fav_products_location, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15));
        if (firstTime) {
            showStore(mCurrentLocation, 500, favList.get(0).getDisplayFlag());
            firstTime = false;
        }
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
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    public void showStore(Location currentLocation, int radius, final int displayFlag) {
        String url = String.format("https://maps.googleapis.com/maps/api/place/search/json?location=%f,%f&radius=%d&keyword=wellcome|parknshop|jusco|market&types=grocery_or_supermarket|department_store&sensor=true&key=%s",
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                radius, getString(R.string.api_key));
        Log.d(TAG, "url:" + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    JSONArray results = obj.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                        LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                        String id = result.getString("place_id");
                        String name = result.getString("name");
                        String vicinity = result.getString("vicinity");
                        switch (displayFlag) {
                            case 0:
                                if (name.toLowerCase().contains("parknshop")) {
                                    storeType = 0;
                                }
                                break;
                            case 1:
                                if (name.toLowerCase().contains("wellcome")) {
                                    storeType = 1;
                                }
                                break;
                            case 2:
                                if (name.toLowerCase().contains("jusco")) {
                                    storeType = 2;
                                }
                                break;
                            case 3:
                                if (name.toLowerCase().contains("market place")) {
                                    storeType = 3;
                                }
                                break;
                            case 4:
                                if (name.toLowerCase().contains("parknshop")) {
                                    storeType = 0;
                                } else if (name.toLowerCase().contains("wellcome")) {
                                    storeType = 1;
                                } else if (name.toLowerCase().contains("jusco")) {
                                    storeType = 2;
                                } else if (name.toLowerCase().contains("market place")) {
                                    storeType = 3;
                                }
                        }
                        placeMarker(new Store(id, name, latLng, vicinity, storeType));
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
        AppController.getInstance().addToRequestQueue(jsObjRequest);

    }

    public Marker placeMarker(Store store) {
        switch (store.getType()) {
            case 0:
                pinIcon = MARKER_PK;
                break;
            case 1:
                pinIcon = MARKER_WELLCOME;
                break;
            case 2:
                pinIcon = MARKER_JUSCO;
                break;
            case 3:
                pinIcon = MARKER_MP;
        }
        return map.addMarker(new MarkerOptions()
                .title(store.getName())
                .snippet(store.getAddress())
                .position(store.getLatLong())
                .icon(BitmapDescriptorFactory.fromResource(pinIcon)));

    }

    public View getInfoView(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.info_window, null);
        String tittle = marker.getTitle();
        String snippet = marker.getSnippet();
        TextView tittleTV = ((TextView) view.findViewById(R.id.store_name));
        TextView snippetTV = ((TextView) view.findViewById(R.id.store_location));
        TextView nameTV = (TextView) view.findViewById(R.id.name_tv);
        TextView priceTV = ((TextView) view.findViewById(R.id.price_tv));
        TextView discountTV = ((TextView) view.findViewById(R.id.discount_tv));
        String productList = "Your favourite products:\n\n";
        tittleTV.setText(tittle);
        nameTV.setVisibility(View.GONE); // hide name TV
        discountTV.setVisibility(View.GONE); // hide discount TV
        snippetTV.setText(getString(R.string.tv_location, snippet));
        if (tittle.toLowerCase().contains("park")) {
            if (favList.isEmpty()) {
                productList += "None.\n";
            } else {
                productList = getProductInfo(0);
            }
        } else if (tittle.toLowerCase().contains("well")) {
            if (favList.isEmpty()) {
                productList += "None.\n";
            } else {
                productList = getProductInfo(1);
            }
        } else if (tittle.toLowerCase().contains("jusco")) {
            if (favList.isEmpty()) {
                productList += "None.\n";
            } else {
                productList = getProductInfo(2);
            }
        } else if (tittle.toLowerCase().contains("market place")) {
            if (favList.isEmpty()) {
                productList += "None.\n";
            } else {
                productList = getProductInfo(3);
            }
        }
        priceTV.setText(productList);

        return view;
    }

    public String getProductInfo(int displayFlag) {
        String info = "";
        for (FavProduct favProduct : favList) {
            double unitPrice = favProduct.getUnitPrice(displayFlag);
            if (unitPrice == 0.0) {
                info += this.getResources().getString(R.string.no_price_string, favProduct.getName(), favProduct.getQty());
            } else {
                info += this.getResources().getString(R.string.price_string, favProduct.getName(), favProduct.getQty(), favProduct.getSubTotal(displayFlag));
            }
        }
        return info;
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