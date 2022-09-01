package com.cwlarson.deviceid.tabsdetail

import androidx.lifecycle.ViewModel
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TabsDetailViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    deviceRepository: Lazy<DeviceRepository>,
    networkRepository: Lazy<NetworkRepository>,
    softwareRepository: Lazy<SoftwareRepository>,
    hardwareRepository: Lazy<HardwareRepository>,
) : ViewModel() {
    private val currentItem = MutableStateFlow<Item?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val item : Flow<TabDetailStatus> = currentItem.flatMapLatest { item ->
        when (item?.itemType) {
            ItemType.DEVICE -> deviceRepository.get()
            ItemType.NETWORK -> networkRepository.get()
            ItemType.SOFTWARE -> softwareRepository.get()
            ItemType.HARDWARE -> hardwareRepository.get()
            else -> return@flatMapLatest flowOf(TabDetailStatus.Error)
        }.details(item).flowOn(dispatcherProvider.IO)
    }

    fun updateCurrentItem(item: Item?) { currentItem.value = item }
}