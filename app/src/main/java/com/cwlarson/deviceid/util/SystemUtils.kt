package com.cwlarson.deviceid.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
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
        packageManager.getPackageInfo("com.google.android.gms", 0)
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
    methodGet.invoke(systemProperties, key) as String
} catch (e: Throwable) {
    null
}

@Composable
fun <T : R, R> Flow<T>.collectAsStateWithLifecycle(
    initial: R,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<R> {
    val lifecycleOwner = checkNotNull(LocalLifecycleOwner.current)
    return remember(this, lifecycleOwner) {
        flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
    }.collectAsState(initial = initial)
}