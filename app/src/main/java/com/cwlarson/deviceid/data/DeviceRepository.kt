package com.cwlarson.deviceid.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.AppPermission
import com.cwlarson.deviceid.util.isGranted
import com.cwlarson.deviceid.util.telephonyManager
import timber.log.Timber
import java.util.*

class DeviceRepository(private val context: Context, filterUnavailable: Boolean = false)
    : TabData(filterUnavailable) {
    override suspend fun list(): List<Item> = listOf(imei(), deviceModel(), serial(), androidID(),
            gsfid())

    // Request permission for IMEI/MEID for Android M+
    @SuppressLint("HardwareIds", "MissingPermission")
    private fun imei(): Item = Item(R.string.device_title_imei, ItemType.DEVICE).apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                subtitle = if (context.isGranted(AppPermission.ReadPhoneState)) {
                    val telephonyManager = context.telephonyManager
                    @Suppress("DEPRECATION") ItemSubtitle.Text(telephonyManager.deviceId)
                } else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
            } catch (e: Throwable) {
                Timber.w(e)
            }
        } else {
            subtitle = ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.O)
        }
    }

    private fun deviceModel() = Item(R.string.device_title_model, ItemType.DEVICE).apply {
        var device = ""
        try {
            val manufacturer = if (Build.MANUFACTURER == null || Build.MANUFACTURER.isEmpty()) ""
            else Build.MANUFACTURER
            val product = if (Build.PRODUCT == null || Build.PRODUCT.isEmpty()) "" else Build
                    .PRODUCT
            val model = if (Build.MODEL == null || Build.MODEL.isEmpty()) "" else Build.MODEL
            device = (if (model.startsWith(manufacturer)) {
                "$model ($product)"
            } else {
                "$manufacturer $model ($product)"
            }).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } catch (e: Throwable) {
            Timber.w(e)
        }
        subtitle = ItemSubtitle.Text(device)
    }

    @SuppressLint("HardwareIds")
    private fun serial() = Item(R.string.device_title_serial, ItemType.DEVICE).apply {
        subtitle = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION") ItemSubtitle.Text(Build.SERIAL)
        } else {
            ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.O)
        }
    }

    @SuppressLint("HardwareIds")
    private fun androidID() = Item(R.string.device_title_android_id, ItemType.DEVICE).apply {
        try {
            subtitle = ItemSubtitle.Text(Settings.Secure.getString(context.contentResolver,
                    Settings.Secure.ANDROID_ID))
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }

    private fun gsfid() = Item(R.string.device_title_gsfid, ItemType.DEVICE).apply {
        try {
            val uri = Uri.parse("content://com.google.android.gsf.gservices")
            val idKey = "android_id"
            val params = arrayOf(idKey)
            val c = context.contentResolver.query(uri, null, null, params, null)
            if (c != null && (!c.moveToFirst() || c.columnCount < 2)) {
                if (!c.isClosed) c.close()
            } else {
                try {
                    c?.let {
                        val result = java.lang.Long.toHexString(java.lang.Long.parseLong(it.getString(1)))
                        if (!it.isClosed) it.close()
                        subtitle = ItemSubtitle.Text(result)
                    }
                } catch (e: Throwable) {
                    if (c?.isClosed != true) c?.close()
                }

            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
    }
}
