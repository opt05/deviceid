package com.cwlarson.deviceid.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.tabs.ItemSubtitle
import com.cwlarson.deviceid.tabs.ItemType
import com.cwlarson.deviceid.util.AppPermission
import com.cwlarson.deviceid.util.DispatcherProvider
import com.cwlarson.deviceid.util.isGranted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.util.*
import javax.inject.Inject

open class DeviceRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val context: Context,
    preferenceManager: PreferenceManager
) : TabData(dispatcherProvider, context, preferenceManager) {
    private val telephonyManager by lazy { context.getSystemService<TelephonyManager>() }

    override fun items() = flowOf(listOf(imei(), deviceModel(), serial(), androidID(), gsfid()))
        .flowOn(dispatcherProvider.IO)

    // Request permission for IMEI/MEID for Android M+
    @SuppressLint("HardwareIds", "MissingPermission")
    private fun imei() = Item(
        title = R.string.device_title_imei, itemType = ItemType.DEVICE,
        subtitle = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                if (context.isGranted(AppPermission.ReadPhoneState))
                    telephonyManager?.let { @Suppress("DEPRECATION") ItemSubtitle.Text(it.deviceId) }
                        ?: ItemSubtitle.Error
                else ItemSubtitle.Permission(AppPermission.ReadPhoneState)
            } catch (e: Throwable) {
                Timber.w(e)
                ItemSubtitle.Error
            }
        } else {
            ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.O)
        }
    )

    private fun deviceModel() = Item(
        title = R.string.device_title_model, itemType = ItemType.DEVICE,
        subtitle = try {
            val manufacturer = if (Build.MANUFACTURER.isNullOrEmpty()) "" else Build.MANUFACTURER
            val product = if (Build.PRODUCT.isNullOrEmpty()) "" else Build.PRODUCT
            val model = if (Build.MODEL.isNullOrEmpty()) "" else Build.MODEL
            ItemSubtitle.Text(buildString {
                if (model.startsWith(manufacturer) && model.isNotBlank()) append(model)
                else if (manufacturer.isNotBlank() && model.isNotBlank()) append("$manufacturer $model")
                if (product.isNotBlank()) append(" ($product)")
            }.trim().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            })
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    @SuppressLint("HardwareIds")
    private fun serial() = Item(
        title = R.string.device_title_serial, itemType = ItemType.DEVICE,
        subtitle = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION") ItemSubtitle.Text(Build.SERIAL)
        } else {
            ItemSubtitle.NoLongerPossible(Build.VERSION_CODES.O)
        }
    )

    @SuppressLint("HardwareIds")
    private fun androidID() = Item(
        title = R.string.device_title_android_id, itemType = ItemType.DEVICE,
        subtitle = try {
            ItemSubtitle.Text(
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            )
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )

    private fun gsfid() = Item(
        title = R.string.device_title_gsfid, itemType = ItemType.DEVICE,
        subtitle = try {
            context.contentResolver.query(
                "content://com.google.android.gsf.gservices".toUri(),
                null,
                null,
                arrayOf("android_id"),
                null
            )?.use { c ->
                if (!c.moveToFirst() || c.columnCount < 2) ItemSubtitle.Error
                else ItemSubtitle.Text(c.getString(1).toULong().toString(16))
            } ?: ItemSubtitle.Error
        } catch (e: Throwable) {
            Timber.w(e)
            ItemSubtitle.Error
        }
    )
}
