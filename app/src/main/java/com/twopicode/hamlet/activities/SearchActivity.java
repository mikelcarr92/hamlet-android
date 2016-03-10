package com.twopicode.hamlet.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.twopicode.hamlet.R;
import com.twopicode.hamlet.Util.GsonRequest;
import com.twopicode.hamlet.Util.Util;
import com.twopicode.hamlet.listadapters.SearchListAdapter;
import com.twopicode.hamlet.searchresult.SearchResult;

import java.util.ArrayList;

/****************************************
 * Created by michaelcarr on 28/11/15.
 ****************************************/
public class SearchActivity extends AppCompatActivity implements SearchView.OnCloseListener,
        Response.Listener<SearchResult>, Response.ErrorListener, SearchView.OnQueryTextListener,
        SearchListAdapter.Callback {

    //Returning intent
    public static final String INTENT_EXTRA_URL = "INTENT_EXTRA_URL";

    //Saving instance state
    public static final String SEARCH_TERM = "SEARCH_TERM";

    //Static
    private static final String VOLLEY_TAG = "SearchRequest";

    //Instance
    private SearchListAdapter mAdapter;
    private ArrayList<Object> mListItems = new ArrayList<>();
    private String mSearchTerm = "";
    private RequestQueue mQueue;
    private TextView mInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState != null)
            mSearchTerm = savedInstanceState.getString(SEARCH_TERM, "");

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mQueue = Volley.newRequestQueue(this);
        mInfoTextView = (TextView) findViewById(R.id.activity_search_info_textView);
        ListView listView = (ListView) findViewById(R.id.activity_search_listView);
        mAdapter = new SearchListAdapter(this, mListItems);
        mAdapter.setCallback(this);
        listView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_main_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setOnCloseListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.setQuery(mSearchTerm, false);

        return true;
    }

    private void sendSearchRequest(final String searchQuery) {

        //Removes response handlers #racecondition
        mQueue.cancelAll(VOLLEY_TAG);

        String url = Util.getSearchQueryURL(this, searchQuery.replace(" ", "%20"));
        GsonRequest<SearchResult> request = new GsonRequest<>(url, SearchResult.class, null, this, this);
        request.setTag(VOLLEY_TAG);
        mQueue.add(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TERM, mSearchTerm);
    }

    /** Search View close button pressed **/
    @Override
    public boolean onClose() {
        finish();
        return true;
    }

    /** Volley error **/
    @Override
    public void onErrorResponse(VolleyError error) {
        //Client error
        mInfoTextView.setVisibility(View.VISIBLE);
        mInfoTextView.setText(getString(R.string.search_info_error_client));
    }

    /** Volley success **/
    @Override
    public void onResponse(SearchResult response) {

        mListItems.clear();

        if (response != null && response.status == 200) {

            if (response.data != null) {

                if (response.data.notes != null)
                    mListItems.addAll(response.data.notes);

                if (response.data.tasks != null)
                    mListItems.addAll(response.data.tasks);
            }

        } else { //Server didn't return happy result
            mInfoTextView.setVisibility(View.VISIBLE);
            mInfoTextView.setText(getString(R.string.search_info_error_server));
        }

        if (mListItems.size() == 0 && mSearchTerm.length() == 0) { //No search query
            mInfoTextView.setVisibility(View.VISIBLE);
            mInfoTextView.setText(getString(R.string.search_info_default));
        } else if (mListItems.size() == 0 && mSearchTerm.length() > 0){ //No results
            mInfoTextView.setVisibility(View.VISIBLE);
            mInfoTextView.setText(getString(R.string.search_info_no_results));
        } else {
            mInfoTextView.setVisibility(View.INVISIBLE);
        }

        mAdapter.notifyDataSetChanged();
    }

    /** Search View text submit **/
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    /** Search View text changed **/
    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchTerm = newText;
        sendSearchRequest(newText);
        return true;
    }

    /** ListView item click **/
    @Override
    public void onItemPressed(String url) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(INTENT_EXTRA_URL, url);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(VOLLEY_TAG);
        }
    }
}
