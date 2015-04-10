package com.oufyp.bestpricehk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.app.CustomRequest;
import com.oufyp.bestpricehk.database.DatabaseHandler;
import com.oufyp.bestpricehk.model.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShareActivity extends Activity {
    public static final String TAG = ShareActivity.class.getSimpleName();
    private DatabaseHandler db = new DatabaseHandler(this);
    private TextView wordCount;
    private EditText message;
    private Product product;
    private String[] prices;
    private String[] discounts;
    private Context mContext = this;
    private boolean success = false;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Intent intent = getIntent();
        product = (Product) intent.getExtras().getSerializable("PRODUCT");
        TextView description = (TextView) findViewById(R.id.product_desc);
        description.setText(product.getDescription());
        prices = product.getPrice();
        discounts = product.getDiscount();
        message = (EditText) findViewById(R.id.message);
        wordCount = (TextView) findViewById(R.id.word_count);
        message.requestFocus();
        TextWatcher mTextEditorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wordCount.setText(String.valueOf(s.length()));
                if (s.length() > 50) {
                    wordCount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                } else {
                    wordCount.setTextColor(getResources().getColor(android.R.color.black));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        message.addTextChangedListener(mTextEditorWatcher);
        spinner = (Spinner) findViewById(R.id.spinner);
        List<String> priceList = new ArrayList<>();
        priceList.add("ParknShop price: $" + prices[0] + ", discount: " + discounts[0]);
        priceList.add("Wellcome price: $" + prices[1] + ", discount: " + discounts[1]);
        priceList.add("Jusco price: $" + prices[2] + ", discount: " + discounts[2]);
        priceList.add("Market Place Price: $" + prices[3] + ", discount: " + discounts[3]);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priceList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send) {
            if (message.length() > 50) {
                Toast toast = Toast.makeText(this, getString(R.string.share_limit), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                sendShare();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendShare() {
        HashMap<String, String> user = db.getUserDetails();
        String uid = user.get("uid");
        String url = "http://101.78.220.131:8909/bestpricehk/share_api/";
        Map<String, String> params = new HashMap<>();
        params.put("tag", "add");
        params.put("uid", uid);
        params.put("pid", product.getId());
        params.put("details", spinner.getSelectedItem().toString());
        params.put("msg",message.getText().toString());
        CustomRequest request = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                try {
                    success = obj.getBoolean("success");
                    if (success) {
                        Toast.makeText(mContext, getString(R.string.message_shared), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(mContext, getString(R.string.message_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(mContext, getString(R.string.message_error), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(request);
    }
}
