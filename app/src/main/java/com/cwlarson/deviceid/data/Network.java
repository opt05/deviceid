package com.cwlarson.deviceid.data;

import android.annotation.TargetApi;
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
import com.cwlarson.deviceid.util.MyAdapter;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Network {
    String TAG = "Network";
    private Activity activity;
    private Context context;
    private WifiInfo mWifiConnectionInfo;

    public Network(Activity activity){
        this.activity=activity;
        this.context=activity.getApplicationContext();
        this.mWifiConnectionInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
    }

    public void setNetworkTiles(MyAdapter mAdapter){
        mAdapter.addItem(Arrays.asList("Bluetooth Hostname", getBluetoothHostname()));
        mAdapter.addItem(Arrays.asList("Bluetooth MAC Address", getBluetoothMac()));
        mAdapter.addItem(Arrays.asList("Cellular Network", getCellNetworkName()));
        mAdapter.addItem(Arrays.asList("Cellular Type", getCellNetworkType()));
        mAdapter.addItem(Arrays.asList("Phone Number", getPhoneNumber()));
        mAdapter.addItem(Arrays.asList("Sim Country", getSimCountry()));
        mAdapter.addItem(Arrays.asList("Sim Operator Name", getSimOperatorName()));
        mAdapter.addItem(Arrays.asList("Sim Serial", getSimSerial()));
        mAdapter.addItem(Arrays.asList("Sim State", getSimState()));
        mAdapter.addItem(Arrays.asList("Voicemail Number", getVoicemailNumber()));
        mAdapter.addItem(Arrays.asList("Wi-Fi BSSID", getWifiBSSID()));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) mAdapter.addItem(Arrays.asList("Wi-Fi Frequency", getWifiFrequency()));
        mAdapter.addItem(Arrays.asList("Wi-Fi Hidden SSID", getWifiHiddenSSID()));
        mAdapter.addItem(Arrays.asList("Wi-Fi Hostname", getWifiHostname()));
        mAdapter.addItem(Arrays.asList("Wi-Fi IP Address", getWifiIpAddress()));
        mAdapter.addItem(Arrays.asList("Wi-Fi Link Speed", getWifiLinkSpeed()));
        mAdapter.addItem(Arrays.asList("Wi-Fi MAC Address", getWifiMac()));
        mAdapter.addItem(Arrays.asList("Wi-Fi Network ID", getWifiNetworkID()));
        mAdapter.addItem(Arrays.asList("Wi-Fi RSSI", getWifiRSSI()));
        mAdapter.addItem(Arrays.asList("Wi-Fi SSID", getWifiSSID()));
    }

    private String getWifiMac(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = mWifiConnectionInfo.getMacAddress();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac == null || wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiBSSID(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = mWifiConnectionInfo.getBSSID();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiBSSID");
        }
        return wifiInfoMac == null || wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiSSID(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = mWifiConnectionInfo.getSSID();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiSSID");
        }
        return wifiInfoMac == null || wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getWifiFrequency(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = Integer.toString(mWifiConnectionInfo.getFrequency());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiHiddenSSID(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = Boolean.toString(mWifiConnectionInfo.getHiddenSSID());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiIpAddress(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = Integer.toString(mWifiConnectionInfo.getIpAddress());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiLinkSpeed(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = Integer.toString(mWifiConnectionInfo.getLinkSpeed());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiNetworkID(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = Integer.toString(mWifiConnectionInfo.getNetworkId());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiRSSI(){
        String wifiInfoMac = "";
        try {
            wifiInfoMac = Integer.toString(mWifiConnectionInfo.getRssi());
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getWifiHostname(){
        String wifiInfoMac = "";
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            wifiInfoMac = getString.invoke(null,"net.hostname").toString();
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG, "Null in getWiFiMac");
        }
        return wifiInfoMac == null || wifiInfoMac.equals("") ? context.getResources().getString(R.string.not_found) : wifiInfoMac;
    }

    private String getBluetoothMac() {
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
            Log.w(TAG, "Null in getBluetoothMac");
        }
        return macAddress == null || macAddress.equals("")  ? context.getResources().getString(R.string.not_found) : macAddress;
    }

    private String getBluetoothHostname() {
        String macAddress="";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                macAddress = bm.getAdapter().getName();
            } else {
                macAddress = BluetoothAdapter.getDefaultAdapter().getName();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return macAddress == null || macAddress.equals("")  ? context.getResources().getString(R.string.not_found) : macAddress;
    }

    private String getSimSerial() {
        String sim="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sim= telephonyManager.getSimSerialNumber();
        } catch (NullPointerException | SecurityException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return sim == null || sim.equals("") ? context.getResources().getString(R.string.not_found) : sim;
    }

    private String getSimOperatorName() {
        String sim="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sim= telephonyManager.getSimOperatorName();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return sim == null || sim.equals("") ? context.getResources().getString(R.string.not_found) : sim;
    }

    private String getSimCountry() {
        String sim="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sim= telephonyManager.getSimCountryIso();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return sim == null || sim.equals("") ? context.getResources().getString(R.string.not_found) : sim;
    }

    private String getSimState() {
        String sim="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getSimState()){
                case TelephonyManager.SIM_STATE_ABSENT:
                    sim = "Absent";
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    sim = "Network Locked";
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    sim = "PIN Required";
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    sim = "PUK Required";
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    sim = "Ready";
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                default:
                    sim = "Network Unknown";
                    break;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getBluetoothHostname");
        }
        return sim.equals("") ? context.getResources().getString(R.string.not_found) : sim;
    }

    private String getPhoneNumber() {
        String sim="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sim= telephonyManager.getLine1Number();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return sim == null || sim.equals("") ? context.getResources().getString(R.string.not_found) : sim;
    }

    private String getVoicemailNumber() {
        String sim="";
        try {
            if (new Permissions(activity).hasPermission(Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                sim= telephonyManager.getVoiceMailNumber();
            } else {
                sim = context.getResources().getString(R.string.phone_permission_denied);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneStrength");
        }
        return sim == null || sim.equals("") ? context.getResources().getString(R.string.not_found) : sim;
    }

    private String getCellNetworkName() {
        String string="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            string= telephonyManager.getNetworkOperatorName();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return string == null || string.equals("") ? context.getResources().getString(R.string.not_found) : string;
    }

    private String getCellNetworkType() {
        String string="";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(telephonyManager.getNetworkType()){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    string="2G";
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
                    string="3G";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    string="4G";
                    break;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    string=context.getResources().getString(R.string.not_found);
                    break;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "Null in getPhoneNumber");
        }
        return string.equals("") ? context.getResources().getString(R.string.not_found) : string;
    }
}
