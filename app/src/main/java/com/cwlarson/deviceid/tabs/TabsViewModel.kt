package com.cwlarson.deviceid.tabs

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwlarson.deviceid.data.DeviceRepository
import com.cwlarson.deviceid.data.HardwareRepository
import com.cwlarson.deviceid.data.NetworkRepository
import com.cwlarson.deviceid.data.Software
import com.cwlarson.deviceid.settings.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class TabsViewModel @ViewModelInject constructor(
    @ApplicationContext context: Context,
    preferenceManager: PreferenceManager,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val refreshDisabled =
        preferenceManager.observerAutoRefreshRate().distinctUntilChanged().mapLatest { it > 0 }
            .flowOn(Dispatchers.IO)
    private val tabData = when (savedStateHandle.get<ItemType>("tab")) {
        ItemType.DEVICE -> DeviceRepository(context, preferenceManager.hideUnavailable)
        ItemType.NETWORK -> NetworkRepository(context, preferenceManager.hideUnavailable)
        ItemType.SOFTWARE -> Software(context, preferenceManager.hideUnavailable)
        ItemType.HARDWARE -> HardwareRepository(context, preferenceManager.hideUnavailable)
        else -> throw IllegalArgumentException("Item type is undefined")
    }.apply {
        viewModelScope.launch(Dispatchers.IO) {
            preferenceManager.observeHideUnavailable().distinctUntilChanged()
                .collectLatest { filterUnavailable = it }
        }
    }
    val status = tabData.status
    val allItems = tabData.subscribe(context)

    fun refresh(noStatus: Boolean = false) {
        tabData.refresh(noStatus)
    }
}