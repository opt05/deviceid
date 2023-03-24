package com.cwlarson.deviceid.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import timber.log.Timber

/**
 * A [Timber.Tree] for debug builds.
 * Automatically shows a Hyperlink to the calling Class and Linenumber in the Logs.
 * Allows quick lookup of the caller source just by clicking on the Hyperlink in the Log.
 */
class HyperlinkedDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        with(element) { return "($fileName:$lineNumber) $methodName" }
    }
}

inline val Context.gmsPackageInfo: PackageInfo?
    get() = try {
        val name = "com.google.android.gms"
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                packageManager.getPackageInfo(name, PackageManager.PackageInfoFlags.of(0))
            else -> @Suppress("DEPRECATION") packageManager.getPackageInfo(name, 0)
        }
    } catch (e: Throwable) {
        null
    }

/**
 * Get system properties not available via Android SDK
 */
fun Context.systemProperty(key: String): String? = try {
    @SuppressLint("PrivateApi")
    val systemProperties = classLoader.loadClass("android.os.SystemProperties")
    val methodGet = systemProperties.getMethod("get", String::class.java)
    methodGet(systemProperties, key) as String
} catch (e: Throwable) {
    null
}