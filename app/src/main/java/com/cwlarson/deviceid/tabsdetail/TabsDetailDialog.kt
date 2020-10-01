package com.cwlarson.deviceid.tabsdetail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.util.calculateBottomSheetMaxWidth
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * A BottomSheetFragment with fixes for tablets
 * Includes: has a max screen width it will occupy & start fully expanded upward
 */
@AndroidEntryPoint
class TabsDetailDialog : BottomSheetDialogFragment() {
    @ExperimentalStdlibApi
    private val tabsDetailViewModel by viewModels<TabsDetailViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.bottom_sheet, container, false)

    @ExperimentalStdlibApi
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var clickItem: Item? = null
        viewLifecycleOwner.lifecycleScope.launch {
            tabsDetailViewModel.detailItem.distinctUntilChanged().collectLatest { item ->
                clickItem = item
                bottom_title.text = item?.getFormattedString(view.context) ?: getString(R.string.not_found)
                bottom_subtitle.text = item?.subtitle?.getSubTitleText()
                        ?: getString(R.string.not_found)
                bottom_button_share.isEnabled = item != null
                bottom_button_copy.isEnabled = item != null
            }
        }
        bottom_button_share.setOnClickListener {
            clickItem?.let { if (activity.shareItem(it)) dialog?.dismiss() }
        }
        bottom_button_copy.setOnClickListener {
            clickItem?.let { if (activity.copyItemToClipboard(it)) dialog?.dismiss() }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            super.onCreateDialog(savedInstanceState).calculateBottomSheetMaxWidth()
}
