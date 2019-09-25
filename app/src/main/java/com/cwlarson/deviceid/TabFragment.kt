package com.cwlarson.deviceid

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.cwlarson.deviceid.database.AllItemsViewModel
import com.cwlarson.deviceid.database.MainActivityViewModel
import com.cwlarson.deviceid.databinding.FragmentTabsBinding
import com.cwlarson.deviceid.util.ItemClickHandler
import com.cwlarson.deviceid.util.MyAdapter
import com.cwlarson.deviceid.util.calculateNoOfColumns
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TabFragment : Fragment(), OnSharedPreferenceChangeListener, CoroutineScope {
    private lateinit var binding: FragmentTabsBinding
    private lateinit var preferences: SharedPreferences
    private var refreshJob: Job? = null
    private val args by navArgs<TabFragmentArgs>()
    private val allItemsViewModel by viewModels<AllItemsViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
            registerOnSharedPreferenceChangeListener(this@TabFragment)
        }
        binding = DataBindingUtil.inflate<FragmentTabsBinding>(inflater, R.layout.fragment_tabs, container, false).apply {
            lifecycleOwner = this@TabFragment
            model = allItemsViewModel.apply {
                setHideUnavailable(preferences.getBoolean(getString(R.string.pref_hide_unavailable_key), false))
                initialize(args.tab)
                refreshDisabled.value = preferences.getInt(getString(R.string.pref_auto_refresh_rate_key),0) > 0
            }
            mainModel = mainActivityViewModel
            val view = activity?.findViewById<CoordinatorLayout>(R.id.coordinator_layout) ?: topLayout
            val myAdapter = MyAdapter(ItemClickHandler(view, activity))
            recyclerView.apply {
                layoutManager = GridLayoutManager(activity, context.calculateNoOfColumns(mainActivityViewModel.twoPane))
                setHasFixedSize(true)
                adapter = myAdapter
            }
            allItemsViewModel.getAllItems()?.observe(viewLifecycleOwner, Observer { items ->
                myAdapter.submitList(items)
                allItemsViewModel.itemsCount.value = items?.size ?: 0
                // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                executePendingBindings()
            })
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (preferences.getInt(getString(R.string.pref_auto_refresh_rate_key),0)).apply {
            if(this@apply > 0)
                launch {
                    while (isActive) {
                        delay(this@apply * 1000L)
                        allItemsViewModel.refreshData(true)
                    }
                }.also { refreshJob = it }
        }
    }
    override fun onPause() {
        super.onPause()
        refreshJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        coroutineContext.cancel()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key) {
            getString(R.string.pref_hide_unavailable_key) -> {
                allItemsViewModel.setHideUnavailable(
                        sharedPreferences.getBoolean(key, false))
            }
            getString(R.string.pref_auto_refresh_rate_key) -> {
                allItemsViewModel.refreshDisabled.value =
                        sharedPreferences.getInt(key, 0) > 0
            }
        }
    }
}
