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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.oufyp.bestpricehk.app.AppController;
import com.oufyp.bestpricehk.app.CustomRequest;
import com.oufyp.bestpricehk.app.UserFunctions;
import com.oufyp.bestpricehk.database.DatabaseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends Activity {
    public static final String TAG = RegisterActivity.class.getSimpleName();
    private Context mContext = this;
    private DatabaseHandler db;
    private UserFunctions uf = new UserFunctions();
    private Button btnReg;
    private Button btnLinkToLogin;
    private EditText regUserName;
    private EditText regEmail;
    private EditText regPassword;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = DatabaseHandler.getInstance(mContext);
        regUserName = (EditText) findViewById(R.id.reg_username);
        regUserName.requestFocus();
        regEmail = (EditText) findViewById(R.id.reg_email);
        regPassword = (EditText) findViewById(R.id.reg_password);
        btnReg = (Button) findViewById(R.id.button_reg);
        btnLinkToLogin = (Button) findViewById(R.id.button_link_login);
        errorMsg = (TextView) findViewById(R.id.massage_error);
        btnReg.setOnClickListener(new RegRequest());
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private class RegRequest implements View.OnClickListener {
        private String url;

        @Override
        public void onClick(View view) {
            String username = regUserName.getText().toString();
            String password = regPassword.getText().toString();
            String email = regEmail.getText().toString();
            if (validInput()) {
                pDialog = new ProgressDialog(RegisterActivity.this);
                pDialog.setMessage("Registering...");
                pDialog.show();
                url = "http://101.78.220.131:8909/bestpricehk/login_api/index.php";
                Map<String, String> params = new HashMap<>();
                params.put("tag", "register");
                params.put("email", email);
                params.put("name", username);
                params.put("password", password);
                CustomRequest regRequest = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
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
                                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                                // Close all views before launching Dashboard
                                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainActivity);
                                // Close Registration Screen
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
                AppController.getInstance().addToRequestQueue(regRequest);
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

    public boolean validInput() {
        return validUsername() && validEmail() && validPassword();
    }

    public boolean validUsername() {
        String username = regUserName.getText().toString().replaceAll("\\s", "");
        if (username.length() >= 5 && username.length() <= 10) {
            return true;
        } else {
            errorMsg.setText(getString(R.string.reg_wrong_name));
            return false;
        }
    }

    public boolean validEmail() {
        String email = regEmail.getText().toString();
        if (email.length() >= 0) {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return true;
            } else {
                errorMsg.setText(getString(R.string.reg_wrong_mail));
                return false;
            }
        } else {
            errorMsg.setText(getString(R.string.reg_wrong_mail));
            return false;
        }
    }

    public boolean validPassword() {
        String password = regPassword.getText().toString();
        if (password.length() >= 5 && password.length() <= 10) {
            return true;
        } else {
            errorMsg.setText(getString(R.string.reg_wrong_pw));
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
