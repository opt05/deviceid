package com.cwlarson.deviceid.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwlarson.deviceid.MainActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.util.MyAdapter;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteOrder;

public class Network {
    private final String TAG = Network.class.getSimpleName();
    private final Activity activity;
    private final Context context;
    private final WifiInfo mWifiConnectionInfo;
    // IDs reserved 5001-15000

    @SuppressLint("WifiManagerPotentialLeak")
    public Network(Activity activity){
        this.activity=activity;
        this.context=activity.getApplicationContext();
        this.mWifiConnectionInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
    }

    public void setNetworkTiles(final MyAdapter mAdapter){
        new AsyncTask<Void, Item, Void>() {
            @Override
            protected void onProgressUpdate(Item... values) {
                if(mAdapter!=null) mAdapter.add(values[0]);
            }

            @Override
            protected Void doInBackground(Void... aVoid) {
                publishProgress(getWifiMac());
                publishProgress(getWifiBSSID());
                publishProgress(getWifiSSID());
                publishProgress(getWifiFrequency());
                publishProgress(getWifiHiddenSSID());
                publishProgress(getWifiIpAddress());
                publishProgress(getWifiLinkSpeed());
                publishProgress(getWifiNetworkID());
                publishProgress(getWifiRSSI());
                publishProgress(getWifiHostname());
                publishProgress(getBluetoothMac());
                publishProgress(getBluetoothHostname());
                publishProgress(getSimSerial());
                publishProgress(getSimOperatorName());
                publishProgress(getSimCountry());
                publishProgress(getSimState());
                publishProgress(getPhoneNumber());
                publishProgress(getVoicemailNumber());
                publishProgress(getCellNetworkName());
                publishProgress(getCellNetworkType());
                return null;
            }
        }.execute();
    }

