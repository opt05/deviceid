package com.cwlarson.deviceid.testutils.shadows

import android.telephony.SubscriptionManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowSubscriptionManager

@Implements(value = SubscriptionManager::class)
class ExceptionShadowSubscriptionManager: ShadowSubscriptionManager() {
    @Implementation
    override fun getPhoneNumber(subscriptionId: Int): String {
        throw NullPointerException()
    }
}