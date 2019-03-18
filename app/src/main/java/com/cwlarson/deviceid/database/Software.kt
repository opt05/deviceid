package com.cwlarson.deviceid.database

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.content.pm.PackageInfoCompat
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.UnavailableItem
import com.cwlarson.deviceid.databinding.UnavailableType
import com.cwlarson.deviceid.util.SystemProperty
import com.cwlarson.deviceid.util.activityManager
import com.cwlarson.deviceid.util.gmsPackageInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@WorkerThread
internal class Software(private val context: Context, db: AppDatabase) {
    companion object {
        private const val TAG = "Software"
    }

    init {
        //Set Software Tiles
        db.addItems(context,androidVersion(),patchLevel(),previewSDKInt(),deviceBuildVersion(),
                buildBaseband(),buildKernel(),buildDate(),buildNumber(),buildBoard(),
                buildBootloader(),buildBrand(),buildDevice(),buildDisplay(),buildFingerprint(),
                buildHardware(),buildHost(),buildTags(),buildType(),buildUser(),openGLVersion(),
                googlePlayServicesVersion(),googlePlayServicesInstallDate(),
                googlePlayServicesUpdatedDate())
    }

    private enum class Codename(val value: Int) {
        BASE(1), BASE_1_1(2),
        CUPCAKE(3),
        CUR_DEVELOPMENT(1000),
        DONUT(4),
        ECLAIR(5), ECLAIR_MR1(6), ECLAIR_MR2(7),
        FROYO(8),
        GINGERBREAD(9), GINGERBREAD_MR1(10),
        HONEYCOMB(11), HONEYCOMB_MR1(12), HONEYCOMB_MR2(13),
        ICE_CREAM_SANDWICH(14), ICE_CREAM_SANDWICH_MR1(15),
        JELLY_BEAN(16), JELLY_BEAN_MR1(17), JELLY_BEAN_MR2(18),
        KITKAT(19), KITKAT_WATCH(20),
        LOLLIPOP(21), LOLLIPOP_MR1(22),
        MARSHMALLOW(23),
        NOUGAT(24), NOUGAT_MR1(25),
        OREO(26), OREO_MR1(27),
        PIE(28);

        companion object {
            fun fromInt(value: Int) = values().firstOrNull { it.value == value }
        }
    }

    private fun androidVersion() = Item("Android Version", ItemType.SOFTWARE).apply {
        try {
            subtitle = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT}) ${Codename.fromInt(Build.VERSION.SDK_INT)?.toString().orEmpty()}"
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
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
                unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET, "6.0")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun previewSDKInt() = Item("Preview SDK Number", ItemType.SOFTWARE).apply {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                subtitle = Build.VERSION.PREVIEW_SDK_INT.run {
                    if(this == 0) "Non-Preview" else "Preview $this"
                }
            } else {
                unavailableItem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET, "6.0")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    //Get Moto specific build version if available
    private fun deviceBuildVersion() = Item("Build Version", ItemType.SOFTWARE).apply {
        try {
            val sp = SystemProperty(context)
            subtitle = if (sp["ro.build.version.full"] == null || sp["ro.build.version.full"] == "") Build.DISPLAY else sp["ro.build.version.full"]
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBaseband() = Item("Build Baseband", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.getRadioVersion()
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildKernel() = Item("Kernel Version", ItemType.SOFTWARE).apply {
        try {
            subtitle = System.getProperty("os.version")
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildDate() = Item("Build Date", ItemType.SOFTWARE).apply {
        try {
            subtitle = SimpleDateFormat.getInstance().format(Date(Build.TIME))
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildNumber() = Item("Build Number", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.ID
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBoard() = Item("Build Board", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.BOARD
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBootloader() = Item("Build Bootloader", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.BOOTLOADER
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildBrand() = Item("Build Brand", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.BRAND
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildDevice() = Item("Build Device", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.DEVICE
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildDisplay() = Item("Build Display", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.DISPLAY
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildFingerprint() = Item("Build Fingerprint", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.FINGERPRINT
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildHardware() = Item("Build Hardware", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.HARDWARE
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildHost() = Item("Build Host", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.HOST
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildTags() = Item("Build Tags", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.TAGS
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildType() = Item("Build Type", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.TYPE
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun buildUser() = Item("Build User", ItemType.SOFTWARE).apply {
        try {
            subtitle = Build.USER
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun openGLVersion() = Item("OpenGL Version", ItemType.SOFTWARE).apply {
        try {
            subtitle = context.activityManager.deviceConfigurationInfo.glEsVersion
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun googlePlayServicesVersion() = Item("Google Play Services Version", ItemType.SOFTWARE).apply {
        try {
            val pi = context.gmsPackageInfo
            val v = PackageInfoCompat.getLongVersionCode(pi)
            subtitle = "${pi.versionName} ($v)"
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun googlePlayServicesInstallDate() = Item("Google Play Services Installed", ItemType.SOFTWARE).apply {
        try {
            subtitle = DateFormat.getDateFormat(context).format(context.gmsPackageInfo.firstInstallTime)
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun googlePlayServicesUpdatedDate() = Item("Google Play Services Updated", ItemType.SOFTWARE).apply {
        try {
            subtitle = DateFormat.getDateFormat(context).format(context.gmsPackageInfo.lastUpdateTime)
        } catch (e: Exception) {
            Log.w(TAG, "Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }
}
