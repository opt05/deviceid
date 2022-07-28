package com.cwlarson.deviceid.data

import android.app.Application
import app.cash.turbine.test
import com.cwlarson.deviceid.settings.Filters
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TabDataTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var context: Application
    private lateinit var preferencesManager: PreferenceManager

    private inner class TestTabData(itemsList: List<Item>) :
        TabData(dispatcherProvider, context, preferencesManager) {
        private val internalFlow = MutableStateFlow(itemsList)

        override fun items(): Flow<List<Item>> = internalFlow

        suspend fun updateList(itemsList: List<Item>) {
            internalFlow.emit(itemsList)
        }
    }

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
        context = mockk()
        preferencesManager = mockk()
    }

    @Test
    fun `Verify list returns success when swipeRefreshDisabled and hideUnavailable is false`() =
        runTest {
            val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
            every { preferencesManager.getFilters() } returns
                flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = false))
            TestTabData(itemsList).list().test {
                assertEquals(TabDataStatus.Loading, awaitItem())
                assertEquals(TabDataStatus.Success(itemsList), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `Verify list returns success when swipeRefreshDisabled is true and hideUnavailable is false`() =
        runTest {
            val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
            every { preferencesManager.getFilters() } returns
                flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
            TestTabData(itemsList).list().test {
                assertEquals(TabDataStatus.Loading, awaitItem())
                assertEquals(TabDataStatus.Success(itemsList), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `Verify list returns success when swipeRefreshDisabled and hideUnavailable is true`() =
        runTest {
            val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
            every { preferencesManager.getFilters() } returns
                flowOf(Filters(hideUnavailable = true, swipeRefreshDisabled = true))
            TestTabData(itemsList).list().test {
                assertEquals(TabDataStatus.Loading, awaitItem())
                assertEquals(TabDataStatus.Success(emptyList()), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `Verify list returns success statuses in order when having multiple`() = runTest {
        every { context.getString(any()) } returns "a" andThen "b"
        val itemsList = listOf(
            Item(0, ItemType.SOFTWARE, ItemSubtitle.Text(null)),
            Item(0, ItemType.DEVICE, ItemSubtitle.Text(null))
        )
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
        TestTabData(itemsList).list().test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Success(itemsList.asReversed()), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify list updates filters when new provided`() = runTest {
        val filterFlow =
            MutableStateFlow(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
        every { preferencesManager.getFilters() } returns filterFlow
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        TestTabData(itemsList).list().test {
            filterFlow.emit(filterFlow.value.copy(hideUnavailable = true))
            delay(500)
            assertEquals(TabDataStatus.Success(emptyList()), expectMostRecentItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify list updates list when new provided`() = runTest {
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
        val itemsList1 = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        val itemsList2 = listOf(Item(0, ItemType.SOFTWARE, ItemSubtitle.Text(null)))
        val data = TestTabData(itemsList1)
        data.list().test {
            data.updateList(itemsList2)
            delay(500)
            assertEquals(TabDataStatus.Success(itemsList2), expectMostRecentItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify list returns error when exception is thrown`() = runTest {
        every { context.getString(any()) } throws NullPointerException()
        val itemsList = listOf(
            Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)),
            Item(0, ItemType.DEVICE, ItemSubtitle.Text(null))
        )
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
        TestTabData(itemsList).list().test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Error, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify list returns new status when filter updates`() = runTest {
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        val filters = MutableStateFlow(
            Filters(hideUnavailable = false, swipeRefreshDisabled = true)
        )
        every { preferencesManager.getFilters() } returns filters
        TestTabData(itemsList).list().test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Success(itemsList), awaitItem())
            filters.emit(filters.value.copy(hideUnavailable = true))
            assertEquals(TabDataStatus.Success(emptyList()), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify details returns success when available`() = runTest {
        val itemsList = listOf(
            Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)),
            Item(1, ItemType.SOFTWARE, ItemSubtitle.Text("lorem"), listOf("ipsum"))
        )
        TestTabData(itemsList).details(itemsList[1]).test {
            assertEquals(TabDetailStatus.Loading, awaitItem())
            assertEquals(TabDetailStatus.Success(itemsList[1]), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify details updates list when new provided`() = runTest {
        val itemsList1 = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        val itemsList2 = listOf(Item(0, ItemType.SOFTWARE, ItemSubtitle.Text(null)))
        val data = TestTabData(itemsList1)
        data.details(itemsList1[0]).test {
            data.updateList(itemsList2)
            delay(500)
            assertEquals(TabDetailStatus.Success(itemsList2[0]), expectMostRecentItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify details returns error when exception is thrown`() = runTest {
        val item: Item = mockk { every { title } throws NullPointerException() }
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
        TestTabData(listOf(item)).details(item).test {
            assertEquals(TabDetailStatus.Loading, awaitItem())
            assertEquals(TabDetailStatus.Error, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search returns success when hideUnavailable is false`() = runTest {
        every { context.getString(any()) } returns "a"
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = false))
        TestTabData(itemsList).search(MutableStateFlow("a")).test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Success(itemsList), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search returns success when hideUnavailable is true`() = runTest {
        every { context.getString(any()) } returns "a"
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = true, swipeRefreshDisabled = false))
        TestTabData(itemsList).search(MutableStateFlow("a")).test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Success(emptyList()), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search returns success statuses in order when having multiple`() = runTest {
        every { context.getString(0) } returns "a"
        every { context.getString(1) } returns "ab"
        val itemsList = listOf(
            Item(1, ItemType.SOFTWARE, ItemSubtitle.Text(null)),
            Item(0, ItemType.DEVICE, ItemSubtitle.Text(null))
        )
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = false))
        TestTabData(itemsList).search(MutableStateFlow("a")).test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Success(itemsList.asReversed()), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search returns success empty list when search text is blank`() = runTest {
        every { context.getString(any()) } returns "a"
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = false))
        TestTabData(itemsList).search(MutableStateFlow("")).test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Success(emptyList()), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search updates filters when new provided`() = runTest {
        every { context.getString(any()) } returns "a"
        val filterFlow =
            MutableStateFlow(Filters(hideUnavailable = false, swipeRefreshDisabled = true))
        every { preferencesManager.getFilters() } returns filterFlow
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        TestTabData(itemsList).search(MutableStateFlow("a")).test {
            filterFlow.emit(filterFlow.value.copy(hideUnavailable = true))
            delay(500)
            assertEquals(TabDataStatus.Success(emptyList()), expectMostRecentItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search updates list when new provided`() = runTest {
        every { context.getString(any()) } returns "a"
        val itemsList1 = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        val itemsList2 = listOf(Item(0, ItemType.SOFTWARE, ItemSubtitle.Text(null)))
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = false))
        val data = TestTabData(itemsList1)
        data.search(MutableStateFlow("a")).test {
            data.updateList(itemsList2)
            delay(500)
            assertEquals(TabDataStatus.Success(itemsList2), expectMostRecentItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `Verify search returns error when exception is thrown`() = runTest {
        every { context.getString(any()) } throws NullPointerException()
        val itemsList = listOf(Item(0, ItemType.DEVICE, ItemSubtitle.Text(null)))
        every { preferencesManager.getFilters() } returns
            flowOf(Filters(hideUnavailable = false, swipeRefreshDisabled = false))
        TestTabData(itemsList).search(MutableStateFlow("a")).test {
            assertEquals(TabDataStatus.Loading, awaitItem())
            assertEquals(TabDataStatus.Error, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}