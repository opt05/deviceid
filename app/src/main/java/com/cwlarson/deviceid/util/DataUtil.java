package com.cwlarson.deviceid.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.cwlarson.deviceid.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DataUtil {

    String TAG = DataUtil.this.toString();
    public static final String HEADER  = "I_AM_A_HEADER_FEAR_ME";

    //titles for listview
    public String[] titles={
            "Device",
            "IMEI / MEID",
            "Model Number",
            "Android ID",

            "Network",
            "Wi-Fi MAC Address",
            "Bluetooth MAC Address",

            "Software",
            "Android Version",
            "Build Version",

            "Hardware",
            "Screen Density"
    };

    public String[] bodies(Context c) {
        return new String[]{
                HEADER,
                getIMEI(c),
                getDeviceModel(c),
                getAndroidID(c),

                HEADER,
                getWiFiMac(c),
                getBluetoothMac(c),

                HEADER,
                getAndroidVersion(c),
                getDeviceBuildVersion(c),

                HEADER,
                getDeviceScreenDensity(c)};
    }

    private String getAndroidID(Context context) {
        String android_id="";
        try {
            android_id=Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.e(TAG, "Null in getAndroidID");
        }
        return android_id==null || android_id.equals("") ? context.getResources().getString(R.string.not_found) : android_id;
    }

    private String getIMEI(Context context) {
        String imei="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "Null in getIMEI");
        }
        return imei == null || imei.equals("") ? context.getResources().getString(R.string.not_found) : imei;
    }

    private String getWiFiMac(Context context) {
        String wifiInfoMac = "";
        try {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            wifiInfoMac=wifiInfo.getMacAddress();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.e(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac == null || wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getBluetoothMac(Context context) {
        String macAddress="";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                macAddress = bm.getAdapter().getAddress();
            } else {
                macAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.e(TAG, "Null in getBluetoothMac");
        }
        return macAddress == null || macAddress.equals("")  ? context.getResources().getString(R.string.not_found) : macAddress;
    }

    enum Codenames {
        BASE, BASE_1_1,
        CUPCAKE,
        CUR_DEVELOPMENT,
        DONUT,
        ECLAIR, ECLAIR_MR1, ECLAIR_MR2,
        FROYO,
        GINGERBREAD, GINGERBREAD_MR1,
        HONEYCOMB, HONEYCOMB_MR1, HONEYCOMB_MR2,
        ICE_CREAM_SANDWICH, ICE_CREAM_SANDWICH_MR1,
        JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2,
        KITKAT, KITKAT_WATCH,
        LOLLIPOP, LOLLIPOP_MR1;

        public static Codenames getCodename()
        {
            int api = Build.VERSION.SDK_INT;
            switch (api) {
                case 1:
                    return BASE;
                case 2:
                    return BASE_1_1;
                case 3:
                    return CUPCAKE;
                case 4:
                    return DONUT;
                case 5:
                    return ECLAIR;
                case 6:
                    return ECLAIR_MR1;
                case 7:
                    return ECLAIR_MR2;
                case 8:
                    return FROYO;
                case 9:
                    return GINGERBREAD;
                case 10:
                    return GINGERBREAD_MR1;
                case 11:
                    return HONEYCOMB;
                case 12:
                    return HONEYCOMB_MR1;
                case 13:
                    return HONEYCOMB_MR2;
                case 14:
                    return ICE_CREAM_SANDWICH;
                case 15:
                    return ICE_CREAM_SANDWICH_MR1;
                case 16:
                    return JELLY_BEAN;
                case 17:
                    return JELLY_BEAN_MR1;
                case 18:
                    return JELLY_BEAN_MR2;
                case 19:
                    return KITKAT;
                case 20:
                    return KITKAT_WATCH;
                case 21:
                    return LOLLIPOP;
                case 22:
                    return LOLLIPOP_MR1;
                case 1000:
                    return CUR_DEVELOPMENT;
                default:
                    return null;
            }
        }
    }

    private String getAndroidVersion(Context context) {
        String version="",api="",versionName="";
        try {
            version = Build.VERSION.RELEASE;
            api = Integer.toString(Build.VERSION.SDK_INT);
            versionName = Codenames.getCodename().toString() == null ? "" : Codenames.getCodename().toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "Null in getAndroidVersion");
        }
        version=version+ " (" +api+") "+versionName;

        return version.equals("") ? context.getResources().getString(R.string.not_found) : version;
    }

    private String getDeviceModel(Context context){
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
            Log.e(TAG, "Null in getDeviceModel");
        }
        deviceModel=Character.isUpperCase(deviceModel.charAt(0)) ? deviceModel : Character.toUpperCase(deviceModel.charAt(0))+deviceModel.substring(1);

        return deviceModel.equals("") ? context.getResources().getString(R.string.not_found) : deviceModel;
    }

    private String getDeviceBuildVersion(Context context) {
        //Get Moto specific build version if available
        SystemProperty sp = new SystemProperty(context);
        return sp.get("ro.build.version.full")==null|| sp.get("ro.build.version.full").equals("") ? Build.DISPLAY : sp.get("ro.build.version.full");
    }

    private String getDeviceScreenDensity(Context context) {
        int density = context.getResources().getDisplayMetrics().densityDpi;
        int width = 0, height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Method mGetRawH, mGetRawW;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                width = metrics.widthPixels;
                height = metrics.heightPixels;
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IllefalArgumentException in getDeviceScreenDensity");
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
            Log.e(TAG, "NoSuchMethodException in getDeviceScreenDensity");
        }
        String sizeInPixels = " ("+Integer.toString(height)+"x"+Integer.toString(width)+")";

        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                return "LDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_MEDIUM:
                return "MDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_HIGH:
                return "HDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_XHIGH:
                return "XHDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_XXHIGH:
                return "XXHDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "XXXHDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_TV:
                return "TVDPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_400:
                return "400DPI"+sizeInPixels;
            case DisplayMetrics.DENSITY_560:
                return "560DPI"+sizeInPixels;
            default:
                return context.getResources().getString(R.string.not_found)+sizeInPixels;
        }
    }
}
