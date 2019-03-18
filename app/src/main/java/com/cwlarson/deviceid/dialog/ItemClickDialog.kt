package com.cwlarson.deviceid.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.database.BottomSheetViewModel
import com.cwlarson.deviceid.databinding.BottomSheetBinding
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.ItemClickDialogHandler
import com.cwlarson.deviceid.util.calculateBottomSheetMaxWidth
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A BottomSheetFragment with fixes for tablets
 * Includes: has a max screen width it will occupy & start fully expanded upward
 */
class ItemClickDialog : BottomSheetDialogFragment() {
    companion object {
        @JvmStatic
        fun newInstance(itemTitle: String?, itemType: ItemType?): ItemClickDialog =
            ItemClickDialog().apply {
                arguments = Bundle().apply {
                    putString("title", itemTitle)
                    putString("type", itemType?.name)
                }
            }
        const val DIALOG_TAG = "itemClickDialog"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        DataBindingUtil.inflate<BottomSheetBinding>(LayoutInflater.from(context),
                R.layout.bottom_sheet, null, false).apply {
            lifecycleOwner = this@ItemClickDialog
            handler = ItemClickDialogHandler(activity, this@ItemClickDialog)
            model = ViewModelProviders.of(this@ItemClickDialog).get<BottomSheetViewModel>().apply {
                setItem(arguments?.getString("title", null) ?: getString(R.string.not_found),
                        arguments?.let { ItemType.valueOf(it.getString("type", ItemType.NONE.name)) } ?: ItemType.NONE)
            }
        }.root

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                window?.setLayout(context.calculateBottomSheetMaxWidth(),
                        ViewGroup.LayoutParams.MATCH_PARENT)
                findViewById<View?>(R.id.design_bottom_sheet)?.apply {
                    BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
    }
}
