package com.cwlarson.deviceid.testutils.shadows

import android.telephony.TelephonyManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowTelephonyManager

@Suppress("unused")
@Implements(TelephonyManager::class)
class ExceptionShadowTelephonyManager: ShadowTelephonyManager() {
    @Implementation
    override fun getDeviceId(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getDeviceSoftwareVersion(): String {
        throw NullPointerException()
    }

    @Implementation
    fun getManufacturerCode(): String {
        throw NullPointerException()
    }

    @Implementation
    fun getNai(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getActiveModemCount(): Int {
        throw NullPointerException()
    }

    @Implementation
    override fun getPhoneCount(): Int {
        throw NullPointerException()
    }

    @Implementation
    override fun getSimSerialNumber(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getSimOperatorName(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getSimCountryIso(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getSimState(): Int {
        throw NullPointerException()
    }

    @Implementation
    override fun getLine1Number(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getVoiceMailNumber(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getNetworkOperatorName(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun getDataNetworkType(): Int {
        throw NullPointerException()
    }

    @Implementation
    override fun getNetworkType(): Int {
        throw NullPointerException()
    }

    @Implementation
    fun isConcurrentVoiceAndDataSupported(): Boolean {
        throw NullPointerException()
    }

    @Implementation
    fun isDataRoamingEnabled(): Boolean {
        throw NullPointerException()
    }

    @Implementation
    override fun isHearingAidCompatibilitySupported(): Boolean {
        throw NullPointerException()
    }

    @Implementation
    fun isMultiSimSupported(): Int {
        throw NullPointerException()
    }

    @Implementation
    override fun isRttSupported(): Boolean {
        throw NullPointerException()
    }

    @Implementation
    override fun isSmsCapable(): Boolean {
        throw NullPointerException()
    }

    @Implementation
    override fun isVoiceCapable(): Boolean {
        throw NullPointerException()
    }
}