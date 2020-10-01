package com.cwlarson.deviceid.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import androidx.core.content.ContextCompat
import androidx.core.os.EnvironmentCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.ChartItem
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.activityManager
import com.cwlarson.deviceid.util.displayManger
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.coroutines.resume

class HardwareRepository(private val context: Context, filterUnavailable: Boolean = false)
    : TabData(filterUnavailable) {

    override suspend fun list(): List<Item> = listOf(ramSize(), formattedInternalMemory(),
            formattedExternalMemory(), getBattery(), *getDisplayInfo())

    //Total memory is only available on API 16+
    private fun ramSize() = Item(R.string.hardware_title_memory, ItemType.HARDWARE).apply {
        try {
            val mi = ActivityManager.MemoryInfo()
            context.activityManager.getMemoryInfo(mi)
            subtitle = ItemSubtitle.Chart(
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        ChartItem(mi.availMem.toFloat(),
                                0f,
                                R.drawable.ic_memory,
                                context.resources.getString(R.string
                                        .hardware_storage_output_format_api15,
                                        Formatter.formatFileSize(context, mi.availMem)))
                    } else {
                        ChartItem(mi.availMem.toFloat(),
                                mi.totalMem.toFloat(),
                                R.drawable.ic_memory,
                                if (mi.totalMem > 0)
                                    context.resources.getString(R.string
                                            .hardware_storage_output_format,
                                            Formatter.formatFileSize(context, mi.availMem),
                                            Formatter.formatFileSize(context, mi.totalMem))
                                else null)
                    })
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun formattedInternalMemory() = Item(R.string.hardware_title_internal_storage, ItemType.HARDWARE).apply {
        try {
            val dir = Environment.getDataDirectory()
            val stat = StatFs(dir.path)

            @Suppress("DEPRECATION")
            val available = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                stat.blockSize.toLong() * stat.availableBlocks.toLong()
            else stat.availableBytes

            @Suppress("DEPRECATION")
            val total = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                stat.blockCount.toLong() * stat.blockCount.toLong()
            else stat.totalBytes
            subtitle = ItemSubtitle.Chart(
                    ChartItem(available.toFloat(),
                            total.toFloat(),
                            R.drawable.ic_storage,
                            if (total > 0L)
                                context.resources.getString(R.string.hardware_storage_output_format,
                                        Formatter.formatFileSize(context, available),
                                        Formatter.formatFileSize(context, total))
                            else null)
            )
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun formattedExternalMemory() = Item(R.string.hardware_title_external_storage, ItemType.HARDWARE).apply {
        try {
            // Mounted and not emulated, most likely a real SD Card
            val appsDir = ContextCompat.getExternalFilesDirs(context, null).filter {
                it != null
                        && EnvironmentCompat.getStorageState(it) == Environment.MEDIA_MOUNTED
                        && !it.isExternalStorageEmulatedCompat()
            }
            val availSize = appsDir.map { it.freeSpace }.toTypedArray().sum()
            val totalSize = appsDir.map { it.totalSpace }.toTypedArray().sum()
            subtitle = ItemSubtitle.Chart(
                    ChartItem(availSize.toFloat(),
                            totalSize.toFloat(),
                            R.drawable.ic_storage,
                            if (totalSize > 0L)
                                context.resources.getString(R.string.hardware_storage_output_format,
                                        Formatter.formatFileSize(context, availSize),
                                        Formatter.formatFileSize(context, totalSize))
                            else null)
            )
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun File.isExternalStorageEmulatedCompat(): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Environment.isExternalStorageEmulated(this)
            else Environment.isExternalStorageEmulated()

    private suspend fun getBattery(): Item =
            suspendCancellableCoroutine { cont ->
                val batteryReceiver = object : BroadcastReceiver() {
                    override fun onReceive(c: Context?, intent: Intent?) {
                        context.unregisterReceiver(this)
                        intent?.action?.let { act ->
                            val result = goAsync()
                            val item = Item(R.string.hardware_title_battery, ItemType.HARDWARE)
                            if (act == Intent.ACTION_BATTERY_CHANGED) {
                                //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
                                val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                                    BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.BATTERY_HEALTH_COLD)
                                    BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.BATTERY_HEALTH_DEAD)
                                    BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.BATTERY_HEALTH_GOOD)
                                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.BATTERY_HEALTH_OVERHEAT)
                                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.BATTERY_HEALTH_OVER_VOLTAGE)
                                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.BATTERY_HEALTH_UNSPECIFIED_FAILURE)
                                    else -> context.getString(R.string.BATTERY_HEALTH_UNKNOWN)
                                }
                                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                                item.subtitle = ItemSubtitle.Chart(
                                        ChartItem((100 - level).toFloat(),
                                                100f,
                                                R.drawable.ic_battery,
                                                StringBuilder().apply {
                                                    append("$level% - ")
                                                    // Are we charging / charged?
                                                    when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                                                        BatteryManager.BATTERY_STATUS_CHARGING -> append(context.getString(R.string.BATTERY_STATUS_CHARGING))
                                                        BatteryManager.BATTERY_STATUS_FULL -> append(context.getString(R.string.BATTERY_STATUS_FULL))
                                                        BatteryManager.BATTERY_STATUS_DISCHARGING -> append(context.getString(R.string.BATTERY_STATUS_DISCHARGING))
                                                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> append(context.getString(R.string.BATTERY_STATUS_NOT_CHARGING))
                                                        else -> append(context.getString(R.string.BATTERY_STATUS_UNKNOWN))
                                                    }
                                                    append(" ($health)")
                                                    // How are we charging?
                                                    val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                                                    val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                                                    val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
                                                    append(" ")
                                                    if (usbCharge)
                                                        append(context.getString(R.string.BATTERY_PLUGGED_USB))
                                                    else if (acCharge)
                                                        append(context.getString(R.string.BATTERY_PLUGGED_AC))
                                                    //subTitle = String.valueOf(temperature+"\u00b0C");
                                                }.toString()))
                                // Must call finish() so the BroadcastReceiver can be recycled.
                                result.finish()
                            }
                            cont.resume(item)
                        }
                    }
                }
                cont.invokeOnCancellation { context.unregisterReceiver(batteryReceiver) }
                context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }

    private fun getDisplayInfo(): Array<Item> = mutableListOf<Item>().apply {
        try {
            add(Item(R.string.hardware_title_display_name, ItemType.HARDWARE))
            add(Item(R.string.hardware_title_display_hdr, ItemType.HARDWARE))
            add(Item(R.string.hardware_title_display_rotation, ItemType.HARDWARE))
            add(Item(R.string.hardware_title_display_state, ItemType.HARDWARE))
            add(Item(R.string.hardware_title_display_cutout, ItemType.HARDWARE))
            add(Item(R.string.hardware_title_display_refresh_rate, ItemType.HARDWARE))
            add(Item(R.string.hardware_title_display_density, ItemType.HARDWARE))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context.displayManger.displays.forEach { display ->
                    get(0).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        subtitle = ItemSubtitle.Text(display.name)
                    }
                    get(1).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            ItemSubtitle.Text(display.isHdr.toString())
                        else
                            ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.O)
                    }
                    get(2).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        subtitle = ItemSubtitle.Text(when (display.rotation) {
                            Surface.ROTATION_0 -> "0\u00B0"
                            Surface.ROTATION_90 -> "90\u00B0"
                            Surface.ROTATION_180 -> "180\u00B0"
                            Surface.ROTATION_270 -> "270\u00B0"
                            else -> null
                        })
                    }
                    get(3).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
                            ItemSubtitle.Text(when (display.state) {
                                Display.STATE_ON -> "STATE_ON"
                                Display.STATE_OFF -> "STATE_OFF"
                                Display.STATE_DOZE -> "STATE_DOZE"
                                Display.STATE_DOZE_SUSPEND -> "STATE_DOZE_SUSPEND"
                                Display.STATE_ON_SUSPEND -> "STATE_ON_SUSPEND"
                                Display.STATE_VR -> "STATE_VR"
                                else -> "STATE_UNKNOWN"
                            })
                        else
                            ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.KITKAT_WATCH)
                    }
                    get(4).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            ItemSubtitle.Text(display.cutout?.run {
                                StringJoiner(" / ").apply {
                                    if (!boundingRectTop.isEmpty) add("Top")
                                    if (!boundingRectBottom.isEmpty) add("Bottom")
                                    if (!boundingRectLeft.isEmpty) add("Left")
                                    if (!boundingRectRight.isEmpty) add("Right")
                                }.toString()
                            })
                        else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q)
                    }
                    get(5).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        subtitle = ItemSubtitle.Text("${display.refreshRate.toInt()}Hz")
                    }
                    get(6).apply {
                        titleFormatArgs = arrayOf(display.displayId.toString())
                        val width: Int
                        val height: Int
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            val metrics = DisplayMetrics().apply { display.getRealMetrics(this) }
                            width = metrics.widthPixels
                            height = metrics.heightPixels
                        } else {
                            val rawH = Display::class.java.getMethod("getRawHeight")
                            val rawW = Display::class.java.getMethod("getRawWidth")
                            width = rawW.invoke(display) as Int
                            height = rawH.invoke(display) as Int
                        }
                        subtitle = ItemSubtitle.Text(when (context.resources.displayMetrics.densityDpi) {
                            DisplayMetrics.DENSITY_LOW -> "LDPI"
                            DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
                            DisplayMetrics.DENSITY_HIGH -> "HDPI"
                            DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
                            DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
                            DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
                            DisplayMetrics.DENSITY_TV -> "TVDPI"
                            DisplayMetrics.DENSITY_140 -> "140 DPI"
                            DisplayMetrics.DENSITY_180 -> "180 DPI"
                            DisplayMetrics.DENSITY_200 -> "200 DPI"
                            DisplayMetrics.DENSITY_220 -> "220 DPI"
                            DisplayMetrics.DENSITY_260 -> "260 DPI"
                            DisplayMetrics.DENSITY_280 -> "280 DPI"
                            DisplayMetrics.DENSITY_300 -> "300 DPI"
                            DisplayMetrics.DENSITY_340 -> "340 DPI"
                            DisplayMetrics.DENSITY_360 -> "360 DPI"
                            DisplayMetrics.DENSITY_400 -> "400 DPI"
                            DisplayMetrics.DENSITY_420 -> "420 DPI"
                            DisplayMetrics.DENSITY_440 -> "440 DPI"
                            DisplayMetrics.DENSITY_560 -> "560 DPI"
                            DisplayMetrics.DENSITY_600 -> "600 DPI"
                            else -> null
                        }.plus(" (${height}x${width} pixels)").trim())
                    }
                }
            } else
                forEach {
                    it.titleFormatArgs = arrayOf("0")
                    it.subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.JELLY_BEAN_MR1)
                }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }.toTypedArray()
}