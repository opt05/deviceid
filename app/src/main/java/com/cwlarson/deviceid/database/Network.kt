package com.cwlarson.deviceid.database

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.*
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder

internal class Network(activity: Activity, db: AppDatabase) {
    private val tag = Network::class.java.simpleName
    private val context: Context = activity.applicationContext
    private val mWifiConnectionInfo: WifiInfo =
            (activity.applicationContext.getSystemService(Context.WIFI_SERVICE)
                    as WifiManager).connectionInfo

    init {
        //Set Network Tiles
        val itemAdder = ItemAdder(context, db)
        itemAdder.addItems(wifiMac())
        itemAdder.addItems(wifiBSSID())
        itemAdder.addItems(wifiSSID())
        itemAdder.addItems(wifiFrequency())
        itemAdder.addItems(wifiHiddenSSID())
        itemAdder.addItems(wifiIpAddress())
        itemAdder.addItems(wifiLinkSpeed())
        itemAdder.addItems(wifiNetworkID())
        itemAdder.addItems(wifiRSSI())
        itemAdder.addItems(wifiHostname())
        itemAdder.addItems(bluetoothMac())
        itemAdder.addItems(bluetoothHostname())
        itemAdder.addItems(simSerial())
        itemAdder.addItems(simOperatorName())
        itemAdder.addItems(simCountry())
        itemAdder.addItems(simState())
        itemAdder.addItems(phoneNumber())
        itemAdder.addItems(voicemailNumber())
        itemAdder.addItems(cellNetworkName())
        itemAdder.addItems(cellNetworkType())
    }

