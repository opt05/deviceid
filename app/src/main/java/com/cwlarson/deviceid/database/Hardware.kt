package com.cwlarson.deviceid.database

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
import androidx.core.content.ContextCompat
import androidx.core.os.EnvironmentCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.ChartItem
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.activityManager
import com.cwlarson.deviceid.util.windowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.lang.reflect.Method
import kotlin.coroutines.resume

class Hardware(private val context: Context, private val db: AppDatabase, private val scope: CoroutineScope) {
    init {
        scope.launch {
            db.addItems(context, deviceScreenDensity(), ramSize(), formattedInternalMemory(),
                    formattedExternalMemory(), getBattery())
        }
    }

    private fun deviceScreenDensity() = Item("Screen Density", ItemType.HARDWARE).apply {
        try {
            val density = context.resources.displayMetrics.densityDpi
            val width: Int
            val height: Int
            val metrics = DisplayMetrics()
            val display = context.windowManager.defaultDisplay
            val mGetRawH: Method
            val mGetRawW: Method
            // For JellyBean 4.2 (API 17) and onward
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics)
                width = metrics.widthPixels
                height = metrics.heightPixels
            } else {
                mGetRawH = Display::class.java.getMethod("getRawHeight")
                mGetRawW = Display::class.java.getMethod("getRawWidth")
                width = mGetRawW.invoke(display) as Int
                height = mGetRawH.invoke(display) as Int
            }
            subtitle = when (density) {
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
                else -> { null }
            }.plus(" (${height}x$width)").trim()
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    //Total memory is only available on API 16+
    private fun ramSize() = Item("Memory", ItemType.HARDWARE).apply {
        try {
            val mi = ActivityManager.MemoryInfo()
            context.activityManager.getMemoryInfo(mi)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                subtitle = context.resources.getString(R.string
                        .hardware_storage_output_format_api15,
                        Formatter.formatFileSize(context, mi.availMem))
                chartItem = ChartItem(mi.availMem.toFloat(), 0f, R.drawable.ic_memory)
            } else {
                subtitle = if (mi.totalMem <= 0)
                    context.resources.getString(R.string.not_found)
                else
                    context.resources.getString(R.string.hardware_storage_output_format,
                            Formatter.formatFileSize(context, mi.availMem),
                            Formatter.formatFileSize(context, mi.totalMem))
                chartItem = ChartItem(mi.availMem.toFloat(), mi.totalMem.toFloat(), R.drawable.ic_memory)
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun formattedInternalMemory() = Item("Internal Storage", ItemType.HARDWARE).apply {
        try {
            val dir = Environment.getDataDirectory()
            val stat = StatFs(dir.path)
            @Suppress("DEPRECATION")
            val available = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                stat.blockSize.toLong() * stat.availableBlocks.toLong()
            else stat.availableBytes
            @Suppress("DEPRECATION")
            val total = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                stat.blockCount.toLong() * stat.blockCount.toLong()
            else stat.totalBytes
            if (total > 0L)
                subtitle = context.resources.getString(R.string.hardware_storage_output_format,
                        Formatter.formatFileSize(context, available), Formatter.formatFileSize(context, total))
            chartItem = ChartItem(available.toFloat(), total.toFloat(), R.drawable.ic_storage)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun formattedExternalMemory() = Item("External Storage", ItemType.HARDWARE).apply {
        try {
            // Mounted and not emulated, most likely a real SD Card
            val appsDir = ContextCompat.getExternalFilesDirs(context, null).filter { it != null
                    && EnvironmentCompat.getStorageState(it) == Environment.MEDIA_MOUNTED
                    && !it.isExternalStorageEmulatedCompat()}
            val availSize = appsDir.map { it.freeSpace }.toTypedArray().sum()
            val totalSize = appsDir.map { it.totalSpace }.toTypedArray().sum()
            if (totalSize > 0L)
                subtitle = context.resources.getString(R.string.hardware_storage_output_format,
                        Formatter.formatFileSize(context, availSize), Formatter.formatFileSize(context, totalSize))
            chartItem = ChartItem(availSize.toFloat(), totalSize.toFloat(), R.drawable.ic_storage)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun File.isExternalStorageEmulatedCompat(): Boolean =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Environment.isExternalStorageEmulated(this)
        else Environment.isExternalStorageEmulated()

    private suspend fun getBattery(): Item =
            suspendCancellableCoroutine { cont ->
                val batteryReceiver = object: BroadcastReceiver() {
                    override fun onReceive(c: Context?, intent: Intent?) {
                        context.unregisterReceiver(this)
                        intent?.action?.let { act ->
                            val result = goAsync()
                            val item = Item("Battery", ItemType.HARDWARE)
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
                                item.subtitle = StringBuilder().apply {
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
                                }.toString()
                                item.chartItem = ChartItem((100 - level).toFloat(), 100f, R.drawable.ic_battery)
                                // Must call finish() so the BroadcastReceiver can be recycled.
                                result.finish()
                            }
                            cont.resume(item)
                        }
                    }
                }
                cont.invokeOnCancellation {
                    context.unregisterReceiver(batteryReceiver)
                }
                context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }

    private inner class InfoReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent?) {
            while(scope.isActive) {
                intent?.action?.let { act ->
                    val result = goAsync()
                    scope.launch {
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
                            val item = Item("Battery", ItemType.HARDWARE)
                            item.subtitle = StringBuilder().apply {
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
                                //append(" $temperature\u00b0C")
                            }.toString()
                            item.chartItem = ChartItem((100 - level).toFloat(), 100f, R.drawable.ic_battery)
                            db.itemDao().insert(item)
                            // Must call finish() so the BroadcastReceiver can be recycled.
                            result.finish()
                        }
                    }
                    context.unregisterReceiver(this)
                }
            }
        }
    }
}