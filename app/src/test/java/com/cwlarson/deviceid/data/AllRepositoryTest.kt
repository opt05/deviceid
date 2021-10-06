package com.cwlarson.deviceid.data

import android.app.Application
import android.content.Intent
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.testutils.itemFromList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class AllRepositoryTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var context: Application
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var repository: AllRepository

    @Before
    fun setup() {
        context = spy(ApplicationProvider.getApplicationContext() as Application)
        @Suppress("DEPRECATION")
        context.sendStickyBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            putExtra(BatteryManager.EXTRA_LEVEL, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_FULL)
            putExtra(BatteryManager.EXTRA_PLUGGED, -1)
        })
        preferencesManager = mock()
        repository = AllRepository(context, preferencesManager)
    }

    @FlowPreview
    @Test
    fun `Verify item list is from all repositories`() = runBlocking {
        whenever(preferencesManager.autoRefreshRateMillis).thenReturn(flowOf(0))
        repository.items().test {
            val item = awaitItem()
            assertNotNull(item.itemFromList(R.string.device_title_android_id))
            assertNotNull(item.itemFromList(R.string.network_title_phone_number))
            assertNotNull(item.itemFromList(R.string.software_title_android_version))
            assertNotNull(item.itemFromList(R.string.hardware_title_battery))
            cancelAndConsumeRemainingEvents()
        }

    }
}