    @SuppressLint("HardwareIds")
    private Item getWifiMac(){
        Item item = new Item(5001,"Wi-Fi MAC Address",context.getString(R.string.not_found));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                item.setSubTitle(mWifiConnectionInfo.getMacAddress());
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.w(TAG, "Null in getWiFiMac");
            }
        } else {
            /*
             * Marshmallow has started to depreciate this method
             * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
            */
            item.setSubTitle(context.getResources().getString(R.string.no_longer_possible,"6.0"));
        }
        return item;
    }

    private Item getWifiBSSID(){
        Item item = new Item(5002,"Wi-Fi BSSID",context.getString(R.string.not_found));
        try {
            item.setSubTitle(mWifiConnectionInfo.getBSSID());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiBSSID");
        }
        return item;
    }

    private Item getWifiSSID(){
        Item item = new Item(5003,"Wi-Fi SSID",context.getString(R.string.not_found));
        try {
            item.setSubTitle(mWifiConnectionInfo.getSSID());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiSSID");
        }
        return item;
    }
    
    private Item getWifiFrequency(){
        Item item = new Item(5004,"Wi-Fi Frequency",context.getString(R.string.not_found));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                item.setSubTitle(Integer.toString(mWifiConnectionInfo.getFrequency()));
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.w(TAG, "Null in getWiFiMac");
            }
        }
        return item;
    }

    private Item getWifiHiddenSSID(){
        Item item = new Item(5005,"Wi-Fi Hidden SSID",context.getString(R.string.not_found));
        try {
            item.setSubTitle(Boolean.toString(mWifiConnectionInfo.getHiddenSSID()));
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiIpAddress(){
        Item item = new Item(5006,"Wi-Fi IP Address",context.getString(R.string.not_found));
        try {
            int ipAddress = mWifiConnectionInfo.getIpAddress();
            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ipAddress = Integer.reverseBytes(ipAddress);
            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
            item.setSubTitle(InetAddress.getByAddress(ipByteArray).getHostAddress());
        } catch (Exception e){
            Log.w(TAG, "Exception in getWiFiMac");
        }
        return item;
    }

    private Item getWifiLinkSpeed(){
        Item item = new Item(5007,"Wi-Fi Link Speed",context.getString(R.string.not_found));
        try {
            item.setSubTitle(Integer.toString(mWifiConnectionInfo.getLinkSpeed()));
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiNetworkID(){
        Item item = new Item(5008,"Wi-Fi Network ID",context.getString(R.string.not_found));
        try {
            item.setSubTitle(Integer.toString(mWifiConnectionInfo.getNetworkId()));
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiRSSI(){
        Item item = new Item(5009,"Wi-Fi RSSI",context.getString(R.string.not_found));
        try {
            item.setSubTitle(Integer.toString(mWifiConnectionInfo.getRssi()));
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiHostname(){
        Item item = new Item(5010,"Wi-Fi Hostname",context.getString(R.string.not_found));
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            item.setSubTitle(getString.invoke(null,"net.hostname").toString());
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getBluetoothMac() {
        Item item = new Item(5011,"Bluetooth MAC Address",context.getString(R.string.not_found));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    item.setSubTitle(bm.getAdapter().getAddress());
                } else {
                    item.setSubTitle(BluetoothAdapter.getDefaultAdapter().getAddress());
                }
            } catch (NullPointerException e){
                e.printStackTrace();
                Log.w(TAG, "Null in getBluetoothMac");
            }
        } else {
            /*
             * Marshmallow has started to depreciate this method
             * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
            */
            item.setSubTitle(context.getResources().getString(R.string.no_longer_possible,"6.0"));
        }
        return item;
    }

    private Item getBluetoothHostname() {
        Item item = new Item(5012,"Bluetooth Hostname",context.getString(R.string.not_found));
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                item.setSubTitle(bm.getAdapter().getName());
            } else {
                item.setSubTitle(BluetoothAdapter.getDefaultAdapter().getName());
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getSimSerial() {
        Item item = new Item(5013,"Sim Serial",context.getString(R.string.not_found));
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubTitle(telephonyManager.getSimSerialNumber());
        } catch (NullPointerException | SecurityException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    private Item getSimOperatorName() {
        Item item = new Item(5014,"Sim Operator Name",context.getString(R.string.not_found));
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubTitle(telephonyManager.getSimOperatorName());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    private Item getSimCountry() {
        Item item = new Item(5015,"Sim Country",context.getString(R.string.not_found));
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubTitle(telephonyManager.getSimCountryIso());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    private Item getSimState() {
        Item item = new Item(5016,"Sim State",context.getString(R.string.not_found));
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getSimState()){
                case TelephonyManager.SIM_STATE_ABSENT:
                    item.setSubTitle("Absent");
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    item.setSubTitle("Network Locked");
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    item.setSubTitle("PIN Required");
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    item.setSubTitle("PUK Required");
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    item.setSubTitle("Ready");
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                default:
                    item.setSubTitle("Network Unknown");
                    break;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getPhoneNumber() {
        Item item = new Item(5017,"Phone Number",context.getString(R.string.not_found));
        try {
            if (new Permissions(activity).hasPermission(MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                item.setSubTitle(telephonyManager.getLine1Number());
                item.setPermissionCode(0);
            } else {
                item.setSubTitle(context.getResources().getString(R.string.phone_permission_denied));
                item.setPermissionCode(MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return item;
    }

    private Item getVoicemailNumber() {
        Item item = new Item(5018,"Voicemail Number",context.getString(R.string.not_found));
        try {
            if (new Permissions(activity).hasPermission(MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                item.setSubTitle(telephonyManager.getVoiceMailNumber());
                item.setPermissionCode(0);
            } else {
                item.setSubTitle(context.getResources().getString(R.string.phone_permission_denied));
                item.setPermissionCode(MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneStrength");
        }
        return item;
    }

    private Item getCellNetworkName() {
        Item item = new Item(5019,"Cell Network Name",context.getString(R.string.not_found));
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubTitle(telephonyManager.getNetworkOperatorName());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return item;
    }

    private Item getCellNetworkType() {
        Item item = new Item(5020,"Cell Network Type",context.getString(R.string.not_found));
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getNetworkType()){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    item.setSubTitle("2G");
                    break;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    item.setSubTitle("3G");
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    item.setSubTitle("4G");
                    break;
                /*case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    network=context.getResources().getString(R.string.not_found);
                    break;*/
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return item;
    }
}
