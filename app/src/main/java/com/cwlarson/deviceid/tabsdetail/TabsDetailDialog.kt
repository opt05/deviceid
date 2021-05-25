package com.cwlarson.deviceid.tabsdetail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.databinding.BottomSheetBinding
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.util.calculateBottomSheetMaxWidth
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A BottomSheetFragment with fixes for tablets
 * Includes: has a max screen width it will occupy & start fully expanded upward
 */
@AndroidEntryPoint
class TabsDetailDialog : BottomSheetDialogFragment() {
    private val tabsDetailViewModel by viewModels<TabsDetailViewModel>()
    private var _binding: BottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var clickItem: Item? = null
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            tabsDetailViewModel.detailItem.distinctUntilChanged().collectLatest { item ->
                clickItem = item
                binding.bottomTitle.text = item?.getFormattedString(view.context) ?: getString(R.string.not_found)
                binding.bottomSubtitle.text = item?.subtitle?.getSubTitleText()
                        ?: getString(R.string.not_found)
                binding.bottomButtonShare.isEnabled = item != null
                binding.bottomButtonCopy.isEnabled = item != null
            }
        }
        binding.bottomButtonShare.setOnClickListener {
            clickItem?.let { if (activity.shareItem(it)) dialog?.dismiss() }
        }
        binding.bottomButtonCopy.setOnClickListener {
            clickItem?.let { if (activity.copyItemToClipboard(it)) dialog?.dismiss() }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            super.onCreateDialog(savedInstanceState).calculateBottomSheetMaxWidth()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
