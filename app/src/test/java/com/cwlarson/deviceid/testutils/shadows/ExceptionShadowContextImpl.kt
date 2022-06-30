package com.cwlarson.deviceid.testutils.shadows

import android.content.ContentResolver
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowContextImpl

@Implements(className = ShadowContextImpl.CLASS_NAME)
class ExceptionShadowContextImpl : ShadowContextImpl() {
    @Implementation
    override fun getContentResolver(): ContentResolver {
        throw NullPointerException()
    }
}