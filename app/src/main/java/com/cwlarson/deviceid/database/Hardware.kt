package com.cwlarson.deviceid.database

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.Display
import androidx.core.content.ContextCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.ChartItem
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.activityManager
import com.cwlarson.deviceid.util.windowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.lang.reflect.Method

class Hardware(private val context: Context, private val db: AppDatabase, scope: CoroutineScope) {
    //Set Hardware Tiles
    init {
        scope.launch(Dispatchers.IO) {
            db.addItems(context, deviceScreenDensity(), ramSize(), formattedInternalMemory(),
                    formattedExternalMemory())
        }
        getBattery()
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
            val sizeInPixels = " (${height}x$width)"

            subtitle = when (density) {
                DisplayMetrics.DENSITY_LOW -> "LDPI$sizeInPixels"
                DisplayMetrics.DENSITY_MEDIUM -> "MDPI$sizeInPixels"
                DisplayMetrics.DENSITY_HIGH -> "HDPI$sizeInPixels"
                DisplayMetrics.DENSITY_XHIGH -> "XHDPI$sizeInPixels"
                DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI$sizeInPixels"
                DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI$sizeInPixels"
                DisplayMetrics.DENSITY_TV -> "TVDPI$sizeInPixels"
                DisplayMetrics.DENSITY_400 -> "400DPI$sizeInPixels"
                DisplayMetrics.DENSITY_560 -> "560DPI$sizeInPixels"
                else -> { null }
            }
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
            val available = File(context.filesDir.absoluteFile.toString()).freeSpace
            val total = File(context.filesDir.absoluteFile.toString()).totalSpace
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
            var availSize = 0L
            var totalSize = 0L
            val appsDir = ContextCompat.getExternalFilesDirs(context, null)
            for (file in appsDir) {
                availSize += file?.parentFile?.parentFile?.parentFile?.parentFile?.freeSpace ?: 0L
                totalSize += file?.parentFile?.parentFile?.parentFile?.parentFile?.totalSpace ?: 0L
            }
            availSize -= File(context.filesDir.absoluteFile.toString()).freeSpace
            totalSize -= File(context.filesDir.absoluteFile.toString()).totalSpace
            if (totalSize > 0L)
                subtitle = context.resources.getString(R.string.hardware_storage_output_format,
                        Formatter.formatFileSize(context, availSize), Formatter.formatFileSize(context, totalSize))
            chartItem = ChartItem(availSize.toFloat(), totalSize.toFloat(), R.drawable.ic_storage)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }
    // FIXME: Move to lifecycle components
    private fun getBattery() =
        context.registerReceiver(InfoReceiver(), IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    private inner class InfoReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            intent?.action?.let { act ->
                val result = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    if(act == Intent.ACTION_BATTERY_CHANGED) {
                        //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
                        val health = when(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.BATTERY_HEALTH_COLD)
                            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.BATTERY_HEALTH_DEAD)
                            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.BATTERY_HEALTH_GOOD)
                            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.BATTERY_HEALTH_OVERHEAT)
                            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.BATTERY_HEALTH_OVER_VOLTAGE)
                            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.BATTERY_HEALTH_UNSPECIFIED_FAILURE)
                            else -> context.getString(R.string.BATTERY_HEALTH_UNKNOWN)
                        }
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val subtitle = StringBuilder()
                        subtitle.append("$level% - ")
                        // Are we charging / charged?
                        when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                            BatteryManager.BATTERY_STATUS_CHARGING -> subtitle.append(context.getString(R.string.BATTERY_STATUS_CHARGING))
                            BatteryManager.BATTERY_STATUS_FULL -> subtitle.append(context.getString(R.string.BATTERY_STATUS_FULL))
                            BatteryManager.BATTERY_STATUS_DISCHARGING -> subtitle.append(context.getString(R.string.BATTERY_STATUS_DISCHARGING))
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> subtitle.append(context.getString(R.string.BATTERY_STATUS_NOT_CHARGING))
                            else -> subtitle.append(context.getString(R.string.BATTERY_STATUS_UNKNOWN))
                        }
                        subtitle.append(" ($health)")
                        // How are we charging?
                        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                        val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                        val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
                        subtitle.append(" ")
                        if (usbCharge)
                            subtitle.append(context.getString(R.string.BATTERY_PLUGGED_USB))
                        else if (acCharge)
                            subtitle.append(context.getString(R.string.BATTERY_PLUGGED_AC))
                        //subTitle = String.valueOf(temperature+"\u00b0C");
                        val item = Item("Battery", ItemType.HARDWARE)
                        item.subtitle = subtitle.toString()
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