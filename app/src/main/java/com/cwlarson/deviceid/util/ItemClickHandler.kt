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
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.cwlarson.deviceid.dialog.ItemClickDialog

class ItemClickHandler(var activity: Activity?, var item: Item?) {

    fun onClick(): Boolean {
        item?.unavailableitem?.let {
            if(it.unavailablepermissioncode != null) {
                //Needs permission granted first
                getPermissionClickAdapter(it.unavailablepermissioncode,item?.title)
            } else if (it.unavailabletype != null) {
                // Unavailable for another reason
                val view = activity?.findViewById<View>(android.R.id.content)
                val message = activity?.resources?.getString(R.string.snackbar_not_found_adapter, item?.title)
                view?.let {
                    Snackbar.make(it, message ?: "Unknown error",
                            Snackbar.LENGTH_LONG).show()

                }
            }
        } ?: //We are fine to launch dialog for details of item
            if(activity is AppCompatActivity) {
                ItemClickDialog.newInstance(item?.title, item?.itemtype).show(
                        (activity as AppCompatActivity).supportFragmentManager, "itemClickDialog")
            }
        return true
    }

    fun onLongClick(): Boolean {
        item?.let {
            return if(it.unavailableitem==null){
                copyToClipboard()
                true
            } else
                false
        } ?: return false
    }

    fun onShareClick(): Boolean {
        activity?.let {
            val shareIntent = ShareCompat.IntentBuilder.from(it)
                    .setType("text/plain")
                    .setChooserTitle(R.string.send_to)
                    .setText(item?.subtitle)
                    .intent
            if (shareIntent.resolveActivity(it.packageManager) != null)
                it.startActivity(shareIntent)
            return true
        } ?: return false

    }

    //Copy to clipboard
    private fun copyToClipboard() {
        activity?.let {
            val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(item?.title, item?.subtitle)
            clipboard.primaryClip = clip
            //Prevents multiple times toast issue with the button
            Toast.makeText(it, it.resources.getString(R.string.copy_to_clipboard, item?.title),
                    Toast.LENGTH_SHORT).show()
        } ?: Log.e("ItemClickHandler","copyToClipboard error...")
    }

    // Request permission for IMEI/MEID for Android M+
    private fun getPermissionClickAdapter(MY_PERMISSION: UnavailablePermission?, itemTitle: String?) {
        val permission: String
        when (MY_PERMISSION) {
            UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> permission = Manifest.permission.READ_PHONE_STATE
            else -> return
        }

        activity?.let {
            if (ContextCompat.checkSelfPermission(it, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, permission)) {
                    if (it.findViewById<View>(android.R.id.content) != null) {
                        Snackbar.make(it.findViewById<View>(android.R.id.content),
                                it.resources.getString(R.string.phone_permission_snackbar, itemTitle),
                                Snackbar.LENGTH_INDEFINITE).setAction(R.string.phone_permission_snackbar_button,
                                { _ ->
                                    ActivityCompat.requestPermissions(it,
                                            arrayOf(permission), MY_PERMISSION.value)
                                }).show()
                    }
                } else {
                    ActivityCompat.requestPermissions(it, arrayOf(permission), MY_PERMISSION.value)
                }
            }
        }

    }

}
