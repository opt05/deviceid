package com.cwlarson.deviceid.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AppUpdateDialog: AppCompatDialogFragment() {
    private val args by navArgs<AppUpdateDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            MaterialAlertDialogBuilder(it)
                    .setTitle(it.getString(args.title))
                    .setMessage(it.getString(args.message))
                    .setPositiveButton(it.getString(args.button)) {
                        dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
        } ?: super.onCreateDialog(savedInstanceState)
    }
}