@file:JvmName("PermissionsUtils")

package com.cwlarson.deviceid.util

import android.Manifest
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment

sealed class AppPermission(val permissionName: String) {
    object ReadPhoneState : AppPermission(Manifest.permission.READ_PHONE_STATE)
    object AccessFineLocation : AppPermission(Manifest.permission.ACCESS_FINE_LOCATION)
}

/**
 * Used to store and retrieve all app permissions used in the app. These utils add ktx functionality
 * to access permission status, requesting permissions and if a rational is needed.
 */

fun Context.isGranted(vararg permission: AppPermission) =
    permission.map { p ->
        (PermissionChecker.checkSelfPermission(
            this,
            p.permissionName
        ) == PermissionChecker.PERMISSION_GRANTED)
    }.all { it }

fun Fragment.shouldShowRationale(permission: AppPermission) =
    shouldShowRequestPermissionRationale(permission.permissionName)

fun ActivityResultLauncher<String>.requestPermission(permission: AppPermission) {
    launch(permission.permissionName)
}

fun Fragment.registerRequestPermission(isGranted: (Boolean) -> Unit) =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted(it) }

fun Context.loadPermissionLabel(permission: AppPermission) =
    packageManager.getPermissionInfo(permission.permissionName, 0).loadLabel(packageManager)