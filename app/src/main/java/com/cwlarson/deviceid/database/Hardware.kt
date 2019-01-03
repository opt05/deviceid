package com.cwlarson.deviceid.database

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.ChartItem
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import java.io.File
import java.lang.reflect.Method

@WorkerThread
internal class Hardware(context: Context, db: AppDatabase) {
    private val tag = Hardware::class.java.simpleName
    private val context: Context = context.applicationContext

    //Set Hardware Tiles
    init {
        db.addItems(context,deviceScreenDensity())
        db.addItems(context,ramSize())
        db.addItems(context,formattedInternalMemory())
        db.addItems(context,formattedExternalMemory())
        getBattery()
    }

    // For JellyBean 4.2 (API 17) and onward
    private fun deviceScreenDensity(): Item {
        val item = Item("Screen Density", ItemType.HARDWARE)
        try {
            val density = context.resources.displayMetrics.densityDpi
            val width: Int
            val height: Int
            val metrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val mGetRawH: Method
            val mGetRawW: Method
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
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

            when (density) {
                DisplayMetrics.DENSITY_LOW -> item.subtitle = "LDPI$sizeInPixels"
                DisplayMetrics.DENSITY_MEDIUM -> item.subtitle = "MDPI$sizeInPixels"
                DisplayMetrics.DENSITY_HIGH -> item.subtitle = "HDPI$sizeInPixels"
                DisplayMetrics.DENSITY_XHIGH -> item.subtitle = "XHDPI$sizeInPixels"
                DisplayMetrics.DENSITY_XXHIGH -> item.subtitle = "XXHDPI$sizeInPixels"
                DisplayMetrics.DENSITY_XXXHIGH -> item.subtitle = "XXXHDPI$sizeInPixels"
                DisplayMetrics.DENSITY_TV -> item.subtitle = "TVDPI$sizeInPixels"
                DisplayMetrics.DENSITY_400 -> item.subtitle = "400DPI$sizeInPixels"
                DisplayMetrics.DENSITY_560 -> item.subtitle = "560DPI$sizeInPixels"
                else -> {
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "NoSuchMethodException in getDeviceScreenDensity")
        }

        return item
    }

    //Total memory is only available on API 16+
    private fun ramSize(): Item {
        val item = Item("Memory", ItemType.HARDWARE)
        try {
            val mi = ActivityManager.MemoryInfo()
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            activityManager.getMemoryInfo(mi)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                item.subtitle = context.resources.getString(R.string
                        .hardware_storage_output_format_api15,
                        Formatter.formatFileSize(context, mi.availMem))
                item.chartitem = ChartItem(mi.availMem.toFloat(), 0f, R.drawable.ic_memory)
            } else {
                item.subtitle = if (mi.totalMem <= 0)
                    context.resources.getString(R.string.not_found)
                else
                    context.resources.getString(R.string.hardware_storage_output_format,
                            Formatter.formatFileSize(context, mi.availMem),
                            Formatter.formatFileSize(context, mi.totalMem))
                item.chartitem = ChartItem(mi.availMem.toFloat(), mi.totalMem.toFloat(), R.drawable.ic_memory)
            }
        } catch (e: Exception) {
            Log.w(tag, "Exception in getRamSize")
        }

        return item
    }

    private fun formattedInternalMemory(): Item {
        val item = Item("Internal Storage", ItemType.HARDWARE)
        val available = File(context.filesDir.absoluteFile.toString()).freeSpace
        val total = File(context.filesDir.absoluteFile.toString()).totalSpace
        if (total > 0L)
            item.subtitle = context.resources.getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context, available), Formatter.formatFileSize(context, total))
        item.chartitem = ChartItem(available.toFloat(), total.toFloat(), R.drawable.ic_storage)
        return item
    }

    private fun formattedExternalMemory(): Item {
        val item = Item("External Storage", ItemType.HARDWARE)
        var availSize = 0L
        var totalSize = 0L
        val appsDir = ContextCompat.getExternalFilesDirs(context, null)
        for (file in appsDir) {
            availSize += file.parentFile.parentFile.parentFile.parentFile.freeSpace
            totalSize += file.parentFile.parentFile.parentFile.parentFile.totalSpace
        }
        availSize -= File(context.filesDir.absoluteFile.toString()).freeSpace
        totalSize -= File(context.filesDir.absoluteFile.toString()).totalSpace
        if (totalSize > 0L)
            item.subtitle = context.resources.getString(R.string.hardware_storage_output_format, Formatter.formatFileSize(context, availSize), Formatter.formatFileSize(context, totalSize))
        item.chartitem = ChartItem(availSize.toFloat(), totalSize.toFloat(), R.drawable.ic_storage)
        return item
    }

    private fun getBattery() {
        //Item item = new Item("Battery", ItemType.HARDWARE);
        context.registerReceiver(InfoReceiver(), IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
    // Are we charging / charged?
    // How are we charging?
    //subTitle = String.valueOf(temperature+"\u00b0C");
    private inner class InfoReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action == null) return
            val result = goAsync()
            ProcessInfoBroadcastAsync(context, intent, result).execute()
            context.unregisterReceiver(this)
        }
    }

    private class ProcessInfoBroadcastAsync internal constructor(context: Context, private val intent: Intent, private val result: BroadcastReceiver.PendingResult) : AsyncTask<Void, Void, Void>() {
        private val database: AppDatabase = AppDatabase.getDatabase(context)
        private val charging: String = context.getString(R.string.BATTERY_STATUS_CHARGING)
        private val full: String = context.getString(R.string.BATTERY_STATUS_FULL)
        private val discharging: String = context.getString(R.string.BATTERY_STATUS_DISCHARGING)
        private val notcharging: String = context.getString(R.string.BATTERY_STATUS_NOT_CHARGING)
        private val unknown: String = context.getString(R.string.BATTERY_STATUS_UNKNOWN)
        private val usb: String = context.getString(R.string.BATTERY_PLUGGED_USB)
        private val ac: String = context.getString(R.string.BATTERY_PLUGGED_AC)

        override fun doInBackground(vararg voids: Void): Void? {
            if (intent.action?.equals(Intent.ACTION_BATTERY_CHANGED, ignoreCase = true) == true) {
                //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val subtitle = StringBuilder()
                subtitle.append(level).append("% - ")
                // Are we charging / charged?
                when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> subtitle.append(charging)
                    BatteryManager.BATTERY_STATUS_FULL -> subtitle.append(full)
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> subtitle.append(discharging)
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> subtitle.append(notcharging)
                    else -> subtitle.append(unknown)
                }
                // How are we charging?
                val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
                subtitle.append(" ")
                if (usbCharge)
                    subtitle.append(usb)
                else if (acCharge)
                    subtitle.append(ac)
                //subTitle = String.valueOf(temperature+"\u00b0C");
                val item = Item("Battery", ItemType.HARDWARE)
                item.subtitle = subtitle.toString()
                item.chartitem = ChartItem((100 - level).toFloat(), 100f, R.drawable.ic_battery)
                database.itemDao().insertItems(item)
                // Must call finish() so the BroadcastReceiver can be recycled.
                result.finish()
            }
            return null
        }
    }
}