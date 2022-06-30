package com.cwlarson.deviceid.testutils.shadows

import android.telephony.TelephonyManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowTelephonyManager

@Suppress("unused")
@Implements(TelephonyManager::class)
class MyShadowTelephonyManager : ShadowTelephonyManager() {
    private var nai: String = ""
    private var manufacturerCode = ""
    private var isConcurrentVoiceAndDataSupported: Boolean = false
    private var isDataRoamingEnabled: Boolean = false
    private var isMultiSimSupported: Int = TelephonyManager.MULTISIM_NOT_SUPPORTED_BY_HARDWARE
    private var activeModemCount: Int = 1

    fun setNai(value: String) {
        nai = value
    }

    @Implementation
    fun getNai(): String = nai

    fun setIsConcurrentVoiceAndDataSupported(value: Boolean) {
        isConcurrentVoiceAndDataSupported = value
    }

    @Implementation
    fun isConcurrentVoiceAndDataSupported() = isConcurrentVoiceAndDataSupported

    fun setIsDataRoamingEnabled(value: Boolean) {
        isDataRoamingEnabled = value
    }

    @Implementation
    fun isDataRoamingEnabled() = isDataRoamingEnabled

    fun setIsMultiSimSupported(value: Int) {
        isMultiSimSupported = value
    }

    @Implementation
    fun isMultiSimSupported(): Int = isMultiSimSupported

    fun setManufacturerCode(value: String) {
        manufacturerCode = value
    }

    @Implementation
    fun getManufacturerCode(): String = manufacturerCode

    fun setActiveModemCount(value: Int) {
        activeModemCount = value
    }

    @Implementation
    fun getActiveModemCount() = activeModemCount
}