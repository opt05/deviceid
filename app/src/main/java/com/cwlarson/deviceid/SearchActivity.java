package com.cwlarson.deviceid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;

import com.cwlarson.deviceid.data.Device;
import com.cwlarson.deviceid.data.Hardware;
import com.cwlarson.deviceid.data.Network;
import com.cwlarson.deviceid.data.Software;
import com.cwlarson.deviceid.databinding.ActivitySearchBinding;
import com.cwlarson.deviceid.util.MyAdapter;

public class SearchActivity extends PermissionsActivity implements SearchView.OnQueryTextListener {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = "SearchActivity", KEY_SAVED_FILTER_CONSTRAINT ="KEY_SAVED_FILTER_CONSTRAINT";
    private MyAdapter mAdapter;
    private SearchView searchView;
    private String restoredSearch;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(searchView!=null) outState.putString(KEY_SAVED_FILTER_CONSTRAINT, searchView.getQuery().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        setSupportActionBar(binding.toolbar);
        if(getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // use a linear layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(this,getResources().getInteger(R.integer.grid_layout_columns));
        binding.contextSearch.searchRecyclerView.setLayoutManager(mLayoutManager);
        binding.contextSearch.searchRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(this, binding.contextSearch.textviewRecyclerviewNoItems);
        binding.contextSearch.searchRecyclerView.setAdapter(mAdapter);
        new Device(this).setDeviceTiles(mAdapter);
        new Network(this).setNetworkTiles(mAdapter);
        new Software(this).setSoftwareTiles(mAdapter);
        new Hardware(this).setHardwareTiles(mAdapter);
        if(savedInstanceState!=null && savedInstanceState.getString(KEY_SAVED_FILTER_CONSTRAINT)!=null) restoredSearch=savedInstanceState.getString(KEY_SAVED_FILTER_CONSTRAINT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        //Expand search view
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(this);
        // Set max width only if it is not a tablet
        if(getDisplayWidth()<600) searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mAdapter.filter("");
                return false;
            }
        });
        // Restore query on rotate of screen
        if(restoredSearch!=null && !restoredSearch.isEmpty()) {
            searchView.setQuery(restoredSearch,true);
            searchView.clearFocus();
            restoredSearch=null;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private float getDisplayWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        //Log.d(TAG, s);
        mAdapter.filter(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        //Log.d(TAG, s);
        mAdapter.filter(s);
        return true;
    }
}
