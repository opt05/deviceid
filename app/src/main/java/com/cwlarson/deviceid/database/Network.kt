package com.cwlarson.deviceid.database

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.*
import com.cwlarson.deviceid.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder
import java.util.*

class Network(private val context: Context, db: AppDatabase, scope: CoroutineScope) {
    private val wifiConnectionInfo: WifiInfo = context.wifiManager.connectionInfo

    init {
        scope.launch {
            db.addItems(context, wifiMac(), wifiBSSID(), wifiSSID(), wifiFrequency(), wifiHiddenSSID(),
                    wifiIpAddress(), wifiLinkSpeed(), wifiTxLinkSpeed(), wifiNetworkID(),
                    wifiPasspointFqdn(), wifiPasspointProviderFriendlyName(), wifiRSSI(),
                    wifiSignalLevel(), wifiHostname(), bluetoothMac(), bluetoothHostname(),
                    phoneCount(), simSerial(), simOperatorName(), simCountry(), simState(),
                    phoneNumber(), voicemailNumber(), cellNetworkName(), cellNetworkType(),
                    cellNetworkClass(), eSimID(), eSimEnabled(), eSimOSVersion(),
                    isConcurrentVoiceAndDataSupported(), isDataRoamingEnabled(),
                    isHearingAidSupported(), isMultiSimSupported(), isRttSupported(),
                    isSmsCapable(), isVoiceCapable(), deviceSoftwareVersion(), manufacturerCode(),
                    nai())
        }
    }

