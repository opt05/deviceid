package com.cwlarson.deviceid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.cwlarson.deviceid.data.Device;
import com.cwlarson.deviceid.data.Hardware;
import com.cwlarson.deviceid.data.Item;
import com.cwlarson.deviceid.data.Network;
import com.cwlarson.deviceid.data.Software;
import com.cwlarson.deviceid.util.DataUtil;
import com.cwlarson.deviceid.util.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = "SearchActivity", KEY_SAVED_FILTER_CONSTRAINT ="KEY_SAVED_FILTER_CONSTRAINT";
    private MyAdapter mAdapter;
    private BroadcastReceiver mReceiver;
    private final List<Item> mItemsList = new ArrayList<>();
    private SearchView searchView;
    private String restoredSearch;
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(searchView!=null) outState.putString(KEY_SAVED_FILTER_CONSTRAINT, searchView.getQuery().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        View v = findViewById(android.R.id.content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.search_recycler_view);

        // use a linear layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(this,getResources().getInteger(R.integer.grid_layout_columns));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(this, (TextView)v.findViewById(R.id.textview_recyclerview_no_items));
        mRecyclerView.setAdapter(mAdapter);

        mItemsList.addAll(new Device(this).setDeviceTiles(null));
        mItemsList.addAll(new Network(this).setNetworkTiles(null));
        mItemsList.addAll(new Software(this).setSoftwareTiles(null));
        mItemsList.addAll(new Hardware(this).setHardwareTiles(null));

        mReceiver = new DataUpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(DataUtil.BROADCAST_UPDATE_FAV));
        if(savedInstanceState!=null && savedInstanceState.getString(KEY_SAVED_FILTER_CONSTRAINT)!=null) restoredSearch=savedInstanceState.getString(KEY_SAVED_FILTER_CONSTRAINT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
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
                mAdapter.clear();
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
        mAdapter.clear();
        if(s.length()>0) {
            for (Item item : mItemsList) {
                if (item.getTitle().toLowerCase().contains(s) || item.getSubTitle().toLowerCase().contains(s))
                    mAdapter.add(item);
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        //Log.d(TAG, s);
        mAdapter.clear();
        return true;
    }

    public class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("ACTION").equals("REMOVE")) {
                Log.d(TAG, "REMOVE");
                Item item = new Item(context);
                item.setTitle(intent.getStringExtra("ITEM_TITLE"));
                item.setSubTitle(intent.getStringExtra("ITEM_SUB"));
                mAdapter.setFavStar(item);
            }else if(intent.getStringExtra("ACTION").equals("ADD")){
                Log.d(TAG,"ADD");
                Item item = new Item(context);
                item.setTitle(intent.getStringExtra("ITEM_TITLE"));
                item.setSubTitle(intent.getStringExtra("ITEM_SUB"));
                mAdapter.setFavStar(item);
            }
        }
    }
}
