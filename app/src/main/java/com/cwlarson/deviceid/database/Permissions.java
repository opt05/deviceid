package com.cwlarson.deviceid.database;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.cwlarson.deviceid.databinding.UnavailablePermission;

class Permissions {
    private final Context appContext;

    Permissions(Context context) {
        this.appContext = context;
    }

    Boolean hasPermission(UnavailablePermission permission){
        switch (permission){
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                return ActivityCompat.checkSelfPermission(appContext, Manifest.permission
                    .READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            default:
                return false;
        }
    }
}
