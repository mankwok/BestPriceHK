package com.oufyp.bestpricehk;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;


public class CommunityFragment extends Fragment {
    public static final String TAG = CommunityFragment.class.getSimpleName();
    private TextView favTV;
    private TextView shareTV;
    private TextView userCountTV;
    private ProgressDialog pDialog;
    // most favourited
    private String favPid = "";
    private String favName = "";
    // most shared
    private String sharePid = "";
    private String shareName = "";

    public CommunityFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        favTV = (TextView) view.findViewById(R.id.tv_comm_favourite);
        shareTV = (TextView) view.findViewById(R.id.tv_comm_share);
        userCountTV = (TextView) view.findViewById(R.id.tv_comm_usercount);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Fetching community information...");
        pDialog.show();
        getCommInfo();
        favTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DisplayProductInfo.class);
                intent.putExtra("TAG", TAG);
                intent.putExtra("PID", favPid);
                startActivity(intent);
            }
        });
        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DisplayProductInfo.class);
                intent.putExtra("TAG", TAG);
                intent.putExtra("PID", sharePid);
                startActivity(intent);
            }
        });
        return view;
    }

    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    public void getCommInfo() {
        String url = "http://101.78.220.131:8909/bestpricehk/community.php";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                hidePDialog();
                try {
                    JSONObject favObj = obj.getJSONArray("most_fav").getJSONObject(0);
                    favPid = favObj.getString("pid");
                    favName = favObj.getString("name");
                    JSONObject shareObj = obj.getJSONArray("most_share").getJSONObject(0);
                    sharePid = shareObj.getString("pid");
                    shareName = shareObj.getString("name");
                    String userCount = obj.getString("user_count");
                    favTV.setText(getString(R.string.comm_fav,favName));
                    shareTV.setText(getString(R.string.comm_share,shareName));
                    userCountTV.setText(getString(R.string.comm_usercount,userCount));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();
            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);

    }
}
