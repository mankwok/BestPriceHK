package com.oufyp.bestpricehk;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.app.UserFunctions;
import com.oufyp.bestpricehk.database.DatabaseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class ProfileFragment extends Fragment {
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private UserFunctions uf = new UserFunctions();
    private DatabaseHandler db;
    private TextView favTV;
    private TextView shareTV;
    private TextView usernameTV;
    private TextView rankTV;
    private boolean success = false;
    private String countFav;
    private String countShare;
    private ProgressDialog pDialog;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (uf.isUserLoggedIn(getActivity())) {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);
            db = DatabaseHandler.getInstance(getActivity());
            usernameTV = (TextView) view.findViewById(R.id.username);
            favTV = (TextView) view.findViewById(R.id.count_favourite);
            shareTV = (TextView) view.findViewById(R.id.count_share);
            rankTV = (TextView) view.findViewById(R.id.tv_rank);
            HashMap<String, String> user = db.getUserDetails();
            usernameTV.setText(user.get("name"));
            setRankTV();
            RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.share);
            layout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), DisplayShares.class);
                            intent.putExtra("TAG", TAG);
                            getActivity().startActivity(intent);
                        }
                    }
            );
            RelativeLayout favLayout = (RelativeLayout) view.findViewById(R.id.favourite);
            favLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), FavouritesActivity.class);
                            getActivity().startActivity(intent);
                        }
                    }
            );
            Button logoutButton = (Button) view.findViewById(R.id.button_logout);
            logoutButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            uf.logoutUser(getActivity());
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        }
                    }
            );
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.message_fetch_info));
            pDialog.show();
            getUserCount();
            return view;
        } else {
            View view = inflater.inflate(R.layout.not_logged_in, container, false);
            TextView tv = (TextView) view.findViewById(R.id.massage_not_login);
            tv.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            uf.logoutUser(getActivity());
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            getActivity().startActivity(intent);
                        }
                    }
            );
            return view;
        }
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

    public void getUserCount() {
        HashMap<String, String> user = db.getUserDetails();
        String uid = user.get("uid");
        String url = String.format("http://101.78.220.131:8909/bestpricehk/user_count.php?uid=%s", uid);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                hidePDialog();
                try {
                    success = obj.getBoolean("success");
                    if (success) {
                        JSONObject count = obj.getJSONObject("count");
                        countShare = count.getString("count_share");
                        countFav = count.getString("count_fav");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                favTV.setText(countFav);
                shareTV.setText(countShare);
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

    public void setRankTV() {
        if (uf.isGoldMember(getActivity())) {
            rankTV.setText(getString(R.string.gold_member));
        } else if (uf.isSilverMember(getActivity())) {
            rankTV.setText(getString(R.string.silver_member));
        } else {
            rankTV.setText(getString(R.string.normal_member));
        }
    }
}
