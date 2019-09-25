package com.cwlarson.deviceid

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwlarson.deviceid.database.SearchItemsViewModel
import com.cwlarson.deviceid.databinding.FragmentSearchBinding
import com.cwlarson.deviceid.util.HeaderDecoration
import com.cwlarson.deviceid.util.ItemClickHandler
import com.cwlarson.deviceid.util.MyAdapter

class SearchFragment: Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: FragmentSearchBinding
    private val searchItemsViewModel by activityViewModels<SearchItemsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        preferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
            registerOnSharedPreferenceChangeListener(this@SearchFragment) }
        binding = DataBindingUtil.inflate<FragmentSearchBinding>(inflater, R.layout.fragment_search,
                container, false).apply {
            val view = activity?.findViewById<CoordinatorLayout>(R.id
                    .coordinator_layout) ?: topLayout
            val myAdapter = MyAdapter(ItemClickHandler(view, activity))
            searchRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = myAdapter
                setHasFixedSize(true)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                addItemDecoration(HeaderDecoration(myAdapter), -1)
            }
            lifecycleOwner = this@SearchFragment
            model = searchItemsViewModel.apply {
                setHideUnavailable(preferences.getBoolean(getString(R.string.pref_hide_unavailable_key), false))
                searchItems.observe(viewLifecycleOwner, Observer { itemModels ->
                    myAdapter.submitList(itemModels)
                    itemsCount.value = itemModels?.size ?: 0
                    isLoading.value = false
                    // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                    executePendingBindings()
                })
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            getString(R.string.pref_hide_unavailable_key) -> {
                searchItemsViewModel.setHideUnavailable(
                        sharedPreferences?.getBoolean(key, false) ?: false)
            }
        }
    }
}