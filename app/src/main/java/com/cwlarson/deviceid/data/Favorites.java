package com.cwlarson.deviceid.data;

import android.app.Activity;

import com.cwlarson.deviceid.util.DataUtil;
import com.cwlarson.deviceid.util.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class Favorites {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "Favorites";
    private final Activity activity;
    private final List<Item> mItems= new ArrayList<>();

    public Favorites(Activity activity){
        this.activity = activity;
    }

    public void setFavoritesTiles(MyAdapter mAdapter){
        mItems.addAll(new Device(activity).setDeviceTiles(null));
        mItems.addAll(new Network(activity).setNetworkTiles(null));
        mItems.addAll(new Software(activity).setSoftwareTiles(null));
        mItems.addAll(new Hardware(activity).setHardwareTiles(null));
        //Get all the current favorites on first load
        DataUtil dataUtil = new DataUtil(activity);
        for (Item item : mItems) {
            if (dataUtil.isFavoriteItem(item.getTitle()))
                mAdapter.add(item);
        }
    }
}
