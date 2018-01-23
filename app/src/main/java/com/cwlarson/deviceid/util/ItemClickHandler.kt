package com.cwlarson.deviceid.util

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.cwlarson.deviceid.dialog.ItemClickDialog

class ItemClickHandler(var activity: Activity, var item: Item) {

    fun onClick(): Boolean {
        if(item.unavailableitem!=null) {
            if(item.unavailableitem!!.unavailablepermissioncode != null) {
                //Needs permission granted first
                getPermissionClickAdapter(item.unavailableitem!!.unavailablepermissioncode,item.title)
            } else if (item.unavailableitem!!.unavailabletype != null) {
                // Unavailable for another reason
                Snackbar.make(activity.findViewById<View>(android.R.id.content), activity.resources
                        .getString(R.string.snackbar_not_found_adapter, item.title), Snackbar.LENGTH_LONG).show()
            }
        } else {
            //We are fine to launch dialog for details of item
            if(activity is AppCompatActivity) {
                ItemClickDialog.newInstance(item.title, item.itemtype).show(
                        (activity as AppCompatActivity).supportFragmentManager, "itemClickDialog")
            }

        }
        return true
    }

    fun onLongClick(): Boolean {
        if (item.unavailableitem==null) {
            copyToClipboard()
            return true
        }
        return false
    }

    fun onShareClick(): Boolean {
        val shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setChooserTitle(R.string.send_to)
                .setText(item.subtitle)
                .intent
        if (shareIntent.resolveActivity(activity.packageManager) != null)
            activity.startActivity(shareIntent)
        return true
    }

    //Copy to clipboard
    private fun copyToClipboard() {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(item.title, item.subtitle)
        clipboard.primaryClip = clip
        //Prevents multiple times toast issue with the button
        Toast.makeText(activity, activity.resources.getString(R.string.copy_to_clipboard, item.title),
                Toast.LENGTH_SHORT).show()
    }

    // Request permission for IMEI/MEID for Android M+
    private fun getPermissionClickAdapter(MY_PERMISSION: UnavailablePermission?, itemTitle:
    String) {
        val permission: String
        when (MY_PERMISSION) {
            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> permission = Manifest.permission.READ_PHONE_STATE
            else -> return
        }

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager
                .PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                if (activity.findViewById<View>(android.R.id.content) != null) {
                    Snackbar.make(activity.findViewById<View>(android.R.id.content),
                            activity.resources.getString(R.string.phone_permission_snackbar, itemTitle),
                            Snackbar.LENGTH_INDEFINITE).setAction(R.string.phone_permission_snackbar_button,
                            {
                                ActivityCompat.requestPermissions(activity, arrayOf
                                (permission), MY_PERMISSION.value)
                            }).show()
                }
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), MY_PERMISSION.value)
            }
        }
    }

}
