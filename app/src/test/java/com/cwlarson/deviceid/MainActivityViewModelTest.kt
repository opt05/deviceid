package com.cwlarson.deviceid

import android.content.Intent
import app.cash.turbine.test
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class MainActivityViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var preferenceManager: PreferenceManager
    private lateinit var testObject: MainActivityViewModel

    @Before
    fun setup() {
        whenever(preferenceManager.searchHistory).thenReturn(flowOf(false))
        whenever(preferenceManager.getSearchHistoryItems(any())).thenReturn(flowOf(listOf("item1")))
        testObject = MainActivityViewModel(preferenceManager)
    }

    @Test
    fun `Verify isSearchHistory returns search history value`() = runBlocking {
        verify(preferenceManager).searchHistory
        testObject.isSearchHistory.test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Verify startTitleFade when is twoPane and has empty intent`() = runBlocking {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(true, mock())
            assertEquals(TitleVisibility(visible = false, noFade = true), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when is not twoPane and has search intent`() = runBlocking {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mock { on { action } doReturn Intent.ACTION_SEARCH })
            assertEquals(TitleVisibility(visible = false, noFade = true), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when is not twoPane and empty intent`() = runBlocking {
        testObject.titleVisibility.test {
            assertEquals(TitleVisibility(visible = true, noFade = false), awaitItem())
            testObject.startTitleFade(false, mock())
            expectNoEvents()
            delay(TimeUnit.SECONDS.toMillis(2))
            assertEquals(TitleVisibility(visible = false, noFade = false), awaitItem())
        }
    }

    @Test
    fun `Verify startTitleFade when multiple calls does not return more`() = runBlocking {
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
    fun `Verify saveSearchHistory saves data in preferences`() = runBlocking {
        testObject.saveSearchHistory("query")
        verify(preferenceManager).saveSearchHistoryItem(eq("query"))
    }

    @Test
    fun `Verify getSearchHistoryItems returns data from preferences`() = runBlocking {
        testObject.getSearchHistoryItems("item1").test {
            assertEquals(listOf("item1"), awaitItem())
            awaitComplete()
        }
    }
}