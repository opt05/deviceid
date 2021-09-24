package com.cwlarson.deviceid.data

import android.content.Context
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

open class AllRepository @Inject constructor(
    private val context: Context,
    private val preferenceManager: PreferenceManager
) : TabData(context, preferenceManager) {

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun items(): Flow<List<Item>> = combine(
        DeviceRepository(context, preferenceManager).items(),
        NetworkRepository(context, preferenceManager).items(),
        SoftwareRepository(context, preferenceManager).items(),
        HardwareRepository(context, preferenceManager).items()
    ) { list, list2, list3, list4 -> list + list2 + list3 + list4 }.flowOn(Dispatchers.IO)
}