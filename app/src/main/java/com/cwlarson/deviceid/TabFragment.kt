package com.cwlarson.deviceid

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.cwlarson.deviceid.database.AllItemsViewModel
import com.cwlarson.deviceid.database.Status
import com.cwlarson.deviceid.databinding.FragmentTabsBinding
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.MyAdapter
import com.cwlarson.deviceid.util.calculateNoOfColumns
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TabFragment : Fragment(), OnSharedPreferenceChangeListener, CoroutineScope {
    companion object {
        @Suppress("FieldCanBeLocal","unused")
        private const val TAG = "TabFragment"
    }
    private lateinit var binding: FragmentTabsBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var itemType: ItemType
    private var refreshJob: Job? = null
    private val args: TabFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        itemType = when(args.tab) {
            0 -> ItemType.DEVICE
            1 -> ItemType.NETWORK
            2 -> ItemType.SOFTWARE
            3 -> ItemType.HARDWARE
            else -> ItemType.NONE
        }
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
            registerOnSharedPreferenceChangeListener(this@TabFragment)
        }
        binding = DataBindingUtil.inflate<FragmentTabsBinding>(inflater, R.layout.fragment_tabs, container, false).apply {
            lifecycleOwner = this@TabFragment
            model = ViewModelProviders.of(this@TabFragment).get<AllItemsViewModel>().apply {
                setHideUnavailable(preferences.getBoolean(getString(R.string.pref_hide_unavailable_key), false))
            }
            recyclerView.layoutManager = GridLayoutManager(activity, context.calculateNoOfColumns())
            recyclerView.setHasFixedSize(true)
            val adapter = MyAdapter(topLayout, activity)
            recyclerView.adapter = adapter
            // Setup SwipeToRefresh
            swipeToRefreshLayout.run {
                setOnRefreshListener { model?.refreshData(context, itemType) }
                setColorSchemeColors(ContextCompat.getColor(context,R.color.colorSecondary))
                isEnabled = preferences.getInt(getString(R.string.pref_auto_refresh_rate_key),0) <= 0
            }
            model?.getAllItems(itemType)?.observe(this@TabFragment, Observer { items ->
                adapter.submitList(items)
                model?.itemsCount?.value = items?.size ?: 0
                // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                executePendingBindings()
            })
            model?.status?.observe(this@TabFragment, Observer { status ->
                swipeToRefreshLayout.isRefreshing = status != null && status === Status.LOADING
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
                        binding.model?.refreshData(context, itemType, true)
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
                binding.model?.setHideUnavailable(
                        sharedPreferences.getBoolean(key, false))
            }
            getString(R.string.pref_auto_refresh_rate_key) -> {
                binding.swipeToRefreshLayout.isEnabled =
                        sharedPreferences.getInt(key, 0) <= 0
            }
        }
    }
}
