package com.cwlarson.deviceid.database

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.*

internal class Device(activity: Activity, db: AppDatabase) {
    private val tag = Device::class.java.simpleName
    private val context: Context = activity.applicationContext

    init {
        //Set Device Tiles
        val itemAdder = ItemAdder(context, db)
        itemAdder.addItems(imei)
        itemAdder.addItems(deviceModel)
        itemAdder.addItems(serial)
        itemAdder.addItems(androidID)
        itemAdder.addItems(gsfid)
    }

    // Request permission for IMEI/MEID for Android M+
    private val imei: Item
        @SuppressLint("HardwareIds", "MissingPermission")
        get() {
            val item = Item("IMEI / MEID", ItemType.DEVICE)
            try {
                if (Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        item.subtitle = @Suppress("DEPRECATION") telephonyManager.deviceId
                    } else {
                        val builder = StringBuilder()
                        val imei = telephonyManager.imei
                        val meid = telephonyManager.meid
                        if(!imei.isNullOrBlank()) builder.append(imei)
                        if(!imei.isNullOrBlank() && !meid.isNullOrBlank()) builder.append(" / ")
                        if(!meid.isNullOrBlank()) builder.append(meid)
                        item.subtitle = builder.toString()
                    }
                } else {
                    item.unavailableitem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                            context.resources.getString(R.string.phone_permission_denied),
                            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } catch (e: Exception) {
                Log.w(tag, "Null in getIMEI")
            }

            return item
        }

    private val deviceModel: Item
        get() {
            val item = Item("Model Number", ItemType.DEVICE)
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
                Log.w(tag, "Null in getDeviceModel")
            }

            item.subtitle = if (Character.isUpperCase(device[0])) device else Character.toUpperCase(device[0]) + device.substring(1)
            return item
        }

    private val serial: Item
        @SuppressLint("HardwareIds", "MissingPermission")
        get() {
            val item = Item("Serial", ItemType.DEVICE)
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION") Build.SERIAL
            } else {
                try {
                    if (Permissions(context).hasPermission(UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)) {
                        item.subtitle = Build.getSerial()
                    } else {
                        item.unavailableitem = UnavailableItem(UnavailableType.NEEDS_PERMISSION,
                                context.resources.getString(R.string.phone_permission_denied),
                                UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Null in serial")
                }
            }
            return item
        }

    private val androidID: Item
        @SuppressLint("HardwareIds")
        get() {
            val item = Item("Android/Hardware ID", ItemType.DEVICE)
            try {
                item.subtitle = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                Log.w(tag, "Null in androidID")
            }

            return item
        }

    private val gsfid: Item
        get() {
            val item = Item("Google Service Framework (GSF) ID", ItemType.DEVICE)
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
                            item.subtitle = result
                        }
                    } catch (e: Exception) {
                        if (c?.isClosed != true) c?.close()
                    }

                }
            } catch (e: Exception) {
                Log.w(tag, "Null in gsfid")
            }

            return item
        }
}