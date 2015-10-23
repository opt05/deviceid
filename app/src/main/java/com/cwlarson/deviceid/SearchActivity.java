package com.cwlarson.deviceid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.cwlarson.deviceid.data.Device;
import com.cwlarson.deviceid.data.Hardware;
import com.cwlarson.deviceid.data.Item;
import com.cwlarson.deviceid.data.Network;
import com.cwlarson.deviceid.data.Software;
import com.cwlarson.deviceid.util.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = "SearchActivity";
    private MyAdapter mAdapter;
    private final List<Item> mItemsList = new ArrayList<>();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        //Expand search view
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mAdapter.clear();
                return false;
            }
        });
        return true;
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
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        //Log.d(TAG, s);
        mAdapter.clear();
        return false;
    }
}
