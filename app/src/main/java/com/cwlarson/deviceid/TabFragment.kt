package com.cwlarson.deviceid

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.cwlarson.deviceid.database.AllItemsViewModel
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
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
            registerOnSharedPreferenceChangeListener(this@TabFragment)
        }
        binding = DataBindingUtil.inflate<FragmentTabsBinding>(inflater, R.layout.fragment_tabs, container, false).apply {
            lifecycleOwner = this@TabFragment
            model = ViewModelProviders.of(this@TabFragment).get<AllItemsViewModel>().apply {
                setHideUnavailable(preferences.getBoolean(getString(R.string.pref_hide_unavailable_key), false))
                initialize(args.tab)
                refreshDisabled.value = preferences.getInt(getString(R.string.pref_auto_refresh_rate_key),0) > 0
            }
            activity?.let{ mainModel = ViewModelProviders.of(it).get() }
            val myAdapter = MyAdapter(ItemClickHandler(topLayout, activity))
            recyclerView.apply {
                layoutManager = GridLayoutManager(activity, context.calculateNoOfColumns())
                setHasFixedSize(true)
                adapter = myAdapter
            }
            model?.getAllItems()?.observe(this@TabFragment, Observer { items ->
                myAdapter.submitList(items)
                model?.itemsCount?.value = items?.size ?: 0
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
                        binding.model?.refreshData(true)
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
                binding.model?.refreshDisabled?.value =
                        sharedPreferences.getInt(key, 0) > 0
            }
        }
    }
}
