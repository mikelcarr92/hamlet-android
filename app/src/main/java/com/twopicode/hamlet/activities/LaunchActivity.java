package com.twopicode.hamlet.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.twopicode.hamlet.R;

/****************************************
 * Created by michaelcarr on 10/11/15.
 ****************************************/
public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent;

        if (preferences.contains(getResources().getString(R.string.prefkey_token))) {
            intent = new Intent(LaunchActivity.this, MainActivity.class);
        } else {
            intent = new Intent(LaunchActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();

    }


}
