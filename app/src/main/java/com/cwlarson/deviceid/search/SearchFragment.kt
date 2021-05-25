package com.cwlarson.deviceid.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwlarson.deviceid.data.Status
import com.cwlarson.deviceid.databinding.FragmentSearchBinding
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.HeaderDecoration
import com.cwlarson.deviceid.tabs.TabsAdapter
import com.cwlarson.deviceid.tabs.handleItemClick
import com.cwlarson.deviceid.tabsdetail.copyItemToClipboard
import com.cwlarson.deviceid.util.applySystemWindows
import com.cwlarson.deviceid.util.registerRequestPermission
import com.cwlarson.deviceid.util.setAnimatedVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {
    @Inject
    lateinit var preferenceManager: PreferenceManager
    private val searchViewModel by activityViewModels<SearchViewModel>()
    private var refreshJob: Job? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    @ExperimentalCoroutinesApi
    private val registry = registerRequestPermission { granted ->
        if (granted) searchViewModel.refresh()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myAdapter = TabsAdapter { item, isLongClick ->
            if (isLongClick) {
                activity.copyItemToClipboard(item)
            } else {
                handleItemClick(item, binding.topLayout, registry)
            }
        }
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            addItemDecoration(HeaderDecoration(myAdapter), -1)
            applySystemWindows(applyTop = true, applyBottom = true, applyActionBarPadding = true)
        }
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            searchViewModel.status.distinctUntilChanged().collectLatest {
                binding.searchProgress.isVisible = it == Status.LOADING
            }
        }
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            searchViewModel.allItems.distinctUntilChanged().collectLatest {
                myAdapter.submitList(it)
                binding.noItems.root.isVisible = it.isEmpty()
                binding.searchRecyclerView.setAnimatedVisibility(if (it.isNotEmpty()) View.VISIBLE else View.GONE)
                binding.noItems.root.setAnimatedVisibility(if (it.isNullOrEmpty()) View.VISIBLE else View.GONE)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onResume() {
        super.onResume()
        refreshJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val rate = preferenceManager.autoRefreshRate
            while (isActive && rate > 0) {
                delay(TimeUnit.SECONDS.toMillis(rate.toLong()))
                searchViewModel.refresh()
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