package com.cwlarson.deviceid.tabs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cwlarson.deviceid.data.DeviceRepository
import com.cwlarson.deviceid.data.HardwareRepository
import com.cwlarson.deviceid.data.NetworkRepository
import com.cwlarson.deviceid.data.SoftwareRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.util.DispatcherProvider
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@HiltViewModel
class TabsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    deviceRepository: Lazy<DeviceRepository>,
    networkRepository: Lazy<NetworkRepository>,
    softwareRepository: Lazy<SoftwareRepository>,
    hardwareRepository: Lazy<HardwareRepository>,
    private val preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val refreshDisabled =
        preferenceManager.autoRefreshRate.distinctUntilChanged().mapLatest { it > 0 }
            .flowOn(dispatcherProvider.IO)

    val allItems = when (val t = savedStateHandle.get<ItemType>("tab")) {
        ItemType.DEVICE -> deviceRepository.get()
        ItemType.NETWORK -> networkRepository.get()
        ItemType.SOFTWARE -> softwareRepository.get()
        ItemType.HARDWARE -> hardwareRepository.get()
        else -> throw IllegalArgumentException("Item type is undefined ${t.toString()}")
    }.list()

    fun forceRefresh() = preferenceManager.forceRefresh()
}