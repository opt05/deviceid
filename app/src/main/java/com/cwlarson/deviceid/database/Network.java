package com.cwlarson.deviceid.database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;
import com.cwlarson.deviceid.databinding.UnavailableItem;
import com.cwlarson.deviceid.databinding.UnavailablePermission;
import com.cwlarson.deviceid.databinding.UnavailableType;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteOrder;

class Network {
    private final String TAG = Network.class.getSimpleName();
    private final Context context;
    private final WifiInfo mWifiConnectionInfo;

    Network(Activity activity, AppDatabase db) {
        this.context = activity.getApplicationContext();
        this.mWifiConnectionInfo = ((WifiManager)
            activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        //Set Network Tiles
        ItemAdder itemAdder = new ItemAdder(context, db);
        itemAdder.addItems(getWifiMac());
        itemAdder.addItems(getWifiBSSID());
        itemAdder.addItems(getWifiSSID());
        itemAdder.addItems(getWifiFrequency());
        itemAdder.addItems(getWifiHiddenSSID());
        itemAdder.addItems(getWifiIpAddress());
        itemAdder.addItems(getWifiLinkSpeed());
        itemAdder.addItems(getWifiNetworkID());
        itemAdder.addItems(getWifiRSSI());
        itemAdder.addItems(getWifiHostname());
        itemAdder.addItems(getBluetoothMac());
        itemAdder.addItems(getBluetoothHostname());
        itemAdder.addItems(getSimSerial());
        itemAdder.addItems(getSimOperatorName());
        itemAdder.addItems(getSimCountry());
        itemAdder.addItems(getSimState());
        itemAdder.addItems(getPhoneNumber());
        itemAdder.addItems(getVoicemailNumber());
        itemAdder.addItems(getCellNetworkName());
        itemAdder.addItems(getCellNetworkType());
    }

    @SuppressLint("HardwareIds")
    private Item getWifiMac(){
        Item item = new Item("Wi-Fi MAC Address", ItemType.NETWORK);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                item.setSubtitle(mWifiConnectionInfo.getMacAddress());
            } catch (Exception e) {
                Log.w(TAG, "Null in getWiFiMac");
            }
        } else {
            /*
             * Marshmallow has started to depreciate this method
             * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
            */
            item.setUnavailableitem(
                new UnavailableItem(UnavailableType.NO_LONGER_POSSIBLE,"6.0"));
        }
        return item;
    }

    private Item getWifiBSSID(){
        Item item = new Item("Wi-Fi BSSID", ItemType.NETWORK);
        try {
            item.setSubtitle(mWifiConnectionInfo.getBSSID());
        } catch (Exception e){
            Log.w(TAG, "Null in getWiFiBSSID");
        }
        return item;
    }

    private Item getWifiSSID(){
        Item item = new Item("Wi-Fi SSID", ItemType.NETWORK);
        try {
            item.setSubtitle(mWifiConnectionInfo.getSSID());
        } catch (Exception e){
            Log.w(TAG, "Null in getWiFiSSID");
        }
        return item;
    }
    
    private Item getWifiFrequency(){
        Item item = new Item("Wi-Fi Frequency", ItemType.NETWORK);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                item.setSubtitle(Integer.toString(mWifiConnectionInfo.getFrequency()));
            } catch (Exception e) {
                Log.w(TAG, "Null in getWiFiMac");
            }
        }
        return item;
    }

    private Item getWifiHiddenSSID(){
        Item item = new Item("Wi-Fi Hidden SSID", ItemType.NETWORK);
        try {
            item.setSubtitle(Boolean.toString(mWifiConnectionInfo.getHiddenSSID()));
        } catch (Exception e){
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiIpAddress(){
        Item item = new Item("Wi-Fi IP Address", ItemType.NETWORK);
        try {
            int ipAddress = mWifiConnectionInfo.getIpAddress();
            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ipAddress = Integer.reverseBytes(ipAddress);
            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
            item.setSubtitle(InetAddress.getByAddress(ipByteArray).getHostAddress());
        } catch (Exception e){
            Log.w(TAG, "Exception in getWiFiMac");
        }
        return item;
    }

    private Item getWifiLinkSpeed(){
        Item item = new Item("Wi-Fi Link Speed", ItemType.NETWORK);
        try {
            item.setSubtitle(Integer.toString(mWifiConnectionInfo.getLinkSpeed()));
        } catch (Exception e){
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiNetworkID(){
        Item item = new Item("Wi-Fi Network ID", ItemType.NETWORK);
        try {
            item.setSubtitle(Integer.toString(mWifiConnectionInfo.getNetworkId()));
        } catch (NullPointerException e){
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiRSSI(){
        Item item = new Item("Wi-Fi RSSI", ItemType.NETWORK);
        try {
            item.setSubtitle(Integer.toString(mWifiConnectionInfo.getRssi()));
        } catch (Exception e){
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    private Item getWifiHostname(){
        Item item = new Item("Wi-Fi Hostname", ItemType.NETWORK);
        try {
            @SuppressLint("PrivateApi")
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            item.setSubtitle(getString.invoke(null,"net.hostname").toString());
        } catch (Exception e){
            Log.w(TAG, "Null in getWiFiMac");
        }
        return item;
    }

    @SuppressLint("HardwareIds")
    private Item getBluetoothMac() {
        Item item = new Item("Bluetooth MAC Address", ItemType.NETWORK);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    item.setSubtitle(bm.getAdapter().getAddress());
                } else {
                    item.setSubtitle(BluetoothAdapter.getDefaultAdapter().getAddress());
                }
            } catch (Exception e){
                Log.w(TAG, "Null in getBluetoothMac");
            }
        } else {
            /*
             * Marshmallow has started to depreciate this method
             * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
            */
            item.setUnavailableitem(
                new UnavailableItem(UnavailableType.NO_LONGER_POSSIBLE,"6.0"));
        }
        return item;
    }

    private Item getBluetoothHostname() {
        Item item = new Item("Bluetooth Hostname", ItemType.NETWORK);
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                item.setSubtitle(bm.getAdapter().getName());
            } else {
                item.setSubtitle(BluetoothAdapter.getDefaultAdapter().getName());
            }
        } catch (Exception e){
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private Item getSimSerial() {
        Item item = new Item("Sim Serial", ItemType.NETWORK);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubtitle(telephonyManager.getSimSerialNumber());
        } catch (Exception e){
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    private Item getSimOperatorName() {
        Item item = new Item("Sim Operator Name", ItemType.NETWORK);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubtitle(telephonyManager.getSimOperatorName());
        } catch (Exception e){
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    private Item getSimCountry() {
        Item item = new Item("Sim Country", ItemType.NETWORK);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubtitle(telephonyManager.getSimCountryIso());
        } catch (Exception e){
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    private Item getSimState() {
        Item item = new Item("Sim State", ItemType.NETWORK);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getSimState()){
                case TelephonyManager.SIM_STATE_ABSENT:
                    item.setSubtitle("Absent");
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    item.setSubtitle("Network Locked");
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    item.setSubtitle("PIN Required");
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    item.setSubtitle("PUK Required");
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    item.setSubtitle("Ready");
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                default:
                    item.setSubtitle("Network Unknown");
                    break;
            }
        } catch (Exception e){
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return item;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private Item getPhoneNumber() {
        Item item = new Item("Phone Number", ItemType.NETWORK);
        try {
            if (new Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                item.setSubtitle(telephonyManager.getLine1Number());
            } else {
                item.setUnavailableitem(new UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                    context.getResources().getString(R.string.phone_permission_denied),
                    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE));
            }
        } catch (Exception e){
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return item;
    }

    @SuppressLint("MissingPermission")
    private Item getVoicemailNumber() {
        Item item = new Item("Voicemail Number", ItemType.NETWORK);
        try {
            if (new Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                item.setSubtitle(telephonyManager.getVoiceMailNumber());
            } else {
                item.setUnavailableitem(new UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                    context.getResources().getString(R.string.phone_permission_denied),
                    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE));
            }
        } catch (Exception e){
            Log.w(TAG, "Null in getPhoneStrength");
        }
        return item;
    }

    private Item getCellNetworkName() {
        Item item = new Item("Cell Network Name", ItemType.NETWORK);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            item.setSubtitle(telephonyManager.getNetworkOperatorName());
        } catch (Exception e){
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return item;
    }

    private Item getCellNetworkType() {
        Item item = new Item("Cell Network Type", ItemType.NETWORK);
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //TODO Update with new ones
            switch(telephonyManager.getNetworkType()){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    item.setSubtitle("2G");
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
                    item.setSubtitle("3G");
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    item.setSubtitle("4G");
                    break;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    break;
            }
        } catch (Exception e){
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return item;
    }
}
