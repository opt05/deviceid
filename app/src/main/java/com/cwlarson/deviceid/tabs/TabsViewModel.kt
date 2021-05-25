package com.cwlarson.deviceid.tabs

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwlarson.deviceid.data.DeviceRepository
import com.cwlarson.deviceid.data.HardwareRepository
import com.cwlarson.deviceid.data.NetworkRepository
import com.cwlarson.deviceid.data.SoftwareRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    @ExperimentalCoroutinesApi
    val refreshDisabled =
        preferenceManager.observerAutoRefreshRate().distinctUntilChanged().mapLatest { it > 0 }
            .flowOn(Dispatchers.IO)
    private var job: Job? = null
    @ExperimentalCoroutinesApi
    private val tabData = when (savedStateHandle.get<ItemType>("tab")) {
        ItemType.DEVICE -> DeviceRepository(context, preferenceManager.hideUnavailable)
        ItemType.NETWORK -> NetworkRepository(context, preferenceManager.hideUnavailable)
        ItemType.SOFTWARE -> SoftwareRepository(context, preferenceManager.hideUnavailable)
        ItemType.HARDWARE -> HardwareRepository(context, preferenceManager.hideUnavailable)
        else -> throw IllegalArgumentException("Item type is undefined")
    }.apply {
        job = viewModelScope.launch(Dispatchers.IO) {
            preferenceManager.observeHideUnavailable().distinctUntilChanged()
                .collectLatest { filterUnavailable = it }
        }
    }
    @ExperimentalCoroutinesApi
    val status = tabData.status
    @ExperimentalCoroutinesApi
    val allItems = tabData.subscribe(context)

    @ExperimentalCoroutinesApi
    fun refresh(noStatus: Boolean = false) {
        tabData.refresh(noStatus)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}