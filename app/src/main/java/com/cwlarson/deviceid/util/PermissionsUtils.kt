package com.cwlarson.deviceid.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import kotlinx.parcelize.Parcelize

sealed class AppPermission(val permissionName: String) : Parcelable {
    @Parcelize
    data object ReadPhoneState : AppPermission(Manifest.permission.READ_PHONE_STATE)

    @RequiresApi(Build.VERSION_CODES.O)
    @Parcelize
    data object ReadPhoneNumbers : AppPermission(Manifest.permission.READ_PHONE_NUMBERS)

    @Parcelize
    data object AccessFineLocation : AppPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.S)
    @Parcelize
    data object AccessBluetoothConnect : AppPermission(Manifest.permission.BLUETOOTH_CONNECT)
}

/**
 * Used to retrieve all app permission statuses used in the app in non-compose functions
 */
fun Context.isGranted(vararg permission: AppPermission) = permission.map { p ->
    PermissionChecker.checkSelfPermission(
        this, p.permissionName
    ) == PermissionChecker.PERMISSION_GRANTED
}.all { it }