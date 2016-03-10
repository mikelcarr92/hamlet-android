package com.twopicode.hamlet.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.twopicode.hamlet.R;

import java.util.Map;

/****************************************
 * Created by michaelcarr on 26/11/15.
 ****************************************/
public class Util {

    public static String token = "";

    public static int getAppVersion(Context context) {

        int versionCode = -1;

        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }

    public static String getLoginToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getResources().getString(R.string.prefkey_token), "");
    }

    public static String getBaseURL(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getResources().getString(R.string.prefkey_server_url), "");
    }

    public static String getLoginURL(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Util.getBaseURL(context));
        stringBuilder.append(context.getResources().getString(R.string.api_claim));
        stringBuilder.append("?token=");
        stringBuilder.append(Util.getLoginToken(context));
        return stringBuilder.toString();
    }

    public static String getSearchQueryURL(Context context, String searchQuery) {
        return getBaseURL(context) + context.getString(R.string.url_search) + searchQuery;
    }

}
