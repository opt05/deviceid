package com.cwlarson.deviceid.testutils.shadows

import android.app.ActivityManager
import android.content.pm.ConfigurationInfo
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowActivityManager

@Implements(value = ActivityManager::class, looseSignatures = true)
class ExceptionShadowActivityManager: ShadowActivityManager() {
    @Implementation
    override fun getDeviceConfigurationInfo(): ConfigurationInfo {
        throw NullPointerException("")
    }

    @Implementation
    override fun getMemoryInfo(outInfo: ActivityManager.MemoryInfo?) {
        throw NullPointerException("")
    }
}