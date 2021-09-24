package com.cwlarson.deviceid.testutils.shadows

import android.os.Environment
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowEnvironment

@Implements(Environment::class)
class ExceptionShadowEnvironment : ShadowEnvironment() {
    @Implementation
    fun getExternalStorageState() {
        throw NullPointerException("")
    }
    @Implementation
    fun isExternalStorageEmulated() {
        throw NullPointerException("")
    }
}