package com.cwlarson.deviceid.util

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.Item
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.databinding.UnavailablePermission
import com.cwlarson.deviceid.dialog.ItemClickDialogDirections
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

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
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, a.getString(R.string.send_to))
            putExtra(Intent.EXTRA_TEXT, item?.subtitle)
        }
        shareIntent.resolveActivity(a.packageManager)?.let {
            a.startActivity(Intent.createChooser(shareIntent, null))
        } ?: Toast.makeText(a, "No app available", Toast.LENGTH_LONG).show()
        dialog?.dismiss()
        true
        } ?: false

    //Copy to clipboard
    fun onCopyClick(item: Item?) {
        activity?.let {
            val clip = ClipData.newPlainText(item?.title, item?.subtitle)
            it.clipboardManager.setPrimaryClip(clip)
            Toast.makeText(it, it.resources.getString(R.string.copy_to_clipboard, item?.title), Toast.LENGTH_SHORT).show()
        } ?: Timber.e("copyToClipboard error...")
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
                Snackbar.make(snackbarView, message ?: "Unknown error", Snackbar.LENGTH_LONG)
                        .setActionTextColor(ContextCompat.getColor(activity, R.color.imageSecondary))
                        .setAnchorView(R.id.bottom_navigation).show()
            }
        } ?: a.findNavController(R.id.nav_host_fragment).navigate(ItemClickDialogDirections
                .actionGlobalItemClickDialog(item?.title, item?.itemType ?: ItemType.NONE))
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
            val permission: String = when (MY_PERMISSION) {
                UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> Manifest.permission.READ_PHONE_STATE
                UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE -> Manifest.permission.ACCESS_FINE_LOCATION
                else -> return
            }
            if (ContextCompat.checkSelfPermission(a, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(a, permission)) {
                    Snackbar.make(snackbarView,
                            a.getString(R.string.permission_snackbar_retry,
                                    a.packageManager.getPermissionInfo(Manifest.permission.READ_PHONE_STATE, 0)
                                            .loadLabel(a.packageManager).toString(), itemTitle), Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(ContextCompat.getColor(a, R.color.imageSecondary))
                            .setAnchorView(R.id.bottom_navigation)
                            .setAction(R.string.permission_snackbar_button
                    ) { ActivityCompat.requestPermissions(a,
                            arrayOf(permission), MY_PERMISSION.value) }.show()

                } else
                    ActivityCompat.requestPermissions(a, arrayOf(permission), MY_PERMISSION.value)
            }
        }

    }

}
