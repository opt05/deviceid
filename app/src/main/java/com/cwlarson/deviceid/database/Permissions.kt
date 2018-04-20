package com.cwlarson.deviceid.database

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

import com.cwlarson.deviceid.databinding.UnavailablePermission

internal class Permissions(private val appContext: Context) {

    fun hasPermission(permission: UnavailablePermission): Boolean {
        return when (permission) {
            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE ->
                ActivityCompat.checkSelfPermission(appContext, Manifest.permission
                    .READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        }
    }
}
