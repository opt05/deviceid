package com.cwlarson.deviceid.data

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.VisibleForTesting
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.webkit.WebViewCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.gmsPackageInfo
import com.cwlarson.deviceid.util.systemProperty
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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
        Build.VERSION_CODES.R -> "11.0"
        Build.VERSION_CODES.S -> "12.0"
        else -> Build.VERSION.CODENAME
    }
}

/**
 * Use [Build.VERSION.SDK_INT] to get the version name of the release
 * NOTE: No longer used after Android Pie
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun Int.getCodename(): String =
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

class SoftwareRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val context: Context,
    preferenceManager: PreferenceManager
) : TabData(dispatcherProvider, context, preferenceManager) {
    private val activityManager by lazy { context.getSystemService<ActivityManager>() }

    override fun items() = flowOf(
        listOf(
            androidVersion(), patchLevel(),
            previewSDKInt(), deviceBuildVersion(), buildBaseband(), buildKernel(), buildDate(),
            buildNumber(), buildBoard(), buildBootloader(), buildBrand(), buildDevice(),
            buildDisplay(), buildFingerprint(), buildHardware(), buildHost(), buildTags(),
            buildType(), buildUser(), openGLVersion(), googlePlayServicesVersion(),
            googlePlayServicesInstallDate(), googlePlayServicesUpdatedDate(), webViewVersion()
        )
    ).flowOn(dispatcherProvider.IO)

    private fun androidVersion() = Item(
        title = R.string.software_title_android_version, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(
                "${Build.VERSION.RELEASE} (API level ${Build.VERSION.SDK_INT}) ${Build.VERSION.SDK_INT.getCodename()}".trim()
            )
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun patchLevel() = Item(
        title = R.string.software_title_patch_level, itemType = ItemType.SOFTWARE,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ItemSubtitle.Text(
                    try {
                        val patchDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).run {
                            parse(Build.VERSION.SECURITY_PATCH)
                        }
                        DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMM d, yyyy").run {
                            "${DateFormat.format(this, patchDate)}"
                        }
                    } catch (e: ParseException) {
                        Timber.w(e)
                        Build.VERSION.SECURITY_PATCH
                    }
                )
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.M)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun previewSDKInt() = Item(
        title = R.string.software_title_preview_sdk_int, itemType = ItemType.SOFTWARE,
        subtitle = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                ItemSubtitle.Text("${Build.VERSION.PREVIEW_SDK_INT}")
            else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.N)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun deviceBuildVersion() = Item(
        title = R.string.software_title_device_build_version, itemType = ItemType.SOFTWARE,
        subtitle = try {
            // Get Moto specific build version if available
            ItemSubtitle.Text(context.systemProperty("ro.build.version.full").run {
                if (isNullOrBlank()) Build.DISPLAY else this
            })
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildBaseband() = Item(
        title = R.string.software_title_build_baseband, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.getRadioVersion())
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildKernel() = Item(
        title = R.string.software_title_build_kernel, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(System.getProperty("os.version"))
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildDate() = Item(
        title = R.string.software_title_build_date, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(SimpleDateFormat.getInstance().format(Date(Build.TIME)))
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildNumber() = Item(
        title = R.string.software_title_build_number, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.ID)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildBoard() = Item(
        title = R.string.software_title_build_board, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.BOARD)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildBootloader() = Item(
        title = R.string.software_title_build_bootloader, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.BOOTLOADER)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildBrand() = Item(
        title = R.string.software_title_build_brand, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.BRAND)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildDevice() = Item(
        title = R.string.software_title_build_device, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.DEVICE)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildDisplay() = Item(
        title = R.string.software_title_build_display, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.DISPLAY)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildFingerprint() = Item(
        title = R.string.software_title_build_fingerprint, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.FINGERPRINT)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildHardware() = Item(
        title = R.string.software_title_build_hardware, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.HARDWARE)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildHost() = Item(
        title = R.string.software_title_build_host, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.HOST)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildTags() = Item(
        title = R.string.software_title_build_tags, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.TAGS)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildType() = Item(
        title = R.string.software_title_build_type, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.TYPE)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun buildUser() = Item(
        title = R.string.software_title_build_user, itemType = ItemType.SOFTWARE,
        subtitle = try {
            ItemSubtitle.Text(Build.USER)
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun openGLVersion() = Item(
        title = R.string.software_title_open_gl_version, itemType = ItemType.SOFTWARE,
        subtitle = try {
            activityManager?.let { ItemSubtitle.Text(it.deviceConfigurationInfo.glEsVersion) }
                ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun googlePlayServicesVersion() = Item(
        title = R.string.software_title_google_play_services_version, itemType = ItemType.SOFTWARE,
        subtitle = try {
            context.gmsPackageInfo?.let {
                ItemSubtitle.Text("${it.versionName} (${PackageInfoCompat.getLongVersionCode(it)})")
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun googlePlayServicesInstallDate() = Item(
        title = R.string.software_title_google_play_services_install_date,
        itemType = ItemType.SOFTWARE,
        subtitle = try {
            context.gmsPackageInfo?.let {
                ItemSubtitle.Text(
                    DateFormat.getDateFormat(context).format(it.firstInstallTime)
                )
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun googlePlayServicesUpdatedDate() = Item(
        title = R.string.software_title_google_play_services_updated_date,
        itemType = ItemType.SOFTWARE,
        subtitle = try {
            context.gmsPackageInfo?.let {
                ItemSubtitle.Text(
                    DateFormat.getDateFormat(context).format(it.lastUpdateTime)
                )
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun webViewVersion() = Item(
        title = R.string.software_title_webview_version, itemType = ItemType.SOFTWARE,
        subtitle = try {
            WebViewCompat.getCurrentWebViewPackage(context)?.let {
                ItemSubtitle.Text(it.versionName)
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )
}
