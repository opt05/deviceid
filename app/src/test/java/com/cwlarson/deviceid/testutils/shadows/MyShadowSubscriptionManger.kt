package com.cwlarson.deviceid.testutils.shadows

import android.Manifest
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowSubscriptionManager

@Suppress("unused", "UNUSED_PARAMETER")
@Implements(value = SubscriptionManager::class)
class MyShadowSubscriptionManager: ShadowSubscriptionManager() {
    private var phoneNumber: String = ""

    fun setPhoneNumber(number: String) {
        phoneNumber = number
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_NUMBERS)
    @Implementation
    fun getPhoneNumber(subscriptionId: Int): String = phoneNumber
}