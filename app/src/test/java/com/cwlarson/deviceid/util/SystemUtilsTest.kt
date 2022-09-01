package com.cwlarson.deviceid.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SystemUtilsTest {

    @Test
    fun `Verify gmsPackageInfo returns package info when available`() {
        val context: Context = mockk()
        val packageInfo: PackageInfo = mockk()
        val packageManager: PackageManager = mockk()
        every { context.packageManager } returns packageManager
        every { packageManager.getPackageInfo("com.google.android.gms", 0) } returns packageInfo
        assertEquals(packageInfo, context.gmsPackageInfo)
    }

    @Test
    fun `Verify gmsPackageInfo returns null when name not found exception`() {
        val context: Context = mockk()
        val packageManager: PackageManager = mockk()
        every { context.packageManager } returns packageManager
        every { packageManager.getPackageInfo("com.google.android.gms", 0) } throws PackageManager
            .NameNotFoundException()
        assertNull(context.gmsPackageInfo)
    }

    @Test
    fun `Verify gmsPackageInfo returns null when other exception`() {
        val context: Context = mockk()
        val packageManager: PackageManager = mockk()
        every { context.packageManager } returns packageManager
        every { packageManager.getPackageInfo("com.google.android.gms", 0) } throws
                NullPointerException()
        assertNull(context.gmsPackageInfo)
    }

    @Test
    fun `Verify systemProperty returns string when available`() {
        val context: Context = mockk()
        val classLoader: ClassLoader = mockk()
        every { context.classLoader } returns classLoader
        every { classLoader.loadClass("android.os.SystemProperties") } returns TestClass().javaClass
        assertEquals("lorem ipsum", context.systemProperty("something"))
    }

    @Test
    fun `Verify systemProperty returns null when exception`() {
        val context: Context = mockk()
        val classLoader: ClassLoader = mockk()
        every { context.classLoader } returns classLoader
        every { classLoader.loadClass("android.os.SystemProperties") } returns this.javaClass
        assertNull(context.systemProperty("something"))
    }

    class TestClass {
        companion object {
            @JvmStatic
            @Suppress("UNUSED_PARAMETER")
            fun get(key: String): String = "lorem ipsum"
        }
    }
}