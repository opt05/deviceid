package com.cwlarson.deviceid.database

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.core.content.pm.PackageInfoCompat
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.UnavailableItem
import com.cwlarson.deviceid.databinding.UnavailableType
import com.cwlarson.deviceid.util.SystemProperty
import com.cwlarson.deviceid.util.activityManager
import com.cwlarson.deviceid.util.gmsPackageInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Use [Build.VERSION.SDK_INT] to get the version number of the release
 */
fun Int.sdkToVersion(): String {
    return when(this) {
        Build.VERSION_CODES.BASE -> "1.0"
        Build.VERSION_CODES.BASE_1_1 -> "1.1"
        Build.VERSION_CODES.CUPCAKE -> "1.5"
        Build.VERSION_CODES.DONUT -> "1.6"
        Build.VERSION_CODES.ECLAIR -> "2.0"
        Build.VERSION_CODES.ECLAIR_0_1 -> "2.0.1"
        Build.VERSION_CODES.ECLAIR_MR1 -> "2.1"
        Build.VERSION_CODES.FROYO -> "2.2"
        Build.VERSION_CODES.GINGERBREAD -> "2.3"
        Build.VERSION_CODES.GINGERBREAD_MR1 -> "2.3.3"
        Build.VERSION_CODES.HONEYCOMB -> "3.0"
        Build.VERSION_CODES.HONEYCOMB_MR1 -> "3.1"
        Build.VERSION_CODES.HONEYCOMB_MR2 -> "3.2"
        Build.VERSION_CODES.ICE_CREAM_SANDWICH -> "4.0.1"
        Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> "4.0.3"
        Build.VERSION_CODES.JELLY_BEAN -> "4.1"
        Build.VERSION_CODES.JELLY_BEAN_MR1 -> "4.2"
        Build.VERSION_CODES.JELLY_BEAN_MR2 -> "4.3"
        Build.VERSION_CODES.KITKAT -> "4.4"
        Build.VERSION_CODES.KITKAT_WATCH -> "4.4W"
        Build.VERSION_CODES.LOLLIPOP -> "5.0"
        Build.VERSION_CODES.LOLLIPOP_MR1 -> "5.1"
        Build.VERSION_CODES.M -> "6.0"
        Build.VERSION_CODES.N -> "7.0"
        Build.VERSION_CODES.N_MR1 -> "7.1"
        Build.VERSION_CODES.O -> "8.0"
        Build.VERSION_CODES.O_MR1 -> "8.1"
        Build.VERSION_CODES.P -> "9.0"
        Build.VERSION_CODES.Q -> "10.0"
        else -> "UNKNOWN"
    }
}

/**
 * Use [Build.VERSION.SDK_INT] to get the version name of the release
 * NOTE: No longer used after Android Pie
 */
private fun Int.getCodename(): String =
        when(this) {
            Build.VERSION_CODES.CUPCAKE -> "Cupcake"
            Build.VERSION_CODES.DONUT -> "Donut"
            Build.VERSION_CODES.ECLAIR, Build.VERSION_CODES.ECLAIR_0_1, Build.VERSION_CODES.ECLAIR_MR1 -> "Eclair"
            Build.VERSION_CODES.FROYO -> "Froyo"
            Build.VERSION_CODES.GINGERBREAD, Build.VERSION_CODES.GINGERBREAD_MR1 -> "Gingerbread"
            Build.VERSION_CODES.HONEYCOMB, Build.VERSION_CODES.HONEYCOMB_MR1, Build.VERSION_CODES.HONEYCOMB_MR2 -> "Honeycomb"
            Build.VERSION_CODES.ICE_CREAM_SANDWICH, Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> "Ice Cream Sandwich"
            Build.VERSION_CODES.JELLY_BEAN, Build.VERSION_CODES.JELLY_BEAN_MR1, Build.VERSION_CODES.JELLY_BEAN_MR2 -> "Jelly Bean"
            Build.VERSION_CODES.KITKAT -> "KitKat"
            Build.VERSION_CODES.KITKAT_WATCH -> "KitKat Watch"
            Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1 -> "Lollipop"
            Build.VERSION_CODES.M -> "Marshmallow"
            Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1 -> "Nougat"
            Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1 -> "Oreo"
            Build.VERSION_CODES.P -> "Pie"
            else -> ""
        }

