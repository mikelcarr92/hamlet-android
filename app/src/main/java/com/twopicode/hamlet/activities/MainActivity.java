package com.twopicode.hamlet.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.twopicode.hamlet.R;
import com.twopicode.hamlet.Util.Constants;
import com.twopicode.hamlet.Util.CookieMonster;
import com.twopicode.hamlet.Util.HamletMenuItem;
import com.twopicode.hamlet.Util.Util;
import com.twopicode.hamlet.gcm.GcmBroadcastReceiver;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String ARGS_SELECTED_MENU_ID = "MainActivity.ARGS_SELECTED_MENU_ID";
    private static final String ARGS_TITLE = "MainActivity.ARGS_TITLE";
    private static final int RESULT_SEARCH = 2;

    private int mSelectedMenuID = R.id.nav_item_dashboard;
    private String mTitle;
    private WebView mWebView;
    private LinearLayout mErrorView;
    private HashMap<Integer, HamletMenuItem> mMenuMap = new HashMap<>();
    private HashMap<String, Integer> mUrlToMenuMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new MaterialModule());
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mSelectedMenuID = savedInstanceState.getInt(ARGS_SELECTED_MENU_ID, R.id.nav_item_dashboard);
            mTitle = savedInstanceState.getString(ARGS_TITLE, getString(R.string.navigation_item_dashboard));
        }

        setUpMenuMap();
        setUpUrlToMenuMap();
        setTitle(mTitle);
        setUpNavigationView();
        setUpWebView();

        if (savedInstanceState == null)
            selectItem(mSelectedMenuID);
    }

    /**
     * Map of menu ids to corresponding title and URL
     */
    private void setUpMenuMap() {
        mMenuMap.put(R.id.nav_item_notebook,
                new HamletMenuItem(R.string.navigation_item_notebook, R.string.url_notebook));
        mMenuMap.put(R.id.nav_item_timetable,
                new HamletMenuItem(R.string.navigation_item_timetable, R.string.url_timetable));
        mMenuMap.put(R.id.nav_item_tasks,
                new HamletMenuItem(R.string.navigation_item_tasks, R.string.url_tasks));
        mMenuMap.put(R.id.nav_item_settings,
                new HamletMenuItem(R.string.navigation_item_settings, R.string.url_settings));
        mMenuMap.put(R.id.nav_item_notifications,
                new HamletMenuItem(R.string.navigation_item_notifications, R.string.url_notifications));
    }

    private void setUpUrlToMenuMap() {
        mUrlToMenuMap.put(getString(R.string.url_notebook).split("/")[1], R.id.nav_item_notebook);
        mUrlToMenuMap.put(getString(R.string.url_tasks).split("/")[1], R.id.nav_item_tasks);
    }

    private void setUpNavigationView() {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.activity_main_navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(mSelectedMenuID).setChecked(true);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = String.valueOf(title);
        super.setTitle(title);
    }

    private void selectItem(int itemID) {

        checkMenuItem(itemID);

        switch (itemID) {
            case R.id.nav_item_dashboard:
                mWebView.loadUrl(Util.getLoginURL(this));
                setTitle(getString(R.string.navigation_item_dashboard));
                break;

            default:
                mWebView.loadUrl(Util.getBaseURL(this) + getString(mMenuMap.get(itemID).urlResource));
                setTitle(getString(mMenuMap.get(itemID).titleResource));
                break;
        }
    }

    @SuppressWarnings("all")
     void setUpWebView() {

        //View that shows up when there is problem with the connection
        mErrorView = (LinearLayout) findViewById(R.id.activity_main_error_layout);

        //Button which is part of the error view
        findViewById(R.id.activity_main_reload_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.reload();
            }
        });

        mWebView = (WebView) findViewById(R.id.content_main_webView);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                findViewById(R.id.activity_main_progress).setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.INVISIBLE);
                mErrorView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                findViewById(R.id.activity_main_progress).setVisibility(View.GONE);
                CookieMonster.setCookieFromWebView(CookieManager.getInstance().getCookie(url));
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                if (mErrorView.getVisibility() == View.INVISIBLE)
                    mWebView.setVisibility(View.VISIBLE);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mWebView.setVisibility(View.INVISIBLE);
                mErrorView.setVisibility(View.VISIBLE);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
            }

        });

        mWebView.getSettings().setUserAgentString(Constants.WEBVIEW_USER_AGENT);
        mWebView.getSettings().setJavaScriptEnabled(true);
    }

    private void doLogout() {
        SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        preferencesEditor.remove(getString(R.string.prefkey_token));
        preferencesEditor.apply();
        Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_main_action_refresh:
                mWebView.reload();
                break;

            case R.id.menu_main_search:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, RESULT_SEARCH);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SEARCH:

                if (data != null && data.hasExtra(SearchActivity.INTENT_EXTRA_URL)) {

                    String url = data.getStringExtra(SearchActivity.INTENT_EXTRA_URL);
                    mWebView.loadUrl(Util.getBaseURL(MainActivity.this) + url);

                    String[] splitURL = url.split("/");

                    if (splitURL.length > 0) {
                        String urlKey = splitURL[1];
                        int menuID = mUrlToMenuMap.get(urlKey);
                        checkMenuItem(menuID);
                        setTitle(getString(mMenuMap.get(menuID).titleResource));
                    }
                }

                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
        outState.putInt(ARGS_SELECTED_MENU_ID, mSelectedMenuID);
        outState.putString(ARGS_TITLE, mTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    private void checkMenuItem(int id) {
        ((NavigationView)findViewById(R.id.activity_main_navigation_view)).setCheckedItem(id);
        mSelectedMenuID = id;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id) {

            case R.id.nav_item_dashboard:
            case R.id.nav_item_notebook:
            case R.id.nav_item_timetable:
            case R.id.nav_item_tasks:
            case R.id.nav_item_settings:
            case R.id.nav_item_notifications:

                if (mSelectedMenuID != id)
                    selectItem(id);

                break;

            case R.id.nav_item_logout:
                doLogout();
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}