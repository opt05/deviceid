package com.cwlarson.deviceid.util

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.cwlarson.deviceid.dialog.ItemClickDialog
import com.google.android.material.snackbar.Snackbar

class SearchClickHandler(private val navController: NavController,
                         private val searchView: SearchView) {

    fun onSearchSubmit(query: String?) {
        if(!query.isNullOrBlank() &&
                navController.currentDestination?.id != R.id.search_fragment_dest) {
            navController.navigate(R.id.search_fragment_dest)
        }
    }

    fun onSearchSubmitIntent() {
        if(navController.currentDestination?.id != R.id.search_fragment_dest) {
            navController.navigate(R.id.search_fragment_dest)
        }
    }

    fun onSearchNavigateUp() {
        searchView.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            clearFocus()
        }
        navController.navigateUp()
    }
}

class ItemClickDialogHandler(private val activity: Activity?,
                             private val dialog: AppCompatDialogFragment? = null) {
    fun onShareClick(item: Item?): Boolean = activity?.let {  a ->
        val shareIntent = ShareCompat.IntentBuilder.from(a)
                .setType("text/plain")
                .setChooserTitle(R.string.send_to)
                .setText(item?.subtitle)
                .intent
        shareIntent.resolveActivity(a.packageManager)?.let { a.startActivity(shareIntent) }
        dialog?.dismiss()
        true
        } ?: false

    //Copy to clipboard
    fun onCopyClick(item: Item?) {
        activity?.let {
            val clip = ClipData.newPlainText(item?.title, item?.subtitle)
            it.clipboardManager.primaryClip = clip
            Toast.makeText(it, it.resources.getString(R.string.copy_to_clipboard, item?.title),
                    Toast.LENGTH_SHORT).show()
        } ?: Log.e("ItemClickHandler","copyToClipboard error...")
        dialog?.dismiss()
    }
}

class ItemClickHandler(private val snackbarView: View, private val activity: FragmentActivity?) {
    fun onClick(item: Item?): Boolean = activity?.let { a ->
        item?.unavailableItem?.let {
            if(it.unavailablePermission != null) {
                //Needs permission granted first
                getPermissionClickAdapter(it.unavailablePermission,item.title)
            } else if (it.unavailableType != null) {
                // Unavailable for another reason
                val message = a.resources?.getString(R.string.snackbar_not_found_adapter, item.title)
                Snackbar.make(snackbarView, message ?: "Unknown error",
                        Snackbar.LENGTH_LONG).setAnchorView(R.id.bottom_navigation).show()
            }
        } ?: ItemClickDialog.newInstance(item?.title, item?.itemType).show(
                a.supportFragmentManager, ItemClickDialog.DIALOG_TAG)
        true
    } ?: false

    fun onLongClick(item: Item?): Boolean =
        if(item?.unavailableItem == null) {
            ItemClickDialogHandler(activity).onCopyClick(item)
            true
        } else false

    // Request permission for IMEI/MEID for Android M+
    private fun getPermissionClickAdapter(MY_PERMISSION: UnavailablePermission?, itemTitle: String?) {
        activity?.let {  a ->
            val permission: String
            when (MY_PERMISSION) {
                UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> permission = Manifest.permission.READ_PHONE_STATE
                else -> return
            }
            if (ContextCompat.checkSelfPermission(a, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(a, permission)) {
                    Snackbar.make(snackbarView,
                            a.getString(R.string.permission_snackbar_retry,
                                    a.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                            .loadLabel(a.packageManager).toString(),
                                    itemTitle),
                            Snackbar.LENGTH_INDEFINITE).setAnchorView(R.id.bottom_navigation)
                            .setAction(R.string.permission_snackbar_button
                    ) { ActivityCompat.requestPermissions(a,
                            arrayOf(permission), MY_PERMISSION.value) }.show()

                } else
                    ActivityCompat.requestPermissions(a, arrayOf(permission), MY_PERMISSION.value)
            }
        }

    }

}