    /*
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds")
    private fun wifiMac(): Item {
        val item = Item("Wi-Fi MAC Address", ItemType.NETWORK)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                item.subtitle = mWifiConnectionInfo.macAddress
            } catch (e: Exception) {
                Log.w(tag, "Null in getWiFiMac")
            }

        } else {
            item.unavailableitem = UnavailableItem(UnavailableType.NO_LONGER_POSSIBLE, "6.0")
        }
        return item
    }

    private fun wifiBSSID(): Item {
        val item = Item("Wi-Fi BSSID", ItemType.NETWORK)
        try {
            item.subtitle = mWifiConnectionInfo.bssid
        } catch (e: Exception) {
            Log.w(tag, "Null in getWiFiBSSID")
        }

        return item
    }

    private fun wifiSSID(): Item {
        val item = Item("Wi-Fi SSID", ItemType.NETWORK)
        try {
            item.subtitle = mWifiConnectionInfo.ssid
        } catch (e: Exception) {
            Log.w(tag, "Null in getWiFiSSID")
        }

        return item
    }

    private fun wifiFrequency(): Item {
        val item = Item("Wi-Fi Frequency", ItemType.NETWORK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                item.subtitle = Integer.toString(mWifiConnectionInfo.frequency)
            } catch (e: Exception) {
                Log.w(tag, "Null in getWiFiMac")
            }

        }
        return item
    }

    private fun wifiHiddenSSID(): Item {
        val item = Item("Wi-Fi Hidden SSID", ItemType.NETWORK)
        try {
            item.subtitle = java.lang.Boolean.toString(mWifiConnectionInfo.hiddenSSID)
        } catch (e: Exception) {
            Log.w(tag, "Null in getWiFiMac")
        }

        return item
    }

    // Convert little-endian to big-endian if needed
    private fun wifiIpAddress(): Item {
        val item = Item("Wi-Fi IP Address", ItemType.NETWORK)
        try {
            var ipAddress = mWifiConnectionInfo.ipAddress
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ipAddress = Integer.reverseBytes(ipAddress)
            val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
            item.subtitle = InetAddress.getByAddress(ipByteArray).hostAddress
        } catch (e: Exception) {
            Log.w(tag, "Exception in getWiFiMac")
        }

        return item
    }

    private fun wifiLinkSpeed(): Item  {
        val item = Item("Wi-Fi Link Speed", ItemType.NETWORK)
        try {
            item.subtitle = Integer.toString(mWifiConnectionInfo.linkSpeed)
        } catch (e: Exception) {
            Log.w(tag, "Null in getWiFiMac")
        }

        return item
    }

    private fun wifiNetworkID(): Item {
        val item = Item("Wi-Fi Network ID", ItemType.NETWORK)
        try {
            item.subtitle = Integer.toString(mWifiConnectionInfo.networkId)
        } catch (e: NullPointerException) {
            Log.w(tag, "Null in getWiFiMac")
        }

        return item
    }

    private fun wifiRSSI(): Item {
        val item = Item("Wi-Fi RSSI", ItemType.NETWORK)
        try {
            item.subtitle = Integer.toString(mWifiConnectionInfo.rssi)
        } catch (e: Exception) {
            Log.w(tag, "Null in getWiFiMac")
        }

        return item
    }

    private fun wifiHostname(): Item  {
        val item = Item("Wi-Fi Hostname", ItemType.NETWORK)
        try {
            @SuppressLint("PrivateApi")
            val getString = Build::class.java.getDeclaredMethod("getString", String::class.java)
            getString.isAccessible = true
            item.subtitle = getString.invoke(null, "net.hostname").toString()
        } catch (e: Exception) {
            Log.w(tag, "Null in getWiFiMac")
        }

        return item
    }

    /*
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds")
    private fun bluetoothMac(): Item {
        val item = Item("Bluetooth MAC Address", ItemType.NETWORK)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    item.subtitle = bm.adapter.address
                } else {
                    item.subtitle = BluetoothAdapter.getDefaultAdapter().address
                }
            } catch (e: Exception) {
                Log.w(tag, "Null in getBluetoothMac")
            }

        } else {
            item.unavailableitem = UnavailableItem(UnavailableType.NO_LONGER_POSSIBLE, "6.0")
        }
        return item
    }

    private fun bluetoothHostname(): Item  {
        val item = Item("Bluetooth Hostname", ItemType.NETWORK)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                item.subtitle = bm.adapter.name
            } else {
                item.subtitle = BluetoothAdapter.getDefaultAdapter().name
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getBluetoothHostname")
        }

        return item
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun simSerial(): Item {
        val item = Item("Sim Serial", ItemType.NETWORK)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            item.subtitle = telephonyManager.simSerialNumber
        } catch (e: Exception) {
            Log.w(tag, "Null in getBluetoothHostname")
        }

        return item
    }

    private fun simOperatorName(): Item  {
        val item = Item("Sim Operator Name", ItemType.NETWORK)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            item.subtitle = telephonyManager.simOperatorName
        } catch (e: Exception) {
            Log.w(tag, "Null in getBluetoothHostname")
        }

        return item
    }

    private fun simCountry(): Item {
        val item = Item("Sim Country", ItemType.NETWORK)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            item.subtitle = telephonyManager.simCountryIso
        } catch (e: Exception) {
            Log.w(tag, "Null in getBluetoothHostname")
        }

        return item
        }

    private fun simState(): Item {
        val item = Item("Sim State", ItemType.NETWORK)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephonyManager.simState) {
                TelephonyManager.SIM_STATE_ABSENT -> item.subtitle = "Absent"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> item.subtitle = "Network Locked"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> item.subtitle = "PIN Required"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> item.subtitle = "PUK Required"
                TelephonyManager.SIM_STATE_READY -> item.subtitle = "Ready"
                TelephonyManager.SIM_STATE_UNKNOWN -> item.subtitle = "Network Unknown"
                else -> item.subtitle = "Network Unknown"
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getBluetoothHostname")
        }

        return item
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun phoneNumber(): Item {
        val item = Item("Phone Number", ItemType.NETWORK)
        try {
            if (Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                item.subtitle = telephonyManager.line1Number
            } else {
                item.unavailableitem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                        context.resources.getString(R.string.phone_permission_denied),
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getPhoneNumber")
        }

        return item
    }

    @SuppressLint("MissingPermission")
    private fun voicemailNumber(): Item {
        val item = Item("Voicemail Number", ItemType.NETWORK)
        try {
            if (Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                item.subtitle = telephonyManager.voiceMailNumber
            } else {
                item.unavailableitem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                        context.resources.getString(R.string.phone_permission_denied),
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getPhoneStrength")
        }

        return item
    }

    private fun cellNetworkName(): Item  {
        val item = Item("Cell Network Name", ItemType.NETWORK)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            item.subtitle = telephonyManager.networkOperatorName
        } catch (e: Exception) {
            Log.w(tag, "Null in getPhoneNumber")
        }

        return item
    }

    private fun cellNetworkType(): Item {
        val item = Item("Cell Network Type", ItemType.NETWORK)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_IDEN -> item.subtitle = "2G"
                TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_UMTS -> item.subtitle = "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> item.subtitle = "4G"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> { }
                else -> { }
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getPhoneNumber")
        }

        return item
    }
}
