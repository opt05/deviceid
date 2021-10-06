package com.cwlarson.deviceid.tabsdetail

import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TabsDetailViewModelTest {
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
    private lateinit var testObject: TabsDetailViewModel

    @Test
    fun `Verify item when called with item type device then returns device repo`() =
        runBlockingTest {
            val item = Item(itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            whenever(deviceRepository.details(any())).thenReturn(flowOf(result))
            testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with item type network then returns network repo`() =
        runBlockingTest {
            val item = Item(itemType = ItemType.NETWORK, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            whenever(networkRepository.details(any())).thenReturn(flowOf(result))
            testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with item type software then returns software repo`() =
        runBlockingTest {
            val item = Item(itemType = ItemType.SOFTWARE, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            whenever(softwareRepository.details(any())).thenReturn(flowOf(result))
            testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with item type hardware then returns hardware repo`() =
        runBlockingTest {
            val item = Item(itemType = ItemType.HARDWARE, subtitle = ItemSubtitle.Error)
            val result = TabDetailStatus.Success(item)
            whenever(hardwareRepository.details(any())).thenReturn(flowOf(result))
            testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(item)
            assertEquals(result, testObject.item.first())
        }

    @Test
    fun `Verify item when called with no item type then returns error`() = runBlockingTest {
        testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository })
        assertEquals(TabDetailStatus.Error, testObject.item.first())
    }

    @Test
    fun `Verify item when called with null item type then returns error`() = runBlockingTest {
            testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
                { softwareRepository }, { hardwareRepository })
            testObject.updateCurrentItem(null)
            assertEquals(TabDetailStatus.Error, testObject.item.first())
        }

    @Test
    fun `Sets current item when called and calls details in repository`() = runBlockingTest {
        val item = Item(itemType = ItemType.DEVICE, subtitle = ItemSubtitle.Error)
        val captor = argumentCaptor<Item>()
        whenever(deviceRepository.details(any())).thenReturn(flowOf(TabDetailStatus.Success(item)))
        testObject = TabsDetailViewModel({ deviceRepository }, { networkRepository },
            { softwareRepository }, { hardwareRepository })
        testObject.updateCurrentItem(item)
        testObject.item.first()
        verify(deviceRepository).details(captor.capture())
        assertEquals(item, captor.lastValue)
    }
}