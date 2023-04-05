package com.cwlarson.deviceid.testutils.shadows

import android.telephony.SubscriptionManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowSubscriptionManager

@Suppress("unused", "UNUSED_PARAMETER")
@Implements(value = SubscriptionManager::class)
class ExceptionShadowSubscriptionManager: ShadowSubscriptionManager() {
    @Implementation
    fun getPhoneNumber(subscriptionId: Int): String {
        throw NullPointerException()
    }
}