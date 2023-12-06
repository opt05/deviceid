package com.cwlarson.deviceid.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class SystemUtilsTest {
    @Test
    fun `Verify gmsPackageInfo returns package info when available on Android 13+`() {
        val context: Context = mockk()
        val packageInfo: PackageInfo = mockk()
        every { context.packageManager } returns mockk {
            every {
                getPackageInfo(
                    "com.google.android.gms", any<PackageManager.PackageInfoFlags>()
                )
            } returns packageInfo
        }
        assertEquals(packageInfo, context.gmsPackageInfo)
    }

    @Config(sdk = [Build.VERSION_CODES.S_V2])
    @Test
    fun `Verify gmsPackageInfo returns package info when available on older Android`() {
        val context: Context = mockk()
        val packageInfo: PackageInfo = mockk()
        every { context.packageManager } returns mockk {
            every {
                getPackageInfo("com.google.android.gms", 0)
            } returns packageInfo
        }
        assertEquals(packageInfo, context.gmsPackageInfo)
    }

    @Test
    fun `Verify gmsPackageInfo returns null when name not found exception on Android 13+`() {
        val context: Context = mockk()
        every { context.packageManager } returns mockk {
            every {
                getPackageInfo(
                    "com.google.android.gms", any<PackageManager.PackageInfoFlags>()
                )
            } throws PackageManager.NameNotFoundException()
        }
        assertNull(context.gmsPackageInfo)
    }

    @Config(sdk = [Build.VERSION_CODES.S_V2])
    @Test
    fun `Verify gmsPackageInfo returns null when name not found exception on older Android`() {
        val context: Context = mockk()
        every { context.packageManager } returns mockk {
            every {
                getPackageInfo("com.google.android.gms", 0)
            } throws PackageManager.NameNotFoundException()
        }
        assertNull(context.gmsPackageInfo)
    }

    @Test
    fun `Verify gmsPackageInfo returns null when other exception on Android 13+`() {
        val context: Context = mockk()
        every { context.packageManager } returns mockk {
            every {
                getPackageInfo(
                    "com.google.android.gms", any<PackageManager.PackageInfoFlags>()
                )
            } throws NullPointerException()
        }
        assertNull(context.gmsPackageInfo)
    }

    @Config(sdk = [Build.VERSION_CODES.S_V2])
    @Test
    fun `Verify gmsPackageInfo returns null when other exception on older Android`() {
        val context: Context = mockk()
        every { context.packageManager } returns mockk {
            every {
                getPackageInfo("com.google.android.gms", 0)
            } throws NullPointerException()
        }
        assertNull(context.gmsPackageInfo)
    }

    @Test
    fun `Verify systemProperty returns string when available`() {
        val context: Context = mockk()
        every { context.classLoader } returns mockk {
            every { loadClass("android.os.SystemProperties") } returns TestClass().javaClass
        }
        assertEquals("lorem ipsum", context.systemProperty("something"))
    }

    @Test
    fun `Verify systemProperty returns null when exception`() {
        val context: Context = mockk()
        every { context.classLoader } returns mockk {
            every { loadClass("android.os.SystemProperties") } returns this.javaClass
        }
        assertNull(context.systemProperty("something"))
    }

    class TestClass {
        companion object {
            @JvmStatic
            @Suppress("UNUSED_PARAMETER", "SameReturnValue")
            fun get(key: String): String = "lorem ipsum"
        }
    }
}