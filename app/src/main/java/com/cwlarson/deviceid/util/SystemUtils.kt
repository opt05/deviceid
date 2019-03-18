@file:JvmName("SystemUtils") // pretty name for utils class if called from
package com.cwlarson.deviceid.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.cwlarson.deviceid.databinding.UnavailablePermission

fun Context?.calculateNoOfColumns(): Int {
    this?.let {
        val displayMetrics = it.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / 300).toInt()
    } ?: return 1
}

fun Context?.calculateBottomSheetMaxWidth(): Int {
    this?.let {
        val displayMetrics = it.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return if(dpWidth > 750) 750 else ViewGroup.LayoutParams.MATCH_PARENT
    } ?: return ViewGroup.LayoutParams.MATCH_PARENT
}

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

fun Context.hasPermission(permission: UnavailablePermission): Boolean = when (permission) {
    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE ->
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
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