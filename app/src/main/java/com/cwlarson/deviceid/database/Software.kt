package com.cwlarson.deviceid.database

import android.app.ActivityManager
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@WorkerThread
internal class Software(context: Context, db: AppDatabase) {
    private val tag = Software::class.java.simpleName
    private val context: Context = context.applicationContext

    init {
        //Set Software Tiles
        db.addItems(context,androidVersion())
        db.addItems(context,patchLevel())
        db.addItems(context,previewSDKInt())
        db.addItems(context,deviceBuildVersion())
        db.addItems(context,buildBaseband())
        db.addItems(context,buildKernel())
        db.addItems(context,buildDate())
        db.addItems(context,buildNumber())
        db.addItems(context,buildBoard())
        db.addItems(context,buildBootloader())
        db.addItems(context,buildBrand())
        db.addItems(context,buildDevice())
        db.addItems(context,buildDisplay())
        db.addItems(context,buildFingerprint())
        db.addItems(context,buildHardware())
        db.addItems(context,buildHost())
        db.addItems(context,buildTags())
        db.addItems(context,buildType())
        db.addItems(context,buildUser())
        db.addItems(context,openGLVersion())
        db.addItems(context,googlePlayServicesVersion())
        db.addItems(context,googlePlayServicesInstallDate())
        db.addItems(context,googlePlayServicesUpdatedDate())
    }

    private enum class Codenames {
        BASE, BASE_1_1,
        CUPCAKE,
        CUR_DEVELOPMENT,
        DONUT,
        ECLAIR, ECLAIR_MR1, ECLAIR_MR2,
        FROYO,
        GINGERBREAD, GINGERBREAD_MR1,
        HONEYCOMB, HONEYCOMB_MR1, HONEYCOMB_MR2,
        ICE_CREAM_SANDWICH, ICE_CREAM_SANDWICH_MR1,
        JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2,
        KITKAT, KITKAT_WATCH,
        LOLLIPOP, LOLLIPOP_MR1,
        MARSHMALLOW,
        NOUGAT, NOUGAT_MR1,
        OREO, OREO_MR1,
        PIE;


        companion object {
            internal val codename: Codenames?
                get() {
                    val api = Build.VERSION.SDK_INT
                    when (api) {
                        1 -> return BASE
                        2 -> return BASE_1_1
                        3 -> return CUPCAKE
                        4 -> return DONUT
                        5 -> return ECLAIR
                        6 -> return ECLAIR_MR1
                        7 -> return ECLAIR_MR2
                        8 -> return FROYO
                        9 -> return GINGERBREAD
                        10 -> return GINGERBREAD_MR1
                        11 -> return HONEYCOMB
                        12 -> return HONEYCOMB_MR1
                        13 -> return HONEYCOMB_MR2
                        14 -> return ICE_CREAM_SANDWICH
                        15 -> return ICE_CREAM_SANDWICH_MR1
                        16 -> return JELLY_BEAN
                        17 -> return JELLY_BEAN_MR1
                        18 -> return JELLY_BEAN_MR2
                        19 -> return KITKAT
                        20 -> return KITKAT_WATCH
                        21 -> return LOLLIPOP
                        22 -> return LOLLIPOP_MR1
                        23 -> return MARSHMALLOW
                        24 -> return NOUGAT
                        25 -> return NOUGAT_MR1
                        26 -> return OREO
                        27 -> return OREO_MR1
                        28 -> return PIE
                        1000 -> return CUR_DEVELOPMENT
                        else -> return null
                    }
                }
        }
    }

    private fun androidVersion(): Item {
        val item = Item("Android Version", ItemType.SOFTWARE)
        try {
            val versionName = Codenames.codename?.toString() ?: ""
            item.subtitle = "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT}) $versionName"
        } catch (e: Exception) {
            Log.w(tag, "Null in getAndroidVersion")
        }

        return item
    }

