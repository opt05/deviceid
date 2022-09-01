package com.cwlarson.deviceid.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.*
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.AppPermission
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.isGranted
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.net.InetAddress
import javax.inject.Inject

class NetworkRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val context: Context,
    preferenceManager: PreferenceManager
) : TabData(dispatcherProvider, context, preferenceManager) {
    private val connectivityManager by lazy { context.getSystemService<ConnectivityManager>() }
    private val wifiManager by lazy { context.getSystemService<WifiManager>() }
    private val telephonyManager by lazy { context.getSystemService<TelephonyManager>() }
    private val bluetoothManager by lazy { context.getSystemService<BluetoothManager>() }
    private val euiccManager by lazy { context.getSystemService<EuiccManager>() }

    @OptIn(FlowPreview::class)
    override fun items(): Flow<List<Item>> = flowOf(
        wifiInfo(), flowOf(
            listOf(
                deviceSoftwareVersion(), bluetoothMac(), bluetoothHostname(), manufacturerCode(),
                nai(), phoneCount(), simSerial(), simOperatorName(), simCountry(), simState(),
                phoneNumber(), voicemailNumber(), cellNetworkName(), cellNetworkType(),
                cellNetworkClass(), eSimID(), eSimEnabled(), eSimOSVersion(),
                isConcurrentVoiceAndDataSupported(), isDataRoamingEnabled(),
                isHearingAidSupported(), isMultiSimSupported(), isRttSupported(), isSmsCapable(),
                isVoiceCapable()
            )
        )
    ).flattenMerge().flowOn(dispatcherProvider.IO)

    @SuppressLint("MissingPermission")
    private fun deviceSoftwareVersion() = Item(
        title = R.string.network_title_device_software_version, itemType = ItemType.NETWORK,
        subtitle = try {
            if (context.isGranted(AppPermission.ReadPhoneState)) {
                telephonyManager?.let { ItemSubtitle.Text(it.deviceSoftwareVersion) }
                    ?: ItemSubtitle.Error
            } else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    /**
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds", "MissingPermission")
    private fun wifiMac(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_mac, itemType = ItemType.NETWORK,
        subtitle = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                wifiInfo?.let { ItemSubtitle.Text(it.macAddress) } ?: ItemSubtitle.Error
            } catch (e: Throwable) {
                Timber.w(e)
                ItemSubtitle.Error
            }
        } else ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.M)
    )

    private fun wifiBSSID(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_bssid, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (context.isGranted(AppPermission.AccessFineLocation))
                        ItemSubtitle.Text(it.bssid)
                    else ItemSubtitle.Permission(AppPermission.AccessFineLocation)
                } else ItemSubtitle.Text(it.bssid)
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiSSID(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_ssid, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (context.isGranted(AppPermission.AccessFineLocation))
                        ItemSubtitle.Text(it.ssid)
                    else ItemSubtitle.Permission(AppPermission.AccessFineLocation)
                } else ItemSubtitle.Text(it.ssid)
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiFrequency(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_frequency, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let {
                ItemSubtitle.Text("${it.frequency}${WifiInfo.FREQUENCY_UNITS}")
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiHiddenSSID(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_hidden_ssid, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let { ItemSubtitle.Text("${it.hiddenSSID}") } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiIpAddress(address: List<InetAddress>) = Item(
        title = R.string.network_title_wifi_ip_address, itemType = ItemType.NETWORK,
        subtitle = try {
            if(address.isEmpty()) ItemSubtitle.Error
            else ItemSubtitle.Text(address.mapNotNull { it.hostAddress }.joinToString())
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiLinkSpeed(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_link_speed, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let {
                ItemSubtitle.Text("${it.linkSpeed}${WifiInfo.LINK_SPEED_UNITS}")
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiTxLinkSpeed(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_tx_link_speed, itemType = ItemType.NETWORK,
        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                wifiInfo?.let {
                    ItemSubtitle.Text("${it.txLinkSpeedMbps}${WifiInfo.LINK_SPEED_UNITS}")
                } ?: ItemSubtitle.Error
            } catch (e: Throwable) {
                Timber.w(e)
                ItemSubtitle.Error
            }
        } else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
    )

    private fun wifiNetworkID(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_network_id, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let { ItemSubtitle.Text("${it.networkId}") } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiPasspointFqdn(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_passpoint_fqdn, itemType = ItemType.NETWORK,
        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                wifiInfo?.let { ItemSubtitle.Text(it.passpointFqdn) } ?: ItemSubtitle.Error
            } catch (e: Throwable) {
                Timber.w(e)
                ItemSubtitle.Error
            }
        } else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
    )

    private fun wifiPasspointProviderFriendlyName(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_passpoint_friendly_name, itemType = ItemType.NETWORK,
        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                wifiInfo?.let {
                    ItemSubtitle.Text(it.passpointProviderFriendlyName)
                } ?: ItemSubtitle.Error
            } catch (e: Throwable) {
                Timber.w(e)
                ItemSubtitle.Error
            }
        } else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
    )

    private fun wifiRSSI(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_rssid, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiInfo?.let { ItemSubtitle.Text("${it.rssi}") } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiSignalLevel(wifiInfo: WifiInfo?) = Item(
        title = R.string.network_title_wifi_signal_level, itemType = ItemType.NETWORK,
        subtitle = try {
            wifiManager?.let { manager ->
                wifiInfo?.let { info ->
                    ItemSubtitle.Text(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            "${manager.calculateSignalLevel(info.rssi)}%" //TODO Fix this
                        else
                            @Suppress("DEPRECATION")
                            "${WifiManager.calculateSignalLevel(info.rssi, 100)}%"
                    )
                } ?: ItemSubtitle.Error
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiHostname(address: List<InetAddress>) = Item(
        title = R.string.network_title_wifi_hostname,
        itemType = ItemType.NETWORK,
        subtitle = try {
            if(address.isEmpty()) ItemSubtitle.Error
            else ItemSubtitle.Text(address.joinToString { it.hostName })
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun wifiCanonicalHostname(address: List<InetAddress>) = Item(
        title = R.string.network_title_wifi_canonical_hostname,
        itemType = ItemType.NETWORK,
        subtitle = try {
            if(address.isEmpty()) ItemSubtitle.Error
            else ItemSubtitle.Text(address.joinToString { it.canonicalHostName })
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun wifiInfo() = callbackFlow {
        val map = mutableMapOf(0 to wifiMac(null))
        val listener = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                onUnavailable()
            }

            override fun onUnavailable() {
                map[0] = wifiMac(null)
                map[1] = wifiBSSID(null)
                map[2] = wifiSSID(null)
                map[3] = wifiFrequency(null)
                map[4] = wifiHiddenSSID(null)
                map[5] = wifiIpAddress(emptyList())
                map[6] = wifiLinkSpeed(null)
                map[7] = wifiTxLinkSpeed(null)
                map[8] = wifiNetworkID(null)
                map[9] = wifiPasspointFqdn(null)
                map[10] = wifiPasspointProviderFriendlyName(null)
                map[11] = wifiRSSI(null)
                map[12] = wifiSignalLevel(null)
                map[13] = wifiHostname(emptyList())
                map[14] = wifiCanonicalHostname(emptyList())
                trySend(map.values.toList())
            }

            override fun onLinkPropertiesChanged(
                network: Network, linkProperties: LinkProperties
            ) {
                val ipAddresses = linkProperties.linkAddresses.map { it.address }
                map[5] = wifiIpAddress(ipAddresses)
                map[13] = wifiHostname(ipAddresses)
                map[14] = wifiCanonicalHostname(ipAddresses)
                trySend(map.values.toList())
            }

            override fun onCapabilitiesChanged(
                network: Network, networkCapabilities: NetworkCapabilities
            ) {
                val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    networkCapabilities.transportInfo as? WifiInfo
                else @Suppress("DEPRECATION") wifiManager?.connectionInfo
                map[0] = wifiMac(wifiInfo)
                map[1] = wifiBSSID(wifiInfo)
                map[2] = wifiSSID(wifiInfo)
                map[3] = wifiFrequency(wifiInfo)
                map[4] = wifiHiddenSSID(wifiInfo)
                map[6] = wifiLinkSpeed(wifiInfo)
                map[7] = wifiTxLinkSpeed(wifiInfo)
                map[8] = wifiNetworkID(wifiInfo)
                map[9] = wifiPasspointFqdn(wifiInfo)
                map[10] = wifiPasspointProviderFriendlyName(wifiInfo)
                map[11] = wifiRSSI(wifiInfo)
                map[12] = wifiSignalLevel(wifiInfo)
                trySend(map.values.toList())
            }
        }
        if (map.isEmpty()) listener.onUnavailable()
        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
            listener
        )
        awaitClose { connectivityManager?.unregisterNetworkCallback(listener) }
    }.conflate()

    /**
     * Marshmallow has started to depreciate this method
     * http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
     */
    @SuppressLint("HardwareIds", "MissingPermission")
    private fun bluetoothMac() = Item(
        title = R.string.network_title_bluetooth_mac, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                bluetoothManager?.let {
                    ItemSubtitle.Text(it.adapter.address)
                } ?: ItemSubtitle.Error
            } else {
                ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.M)
            }
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    private fun bluetoothHostname() = Item(
        title = R.string.network_title_bluetooth_hostname, itemType = ItemType.NETWORK,
        subtitle = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (context.isGranted(AppPermission.AccessBluetoothConnect))
                        bluetoothManager?.let { ItemSubtitle.Text(it.adapter.name) }
                            ?: ItemSubtitle.Error
                    else ItemSubtitle.Permission(AppPermission.AccessBluetoothConnect)
                }
                else -> bluetoothManager?.let { ItemSubtitle.Text(it.adapter.name) }
                    ?: ItemSubtitle.Error
            }
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun manufacturerCode() = Item(
        title = R.string.network_title_manufacturer_code, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                telephonyManager?.let {
                    ItemSubtitle.Text(it.manufacturerCode)
                } ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    private fun nai() = Item(
        title = R.string.network_title_nai, itemType = ItemType.NETWORK,
        subtitle = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                    ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.P)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->
                    if (context.isGranted(AppPermission.ReadPhoneState))
                        telephonyManager?.let { ItemSubtitle.Text(it.nai) } ?: ItemSubtitle.Error
                    else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
                else -> ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
            }
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun phoneCount() = Item(
        title = R.string.network_title_phone_count, itemType = ItemType.NETWORK,
        subtitle = try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                    telephonyManager?.let { ItemSubtitle.Text("${it.activeModemCount}") }
                        ?: ItemSubtitle.Error
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    @Suppress("DEPRECATION")
                    telephonyManager?.let { ItemSubtitle.Text("${it.phoneCount}") }
                        ?: ItemSubtitle.Error
                else -> ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
            }
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun simSerial() = Item(
        title = R.string.network_title_sim_serial, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                telephonyManager?.let { ItemSubtitle.Text(it.simSerialNumber) }
                    ?: ItemSubtitle.Error
            else ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.Q)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun simOperatorName() = Item(
        title = R.string.network_title_sim_operator, itemType = ItemType.NETWORK,
        subtitle = try {
            telephonyManager?.let { ItemSubtitle.Text(it.simOperatorName) } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun simCountry() = Item(
        title = R.string.network_title_sim_country, itemType = ItemType.NETWORK,
        subtitle = try {
            telephonyManager?.let { ItemSubtitle.Text(it.simCountryIso) } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun simState() = Item(
        title = R.string.network_title_sim_state, itemType = ItemType.NETWORK,
        subtitle = try {
            telephonyManager?.let {
                ItemSubtitle.Text(
                    when (it.simState) {
                        TelephonyManager.SIM_STATE_ABSENT ->
                            "No SIM card is available in the device"
                        TelephonyManager.SIM_STATE_NETWORK_LOCKED ->
                            "Locked: requires a network PIN to unlock"
                        TelephonyManager.SIM_STATE_PIN_REQUIRED ->
                            "Locked: requires the user's SIM PIN to unlock"
                        TelephonyManager.SIM_STATE_PUK_REQUIRED ->
                            "Locked: requires the user's SIM PUK to unlock"
                        TelephonyManager.SIM_STATE_READY -> "Ready"
                        TelephonyManager.SIM_STATE_NOT_READY -> "Not ready"
                        TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently disabled"
                        TelephonyManager.SIM_STATE_UNKNOWN -> "Network unknown"
                        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Error, present but faulty"
                        TelephonyManager.SIM_STATE_CARD_RESTRICTED ->
                            "Restricted, present but not usable due to carrier restrictions"
                        else -> "Network unknown"
                    }
                )
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun phoneNumber() = Item(
        title = R.string.network_title_phone_number, itemType = ItemType.NETWORK,
        subtitle = try {
            if (context.isGranted(AppPermission.ReadPhoneState))
                telephonyManager?.let { ItemSubtitle.Text(it.line1Number) } ?: ItemSubtitle.Error
            else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    private fun voicemailNumber() = Item(
        title = R.string.network_title_voicemail_number, itemType = ItemType.NETWORK,
        subtitle = try {
            if (context.isGranted(AppPermission.ReadPhoneState))
                telephonyManager?.let { ItemSubtitle.Text(it.voiceMailNumber) }
                    ?: ItemSubtitle.Error
            else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun cellNetworkName() = Item(
        title = R.string.network_title_cell_network_name, itemType = ItemType.NETWORK,
        subtitle = try {
            telephonyManager?.let { ItemSubtitle.Text(it.networkOperatorName) }
                ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun cellNetworkType() = Item(
        title = R.string.network_title_cell_network_type, itemType = ItemType.NETWORK,
        subtitle = try {
            if (context.isGranted(AppPermission.ReadPhoneState))
                telephonyManager?.let {
                    ItemSubtitle.Text(
                        when (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) it.dataNetworkType else it.networkType) {
                            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "CDMA - EvDo rev. 0"
                            TelephonyManager.NETWORK_TYPE_EVDO_A -> "CDMA - EvDo rev. A"
                            TelephonyManager.NETWORK_TYPE_EVDO_B -> "CDMA - EvDo rev. B"
                            TelephonyManager.NETWORK_TYPE_1xRTT -> "CDMA - 1xRTT"
                            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                            TelephonyManager.NETWORK_TYPE_EHRPD -> "CDMA - eHRPD"
                            TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
                            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
                            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
                            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
                            TelephonyManager.NETWORK_TYPE_NR -> "5G"
                            else -> null
                        }
                    )
                } ?: ItemSubtitle.Error
            else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun cellNetworkClass() = Item(
        title = R.string.network_title_cell_network_class, itemType = ItemType.NETWORK,
        subtitle = try {
            if (context.isGranted(AppPermission.ReadPhoneState))
                telephonyManager?.let {
                    ItemSubtitle.Text(
                        when (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) it.dataNetworkType else it.networkType) {
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
                        }
                    )
                } ?: ItemSubtitle.Error
            else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun isConcurrentVoiceAndDataSupported() = Item(
        title = R.string.network_title_concurrent_voice_data_supported, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                telephonyManager?.let {
                    ItemSubtitle.Text("${it.isConcurrentVoiceAndDataSupported}")
                } ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.O)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    private fun isDataRoamingEnabled() = Item(
        title = R.string.network_title_data_roaming_enabled, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                if (context.isGranted(AppPermission.ReadPhoneState))
                    telephonyManager?.let {
                        ItemSubtitle.Text("${it.isDataRoamingEnabled}")
                    } ?: ItemSubtitle.Error
                else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun isHearingAidSupported() = Item(
        title = R.string.network_title_hearing_aid_supported, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                telephonyManager?.let {
                    ItemSubtitle.Text("${it.isHearingAidCompatibilitySupported}")
                } ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("MissingPermission")
    private fun isMultiSimSupported() = Item(
        title = R.string.network_title_multi_sim_supported, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                if (context.isGranted(AppPermission.ReadPhoneState))
                    telephonyManager?.let {
                        ItemSubtitle.Text(
                            when (it.isMultiSimSupported) {
                                TelephonyManager.MULTISIM_ALLOWED ->
                                    "Supports multiple SIMs"
                                TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_HARDWARE ->
                                    "Device does not support multiple SIMs"
                                TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_CARRIER ->
                                    "Device does support multiple SIMS but restricted by carrier"
                                else -> null
                            }
                        )
                    } ?: ItemSubtitle.Error
                else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun isRttSupported() = Item(
        title = R.string.network_title_rtt_supported, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                telephonyManager?.let {
                    ItemSubtitle.Text("${it.isRttSupported}")
                } ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun isSmsCapable() = Item(
        title = R.string.network_title_sms_capable, itemType = ItemType.NETWORK,
        subtitle = try {
            telephonyManager?.let { ItemSubtitle.Text("${it.isSmsCapable}") } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun isVoiceCapable() = Item(
        title = R.string.network_title_voice_capable, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                telephonyManager?.let { ItemSubtitle.Text("${it.isVoiceCapable}") }
                    ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.LOLLIPOP_MR1)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun eSimID() = Item(
        title = R.string.network_title_esim_id, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                euiccManager?.let { ItemSubtitle.Text("${it.eid}") } ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun eSimEnabled() = Item(
        title = R.string.network_title_esim_enabled, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                euiccManager?.let { ItemSubtitle.Text("${it.isEnabled}") } ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun eSimOSVersion() = Item(
        title = R.string.network_title_esim_os_version, itemType = ItemType.NETWORK,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                euiccManager?.let { ItemSubtitle.Text(it.euiccInfo?.osVersion) }
                    ?: ItemSubtitle.Error
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.P)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )
}
