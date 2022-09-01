package com.cwlarson.deviceid.settings

import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK(relaxUnitFun = true)
    lateinit var preferenceManager: PreferenceManager
    private lateinit var testObject: SettingsViewModel
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        every { preferenceManager.userPreferencesFlow } returns flowOf(UserPreferences())
        testObject = SettingsViewModel(dispatcherProvider, preferenceManager)
    }

    @Test
    fun `Verify user preferences is called when method is called`() = runTest {
        assertEquals(UserPreferences(), testObject.userPreferencesFlow.first())
    }

    @Test
    fun `Sets hide unavailable with value then calls preferences`() = runTest {
        val argument = slot<Boolean>()
        testObject.setHideUnavailable(true)
        coVerify { preferenceManager.hideUnavailable(capture(argument)) }
        assertTrue(argument.captured)
    }

    @Test
    fun `Sets refresh rate with value then calls preferences`() = runTest {
        val argument = slot<Int>()
        testObject.setAutoRefreshRate(20)
        coVerify { preferenceManager.autoRefreshRate(capture(argument)) }
        assertEquals(20, argument.captured)
    }

    @Test
    fun `Sets dark mode with value then calls preferences`() = runTest {
        val argument = slot<String>()
        testObject.setDarkMode("test")
        coVerify { preferenceManager.setDarkTheme(capture(argument)) }
        assertEquals("test", argument.captured)
    }

    @Test
    fun `Sets search history with value then calls preferences`() = runTest {
        val argument = slot<Boolean>()
        testObject.setSearchHistory(true)
        coVerify { preferenceManager.searchHistory(capture(argument)) }
        assertTrue(argument.captured)
    }
}