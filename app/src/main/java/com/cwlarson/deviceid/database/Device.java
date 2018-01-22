package com.cwlarson.deviceid.database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;
import com.cwlarson.deviceid.databinding.UnavailableItem;
import com.cwlarson.deviceid.databinding.UnavailablePermission;
import com.cwlarson.deviceid.databinding.UnavailableType;

class Device {
    private final String TAG = Device.class.getSimpleName();
    private final Context context;

    Device(Activity activity, AppDatabase db){
        this.context = activity.getApplicationContext();
        //Set Device Tiles
        ItemAdder itemAdder = new ItemAdder(context, db);
        itemAdder.addItems(getIMEI());
        itemAdder.addItems(getDeviceModel());
        itemAdder.addItems(getSerial());
        itemAdder.addItems(getAndroidID());
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private Item getIMEI() {
        Item item = new Item("IMEI / MEID",ItemType.DEVICE);
        try {
            // Request permission for IMEI/MEID for Android M+
            if (new Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                    .TELEPHONY_SERVICE);
                item.setSubtitle(telephonyManager.getDeviceId());
            } else {
                item.setUnavailableitem(new UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                    context.getResources().getString(R.string.phone_permission_denied),
                    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE));
            }
        } catch (Exception e) {
            Log.w(TAG, "Null in getIMEI");
        }
        return item;
    }

    private Item getDeviceModel(){
        Item item = new Item("Model Number",ItemType.DEVICE);
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
        } catch (Exception e){
            Log.w(TAG, "Null in getDeviceModel");
        }
        item.setSubtitle(Character.isUpperCase(device.charAt(0)) ? device : Character.toUpperCase(device.charAt(0))+device.substring(1));
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getSerial() {
        Item item = new Item("Serial",ItemType.DEVICE);
        item.setSubtitle(Build.SERIAL);
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getAndroidID() {
        Item item = new Item("Android/Hardware ID",ItemType.DEVICE);
        try {
            item.setSubtitle(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch (Exception e){
            Log.w(TAG, "Null in getAndroidID");
        }
        return item;
    }
}
