package com.cwlarson.deviceid.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.*
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Storage
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.os.EnvironmentCompat
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.ChartItem
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.DispatcherProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class HardwareRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val context: Context,
    private val preferenceManager: PreferenceManager
) : TabData(dispatcherProvider, context, preferenceManager) {
    private val activityManager by lazy { context.getSystemService<ActivityManager>() }
    private val displayManager by lazy { context.getSystemService<DisplayManager>() }

    override fun items(): Flow<List<Item>> = combineTransform<Any, List<Item>>(
        ramSize(), formattedInternalMemory(), formattedExternalMemory(),
        getBattery(), getDisplayInfo(), socManufacturer(), socModel()
    ) { items ->
        emit(mutableListOf<Item>().apply {
            items.forEach {
                when (it) {
                    is Item -> add(it)
                    is List<*> -> addAll(it.filterIsInstance<Item>())
                }
            }
        })
    }.flowOn(dispatcherProvider.IO)

    private fun ramSize() = flow {
        while (true) {
            val result = Item(
                title = R.string.hardware_title_memory, itemType = ItemType.HARDWARE,
                subtitle = try {
                    activityManager?.let {
                        val mi = ActivityManager.MemoryInfo().apply {
                            it.getMemoryInfo(this)
                        }
                        ItemSubtitle.Chart(
                            ChartItem(
                                mi.availMem.toFloat(),
                                mi.totalMem.toFloat(),
                                Icons.Outlined.Memory,
                                if (mi.totalMem > 0)
                                    context.resources.getString(
                                        R.string.hardware_storage_output_format,
                                        Formatter.formatFileSize(context, mi.availMem),
                                        Formatter.formatFileSize(context, mi.totalMem)
                                    )
                                else null
                            )
                        )
                    } ?: ItemSubtitle.Error
                } catch (e: Throwable) {
                    Timber.w(e)
                    ItemSubtitle.Error
                }
            )
            emit(result)
            val rate = preferenceManager.autoRefreshRateMillis.first()
            if (rate > 0) delay(rate) else break
        }
    }

    private fun formattedInternalMemory() = flow {
        while (true) {
            val result = Item(
                title = R.string.hardware_title_internal_storage, itemType = ItemType.HARDWARE,
                subtitle = try {
                    val stat = StatFs(Environment.getDataDirectory().path)
                    val available = stat.availableBytes
                    val total = stat.totalBytes
                    ItemSubtitle.Chart(
                        ChartItem(
                            available.toFloat(),
                            total.toFloat(),
                            Icons.Outlined.Storage,
                            if (total > 0L)
                                context.resources.getString(
                                    R.string.hardware_storage_output_format,
                                    Formatter.formatFileSize(context, available),
                                    Formatter.formatFileSize(context, total)
                                )
                            else null
                        )
                    )
                } catch (e: Throwable) {
                    Timber.w(e)
                    ItemSubtitle.Error
                }
            )
            emit(result)
            val rate = preferenceManager.autoRefreshRateMillis.first()
            if (rate > 0) delay(rate) else break
        }
    }

    private fun formattedExternalMemory() = flow {
        while (true) {
            val result = Item(
                title = R.string.hardware_title_external_storage, itemType = ItemType.HARDWARE,
                subtitle = try {
                    // Mounted and not emulated, most likely a real SD Card
                    val appsDir = ContextCompat.getExternalFilesDirs(context, null).filter {
                        it != null
                                && EnvironmentCompat.getStorageState(it) == Environment.MEDIA_MOUNTED
                                && !Environment.isExternalStorageEmulated(it)
                    }
                    val availSize = appsDir.map { it.freeSpace }.toTypedArray().sum()
                    val totalSize = appsDir.map { it.totalSpace }.toTypedArray().sum()
                    ItemSubtitle.Chart(
                        ChartItem(
                            availSize.toFloat(),
                            totalSize.toFloat(),
                            Icons.Outlined.Storage,
                            if (totalSize > 0L)
                                context.resources.getString(
                                    R.string.hardware_storage_output_format,
                                    Formatter.formatFileSize(context, availSize),
                                    Formatter.formatFileSize(context, totalSize)
                                )
                            else null
                        )
                    )
                } catch (e: Throwable) {
                    Timber.w(e)
                    ItemSubtitle.Error
                }
            )
            emit(result)
            val rate = preferenceManager.autoRefreshRateMillis.first()
            if (rate > 0) delay(rate) else break
        }
    }

    private fun getBattery() = callbackFlow {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val result = goAsync()
                try {
                    intent?.action?.let { act ->
                        if (act == Intent.ACTION_BATTERY_CHANGED) {
                            //int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10;
                            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                            val item = Item(
                                title = R.string.hardware_title_battery,
                                itemType = ItemType.HARDWARE,
                                subtitle = ItemSubtitle.Chart(
                                    ChartItem(
                                        (100 - level).toFloat(),
                                        100f,
                                        Icons.Outlined.BatteryStd,
                                        StringBuilder().apply {
                                            append("$level% - ")
                                            // Are we charging / charged?
                                            when (intent.getIntExtra(
                                                BatteryManager.EXTRA_STATUS,
                                                -1
                                            )) {
                                                BatteryManager.BATTERY_STATUS_CHARGING ->
                                                    append(context.getString(R.string.battery_status_charging))
                                                BatteryManager.BATTERY_STATUS_FULL ->
                                                    append(context.getString(R.string.battery_status_full))
                                                BatteryManager.BATTERY_STATUS_DISCHARGING ->
                                                    append(context.getString(R.string.battery_status_discharging))
                                                BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                                                    append(context.getString(R.string.battery_status_not_charging))
                                                else -> append(context.getString(R.string.battery_status_unknown))
                                            }
                                            val health = when (intent.getIntExtra(
                                                BatteryManager.EXTRA_HEALTH,
                                                0
                                            )) {
                                                BatteryManager.BATTERY_HEALTH_COLD ->
                                                    context.getString(R.string.battery_health_cold)
                                                BatteryManager.BATTERY_HEALTH_DEAD ->
                                                    context.getString(R.string.battery_health_dead)
                                                BatteryManager.BATTERY_HEALTH_GOOD ->
                                                    context.getString(R.string.battery_health_good)
                                                BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                                                    context.getString(R.string.battery_health_overheat)
                                                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                                                    context.getString(R.string.battery_health_over_voltage)
                                                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE ->
                                                    context.getString(R.string.battery_health_unspecified_failure)
                                                else -> context.getString(R.string.battery_health_unknown)
                                            }
                                            append(" ($health) ")
                                            // How are we charging?
                                            when (intent.getIntExtra(
                                                BatteryManager.EXTRA_PLUGGED,
                                                -1
                                            )) {
                                                BatteryManager.BATTERY_PLUGGED_USB ->
                                                    append(context.getString(R.string.battery_plugged_usb))
                                                BatteryManager.BATTERY_PLUGGED_AC ->
                                                    append(context.getString(R.string.battery_plugged_ac))
                                                BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                                                    append(context.getString(R.string.battery_plugged_wireless))
                                            }
                                            //subTitle = String.valueOf(temperature+"\u00b0C");
                                        }.trim().toString()
                                    )
                                )
                            )
                            trySend(item)
                        }
                    }
                } catch (e: Throwable) {
                    Timber.w(e)
                    trySend(
                        Item(
                            title = R.string.hardware_title_battery,
                            itemType = ItemType.HARDWARE,
                            subtitle = ItemSubtitle.Error
                        )
                    )
                } finally {
                    result.finish()
                }
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose { context.unregisterReceiver(batteryReceiver) }
    }.conflate()

    private fun getDisplayInfo() = callbackFlow {
        val map = mutableMapOf<Int, List<Item>>()
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                try {
                    displayManager?.getDisplay(displayId)?.let { display ->
                        map[displayId] = listOf(
                            Item(
                                title = R.string.hardware_title_display_name,
                                itemType = ItemType.HARDWARE,
                                subtitle = ItemSubtitle.Text(display.name),
                                titleFormatArgs = listOf("$displayId")
                            ), Item(
                                title = R.string.hardware_title_display_hdr,
                                itemType = ItemType.HARDWARE,
                                subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    ItemSubtitle.Text("${display.isHdr}")
                                else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.O),
                                titleFormatArgs = listOf("$displayId")
                            ), Item(
                                title = R.string.hardware_title_display_rotation,
                                itemType = ItemType.HARDWARE,
                                subtitle = ItemSubtitle.Text(
                                    when (display.rotation) {
                                        Surface.ROTATION_0 -> context.getString(R.string.display_rotation_0)
                                        Surface.ROTATION_90 -> context.getString(R.string.display_rotation_90)
                                        Surface.ROTATION_180 -> context.getString(R.string.display_rotation_180)
                                        Surface.ROTATION_270 -> context.getString(R.string.display_rotation_270)
                                        else -> null
                                    }
                                ),
                                titleFormatArgs = listOf("$displayId")
                            ), Item(
                                title = R.string.hardware_title_display_state,
                                itemType = ItemType.HARDWARE,
                                subtitle = ItemSubtitle.Text(
                                    when (display.state) {
                                        Display.STATE_ON -> context.getString(R.string.display_state_on)
                                        Display.STATE_OFF -> context.getString(R.string.display_state_off)
                                        Display.STATE_DOZE -> context.getString(R.string.display_state_doze)
                                        Display.STATE_DOZE_SUSPEND -> context.getString(R.string.display_state_doze_suspend)
                                        Display.STATE_ON_SUSPEND -> context.getString(R.string.display_state_on_suspend)
                                        Display.STATE_VR -> context.getString(R.string.display_state_vr)
                                        else -> context.getString(R.string.display_state_unknown)
                                    }
                                ),
                                titleFormatArgs = listOf("$displayId")
                            ), Item(
                                title = R.string.hardware_title_display_cutout,
                                itemType = ItemType.HARDWARE,
                                subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                    ItemSubtitle.Text(display.cutout?.run {
                                        StringJoiner(" / ").apply {
                                            if (!boundingRectTop.isEmpty) add(context.getString(R.string.display_cutout_top))
                                            if (!boundingRectBottom.isEmpty) add(context.getString(R.string.display_cutout_bottom))
                                            if (!boundingRectLeft.isEmpty) add(context.getString(R.string.display_cutout_left))
                                            if (!boundingRectRight.isEmpty) add(context.getString(R.string.display_cutout_right))
                                        }.toString()
                                    })
                                else ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q),
                                titleFormatArgs = listOf("$displayId")
                            ), Item(
                                title = R.string.hardware_title_display_refresh_rate,
                                itemType = ItemType.HARDWARE,
                                subtitle = ItemSubtitle.Text(
                                    context.getString(
                                        R.string.display_refresh_rate,
                                        display.refreshRate.toInt()
                                    )
                                ),
                                titleFormatArgs = listOf("$displayId")
                            ), Item(
                                title = R.string.hardware_title_display_density,
                                itemType = ItemType.HARDWARE,
                                subtitle = ItemSubtitle.Text(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                        context.createDisplayContext(display)
                                            .createWindowContext(TYPE_APPLICATION, null)
                                            .getSystemService(WindowManager::class.java)
                                            .maximumWindowMetrics.bounds.run {
                                                densityDpiToString(
                                                    context.resources.configuration.densityDpi,
                                                    height(), width()
                                                )
                                            }
                                    else DisplayMetrics().apply {
                                        @Suppress("DEPRECATION")
                                        display.getRealMetrics(this)
                                    }.run {
                                        densityDpiToString(
                                            densityDpi, heightPixels, widthPixels
                                        )
                                    }
                                ), titleFormatArgs = listOf("$displayId")
                            )
                        )
                    }
                } catch (e: Throwable) {
                    Timber.w(e)
                }
                trySend(map.values.flatten())
            }

            override fun onDisplayRemoved(displayId: Int) {
                map.remove(displayId)
                trySend(map.values.flatten())
            }

            override fun onDisplayChanged(displayId: Int) {
                onDisplayAdded(displayId)
            }
        }
        if (map.isEmpty())
            displayManager?.displays?.forEach { listener.onDisplayAdded(it.displayId) }
                ?: trySend(map.values.flatten())
        displayManager?.registerDisplayListener(listener, Handler(Looper.getMainLooper()))
        awaitClose { displayManager?.unregisterDisplayListener(listener) }
    }.conflate()

    /**
     * To convert screen density, height and width to the [Item] text format
     * @param densityDpi Dpi int from either [DisplayMetrics] or [WindowManager]
     * @param height Int value of the current screen height
     * @param width Int value of the current screen width
     */
    private fun densityDpiToString(densityDpi: Int, height: Int, width: Int): String =
        when (densityDpi) {
            DisplayMetrics.DENSITY_LOW -> R.string.display_density_ldpi
            DisplayMetrics.DENSITY_MEDIUM -> R.string.display_density_mdpi
            DisplayMetrics.DENSITY_HIGH -> R.string.display_density_hdpi
            DisplayMetrics.DENSITY_XHIGH -> R.string.display_density_xhdpi
            DisplayMetrics.DENSITY_XXHIGH -> R.string.display_density_xxhdpi
            DisplayMetrics.DENSITY_XXXHIGH -> R.string.display_density_xxxhdpi
            DisplayMetrics.DENSITY_TV -> R.string.display_density_tvdpi
            DisplayMetrics.DENSITY_140 -> R.string.display_density_140
            DisplayMetrics.DENSITY_180 -> R.string.display_density_180
            DisplayMetrics.DENSITY_200 -> R.string.display_density_200
            DisplayMetrics.DENSITY_220 -> R.string.display_density_220
            DisplayMetrics.DENSITY_260 -> R.string.display_density_260
            DisplayMetrics.DENSITY_280 -> R.string.display_density_280
            DisplayMetrics.DENSITY_300 -> R.string.display_density_300
            DisplayMetrics.DENSITY_340 -> R.string.display_density_340
            DisplayMetrics.DENSITY_360 -> R.string.display_density_360
            DisplayMetrics.DENSITY_400 -> R.string.display_density_400
            DisplayMetrics.DENSITY_420 -> R.string.display_density_420
            DisplayMetrics.DENSITY_440 -> R.string.display_density_440
            DisplayMetrics.DENSITY_450 -> R.string.display_density_450
            DisplayMetrics.DENSITY_560 -> R.string.display_density_560
            DisplayMetrics.DENSITY_600 -> R.string.display_density_600
            else -> null
        }?.run {
            context.getString(
                R.string.display_density_format_with_name,
                context.getString(this), height, width
            )
        } ?: context.getString(
            R.string.display_density_format_with_name,
            context.getString(R.string.display_density_other, densityDpi), height, width
        )

    private fun socManufacturer() = flowOf(
        Item(
            title = R.string.hardware_title_soc_manufacturer, itemType = ItemType.HARDWARE,
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ItemSubtitle.Text(Build.SOC_MANUFACTURER)
            } else {
                ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.S)
            }
        )
    )

    private fun socModel() = flowOf(
        Item(
            title = R.string.hardware_title_soc_model, itemType = ItemType.HARDWARE,
            subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ItemSubtitle.Text(Build.SOC_MODEL)
            } else {
                ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.S)
            }
        )
    )
}