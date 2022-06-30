package com.cwlarson.deviceid.tabs

import androidx.lifecycle.SavedStateHandle
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.*

class TabsViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var deviceRepository: DeviceRepository

    @Mock
    lateinit var networkRepository: NetworkRepository

    @Mock
    lateinit var softwareRepository: SoftwareRepository

    @Mock
    lateinit var hardwareRepository: HardwareRepository

    @Mock
    lateinit var preferenceManager: PreferenceManager

    @Mock
    lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testObject: TabsViewModel
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
    }

    @Test
    fun `Verify refresh disabled is called when class is created`() = runTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        verify(preferenceManager).autoRefreshRate
    }

    @Test
    fun `Verify refresh disabled is true when set to above 0`() = runTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(1))
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertTrue(testObject.refreshDisabled.first())
    }

    @Test
    fun `Verify refresh disabled is false when set to 0`() = runTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertFalse(testObject.refreshDisabled.first())
    }

    @Test
    fun `Verify refresh return latest value when called`() = runTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).doReturn(flow {
            emit(0)
            emit(1)
        })
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertTrue(testObject.refreshDisabled.last())
    }

    @Test
    fun `Verify all items when called with item type device then returns device repo`() =
        runTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.DEVICE)
            whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(deviceRepository.list()).doReturn(flowOf(result))
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with item type network then returns network repo`() =
        runTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.NETWORK)
            whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(networkRepository.list()).doReturn(flowOf(result))
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with item type software then returns software repo`() =
        runTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.SOFTWARE)
            whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(softwareRepository.list()).doReturn(flowOf(result))
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with item type hardware then returns hardware repo`() =
        runTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.HARDWARE)
            whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(hardwareRepository.list()).doReturn(flowOf(result))
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with invalid item type then returns exception`() =
        runTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(null)
            whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
            val exception = assertThrows(IllegalArgumentException::class.java) {
                testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                    { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
                )
                runTest { testObject.allItems.first() }
            }
            assertEquals("Item type is undefined null", exception.message)
        }

    @Test
    fun `Verify force refresh is called when method is called`() = runTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).doReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).doReturn(flowOf(0))
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        testObject.forceRefresh()
        verify(preferenceManager, times(1)).forceRefresh()
    }
}