package com.twopicode.hamlet.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.twopicode.hamlet.R;
import com.twopicode.hamlet.Util.Util;
import com.twopicode.hamlet.gcm.GcmUtil;
import com.twopicode.hamlet.gcm.QuickstartPreferences;
import com.twopicode.hamlet.gcm.RegistrationIntentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/****************************************
 * Created by michaelcarr on 9/11/15.
 ****************************************/
public class LoginActivity extends AppCompatActivity implements Response.Listener<String>,
        Response.ErrorListener {

    private static final int HTTP_OK = 200;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";

    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;

    private ProgressDialog mProgressDialog;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameEditText = (EditText) findViewById(R.id.activity_login_username_editText);
        mPasswordEditText = (EditText) findViewById(R.id.activity_login_password_editText);
        mLoginButton = (Button) findViewById(R.id.activity_login_login_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginButton.setEnabled(false);
                loginPressed();
            }
        });

        //Debugging purposes only
        mLoginButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mUserNameEditText.setText("hamlet.administrator");
                mPasswordEditText.setText("UJnY2F6vAr7y63O");
                return true;
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Toast.makeText(LoginActivity.this, "sent token yes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "sent token error", Toast.LENGTH_SHORT).show();
                }
            }
        };


        //TODO: One of these probably works
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

//        GcmUtil.registerGCMInBackground(this, new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
    }

    private void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    private String getAuthenticateURL() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String baseURL = prefs.getString(getResources().getString(R.string.prefkey_server_url), "");
        String authenticateAPI = getString(R.string.api_authenticate);
        return baseURL + authenticateAPI;
    }

    private void showNetworkErrorDialog(int statusCode, String error) {

        mLoginButton.setEnabled(true);
        hideProgressDialog();

        String message;
        if (error == null) {
            message = getString(R.string.error_code) + statusCode;
        } else {
            message = error + " " + getString(R.string.error_code) + statusCode;
        }
        showErrorDialog(getString(R.string.login_failed_title), message);
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton(getString(R.string.neutral_dialog_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.create().show();
    }

    private void storeCredentials(String token, String expiry) {
        SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        preferencesEditor.putString(getString(R.string.prefkey_token), token);
        preferencesEditor.putString(getString(R.string.prefkey_expiry), expiry);
        preferencesEditor.apply();
    }

    private void loginPressed() {
        if (mUserNameEditText.getText().length() == 0 || mPasswordEditText.getText().length() == 0) {
            Toast.makeText(LoginActivity.this, getString(R.string.username_password_not_entered),
                    Toast.LENGTH_LONG).show();
            return;
        }
        sendLoginRequest(mUserNameEditText.getText().toString(), mPasswordEditText.getText().toString());
    }

    private void sendLoginRequest(final String username, final String password) {

        final RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(new StringRequest(Request.Method.POST, getAuthenticateURL(), this, this) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(getString(R.string.username_param), username);
                params.put(getString(R.string.password_param), password);
//                params.put("token", GcmUtil.getRegistrationId(LoginActivity.this));
                params.put("token", Util.token);
                params.put("platform", "android");
                return params;
            }
        });

        showProgressDialog(getString(R.string.signing_in));
    }

    @Override
    public void onResponse(String response) {

        try {

            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.getInt(Response.STATUS) != HTTP_OK) {
                showNetworkErrorDialog(jsonObject.getInt(Response.STATUS), jsonObject.getString(Response.ERROR));
                return;
            }

            JSONObject data = jsonObject.getJSONObject(Response.DATA);

            if (data == null) {
                showNetworkErrorDialog(0, getString(R.string.server_returned_no_data));
                return;
            }

            String token = data.getString(Response.TOKEN);
            String expiry = data.getString(Response.EXPIRES);

            storeCredentials(token, expiry);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
            showNetworkErrorDialog(0, getString(R.string.server_returned_unexpected_data));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error != null && error.networkResponse != null) {
            showNetworkErrorDialog(error.networkResponse.statusCode, getString(R.string.could_not_connect_to_server));
        } else {
            showNetworkErrorDialog(0, getString(R.string.could_not_connect_to_server));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_login_action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    public static class Response {
        public static final String STATUS = "status";
        public static final String ERROR = "error";
        public static final String DATA = "data";
        public static final String TOKEN = "token";
        public static final String EXPIRES = "expires";
    }
}