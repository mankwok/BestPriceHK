package com.oufyp.bestpricehk;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

public class HotFragment extends Fragment {
    public static final String TAG = HotFragment.class.getSimpleName();
    private ArrayList<Product> productsList = new ArrayList<>();
    private ListView lv;
    private ProductListAdapter adapter;
    private boolean success = false;

    public HotFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hot, container, false);
        adapter = new ProductListAdapter(getActivity(), productsList);
        lv = (ListView) view.findViewById(android.R.id.list);
        lv.setAdapter(adapter);
        getHotProducts();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Product aProduct = (Product) lv.getItemAtPosition(position);
                Intent intent = new Intent(view.getContext(), DisplayProductInfo.class);
                intent.putExtra("product", aProduct);
                startActivity(intent);
            }
        });
        return view;
    }

    public void getHotProducts() {
        String url = "http://101.78.220.131:8909/bestpricehk/hot_products.php";
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
                            String bestPrice = "none";
                            if(!jsonObject.getString("bestPrice").equals("null")) {
                                bestPrice = jsonObject.getString("bestPrice");
                            }
                            Product product = new Product(id, name, type, brand,countFav,countShare,bestPrice);
                            productsList.add(product);
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
            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }
}
