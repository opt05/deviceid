package com.cwlarson.deviceid.testutils.shadows

import android.hardware.display.DisplayManager
import android.view.Display
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowDisplayManager

@Suppress("unused", "UNUSED_PARAMETER")
@Implements(DisplayManager::class)
class ExceptionShadowDisplayManager : ShadowDisplayManager() {
    @Implementation
    fun getDisplay(displayId: Int): Display {
        throw NullPointerException()
    }
}