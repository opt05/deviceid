@file:JvmName("SystemUtils")
package com.cwlarson.deviceid.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.UnavailablePermission
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
 * Determine if the requested permission has be granted or not
 * @return if granted or not
 * TODO: Move to own class?
 */
fun Context.hasPermission(permission: UnavailablePermission): Boolean = when (permission) {
    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE ->
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE ->
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}

/**
 * Sets the Android Recent App background color based on if night mode or not.
 * Only affects Lollipop and above
 */
suspend fun Activity.setTaskDescription() = withContext(Dispatchers.IO) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return@withContext
    val color = TypedValue().run {
        val wasResolved = theme.resolveAttribute(R.attr.colorPrimarySurface, this, true)
        ContextCompat.getColor(this@setTaskDescription,
                if (wasResolved) resourceId else R.color.colorPrimary)
    }
    val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        ActivityManager.TaskDescription(getString(R.string.app_name),
                R.mipmap.ic_launcher, color)
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
    val isNight = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        if (makeTranslucent)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = window.decorView.systemUiVisibility
        if (makeTranslucent) {
            flags = if (isNight)
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            else
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(R.color.colorSurface)
        } else {
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.statusBarColor = if (isNight)
                getColor(android.R.color.black)
            else
                getColor(R.color.colorPrimaryVariant)
        }
        window.decorView.systemUiVisibility = flags
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        else {
            window.navigationBarColor = getColor(android.R.color.transparent)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }
}



/**
 * A [Timber.Tree] for debug builds.
 * Automatically shows a Hyperlink to the calling Class and Linenumber in the Logs.
 * Allows quick lookup of the caller source just by clicking on the Hyperlink in the Log.
 */
class HyperlinkedDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        with(element) {
            return "($fileName:$lineNumber) $methodName"
        }
    }
}

val Context.euiccManager: EuiccManager
    @SuppressLint("WrongConstant") @RequiresApi(Build.VERSION_CODES.P)
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