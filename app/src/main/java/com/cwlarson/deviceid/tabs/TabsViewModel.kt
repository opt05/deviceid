package com.cwlarson.deviceid.tabs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cwlarson.deviceid.data.DeviceRepository
import com.cwlarson.deviceid.data.HardwareRepository
import com.cwlarson.deviceid.data.NetworkRepository
import com.cwlarson.deviceid.data.SoftwareRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@HiltViewModel
class TabsViewModel @Inject constructor(
    deviceRepository: Lazy<DeviceRepository>,
    networkRepository: Lazy<NetworkRepository>,
    softwareRepository: Lazy<SoftwareRepository>,
    hardwareRepository: Lazy<HardwareRepository>,
    private val preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val refreshDisabled =
        preferenceManager.autoRefreshRate.distinctUntilChanged().mapLatest { it > 0 }
            .flowOn(Dispatchers.IO)

    val allItems = when (val t = savedStateHandle.get<ItemType>("tab")) {
        ItemType.DEVICE -> deviceRepository.get()
        ItemType.NETWORK -> networkRepository.get()
        ItemType.SOFTWARE -> softwareRepository.get()
        ItemType.HARDWARE -> hardwareRepository.get()
        else -> throw IllegalArgumentException("Item type is undefined ${t.toString()}")
    }.list()

    fun forceRefresh() = preferenceManager.forceRefresh()
}