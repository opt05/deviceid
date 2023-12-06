package com.cwlarson.deviceid.data

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Insets
import android.graphics.Rect
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Storage
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.ChartItem
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.testutils.awaitItemFromList
import com.cwlarson.deviceid.testutils.expectMostRecentItemFromList
import com.cwlarson.deviceid.testutils.itemFromList
import com.cwlarson.deviceid.testutils.shadows.ExceptionShadowActivityManager
import com.cwlarson.deviceid.testutils.shadows.ExceptionShadowDisplayManager
import com.cwlarson.deviceid.testutils.shadows.ExceptionShadowStatFs
import com.cwlarson.deviceid.testutils.shadows.MyShadowBuild
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDisplay
import org.robolectric.shadows.ShadowDisplayManager
import org.robolectric.shadows.ShadowEnvironment
import org.robolectric.shadows.ShadowStatFs
import java.io.File

@RunWith(AndroidJUnit4::class)
class HardwareRepositoryTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var context: Application
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var repository: HardwareRepository

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        context = spyk(ApplicationProvider.getApplicationContext() as Application)
        sendBatteryBroadcast()
        preferencesManager = mockk()
        repository = HardwareRepository(dispatcherProvider, context, preferencesManager)
    }

    @Suppress("DEPRECATION")
    private fun sendBatteryBroadcast(
        health: Int = BatteryManager.BATTERY_HEALTH_GOOD, level: Int = 100,
        status: Int = BatteryManager.BATTERY_STATUS_FULL, plugged: Int = -1
    ) {
        context.sendStickyBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_HEALTH, health)
            putExtra(BatteryManager.EXTRA_LEVEL, level)
            putExtra(BatteryManager.EXTRA_STATUS, status)
            putExtra(BatteryManager.EXTRA_PLUGGED, plugged)
        })
    }

    @Test
    fun `Verify item list is returned when items method is called`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            val item = awaitItem()
            assertEquals(R.string.hardware_title_memory, item[0].title)
            assertEquals(R.string.hardware_title_internal_storage, item[1].title)
            assertEquals(R.string.hardware_title_external_storage, item[2].title)
            assertEquals(R.string.hardware_title_battery, item[3].title)
            assertEquals(R.string.hardware_title_display_name, item[4].title)
            assertEquals(R.string.hardware_title_display_hdr, item[5].title)
            assertEquals(R.string.hardware_title_display_rotation, item[6].title)
            assertEquals(R.string.hardware_title_display_state, item[7].title)
            assertEquals(R.string.hardware_title_display_cutout, item[8].title)
            assertEquals(R.string.hardware_title_display_refresh_rate, item[9].title)
            assertEquals(R.string.hardware_title_display_density, item[10].title)
            assertEquals(R.string.hardware_title_soc_manufacturer, item[11].title)
            assertEquals(R.string.hardware_title_soc_model, item[12].title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when ram size is total above 0`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .setMemoryInfo(ActivityManager.MemoryInfo().apply {
                availMem = 1L
                totalMem = 2L
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_memory,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(1F, 2F, Icons.Outlined.Memory, "1 B of 2 B free")
                    )
                ), awaitItemFromList(R.string.hardware_title_memory)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when ram size is total below zero`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .setMemoryInfo(ActivityManager.MemoryInfo().apply {
                availMem = 1L
                totalMem = 0L
            })
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_memory,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(1F, 0F, Icons.Outlined.Memory, null)
                    )
                ), awaitItemFromList(R.string.hardware_title_memory)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when ram size is available and auto refresh is greater than 0`() =
        runTest {
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(1)
            val manager =
                shadowOf(context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).apply {
                    setMemoryInfo(ActivityManager.MemoryInfo().apply {
                        availMem = 1L
                        totalMem = 2L
                    })
                }
            repository.items().test {
                delay(500)
                manager.setMemoryInfo(ActivityManager.MemoryInfo().apply {
                    availMem = 2L
                    totalMem = 2L
                })
                delay(1500)
                assertEquals(
                    Item(
                        title = R.string.hardware_title_memory,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Chart(
                            ChartItem(2F, 2F, Icons.Outlined.Memory, "2 B of 2 B free")
                        )
                    ), expectMostRecentItemFromList(R.string.hardware_title_memory)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `Returns error when ram size with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.ACTIVITY_SERVICE)
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_memory,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.hardware_title_memory)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowActivityManager::class])
    fun `Returns error when ram size with an exception`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_memory,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.hardware_title_memory)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when internal memory is total above 0`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        ShadowStatFs.registerStats(Environment.getDataDirectory().path, 1000, 2, 1)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_internal_storage,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            4096F,
                            4096000F,
                            Icons.Outlined.Storage,
                            "4.10 kB of 4.10 MB free"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_internal_storage)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when internal memory is total below zero`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        ShadowStatFs.registerStats(Environment.getDataDirectory().path, 0, 0, 0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_internal_storage,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(0F, 0F, Icons.Outlined.Storage, null)
                    )
                ), awaitItemFromList(R.string.hardware_title_internal_storage)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when internal memory is available and auto refresh is greater than 0`() =
        runTest {
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(1)
            ShadowStatFs.registerStats(Environment.getDataDirectory().path, 1000, 2, 1)
            repository.items().test {
                delay(500)
                ShadowStatFs.registerStats(Environment.getDataDirectory().path, 1000, 2, 2)
                delay(1500)
                assertEquals(
                    Item(
                        title = R.string.hardware_title_internal_storage,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Chart(
                            ChartItem(
                                8192F,
                                4096000F,
                                Icons.Outlined.Storage,
                                "8.19 kB of 4.10 MB free"
                            )
                        )
                    ), expectMostRecentItemFromList(R.string.hardware_title_internal_storage)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    @Config(shadows = [ExceptionShadowStatFs::class])
    fun `Returns error when internal memory with an exception`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_internal_storage,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.hardware_title_internal_storage)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when external memory is total above 0`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        val dir = context.getExternalFilesDir(null)
        ShadowEnvironment.setExternalStorageState(dir, Environment.MEDIA_MOUNTED)
        ShadowEnvironment.setIsExternalStorageEmulated(false)
        File(dir, "test").mkdir()
        repository.items().test {
            with(awaitItemFromList(R.string.hardware_title_external_storage)) {
                assertEquals(R.string.hardware_title_external_storage, this?.title)
                assertEquals(ItemType.HARDWARE, this?.itemType)
                assertTrue(
                    this?.subtitle is ItemSubtitle.Chart
                            && subtitle.getIcon() == Icons.Outlined.Storage
                            && subtitle.getSubTitleText()
                        ?.matches(Regex("^(?=.*\\bof\\b)(?=.*\\bfree\\b\$).*\$")) == true
                )
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when external memory is total below zero`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_external_storage,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(0F, 0F, Icons.Outlined.Storage, null)
                    )
                ), awaitItemFromList(R.string.hardware_title_external_storage)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when external memory is available and auto refresh is greater than 0`() =
        runTest {
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(1)
            val dir = context.getExternalFilesDir(null)
            ShadowEnvironment.setExternalStorageState(dir, Environment.MEDIA_MOUNTED)
            ShadowEnvironment.setIsExternalStorageEmulated(false)
            repository.items().test {
                delay(500)
                File(dir, "test").mkdir()
                delay(1500)
                with(awaitItemFromList(R.string.hardware_title_external_storage)) {
                    assertEquals(R.string.hardware_title_external_storage, this?.title)
                    assertEquals(ItemType.HARDWARE, this?.itemType)
                    assertTrue(
                        this?.subtitle is ItemSubtitle.Chart
                                && subtitle.getIcon() == Icons.Outlined.Storage
                                && subtitle.getSubTitleText()
                            ?.matches(Regex("^(?=.*\\bof\\b)(?=.*\\bfree\\b\$).*\$")) == true
                    )
                }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `Returns error when external memory with an exception`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        every { context.getExternalFilesDirs(any()) } throws NullPointerException()
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_external_storage,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.hardware_title_external_storage)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is charging cold over USB`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(
            BatteryManager.BATTERY_HEALTH_COLD, 100,
            BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_PLUGGED_USB
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            0F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "100% - Charging (cold) over USB"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is discharging dead over AC`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(
            BatteryManager.BATTERY_HEALTH_DEAD, 0,
            BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_PLUGGED_AC
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            100F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "0% - Discharging (dead) on AC"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is full good wireless`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(
            health = BatteryManager.BATTERY_HEALTH_GOOD,
            status = BatteryManager.BATTERY_STATUS_FULL,
            plugged = BatteryManager.BATTERY_PLUGGED_WIRELESS
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            0F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "100% - Full (good) wirelessly"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is not charging overheat none`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(
            health = BatteryManager.BATTERY_HEALTH_OVERHEAT,
            status = BatteryManager.BATTERY_STATUS_NOT_CHARGING
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            0F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "100% - Not Charging (overheat)"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is unknown over voltage none`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(
            health = BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE,
            status = BatteryManager.BATTERY_STATUS_UNKNOWN
        )
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            0F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "100% - Unable to determine (over voltage)"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is full health unspecified failure none`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(health = BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            0F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "100% - Full (unspecified failure)"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns text when battery is full unknown none`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        sendBatteryBroadcast(health = BatteryManager.BATTERY_HEALTH_UNKNOWN)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Chart(
                        ChartItem(
                            0F,
                            100F,
                            Icons.Outlined.BatteryStd,
                            "100% - Full (unknown)"
                        )
                    )
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Returns error when battery with an exception`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        every { context.getString(any(), any()) } throws NullPointerException()
        every { context.getString(any()) } throws NullPointerException()
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_battery,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Error
                ), awaitItemFromList(R.string.hardware_title_battery)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when display info named, hdr, 0 rotation, on, all cutout, ldpi`() =
        runTest {
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
            shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
                setName("test")
                setDisplayHdrCapabilities(0, 100f, 100f, 0f, Display.HdrCapabilities.HDR_TYPE_HDR10)
                setRotation(Surface.ROTATION_0)
                setState(Display.STATE_ON)
                setDisplayCutout(
                    DisplayCutout(
                        Insets.NONE,
                        Rect(1, 3, 2, 4), Rect(1, 3, 2, 4),
                        Rect(1, 3, 2, 4), Rect(1, 3, 2, 4)
                    )
                )
                setRefreshRate(60F)
                setDensityDpi(DisplayMetrics.DENSITY_LOW)
                setRealHeight(20)
                setRealWidth(10)
            }
            repository.items().test {
                val list = awaitItem()
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_name,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("test"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_name)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_hdr,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("true"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_hdr)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_rotation,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("0\u00B0"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_rotation)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_state,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("On"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_state)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_cutout,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("Top / Bottom / Left / Right"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_cutout)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_refresh_rate,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("60Hz"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_refresh_rate)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_density,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("LDPI (20x10 pixels)"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_density)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when display not hdr, 90 rotation, off, no cutout, mdpi`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setDisplayHdrCapabilities(0, 100f, 100f, 0f)
            setRotation(Surface.ROTATION_90)
            setState(Display.STATE_OFF)
            setDensityDpi(DisplayMetrics.DENSITY_MEDIUM)
            setRealHeight(40)
            setRealWidth(20)
        }
        repository.items().test {
            val list = awaitItem()
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_hdr,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("false"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_hdr)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_rotation,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("90\u00B0"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_rotation)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_state,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("Off"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_state)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_cutout,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text(null),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_cutout)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("MDPI (40x20 pixels)"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_density)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when display 180 rotation, doze, top cutout, hdpi`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setRotation(Surface.ROTATION_180)
            setState(Display.STATE_DOZE)
            setDisplayCutout(DisplayCutout(Insets.NONE, null, Rect(1, 3, 2, 4), null, null))
            setDensityDpi(DisplayMetrics.DENSITY_HIGH)
            setRealHeight(80)
            setRealWidth(40)
        }
        repository.items().test {
            val list = awaitItem()
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_rotation,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("180\u00B0"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_rotation)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_state,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("Doze"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_state)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_cutout,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("Top"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_cutout)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("HDPI (80x40 pixels)"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_density)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when display 270 rotation, doze suspend, top bottom cutout, xhdpi`() =
        runTest {
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
            shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
                setRotation(Surface.ROTATION_270)
                setState(Display.STATE_DOZE_SUSPEND)
                setDisplayCutout(
                    DisplayCutout(
                        Insets.NONE,
                        null,
                        Rect(1, 3, 2, 4),
                        null,
                        Rect(1, 3, 2, 4)
                    )
                )
                setDensityDpi(DisplayMetrics.DENSITY_XHIGH)
                setRealHeight(160)
                setRealWidth(80)
            }
            repository.items().test {
                val list = awaitItem()
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_rotation,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("270\u00B0"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_rotation)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_state,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("Doze suspend"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_state)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_cutout,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("Top / Bottom"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_cutout)
                )
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_density,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("XHDPI (160x80 pixels)"),
                        titleFormatArgs = listOf("0")
                    ), list.itemFromList(R.string.hardware_title_display_density)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when display on suspend, top bottom left cutout, xxhdpi`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setState(Display.STATE_ON_SUSPEND)
            setDisplayCutout(
                DisplayCutout(
                    Insets.NONE,
                    Rect(1, 3, 2, 4),
                    Rect(1, 3, 2, 4),
                    null,
                    Rect(1, 3, 2, 4)
                )
            )
            setDensityDpi(DisplayMetrics.DENSITY_XXHIGH)
            setRealHeight(320)
            setRealWidth(160)
        }
        repository.items().test {
            val list = awaitItem()
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_state,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("On suspend"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_state)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_cutout,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("Top / Bottom / Left"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_cutout)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("XXHDPI (320x160 pixels)"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_density)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `Returns text when display vr, not possible cutout and hdr, xxxhdpi`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setState(Display.STATE_VR)
            setDensityDpi(DisplayMetrics.DENSITY_XXXHIGH)
            setRealHeight(640)
            setRealWidth(320)
        }
        repository.items().test {
            val list = awaitItem()
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_hdr,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.O),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_hdr)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_state,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("VR"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_state)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_cutout,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.Q),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_cutout)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("XXXHDPI (640x320 pixels)"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_density)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when display unknown, tvdpi`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setState(Display.STATE_UNKNOWN)
            setDensityDpi(DisplayMetrics.DENSITY_TV)
            setRealHeight(1280)
            setRealWidth(640)
        }
        repository.items().test {
            val list = awaitItem()
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_state,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("Unknown"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_state)
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("TVDPI (1280x640 pixels)"),
                    titleFormatArgs = listOf("0")
                ), list.itemFromList(R.string.hardware_title_display_density)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text numbered densities are available and return on new values`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        val defaultDisplay = ShadowDisplay.getDefaultDisplay()
        shadowOf(defaultDisplay).apply {
            setDensityDpi(DisplayMetrics.DENSITY_140)
            setRealHeight(0)
            setRealWidth(0)
        }
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("140 DPI (0x0 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_180)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "180dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("180 DPI (528x360 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_200)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "200dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("200 DPI (587x400 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_220)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "220dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("220 DPI (646x440 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_260)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "260dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("260 DPI (763x520 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_280)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "280dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("280 DPI (822x560 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_300)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "300dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("300 DPI (881x600 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_340)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "340dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("340 DPI (998x680 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_360)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "360dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("360 DPI (1057x720 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_400)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "400dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("400 DPI (1175x800 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_420)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "420dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("420 DPI (1233x840 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_440)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "440dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("440 DPI (1292x880 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_450)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "450dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("450 DPI (1321x900 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_560)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "560dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("560 DPI (1645x1120 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(DisplayMetrics.DENSITY_600)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "600dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("600 DPI (1762x1200 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            //display.setDensityDpi(null)
            ShadowDisplayManager.changeDisplay(defaultDisplay.displayId, "1000dpi")
            delay(500)
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("1000 DPI (2937x2000 pixels)"),
                    titleFormatArgs = listOf("0")
                ), awaitItemFromList(R.string.hardware_title_display_density)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `Returns text numbered densities are available and return on new values on android R`() =
        runTest {
            val displayContext: Activity = mockk()
            @Suppress("DEPRECATION") val windowManager: WindowManager = mockk {
                every { maximumWindowMetrics } returns WindowMetrics(
                    Rect(0, 0, 100, 200), WindowInsets.CONSUMED
                )
            }
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
            every { context.createDisplayContext(any()) } returns displayContext
            every { displayContext.createWindowContext(eq(TYPE_APPLICATION), any()) } returns displayContext
            every { displayContext.getSystemService(eq(WindowManager::class.java)) } returns windowManager
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_density,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("MDPI (200x100 pixels)"),
                        titleFormatArgs = listOf("0")
                    ), awaitItemFromList(R.string.hardware_title_display_density)
                )
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun `Returns text numbered densities are available and return on new values on android U+`() =
        runTest {
            val displayContext: Activity = mockk()
            val windowManager: WindowManager = mockk {
                every { maximumWindowMetrics } returns WindowMetrics(
                    Rect(0, 0, 100, 200), WindowInsets.CONSUMED, 0f
                )
            }
            every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
            every { context.createDisplayContext(any()) } returns displayContext
            every { displayContext.createWindowContext(eq(TYPE_APPLICATION), any()) } returns displayContext
            every { displayContext.getSystemService(eq(WindowManager::class.java)) } returns windowManager
            repository.items().test {
                assertEquals(
                    Item(
                        title = R.string.hardware_title_display_density,
                        itemType = ItemType.HARDWARE,
                        subtitle = ItemSubtitle.Text("MDPI (200x100 pixels)"),
                        titleFormatArgs = listOf("0")
                    ), awaitItemFromList(R.string.hardware_title_display_density)
                )
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Returns text when multiple displays are available`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        val defaultDisplay = ShadowDisplay.getDefaultDisplay()
        shadowOf(defaultDisplay).apply {
            setDensityDpi(DisplayMetrics.DENSITY_MEDIUM)
            setRealHeight(0)
            setRealWidth(0)
        }
        val secondDisplayId = ShadowDisplayManager.addDisplay("w100dp-h200dp")
        repository.items().test {
            delay(1000)
            val list = expectMostRecentItem()
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("MDPI (0x0 pixels)"),
                    titleFormatArgs = listOf("${defaultDisplay.displayId}")
                ),
                list.itemFromList(
                    R.string.hardware_title_display_density,
                    listOf("${defaultDisplay.displayId}")
                )
            )
            assertEquals(
                Item(
                    title = R.string.hardware_title_display_density,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("MDPI (200x100 pixels)"),
                    titleFormatArgs = listOf("$secondDisplayId")
                ),
                list.itemFromList(
                    R.string.hardware_title_display_density,
                    listOf("$secondDisplayId")
                )
            )
        }
    }

    @Test
    fun `Returns text when multiple displays are removed`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        val secondDisplayId = ShadowDisplayManager.addDisplay("w100dp-h200dp")
        repository.items().test {
            ShadowDisplayManager.removeDisplay(secondDisplayId)
            delay(1000)
            assertNull(
                expectMostRecentItemFromList(
                    R.string.hardware_title_display_density,
                    listOf("$secondDisplayId")
                )
            )
        }
    }

    @Test
    fun `Returns error when display info with a null system service`() = runTest {
        shadowOf(context).removeSystemService(Context.DISPLAY_SERVICE)
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertNull(awaitItemFromList(R.string.hardware_title_display_density))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(shadows = [ExceptionShadowDisplayManager::class])
    fun `Returns error when display info with an exception`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertNull(awaitItemFromList(R.string.hardware_title_display_density))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S], shadows = [MyShadowBuild::class])
    fun `Returns text when soc manufacturer is above android R`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        MyShadowBuild.setSoCManufacturer("Google")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_soc_manufacturer,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("Google")
                ), awaitItemFromList(R.string.hardware_title_soc_manufacturer)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `Returns not possible when soc manufacturer is below android S`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_soc_manufacturer,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.S)
                ), awaitItemFromList(R.string.hardware_title_soc_manufacturer)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when soc manufacturer with an exception`() = runTest { }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S], shadows = [MyShadowBuild::class])
    fun `Returns text when soc model is above android R`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        MyShadowBuild.setSoCModel("ABC123")
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_soc_model,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.Text("ABC123")
                ), awaitItemFromList(R.string.hardware_title_soc_model)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `Returns not possible when soc model is below android S`() = runTest {
        every { preferencesManager.autoRefreshRateMillis } returns flowOf(0)
        repository.items().test {
            assertEquals(
                Item(
                    title = R.string.hardware_title_soc_model,
                    itemType = ItemType.HARDWARE,
                    subtitle = ItemSubtitle.NotPossibleYet(Build.VERSION_CODES.S)
                ), awaitItemFromList(R.string.hardware_title_soc_model)
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Ignore("Not possible?")
    @Test
    fun `Returns error when soc model with an exception`() = runTest { }
}