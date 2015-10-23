package com.cwlarson.deviceid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cwlarson.deviceid.data.Device;
import com.cwlarson.deviceid.data.Favorites;
import com.cwlarson.deviceid.data.Hardware;
import com.cwlarson.deviceid.data.Item;
import com.cwlarson.deviceid.data.Network;
import com.cwlarson.deviceid.data.Software;
import com.cwlarson.deviceid.util.DataUtil;
import com.cwlarson.deviceid.util.MyAdapter;

public class TabFragment extends Fragment {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = "TabFragment";
    private MyAdapter mAdapter;
    private BroadcastReceiver mReceiver;

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
            case 4:
                new Favorites(getActivity()).setFavoritesTiles(mAdapter);
                break;
            default:
                new Device(getActivity()).setDeviceTiles(mAdapter);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getArguments().getInt("tab") == 4)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments().getInt("tab")==4) {
            mReceiver = new DataUpdateReceiver();
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter(DataUtil.BROADCAST_UPDATE_FAV));
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tabs, container,false);

        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.device_recycler_view);

        // use a linear layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(),getResources().getInteger(R.integer.grid_layout_columns));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(getActivity(), (TextView)v.findViewById(R.id.textview_recyclerview_no_items));
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    public class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("ACTION").equals("REMOVE")) {
                Log.d(TAG, "REMOVE");
                Item item = new Item(context);
                item.setTitle(intent.getStringExtra("ITEM_TITLE"));
                item.setSubTitle(intent.getStringExtra("ITEM_SUB"));
                mAdapter.remove(item);
            }else if(intent.getStringExtra("ACTION").equals("ADD")){
                Log.d(TAG,"ADD");
                Item item = new Item(context);
                item.setTitle(intent.getStringExtra("ITEM_TITLE"));
                item.setSubTitle(intent.getStringExtra("ITEM_SUB"));
                mAdapter.add(item);
            }
        }
    }
}
