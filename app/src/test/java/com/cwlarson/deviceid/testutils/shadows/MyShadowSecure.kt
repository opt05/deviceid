package com.cwlarson.deviceid.testutils.shadows

import android.provider.Settings
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowSettings

@Implements(value = Settings.Secure::class)
object MyShadowSecure : ShadowSettings.ShadowSecure() {
    fun setAndroidID(value: String) {
        putString(
            RuntimeEnvironment.getApplication().contentResolver,
            Settings.Secure.ANDROID_ID,
            value
        )
    }
}