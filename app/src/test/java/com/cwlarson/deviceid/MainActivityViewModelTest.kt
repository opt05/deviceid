package com.cwlarson.deviceid

import android.content.Intent
import app.cash.turbine.test
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class MainActivityViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var preferenceManager: PreferenceManager
    private lateinit var testObject: MainActivityViewModel
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        whenever(preferenceManager.searchHistory).doReturn(flowOf(false))
        whenever(preferenceManager.getSearchHistoryItems(any())).doReturn(flowOf(listOf("item1")))
        testObject = MainActivityViewModel(dispatcherProvider, preferenceManager)
    }

    @Test
    fun `Verify isSearchHistory returns search history value`() = runTest {
        verify(preferenceManager).searchHistory
        testObject.isSearchHistory.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Verify startTitleFade when is twoPane and has empty intent`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(true, mock())
            assertEquals(TitleVisibility(visible = false, noFade = true), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when is not twoPane and has search intent`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mock { on { action } doReturn Intent.ACTION_SEARCH })
            assertEquals(TitleVisibility(visible = false, noFade = true), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when is not twoPane and empty intent`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mock())
            expectNoEvents()
            delay(TimeUnit.SECONDS.toMillis(2))
            assertEquals(TitleVisibility(visible = false, noFade = false), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when multiple calls does not return more`() = runTest {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mock())
            testObject.startTitleFade(false, mock())
            expectNoEvents()
            delay(TimeUnit.SECONDS.toMillis(2))
            assertEquals(TitleVisibility(visible = false, noFade = false), awaitItem())
        }
    }

    @Test
    fun `Verify saveSearchHistory saves data in preferences`() = runTest {
        testObject.saveSearchHistory("query")
        verify(preferenceManager).saveSearchHistoryItem(eq("query"))
    }

    @Test
    fun `Verify getSearchHistoryItems returns data from preferences`() = runTest {
        testObject.getSearchHistoryItems("item1").test {
            assertEquals(listOf("item1"), awaitItem())
            awaitComplete()
        }
    }
}