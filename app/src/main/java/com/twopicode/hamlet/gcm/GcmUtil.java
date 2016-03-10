package com.twopicode.hamlet.gcm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.twopicode.hamlet.Util.Constants;
import com.twopicode.hamlet.Util.Util;

import java.io.IOException;

public class GcmUtil {

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String registrationId = prefs.getString(Constants.PREFS_GCM_REGISTRATION_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(Constants.TAG_GCM, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(Constants.PREFS_CURRENT_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Util.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Constants.TAG_GCM, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    @SuppressLint("CommitPrefEdits")
    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int appVersion = Util.getAppVersion(context);
        Log.i(Constants.TAG_GCM, "Saving registration ID on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_GCM_REGISTRATION_ID, regId);
        editor.putInt(Constants.PREFS_CURRENT_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public static void registerGCMInBackground(final Context context, final Runnable onSuccess, final Runnable onFail) {
        AsyncTask task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... v) {
                String msg = "";
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    String regId = gcm.register(Constants.GCM_PROJECT_ID);
                    Log.d(Constants.TAG_GCM, "Stored GCM Registration ID: " + regId);
                    GcmUtil.storeRegistrationId(context, regId);
                    if(onSuccess != null)
                        onSuccess.run();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // TODO: If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    if(onFail != null)
                        onFail.run();
                }
                return msg;
            }
        }.execute();
    }
}