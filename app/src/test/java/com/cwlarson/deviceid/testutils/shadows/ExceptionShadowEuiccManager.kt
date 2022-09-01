package com.cwlarson.deviceid.testutils.shadows

import android.telephony.euicc.EuiccInfo
import android.telephony.euicc.EuiccManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowEuiccManager

@Suppress("unused")
@Implements(EuiccManager::class)
class ExceptionShadowEuiccManager: ShadowEuiccManager() {

    @Implementation
    override fun getEid(): String {
        throw NullPointerException()
    }

    @Implementation
    override fun isEnabled(): Boolean {
        throw NullPointerException()
    }

    @Implementation
    fun getEuiccInfo(): EuiccInfo {
        throw NullPointerException()
    }
}