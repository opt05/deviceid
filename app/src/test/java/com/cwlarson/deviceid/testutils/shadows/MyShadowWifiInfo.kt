package com.cwlarson.deviceid.testutils.shadows

import android.net.wifi.WifiInfo
import android.os.Build
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow.directlyOn
import org.robolectric.shadows.ShadowWifiInfo
import org.robolectric.util.ReflectionHelpers

@Implements(WifiInfo::class)
class MyShadowWifiInfo: ShadowWifiInfo() {
    private var txLinkSpeed: Int = 0
    private var passpointFQDN: String = ""
    private var passpointName: String = ""

    fun setTxLinkSpeedMbps(value: Int) {
        txLinkSpeed = value
    }

    @Implementation
    fun getTxLinkSpeedMbps() = txLinkSpeed

    fun setPasspointFqdn(value: String) {
        passpointFQDN = value
    }

    @Implementation
    fun getPasspointFqdn(): String = passpointFQDN

    fun setPasspointProviderFriendlyName(value: String) {
        passpointName = value
    }

    @Implementation
    fun getPasspointProviderFriendlyName() = passpointName
}