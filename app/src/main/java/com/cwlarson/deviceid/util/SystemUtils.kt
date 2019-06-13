@file:JvmName("SystemUtils") // pretty name for utils class if called from
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
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.UnavailablePermission

/**
 * For [androidx.recyclerview.widget.GridLayoutManager] to determine number
 * of columns based on screen width
 * @return number of columns
 */
fun Context?.calculateNoOfColumns(): Int {
    this?.let {
        val displayMetrics = it.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / 300).toInt()
    } ?: return 1
}

/**
 * Determines the bottom sheet's max width should screen size be greater than 750px
 * @return pixel width of the bottom sheet or match_parent dimension
 */
fun Context?.calculateBottomSheetMaxWidth(): Int {
    this?.let {
        val displayMetrics = it.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return if(dpWidth > 750) 750 else ViewGroup.LayoutParams.MATCH_PARENT
    } ?: return ViewGroup.LayoutParams.MATCH_PARENT
}

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
        } catch (e: Exception) {
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

    private inner class NoSuchPropertyException internal constructor(e: Exception) : Exception(e)
}

/**
 * Determine if the requested permission has be granted or not
 * @return if granted or not
 */
fun Context.hasPermission(permission: UnavailablePermission): Boolean = when (permission) {
    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE ->
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
}

fun Activity.setTranslucentStatus(makeTranslucent: Boolean) {
    val isNight = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK)== Configuration.UI_MODE_NIGHT_YES
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        if(makeTranslucent)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = window.decorView.systemUiVisibility
        if(makeTranslucent) {
            flags = if(isNight)
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            else
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = getColor(R.color.colorSurface)
        } else {
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.statusBarColor = if(isNight)
                getColor(android.R.color.black)
            else
                getColor(R.color.colorPrimaryVariant)
        }
        window.decorView.systemUiVisibility = flags
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        else {
            window.navigationBarColor = getColor(android.R.color.transparent)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
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

val Context.windowManager: WindowManager
    get() = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.activityManager: ActivityManager
    get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

val Context.gmsPackageInfo: PackageInfo
    get() = packageManager.getPackageInfo("com.google.android.gms", 0)

val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager