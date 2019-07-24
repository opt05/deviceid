package com.cwlarson.deviceid.database

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.WorkerThread
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.*
import com.cwlarson.deviceid.util.hasPermission
import com.cwlarson.deviceid.util.telephonyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

@WorkerThread
class Device(private val context: Context, db: AppDatabase, scope: CoroutineScope) {
    init {
        //Set Device Tiles
        scope.launch(Dispatchers.IO) {
            db.addItems(context, *imei(), deviceModel(), serial(), androidID(), gsfid())
        }
    }

    // Request permission for IMEI/MEID for Android M+
    @SuppressLint("HardwareIds", "MissingPermission")
    private fun imei(): Array<Item> = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            arrayOf(Item("IMEI / MEID", ItemType.DEVICE))
        } else {
            arrayOf(Item("IMEI", ItemType.DEVICE),Item("MEID", ItemType.DEVICE))
        }.apply {
            try {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    val telephonyManager = context.telephonyManager
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        get(0).subtitle = @Suppress("DEPRECATION") telephonyManager.deviceId
                    } else {
                        get(0).subtitle = telephonyManager.imei
                        get(1).subtitle = telephonyManager.meid
                    }
                } else
                    forEach { item ->
                        item.unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                                context.resources.getString(R.string.permission_item_subtitle,
                                        context.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                                .loadLabel(context.packageManager).toString()
                                                .toUpperCase(Locale.getDefault())),
                                UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                    }
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
    }

    private fun deviceModel() = Item("Model Number", ItemType.DEVICE).apply {
        var device = ""
        val manufacturer: String
        val product: String
        val model: String
        try {
            manufacturer = if (Build.MANUFACTURER == null || Build.MANUFACTURER.isEmpty()) "" else Build.MANUFACTURER
            product = if (Build.PRODUCT == null || Build.PRODUCT.isEmpty()) "" else Build.PRODUCT
            model = if (Build.MODEL == null || Build.MODEL.isEmpty()) "" else Build.MODEL
            device = if (model.startsWith(manufacturer)) {
                "$model ($product)"
            } else {
                "$manufacturer $model ($product)"
            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
        subtitle = if (Character.isUpperCase(device[0])) device else Character.toUpperCase(device[0]) + device.substring(1)
    }

    @SuppressLint("MissingPermission")
    private fun serial() = Item("Serial", ItemType.DEVICE).apply {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION") Build.SERIAL
        } else {
            try {
                if (context.hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    subtitle = Build.getSerial()
                } else {
                    unavailableItem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.permission_item_subtitle,
                                    context.packageManager.getPermissionInfo(Manifest.permission
                                            .READ_PHONE_STATE, 0).loadLabel(context.packageManager)
                                            .toString().toUpperCase(Locale.getDefault())),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Exception) {
                Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun androidID() = Item("Android/Hardware ID", ItemType.DEVICE).apply {
        try {
            subtitle = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }

    private fun gsfid() = Item("Google Service Framework (GSF) ID", ItemType.DEVICE).apply {
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
                        subtitle = result
                    }
                } catch (e: Exception) {
                    if (c?.isClosed != true) c?.close()
                }

            }
        } catch (e: Exception) {
            Timber.w("Null in ${object{}.javaClass.enclosingMethod?.name}")
        }
    }
}
