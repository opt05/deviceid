package com.cwlarson.deviceid.testutils.shadows

import android.os.Build
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowBuild
import org.robolectric.shadows.ShadowSystemProperties
import org.robolectric.util.ReflectionHelpers

@Implements(value = Build::class)
class MyShadowBuild : ShadowBuild() {
    companion object {
        fun setSerial(value: String) {
            ReflectionHelpers.setStaticField(Build::class.java, "SERIAL", value)
        }

        fun setPreviewSdkInt(value: Int) {
            ReflectionHelpers.setStaticField(Build.VERSION::class.java, "PREVIEW_SDK_INT", value)
        }

        fun setBuildVersion(value: String) {
            ReflectionHelpers.setStaticField(Build::class.java, "DISPLAY", value)
        }

        fun setBuildDate(value: Long) {
            ReflectionHelpers.setStaticField(Build::class.java, "TIME", value)
        }

        fun setBuildBoard(value: String) {
            ReflectionHelpers.setStaticField(Build::class.java, "BOARD", value)
        }

        fun setBuildBootloader(value: String) {
            ReflectionHelpers.setStaticField(Build::class.java, "BOOTLOADER", value)
        }

        fun setBuildHost(value: String) {
            ReflectionHelpers.setStaticField(Build::class.java, "HOST", value)
        }

        fun setBuildUser(value: String) {
            ReflectionHelpers.setStaticField(Build::class.java, "USER", value)
        }
    }
}