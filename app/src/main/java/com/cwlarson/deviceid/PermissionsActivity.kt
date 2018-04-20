package com.cwlarson.deviceid

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate

import com.cwlarson.deviceid.database.AppDatabase
import com.cwlarson.deviceid.database.DatabaseInitializer
import com.cwlarson.deviceid.databinding.UnavailablePermission

abstract class PermissionsActivity : AppCompatActivity() {

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode  The request code passed in [.requestPermissions].
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see .requestPermissions
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE.value) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // GRANTED: Force new data updates
                DatabaseInitializer.populateAsync(this, AppDatabase.getDatabase(this))
            } else {
                // DENIED: We do nothing (it is handled by the ViewAdapter)
            }
        }
    }

    companion object {
        init {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        }
    }
}
