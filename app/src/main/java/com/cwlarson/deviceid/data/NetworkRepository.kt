package com.cwlarson.deviceid.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.tabs.UnavailablePermission
import com.cwlarson.deviceid.util.*
import timber.log.Timber
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder

class NetworkRepository(private val context: Context, filterUnavailable: Boolean = false)
    : TabData(filterUnavailable) {
    private val wifiConnectionInfo: WifiInfo = context.wifiManager.connectionInfo

    override suspend fun list(): List<Item> = listOf(wifiMac(), wifiBSSID(), wifiSSID(),
            wifiFrequency(), wifiHiddenSSID(), wifiIpAddress(), wifiLinkSpeed(), wifiTxLinkSpeed(),
            wifiNetworkID(), wifiPasspointFqdn(), wifiPasspointProviderFriendlyName(), wifiRSSI(),
            wifiSignalLevel(), wifiHostname(), bluetoothMac(), bluetoothHostname(),
            phoneCount(), simSerial(), simOperatorName(), simCountry(), simState(),
            phoneNumber(), voicemailNumber(), cellNetworkName(), cellNetworkType(),
            cellNetworkClass(), eSimID(), eSimEnabled(), eSimOSVersion(),
            isConcurrentVoiceAndDataSupported(), isDataRoamingEnabled(),
            isHearingAidSupported(), isMultiSimSupported(), isRttSupported(),
            isSmsCapable(), isVoiceCapable(), deviceSoftwareVersion(),
            manufacturerCode(), nai())

    @SuppressLint("MissingPermission")
    private fun deviceSoftwareVersion() = Item(R.string.network_title_device_software_version, ItemType.NETWORK).apply {
        try {
            subtitle = if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                ItemSubtitle.Text(context.telephonyManager.deviceSoftwareVersion)
            } else {
                ItemSubtitle.Permission(Manifest.permission.READ_PHONE_STATE,
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    /**
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds")
    private fun wifiMac() = Item(R.string.network_title_wifi_mac, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                subtitle = ItemSubtitle.Text(wifiConnectionInfo.macAddress)
            } catch (e: Throwable) {
                Timber.w(e)
            }

        } else {
            subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.M)
        }
    }

    private fun wifiBSSID() = Item(R.string.network_title_wifi_bssid, ItemType.NETWORK).apply {
        try {
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)) {
                    ItemSubtitle.Text(wifiConnectionInfo.bssid)
                } else {
                    ItemSubtitle.Permission(Manifest.permission.ACCESS_FINE_LOCATION,
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)
                }
            } else
                ItemSubtitle.Text(wifiConnectionInfo.bssid)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun wifiSSID() = Item(R.string.network_title_wifi_ssid, ItemType.NETWORK).apply {
        try {
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)) {
                    ItemSubtitle.Text(wifiConnectionInfo.ssid)
                } else {
                    ItemSubtitle.Permission(Manifest.permission.ACCESS_FINE_LOCATION,
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE)
                }
            } else
                ItemSubtitle.Text(wifiConnectionInfo.ssid)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun wifiFrequency() = Item(R.string.network_title_wifi_frequency, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                subtitle = ItemSubtitle.Text(
                        wifiConnectionInfo.frequency.toString().plus(WifiInfo.FREQUENCY_UNITS))
            } catch (e: Throwable) {
                Timber.w(e)
            }

        }
    }

    private fun wifiHiddenSSID() = Item(R.string.network_title_wifi_hidden_ssid, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(wifiConnectionInfo.hiddenSSID.toString())
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    // Convert little-endian to big-endian if needed
    private fun wifiIpAddress() = Item(R.string.network_title_wifi_ip_address, ItemType.NETWORK).apply {
        try {
            var ipAddress = wifiConnectionInfo.ipAddress
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ipAddress = Integer.reverseBytes(ipAddress)
            val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
            subtitle = ItemSubtitle.Text(InetAddress.getByAddress(ipByteArray).hostAddress)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun wifiLinkSpeed() = Item(R.string.network_title_wifi_link_speed, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(
                    wifiConnectionInfo.linkSpeed.toString().plus(WifiInfo.LINK_SPEED_UNITS))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun wifiTxLinkSpeed() = Item(R.string.network_title_wifi_tx_link_speed, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = ItemSubtitle.Text(wifiConnectionInfo.txLinkSpeedMbps.toString().plus(
                        WifiInfo.LINK_SPEED_UNITS))
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    private fun wifiNetworkID() = Item(R.string.network_title_wifi_network_id, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(wifiConnectionInfo.networkId.toString())
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun wifiPasspointFqdn() = Item(R.string.network_title_wifi_passpoint_fqdn, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = ItemSubtitle.Text(wifiConnectionInfo.passpointFqdn)
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    private fun wifiPasspointProviderFriendlyName() = Item(R.string.network_title_wifi_passpoint_friendly_name, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = ItemSubtitle.Text(wifiConnectionInfo.passpointProviderFriendlyName)
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    private fun wifiRSSI() = Item(R.string.network_title_wifi_rssid, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(wifiConnectionInfo.rssi.toString())
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun wifiSignalLevel() = Item(R.string.network_title_wifi_signal_level, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(WifiManager.calculateSignalLevel(wifiConnectionInfo.rssi, 100)
                    .toString().plus("%"))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun wifiHostname() = Item(R.string.network_title_wifi_hostname, ItemType.NETWORK).apply {
        try {
            Build::class.java.getDeclaredMethod("getString", String::class.java).run {
                isAccessible = true
                subtitle = ItemSubtitle.Text(invoke(null, "net.hostname")?.toString())
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    /**
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds")
    private fun bluetoothMac() = Item(R.string.network_title_bluetooth_mac, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                subtitle = ItemSubtitle.Text(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            context.bluetoothManager.adapter.address
                        } else {
                            BluetoothAdapter.getDefaultAdapter().address
                        })
            } catch (e: Throwable) {
                Timber.w(e)
            }

        } else {
            subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.M)
        }
    }

    private fun bluetoothHostname() = Item(R.string.network_title_bluetooth_hostname, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        context.bluetoothManager.adapter.name
                    } else {
                        BluetoothAdapter.getDefaultAdapter().name
                    })
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun manufacturerCode() = Item(R.string.network_title_manufacturer_code, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = ItemSubtitle.Text(context.telephonyManager.manufacturerCode)
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    @SuppressLint("MissingPermission")
    private fun nai() = Item(R.string.network_title_nai, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    ItemSubtitle.Text(context.telephonyManager.nai)
                } else {
                    ItemSubtitle.Permission(Manifest.permission.READ_PHONE_STATE,
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        }
    }

    private fun phoneCount() = Item(R.string.network_title_phone_count, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                subtitle = ItemSubtitle.Text(context.telephonyManager.phoneCount.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun simSerial() = Item(R.string.network_title_sim_serial, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(context.telephonyManager.simSerialNumber)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun simOperatorName() = Item(R.string.network_title_sim_operator, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(context.telephonyManager.simOperatorName)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun simCountry() = Item(R.string.network_title_sim_country, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(context.telephonyManager.simCountryIso)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun simState() = Item(R.string.network_title_sim_state, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(when (context.telephonyManager.simState) {
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
            })
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun phoneNumber() = Item(R.string.network_title_phone_number, ItemType.NETWORK).apply {
        try {
            subtitle = if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                ItemSubtitle.Text(context.telephonyManager.line1Number)
            } else {
                ItemSubtitle.Permission(Manifest.permission.READ_PHONE_STATE,
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun voicemailNumber() = Item(R.string.network_title_voicemail_number, ItemType.NETWORK).apply {
        try {
            subtitle = if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                ItemSubtitle.Text(context.telephonyManager.voiceMailNumber)
            } else {
                ItemSubtitle.Permission(Manifest.permission.READ_PHONE_STATE,
                        UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun cellNetworkName() = Item(R.string.network_title_cell_network_name, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(context.telephonyManager.networkOperatorName)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun cellNetworkType() = Item(R.string.network_title_cell_network_type, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(when (context.telephonyManager.networkType) {
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
                else -> {
                    null
                }
            })
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun cellNetworkClass() = Item(R.string.network_title_cell_network_class, ItemType.NETWORK).apply {
        try {
            subtitle = ItemSubtitle.Text(when (context.telephonyManager.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_GSM,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> null
                else -> null
            })
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun isConcurrentVoiceAndDataSupported() = Item(R.string.network_title_concurrent_voice_data_supported, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                subtitle = ItemSubtitle.Text(
                        context.telephonyManager.isConcurrentVoiceAndDataSupported.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.O)
        }
    }

    private fun isDataRoamingEnabled() = Item(R.string.network_title_data_roaming_enabled, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    ItemSubtitle.Text(context.telephonyManager.isDataEnabled.toString())
                } else {
                    ItemSubtitle.Permission(Manifest.permission.READ_PHONE_STATE,
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    private fun isHearingAidSupported() = Item(R.string.network_title_hearing_aid_supported, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                subtitle = ItemSubtitle.Text(context.telephonyManager.isSmsCapable.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
        }
    }

    @SuppressLint("MissingPermission")
    private fun isMultiSimSupported() = Item(R.string.network_title_multi_sim_supported, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    ItemSubtitle.Text(when (context.telephonyManager.isMultiSimSupported) {
                        TelephonyManager.MULTISIM_ALLOWED -> "Supports multiple SIMs"
                        TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_HARDWARE -> "Device does not " +
                                "support multiple SIMs"
                        TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_CARRIER -> "Device does support " +
                                "multiple SIMS but restricted by carrier"
                        else -> {
                            null
                        }
                    })
                } else {
                    ItemSubtitle.Permission(Manifest.permission.READ_PHONE_STATE,
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    private fun isRttSupported() = Item(R.string.network_title_rtt_supported, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                subtitle = ItemSubtitle.Text(context.telephonyManager.isSmsCapable.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        }
    }

    private fun isSmsCapable() = Item(R.string.network_title_sms_capable, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                subtitle = ItemSubtitle.Text(context.telephonyManager.isSmsCapable.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.LOLLIPOP)
        }
    }

    private fun isVoiceCapable() = Item(R.string.network_title_voice_capable, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                subtitle = ItemSubtitle.Text(context.telephonyManager.isVoiceCapable.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.LOLLIPOP_MR1)
        }
    }

    private fun eSimID() = Item(R.string.network_title_esim_id, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = ItemSubtitle.Text(context.euiccManager.eid)
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        }
    }

    private fun eSimEnabled() = Item(R.string.network_title_esim_enabled, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = ItemSubtitle.Text(context.euiccManager.isEnabled.toString())
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        }
    }

    private fun eSimOSVersion() = Item(R.string.network_title_esim_os_version, ItemType.NETWORK).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                subtitle = ItemSubtitle.Text(context.euiccManager.euiccInfo?.osVersion)
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        }
    }
}
