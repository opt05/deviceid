package com.cwlarson.deviceid.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwlarson.deviceid.MainActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.util.MyAdapter;

public class Device {
    private final String TAG = Device.class.getSimpleName();
    private final Activity activity;
    private final Context context;
    // IDs reserved 1-5000

    public Device(Activity activity){
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void setDeviceTiles(final MyAdapter mAdapter){
        new AsyncTask<Void, Item, Void>() {
            @Override
            protected void onProgressUpdate(Item... values) {
                if(mAdapter!=null) mAdapter.add(values[0]);
            }

            @Override
            protected Void doInBackground(Void... aVoid) {
                publishProgress(getDeviceModel());
                publishProgress(getIMEI());
                publishProgress(getSerial());
                publishProgress(getAndroidID());
                return null;
            }
        }.execute();
    }

    @SuppressLint("HardwareIds")
    private Item getIMEI() {
        Item item = new Item(1,"IMEI / MEID",context.getString(R.string.not_found));
        try {
            // Request permission for IMEI/MEID for Android M+
            if (new Permissions(activity).hasPermission(MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                item.setSubTitle(telephonyManager.getDeviceId());
                item.setPermissionCode(0);
            } else {
                item.setSubTitle(context.getResources().getString(R.string.phone_permission_denied));
                item.setPermissionCode(MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.w(TAG, "Null in getIMEI");
        } finally {
            }
        return item;
    }

    private Item getDeviceModel(){
        Item item = new Item(2,"Model Number",context.getString(R.string.not_found));
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
        item.setSubTitle(Character.isUpperCase(device.charAt(0)) ? device : Character.toUpperCase(device.charAt(0))+device.substring(1));
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getSerial() {
        Item item = new Item(3,"Serial",context.getString(R.string.not_found));
        item.setSubTitle(Build.SERIAL);
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getAndroidID() {
        Item item = new Item(4,"Android/Hardware ID",context.getString(R.string.not_found));
        try {
            item.setSubTitle(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getAndroidID");
        }
        return item;
    }
}
