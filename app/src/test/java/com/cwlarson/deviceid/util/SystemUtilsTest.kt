package com.cwlarson.deviceid.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SystemUtilsTest {

    @Test
    fun `Verify gmsPackageInfo returns package info when available`() {
        val context: Context = mock()
        val packageInfo: PackageInfo = mock()
        val packageManager: PackageManager = mock()
        whenever(context.packageManager).doReturn(packageManager)
        whenever(packageManager.getPackageInfo("com.google.android.gms", 0))
            .doReturn(packageInfo)
        assertEquals(packageInfo, context.gmsPackageInfo)
    }

    @Test
    fun `Verify gmsPackageInfo returns null when name not found exception`() {
        val context: Context = mock()
        val packageManager: PackageManager = mock()
        whenever(context.packageManager).doReturn(packageManager)
        whenever(packageManager.getPackageInfo("com.google.android.gms", 0))
            .thenThrow(PackageManager.NameNotFoundException())
        assertNull(context.gmsPackageInfo)
    }

    @Test
    fun `Verify gmsPackageInfo returns null when other exception`() {
        val context: Context = mock()
        val packageManager: PackageManager = mock()
        whenever(context.packageManager).doReturn(packageManager)
        whenever(packageManager.getPackageInfo("com.google.android.gms", 0))
            .thenThrow(NullPointerException())
        assertNull(context.gmsPackageInfo)
    }

    @Test
    fun `Verify systemProperty returns string when available`() {
        val context: Context = mock()
        val classLoader: ClassLoader = mock()
        whenever(context.classLoader).doReturn(classLoader)
        whenever(classLoader.loadClass("android.os.SystemProperties"))
            .doReturn(TestClass().javaClass)
        assertEquals("lorem ipsum", context.systemProperty("something"))
    }

    @Test
    fun `Verify systemProperty returns null when exception`() {
        val context: Context = mock()
        val classLoader: ClassLoader = mock()
        whenever(context.classLoader).doReturn(classLoader)
        whenever(classLoader.loadClass("android.os.SystemProperties"))
            .doReturn(this.javaClass)
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