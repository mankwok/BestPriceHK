package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Request.Method;
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
import java.util.HashMap;


public class LoginActivity extends Activity {
    public static final String TAG = LoginActivity.class.getSimpleName();
    private Context mContext = this;
    private DatabaseHandler db;
    private UserFunctions uf = new UserFunctions();
    private Button btnLogin;
    private Button btnLinkToReg;
    private EditText inputEmail;
    private EditText inputPassword;
    private TextView errorMsg;
    private ProgressDialog pDialog;
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_RANK = "rank";
    private static String KEY_CREATED_AT = "created_at";
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = DatabaseHandler.getInstance(mContext);
        inputEmail = (EditText) findViewById(R.id.email);
        inputEmail.requestFocus();
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.button_login);
        btnLinkToReg = (Button) findViewById(R.id.button_link_reg);
        btnLogin.setOnClickListener(new LoginRequest());
        // Link to Register Screen
        btnLinkToReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private class LoginRequest implements View.OnClickListener {
        private String url;

        @Override
        public void onClick(View view) {
            errorMsg = (TextView) findViewById(R.id.massage_error);
            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();
            if (!email.equals("") && !password.equals("")) {
                pDialog = new ProgressDialog(LoginActivity.this);
                pDialog.setMessage(getString(R.string.message_login));
                pDialog.show();
                url = "http://101.78.220.131:8909/bestpricehk/login_api/index.php";
                HashMap<String, String> params = new HashMap<>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);
                CustomRequest loginRequest = new CustomRequest(Method.POST, url, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject obj) {
                        hidePDialog();
                        try {
                            if (Integer.parseInt(obj.getString(KEY_SUCCESS)) == 1) {
                                JSONObject user = obj.getJSONObject("user");
                                String uid = obj.getString(KEY_UID);
                                String username = user.getString(KEY_NAME);
                                String email = user.getString(KEY_EMAIL);
                                String rank = user.getString(KEY_RANK);
                                String createdAt = user.getString(KEY_CREATED_AT);
                                uf.logoutUser(getApplicationContext());
                                db.addUser(username, email, uid, rank, createdAt);
                                initFavList(uid);
                                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                // Close all views before launching Dashboard
                                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainActivity);
                                finish();
                            } else if (Integer.parseInt(obj.getString(KEY_ERROR)) >= 1) {
                                String msg = obj.getString(KEY_ERROR_MSG);
                                errorMsg.setText(msg);//user exist
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hidePDialog();
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), getString(R.string.message_error), Toast.LENGTH_SHORT).show();
                    }
                });
                AppController.getInstance().addToRequestQueue(loginRequest);
            } else {
                errorMsg.setText(getString(R.string.message_wrong_pw));
            }
        }
    }

    @Override
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void initFavList(String uid) {
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
                            Product product = new Product(id, name, type, brand);
                            db.addFavProduct(product);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorMsg.setText("Cannot initialize favourites list.");
            }
        });
        AppController.getInstance().addToRequestQueue(jsObjRequest);
    }
}
