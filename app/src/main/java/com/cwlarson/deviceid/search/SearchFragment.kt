package com.cwlarson.deviceid.search

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.Status
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabs.HeaderDecoration
import com.cwlarson.deviceid.tabs.TabsAdapter
import com.cwlarson.deviceid.tabs.handleItemClick
import com.cwlarson.deviceid.tabsdetail.copyItemToClipboard
import com.cwlarson.deviceid.util.applySystemWindows
import com.cwlarson.deviceid.util.setAnimatedVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @ExperimentalCoroutinesApi
    private val searchViewModel by activityViewModels<SearchViewModel>()
    private var refreshJob: Job? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myAdapter = TabsAdapter { item, isLongClick ->
            if (isLongClick) {
                activity.copyItemToClipboard(item)
            } else {
                handleItemClick(item, top_layout)
            }
        }
        search_recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            addItemDecoration(HeaderDecoration(myAdapter), -1)
            applySystemWindows(applyTop = true, applyBottom = true, applyActionBarPadding = true)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.status.distinctUntilChanged().collectLatest {
                search_progress.isVisible = it == Status.LOADING
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.allItems.distinctUntilChanged().collectLatest {
                myAdapter.submitList(it)
                no_items.isVisible = it.isEmpty()
                search_recycler_view.setAnimatedVisibility(if (it.isNotEmpty()) View.VISIBLE else View.GONE)
                no_items.setAnimatedVisibility(if (it.isNullOrEmpty()) View.VISIBLE else View.GONE)
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
}