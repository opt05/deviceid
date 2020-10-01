package com.cwlarson.deviceid.tabs

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.cwlarson.deviceid.MainActivityViewModel
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.data.Status
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.tabsdetail.copyItemToClipboard
import com.cwlarson.deviceid.util.applySystemWindows
import com.cwlarson.deviceid.util.calculateNoOfColumns
import com.cwlarson.deviceid.util.setAnimatedVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tabs.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
open class TabFragment : Fragment(R.layout.fragment_tabs) {
    @Inject
    lateinit var preferenceManager: PreferenceManager
    private var refreshJob: Job? = null

    @ExperimentalStdlibApi
    @ExperimentalCoroutinesApi
    private val tabsViewModel by viewModels<TabsViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    @ExperimentalStdlibApi
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.apply {
            layoutManager =
                GridLayoutManager(context, calculateNoOfColumns(mainActivityViewModel.twoPane))
            setHasFixedSize(true)
        }
        swipe_to_refresh_layout.setColorSchemeColors(
            *resources.getIntArray(R.array.swipe_to_refresh_colors)
        )
        viewLifecycleOwner.lifecycleScope.launch {
            tabsViewModel.refreshDisabled.collectLatest { swipe_to_refresh_layout.isEnabled = !it }
        }
        mainActivityViewModel.hideSearchBar.onEach { hidden ->
            recycler_view.applySystemWindows(
                applyTop = !hidden, applyBottom = true,
                applyActionBarPadding = !hidden
            )
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        val myAdapter = TabsAdapter { item, isLongClick ->
            if (isLongClick) {
                activity.copyItemToClipboard(item)
            } else {
                handleItemClick(item, top_layout)
            }
        }
        recycler_view.adapter = myAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            tabsViewModel.allItems.distinctUntilChanged().collectLatest {
                myAdapter.submitList(it)
                recycler_view.setAnimatedVisibility(if (it.isNotEmpty()) View.VISIBLE else View.GONE)
                no_items.setAnimatedVisibility(if (it.isNullOrEmpty()) View.VISIBLE else View.GONE)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            tabsViewModel.status.distinctUntilChanged().collectLatest {
                swipe_to_refresh_layout.isRefreshing = it == Status.LOADING
            }
        }
        swipe_to_refresh_layout.setOnRefreshListener { tabsViewModel.refresh() }
    }

    @ExperimentalStdlibApi
    @ExperimentalCoroutinesApi
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == UnavailablePermission.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE.value ||
            requestCode == UnavailablePermission.MY_PERMISSIONS_REQUEST_LOCATION_STATE.value
        ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // GRANTED: Force new data updates
                tabsViewModel.refresh()
            } /*else {
                // DENIED: We do nothing (it is handled by the ViewAdapter)
            }*/
        }
    }

    @ExperimentalStdlibApi
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
}