class Software(private val context: Context, db: AppDatabase, scope: CoroutineScope) {
    init {
        scope.launch {
            db.addItems(context, androidVersion(), patchLevel(), previewSDKInt(), deviceBuildVersion(),
                    buildBaseband(), buildKernel(), buildDate(), buildNumber(), buildBoard(),
                    buildBootloader(), buildBrand(), buildDevice(), buildDisplay(), buildFingerprint(),
                    buildHardware(), buildHost(), buildTags(), buildType(), buildUser(), openGLVersion(),
                    googlePlayServicesVersion(), googlePlayServicesInstallDate(),
                    googlePlayServicesUpdatedDate())
        }
    }

    private fun androidVersion() = Item("Android Version", ItemType.SOFTWARE).apply {
        try {
            subtitle = "${Build.VERSION.RELEASE} (API level ${Build.VERSION.SDK_INT}) ${Build.VERSION.SDK_INT.getCodename()}".trim()
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun patchLevel() = Item("Security Patch Level", ItemType.SOFTWARE).apply {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                subtitle = try {
                    val template = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val patchDate = template.parse(Build.VERSION.SECURITY_PATCH)
                    val format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy")
                    DateFormat.format(format, patchDate).toString()
                } catch (e: ParseException) {
                    e.printStackTrace()
                    Build.VERSION.SECURITY_PATCH
                }
            } else {
                unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                        Build.VERSION_CODES.M.sdkToVersion())
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun previewSDKInt() = Item("Preview SDK Number", ItemType.SOFTWARE).apply {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                subtitle = Build.VERSION.PREVIEW_SDK_INT.run {
                    if(this == 0) "Non-Preview" else "Preview $this"
                }
            } else {
                unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET,
                        Build.VERSION_CODES.N.sdkToVersion())
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    //Get Moto specific build version if available
    private fun deviceBuildVersion() = Item("Build Version", ItemType.SOFTWARE).apply {
        try {
            val sp = SystemProperty(context)
            subtitle = if (sp["ro.build.version.full"] == null || sp["ro.build.version.full"] == "") Build.DISPLAY else sp["ro.build.version.full"]
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBaseband() = Item("Build Baseband", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.getRadioVersion()
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildKernel() = Item("Kernel Version", ItemType.SOFTWARE).apply {
        try {
            subtitle = System.getProperty("os.version")
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildDate() = Item("Build Date", ItemType.SOFTWARE).apply {
        try {
            subtitle = SimpleDateFormat.getInstance().format(Date(Build.TIME))
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildNumber() = Item("Build Number", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.ID
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBoard() = Item("Build Board", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.BOARD
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBootloader() = Item("Build Bootloader", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.BOOTLOADER
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBrand() = Item("Build Brand", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.BRAND
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildDevice() = Item("Build Device", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.DEVICE
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildDisplay() = Item("Build Display", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.DISPLAY
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildFingerprint() = Item("Build Fingerprint", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.FINGERPRINT
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildHardware() = Item("Build Hardware", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.HARDWARE
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildHost() = Item("Build Host", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.HOST
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildTags() = Item("Build Tags", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.TAGS
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildType() = Item("Build Type", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.TYPE
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildUser() = Item("Build User", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.USER
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun openGLVersion() = Item("OpenGL Version", ItemType.SOFTWARE).apply {
        try {
            subtitle = context.activityManager.deviceConfigurationInfo.glEsVersion
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun googlePlayServicesVersion() = Item("Google Play Services Version", ItemType.SOFTWARE).apply {
        try {
            val pi = context.gmsPackageInfo
            val v = PackageInfoCompat.getLongVersionCode(pi)
            subtitle = "${pi.versionName} ($v)"
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun googlePlayServicesInstallDate() = Item("Google Play Services Installed", ItemType.SOFTWARE).apply {
        try {
            subtitle = DateFormat.getDateFormat(context).format(context.gmsPackageInfo.firstInstallTime)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun googlePlayServicesUpdatedDate() = Item("Google Play Services Updated", ItemType.SOFTWARE).apply {
        try {
            subtitle = DateFormat.getDateFormat(context).format(context.gmsPackageInfo.lastUpdateTime)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }
}
