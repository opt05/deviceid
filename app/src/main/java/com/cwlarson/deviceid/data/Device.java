package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class Device {
    private final String TAG = "Network";
    private final Activity activity;
    private final Context context;

    public Device(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void setDeviceTiles(MyAdapter mAdapter){
        List<Item> items = new ArrayList<>();
        items.add(getDeviceModel());
        items.add(getIMEI());
        items.add(getSerial());
        mAdapter.addAll(items);
    }

    private Item getIMEI() {
        String device="";
        try {
            // Request permission for IMEI/MEID for Android M+
            if (new Permissions(activity).hasPermission(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                device = telephonyManager.getDeviceId();
            } else {
                device = context.getResources().getString(R.string.phone_permission_denied);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getIMEI");
        }
        Item item = new Item(context);
        item.setTitle("IMEI / MEID");
        item.setSubTitle(device); 
        return item;
    }

    private Item getDeviceModel(){
        String device="",manufacturer,product,model;
        try {
            manufacturer = (Build.MANUFACTURER == null || Build.MANUFACTURER.length() == 0) ? "" : Build.MANUFACTURER;
            product = (Build.PRODUCT == null || Build.PRODUCT.length() == 0) ? "" : Build.PRODUCT;
            model = (Build.MODEL == null || Build.MODEL.length() == 0) ? "" : Build.MODEL;
            if (model.startsWith(manufacturer)) {
                device = model + " (" + product + ")";
            } else {
                device = manufacturer + " " + model + " (" + product + ")";
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getDeviceModel");
        }
        device=Character.isUpperCase(device.charAt(0)) ? device : Character.toUpperCase(device.charAt(0))+device.substring(1);
        Item item = new Item(context);
        item.setTitle("Model Number");
        item.setSubTitle(device);
        return item;
    }

    private Item getSerial() {
        String device=Build.SERIAL;
        Item item = new Item(context);
        item.setTitle("Serial");
        item.setSubTitle(device);
        return item;
    }
}
