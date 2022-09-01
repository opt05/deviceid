package com.cwlarson.deviceid.testutils.shadows

import android.telephony.euicc.EuiccInfo
import android.telephony.euicc.EuiccManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowEuiccManager

@Suppress("unused")
@Implements(EuiccManager::class)
class MyShadowEuiccManager: ShadowEuiccManager() {
    private var euiccInfo: EuiccInfo? = null

    fun setEuiccInfo(info: EuiccInfo?) {
        euiccInfo = info
    }

    @Implementation
    fun getEuiccInfo(): EuiccInfo? = euiccInfo
}