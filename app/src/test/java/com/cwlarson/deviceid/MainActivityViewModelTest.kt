package com.cwlarson.deviceid

import android.content.Intent
import app.cash.turbine.test
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class MainActivityViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var preferenceManager: PreferenceManager
    private lateinit var testObject: MainActivityViewModel
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        every { preferenceManager.darkTheme } returns flowOf(false)
        every { preferenceManager.searchHistory } returns flowOf(false)
        every { preferenceManager.getSearchHistoryItems(any()) } returns flowOf(listOf("item1"))
        testObject = MainActivityViewModel(dispatcherProvider, preferenceManager)
    }

    @Test
    fun `Verify isSearchHistory returns search history value`() = runTest {
        verify { preferenceManager.searchHistory }
        testObject.isSearchHistory.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Verify startTitleFade when is twoPane and has empty intent`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(true, mockk())
            assertEquals(TitleVisibility(visible = false, noFade = true), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when is not twoPane and has search intent`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mockk { every { action } returns Intent.ACTION_SEARCH })
            assertEquals(TitleVisibility(visible = false, noFade = true), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when is not twoPane and empty intent`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mockk(relaxed = true))
            expectNoEvents()
            delay(TimeUnit.SECONDS.toMillis(2))
            assertEquals(TitleVisibility(visible = false, noFade = false), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when multiple calls does not return more`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mockk(relaxed = true))
            testObject.startTitleFade(false, mockk(relaxed = true))
            expectNoEvents()
            delay(TimeUnit.SECONDS.toMillis(2))
            assertEquals(TitleVisibility(visible = false, noFade = false), awaitItem())
        }
    }

    @Test
    fun `Verify saveSearchHistory saves data in preferences`() = runTest {
        coJustRun { preferenceManager.saveSearchHistoryItem(any()) }
        testObject.saveSearchHistory("query")
        coVerify { preferenceManager.saveSearchHistoryItem("query") }
    }

    @Test
    fun `Verify getSearchHistoryItems returns data from preferences`() = runTest {
        testObject.getSearchHistoryItems("item1").test {
            assertEquals(listOf("item1"), awaitItem())
            awaitComplete()
        }
    }
}