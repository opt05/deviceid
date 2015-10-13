package com.cwlarson.deviceid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cwlarson.deviceid.data.Device;
import com.cwlarson.deviceid.data.Hardware;
import com.cwlarson.deviceid.data.Network;
import com.cwlarson.deviceid.data.Software;
import com.cwlarson.deviceid.util.MyAdapter;

public class TabFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String TAG = "TabFragment";
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    public static TabFragment newInstance(int tabInteger) {
        TabFragment dtf = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("tab", tabInteger);
        dtf.setArguments(bundle);
        return dtf;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (getArguments().getInt("tab")){
            case 0:
                new Device(getActivity()).setDeviceTiles(mAdapter);
                break;
            case 1:
                new Network(getActivity()).setNetworkTiles(mAdapter);
                break;
            case 2:
                new Software(getActivity()).setSoftwareTiles(mAdapter);
                break;
            case 3:
                new Hardware(getActivity()).setHardwareTiles(mAdapter);
                break;
            default:
                new Device(getActivity()).setDeviceTiles(mAdapter);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tabs, container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.device_recycler_view);

        // use a linear layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(),getResources().getInteger(R.integer.grid_layout_columns));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(getActivity(), (TextView)v.findViewById(R.id.textview_recyclerview_no_items));
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // SearchView
        MainActivity.searchItem = menu.findItem(R.id.action_search);
        MainActivity.searchView = (SearchView) MenuItemCompat.getActionView(MainActivity.searchItem);
        if (MainActivity.searchView != null) {
            MainActivity.searchView.setQueryHint("Search here");
            MainActivity.searchView.setOnQueryTextListener(this);
            MainActivity.searchView.setMaxWidth(4000);
            MainActivity.searchView.setIconifiedByDefault(true);
            MainActivity.searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
                    return false;
                }
            });
        }
        menu.findItem(R.id.action_search).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //handled by AppCompat (nothing to do here)
                return true;
            case R.id.action_filter:
                if (!((MyAdapter) mRecyclerView.getAdapter()).isFiltered()) {
                    ((MyAdapter) mRecyclerView.getAdapter()).setFilterFavorite();
                } else {
                    ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //Log.d(TAG, query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.d(TAG, s);
        if(s.length()>0) {
            ((MyAdapter) mRecyclerView.getAdapter()).setSearch(s);
        } else {
            ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
        }
        return false;
    }
}
