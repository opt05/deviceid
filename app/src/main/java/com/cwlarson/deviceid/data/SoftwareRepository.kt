package com.cwlarson.deviceid.data

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.core.content.pm.PackageInfoCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.SystemProperty
import com.cwlarson.deviceid.util.activityManager
import com.cwlarson.deviceid.util.gmsPackageInfo
import com.cwlarson.deviceid.util.webViewPackageInfo
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Use [Build.VERSION.SDK_INT] to get the version number of the release
 */
fun Int.sdkToVersion(): String {
    return when (this) {
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
        else -> Build.VERSION.CODENAME
    }
}

/**
 * Use [Build.VERSION.SDK_INT] to get the version name of the release
 * NOTE: No longer used after Android Pie
 */
private fun Int.getCodename(): String =
        when (this) {
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

class SoftwareRepository(private val context: Context, filterUnavailable: Boolean = false)
    : TabData(filterUnavailable) {

    override suspend fun list(): List<Item> = listOf(androidVersion(), patchLevel(),
            previewSDKInt(), deviceBuildVersion(), buildBaseband(), buildKernel(), buildDate(),
            buildNumber(), buildBoard(), buildBootloader(), buildBrand(), buildDevice(),
            buildDisplay(), buildFingerprint(), buildHardware(), buildHost(), buildTags(),
            buildType(), buildUser(), openGLVersion(), googlePlayServicesVersion(),
            googlePlayServicesInstallDate(), googlePlayServicesUpdatedDate(), webViewVersion())

    private fun androidVersion() = Item(R.string.software_title_android_version, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(
                    "${Build.VERSION.RELEASE} (API level ${Build.VERSION.SDK_INT}) ${Build
                            .VERSION.SDK_INT.getCodename()}".trim())
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun patchLevel() = Item(R.string.software_title_patch_level, ItemType.SOFTWARE).apply {
        try {
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ItemSubtitle.Text(try {
                    val template = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val patchDate = template.parse(Build.VERSION.SECURITY_PATCH)
                    val format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy")
                    DateFormat.format(format, patchDate).toString()
                } catch (e: ParseException) {
                    e.printStackTrace()
                    Build.VERSION.SECURITY_PATCH
                })
            } else {
                ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun previewSDKInt() = Item(R.string.software_title_preview_sdk_int, ItemType.SOFTWARE).apply {
        try {
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ItemSubtitle.Text(Build.VERSION.PREVIEW_SDK_INT.run {
                    if (this == 0) "Non-Preview" else "Preview $this"
                })
            } else {
                ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.N)
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    //Get Moto specific build version if available
    private fun deviceBuildVersion() = Item(R.string.software_title_device_build_version, ItemType.SOFTWARE).apply {
        try {
            val sp = SystemProperty(context)
            subtitle = ItemSubtitle.Text(if (sp["ro.build.version.full"] == null || sp["ro.build" +
                            ".version.full"] == "") Build.DISPLAY else sp["ro.build.version.full"])
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildBaseband() = Item(R.string.software_title_build_baseband, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.getRadioVersion())
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildKernel() = Item(R.string.software_title_build_kernel, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(System.getProperty("os.version"))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildDate() = Item(R.string.software_title_build_date, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(SimpleDateFormat.getInstance().format(Date(Build.TIME)))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildNumber() = Item(R.string.software_title_build_number, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.ID)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildBoard() = Item(R.string.software_title_build_board, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.BOARD)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildBootloader() = Item(R.string.software_title_build_bootloader, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.BOOTLOADER)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildBrand() = Item(R.string.software_title_build_brand, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.BRAND)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildDevice() = Item(R.string.software_title_build_device, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.DEVICE)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildDisplay() = Item(R.string.software_title_build_display, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.DISPLAY)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildFingerprint() = Item(R.string.software_title_build_fingerprint, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.FINGERPRINT)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildHardware() = Item(R.string.software_title_build_hardware, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.HARDWARE)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildHost() = Item(R.string.software_title_build_host, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.HOST)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildTags() = Item(R.string.software_title_build_tags, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.TAGS)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildType() = Item(R.string.software_title_build_type, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.TYPE)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun buildUser() = Item(R.string.software_title_build_user, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(Build.USER)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun openGLVersion() = Item(R.string.software_title_open_gl_version, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(context.activityManager.deviceConfigurationInfo
                    .glEsVersion)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun googlePlayServicesVersion() = Item(R.string.software_title_google_play_services_version, ItemType.SOFTWARE).apply {
        try {
            val pi = context.gmsPackageInfo
            val v = PackageInfoCompat.getLongVersionCode(pi)
            subtitle = ItemSubtitle.Text("${pi.versionName} ($v)")
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun googlePlayServicesInstallDate() = Item(R.string.software_title_google_play_services_install_date, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(DateFormat.getDateFormat(context).format(context
                    .gmsPackageInfo.firstInstallTime))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun googlePlayServicesUpdatedDate() = Item(R.string.software_title_google_play_services_updated_date, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(DateFormat.getDateFormat(context).format(context
                    .gmsPackageInfo.lastUpdateTime))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun webViewVersion() = Item(R.string.software_title_webview_version, ItemType.SOFTWARE).apply {
        try {
            subtitle = ItemSubtitle.Text(context.webViewPackageInfo?.versionName)
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }
}
