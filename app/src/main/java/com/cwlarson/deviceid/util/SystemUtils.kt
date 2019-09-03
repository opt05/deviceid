@file:JvmName("SystemUtils") // pretty name for utils class if called from
package com.cwlarson.deviceid.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.bluetooth.BluetoothManager
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
 * and updates the dialog as such. This should be run in [BottomSheetDialogFragment.onCreateDialog]
 * @return The same dialog
 */
fun Dialog.calculateBottomSheetMaxWidth(): Dialog {
    setOnShowListener {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        window?.setLayout(if(dpWidth > 750) 750 else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    }
    return this
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

    private inner class NoSuchPropertyException constructor(e: Exception) : Exception(e)
}

/**
 * Determine if the requested permission has be granted or not
 * @return if granted or not
 */
fun Context.hasPermission(permission: UnavailablePermission): Boolean = when (permission) {
    UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE ->
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE ->
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}

/**
 * Sets the Android Recent App background color based on if night mode or not.
 * Only affects Lollipop and above
 */
fun Activity.setTaskDescription() {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
    val color = TypedValue().run {
        val wasResolved = theme.resolveAttribute(R.attr.colorPrimarySurface, this, true)
        ContextCompat.getColor(this@setTaskDescription,
                if(wasResolved) resourceId else R.color.colorPrimary)
    }
    val desc = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
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

const val UPDATE_FLEXIBLE_REQUEST_CODE = 8831

sealed class UpdateState {
    object Yes: UpdateState()
    object YesButNotAllowed: UpdateState()
    data class No(@UpdateAvailability val availability: Int, @StringRes val title: Int,
                  @StringRes val message: Int, @StringRes val button: Int): UpdateState()
}

/**
 * Used to check if flexible update is available and perform action if so
 */
@Throws(Exception::class)
suspend fun Task<AppUpdateInfo>.awaitIsUpdateAvailable(@AppUpdateType appUpdateType: Int): UpdateState {
    return suspendCoroutine { continuation ->
        addOnCompleteListener { result ->
            if (result.isSuccessful) {
                Timber.d(result.result.updateAvailability().toString())
                continuation.resume(when(val avail = result.result.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        if(result.result.isUpdateTypeAllowed(appUpdateType))
                            UpdateState.Yes
                        else UpdateState.YesButNotAllowed
                    }
                    UpdateAvailability.UPDATE_NOT_AVAILABLE ->
                        UpdateState.No(avail, R.string.update_notavailable_title, R.string
                                .update_notavailable_message, R.string.update_notavailable_ok)
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                        UpdateState.No(avail, R.string.update_inprogress_title, R.string
                                .update_inprogress_message, R.string.update_inprogress_ok)
                    else -> { // UpdateAvailability.UNKNOWN
                        Timber.e("Unknown update availability type: $avail")
                        UpdateState.No(avail, R.string.update_unknown_title, R.string
                                .update_unknown_message, R.string.update_unknown_ok)
                    }
                })
            } else {
                Timber.e(result.exception)
            }
        }
    }
}

/**
 * Used in [Activity.onResume] to check if flexible update has downloaded
 * while user was away from the app
 */
@Throws(Exception::class)
suspend fun Task<AppUpdateInfo>.awaitIsFlexibleUpdateDownloaded(): Boolean {
    return suspendCoroutine { continuation ->
        addOnCompleteListener { result ->
            if (result.isSuccessful) {
                continuation.resume(
                        result.result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                && result.result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                                && result.result.installStatus() == InstallStatus.DOWNLOADED)
            } else {
                Timber.e(result.exception)
            }
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