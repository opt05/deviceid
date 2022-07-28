package com.cwlarson.deviceid.tabsdetail

import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import com.cwlarson.deviceid.util.DispatcherProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TabsDetailViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var deviceRepository: DeviceRepository

    @MockK
    lateinit var networkRepository: NetworkRepository

    @MockK
    lateinit var softwareRepository: SoftwareRepository

    @MockK
    lateinit var hardwareRepository: HardwareRepository
    private lateinit var testObject: TabsDetailViewModel
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        dispatcherProvider = DispatcherProvider.provideDispatcher(coroutineRule.dispatcher)
    }

    @Test
    fun `Verify item when called with item type device then returns device repo`() =
        runTest {
            val item = Item(itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            every { deviceRepository.details(any()) } returns flowOf(result)
            testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
                { networkRepository }, { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with item type network then returns network repo`() =
        runTest {
            val item = Item(itemType = ItemType.NETWORK, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            every { networkRepository.details(any()) } returns flowOf(result)
            testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
                { networkRepository }, { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with item type software then returns software repo`() =
        runTest {
            val item = Item(itemType = ItemType.SOFTWARE, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            every { softwareRepository.details(any()) } returns flowOf(result)
            testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
                { networkRepository }, { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with item type hardware then returns hardware repo`() =
        runTest {
            val item = Item(itemType = ItemType.HARDWARE, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            every { hardwareRepository.details(any()) } returns flowOf(result)
            testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
                { networkRepository }, { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with no item type then returns error`() = runTest {
        testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
            { networkRepository }, { softwareRepository }, { hardwareRepository })
        assertEquals(TabDetailStatus.Error, testObject.item.first())
    }

    @Test
    fun `Verify item when called with null item type then returns error`() = runTest {
            testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
                { networkRepository }, { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(null)
            assertEquals(TabDetailStatus.Error, testObject.item.first())
        }

    @Test
    fun `Sets current item when called and calls details in repository`() = runTest {
        val item = Item(itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Error)
        val argument = slot<Item>()
        every { deviceRepository.details(any()) } returns flowOf(TabDetailStatus.Success(item))
        testObject = TabsDetailViewModel(dispatcherProvider, { deviceRepository },
            { networkRepository }, { softwareRepository }, { hardwareRepository })
        testObject.updateCurrentItem(item)
        testObject.item.first()
        verify { deviceRepository.details(capture(argument)) }
        assertEquals(item, argument.captured)
    }
}