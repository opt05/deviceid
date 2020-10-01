@file:JvmName("ClickHandlers")
package com.cwlarson.deviceid.tabs

import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabsdetail.TabsDetailDialogDirections
import com.cwlarson.deviceid.util.snackbar
import com.google.android.material.snackbar.Snackbar

fun Fragment.handleItemClick(item: Item, snackbarView: View) {
    when (val sub = item.subtitle) {
        is ItemSubtitle.Permission -> {
            if (ContextCompat.checkSelfPermission(requireContext(), sub.androidPermission) != PackageManager
                            .PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(sub.androidPermission)) {
                    snackbarView.snackbar(getString(R.string.permission_snackbar_retry,
                            context?.packageManager?.let { pm ->
                                pm.getPermissionInfo(sub.androidPermission, 0).loadLabel(pm)
                            }, item.getFormattedString(requireContext())),
                            Snackbar.LENGTH_INDEFINITE, getString(R.string.permission_snackbar_button)) {
                        requestPermissions(arrayOf(sub.androidPermission), sub.permission.value)
                    }
                } else
                    requestPermissions(arrayOf(sub.androidPermission), sub.permission.value)
            }
        }
        else -> {
            if (sub?.getSubTitleText().isNullOrBlank()) {
                // Unavailable for another reason
                snackbarView.snackbar(getString(R.string.snackbar_not_found_adapter,
                        item.getFormattedString(requireContext())), Snackbar.LENGTH_LONG)
            } else {
                findNavController().navigate(TabsDetailDialogDirections
                        .actionGlobalItemClickDialog(item.title, item.itemType, item.titleFormatArgs))
            }
        }
    }
}