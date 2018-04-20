package com.cwlarson.deviceid.database

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.UnavailableItem
import com.cwlarson.deviceid.databinding.UnavailableType
import com.cwlarson.deviceid.util.SystemProperty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class Software(activity: Activity, db: AppDatabase) {
    private val tag = Software::class.java.simpleName
    private val context: Context = activity.applicationContext

    init {
        //Set Software Tiles
        val itemAdder = ItemAdder(context, db)
        itemAdder.addItems(androidVersion)
        itemAdder.addItems(patchLevel)
        itemAdder.addItems(previewSDKInt)
        itemAdder.addItems(deviceBuildVersion)
        itemAdder.addItems(buildBaseband)
        itemAdder.addItems(buildKernel)
        itemAdder.addItems(buildDate)
        itemAdder.addItems(buildNumber)
        itemAdder.addItems(buildBoard)
        itemAdder.addItems(buildBootloader)
        itemAdder.addItems(buildBrand)
        itemAdder.addItems(buildDevice)
        itemAdder.addItems(buildDisplay)
        itemAdder.addItems(buildFingerprint)
        itemAdder.addItems(buildHardware)
        itemAdder.addItems(buildHost)
        itemAdder.addItems(buildTags)
        itemAdder.addItems(buildType)
        itemAdder.addItems(buildUser)
        itemAdder.addItems(openGLVersion)
        itemAdder.addItems(googlePlayServicesVersion)
        itemAdder.addItems(googlePlayServicesInstallDate)
        itemAdder.addItems(googlePlayServicesUpdatedDate)
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
        OREO, OREO_MR1;


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
                        1000 -> return CUR_DEVELOPMENT
                        else -> return null
                    }
                }
        }
    }

    private val androidVersion: Item
        get() {
            val item = Item("Android Version", ItemType.SOFTWARE)
            try {
                val versionName = Codenames.codename?.toString() ?: ""
                item.subtitle = Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT.toString() + ") " +
                        versionName
            } catch (e: Exception) {
                Log.w(tag, "Null in getAndroidVersion")
            }

            return item
        }

    private val patchLevel: Item
        get() {
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

    private val previewSDKInt: Item
        get() {
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

    private//Get Moto specific build version if available
    val deviceBuildVersion: Item
        get() {
            val item = Item("Build Version", ItemType.SOFTWARE)
            val sp = SystemProperty(context)
            item.subtitle = if (sp["ro.build.version.full"] == null || sp["ro.build.version.full"] == "") Build.DISPLAY else sp["ro.build.version.full"]
            return item
        }

    private val buildBaseband: Item
        get() {
            val item = Item("Build Baseband", ItemType.SOFTWARE)
            item.subtitle = Build.getRadioVersion()
            return item
        }

    private val buildKernel: Item
        get() {
            val item = Item("Kernel Version", ItemType.SOFTWARE)
            item.subtitle = System.getProperty("os.version")
            return item
        }

    private val buildDate: Item
        get() {
            val item = Item("Build Date", ItemType.SOFTWARE)
            item.subtitle = SimpleDateFormat.getInstance().format(Date(Build.TIME))
            return item
        }

    private val buildNumber: Item
        get() {
            val item = Item("Build Number", ItemType.SOFTWARE)
            item.subtitle = Build.ID
            return item
        }

    private val buildBoard: Item
        get() {
            val item = Item("Build Board", ItemType.SOFTWARE)
            item.subtitle = Build.BOARD
            return item
        }

    private val buildBootloader: Item
        get() {
            val item = Item("Build Bootloader", ItemType.SOFTWARE)
            item.subtitle = Build.BOOTLOADER
            return item
        }

    private val buildBrand: Item
        get() {
            val item = Item("Build Brand", ItemType.SOFTWARE)
            item.subtitle = Build.BRAND
            return item
        }

    private val buildDevice: Item
        get() {
            val item = Item("Build Brand", ItemType.SOFTWARE)
            item.subtitle = Build.DEVICE
            return item
        }

    private val buildDisplay: Item
        get() {
            val item = Item("Build Display", ItemType.SOFTWARE)
            item.subtitle = Build.DISPLAY
            return item
        }

    private val buildFingerprint: Item
        get() {
            val item = Item("Build Fingerprint", ItemType.SOFTWARE)
            item.subtitle = Build.FINGERPRINT
            return item
        }

    private val buildHardware: Item
        get() {
            val item = Item("Build Hardware", ItemType.SOFTWARE)
            item.subtitle = Build.HARDWARE
            return item
        }

    private val buildHost: Item
        get() {
            val item = Item("Build Host", ItemType.SOFTWARE)
            item.subtitle = Build.HOST
            return item
        }

    private val buildTags: Item
        get() {
            val item = Item("Build Tags", ItemType.SOFTWARE)
            item.subtitle = Build.TAGS
            return item
        }

    private val buildType: Item
        get() {
            val item = Item("Build Type", ItemType.SOFTWARE)
            item.subtitle = Build.TYPE
            return item
        }

    private val buildUser: Item
        get() {
            val item = Item("Build User", ItemType.SOFTWARE)
            item.subtitle = Build.USER
            return item
        }

    private val openGLVersion: Item
        get() {
            val item = Item("OpenGL Version", ItemType.SOFTWARE)
            try {
                val configurationInfo = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo
                item.subtitle = configurationInfo.glEsVersion
            } catch (e: Exception) {
                Log.e(tag, "Exception in getOpenGLVersion")
            }

            return item
        }

    private val googlePlayServicesVersion: Item
        get() {
            val item = Item("Google Play Services Version", ItemType.SOFTWARE)
            try {
                val n = context.packageManager.getPackageInfo("com.google.android.gms", 0).versionName
                val v = context.packageManager.getPackageInfo("com.google.android.gms", 0).versionCode
                item.subtitle = n + " (" + v.toString() + ")"
            } catch (e: Exception) {
                Log.e(tag, "Exception in getGooglePlayServicesVersion")
            }

            return item
        }

    private val googlePlayServicesInstallDate: Item
        get() {
            val item = Item("Google Play Services Installed", ItemType.SOFTWARE)
            try {
                val t = context.packageManager.getPackageInfo("com.google.android.gms", 0).firstInstallTime
                item.subtitle = DateFormat.getDateFormat(context).format(t)
            } catch (e: Exception) {
                Log.e(tag, "Exception in getGooglePlayServicesVersion")
            }

            return item
        }

    private val googlePlayServicesUpdatedDate: Item
        get() {
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
