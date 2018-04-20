package com.cwlarson.deviceid.dialog

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.database.BottomSheetViewModel
import com.cwlarson.deviceid.databinding.BottomSheetBinding
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.ItemClickHandler
import com.cwlarson.deviceid.util.calculateBottomSheetMaxWidth

/**
 * A BottomSheetFragment with fixes for tablets
 * Includes: has a max screen width it will occupy & start fully expanded upward
 */
class ItemClickDialog : BottomSheetDialogFragment() {
    private var binding: BottomSheetBinding? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet, null, false)
        val mModel = ViewModelProviders.of(this).get(BottomSheetViewModel::class.java)
        val title = arguments?.getString("title", "") ?: ""
        val type = arguments?.let {
            ItemType.valueOf(it.getString("type", ItemType.NONE.name))
        } ?: ItemType.NONE
        mModel.getItem(title, type)?.observe(this, Observer { item ->
            binding?.item = item
            binding?.handler = ItemClickHandler(activity, item)
        })
        dialog.setContentView(binding?.root)
        val layoutParams = (binding?.root?.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.setOnShowListener {
            dialog.window?.setLayout(
                    calculateBottomSheetMaxWidth(inflater.context),
                    ViewGroup.LayoutParams.MATCH_PARENT)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {

        fun newInstance(itemTitle: String?, itemType: ItemType?): ItemClickDialog {
            val bundle = Bundle()
            bundle.putString("title", itemTitle)
            bundle.putString("type", itemType?.name)
            val dialog = ItemClickDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}
