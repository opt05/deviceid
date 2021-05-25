package com.cwlarson.deviceid.tabsdetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.tabs.ItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
class TabsDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val tabData: TabData = when (savedStateHandle.get<ItemType>("type")) {
        ItemType.DEVICE -> DeviceRepository(context)
        ItemType.NETWORK -> NetworkRepository(context)
        ItemType.SOFTWARE -> SoftwareRepository(context)
        ItemType.HARDWARE -> HardwareRepository(context)
        else -> throw IllegalArgumentException("Item type is undefined")
    }

    @ExperimentalCoroutinesApi
    val detailItem = tabData.subscribe(
        savedStateHandle.get<Int>("title"), savedStateHandle.get<Array<String>>("titleFormatArgs")
    )
}