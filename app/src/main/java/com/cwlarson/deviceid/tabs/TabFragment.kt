package com.cwlarson.deviceid.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.cwlarson.deviceid.MainActivityViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.Status
import com.cwlarson.deviceid.databinding.FragmentTabsBinding
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabsdetail.copyItemToClipboard
import com.cwlarson.deviceid.util.applySystemWindows
import com.cwlarson.deviceid.util.calculateNoOfColumns
import com.cwlarson.deviceid.util.registerRequestPermission
import com.cwlarson.deviceid.util.setAnimatedVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
open class TabFragment : Fragment() {
    @Inject
    lateinit var preferenceManager: PreferenceManager
    private var refreshJob: Job? = null
    private val tabsViewModel by viewModels<TabsViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private var _binding: FragmentTabsBinding? = null
    private val binding get() = _binding!!
    @ExperimentalCoroutinesApi
    private val registry = registerRequestPermission { granted ->
        if (granted) tabsViewModel.refresh()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager =
                GridLayoutManager(context, calculateNoOfColumns(mainActivityViewModel.twoPane))
            setHasFixedSize(true)
        }
        binding.swipeToRefreshLayout.setColorSchemeColors(
            *resources.getIntArray(R.array.swipe_to_refresh_colors)
        )
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            tabsViewModel.refreshDisabled.collectLatest { binding.swipeToRefreshLayout.isEnabled = !it }
        }
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            mainActivityViewModel.hideSearchBar.collect { hidden ->
                binding.recyclerView.applySystemWindows(
                    applyTop = !hidden, applyBottom = true,
                    applyActionBarPadding = !hidden
                )
            }
        }
        val myAdapter = TabsAdapter { item, isLongClick ->
            if (isLongClick) {
                activity.copyItemToClipboard(item)
            } else {
                handleItemClick(item, binding.topLayout, registry)
            }
        }
        binding.recyclerView.adapter = myAdapter
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            tabsViewModel.allItems.distinctUntilChanged().collectLatest {
                myAdapter.submitList(it)
                binding.recyclerView.setAnimatedVisibility(if (it.isNotEmpty()) View.VISIBLE else View.GONE)
                binding.noItems.root.setAnimatedVisibility(if (it.isNullOrEmpty()) View.VISIBLE else View.GONE)
            }
        }
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            tabsViewModel.status.distinctUntilChanged().collectLatest {
                binding.swipeToRefreshLayout.isRefreshing = it == Status.LOADING
            }
        }
        binding.swipeToRefreshLayout.setOnRefreshListener { tabsViewModel.refresh() }
    }

    @ExperimentalCoroutinesApi
    override fun onResume() {
        super.onResume()
        refreshJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val rate = preferenceManager.autoRefreshRate
            while (isActive && rate > 0) {
                delay(TimeUnit.SECONDS.toMillis(rate.toLong()))
                tabsViewModel.refresh(true)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        refreshJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()
        registry.unregister()
    }
}
