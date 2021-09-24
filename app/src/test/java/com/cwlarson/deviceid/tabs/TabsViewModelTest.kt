package com.cwlarson.deviceid.tabs

import androidx.lifecycle.SavedStateHandle
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TabsViewModelTest {
    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify refresh disabled is called when class is created`() = runBlockingTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
        testObject = TabsViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        verify(preferenceManager).autoRefreshRate
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify refresh disabled is true when set to above 0`() = runBlocking {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(1))
        testObject = TabsViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertTrue(testObject.refreshDisabled.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify refresh disabled is false when set to 0`() = runBlocking {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
        testObject = TabsViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertFalse(testObject.refreshDisabled.first())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify refresh return latest value when called`() = runBlocking {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).thenReturn(flow {
            emit(0)
            emit(1)
        })
        testObject = TabsViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertTrue(testObject.refreshDisabled.last())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify all items when called with item type device then returns device repo`() =
        runBlockingTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.DEVICE)
            whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(deviceRepository.list()).thenReturn(flowOf(result))
            testObject = TabsViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify all items when called with item type network then returns network repo`() =
        runBlockingTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.NETWORK)
            whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(networkRepository.list()).thenReturn(flowOf(result))
            testObject = TabsViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify all items when called with item type software then returns software repo`() =
        runBlockingTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.SOFTWARE)
            whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(softwareRepository.list()).thenReturn(flowOf(result))
            testObject = TabsViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify all items when called with item type hardware then returns hardware repo`() =
        runBlockingTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.HARDWARE)
            whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
            val result = TabDataStatus.Success(emptyList())
            whenever(hardwareRepository.list()).thenReturn(flowOf(result))
            testObject = TabsViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify all items when called with invalid item type then returns exception`() =
        runBlockingTest {
            whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(null)
            whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
            val exception = assertThrows(IllegalArgumentException::class.java) {
                testObject = TabsViewModel({ deviceRepository }, { networkRepository },
                    { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
                )
                runBlocking { testObject.allItems.first() }
            }
            assertEquals("Item type is undefined null", exception.message)
        }

    @ExperimentalCoroutinesApi
    @Test
    fun `Verify force refresh is called when method is called`() = runBlockingTest {
        whenever(savedStateHandle.get<ItemType>(eq("tab"))).thenReturn(ItemType.DEVICE)
        whenever(preferenceManager.autoRefreshRate).thenReturn(flowOf(0))
        testObject = TabsViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        testObject.forceRefresh()
        verify(preferenceManager, times(1)).forceRefresh()
    }
}