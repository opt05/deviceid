package com.cwlarson.deviceid.testutils.shadows

import android.os.StatFs
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowStatFs

@Implements(StatFs::class)
class ExceptionShadowStatFs: ShadowStatFs() {
    @Suppress("TestFunctionName")
    @Implementation
    override fun __constructor__(path: String?) {
        throw NullPointerException("")
    }
}