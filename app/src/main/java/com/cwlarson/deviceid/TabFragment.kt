package com.cwlarson.deviceid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cwlarson.deviceid.database.AllItemsViewModel
import com.cwlarson.deviceid.database.AppDatabase
import com.cwlarson.deviceid.database.DatabaseInitializer
import com.cwlarson.deviceid.database.Status
import com.cwlarson.deviceid.databinding.FragmentTabsBinding
import com.cwlarson.deviceid.databinding.ItemType
import com.cwlarson.deviceid.util.MyAdapter
import com.cwlarson.deviceid.util.calculateNoOfColumns

class TabFragment : Fragment(), OnSharedPreferenceChangeListener, DatabaseInitializer.OnPopulate {
    private var mAdapter: MyAdapter? = null
    private var binding: FragmentTabsBinding? = null
    private var appContext: Context? = null
    private var mPreferences: SharedPreferences? = null
    private var itemType: ItemType? = null
    private var mModel: AllItemsViewModel? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.appContext = context?.applicationContext
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        mPreferences?.registerOnSharedPreferenceChangeListener(this)

        arguments?.let {
            when (it.getInt("tab")) {
                0 -> itemType = ItemType.DEVICE
                1 -> itemType = ItemType.NETWORK
                2 -> itemType = ItemType.SOFTWARE
                3 -> itemType = ItemType.HARDWARE
            }
        }

        mModel = ViewModelProviders.of(this).get(AllItemsViewModel::class.java)
        mModel?.setHideUnavailable(mPreferences?.getBoolean("hide_unables", false) ?: false)
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after [.onDestroy].
     */
    override fun onDetach() {
        super.onDetach()
        mPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Loads the data into Room
     */
    private fun loadData() {
        appContext?.let {
            DatabaseInitializer.populateAsync(activity, AppDatabase.getDatabase(it),
                    itemType, this@TabFragment)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tabs, container, false)

        val mLayoutManager =
            GridLayoutManager(activity, calculateNoOfColumns(context))
        binding?.recyclerView?.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = MyAdapter(activity as PermissionsActivity)
        binding?.recyclerView?.adapter = mAdapter

        // Setup SwipeToRefresh
        binding?.swipeToRefreshLayout?.setOnRefreshListener { loadData() }
        binding?.swipeToRefreshLayout?.setColorSchemeResources(R.color.accent_color)
        mModel?.getAllItems(itemType)?.observe(this, Observer { items ->
            mAdapter?.setItems(items)
            binding?.itemsCount = items?.size ?: 0
            // Espresso does not know how to wait for data binding's loop so we execute changes sync.
            binding?.executePendingBindings()
        })
        mModel?.status?.observe(this, Observer { status ->
            binding?.swipeToRefreshLayout?.isRefreshing = status != null && status === Status.LOADING })
        loadData()
        return binding?.root
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == "hide_unables") {
            mModel?.setHideUnavailable(sharedPreferences.getBoolean("hide_unables", false))
        }
    }

    override fun status(status: Status) {
        mModel?.setStatus(status)
    }

    companion object {
        fun newInstance(tabInteger: Int): TabFragment {
            val dtf = TabFragment()
            val bundle = Bundle()
            bundle.putInt("tab", tabInteger)
            dtf.arguments = bundle
            return dtf
        }
    }
}
