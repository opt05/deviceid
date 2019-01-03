package com.cwlarson.deviceid

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.cwlarson.deviceid.database.AllItemsViewModel
import com.cwlarson.deviceid.database.Status
import com.cwlarson.deviceid.databinding.FragmentTabsBinding
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.MyAdapter
import com.cwlarson.deviceid.util.calculateNoOfColumns

class TabFragment : Fragment(), OnSharedPreferenceChangeListener {
    private var adapter: MyAdapter? = null
    private var binding: FragmentTabsBinding? = null
    private var preferences: SharedPreferences? = null
    private var itemType: ItemType? = null

    companion object {
        fun newInstance(tabInteger: Int): TabFragment {
            val fragment = TabFragment()
            val bundle = Bundle()
            bundle.putInt("tab", tabInteger)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            when (it.getInt("tab")) {
                0 -> itemType = ItemType.DEVICE
                1 -> itemType = ItemType.NETWORK
                2 -> itemType = ItemType.SOFTWARE
                3 -> itemType = ItemType.HARDWARE
            }
        }
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences?.registerOnSharedPreferenceChangeListener(this)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tabs, container, false)
        binding?.setLifecycleOwner(this)
        binding?.model = ViewModelProviders.of(this).get(AllItemsViewModel::class.java).apply {
            setHideUnavailable(preferences?.getBoolean(PREFERENCE_HIDE_UNAVAILABLE, false) ?: false)
        }
        val mLayoutManager = GridLayoutManager(activity, context.calculateNoOfColumns())
        binding?.recyclerView?.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        adapter = MyAdapter(activity as PermissionsActivity)
        binding?.recyclerView?.adapter = adapter

        // Setup SwipeToRefresh
        binding?.swipeToRefreshLayout?.setOnRefreshListener { binding?.model?.loadData(context, itemType) }
        binding?.swipeToRefreshLayout?.setColorSchemeResources(R.color.accent_color)
        binding?.model?.getAllItems(itemType)?.observe(this, Observer { items ->
            adapter?.submitList(items)
            binding?.model?.itemsCount?.value = items?.size ?: 0
            // Espresso does not know how to wait for data binding's loop so we execute changes sync.
            binding?.executePendingBindings()
        })
        binding?.model?.status?.observe(this, Observer { status ->
            binding?.swipeToRefreshLayout?.isRefreshing = status != null && status === Status.LOADING })
        binding?.model?.loadData(context, itemType)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key) {
            PREFERENCE_HIDE_UNAVAILABLE -> {
                binding?.model?.setHideUnavailable(
                        sharedPreferences.getBoolean(PREFERENCE_HIDE_UNAVAILABLE, false))
            }
        }
    }
}
