package com.cwlarson.deviceid.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.database.BottomSheetViewModel
import com.cwlarson.deviceid.databinding.BottomSheetBinding
import com.cwlarson.deviceid.util.ItemClickDialogHandler
import com.cwlarson.deviceid.util.calculateBottomSheetMaxWidth
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A BottomSheetFragment with fixes for tablets
 * Includes: has a max screen width it will occupy & start fully expanded upward
 */
class ItemClickDialog : BottomSheetDialogFragment() {
    private val args by navArgs<ItemClickDialogArgs>()
    private val bottomSheetViewModel by viewModels<BottomSheetViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        DataBindingUtil.inflate<BottomSheetBinding>(LayoutInflater.from(context),
                R.layout.bottom_sheet, null, false).apply {
            lifecycleOwner = viewLifecycleOwner
            handler = ItemClickDialogHandler(activity, this@ItemClickDialog)
            bottomSheetViewModel.setItem(args.title ?: getString(R.string.not_found), args.type)
            model = bottomSheetViewModel
        }.root

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            super.onCreateDialog(savedInstanceState).calculateBottomSheetMaxWidth()
}
