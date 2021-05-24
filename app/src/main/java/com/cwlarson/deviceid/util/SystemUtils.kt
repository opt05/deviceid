@file:JvmName("SystemUtils")
package com.cwlarson.deviceid.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.webkit.WebViewCompat
import com.cwlarson.deviceid.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Class to get system properties not available via Android SDK
 */
class SystemProperty(private val context: Context) {

    @Throws(NoSuchPropertyException::class)
    private fun getOrThrow(key: String): String {
        try {
            val classLoader = context.classLoader

            @SuppressLint("PrivateApi")
            val systemProperties = classLoader.loadClass("android.os.SystemProperties")
            val methodGet = systemProperties.getMethod("get", String::class.java)
            return methodGet.invoke(systemProperties, key) as String
        } catch (e: Throwable) {
            throw NoSuchPropertyException(e)
        }

    }

    operator fun get(key: String): String? {
        return try {
            getOrThrow(key)
        } catch (e: NoSuchPropertyException) {
            null
        }

    }

    private inner class NoSuchPropertyException constructor(e: Throwable) : Throwable(e)
}

/**
 * Sets the Android Recent App background color based on if night mode or not.
 * Only affects Lollipop and above
 */
suspend fun Activity.setTaskDescription() = withContext(Dispatchers.IO) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@withContext
    val color = TypedValue().run {
        val wasResolved = theme.resolveAttribute(R.attr.colorPrimarySurface, this, true)
        ContextCompat.getColor(
            this@setTaskDescription,
            if (wasResolved) resourceId else R.color.colorPrimary
        )
    }
    val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        ActivityManager.TaskDescription(
            getString(R.string.app_name),
            R.mipmap.ic_launcher, color
        )
    else {
        val bm = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        @Suppress("DEPRECATION")
        ActivityManager.TaskDescription(getString(R.string.app_name), bm, color)
    }
    setTaskDescription(desc)
}

/**
 * Sets the status bar to be translucent if no action bar is present, or back to primaryVariant
 * @param makeTranslucent Should the status bar be translucent?
 */
fun Activity.setTranslucentStatus(makeTranslucent: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
    val isNight = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars =
        !isNight && makeTranslucent
    window.statusBarColor = when {
        !makeTranslucent && isNight -> ContextCompat.getColor(this, android.R.color.black)
        !makeTranslucent && !isNight -> ContextCompat.getColor(this, R.color.colorPrimaryVariant)
        else -> ContextCompat.getColor(this, R.color.colorSurface)
    }
    window.navigationBarColor = ContextCompat.getColor(
        this,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) android.R.color.transparent
        else R.color.colorScrim
    )
}


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

val Context.euiccManager: EuiccManager
    @RequiresApi(Build.VERSION_CODES.P)
    get() = applicationContext.getSystemService(Context.EUICC_SERVICE) as EuiccManager

val Context.telephonyManager: TelephonyManager
    get() = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

val Context.bluetoothManager: BluetoothManager
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    get() = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

val Context.wifiManager: WifiManager
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

val Context.activityManager: ActivityManager
    get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

val Context.gmsPackageInfo: PackageInfo
    get() = packageManager.getPackageInfo("com.google.android.gms", 0)

val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

val Context.webViewPackageInfo: PackageInfo?
    get() = WebViewCompat.getCurrentWebViewPackage(this)

val Context.displayManger: DisplayManager
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    get() = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager