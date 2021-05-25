@file:JvmName("ClickHandlers")

package com.cwlarson.deviceid.tabs

import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabsdetail.TabsDetailDialogDirections
import com.cwlarson.deviceid.util.*
import com.google.android.material.snackbar.Snackbar

fun Fragment.handleItemClick(
    item: Item,
    snackbarView: View,
    registry: ActivityResultLauncher<String>
) {
    when (val sub = item.subtitle) {
        is ItemSubtitle.Permission -> {
            if (context?.isGranted(sub.permission) == false) {
                if (shouldShowRationale(sub.permission)) {
                    snackbarView.snackbar(
                        getString(
                            R.string.permission_snackbar_retry,
                            context?.loadPermissionLabel(sub.permission),
                            item.getFormattedString(requireContext())
                        ),
                        Snackbar.LENGTH_INDEFINITE, getString(R.string.permission_snackbar_button)
                    ) {
                        registry.requestPermission(sub.permission)
                    }
                } else registry.requestPermission(sub.permission)
            }
        }
        else -> {
            if (sub?.getSubTitleText().isNullOrBlank()) {
                // Unavailable for another reason
                snackbarView.snackbar(
                    getString(
                        R.string.snackbar_not_found_adapter,
                        item.getFormattedString(requireContext())
                    ), Snackbar.LENGTH_LONG
                )
            } else {
                findNavController().navigate(
                    TabsDetailDialogDirections
                        .actionGlobalItemClickDialog(
                            item.title,
                            item.itemType,
                            item.titleFormatArgs
                        )
                )
            }
        }
    }
}