    private fun patchLevel(): Item {
        val item = Item("Security Patch Level", ItemType.SOFTWARE)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val template = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val patchDate = template.parse(Build.VERSION.SECURITY_PATCH)
                    val format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy")
                    item.subtitle = DateFormat.format(format, patchDate).toString()
                } catch (e: ParseException) {
                    e.printStackTrace()
                    item.subtitle = Build.VERSION.SECURITY_PATCH
                }

            } else {
                item.unavailableitem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET, "6.0")
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getAndroidVersion")
        }

        return item
    }

    private fun previewSDKInt(): Item {
        val item = Item("Preview SDK Number", ItemType.SOFTWARE)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val sdk = Build.VERSION.PREVIEW_SDK_INT
                if (sdk == 0)
                    item.subtitle = "Non-Preview"
                else
                    item.subtitle = "Preview " + Integer.toString(sdk)
            } else {
                item.unavailableitem = UnavailableItem(UnavailableType.NOT_POSSIBLE_YET, "6.0")
            }
        } catch (e: Exception) {
            Log.w(tag, "Null in getPreviewSDKInt")
        }

        return item
    }

    //Get Moto specific build version if available
    private fun deviceBuildVersion(): Item {
        val item = Item("Build Version", ItemType.SOFTWARE)
        val sp = SystemProperty(context)
        item.subtitle = if (sp["ro.build.version.full"] == null || sp["ro.build.version.full"] == "") Build.DISPLAY else sp["ro.build.version.full"]
        return item
    }

    private fun buildBaseband(): Item {
        val item = Item("Build Baseband", ItemType.SOFTWARE)
        item.subtitle = Build.getRadioVersion()
        return item
    }

    private fun buildKernel(): Item {
        val item = Item("Kernel Version", ItemType.SOFTWARE)
        item.subtitle = System.getProperty("os.version")
        return item
    }

    private fun buildDate(): Item {
        val item = Item("Build Date", ItemType.SOFTWARE)
        item.subtitle = SimpleDateFormat.getInstance().format(Date(Build.TIME))
        return item
    }

    private fun buildNumber(): Item {
        val item = Item("Build Number", ItemType.SOFTWARE)
        item.subtitle = Build.ID
        return item
    }

    private fun buildBoard(): Item  {
        val item = Item("Build Board", ItemType.SOFTWARE)
        item.subtitle = Build.BOARD
        return item
    }

    private fun buildBootloader(): Item {
        val item = Item("Build Bootloader", ItemType.SOFTWARE)
        item.subtitle = Build.BOOTLOADER
        return item
    }

    private fun buildBrand(): Item {
        val item = Item("Build Brand", ItemType.SOFTWARE)
        item.subtitle = Build.BRAND
        return item
    }

    private fun buildDevice(): Item {
        val item = Item("Build Brand", ItemType.SOFTWARE)
        item.subtitle = Build.DEVICE
        return item
    }

    private fun buildDisplay(): Item {
        val item = Item("Build Display", ItemType.SOFTWARE)
        item.subtitle = Build.DISPLAY
        return item
    }

    private fun buildFingerprint(): Item {
        val item = Item("Build Fingerprint", ItemType.SOFTWARE)
        item.subtitle = Build.FINGERPRINT
        return item
    }

    private fun buildHardware(): Item {
        val item = Item("Build Hardware", ItemType.SOFTWARE)
        item.subtitle = Build.HARDWARE
        return item
    }

    private fun buildHost(): Item {
        val item = Item("Build Host", ItemType.SOFTWARE)
        item.subtitle = Build.HOST
        return item
    }

    private fun buildTags(): Item {
        val item = Item("Build Tags", ItemType.SOFTWARE)
        item.subtitle = Build.TAGS
        return item
    }

    private fun buildType(): Item {
        val item = Item("Build Type", ItemType.SOFTWARE)
        item.subtitle = Build.TYPE
        return item
    }

    private fun buildUser(): Item {
        val item = Item("Build User", ItemType.SOFTWARE)
        item.subtitle = Build.USER
        return item
    }

    private fun openGLVersion(): Item {
        val item = Item("OpenGL Version", ItemType.SOFTWARE)
        try {
            val configurationInfo = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo
            item.subtitle = configurationInfo.glEsVersion
        } catch (e: Exception) {
            Log.e(tag, "Exception in getOpenGLVersion")
        }

        return item
    }

    private fun googlePlayServicesVersion(): Item {
        val item = Item("Google Play Services Version", ItemType.SOFTWARE)
        try {
            val pi = context.packageManager.getPackageInfo("com.google.android.gms", 0)
            val n =  pi.versionName
            val v = PackageInfoCompat.getLongVersionCode(pi)
            item.subtitle = "$n ($v)"
        } catch (e: Exception) {
            Log.e(tag, "Exception in getGooglePlayServicesVersion")
        }

        return item
    }

    private fun googlePlayServicesInstallDate(): Item {
        val item = Item("Google Play Services Installed", ItemType.SOFTWARE)
        try {
            val t = context.packageManager.getPackageInfo("com.google.android.gms", 0).firstInstallTime
            item.subtitle = DateFormat.getDateFormat(context).format(t)
        } catch (e: Exception) {
            Log.e(tag, "Exception in getGooglePlayServicesVersion")
        }

        return item
    }

    private fun googlePlayServicesUpdatedDate(): Item {
        val item = Item("Google Play Services Updated", ItemType.SOFTWARE)
        try {
            val t = context.packageManager.getPackageInfo("com.google.android.gms", 0).lastUpdateTime
            item.subtitle = DateFormat.getDateFormat(context).format(t)
        } catch (e: Exception) {
            Log.e(tag, "Exception in getGooglePlayServicesVersion")
        }

        return item
    }
}
