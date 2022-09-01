package com.cwlarson.deviceid.tabs

import androidx.lifecycle.SavedStateHandle
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TabsViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK(relaxed = true)
    lateinit var deviceRepository: DeviceRepository

    @MockK(relaxed = true)
    lateinit var networkRepository: NetworkRepository

    @MockK(relaxed = true)
    lateinit var softwareRepository: SoftwareRepository

    @MockK(relaxed = true)
    lateinit var hardwareRepository: HardwareRepository

    @MockK
    lateinit var preferenceManager: PreferenceManager

    @MockK
    lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testObject: TabsViewModel
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
    }

    @Test
    fun `Verify refresh disabled is called when class is created`() = runTest {
        every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.DEVICE
        every { preferenceManager.autoRefreshRate } returns flowOf(0)
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        verify { preferenceManager.autoRefreshRate }
    }

    @Test
    fun `Verify refresh disabled is true when set to above 0`() = runTest {
        every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.DEVICE
        every { preferenceManager.autoRefreshRate } returns flowOf(1)
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertTrue(testObject.refreshDisabled.first())
    }

    @Test
    fun `Verify refresh disabled is false when set to 0`() = runTest {
        every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.DEVICE
        every { preferenceManager.autoRefreshRate } returns flowOf(0)
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertFalse(testObject.refreshDisabled.first())
    }

    @Test
    fun `Verify refresh return latest value when called`() = runTest {
        every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.DEVICE
        every { preferenceManager.autoRefreshRate } returns flow {
            emit(0)
            emit(1)
        }
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        assertTrue(testObject.refreshDisabled.last())
    }

    @Test
    fun `Verify all items when called with item type device then returns device repo`() =
        runTest {
            every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.DEVICE
            every { preferenceManager.autoRefreshRate } returns flowOf(0)
            val result = TabDataStatus.Success(emptyList())
            every { deviceRepository.list() } returns flowOf(result)
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with item type network then returns network repo`() =
        runTest {
            every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.NETWORK
            every { preferenceManager.autoRefreshRate } returns flowOf(0)
            val result = TabDataStatus.Success(emptyList())
            every { networkRepository.list() } returns flowOf(result)
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with item type software then returns software repo`() =
        runTest {
            every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.SOFTWARE
            every { preferenceManager.autoRefreshRate } returns flowOf(0)
            val result = TabDataStatus.Success(emptyList())
            every { softwareRepository.list() } returns flowOf(result)
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with item type hardware then returns hardware repo`() =
        runTest {
            every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.HARDWARE
            every { preferenceManager.autoRefreshRate } returns flowOf(0)
            val result = TabDataStatus.Success(emptyList())
            every { hardwareRepository.list() } returns flowOf(result)
            testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
            )
            assertEquals(result, testObject.allItems.first())
        }

    @Test
    fun `Verify all items when called with invalid item type then returns exception`() =
        runTest {
            every { savedStateHandle.get<ItemType>(eq("tab")) } returns null
            every { preferenceManager.autoRefreshRate } returns flowOf(0)
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
        justRun { preferenceManager.forceRefresh() }
        every { savedStateHandle.get<ItemType>(eq("tab")) } returns ItemType.DEVICE
        every { preferenceManager.autoRefreshRate } returns flowOf(0)
        testObject = TabsViewModel(dispatcherProvider, { deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository }, preferenceManager, savedStateHandle
        )
        testObject.forceRefresh()
        verify(exactly = 1) { preferenceManager.forceRefresh() }
    }
}