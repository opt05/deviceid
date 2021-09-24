package com.cwlarson.deviceid.util

import android.Manifest
import android.content.Context
import android.os.Parcelable
import androidx.core.content.PermissionChecker
import kotlinx.parcelize.Parcelize

sealed class AppPermission(val permissionName: String) : Parcelable {
    @Parcelize
    object ReadPhoneState : AppPermission(Manifest.permission.READ_PHONE_STATE)

    @Parcelize
    object AccessFineLocation : AppPermission(Manifest.permission.ACCESS_FINE_LOCATION)
}

/**
 * Used to retrieve all app permission statuses used in the app in non-compose functions
 */
fun Context.isGranted(vararg permission: AppPermission) = permission.map { p ->
    PermissionChecker.checkSelfPermission(
        this,
        p.permissionName
    ) == PermissionChecker.PERMISSION_GRANTED
}.all { it }