package com.cwlarson.deviceid.data;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.MyAdapter;

import java.util.Arrays;

public class Device {
    String TAG = "Network";
    private Activity activity;
    private Context context;

    public Device(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void setDeviceTiles(MyAdapter mAdapter){
        mAdapter.addItem(Arrays.asList("IMEI / MEID", getIMEI()));
        mAdapter.addItem(Arrays.asList("Model Number", getDeviceModel()));
        mAdapter.addItem(Arrays.asList("Serial", getSerial()));
    }

    private String getIMEI() {
        String imei="";
        try {
            // Request permission for IMEI/MEID for Android M+
            if (new Permissions(activity).hasPermission(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imei = telephonyManager.getDeviceId();
            } else {
                imei = context.getResources().getString(R.string.phone_permission_denied);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getIMEI");
        }
        return imei == null || imei.equals("") ? context.getResources().getString(R.string.not_found) : imei;
    }

    private String getDeviceModel(){
        String deviceModel="",manufacturer,product,model;
        try {
            manufacturer = (Build.MANUFACTURER == null || Build.MANUFACTURER.length() == 0) ? "" : Build.MANUFACTURER;
            product = (Build.PRODUCT == null || Build.PRODUCT.length() == 0) ? "" : Build.PRODUCT;
            model = (Build.MODEL == null || Build.MODEL.length() == 0) ? "" : Build.MODEL;
            if (model.startsWith(manufacturer)) {
                deviceModel = model + " (" + product + ")";
            } else {
                deviceModel = manufacturer + " " + model + " (" + product + ")";
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getDeviceModel");
        }
        deviceModel=Character.isUpperCase(deviceModel.charAt(0)) ? deviceModel : Character.toUpperCase(deviceModel.charAt(0))+deviceModel.substring(1);

        return deviceModel.equals("") ? context.getResources().getString(R.string.not_found) : deviceModel;
    }

    private String getSerial() {
        return Build.SERIAL==null || Build.SERIAL.equals("") ? context.getResources().getString(R.string.not_found) : Build.SERIAL;
    }
}
