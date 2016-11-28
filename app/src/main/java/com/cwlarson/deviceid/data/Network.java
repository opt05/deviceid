package com.cwlarson.deviceid.data;

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

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.util.DataUtil;
import com.cwlarson.deviceid.util.MyAdapter;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteOrder;

public class Network {
    private final String TAG = "Network";
    private final Activity activity;
    private final Context context;
    private final WifiInfo mWifiConnectionInfo;
    private DataUtil dataUtil;

    public Network(Activity activity){
        this.activity=activity;
        this.context=activity.getApplicationContext();
        this.mWifiConnectionInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        this.dataUtil = new DataUtil(activity);
    }

    public void setNetworkTiles(final MyAdapter mAdapter, final boolean favsOnly){
        new AsyncTask<Void, Item, Void>() {
            @Override
            protected void onProgressUpdate(Item... values) {
                if(mAdapter!=null && (!favsOnly || dataUtil.isFavoriteItem(values[0].getTitle()))) {
                    mAdapter.add(values[0]);
                }
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

    public void setNetworkTiles(final MyAdapter mAdapter, final String searchString) {
        new AsyncTask<Void, Item, Void>() {
            @Override
            protected void onProgressUpdate(Item... values) {
                if(mAdapter!=null && values[0].matchesSearchText(searchString,activity)) {
                    mAdapter.add(values[0]);
                }
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

    private Item getWifiMac(){
        String network = "";
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                network = mWifiConnectionInfo.getMacAddress();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.w(TAG, "Null in getWiFiMac");
            }
        } else {
            /*
             * Marshmallow has started to depreciate this method
             * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
            */
            network=context.getResources().getString(R.string.no_longer_possible,"6.0");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi MAC Address");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiBSSID(){
        String network = "";
        try {
            network = mWifiConnectionInfo.getBSSID();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiBSSID");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi BSSID");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiSSID(){
        String network = "";
        try {
            network = mWifiConnectionInfo.getSSID();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiSSID");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi SSID");
        item.setSubTitle(network);
        return item;
    }
    
    private Item getWifiFrequency(){
        String network = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                network = Integer.toString(mWifiConnectionInfo.getFrequency());
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.w(TAG, "Null in getWiFiMac");
            }
        } else {
            network = context.getResources().getString(R.string.not_found);
        }
        Item item = new Item();
        item.setTitle("Wi-Fi Frequency");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiHiddenSSID(){
        String network = "";
        try {
            network = Boolean.toString(mWifiConnectionInfo.getHiddenSSID());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi Hidden SSID");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiIpAddress(){
        String network = "";
        try {
            int ipAddress = mWifiConnectionInfo.getIpAddress();
            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ipAddress = Integer.reverseBytes(ipAddress);
            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
            network = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (Exception e){
            Log.w(TAG, "Exception in getWiFiMac");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi IP Address");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiLinkSpeed(){
        String network= "";
        try {
            network = Integer.toString(mWifiConnectionInfo.getLinkSpeed());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi Link Speed");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiNetworkID(){
        String network = "";
        try {
            network = Integer.toString(mWifiConnectionInfo.getNetworkId());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi Network ID");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiRSSI(){
        String network = "";
        try {
            network = Integer.toString(mWifiConnectionInfo.getRssi());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi RSSI");
        item.setSubTitle(network);
        return item;
    }

    private Item getWifiHostname(){
        String network = "";
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            network = getString.invoke(null,"net.hostname").toString();
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        Item item = new Item();
        item.setTitle("Wi-Fi Hostname");
        item.setSubTitle(network);
        return item;
    }

    private Item getBluetoothMac() {
        String network="";
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    network = bm.getAdapter().getAddress();
                } else {
                    network = BluetoothAdapter.getDefaultAdapter().getAddress();
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
            network=context.getResources().getString(R.string.no_longer_possible,"6.0");
        }
        Item item = new Item();
        item.setTitle("Bluetooth MAC Address");
        item.setSubTitle(network);
        return item;
    }

    private Item getBluetoothHostname() {
        String network="";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                network = bm.getAdapter().getName();
            } else {
                network = BluetoothAdapter.getDefaultAdapter().getName();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        Item item = new Item();
        item.setTitle("Bluetooth Hostname");
        item.setSubTitle(network);
        return item;
    }

    private Item getSimSerial() {
        String network="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            network= telephonyManager.getSimSerialNumber();
        } catch (NullPointerException | SecurityException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        Item item = new Item();
        item.setTitle("Sim Serial");
        item.setSubTitle(network);
        return item;
    }

    private Item getSimOperatorName() {
        String network="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            network= telephonyManager.getSimOperatorName();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        Item item = new Item();
        item.setTitle("Sim Operator Name");
        item.setSubTitle(network);
        return item;
    }

    private Item getSimCountry() {
        String network="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            network= telephonyManager.getSimCountryIso();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        Item item = new Item();
        item.setTitle("Sim Country");
        item.setSubTitle(network);
        return item;
    }

    private Item getSimState() {
        String network="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getSimState()){
                case TelephonyManager.SIM_STATE_ABSENT:
                    network = "Absent";
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    network = "Network Locked";
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    network = "PIN Required";
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    network = "PUK Required";
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    network = "Ready";
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                default:
                    network = "Network Unknown";
                    break;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        Item item = new Item();
        item.setTitle("Sim State");
        item.setSubTitle(network);
        return item;
    }

    private Item getPhoneNumber() {
        String network="";
        try {
            if (new Permissions(activity).hasPermission(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                network= telephonyManager.getLine1Number();
            } else {
                network = context.getResources().getString(R.string.phone_permission_denied);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        Item item = new Item();
        item.setTitle("Phone Number");
        item.setSubTitle(network);
        return item;
    }

    private Item getVoicemailNumber() {
        String network="";
        try {
            if (new Permissions(activity).hasPermission(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                network= telephonyManager.getVoiceMailNumber();
            } else {
                network = context.getResources().getString(R.string.phone_permission_denied);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneStrength");
        }
        Item item = new Item();
        item.setTitle("Voicemail Number");
        item.setSubTitle(network);
        return item;
    }

    private Item getCellNetworkName() {
        String network="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            network= telephonyManager.getNetworkOperatorName();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        Item item = new Item();
        item.setTitle("Cell Network Name");
        item.setSubTitle(network);
        return item;
    }

    private Item getCellNetworkType() {
        String network="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getNetworkType()){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    network="2G";
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
                    network="3G";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    network="4G";
                    break;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    network=context.getResources().getString(R.string.not_found);
                    break;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        Item item = new Item();
        item.setTitle("Cell Network Type");
        item.setSubTitle(network);
        return item;
    }
}
