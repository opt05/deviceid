package com.cwlarson.deviceid.settings

import com.cwlarson.deviceid.testutils.CoroutineTestRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SettingsViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var preferenceManager: PreferenceManager
    private lateinit var testObject: SettingsViewModel

    @Before
    fun setup() {
        whenever(preferenceManager.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))
        testObject = SettingsViewModel(preferenceManager)
    }

    @Test
    fun `Verify user preferences is called when method is called`() = runBlockingTest {
        assertEquals(UserPreferences(), testObject.userPreferencesFlow.first())
    }

    @Test
    fun `Sets hide unavailable with value then calls preferences`() = runBlockingTest {
        val captor = argumentCaptor<Boolean>()
        testObject.setHideUnavailable(true)
        verify(preferenceManager).hideUnavailable(captor.capture())
        assertTrue(captor.lastValue)
    }

    @Test
    fun `Sets refresh rate with value then calls preferences`() = runBlockingTest {
        val captor = argumentCaptor<Int>()
        testObject.setAutoRefreshRate(20)
        verify(preferenceManager).autoRefreshRate(captor.capture())
        assertEquals(20, captor.lastValue)
    }

    @Test
    fun `Sets dark mode with value then calls preferences`() = runBlockingTest {
        val captor = argumentCaptor<String>()
        testObject.setDarkMode("test")
        verify(preferenceManager).setDarkTheme(captor.capture())
        assertEquals("test", captor.lastValue)
    }

    @Test
    fun `Sets search history with value then calls preferences`() = runBlockingTest {
        val captor = argumentCaptor<Boolean>()
        testObject.setSearchHistory(true)
        verify(preferenceManager).searchHistory(captor.capture())
        assertTrue(captor.lastValue)
    }
}