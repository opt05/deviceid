package com.cwlarson.deviceid.data

import android.content.Context
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AllRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val context: Context,
    private val preferenceManager: PreferenceManager
) : TabData(dispatcherProvider, context, preferenceManager) {

    override fun items(): Flow<List<Item>> = combine(
        DeviceRepository(dispatcherProvider, context, preferenceManager).items(),
        NetworkRepository(dispatcherProvider, context, preferenceManager).items(),
        SoftwareRepository(dispatcherProvider, context, preferenceManager).items(),
        HardwareRepository(dispatcherProvider, context, preferenceManager).items()
    ) { list, list2, list3, list4 -> list + list2 + list3 + list4 }.flowOn(dispatcherProvider.IO)
}