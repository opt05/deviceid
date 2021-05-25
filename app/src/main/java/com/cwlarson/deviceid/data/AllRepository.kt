package com.cwlarson.deviceid.data

import android.content.Context
import com.cwlarson.deviceid.tabs.Item

class AllRepository(private val context: Context, filterUnavailable: Boolean) : TabData(filterUnavailable) {

    override suspend fun list(): List<Item> = DeviceRepository(context, filterUnavailable).list()
            .union(NetworkRepository(context, filterUnavailable).list())
            .union(SoftwareRepository(context, filterUnavailable).list())
            .union(HardwareRepository(context, filterUnavailable).list()).toList()
}