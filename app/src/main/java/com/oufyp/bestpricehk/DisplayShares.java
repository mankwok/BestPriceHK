package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.adapter.ShareListAdapter;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.app.UserFunctions;
import com.oufyp.bestpricehk.model.Share;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DisplayShares extends Activity {
    public static final String TAG = DisplayShares.class.getSimpleName();
    private Context mContext = this;
    private UserFunctions uf = new UserFunctions();
    private ArrayList<Share> sharesList = new ArrayList<>();
    private ListView lv;
    private ShareListAdapter adapter;
    private View loadingView;
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_shares);
        lv = (ListView) findViewById(R.id.listView);
        loadingView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loading, null, false);
        adapter = new ShareListAdapter(mContext, sharesList);
        lv.addFooterView(loadingView, null, false);
        lv.setAdapter(adapter);
        Intent intent = getIntent();
        String tag = intent.getStringExtra("TAG");
        if (tag.equals(ProfileFragment.TAG)) {
            getActionBar().setTitle(getString(R.string.label_my_shares));
            String uid = uf.getUserID(mContext);
            getMyShares(uid);
        } else if (tag.equals(DisplayProductInfo.TAG)) {
            String uid = intent.getStringExtra("UID");
            String pid = intent.getStringExtra("PID");
            getProductShares(uid, pid);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Share share = (Share) lv.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), DisplayProductInfo.class);
                intent.putExtra("TAG", TAG);
                intent.putExtra("PID", share.getPid());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_shares, menu);
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
        return super.onOptionsItemSelected(item);
    }

    public void getMyShares(String uid) {
        String url = String.format("http://101.78.220.131:8909/bestpricehk/share_api/view_shares.php?uid=%s", uid);
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    success = obj.getBoolean("success");
                    if (success) {
                        JSONArray products = obj.getJSONArray("shares");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject jsonObject = products.getJSONObject(i);
                            String pid = jsonObject.getString("pid");
                            String productName = jsonObject.getString("name");
                            String details = jsonObject.getString("details");
                            String msg = jsonObject.getString("msg");
                            String timestamp = jsonObject.getString("timestamp");
                            Share share = new Share(pid, productName, "Me", details, msg, timestamp);
                            sharesList.add(share);
                        }
                    }else{
                        View header = getLayoutInflater().inflate(R.layout.view_no_share, lv, false);
                        lv.addHeaderView(header, null, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                lv.removeFooterView(loadingView);
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

    public void getProductShares(String uid, String pid) {
        String url = String.format("http://101.78.220.131:8909/bestpricehk/share_api/view_shares.php?uid=%s&pid=%s", uid, pid);
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    success = obj.getBoolean("success");
                    if (success) {
                        JSONArray products = obj.getJSONArray("shares");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject jsonObject = products.getJSONObject(i);
                            String username = jsonObject.getString("username");
                            String pid = jsonObject.getString("pid");
                            String productName = jsonObject.getString("name");
                            String details = jsonObject.getString("details");
                            String msg = jsonObject.getString("msg");
                            String timestamp = jsonObject.getString("timestamp");
                            Share share = new Share(pid, productName, username, details, msg, timestamp);
                            sharesList.add(share);
                        }
                    }
                    else{
                        View header = getLayoutInflater().inflate(R.layout.view_no_share, lv, false);
                        lv.addHeaderView(header, null, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                lv.removeFooterView(loadingView);
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
}
