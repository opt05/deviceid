package com.cwlarson.deviceid.testutils.shadows

import android.net.wifi.WifiInfo
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowWifiInfo

@Implements(WifiInfo::class)
class ExceptionShadowWifiInfo: ShadowWifiInfo() {
    @Implementation
    fun getMacAddress(): String { throw NullPointerException() }

    @Implementation
    fun getBSSID(): String { throw NullPointerException() }

    @Implementation
    fun getSSID(): String { throw NullPointerException() }

    @Implementation
    fun getFrequency(): Int { throw NullPointerException() }

    @Implementation
    fun getHiddenSSID(): Boolean { throw NullPointerException() }

    @Implementation
    fun getIpAddress(): Int { throw NullPointerException() }

    @Implementation
    fun getLinkSpeed(): Int { throw NullPointerException() }

    @Implementation
    fun getTxLinkSpeedMbps(): Int { throw NullPointerException() }

    @Implementation
    fun getNetworkId(): Int { throw NullPointerException() }

    @Implementation
    fun getPasspointFqdn(): String { throw NullPointerException() }

    @Implementation
    fun getPasspointProviderFriendlyName(): String { throw NullPointerException() }

    @Implementation
    fun getRssi(): Int { throw NullPointerException() }

    @Implementation
    fun getHostName(): String { throw NullPointerException() }

    @Implementation
    fun getCanonicalHostName(): String { throw NullPointerException() }
}