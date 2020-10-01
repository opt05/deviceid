package com.cwlarson.deviceid.tabsdetail

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cwlarson.deviceid.data.*
import com.cwlarson.deviceid.tabs.ItemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TabsDetailViewModel @ViewModelInject constructor(
    @ApplicationContext context: Context,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val tabData: TabData = when (savedStateHandle.get<ItemType>("type")) {
        ItemType.DEVICE -> DeviceRepository(context)
        ItemType.NETWORK -> NetworkRepository(context)
        ItemType.SOFTWARE -> Software(context)
        ItemType.HARDWARE -> HardwareRepository(context)
        else -> throw IllegalArgumentException("Item type is undefined")
    }

    @ExperimentalCoroutinesApi
    val detailItem = tabData.subscribe(
        savedStateHandle.get<Int>("title"), savedStateHandle.get<Array<String>>("titleFormatArgs")
    )
}