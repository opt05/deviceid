package com.cwlarson.deviceid.data;

import android.app.Activity;

import com.cwlarson.deviceid.util.MyAdapter;

public class Favorites {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "Favorites";
    private final Activity activity;

    public Favorites(Activity activity){
        this.activity = activity;
    }

    public void setFavoritesTiles(MyAdapter mAdapter){
        new Device(activity).setDeviceTiles(mAdapter,true);
        new Network(activity).setNetworkTiles(mAdapter,true);
        new Software(activity).setSoftwareTiles(mAdapter,true);
        new Hardware(activity).setHardwareTiles(mAdapter,true);
    }
}