    @SuppressLint("MissingPermission")
    private fun deviceSoftwareVersion() = Item("Device Software Version", ItemType.NETWORK).apply {
        try {
            if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                subtitle = context.telephonyManager.deviceSoftwareVersion
            } else {
                unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                        context.resources.getString(R.string.permission_item_subtitle,
                                context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                        .loadLabel(context.packageManager).toString()
                                        .toUpperCase(Locale.getDefault())),
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }
    /**
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds")
    private fun wifiMac() = Item("Wi-Fi MAC Address", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                subtitle = wifiConnectionInfo.macAddress
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }

        } else {
            unavailableItem = UnavailableItem(UnavailableType.NO_LONGER_POSSIBLE,
                    Build.VERSION_CODES.M.sdkToVersion())
        }
    }

    private fun wifiBSSID() = Item("Wi-Fi BSSID", ItemType.NETWORK).apply {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)) {
                    subtitle = wifiConnectionInfo.bssid
                } else {
                    unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.permission_item_subtitle,
                                    context.packageManager.getPermissionInfo(Manifest.permission.ACCESS_FINE_LOCATION, 0)
                                            .loadLabel(context.packageManager).toString()
                                            .toUpperCase(Locale.getDefault())),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)
                }
            } else
                subtitle = wifiConnectionInfo.bssid
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun wifiSSID() = Item("Wi-Fi SSID", ItemType.NETWORK).apply {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)) {
                    subtitle = wifiConnectionInfo.ssid
                } else {
                    unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.permission_item_subtitle,
                                    context.packageManager.getPermissionInfo(Manifest.permission.ACCESS_FINE_LOCATION, 0)
                                            .loadLabel(context.packageManager).toString()
                                            .toUpperCase(Locale.getDefault())),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)
                }
            } else
                subtitle = wifiConnectionInfo.ssid
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun wifiFrequency() = Item("Wi-Fi Frequency", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                subtitle = wifiConnectionInfo.frequency.toString().plus(WifiInfo.FREQUENCY_UNITS)
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }

        }
    }

    private fun wifiHiddenSSID() = Item("Wi-Fi Hidden SSID", ItemType.NETWORK).apply {
        try {
            subtitle = wifiConnectionInfo.hiddenSSID.toString()
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    // Convert little-endian to big-endian if needed
    private fun wifiIpAddress() = Item("Wi-Fi IP Address", ItemType.NETWORK).apply {
        try {
            var ipAddress = wifiConnectionInfo.ipAddress
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ipAddress = Integer.reverseBytes(ipAddress)
            val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
            subtitle = InetAddress.getByAddress(ipByteArray).hostAddress
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun wifiLinkSpeed() = Item("Wi-Fi Link Speed", ItemType.NETWORK).apply {
        try {
            subtitle = wifiConnectionInfo.linkSpeed.toString().plus(WifiInfo.LINK_SPEED_UNITS)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun wifiTxLinkSpeed() = Item("Wi-Fi Transmit Link Speed", ItemType.NETWORK).apply {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = wifiConnectionInfo.txLinkSpeedMbps.toString().plus(WifiInfo.LINK_SPEED_UNITS)
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    private fun wifiNetworkID() = Item("Wi-Fi Network ID", ItemType.NETWORK).apply {
        try {
            subtitle = wifiConnectionInfo.networkId.toString()
        } catch (e: NullPointerException) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun wifiPasspointFqdn() = Item("Wi-Fi Passpoint Fully Qualified Domain Name", ItemType.NETWORK).apply {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = wifiConnectionInfo.passpointFqdn
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    private fun wifiPasspointProviderFriendlyName() = Item("Wi-Fi Passpoint Provider Friendly Name", ItemType.NETWORK).apply {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = wifiConnectionInfo.passpointProviderFriendlyName
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    private fun wifiRSSI() = Item("Wi-Fi RSSI", ItemType.NETWORK).apply {
        try {
            subtitle = wifiConnectionInfo.rssi.toString()
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun wifiSignalLevel() = Item("Wi-Fi Signal Level", ItemType.NETWORK).apply {
        try {
            subtitle = WifiManager.calculateSignalLevel(wifiConnectionInfo.rssi, 100)
                    .toString().plus("%")
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    @SuppressLint("PrivateApi","DiscouragedPrivateApi")
    private fun wifiHostname() = Item("Wi-Fi Hostname", ItemType.NETWORK).apply {
        try {
            Build::class.java.getDeclaredMethod("getString", String::class.java).run {
                isAccessible = true
                subtitle = invoke(null, "net.hostname")?.toString()
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    /**
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds")
    private fun bluetoothMac() = Item("Bluetooth MAC Address", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    context.bluetoothManager.adapter.address
                } else {
                    BluetoothAdapter.getDefaultAdapter().address
                }
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }

        } else {
            unavailableItem = UnavailableItem(UnavailableType.NO_LONGER_POSSIBLE,
                    Build.VERSION_CODES.M.sdkToVersion())
        }
    }

    private fun bluetoothHostname() = Item("Bluetooth Hostname", ItemType.NETWORK).apply {
        try {
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                context.bluetoothManager.adapter.name
            } else {
                BluetoothAdapter.getDefaultAdapter().name
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun manufacturerCode() = Item("Manufacturer Code", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = context.telephonyManager.manufacturerCode
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    @SuppressLint("MissingPermission")
    private fun nai() = Item("Network Access Identifier (NAI)", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    subtitle = context.telephonyManager.nai
                } else {
                    unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.permission_item_subtitle,
                                    context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                            .loadLabel(context.packageManager).toString()
                                            .toUpperCase(Locale.getDefault())),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.P.sdkToVersion())
        }
    }

    private fun phoneCount() = Item("Phone Count", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                subtitle = context.telephonyManager.phoneCount.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.M.sdkToVersion())
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun simSerial() = Item("Sim Serial", ItemType.NETWORK).apply {
        try {
            subtitle = context.telephonyManager.simSerialNumber
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun simOperatorName() = Item("Sim Operator Name", ItemType.NETWORK).apply {
        try {
            subtitle = context.telephonyManager.simOperatorName
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun simCountry() = Item("Sim Country", ItemType.NETWORK).apply {
        try {
            subtitle = context.telephonyManager.simCountryIso
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun simState() = Item("Sim State", ItemType.NETWORK).apply {
        try {
            subtitle = when (context.telephonyManager.simState) {
                TelephonyManager.SIM_STATE_ABSENT -> "No SIM card is available in the device "
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Locked: requires a network PIN to unlock"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "Locked: requires the user's SIM PIN to unlock"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "Locked: requires the user's SIM PUK to unlock"
                TelephonyManager.SIM_STATE_READY -> "Ready"
                TelephonyManager.SIM_STATE_NOT_READY -> "Not ready"
                TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently disabled"
                TelephonyManager.SIM_STATE_UNKNOWN -> "Network unknown"
                TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Error, present but faulty"
                TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Restricted, present but not " +
                        "usable due to carrier restrictions"
                else -> "Network unknown"
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun phoneNumber() = Item("Phone Number", ItemType.NETWORK).apply {
        try {
            if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                subtitle = context.telephonyManager.line1Number
            } else {
                unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                        context.resources.getString(R.string.permission_item_subtitle,
                                context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                        .loadLabel(context.packageManager).toString()
                                        .toUpperCase(Locale.getDefault())),
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun voicemailNumber() = Item("Voicemail Number", ItemType.NETWORK).apply {
        try {
            if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                subtitle = context.telephonyManager.voiceMailNumber
            } else {
                unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                    context.resources.getString(R.string.permission_item_subtitle,
                            context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                    .loadLabel(context.packageManager).toString()
                                    .toUpperCase(Locale.getDefault())),
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun cellNetworkName() = Item("Cell Network Name", ItemType.NETWORK).apply {
        try {
            subtitle = context.telephonyManager.networkOperatorName
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun cellNetworkType() = Item("Cell Network Type", ItemType.NETWORK).apply {
        try {
            subtitle = when (context.telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> "CDMA - EvDo rev. 0"
                TelephonyManager.NETWORK_TYPE_EVDO_A -> "CDMA - EvDo rev. A"
                TelephonyManager.NETWORK_TYPE_EVDO_B -> "CDMA  - EvDo rev. B"
                TelephonyManager.NETWORK_TYPE_1xRTT -> "CDMA - 1xRTT"
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_EHRPD -> "CDMA - eHRPD"
                TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
                TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
                else -> { null }
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun cellNetworkClass() = Item("Cell Network Class", ItemType.NETWORK).apply {
        try {
            when (context.telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_GSM,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN-> subtitle = "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> subtitle = "3G"
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN -> subtitle = "4G"
                TelephonyManager.NETWORK_TYPE_NR -> subtitle = "5G"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> { }
                else -> { }
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun isConcurrentVoiceAndDataSupported() = Item("Concurrent Voice and Data Supported", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                subtitle = context.telephonyManager.isConcurrentVoiceAndDataSupported.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.O.sdkToVersion())
        }
    }

    private fun isDataRoamingEnabled() = Item("Data Roaming Enabled", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    subtitle = context.telephonyManager.isDataEnabled.toString()
                } else {
                    unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.permission_item_subtitle,
                                    context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                            .loadLabel(context.packageManager).toString()
                                            .toUpperCase(Locale.getDefault())),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    private fun isHearingAidSupported() = Item("Hearing Aid Compatibility Supported", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                subtitle = context.telephonyManager.isSmsCapable.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.M.sdkToVersion())
        }
    }

    private fun isMultiSimSupported() = Item("Multi Sim Supported", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    subtitle = when(context.telephonyManager.isMultiSimSupported) {
                        TelephonyManager.MULTISIM_ALLOWED -> "Supports multiple SIMs"
                        TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_HARDWARE -> "Device does not " +
                                "support multiple SIMs"
                        TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_CARRIER -> "Device does support " +
                                "multiple SIMS but restricted by carrier"
                        else -> { null }
                    }
                } else {
                    unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.permission_item_subtitle,
                                    context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                            .loadLabel(context.packageManager).toString()
                                            .toUpperCase(Locale.getDefault())),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    private fun isRttSupported() = Item("RTT Supported", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = context.telephonyManager.isSmsCapable.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.Q.sdkToVersion())
        }
    }

    private fun isSmsCapable() = Item("SMS Capable", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                subtitle = context.telephonyManager.isSmsCapable.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.LOLLIPOP.sdkToVersion())
        }
    }

    private fun isVoiceCapable() = Item("Voice Capable", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                subtitle = context.telephonyManager.isVoiceCapable.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.LOLLIPOP_MR1.sdkToVersion())
        }
    }

    private fun eSimID() = Item("eSIM ID", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = context.euiccManager.eid
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.P.sdkToVersion())
        }
    }

    private fun eSimEnabled() = Item("eSIM Enabled", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = context.euiccManager.isEnabled.toString()
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.P.sdkToVersion())
        }
    }

    private fun eSimOSVersion() = Item("eSIM OS Version", ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = context.euiccManager.euiccInfo?.osVersion
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        } else {
            unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                    Build.VERSION_CODES.P.sdkToVersion())
        }
    }
}
